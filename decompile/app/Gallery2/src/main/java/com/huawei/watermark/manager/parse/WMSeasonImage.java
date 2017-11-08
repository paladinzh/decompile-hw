package com.huawei.watermark.manager.parse;

import android.content.Context;
import com.huawei.watermark.ui.WMImageView;
import java.util.Calendar;
import org.xmlpull.v1.XmlPullParser;

public class WMSeasonImage extends WMImage {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMWeekImage.class.getSimpleName());
    private DecoratorSeasonRunnable mDecoratorSeasonRunnable;
    private String pic_autumn;
    private String pic_spring;
    private String pic_summer;
    private String pic_winter;

    private class DecoratorSeasonRunnable implements Runnable {
        private boolean mCancel;
        private boolean mIsFirstRun;
        private WMImageView mSeasonImageView;
        private WaterMark mWaterMark;

        private DecoratorSeasonRunnable(WaterMark wm, WMImageView mSeasonImageView) {
            this.mIsFirstRun = true;
            this.mCancel = false;
            this.mWaterMark = wm;
            this.mSeasonImageView = mSeasonImageView;
        }

        public void run() {
            if (this.mSeasonImageView != null) {
                String tempPic = WMSeasonImage.this.pic;
                WMSeasonImage.this.logic();
                if (this.mIsFirstRun || !(WMSeasonImage.this.pic == null || WMSeasonImage.this.pic.equals(tempPic))) {
                    this.mIsFirstRun = false;
                    this.mSeasonImageView.setWMImagePath(this.mWaterMark.getPath(), WMSeasonImage.this.pic);
                }
                if (!this.mCancel) {
                    this.mSeasonImageView.postDelayed(this, 1000);
                }
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMSeasonImage(XmlPullParser parser) {
        super(parser);
        this.pic_spring = getStringByAttributeName(parser, "pic_spring");
        this.pic_summer = getStringByAttributeName(parser, "pic_summer");
        this.pic_autumn = getStringByAttributeName(parser, "pic_autumn");
        this.pic_winter = getStringByAttributeName(parser, "pic_winter");
    }

    private void logic() {
        switch (Calendar.getInstance().get(2)) {
            case 0:
            case 1:
            case 11:
            case 12:
                this.pic = this.pic_winter;
                return;
            case 2:
            case 3:
            case 4:
                this.pic = this.pic_spring;
                return;
            case 5:
            case 6:
            case 7:
                this.pic = this.pic_summer;
                return;
            case 8:
            case 9:
            case 10:
                this.pic = this.pic_autumn;
                return;
            default:
                this.pic = this.pic_spring;
                return;
        }
    }

    public void decoratorImage(Context context, WaterMark wm, WMImageView iv) {
        if (this.mDecoratorSeasonRunnable != null) {
            this.mDecoratorSeasonRunnable.pause();
        }
        this.mDecoratorSeasonRunnable = new DecoratorSeasonRunnable(wm, iv);
        this.mDecoratorSeasonRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorSeasonRunnable != null) {
            this.mDecoratorSeasonRunnable.pause();
        }
    }
}
