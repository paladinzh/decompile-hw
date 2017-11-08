package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class HwMultiSimSendButton extends LinearLayout {
    private TextView mButtonText;
    private ImageView mCardImage;

    public HwMultiSimSendButton(Context context) {
        this(context, null);
    }

    public HwMultiSimSendButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwMultiSimSendButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCardImage = (ImageView) findViewById(R.id.button_image);
        if (this.mCardImage == null) {
            MLog.e("HwMultiSimSendButton", "onFinishInflate: mCardImage is null");
        } else {
            this.mCardImage.setVisibility(8);
        }
        this.mButtonText = (TextView) findViewById(R.id.button_text);
    }

    public void setText(CharSequence text) {
        if (this.mButtonText == null) {
            MLog.e("HwMultiSimSendButton", "setText: mButtonText is null");
        } else {
            this.mButtonText.setText(text);
        }
    }

    public void setLeftDrawables(int resId) {
        if (this.mCardImage == null) {
            MLog.e("HwMultiSimSendButton", "setLeftDrawables: mCardImage is null");
            return;
        }
        this.mCardImage.setBackgroundResource(resId);
        this.mCardImage.setVisibility(0);
    }

    public void setCurSimIndicatorVisibility(int visibility) {
        if (visibility == 0) {
            setBackgroundResource(R.drawable.recommend_button_bg);
        } else {
            setBackgroundResource(R.drawable.mms_btn_call);
        }
    }
}
