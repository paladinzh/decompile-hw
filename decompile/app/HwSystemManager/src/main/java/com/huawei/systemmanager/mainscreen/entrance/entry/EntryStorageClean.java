package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TopVideoFilter;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;
import com.huawei.systemmanager.util.HwLog;

public class EntryStorageClean extends SimpleEntrace {
    public static final String NAME = "EntryStorageClean";
    private HsmSingleExecutor mExecutor = new HsmSingleExecutor();
    private final TipView mTipView = new TipView();

    public class SpaceCleanTipTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "SpaceCleanTipTask";

        protected Integer doInBackground(Void... params) {
            if (isCancelled()) {
                return Integer.valueOf(0);
            }
            Context ctx = GlobalContext.getContext();
            int tipNum = 0;
            if (TMSEngineFeature.isSupportTMS()) {
                long startTime = SystemClock.elapsedRealtime();
                VedioCacheUtils.initRedPoint();
                boolean hasLargeVideo = ScanManager.isTooLargeVideoTrash(ctx, TopVideoFilter.getTopVideoApp(), VedioCacheUtils.getMaxSize());
                VedioCacheUtils.saveRedPoint(hasLargeVideo);
                HwLog.i(TAG, "isTooLargeVideoTrash result:" + hasLargeVideo + ", cost time:" + (SystemClock.elapsedRealtime() - startTime));
                if (hasLargeVideo) {
                    tipNum = 1;
                }
            } else {
                HwLog.i(TAG, "not support TMS");
            }
            return Integer.valueOf(tipNum);
        }

        protected void onPostExecute(Integer tipNum) {
            HwLog.i(TAG, "task end, tipNum:" + tipNum);
            if (tipNum != null) {
                EntryStorageClean.this.mTipView.setNumber(tipNum.intValue());
            }
        }

        protected void onCancelled() {
            HwLog.i(TAG, "task cancelled");
        }
    }

    public void onResume() {
        refreshData();
    }

    protected int getIconResId() {
        return R.drawable.ic_system_optimize_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_cleanup;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_STORAGECLEANNER;
    }

    protected void onCreateView(View container) {
        this.mTipView.setTipTextView((TextView) container.findViewById(R.id.tip));
        Utility.setViewEnabled(container, Utility.isOwnerUser(false));
    }

    public String getEntryName() {
        return NAME;
    }

    public void refreshData() {
        new SpaceCleanTipTask().executeOnExecutor(this.mExecutor, new Void[0]);
    }

    public void onDestory() {
        this.mExecutor.clearAllTask();
    }
}
