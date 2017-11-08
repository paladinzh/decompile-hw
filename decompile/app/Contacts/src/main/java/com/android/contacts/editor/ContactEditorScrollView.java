package com.android.contacts.editor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ContactEditorScrollView extends ScrollView {
    private boolean isBlockEventDelivery = false;

    public ContactEditorScrollView(Context context) {
        super(context);
    }

    public ContactEditorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactEditorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ContactEditorScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.isBlockEventDelivery) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setEventDeliveryState(boolean state) {
        this.isBlockEventDelivery = state;
    }
}
