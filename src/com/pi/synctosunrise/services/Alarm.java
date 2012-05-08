package com.pi.synctosunrise.services;

import java.io.IOException;

import com.pi.synctosunrise.db.SPAdapter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class Alarm extends Service {
	
	// Get Data Adapters
	private SPAdapter sp;
	
	// Values
	private String uri;
	private boolean disableAlarm;
	private boolean gradualAlarm;
	private String wakeOrSleep;
    private float leftVol = 0f;
    private float rightVol = 0f;
    private Runnable increaseVol;
	
	// Get Players
	private Vibrator vibrator;
	private MediaPlayer mediaPlayer;
	long[] vibratePattern; // TODO Generate Random Vibrate Pattern
	
	@Override
	public void onCreate() {
		super.onCreate();
		sp = new SPAdapter(getApplicationContext());		
	}
	
	@Override
    public void onDestroy() {	
		mediaPlayer.release();
        vibrator.cancel();
        super.onDestroy();	
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		wakeOrSleep = intent.getStringExtra(Intent.EXTRA_TEXT);
		
		// Gradual Alarm
		final Handler h = new Handler();
		
		increaseVol = new Runnable(){
	        public void run(){
	            mediaPlayer.setVolume(leftVol, rightVol);
	            if(leftVol < 1.0f){
	                leftVol += .05f;
	                rightVol += .05f;
	                h.postDelayed(increaseVol, 500);
	            }
	        }
		};
		
		
		// Initialize Values
		if(wakeOrSleep.equals("wake")){
			uri = sp.getWakeURI();
			disableAlarm = sp.getWakeDisabled();
			gradualAlarm = sp.getWakeGradual();
		}
		else if(wakeOrSleep.equals("sleep")){
			uri = sp.getSleepURI();
			disableAlarm = sp.getSleepDisabled();
			gradualAlarm = sp.getSleepGradual();
		}
		
		// Just a Test
		
		// If Alarm is null get default
		if(uri == null){
            // alert is null, using backup
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
            if(uri == null){ 
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();               
            }
        }
		
		// Set Off Repeating Vibration
						
				vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
				 
				long[] pattern = { 0, 300, 300};
			
			if (disableAlarm == false){	
						 
				vibrator.vibrate(pattern, 0);
				
			}

		// Set Off Music	
					
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.setLooping(true);
			
			// Init Volume for Gradual Alarm
			if (gradualAlarm == true){
				mediaPlayer.setVolume(0, 0);
			}
			else {
				mediaPlayer.setVolume(100, 100);
			}
			
			
				try {
					mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri));
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		if (disableAlarm == false){		
				
				mediaPlayer.start();
				
				if (gradualAlarm == true){
		            h.post(increaseVol);
				}
				
		}
					
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
}
