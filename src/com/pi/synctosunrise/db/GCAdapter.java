package com.pi.synctosunrise.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.widget.Toast;

public class GCAdapter {
	
	// Data Objects
	private Context ctx;
	private ContentResolver cr;
	
	// Query Strings
	private final String[] CALENDAR_PROJECTION = new String[] {Calendars._ID};
	private final String[] EVENT_PROJECTION = new String[] {Instances.BEGIN, Instances.END, Events.CALENDAR_ID, Events.TITLE};
	private Uri calendarUri = Calendars.CONTENT_URI;
	private Uri eventsUri = Events.CONTENT_URI;
	private Uri instancesUri = Instances.CONTENT_URI;
	
	// Constants
	private final int MILLIS_IN_DAY = 86400000;
	
	public GCAdapter(Context ctx){
		
		this.ctx = ctx;
		cr = ctx.getContentResolver();
		
	}
	
	public Cursor getCalendarIDs(){
		
		String selection = Calendars.ACCOUNT_NAME + " = ? AND " + Calendars.ACCOUNT_TYPE + " = ?";
		String[] selectionArgs = new String[] {"msevrens@gmail.com", "com.google"}; 
		
		Cursor cursor = null;
		cursor = cr.query(calendarUri, CALENDAR_PROJECTION, selection, selectionArgs, null);
		cursor.moveToFirst();
				
		return cursor;
		
	}
			
	public Cursor getTodaysEvents(){
		
		Cursor eCursor = null;
		Cursor cCursor = null;
		
		Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
 		
		long morning = today.getTimeInMillis();
		long night = morning + MILLIS_IN_DAY;
		
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, morning);
		ContentUris.appendId(builder, night);
		
		cCursor = getCalendarIDs();
		List<String> selectionArgsList = new ArrayList<String>();
		
		while(cCursor.isAfterLast() == false){
			selectionArgsList.add(cCursor.getString(0));
			cCursor.moveToNext();
		}
		
		String[] selectionArgs = (String[]) selectionArgsList.toArray(new String[selectionArgsList.size()]);
				
		String selection = "(";
		
		for(int i = 0, len = cCursor.getCount(); i < len; i++){
			selection += Events.CALENDAR_ID + " = ? or ";
		}
		
		selection = selection.substring(0, selection.length() - 4);
		selection += ")";
			
		// Run Query
		eCursor = cr.query(builder.build(), EVENT_PROJECTION, selection, selectionArgs, null);
		eCursor.moveToFirst();
			
		return eCursor; 
		
	}

}
