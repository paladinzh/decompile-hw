package com.android.alarmclock;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.util.Log;

public class WidgetParentLayout extends LinearLayout {
    public WidgetParentLayout(Context context) {
        super(context);
    }

    public WidgetParentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetParentLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        if (getParent() instanceof AppWidgetHostView) {
            Log.i("WidgetParentLayoutL", "The Parent is AppWidgetHostView");
            AppWidgetHostView appWidgetHostView = (AppWidgetHostView) getParent();
            if (appWidgetHostView != null) {
                appWidgetHostView.setPadding(0, 0, 0, 0);
            }
        } else if (getParent() instanceof View) {
            View view = (View) getParent();
            if (view != null) {
                view.setPadding(0, 0, 0, 0);
            }
        }
        super.onAttachedToWindow();
    }
}
