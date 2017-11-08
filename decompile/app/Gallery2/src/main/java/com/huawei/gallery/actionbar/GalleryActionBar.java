package com.huawei.gallery.actionbar;

import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.android.gallery3d.R;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.huawei.gallery.actionbar.view.ActionCustomMode;
import com.huawei.gallery.app.AbstractGalleryActivity;

public class GalleryActionBar {
    private final ActionBarContainerManager mActionBarContainerManager;
    private AbstractGalleryActivity mActivity;
    private ActionBarStateBase mCurrentMode = null;
    private int mLastMode = 0;
    private Bundle mLastModeData = null;
    private Listener mListener;

    public interface Listener {
        void onCreateModeCompleted(ActionBarStateBase actionBarStateBase);
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public GalleryActionBar(AbstractGalleryActivity activity) {
        this.mActivity = activity;
        this.mActionBarContainerManager = new ActionBarContainerManager(this.mActivity);
    }

    public void onCreateOptionsMenu(Menu menu) {
        this.mActionBarContainerManager.onCreateOptionsMenu(menu);
    }

    public void onOptionsItemSelected(MenuItem item) {
        this.mActionBarContainerManager.onOptionsItemSelected(item);
    }

    public void setShoudTransition(boolean enabled) {
        this.mActionBarContainerManager.setShoudTransition(enabled, false);
    }

