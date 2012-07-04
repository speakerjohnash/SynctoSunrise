package com.pi.synctosunrise.services;

import java.util.Calendar;

import com.pi.synctosunrise.db.DBAdapter;
import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.util.AlarmLogic;
import com.pi.synctosunrise.util.SunriseSunset;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AlarmSave extends IntentService {
	
	public AlarmSave() {
		super("AlarmSave");
	}

	private DBAdapter db;
	private SPAdapter sp;
	private AlarmLogic al;
	private long millisInDay = 1000 * 60 * 60 * 24;
	private Calendar cal;
	private Calendar today;
	private Calendar tmrw;
	private SunriseSunset sunrise;
	private SunriseSunset sunset;
	private Calendar sunriseCal;
	private Calendar sunsetCal;
	private long sleepLength;
	private long wakeLength;
	private long alarmTime;
	private long wakeOffset;
	private long minSleep;
	private long maxSleep;
	private long nextSleepAlarm;
	private boolean mode;
	private String wakeOrSleep;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Data
		db = new DBAdapter(getApplicationContext());
		sp = new SPAdapter(getApplicationContext());
		al = new AlarmLogic(getApplicationContext());
		
		// Calls
		today = Calendar.getInstance();
		tmrw = Calendar.getInstance();
		tmrw.add(Calendar.DATE,1);
		sunrise = new SunriseSunset(tmrw, SunriseSunset.SunEnum.SUNRISE, getApplicationContext());
		sunset = new SunriseSunset(tmrw, SunriseSunset.SunEnum.SUNSET, getApplicationContext());
		sunriseCal = sunrise.getSunCalendar();
		sunsetCal = sunset.getSunCalendar();
		
		//Values
		wakeOffset = sp.getWakeOffset();
		mode = sp.getMode();
		minSleep = sp.getMinSleep();
		maxSleep = sp.getMaxSleep();
		wakeLength = sunsetCal.getTimeInMillis() - sunriseCal.getTimeInMillis();
		sleepLength = millisInDay - wakeLength;
		
		
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		// Open Database
		db.open();
		
		wakeOrSleep = intent.getStringExtra(Intent.EXTRA_TEXT);
		alarmTime = today.getTimeInMillis();
		
		// Save Alarm
		if(wakeOrSleep.equals("wake")){
			db.insertWake(alarmTime);
			sp.setDayOrNight(true);
		}
		else if (wakeOrSleep.equals("sleep")){
			db.insertSleep(alarmTime);
			sp.setDayOrNight(false);
		}
		
		// Close Database
		db.close();
		
		// Start Next Alarm
		// SYNC TO SUNRISE
		if (mode == true){
			// Wake
			if(wakeOrSleep.equals("wake")){
				// If Goal Passed
				if (today.after(sp.getWakeGoalCal())){
					
					Calendar c = sunrise.getSunCalendar();
					
					// Add Wake Offset
					c.add(Calendar.MILLISECOND, (int) wakeOffset);
					
					// Set Alarm 
					al.setAlarm(c, AlarmLogic.WakeSleepEnum.WAKE);
				}
				else {
					// Still Moving Towards Goal
					Calendar cal = al.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, false);
					al.setAlarm(cal, AlarmLogic.WakeSleepEnum.WAKE);
				}
			}
			
			// Sleep
			else if(wakeOrSleep.equals("sleep")){
				// If Goal Passed
				if (today.after(sp.getSleepGoalCal())){
				    Calendar c = sunset.getSunCalendar();
					Calendar c2 = sunrise.getSunCalendar();
					
					// Check if MaxSleep or MinSleep Violated
					if (maxSleep!= 0 || minSleep!=0){
						if (maxSleep <= sleepLength){
							c2.add(Calendar.MILLISECOND, (int) (millisInDay - maxSleep));
						}
						if (minSleep >= sleepLength){
							c2.add(Calendar.MILLISECOND, (int) (millisInDay - minSleep));
						}
						al.setAlarm(c2, AlarmLogic.WakeSleepEnum.SLEEP);
						
						// Cache Sleep Alarm Value
						nextSleepAlarm = c2.getTimeInMillis();
						sp.setNextSleepAlarm(nextSleepAlarm);
						
					}
					
					// Otherwise just set alarm
					else {
						al.setAlarm(c, AlarmLogic.WakeSleepEnum.SLEEP);
						// Cache Sleep Alarm Value
						nextSleepAlarm = c.getTimeInMillis();
						sp.setNextSleepAlarm(nextSleepAlarm);
					}
					
				}
				else {
					// Still Moving Towards Goal
					Calendar nextCal = al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, false);
					al.setAlarm(nextCal, AlarmLogic.WakeSleepEnum.SLEEP);
					// Cache Sleep Alarm Value
					nextSleepAlarm = nextCal.getTimeInMillis();
					sp.setNextSleepAlarm(nextSleepAlarm);
				}
			}
		}
		
		// CUSTOM SCHEDULE
		else if (mode == false){
			if(wakeOrSleep.equals("wake")) al.setAlarm(al.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, false), AlarmLogic.WakeSleepEnum.WAKE);
			else if(wakeOrSleep.equals("sleep")) {
				al.setAlarm(al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, false), AlarmLogic.WakeSleepEnum.SLEEP);
				nextSleepAlarm = al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, false).getTimeInMillis();
				sp.setNextSleepAlarm(nextSleepAlarm);
			}
		}	
					
	} 

}
