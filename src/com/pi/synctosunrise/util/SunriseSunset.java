/*
 * LOOK AT THIS TO SEE WHY WE DO THINGS
 * http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
 */

package com.pi.synctosunrise.util;

import java.util.Calendar;
import java.lang.Math;
import java.util.TimeZone;

import com.pi.synctosunrise.db.SPAdapter;

import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SunriseSunset {
	
	private double zenith = 90.83;
	private int dayOfYear;
	private double latitude;
	private double longitude;
	private String provider;
	public Location loc;
	private boolean britishEmpire = false;
	private Calendar SunCalendar;
	private SPAdapter sp;
	
	private double three60ify(double number)
	{
		if (number > 360){
			number -= 360;
		}
		else if (number < 0){
			number += 360;
		}
		return number;
	}
	
	public enum SunEnum {
		SUNRISE, SUNSET
	}

	// Constructor
	public SunriseSunset(Calendar cal, SunEnum value, Context ctx) {
				
		//Get Location Object
		LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);

		if (location == null){
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		
		// Get Latitude / Longitude
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		// Get Location from Preferences if unavailable
		sp = new SPAdapter(ctx);
		
		if(latitude == Double.NaN || longitude == Double.NaN){
			latitude = sp.getLatitude();
			longitude = sp.getLongitude();
		}
		
		// Cache Latitude / Longitude
		sp.cacheLocation(latitude, longitude);
				
		// GET HOUR ANGLE
		double hourAngle = longitude / 15;
		
		// Set base time of rise/set
		dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		int defaultTime;
		
		switch (value) {
			case SUNRISE:
				defaultTime = 6;
				break;
			case SUNSET:
				defaultTime = 18;
				break;
			default:
				defaultTime = 6;
		}
		double baseT = (double) dayOfYear + ( ( defaultTime - hourAngle ) / 24 );
			
		// Now we will calculate the mean anomaly of the sun /geek
		double mean = ( 0.9856 * baseT ) - 3.289;
		
		// Calculate true longitude of the sun
		double trueLon = mean + (1.916 * Math.sin(Math.toRadians(mean))) + (0.02 * Math.sin(Math.toRadians(2 * mean))) + 282.634;
		
		// Sometimes L is outside the range of 0 - 360 degrees.
		trueLon = three60ify(trueLon);
		
		// Now we calculate Right Ascension
		double rightAsc = 0.91764 * Math.tan(Math.toRadians(trueLon));
		rightAsc = Math.toDegrees(Math.atan(rightAsc)); 
		
		// Move Right Ascension to correct quadrant
		double lQuad = (Math.floor(trueLon / 90) * 90);
		double raQuad = (Math.floor(rightAsc / 90) * 90);
		rightAsc = rightAsc + (lQuad - raQuad); // rawquads
		
		// Convert right ascension to hours
		rightAsc = rightAsc / 15;
		
		// Find sun's declination
		double sinDec = 0.39782 * Math.sin(Math.toRadians(trueLon));
		double cosDec = Math.asin(sinDec);
		cosDec = Math.cos(cosDec);
			
		// Calculate the sun's hour angle
		double cosh = Math.cos(Math.toRadians(zenith));
		cosh = cosh - (sinDec * (Math.sin(Math.toRadians(latitude))));
		cosh = cosh / (cosDec * Math.cos(Math.toRadians(latitude)));
	
		// If the value of cosh is outside of -1 .. 1, the sun will not rise or set
		// You are in the arctic circle.
		if(cosh > 1 || cosh < -1) {
			britishEmpire = true;
		}
		
		double H;
		
		switch(value) {
			case SUNRISE:
				H = 360 - Math.toDegrees(Math.acos(cosh));
				break;
			case SUNSET:
				H = Math.toDegrees(Math.acos(cosh));
				break;
			default:
				H = 360 - Math.toDegrees(Math.acos(cosh));
		}
		
		// Convert hours to hours
		H = H / 15;
				
		// Just go crazy with it
		double time = H + rightAsc - (0.06571 * baseT) - 6.622;		
		
		// Universal Time
		double UT = time - hourAngle;
		
		if (UT < 0) {
			UT += 24;
		}
		else if (UT > 24) {
			UT -= 24;
		}
	
		// Convert To Local Time
		TimeZone tZone = cal.getTimeZone();
		double offset = (double) tZone.getRawOffset()/ 3600000;		
		UT += offset;
		
		if (UT < 0){
			UT += 23;
		}
			
		// Set SunCalendar
		SunCalendar = Calendar.getInstance();
		SunCalendar.clear();
		int UTMins = (int) Math.floor((UT * 60)) % 60; 
		int UTHours = (int) UT / 1;
		
		SunCalendar.set(Calendar.HOUR_OF_DAY, (int) UTHours);
		SunCalendar.set(Calendar.MINUTE, (int) UTMins);
		
	}
	
	public Calendar getSunCalendar() {
		return SunCalendar;
	}
	
	public boolean getBritishEmpire() {
		return britishEmpire;
	}
	
	
}
