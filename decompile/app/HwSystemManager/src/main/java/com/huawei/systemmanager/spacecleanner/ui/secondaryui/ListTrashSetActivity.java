package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.MultiFragmentActivity;
import com.huawei.systemmanager.spacecleanner.CommonHandler;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.SimleListener;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.OpenSecondaryParam;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class ListTrashSetActivity extends MultiFragmentActivity implements onCallTrashSetListener, MessageHandler {
    private static final int MSG_REPLACE_FRAGMENT = 1;
    private static final String TAG = "ListTrashSetActivity";
    private boolean mCleanedOperation = false;
    private DataHolder mDataHolder;
    private final Handler mHandler = new CommonHandler(this);
    private int mNeedWaitingTrashs = 0;
    private SimleListener mScanListener;
    private TrashScanHandler mTrashHandler;

    protected Fragment buildDefaultFragment() {
        return new ListTrashSetFragment();
    }

    public int onGetCustomThemeStyle() {
        return R.style.spaceclean_style;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState == null && intent != null && initData(intent)) {
            checkNeedWaiting();
            SpaceStatsUtils.reportFromDeepItemEnterence(fromDeepManagerEnterence());
            return;
        }
        HwLog.d(TAG, "intent is invalida te or save");
        finish();
    }

    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    public int fromDeepManagerEnterence() {
        return this.mDataHolder.param.getDeepItemType();
    }

    private void finishActivity() {
        backToLastActivity(this.mCleanedOperation);
    }

    private void backToLastActivity(boolean isCleaned) {
        Intent intent = new Intent();
        if (isCleaned) {
            setResult(1000, intent);
        } else {
            setResult(1001, intent);
        }
        HwLog.i(TAG, "backToLastActivity isCleaned" + isCleaned);
        finish();
    }

    private boolean initData(Intent intent) {
        long id = intent.getLongExtra("handler_id", -1);
        OpenSecondaryParam param = (OpenSecondaryParam) intent.getParcelableExtra(SecondaryConstant.OPEN_SECONDARY_PARAM);
        if (id == -1 || param == null) {
            HwLog.e(TAG, "initData param is null");
            return false;
        }
        this.mTrashHandler = ScanManager.getCachedHander(id);
        if (this.mTrashHandler == null) {
            HwLog.e(TAG, "initData can not found scanHandler, id:" + id);
            return false;
        }
        this.mDataHolder = new DataHolder();
        this.mDataHolder.trashHander = this.mTrashHandler;
        this.mDataHolder.param = param;
        this.mDataHolder.index = intent.getIntExtra(SecondaryConstant.SUB_TRASH_ID_EXTRA, -1);
        return true;
    }

    private void checkNeedWaiting() {
        this.mNeedWaitingTrashs = 1310720;
        int finishType = this.mDataHolder.trashHander.getFinishedType();
        final int trashType = this.mDataHolder.param.getTrashType();
        if ((finishType & trashType) == 0 && (this.mNeedWaitingTrashs & trashType) != 0) {
            this.mScanListener = new SimleListener() {
                public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
                    if ((trashType & supportTrashType) != 0 && ListTrashSetActivity.this.mHandler != null) {
                        ListTrashSetActivity.this.mDataHolder.trashHander.removeScanListenr(ListTrashSetActivity.this.mScanListener);
                        ListTrashSetActivity.this.mHandler.sendEmptyMessage(1);
                    }
                }
            };
            getFragmentManager().beginTransaction().replace(16908290, new ListTrashSetWaitingFragment()).commit();
            HwLog.i(TAG, "checkNeedWaiting, scan not finish replace the waiting fragment");
            this.mDataHolder.trashHander.addScanListener(this.mScanListener);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mDataHolder != null && this.mDataHolder.trashHander != null && this.mScanListener != null) {
            HwLog.i(TAG, "onDestroy, remove scan listenr");
            this.mDataHolder.trashHander.removeScanListenr(this.mScanListener);
        }
    }

    public List<Trash> initAndGetData() {
        TrashGroup trashGroup;
        OpenSecondaryParam param = this.mDataHolder.param;
        int scanType = param.getScanType();
        int trashType = param.getTrashType();
        TrashScanHandler handler = this.mDataHolder.trashHander;
        if (scanType == 50) {
            trashGroup = handler.getNormalTrashByType(trashType);
        } else {
            trashGroup = handler.getTrashByType(trashType);
        }
        if (trashGroup == null) {
            HwLog.w(TAG, "initAndGetData, trashGroup is empty! trashType:" + trashType);
            return Lists.newArrayList();
        }
        String uniqueDes = param.getUniqueDescription();
        if (TextUtils.isEmpty(uniqueDes)) {
            HwLog.i(TAG, "initAndGetData, find trash no with uniqueDes, trashType:" + trashType);
            return trashGroup.getTrashListUnclened();
        }
        Trash targetTrash = trashGroup.findTrashByuniqueDes(uniqueDes);
        if (targetTrash == null) {
            HwLog.w(TAG, "initAndGetData, findTrashByuniqueDes failed! uniqueDes:" + uniqueDes + ", trashType:" + trashType);
            return Lists.newArrayList();
        } else if (targetTrash instanceof TrashGroup) {
            HwLog.i(TAG, "initAndGetData, find trash  with uniqueDes:" + uniqueDes + ",trashType:" + trashType);
            return ((TrashGroup) targetTrash).getTrashListUnclened();
        } else {
            HwLog.e(TAG, "initAndGetData, targetTrash is not TrashGroup! uniqueDes:" + uniqueDes + ", trashType:" + trashType);
            return Lists.newArrayList();
        }
    }

    public TrashScanHandler getTrashHandler() {
        return this.mTrashHandler;
    }

    public DataHolder getDataHolder() {
        return this.mDataHolder;
    }

    public void setCleanedOperation(boolean cleaned) {
        this.mCleanedOperation = cleaned;
    }

    public void resetTrashSet() {
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (!isFinishing() && !isDestroyed()) {
                    Fragment frg = buildDefaultFragment();
                    setCurrentFragmentTag(MultiFragmentActivity.KEY_DEFAULT_FRAGMENT_TAG);
                    getFragmentManager().beginTransaction().replace(16908290, frg, MultiFragmentActivity.KEY_DEFAULT_FRAGMENT_TAG).commitAllowingStateLoss();
                    HwLog.i(TAG, "checkNeedWaiting, onScanEnd, scan finish replace the data fragment");
                    break;
                }
                HwLog.i(TAG, "checkNeedWaiting, onScanEnd, activity finish ,return");
                return;
                break;
        }
    }
}
