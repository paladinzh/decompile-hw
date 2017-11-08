package com.huawei.gallery.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.widget.Toast;
import com.android.gallery3d.R;
import com.huawei.gallery.util.HwCustGalleryUtils;
import java.io.File;

public class HwCustPhotoPageImpl extends HwCustPhotoPage {
    private static final String BLUETOOTH_DISABLE_PATH = "/data/OtaSave/Extensions/";
    private static final String BLUETOOTH_FILE_NAME = "bluetooth.disable";
    private static final boolean IS_SPRINT = SystemProperties.getBoolean("ro.config.sprint_dm_ext", false);
    private static final int REQUEST_SLIDESHOW = 1;
    private static final int REQUEST_SLIDE_SETTINGS_DIALOG = 300;

    public boolean isBluetoothRestricted(Activity activity) {
        if (!isDeviceManagementFreatureEnabled() || !new File("/data/OtaSave/Extensions/bluetooth.disable").exists()) {
            return false;
        }
        Toast.makeText(activity, activity.getString(R.string.bluetooth_notification_restricted_sharing), 1).show();
        return true;
    }

    private static boolean isDeviceManagementFreatureEnabled() {
        return IS_SPRINT;
    }

    public boolean handleCustSlideshowItemClicked(GLHost host, String path) {
        if (!HwCustGalleryUtils.isSlideshowSettingsSupported() || host == null) {
            return false;
        }
        Intent target = new Intent(host.getActivity(), SlideShowSettings.class);
        target.putExtra("radio_btn_visibility", true);
        target.putExtra("item_path", path);
        host.getActivity().startActivityForResult(target, 300);
        return true;
    }

    public void onCustStateResult(int requestCode, int resultCode, Intent data, GLHost host, String mediaPath, int indx) {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && data != null && host != null) {
            switch (requestCode) {
                case 300:
                    if (resultCode == -1) {
                        startSlideShow(host, mediaPath, indx, data.getIntExtra(HwCustSlideShowPageImpl.KEY_INTERVAL, 3), data.getIntExtra("sel_id", -1), data.getStringExtra(HwCustSlideShowPageImpl.KEY_BCK_AUDIO), data.getStringExtra("item_path"));
                        break;
                    }
                    break;
            }
        }
    }

    private void startSlideShow(GLHost host, String mediaPath, int indx, int interval, int id, String audioPath, String itemPath) {
        Bundle data = new Bundle();
        data.putString("media-set-path", mediaPath);
        int index = 0;
        switch (id) {
            case R.id.current_indx_radiobtn:
                index = indx;
                data.putString("media-item-path", itemPath);
                break;
            case R.id.start_indx_radiobtn:
                index = 0;
                break;
        }
        data.putInt("photo-index", index);
        data.putBoolean("repeat", true);
        data.putInt(HwCustSlideShowPageImpl.KEY_INTERVAL, interval);
        data.putString(HwCustSlideShowPageImpl.KEY_BCK_AUDIO, audioPath);
        host.getStateManager().startStateForResult(SlideShowPage.class, 1, data);
    }
}
