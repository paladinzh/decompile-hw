package com.huawei.systemmanager.spacecleanner.ui.spacemanager;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.comm.widget.statmachine.IState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleStateMachine;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.spacecleanner.SpaceManagerActivity;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.SimleListener;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.SdcardStateReceiver;
import com.huawei.systemmanager.spacecleanner.ui.dialog.DialogManager;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.TrashDeepItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class SpaceManagerStateMachine extends SimpleStateMachine {
    private static final int MSG_ALL_TASK_SCAN_END = 5;
    private static final int MSG_BACK_FROME_SECONDARY = 9;
    private static final int MSG_ON_RESUME = 7;
    private static final int MSG_SCAN_PROGRESS_CHANGE = 1;
    private static final int MSG_SDCARD_ERROR = 50;
    private static final int MSG_SINGLE_TASK_SCAN_END = 3;
    private static final String TAG = "SpaceManagerStateMachine";
    private ViewStub emptyView;
    private DeepCircle mDeepCircle;
    private DialogManager mDialogManager;
    private IState mEmptyState = new EmptyState();
    private Fragment mFragment;
    private OnClickListener mGoHanlderClicker = new OnClickListener() {
        public void onClick(View v) {
            Activity ac = SpaceManagerStateMachine.this.mFragment.getActivity();
            if (ac == null) {
                HwLog.e(SpaceManagerStateMachine.TAG, "onclick but activity is null");
                return;
            }
            DeepItem item = (DeepItem) v.getTag();
            if (item == null) {
                HwLog.e(SpaceManagerStateMachine.TAG, "onclick but item is null");
            } else if (!item.shouldCheckFinished() || item.isFinished()) {
                Intent intent = item.getIntent(ac);
                intent.putExtra("handler_id", SpaceManagerStateMachine.this.mScanHander.getId());
                int type = item.getDeepItemType();
                try {
                    SpaceManagerStateMachine.this.mFragment.startActivityForResult(intent, type);
                    SpaceStatsUtils.reportDeepItemEnterenceClick(type);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                HwLog.i(SpaceManagerStateMachine.TAG, "onclick but item is not finished");
            }
        }
    };
    private final ItemsController mItemController;
    private IState mScanEndState = new ScanEndState();
    private TrashScanHandler mScanHander;
    private ITrashScanListener mScanListener = new SimleListener() {
        public void onScanProgressChange(int scannerType, int progress, String info, long normalTrashSize, int normalProgress) {
            SpaceManagerStateMachine.this.sendMessage(1, info, scannerType, progress);
        }

        public void onScanEnd(int scannerType, int supportTrashType, boolean canceled) {
            HwLog.i(SpaceManagerStateMachine.TAG, "receive onScanEnd, scannerType:" + scannerType + ", canceled:" + canceled + ", support type:" + Integer.toBinaryString(supportTrashType));
            if (scannerType == 100) {
                SpaceManagerStateMachine.this.sendEmptyMessage(5);
            } else {
                SpaceManagerStateMachine.this.sendMessage(3);
            }
        }
    };
    private IState mScanningState = new ScanningState();
    private SdcardStateReceiver mSdcardReceiver = new SdcardStateReceiver(getContext()) {
        protected void handlerSdcardError(String action) {
            HwLog.i(SpaceManagerStateMachine.TAG, "handlerSdcardError recive action:" + action + ", current state is:" + SpaceManagerStateMachine.this.getCurrentState().getName());
            SpaceManagerStateMachine.this.sendMessage(50);
        }
    };
    private View scrollView;

    private class EmptyState extends SimpleState {
        private EmptyState() {
        }

        public void enter() {
            SpaceManagerStateMachine.this.scrollView.setVisibility(8);
            View view = SpaceManagerStateMachine.this.emptyView.inflate();
            ViewUtils.setVisibility(view, 0);
            ViewUtil.initEmptyViewMargin(SpaceManagerStateMachine.getContext(), SpaceManagerStateMachine.this.emptyView);
            ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.space_clean_result_no_trash_info);
            ((ImageView) view.findViewById(R.id.empty_image)).setImageResource(R.drawable.ic_no_apps);
        }

        public String getName() {
            return "EmptyState";
        }
    }

    private class ScanEndState extends SimpleState {
        private ScanEndState() {
        }

        public void enter() {
            SpaceManagerStateMachine.this.mItemController.initItems(SpaceManagerStateMachine.this.mScanHander);
            SpaceManagerStateMachine.this.mItemController.checkItemFinished(SpaceManagerStateMachine.this.mScanHander);
            Activity ac = SpaceManagerStateMachine.this.mFragment.getActivity();
            if ((ac instanceof SpaceManagerActivity) && ((SpaceManagerActivity) ac).ismOnlyScanInternal()) {
                SparseArray<Long> trashInfo = SpaceManagerStateMachine.this.getScanTrashInfo();
                if (trashInfo.size() > 0) {
                    SpaceStatsUtils.reportSpaceManagerTrashSizeFromLowMemTips(trashInfo);
                }
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    HwLog.i(SpaceManagerStateMachine.TAG, "handler MSG_ON_RESUME, update the storage percent");
                    SpaceManagerStateMachine.this.mDeepCircle.showCircleView();
                    break;
                case 9:
                    SpaceManagerStateMachine.this.hanlderOnActivityResult(msg);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public String getName() {
            return "ScanEndState";
        }
    }

    private class ScanningState extends SimpleState {
        private ScanningState() {
        }

        public void enter() {
            SpaceManagerStateMachine.this.mItemController.initItems(SpaceManagerStateMachine.this.mScanHander);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 3:
                    SpaceManagerStateMachine.this.mItemController.checkItemFinished(SpaceManagerStateMachine.this.mScanHander);
                    break;
                case 5:
                    HwLog.i(SpaceManagerStateMachine.TAG, "receive MSG_ALL_TASK_SCAN_END in scaning state");
                    SpaceManagerStateMachine.this.transitionTo(SpaceManagerStateMachine.this.mScanEndState);
                    if (SpaceManagerStateMachine.this.mItemController.checkAllItemEmpty(SpaceManagerStateMachine.this.mScanHander)) {
                        HwLog.i(SpaceManagerStateMachine.TAG, "receive MSG_ALL_TASK_SCAN_END check all item empty, trans to empty");
                        SpaceManagerStateMachine.this.transitionTo(SpaceManagerStateMachine.this.mEmptyState);
                        break;
                    }
                    break;
                case 9:
                    SpaceManagerStateMachine.this.hanlderOnActivityResult(msg);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public String getName() {
            return "ScanningState";
        }
    }

    protected SpaceManagerStateMachine(Fragment frg, View view, TrashScanHandler hanlder) {
        super(TAG, Looper.getMainLooper());
        Activity ac = frg.getActivity();
        this.mScanHander = hanlder;
        this.mFragment = frg;
        this.scrollView = view.findViewById(R.id.scroll_view);
        this.scrollView.setOverScrollMode(2);
        this.emptyView = (ViewStub) view.findViewById(R.id.no_item);
        this.mDeepCircle = new DeepCircle((ViewGroup) view.findViewById(R.id.upperview), ac, ac.getApplicationContext(), ac.getLayoutInflater(), this.mScanHander.hasSdcard());
        int analysisTrashType = 0;
        if (ac instanceof SpaceManagerActivity) {
            analysisTrashType = ((SpaceManagerActivity) ac).getAnalysisTrashType();
        }
        this.mItemController = new ItemsController((ViewGroup) view.findViewById(R.id.item_container), this.mGoHanlderClicker, analysisTrashType);
        this.mDialogManager = new DialogManager(ac, frg);
    }

    public DeepCircle getDeepCircle() {
        return this.mDeepCircle;
    }

    public void start() {
        super.start();
        this.mSdcardReceiver.register();
        this.mScanHander.addScanListener(this.mScanListener);
        boolean scanEnd = this.mScanHander.isScanEnd();
        this.mDeepCircle.showCircleView();
        if (scanEnd) {
            HwLog.i(TAG, "start(), not scan end, transto scanning");
            this.mItemController.initItems(this.mScanHander);
            if (this.mItemController.checkAllItemEmpty(this.mScanHander)) {
                HwLog.i(TAG, "start(), scan end, all item empty, transt to empty");
                setInitialState(this.mEmptyState);
                return;
            }
            HwLog.i(TAG, "start(), scan end, not all item empty");
            setInitialState(this.mScanEndState);
            return;
        }
        HwLog.i(TAG, "start(), not scan end, transto scanning");
        setInitialState(this.mScanningState);
    }

    public void quit() {
        super.quit();
        this.mSdcardReceiver.unRegseter();
        this.mScanHander.removeScanListenr(this.mScanListener);
    }

    private SparseArray<Long> getScanTrashInfo() {
        SparseArray<Long> trashInfo = new SparseArray();
        List<DeepItem> deepItems = this.mItemController.getItems();
        if (deepItems != null && deepItems.size() > 0) {
            for (DeepItem item : deepItems) {
                if (item instanceof TrashDeepItem) {
                    trashInfo.put(item.getDeepItemType(), Long.valueOf(((TrashDeepItem) item).getTrashSize()));
                }
            }
        }
        return trashInfo;
    }

    private void hanlderOnActivityResult(Message msg) {
        this.mItemController.updateAllViewState();
        if (this.mItemController.checkAllItemEmpty(this.mScanHander)) {
            HwLog.i(TAG, "hanlderOnActivityResult check all item empty, trans to empty");
            transitionTo(this.mEmptyState);
        }
    }

    protected void defaultHandlerMessage(Message msg) {
        switch (msg.what) {
            case 50:
                if (!this.mScanHander.isScanEnd()) {
                    HwLog.i(TAG, "handler MSG_SDCARD_ERROR, mScanHander not end, cancel scan");
                    this.mScanHander.cancelScan();
                }
                showSdcardWarnningDialog();
                return;
            default:
                super.defaultHandlerMessage(msg);
                return;
        }
    }

    private void showSdcardWarnningDialog() {
        Activity ac = this.mFragment.getActivity();
        if (ac == null) {
            HwLog.e(TAG, "showSdcardWarnningDialog ac is null!");
            return;
        }
        ac.setResult(4);
        this.mDialogManager.createSdcardWarningDlg(getContext().getString(R.string.space_clean_optimize_sdcard_exception_msg_change), true);
    }

    private static Context getContext() {
        return GlobalContext.getContext();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        HwLog.i(TAG, "onActivityResult, requestCode:" + requestCode + ", resultCode:" + requestCode + ", dataChanged:" + (resultCode == 1000));
        sendMessage(9, data, requestCode, resultCode);
    }

    public void onResume() {
        sendEmptyMessage(7);
    }
}
