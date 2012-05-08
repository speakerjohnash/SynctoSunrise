package com.pi.synctosunrise.display;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;

public class HourMinuteDialog extends AlertDialog implements OnClickListener {
	
	private OnTimeSetListener mListener;
    private NumberPicker mHourPicker;
    private NumberPicker mMinutePicker;
    
    private int mInitialHour;
    private int mInitialMinute;

    public HourMinuteDialog(Context context, OnTimeSetListener listener, int hour, int minute) {
        super(context);
        mInitialHour = hour;
        mInitialMinute = minute;
        mListener = listener;

        setButton(BUTTON_POSITIVE, "Set", this);
        setButton(BUTTON_NEGATIVE, "Cancel", (OnClickListener) null);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.pi.synctosunrise.R.layout.hourminutedialog, null);
        setView(view);

        mHourPicker = (NumberPicker) view.findViewById(com.pi.synctosunrise.R.id.dialoghour);
        mHourPicker.setCurrent(mInitialHour);
        
        mMinutePicker = (NumberPicker) view.findViewById(com.pi.synctosunrise.R.id.dialogminute);
        mMinutePicker.setCurrent(mInitialMinute);
    }
    
    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
            mListener.onTimeSet(mHourPicker.getCurrent(),mMinutePicker.getCurrent());
        }
    }

    public interface OnTimeSetListener {
        public void onTimeSet(int hour, int minute);
    }

}
