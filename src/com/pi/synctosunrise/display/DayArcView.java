package com.pi.synctosunrise.display;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.pi.synctosunrise.db.DBAdapter;
import com.pi.synctosunrise.db.GCAdapter;
import com.pi.synctosunrise.db.SPAdapter;
import com.pi.synctosunrise.util.SunriseSunset;

public class DayArcView extends View {
	
	// Data
	private Calendar mCalendar;
	private Calendar sunriseCal;
	private Calendar sunsetCal;
	private SunriseSunset sunrise;
	private SunriseSunset sunset;
	private SPAdapter sp;
    private DBAdapter db;
    private GCAdapter gc;
    private Cursor cursor;
    
    // Values
    private boolean dayOrNight;
    private long currentTime;
    private float currentTimeDegrees;
	private float dayArcLength;
	private float canvasRadius;
	private float darkCircleRadius;
	private float darkCircleRatio = 14f / 15f;
	private float dayArcRatio = 0.7f;
	private float markerHeight = 20.0f;
	private float markerWidth = markerHeight * 1.4f;
	private float dayArcPadding;
	private float dayArcWidth;
	private long visibleSunlight;
	private float sunArcLength;
	private long sunOffset;
	private float sunOffsetDegrees;
	private final int MILLIS_TO_DEGREES = 240000;
	
	// In DIP (Approximately 3 cells)
	private int widgetWidth = 250;
	private int widgetHeight = 250;
	
	// Shapes
	private RectF boundingCircle;
	private Path dayProgress;
	
	// State
	private boolean mKeepon = false;
	private boolean mShowNow = true;
	private static final int UPDATE_INTERVAL = 1000 * 15;
	
	public DayArcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DayArcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DayArcView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		
		// Get Data
		mCalendar = Calendar.getInstance();
		sp = new SPAdapter(context);
        db = new DBAdapter(context);
		dayOrNight = sp.getDayOrNight();
		
        // Night Set Up        
        if (!dayOrNight){
			return; // Don't Draw Anything at Night time
		}
        
		// Get Last Wake/ Next Sleep
        db.open();
        long cWake = db.getLastWake();
        long cSleep = sp.getNextSleepAlarm();		
        db.close();
        Log.d("cwake",Long.toString(cWake));
        Log.d("cSleep",Long.toString(cSleep));
                               		
