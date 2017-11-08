package com.android.server.mtm;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.mtm.IMultiTaskManagerService.Stub;
import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.MultiTaskPolicy;
import android.app.mtm.iaware.RSceneData;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.rms.HwSysResManager;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import com.android.server.mtm.iaware.RPolicyManager;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.policy.MultiTaskPolicyCreator;
import com.android.server.mtm.policy.MultiTaskPolicyCreatorImp;
import com.android.server.mtm.policy.MultiTaskPolicyList;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.mtm.test.TestCase;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MultiTaskManagerService extends Stub {
    static final boolean DEBUG = false;
    private static final int MSG_GROUP_CHANGE_NOTIFY = 1;
    private static final int MSG_PROCESS_CHANGE_NOTIFY = 2;
    private static final int MSG_PROCESS_DIE_NOTIFY = 3;
    private static final int MSG_RESOURCE_NOTIFY = 4;
    public static final int MY_PID = Process.myPid();
    static final String TAG = "MultiTaskManagerService";
    private static MultiTaskManagerService mSelf;
    HandlerThread handlerThread = new HandlerThread(TAG);
    private final Context mContext;
    private final Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    if (MultiTaskManagerService.this.mProcInfo != null) {
                        MultiTaskManagerService.this.mProcInfo.recordProcessInfo(msg.arg1, msg.arg2);
                    }
                    return true;
                case 3:
                    if (MultiTaskManagerService.this.mProcInfo != null) {
                        MultiTaskManagerService.this.mProcInfo.removeKilledProcess(msg.arg1);
                    }
                    return true;
                case 4:
                    MultiTaskManagerService.this.dispachMultiTaskPolicy(msg);
                    return true;
                default:
                    return false;
            }
        }
    };
    private ProcessCleaner mProcCleaner = null;
    private ProcessInfoCollector mProcInfo = null;
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (Log.HWINFO) {
                Slog.i(MultiTaskManagerService.TAG, "onForegroundActivitiesChanged pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessStatusChange(pid, uid);
            MultiTaskManagerService.this.dispatchFgActivitiesChanged(pid, uid, foregroundActivities);
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
            MultiTaskManagerService.this.notifyProcessStatusChange(pid, uid);
        }

        public void onProcessDied(int pid, int uid) {
            if (Log.HWINFO) {
                Slog.i(MultiTaskManagerService.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessDiedChange(pid, uid);
            MultiTaskManagerService.this.dispatchProcessDied(pid, uid);
        }
    };
    final RemoteCallbackList<IMultiTaskProcessObserver> mProcessObserverList = new RemoteCallbackList();
    private RPolicyManager mRPolicyManager = null;
    private HwSysResManager mResourceManger;
    MultiTaskPolicyList mStaticPolicyList = MultiTaskPolicyList.getInstance();
    IPackageManager pm = null;

    public MultiTaskManagerService(Context context) {
        this.mContext = context;
        setSelf(this);
        this.mStaticPolicyList.init(context);
        systemReady();
        this.handlerThread.start();
        this.mHandler = new Handler(this.handlerThread.getLooper(), this.mHandlerCallback);
        this.mProcInfo = ProcessInfoCollector.getInstance();
        this.mProcCleaner = ProcessCleaner.getInstance(this.mContext);
        this.mResourceManger = HwSysResManager.getInstance();
        AwareAppMngSort.getInstance(context);
        AwareUserHabit.getInstance(context);
        this.mRPolicyManager = new RPolicyManager(this.mContext, this.handlerThread);
    }

    public static MultiTaskManagerService self() {
        return mSelf;
    }

    private static void setSelf(MultiTaskManagerService curInstance) {
        mSelf = curInstance;
    }

    public Context context() {
        return this.mContext;
    }

    private void systemReady() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
            if (Log.HWINFO) {
                Slog.i(TAG, "MultiTaskManagerService systemReady success");
            }
            this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
        } catch (RemoteException e) {
            Slog.e(TAG, "MultiTaskManagerService systemReady failed");
        }
    }

    private void dispatchFgActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onForegroundActivitiesChanged(pid, uid, foregroundActivities);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    private void dispatchProcessStateChanged(int pid, int uid, int procState) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onProcessStateChanged(pid, uid, procState);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    private void dispatchProcessDied(int pid, int uid) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onProcessDied(pid, uid);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    public MultiTaskPolicy getMultiTaskPolicy(int resourceType, String resourceextend, int resourceStatus, Bundle args) {
        enforceCallingPermission();
        MultiTaskPolicyCreator mpolicycreator = MultiTaskPolicyCreatorImp.getPolicyCreator(resourceType);
        if (mpolicycreator != null) {
            return mpolicycreator.getResourcePolicy(resourceType, resourceextend, resourceStatus, args);
        }
        return null;
    }

    private void dispachMultiTaskPolicy(Message msg) {
        int resourcetype = msg.arg1;
        int resourcestatus = msg.arg2;
        SomeArgs args = msg.obj;
        String resourceextend = args.arg1;
        Bundle otherargs = args.arg2;
        args.recycle();
        MultiTaskPolicyCreator mpolicycreator = MultiTaskPolicyCreatorImp.getPolicyCreator(resourcetype);
        if (mpolicycreator != null) {
            MultiTaskPolicy mpolicy = mpolicycreator.getResourcePolicy(resourcetype, resourceextend, resourcestatus, otherargs);
            if (mpolicy != null && this.mResourceManger != null) {
                this.mResourceManger.dispatch(resourcetype, mpolicy);
            }
        }
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        enforceCallingPermission();
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        msg.arg1 = resourcetype;
        msg.arg2 = resourcestatus;
        SomeArgs othereargs = SomeArgs.obtain();
        othereargs.arg1 = resourceextend;
        othereargs.arg2 = args;
        msg.obj = othereargs;
        this.mHandler.sendMessage(msg);
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this) {
            this.mProcessObserverList.register(observer);
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this) {
            this.mProcessObserverList.unregister(observer);
        }
    }

    private void enforceCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (pid != Process.myPid() && uid != 0 && uid != 1000) {
            String msg = "Permission Denial: can not access MultiTaskManagerService! pid = " + pid + ",uid = " + uid;
            Slog.e(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        if (pid == MY_PID || uid == 0 || uid == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        try {
            if (this.pm == null) {
                this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY));
            }
            if (this.pm != null) {
                return this.pm.checkUidPermission(permission, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManager is dead?!?", e);
        }
        return -1;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump Multi task service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
        } else {
            TestCase.test(this.mContext, pw, args);
        }
    }

    public boolean killProcess(int pid, boolean restartservice) {
        enforceCallingPermission();
        return this.mProcCleaner.killProcess(pid, restartservice);
    }

    public boolean forcestopApps(int pid) {
        enforceCallingPermission();
        return this.mProcCleaner.forcestopApps(pid);
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(1, pid, uid);
    }

    public void notifyProcessStatusChange(int pid, int uid) {
        sendProcessChangeMessage(2, pid, uid);
    }

    public void notifyProcessDiedChange(int pid, int uid) {
        sendProcessChangeMessage(3, pid, uid);
    }

    private void sendProcessChangeMessage(int event, int pid, int uid) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = event;
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        enforceCallingPermission();
        return this.mRPolicyManager.reportScene(featureId, scene);
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        enforceCallingPermission();
        return this.mRPolicyManager.acquirePolicyData(featureId, scene);
    }
}
