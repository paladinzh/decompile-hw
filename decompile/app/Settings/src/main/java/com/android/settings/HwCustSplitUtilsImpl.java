package com.android.settings;

import android.app.Activity;
import android.app.HwActivitySplitterImpl;
import android.content.Intent;
import android.view.ViewGroup;

public class HwCustSplitUtilsImpl extends HwCustSplitUtils {
    protected HwActivitySplitterImpl mIHwActivitySplitterImpl;

    public HwCustSplitUtilsImpl(Activity activity) {
        super(activity);
        if (Utils.isTablet()) {
            this.mIHwActivitySplitterImpl = HwActivitySplitterImpl.getDefault(activity, isBaseActivity(activity));
        }
    }

    protected boolean isBaseActivity(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean z;
        if (activity.getClass().getName().equals(HWSettings.class.getName())) {
            z = true;
        } else {
            z = activity.getClass().getName().equals(Settings.class.getName());
        }
        return z;
    }

    public void setSplit(ViewGroup contentIndexView) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.setSplit(contentIndexView);
        }
    }

    public void setFirstIntent(Intent intent) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.setFirstIntent(intent);
        }
    }

    public void cancelSplit(Intent intent) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.cancelSplit(intent);
        }
    }

    public void setTargetIntent(Intent targetIntent) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.setTargetIntent(targetIntent);
        }
    }

    public void finishAllSubActivities() {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.finishAllSubActivities();
        }
    }

    public void hideAllContent() {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.hideAllContent();
        }
    }

    public void setControllerShowing(boolean isControllerShowing) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.setControllerShowing(isControllerShowing);
        }
    }

    public boolean reachSplitSize() {
        if (this.mIHwActivitySplitterImpl != null) {
            return this.mIHwActivitySplitterImpl.reachSplitSize();
        }
        return false;
    }

    public boolean isSplitMode() {
        if (this.mIHwActivitySplitterImpl != null) {
            return this.mIHwActivitySplitterImpl.isSplitMode();
        }
        return false;
    }

    public Intent getCurrentSubIntent() {
        if (this.mIHwActivitySplitterImpl != null) {
            return this.mIHwActivitySplitterImpl.getCurrentSubIntent();
        }
        return null;
    }

    public void setExitWhenContentGone(boolean exit) {
        if (this.mIHwActivitySplitterImpl != null) {
            this.mIHwActivitySplitterImpl.setExitWhenContentGone(exit);
        }
    }

    public void setAsJumpedActivity(Intent intent) {
        if (intent != null) {
            HwActivitySplitterImpl.setAsJumpActivity(intent);
        }
    }
}
