package com.android.settings.dashboard;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class UpdateEnabler {
    private Context mContext;
    private TextView mCountText;
    private float mDensity = 2.0f;
    private FrameLayout mFrameRoot;
    private boolean mIsActive;
    private ContentResolver mResolver;
    private ContentObserver mSystemUpdateObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            UpdateEnabler.this.handleCountChanged();
        }
    };
    private int mUpdateCount = 0;

    public UpdateEnabler(Context context, FrameLayout frameLayout) {
        this.mContext = context;
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        this.mResolver = this.mContext.getContentResolver();
        this.mFrameRoot = frameLayout;
    }

    public void resume() {
        this.mResolver.registerContentObserver(Secure.getUriFor("hw_new_system_update"), false, this.mSystemUpdateObserver);
        handleCountChanged();
        this.mIsActive = true;
    }

    public void pause() {
        this.mResolver.unregisterContentObserver(this.mSystemUpdateObserver);
        this.mIsActive = false;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        if (frameLayout != null) {
            this.mFrameRoot = frameLayout;
            handleCountChanged();
        }
    }

    private void handleCountChanged() {
        setUpdateCount(Secure.getInt(this.mResolver, "hw_new_system_update", 0));
    }

    private void setUpdateCount(int updateCount) {
        if (this.mFrameRoot != null) {
            this.mUpdateCount = updateCount;
            this.mCountText = (TextView) this.mFrameRoot.findViewById(2131886444);
            if (this.mUpdateCount < 100) {
                this.mCountText.setText(String.valueOf(this.mUpdateCount));
            } else {
                this.mCountText.setText("99+");
            }
            LayoutParams params = (LayoutParams) this.mFrameRoot.getLayoutParams();
            if (this.mUpdateCount <= 0) {
                this.mFrameRoot.setVisibility(8);
            } else {
                this.mFrameRoot.setVisibility(0);
                if (this.mUpdateCount < 10) {
                    params.width = (int) (this.mDensity * 18.0f);
                    params.height = (int) (this.mDensity * 18.0f);
                } else if (this.mUpdateCount < 100) {
                    params.width = (int) (this.mDensity * 42.0f);
                } else {
                    params.width = (int) (this.mDensity * 48.0f);
                }
            }
            this.mFrameRoot.setLayoutParams(params);
        }
    }

    public boolean isActive() {
        return this.mIsActive;
    }
}
