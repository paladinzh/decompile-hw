package com.android.settings.fingerprint.enrollment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Message;
import android.os.SystemProperties;
import android.text.BidiFormatter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.settings.SettingsExtUtils;
import com.android.settings.fingerprint.enrollment.animation.AnimationUtil;
import com.android.settings.fingerprint.enrollment.animation.FingerprintAnimation;
import java.text.NumberFormat;

public class HwCustFingerprintEnrollImpl extends HwCustFingerprintEnroll {
    private static final int ANIMATION_RES_ID = 2131230786;
    private static final boolean DBG = true;
    private static final int FONT_COLOR = 2131427579;
    private static final int FP_TIP_SUCCESS = 100;
    private static final int MARGIN_BOTTOM_ID = 2131559132;
    private static final String TAG = "fingerprint.HwCustFingerprintEnrollImpl";
    private static final float TEXT_FONT_SIZE = 16.0f;
    private Context mContext;
    protected FingerprintAnimation mFingerprintAnimation;
    protected TextView mFingerprintProcessTextView;
    private FingerprintEnrollFragment mFragment;
    private int mStage = -1;

    public HwCustFingerprintEnrollImpl(FingerprintEnrollFragment mFingerprintEnrollFragment) {
        super(mFingerprintEnrollFragment);
        this.mFragment = mFingerprintEnrollFragment;
        AnimationUtil.calculateScale(this.mFragment.mContext);
        AnimationUtil.createSectionData(AnimationUtil.getPaths(this.mFragment.mContext, ANIMATION_RES_ID));
    }

