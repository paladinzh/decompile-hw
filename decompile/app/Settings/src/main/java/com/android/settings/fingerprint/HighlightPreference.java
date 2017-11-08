package com.android.settings.fingerprint;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;

public class HighlightPreference extends Preference {
    private Bundle mBundle;
    private int mCommonColor;
    private int mHighlightColor;
    private HighlightHandler mHighlightHandler = new HighlightHandler();
    private View mPrefView;

    private class HighlightHandler extends Handler {
        private HighlightHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HighlightPreference.this.setPrefBgColor(HighlightPreference.this.mHighlightColor);
                    return;
                case 2:
                    HighlightPreference.this.setPrefBgColor(HighlightPreference.this.mCommonColor);
                    return;
                default:
                    return;
            }
        }
    }

    public HighlightPreference(Context context) {
        super(context);
        this.mHighlightColor = context.getResources().getColor(2131427503);
        this.mCommonColor = context.getResources().getColor(2131427504);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mPrefView = view.itemView;
    }

    public void setPrefBgColor(int color) {
        if (this.mPrefView != null) {
            this.mPrefView.setBackgroundColor(color);
            this.mPrefView.invalidate();
        }
    }

    public void highlightBgColor(boolean isHighlight) {
        if (isHighlight) {
            this.mHighlightHandler.sendEmptyMessageDelayed(1, 100);
        } else {
            this.mHighlightHandler.sendEmptyMessageDelayed(2, 100);
        }
    }

    public Bundle getBundle() {
        return this.mBundle;
    }

    public void setBundle(Bundle bundle) {
        this.mBundle = bundle;
    }
}
