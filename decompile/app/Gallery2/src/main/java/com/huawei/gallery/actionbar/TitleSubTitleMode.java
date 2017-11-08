package com.huawei.gallery.actionbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.view.SimpleActionItem;
import com.huawei.gallery.util.ImmersionUtils;

public class TitleSubTitleMode extends AbstractTitleMode {
    private String mCountStr = null;
    private TextView mCountView = null;
    private TextView mSubTitle = null;
    private int mSubTitleRes = -1;

    protected void resume(Bundle resumeData) {
        super.resume(resumeData);
        this.mCountStr = resumeData.getString("COUNT_KEY");
    }

    protected Bundle saveState() {
        Bundle data = super.saveState();
        if (this.mCountStr != null) {
            data.putString("COUNT_KEY", this.mCountStr);
        }
        return data;
    }

    public int getMode() {
        return 6;
    }

    private void setCount(String countStr) {
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
        }
    }

    private void updateSubTitle() {
        if (this.mSubTitle != null && this.mSubTitleRes > 0) {
            this.mSubTitle.setText(this.mSubTitleRes);
        }
    }

    protected void initViewItem() {
        this.mMainView = (ViewGroup) this.mActivity.getLayoutInflater().inflate(R.layout.headview_title_subtitle, null);
        this.mLeftActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_left);
        this.mLeftActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
        this.mLeftActionItem.setAction(this.mLeftAction);
        this.mLeftActionItem.setOnClickListener(this);
        this.mRightActionItem = (SimpleActionItem) this.mMainView.findViewById(R.id.head_select_right);
        this.mRightActionItem.applyStyle(this.mActionBarMenuManager.getStyle());
        this.mRightActionItem.setAction(this.mRightAction);
        this.mRightActionItem.setOnClickListener(this);
        this.mTitleView = (TextView) this.mMainView.findViewById(R.id.main_title_text);
        setTitleInternal();
        this.mCountView = (TextView) this.mMainView.findViewById(R.id.main_title_count);
        GalleryUtils.setTypeFaceAsSlim(this.mCountView);
        setCount(this.mCountStr);
        ImmersionUtils.setTextViewDefaultColorImmersionStyle(this.mCountView, this.mActionBarMenuManager.getStyle());
        ImmersionUtils.setViewBackgroundImmersionStyle(this.mCountView, R.drawable.ic_choosed_number_bg_light, R.drawable.ic_choosed_number_bg, this.mActionBarMenuManager.getStyle());
        this.mSubTitle = (TextView) this.mMainView.findViewById(R.id.sub_title);
        updateSubTitle();
        ImmersionUtils.setTextViewColorImmersionStyle(this.mSubTitle, R.color.title_sub_title_text_enable_color_light, R.color.title_sub_title_text_enable_color);
    }
}
