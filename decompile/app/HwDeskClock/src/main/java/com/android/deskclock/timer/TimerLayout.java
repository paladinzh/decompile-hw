package com.android.deskclock.timer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import java.util.Locale;

public class TimerLayout extends RelativeLayout {
    public TimerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean resolveLayoutDirection() {
        String languageString = Locale.getDefault().toString();
        if (languageString.contains("ar") || languageString.contains("fa") || Locale.getDefault().getLanguage().contains("ur")) {
            return false;
        }
        return super.resolveLayoutDirection();
    }
}
