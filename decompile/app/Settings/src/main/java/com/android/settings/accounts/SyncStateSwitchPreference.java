package com.android.settings.accounts;

import android.accounts.Account;
import android.app.ActivityManager;
import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settingslib.widget.AnimatedImageView;

public class SyncStateSwitchPreference extends SwitchPreference {
    private Account mAccount;
    private String mAuthority;
    private boolean mFailed = false;
    private boolean mIsActive = false;
    private boolean mIsPending = false;
    private boolean mOneTimeSyncMode = false;

    public SyncStateSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130968986);
        this.mAccount = null;
        this.mAuthority = null;
    }

    public SyncStateSwitchPreference(Context context, Account account, String authority) {
        super(context, null);
        this.mAccount = account;
        this.mAuthority = authority;
        setLayoutResource(2130968986);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        boolean activeVisible;
        int i;
        boolean failedVisible;
        super.onBindViewHolder(view);
        AnimatedImageView syncActiveView = (AnimatedImageView) view.findViewById(2131886951);
        View syncFailedView = view.findViewById(2131886950);
        if (this.mIsActive) {
            activeVisible = true;
        } else {
            activeVisible = this.mIsPending;
        }
        if (activeVisible) {
            i = 0;
        } else {
            i = 8;
        }
        syncActiveView.setVisibility(i);
        syncActiveView.setAnimating(this.mIsActive);
        if (!this.mFailed || activeVisible) {
            failedVisible = false;
        } else {
            failedVisible = true;
        }
        if (failedVisible) {
            i = 0;
        } else {
            i = 8;
        }
        syncFailedView.setVisibility(i);
        View widgetFrame = view.findViewById(16908312);
        if (this.mOneTimeSyncMode) {
            widgetFrame.setVisibility(8);
            ((TextView) view.findViewById(16908304)).setText(getContext().getString(2131627561, new Object[]{getSummary()}));
            return;
        }
        widgetFrame.setVisibility(0);
        Switch statusSwitch = (Switch) widgetFrame.findViewById(16908352);
        if (statusSwitch != null) {
            statusSwitch.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SyncStateSwitchPreference.this.callChangeListener(Boolean.valueOf(SyncStateSwitchPreference.this.isChecked()));
                }
            });
        }
    }

    public void setActive(boolean isActive) {
        this.mIsActive = isActive;
        notifyChanged();
    }

    public void setPending(boolean isPending) {
        this.mIsPending = isPending;
        notifyChanged();
    }

    public void setFailed(boolean failed) {
        this.mFailed = failed;
        notifyChanged();
    }

    public void setOneTimeSyncMode(boolean oneTimeSyncMode) {
        this.mOneTimeSyncMode = oneTimeSyncMode;
        notifyChanged();
    }

    public boolean isOneTimeSyncMode() {
        return this.mOneTimeSyncMode;
    }

    protected void onClick() {
        if (!this.mOneTimeSyncMode) {
            if (ActivityManager.isUserAMonkey()) {
                Log.d("SyncState", "ignoring monkey's attempt to flip sync state");
            } else {
                super.onClick();
            }
        }
    }

    public Account getAccount() {
        return this.mAccount;
    }

    public String getAuthority() {
        return this.mAuthority;
    }
}
