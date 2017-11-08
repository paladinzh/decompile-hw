package com.huawei.systemmanager.comm.widget;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.huawei.systemmanager.comm.misc.Utility;

public class CommonSwitchController {
    private OnCheckedChangeListener mCheckChangeListener;
    private OnClickListener mGroupClicker;
    private View mGroupView;
    private boolean mSupportMultiUser;
    private Switch mSwtichView;

    public CommonSwitchController(View group, Switch switcher) {
        this.mSupportMultiUser = true;
        this.mGroupClicker = new OnClickListener() {
            public void onClick(View v) {
                if (CommonSwitchController.this.mSwtichView != null) {
                    if (CommonSwitchController.this.mSupportMultiUser || Utility.isOwnerUser()) {
                        CommonSwitchController.this.mSwtichView.toggle();
                    }
                }
            }
        };
        this.mGroupView = group;
        this.mSwtichView = switcher;
        if (this.mGroupView != null) {
            this.mGroupView.setOnClickListener(this.mGroupClicker);
        }
    }

    public CommonSwitchController(View group, Switch switcher, boolean setEmuiBackground) {
        this(group, switcher);
        if (setEmuiBackground && group != null) {
            int themeID = group.getResources().getIdentifier(ViewUtil.EMUI_SELECTOR_BACKGROUND, null, null);
            if (themeID != 0) {
                group.setBackgroundResource(themeID);
            }
        }
    }

    public void setSupportMultiUser(boolean supprtMultiUser) {
        this.mSupportMultiUser = supprtMultiUser;
        if (this.mSwtichView != null) {
            if (this.mSupportMultiUser) {
                this.mSwtichView.setEnabled(true);
            } else {
                this.mSwtichView.setEnabled(Utility.isOwnerUser(false));
            }
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        this.mCheckChangeListener = l;
        if (this.mSwtichView != null) {
            this.mSwtichView.setOnCheckedChangeListener(this.mCheckChangeListener);
        }
    }

    public void updateCheckState(boolean check) {
        if (this.mSwtichView != null) {
            this.mSwtichView.setOnCheckedChangeListener(null);
            this.mSwtichView.setChecked(check);
            this.mSwtichView.setOnCheckedChangeListener(this.mCheckChangeListener);
        }
    }

    public void setVisibility(int visibility) {
        if (this.mGroupView != null) {
            this.mGroupView.setVisibility(visibility);
        }
    }

    public boolean isChecked() {
        if (this.mSwtichView != null) {
            return this.mSwtichView.isChecked();
        }
        return false;
    }
}
