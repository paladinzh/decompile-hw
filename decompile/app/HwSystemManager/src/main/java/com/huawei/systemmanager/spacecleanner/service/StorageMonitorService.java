package com.huawei.systemmanager.spacecleanner.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.LowerMemTipActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class StorageMonitorService extends Service {
    private static final long DEFAULT_CHECK_INTERVAL = 60000;
    private static final long DEFAULT_FULL_STORAGE_BYTES = 104857600;
    private static final int MSG_CHECK_STORAGE = 1;
    private static final String TAG = "StorageMonitorService";
    private AlertDialog mAlertDialog;
    private HelpHandler mHelpHandler;

    private class HelpHandler extends Handler {
        public HelpHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        if (StorageMonitorService.this.checkFullStorage() && StorageMonitorService.this.canCreateDialog()) {
                            StorageMonitorService.this.createDialog();
                        }
                        StorageMonitorService.this.sendCheckMsg();
                        break;
                }
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHelpHandler = new HelpHandler(handlerThread.getLooper());
        sendCheckMsg();
    }

    public void onDestroy() {
        super.onDestroy();
        quitLooper();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendCheckMsg() {
        if (this.mHelpHandler != null) {
            this.mHelpHandler.sendEmptyMessageDelayed(1, 60000);
        }
    }

    private long reFreshFreeMem() {
        long availableSize = StorageHelper.getStorage().getAvalibaleSize(0);
        HwLog.d(TAG, "reFreshFreeMem, availableSize is: " + availableSize);
        return availableSize;
    }

    private boolean checkFullStorage() {
        long freeMem = reFreshFreeMem();
        if (freeMem < 0 || freeMem > 104857600) {
            return false;
        }
        HwLog.i(TAG, "checkFullStorage, value is true: " + freeMem);
        return true;
    }

    private boolean canCreateDialog() {
        if (UserHandle.myUserId() != ActivityManager.getCurrentUser()) {
            HwLog.i(TAG, "not current user");
            return false;
        } else if ((this.mAlertDialog == null || !this.mAlertDialog.isShowing()) && isApplicationBroughtToBackground()) {
            return true;
        } else {
            return false;
        }
    }

    private void quitLooper() {
        this.mHelpHandler.removeCallbacksAndMessages(null);
        this.mHelpHandler.getLooper().quit();
    }

    private void createDialog() {
        final Context context = GlobalContext.getContext();
        Builder dialogBuilder = new Builder(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        dialogBuilder.setPositiveButton(R.string.spaceclean_full_storage_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (UserHandle.myUserId() == 0) {
                    Intent lowMemIntent = new Intent();
                    lowMemIntent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
                    lowMemIntent.setClass(context, LowerMemTipActivity.class);
                    StorageMonitorService.this.startActivity(lowMemIntent);
                }
            }
        });
        dialogBuilder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                StorageMonitorService.this.mAlertDialog = null;
            }
        });
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(R.string.spaceclean_full_storage_warning);
        dialogBuilder.setMessage(R.string.spaceclean_full_storage_warning_msg);
        this.mAlertDialog = dialogBuilder.create();
        this.mAlertDialog.getWindow().setType(2003);
        this.mAlertDialog.show();
    }

    public static boolean isApplicationBroughtToBackground() {
        Context context = GlobalContext.getContext();
        List<RunningTaskInfo> tasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        return (tasks == null || tasks.isEmpty() || ((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName().equals(context.getPackageName())) ? false : true;
    }
}
