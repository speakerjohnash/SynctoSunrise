package com.pi.synctosunrise.activities;

import java.text.DateFormat;
import java.util.Calendar;

import com.pi.synctosunrise.activities.AlarmSettingsActivity.RotateSave;
import com.pi.synctosunrise.db.DBAdapter;
import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.db.SPAdapter.GoalDate;
import com.pi.synctosunrise.db.SPAdapter.Gradients;
import com.pi.synctosunrise.display.HourMinuteDialog;
import com.pi.synctosunrise.display.IconPreferenceScreen;
import com.pi.synctosunrise.util.AlarmLogic;
import com.pi.synctosunrise.util.GetLocation.LocationResult;
import com.pi.synctosunrise.util.GetLocation;
import com.pi.synctosunrise.util.SunriseSunset;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;

public class SetGoalActivity extends PreferenceActivity {
	
	// Dialog IDs
	static final int CURRENT_WAKE_ID = 1;
	static final int CURRENT_SLEEP_ID = 2;
	static final int GOAL_WAKE_ID = 3;
	static final int GOAL_SLEEP_ID = 4;
	static final int GOAL_DATE_ID = 5;
	static final int MAX_SLEEP_ID = 6;
	static final int MIN_SLEEP_ID = 7;
	static final int WAKE_OFFSET_ID = 8;
	
	
	// Values
	private boolean mode;
	private long maxSleep;
	private long minSleep;
	private long wakeOffset;
	private long currentWake;
    private long currentSleep;
    private long goalWake;
    private long goalSleep; 
    private GoalDate goalDate;
    
    // Validate Values
    private boolean gdClicked;
    private boolean cwClicked;
    private boolean gwClicked;
    private boolean gsClicked;
    private boolean csClicked;
    
	
	// Preferences
    Preference currentWakePref;    
    Preference currentSleepPref;
    Preference goalWakePref;
    Preference goalSleepPref;
    CheckBoxPreference syncPref;
    CheckBoxPreference customPref;
    Preference maxSleepPref;
    Preference minSleepPref;
    Preference offsetPref;
    IconPreferenceScreen locationPref;
    Preference goalDatePref;
    PreferenceScreen syncPrefs;
    PreferenceScreen customPrefs;
    PreferenceScreen generalPrefs;
    
    // Data
    private SPAdapter sp;
    private DBAdapter db;
    private AlarmLogic al;
    private SunriseSunset sunrise;
    private SunriseSunset sunset;
    private GetLocation myLocation;
    private Calendar cal;
    private Calendar today;
    private Calendar sunriseCal;
    private Calendar sunsetCal;
      
	// ON CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sync to Sunrise: Set Goal");
               
     // Create Data Objects
        sp = new SPAdapter(getApplicationContext());
        db = new DBAdapter(getApplicationContext());
        al = new AlarmLogic(getApplicationContext());
        final Object data = getLastNonConfigurationInstance();
        myLocation = new GetLocation();
        
     // Backup Database and Store on SD
        db.exportDatabase();
        
        cal = Calendar.getInstance();
        today = Calendar.getInstance();

     // Set Preferences Layout
        addPreferencesFromResource(com.pi.synctosunrise.R.xml.preference);
     	setContentView(com.pi.synctosunrise.R.layout.preferencelayout);
     	
