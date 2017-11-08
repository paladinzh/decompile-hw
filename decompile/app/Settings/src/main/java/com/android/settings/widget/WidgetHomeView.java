package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class WidgetHomeView extends LinearLayout {
    public WidgetHomeView(Context context) {
        this(context, null);
    }

    public WidgetHomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetHomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((View) getParent()).setPadding(0, 0, 0, 0);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
