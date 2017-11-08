package com.android.settings.dashboard;

import android.content.Context;
import android.widget.TextView;

public class DashBoardTileEnabler {
    protected Context mContext;
    private String mStatus = "";
    private TextView mStatusTextView;

    protected void updateStatusText(String text) {
        if (this.mStatusTextView != null && this.mContext != null) {
            this.mStatus = text;
            this.mStatusTextView.setText(text);
        }
    }
}
