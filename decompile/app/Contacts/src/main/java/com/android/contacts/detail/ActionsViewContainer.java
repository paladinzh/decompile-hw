package com.android.contacts.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;

class ActionsViewContainer extends LinearLayout {
    private ContextMenuInfo mContextMenuInfo;

    public ActionsViewContainer(Context context) {
        super(context);
    }

    public ActionsViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionsViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPosition(int position) {
        this.mContextMenuInfo = new AdapterContextMenuInfo(this, position, -1);
    }

    public ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }
}
