package com.huawei.watermark.manager.parse;

import android.content.Context;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.watermark.ui.WMImageView;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMWeekImage extends WMImage {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMWeekImage.class.getSimpleName());
    private DecoratorWeekRunnable mDecoratorWeekRunnable;
    private String pic_friday;
    private String pic_monday;
    private String pic_saturday;
    private String pic_sunday;
    private String pic_thursday;
    private String pic_tuesday;
    private String pic_wednesday;

    private class DecoratorWeekRunnable implements Runnable {
        private boolean mCancel;
        private boolean mIsFirstRun;
        private WaterMark mWaterMark;
        private WMImageView mWeekImageView;

        private DecoratorWeekRunnable(WaterMark wm, WMImageView mWeekImageView) {
            this.mIsFirstRun = true;
            this.mCancel = false;
            this.mWaterMark = wm;
            this.mWeekImageView = mWeekImageView;
        }

        public void run() {
            if (this.mWeekImageView != null) {
                String tempPic = WMWeekImage.this.pic;
                WMWeekImage.this.logic();
                if (this.mIsFirstRun || !(WMWeekImage.this.pic == null || WMWeekImage.this.pic.equals(tempPic))) {
                    this.mIsFirstRun = false;
                    this.mWeekImageView.setWMImagePath(this.mWaterMark.getPath(), WMWeekImage.this.pic);
                }
                if (!this.mCancel) {
                    this.mWeekImageView.postDelayed(this, 1000);
                }
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMWeekImage(XmlPullParser parser) {
        super(parser);
        this.pic_monday = getStringByAttributeName(parser, "pic_monday");
        this.pic_tuesday = getStringByAttributeName(parser, "pic_tuesday");
        this.pic_wednesday = getStringByAttributeName(parser, "pic_wednesday");
        this.pic_thursday = getStringByAttributeName(parser, "pic_thursday");
        this.pic_friday = getStringByAttributeName(parser, "pic_friday");
        this.pic_saturday = getStringByAttributeName(parser, "pic_saturday");
        this.pic_sunday = getStringByAttributeName(parser, "pic_sunday");
    }

    private void logic() {
        String mWay = String.valueOf(Calendar.getInstance().get(7));
        if ("1".equals(mWay)) {
            this.pic = this.pic_sunday;
        } else if (GpsMeasureMode.MODE_2_DIMENSIONAL.equals(mWay)) {
            this.pic = this.pic_monday;
        } else if (GpsMeasureMode.MODE_3_DIMENSIONAL.equals(mWay)) {
            this.pic = this.pic_tuesday;
        } else if ("4".equals(mWay)) {
            this.pic = this.pic_wednesday;
        } else if ("5".equals(mWay)) {
            this.pic = this.pic_thursday;
        } else if ("6".equals(mWay)) {
            this.pic = this.pic_friday;
        } else if ("7".equals(mWay)) {
            this.pic = this.pic_saturday;
        }
    }

    public void decoratorImage(Context context, WaterMark wm, WMImageView iv) {
        if (this.mDecoratorWeekRunnable != null) {
            this.mDecoratorWeekRunnable.pause();
        }
        this.mDecoratorWeekRunnable = new DecoratorWeekRunnable(wm, iv);
        this.mDecoratorWeekRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorWeekRunnable != null) {
            this.mDecoratorWeekRunnable.pause();
        }
    }
}
