package com.pi.synctosunrise.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.ContentValues;

public class DBAdapter {

		// Days Database Column Definitions
		public static final String KEY_ROWID = "_id";
		public static final String KEY_GOALWAKE = "goal_wake";
		public static final String KEY_ACTUALWAKE = "actual_wake";
		public static final String KEY_GOALSLEEP = "goal_sleep";
		public static final String KEY_ACTUALSLEEP = "actual_sleep";
		public static final String KEY_GOALID = "goal_id_fk";
		
		
		// Goals Database Column Definitions
		public static final String KEY_STARTGOALTIME = "start_goal_time";
		public static final String KEY_ENDGOALTIME = "end_goal_time";
		public static final String KEY_WAKEGRADIENT = "wake_gradient";
		public static final String KEY_SLEEPGRADIENT = "sleep_gradient";
		
		// Database Meta Info
		private static final String DATABASE_NAME = "synctosunrise.db";
		private static final int DATABASE_VERSION = 1;
		private static final String TABLE_DAYS = "days";
		private static final String TABLE_GOALS = "goals";
		
		// Query For Most Recent Row
		private static final String CURRENT_DAY = "SELECT _id FROM days WHERE actual_wake ISNULL OR actual_sleep " +
				"ISNULL ORDER BY _id DESC LIMIT 1";
				
		// Create Database SQL strings
		private static final String CREATE_TABLE_DAYS = 
				"CREATE TABLE days (_id INTEGER primary key autoincrement, "
				+ "goal_wake INTEGER, actual_wake INTEGER, "
				+ "goal_sleep INTEGER, actual_sleep INTEGER, goal_id INTEGER, FOREIGN KEY(goal_id) REFERENCES goals(_id));";
		
		private static final String CREATE_TABLE_GOALS = 
				"CREATE TABLE goals (_id INTEGER primary key autoincrement, "
				+ "start_goal_time INTEGER, end_goal_time INTEGER, wake_gradient INTEGER, sleep_gradient INTEGER);";
		
		// Create Objects
		private final Context context;
		private DatabaseHelper DBHelper;
		private SQLiteDatabase db;
		
		// DBAdapter Constructor
		public DBAdapter(Context ctx){
		    this.context = ctx;
		    DBHelper = new DatabaseHelper(context);
		}

		private static class DatabaseHelper extends SQLiteOpenHelper{
			
			// Call Super Class Constructor
			DatabaseHelper(Context context){
			        super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
			
		    @Override
		    public void onCreate(SQLiteDatabase db){
		        
		    	// Create Database (each table created needs own execSQL call)
		    	db.execSQL(CREATE_TABLE_DAYS);
		    	db.execSQL(CREATE_TABLE_GOALS);
		    	
		        // Create New Row to Ensure Alarms Have Something to Update
				db.insert(TABLE_DAYS, KEY_ACTUALWAKE, null);
		    }

		    @Override
		    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		           Log.w("Pi", "Upgrading database from version " + oldVersion
		                 + " to "
		                 + newVersion + ", which will destroy all old data");
		           db.execSQL("DROP TABLE IF EXISTS days");
		           db.execSQL("DROP TABLE IF EXISTS goals");
		           onCreate(db);
		    }
			
		}
		
		// Open Database
		public DBAdapter open() throws SQLException
		{
		    db = DBHelper.getWritableDatabase();
		    return this;
		}

		// Close Database
		public void close()
		{
		    DBHelper.close();
		}
		
		// Insert Wake Time
		public void insertWake(long time)
		{
			
			// Create New Row
			db.insert(TABLE_DAYS, KEY_ACTUALWAKE, null);
			
			// Return Cursor Object at Most Recent Day
			Cursor cursor = db.rawQuery(CURRENT_DAY, null);
			
			// Move Cursor to First row
			cursor.moveToFirst();
			
			// Grab Row Id - Number is columnIndex
			int id = cursor.getInt(0);
			
			// Build Query
			String whereClause = "_id=" + Integer.toString(id);
			ContentValues values = new ContentValues();
			values.put("actual_wake", time);
			
			// Add to Database
			db.update("days", values, whereClause, null);
			
			// Close cursor
			cursor.close();
			
		}

		// Insert Sleep Time
		public void insertSleep(long time)
		{
			// Return Cursor Object at Most Recent Day
			Cursor cursor = db.rawQuery(CURRENT_DAY, null);
			
			// Move Cursor to First row
			cursor.moveToFirst();
						
			// Grab Row Id - Number is columnIndex
			int id = cursor.getInt(0);
						
			// Build Query
			String whereClause = "_id=" + Integer.toString(id);
			ContentValues values = new ContentValues();
			values.put("actual_sleep", time);
						
			// Add to Database
			db.update("days", values, whereClause, null);
			
			// Close cursor
			cursor.close();
			
		}
		
		// Get All Days from Database
		public Cursor getAllDays()
		{
		    Cursor cursor = db.rawQuery("SELECT * FROM days", null);
		    
		    // Move Cursor to First row
		 	cursor.moveToFirst();
		 			
		    return cursor;
		}
		
