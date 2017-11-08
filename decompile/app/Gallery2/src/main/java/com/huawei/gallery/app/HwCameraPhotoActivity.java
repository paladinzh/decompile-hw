package com.huawei.gallery.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.android.gallery3d.R;
import com.android.gallery3d.util.PerformanceRadar.Reporter;
import com.android.gallery3d.util.TraceController;

public class HwCameraPhotoActivity extends SinglePhotoActivity {
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwCameraPhotoActivity.this.mIsSecureCamera) {
                HwCameraPhotoActivity.this.resetStartTakenTime();
                HwCameraPhotoActivity.this.finish();
            }
        }
    };
    private BroadcastReceiver mUnlockScreenReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwCameraPhotoActivity.this.mIsSecureCamera) {
                HwCameraPhotoActivity.this.resetStartTakenTime();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mIsSecureCamera) {
            registerReceiver(this.mScreenOffReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
            registerReceiver(this.mUnlockScreenReceiver, new IntentFilter("android.intent.action.USER_PRESENT"));
        }
    }

    protected void onResume() {
        TraceController.beginSection("HwCameraPhotoActivity.onResume");
        Reporter.CAMERA_SEE_TO_REVIEW.start(null);
        super.onResume();
        TraceController.endSection();
    }

    protected void onPause() {
        Reporter.CAMERA_SEE_TO_REVIEW.end(null);
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mIsSecureCamera) {
            unregisterReceiver(this.mScreenOffReceiver);
            unregisterReceiver(this.mUnlockScreenReceiver);
            resetStartTakenTime();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getBooleanExtra("keep-from-camera", true)) {
            overridePendingTransition(R.anim.gallery_to_camera_close_enter, R.anim.gallery_to_camera_close_exit);
        }
    }

    private void resetStartTakenTime() {
        getDataManager().getMediaSet("/local/camera").setStartTakenTime(0);
        getDataManager().getMediaSet("/local/album/from/camera").setStartTakenTime(0);
    }
}
