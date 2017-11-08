package com.android.server.am;

import android.app.ActivityManager;
import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.IMultiTaskProcessObserver.Stub;
import android.app.mtm.MultiTaskManager;
import android.app.mtm.MultiTaskUtils;
import android.content.pm.IPackageManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwConnectivityService;
import com.android.server.HwInputMethodManagerService;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwMtmBroadcastResourceManager implements AbsHwMtmBroadcastResourceManager {
    static final int BOOSTCPUTIME = 1000;
    static final int BROADCAST_THRESHOLD = (ActivityManager.isLowRamDeviceStatic() ? 100 : HwActivityManagerService.SERVICE_ADJ);
    static final boolean DEBUG_BROADCAST = false;
    static final String TAG = "HwMtmBroadcastResourceManager";
    static final boolean mHwMtmBroadcastManageEnabled = SystemProperties.getBoolean("ro.config.multi_task_enable", false);
    private long lastboostTime = 0;
    private IMultiTaskProcessObserver mBroadcastProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            String[] packageNameList = null;
            if (uid > 10000 && foregroundActivities) {
                try {
                    if (HwMtmBroadcastResourceManager.this.mPM == null) {
                        HwMtmBroadcastResourceManager.this.mPM = IPackageManager.Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
                    }
                    packageNameList = HwMtmBroadcastResourceManager.this.mPM.getPackagesForUid(uid);
                } catch (RemoteException e) {
                    Slog.e(HwMtmBroadcastResourceManager.TAG, "RemoteException packageNameList");
                }
                if (packageNameList == null) {
                    Slog.e(HwMtmBroadcastResourceManager.TAG, "packageNameList is null");
                    return;
                }
                for (String appType : packageNameList) {
                    switch (MultiTaskUtils.getAppType(pid, uid, appType)) {
                        case 4:
                            if (Log.HWINFO) {
                                Slog.i(HwMtmBroadcastResourceManager.TAG, "appType = THIRDPARTY");
                            }
                            synchronized (HwMtmBroadcastResourceManager.this.mQueue.mService.mPidsSelfLocked) {
                            }
                            HwMtmBroadcastResourceManager.this.sendAndRemoveDelayBroadcastToApp((ProcessRecord) HwMtmBroadcastResourceManager.this.mQueue.mService.mPidsSelfLocked.get(pid));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
            if (Log.HWINFO) {
                Slog.i(HwMtmBroadcastResourceManager.TAG, "mtm broadcast onProcessDied pid = " + pid + ", uid = " + uid);
            }
            if (uid <= 10000 && Log.HWINFO) {
                Slog.i(HwMtmBroadcastResourceManager.TAG, "uid <= Process.FIRST_APPLICATION_UID, we do nothing now");
            }
            HwMtmBroadcastResourceManager.this.clearProcessInDelayBroadcast(pid, uid);
        }
    };
    final ArrayList<BroadcastRecord> mDelayBroadcasts = new ArrayList();
    private MultiTaskManager mMultiTaskManager = null;
    private IPackageManager mPM;
    final BroadcastQueue mQueue;

    public HwMtmBroadcastResourceManager(BroadcastQueue queue) {
        this.mQueue = queue;
        this.mPM = IPackageManager.Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
        this.mMultiTaskManager = MultiTaskManager.getInstance();
    }

    private final boolean updateDelayBroadcastLocked(BroadcastRecord record, BroadcastFilter filter) {
        boolean res = false;
        if (record == null || record.receivers == null || filter == null) {
            if (Log.HWINFO) {
                Slog.v(TAG, "this should never hapen");
            }
            return false;
        }
        String rAction = record.intent.getAction();
        if (rAction == null) {
            return false;
        }
        if (Log.HWINFO) {
            Slog.v(TAG, "updateDelayBroadcastLocked record = " + record + "record.receivers.size()= " + record.receivers.size() + "mDelayBroadcasts.size()= " + this.mDelayBroadcasts.size());
        }
        for (int i = 0; i <= this.mDelayBroadcasts.size() - 1; i++) {
            if (Log.HWINFO) {
                Slog.v(TAG, "i = " + i + ", mDelayBroadcasts.size() = " + this.mDelayBroadcasts.size() + ", mDelayBroadcasts.get(i) = " + this.mDelayBroadcasts.get(i) + ", mDelayBroadcasts.get(i).receivers = " + ((BroadcastRecord) this.mDelayBroadcasts.get(i)).receivers);
            }
            BroadcastRecord r = (BroadcastRecord) this.mDelayBroadcasts.get(i);
            if (rAction.equals(r.intent.getAction())) {
                boolean hasOne = false;
                for (BroadcastFilter o : r.receivers) {
                    if (!(o instanceof BroadcastFilter) && Log.HWINFO) {
                        Slog.v(TAG, "i = " + i + "not o instanceof BroadcastFilter");
                    }
                    if (o.packageName.equals(filter.packageName)) {
                        if (Log.HWINFO) {
                            Slog.v(TAG, "updateDelayBroadcastLocked equals packageName");
                        }
                        hasOne = true;
                        if (!hasOne) {
                            if (Log.HWINFO) {
                                Slog.v(TAG, "!hasOne");
                            }
                            r.receivers.add(filter);
                            Slog.v(TAG, "add Or Update add filter in receivers =" + r.receivers);
                        }
                        res = true;
                        if (Log.HWINFO) {
                            Slog.v(TAG, "updateDelayBroadcastLocked res=" + res);
                        }
                        return res;
                    }
                }
                if (hasOne) {
                    if (Log.HWINFO) {
                        Slog.v(TAG, "!hasOne");
                    }
                    r.receivers.add(filter);
                    Slog.v(TAG, "add Or Update add filter in receivers =" + r.receivers);
                }
                res = true;
                if (Log.HWINFO) {
                    Slog.v(TAG, "updateDelayBroadcastLocked res=" + res);
                }
                return res;
            }
        }
        if (Log.HWINFO) {
            Slog.v(TAG, "updateDelayBroadcastLocked res=" + res);
        }
        return res;
    }

    private final void addOrUpdateDelayBroadcastLocked(BroadcastRecord record, BroadcastFilter filter) {
        if (Log.HWINFO) {
            Slog.v(TAG, "addOrUpdateDelayBroadcast start mDelayBroadcasts.size()=" + this.mDelayBroadcasts.size());
        }
        if (!updateDelayBroadcastLocked(record, filter)) {
            if (Log.HWINFO) {
                Slog.v(TAG, "add DelayBroadcast");
            }
            List<BroadcastFilter> registeredReceivers = new ArrayList();
            registeredReceivers.add(filter);
            this.mDelayBroadcasts.add(new BroadcastRecord(record.queue, record.intent, record.callerApp, record.callerPackage, record.callingPid, record.callingUid, record.resolvedType, record.requiredPermissions, record.appOp, record.options, registeredReceivers, record.resultTo, record.resultCode, record.resultData, record.resultExtras, record.ordered, record.sticky, false, record.userId));
            Slog.v(TAG, "add Or Update add r in mDelayBroadcasts");
        } else if (Log.HWINFO) {
            Slog.v(TAG, "update DelayBroadcast");
        }
        if (Log.HWINFO) {
            Slog.v(TAG, "addOrUpdateDelayBroadcast end mDelayBroadcasts.size()=" + this.mDelayBroadcasts.size());
        }
    }

    private final void clearDelayBroadcastLocked(BroadcastRecord record) {
        if (Log.HWINFO) {
            Slog.v(TAG, "clearDelayBroadcast start mDelayBroadcasts.size()=" + this.mDelayBroadcasts.size());
        }
        String rAction = record.intent.getAction();
        if (rAction != null) {
            Iterator<BroadcastRecord> it = this.mDelayBroadcasts.iterator();
            while (it.hasNext()) {
                if (rAction.equals(((BroadcastRecord) it.next()).intent.getAction())) {
                    Slog.i(TAG, "mtm clear equals element");
                    it.remove();
                    break;
                }
            }
            if (Log.HWINFO) {
                Slog.v(TAG, "clearDelayBroadcast end mDelayBroadcasts.size()=" + this.mDelayBroadcasts.size());
            }
        }
    }

    public void boostCpuBySpecialBroadcast(BroadcastRecord record) {
        long now = SystemClock.uptimeMillis();
        String action = record.intent.getAction();
        if ((HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(action) || "android.intent.action.SCREEN_ON".equals(action)) && now > this.lastboostTime + 1000) {
            this.lastboostTime = now;
            Jlog.perfEvent(HwInputMethodManagerService.SECURE_IME_NO_HIDE_FLAG, AppHibernateCst.INVALID_PKG, new int[]{3000, 459052, 524437});
            if (Log.HWINFO) {
                Slog.d(TAG, "mtm boost cpu by " + action);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isBroadcastResourceManaged(BroadcastRecord record, BroadcastFilter filter) {
        if (this.mMultiTaskManager == null) {
            this.mMultiTaskManager = MultiTaskManager.getInstance();
            if (this.mMultiTaskManager != null) {
                this.mMultiTaskManager.registerObserver(getBroadcastProcessObserver());
                if (Log.HWINFO) {
                    Slog.d(TAG, "registered MultiTaskProcess");
                }
            } else {
                Slog.e(TAG, "can not get multi task manager!");
                return false;
            }
        }
        boolean res = false;
        if (record == null || record.intent == null || filter == null || filter.receiverList == null || filter.receiverList.app == null || filter.receiverList.app.foregroundActivities || filter.receiverList.app.uid <= 10000) {
            return false;
        }
        synchronized (this.mQueue.mService.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mQueue.mService.mPidsSelfLocked.get(filter.receiverList.app.pid);
            if (proc == null || proc.curAdj > 200) {
            } else {
                return false;
            }
        }
    }

    private boolean cacheBroadcastToDelay(BroadcastRecord record, BroadcastFilter filter) {
        boolean res = false;
        if (HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(record.intent.getAction())) {
            Slog.i(TAG, "cache record.intent.getAction()= " + record.intent.getAction());
            if (record.intent.getIntExtra("networkType", -1) == 1) {
                synchronized (this) {
                    res = wifiOnOffProcessLocked(record, filter);
                }
            }
        }
        return res;
    }

    private boolean wifiOnOffProcessLocked(BroadcastRecord record, BroadcastFilter filter) {
        NetworkInfo networkInfo = (NetworkInfo) record.intent.getParcelableExtra("networkInfo");
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == State.CONNECTED) {
            Slog.i(TAG, "NetworkInfo.State.CONNECTED app=" + filter.receiverList.app);
            addOrUpdateDelayBroadcastLocked(record, filter);
            return true;
        } else if (networkInfo.getState() != State.DISCONNECTED) {
            return false;
        } else {
            Slog.i(TAG, "NetworkInfo.State.DISCONNECTED app=" + filter.receiverList.app);
            clearDelayBroadcastLocked(record);
            return false;
        }
    }

    public IMultiTaskProcessObserver getBroadcastProcessObserver() {
        return this.mBroadcastProcessObserver;
    }

    private void sendAndRemoveDelayBroadcastToApp(ProcessRecord app) {
        List<BroadcastRecord> needToSendBroadcasts = new ArrayList();
        synchronized (this) {
            Iterator<BroadcastRecord> iter = this.mDelayBroadcasts.iterator();
            while (iter.hasNext()) {
                BroadcastRecord r = (BroadcastRecord) iter.next();
                Iterator it = r.receivers.iterator();
                while (it.hasNext()) {
                    Object target = it.next();
                    if (target instanceof BroadcastFilter) {
                        if (((BroadcastFilter) target).receiverList.app.equals(app)) {
                            if (Log.HWINFO) {
                                Slog.i(TAG, "sendAndRemove ((BroadcastFilter)target).receiverList.app.equals(app)");
                            }
                            List<BroadcastFilter> registeredReceivers = new ArrayList();
                            registeredReceivers.add((BroadcastFilter) target);
                            needToSendBroadcasts.add(new BroadcastRecord(r.queue, r.intent, r.callerApp, r.callerPackage, r.callingPid, r.callingUid, r.resolvedType, r.requiredPermissions, r.appOp, r.options, registeredReceivers, r.resultTo, r.resultCode, r.resultData, r.resultExtras, r.ordered, r.sticky, false, r.userId));
                            it.remove();
                            if (r.receivers.size() == 0) {
                                if (Log.HWINFO) {
                                    Slog.i(TAG, "mDelayBroadcasts.remove it;");
                                }
                                iter.remove();
                            }
                        }
                    } else if (Log.HWINFO) {
                        Slog.i(TAG, "sendAndRemove !(target instanceof BroadcastFilter)");
                    }
                }
            }
        }
        synchronized (this.mQueue.mService) {
            for (BroadcastRecord recordToSend : needToSendBroadcasts) {
                boolean hasSame = hasSameParallelBroadcast(recordToSend);
                if (Log.HWINFO) {
                    Slog.i(TAG, "hasSame = " + hasSame);
                }
                if (!hasSame) {
                    this.mQueue.enqueueParallelBroadcastLocked(recordToSend);
                    this.mQueue.scheduleBroadcastsLocked();
                }
            }
        }
        needToSendBroadcasts.clear();
    }

    private final boolean hasSameParallelBroadcast(BroadcastRecord r) {
        String rAction = r.intent.getAction();
        if (rAction == null) {
            return false;
        }
        for (int i = this.mQueue.mParallelBroadcasts.size() - 1; i >= 0; i--) {
            if (rAction.equals(((BroadcastRecord) this.mQueue.mParallelBroadcasts.get(i)).intent.getAction())) {
                if (r.intent.filterEquals(((BroadcastRecord) this.mQueue.mParallelBroadcasts.get(i)).intent) && Log.HWINFO) {
                    Slog.i(TAG, "r.intent.filterEquals i = " + i);
                }
                return true;
            }
        }
        return false;
    }

    private void clearProcessInDelayBroadcast(int pid, int uid) {
        if (Log.HWINFO) {
            Slog.v(TAG, "clear Delay Broadcasts.size() = " + this.mDelayBroadcasts.size() + ",pid = " + pid + ", uid = " + uid);
        }
        synchronized (this) {
            Iterator<BroadcastRecord> iter = this.mDelayBroadcasts.iterator();
            while (iter.hasNext()) {
                BroadcastRecord r = (BroadcastRecord) iter.next();
                Iterator<BroadcastFilter> it = r.receivers.iterator();
                while (it.hasNext()) {
                    BroadcastFilter filter = (BroadcastFilter) it.next();
                    if (filter.owningUid == uid && filter.receiverList.pid == pid) {
                        Slog.i(TAG, "clearProcess remove filter.packageName=" + filter.packageName);
                        it.remove();
                    }
                }
                if (r.receivers.size() == 0) {
                    Slog.i(TAG, "clearProcess cache r size 0.remove");
                    iter.remove();
                }
            }
        }
    }

    public void removeReceiverInDelayBroadcast(ReceiverList rl) {
        Slog.i(TAG, "removeReceiver mtm Broadcast");
        clearProcessInDelayBroadcast(rl.pid, rl.uid);
    }

    protected void dump(PrintWriter pw, String dumpPackage, boolean needSep) {
    }
}
