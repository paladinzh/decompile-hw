package com.huawei.powergenie.integration.adapter.pged;

import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.pged.IPgedBinder.Stub;
import java.util.ArrayList;
import java.util.List;

public final class PgedBinderAdapter implements DeathRecipient, FreezeInterface, KStateInterface {
    private static PgedBinderAdapter mInstance = null;
    private static PgedBinderListener mListener = null;
    private int mCrashCount = 0;
    private int mCurMask = 0;
    private IPgedBinder mFrzService = null;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    PgedBinderAdapter.this.reInitHwpgedService();
                    return;
                default:
                    return;
            }
        }
    };

    public boolean closeKState(int r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.powergenie.integration.adapter.pged.PgedBinderAdapter.closeKState(int):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.powergenie.integration.adapter.pged.PgedBinderAdapter.closeKState(int):boolean");
    }

    protected static synchronized PgedBinderAdapter getInstance(KStateMonitor monitor) {
        PgedBinderAdapter pgedBinderAdapter;
        synchronized (PgedBinderAdapter.class) {
            if (mInstance == null) {
                mInstance = new PgedBinderAdapter();
            }
            if (monitor != null) {
                mListener = new PgedBinderListener(monitor);
            }
            pgedBinderAdapter = mInstance;
        }
        return pgedBinderAdapter;
    }

    private PgedBinderAdapter() {
        creatPgedBinder();
    }

    private void creatPgedBinder() {
        this.mFrzService = Stub.asInterface(ServiceManager.getService("Binder.Pged"));
        if (this.mFrzService != null) {
            try {
                this.mFrzService.asBinder().linkToDeath(this, 0);
                return;
            } catch (Exception e) {
                Log.w("PgedBinderAdapter", "creatPgedBinder linkToDeath failed !");
                return;
            }
        }
        Log.e("PgedBinderAdapter", "creatPgedBinder mFrzService is null !");
    }

    public boolean registerHwPgedListener() {
        boolean z = false;
        if (this.mFrzService == null) {
            Log.w("PgedBinderAdapter", "registerHwPgedListener failed, mFrzService not init  ");
            return false;
        }
        int ret = 0;
        try {
            ret = this.mFrzService.registerListener(mListener, 536870919);
        } catch (Exception e) {
            Log.w("PgedBinderAdapter", "hwpged may be chashed.");
            creatPgedBinder();
            if (this.mFrzService == null) {
                Log.e("PgedBinderAdapter", "ERROR: hwpged is not found.");
                return false;
            }
            try {
                ret = this.mFrzService.registerListener(mListener, 536870919);
            } catch (Exception err) {
                Log.e("PgedBinderAdapter", "register listener for hwpged fail.", err);
            }
        }
        Log.d("PgedBinderAdapter", "registerHwPgedListener result: " + ret);
        if (ret == 0) {
            z = true;
        }
        return z;
    }

    public boolean unregisterHwPgedListener() {
        boolean z = false;
        if (this.mFrzService == null) {
            Log.w("PgedBinderAdapter", "unregister failure!, mFrzService not init");
            return false;
        }
        try {
            int ret = this.mFrzService.unregisterListener(mListener);
            Log.d("PgedBinderAdapter", "unregisterHwPgedListener result: " + ret);
            if (ret == 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.e("PgedBinderAdapter", "hwpged may be crashed, unregisterListener failed!");
            return false;
        }
    }

    public boolean openKState(int mask) {
        return setKStateMask(this.mCurMask | mask);
    }

    private boolean setKStateMask(int mask) {
        boolean z = true;
        if (this.mFrzService == null || mListener == null) {
            Log.w("PgedBinderAdapter", "mFrzService and  mListener is not prepared ");
            return false;
        } else if (this.mCurMask == mask) {
            Log.w("PgedBinderAdapter", "the mask: " + mask + "was set.");
            return true;
        } else {
            this.mCurMask = mask;
            try {
                int ret = this.mFrzService.setKstateMask(mask);
                Log.d("PgedBinderAdapter", "setKstateMask:" + mask + " result: " + ret);
                if (ret != 0) {
                    z = false;
                }
                return z;
            } catch (RemoteException e) {
                Log.w("PgedBinderAdapter", "RemoteException!, setKStateMask failed");
                return false;
            } catch (Exception err) {
                Log.e("PgedBinderAdapter", "set kstate mask fail.", err);
                return false;
            }
        }
    }

    public boolean checkPgedRunning() {
        return this.mFrzService != null;
    }

    private boolean reInitHwpgedService() {
        Log.w("PgedBinderAdapter", "hwpged may be crashed, reInitHwpgedService!");
        creatPgedBinder();
        registerHwPgedListener();
        return checkPgedRunning();
    }

    private boolean doNotifyBastetProxy(List<Integer> pidsList, int action) {
        if (pidsList == null) {
            return false;
        }
        int n = pidsList.size();
        if (n == 0) {
            return false;
        }
        int[] pids = new int[(n + 1)];
        for (int i = 0; i < n; i++) {
            try {
                pids[i] = ((Integer) pidsList.get(i)).intValue();
            } catch (NullPointerException e) {
                Log.w("PgedBinderAdapter", "warning, null pointer");
            }
        }
        pids[n] = -1;
        try {
            this.mFrzService.notifyBastet(action, pids);
            Log.i("PgedBinderAdapter", "doNotifyBastetProxy [action:" + action + "] -> [" + pidsList + "]");
            return true;
        } catch (Exception e2) {
            Log.e("PgedBinderAdapter", "doNotifyBastetProxy exception:", e2);
            return false;
        }
    }

    public boolean notifyBastetProxy(List<Integer> pidsList) {
        return doNotifyBastetProxy(pidsList, 4);
    }

    public boolean notifyBastetUnProxy(List<Integer> pidsList) {
        return doNotifyBastetProxy(pidsList, 5);
    }

    public boolean notifyBastetUnProxyAll() {
        try {
            this.mFrzService.notifyBastet(6, new int[]{-1});
        } catch (Exception e) {
            Log.e("PgedBinderAdapter", "notifyBastetUnProxyAll exception:", e);
        }
        return true;
    }

    private boolean doFreezeProcess(List<Integer> pidsList, int action) {
        if (pidsList == null) {
            return false;
        }
        int n = pidsList.size();
        if (n == 0) {
            return false;
        }
        int[] pids = new int[(n + 1)];
        for (int i = 0; i < n; i++) {
            try {
                pids[i] = ((Integer) pidsList.get(i)).intValue();
            } catch (NullPointerException e) {
                Log.w("PgedBinderAdapter", "warning, null pointer");
            }
        }
        pids[n] = -1;
        try {
            if (this.mFrzService.doFreezer(action, pids) < 0) {
                if (1 == action) {
                    Log.w("PgedBinderAdapter", "frz failed pid: " + pidsList);
                } else {
                    Log.w("PgedBinderAdapter", "unfrz failed pid: " + pidsList);
                }
                return false;
            }
            if (1 == action) {
                Log.d("PgedBinderAdapter", "frz ok, pid: " + pidsList);
            } else {
                Log.d("PgedBinderAdapter", "unfrz ok, pid: " + pidsList);
            }
            return true;
        } catch (Exception e2) {
            if (reInitHwpgedService()) {
                unfreezeAllProcess();
            }
            return false;
        }
    }

    public boolean freezeProcess(List<Integer> pidsList) {
        return doFreezeProcess(pidsList, 1);
    }

    public boolean unfreezeProcess(List<Integer> pidsList) {
        return doFreezeProcess(pidsList, 2);
    }

    public boolean unfreezeAllProcess() {
        int[] pids = new int[]{-1};
        int ret = -1;
        try {
            ret = this.mFrzService.doFreezer(3, pids);
        } catch (Exception e) {
            if (reInitHwpgedService()) {
                try {
                    ret = this.mFrzService.doFreezer(3, pids);
                } catch (Exception e2) {
                    Log.w("PgedBinderAdapter", "reInitHwpgedService and doFreezer failed");
                }
            } else {
                Log.w("PgedBinderAdapter", "reInitHwpgedService failed");
            }
        }
        if (ret < 0) {
            Log.w("PgedBinderAdapter", "unfreeze all process failed!");
            return false;
        }
        Log.i("PgedBinderAdapter", "unfreeze all process ok!");
        return true;
    }

    public boolean netPacketListener(int len, ArrayList<Integer> uidsList) {
        if (uidsList == null) {
            return false;
        }
        int[] uids = new int[len];
        for (int i = 0; i < len; i++) {
            uids[i] = ((Integer) uidsList.get(i)).intValue();
        }
        int ret = -1;
        if (len == 0) {
            try {
                Log.i("PgedBinderAdapter", "Stop listener network packet!");
            } catch (Exception e) {
                Log.w("PgedBinderAdapter", "netPacketListener failed", e);
            }
        } else {
            Log.i("PgedBinderAdapter", "Listener network packet by uids: " + uidsList);
        }
        ret = this.mFrzService.netPacketListener(len, uids);
        if (ret == 0 || len == 0) {
            return true;
        }
        Log.e("PgedBinderAdapter", "netPacketListener fail");
        return false;
    }

    public int getProcUTime(int pid) {
        int ret = -1;
        try {
            ret = this.mFrzService.getProcUTime(pid);
        } catch (Exception e) {
            Log.w("PgedBinderAdapter", "getProcUTime failed", e);
        }
        return ret;
    }

    public void binderDied() {
        Log.e("PgedBinderAdapter", "HwPged binder was died and connecting ...");
        Message msg = this.mHandler.obtainMessage(100);
        this.mHandler.removeMessages(100);
        this.mHandler.sendMessageDelayed(msg, 3000);
        this.mCrashCount++;
        DbgUtils.sendNotification("hwpged crash count: " + this.mCrashCount, "");
    }
}
