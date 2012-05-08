package com.pi.synctosunrise.display;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class IconPreferenceScreen extends Preference {

    private Drawable mIcon;

    public IconPreferenceScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconPreferenceScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(com.pi.synctosunrise.R.layout.preferenceicon);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.pi.synctosunrise.R.styleable.IconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(com.pi.synctosunrise.R.styleable.IconPreferenceScreen_icon);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        ImageView imageView = (ImageView) view.findViewById(com.pi.synctosunrise.R.id.icon);
        if (imageView != null && mIcon != null) {
            imageView.setImageDrawable(mIcon);
        }
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && mIcon != null) || (icon != null && !icon.equals(mIcon))) {
            mIcon = icon;
            notifyChanged();
        }
    }

    public Drawable getIcon() {
        return mIcon;
    }
}