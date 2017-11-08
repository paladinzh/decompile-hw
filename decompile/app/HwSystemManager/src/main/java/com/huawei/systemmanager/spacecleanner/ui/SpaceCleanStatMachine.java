package com.huawei.systemmanager.spacecleanner.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.HwActivitySplitterImpl;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IAppInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.statmachine.IState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleStateMachine;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.spacecleanner.Const;
import com.huawei.systemmanager.spacecleanner.SpaceManagerActivity;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.SimleListener;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.base.CleanTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ICleanListener.SimpleListener;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.statistics.TrashInfoBuilder;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.dialog.DialogManager;
import com.huawei.systemmanager.spacecleanner.ui.dialog.TrashDetailDialog;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.AppProcessTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.ListTrashSetActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.spacecleanner.ui.suggestcleaner.ListController;
import com.huawei.systemmanager.spacecleanner.ui.trashlistadapter.TrashListAdapter;
import com.huawei.systemmanager.spacecleanner.ui.upperview.HeadViewController;
import com.huawei.systemmanager.spacecleanner.ui.upperview.SpaceCleanEndViewController;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.SplitModeUtil;
import java.util.List;
import java.util.Map;

public class SpaceCleanStatMachine extends SimpleStateMachine {
    private static final int CMD_CLICK_BACK = 4;
    private static final int CMD_CLICK_SCAN_BUTTON = 3;
    private static final int FLAG_SCAN_CANCELED = 2;
    private static final int FLAG_SCAN_NO_TRASH = 3;
    private static final int MSG_BACK_FROM_SECONDARY = 40;
    private static final int MSG_CLEAN_END = 6;
    private static final int MSG_CLEAN_PROGRESS = 20;
    private static final int MSG_CLICK_CHILD_CHECK = 60;
    private static final int MSG_CLICK_GROUP_CHECK = 80;
    private static final int MSG_DEEP_SCANNER_END = 82;
    private static final int MSG_DIVIDER_SCANNER_END = 83;
    private static final int MSG_NORMAL_SCANNER_END = 81;
    private static final int MSG_NORMAL_SCANNER_START = 70;
    private static final int MSG_ONRESUME = 100;
    private static final int MSG_REFRESH_ADAPTER = 90;
    public static final int MSG_REFRESH_CHECKED_SIZE = 30;
    private static final int MSG_SCAN_PROGRESS = 9;
    private static final int MSG_SDCARD_ERROR = 50;
    private static final int MSG_SHOW_SCAN_FINISHED = 10;
    private static final int REQUEST_CODE_SPACE_MANAGER = 7;
    private static final long START_SCAN_DELAY = 600;
    private static final String TAG = "SpaceCleanStatMachine";
    private long cleanTrashSize = 0;
    private final TrashInfoBuilder mBuilder;
    private OnClickListener mCheckClicker = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag();
            if (item == null) {
                HwLog.i(SpaceCleanStatMachine.TAG, "onCheckedChanged, but trash in null!");
                return;
            }
            HwLog.i(SpaceCleanStatMachine.TAG, "user click trash checkbox");
            Trash trash = item.getTrash();
            if (trash instanceof IAppInfo) {
                SpaceStatsUtils.reportOneKeyCleanPkgInfo(item.getTrashType(), ((IAppInfo) trash).getPackageName(), !item.isChecked());
            }
            SpaceCleanStatMachine.this.sendMessage(60, (Object) item);
        }
    };
    private ICleanListener mCleanListener = new SimpleListener() {
        public void onCleanProgressChange(int progress, String info) {
            SpaceCleanStatMachine.this.sendMessage(20, (Object) info);
        }

        public void onCleanEnd(boolean canceled, long cleanedSize) {
            if (canceled) {
                HwLog.i(SpaceCleanStatMachine.TAG, "recieve msg MSG_CLEAN_END, canceled:" + canceled);
            } else if (cleanedSize > 0) {
                SharedPreferences historySizePerfer = GlobalContext.getContext().getSharedPreferences(Const.SPACE_CLEAN_SHARED_PERFERENCE, 0);
                historySizePerfer.edit().putLong(Const.NORMAL_CLEANED_HISTORY_TOTAL_SIZE, historySizePerfer.getLong(Const.NORMAL_CLEANED_HISTORY_TOTAL_SIZE, 0) + cleanedSize).commit();
                SpaceCleanStatMachine.this.cleanTrashSize = cleanedSize;
            } else {
                HwLog.e(SpaceCleanStatMachine.TAG, "cleaned size is error;cleaned size:" + cleanedSize);
            }
            SpaceCleanStatMachine.this.sendEmptyMessage(6);
        }
    };
    private Context mContext;
    private TrashDetailDialog mDetailDialog;
    private final DialogManager mDialogManager;
    private volatile int mEndScanTrashType = 0;
    private SpaceCleanEndViewController mEndViewController;
    private OnClickListener mEnterDeepClick = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(SpaceCleanStatMachine.TAG, "enter deep button click, current state is:" + SpaceCleanStatMachine.this.getCurrentState().getName());
            SpaceCleanStatMachine.this.enterSpaceManager();
        }
    };
    private Fragment mFragment;
    private OnClickListener mGroupCheckClicker = new OnClickListener() {
        public void onClick(View v) {
            TrashItemGroup itemGroup = (TrashItemGroup) v.getTag();
            if (itemGroup == null) {
                HwLog.i(SpaceCleanStatMachine.TAG, "onCheckedChanged, but trash group in null!");
                return;
            }
            HwLog.i(SpaceCleanStatMachine.TAG, "user click trash group checkbox");
            SpaceStatsUtils.reportOneKeyCleanTrashItem(itemGroup.getTrashType(), !itemGroup.isChecked());
            SpaceCleanStatMachine.this.sendMessage(80, (Object) itemGroup);
        }
    };
    private HeadViewController mHeadViewController;
    private OnClickListener mItemClicker = new OnClickListener() {
        public void onClick(View v) {
            ITrashItem item = (ITrashItem) v.getTag(R.id.convertview_tag_item);
            if (item == null) {
                HwLog.i(SpaceCleanStatMachine.TAG, "item click, but trash item in null!");
            } else {
                SpaceCleanStatMachine.this.handlerTrashItemClick(item);
            }
        }
    };
    private ITrashItem mJumpItem;
    private final ListController mListController;
    private OnLongClickListener mLongClicker = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            HwLog.i(SpaceCleanStatMachine.TAG, "item long click");
            if (SpaceCleanStatMachine.this.checkScanEndState()) {
                ITrashItem item = (ITrashItem) v.getTag(R.id.convertview_tag_item);
                if (item == null) {
                    HwLog.i(SpaceCleanStatMachine.TAG, "onLongClick, item is null");
                    return false;
                } else if (!item.isCleaned()) {
                    return SpaceCleanStatMachine.this.mDialogManager.showLongclickDialog(new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == -1) {
                                SpaceCleanStatMachine.this.sendMessage(90);
                            }
                        }
                    }, item);
                } else {
                    HwLog.i(SpaceCleanStatMachine.TAG, "onLongClick, item is cleaned;");
                    return false;
                }
            }
            HwLog.i(SpaceCleanStatMachine.TAG, "onLongClick, current is not scan end state");
            return false;
        }
    };
    private IState mNormalCleanEndState = new NormalCleanEndState();
    private IState mNormalCleanningState = new NormalCleanningState();
    private IState mNormalScanEndState = new NormalScanEndState();
    private IState mNormalScanningState = new NormalScanningState();
    private Button mScanBtn;
    private OnClickListener mScanClicker = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(SpaceCleanStatMachine.TAG, "scan button click, current state is:" + SpaceCleanStatMachine.this.getCurrentState().getName());
            SpaceCleanStatMachine.this.sendEmptyMessage(3);
        }
    };
    private TrashScanHandler mScanHandler;
    private LinearLayout mScanLinearLayout;
    private ITrashScanListener mScanListner = new SimleListener() {
        public void onScanProgressChange(int scannerType, int progress, String info, long normalTrashSize, int normalProgress) {
            if (SpaceCleanStatMachine.this.getHandler() != null && !SpaceCleanStatMachine.this.getHandler().hasMessages(9)) {
                SpaceCleanStatMachine.this.sendMessage(9, new ScanDataBean(normalTrashSize, info, normalProgress), scannerType, progress);
            }
        }

        public void onScanEnd(int scannerType, int supportType, boolean canceled) {
            long j = 0;
            HwLog.i(SpaceCleanStatMachine.TAG, "receive onScanEnd, scannerType:" + scannerType + ", canceled:" + canceled + ", support type:" + supportType);
            synchronized (SpaceCleanStatMachine.this) {
                SpaceCleanStatMachine spaceCleanStatMachine = SpaceCleanStatMachine.this;
                spaceCleanStatMachine.mEndScanTrashType = spaceCleanStatMachine.mEndScanTrashType | supportType;
            }
            SpaceCleanStatMachine spaceCleanStatMachine2;
            if (scannerType == 50) {
                SpaceCleanStatMachine.this.sendMessage(10, 50);
                spaceCleanStatMachine2 = SpaceCleanStatMachine.this;
                if (!canceled) {
                    j = 900;
                }
                spaceCleanStatMachine2.sendMessageDelay(SpaceCleanStatMachine.MSG_NORMAL_SCANNER_END, j);
            } else if (scannerType == 100) {
                spaceCleanStatMachine2 = SpaceCleanStatMachine.this;
                if (!canceled) {
                    j = 900;
                }
                spaceCleanStatMachine2.sendMessageDelay(82, j);
                if (!canceled) {
                    SpaceCleanStatMachine.this.mBuilder.setEndTime(System.currentTimeMillis());
                    SpaceCleanStatMachine.this.mBuilder.setScanETimeMemory(SpaceCleanStatMachine.this.mContext);
                    SpaceStatsUtils.reportTrashScanResult(SpaceCleanStatMachine.this.mScanHandler, SpaceCleanStatMachine.this.mBuilder);
                    SpaceStatsUtils.reportFileNumAndSize(SpaceCleanStatMachine.this.mScanHandler);
                    SpaceStatsUtils.reportMaxStorageTopApp(SpaceCleanStatMachine.this.mScanHandler);
                }
            } else {
                SpaceCleanStatMachine.this.sendMessage(83);
            }
        }
    };
    private SdcardStateReceiver mSdcardReceiver = new SdcardStateReceiver(GlobalContext.getContext()) {
        protected void handlerSdcardError(String action) {
            HwLog.i(SpaceCleanStatMachine.TAG, "handlerSdcardError recive action:" + action + ", current state is:" + SpaceCleanStatMachine.this.getCurrentState().getName());
            SpaceCleanStatMachine.this.sendMessage(50);
        }
    };
    private LinearLayout mTrashLinearLayout;

    private abstract class SpaceCleanBaseState extends SimpleState {
        abstract SpaceState getState();

        private SpaceCleanBaseState() {
        }

        public String getName() {
            return getState().toString();
        }

        protected void showSdcardWarnningDialog() {
            SpaceCleanStatMachine.this.mDialogManager.createSdcardWarningDlg(SpaceCleanStatMachine.this.mContext.getString(R.string.space_clean_optimize_sdcard_exception_msg_change), true);
        }
    }

    private class NormalCleanEndState extends SpaceCleanBaseState {
        private NormalCleanEndState() {
            super();
        }

        public void enter() {
            if (Utility.isSupportOrientation()) {
                SpaceCleanStatMachine.this.mFragment.getActivity().setRequestedOrientation(-1);
            }
            SpaceCleanStatMachine.this.mDialogManager.hiddenCancelDlg();
            SpaceCleanStatMachine.this.mListController.setCleanEnd(SpaceCleanStatMachine.this.mEnterDeepClick, SpaceCleanStatMachine.this.mFragment, SpaceCleanStatMachine.this.mScanHandler);
            boolean noTrash = false;
            Message msg = SpaceCleanStatMachine.this.getCurrentMessage();
            if (msg != null) {
                noTrash = msg.arg2 == 3;
            }
            SpaceCleanStatMachine.this.mEndViewController.init(SpaceCleanStatMachine.this.mFragment, SpaceCleanStatMachine.this.mScanHandler);
            SpaceCleanStatMachine.this.mEndViewController.setCleanTrashSize(noTrash, SpaceCleanStatMachine.this.cleanTrashSize);
            SpaceCleanStatMachine.this.mEndViewController.showView();
            SpaceCleanStatMachine.this.mScanBtn.setText(R.string.space_clean_complete);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    SpaceStatsUtils.reportOneKeyCleanFinishOp();
                    SpaceCleanStatMachine.this.finishActivity();
                    break;
                case 50:
                    showSdcardWarnningDialog();
                    break;
                case 100:
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }

        SpaceState getState() {
            return SpaceState.NORMAL_CLEAN_END;
        }
    }

    private class NormalCleanningState extends SpaceCleanBaseState {
        private CleanTask mCleanTask;

        private NormalCleanningState() {
            super();
        }

        public void enter() {
            List list = null;
            Message msg = SpaceCleanStatMachine.this.getCurrentMessage();
            if (msg == null) {
                HwLog.i(SpaceCleanStatMachine.TAG, "enter CleanningState, but message is null!!");
            } else {
                list = msg.obj;
            }
            if (list == null) {
                HwLog.i(SpaceCleanStatMachine.TAG, "enter CleanningState, but get chectlist from message is null!!");
                list = Lists.newArrayList();
            }
            SpaceCleanStatMachine.this.mListController.setCleanning();
            SpaceCleanStatMachine.this.mScanBtn.setText(R.string.space_clean_cancel_cleanning);
            SpaceCleanStatMachine.this.mHeadViewController.setTrashCleaning();
            this.mCleanTask = CleanTask.startCleanWithInterval(list, SpaceCleanStatMachine.this.mCleanListener, SpaceCleanStatMachine.this.mScanHandler);
        }

        public void exit() {
            if (this.mCleanTask != null && !this.mCleanTask.isEnd()) {
                HwLog.i(SpaceCleanStatMachine.TAG, "NormalCleanningState exit but mCleantask is not end, cancel it");
                this.mCleanTask.cancel();
            }
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    HwLog.i(SpaceCleanStatMachine.TAG, "user click cancel clean");
                    HsmStat.statE(Events.E_OPTMIZE_ONE_KEY_CLEAN_CANCEL);
                    if (this.mCleanTask != null) {
                        this.mCleanTask.cancel();
                        break;
                    }
                    break;
                case 4:
                    SpaceCleanStatMachine.this.mDialogManager.showCancelCleanningDlg();
                    break;
                case 6:
                    SpaceCleanStatMachine.this.transitionTo(SpaceCleanStatMachine.this.mNormalCleanEndState);
                    break;
                case 20:
                case 100:
                    break;
                case 50:
                    if (this.mCleanTask != null) {
                        this.mCleanTask.cancel();
                    }
                    showSdcardWarnningDialog();
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }

        SpaceState getState() {
            return SpaceState.NORMAL_CLEANNING;
        }
    }

    private class NormalScanEndState extends SpaceCleanBaseState {
        private TrashListAdapter mAdapter;

        private NormalScanEndState() {
            super();
        }

        public void enter() {
            SpaceCleanStatMachine.this.mListController.setScanEnd(SpaceCleanStatMachine.this.mScanHandler.getNormalTrashes(), SpaceCleanStatMachine.this.mCheckClicker, SpaceCleanStatMachine.this.mItemClicker, SpaceCleanStatMachine.this.mLongClicker, SpaceCleanStatMachine.this.mGroupCheckClicker);
            this.mAdapter = SpaceCleanStatMachine.this.mListController.getTrashListAdapter();
            SpaceCleanStatMachine.this.mHeadViewController.setTrashTotalTrashSize(this.mAdapter.getTotalSize());
            refreshCheckedSize();
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    HsmStat.statE(Events.E_OPTMIZE_ONE_KEY_CLEAN);
                    tryToCleanningState();
                    break;
                case 30:
                    refreshCheckedSize();
                    break;
                case 50:
                    showSdcardWarnningDialog();
                    break;
                case 60:
                    msg.obj.toggle();
                    refreshAdapter();
                    break;
                case 80:
                    msg.obj.toggle();
                    refreshAdapter();
                    break;
                case 90:
                    refreshAdapter();
                    break;
                case 100:
                    HwLog.i(SpaceCleanStatMachine.TAG, "receive msg:MSG_onresume");
                    doResumeInScanEnd();
                    if (this.mAdapter.checkIsAllCleaned()) {
                        HwLog.i(SpaceCleanStatMachine.TAG, "all item cleaned, trans to clean end");
                        SpaceCleanStatMachine.this.transitionTo(SpaceCleanStatMachine.this.mNormalCleanEndState);
                    }
                    refreshAdapter();
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }

        SpaceState getState() {
            return SpaceState.NORMAL_SCAN_END;
        }

        protected final boolean tryToCleanningState() {
            List<Trash> checkedList = this.mAdapter.getCheckedTrashes();
            if (checkedList.isEmpty()) {
                return false;
            }
            SpaceStatsUtils.reportOneKeyCleanTrash(checkedList);
            Message msg = SpaceCleanStatMachine.this.getCurrentMessage();
            if (msg == null) {
                HwLog.e(SpaceCleanStatMachine.TAG, "tryToCleanningState msg is null! Current State:" + getState());
                return false;
            }
            msg.obj = checkedList;
            SpaceCleanStatMachine.this.transitionTo(SpaceCleanStatMachine.this.mNormalCleanningState);
            return true;
        }

        protected void refreshAdapter() {
            this.mAdapter.notifyDataSetChanged();
            refreshCheckedSize();
        }

        protected void doResumeInScanEnd() {
            this.mAdapter.refreshAppProcess(false);
            this.mAdapter.handlerRefreshSize();
            refreshCheckedSize();
            if (SpaceCleanStatMachine.this.mJumpItem != null && SpaceCleanStatMachine.this.mJumpItem.isCleaned()) {
                SpaceCleanStatMachine.this.mDialogManager.hideCheckSureDialog();
            }
        }

        protected final void refreshCheckedSize() {
            boolean z = false;
            long checkedSize = this.mAdapter.getCheckedSizeUncleaned();
            String sizeText = Formatter.formatFileSize(GlobalContext.getContext(), checkedSize);
            SpaceCleanStatMachine.this.mScanBtn.setText(GlobalContext.getString(R.string.space_clean_normal_scan_result_bth, sizeText));
            Button -get19 = SpaceCleanStatMachine.this.mScanBtn;
            if (!this.mAdapter.getCheckedTrashes().isEmpty()) {
                z = true;
            }
            -get19.setEnabled(z);
            SpaceCleanStatMachine.this.mHeadViewController.setTrashSelectedTrashSize(checkedSize);
        }
    }

    private class NormalScanningState extends SpaceCleanBaseState {
        protected boolean mScanCanceled;

        private NormalScanningState() {
            super();
        }

        public void enter() {
            this.mScanCanceled = false;
            SpaceCleanStatMachine.this.mListController.setScanning();
            SpaceCleanStatMachine.this.mScanBtn.setText(R.string.space_clean_stop_scan);
        }

        public boolean processMessage(Message msg) {
            if (hanlderScanMsg(msg)) {
                return true;
            }
            switch (msg.what) {
                case 3:
                    HwLog.i(SpaceCleanStatMachine.TAG, "User cancel normal scan");
                    this.mScanCanceled = true;
                    if (SpaceCleanStatMachine.this.mScanHandler == null) {
                        HwLog.i(SpaceCleanStatMachine.TAG, "mScanHandler is null, ignore this cmd");
                        break;
                    }
                    SpaceStatsUtils.reportStopSpaceScanOp("1");
                    SpaceCleanStatMachine.this.mScanHandler.cancelScan(50);
                    break;
                case 4:
                    SpaceCleanStatMachine.this.mDialogManager.showCancelScanningDlg();
                    break;
                case 50:
                    if (SpaceCleanStatMachine.this.mScanHandler != null) {
                        SpaceCleanStatMachine.this.mScanHandler.cancelScan();
                    } else {
                        HwLog.w(SpaceCleanStatMachine.TAG, "recieve MSG_SDCARD_ERROR, but mScanHandler is null!");
                    }
                    showSdcardWarnningDialog();
                    break;
                case 100:
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }

        private boolean hanlderScanMsg(Message msg) {
            switch (msg.what) {
                case 9:
                    if (msg.arg1 == 50 && (msg.obj instanceof ScanDataBean)) {
                        ScanDataBean bean = msg.obj;
                        SpaceCleanStatMachine.this.mHeadViewController.setScanTrashInfo(bean.trashInfo);
                        SpaceCleanStatMachine.this.mHeadViewController.setScanTrashSize(bean.normalTrashSize);
                        SpaceCleanStatMachine.this.mHeadViewController.setScanProgress(bean.normalProgress);
                        break;
                    }
                case 10:
                    if (msg.arg1 != 50) {
                        break;
                    }
                    break;
                case 70:
                    HwLog.i(SpaceCleanStatMachine.TAG, "handler message MSG_NORMAL_SCANNER_START");
                    SpaceCleanStatMachine.this.mBuilder.setScanSTimeMemory(SpaceCleanStatMachine.this.mContext).setStartTime(System.currentTimeMillis());
                    SpaceCleanStatMachine.this.mScanHandler = ScanManager.startScan(SpaceCleanStatMachine.this.mContext, SpaceCleanStatMachine.this.mScanListner);
                    break;
                case SpaceCleanStatMachine.MSG_NORMAL_SCANNER_END /*81*/:
                    HwLog.i(SpaceCleanStatMachine.TAG, "handler message normal scanTask end!");
                    IState targetState = SpaceCleanStatMachine.this.mNormalScanEndState;
                    if (this.mScanCanceled) {
                        msg.arg1 = 2;
                    }
                    if (checkIfNoTrash()) {
                        HwLog.i(SpaceCleanStatMachine.TAG, "no trash, jump to cleanEnd state");
                        msg.arg2 = 3;
                        targetState = SpaceCleanStatMachine.this.mNormalCleanEndState;
                    }
                    SpaceCleanStatMachine.this.transitionTo(targetState);
                    break;
                case 83:
                    int type;
                    synchronized (SpaceCleanStatMachine.this) {
                        type = SpaceCleanStatMachine.this.mEndScanTrashType;
                    }
                    SpaceCleanStatMachine.this.mListController.checkScanEnd(type);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean checkIfNoTrash() {
            Map<Integer, TrashGroup> result = SpaceCleanStatMachine.this.mScanHandler.getNormalTrashes();
            if (result == null || result.isEmpty()) {
                return true;
            }
            for (TrashGroup trashGroup : result.values()) {
                if (!trashGroup.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        SpaceState getState() {
            return SpaceState.NORMAL_SCANNING;
        }
    }

    private static class ScanDataBean {
        public final int normalProgress;
        public final long normalTrashSize;
        public final String trashInfo;

        private ScanDataBean(long trashSize, String trashInfo, int progress) {
            this.normalTrashSize = trashSize;
            this.trashInfo = trashInfo;
            this.normalProgress = progress;
        }
    }

    SpaceCleanStatMachine(Fragment frag, View mainView) {
        super(TAG, Looper.getMainLooper());
        this.mFragment = frag;
        this.mContext = GlobalContext.getContext();
        Activity ac = this.mFragment.getActivity();
        this.mListController = new ListController(ac, mainView.findViewById(R.id.container));
        this.mHeadViewController = new HeadViewController(ac);
        this.mEndViewController = new SpaceCleanEndViewController(ac, mainView.findViewById(R.id.scroll_view), this.mEnterDeepClick);
        this.mListController.setHeadViewController(this.mHeadViewController);
        this.mBuilder = new TrashInfoBuilder();
        this.mScanBtn = (Button) mainView.findViewById(R.id.scan);
        this.mScanBtn.setOnClickListener(this.mScanClicker);
        this.mScanLinearLayout = this.mHeadViewController.getScanLinearLayout();
        this.mScanLinearLayout.setOnClickListener(this.mEnterDeepClick);
        this.mTrashLinearLayout = this.mHeadViewController.getTrashLinearLayout();
        this.mTrashLinearLayout.setOnClickListener(this.mEnterDeepClick);
        this.mDialogManager = new DialogManager(ac, frag);
        setInitialState(this.mNormalScanningState);
    }

    public void start() {
        super.start();
        this.mSdcardReceiver.register();
    }

    public void quit() {
        super.quit();
        this.mSdcardReceiver.unRegseter();
        this.mDialogManager.destory();
        if (this.mScanHandler != null) {
            this.mScanHandler.removeScanListenr(this.mScanListner);
            this.mScanHandler.destory();
        }
    }

    public void startNormalScan() {
        sendMessageDelay(70, 600);
    }

    private boolean handlerTrashItemClick(ITrashItem item) {
        if (!checkScanEndState()) {
            return false;
        }
        int action = item.doClickAction();
        if (action == 4) {
            SpaceStatsUtils.reportEnterDeepManagerNums(1);
            enterSpaceManager();
            return true;
        } else if (action == 2) {
            sendMessage(60, (Object) item);
            return true;
        } else if (action == 3) {
            trash = item.getTrash();
            if (trash instanceof IAppInfo) {
                SpaceStatsUtils.reportSpaceScanFileDetail(item.getTrashType(), ((IAppInfo) trash).getPackageName());
            }
            openSecondary(item);
            return true;
        } else if (action != 5) {
            return false;
        } else {
            trash = item.getTrash();
            if (trash instanceof IAppInfo) {
                SpaceStatsUtils.reportSpaceScanFileDetail(item.getTrashType(), ((IAppInfo) trash).getPackageName());
            }
            openDialog(item);
            return true;
        }
    }

    protected void defaultHandlerMessage(Message msg) {
        switch (msg.what) {
            case 3:
                HwLog.i(TAG, "not state handle the msg CMD_CLICK_SCAN_BUTTON, cur state is " + getCurrentState().getName());
                return;
            case 4:
                finishActivity();
                return;
            default:
                super.defaultHandlerMessage(msg);
                return;
        }
    }

    private void enterSpaceManager() {
        Activity ac = this.mFragment.getActivity();
        if (ac != null && this.mScanHandler != null) {
            Intent intent = new Intent(ac, SpaceManagerActivity.class);
            intent.putExtra("handler_id", this.mScanHandler.getId());
            if (SplitModeUtil.isSplitMode(ac)) {
                HwActivitySplitterImpl.setNotSplit(intent);
            }
            try {
                this.mFragment.startActivityForResult(intent, 7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openSecondary(ITrashItem item) {
        HwLog.d(TAG, "openSecondary start");
        Activity ac = this.mFragment.getActivity();
        if (ac != null && item != null) {
            Intent intent;
            int itemType = item.getTrashType();
            switch (itemType) {
                case 32768:
                    intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ((AppProcessTrashItem) item).getPkgName(), null));
                    break;
                default:
                    intent = new Intent(ac, ListTrashSetActivity.class).putExtra("handler_id", this.mScanHandler.getId()).putExtra(SecondaryConstant.OPEN_SECONDARY_PARAM, item.getOpenSecondaryParam());
                    break;
            }
            if (intent != null) {
                this.mJumpItem = item;
                try {
                    this.mFragment.startActivityForResult(intent, itemType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkScanEndState() {
        if (getCurrentState() == this.mNormalScanEndState) {
            return true;
        }
        return false;
    }

    boolean clickBack() {
        HwLog.i(TAG, "back pressed");
        sendEmptyMessage(4);
        return true;
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean dataChanged = false;
        switch (requestCode) {
            case 32768:
                if (this.mJumpItem != null && (this.mJumpItem instanceof AppProcessTrashItem) && ((AppProcessTrashItem) this.mJumpItem).checkAliveStateChanged()) {
                    dataChanged = true;
                    break;
                }
            default:
                if (resultCode != 1000) {
                    dataChanged = false;
                    break;
                } else {
                    dataChanged = true;
                    break;
                }
        }
        HwLog.i(TAG, "onActivityResult, datachanged:" + dataChanged);
        if (resultCode == 4) {
            HwLog.i(TAG, "onActivityResult recevie Const.RESULT_CODE_FINISH");
            finishActivity();
            return;
        }
        if (dataChanged) {
            sendEmptyMessage(40);
        }
    }

    void onResume() {
        HwLog.i(TAG, "onResume, current stat is " + getCurrentState().getName());
        sendMessage(100);
    }

    private void openDialog(ITrashItem item) {
        if (this.mDetailDialog == null || !this.mDetailDialog.isAdded()) {
            this.mDetailDialog = new TrashDetailDialog(item);
            FragmentTransaction ft = this.mFragment.getFragmentManager().beginTransaction();
            if (!this.mDetailDialog.isVisible()) {
                this.mDetailDialog.show(ft, "normal trash_detail_dialog");
            }
        }
    }

    private void finishActivity() {
        Activity ac = this.mFragment.getActivity();
        if (ac != null) {
            ac.finish();
        }
    }
}
