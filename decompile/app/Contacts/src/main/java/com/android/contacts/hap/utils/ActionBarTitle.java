package com.android.contacts.hap.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.android.gms.R;
import java.text.NumberFormat;

public class ActionBarTitle {
    private View mActionBarTitleView;
    private OnClickListener mBackListener;
    private ImageView mBackView;
    private OnClickListener mCancelListener;
    private ImageView mCancelView;
    private Context mContext;
    private boolean mIsCancelViewVisible;
    private boolean mIsOkViewVisible;
    private ImageView mOkView;
    private TextView mTitle;
    private TextView mTitleNumber;
    private View mTitleholder;
    private TextView mTitleleft;

    public ActionBarTitle(Context context, View actionBarTitle) {
        this.mContext = context;
        this.mActionBarTitleView = actionBarTitle;
        initViews();
    }

    private void initViews() {
        this.mTitleleft = (TextView) this.mActionBarTitleView.findViewById(R.id.title_left);
        this.mTitleholder = this.mActionBarTitleView.findViewById(R.id.title_holder);
        this.mTitle = (TextView) this.mActionBarTitleView.findViewById(R.id.title);
        this.mTitleNumber = (TextView) this.mActionBarTitleView.findViewById(R.id.title_number);
        this.mBackView = (ImageView) this.mActionBarTitleView.findViewById(R.id.bt_back);
        this.mCancelView = (ImageView) this.mActionBarTitleView.findViewById(R.id.bt_cancel);
        this.mOkView = (ImageView) this.mActionBarTitleView.findViewById(R.id.bt_ok);
    }

    public void setBackIconVisible(boolean isBackVisible) {
        if (isBackVisible) {
            this.mBackView.setVisibility(0);
        } else {
            this.mBackView.setVisibility(8);
        }
    }

    public void setStartIconVisible(boolean isCancelVisible) {
        this.mIsCancelViewVisible = isCancelVisible;
        triggerIconsVisible(this.mIsCancelViewVisible, this.mIsOkViewVisible);
    }

    public void triggerIconsVisible(boolean isCancelVisible, boolean isOkVisible) {
        int i = 4;
        if (isCancelVisible) {
            this.mCancelView.setVisibility(0);
        } else {
            this.mCancelView.setVisibility(isOkVisible ? 4 : 8);
        }
        if (isOkVisible) {
            this.mOkView.setVisibility(0);
            return;
        }
        ImageView imageView = this.mOkView;
        if (!isCancelVisible) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    public void setBackIconImage(Drawable backIcon) {
        if (backIcon != null) {
            this.mBackView.setImageDrawable(backIcon);
        }
    }

    public void setStartIconImage(Drawable cancelIcon) {
        if (cancelIcon != null) {
            this.mCancelView.setImageDrawable(cancelIcon);
        }
    }

    public void setBackIconListener(OnClickListener backListener) {
        if (backListener != null) {
            this.mBackListener = backListener;
            this.mBackView.setOnClickListener(this.mBackListener);
        }
    }

    public void setStartIconListener(OnClickListener cancelListener) {
        if (cancelListener != null) {
            this.mCancelListener = cancelListener;
            this.mCancelView.setOnClickListener(this.mCancelListener);
        }
    }

    public void setBackIcon(boolean isBackVisible, Drawable backIcon, OnClickListener backListener) {
        setBackIconVisible(isBackVisible);
        setBackIconImage(backIcon);
        setBackIconListener(backListener);
    }

    public void setStartIcon(boolean isCancelVisible, Drawable cancelIcon, OnClickListener cancelListener) {
        setStartIconVisible(isCancelVisible);
        setStartIconImage(cancelIcon);
        setStartIconListener(cancelListener);
    }

    public void setTitle(CharSequence titleleft) {
        if (titleleft != null) {
            this.mTitleleft.setText(titleleft);
            this.mTitleleft.setVisibility(0);
        }
    }

    public void setTitleVisible(boolean bVisible) {
        if (bVisible) {
            this.mTitleleft.setVisibility(0);
        } else {
            this.mTitleleft.setVisibility(8);
        }
    }

    public void setTitleMiddle(CharSequence title) {
        if (this.mTitle != null) {
            this.mTitle.setText(title);
            this.mTitleholder.setVisibility(0);
        }
        if (this.mTitleNumber != null) {
            this.mTitleNumber.setVisibility(8);
        }
    }

    public void setTitleMiddleVisible(boolean bVisible) {
        if (bVisible) {
            this.mTitleholder.setVisibility(0);
        } else {
            this.mTitleholder.setVisibility(8);
        }
    }

    public void setTitleMiddle(CharSequence title, int size) {
        if (this.mTitleNumber != null && this.mTitle != null) {
            if (size == 0) {
                setTitleMiddle(title);
                return;
            }
            ImmersionUtils.setTextViewOrEditViewImmersonColorLight(this.mContext, this.mTitle, false);
            ImmersionUtils.setTextViewOrEditViewImmersonColorLight(this.mContext, this.mTitleNumber, false);
            if (ImmersionUtils.getImmersionStyle(this.mContext) == 1) {
                this.mTitleNumber.setBackgroundResource(R.drawable.csp_actionbar_number_circle_light);
            }
            if (this.mTitleNumber.getVisibility() != 0) {
                this.mTitleNumber.setVisibility(0);
            }
            this.mTitleholder.setVisibility(0);
            this.mTitle.setText(title);
            String strSize = NumberFormat.getIntegerInstance().format((long) size);
            if (size > 10) {
                this.mTitleNumber.setText(HwCustPreloadContacts.EMPTY_STRING + strSize + HwCustPreloadContacts.EMPTY_STRING);
            } else {
                this.mTitleNumber.setText("" + strSize);
            }
        }
    }
}