     // TODO Initialize Values 
     	if (data == null){
     		mode = sp.getMode();
         	maxSleep = sp.getMaxSleep();
         	minSleep = sp.getMinSleep();
         	wakeOffset = sp.getWakeOffset();
         	goalDate = sp.getGoalDate();
         	currentWake = sp.getCurrentWake();
         	currentSleep = sp.getCurrentSleep();
         	goalWake = sp.getGoalWake();
         	goalSleep = sp.getGoalSleep();
         	gdClicked = false;
            cwClicked = false;
            gwClicked = false;
            gsClicked = false;
            csClicked = false;
     	}
     	else {
     		final RotateSave rs = (RotateSave) data;
     		mode = rs.mode;
         	maxSleep = rs.maxSleep;
         	minSleep = rs.minSleep;
         	wakeOffset = rs.wakeOffset;
         	goalDate = rs.goalDate;
         	currentWake = rs.currentWake;
         	currentSleep = rs.currentSleep;
         	goalWake = rs.goalWake;
         	goalSleep = rs.goalSleep;
         	gdClicked = rs.gdClicked;
            cwClicked = rs.cwClicked;
            gwClicked = rs.gwClicked;
            gsClicked = rs.gsClicked;
            csClicked = rs.csClicked;
     	}
     	
     	       
     // Get Buttons
        final Button startButton = (Button) findViewById(com.pi.synctosunrise.R.id.startgoal);
        currentWakePref = (Preference) findPreference("currentWakePref");
        currentSleepPref = (Preference) findPreference("currentSleepPref");
        goalWakePref = (Preference) findPreference("goalWakePref");
        goalSleepPref = (Preference) findPreference("goalSleepPref");
        syncPref = (CheckBoxPreference) findPreference("syncPref");
        customPref = (CheckBoxPreference) findPreference("customPref");
        maxSleepPref = (Preference) findPreference("maxSleepPref");
        minSleepPref = (Preference) findPreference("minSleepPref");
        offsetPref = (Preference) findPreference("offsetPref");
        locationPref = (IconPreferenceScreen) findPreference("locationPref");
        goalDatePref = (Preference) findPreference("goalDatePref");
        syncPrefs = (PreferenceScreen) findPreference("syncPrefs");
        generalPrefs = (PreferenceScreen) findPreference("goalPrefs");
        customPrefs = (PreferenceScreen) findPreference("customPrefs");
        
     // Set Icons
        Resources res = getResources();
        Drawable icon = res.getDrawable(com.pi.synctosunrise.R.drawable.location_icon);
        locationPref.setIcon(icon);
        
     // Set Initial Visible Values  
     
        // Mode
        
        // Sync Mode
        if (mode == true){ 
        	syncPref.setChecked(true);
        	customPref.setChecked(false);
        	syncPrefs.setEnabled(true);
        	customPrefs.setEnabled(false);
        }
        
        // Custom Mode
        else if (mode == false){
        	syncPref.setChecked(false);
        	customPref.setChecked(true);
        	syncPrefs.setEnabled(false);
        	customPrefs.setEnabled(true);
        }
        
        refreshPrefs();
                   
     // ADD LISTENERS
             
     // Sync to Sunrise
        syncPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		syncPref.setChecked(true);
            		customPref.setChecked(false);
            		mode = true;
            		
            		// TODO Disable/ Enable Preferences
            		syncPrefs.setEnabled(true);
                	customPrefs.setEnabled(false);

