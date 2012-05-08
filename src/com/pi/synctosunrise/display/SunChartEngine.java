package com.pi.synctosunrise.display;

import java.util.Calendar;

import com.pi.synctosunrise.db.DBAdapter;
import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.util.SunriseSunset;

import android.content.Context;
import android.graphics.Path;

public class SunChartEngine {
	
	private Context ctx;
	private Calendar today;
	private DBAdapter db;
	private SPAdapter sp;
	
	
	public SunChartEngine(Context ctx, int goalID, int screenWidth, int screenHeight){
		
		this.ctx = ctx;
		today = Calendar.getInstance();
		sp = new SPAdapter(ctx);
		db = new DBAdapter(ctx);
		
	}
	
	public Path getSleepPath(){
		return null;
	}
	
	public Path getWakePath(){
		return null;
	}
	
	public void getBounds(){
		
	}
	
	public Path getSunsetPath(){
		return null;
	}

	public Path getSunrisePath(){
		return null;
	}
	
	public void getResolution(){
		
	}
	
}
