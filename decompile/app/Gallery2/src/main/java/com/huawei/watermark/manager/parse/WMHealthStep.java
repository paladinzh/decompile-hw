package com.huawei.watermark.manager.parse;

import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.util.WMHealthyReportService.HealthUpdateCallback;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.ui.WMEditor.OnTextChangedListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMHealthStep extends WMText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMHealthStep.class.getSimpleName());
    private String mCurrentStepValue = "";
    private DecoratorHealthRunnable mDecoratorHealthRunnable;
    private TextView mTextView;
    private WMEditor mWmEditor;

    private class DecoratorHealthRunnable implements Runnable {
        private boolean mCancel;
        private HealthUpdateCallback mHealthCallback;

        private DecoratorHealthRunnable() {
            this.mHealthCallback = new HealthUpdateCallback() {
                public void onHealthReport(String step, String calories) {
                    if (DecoratorHealthRunnable.this.mCancel) {
                        WMHealthStep.this.mCurrentStepValue = "";
                        return;
                    }
                    WMHealthStep.this.mCurrentStepValue = step;
                    WMLogicData.getInstance(WMHealthStep.this.mTextView.getContext()).setHealthTextWithKeyname("HEALTH_ORIGIN_STEP_TAG", step);
                }
            };
            WMHealthStep.this.mLogicDelegate.addHealthUpdateCallback(this.mHealthCallback);
        }

        public void run() {
            String newValue;
            String storeValue = WMLogicData.getInstance(WMHealthStep.this.mTextView.getContext()).getHealthTextWithKeyname("HEALTH_USER_STEP_TAG");
            if (storeValue != null && !storeValue.isEmpty()) {
                newValue = storeValue;
            } else if (WMHealthStep.this.mCurrentStepValue == null || WMHealthStep.this.mCurrentStepValue.isEmpty()) {
                newValue = WMHealthStep.this.text;
            } else {
                newValue = WMHealthStep.this.mCurrentStepValue;
            }
            if (!WMHealthStep.this.mTextView.getText().toString().equals(newValue)) {
                WMHealthStep.this.mTextView.setText(newValue);
            }
            if (!this.mCancel) {
                WMHealthStep.this.mTextView.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }

        public void pauseAndRestore() {
            this.mCancel = true;
            WMHealthStep.this.mCurrentStepValue = "";
        }
    }

    public WMHealthStep(XmlPullParser parser) {
        super(parser);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
        if (this.mDecoratorHealthRunnable != null) {
            this.mDecoratorHealthRunnable.pauseAndRestore();
        }
        this.mDecoratorHealthRunnable = new DecoratorHealthRunnable();
        this.mDecoratorHealthRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mWmEditor != null) {
            this.mWmEditor.pause();
        }
        if (this.mDecoratorHealthRunnable != null) {
            this.mDecoratorHealthRunnable.pauseAndRestore();
        }
    }

    public void onWaterMarkClicked(float x, float y) {
        if (clickOnElement(x, y, this.mTextView)) {
            if (this.mDecoratorHealthRunnable != null) {
                this.mDecoratorHealthRunnable.pause();
            }
            this.mWmEditor = new WMEditor(this.mTextView.getContext(), this.mTextView.getText().toString(), WMEditor.TYPENUM, true, 6, new OnTextChangedListener() {
                public void onTextChanged(String text) {
                    WMLogicData.getInstance(WMHealthStep.this.mTextView.getContext()).setHealthTextWithKeyname("HEALTH_USER_STEP_TAG", text);
                    WMHealthStep.this.mTextView.setText(text);
                    WMHealthStep.this.mCurrentStepValue = "";
                    WMHealthStep.this.resetWaterMarkLayoutParams();
                }

                public void onDialogDismissed() {
                    WMHealthStep.this.mDecoratorHealthRunnable = new DecoratorHealthRunnable();
                    WMHealthStep.this.mDecoratorHealthRunnable.run();
                }
            }, this.mLogicDelegate);
        }
    }

    public void showAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        if (animationDrawable == null) {
            this.mTextView.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
            animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        }
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        animationDrawable.start();
    }

    public void hideAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mTextView.getBackground();
        if (animationDrawable != null) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            this.mTextView.setBackground(null);
        }
    }
}
