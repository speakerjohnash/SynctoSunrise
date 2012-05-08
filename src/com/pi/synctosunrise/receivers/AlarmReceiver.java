package com.pi.synctosunrise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle extras = intent.getExtras();
		String wakeOrSleep = extras.getString(Intent.EXTRA_TEXT);
		
		// Start Alarm Service
		Intent s = new Intent(context, com.pi.synctosunrise.services.Alarm.class);
	    s.putExtra(Intent.EXTRA_TEXT, wakeOrSleep);
	    context.startService(s);
		
	    // Start Alarm Activity
		Intent i = new Intent(context, com.pi.synctosunrise.activities.AlarmActivity.class);  
		i.putExtra(Intent.EXTRA_TEXT, wakeOrSleep);
	    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
	    context.startActivity(i);
	    
	}

}
