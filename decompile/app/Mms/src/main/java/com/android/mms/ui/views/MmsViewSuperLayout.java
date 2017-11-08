package com.android.mms.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.views.MmsPopView.MmsPopViewCallback;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class MmsViewSuperLayout extends LinearLayout {
    private MmsPopView mChildView = null;
    private int mLastChildType = 1000;
    private int mLastChildViewSize = -1;
    private MessageItem mMessageItem = null;
    private MmsPopViewCallback mMmsPopViewClickCallback;

    public MmsViewSuperLayout(Context context) {
        super(context);
    }

    public MmsViewSuperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MmsViewSuperLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void show() {
        setVisibility(0);
        setClickable(true);
    }

    public void hide() {
        setClickable(false);
        setVisibility(8);
    }

    public void bind(MessageItem messageItem) {
        removeAllViews();
        this.mMessageItem = messageItem;
        addChildMmsPopView();
    }

    private void addChildMmsPopView() {
        if (this.mMessageItem.mSlideshow == null) {
            setMmsViewMinSize(1000);
            show();
            return;
        }
        if (this.mMessageItem.hasVCalendar() || this.mMessageItem.isHasVcard()) {
            addChildViewByFlag(1001);
        } else if (this.mMessageItem.mAttachmentType != 0) {
            addChildViewByFlag(1002);
        }
        setBottomMargin();
    }

    public void setMmsViewMinSize(int flag) {
        int currentMmsViewSize = getMmsViewSizeByFlag(flag);
        if (this.mLastChildViewSize != currentMmsViewSize || currentMmsViewSize == -1) {
            int[] widthAndHegiht;
            switch (flag) {
                case 1001:
                    setMinimumWidth(0);
                    setMinimumHeight(0);
                    this.mLastChildViewSize = 0;
                    break;
                case 1002:
                    if (this.mMessageItem.mAttachmentType != 1 && this.mMessageItem.mAttachmentType != 2 && !this.mMessageItem.mHasImageInFirstSlidShow) {
                        setMinimumWidth(getContext().getResources().getDimensionPixelSize(R.dimen.mms_img_min_width));
                        setMinimumHeight(getContext().getResources().getDimensionPixelSize(R.dimen.mms_img_min_width));
                        this.mLastChildViewSize = 1;
                        break;
                    }
                    widthAndHegiht = MessageUtils.getImgWidthAndHeight(100, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, getContext());
                    setMinimumWidth(widthAndHegiht[0]);
                    setMinimumHeight(widthAndHegiht[1]);
                    this.mLastChildViewSize = 2;
                    break;
                    break;
                default:
                    widthAndHegiht = MessageUtils.getImgWidthAndHeight(100, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, getContext());
                    setMinimumWidth(widthAndHegiht[0]);
                    setMinimumHeight(widthAndHegiht[1]);
                    this.mLastChildViewSize = 2;
                    break;
            }
        }
    }

    public int getMmsViewSizeByFlag(int flag) {
        switch (flag) {
            case 1001:
                return 0;
            case 1002:
                if (this.mMessageItem.mAttachmentType == 1 || this.mMessageItem.mAttachmentType == 2 || this.mMessageItem.mHasImageInFirstSlidShow) {
                    return 2;
                }
                return 1;
            default:
                return -1;
        }
    }

    public void setBottomMargin() {
        if (getChildCount() == 0) {
            hide();
        } else if (this.mMessageItem != null) {
            show();
            LayoutParams layoutParams = (LayoutParams) getLayoutParams();
            if (getChildCount() <= 0 || !this.mMessageItem.hasText()) {
                layoutParams.bottomMargin = 0;
            } else {
                layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.mms_sms_pop_margin);
            }
            setLayoutParams(layoutParams);
        }
    }

    private void addChildViewByFlag(int flag) {
        setMmsViewMinSize(flag);
        if (this.mLastChildType != flag || this.mChildView == null) {
            if (this.mChildView != null) {
                this.mChildView = null;
            }
            int layoutId = -1;
            switch (flag) {
                case 1001:
                    layoutId = R.layout.vattch_layout_msgitem;
                    break;
                case 1002:
                    layoutId = R.layout.mms_layout_view;
                    break;
            }
            if (layoutId == -1) {
                MLog.e("MmsViewSuperLayout", "add child view, but layoutId is -1, use wrong res");
                return;
            }
            MmsPopView childView = (MmsPopView) View.inflate(getContext(), layoutId, null);
            childView.setMmsPopViewClickCallback(this.mMmsPopViewClickCallback);
            childView.bind(this.mMessageItem);
            childView.setClickable(true);
            addView(childView);
            this.mLastChildType = flag;
            this.mChildView = childView;
            return;
        }
        this.mChildView.bind(this.mMessageItem);
        addView(this.mChildView);
    }

    public void setOnMmsPopViewDoubleClickCallback(MmsPopViewCallback mmsPopViewClickCallback) {
        this.mMmsPopViewClickCallback = mmsPopViewClickCallback;
    }
}
