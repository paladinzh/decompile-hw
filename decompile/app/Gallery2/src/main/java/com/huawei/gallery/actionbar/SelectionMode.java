package com.huawei.gallery.actionbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.view.SimpleActionItem;

public class SelectionMode extends AbstractTitleMode {
    private CheckBox mCheckBox = null;
    private ViewGroup mCheckBoxStub = null;
    private String mCountStr = null;
    private TextView mCountView = null;

    protected void resume(Bundle resumeData) {
        super.resume(resumeData);
        this.mCountStr = resumeData.getString("SELECT_MODE_COUNT_KEY");
    }

    protected Bundle saveState() {
        Bundle data = super.saveState();
        if (this.mCountStr != null) {
            data.putString("SELECT_MODE_COUNT_KEY", this.mCountStr);
        }
        return data;
    }

    public int getMode() {
        return 3;
    }

    public void setCount(int count) {
        setCount(GalleryUtils.getValueFormat((long) count));
    }

    public void setCount(int count, int max) {
        setCount(String.format(this.mActivity.getActivityContext().getString(R.string.fraction), new Object[]{Integer.valueOf(count), Integer.valueOf(max)}));
    }

    private void setShadowCount(int key, String countStr) {
        View view = getView(0, key);
        if (view instanceof TextView) {
            TextView countShadowView = (TextView) view;
            if (TextUtils.isEmpty(countStr)) {
                countShadowView.setVisibility(8);
            } else {
                countShadowView.setText(countStr);
                countShadowView.setVisibility(0);
            }
            countShadowView.setBackground(this.mActivity.getResources().getDrawable(getReverseStyle() == 0 ? R.drawable.ic_choosed_number_bg_light : R.drawable.ic_choosed_number_bg_dark));
        }
    }

    public void setCount(String countStr) {
        this.mCountStr = countStr;
        if (this.mCountView != null) {
            if (this.mCountStr != null && this.mCountStr.length() > 2) {
                this.mCountStr = " " + this.mCountStr + " ";
            }
            if (TextUtils.isEmpty(this.mCountStr)) {
                this.mCountView.setVisibility(8);
            } else {
                this.mCountView.setText(this.mCountStr);
                this.mCountView.setVisibility(0);
            }
            this.mCountView.setBackground(this.mActivity.getResources().getDrawable(this.mActionBarMenuManager.getStyle() == 0 ? R.drawable.ic_choosed_number_bg_light : R.drawable.ic_choosed_number_bg_dark));
            setShadowCount(this.mCountView.hashCode(), this.mCountStr);
        }
    }

    protected void initViewItem() {
        clearView(0);
        clearView(1);
        this.mMainView = new RelativeLayout(this.mActivity);
        this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_selection, this.mMainView);
        this.mLeftActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_left);
        this.mRightActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_right);
        ViewGroup titleLayout = (ViewGroup) this.mMainView.findViewById(R.id.head_actionmode_title_layout);
        this.mTitleView = (TextView) titleLayout.findViewById(R.id.head_actionmode_title);
        this.mCountView = (TextView) titleLayout.findViewById(R.id.head_actionmode_count);
        this.mCheckBoxStub = (ViewGroup) this.mMainView.findViewById(R.id.stub);
        putView(1, this.mLeftActionItem.hashCode(), this.mLeftActionItem);
        putView(1, this.mRightActionItem.hashCode(), this.mRightActionItem);
        putView(1, this.mTitleView.hashCode(), this.mTitleView);
        putView(1, this.mCountView.hashCode(), this.mCountView);
        putView(1, this.mCheckBoxStub.hashCode(), this.mCheckBoxStub);
        this.mShadowRootView = this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_selection, null);
        putView(0, this.mLeftActionItem.hashCode(), this.mShadowRootView.findViewById(R.id.head_select_left));
        putView(0, this.mRightActionItem.hashCode(), this.mShadowRootView.findViewById(R.id.head_select_right));
        putView(0, this.mTitleView.hashCode(), this.mShadowRootView.findViewById(R.id.head_actionmode_title));
        putView(0, this.mCountView.hashCode(), this.mShadowRootView.findViewById(R.id.head_actionmode_count));
        putView(0, this.mCheckBoxStub.hashCode(), this.mShadowRootView.findViewById(R.id.stub));
        setSupportDoubleFace(this.mIsSupportDoubleFace);
        this.mMainView.addView(this.mShadowRootView, 0);
        setLeftAction(this.mLeftAction);
        this.mLeftActionItem.setOnClickListener(this);
        setRightAction(this.mRightAction);
        this.mRightActionItem.setOnClickListener(this);
        setTitleInternal();
        GalleryUtils.setTypeFaceAsSlim(this.mCountView);
        GalleryUtils.setTypeFaceAsSlim((TextView) this.mShadowRootView.findViewById(R.id.head_actionmode_count));
        setCount(this.mCountStr);
    }

    public void onClick(View view) {
        if (view instanceof CheckBox) {
            this.mActivity.onActionItemClicked(this.mRightAction);
        } else {
            super.onClick(view);
        }
    }

    public void setRightAction(Action action) {
        boolean z = false;
        super.setRightAction(action);
        if (this.mRightActionItem != null && this.mCheckBoxStub != null) {
            if (action == Action.MULTI_SELECTION_ON || action == Action.MULTI_SELECTION) {
                if (this.mCheckBox == null) {
                    this.mCheckBox = new CheckBox(this.mActivity);
                    this.mCheckBox.setId(R.id.set_right);
                    this.mCheckBoxStub.addView(this.mCheckBox);
                    this.mCheckBox.setOnClickListener(this);
                }
                this.mCheckBox.setVisibility(0);
                CheckBox checkBox = this.mCheckBox;
                if (action == Action.MULTI_SELECTION_ON) {
                    z = true;
                }
                checkBox.setChecked(z);
                this.mRightActionItem.setVisibility(4);
            } else if (this.mCheckBox != null) {
                this.mCheckBoxStub.removeView(this.mCheckBox);
                this.mCheckBox = null;
            }
        }
    }
}
