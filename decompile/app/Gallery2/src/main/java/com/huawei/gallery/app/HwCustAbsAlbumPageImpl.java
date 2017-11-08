package com.huawei.gallery.app;

import android.content.Intent;
import android.os.Bundle;
import com.android.gallery3d.data.MediaSet;
import com.huawei.gallery.util.HwCustGalleryUtils;

public class HwCustAbsAlbumPageImpl extends HwCustAbsAlbumPage {
    private static final String KEY_BCK_AUDIO = "back-audio";
    private static final String KEY_INTERVAL = "interval";
    private static final int REQUEST_SLIDESHOW = 103;
    private static final int REQUEST_SLIDE_SETTINGS_DIALOG = 300;

    public boolean handleCustStartSlideShow(GLHost host) {
        if (!HwCustGalleryUtils.isSlideshowSettingsSupported() || host == null) {
            return false;
        }
        host.getActivity().startActivityForResult(new Intent(host.getActivity(), SlideShowSettings.class), 300);
        return true;
    }

    public void handleCustStateResult(int requestCode, int resultCode, Intent data, GLHost host, MediaSet mediaSet) {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && 300 == requestCode && -1 == resultCode && data != null && host != null && mediaSet != null) {
            onSlideStart(data.getIntExtra("interval", 3), data.getStringExtra("back-audio"), host, mediaSet);
        }
    }

    private void onSlideStart(int interval, String audioPath, GLHost host, MediaSet mediaSet) {
        Bundle data = new Bundle();
        data.putString("media-set-path", mediaSet.getPath().toString());
        data.putBoolean("repeat", true);
        data.putInt("interval", interval);
        data.putString("back-audio", audioPath);
        host.getStateManager().startStateForResult(SlideShowPage.class, 103, data);
    }
}
