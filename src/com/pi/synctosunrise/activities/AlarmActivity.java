package com.pi.synctosunrise.activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlarmActivity extends Activity {
	
	private String messageText;
	private String wakeOrSleep;
	protected AlertDialog.Builder alertbox;
	protected DialogInterface mDialog;
	Calendar today;
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get Calendar for right now
        today = Calendar.getInstance();
        
        // Fetch Intent to See if a Wake or Sleep Alarm
        Bundle extras = getIntent().getExtras();
        wakeOrSleep = extras.getString(Intent.EXTRA_TEXT);
        
        if (wakeOrSleep.equals("wake")){
        	messageText = "Wake up bitch";
        }
        else if (wakeOrSleep.equals("sleep")){
        	messageText = "Go the fuck to sleep";
        }
        
     // Start Dialog

        alertbox = new AlertDialog.Builder(this);

        // set the message to display
        alertbox.setMessage(messageText);

        // add a neutral button to the alert box and assign a click listener
        alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

            // click listener on the alert box
            public void onClick(DialogInterface dialog, int which) {
            	
            	Log.d("click registered", "yeah");
            	
            	// Start Alarm Save Service
            	Intent s = new Intent(getApplicationContext(), com.pi.synctosunrise.services.AlarmSave.class);
            	s.putExtra(Intent.EXTRA_TEXT, wakeOrSleep);
            	s.putExtra("Alarm Time", today.getTimeInMillis());
        	    getApplicationContext().startService(s);
        	                	
        	    // Kill Alarm Service
        	    Intent s2 = new Intent(getApplicationContext(), com.pi.synctosunrise.services.Alarm.class);
        	    getApplicationContext().stopService(s2);
            	       	    
        	    dialog.dismiss();
        	    finish();

            }
            
        });

        // Show Alert Box
        alertbox.show();
        
	}
		
	@Override
	public void onDestroy() {
	    super.onDestroy();
	}
	
}
