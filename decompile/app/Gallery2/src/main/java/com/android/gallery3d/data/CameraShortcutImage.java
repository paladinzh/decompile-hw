package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.settings.HicloudAccountReceiver;

public class CameraShortcutImage extends ActionImage {
    public CameraShortcutImage(Path path, GalleryApp application) {
        super(path, application, R.drawable.btn_check_on_disable_emui);
    }

    public int getSupportedOperations() {
        return super.getSupportedOperations() | HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT;
    }
}
