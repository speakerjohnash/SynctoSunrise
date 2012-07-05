package com.pi.synctosunrise.receivers;

import java.util.Calendar;

import com.pi.synctosunrise.display.DayArcView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews;


public class DayArcWidget extends AppWidgetProvider {
	
	private DayArcView arcView;
	private int widgetSize;
	
	// Sending this broadcast intent will cause the widget to update
	public static String ACTION_CLOCK_UPDATE = "com.pi.synctosunrise.ACTION_CLOCK_UPDATE";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		Log.d("synctosunrise", "entered onEnabled");
		startDisplay(context);
		arcView = new DayArcView(context);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(createUpdate(context));
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		// Process Intent
		final String action = intent.getAction();
		
		if (ACTION_CLOCK_UPDATE.equals(action) ||
				Intent.ACTION_TIME_CHANGED.equals(action) ||
				Intent.ACTION_TIMEZONE_CHANGED.equals(action)){
			final ComponentName appWidgets = new ComponentName(context.getPackageName(), getClass().getName());
			final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			final int ids[] = appWidgetManager.getAppWidgetIds(appWidgets);
			if (ids.length > 0){
				onUpdate(context, appWidgetManager, ids);
			}
		}	
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
				
		final RemoteViews rv = new RemoteViews(context.getPackageName(), com.pi.synctosunrise.R.layout.widget);
		
		if (arcView == null){
						
			arcView = new DayArcView(context);

			final int s = arcView.getSuggestedMinimumHeight();
						
			arcView.setDrawingCacheEnabled(true);
			arcView.measure(s,s);
			arcView.layout(0, 0, s, s);
			widgetSize = s;
						
		}
		
		Bitmap cached = arcView.getDrawingCache(true);
		
		if (cached == null) {
			Log.d("com.pi.synctosunrise", "cached was null onUpdate");
		}
		
		if (cached != null){
			Log.d("com.pi.synctosunrise", "cached was not null onUpdate");
			rv.setImageViewBitmap(com.pi.synctosunrise.R.id.arc, cached);
		}
		
		appWidgetManager.updateAppWidget(appWidgetIds, rv);
		
	}
	
	// This schedules the widget to update every 10 minutes
	private void startDisplay(Context context){
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Schedules updates so they occur on the top of the minute
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.MINUTE, 1);
		alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), 1000 * 60 * 15, createUpdate(context));
	}
	
	// This creates an intent to update the widget
	private PendingIntent createUpdate(Context context){
		return PendingIntent.getBroadcast(context, 0,
				new Intent(ACTION_CLOCK_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
