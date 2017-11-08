package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.R;

public class NoMessageView extends LinearLayout {
    RelativeLayout mImageLayout = null;
    LinearLayout mImageSuper = null;
    ImageView mImageView = null;
    private boolean mIsInMultiWindowMode = false;
    TextView mTextView = null;
    private int mType = 0;

    public NoMessageView(Context context) {
        super(context);
    }

    public NoMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initRes();
        if (getContext() != null && (getContext() instanceof Activity)) {
            this.mIsInMultiWindowMode = ((Activity) getContext()).isInMultiWindowMode();
        }
        resetImageLayout(getOrientation());
    }

    public void setInMultiWindowMode(boolean isInMultiWindowMode) {
        boolean tempMultiWindowMode = this.mIsInMultiWindowMode;
        this.mIsInMultiWindowMode = isInMultiWindowMode;
        if (tempMultiWindowMode && !isInMultiWindowMode) {
            resetImageLayout(getOrientation());
        }
    }

    public void setViewType(int type) {
        this.mType = type;
        initRes();
    }

    private void initRes() {
        this.mImageView = (ImageView) findViewById(R.id.hint_image);
        this.mTextView = (TextView) findViewById(R.id.hint_text);
        this.mImageLayout = (RelativeLayout) findViewById(R.id.no_message_image_layout);
        this.mImageSuper = (LinearLayout) findViewById(R.id.no_message_image_super);
        if (this.mType != 0) {
            switch (this.mType) {
                case 1:
                    setIcon(R.drawable.ic_empty_message);
                    setText(R.string.has_no_conversations);
                    break;
                case 2:
                    setIcon(R.drawable.ic_empty_message);
                    setText(R.string.hint_has_no_favorites);
                    break;
                case 3:
                case 4:
                    setIcon(R.drawable.ic_empty_message);
                    setText(R.string.no_message);
                    break;
                case 5:
                    setIcon(R.drawable.ic_empty_message);
                    setText(R.string.search_empty);
                    break;
            }
        }
    }

    private void setText(int titleId) {
        if (this.mTextView != null) {
            this.mTextView.setText(titleId);
        }
    }

    private void setIcon(int resId) {
        if (this.mImageView != null) {
            this.mImageView.setBackground(getResources().getDrawable(resId));
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetImageLayout(newConfig.orientation);
    }

    public void resetImageLayout(int orientation) {
        LayoutParams layoutParams = (LayoutParams) this.mImageLayout.getLayoutParams();
        RelativeLayout.LayoutParams imageSuperParams = new RelativeLayout.LayoutParams(-2, -2);
        Resources resources = getResources();
        if (this.mIsInMultiWindowMode) {
            layoutParams.topMargin = resources.getDimensionPixelOffset(R.dimen.mms_nomessageview_height_top_margin_multiwindow);
            imageSuperParams.addRule(13);
        } else {
            layoutParams.topMargin = resources.getDimensionPixelOffset(R.dimen.mms_nomessageview_height);
            imageSuperParams.addRule(14);
        }
        this.mImageSuper.setLayoutParams(imageSuperParams);
        this.mImageLayout.setLayoutParams(layoutParams);
    }

    public void setVisibility(int visibility, boolean isInMultiWindowMode) {
        this.mIsInMultiWindowMode = isInMultiWindowMode;
        setVisibility(visibility);
    }
}
