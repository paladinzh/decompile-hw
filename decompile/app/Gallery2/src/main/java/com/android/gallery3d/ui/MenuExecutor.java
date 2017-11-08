package com.android.gallery3d.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.IntentChooser;
import com.android.gallery3d.app.IntentChooser.AppsListOnClickedlistener;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.IRecycle;
import com.android.gallery3d.data.KeyguardItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaScannerClient;
import com.android.gallery3d.util.PasteWorker.PasteEventHandler;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.util.BurstUtils;
import java.io.File;
import java.util.ArrayList;

public class MenuExecutor implements PasteEventHandler, AppsListOnClickedlistener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static volatile int sFileRecoverProcessCount = 0;
    private static final Object sRecoverLock = new Object();
    private boolean isLocked;
    private int mAction;
    final GalleryContext mActivity;
    private CheckBox mCheckBox;
    private ConfirmDialogListener mConfirmDialogListener;
    private boolean mContinueToPaste;
    private int mCurrentProgress;
    private Bundle mData;
    private ActionDeleteAndConfirm mDeleteDialog;
    private int mDeleteFlag;
    private ProgressDialog mDialog;
    private int mFinishedJobCount;
    private SynchronizedHandler mHandler;
    private IntentChooser mIntentChooser;
    private boolean mIsWholeProcessValid;
    private ProgressListener mListener;
    private final Object mLock;
    private final SelectionManager mSelectionManager;
    private ShareProgressListener mShareProcessor;
    private int mStrategyForPasteSameFile;
    private Future<?> mTask;
    private int mTotalJobCount;
    private boolean mWaitOnStop;

    public interface ExtraActionListener {
        void onExecuteExtraActionEnd();
    }

    private class ConfirmDialogListener implements OnClickListener, OnCancelListener, ExtraActionListener {
        private final int mActionId;
        private Bundle mData;
        private int mDelFlags;
        private final ProgressListener mListener;
        private final ArrayList<Path> mPaths;

        public ConfirmDialogListener(int actionId, ProgressListener listener, Bundle data) {
            this.mActionId = actionId;
            this.mListener = listener;
            this.mData = data;
            this.mPaths = listener.getExecutePath();
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                GalleryLog.d("MenuExecutor", "Click BUTTON_POSITIVE in MenuExecutor");
                if (this.mListener != null) {
                    this.mListener.onConfirmDialogDismissed(true);
                    if (PhotoShareUtils.isHiCloudLogin() && PhotoShareUtils.isCloudPhotoSwitchOpen() && ((this.mListener.isHicloudAlbum() || this.mListener.isSyncedAlbum()) && MenuExecutor.this.mDeleteDialog != null)) {
                        int i;
                        if (this.mListener.isHicloudAlbum() || MenuExecutor.this.mDeleteDialog.getCheckBoxStatus()) {
                            i = 3;
                        } else {
                            i = 1;
                        }
                        this.mDelFlags = i;
                        ReportToBigData.reportForDeleteLocalOrAll(this.mDelFlags, "MenuExecutor");
                    }
                }
                if (this.mListener == null || !this.mListener.excuteExtraAction(this, this.mDelFlags)) {
                    MenuExecutor.this.startAction(R.id.action_delete, R.string.delete, null, false, true, Style.NORMAL_STYLE, this.mPaths, this.mData, this.mDelFlags);
                }
            } else {
                GalleryLog.d("MenuExecutor", "Cancel process in MenuExecutor");
                if (this.mListener != null) {
                    this.mListener.onConfirmDialogDismissed(false);
                }
            }
            MenuExecutor.this.mDeleteDialog = null;
            MenuExecutor.this.mConfirmDialogListener = null;
        }

        public void onCancel(DialogInterface dialog) {
            if (this.mListener != null) {
                this.mListener.onConfirmDialogDismissed(false);
            }
            MenuExecutor.this.mDeleteDialog = null;
            MenuExecutor.this.mConfirmDialogListener = null;
        }

        public void onExecuteExtraActionEnd() {
            GalleryLog.d("MenuExecutor", "onExecuteExtraActionEnd, may be from delete item");
            MenuExecutor.this.mHandler.sendMessage(MenuExecutor.this.mHandler.obtainMessage(20, this.mActionId, this.mDelFlags, this));
        }
    }

    private class GetProcessingList extends BaseJob<ArrayList<Path>> {
        private GetProcessingList() {
        }

        public ArrayList<Path> run(JobContext jc) {
            if (jc.isCancelled()) {
                return null;
            }
            return MenuExecutor.this.mSelectionManager.getSelected(false, jc);
        }

        public String workContent() {
            return "get processing list. size: " + MenuExecutor.this.mSelectionManager.getSelectedCount();
        }
    }

    private class MediaOperation extends BaseJob<Void> {
        private final Bundle mData;
        private final ArrayList<Path> mItems;
        private final ProgressListener mListener;
        private final int mOperation;
        private final boolean mUseCustomProgress;
        final /* synthetic */ MenuExecutor this$0;

        public MediaOperation(MenuExecutor this$0, int operation, ArrayList<Path> items, ProgressListener listener, Bundle data) {
            boolean z = false;
            this.this$0 = this$0;
            this.mOperation = operation;
            this.mItems = items;
            this.mListener = listener;
            this.mData = data;
            if (data != null) {
                z = data.getBoolean("key-customprogress", false);
            }
            this.mUseCustomProgress = z;
        }

        public Void run(JobContext jc) {
            int index = 0;
            if (this.mOperation == R.id.action_recovery) {
                synchronized (MenuExecutor.sRecoverLock) {
                    MenuExecutor.sFileRecoverProcessCount = MenuExecutor.sFileRecoverProcessCount + 1;
                }
                GalleryLog.d("MenuExecutor", MenuExecutor.sFileRecoverProcessCount + " recover in process");
            }
            DataManager manager = this.this$0.mActivity.getDataManager();
            int result = 1;
            try {
                result = operationList(this.mOperation);
                GalleryLog.d("MenuExecutor", "start process in:" + this);
                this.this$0.onProgressStart(this.mListener, this.mOperation, this.mData, this.mItems);
                int size = this.mItems.size();
                if (size == 0) {
                    GalleryLog.d("MenuExecutor", "MediaOperation size is zero!");
                    result = 4;
                }
                for (int i = 0; i < size && this.this$0.mIsWholeProcessValid; i++) {
                    Path id = (Path) this.mItems.get(i);
                    if (jc.isCancelled()) {
                        result = 3;
                        break;
                    }
                    if (manager.getMediaObject(id) == null) {
                        GalleryLog.d("MenuExecutor", "getMediaObject is null before execute");
                    }
                    if (!this.this$0.execute(manager, jc, this.mOperation, id, this.mData)) {
                        result = 2;
                    } else if (this.mListener != null) {
                        this.mListener.onProgressExecuteSuccess(id.toString());
                    }
                    index++;
                    if (!this.mUseCustomProgress) {
                        this.this$0.updateProgress(index, this.mListener);
                    }
                }
                GalleryLog.d("MenuExecutor", "end process in:" + this);
                AsyncCloudAlbum(this.mOperation);
                if (this.mOperation == R.id.action_recovery) {
                    synchronized (MenuExecutor.sRecoverLock) {
                        MenuExecutor.sFileRecoverProcessCount = MenuExecutor.sFileRecoverProcessCount - 1;
                    }
                    GalleryLog.d("MenuExecutor", MenuExecutor.sFileRecoverProcessCount + " recover process left");
                }
                this.this$0.onProgressComplete(result, this.mListener, this.mOperation);
            } catch (Throwable th) {
                GalleryLog.d("MenuExecutor", "end process in:" + this);
                AsyncCloudAlbum(this.mOperation);
                if (this.mOperation == R.id.action_recovery) {
                    synchronized (MenuExecutor.sRecoverLock) {
                        MenuExecutor.sFileRecoverProcessCount = MenuExecutor.sFileRecoverProcessCount - 1;
                        GalleryLog.d("MenuExecutor", MenuExecutor.sFileRecoverProcessCount + " recover process left");
                    }
                }
                this.this$0.onProgressComplete(result, this.mListener, this.mOperation);
            }
            return null;
        }

        private void AsyncCloudAlbum(int operation) {
            if (!RecycleUtils.supportRecycle()) {
                return;
            }
            if (operation == R.string.delete || operation == R.id.action_delete || operation == R.id.action_recovery || operation == R.id.action_thorough_delete) {
                RecycleUtils.startAsyncAlbumInfo();
            }
        }

        private int operationList(int action) {
            switch (action) {
                case R.id.action_delete:
                    if (this.mItems.size() > 0 && "photoshare".equals(((Path) this.mItems.get(0)).getPrefix())) {
                        return 1;
                    }
                case R.id.action_paste:
                    break;
                default:
                    return 1;
            }
            ArrayList<Path> needExpendList = new ArrayList();
            DataManager manager = this.this$0.mActivity.getDataManager();
            for (Path path : this.mItems) {
                if (!this.this$0.mIsWholeProcessValid) {
                    return 3;
                }
                needExpendList.addAll(BurstUtils.getPathFromBurstCover(manager.getMediaObject(path), manager, action));
            }
            this.mItems.clear();
            this.mItems.addAll(needExpendList);
            return 1;
        }

        public boolean isHeavyJob() {
            switch (this.mOperation) {
                case R.string.delete:
                case R.id.action_delete:
                case R.id.action_paste:
                case R.id.action_remove:
                    return this.mItems.size() > 2;
                default:
                    return super.isHeavyJob();
            }
        }

        public String workContent() {
            return String.format("do operation: %s, item count: %s ", new Object[]{Integer.valueOf(this.mOperation), Integer.valueOf(this.mItems.size())});
        }
    }

    private class PasteSameFileDialogClickLisntener implements OnClickListener, OnCancelListener {
        private PasteSameFileDialogClickLisntener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -3:
                    MenuExecutor.this.setContinueToPasteState(true);
                    if (MenuExecutor.this.mCheckBox != null && MenuExecutor.this.mCheckBox.isChecked()) {
                        MenuExecutor.this.mStrategyForPasteSameFile = 22;
                        break;
                    }
                case -2:
                    MenuExecutor.this.setContinueToPasteState(false);
                    if (MenuExecutor.this.mCheckBox != null && MenuExecutor.this.mCheckBox.isChecked()) {
                        MenuExecutor.this.mStrategyForPasteSameFile = 23;
                        break;
                    }
                case -1:
                    MenuExecutor.this.setContinueToPasteState(false);
                    MenuExecutor.this.mIsWholeProcessValid = false;
                    break;
            }
            MenuExecutor.this.notifyAllWaitingLock();
        }

        public void onCancel(DialogInterface dialog) {
            MenuExecutor.this.setContinueToPasteState(false);
            MenuExecutor.this.notifyAllWaitingLock();
        }
    }

    private class PasteUserCancelDialogClickLisntener implements OnClickListener, OnCancelListener {
        private PasteUserCancelDialogClickLisntener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    MenuExecutor.this.setContinueToPasteState(true);
                    if (MenuExecutor.this.mDialog != null) {
                        MenuExecutor.this.mDialog.show();
                        break;
                    }
                    break;
                case -1:
                    MenuExecutor.this.setContinueToPasteState(false);
                    MenuExecutor.this.mIsWholeProcessValid = false;
                    break;
            }
            MenuExecutor.this.notifyAllWaitingLock();
        }

        public void onCancel(DialogInterface dialog) {
            MenuExecutor.this.setContinueToPasteState(true);
            if (MenuExecutor.this.mDialog != null) {
                MenuExecutor.this.mDialog.show();
            }
            MenuExecutor.this.notifyAllWaitingLock();
        }
    }

    private class ProcessingListFinished implements FutureListener<ArrayList<Path>> {
        private ProcessingListFinished() {
        }

        public void onFutureDone(Future<ArrayList<Path>> future) {
            MenuExecutor.this.mTask = null;
            if (future == null || future.get() == null) {
                GalleryLog.d("MenuExecutor", "future is empty");
            } else {
                handleInMain((ArrayList) future.get());
            }
        }

        private void handleInMain(final ArrayList<Path> list) {
            MenuExecutor.this.mHandler.post(new Runnable() {
                public void run() {
                    MenuExecutor.this.startActionReady(list);
                }
            });
        }
    }

    public interface ProgressListener {
        boolean excuteExtraAction(ExtraActionListener extraActionListener, int i);

        ArrayList<Path> getExecutePath();

        boolean isHicloudAlbum();

        boolean isSyncedAlbum();

        void onConfirmDialogDismissed(boolean z);

        void onConfirmDialogShown();

        void onProgressComplete(int i);

        void onProgressExecuteSuccess(String str);

        void onProgressStart();

        void onProgressUpdate(int i);

        void setOnCompleteToastContent(String str);
    }

    public interface ShareProgressListener {
        void onProcessDone();

        void process(Path path);
    }

    public static class SimpleProgressListener implements ProgressListener {
        public void onConfirmDialogShown() {
        }

        public void onConfirmDialogDismissed(boolean confirmed) {
        }

        public void onProgressStart() {
        }

        public void onProgressUpdate(int index) {
        }

        public void onProgressExecuteSuccess(String path) {
        }

        public void onProgressComplete(int result) {
        }

        public boolean excuteExtraAction(ExtraActionListener listener, int deleteFlag) {
            return false;
        }

        public boolean isSyncedAlbum() {
            return false;
        }

        public boolean isHicloudAlbum() {
            return false;
        }

        public ArrayList<Path> getExecutePath() {
            return null;
        }

        public void setOnCompleteToastContent(String content) {
        }
    }

    public interface SlotDeleteProgressListener extends ProgressListener {
        void onDeleteStart();
    }

    public static class SimpleSlotDeleteProgressListener extends SimpleProgressListener implements SlotDeleteProgressListener {
        public void onDeleteStart() {
        }

        public void setOnCompleteToastContent(String str) {
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 18;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 19;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 20;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 21;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 22;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 23;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 24;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 25;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 26;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 27;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 28;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 2;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 29;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 30;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 3;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 31;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 32;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 33;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 34;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 4;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 5;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 35;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 36;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 37;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 6;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 38;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 39;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 40;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 41;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 42;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 43;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 44;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 7;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 45;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 46;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 8;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 47;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 48;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 49;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 50;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 51;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 52;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 53;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 54;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 55;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 56;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 57;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 9;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 58;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 59;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 60;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 61;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 62;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 63;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 64;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 65;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 66;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 67;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 68;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 69;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 70;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 71;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 72;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 73;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 74;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 75;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 76;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 10;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 11;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 77;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 12;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 78;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 79;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 13;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 14;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 15;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 80;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 81;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 16;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 82;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 83;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 84;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 85;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 86;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 87;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 88;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 89;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 90;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 17;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    public void setShareProcessor(ShareProgressListener listener) {
        this.mShareProcessor = listener;
    }

    public MenuExecutor(GalleryContext activity, SelectionManager selectionManager, GLRoot glRoot) {
        this(activity, selectionManager, null, glRoot);
    }

    public MenuExecutor(GalleryContext activity, SelectionManager selectionManager) {
        this(activity, selectionManager, null);
    }

    public MenuExecutor(GalleryContext activity, SelectionManager selectionManager, IntentChooser intentChooser, GLRoot glRoot) {
        this.mDeleteFlag = 0;
        this.mStrategyForPasteSameFile = 21;
        this.mContinueToPaste = true;
        this.mIsWholeProcessValid = true;
        this.mTotalJobCount = 0;
        this.mFinishedJobCount = 0;
        this.mLock = new Object();
        this.isLocked = false;
        this.mActivity = (GalleryContext) Utils.checkNotNull(activity);
        this.mSelectionManager = (SelectionManager) Utils.checkNotNull(selectionManager);
        this.mIntentChooser = intentChooser;
        setGLRoot(glRoot);
    }

    public void setIntentChooser(IntentChooser chooser) {
        this.mIntentChooser = chooser;
    }

    public void setGLRoot(GLRoot glRoot) {
        if (this.mHandler != null) {
            this.mHandler.setGLRoot(glRoot);
        } else {
            this.mHandler = new SynchronizedHandler(glRoot) {
                public void handleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            if (message.obj != null) {
                                ProgressListener listener = (ProgressListener) message.obj;
                                if (!(listener instanceof SlotDeleteProgressListener)) {
                                    MenuExecutor.this.stopTaskAndDismissDialog();
                                }
                                listener.onProgressComplete(message.arg1);
                            } else {
                                MenuExecutor.this.stopTaskAndDismissDialog();
                            }
                            MenuExecutor.this.mSelectionManager.leaveSelectionMode();
                            break;
                        case 2:
                            MenuExecutor.this.mCurrentProgress = message.arg1;
                            if (MenuExecutor.this.mDialog != null) {
                                MenuExecutor.this.mDialog.setProgress(message.arg1);
                            }
                            if (message.obj != null) {
                                ((ProgressListener) message.obj).onProgressUpdate(message.arg1);
                                break;
                            }
                            break;
                        case 3:
                            if (message.obj != null) {
                                message.obj.onProgressStart();
                                break;
                            }
                            break;
                        case 15:
                            MenuExecutor.this.handlePasteInitEventUiThread(message);
                            break;
                        case 16:
                            MenuExecutor.this.handlePasteEventUiThread(message);
                            break;
                        case 17:
                            GalleryLog.d("MenuExecutor", "Paste complete.");
                            break;
                        case 20:
                            GalleryLog.d("MenuExecutor", "receive delete from GL thread to UI thread to do the real delete");
                            if (message.obj != null) {
                                ConfirmDialogListener listener2 = message.obj;
                                MenuExecutor.this.startAction(message.arg1, R.string.delete, listener2.mListener, false, false, Style.NORMAL_STYLE, listener2.mPaths, listener2.mData, message.arg2);
                                break;
                            }
                            break;
                        case 30:
                            if (MenuExecutor.this.mShareProcessor != null) {
                                MenuExecutor.this.mShareProcessor.onProcessDone();
                            }
                            MenuExecutor.this.mShareProcessor = null;
                            break;
                        case 40:
                            if (message.arg1 != 2) {
                                String outputFileName = ((MediaItem) MenuExecutor.this.mActivity.getDataManager().getMediaObject(MenuExecutor.this.getSingleSelectedPath())).getOutputFileName();
                                if (outputFileName != null) {
                                    MediaScannerClient mediaScannerClient = new MediaScannerClient(MenuExecutor.this.mActivity.getActivityContext(), new File(outputFileName), null);
                                    break;
                                }
                            }
                            ContextedUtils.showToastQuickly(MenuExecutor.this.mActivity.getActivityContext(), MenuExecutor.this.mActivity.getResources().getString(R.string.save_fail), 1);
                            return;
                            break;
                        case 50:
                            MenuExecutor.this.stopTaskAndDismissDialog();
                            if (message.obj != null) {
                                ((ProgressListener) message.obj).onProgressComplete(message.arg1);
                                break;
                            }
                            break;
                        case 100:
                            MenuExecutor.this.mDialog = (ProgressDialog) message.obj;
                            MenuExecutor.this.mDialog.show();
                            MenuExecutor.this.mDialog.setProgress(MenuExecutor.this.mCurrentProgress);
                            GalleryLog.d("MenuExecutor", "process MSG_TASK_SHOW_DIALOG");
                            break;
                    }
                }
            };
        }
    }

    public boolean stopTaskAndDismissDialog() {
        boolean haveDialog = this.mDialog != null;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(100);
            GalleryLog.d("MenuExecutor", "remove MSG_TASK_SHOW_DIALOG");
        }
        if (this.mTask != null) {
            if (!this.mWaitOnStop) {
                this.mTask.cancel();
            }
            this.mTask.waitDone();
            GalleryUtils.dismissDialogSafely(this.mDialog, null);
            this.mDialog = null;
            this.mCurrentProgress = 0;
            this.mTask = null;
        }
        return haveDialog;
    }

    public void pause() {
        stopTaskAndDismissDialog();
    }

    public void updateProgress(int index, ProgressListener listener) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, index, 0, listener));
    }

    private void onProgressStart(ProgressListener listener, int operation, Bundle data, ArrayList<Path> itemList) {
        if (this.mIsWholeProcessValid && data != null) {
            DataManager manager = this.mActivity.getDataManager();
            switch (operation) {
                case R.string.delete:
                case R.id.action_delete:
                case R.id.action_recovery:
                    if (data.getInt("recycle_flag", 0) != 0) {
                        data.putInt("key-pastestate", 2);
                        this.mIsWholeProcessValid = manager.initPaste(data, this, itemList);
                        break;
                    }
                    break;
                case R.id.action_paste:
                    this.mIsWholeProcessValid = manager.initPaste(data, this, itemList);
                    break;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, listener));
        }
    }

    private void onProgressComplete(int result, ProgressListener listener, int operation) {
        this.mIsWholeProcessValid = true;
        switch (operation) {
            case R.id.action_share:
                this.mHandler.sendMessage(this.mHandler.obtainMessage(50, result, 0, listener));
                this.mHandler.sendMessage(this.mHandler.obtainMessage(30, result, 0, listener));
                return;
            case R.id.action_delete:
                if (!((this.mDeleteFlag & 2) == 0 || PhotoShareUtils.getServer() == null)) {
                    try {
                        PhotoShareUtils.getServer().deleteGeneralFile();
                        GalleryLog.d("photoshareLogTag", "call deleteGeneralFile");
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                return;
            case R.id.action_paste:
                this.mActivity.getDataManager().onPasteComplete(this);
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                return;
            case R.id.action_set_as_favorite:
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                GalleryLog.v("MenuExecutor", "action_set_as_favorite onProgressComplete");
                return;
            case R.id.action_cancel_favorite:
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                GalleryLog.v("MenuExecutor", "action_cancel_favorite onProgressComplete");
                return;
            case R.id.action_output:
                this.mHandler.sendMessage(this.mHandler.obtainMessage(40, result, 0, listener));
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                return;
            default:
                this.mHandler.sendMessage(this.mHandler.obtainMessage(1, result, 0, listener));
                return;
        }
    }

    private Path getSingleSelectedPath() {
        boolean z = true;
        ArrayList<Path> ids = this.mSelectionManager.getSelected(true);
        if (ids.size() != 1) {
            z = false;
        }
        Utils.assertTrue(z);
        return (Path) ids.get(0);
    }

    private Intent getIntentBySingleSelectedPath(String action) {
        DataManager manager = this.mActivity.getDataManager();
        Path path = getSingleSelectedPath();
        return new Intent(action).setDataAndType(manager.getContentUri(path), getMimeType(manager.getMediaType(path)));
    }

    private void processIntentForSetAs(Intent intent) {
        MediaObject mo = this.mActivity.getDataManager().getMediaObject(getSingleSelectedPath());
        if ((mo instanceof MediaItem) && ((MediaItem) mo).isDrm()) {
            intent.setPackage(HicloudAccountManager.PACKAGE_NAME);
        }
    }

    private void onMenuClicked(int action, ProgressListener listener) {
        onMenuClicked(action, listener, false, true);
    }

    public void onMenuClicked(Action action, ProgressListener listener, boolean waitOnStop, boolean showDialog) {
        int actionid;
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                actionid = R.id.action_select_all;
                break;
            case 2:
                actionid = R.id.action_delete;
                break;
            case 3:
                actionid = R.id.action_edit;
                break;
            case 4:
            case 5:
                actionid = R.id.action_keyguard_like;
                break;
            case 7:
            case 8:
                actionid = R.id.action_set_as_favorite;
                break;
            case 10:
                actionid = R.id.action_thorough_delete;
                break;
            case 11:
                actionid = R.id.action_recovery;
                break;
            case 12:
                actionid = R.id.action_remove;
                break;
            case 16:
                actionid = R.id.action_setas;
                break;
            default:
                return;
        }
        onMenuClicked(actionid, listener, waitOnStop, showDialog);
    }

    public void onMenuClicked(int action, ProgressListener listener, boolean waitOnStop, boolean showDialog) {
        int title;
        Intent intent;
        switch (action) {
            case R.id.action_select_all:
                if (this.mSelectionManager.inSelectAllMode()) {
                    this.mSelectionManager.deSelectAll();
                } else {
                    this.mSelectionManager.selectAll();
                }
                return;
            case R.id.action_photoshare_download:
            case R.id.action_photoshare_download_short:
                title = R.string.photoshare_download;
                break;
            case R.id.action_delete:
                title = R.string.delete;
                break;
            case R.id.action_recovery:
                title = R.string.toolbarbutton_recover;
                break;
            case R.id.action_thorough_delete:
                title = R.string.delete;
                break;
            case R.id.action_set_as_favorite:
                title = R.string.favorite_set_as_favorite_general;
                break;
            case R.id.action_cancel_favorite:
                title = R.string.favorite_cancel_favorite;
                break;
            case R.id.action_import:
                title = R.string.Import;
                break;
            case R.id.action_output:
                title = R.string.saving_image;
                break;
            case R.id.action_edit:
                GalleryUtils.startActivityWithChooser(this.mActivity, getIntentBySingleSelectedPath("android.intent.action.EDIT").setFlags(1), null);
                return;
            case R.id.action_more_edit:
                intent = getIntentBySingleSelectedPath("android.intent.action.EDIT").addFlags(1);
                intent.putExtra("mimeType", intent.getType());
                if (this.mIntentChooser != null) {
                    this.mIntentChooser.setGridListOnclickedlistener(this);
                    this.mIntentChooser.setAwaysUseOption(true);
                    this.mIntentChooser.startDialogWithChooser(intent, R.string.more_edit);
                } else {
                    GalleryUtils.startActivityWithChooser(this.mActivity, intent, this.mActivity.getString(R.string.set_as_new));
                }
                return;
            case R.id.action_rotate_ccw:
                title = R.string.rotate_left;
                break;
            case R.id.action_rotate_cw:
                title = R.string.rotate_right;
                break;
            case R.id.action_crop:
                return;
            case R.id.action_setas:
                intent = getIntentBySingleSelectedPath("android.intent.action.ATTACH_DATA").addFlags(1);
                intent.putExtra("mimeType", intent.getType());
                processIntentForSetAs(intent);
                if (this.mIntentChooser != null) {
                    this.mIntentChooser.setGridListOnclickedlistener(this);
                    this.mIntentChooser.startDialogWithChooser(intent, R.string.set_as_new);
                } else {
                    GalleryUtils.startActivityWithChooser(this.mActivity, intent, this.mActivity.getString(R.string.set_as_new));
                }
                return;
            case R.id.action_show_on_map:
                title = R.string.show_on_map;
                break;
            case R.id.action_show:
                title = R.string.show;
                break;
            case R.id.action_remove:
                title = R.string.action_remove_title;
                break;
            case R.id.action_keyguard_like:
                title = R.string.emui30_keyguard_cover_like;
                break;
            default:
                return;
        }
        startAction(action, title, listener, waitOnStop, showDialog);
    }

    public void onMenuClicked(Action action, String confirmMsg, String title, ProgressListener listener) {
        onMenuClicked(action, confirmMsg, title, listener, null);
    }

    public void onMenuClicked(Action action, String confirmMsg, String title, ProgressListener listener, Bundle data) {
        this.mData = data;
        int actionid = 0;
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 2:
                actionid = R.id.action_delete;
                break;
            case 4:
            case 5:
                actionid = R.id.action_keyguard_like;
                break;
            case 6:
                actionid = R.id.action_more_edit;
                break;
            case 7:
                actionid = R.id.action_set_as_favorite;
                break;
            case 9:
                actionid = R.id.action_photoshare_download_short;
                break;
            case 10:
                actionid = R.id.action_thorough_delete;
                break;
            case 11:
                actionid = R.id.action_recovery;
                break;
            case 12:
                actionid = R.id.action_remove;
                break;
            case 13:
                actionid = R.id.action_rotate_ccw;
                break;
            case 14:
                actionid = R.id.action_rotate_cw;
                break;
            case 15:
                actionid = R.id.action_output;
                break;
            case 16:
                actionid = R.id.action_setas;
                break;
            case 17:
                actionid = R.id.action_show_on_map;
                break;
        }
        onMenuClicked(actionid, confirmMsg, title, listener);
    }

    public void onMenuClicked(int action, String confirmMsg, String confirmTitle, ProgressListener listener) {
        if (confirmMsg == null && confirmTitle == null) {
            onMenuClicked(action, listener);
            return;
        }
        if (listener != null) {
            listener.onConfirmDialogShown();
        }
        this.mConfirmDialogListener = new ConfirmDialogListener(action, listener, this.mData);
        int flag = 0;
        if (this.mData != null) {
            flag = this.mData.getInt("recycle_flag", 0);
        }
        if (flag == 0) {
            this.mDeleteDialog = new ActionDeleteAndConfirm(this.mActivity.getActivityContext(), confirmMsg, confirmTitle, action == R.id.action_remove ? R.string.action_remove_title : R.string.delete, R.string.cancel);
        } else if (flag == 1) {
            this.mConfirmDialogListener.onClick(null, -1);
        } else {
            boolean isCloudOperateTogether = PhotoShareUtils.isHiCloudLoginAndCloudPhotoSwitchOpen();
            int totalCount = this.mSelectionManager.getItemSelectCount();
            if (flag == 2) {
                if (isCloudOperateTogether) {
                    confirmTitle = this.mActivity.getResources().getQuantityString(R.plurals.delete_synced_photo_msg, totalCount, new Object[]{Integer.valueOf(30)});
                } else {
                    confirmTitle = this.mActivity.getResources().getQuantityString(R.plurals.delete_local_photo_msg, totalCount, new Object[]{Integer.valueOf(30)});
                }
            }
            this.mDeleteDialog = new ActionRecycleAndConfirm(this.mActivity.getActivityContext(), confirmMsg, confirmTitle, R.string.delete, R.string.cancel, flag);
        }
        if (this.mDeleteDialog != null) {
            this.mDeleteDialog.setOnClickListener(this.mConfirmDialogListener);
            this.mDeleteDialog.setOnCancelListener(this.mConfirmDialogListener);
            if (listener != null) {
                this.mDeleteDialog.updateStatus(listener.isSyncedAlbum(), listener.isHicloudAlbum());
            }
            this.mDeleteDialog.show();
        }
    }

    public void dismissAlertDialog() {
        if (this.mDeleteDialog != null) {
            this.mDeleteDialog.dismiss();
            if (this.mConfirmDialogListener != null) {
                this.mConfirmDialogListener.onCancel(null);
            }
            this.mConfirmDialogListener = null;
            this.mDeleteDialog = null;
        }
    }

    public void startAction(int action, int title, ProgressListener listener) {
        startAction(action, title, listener, false, true);
    }

    public void startAction(int action, int title, ProgressListener listener, boolean waitOnStop, boolean showDialog) {
        showDialog = showDialog ? (action == R.id.action_output || this.mSelectionManager.getSelectedCount() > 1) ? true : this.mSelectionManager.isAlbumSet() : false;
        startAction(action, title, listener, waitOnStop, showDialog, Style.NORMAL_STYLE, null, null);
    }

    public void startAction(int action, int title, ProgressListener listener, boolean waitOnStop, boolean showDialog, Style style, ArrayList<Path> ids, Bundle data) {
        startAction(action, title, listener, waitOnStop, showDialog, style, ids, data, 0);
    }

    public boolean startAction(int action, int title, ProgressListener listener, boolean waitOnStop, boolean showDialog, Style style, ArrayList<Path> ids, Bundle data, int deleteFlag) {
        stopTaskAndDismissDialog();
        return startActionDataInitialize(action, title, listener, waitOnStop, showDialog, style, ids, data, deleteFlag);
    }

    private boolean startActionDataInitialize(int action, int title, ProgressListener listener, boolean waitOnStop, boolean showDialog, Style style, ArrayList<Path> ids, Bundle data, int deleteFlag) {
        this.mAction = action;
        this.mData = data;
        this.mListener = listener;
        this.mDeleteFlag = deleteFlag;
        if (showDialog) {
            ProgressDialog dialog = MenuExecutorFactory.create(this.mActivity, title, this.mSelectionManager.getSelectedCount(), this, style, data);
            if (listener instanceof SlotDeleteProgressListener) {
                Message message = this.mHandler.obtainMessage();
                message.what = 100;
                message.obj = dialog;
                this.mHandler.sendMessageDelayed(message, 500);
                GalleryLog.d("MenuExecutor", "send MSG_TASK_SHOW_DIALOG");
            } else {
                this.mDialog = dialog;
                if (action != R.id.action_paste || ids == null) {
                    this.mDialog.show();
                }
            }
        } else {
            this.mDialog = null;
        }
        GalleryLog.i("MenuExecutor", "The list getting for action is start");
        if (ids == null) {
            ids = listener == null ? null : listener.getExecutePath();
        }
        if (ids != null) {
            startActionReady(ids);
        } else {
            submitGetProcessListJob();
        }
        this.mWaitOnStop = waitOnStop;
        return true;
    }

    public void startActionReady(ArrayList<Path> list) {
        if (this.mListener instanceof SlotDeleteProgressListener) {
            this.mListener.onDeleteStart();
        }
        this.mCurrentProgress = 0;
        MediaOperation operation = new MediaOperation(this, this.mAction, list, this.mListener, this.mData);
        GalleryLog.d("MenuExecutor", "The list getting for action is ready, will run in:" + operation);
        this.mTask = this.mActivity.getThreadPool().submit(operation, null);
    }

    public void submitGetProcessListJob() {
        if (this.mTask != null) {
            this.mTask.cancel();
            this.mTask = null;
        }
        this.mTask = this.mActivity.getThreadPool().submit(new GetProcessingList(), new ProcessingListFinished());
    }

    public static String getMimeType(int type) {
        switch (type) {
            case 2:
                return "image/*";
            case 4:
                return "video/*";
            default:
                return "*/*";
        }
    }

    private boolean execute(DataManager manager, JobContext jc, int cmd, Path path, Bundle data) {
        boolean z = true;
        GalleryLog.v("MenuExecutor", "Execute cmd: " + cmd + " for " + path);
        long startTime = System.currentTimeMillis();
        IRecycle iRecycle;
        ArrayList<Path> paths;
        int size;
        int i;
        switch (cmd) {
            case R.string.delete:
            case R.id.action_delete:
                int flag = 0;
                if (data != null) {
                    flag = data.getInt("recycle_flag", 0);
                }
                if (flag != 0) {
                    manager.paste(path, data, this);
                } else if (this.mDeleteFlag == 0) {
                    manager.delete(path);
                } else {
                    z = manager.delete(path, this.mDeleteFlag);
                }
                GalleryLog.v("MenuExecutor", "Execute delete for " + path);
                break;
            case R.string.rename:
                if (data != null) {
                    z = manager.rename(path, data.getString("key_bucket_name_alias"));
                    GalleryLog.v("MenuExecutor", "Execute rename for " + path);
                    break;
                }
                GalleryLog.d("MenuExecutor", "rename data == null");
                break;
            case R.string.move_out:
                manager.moveOUT(path);
                GalleryLog.v("MenuExecutor", "Execute move out for " + path);
                break;
            case R.string.move_in:
                manager.moveIN(path);
                GalleryLog.v("MenuExecutor", "Execute move in for " + path);
                break;
            case R.id.action_toggle_full_caching:
                int cacheFlag;
                MediaObject obj = manager.getMediaObject(path);
                if (obj.getCacheFlag() == 2) {
                    cacheFlag = 1;
                } else {
                    cacheFlag = 2;
                }
                obj.cache(cacheFlag);
                GalleryLog.v("MenuExecutor", "Execute caching flag for " + path);
                break;
            case R.id.action_share:
                if (this.mShareProcessor != null) {
                    this.mShareProcessor.process(path);
                }
                if (this.mIntentChooser != null) {
                    this.mIntentChooser.setGridListOnclickedlistener(this);
                }
                GalleryLog.v("MenuExecutor", "Execute share for " + path);
                break;
            case R.id.action_photoshare_download:
            case R.id.action_photoshare_download_short:
                GalleryLog.v("MenuExecutor", "Execute download photoshare for " + path);
                break;
            case R.id.action_recovery:
                MediaObject mediaObject = manager.getMediaObject(path);
                if (mediaObject instanceof IRecycle) {
                    iRecycle = (IRecycle) mediaObject;
                    if (!iRecycle.isHwBurstCover()) {
                        recovery(manager, path, data);
                        break;
                    }
                    paths = iRecycle.getBurstCoverPath();
                    size = paths.size();
                    for (i = 0; i < size; i++) {
                        recovery(manager, (Path) paths.get(i), data);
                    }
                    break;
                }
                break;
            case R.id.action_thorough_delete:
                MediaObject managerMediaObject = manager.getMediaObject(path);
                ContentResolver resolver = this.mActivity.getActivityContext().getContentResolver();
                if (managerMediaObject instanceof IRecycle) {
                    iRecycle = (IRecycle) managerMediaObject;
                    if (iRecycle.isHwBurstCover()) {
                        paths = iRecycle.getBurstCoverPath();
                        size = paths.size();
                        for (i = 0; i < size; i++) {
                            RecycleUtils.delete(resolver, manager.getMediaObject((Path) paths.get(i)), data);
                        }
                    } else {
                        RecycleUtils.delete(resolver, managerMediaObject, data);
                    }
                }
                GalleryLog.v("MenuExecutor", "Execute action_thorough_delete for " + path);
                break;
            case R.id.action_paste:
                manager.paste(path, data, this);
                GalleryLog.v("MenuExecutor", "Execute paste for " + path);
                break;
            case R.id.action_set_as_favorite:
                if (((MediaItem) manager.getMediaObject(path)).isMyFavorite()) {
                    manager.cancelFavorite(path, this.mActivity.getAndroidContext());
                } else {
                    manager.setAsFavorite(path, this.mActivity.getAndroidContext());
                }
                GalleryLog.v("MenuExecutor", "Execute update favorite for " + path);
                break;
            case R.id.action_cancel_favorite:
                manager.cancelFavorite(path, this.mActivity.getAndroidContext());
                GalleryLog.v("MenuExecutor", "Execute cancel favorite for " + path);
                break;
            case R.id.remove_from_story_album:
                String code = "";
                if (this.mData != null) {
                    code = this.mData.getString("keyValue", "");
                }
                manager.removeFromStoryAlbum(path, code);
                break;
            case R.id.action_import:
                z = manager.getMediaObject(path).Import();
                GalleryLog.v("MenuExecutor", "Execute import for " + path);
                break;
            case R.id.action_output:
                z = ((MediaItem) manager.getMediaObject(path)).saveToOutput();
                GalleryLog.v("MenuExecutor", "Execute output for " + path);
                break;
            case R.id.action_rotate_ccw:
                manager.rotate(path, -90);
                GalleryLog.v("MenuExecutor", "Execute rotate -90 for " + path);
                break;
            case R.id.action_rotate_cw:
                manager.rotate(path, 90);
                GalleryLog.v("MenuExecutor", "Execute rotate 90 for " + path);
                break;
            case R.id.action_show_on_map:
                double[] latlng = new double[2];
                ((MediaItem) manager.getMediaObject(path)).getLatLong(latlng);
                if (GalleryUtils.isValidLocation(latlng[0], latlng[1])) {
                    GalleryUtils.showOnMap(this.mActivity.getActivityContext(), latlng[0], latlng[1]);
                }
                GalleryLog.v("MenuExecutor", "Execute show on map for " + path);
                break;
            case R.id.action_remove:
                manager.delete(path);
                GalleryLog.v("MenuExecutor", "Execute delete for " + path);
                break;
            case R.id.action_keyguard_like:
                KeyguardItem item = (KeyguardItem) manager.getMediaObject(path);
                if ((item.getExtraTag() & 4) != 0) {
                    item.cancelKeyguardLike(this.mActivity.getAndroidContext());
                } else {
                    item.setAsKeyguardLike(this.mActivity.getAndroidContext());
                }
                GalleryLog.v("MenuExecutor", "Execute update favorite for " + path);
                break;
            default:
                throw new AssertionError();
        }
        GalleryLog.v("MenuExecutor", "It takes " + (System.currentTimeMillis() - startTime) + " ms to execute cmd for " + path);
        return z;
    }

    private void recovery(DataManager manager, Path path, Bundle data) {
        boolean cloudFile = false;
        MediaObject mediaObject = manager.getMediaObject(path);
        if (mediaObject instanceof IRecycle) {
            String filePath = ((IRecycle) mediaObject).getSourcePath();
            if ((mediaObject instanceof GalleryMediaItem) && ((GalleryMediaItem) mediaObject).getLocalMediaId() == -1) {
                cloudFile = true;
            }
            if ((filePath == null || filePath.length() == 0) && !cloudFile) {
                GalleryLog.v("MenuExecutor", "Execute recovery for " + filePath);
                return;
            }
            if (!TextUtils.isEmpty(filePath)) {
                data.putString("recovery_file_name", new File(filePath).getName());
            }
            manager.paste(path, data, this);
        }
    }

    public boolean onPasteInitEvent(int event, Bundle data) {
        if (this.mActivity == null || (ApiHelper.API_VERSION_MIN_17 && this.mActivity.isDestroyed())) {
            return false;
        }
        switch (event) {
            case 0:
                resetPasteFlag();
                resetProgressDialogForPaste(data);
                return true;
            case 3:
                onPasteNoSpace(data);
                return false;
            case 4:
                return false;
            default:
                return false;
        }
    }

    public boolean onPasteEvent(int event, Bundle data) {
        if (this.mActivity == null || (ApiHelper.API_VERSION_MIN_17 && this.mActivity.isDestroyed())) {
            setContinueToPasteState(false);
            this.mIsWholeProcessValid = false;
            return false;
        }
        setContinueToPasteState(true);
        switch (event) {
            case 1:
                setContinueToPasteState(false);
                break;
            case 2:
                setContinueToPasteState(onPasteSameFileName(data));
                break;
            case 3:
                setContinueToPasteState(onPasteNoSpace(data));
                break;
            case 4:
                setContinueToPasteState(false);
                break;
            case 5:
                setContinueToPasteState(onPasteUserCancel(data));
                break;
            case 11:
                this.mFinishedJobCount = (int) (data.getLong("key-volumecoped") / 1048576);
                if (this.mTotalJobCount == 0) {
                    this.mTotalJobCount = 1;
                }
                updateProgress((this.mFinishedJobCount * 100) / this.mTotalJobCount, null);
                break;
            default:
                setContinueToPasteState(false);
                break;
        }
        return this.mContinueToPaste;
    }

    public boolean onPasteCompleteEvent(int event, Bundle data) {
        resetPasteFlag();
        sendMessageToUIThread(17, 0, 0, null, data);
        return true;
    }

    public boolean getCheckBoxState() {
        return this.mIsWholeProcessValid;
    }

    private void resetProgressDialogForPaste(Bundle data) {
        int flag;
        long totalSpace = data != null ? data.getLong("key-volumeneed") : 0;
        if (data != null) {
            flag = data.getInt("recycle_flag", 0);
        } else {
            flag = 0;
        }
        if (flag == 0) {
            this.mTotalJobCount = (int) (totalSpace / 1048576);
            if (this.mTotalJobCount < 1) {
                this.mTotalJobCount = 1;
            }
            if (this.mDialog != null) {
                this.mDialog.setMax(100);
                this.mDialog.setProgressNumberFormat(null);
                this.mDialog.setProgressPercentFormat(GalleryUtils.getPercentFormat(0));
                if (isActivityActive()) {
                    Message message = this.mHandler.obtainMessage();
                    message.what = 100;
                    message.obj = this.mDialog;
                    this.mHandler.sendMessage(message);
                }
            }
        }
    }

    private void resetPasteFlag() {
        setContinueToPasteState(true);
        this.mIsWholeProcessValid = true;
        this.mStrategyForPasteSameFile = 21;
        this.mTotalJobCount = 0;
        this.mFinishedJobCount = 0;
    }

    private boolean onPasteNoSpace(Bundle data) {
        setContinueToPasteState(false);
        this.mIsWholeProcessValid = false;
        sendMessageToUIThread(16, 3, 0, null, data);
        return false;
    }

    private boolean onPasteSameFileName(Bundle data) {
        switch (this.mStrategyForPasteSameFile) {
            case 22:
                return true;
            case 23:
                return false;
            default:
                if (sendMessageToUIThread(16, 2, 0, null, data)) {
                    waitUntilNotify();
                } else {
                    setContinueToPasteState(false);
                }
                return this.mContinueToPaste;
        }
    }

    private boolean onPasteUserCancel(Bundle data) {
        if (sendMessageToUIThread(16, 5, 0, null, data)) {
            waitUntilNotify();
        } else {
            setContinueToPasteState(true);
        }
        return this.mContinueToPaste;
    }

    private boolean sendMessageToUIThread(int what, int arg1, int arg2, Object obj, Bundle extraData) {
        if (!isActivityActive()) {
            return false;
        }
        Message msg = this.mHandler.obtainMessage(what, arg1, arg2, obj);
        if (extraData != null) {
            msg.setData(extraData);
        }
        this.mHandler.sendMessage(msg);
        return true;
    }

    private void handlePasteInitEventUiThread(Message message) {
        if (isActivityActive()) {
            switch (message.arg1) {
                case 3:
                    handlePasteEventNoSpaceUIThread(message.getData());
                    break;
            }
            return;
        }
        GalleryLog.e("MenuExecutor", "The activity is not shown when the paste init event message come to UI thread: " + message.arg1);
    }

    private void handlePasteEventUiThread(Message message) {
        if (isActivityActive()) {
            switch (message.arg1) {
                case 2:
                    handlePasteEventSameFileNameUIThread(message.getData());
                    break;
                case 3:
                    handlePasteEventNoSpaceUIThread(message.getData());
                    break;
                case 5:
                    handlePasteEventUserCancelUIThread(message.getData());
                    break;
            }
            return;
        }
        GalleryLog.e("MenuExecutor", "The activity is not shown when the paste event message come to UI thread: " + message.arg1);
        setContinueToPasteState(false);
        notifyAllWaitingLock();
    }

    private void handlePasteEventNoSpaceUIThread(Bundle data) {
        long spaceAvailable = data.getLong("key-volumeavailable");
        long spaceNeeded = data.getLong("key-volumeneed");
        String szSpaceAvailable = GalleryUtils.getFileSizeString(spaceAvailable);
        String szSpaceNeeded = GalleryUtils.getFileSizeString(spaceNeeded);
        new Builder(this.mActivity.getActivityContext()).setTitle(R.string.paste_nospace_title).setMessage(this.mActivity.getString(R.string.paste_nospace_message, szSpaceAvailable, szSpaceNeeded)).setPositiveButton(R.string.confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    @SuppressLint({"InflateParams"})
    private void handlePasteEventSameFileNameUIThread(Bundle data) {
        PasteSameFileDialogClickLisntener clickListener = new PasteSameFileDialogClickLisntener();
        String fileName = data.getString("key-filename");
        int toBePastedFileCount = data.getInt("key-tobepastedfilecount", 0);
        AlertDialog mSameFileDialog = new Builder(this.mActivity.getActivityContext()).setTitle(R.string.paste_samename_title).setPositiveButton(R.string.paste_samename_cancel, clickListener).setNegativeButton(R.string.paste_samename_jumpover, clickListener).setNeutralButton(R.string.paste_samename_override, clickListener).create();
        View pasteSameNameTips = LayoutInflater.from(this.mActivity.getActivityContext()).inflate(R.layout.alertdialog_custview_paste, null);
        ((TextView) pasteSameNameTips.findViewById(R.id.message)).setText(this.mActivity.getString(R.string.paste_samename_message, fileName));
        if (toBePastedFileCount > 1) {
            this.mCheckBox = (CheckBox) pasteSameNameTips.findViewById(R.id.checkbox);
            this.mCheckBox.setVisibility(0);
        }
        mSameFileDialog.setView(pasteSameNameTips);
        mSameFileDialog.setCanceledOnTouchOutside(false);
        mSameFileDialog.setOnCancelListener(clickListener);
        mSameFileDialog.show();
        GalleryUtils.setHorizontalFadeEdge(mSameFileDialog.getButton(-1));
    }

    private void handlePasteEventUserCancelUIThread(Bundle data) {
        String szCancelMessage;
        long percent = this.mTotalJobCount != 0 ? (((long) this.mFinishedJobCount) * 100) / ((long) this.mTotalJobCount) : 0;
        int pasteState = data.getInt("key-pastestate", 1);
        int iPositiveButtonTextId = pasteState == 1 ? R.string.paste_cancel_copy_cancelconfirm : R.string.paste_cancel_move_cancelconfirm;
        int iNegtiveButtonTextId = pasteState == 1 ? R.string.paste_cancel_copy_continue : R.string.paste_cancel_move_continue;
        if (pasteState == 1) {
            szCancelMessage = this.mActivity.getString(R.string.paste_cancel_copy_message, GalleryUtils.getPercentString((float) percent, 0));
        } else {
            szCancelMessage = this.mActivity.getString(R.string.paste_cancel_move_message, GalleryUtils.getPercentString((float) percent, 0));
        }
        PasteUserCancelDialogClickLisntener clickListener = new PasteUserCancelDialogClickLisntener();
        AlertDialog mUserCancelDialog = new Builder(this.mActivity.getActivityContext()).setMessage(szCancelMessage).setPositiveButton(iPositiveButtonTextId, clickListener).setNegativeButton(iNegtiveButtonTextId, clickListener).create();
        mUserCancelDialog.setCanceledOnTouchOutside(false);
        mUserCancelDialog.setOnCancelListener(clickListener);
        mUserCancelDialog.show();
        GalleryUtils.setTextColor(mUserCancelDialog.getButton(-1), this.mActivity.getAndroidContext().getResources());
    }

    private boolean isActivityActive() {
        return this.mActivity != null ? this.mActivity.isActivityActive() : false;
    }

    private void setContinueToPasteState(boolean state) {
        this.mContinueToPaste = state;
    }

    private void notifyAllWaitingLock() {
        synchronized (this.mLock) {
            this.isLocked = false;
            this.mLock.notifyAll();
        }
    }

    private void waitUntilNotify() {
        synchronized (this.mLock) {
            this.isLocked = true;
            while (this.isLocked) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                    GalleryLog.i("MenuExecutor", "Wait lock failed.");
                }
            }
        }
        return;
    }

    public static boolean isRecycleRecovering() {
        return sFileRecoverProcessCount > 0;
    }

    public boolean onIntentChooserDialogAppsItemClicked() {
        if (!this.mSelectionManager.inSelectionMode()) {
            return false;
        }
        this.mSelectionManager.leaveSelectionMode();
        return true;
    }
}
