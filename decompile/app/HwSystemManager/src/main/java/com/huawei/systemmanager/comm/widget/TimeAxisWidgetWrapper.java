package com.huawei.systemmanager.comm.widget;

import android.view.View;
import huawei.android.widget.TimeAxisWidget;
import java.util.Calendar;

public class TimeAxisWidgetWrapper {
    public static final int FLAG_ONLY_DATE = 1;
    public static final int MODEL_DEFAULT = 0;
    public static final int MODEL_ONLY_TIME = 2;
    public static final int STYLE_DEFAULT = 0;
    private final TimeAxisWidget mTimeAxisView;

    public TimeAxisWidgetWrapper(View timeAxisWidgeView) {
        this.mTimeAxisView = (TimeAxisWidget) timeAxisWidgeView;
    }

    public void setMode(int mode) {
        this.mTimeAxisView.setMode(mode);
    }

    public void setAxisStyle(int style) {
        this.mTimeAxisView.setAxisStyle(style);
    }

    public void setContent(View interView) {
        this.mTimeAxisView.setContent(interView);
    }

    public void setCalendar(Calendar cal) {
        this.mTimeAxisView.setCalendar(cal);
    }
}
