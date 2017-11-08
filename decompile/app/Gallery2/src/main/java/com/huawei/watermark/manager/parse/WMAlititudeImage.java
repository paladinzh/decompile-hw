package com.huawei.watermark.manager.parse;

import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import com.huawei.watermark.manager.parse.util.WMAltitudeService.AltitudeUpdateCallback;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.ui.WMEditor.OnTextChangedListener;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMResourceUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMAlititudeImage extends WMTextShowWithImageBaseLayout {
    protected static final String TAG = ("WM_" + WMAlititudeImage.class.getSimpleName());
    private String mCurrentAltitude = "";
    DecoratorAltitudeRunnable mDecoratorAltitudeRunnable;
    private WMEditor mWmEditor;

    private class DecoratorAltitudeRunnable implements Runnable {
        private AltitudeUpdateCallback mAltitudeUpdateCallback;
        private boolean mCancel;
        private String mShowValue;

        private DecoratorAltitudeRunnable() {
            this.mShowValue = "";
            this.mAltitudeUpdateCallback = new AltitudeUpdateCallback() {
                public void onAltitudeReport(String altitude) {
                    WMAlititudeImage.this.mCurrentAltitude = altitude;
                }
            };
            WMAlititudeImage.this.mLogicDelegate.addAltitudeUpdateCallback(this.mAltitudeUpdateCallback);
        }

        public void run() {
            String storeValue = WMLogicData.getInstance(WMAlititudeImage.this.mBaseview.getContext()).getHealthTextWithKeyname("ALTITUDE_TAG");
            if (storeValue != null) {
                if (!storeValue.equals(this.mShowValue)) {
                    this.mShowValue = storeValue;
                    WMAlititudeImage.this.text = storeValue;
                    WMAlititudeImage.this.showContent();
                    WMAlititudeImage.this.resetWaterMarkLayoutParams();
                }
            } else if (WMAlititudeImage.this.mCurrentAltitude == null || WMAlititudeImage.this.mCurrentAltitude.isEmpty()) {
                if (!WMAlititudeImage.this.text.equals(this.mShowValue)) {
                    this.mShowValue = WMAlititudeImage.this.text;
                    WMAlititudeImage.this.showContent();
                    WMAlititudeImage.this.resetWaterMarkLayoutParams();
                }
            } else if (!WMAlititudeImage.this.mCurrentAltitude.equals(this.mShowValue)) {
                WMAlititudeImage.this.text = WMAlititudeImage.this.mCurrentAltitude;
                this.mShowValue = WMAlititudeImage.this.mCurrentAltitude;
                WMAlititudeImage.this.showContent();
                WMAlititudeImage.this.resetWaterMarkLayoutParams();
            }
            if (!this.mCancel) {
                WMAlititudeImage.this.mBaseview.postDelayed(this, 1000);
            }
        }

        public void resume() {
            this.mCancel = false;
        }

        public void pause() {
            this.mCancel = true;
        }

        public void pauseAndRestore() {
            this.mCancel = true;
            WMAlititudeImage.this.mCurrentAltitude = "";
        }
    }

    public WMAlititudeImage(XmlPullParser parser) {
        super(parser);
    }

    protected void refreshNumValue() {
        Log.d(TAG, "refreshNumValue");
        if (this.mDecoratorAltitudeRunnable == null) {
            this.mDecoratorAltitudeRunnable = new DecoratorAltitudeRunnable();
        }
        this.mDecoratorAltitudeRunnable.resume();
        this.mDecoratorAltitudeRunnable.run();
    }

    public void showAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mBaseview.getBackground();
        if (animationDrawable == null) {
            this.mBaseview.setBackgroundResource(WMResourceUtil.getAnimid(this.mBaseview.getContext(), "wm_jar_editor_anim_for_text_to_view"));
            animationDrawable = (AnimationDrawable) this.mBaseview.getBackground();
        }
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        animationDrawable.start();
    }

    public void hideAnimationTips() {
        AnimationDrawable animationDrawable = (AnimationDrawable) this.mBaseview.getBackground();
        if (animationDrawable != null) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            this.mBaseview.setBackground(null);
        }
    }

    public void onWaterMarkClicked(float x, float y) {
        if (clickOnElement(x, y, this.mBaseview)) {
            if (this.mDecoratorAltitudeRunnable != null) {
                this.mDecoratorAltitudeRunnable.pause();
            }
            this.mWmEditor = new WMEditor(this.mBaseview.getContext(), this.text, WMEditor.TYPESIGNEDNUM, true, 5, new OnTextChangedListener() {
                public void onTextChanged(String text) {
                    WMLogicData.getInstance(WMAlititudeImage.this.mBaseview.getContext()).setAltitudeTextWithKeyname("ALTITUDE_TAG", text);
                    WMAlititudeImage.this.text = text;
                    WMAlititudeImage.this.mCurrentAltitude = "";
                    WMAlititudeImage.this.showContent();
                }

                public void onDialogDismissed() {
                    if (WMAlititudeImage.this.mDecoratorAltitudeRunnable == null) {
                        WMAlititudeImage.this.mDecoratorAltitudeRunnable = new DecoratorAltitudeRunnable();
                    }
                    WMAlititudeImage.this.mDecoratorAltitudeRunnable.resume();
                    WMAlititudeImage.this.mDecoratorAltitudeRunnable.run();
                }
            }, this.mLogicDelegate);
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
}
