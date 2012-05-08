package com.pi.synctosunrise.services;

import com.pi.synctosunrise.util.AlarmLogic;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

public class BootReset extends IntentService {
	
	public BootReset() {
		super("BootReset");
		// TODO Auto-generated constructor stub
	}

	private AlarmLogic alarmLogic;
	
	@Override
	public void onCreate() {
		super.onCreate();
		alarmLogic = new AlarmLogic(getApplicationContext());
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		alarmLogic.setAlarm(alarmLogic.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, true), AlarmLogic.WakeSleepEnum.WAKE);
		alarmLogic.setAlarm(alarmLogic.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, true), AlarmLogic.WakeSleepEnum.SLEEP);
	}

}
