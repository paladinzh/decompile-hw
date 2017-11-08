package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;

public class HwCustPhotoPage {
    public boolean isBluetoothRestricted(Activity activity) {
        return false;
    }

    public boolean handleCustSlideshowItemClicked(GLHost host, String path) {
        return false;
    }

    public void onCustStateResult(int reqCode, int resCode, Intent data, GLHost host, String mediaPath, int indx) {
    }
}
