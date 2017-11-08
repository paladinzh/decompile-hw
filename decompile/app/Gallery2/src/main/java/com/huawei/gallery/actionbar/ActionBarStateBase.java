package com.huawei.gallery.actionbar;

import android.app.ActionBar;
import android.os.Bundle;
import com.huawei.gallery.app.AbstractGalleryActivity;
import java.util.Arrays;

public abstract class ActionBarStateBase {
    protected ActionBar mActionBar;
    protected ActionBarMenuManager mActionBarMenuManager;
    protected AbstractGalleryActivity mActivity;
    protected ActionBarContainerManager mContainerMgr;

    public abstract int getMode();

    protected abstract void showHeadView();

    protected void initContainer(AbstractGalleryActivity activity, ActionBarContainerManager containerMgr) {
        this.mActivity = activity;
        this.mActionBarMenuManager = containerMgr.getMenuManager();
        this.mActionBar = this.mActivity.getActionBar();
        this.mContainerMgr = containerMgr;
    }

    public void show() {
        showHeadView();
        showActionPanel();
    }

    public void hide() {
    }

    protected void showActionPanel() {
    }

    public void setMenu(int count, Action... actions) {
        this.mActionBarMenuManager.setActions(count, this.mContainerMgr.isActionPanelVisible(), (Action[]) Arrays.copyOf(actions, actions.length));
    }

    public void setActionEnable(boolean enable, int actionID) {
        this.mActionBarMenuManager.setActionEnable(enable, actionID);
    }

    public void changeAction(int oldActionID, int newActionID) {
        this.mActionBarMenuManager.changeAction(oldActionID, newActionID);
    }

    protected void resume(Bundle resumeData) {
        this.mActionBarMenuManager.resumeState(resumeData);
    }

    protected Bundle saveState() {
        Bundle data = new Bundle();
        this.mActionBarMenuManager.saveToState(data);
        return data;
    }

    public void reEnter(boolean saveState) {
    }
}
