package com.huawei.watermark.manager.parse;

import android.location.Address;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.Collection;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class WMLongiImage extends WMTextShowWithImageBaseLayout {
    DecoratorLocationRunnable mDecoratorLocationRunnable;

    private class DecoratorLocationRunnable implements Runnable {
        private boolean mCancel;
        private Address mCurrentAddress;
        private LocationUpdateCallback mlocationUpdateCallback;

        private DecoratorLocationRunnable() {
            this.mCurrentAddress = null;
            this.mlocationUpdateCallback = new LocationUpdateCallback() {
                public void onAddressReport(List<Address> addresses) {
                    if (!WMCollectionUtil.isEmptyCollection((Collection) addresses)) {
                        DecoratorLocationRunnable.this.mCurrentAddress = (Address) addresses.get(0);
                    }
                }
            };
            WMLongiImage.this.mLogicDelegate.addLocationUpdateCallback(this.mlocationUpdateCallback);
        }

        public void run() {
            if (this.mCurrentAddress != null && this.mCurrentAddress.hasLongitude()) {
                int tempLongitude = (int) this.mCurrentAddress.getLongitude();
                WMLongiImage.this.text = "" + tempLongitude;
                if (tempLongitude != WMLongiImage.this.mLongitude) {
                    WMLongiImage.this.showContent();
                    WMLongiImage.this.mLongitude = tempLongitude;
                }
            }
            if (!this.mCancel) {
                WMLongiImage.this.mBaseview.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
            WMLongiImage.this.mLongitude = SmsCheckResult.ESCT_181;
        }
    }

    public WMLongiImage(XmlPullParser parser) {
        super(parser);
    }

    public void refreshNumValue() {
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
        }
        this.mDecoratorLocationRunnable = new DecoratorLocationRunnable();
        this.mDecoratorLocationRunnable.run();
    }

    public void pause() {
        super.pause();
        if (this.mDecoratorLocationRunnable != null) {
            this.mDecoratorLocationRunnable.pause();
        }
    }
}
