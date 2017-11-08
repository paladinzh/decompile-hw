package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.WMElement.LogicDelegate;
import com.huawei.watermark.manager.parse.util.WMDateUtil;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.text.SimpleDateFormat;
import org.xmlpull.v1.XmlPullParser;

public class WMDate extends WMText {
    private String format;
    private DecoratorDateRunnable mDecoratorDateRunnable;
    private String mLocationInfo;
    private String originFormat;
    private SimpleDateFormat simpleDateFormat;

    private class DecoratorDateRunnable implements Runnable {
        private boolean mCancel;
        private TextView mDateTextView;

        private DecoratorDateRunnable(TextView mDateTextView) {
            this.mCancel = false;
            this.mDateTextView = mDateTextView;
        }

        public void run() {
            if (this.mDateTextView != null && WMDate.this.simpleDateFormat != null) {
                String text_src = this.mDateTextView.getText().toString();
                String text_new = WMDateUtil.getDate(this.mDateTextView.getContext(), WMDate.this.originFormat, WMDate.this.simpleDateFormat);
                if (!text_src.equals(text_new)) {
                    this.mDateTextView.setText(text_new);
                }
                if (!this.mCancel) {
                    this.mDateTextView.postDelayed(this, 1000);
                }
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMDate(XmlPullParser parser) {
        super(parser);
        String stringByAttributeName = getStringByAttributeName(parser, "format");
        this.format = stringByAttributeName;
        this.originFormat = stringByAttributeName;
    }

    public void initBaseLogicData(Context context, LogicDelegate delegate) {
        super.initBaseLogicData(context, delegate);
        if (context != null && !WMStringUtil.isEmptyString(this.format)) {
            this.format = WMDateUtil.getDateAndTimeFormatStringFromSystem(context, this.format);
            this.simpleDateFormat = WMDateUtil.consDateFormatFromString(this.format);
        }
    }

    public void decoratorText(TextView tv) {
        if (this.simpleDateFormat != null) {
            WMUtil.setLKTypeFace(tv.getContext(), tv);
            tv.setText(WMDateUtil.getDate(tv.getContext(), this.originFormat, this.simpleDateFormat));
            if (this.mDecoratorDateRunnable != null) {
                this.mDecoratorDateRunnable.pause();
            }
            this.mDecoratorDateRunnable = new DecoratorDateRunnable(tv);
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
