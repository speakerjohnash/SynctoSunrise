package com.pi.synctosunrise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ResetAlarmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, com.pi.synctosunrise.services.BootReset.class);  
	    context.startService(i);
	}

}
