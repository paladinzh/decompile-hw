package com.android.settings.dashboard.conditional;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.PersistableBundle;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.dashboard.conditional.HotspotCondition.Receiver;

public abstract class Condition {
    private boolean mIsActive;
    private boolean mIsSilenced;
    private long mLastStateChange;
    protected final ConditionManager mManager;

    public abstract CharSequence[] getActions();

    public abstract Icon getIcon();

    public abstract int getMetricsConstant();

    public abstract CharSequence getSummary();

    public abstract CharSequence getTitle();

    public abstract void onActionClick(int i);

    public abstract void onPrimaryClick();

    public abstract void refreshState();

    Condition(ConditionManager manager) {
        this.mManager = manager;
    }

    void restoreState(PersistableBundle bundle) {
        this.mIsSilenced = bundle.getBoolean("silence");
        this.mIsActive = bundle.getBoolean("active");
        this.mLastStateChange = bundle.getLong("last_state");
    }

    boolean saveState(PersistableBundle bundle) {
        if (this.mIsSilenced) {
            bundle.putBoolean("silence", this.mIsSilenced);
        }
        if (this.mIsActive) {
            bundle.putBoolean("active", this.mIsActive);
            bundle.putLong("last_state", this.mLastStateChange);
        }
        return !this.mIsSilenced ? this.mIsActive : true;
    }

    protected void notifyChanged() {
        this.mManager.notifyChanged(this);
    }

    public boolean isSilenced() {
        return this.mIsSilenced;
    }

    public boolean isActive() {
        return this.mIsActive;
    }

    protected void setActive(boolean active) {
        if (this.mIsActive != active) {
            this.mIsActive = active;
            this.mLastStateChange = System.currentTimeMillis();
            if (this.mIsSilenced && !active) {
                this.mIsSilenced = false;
                onSilenceChanged(this.mIsSilenced);
            }
            notifyChanged();
        }
    }

    public void silence() {
        if (!this.mIsSilenced) {
            this.mIsSilenced = true;
            MetricsLogger.action(this.mManager.getContext(), 372, getMetricsConstant());
            onSilenceChanged(this.mIsSilenced);
            notifyChanged();
        }
    }

    private void onSilenceChanged(boolean silenced) {
        Class<?> clz = getReceiverClass();
        if (clz != null) {
            if (silenced || !(clz == Receiver.class || clz == DndCondition.Receiver.class)) {
                int i;
                PackageManager pm = this.mManager.getContext().getPackageManager();
                ComponentName componentName = new ComponentName(this.mManager.getContext(), clz);
                if (silenced) {
                    i = 1;
                } else {
                    i = 2;
                }
                pm.setComponentEnabledSetting(componentName, i, 1);
            }
        }
    }

    protected Class<?> getReceiverClass() {
        return null;
    }

    public boolean shouldShow() {
        return isActive() && !isSilenced();
    }

    long getLastChange() {
        return this.mLastStateChange;
    }
}
