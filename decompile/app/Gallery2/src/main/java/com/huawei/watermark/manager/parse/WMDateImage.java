package com.huawei.watermark.manager.parse;

import android.content.Context;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.util.WMDateUtil;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xmlpull.v1.XmlPullParser;

public class WMDateImage extends WMTextShowWithImageBaseLayout {
    private String format;
    private DecoratorDateRunnable mDecoratorDateRunnable;
    private String mLocationInfo;
    private SimpleDateFormat simpleDateFormat;

    private class DecoratorDateRunnable implements Runnable {
        private boolean mCancel;

        private DecoratorDateRunnable() {
            this.mCancel = false;
        }

        public void run() {
            if (WMDateImage.this.simpleDateFormat != null) {
                String text_src = WMDateImage.this.text;
                String text_new = WMDateImage.this.simpleDateFormat.format(new Date());
                if (!text_src.equals(text_new)) {
                    WMDateImage.this.text = text_new;
                    WMDateImage.this.showContent();
                }
                if (!this.mCancel) {
                    WMDateImage.this.mBaseview.postDelayed(this, 1000);
                }
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMDateImage(XmlPullParser parser) {
        super(parser);
        this.format = getStringByAttributeName(parser, "format");
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (context != null && !WMStringUtil.isEmptyString(this.format)) {
            this.format = WMDateUtil.getDateAndTimeFormatStringFromSystem(context, this.format);
            this.simpleDateFormat = WMDateUtil.consDateFormatFromString(this.format);
        }
    }

    public void refreshNumValue() {
        if (this.simpleDateFormat != null) {
            if (this.mDecoratorDateRunnable != null) {
                this.mDecoratorDateRunnable.pause();
            }
            this.mDecoratorDateRunnable = new DecoratorDateRunnable();
            this.mDecoratorDateRunnable.run();
        }
    }

    public void resume() {
        super.resume();
        String locationinfo = WMBaseUtil.getProductLocaleRegion();
        if (this.mLocationInfo == null || !this.mLocationInfo.equals(locationinfo)) {
            this.simpleDateFormat = WMDateUtil.consDateFormatFromString(this.format);
            this.mLocationInfo = locationinfo;
        }
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorDateRunnable != null) {
            this.mDecoratorDateRunnable.pause();
        }
    }
}
