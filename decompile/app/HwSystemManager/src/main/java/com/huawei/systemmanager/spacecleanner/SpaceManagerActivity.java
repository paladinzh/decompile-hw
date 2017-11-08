package com.huawei.systemmanager.spacecleanner;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.SimleListener;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.SpaceManagerFragment;
import com.huawei.systemmanager.util.HwLog;

public class SpaceManagerActivity extends SingleFragmentActivity {
    public static final String ANALYSIS_TRASH_TYPE = "analysis_trash_type";
    public static final int FROM_DEFAULT = 0;
    public static final int FROM_FILE_ANALYSIS_NOTIFICATION = 1;
    public static final int FROM_LOW_SPACE_NOTIFICATION = 2;
    public static final String FROM_OP = "from_op";
    public static final String KEY_CREATE_NEW_HANDLER_ID = "create_new_id";
    public static final String KEY_HANDLER_ID = "handler_id";
    public static final String KEY_ONLY_SCAN_INTERNAL = "only_scan_internal";
    private static final String TAG = "SpaceManagerActivity";
    private int mAnalysisTrashType = 0;
    private int mEnterFrom = 0;
    private boolean mOnlyScanInternal = false;
    private ITrashScanListener mScanListner = new SimleListener();
    private TrashScanHandler mTrashHandler;

    public boolean ismOnlyScanInternal() {
        return this.mOnlyScanInternal;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!initTrashScanHandler()) {
            finish();
        }
        initSpaceManageState();
    }

    protected Fragment buildFragment() {
        return new SpaceManagerFragment();
    }

    public int getEnterFrom() {
        return this.mEnterFrom;
    }

    public int getAnalysisTrashType() {
        return this.mAnalysisTrashType;
    }

    private boolean initTrashScanHandler() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.e(TAG, "initTrashScanHandler intent is null");
            return false;
        }
        this.mEnterFrom = intent.getIntExtra(FROM_OP, 0);
        if (1 == this.mEnterFrom) {
            this.mAnalysisTrashType = intent.getIntExtra(ANALYSIS_TRASH_TYPE, 0);
            SpaceStatsUtils.reportFileAnalysisNotificationEnterOp();
        }
        if (intent.getBooleanExtra(KEY_CREATE_NEW_HANDLER_ID, false)) {
            this.mOnlyScanInternal = intent.getBooleanExtra(KEY_ONLY_SCAN_INTERNAL, false);
            if (this.mOnlyScanInternal) {
                changeTitleLabel(getResources().getString(R.string.space_clean_internal_space_manager));
                this.mTrashHandler = ScanManager.startInternalScan(this, this.mScanListner);
            } else {
                this.mTrashHandler = ScanManager.startScan(this, this.mScanListner);
            }
        } else {
            long id = intent.getLongExtra("handler_id", -1);
            this.mTrashHandler = ScanManager.getCachedHander(id);
            if (this.mTrashHandler == null) {
                HwLog.e(TAG, "initTrashScanHandler, cannot find by id:" + id);
                return false;
            }
            HwLog.i(TAG, "initTrashScanHandler sucess by id:" + id);
        }
        return true;
    }

    private void changeTitleLabel(String title) {
        setTitle(title);
    }

    public TrashScanHandler getScanHandler() {
        return this.mTrashHandler;
    }

    private void initSpaceManageState() {
        SpaceCleannerManager.getInstance().cancelFileAnalysisNotify();
    }
}
