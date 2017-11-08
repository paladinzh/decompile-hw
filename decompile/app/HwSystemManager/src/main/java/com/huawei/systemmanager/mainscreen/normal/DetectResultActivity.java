package com.huawei.systemmanager.mainscreen.normal;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.mainscreen.detector.DetectTaskManager;
import com.huawei.systemmanager.util.HwLog;

public class DetectResultActivity extends SingleFragmentActivity {
    public static final String KEY_DETECTOR_ID = "detector_id";
    public static final String TAG = "DetectResultActivity";
    private DetectTaskManager mDetecorManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.systemmanager_module_title_cleanup);
        if (!initDetecor()) {
            HwLog.i(TAG, "init detecor failed!");
            finish();
        }
    }

    private boolean initDetecor() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.e(TAG, "initDetecor getIntent is null!");
            return false;
        }
        long id = intent.getLongExtra(KEY_DETECTOR_ID, -1);
        DetectTaskManager detectMgr = DetectTaskManager.getDetecor(id);
        if (detectMgr == null) {
            HwLog.e(TAG, "cound not find detect manager by id:" + id);
            return false;
        }
        this.mDetecorManager = detectMgr;
        return true;
    }

    public DetectTaskManager getDetectorManager() {
        return this.mDetecorManager;
    }

    protected Fragment buildFragment() {
        return new DetectResultFragment();
    }
}
