package com.android.deskclock;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.util.Utils;

public class SpecialTimeView extends TextView {
    public SpecialTimeView(Context context) {
        this(context, null);
    }

    public SpecialTimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpecialTimeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setIncludeFontPadding(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.TimerView, defStyle, 0);
        boolean bCenter = a.getBoolean(0, false);
        a.recycle();
        if (bCenter) {
            setPadding(this.mPaddingLeft, getPaddingTop() + (((int) context.getResources().getDisplayMetrics().density) * 6), this.mPaddingRight, getPaddingBottom());
        } else {
            setPadding(this.mPaddingLeft, (int) (getTextSize() * -0.15f), this.mPaddingRight, (int) (getTextSize() * -0.08f));
        }
        setTypeface(Utils.getmRobotoThinTypeface());
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setTypeface(Utils.getmRobotoThinTypeface());
    }
}
