package com.huawei.watermark.manager.parse;

import android.widget.TextView;
import com.huawei.watermark.manager.parse.util.WMHealthyReportService.HealthUpdateCallback;
import com.huawei.watermark.wmdata.wmlogicdata.WMLogicData;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.text.DecimalFormat;
import org.xmlpull.v1.XmlPullParser;

public class WMCalories extends WMText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMCalories.class.getSimpleName());
    private String mCurrentCalories = "";
    private DecoratorHealthRunnable mDecoratorHealthRunnable;
    private TextView mTextView;

    private class DecoratorHealthRunnable implements Runnable {
        private boolean mCancel;
        private HealthUpdateCallback mHealthCallback;

        private DecoratorHealthRunnable() {
            this.mHealthCallback = new HealthUpdateCallback() {
                public void onHealthReport(String step, String calories) {
                    if (calories != null && !calories.equalsIgnoreCase(WMCalories.this.mCurrentCalories)) {
                        WMCalories.this.mCurrentCalories = calories;
                        WMLogicData.getInstance(WMCalories.this.mTextView.getContext()).setHealthTextWithKeyname("HEALTH_ORIGIN_CALORIES_TAG", calories);
                    }
                }
            };
            WMCalories.this.mLogicDelegate.addHealthUpdateCallback(this.mHealthCallback);
        }

        public void run() {
            setShowValue();
            if (!this.mCancel) {
                WMCalories.this.mTextView.postDelayed(this, 1000);
            }
        }

        private void setShowValue() {
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String showValue = "";
            float ratio = 0.0f;
            String userStep = WMLogicData.getInstance(WMCalories.this.mTextView.getContext()).getHealthTextWithKeyname("HEALTH_USER_STEP_TAG");
            String originStep = WMLogicData.getInstance(WMCalories.this.mTextView.getContext()).getHealthTextWithKeyname("HEALTH_ORIGIN_STEP_TAG");
            String originCalories = WMLogicData.getInstance(WMCalories.this.mTextView.getContext()).getHealthTextWithKeyname("HEALTH_ORIGIN_CALORIES_TAG");
            if (!(originStep == null || originStep.isEmpty() || originCalories == null || originCalories.isEmpty() || Float.valueOf(originStep).floatValue() <= 0.001f)) {
                ratio = Float.valueOf(originCalories).floatValue() / Float.valueOf(originStep).floatValue();
            }
            if (userStep == null || userStep.isEmpty()) {
                if (originCalories == null || originCalories.isEmpty()) {
                    showValue = WMCalories.this.text;
                } else {
                    showValue = fnum.format((double) (Float.valueOf(originCalories).floatValue() / 1000.0f));
                }
            } else if (ratio > 0.001f) {
                showValue = fnum.format((double) ((Float.valueOf(userStep).floatValue() * ratio) / 1000.0f));
            } else {
                showValue = fnum.format((double) (Float.valueOf(userStep).floatValue() * 0.04f));
            }
            if (!WMCalories.this.mTextView.getText().toString().equals(showValue)) {
                WMCalories.this.mTextView.setText(showValue);
                WMCalories.this.resetWaterMarkLayoutParams();
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMCalories(XmlPullParser parser) {
        super(parser);
    }

    public void decoratorText(TextView tv) {
        this.mTextView = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setBackgroundResource(WMResourceUtil.getAnimid(this.mTextView.getContext(), "wm_jar_editor_anim"));
        if (this.mDecoratorHealthRunnable != null) {
            this.mDecoratorHealthRunnable.pause();
        }
        this.mDecoratorHealthRunnable = new DecoratorHealthRunnable();
        this.mDecoratorHealthRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorHealthRunnable != null) {
            this.mDecoratorHealthRunnable.pause();
        }
    }
}
