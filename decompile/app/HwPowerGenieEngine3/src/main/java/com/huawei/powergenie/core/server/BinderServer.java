package com.huawei.powergenie.core.server;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Debug;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.TimeUtils;
import com.huawei.pgmng.plug.IPGSdk.Stub;
import com.huawei.pgmng.plug.IStateRecognitionSink;
import com.huawei.powergenie.api.ActionsExportMap;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.api.IThermal;
import com.huawei.powergenie.api.IThermalState;
import com.huawei.powergenie.api.Utils;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class BinderServer extends Stub {
    private static final boolean DEBUG = DbgUtils.DBG_USB;
    private final IAppManager mIAppManager;
    private final IAppType mIAppType;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IScenario mIScenario;
    private final IThermal mIThermal;
    private IThermalState mIThermalState = null;
    private int mLastActionId = -1;
    private String mLastPkg = null;
    private final HashMap<Integer, ArrayList<Integer>> mRegPidStates = new HashMap();
    private final HashMap<IStateRecognitionSink, Integer> mSinkMapPid = new HashMap();
    private final RemoteCallbackList<IStateRecognitionSink> mSinks = new RemoteCallbackList();
    private final long mStartPGELAPSED;
    private final long mStartPGRTC;
    private final HashSet<Integer> mSupportedStates = new HashSet<Integer>() {
        {
            add(Integer.valueOf(1));
            add(Integer.valueOf(2));
            add(Integer.valueOf(3));
            add(Integer.valueOf(4));
            add(Integer.valueOf(5));
            add(Integer.valueOf(6));
            add(Integer.valueOf(8));
            add(Integer.valueOf(9));
        }
    };
    private final HashSet<String> mValidArgs2 = new HashSet<String>() {
        {
            add("msg");
            add("event");
            add("adapter");
        }
    };
    private final HashMap<String, String> mValidArgsMap = new HashMap<String, String>() {
        {
            put("-a", null);
            put("help", null);
            put("cpu", "module");
            put("battery", "module");
            put("device", "device");
            put("modules", "module");
            put("thermal", "thermal");
            put("integration", "dbgtest");
            put("stats", "powerstats");
        }
    };

    protected BinderServer(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIScenario = (IScenario) coreContext.getService("scenario");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
        this.mIThermal = (IThermal) coreContext.getService("thermal");
        if (this.mIThermal == null) {
            this.mIThermalState = (IThermalState) coreContext.getService("thermalstate");
        }
        this.mStartPGRTC = System.currentTimeMillis();
        this.mStartPGELAPSED = SystemClock.elapsedRealtime();
        Log.i("PGServer", getProcInfo());
    }

    private boolean checkCallingPermission(String callingPkg) {
        int callingUid = UserHandle.getAppId(Binder.getCallingUid());
        if (callingUid < 10000) {
            return true;
        }
        if (callingPkg == null) {
            ArrayList<String> callingPkgList = this.mIAppManager.getPkgNameByUid(this.mICoreContext.getContext(), callingUid);
            if (callingPkgList != null && callingPkgList.size() > 0) {
                callingPkg = (String) callingPkgList.get(0);
                Log.i("PGServer", "checkCallingPermission : " + callingPkg);
            }
        }
        if (callingPkg != null && ("com.huawei.appmarket".equals(callingPkg) || "com.huawei.hidisk".equals(callingPkg) || "com.huawei.android.ds".equals(callingPkg))) {
            return true;
        }
        Log.e("PGServer", "client has no permission. client uid:" + callingUid);
        return false;
    }

    public boolean checkStateByPid(String callingPkg, int pid, int state) {
        if (!checkCallingPermission(callingPkg)) {
            return false;
        }
        Log.i("PGServer", callingPkg + " query pid: " + pid + " state: " + state);
        switch (state) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                return this.mIDeviceState.isAudioIn(pid);
            case NativeAdapter.PLATFORM_HI /*2*/:
                return this.mIDeviceState.isAudioOut(pid);
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return this.mIDeviceState.hasActiveGps(this.mIAppManager.getUidByPid(pid));
            case 4:
                return this.mIDeviceState.hasActiveSensor(this.mIAppManager.getUidByPid(pid));
            case 5:
                return this.mIDeviceState.isDlUploading(this.mIAppManager.getUidByPid(pid));
            default:
                return false;
        }
    }

    public boolean checkStateByPkg(String callingPkg, String pkg, int state) {
        if (!checkCallingPermission(callingPkg)) {
            return false;
        }
        Log.i("PGServer", callingPkg + " query app: " + pkg + " state: " + state);
        if (pkg == null) {
            Log.e("PGServer", "checkStateByPkg. error: pkg is null for state: " + state);
            return false;
        }
        ArrayList<Integer> pidList;
        switch (state) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                pidList = this.mIAppManager.getPidsByPkg(pkg);
                if (pidList != null) {
                    for (Integer pid : pidList) {
                        if (this.mIDeviceState.isAudioIn(pid.intValue())) {
                            return true;
                        }
                    }
                }
                return false;
            case NativeAdapter.PLATFORM_HI /*2*/:
                pidList = this.mIAppManager.getPidsByPkg(pkg);
                if (pidList != null) {
                    for (Integer pid2 : pidList) {
                        if (this.mIDeviceState.isAudioOut(pid2.intValue())) {
                            return true;
                        }
                    }
                }
                return false;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                return this.mIDeviceState.hasActiveGps(this.mIAppManager.getUidByPkg(pkg));
            case 4:
                return this.mIDeviceState.hasActiveSensor(this.mIAppManager.getUidByPkg(pkg));
            case 5:
                return this.mIDeviceState.isDlUploading(this.mIAppManager.getUidByPkg(pkg));
            case 8:
                return this.mIDeviceState.hasBluetoothConnected(pkg, this.mIAppManager.getUidByPkg(pkg), 0);
            default:
                return true;
        }
    }

    public Map<String, String> getSensorInfoByUid(String callingPkg, int uid) {
        if (!checkCallingPermission(callingPkg)) {
            return null;
        }
        if (uid <= 0) {
            Log.e("PGServer", "getSensorInfoByUid. invalid uid:" + uid);
            return null;
        }
        Map<String, String> sensorMap = this.mIDeviceState.getActiveSensorsByUid(uid);
        Log.i("PGServer", "uid: " + uid + " query sensor handles: " + sensorMap);
        return sensorMap;
    }

    public int getPkgType(String callingPkg, String pkg) {
        if (!checkCallingPermission(callingPkg)) {
            return -1;
        }
        Log.i("PGServer", "checkPkgType. calling pkg: " + callingPkg);
        return this.mIAppType.getAppType(pkg);
    }

    public List<String> getHibernateApps(String callingPkg) {
        if (!checkCallingPermission(callingPkg)) {
            return null;
        }
        Log.i("PGServer", callingPkg + " pkg calls getHibernateApps. result: " + this.mIAppManager.getHibernateApps());
        return this.mIAppManager.getHibernateApps();
    }

    public boolean hibernateApps(String callingPkg, List<String> pkgNames, String reason) {
        if (!checkCallingPermission(callingPkg)) {
            return false;
        }
        Log.i("PGServer", callingPkg + " pkg calls hibernateApps");
        return this.mIAppManager.hibernateApps(pkgNames, reason);
    }

    public String getTopFrontApp(String callingPkg) {
        if (!checkCallingPermission(callingPkg)) {
            return null;
        }
        Log.i("PGServer", "getTopFrontApp. calling pkg: " + callingPkg);
        return this.mIScenario.getFrontPkg();
    }

    public int getThermalInfo(String callingPkg, int type) {
        if (!checkCallingPermission(callingPkg)) {
            return -1;
        }
        Log.i("PGServer", "getThermalInfo. calling pkg: " + callingPkg);
        if (this.mIThermal != null) {
            if (type == -1) {
                return this.mIThermal.getCurThermalStep();
            }
            return this.mIThermal.getThermalTemp(type);
        } else if (this.mIThermalState != null) {
            if (type == -1) {
                return this.mIThermalState.getCurThermalStep();
            }
            return this.mIThermalState.getThermalTemp(type);
        } else if (type == -1) {
            return 0;
        } else {
            return -100000;
        }
    }

    protected boolean needAction() {
        boolean z;
        synchronized (this.mRegPidStates) {
            z = !this.mRegPidStates.isEmpty();
        }
        return z;
    }

    protected void handleActionInner(int exportActionId, String pkgName, String extend1, String extend2) {
        if (ActionsExportMap.isScenario(exportActionId)) {
            if (!ActionsExportMap.isChildScenario(exportActionId) && ActionsExportMap.isScenario(this.mLastActionId)) {
                onActionStateChanged(this.mLastActionId, 2, 0, this.mLastPkg, 0);
            }
            onActionStateChanged(exportActionId, 1, 0, pkgName, 0);
            if (!ActionsExportMap.isChildScenario(exportActionId)) {
                this.mLastActionId = exportActionId;
                this.mLastPkg = pkgName;
            }
            return;
        }
        if (DEBUG) {
            Log.d("PGServer", "discard export action: " + exportActionId);
        }
    }

    protected void handleStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
        onActionStateChanged(stateType, eventType, pid, pkg, uid);
    }

    public int[] getSupportedStates() {
        int[] states = new int[this.mSupportedStates.size()];
        int i = 0;
        for (Integer st : this.mSupportedStates) {
            int i2 = i + 1;
            states[i] = st.intValue();
            i = i2;
        }
        return states;
    }

    public boolean isStateSupported(int stateType) {
        return this.mSupportedStates.contains(Integer.valueOf(stateType));
    }

    public boolean registerSink(IStateRecognitionSink sink) {
        if (sink == null || !checkCallingPermission(null)) {
            return false;
        }
        if (!this.mSinkMapPid.containsKey(sink)) {
            this.mSinkMapPid.put(sink, Integer.valueOf(Binder.getCallingPid()));
            Log.i("PGServer", "register sink from pid: " + Binder.getCallingPid());
        }
        return this.mSinks.register(sink);
    }

    public boolean unregisterSink(IStateRecognitionSink sink) {
        if (sink == null || !checkCallingPermission(null)) {
            return false;
        }
        if (this.mSinkMapPid.containsKey(sink)) {
            this.mSinkMapPid.remove(sink);
            Log.i("PGServer", "unregister sink from pid: " + Binder.getCallingPid());
        }
        return this.mSinks.unregister(sink);
    }

    public boolean enableStateEvent(int stateType) {
        if (!checkCallingPermission(null)) {
            return false;
        }
        if (isStateSupported(stateType) || ActionsExportMap.isValidExportActionID(stateType)) {
            int pid = Binder.getCallingPid();
            synchronized (this.mRegPidStates) {
                ArrayList<Integer> states = (ArrayList) this.mRegPidStates.get(Integer.valueOf(pid));
                if (states == null) {
                    states = new ArrayList();
                    states.add(Integer.valueOf(stateType));
                    this.mRegPidStates.put(Integer.valueOf(pid), states);
                } else if (!states.contains(Integer.valueOf(stateType))) {
                    states.add(Integer.valueOf(stateType));
                }
            }
            Log.i("PGServer", "enable state type: " + stateType + " from pid: " + pid);
            return true;
        }
        Log.e("PGServer", "not enable unsupported state type: " + stateType + " calling pid: " + Binder.getCallingPid());
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean disableStateEvent(int stateType) {
        if (!checkCallingPermission(null)) {
            return false;
        }
        int pid = Binder.getCallingPid();
        synchronized (this.mRegPidStates) {
            ArrayList<Integer> states = (ArrayList) this.mRegPidStates.get(Integer.valueOf(pid));
            if (states == null) {
                Log.i("PGServer", "disable not exist state type: " + stateType);
                return true;
            } else if (states.contains(Integer.valueOf(stateType))) {
                states.remove(Integer.valueOf(stateType));
                if (states.isEmpty()) {
                    this.mRegPidStates.remove(Integer.valueOf(pid));
                }
            }
        }
    }

    private void onActionStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
        synchronized (this.mSinks) {
            try {
                int size = this.mSinks.beginBroadcast();
                for (int i = 0; i < size; i++) {
                    IStateRecognitionSink sink = (IStateRecognitionSink) this.mSinks.getBroadcastItem(i);
                    try {
                        Integer targetPid = (Integer) this.mSinkMapPid.get(sink);
                        if (targetPid == null) {
                            Log.e("PGServer", "Error not find pid for sink: " + sink);
                        } else {
                            synchronized (this.mRegPidStates) {
                                ArrayList<Integer> states = (ArrayList) this.mRegPidStates.get(targetPid);
                                if (states == null) {
                                    Log.w("PGServer", "Error not find states for pid: " + targetPid);
                                } else {
                                    if (states.contains(Integer.valueOf(stateType))) {
                                        Log.i("PGServer", "report state:" + stateType + " event type:" + eventType + " pid:" + pid + " uid:" + uid + " pkg:" + pkg + " to pid: " + targetPid);
                                        sink.onStateChanged(stateType, eventType, pid, pkg, uid);
                                    }
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e("PGServer", "Error delivering activity changed event.", e);
                    }
                }
                this.mSinks.finishBroadcast();
            } catch (IllegalStateException err) {
                Log.e("PGServer", "Error delivering activity changed event.", err);
            }
        }
        return;
    }

    private String getVersion(Context context) {
        String myselfPkg = "com.huawei.powergenie";
        try {
            return context.getPackageManager().getPackageInfo("com.huawei.powergenie", 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e("PGServer", "not find myself!");
            return "unknow";
        }
    }

    private String getProcInfo() {
        StringBuffer buffer = new StringBuffer();
        IDeviceState device = (IDeviceState) this.mICoreContext.getService("device");
        buffer.append("  version: ").append(getVersion(this.mICoreContext.getContext()));
        buffer.append("  mypid: ").append(Process.myPid());
        buffer.append("  crashCnt: ").append(device.getCrashCount());
        buffer.append("  Is64Bit: ").append(Process.is64Bit());
        buffer.append("  Pss: ").append(Debug.getPss()).append("kB");
        buffer.append("  ElapsedCpu: ").append(Process.getElapsedCpuTime()).append("ms");
        return buffer.toString();
    }

    private void printHelper(PrintWriter pw) {
        pw.println("dumpsys powergeinus                      dump all info");
        pw.println("dumpsys powergeinus help                 show cmd information");
        pw.println("dumpsys powergeinus cpu                  dump cpu/ddr/gpu/ freq");
        pw.println("dumpsys powergeinus battery              dump battery monitor states");
        pw.println("dumpsys powergeinus device               dump device states");
        pw.println("dumpsys powergeinus modules              dump all modules info");
        pw.println("dumpsys powergeinus thermal              dump thermal states\n");
        pw.println("dumpsys powergeinus stats                dump power stats data\n");
        pw.println("only use for test, not support official version");
        pw.println("dump powergenius integration             run all integration test");
        pw.println("dump powergenius integration msg         run integration msgevent test");
        pw.println("dump powergenius integration event       run integration hookevent test");
        pw.println("dump powergenius integration adapter     run integration adapter test");
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mICoreContext.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump PG service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (!isValidDumpArgs(args)) {
            pw.println("invalid args !");
        } else if (args.length <= 0 || !args[0].equals("help")) {
            Log.i("PGServer", "PowerGenius Dump");
            pw.println("POWER GENIUS DUMP");
            pw.println(getProcInfo());
            pw.print("  START PG: RTC=");
            pw.print(Utils.formatDate(this.mStartPGRTC));
            pw.print(" ELAPSED=");
            TimeUtils.formatDuration(this.mStartPGELAPSED, pw);
            pw.println();
            pw.println();
            if (args.length == 1 && "-a".equals(args[0])) {
                args = new String[0];
            }
            this.mICoreContext.dumpAll(pw, args, args.length == 0 ? null : (String) this.mValidArgsMap.get(args[0]));
        } else {
            printHelper(pw);
        }
    }

    private boolean isValidDumpArgs(String[] args) {
        if (args == null) {
            return false;
        }
        if (args.length == 2 && args[0].equals("integration") && !this.mValidArgs2.contains(args[1])) {
            return false;
        }
        return args.length < 1 || this.mValidArgsMap.containsKey(args[0]);
    }
}
