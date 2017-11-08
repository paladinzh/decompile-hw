package com.huawei.watermark.manager.parse;

import android.graphics.drawable.AnimationDrawable;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.util.WMAltitudeService.AltitudeUpdateCallback;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.ui.WMEditor.OnTextChangedListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMAltitude extends WMText {
    private String mCurrentAltitude = "";
    DecoratorAltitudeRunnable mDecoratorAltitudeRunnable;
    private TextView mTextView;
    private WMEditor mWmEditor;

    private class DecoratorAltitudeRunnable implements Runnable {
        private AltitudeUpdateCallback mAltitudeUpdateCallback;
        private boolean mCancel;

        private DecoratorAltitudeRunnable() {
            this.mAltitudeUpdateCallback = new AltitudeUpdateCallback() {
                public void onAltitudeReport(String altitude) {
                    WMAltitude.this.mCurrentAltitude = altitude;
                }
            };
            WMAltitude.this.mLogicDelegate.addAltitudeUpdateCallback(this.mAltitudeUpdateCallback);
        }

        public void run() {
            String newValue;
            String storeValue = WMLogicData.getInstance(WMAltitude.this.mTextView.getContext()).getHealthTextWithKeyname("ALTITUDE_TAG");
            if (storeValue != null && !storeValue.isEmpty()) {
                newValue = storeValue;
            } else if (WMAltitude.this.mCurrentAltitude == null || WMAltitude.this.mCurrentAltitude.isEmpty()) {
                newValue = WMAltitude.this.text;
            } else {
                newValue = WMAltitude.this.mCurrentAltitude;
            }
            if (!WMAltitude.this.mTextView.getText().toString().equals(newValue)) {
                WMAltitude.this.mTextView.setText(newValue);
                WMAltitude.this.resetWaterMarkLayoutParams();
            }
            if (!this.mCancel) {
                WMAltitude.this.mTextView.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }

        public void pauseAndRestore() {
            this.mCancel = true;
            WMAltitude.this.mCurrentAltitude = "";
        }
    }

    public WMAltitude(XmlPullParser parser) {
        super(parser);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
        if (this.mDecoratorAltitudeRunnable != null) {
            this.mDecoratorAltitudeRunnable.pauseAndRestore();
        }
        this.mDecoratorAltitudeRunnable = new DecoratorAltitudeRunnable();
        this.mDecoratorAltitudeRunnable.run();
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

    public void pause() {
        super.pause();
        if (this.mWmEditor != null) {
            this.mWmEditor.pause();
        }
        if (this.mDecoratorAltitudeRunnable != null) {
            this.mDecoratorAltitudeRunnable.pauseAndRestore();
        }
    }

    public void onWaterMarkClicked(float x, float y) {
        if (clickOnElement(x, y, this.mTextView)) {
            if (this.mDecoratorAltitudeRunnable != null) {
                this.mDecoratorAltitudeRunnable.pause();
            }
            this.mWmEditor = new WMEditor(this.mTextView.getContext(), this.mTextView.getText().toString(), WMEditor.TYPESIGNEDNUM, true, 5, new OnTextChangedListener() {
                public void onTextChanged(String text) {
                    WMLogicData.getInstance(WMAltitude.this.mTextView.getContext()).setAltitudeTextWithKeyname("ALTITUDE_TAG", text);
                    WMAltitude.this.mTextView.setText(text);
                    WMAltitude.this.mCurrentAltitude = "";
                    WMAltitude.this.resetWaterMarkLayoutParams();
                }

                public void onDialogDismissed() {
                    WMAltitude.this.mDecoratorAltitudeRunnable = new DecoratorAltitudeRunnable();
                    WMAltitude.this.mDecoratorAltitudeRunnable.run();
                }
            }, this.mLogicDelegate);
        }
    }
}
