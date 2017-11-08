package com.huawei.gallery.actionbar;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.view.SimpleActionItem;

public class ActionMode extends AbstractTitleMode implements OnClickListener {
    protected View mHeadBarSplitLine;

    public int getMode() {
        return 2;
    }

    protected void initViewItem() {
        clearView(0);
        clearView(1);
        this.mMainView = new RelativeLayout(this.mActivity);
        this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_action, this.mMainView);
        this.mLeftActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_left);
        this.mRightActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_right);
        this.mTitleView = (TextView) this.mMainView.findViewById(R.id.head_actionmode_title);
        this.mTitleView.setGravity(this.mGravity);
        this.mHeadBarSplitLine = this.mMainView.findViewById(R.id.head_bar_split_line);
        putView(1, this.mLeftActionItem.hashCode(), this.mLeftActionItem);
        putView(1, this.mRightActionItem.hashCode(), this.mRightActionItem);
        putView(1, this.mTitleView.hashCode(), this.mTitleView);
        this.mShadowRootView = this.mActivity.getLayoutInflater().inflate(R.layout.headview_state_action, null);
        putView(0, this.mLeftActionItem.hashCode(), this.mShadowRootView.findViewById(R.id.head_select_left));
        putView(0, this.mRightActionItem.hashCode(), this.mShadowRootView.findViewById(R.id.head_select_right));
        View titleView = this.mShadowRootView.findViewById(R.id.head_actionmode_title);
        titleView.setVisibility(4);
        putView(0, this.mTitleView.hashCode(), titleView);
        setSupportDoubleFace(this.mIsSupportDoubleFace);
        this.mMainView.addView(this.mShadowRootView, 0);
        setLeftAction(this.mLeftAction);
        this.mLeftActionItem.setOnClickListener(this);
        setRightAction(this.mRightAction);
        this.mRightActionItem.setOnClickListener(this);
        this.mTitleView.setClickable(true);
        setTitleInternal();
    }

    public void setHeadBarSplitLineVisibility(boolean isVisible) {
        if (this.mHeadBarSplitLine != null) {
            this.mHeadBarSplitLine.setVisibility(isVisible ? 0 : 4);
        }
    }
}
