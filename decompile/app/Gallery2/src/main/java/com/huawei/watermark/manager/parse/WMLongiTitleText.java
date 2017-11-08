package com.huawei.watermark.manager.parse;

import android.location.Address;
import android.widget.TextView;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMUtil;
import java.util.Collection;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class WMLongiTitleText extends WMText {
    private DecoratorLocationRunnable mDecoratorLocationRunnable;

    private class DecoratorLocationRunnable implements Runnable {
        private boolean mCancel;
        private Address mCurrentAddress;
        private TextView mLongiTitleTextView;
        private LocationUpdateCallback mlocationUpdateCallback;

        private DecoratorLocationRunnable(TextView tv) {
            this.mCurrentAddress = null;
            this.mlocationUpdateCallback = new LocationUpdateCallback() {
                public void onAddressReport(List<Address> addresses) {
                    if (!WMCollectionUtil.isEmptyCollection((Collection) addresses)) {
                        DecoratorLocationRunnable.this.mCurrentAddress = (Address) addresses.get(0);
                    }
                }
            };
            WMLongiTitleText.this.mLogicDelegate.addLocationUpdateCallback(this.mlocationUpdateCallback);
            this.mLongiTitleTextView = tv;
        }

        public void run() {
            if (this.mCurrentAddress != null && this.mCurrentAddress.hasLongitude()) {
                WMLongiTitleText.this.text = this.mCurrentAddress.getLongitude() >= 0.0d ? "other_locationwatermark_longitude" : "other_locationwatermark_westlongitude";
            }
            super.decoratorText(this.mLongiTitleTextView);
            if (!this.mCancel) {
                this.mLongiTitleTextView.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
        }
    }

    public WMLongiTitleText(XmlPullParser parser) {
        super(parser);
    }

    public void decoratorText(TextView tv) {
        tv.setText(getText());
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
        }
        this.mDecoratorLocationRunnable = new DecoratorLocationRunnable(tv);
        this.mDecoratorLocationRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
        }
    }
}
