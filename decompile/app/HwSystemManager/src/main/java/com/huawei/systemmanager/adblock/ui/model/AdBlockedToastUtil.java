package com.huawei.systemmanager.adblock.ui.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.process.HsmProcessUtil;
import com.huawei.systemmanager.service.MainService;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;

public class AdBlockedToastUtil {
    private static final String TAG = "AdBlockedToastUtil";
    private static HashSet<Integer> sAdblockedNotified = new HashSet();

    private static class NotifyRunnable implements Runnable {
        private final Context mContext;
        private final int mPid;
        private final int mUid;

        NotifyRunnable(Context context, int pid, int uid) {
            this.mContext = context;
            this.mPid = pid;
            this.mUid = uid;
        }

        public void run() {
            String pkg = HsmProcessUtil.getAppInfoByUidAndPid(this.mContext, this.mUid, this.mPid);
            AdBlock adBlock = AdBlock.restoreAdBlockWithPkg(this.mContext, pkg);
            if (adBlock == null) {
                HwLog.i(AdBlockedToastUtil.TAG, "NotifyRunnable adBlock is null for pkg=" + pkg);
                return;
            }
            HwLog.i(AdBlockedToastUtil.TAG, "NotifyRunnable pkg=" + pkg + ", uid=" + this.mUid + ", pid=" + this.mPid + ", adBlock =" + adBlock.isEnable() + ", hasAd=" + adBlock.hasAd());
            if (adBlock.isEnable() && adBlock.hasAd()) {
                String label = AdBlockedToastUtil.getLableFromPm(this.mContext.getPackageManager(), pkg);
                String text = this.mContext.getString(R.string.ad_blocked_toast, new Object[]{label});
                Intent intent = new Intent(this.mContext.getApplicationContext(), MainService.class);
                intent.setAction(AdConst.ACTION_AD_BLOCKED_TOAST);
                intent.putExtra(AdConst.BUNDLE_BLOCKED_MESSAGE, text);
                this.mContext.startServiceAsUser(intent, new UserHandle(UserHandle.getUserId(this.mUid)));
            }
        }
    }

    public static synchronized void notifyAdblockedIfNeeded(Context context, int pid, int uid) {
        synchronized (AdBlockedToastUtil.class) {
            if (sAdblockedNotified.contains(Integer.valueOf(pid))) {
                return;
            }
            sAdblockedNotified.add(Integer.valueOf(pid));
            HsmExecutor.THREAD_POOL_EXECUTOR.execute(new NotifyRunnable(context, pid, uid));
        }
    }

    private static String getLableFromPm(PackageManager pm, String pkgName) {
        if (pm == null || pkgName == null) {
            HwLog.e(TAG, "getLableFromPm,but pm or pkgname is null.");
            return "";
        }
        try {
            return pm.getApplicationInfo(pkgName, 8192).loadLabel(pm).toString().trim();
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "can't get application info:" + pkgName);
            return pkgName;
        }
    }
}
