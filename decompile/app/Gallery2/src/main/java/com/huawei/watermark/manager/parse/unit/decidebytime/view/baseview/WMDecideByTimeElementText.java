package com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview;

import android.content.Context;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMText;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeBaseLogic;
import com.huawei.watermark.wmutil.WMUIUtil;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public abstract class WMDecideByTimeElementText extends WMText {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMDecideByTimeElementText.class.getSimpleName());
    private DecoratorFoodRunnable mDecoratorFoodRunnable;
    private TextView mTV;
    public WMDecideByTimeBaseLogic mWMDecideByTimeLogic;

    private class DecoratorFoodRunnable implements Runnable {
        private boolean mCancel;

        private DecoratorFoodRunnable() {
        }

        public void run() {
            String value = WMDecideByTimeElementText.this.getValueWithTime();
            if (!WMDecideByTimeElementText.this.mTV.getText().toString().equals(WMUIUtil.getDecoratorText(WMDecideByTimeElementText.this.mTV.getContext(), value))) {
                WMDecideByTimeElementText.this.mTV.setText(WMUIUtil.getDecoratorText(WMDecideByTimeElementText.this.mTV.getContext(), value));
            }
            if (!this.mCancel) {
                WMDecideByTimeElementText.this.mTV.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public abstract void consWMDecideByTimeLogic();

    public abstract String getValueWithTime();

    public WMDecideByTimeElementText(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (context != null) {
            consWMDecideByTimeLogic();
        }
    }

    public void decoratorText(TextView tv) {
        this.mTV = tv;
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        if (this.mDecoratorFoodRunnable != null) {
            this.mDecoratorFoodRunnable.pause();
        }
        this.mDecoratorFoodRunnable = new DecoratorFoodRunnable();
        this.mDecoratorFoodRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorFoodRunnable != null) {
            this.mDecoratorFoodRunnable.pause();
        }
    }
}
