package com.huawei.gallery.threedmodel;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.permission.PermissionAdapter;
import com.huawei.gallery.util.PermissionInfoAlert;
import com.spe3d.Spe3DViewer;

public class ThreeDModelActivity extends Spe3DViewer {
    private boolean mIsSecureCamera = false;
    PermissionInfoAlert mPermissionInfoAlert = null;

    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);
        setWindowParams();
    }

    private void init() {
        boolean z = false;
        if (getIntent().getBooleanExtra("is-secure-camera-album", false)) {
            z = GalleryUtils.isKeyguardLocked(this);
        }
        this.mIsSecureCamera = z;
        int hasPermissionCount = 0;
        for (String hasPermission : getPermissionsType()) {
            if (PermissionAdapter.hasPermission(this, hasPermission)) {
                hasPermissionCount++;
            }
        }
        boolean hasPermission2 = hasPermissionCount == length;
        if (this.mIsSecureCamera && !hasPermission2) {
            this.mPermissionInfoAlert = new PermissionInfoAlert(this);
            this.mPermissionInfoAlert.start();
        }
    }

    private void setWindowParams() {
        Window win = getWindow();
        LayoutParams params = win.getAttributes();
        if (this.mIsSecureCamera) {
            params.flags |= 524288;
        }
        params.flags |= 134217728;
        win.setAttributes(params);
    }

    protected boolean needToRequestPermissions() {
        return !this.mIsSecureCamera;
    }

    protected void onPause() {
        if (this.mPermissionInfoAlert != null) {
            this.mPermissionInfoAlert.stop();
        }
        super.onPause();
    }

    protected void onDestroy() {
        if (this.mPermissionInfoAlert != null) {
            this.mPermissionInfoAlert.stop();
        }
        super.onDestroy();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        getModelPath();
        GalleryLog.d("ThreeDModelActivity", "onNewIntent");
    }
}
