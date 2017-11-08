package com.android.alarmclock;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.util.Log;

public class WidgetParentRelativeLayout extends RelativeLayout {
    public WidgetParentRelativeLayout(Context context) {
        super(context);
    }

    public WidgetParentRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetParentRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        if (getParent() instanceof AppWidgetHostView) {
            Log.i("WidgetParentLayoutR", "The Parent is AppWidgetHostView");
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
