package com.pi.synctosunrise.activities;

import java.text.DateFormat;
import java.util.Calendar;

import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.util.AlarmLogic;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ViewProgressActivity extends PreferenceActivity {
			
	// ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sync to Sunrise: View Progress");
             
        // TEMP STUFF
        addPreferencesFromResource(com.pi.synctosunrise.R.xml.tempviewprogress);
        setContentView(com.pi.synctosunrise.R.layout.tempviewprogresslayout);

        // Get Next Alarms
        AlarmLogic al = new AlarmLogic(getApplicationContext());
        SPAdapter sp = new SPAdapter(getApplicationContext());
        Calendar nextWake = al.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, true);
        Calendar nextSleep = al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, true);
        Calendar goalDate = sp.getWakeGoalCal();
        Calendar today = Calendar.getInstance();
        long millisUntilGoal = goalDate.getTimeInMillis() - today.getTimeInMillis();
        long millisInDay = 1000 * 60 * 60 * 24;
        int daysUntilGoal = (int) (millisUntilGoal / millisInDay);
        if (daysUntilGoal <= 1) daysUntilGoal = 0;
               
        // Get Preferences
        Preference nextWakeAlarmPref = (Preference) findPreference("nextWakeAlarmPref");
        Preference nextSleepAlarmPref = (Preference) findPreference("nextSleepAlarmPref");
        Preference daysLeftPref = (Preference) findPreference("daysLeftPref");
        
        nextWakeAlarmPref.setSummary("Your next wake alarm will go off at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(nextWake.getTime()));
        nextSleepAlarmPref.setSummary("Your next sleep alarm will go off at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(nextSleep.getTime()));
        daysLeftPref.setSummary("You are scheduled to reach your new sleep schedule in " + daysUntilGoal + " days");
        
        
     // Get Reconfiguration Object
     	final Object graphData = getLastNonConfigurationInstance();
     		
     // TODO If First Time Call Chart Engine And Return Necessary Objects
     	if (graphData == null){
     		
     	}
     	        
    }
    
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(com.pi.synctosunrise.R.layout.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        case com.pi.synctosunrise.R.id.menu_view_progress:
    
            return true;
            
        case com.pi.synctosunrise.R.id.menu_alarm_settings:
        	// Create Intent and Launch Activity
        	Intent alarmIntent = new Intent(this, AlarmSettingsActivity.class);
        	startActivity(alarmIntent);
        	
            return true;
        
        case com.pi.synctosunrise.R.id.menu_set_goal:
        	// Create Intent and Launch Activity
        	Intent goalIntent = new Intent(this, SetGoalActivity.class);
        	startActivity(goalIntent);
        	
            return true;    
            
        case com.pi.synctosunrise.R.id.menu_email:
        	/* Create the Intent */
        	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        	/* Fill it with Data */
        	emailIntent.setType("plain/text");
        	emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"msevrens@gmail.com"});
        	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Dear Awesome Guys");

        	/* Send it off to the Activity-Chooser */
        	startActivity(Intent.createChooser(emailIntent, "Send mail"));
                
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    // Save Object on Configuration Change (Screen Orient)
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		
 		// TODO Return Graph Data (or Bitmap?)
 		return null;
 	}
    	
}
