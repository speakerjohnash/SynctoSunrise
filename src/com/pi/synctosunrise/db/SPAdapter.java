/* Adapter for dealing with Shared Preferences */

package com.pi.synctosunrise.db;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

public class SPAdapter {
	
	private Context ctx;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private Calendar today;
	
	public SPAdapter(Context ctx){
		
		this.ctx = ctx;
		settings = ctx.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
		editor = settings.edit();
		today = Calendar.getInstance();
		
	}
	
	// General Functions
	
	public long MillisSinceMidnight(int hours, int minutes){
		long MILLIS_PER_HOUR = 60 * 60 * 1000;
		long MILLIS_PER_MINUTE = 60 * 1000;
		long time = hours * MILLIS_PER_HOUR + minutes * MILLIS_PER_MINUTE;
		return time;
	}
	
	public Calendar getWakeGoalCal(){
		GoalDate date = getGoalDate();
		long goalTime = getGoalWake();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.DAY_OF_MONTH, date.getDay());
		cal.set(Calendar.MONTH, date.getMonth());
		cal.set(Calendar.YEAR, date.getYear());
		cal.add(Calendar.MILLISECOND, (int) goalTime);
		return cal;
	}
	
	public Calendar getSleepGoalCal(){
		GoalDate date = getGoalDate();
		long goalTime = getGoalSleep();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.DAY_OF_MONTH, date.getDay());
		cal.set(Calendar.MONTH, date.getMonth());
		cal.set(Calendar.YEAR, date.getYear());
		cal.add(Calendar.MILLISECOND, (int) goalTime);
		return cal;
	}
	
	public boolean contains(String prefName){
		boolean test = settings.contains(prefName);
		return test;
	}
	
	// Set Goal Settings
	public void setCurrentWake(int hour, int minute){
		long time = MillisSinceMidnight(hour, minute);
		editor.putLong("currentWake", time);
		editor.commit();
	}
	
	public void setGoalWake(int hour, int minute){
		long time = MillisSinceMidnight(hour, minute);
		editor.putLong("goalWake", time);
		editor.commit();
	}
	
	public void setCurrentWake(long time){
		editor.putLong("currentWake", time);
		editor.commit();
	}
	
	public void setCurrentSleep(long time){
		editor.putLong("currentSleep", time);
		editor.commit();
	}
	
	public void setCurrentSleep(int hour, int minute){
		long time = MillisSinceMidnight(hour, minute);
		editor.putLong("currentSleep", time);
		editor.commit();
	}
	
	public void setGoalWake(long goalWake){
		editor.putLong("goalWake", goalWake);
		editor.commit();
	}
	
	public void setGoalSleep(long goalSleep){
		editor.putLong("goalSleep", goalSleep);
		editor.commit();
	}
	
	public void setGoalSleep(int hour, int minute){
		long time = MillisSinceMidnight(hour, minute);
		editor.putLong("goalSleep", time);
		editor.commit();
	}
	
	public void setGoalDate(int dayOfMonth, int month, int year){
		editor.putInt("goalDay", dayOfMonth);
		editor.putInt("goalMonth", month);
		editor.putInt("goalYear", year);
		editor.commit();
	}
	
	public void setGoalDate(GoalDate goalDate){
		editor.putInt("goalDay", goalDate.getDay());
		editor.putInt("goalMonth", goalDate.getMonth());
		editor.putInt("goalYear", goalDate.getYear());
		editor.commit();
	}
	
	public void setMode(boolean syncOrCustom){
		editor.putBoolean("mode", syncOrCustom);
		editor.commit();
	}
	
	public void setMaximumSleep(long time){
		editor.putLong("maxSleep", time);
		editor.commit();
	}
	
	public void setMinimumSleep(long time){
		editor.putLong("minSleep", time);
		editor.commit();
	}
	
	public void setWakeOffset(long offset){
		editor.putLong("wakeOffset", offset);
		editor.commit();
	}
	
	// Get Current Preferences
	public GoalDate getGoalDate(){
		GoalDate date = new GoalDate(settings.getInt("goalDay",today.get(Calendar.DAY_OF_MONTH)), settings.getInt("goalMonth",today.get(Calendar.MONTH)), settings.getInt("goalYear",today.get(Calendar.YEAR)));
		return date;
	}
	
	public long getCurrentWake(){
		long time = settings.getLong("currentWake",0);
		return time;
	}
	
	public long getGoalWake(){
		long time = settings.getLong("goalWake",0);
		return time;
	}
	
	public long getCurrentSleep(){
		long time = settings.getLong("currentSleep",0);
		return time;
	}
	
	public long getGoalSleep(){
		long time = settings.getLong("goalSleep",0);
		return time;
	}
	
	public boolean getMode(){
		boolean mode = settings.getBoolean("mode", true);
		return mode;
	}
	
	public long getMaxSleep(){
		long maxSleep = settings.getLong("maxSleep", 8);
		return maxSleep;
	}
	
	public long getMinSleep(){
		long minSleep = settings.getLong("minSleep", 8);
		return minSleep;
	}
	
	public long getWakeOffset(){
		long wakeOffset = settings.getLong("wakeOffset", 0);
		return wakeOffset;
	}
	
	// Handle Gradients
	
	public void setGradients(int wakeGradient, int sleepGradient){
		editor.putInt("wakeGradient", wakeGradient);
		editor.putInt("sleepGradient", sleepGradient);
		editor.commit();	
	}
	
	public void setWakeGradient(int gradient){
		editor.putInt("wakeGradient", gradient);
		editor.commit();
	}
	
	public void setSleepGradient(int gradient){
		editor.putInt("sleepGradient", gradient);
		editor.commit();
	}
	
	public Gradients getGradients(){
		Gradients gradients = new Gradients(settings.getInt("wakeGradient",0), settings.getInt("sleepGradient",0));
		return gradients;
	}
		
	// Handle Alarm Settings
	// Set
	public void setWakeDisabled(boolean bool){
		editor.putBoolean("disableWake", bool);
		editor.commit();
	}
	
	public void setSleepDisabled(boolean bool){
		editor.putBoolean("disableSleep", bool);
		editor.commit();
	}
	
	public void setWakeGradual(boolean bool){
		editor.putBoolean("gradualWake", bool);
		editor.commit();
	}
	
	public void setSleepGradual(boolean bool){
		editor.putBoolean("gradualSleep", bool);
		editor.commit();
	}
	
	public void setWakeURI(String str){
		editor.putString("wakeURI", str);
		editor.commit();
	}
	
	public void setSleepURI(String str){
		editor.putString("sleepURI", str);
		editor.commit();
	}
	
	// Get
	public boolean getWakeDisabled(){
		boolean bool = settings.getBoolean("disableWake", false);
		return bool;
	}
	
	public boolean getSleepDisabled(){
		boolean bool = settings.getBoolean("disableSleep", false);
		return bool;
	}
	
	public boolean getWakeGradual(){
		boolean bool = settings.getBoolean("gradualWake", false);
		return bool;
	}
	
	public boolean getSleepGradual(){
		boolean bool = settings.getBoolean("gradualSleep", false);
		return bool;
	}
	
	public String getWakeURI(){
		String str = settings.getString("wakeURI", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
		return str;
	}
	
	public String getSleepURI(){
		String str = settings.getString("sleepURI", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
		return str;
	}
	
	// State
	
	public boolean getDayOrNight(){
		// False is night, true is day. Better to return false by default to not throw errors. 
		boolean bool = settings.getBoolean("dayOrNight", false);
		return bool;
	}
	
	public void setDayOrNight(boolean bool){
		editor.putBoolean("dayOrNight", bool);
		editor.commit();
	}
	
	public void setNextSleepAlarm(long time){
		editor.putLong("nextSleepAlarm", time);
		editor.commit();
	}
	
	public long getNextSleepAlarm(){
		long time = settings.getLong("nextSleepAlarm", 0);
		return time;
	}
	
	// Subclasses
	public class GoalDate {
		
		private int dayOfMonth;
		private int month;
		private int year;
		
		public GoalDate (int dayOfMonth, int month, int year) {
			this.dayOfMonth = dayOfMonth;
			this.month = month;
			this.year = year;
		}
		
		public int getDay() {
			return dayOfMonth;
		}
				
		public int getMonth(){
			return month;
		}
		
		public int getYear(){
			return year;
		}
		
		public void setDay(int day) {
			this.dayOfMonth = day;
		}
				
		public void setMonth(int month){
			this.month = month;
		}
		
		public void setYear(int year){
			this.year = year;
		}
		
		
	}
	
	public class Gradients {
		
		private int wakeGradient;
		private int sleepGradient;
		
		public Gradients (int wakeGradient, int sleepGradient) {
			this.wakeGradient = wakeGradient;
			this.sleepGradient = sleepGradient;
		}
		
		public int getWakeGradient() {
			return wakeGradient;
		}
				
		public int getSleepGradient() {
			return sleepGradient;
		}
		
	}
	
}