    public HwCustFingerprintEnrollImpl(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    public void handleMessage(Message msg) {
        Log.i(TAG, "handleMessage:msg.what=" + msg.what);
        switch (msg.what) {
            case -1:
                this.mFragment.mTipText.setText(this.mFragment.mRes.getString(2131629273));
                this.mFragment.mFpTipImage.setImageResource(2130837695);
                this.mFragment.mFpTipAnima = (AnimationDrawable) this.mFragment.mFpTipImage.getDrawable();
                this.mFragment.mFpTipAnima.start();
                Activity activity = this.mFragment.getActivity();
                if (activity != null && SettingsExtUtils.isStartupGuideMode(activity.getContentResolver())) {
                    ((RelativeLayout) this.mFingerprintAnimation.getParent()).setVisibility(8);
                }
                this.mFingerprintAnimation.setVisibility(8);
                this.mFingerprintProcessTextView.setText(formatPercentage(0.0d));
                this.mFingerprintProcessTextView.setVisibility(8);
                return;
            default:
                if (msg.what > FP_TIP_SUCCESS) {
                    msg.what = FP_TIP_SUCCESS;
                }
                this.mFingerprintProcessTextView.setText(formatPercentage(((double) msg.what) / 100.0d));
                return;
        }
    }

    public void onInput() {
        Log.i(TAG, "onInput");
        if (this.mStage < 20) {
            this.mFragment.mTitleText.setText(this.mFragment.mRes.getString(2131627691));
            this.mFragment.mTipText.setText(this.mFragment.mRes.getString(2131627910));
            this.mFragment.mTipText.setTextColor(this.mFragment.mContext.getResources().getColor(FONT_COLOR));
            if (this.mStage == -1) {
                this.mFragment.mSubTipText.setVisibility(4);
                Activity activity = this.mFragment.getActivity();
                if (activity != null && SettingsExtUtils.isStartupGuideMode(activity.getContentResolver())) {
                    ((RelativeLayout) this.mFingerprintAnimation.getParent()).setVisibility(0);
                }
                this.mFingerprintAnimation.setVisibility(0);
                this.mFingerprintProcessTextView.setVisibility(0);
                this.mFragment.releaseAd(this.mFragment.mFpTipAnima);
                this.mFragment.mFpTipImage.setVisibility(8);
            }
        }
        this.mFragment.mTipDownText.setVisibility(8);
        this.mFingerprintAnimation.needAnimation(false);
        this.mStage++;
    }

    public void drawTheLast() {
        while (this.mStage + 1 < 20) {
            this.mFingerprintAnimation.needAnimation(false);
            this.mStage++;
        }
    }

    public void onDuplicated() {
        Log.i(TAG, "onDuplicated");
        resetAnimation();
    }

    public void onImageLowQuality() {
        Log.i(TAG, "onImageLowQuality");
        resetAnimation();
        this.mFragment.mTipText.setText(this.mFragment.mRes.getString(2131627674));
        this.mFragment.mTipText.setTextColor(-65536);
    }

    public void onFingerRegionChange() {
        Log.i(TAG, "onFingerRegionChange");
    }

    public void onFingerStill() {
        Log.i(TAG, "onFingerStill");
        resetAnimation();
        this.mFragment.mTipText.setText(this.mFragment.mRes.getString(2131627920) + "\n\b");
        this.mFragment.mTipText.setTextColor(-65536);
    }

    public void onFingerLowCoverge() {
        Log.i(TAG, "onFingerLowCoverge");
        resetAnimation();
        this.mFragment.mTipText.setText(this.mFragment.mRes.getString(2131627673));
        this.mFragment.mTipText.setTextColor(-65536);
    }

    private void resetAnimation() {
        this.mFingerprintAnimation.needAnimation(DBG);
        this.mStage--;
    }

    public void reset() {
        if (this.mFingerprintAnimation != null) {
            this.mFingerprintAnimation.reset();
        }
        if (this.mFingerprintProcessTextView != null) {
            this.mFingerprintProcessTextView.setText(formatPercentage(0.0d));
        }
        this.mStage = -1;
    }

    public void addAnimationView(RelativeLayout parent) {
        FingerprintAnimation mAnimationView = new FingerprintAnimation(this.mFragment.mContext);
        if (SettingsExtUtils.isStartupGuideMode(this.mFragment.getActivity().getContentResolver())) {
            mAnimationView.setBackgroundColor(this.mFragment.mContext.getResources().getColor(2131427575));
            mAnimationView.changeBackGroundColor(this.mFragment.mContext.getResources().getColor(2131427575));
        } else {
            mAnimationView.setBackgroundColor(this.mFragment.mContext.getResources().getColor(2131427578));
        }
        mAnimationView.setVisibility(8);
        LayoutParams lps = new LayoutParams((int) this.mFragment.mContext.getResources().getDimension(2131559153), (int) this.mFragment.mContext.getResources().getDimension(2131559152));
        lps.addRule(14);
        parent.addView(mAnimationView, lps);
        this.mFingerprintAnimation = mAnimationView;
    }

    public void addPercentageView(RelativeLayout parent) {
        TextView mTextView = new TextView(this.mFragment.mContext);
        LayoutParams lps = new LayoutParams(-2, -2);
        lps.addRule(14);
        lps.topMargin = (int) this.mFragment.mContext.getResources().getDimension(2131559156);
        mTextView.setPaddingRelative((int) this.mFragment.mContext.getResources().getDimension(2131559154), 0, (int) this.mFragment.mContext.getResources().getDimension(2131559155), 0);
        mTextView.setTextSize(TEXT_FONT_SIZE);
        mTextView.setVisibility(8);
        mTextView.setTextColor(this.mFragment.mContext.getResources().getColor(FONT_COLOR));
        parent.addView(mTextView, lps);
        this.mFingerprintProcessTextView = mTextView;
    }

    public void destory() {
        if (this.mFingerprintAnimation != null) {
            this.mFingerprintAnimation.destory();
        }
    }

    public void changeImageView(ImageView old) {
        LinearLayout mFpImageParent = (LinearLayout) old.getParent();
        ImageView mNewFpImageView = new ImageView(this.mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) this.mContext.getResources().getDimension(2131559153), (int) this.mContext.getResources().getDimension(2131559152));
        mNewFpImageView.setBackgroundResource(2130837716);
        mNewFpImageView.setScaleType(ScaleType.FIT_CENTER);
        layoutParams.setMargins(0, 0, 0, (int) this.mContext.getResources().getDimension(MARGIN_BOTTOM_ID));
        old.setVisibility(8);
        mFpImageParent.addView(mNewFpImageView, 0, layoutParams);
    }

    public boolean isSupportSepFingerPrint() {
        return SystemProperties.getBoolean("ro.config.spe_fingerprint", false);
    }

    private static String formatPercentage(double percentage) {
        return BidiFormatter.getInstance().unicodeWrap(NumberFormat.getPercentInstance().format(percentage));
    }
}
