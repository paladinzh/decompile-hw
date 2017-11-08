package com.huawei.gallery.actionbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.view.ActionItem;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.app.plugin.PhotoExtraButton;

public class DetailActionMode extends AbstractTitleMode {
    private PhotoExtraButton mExtraButton = null;
    private PhotoExtraButton mExtraButton1 = null;
    private boolean mHeadIconVisible = true;
    private View mMarginItem = null;
    private Action mMiddleAction = Action.NONE;
    private SimpleActionItem mMiddleActionItem = null;

    protected Bundle saveState() {
        Bundle data = super.saveState();
        data.putInt("ACTION_MODE_MIDDLE_ITEM_KEY", this.mMiddleAction.id);
        return data;
    }

    protected void resume(Bundle resumeData) {
        super.resume(resumeData);
        this.mMiddleAction = Action.getAction(resumeData.getInt("ACTION_MODE_MIDDLE_ITEM_KEY", Action.ACTION_ID_NONE));
    }

    public void setMiddleAction(Action action) {
        if (this.mHeadIconVisible) {
            this.mMiddleAction = action;
            if (this.mMiddleActionItem != null) {
                this.mMiddleActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
                this.mMiddleActionItem.setAction(action);
            }
        }
    }

    public void setLeftAction(Action action) {
        if (this.mHeadIconVisible) {
            super.setLeftAction(action);
        }
    }

    public void setRightAction(Action action) {
        if (this.mHeadIconVisible) {
            super.setRightAction(action);
        }
    }

    public ActionItem getMiddleActionItem() {
        return this.mMiddleActionItem;
    }

    public int getMode() {
        return 7;
    }

    public void setActionBarVisible(boolean visibleL, boolean visibleM, boolean visibleR) {
        int i = 0;
        if (this.mHeadIconVisible) {
            int i2;
            SimpleActionItem simpleActionItem = this.mLeftActionItem;
            if (!visibleL || this.mLeftActionItem.getAction() == Action.NONE) {
                i2 = 4;
            } else {
                i2 = 0;
            }
            simpleActionItem.setVisibility(i2);
            simpleActionItem = this.mMiddleActionItem;
            if (!visibleM || this.mMiddleActionItem.getAction() == Action.NONE) {
                i2 = 4;
            } else {
                i2 = 0;
            }
            simpleActionItem.setVisibility(i2);
            SimpleActionItem simpleActionItem2 = this.mRightActionItem;
            if (!visibleR || this.mRightActionItem.getAction() == Action.NONE) {
                i = 4;
            }
            simpleActionItem2.setVisibility(i);
        }
    }

    @SuppressLint({"InflateParams"})
    protected void initViewItem() {
        this.mMainView = (ViewGroup) this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_detail_action, null);
        this.mLeftActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_left);
        this.mLeftActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
        this.mLeftActionItem.setAction(this.mLeftAction);
        this.mLeftActionItem.setOnClickListener(this);
        this.mRightActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_right);
        this.mRightActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
        this.mRightActionItem.setAction(this.mRightAction);
        this.mRightActionItem.setOnClickListener(this);
        this.mRightActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_right);
        this.mMiddleActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_middle);
        this.mMiddleActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
        this.mMiddleActionItem.setAction(this.mMiddleAction);
        this.mMiddleActionItem.setOnClickListener(this);
        this.mExtraButton = (PhotoExtraButton) this.mMainView.findViewById(R.id.plugin_button);
        this.mExtraButton1 = (PhotoExtraButton) this.mMainView.findViewById(R.id.plugin_button1);
        this.mMarginItem = this.mMainView.findViewById(R.id.margin);
        checkVisible();
    }

    public void onConfigurationChanged(boolean isPort) {
        this.mMarginItem.setVisibility(isPort ? 8 : 0);
    }

    public PhotoExtraButton getExtraButton() {
        return this.mExtraButton;
    }

    public PhotoExtraButton getExtraButton1() {
        return this.mExtraButton1;
    }

    private void checkVisible() {
        if (!(this.mMainView == null || this.mHeadIconVisible)) {
            this.mExtraButton.setVisibility(8);
            this.mExtraButton1.setVisibility(8);
            this.mLeftActionItem.setVisibility(8);
            this.mRightActionItem.setVisibility(8);
            this.mMiddleActionItem.setVisibility(8);
        }
    }

    public void setHeadIconVisible(boolean visible) {
        this.mHeadIconVisible = visible;
        checkVisible();
    }

    public boolean isHeadIconVisible() {
        return this.mHeadIconVisible;
    }
}
