package com.android.contacts.gridwidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class ContactWidgetLayout extends RelativeLayout {
    public ContactWidgetLayout(Context context) {
        super(context);
    }

    public ContactWidgetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactWidgetLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((View) getParent()).setPadding(0, 0, 0, 0);
    }
}