                    return true;
            }
            
        });
        
     // Custom Schedule
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		syncPref.setChecked(false);
            		customPref.setChecked(true);
            		mode = false;
            		
            		// TODO Disable/ Enable Preferences
            		syncPrefs.setEnabled(false);
                	customPrefs.setEnabled(true);
            		
                    return true;
            }
            
        });   
        
     // Update Location
        locationPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            	  locationClick();
                  return true;
            }
            
        });
        
     // Minimum Sleep
        minSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(MIN_SLEEP_ID);
                    return true;
            }
            
        });
        
     // Maximum Sleep
        maxSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(MAX_SLEEP_ID);
                    return true;
            }
            
        });   
        
     // Wake Offset
        offsetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(WAKE_OFFSET_ID);
                    return true;
            }
            
        });
        
     // Current Sleep
        currentSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(CURRENT_SLEEP_ID);
                    return true;
            }
            
        });
        
     // Current Wake
        currentWakePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(CURRENT_WAKE_ID);
                    return true;
            }
            
        });
        
     // Goal Sleep
        goalSleepPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(GOAL_SLEEP_ID);
                    return true;
            }
            
        });
        
     // Goal Wake
        goalWakePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(GOAL_WAKE_ID);
                    return true;
            }
            
        });
        
     // Goal Date
        goalDatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	 
            public boolean onPreferenceClick(Preference preference) {
            		showDialog(GOAL_DATE_ID);    	
                    return true;
            }
            
        });
        
        
     // Start Button
        startButton.setOnClickListener(new View.OnClickListener() {
        	@Override
            public void onClick(View v) {
        		
        		// Validate Data
        		// General
        		
        		if (!sp.contains("goalDay") || !sp.contains("currentWake") || !sp.contains("currentSleep")){
        			if (!gdClicked || !cwClicked || !csClicked){
            			Toast.makeText(getApplicationContext(), "Please set all settings under General before continuing", Toast.LENGTH_LONG).show();
            			return;
            		}
        		}
        		
        		if (!mode){
        			if (!sp.contains("goalWake") || !sp.contains("goalSleep")){
        					if (!gwClicked || !gsClicked){
        						Toast.makeText(getApplicationContext(), "Please set all settings under Custom Schedule before continuing", Toast.LENGTH_LONG).show();
        						return;
        					}
        			}
        		}
        		
            		
        		// Save Preferences
        		sp.setMode(mode);
        		sp.setGoalDate(goalDate);
    			sp.setCurrentWake(currentWake);
    			sp.setCurrentSleep(currentSleep);
        		sp.setMaximumSleep(maxSleep);
        		sp.setMinimumSleep(minSleep);
        		sp.setWakeOffset(wakeOffset);
        		
        		// Alert User of Changes
        		Toast.makeText(getApplicationContext(), "Preferences Saved", Toast.LENGTH_LONG).show();
        		
        		// TODO Save Goal Data to database

        		//SYNC TO SUNRISE
        		if (mode == true){
        			
        			Calendar gd = Calendar.getInstance();
        			gd.set(Calendar.YEAR, goalDate.getYear());
        			gd.set(Calendar.MONTH, goalDate.getMonth());
        			gd.set(Calendar.DAY_OF_MONTH, goalDate.getDay());
        			
        			Log.d("goalDate when start is clicked", gd.toString());
        			
        	        sunrise = new SunriseSunset(gd, SunriseSunset.SunEnum.SUNRISE, getApplicationContext());
        	        sunset = new SunriseSunset(gd, SunriseSunset.SunEnum.SUNSET, getApplicationContext());
        	        sunriseCal = sunrise.getSunCalendar();
        	        sunsetCal = sunset.getSunCalendar();
        	        
        	        // Set  
        	        sunriseCal.add(Calendar.MILLISECOND, (int) wakeOffset);
        	        
        	        // Calculate Offset
        	        long millisInDay = 1000 * 60 * 60 * 24;
        	        long wakeLength = sunsetCal.getTimeInMillis() - sunriseCal.getTimeInMillis();
        	        long sleepLength = millisInDay - wakeLength;
        	        
        	        // Check if MaxSleep or MinSleep Violated
					if (maxSleep!= 0 || minSleep!=0){						
						sunsetCal = (Calendar) sunriseCal.clone();
						
						if (maxSleep <= sleepLength){
							sunsetCal.add(Calendar.MILLISECOND, (int) (millisInDay - maxSleep));
						}
						if (minSleep >= sleepLength){
							sunsetCal.add(Calendar.MILLISECOND, (int) (millisInDay - minSleep));
						}
					}
        	        
        	        // Save Prefs
            		sp.setGoalWake(sunriseCal.get(Calendar.HOUR_OF_DAY), sunriseCal.get(Calendar.MINUTE));
            		sp.setGoalSleep(sunsetCal.get(Calendar.HOUR_OF_DAY), sunsetCal.get(Calendar.MINUTE));
            		
            		// Save Gradients
            		al.getGradient();
            		
            		// Get Alarms
            		Calendar wakeCal = al.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, false);
            		Calendar sleepCal = al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, false);
               		
            		Log.d("wakeCal", wakeCal.toString());
            		Log.d("sleepCal", sleepCal.toString());
            		
            		// Set Alarms
            		al.setAlarm(wakeCal, AlarmLogic.WakeSleepEnum.WAKE);
        			al.setAlarm(sleepCal, AlarmLogic.WakeSleepEnum.SLEEP);
            		      	     
        		}
        		// CUSTOM SCHEDULE 
        		else if (mode == false){        	        			
        			al.setAlarm(al.getNextAlarm(AlarmLogic.WakeSleepEnum.WAKE, false), AlarmLogic.WakeSleepEnum.WAKE);
        			al.setAlarm(al.getNextAlarm(AlarmLogic.WakeSleepEnum.SLEEP, false), AlarmLogic.WakeSleepEnum.SLEEP);
            		sp.setGoalWake(goalWake);
            		sp.setGoalSleep(goalSleep);
        		}

        		
        		// Refresh Preference Summaries
        		refreshPrefs();
        		
        	}
        });	
     
        
    } 
    
    // ON PAUSE
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    // ON STOP
    @Override
    protected void onStop() {
    	super.onStop();
    }

	// MENU
    
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
        	// Create Intent and Launch Activity
        	Intent progressIntent = new Intent(this, ViewProgressActivity.class);
        	startActivity(progressIntent);
        	
            return true;
        case com.pi.synctosunrise.R.id.menu_alarm_settings:
        	// Create Intent and Launch Activity
        	Intent alarmIntent = new Intent(this, AlarmSettingsActivity.class);
        	startActivity(alarmIntent);
        	
            return true;
        
        case com.pi.synctosunrise.R.id.menu_set_goal:

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
    
    // ON CREATE DIALOGS
    
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case GOAL_DATE_ID:
			return new DatePickerDialog(this,
                    goalDateDPL,
                    goalDate.getYear(), goalDate.getMonth(), goalDate.getDay());
		case GOAL_SLEEP_ID:
			return new TimePickerDialog(this, 
					goalSleepTPL, 
					(int) (goalSleep/3600000), (int) ((goalSleep % 3600000) / 60000), false);
		case GOAL_WAKE_ID:
			return new TimePickerDialog(this, 
					goalWakeTPL, 
					(int) (goalWake/3600000), (int) ((goalWake % 3600000) / 60000), false);
		case CURRENT_SLEEP_ID:
			return new TimePickerDialog(this, 
					currentSleepTPL, 
					(int) (currentSleep/3600000), (int) ((currentSleep % 3600000) / 60000), false);
		case CURRENT_WAKE_ID:
			return new TimePickerDialog(this, 
					currentWakeTPL, 
					(int) (currentWake/3600000), (int) ((currentWake % 3600000) / 60000), false);
		case MAX_SLEEP_ID:
			return new HourMinuteDialog(this, 
					maxSleepTPL, 
					(int) (maxSleep/3600000), (int) ((maxSleep % 3600000) / 60000));
		case MIN_SLEEP_ID:
			return new HourMinuteDialog(this, 
					minSleepTPL, 
					(int) (minSleep/3600000), (int) ((minSleep % 3600000) / 60000));
		case WAKE_OFFSET_ID:
			return new HourMinuteDialog(this, 
					wakeOffsetTPL, 
					(int) (wakeOffset/3600000), (int) ((wakeOffset % 3600000) / 60000));	
		}	
		return null;
	}
    
    // DIALOGS
    
    private DatePickerDialog.OnDateSetListener goalDateDPL =
	          new DatePickerDialog.OnDateSetListener() {
	               public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	               
	               // Make Sure Goal Date is After Today	   
	               cal.clear();
	               cal.set(Calendar.YEAR, year);
	               cal.set(Calendar.MONTH, monthOfYear);
	               cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	               
	               if (!cal.after(today)){
	            	   Toast.makeText(getApplicationContext(), "Goal date must be after today!", Toast.LENGTH_LONG).show();
	            	   return;
	               }
	               
	               // Alert Sunrise On that day
	               if (mode == true){
	            	   SunriseSunset ss = new SunriseSunset(cal, SunriseSunset.SunEnum.SUNRISE, getApplicationContext());
	            	   Calendar c = ss.getSunCalendar();
	            	   Toast.makeText(getApplicationContext(), "The sun will rise at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()) + " on this date at your location" ,
	            				Toast.LENGTH_LONG).show();
	            	   
	               }
	               
	            	
	               goalDate.setDay(dayOfMonth);
	               goalDate.setMonth(monthOfYear); 
	               goalDate.setYear(year);
	               
	                // Save Click
					gdClicked = true;

           			// Alert User to Save
           			Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();
	            	   
	         }
	};
	
	private TimePickerDialog.OnTimeSetListener goalSleepTPL = 
	        new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int selectedHour,
							int selectedMinute) {
				
				goalSleep = sp.MillisSinceMidnight(selectedHour, selectedMinute);
				
				// Save Click
				gsClicked = true;

        		// Alert User to Save
        		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

			}
	};
		
		private TimePickerDialog.OnTimeSetListener goalWakeTPL = 
		        new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int selectedHour,
								int selectedMinute) {
					
					goalWake = sp.MillisSinceMidnight(selectedHour, selectedMinute);
					
					// Save Click
					gwClicked = true;

            		// Alert User to Save
            		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

				}
	};
	
	private TimePickerDialog.OnTimeSetListener currentSleepTPL = 
	        new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int selectedHour,
							int selectedMinute) {
				
				currentSleep = sp.MillisSinceMidnight(selectedHour, selectedMinute);
				
				// Save Click
				csClicked = true;
				
        		// Alert User to Save
        		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

			}
	};
		
		private TimePickerDialog.OnTimeSetListener currentWakeTPL = 
		        new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int selectedHour,
								int selectedMinute) {
					
					currentWake = sp.MillisSinceMidnight(selectedHour, selectedMinute);
					
					// Save Click
					cwClicked = true;
					
            		// Alert User to Save
            		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

				}
	};	
	
	private HourMinuteDialog.OnTimeSetListener maxSleepTPL = 
	        new HourMinuteDialog.OnTimeSetListener() {
				public void onTimeSet(int selectedHour,
							int selectedMinute) {
				
				maxSleep = sp.MillisSinceMidnight(selectedHour, selectedMinute);
				
        		// Alert User to Save
        		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

			}
	};
	
	private HourMinuteDialog.OnTimeSetListener minSleepTPL = 
	        new HourMinuteDialog.OnTimeSetListener() {
				public void onTimeSet(int selectedHour,
							int selectedMinute) {
				
				minSleep = sp.MillisSinceMidnight(selectedHour, selectedMinute);
				
        		// Alert User to Save
        		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

			}
	};
	
	private HourMinuteDialog.OnTimeSetListener wakeOffsetTPL = 
	        new HourMinuteDialog.OnTimeSetListener() {
				public void onTimeSet(int selectedHour,
							int selectedMinute) {
				
				wakeOffset = sp.MillisSinceMidnight(selectedHour, selectedMinute);
				
        		// Alert User to Save
        		Toast.makeText(getApplicationContext(), "Preferences aren't final until saved", Toast.LENGTH_LONG).show();

			}
	};
	
	// Functions
	// Update Preferences on Save
	private void refreshPrefs(){
		
		// General Settings
	       cal.clear();
	       cal.add(Calendar.MILLISECOND, (int) currentWake);
	       currentWakePref.setSummary("Current wake time is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime()));
	       
	       cal.clear();
	       cal.add(Calendar.MILLISECOND, (int) currentSleep);
	       currentSleepPref.setSummary("Current sleep time is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime()));
	       
	       cal.clear();
	       cal.add(Calendar.MILLISECOND, (int) goalWake);
	       goalWakePref.setSummary("Goal wake time is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime()));
	       
	       cal.clear();
	       cal.add(Calendar.MILLISECOND, (int) goalSleep);
	       goalSleepPref.setSummary("Goal sleep time is " + DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime()));
	       
	       cal.clear();
	       cal.set(Calendar.DAY_OF_MONTH, goalDate.getDay());
	       cal.set(Calendar.MONTH, goalDate.getMonth());
	       cal.set(Calendar.YEAR, goalDate.getYear());
	       goalDatePref.setSummary("You are currently scheduled to reach this goal on " + DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime()));
	       
	       if (sp.contains("maxSleep")) {
	    	   maxSleepPref.setSummary("You are currently scheduled to sleep at most " + Long.toString(maxSleep/3600000) + " hours and " + Long.toString((maxSleep % 3600000)/ 60000) + " minutes");
	       }
	       
	       else {
	    	   maxSleepPref.setSummary("Maximum sleep is unset");
	       }
	       
	       if (sp.contains("minSleep")) {
	    	   minSleepPref.setSummary("You are currently scheduled to sleep at least " + Long.toString(minSleep/3600000) + " hours and " + Long.toString((minSleep % 3600000)/ 60000) + " minutes");
	       }
	       
	       else {
	    	   minSleepPref.setSummary("Minimum sleep is unset");
	       }
	       
	       if (sp.contains("wakeOffset")){
	    	   offsetPref.setSummary("You are currently scheduled to wake up " + Long.toString(wakeOffset/3600000) + " hours and " + Long.toString((wakeOffset % 3600000)/ 60000) + " minutes " + (wakeOffset <= 0 ? "before" : "after") + " sunrise");
	       }
	       else {
	    	   offsetPref.setSummary("Wake offset is unset");
	       }
	       
	}
	
	// Location
	
	private void locationClick() {
	    myLocation.getLocation(this, locationResult);
	}

	private LocationResult locationResult = new LocationResult(){
	    @Override
	    public void gotLocation(final Location location){
	    	Toast.makeText(getApplicationContext(), "Your current location is " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();
	    }
	};
	
	// Save Data on Rotate
    @Override
    public Object onRetainNonConfigurationInstance() {
    	final RotateSave rs = new RotateSave(mode, maxSleep, minSleep, wakeOffset, currentWake, currentSleep, goalWake, goalSleep, goalDate, gdClicked, cwClicked, gwClicked, gsClicked, csClicked);
    	return rs;
    }
    
    // Save Data on Configuration Change
    public class RotateSave{
    	
    	public boolean mode;
    	public long maxSleep;
    	public long minSleep;
    	public long wakeOffset;
    	public long currentWake;
        public long currentSleep;
        public long goalWake;
        public long goalSleep; 
        public GoalDate goalDate;
        public boolean gdClicked;
        public boolean cwClicked;
        public boolean gwClicked;
        public boolean gsClicked;
        public boolean csClicked;
    	
    	public RotateSave(boolean mode, long maxSleep, long minSleep, long wakeOffset, 
    			long currentWake, long currentSleep, long goalWake, long goalSleep, GoalDate goalDate,
    			boolean gdClicked, boolean cwClicked, boolean gwClicked, boolean gsClicked, boolean csClicked){
    		this.goalDate = goalDate;
    		this.mode = mode;
    		this.maxSleep = maxSleep;
    		this.minSleep = minSleep;
    		this.wakeOffset = wakeOffset;
    		this.currentWake = currentWake;
    		this.currentSleep = currentSleep;
    		this.goalWake = goalWake;
    		this.goalSleep = goalSleep;
    		this.gdClicked = gdClicked;
    		this.cwClicked = cwClicked;
    		this.gwClicked = gwClicked;
    		this.gsClicked = gsClicked;
    		this.csClicked = csClicked;
    	}
    	
    }
	
}
