package com.pi.synctosunrise.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LoadActivity extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assess whether this is the users first time opening Pi //
	    
	    //Load Shared Preferences -  If a preferences file by this name does not exist, it will be created when you retrieve an editor and commit the changes
	    SharedPreferences settings = getSharedPreferences("MyPreferences", MODE_PRIVATE);
	    
	    // Create Intent to load Activity based on whether this is first time or not
	    Intent loadIntent;
	    if (settings.getBoolean("NotFirstTime", false) == false) {
	       loadIntent = new Intent(this, SetGoalActivity.class);
	       
	       // Set FirstTime preference to true
	       SharedPreferences.Editor prefEditor = settings.edit();
		   prefEditor.putBoolean("NotFirstTime", true); 
		   prefEditor.commit();
		   
	    } else {
	       loadIntent = new Intent(this, ViewProgressActivity.class);
	    }
	    
	    // Call appropriate Activity using the Intent we created
	    startActivity(loadIntent);
	    
	    // Close the currently open activity
	    
	    finish();
	    
	    // Make sure to not call setContentView() as we don't was this Activity to have a UI
        
        
    }
    
    
}