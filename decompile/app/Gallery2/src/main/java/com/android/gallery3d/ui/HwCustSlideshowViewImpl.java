package com.android.gallery3d.ui;

import com.huawei.gallery.util.HwCustGalleryUtils;

public class HwCustSlideshowViewImpl extends HwCustSlideshowView {
    public int getCurrentDuration(int defaultDuration) {
        return HwCustGalleryUtils.getAnimationDuration();
    }

    public boolean allowCustSlideShow() {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported()) {
            return true;
        }
        return false;
    }
}
