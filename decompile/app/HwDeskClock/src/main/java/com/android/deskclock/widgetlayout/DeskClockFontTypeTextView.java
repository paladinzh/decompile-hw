package com.android.deskclock.widgetlayout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.internal.R;
import com.android.util.Utils;

public class DeskClockFontTypeTextView extends TextView {
    private String mFontFamily;

    public DeskClockFontTypeTextView(Context context) {
        this(context, null);
    }

    public DeskClockFontTypeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public DeskClockFontTypeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mFontFamily = null;
        TypedArray appearance = context.obtainStyledAttributes(attrs, R.styleable.TextView, 0, 0);
        if (appearance != null) {
            this.mFontFamily = appearance.getString(75);
            appearance.recycle();
        }
        chooseTypeface();
    }

    private void chooseTypeface() {
        if (!Utils.isExistCustomFont()) {
            Typeface theTypeface;
            if (Utils.isChineseLanguage()) {
                theTypeface = Utils.getSmallWindowFont();
            } else {
                theTypeface = createTypeFace();
            }
            setTypeface(theTypeface);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        chooseTypeface();
    }

    private Typeface createTypeFace() {
        if (this.mFontFamily == null) {
            return Utils.getmRobotoXianBlackTypeface();
        }
        if ("sans-serif-thin".equalsIgnoreCase(this.mFontFamily)) {
            return Utils.getmRobotoThinTypeface();
        }
        if ("sans-serif-light".equalsIgnoreCase(this.mFontFamily)) {
            return Utils.getmRobotoLightTypeface();
        }
        if ("sans-serif".equalsIgnoreCase(this.mFontFamily)) {
            return Utils.getSmallWindowFont();
        }
        return Typeface.DEFAULT;
    }
}
