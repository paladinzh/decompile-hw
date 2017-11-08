package com.huawei.systemmanager.optimize;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class ProtectAppService implements HsmService {
    private static final String ACTION_UNPROTECT_LIST = "huawei.intent.action.HW_STOPPED_PACKAGE_ACTION";
    private static final String KEY_UNPROTECT_LIST = "hw_stopped";
    public static final String NAME = "ProtectAppService";
    private static final String PERMISSION = "android.Manifest.permission.FORCE_STOP_PACKAGES";
    private static final String SEND_UNPROTECT_THREAD_NAME = "send_unprotectlist_thread";
    private final Context mContext;
    private final ContentObserver mObserver = new ProtectAppContentOberver(new Handler());

    private class ProtectAppContentOberver extends ContentObserver {
        ProtectAppContentOberver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            ProtectAppService.sendUnprotectBroadcast(ProtectAppService.this.mContext);
        }
    }

    private static class SendBroadcastRunnable implements Runnable {
        private final Context mCtx;

        public SendBroadcastRunnable(Context ctx) {
            this.mCtx = ctx;
        }

        public void run() {
            ArrayList<String> list = getPackageList();
            Intent intent = new Intent(ProtectAppService.ACTION_UNPROTECT_LIST);
            intent.putStringArrayListExtra(ProtectAppService.KEY_UNPROTECT_LIST, list);
            HwLog.i(ProtectAppService.NAME, "send unprotect list broadcast to FWK, size: " + list.size() + "\n" + ", unprotect list:" + list.toString());
            this.mCtx.sendBroadcastAsUser(intent, UserHandle.OWNER, ProtectAppService.PERMISSION);
        }

        private ArrayList<String> getPackageList() {
            HwLog.d(ProtectAppService.NAME, "getPackageList get un-protected apps");
            return SmcsDbHelper.getProtectList(this.mCtx, false);
        }
    }

    public ProtectAppService(Context ctx) {
        this.mContext = ctx;
    }

    public void init() {
        this.mContext.getContentResolver().registerContentObserver(SmcsDbHelper.SMCS_PROTECT_TABLE_URI, false, this.mObserver);
        sendUnprotectBroadcast(this.mContext);
    }

    public void onDestroy() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    public static void sendUnprotectBroadcast(Context ctx) {
        if (ctx == null) {
            HwLog.e(NAME, "sendBroadcast ctx is null!");
        } else {
            HsmExecutor.executeTask(new SendBroadcastRunnable(ctx), SEND_UNPROTECT_THREAD_NAME);
        }
    }
}