		// Translate Screen Width from DIP to PX
		final Resources r = getResources();		
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widgetWidth, r.getDisplayMetrics());
		widgetWidth = (int) px;
		widgetHeight = (int) px;
		canvasRadius = (float) widgetWidth / 2;

		// Create Shapes
		dayArcWidth = widgetWidth * dayArcRatio;
		dayArcPadding = (widgetWidth - dayArcWidth) / 2;
		boundingCircle = new RectF(0, 0, dayArcWidth, dayArcWidth);
		boundingCircle.offsetTo(dayArcPadding, dayArcPadding);
		
		// Calculate Day Arc Length
		long dayLength = (long) cSleep - cWake;
		dayArcLength = (float) Math.round(dayLength / MILLIS_TO_DEGREES); // Convert to Degrees		
		darkCircleRadius = canvasRadius * darkCircleRatio;
				
		// Calculate current position arc
		currentTime = mCalendar.getTimeInMillis();
		currentTime = currentTime - cWake;
		currentTimeDegrees = (float) Math.round(currentTime / MILLIS_TO_DEGREES);
		dayProgress = new Path();
		dayProgress.moveTo(0, 0);
		dayProgress.lineTo(0.0f, markerHeight);
		dayProgress.lineTo(markerWidth, markerHeight/2);
		dayProgress.close();
		dayProgress.offset(canvasRadius, dayArcPadding - (markerHeight/2) + 1);
		
		// Calculate sun arc length
		sunrise = new SunriseSunset(mCalendar, SunriseSunset.SunEnum.SUNRISE, context);
        sunset = new SunriseSunset(mCalendar, SunriseSunset.SunEnum.SUNSET, context);
        sunriseCal = sunrise.getSunCalendar();
        sunsetCal = sunset.getSunCalendar();
        visibleSunlight = sunsetCal.getTimeInMillis() - sunriseCal.getTimeInMillis();
        sunArcLength = (float) Math.round(visibleSunlight / MILLIS_TO_DEGREES);
		
		// Calculate length of arc from Wake to Sunrise (Even if Negative)
        sunOffset = sunriseCal.getTimeInMillis() - cWake;
        sunOffsetDegrees = (float) Math.round(sunOffset / MILLIS_TO_DEGREES);
        
        // TODO Get Events
        gc = new GCAdapter(context);
        cursor = gc.getTodaysEvents(); 
			
	}
	
	@Override
	protected void onAttachedToWindow() {
		mKeepon = true;
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		mKeepon = false;
		super.onDetachedFromWindow();
	}
	
	public void setShowNow(boolean showNow) {
		mShowNow = showNow;
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (!dayOrNight){
			// TODO Draw Moon
			return;
		}
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
				
		if (mShowNow) {
			mCalendar.setTimeInMillis(System.currentTimeMillis());

			if (mKeepon) {
				postInvalidateDelayed(UPDATE_INTERVAL);
			}
		}

		final float centerX = getWidth() / 2;
		final float centerY = getHeight() / 2;
				
		// Draw Shadow of transparent circle
		int[] colors;
		colors = new int[4];
		colors[0] = Color.TRANSPARENT;
		colors[1] = Color.TRANSPARENT;
		colors[2] = Color.BLACK;
		colors[3] = Color.TRANSPARENT;
		
		float[] positions;
		positions = new float[4];
		positions[0] = 0f;
		positions[1] = 0.93f;
		positions[2] = 0.93f;
		positions[3] = 1f;

		Shader shader = new RadialGradient(centerX, centerY, canvasRadius, colors, positions, Shader.TileMode.REPEAT);
		paint.setShader(shader);
		paint.setARGB(97, 0, 0, 0);
		canvas.drawCircle(centerX, centerY, canvasRadius, paint);
		paint.setShader(null);
			
		// Draw Black Opaque circle
		paint.setARGB(50, 0, 0, 0);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(centerX, centerY, darkCircleRadius - 18, paint);
		canvas.save();
		
		// Draw Sunlight Arc
		paint.setARGB(110, 236, 198, 64);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(20);
		canvas.rotate(-90, centerX, centerY);
		canvas.rotate(-(dayArcLength/2), centerX, centerY);
		canvas.rotate(sunOffsetDegrees,centerX,centerY);
		paint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
		canvas.drawArc(boundingCircle, 0, sunArcLength, false, paint);
		canvas.restore();
		canvas.save();
		
		// Draw Day Arc
		paint.setMaskFilter(null);
		paint.setARGB(80, 0, 0, 0);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
		canvas.rotate(-90, centerX, centerY);
		canvas.rotate(-(dayArcLength/2), centerX, centerY);
		canvas.drawArc(boundingCircle, 0, dayArcLength, false, paint);
		
		// Draw Night Arc
		paint.setARGB(30, 255, 255, 255);
		canvas.drawArc(boundingCircle, 0, dayArcLength - 360, false, paint);
		canvas.restore();
		canvas.save();
		
		// Draw Google Calendar Events
		paint.setStyle(Paint.Style.FILL);
		paint.setARGB(35, 0, 0, 0);
		
		while (!cursor.isAfterLast()){
			long start = (long) (cursor.getLong(0)/MILLIS_TO_DEGREES) - 90;
			long length = (cursor.getLong(1) - cursor.getLong(0))/MILLIS_TO_DEGREES;
			canvas.drawArc(boundingCircle, start, length, true, paint);
			cursor.moveToNext();
		}
						
		// Draw current time arrow		
		canvas.rotate(-(dayArcLength/2), centerX, centerY);
		canvas.rotate(currentTimeDegrees, centerX, centerY);
		paint.setARGB(170, 55, 55, 55);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(dayProgress, paint);
		canvas.restore();

	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {				
		setMeasuredDimension(widgetWidth, widgetHeight);		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	
	@Override
	public int getSuggestedMinimumHeight() {
		return widgetHeight;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return widgetWidth;
	}

}