		// Get All Wake Times from Database
		public Cursor getAllWake()
		{
		    Cursor cursor = db.rawQuery("SELECT _id, actual_wake FROM days", null);
		    
		    // Move Cursor to First row
		 	cursor.moveToFirst();
		 			
		    return cursor;
		}
		
		// Get All Sleep Times from Database
		public Cursor getAllSleep()
		{
		    Cursor cursor = db.rawQuery("SELECT _id, actual_sleep FROM days", null);
		    
		    // Move Cursor to First row
		 	cursor.moveToFirst();
		 			
		    return cursor;
		}

		// Create Dummy Data
		public void addDummyData(int rows, int wakeGradient, int sleepGradient)
		{
			double randomWakeSign;
			double randomSleepSign;
			
			String recentSleepQuery = "SELECT actual_sleep FROM days " +
				"ORDER BY actual_sleep DESC LIMIT 1";
			
			String recentWakeQuery = "SELECT actual_wake FROM days " +
					"ORDER BY actual_wake DESC LIMIT 1";
			
			Cursor recentWakeCursor = db.rawQuery(recentWakeQuery, null);
			recentWakeCursor.moveToFirst();
			long recentWake = recentWakeCursor.getLong(0);
			
			Cursor recentSleepCursor = db.rawQuery(recentSleepQuery, null);
			recentSleepCursor.moveToFirst();
			long recentSleep = recentSleepCursor.getLong(0);
			
			long thisRowSleep;
			long thisRowWake;
			long lastRowSleep = recentSleep;
			long lastRowWake = recentWake;
			
			
			for(int ii = 0; ii < rows; ii++)
			{
				String querbear = "INSERT INTO days (actual_wake,actual_sleep) VALUES ";
				randomWakeSign = Math.random();
				randomSleepSign = Math.random();
				
				long dayOffset = 84600 * 1000;
				
				if(randomSleepSign > 0.5)
					thisRowSleep = (long) (lastRowSleep + ((Math.random() * sleepGradient) * 60000));
				else
					thisRowSleep = (long) (lastRowSleep - ((Math.random() * sleepGradient) * 60000));
				
				if(randomWakeSign > 0.5)
					thisRowWake = (long) (lastRowWake + ((Math.random() * wakeGradient) * 60000));
				else
					thisRowWake = (long) (lastRowWake - ((Math.random() * wakeGradient) * 60000));
				
				//write to the string
				querbear += "(" + Long.toString(thisRowWake) + "," + Long.toString(thisRowSleep) + ")";
				db.execSQL(querbear);

				Log.d("QUEER", querbear);
				
				lastRowSleep = thisRowSleep + dayOffset;
				lastRowWake = thisRowWake + dayOffset;
			}
			
		}
		
		// Insert One Row
		public void insertOneRow(long wake, long sleep){
			String query = "INSERT INTO days (actual_wake,actual_sleep) VALUES " + "(" + Long.toString(wake) + "," + Long.toString(sleep) + ");";
			db.execSQL(query);
		}
		
		// Get Chart Data
		public GraphReturn getChartData(int goalID){
			
			// Get GoalTime at Goal ID
			String timeQuery = "SELECT _id, start_goal_time, end_goal_time FROM goals WHERE _id = " + goalID;
			Cursor cursor = db.rawQuery(timeQuery, null);
			cursor.moveToLast();
			
			// TODO Make sure no error if no goal set
			
			long startGoalTime = cursor.getLong(1);
			long endGoalTime = cursor.getLong(2);
			
			// Get Rows Relevant to that Goal (Only Return Rows With Sleep AND Wake Values)
			String query = "SELECT _id, actual_wake, actual_sleep FROM days WHERE actual_sleep >= " + Long.toString(startGoalTime) + " AND actual_sleep <= " + Long.toString(endGoalTime) + " AND actual_wake > 0";
			
			Cursor cursor2 = db.rawQuery(query, null);
			cursor2.moveToFirst();
			
			// Create Return Object
			GraphReturn graphReturn = new GraphReturn(cursor2, startGoalTime, endGoalTime);
			
			return graphReturn;
		}
		
		// Add Goal Row
		public void insertGoal(long beginGoalTime, long endGoalTime, int wakeGradient, int sleepGradient){
			String query = "INSERT INTO goals (start_goal_time,end_goal_time,wake_gradient,sleep_gradient) VALUES " + "(" + Long.toString(beginGoalTime) + "," + Long.toString(endGoalTime) + "," + Integer.toString(wakeGradient) + "," + Integer.toString(sleepGradient) + ")";
			db.execSQL(query);
		}

		// Multiple Graph Return Values
		
		public class GraphReturn {
			
			private Cursor cursor;
			private long startGoalTime;
			private long endGoalTime;
			
			public GraphReturn (Cursor cursor, long startGoalTime, long endGoalTime) {
				this.cursor = cursor;
				this.startGoalTime = startGoalTime;
				this.endGoalTime = endGoalTime;
			}
			
			public Cursor getCursor() {
				return cursor;
			}
			
			public long getStart() {
				return startGoalTime;
			}
			
			public long getEnd() {
				return endGoalTime;
			}
			
		}

	
}
