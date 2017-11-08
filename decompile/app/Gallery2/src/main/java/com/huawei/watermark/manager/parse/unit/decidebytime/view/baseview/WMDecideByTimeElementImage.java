package com.huawei.watermark.manager.parse.unit.decidebytime.view.baseview;

import android.content.Context;
import android.widget.ImageView;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.WMImage;
import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.manager.parse.unit.decidebytime.logic.WMDecideByTimeBaseLogic;
import com.huawei.watermark.ui.WMImageView;
import com.huawei.watermark.wmutil.WMUIUtil;
import org.xmlpull.v1.XmlPullParser;

public abstract class WMDecideByTimeElementImage extends WMImage {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMDecideByTimeElementImage.class.getSimpleName());
    private DecoratorThreeMealsRunnable mDecoratorThreeMealsRunnable;
    private ImageView mIV;
    public WMDecideByTimeBaseLogic mWMDecideByTimeLogic;

    private class DecoratorThreeMealsRunnable implements Runnable {
        private boolean mCancel;
        private boolean mIsFirstRun;
        private WaterMark mWaterMark;

        private DecoratorThreeMealsRunnable(WaterMark wm) {
            this.mIsFirstRun = true;
            this.mCancel = false;
            this.mWaterMark = wm;
        }

        public void run() {
            String tempPic = WMDecideByTimeElementImage.this.pic;
            WMDecideByTimeElementImage.this.pic = WMDecideByTimeElementImage.this.getValueWithTime();
            if (this.mIsFirstRun || !(WMDecideByTimeElementImage.this.pic == null || WMDecideByTimeElementImage.this.pic.equals(tempPic))) {
                this.mIsFirstRun = false;
                WMUIUtil.recycleView(WMDecideByTimeElementImage.this.mIV);
                ((WMImageView) WMDecideByTimeElementImage.this.mIV).setWMImagePath(this.mWaterMark.getPath(), WMDecideByTimeElementImage.this.pic);
            }
            if (!this.mCancel) {
                WMDecideByTimeElementImage.this.mIV.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public abstract void consWMDecideByTimeLogic();

    public abstract String getValueWithTime();

    public WMDecideByTimeElementImage(XmlPullParser parser) {
        super(parser);
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (context != null) {
            consWMDecideByTimeLogic();
        }
    }

    public void decoratorImage(Context context, WaterMark wm, WMImageView iv) {
        this.mIV = iv;
        if (this.mDecoratorThreeMealsRunnable != null) {
            this.mDecoratorThreeMealsRunnable.pause();
        }
        this.mDecoratorThreeMealsRunnable = new DecoratorThreeMealsRunnable(wm);
        this.mDecoratorThreeMealsRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorThreeMealsRunnable != null) {
            this.mDecoratorThreeMealsRunnable.pause();
        }
    }
}
