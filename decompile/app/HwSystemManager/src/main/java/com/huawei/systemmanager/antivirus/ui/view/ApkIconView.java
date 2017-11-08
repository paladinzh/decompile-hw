package com.huawei.systemmanager.antivirus.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.comm.misc.Utility;

public class ApkIconView extends ImageView {
    private static final int IMAGE_PADDING = 19;
    private float mImagePadding = dip2px(19.0f);
    private int mIndex;
    private AnimationUpdateListener mListener = null;
    private float mSPosStep0 = 0.0f;
    private float mSPosStep1 = (this.mSPosStep0 + dip2px(35.0f));
    private float mSPosStep2 = (this.mSPosStep1 + dip2px(35.0f));
    private float mSPosStep3 = (this.mSPosStep2 + dip2px(39.0f));
    private float mSPosStep4 = (this.mSPosStep3 + dip2px(43.0f));
    private float mSPosStep5 = (this.mSPosStep4 + dip2px(39.0f));

    public interface AnimationUpdateListener {
        void onComplete(ApkIconView apkIconView);
    }

    public ApkIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(AnimationUpdateListener listen) {
        this.mListener = listen;
    }

    public void setAnmationData(int index, float changed) {
        this.mIndex = index;
        onChange(changed);
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    private void onChange(float changed) {
        switch (this.mIndex) {
            case 0:
                onDrawifStep0(changed);
                break;
            case 1:
                onDrawifStep1(changed);
                break;
            case 2:
                onDrawifStep2(changed);
                break;
            case 3:
                onDrawifStep3(changed);
                break;
            case 4:
                onDrawifStep4(changed);
                break;
            case 5:
                onDrawifStep5(changed);
                break;
            default:
                return;
        }
        if (this.mListener != null) {
            this.mListener.onComplete(this);
        }
    }

    private void onDrawifStep0(float changed) {
        float alpha;
        float sPos = this.mSPosStep0;
        float ePos = this.mSPosStep1;
        float tranX = sPos + ((ePos - sPos) * changed);
        setTranslationX(tranX);
        if (tranX < this.mImagePadding) {
            alpha = 0.1f;
        } else {
            alpha = (tranX / this.mImagePadding) + ((this.mImagePadding - ePos) / this.mImagePadding);
        }
        setAlpha(alpha);
    }

    private void onDrawifStep1(float changed) {
        float sPos = this.mSPosStep1;
        setTranslationX(sPos + ((this.mSPosStep2 - sPos) * changed));
        float scaleXY = Utility.ALPHA_MAX + (0.25f * changed);
        setScaleX(scaleXY);
        setScaleY(scaleXY);
        setAlpha(Utility.ALPHA_MAX);
    }

    private void onDrawifStep2(float changed) {
        float sPos = this.mSPosStep2;
        setTranslationX(sPos + ((this.mSPosStep3 - sPos) * changed));
        float scaleXY = 1.25f + (0.25f * changed);
        setScaleX(scaleXY);
        setScaleY(scaleXY);
        setAlpha(Utility.ALPHA_MAX);
    }

    private void onDrawifStep3(float changed) {
        float sPos = this.mSPosStep3;
        setTranslationX(sPos + ((this.mSPosStep4 - sPos) * changed));
        float scaleXY = 1.5f + (-0.25f * changed);
        setScaleX(scaleXY);
        setScaleY(scaleXY);
        setAlpha(Utility.ALPHA_MAX);
    }

    private void onDrawifStep4(float changed) {
        float sPos = this.mSPosStep4;
        setTranslationX(sPos + ((this.mSPosStep5 - sPos) * changed));
        float scaleXY = 1.25f + (-0.25f * changed);
        setScaleX(scaleXY);
        setScaleY(scaleXY);
        setAlpha(Utility.ALPHA_MAX);
    }

    private void onDrawifStep5(float changed) {
        float alpha;
        float sPos = this.mSPosStep5;
        float ePos = this.mSPosStep5 + dip2px(35.0f);
        float tranX = sPos + ((ePos - sPos) * changed);
        setTranslationX(tranX);
        if (tranX < sPos || tranX > ePos - this.mImagePadding) {
            alpha = 0.0f;
        } else {
            alpha = Utility.ALPHA_MAX - ((tranX - sPos) / ((ePos - sPos) - this.mImagePadding));
        }
        setAlpha(alpha);
    }

    public float dip2px(float dipValue) {
        return (dipValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f;
    }
}
