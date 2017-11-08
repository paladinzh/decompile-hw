package com.huawei.watermark.manager.parse;

import android.location.Address;
import com.huawei.watermark.manager.parse.util.WMLocationService.LocationUpdateCallback;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.Collection;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class WMLatiImage extends WMTextShowWithImageBaseLayout {
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
            WMLatiImage.this.mLogicDelegate.addLocationUpdateCallback(this.mlocationUpdateCallback);
        }

        public void run() {
            if (this.mCurrentAddress != null && this.mCurrentAddress.hasLatitude()) {
                int tempLatitude = (int) this.mCurrentAddress.getLatitude();
                WMLatiImage.this.text = "" + tempLatitude;
                if (tempLatitude != WMLatiImage.this.mLatitude) {
                    WMLatiImage.this.showContent();
                    WMLatiImage.this.mLatitude = tempLatitude;
                }
            }
            if (!this.mCancel) {
                WMLatiImage.this.mBaseview.postDelayed(this, 1000);
            }
        }

        public void pause() {
            this.mCancel = true;
            WMLatiImage.this.mLatitude = 91;
        }
    }

    public WMLatiImage(XmlPullParser parser) {
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