    public void disableAnimation(boolean disabled) {
        boolean z;
        boolean z2 = false;
        ActionBarContainerManager actionBarContainerManager = this.mActionBarContainerManager;
        if (disabled) {
            z = false;
        } else {
            z = true;
        }
        actionBarContainerManager.setShoudTransition(z, true);
        ActionBarContainerManager actionBarContainerManager2 = this.mActionBarContainerManager;
        if (!disabled) {
            z2 = true;
        }
        actionBarContainerManager2.setScrollTabAnimation(z2, true);
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mActionBarContainerManager.addOnMenuVisibilityListener(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mActionBarContainerManager.removeOnMenuVisibilityListener(listener);
    }

    private ActionBarStateBase createMode(int modeType) {
        ActionBarStateBase mode;
        switch (modeType) {
            case 1:
                mode = new TabMode();
                break;
            case 2:
                mode = new ActionMode();
                break;
            case 3:
                mode = new SelectionMode();
                break;
            case 4:
                mode = new MergeCardActionMode();
                break;
            case 5:
                mode = new ActionCustomMode();
                break;
            case 6:
                mode = new TitleSubTitleMode();
                break;
            case 7:
                mode = new DetailActionMode();
                break;
            case 8:
                mode = new StandardTitleActionMode();
                break;
            default:
                return null;
        }
        mode.initContainer(this.mActivity, this.mActionBarContainerManager);
        if (this.mListener != null) {
            this.mListener.onCreateModeCompleted(mode);
        }
        return mode;
    }

    public void setMenuClickable(boolean clickable) {
        this.mActionBarContainerManager.setMenuClickable(clickable);
    }

    private void saveLastMode() {
        Bundle bundle = null;
        if (this.mLastModeData != null) {
            bundle = new Bundle(this.mLastModeData);
            bundle.putInt("KEY_LAST_MODE", this.mLastMode);
        }
        this.mLastMode = this.mCurrentMode.getMode();
        this.mLastModeData = this.mCurrentMode.saveState();
        if (bundle != null) {
            this.mLastModeData.putBundle("KEY_LAST_DATA", bundle);
        }
    }

    private void discardLastMode() {
        this.mLastMode = 0;
        this.mLastModeData = null;
    }

    public ActionBarStateBase getCurrentMode() {
        return this.mCurrentMode;
    }

    public ActionBarStateBase enterModeForced(boolean saveState, int modeType) {
        if (saveState) {
            saveLastMode();
        } else {
            discardLastMode();
        }
        this.mCurrentMode = createMode(modeType);
        return this.mCurrentMode;
    }

    public ActionBarStateBase enterMode(boolean saveState, int modeType) {
        if (this.mCurrentMode == null || this.mCurrentMode.getMode() != modeType) {
            return enterModeForced(saveState, modeType);
        }
        this.mCurrentMode.reEnter(saveState);
        return this.mCurrentMode;
    }

    public void leaveCurrentMode() {
        if (this.mLastMode == 0) {
            throw new RuntimeException("No Back State to return!");
        }
        this.mCurrentMode = createMode(this.mLastMode);
        if (this.mCurrentMode == null) {
            throw new RuntimeException("Illegal Back State to resume. mLastMode: " + this.mLastMode);
        }
        this.mCurrentMode.resume(this.mLastModeData);
        this.mCurrentMode.show();
        this.mLastModeData = this.mLastModeData.getBundle("KEY_LAST_DATA");
        if (this.mLastModeData != null) {
            this.mLastMode = this.mLastModeData.getInt("KEY_LAST_MODE", 0);
        } else {
            this.mLastMode = 0;
        }
    }

    public TabMode enterTabMode(boolean saveState) {
        return (TabMode) enterMode(saveState, 1);
    }

    public ActionMode enterActionMode(boolean saveState) {
        return (ActionMode) enterMode(saveState, 2);
    }

    public MergeCardActionMode enterMergeCardActionMode(boolean saveState) {
        return (MergeCardActionMode) enterMode(saveState, 4);
    }

    public StandardTitleActionMode enterStandardTitleActionMode(boolean saveState) {
        return (StandardTitleActionMode) enterMode(saveState, 8);
    }

    public SelectionMode enterSelectionMode(boolean saveState) {
        setShoudTransition(true);
        return (SelectionMode) enterMode(saveState, 3);
    }

    public DetailActionMode enterDetailActionMode(boolean saveState) {
        return (DetailActionMode) enterMode(saveState, 7);
    }

    public void onConfigurationChanged(Configuration oldConfig, Configuration newConfig) {
        this.mActionBarContainerManager.onConfigurationChanged(newConfig);
    }

    public void onNavigationBarChanged(boolean show) {
        this.mActionBarContainerManager.onNavigationBarChanged(show);
    }

    public int getActionBarHeight() {
        return this.mActivity.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
    }

    public int getCurrentFoorBarHeight(Context context, boolean checkActionPanel) {
        return 0;
    }

    public void setActionClickable(boolean clickable) {
        setMenuClickable(clickable);
    }

    public void setActionPanelVisible(boolean visible) {
        setActionPanelVisibleWithFadeInAnimationIfNeed(visible, true);
    }

    public void setActionPanelVisible(boolean visible, boolean withAnimation) {
        setActionPanelVisibleWithFadeInAnimationIfNeed(visible, withAnimation);
    }

    public void setMenuVisible(boolean visible) {
        this.mActionBarContainerManager.getMenuManager().setOptionMenuVisible(visible);
    }

    public void setHeadBarVisible(boolean visible) {
        setHeadBarVisibleWithFadeInAnimationIfNeed(visible, true);
    }

    public void setHeadBarVisible(boolean visible, boolean withAnimation) {
        setHeadBarVisibleWithFadeInAnimationIfNeed(visible, withAnimation);
    }

    public void setActionBarVisible(boolean visible) {
        setHeadBarVisibleWithFadeInAnimationIfNeed(visible, true);
        setActionPanelVisibleWithFadeInAnimationIfNeed(visible, true);
    }

    public void setActionBarVisible(boolean visible, boolean withAnimation) {
        setHeadBarVisibleWithFadeInAnimationIfNeed(visible, withAnimation);
        setActionPanelVisibleWithFadeInAnimationIfNeed(visible, withAnimation);
    }

    private void setHeadBarVisibleWithFadeInAnimationIfNeed(boolean visible, boolean withAnimation) {
        this.mActionBarContainerManager.setHeadBarVisibleWithFadeInAnimationIfNeed(visible, withAnimation);
    }

    private void setActionPanelVisibleWithFadeInAnimationIfNeed(boolean visible, boolean withFadeInAnimation) {
        this.mActionBarContainerManager.setActionPanelVisibleWithFadeInAnimationIfNeed(visible, withFadeInAnimation);
    }

    public void setHeadBackground(int color) {
    }

    public void setHeadDefaultBackground() {
    }

    public void setActionPanelStyle(int style) {
        this.mActionBarContainerManager.setStyle(style);
    }

    public void setNavigationMargin(int margin) {
    }

    public void setNavigationMarginMw() {
    }

    public void hideFootActionContainer() {
        this.mActionBarContainerManager.hideFootActionContainer();
    }

    public void hideHeadActionContainer() {
        if (!MultiWindowStatusHolder.isInMultiMaintained()) {
            this.mActionBarContainerManager.hideHeadActionContainer();
        }
    }

    public void resetHeadAndFootActionContainer() {
        this.mActionBarContainerManager.resetHeadAndFootActionContainer();
    }

    public void setProgress(int progress) {
        this.mActionBarContainerManager.getMenuManager().setProgess(progress);
    }

    public int getProgress() {
        return this.mActionBarContainerManager.getMenuManager().getProgress();
    }

    public int getStyle() {
        return this.mActionBarContainerManager.getStyle();
    }
}
