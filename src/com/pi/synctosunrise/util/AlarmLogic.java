package com.pi.synctosunrise.util;

import java.text.DateFormat;
import java.util.Calendar;

import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.db.SPAdapter.GoalDate;
import com.pi.synctosunrise.db.SPAdapter.Gradients;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class AlarmLogic {
	
	private Context ctx;
	private SPAdapter adapter;
	private Handler mHandler;
	
	public AlarmLogic(Context ctx){
		
		this.ctx = ctx;
		adapter = new SPAdapter(ctx);
		mHandler = new Handler();
		
		
	}
	
	// Wake / Sleep Enum
	
	public enum WakeSleepEnum {
		WAKE, SLEEP
	}
	
	// Get Gradient 
	
	public Gradients getGradient(){
				
		// Initialize Gradient Variable
		long wakeGradient = 0;
		long sleepGradient = 0;
		long millisInDay = 1000 * 60 * 60 * 24;
		
		// Create Calendar Objects
		Calendar goalWakeCal = Calendar.getInstance();
		Calendar currentWakeCal = Calendar.getInstance();
		Calendar goalSleepCal = Calendar.getInstance();
		Calendar currentSleepCal = Calendar.getInstance();
		
		// Get SPAdapter and Load Values into Calendar Object
		int currentWake = (int) adapter.getCurrentWake();
		int currentSleep = (int) adapter.getCurrentSleep();
		int goalWake = (int) adapter.getGoalWake();
		int goalSleep = (int) adapter.getGoalSleep();
		GoalDate goalDay = adapter.getGoalDate();

		// Set Calendars
		//Current Wake
		currentWakeCal.set(Calendar.HOUR_OF_DAY, 0);
		currentWakeCal.set(Calendar.MINUTE, 0);
		currentWakeCal.set(Calendar.SECOND, 0);
		
		// If Irregular sleep schedule (Nocturnal)
		if(goalWake > goalSleep){
		currentWakeCal.add(Calendar.DATE, 1);
		}

		currentWakeCal.add(Calendar.MILLISECOND, currentWake);
		
		// Current Sleep
		currentSleepCal.set(Calendar.HOUR_OF_DAY, 0);
		currentSleepCal.set(Calendar.MINUTE, 0);
		currentSleepCal.set(Calendar.SECOND, 0);
		currentSleepCal.add(Calendar.MILLISECOND, currentSleep);

		// Goal Wake
		goalWakeCal.clear();
		goalWakeCal.set(Calendar.DAY_OF_MONTH, goalDay.getDay());
		goalWakeCal.set(Calendar.MONTH, goalDay.getMonth());
		goalWakeCal.set(Calendar.YEAR, goalDay.getYear());
		goalWakeCal.add(Calendar.MILLISECOND, goalWake);
		
		// Goal Sleep
		goalSleepCal.clear();
		goalSleepCal.set(Calendar.DAY_OF_MONTH, goalDay.getDay());
		goalSleepCal.set(Calendar.MONTH, goalDay.getMonth());
		goalSleepCal.set(Calendar.YEAR, goalDay.getYear());
		goalSleepCal.add(Calendar.MILLISECOND, goalSleep);
	
		// Calculate Time Change
		// Wake
		long wakeDiff = goalWake - currentWake;
			
		if (Math.abs(wakeDiff) >= (millisInDay / 2)){
			if (wakeDiff > 0){
				wakeDiff = wakeDiff - millisInDay;
			}
			
			if (wakeDiff < 0){
				wakeDiff = wakeDiff + millisInDay;
			}			
		}
				
		// Sleep
		long sleepDiff = goalSleep - currentSleep;
		
		Log.d("Sleep Diff before evaluation", Long.toString(sleepDiff));
		
		if (Math.abs(sleepDiff) >= (millisInDay / 2)){
			
			Log.d("Sleep diff is longer than half a day", "yeah");
			
			if (sleepDiff > 0){
				Log.d("Sleep diff is greater than 0", "yeah");
				sleepDiff -= millisInDay;
			}
			
			else if (sleepDiff < 0){
				Log.d("Sleep diff is less than 0", "yeah");
				sleepDiff += millisInDay;
			}			
		}
		
		Log.d("Sleep Diff after evaluation", Long.toString(sleepDiff));
		
		// Calculate Day Difference
		long wakeDayDiff = goalWakeCal.getTimeInMillis() - currentWakeCal.getTimeInMillis();
		long sleepDayDiff = goalSleepCal.getTimeInMillis() - currentSleepCal.getTimeInMillis();
		
		// Calculate Gradient
		wakeGradient = wakeDiff / (wakeDayDiff / millisInDay);
		sleepGradient = sleepDiff / (sleepDayDiff / millisInDay);
		
		Log.d("wakeGradient at end of calculate wakeGradient", Long.toString(wakeGradient));
		Log.d("sleepGradient at end of calculate sleepGradient", Long.toString(sleepGradient));
		
		adapter.setWakeGradient((int) wakeGradient);
		adapter.setSleepGradient((int) sleepGradient);		

		Gradients gradients = adapter.new Gradients((int) wakeGradient, (int) sleepGradient);
		return gradients;
				
	}
	
	// Get Next Alarm
	public Calendar getNextAlarm(WakeSleepEnum value, Boolean onReboot){

		Gradients gradients = adapter.getGradients();
		int gradient;
		long time = 0;
		Calendar cal  = Calendar.getInstance();
		Calendar nextAlarm  = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		Calendar goal = Calendar.getInstance();
		GoalDate gd = adapter.getGoalDate();
		
		// Set Goal Cal to Goal Day
		goal.set(Calendar.DAY_OF_MONTH, gd.getDay());
		goal.set(Calendar.MONTH, gd.getMonth());
		goal.set(Calendar.YEAR, gd.getYear());
		
		// Set Calendars to Midnight
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		nextAlarm.set(Calendar.HOUR_OF_DAY, 0);
		nextAlarm.set(Calendar.MINUTE, 0);
		nextAlarm.set(Calendar.SECOND, 0);
		
		Log.d("Alarm Logic - Cal before mod", cal.toString());
		Log.d("Alarm Logic - Next Alarm before mod", nextAlarm.toString());
		
		
		// If Wake or Sleep, Initialize Variables
		
		switch (value) {
		case WAKE:
			if (today.after(goal)) time = adapter.getGoalWake();
			else time = adapter.getCurrentWake();
			nextAlarm.add(Calendar.MILLISECOND, (int) time);
			gradient = gradients.getWakeGradient();
			break;
		case SLEEP:
			if (today.after(goal)) time = adapter.getGoalSleep();
			else time = adapter.getCurrentSleep();
			nextAlarm.add(Calendar.MILLISECOND, (int) time);
			gradient = gradients.getSleepGradient();
			break;
		default:
			gradient = 0;
		}
		
		//TODO Test
		
		// Check If Goal Date Passed then set Gradient to 0
		if (today.after(goal)){
			gradient = 0;
			adapter.setWakeGradient(0);
			adapter.setSleepGradient(0);
			Log.d("Alarm Logic - getNextAlarm", "goal has passed");
		}
		
		
		// CODE TO RETURN CALENDAR

		if (nextAlarm.after(today)){
			
			// Check for gradient change and Subtract Differences
			if (onReboot == false){
				nextAlarm.add(Calendar.MILLISECOND, gradient);
			}
			
			Log.d("nextAlarm hasn't passed", nextAlarm.toString());
			
			return nextAlarm;
		}
		
		else {
			cal.add(Calendar.DATE,1);
			cal.add(Calendar.MILLISECOND,(int) time);
			
			// Check for Gradient change and Subtract Difference
			if (onReboot == false){
				cal.add(Calendar.MILLISECOND, gradient);
			}
			
			Log.d("nextAlarm already passed, add 1 to date", cal.toString());
			
			return cal;
		}
		
	}
	
	// Set Alarm
	public void setAlarm(Calendar cal, WakeSleepEnum value){
			
		Intent intent;
		PendingIntent pi;
		AlarmManager am;
		final Calendar mCal = cal;
		
		Log.d("setAlarm", cal.toString());
		
		// Create Intent
		switch (value) {
		case WAKE:
			
			intent = new Intent(ctx, com.pi.synctosunrise.receivers.AlarmReceiver.class);
			intent.putExtra(Intent.EXTRA_TEXT, "wake");
			pi = PendingIntent.getBroadcast(ctx, 111, intent, 0);
			am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
			
			// Alert Next Alarm
			mHandler.post(new Runnable() {            
				@Override
				public void run() {
					Toast.makeText(ctx, "Next wake alarm set for " + DateFormat.getTimeInstance(DateFormat.SHORT).format(mCal.getTime()) ,
					        Toast.LENGTH_LONG).show();               
					}
			});
			
			// Reset Preferences
			adapter.setCurrentWake(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			
			break;
		case SLEEP:
			
			intent = new Intent(ctx, com.pi.synctosunrise.receivers.AlarmReceiver.class);
			intent.putExtra(Intent.EXTRA_TEXT, "sleep");
			pi = PendingIntent.getBroadcast(ctx, 222, intent, 0);
			am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
			
			// Alert Next Alarm
			mHandler.post(new Runnable() {            
		        @Override
		        public void run() {
		        	Toast.makeText(ctx, "Next sleep alarm set for " + DateFormat.getTimeInstance(DateFormat.SHORT).format(mCal.getTime()) ,
		        			Toast.LENGTH_LONG).show();               
		        }
		    });
			
			// Reset Preferences
			adapter.setCurrentSleep(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
			
			break;
		default:		
		}
	}
	
	protected Calendar estimateAlarm(Calendar cal, WakeSleepEnum value){
		return null;
	}
	
}
