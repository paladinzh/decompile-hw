package com.huawei.gallery.extfile;

import android.app.Activity;
import android.os.Bundle;
import com.android.gallery3d.R;

public class UploadActivity extends Activity {
    private static boolean sForceFinish = false;
    public static UploadActivity uploadActivityInstance;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_progress_bar);
        uploadActivityInstance = this;
        if (sForceFinish) {
            finishActivity();
        }
    }

    public static UploadActivity getInstance() {
        return uploadActivityInstance;
    }

    protected void onResume() {
        super.onResume();
        if (sForceFinish) {
            finishActivity();
        }
    }

    public static void finishActivity() {
        if (uploadActivityInstance != null) {
            uploadActivityInstance.finish();
            uploadActivityInstance = null;
            sForceFinish = false;
            return;
        }
        sForceFinish = true;
    }
}
