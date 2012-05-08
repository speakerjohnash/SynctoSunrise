package com.pi.synctosunrise.activities;

import com.pi.synctosunrise.db.SPAdapter;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class AlarmSettingsActivity extends PreferenceActivity {
	
	// Values
	private boolean disableWake;
	private boolean disableSleep;
	private boolean gradualWake;
	private boolean gradualSleep;
	private String wakeURI;
	private String sleepURI;
	
	private Preference wakeURIPref;    
    private Preference sleepURIPref;
    private CheckBoxPreference gradualWakePref;    
    private CheckBoxPreference gradualSleepPref;
    private CheckBoxPreference disableWakePref;    
    private CheckBoxPreference disableSleepPref;
    
    // Data
    private SPAdapter sp;
		
	// ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sync to Sunrise: Alarm Settings");
        
        // Create Data Objects
        sp = new SPAdapter(getApplicationContext());
        final Object data = getLastNonConfigurationInstance();
        
        // Layout
        addPreferencesFromResource(com.pi.synctosunrise.R.xml.alarmsettings);
        setContentView(com.pi.synctosunrise.R.layout.alarmsettingslayout);
        
        //Initialize Values
        
        if (data == null){
            wakeURI = sp.getWakeURI();
            sleepURI = sp.getSleepURI();
            gradualSleep = sp.getSleepGradual();
            gradualWake = sp.getWakeGradual();
            disableWake = sp.getWakeDisabled();
            disableSleep = sp.getSleepDisabled();
        }
        else {
        	final RotateSave rs = (RotateSave) data;
            wakeURI = rs.wakeURI;
            sleepURI = rs.sleepURI;
            gradualSleep = rs.gradualSleep;
            gradualWake = rs.gradualWake;
            disableWake = rs.disableWake;
            disableSleep = rs.disableSleep;
        }
        
        // Get Buttons
        final Button startButton = (Button) findViewById(com.pi.synctosunrise.R.id.savealarmsettings);
        disableWakePref = (CheckBoxPreference) findPreference("disableWakePref");
        disableSleepPref = (CheckBoxPreference) findPreference("disableSleepPref");
        gradualWakePref = (CheckBoxPreference) findPreference("gradualWakePref");
        gradualSleepPref = (CheckBoxPreference) findPreference("gradualSleepPref");
        wakeURIPref = (Preference) findPreference("wakeURIPref");
        sleepURIPref = (Preference) findPreference("sleepURIPref");
        
        // Set Initial Visible Values
        disableWakePref.setChecked(disableWake);
        disableSleepPref.setChecked(disableSleep);
        gradualWakePref.setChecked(gradualWake);
        gradualSleepPref.setChecked(gradualSleep);
        
        // Add Preference Listeners
        // Disable Wake
        disableWakePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		disableWake = disableWakePref.isChecked();
                    return true;
            }
            
        }); 
        
        // Disable Sleep
        disableSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		disableSleep = disableSleepPref.isChecked();
                    return true;
            }
            
        });
        
     // Start Button
        startButton.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
        		
        		// Save Preferences
        		sp.setWakeGradual(gradualWake);
        		sp.setSleepGradual(gradualSleep);
        		sp.setWakeDisabled(disableWake);
        		sp.setSleepDisabled(disableSleep);
        		sp.setWakeURI(wakeURI);
        		sp.setSleepURI(sleepURI);
    
        		// Alert User of Changes
        		Toast.makeText(getApplicationContext(), "Preferences Saved", Toast.LENGTH_LONG).show();
        	}
        	
        });	
        
     // Gradual Wake
        gradualWakePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		gradualWake = gradualWakePref.isChecked();
                    return true;
            }
            
        }); 
        
        // Gradual Sleep
        gradualSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		gradualSleep = gradualSleepPref.isChecked();
                    return true;
            }
            
        });
        
        // Wake URI
        wakeURIPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
            		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone");
            		if(wakeURI != null){
            			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(wakeURI));
            		}
            		else {
            			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            		}
            		startActivityForResult(intent, 0);
                    return true;
            }
            
        });
        
        // Sleep URI
        sleepURIPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		Intent intent = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER);
            		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            		if(sleepURI != null){
            			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sleepURI));
            		}
            		else {
            			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            		}
            		startActivityForResult(intent, 1);
                    return true;
            }
            
        }); 
        
    }
    
    // TODO DIALOG LISTENERS
    
    // TODO ON CREATE DIALOG
    
    // MENU
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
        	// Create Intent and Launch Activity
        	Intent progressIntent = new Intent(this, ViewProgressActivity.class);
        	startActivity(progressIntent);
        	
            return true;
        case com.pi.synctosunrise.R.id.menu_alarm_settings:
            
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

    // Activity Result
    // Sleep URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
        case 0:
        	if (resultCode == RESULT_OK) {
                 Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                 if (uri != null) {
                 wakeURI = uri.toString();
                 }  
             }  
        	break;
        case 1:
        	 if (resultCode == RESULT_OK) {
                 Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                 if (uri != null) {
                 sleepURI = uri.toString();
                 }  
             }  
        	break;
        default: return;	
        }

    }
    
    // Save Data on Rotate
    @Override
    public Object onRetainNonConfigurationInstance() {
    	final RotateSave rs = new RotateSave(disableWake, disableSleep, gradualWake, gradualSleep, wakeURI, sleepURI);
    	return rs;
    }
    
    public class RotateSave{
    	
    	public boolean disableWake;
    	public boolean disableSleep;
    	public boolean gradualWake;
    	public boolean gradualSleep;
    	public String wakeURI;
    	public String sleepURI;
    	
    	public RotateSave(boolean disableWake, boolean disableSleep, boolean gradualWake, boolean gradualSleep, String wakeURI, String sleepURI){
    		this.disableWake = disableWake;
    		this.disableSleep = disableSleep;
    		this.gradualWake = gradualWake;
    		this.gradualSleep = gradualSleep;
    		this.wakeURI = wakeURI;
    		this.sleepURI = sleepURI;
    	}
    	
    }
    
}
