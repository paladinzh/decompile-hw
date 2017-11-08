package com.android.server.am;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.IActivityController;
import android.app.IActivityController.Stub;
import android.app.IActivityManager.ContentProviderHolder;
import android.app.IApplicationThread;
import android.app.INotificationManager;
import android.app.IServiceConnection;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.mtm.MultiTaskManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.TransactionTooLargeException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.rms.HwSysResManager;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Input;
import android.rms.iaware.DataContract.Input.Builder;
import android.rms.iaware.LogIAware;
import android.rog.AppRogInfo;
import android.rog.AppRogInfo.UpdateRog;
import android.rog.IHwRogListener;
import android.rog.IRogManager;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.PointerEventListener;
import android.widget.Toast;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;
import com.android.internal.os.HwBootCheck;
import com.android.internal.os.HwBootFail;
import com.android.server.AlarmManagerService;
import com.android.server.HwConnectivityService;
import com.android.server.LocalServices;
import com.android.server.PPPOEStateMachine;
import com.android.server.SMCSAMSHelper;
import com.android.server.ServiceThread;
import com.android.server.UiThread;
import com.android.server.Watchdog;
import com.android.server.am.AbsActivityManager.AppDiedInfo;
import com.android.server.input.HwInputManagerService.HwInputManagerServiceInternal;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.cpu.CPUFeatureAMSCommunicator;
import com.android.server.rms.iaware.cpu.CPUKeyBackground;
import com.android.server.rms.iaware.cpu.CPUResourceConfigControl;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.ResourceFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import com.android.server.security.trustspace.TrustSpaceManagerInternal;
import com.android.server.util.AbsUserBehaviourRecord;
import com.android.server.util.HwUserBehaviourRecord;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.pushagentproxy.PushService;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.hsm.permission.ANRFilter;
import com.huawei.pgmng.log.LogPower;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public final class HwActivityManagerService extends ActivityManagerService {
    public static final int BACKUP_APP_ADJ = 300;
    static final boolean DEBUG_HWTRIM = smcsLOGV;
    static final boolean DEBUG_HWTRIM_PERFORM = smcsLOGV;
    private static final int EMPTY_PROCESS_LIMIT = 4;
    private static final int ENABLE_TIME = 3000;
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int HOME_APP_ADJ = 600;
    private static final String HW_TRIM_MEMORY_ACTION = "huawei.intent.action.HW_TRIM_MEMORY_ACTION";
    static final int IS_IN_MULTIWINDOW_MODE_TRANSACTION = 3103;
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    public static final int NATIVE_ADJ = -1000;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    private static final int PERSISTENT_MASK = 9;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PREVIOUS_APP_ADJ = 700;
    private static final int QUEUE_NUM_DEFAULT = 2;
    private static final int QUEUE_NUM_IAWARE = 6;
    private static final int QUEUE_NUM_RMS = 4;
    static final int REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3101;
    private static final int ROG_CHANGE_EVENT_INFO = 2;
    private static final int ROG_CHANGE_EVENT_SWITCH = 1;
    public static final int SERVICE_ADJ = 500;
    public static final int SERVICE_B_ADJ = 800;
    private static final String SETTING_GUEST_HAS_LOGGED_IN = "guest_has_logged_in";
    static final int SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION = 2101;
    private static final int SHOW_GUEST_SWITCH_DIALOG_MSG = 50;
    private static final int SHOW_SWITCH_DIALOG_MSG = 49;
    static final int SHOW_UNINSTALL_LAUNCHER_MSG = 48;
    private static final int SMART_TRIM_ADJ_LIMIT = SystemProperties.getInt("ro.smart_trim.adj", 3);
    private static final int SMART_TRIM_BEGIN_HW_SYSM = 41;
    private static final int SMART_TRIM_POST_MSG_DELAY = 10;
    private static final int START_HW_SERVICE_POST_MSG_DELAY = 60000;
    public static final int SYSTEM_ADJ = -900;
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final String TAG = "HwActivityManagerService";
    static final int UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3102;
    public static final int VISIBLE_APP_ADJ = 100;
    private static boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    static final boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static IBinder mAudioService = null;
    private static final boolean mIsSMCSHWSYSMEnabled = SystemProperties.getBoolean("ro.enable.hwsysm_smcs", true);
    private static HwActivityManagerService mSelf;
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private static Set<String> sFakeForegroundActivities = new HashSet();
    private static HashMap<String, Integer> sHardCodeAppToSetOomAdjArrays = new HashMap();
    static final boolean smcsLOGV = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private AlarmManagerService mAlms;
    private HwSysResource mAppResource;
    public HwSysResource mAppServiceResource;
    private final ArrayMap<Integer, ArrayMap<Integer, Long>> mAssocMap = new ArrayMap();
    private Handler mBootCheckHandler;
    private HwSysResource mBroadcastResource;
    private String mCloneAppList;
    private HashMap<String, Intent> mCurrentSplitIntent = new HashMap();
    private AbsUserBehaviourRecord mCust;
    ActivityRecord mFocusedActivityForNavi = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwActivityManagerService.SMART_TRIM_BEGIN_HW_SYSM /*41*/:
                    HwActivityManagerService.this.hwTrimApk_HwSysM(HwActivityManagerService.this.mTrimProcName, HwActivityManagerService.this.mTrimProcUid, HwActivityManagerService.this.mTrimType);
                    return;
                case HwActivityManagerService.SHOW_UNINSTALL_LAUNCHER_MSG /*48*/:
                    HwActivityManagerService.this.showUninstallLauncher();
                    return;
                case HwActivityManagerService.SHOW_SWITCH_DIALOG_MSG /*49*/:
                    HwActivityManagerService.this.mUserController.showUserSwitchDialog((Pair) msg.obj);
                    return;
                case 50:
                    HwActivityManagerService.this.showGuestSwitchDialog(msg.arg1, (String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    final Handler mHwHandler;
    final ServiceThread mHwHandlerThread;
    private String mLastLauncherName;
    private Intent mLastSplitIntent;
    private ResetSessionDialog mNewSessionDialog;
    private HwSysResource mOrderedBroadcastResource;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    private boolean[] mScreenStatusRequest = new boolean[]{false, false};
    private SettingsObserver mSettingsObserver;
    private HashMap<String, Stack<IBinder>> mSplitActivityEntryStack;
    private Bundle mSplitExtras;
    private RemoteCallbackList<IMWThirdpartyCallback> mThirdPartyCallbackList;
    private String mTrimProcName = null;
    private int mTrimProcUid = -1;
    private String mTrimType = null;
    private TrustSpaceManagerInternal mTrustSpaceManagerInternal;

    private final class BootCheckHandler extends Handler {
        public BootCheckHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                case 101:
                case 102:
                    try {
                        if (ActivityManagerDebugConfig.HWFLOW) {
                            Slog.i("ActivityManager_FLOW", "mBootCheckHandler: " + msg.what + ",mActivityIdle: " + HwActivityManagerService.this.mActivityIdle);
                        }
                        HwBootCheck.addBootInfo("currBootScene is: " + msg.what);
                        HwBootCheck.addBootInfo("mSystemReady is: " + HwActivityManagerService.this.mSystemReady);
                        HwActivityManagerService.this.bootSceneEnd(msg.what);
                        if (!HwActivityManagerService.this.mActivityIdle) {
                            HwActivityManagerService.this.addBootFailedLog();
                            return;
                        }
                        return;
                    } catch (Exception ex) {
                        Flog.e(100, "BootCheckHandler exception: " + ex.toString());
                        return;
                    }
                default:
                    return;
            }
        }
    }

    static final class IawarePointerEventListener implements PointerEventListener {
        IawarePointerEventListener() {
        }

        public void onPointerEvent(MotionEvent motionEvent) {
            int action = motionEvent.getAction();
            if (action == 0 || action == 1) {
                HwSysResManager resManager = HwSysResManager.getInstance();
                if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_INPUT))) {
                    Builder builder = Input.builder();
                    if (action == 0) {
                        builder.addEvent(10001);
                    } else {
                        builder.addEvent(80001);
                    }
                    CollectData appsData = builder.build();
                    long id = Binder.clearCallingIdentity();
                    resManager.reportData(appsData);
                    Binder.restoreCallingIdentity(id);
                }
            }
        }
    }

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwActivityManagerService.TAG, "OverscanTimeout run");
            Global.putString(HwActivityManagerService.this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    private class ResetSessionDialog extends AlertDialog implements OnClickListener {
        private final int mUserId;

        public ResetSessionDialog(Context context, int userId) {
            super(context, context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog", null, null));
            getWindow().setType(2014);
            getWindow().addFlags(655360);
            if (((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked()) {
                getWindow().addPrivateFlags(Integer.MIN_VALUE);
            }
            setMessage(context.getString(33685834));
            setButton(-1, context.getString(33685836), this);
            setButton(-2, context.getString(33685835), this);
            setCanceledOnTouchOutside(false);
            this.mUserId = userId;
        }

        public void onClick(DialogInterface dialog, int which) {
            Slog.i(HwActivityManagerService.TAG, "onClick which:" + which);
            if (which == -2) {
                HwActivityManagerService.this.wipeGuestSession(this.mUserId);
                dismiss();
            } else if (which == -1) {
                cancel();
                HwActivityManagerService.this.sendMessageToSwitchUser(this.mUserId, HwActivityManagerService.this.getGuestName());
            }
        }
    }

    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.stk.check_screen_idle".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot_id", 0);
                if (slotId < 0 || slotId >= HwActivityManagerService.this.mScreenStatusRequest.length) {
                    Slog.w(HwActivityManagerService.TAG, "ScreenStatusReceiver, slotId " + slotId + " Invalid");
                    return;
                }
                HwActivityManagerService.this.mScreenStatusRequest[slotId] = intent.getBooleanExtra("SCREEN_STATUS_REQUEST", false);
                if (HwActivityManagerService.this.mScreenStatusRequest[slotId]) {
                    ActivityRecord p = HwActivityManagerService.this.getFocusedStack().topRunningActivityLocked();
                    if (p != null) {
                        Intent StkIntent = new Intent("android.intent.action.stk.idle_screen");
                        if (p.intent.hasCategory("android.intent.category.HOME")) {
                            StkIntent.putExtra("SCREEN_IDLE", true);
                        } else {
                            StkIntent.putExtra("SCREEN_IDLE", false);
                        }
                        StkIntent.putExtra("slot_id", slotId);
                        HwActivityManagerService.this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(HwActivityManagerService.TAG, "Broadcasting Home Idle Screen Intent ... slot: " + slotId);
                        }
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(HwActivityManagerService.TAG, "Screen Status request is OFF, slot: " + slotId);
                }
            }
        }
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri CLONE_APP_LIST_URI = Secure.getUriFor("clone_app_list");

        SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = HwActivityManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(this.CLONE_APP_LIST_URI, false, this, 0);
            HwActivityManagerService.this.mCloneAppList = Secure.getStringForUser(resolver, "clone_app_list", 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.CLONE_APP_LIST_URI.equals(uri)) {
                String cloneAppList = Secure.getStringForUser(HwActivityManagerService.this.mContext.getContentResolver(), "clone_app_list", 0);
                Flog.i(100, "Secure.clone_app_list is changed, old is " + HwActivityManagerService.this.mCloneAppList + ", new is " + cloneAppList);
                if (!(HwActivityManagerService.this.mCloneAppList == null || HwActivityManagerService.this.mCloneAppList.equals(cloneAppList))) {
                    for (String pkg : HwActivityManagerService.this.mCloneAppList.split(";")) {
                        if (!(cloneAppList + ";").contains(pkg + ";")) {
                            synchronized (HwActivityManagerService.mSelf) {
                                HwActivityManagerService.this.deleteClonedPackage(pkg);
                                for (int i = HwActivityManagerService.this.mRecentTasks.size() - 1; i >= 0; i--) {
                                    TaskRecord tr = (TaskRecord) HwActivityManagerService.this.mRecentTasks.get(i);
                                    String taskPackageName = tr.getBaseIntent().getComponent().getPackageName();
                                    if (tr.userId == 0 && taskPackageName.equals(pkg) && tr.multiLaunchId != 0) {
                                        HwActivityManagerService.this.removeTaskByIdLocked(tr.taskId, false, true);
                                    }
                                }
                                try {
                                    HwActivityManagerService.this.mInstaller.rmClonedAppDataDir(Environment.getDataDirectory() + File.separator + "data" + File.separator + pkg + File.separator + "_hwclone");
                                } catch (Exception e) {
                                    Flog.i(100, "Failed to rm cloned app data dir", e);
                                }
                                Flog.i(100, "Successfully clean up when deleting cloned app " + pkg);
                            }
                        }
                    }
                }
                HwActivityManagerService.this.mCloneAppList = cloneAppList;
            }
        }
    }

    class TrimMemoryReceiver extends BroadcastReceiver {
        TrimMemoryReceiver() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwActivityManagerService.HW_TRIM_MEMORY_ACTION.equals(intent.getAction()))) {
                HwActivityManagerService.this.trimGLMemory(80);
            }
        }
    }

    static {
        sHardCodeAppToSetOomAdjArrays.put("com.huawei.android.pushagent.PushService", Integer.valueOf(200));
        sAllowedCrossUserForCloneArrays.add(HwCertification.SIGNATURE_MEDIA);
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media.documents");
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher.settings");
        sAllowedCrossUserForCloneArrays.add("com.android.badge");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media");
        sAllowedCrossUserForCloneArrays.add("android.media.IMediaScannerService");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.files");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.app");
        sAllowedCrossUserForCloneArrays.add("com.huawei.numberlocation");
        sAllowedCrossUserForCloneArrays.add("csp-prefs-cfg");
        sAllowedCrossUserForCloneArrays.add("contacts");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts");
        sAllowedCrossUserForCloneArrays.add("android.process.media");
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher");
        sAllowedCrossUserForCloneArrays.add("android.process.acore");
        sAllowedCrossUserForCloneArrays.add("call_log");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FOLDER");
        sAllowedCrossUserForCloneArrays.add("com.android.launcher.action.INSTALL_SHORTCUT");
        sAllowedCrossUserForCloneArrays.add("mms");
        sAllowedCrossUserForCloneArrays.add("sms");
        sAllowedCrossUserForCloneArrays.add("mms-sms");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads");
        sAllowedCrossUserForCloneArrays.add("downloads");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads.documents");
        sFakeForegroundActivities.add("com.ss.android.article.news/com.ss.android.message.sswo.SswoActivity");
        sFakeForegroundActivities.add("dongzheng.szkingdom.android.phone/com.dgzq.IM.ui.activity.KeepAliveActivity");
        sFakeForegroundActivities.add("com.byd.aeri.caranywhere/com.example.jiguangpush.OnePixelActivity");
        sFakeForegroundActivities.add("com.tencent.news/com.tencent.news.push.alive.offactivity.HollowActivity");
        sFakeForegroundActivities.add("com.tencent.reading/com.tencent.news.push.alive.offactivity.HollowActivity");
    }

    public HwActivityManagerService(Context mContext) {
        super(mContext);
        mSelf = this;
        this.mHwHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHwHandlerThread.start();
        this.mHwHandler = new Handler(this.mHwHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 22:
                        synchronized (HwActivityManagerService.this) {
                            int appid = msg.arg1;
                            boolean restart = msg.arg2 == 1;
                            Bundle bundle = msg.obj;
                            String pkg = bundle.getString(HwGpsPowerTracker.DEL_PKG);
                            String reason = bundle.getString("reason");
                            Slog.w(HwActivityManagerService.TAG, "tsy1 stopping pkg excuting at: " + pkg);
                            HwActivityManagerService.this.hwForceStopPackageLocked(pkg, appid, restart, false, true, false, false, -1, reason);
                            Slog.w(HwActivityManagerService.TAG, "tsy1 end stopping" + pkg);
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.mBootCheckHandler = new BootCheckHandler(HwBootCheck.getHandler().getLooper());
        bootSceneStart(100, AppHibernateCst.DELAY_ONE_MINS);
        this.mCpusetSwitch = CPUFeature.isCpusetEnable();
    }

    public static HwActivityManagerService self() {
        return mSelf;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 502:
                data.enforceInterface("android.app.IActivityManager");
                boolean res = handleANRFilterFIFO(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(res ? 1 : 0);
                return true;
            case 503:
                data.enforceInterface("android.app.IActivityManager");
                boolean isClonedProcess = isClonedProcess(data.readInt());
                reply.writeNoException();
                reply.writeInt(isClonedProcess ? 1 : 0);
                return true;
            case 504:
                data.enforceInterface("android.app.IActivityManager");
                String packageName = getPackageNameForPid(data.readInt());
                reply.writeNoException();
                reply.writeString(packageName);
                return true;
            case 505:
                data.enforceInterface("android.app.IActivityManager");
                boolean isPackageCloned = isPackageCloned(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(isPackageCloned ? 1 : 0);
                return true;
            case 506:
                data.enforceInterface("android.app.IActivityManager");
                int isPreloadSuccess = preloadApplication(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(isPreloadSuccess);
                return true;
            case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                data.enforceInterface("android.app.IActivityManager");
                setIntentInfo((Intent) data.readParcelable(null), data.readString(), data.readBundle(), data.readInt() > 0);
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_GATEWAY /*602*/:
                data.enforceInterface("android.app.IActivityManager");
                Parcelable[] p = getIntentInfo(data.readString(), data.readInt() > 0);
                reply.writeNoException();
                reply.writeParcelableArray(p, 0);
                return true;
            case WifiProCommonUtils.RESP_CODE_INVALID_URL /*603*/:
                data.enforceInterface("android.app.IActivityManager");
                addToEntryStack(data.readString(), data.readStrongBinder(), data.readInt(), (Intent) Intent.CREATOR.createFromParcel(data));
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER /*604*/:
                data.enforceInterface("android.app.IActivityManager");
                clearEntryStack(data.readString(), data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED /*605*/:
                data.enforceInterface("android.app.IActivityManager");
                removeFromEntryStack(data.readString(), data.readStrongBinder());
                return true;
            case WifiProCommonUtils.RESP_CODE_CONN_RESET /*606*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean isTop = isTopSplitActivity(data.readString(), data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(isTop ? 1 : 0);
                return true;
            case SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION /*2101*/:
                data.enforceInterface("android.app.IActivityManager");
                setCustomActivityController(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION /*3101*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean registered = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registered ? 1 : 0);
                return true;
            case UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION /*3102*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean unregistered = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(unregistered ? 1 : 0);
                return true;
            case IS_IN_MULTIWINDOW_MODE_TRANSACTION /*3103*/:
                data.enforceInterface("android.app.IActivityManager");
                boolean result = isInMultiWindowMode();
                reply.writeNoException();
                reply.writeInt(result ? 1 : 0);
                return true;
            case 1599294787:
                if (DEBUG_HWTRIM) {
                    Log.v(TAG, "AMS.onTransact: got HWMEMCLEAN_TRANSACTION");
                }
                if (!mIsSMCSHWSYSMEnabled) {
                    if (DEBUG_HWTRIM) {
                        Log.v(TAG, "AMS.onTransact: HWSysM SMCS is disabled.");
                        break;
                    }
                } else if (this.mContext == null) {
                    return false;
                } else {
                    if (this.mContext.checkCallingPermission("huawei.permission.HSM_SMCS") != 0) {
                        if (DEBUG_HWTRIM) {
                            Log.e(TAG, "SMCSAMSHelper.handleTransact permission deny");
                        }
                        return false;
                    } else if (SMCSAMSHelper.getInstance().handleTransact(data, reply, flags)) {
                        return true;
                    }
                }
                break;
        }
        return super.onTransact(code, data, reply, flags);
    }

    protected void updateUsageStats(ActivityRecord resumedComponent, boolean resumed) {
        if (resumed && mIsSMCSHWSYSMEnabled) {
            SMCSAMSHelper.getInstance().smartTrimProcessPackageResume(resumedComponent.realActivity, resumedComponent.processName);
        }
        super.updateUsageStats(resumedComponent, resumed);
    }

    public void killApplication(String pkg, int appId, int userId, String reason) {
        if (!"vold reset".equals(reason) || !"com.android.providers.media".equals(pkg)) {
            super.killApplication(pkg, appId, userId, reason);
        } else if (pkg != null) {
            if (appId < 0) {
                Slog.w(TAG, "Invalid appid specified for pkg : " + pkg);
                return;
            }
            int callerUid = Binder.getCallingUid();
            if (UserHandle.getAppId(callerUid) == 1000) {
                Message msg = this.mHwHandler.obtainMessage(22);
                msg.arg1 = appId;
                msg.arg2 = 0;
                Bundle bundle = new Bundle();
                Slog.w(TAG, "tsy1 stopping pkg at : " + pkg);
                bundle.putString(HwGpsPowerTracker.DEL_PKG, pkg);
                bundle.putString("reason", reason);
                msg.obj = bundle;
                this.mHwHandler.sendMessage(msg);
            } else {
                throw new SecurityException(callerUid + " cannot kill pkg: " + pkg);
            }
        }
    }

    public boolean handleANRFilterFIFO(int uid, int cmd) {
        Log.d(TAG, "handleANRFilterFIFO,uid = " + uid + "cmd = " + cmd);
        switch (cmd) {
            case 0:
                return ANRFilter.getInstance().addUid(uid);
            case 1:
                return ANRFilter.getInstance().removeUid(uid);
            case 2:
                return ANRFilter.getInstance().checkUid(uid);
            default:
                return false;
        }
    }

    public final void hwTrimApkPost_HwSysM(String trimProc, int uid, String trimType) {
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApkPost_HwSysM");
                timeStart = System.currentTimeMillis();
            }
            this.mTrimProcName = trimProc;
            this.mTrimProcUid = uid;
            this.mTrimType = trimType;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    HwActivityManagerService.this.mHandler.sendEmptyMessage(HwActivityManagerService.SMART_TRIM_BEGIN_HW_SYSM);
                }
            }, 10);
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApkPost_HwSysM: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        } catch (Exception e) {
            Log.e(TAG, "AMS.hwTrimApkPost_HwSysM: catch exception: " + e.toString());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void hwTrimApk_HwSysM(String trimProc, int uid, String trimType) {
        HashSet<String> hashSet;
        Throwable th;
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApk");
                timeStart = System.currentTimeMillis();
            }
            synchronized (this) {
                try {
                    ProcessRecord app = getProcessRecordLocked(trimProc, uid, true);
                    if (DEBUG_HWTRIM_PERFORM) {
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM");
                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: get app cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
                    }
                    if (app != null && app.curAdj > SMART_TRIM_ADJ_LIMIT) {
                        if (DEBUG_HWTRIM) {
                            Log.v(TAG, "AMS.hwTrimApk_HwSysM: go to trim " + app.processName);
                        }
                        removeProcessLocked(app, false, false, "smart trim");
                        HashSet<String> pkgList = new HashSet();
                        try {
                            for (Entry<String, ProcessStateHolder> key : app.pkgList.entrySet()) {
                                pkgList.add((String) key.getKey());
                            }
                            if (DEBUG_HWTRIM_PERFORM) {
                                Log.v(TAG, "AMS.hwTrimApk");
                                Log.v(TAG, "AMS.hwTrimApk_HwSysM: trim action cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
                            }
                            SMCSAMSHelper.getInstance().trimProcessPostProcess(trimProc, uid, trimType, pkgList);
                            hashSet = pkgList;
                        } catch (Throwable th2) {
                            th = th2;
                            hashSet = pkgList;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void hwTrimApk_HwSysM(ArrayList<String> procs, HashSet<String> pkgList) {
        Exception e;
        Throwable th;
        long timeStart = 0;
        try {
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimApk_HwSysM");
                timeStart = System.currentTimeMillis();
            }
            if (procs == null || procs.size() == 0) {
                if (DEBUG_HWTRIM) {
                    Log.e(TAG, "AMS.hwTrimApk_HwSysM: invalid trim processes.");
                }
            } else if (pkgList != null) {
                synchronized (this) {
                    try {
                        ArrayList<ProcessRecord> trimProcs = new ArrayList();
                        try {
                            ProcessRecord app;
                            for (SparseArray<ProcessRecord> apps : this.mProcessNames.getMap().values()) {
                                int NA = apps.size();
                                for (int ia = 0; ia < NA; ia++) {
                                    app = (ProcessRecord) apps.valueAt(ia);
                                    if (app != null) {
                                        if (procs.contains(app.processName)) {
                                            trimProcs.add(app);
                                            procs.remove(app.processName);
                                            if (procs.size() == 0) {
                                                break;
                                            }
                                        } else {
                                            continue;
                                        }
                                    }
                                }
                                if (procs.size() == 0) {
                                    break;
                                }
                            }
                            Iterator<ProcessRecord> itTrim = trimProcs.iterator();
                            while (itTrim.hasNext()) {
                                app = (ProcessRecord) itTrim.next();
                                if (app != null) {
                                    if (DEBUG_HWTRIM) {
                                        Log.v(TAG, "AMS.hwTrimApk_HwSysM: go to trim " + app.processName);
                                    }
                                    removeProcessLocked(app, false, false, "smart trim");
                                    for (Entry<String, ProcessStateHolder> key : app.pkgList.entrySet()) {
                                        pkgList.add((String) key.getKey());
                                    }
                                }
                            }
                            try {
                            } catch (Exception e2) {
                                e = e2;
                                Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            ArrayList<ProcessRecord> arrayList = trimProcs;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "AMS.hwTrimApk_HwSysM: catch exception: " + e.toString());
        }
    }

    public final void hwTrimPkgs_HwSysM(ArrayList<String> pkgs) {
        long timeStart = 0;
        if (DEBUG_HWTRIM_PERFORM) {
            Log.v(TAG, "AMS.hwTrimPkgs_HwSysM");
            timeStart = System.currentTimeMillis();
        }
        if (pkgs != null && pkgs.size() != 0) {
            Iterator<String> it = pkgs.iterator();
            while (it.hasNext()) {
                String sPkg = (String) it.next();
                if (sPkg != null && sPkg.length() > 0) {
                    forceStopPackage(sPkg, UserHandle.myUserId());
                }
            }
            if (DEBUG_HWTRIM_PERFORM) {
                Log.v(TAG, "AMS.hwTrimPkgs_HwSysM: cost " + (System.currentTimeMillis() - timeStart) + " ms end.");
            }
        }
    }

    public long getHWMemFreeLimit_HwSysM() {
        return this.mProcessList.getMemLevel(Integer.MAX_VALUE);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getRunningAppProcessRecord_HwSysM(ArrayList<STProcessRecord> runList) {
        Exception e;
        try {
            Throwable th;
            synchronized (this.mLruProcesses) {
                if (runList == null) {
                    return;
                }
                try {
                    STProcessRecord stpr;
                    int i = this.mLruProcesses.size() - 1;
                    STProcessRecord stpr2 = null;
                    while (i >= 0) {
                        try {
                            ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                            if (app == null) {
                                stpr = stpr2;
                            } else if (app.thread == null || app.crashing || app.notResponding || app.curAdj < 0) {
                                stpr = stpr2;
                            } else {
                                stpr = new STProcessRecord(app.processName, app.uid, app.pid, app.curAdj, changeArrayMapPkgList2HashSet(app.pkgList));
                                runList.add(stpr);
                            }
                            i--;
                            stpr2 = stpr;
                        } catch (Throwable th2) {
                            th = th2;
                            stpr = stpr2;
                        }
                    }
                    try {
                    } catch (Exception e2) {
                        e = e2;
                        stpr = stpr2;
                        Log.e(TAG, "AMS.getRunningAppProcessRecord_HwSysM: catch exception: " + e.toString());
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            }
            throw th;
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "AMS.getRunningAppProcessRecord_HwSysM: catch exception: " + e.toString());
        }
    }

    public void smartTrimAddProcessRelation_HwSysM(ContentProviderConnection conn) {
        if (mIsSMCSHWSYSMEnabled && conn != null && conn.client != null && conn.provider != null && conn.provider.proc != null) {
            SMCSAMSHelper.getInstance().smartTrimAddProcessRelation(conn.client.processName, conn.client.curAdj, conn.client.pkgList, conn.provider.proc.processName, conn.provider.proc.curAdj, conn.provider.proc.pkgList);
        }
    }

    public void smartTrimAddProcessRelation_HwSysM(AppBindRecord b, AppBindRecord c) {
        if (mIsSMCSHWSYSMEnabled && c != null && c.service != null && c.service.app != null && b != null && b.client != null) {
            SMCSAMSHelper.getInstance().smartTrimAddProcessRelation(b.client.processName, b.client.curAdj, b.client.pkgList, c.service.app.processName, c.service.app.curAdj, c.service.app.pkgList);
        }
    }

    private HashSet<String> changeArrayMapPkgList2HashSet(ArrayMap<String, ProcessStateHolder> pkgListA) {
        if (pkgListA == null || pkgListA.size() == 0) {
            return null;
        }
        HashSet<String> pkgListH = new HashSet();
        int size = pkgListA.size();
        for (int i = 0; i < size; i++) {
            String pkgName = (String) pkgListA.keyAt(i);
            if (pkgName != null && pkgName.length() > 0) {
                pkgListH.add(pkgName);
            }
        }
        return pkgListH;
    }

    public void addCallerToIntent(Intent intent, IApplicationThread caller) {
        String callerPackage = null;
        if (caller != null) {
            ProcessRecord callerApp = getRecordForAppLocked(caller);
            if (callerApp != null) {
                callerPackage = callerApp.info.packageName;
            }
        }
        if (callerPackage != null) {
            String CALLER_PACKAGE = "caller_package";
            try {
                if (isInstall(intent)) {
                    String callerIndex = intent.getStringExtra("caller_package");
                    if (callerIndex != null) {
                        callerPackage = callerIndex;
                    }
                    intent.putExtra("caller_package", callerPackage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Get package info faild:" + e);
            }
        }
    }

    private boolean isInstall(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        boolean story = false;
        if ("android.intent.action.INSTALL_PACKAGE".equals(action)) {
            story = true;
        }
        if ("application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        return story;
    }

    private final boolean isOomAdjCustomized(ProcessRecord app) {
        if (sHardCodeAppToSetOomAdjArrays.containsKey(app.info.packageName) || sHardCodeAppToSetOomAdjArrays.containsKey(app.processName)) {
            return true;
        }
        return false;
    }

    private int retrieveCustedMaxAdj(String processName) {
        int rc = -901;
        if (sHardCodeAppToSetOomAdjArrays.containsKey(processName)) {
            rc = ((Integer) sHardCodeAppToSetOomAdjArrays.get(processName)).intValue();
        }
        Slog.i(TAG, "retrieveCustedMaxAdj for processName:" + processName + ", get adj:" + rc);
        return rc;
    }

    protected final int computeOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (this.mAdjSeq != app.adjSeq) {
            return super.computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now);
        }
        int app_curRawAdj = super.computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now);
        if (app.curAdj > app.maxAdj && isOomAdjCustomized(app)) {
            app.curAdj = app.maxAdj;
        }
        return app_curRawAdj;
    }

    protected final void startProcessLocked(ProcessRecord app, String hostingType, String hostingNameStr, String abiOverride, String entryPoint, String[] entryPointArgs) {
        if (!shouldPreventStartProcess(app)) {
            noteProcessStart(app.info.packageName, app.processName, app.pid, app.uid, true, hostingType, hostingNameStr);
            super.startProcessLocked(app, hostingType, hostingNameStr, abiOverride, entryPoint, entryPointArgs);
            if (isOomAdjCustomized(app)) {
                int custMaxAdj = retrieveCustedMaxAdj(app.processName);
                if (app.maxAdj > PERSISTENT_PROC_ADJ && custMaxAdj >= SYSTEM_ADJ && custMaxAdj <= 906) {
                    app.maxAdj = custMaxAdj;
                    Slog.i(TAG, "addAppLocked, app:" + app + ", set maxadj to " + custMaxAdj);
                }
            } else if (app.maxAdj > AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ && AwareAppMngSort.checkAppMngEnable() && isAppMngOomAdjCustomized(app.info.packageName)) {
                app.maxAdj = AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
            }
        }
    }

    protected void startPushService() {
        File jarFile = new File("/system/framework/hwpush.jar");
        File custFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if ((jarFile != null && jarFile.exists()) || (custFile != null && custFile.exists())) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Intent serviceIntent = new Intent(HwActivityManagerService.this.mContext, PushService.class);
                    serviceIntent.putExtra("startFlag", PPPOEStateMachine.PHASE_INITIALIZE);
                    HwActivityManagerService.this.mContext.startService(serviceIntent);
                }
            }, AppHibernateCst.DELAY_ONE_MINS);
        }
    }

    public Configuration getCurNaviConfiguration() {
        return this.mWindowManager.getCurNaviConfiguration();
    }

    protected void setFocusedActivityLockedForNavi(ActivityRecord r) {
        if (this.mFocusedActivityForNavi != r) {
            this.mFocusedActivityForNavi = r;
            if (r != null) {
                this.mWindowManager.setFocusedAppForNavi(r.appToken);
            }
        }
    }

    public void showUninstallLauncher() {
        try {
            PackageInfo pInfo = this.mContext.getPackageManager().getPackageInfo(this.mLastLauncherName, 0);
            if (pInfo != null) {
                AlertDialog d = new BaseErrorDialog(this.mContext);
                d.getWindow().setType(2010);
                d.setCancelable(false);
                d.setTitle(this.mContext.getString(17041115));
                String appName = this.mContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo).toString();
                d.setMessage(this.mContext.getString(17041116, new Object[]{appName}));
                d.setButton(-1, this.mContext.getString(17041117), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HwActivityManagerService.this.mContext.getPackageManager().deletePackage(HwActivityManagerService.this.mLastLauncherName, null, 0);
                    }
                });
                d.setButton(-2, this.mContext.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                d.show();
            }
        } catch (NameNotFoundException e) {
        }
    }

    public void showUninstallLauncherDialog(String pkgName) {
        this.mLastLauncherName = pkgName;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_UNINSTALL_LAUNCHER_MSG));
    }

    public String topAppName() {
        ActivityStack focusedStack;
        synchronized (this) {
            focusedStack = getFocusedStack();
        }
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r.shortComponentName;
        }
        return null;
    }

    public void setCpusetSwitch(boolean enable) {
        this.mCpusetSwitch = enable;
    }

    public boolean setCurProcessGroup(ProcessRecord app, int schedGroup) {
        if (app != null) {
            app.curSchedGroup = schedGroup;
        }
        return true;
    }

    public void setWhiteListProcessGroup(ProcessRecord app, ProcessRecord TOP_APP, boolean bConnectTopApp) {
        if (app != null) {
            if (app.curSchedGroup == 0) {
                if (SystemClock.elapsedRealtime() - app.startTime <= 3000) {
                    app.curSchedGroup = 4;
                }
                return;
            }
            if (!(app == TOP_APP || bConnectTopApp || 1 != CPUResourceConfigControl.getInstance().isWhiteList(app.processName))) {
                app.curSchedGroup = 0;
            }
        }
    }

    public void notifyAppEventToIaware(int duration) {
        CPUFeatureAMSCommunicator.getInstance().setTopAppToBoost(duration);
    }

    public boolean serviceIsRunning(ComponentName serviceCmpName, int curUser) {
        boolean z;
        synchronized (this) {
            Slog.d(TAG, "serviceIsRunning, for user " + curUser + ", serviceCmpName " + serviceCmpName);
            z = this.mServices.getServices(curUser).get(serviceCmpName) != null;
        }
        return z;
    }

    void setDeviceProvisioned() {
        ContentResolver cr = this.mContext.getContentResolver();
        if ((Global.getInt(cr, "device_provisioned", 0) == 0 || Secure.getInt(cr, "user_setup_complete", 0) == 0) && ((PackageManagerService) ServiceManager.getService(ControlScope.PACKAGE_ELEMENT_KEY)).isSetupDisabled()) {
            Global.putInt(cr, "device_provisioned", 1);
            Secure.putInt(cr, "user_setup_complete", 1);
        }
    }

    public void systemReady(Runnable goingCallback) {
        if (!testIsSystemReady()) {
            setDeviceProvisioned();
        }
        super.systemReady(goingCallback);
        initTrustSpace();
        this.mContext.registerReceiver(new ScreenStatusReceiver(), new IntentFilter("android.intent.action.stk.check_screen_idle"), "com.huawei.permission.STK_CHECK_SCREEN_IDLE", null);
        this.mContext.registerReceiver(new TrimMemoryReceiver(), new IntentFilter(HW_TRIM_MEMORY_ACTION));
        if (IS_SUPPORT_CLONE_APP) {
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
        }
        this.mThirdPartyCallbackList = new RemoteCallbackList();
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes = new ArrayList();
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            ArrayList<Integer> queueSizesTemp = queue.getIawareDumpData();
            if (queueSizesTemp != null) {
                queueSizes.addAll(queueSizesTemp);
            }
        }
        return queueSizes;
    }

    public void updateSRMSStatisticsData(int subTypeCode) {
        SRMSDumpRadar.getInstance().updateStatisticsData(subTypeCode);
    }

    public boolean getIawareResourceFeature(int type) {
        return ResourceFeature.getIawareResourceFeature(type);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                delay = Math.max(queue.proxyBroadcast(pkgs, proxy), delay);
            }
        }
        return delay;
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                delay = Math.max(queue.proxyBroadcastByPid(pids, proxy), delay);
            }
        }
        return delay;
    }

    public void setProxyBCActions(List<String> actions) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setProxyBCActions(actions);
            }
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setActionExcludePkg(action, pkg);
            }
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.proxyBCConfig(type, key, value);
            }
        }
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
        for (int slotId = 0; slotId < this.mScreenStatusRequest.length; slotId++) {
            if (this.mScreenStatusRequest[slotId]) {
                Intent StkIntent = new Intent("android.intent.action.stk.idle_screen");
                StkIntent.putExtra("SCREEN_IDLE", true);
                StkIntent.putExtra("slot_id", slotId);
                this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
            }
        }
    }

    public void startupFilterReceiverList(Intent intent, List<ResolveInfo> receivers) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            pfwService.startupFilterReceiverList(intent, receivers);
        }
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventStartService(servInfo, callerPid, callerUid);
        }
        return false;
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record) {
        if (intent == null || aInfo == null || record == null) {
            return false;
        }
        if (isSleepingLocked() && sFakeForegroundActivities.contains(record.shortComponentName)) {
            return true;
        }
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService == null) {
            return false;
        }
        return pfwService.shouldPreventStartActivity(intent, aInfo, record);
    }

    public boolean shouldPreventRestartService(String pkgName) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventRestartService(pkgName);
        }
        return false;
    }

    private void initTrustSpace() {
        this.mTrustSpaceManagerInternal = (TrustSpaceManagerInternal) LocalServices.getService(TrustSpaceManagerInternal.class);
        if (this.mTrustSpaceManagerInternal == null) {
            Slog.e(TAG, "TrustSpaceManagerInternal not find !");
        } else {
            this.mTrustSpaceManagerInternal.initTrustSpace();
        }
    }

    private boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        boolean shouldPrevent = false;
        if (this.mSystemReady && this.mTrustSpaceManagerInternal != null) {
            long ident = Binder.clearCallingIdentity();
            try {
                shouldPrevent = this.mTrustSpaceManagerInternal.checkIntent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return shouldPrevent;
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (sInfo == null) {
            return false;
        }
        return shouldPreventStartComponent(2, sInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        if (aInfo == null) {
            return false;
        }
        return shouldPreventStartComponent(0, aInfo.applicationInfo.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerUid, int callerPid, String callerPackage, int userId) {
        if (cpi == null) {
            return false;
        }
        return shouldPreventStartComponent(3, cpi.packageName, callerUid, callerPid, callerPackage, userId);
    }

    public boolean shouldPreventSendBroadcast(Intent intent, String receiver, int callerUid, int callerPid, String callingPackage, int userId) {
        return shouldPreventStartComponent(1, receiver, callerUid, callerPid, callingPackage, userId);
    }

    protected void exitSingleHandMode() {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventStartProvider(cpi, callerPid, callerUid);
        }
        return false;
    }

    protected void setCustomActivityController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setCustomActivityController()");
        synchronized (this) {
            this.mCustomController = controller;
        }
        HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
        if (inputManager != null) {
            inputManager.setCustomActivityController(controller);
        }
    }

    protected boolean customActivityStarting(Intent intent, String packageName) {
        if (this.mCustomController != null) {
            boolean startOK = true;
            try {
                startOK = this.mCustomController.activityStarting(intent.cloneFilter(), packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!startOK) {
                Slog.i(TAG, "Not starting activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    protected boolean customActivityResuming(String packageName) {
        if (this.mCustomController != null) {
            boolean resumeOK = true;
            try {
                resumeOK = this.mCustomController.activityResuming(packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerServiceInternal inputManager = (HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!resumeOK) {
                Slog.i(TAG, "Not resuming activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    protected BroadcastQueue[] initialBroadcastQueue() {
        int queueNum;
        if (enableIaware) {
            queueNum = 6;
        } else if (enableRms) {
            queueNum = 4;
        } else {
            queueNum = 2;
        }
        return new BroadcastQueue[queueNum];
    }

    protected void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableRms || enableIaware) {
            ServiceThread thirdAppHandlerThread = new ServiceThread("ThirdAppHandlerThread", 10, false);
            thirdAppHandlerThread.start();
            Handler thirdAppHandler = new Handler(thirdAppHandlerThread.getLooper());
            this.mFgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "fgthirdapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mBgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "bgthirdapp", AppHibernateCst.DELAY_ONE_MINS, false);
            broadcastQueues[2] = this.mFgThirdAppBroadcastQueue;
            broadcastQueues[3] = this.mBgThirdAppBroadcastQueue;
        }
    }

    protected void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableIaware) {
            ServiceThread keyAppHandlerThread = new ServiceThread("keyAppHanderThread", 0, false);
            keyAppHandlerThread.start();
            Handler keyAppHandler = new Handler(keyAppHandlerThread.getLooper());
            this.mFgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "fgkeyapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mBgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "bgkeyapp", AppHibernateCst.DELAY_ONE_MINS, false);
            broadcastQueues[4] = this.mFgKeyAppBroadcastQueue;
            broadcastQueues[5] = this.mBgKeyAppBroadcastQueue;
        }
    }

    protected boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        boolean z = true;
        if ((!enableRms && !getIawareResourceFeature(1)) || callerApp == null) {
            return false;
        }
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "Split enqueueing broadcast [callerApp]:" + callerApp);
        }
        if (callerApp.instrumentationClass != null) {
            return false;
        }
        if ((callerApp.info.flags & 1) != 0 && (callerApp.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0) {
            z = false;
        }
        return z;
    }

    protected boolean isKeyAppBroadcastQueue(int type, String name) {
        return getIawareResourceFeature(1) && name != null && isKeyApp(type, 0, name);
    }

    protected boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!enableRms && !getIawareResourceFeature(1)) {
            return false;
        }
        if (!this.mFgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!getIawareResourceFeature(1) || this.mFgKeyAppBroadcastQueue == null || this.mBgKeyAppBroadcastQueue == null) {
            return false;
        }
        if (!this.mFgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = this.mBgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return z;
    }

    protected boolean isThirdPartyAppFGBroadcastQueue(BroadcastQueue queue) {
        return queue == this.mFgThirdAppBroadcastQueue;
    }

    protected boolean isKeyAppFGBroadcastQueue(BroadcastQueue queue) {
        return queue == this.mFgKeyAppBroadcastQueue;
    }

    protected BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "thirdAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgthirdapp" : "bgthirdapp") + " queue");
        }
        if (isFg) {
            return this.mFgThirdAppBroadcastQueue;
        }
        return this.mBgThirdAppBroadcastQueue;
    }

    protected BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "keyAppBroadcastQueueForIntent intent " + intent + " on " + (isFg ? "fgkeyapp" : "bgkeyapp") + " queue");
        }
        if (isFg) {
            updateSRMSStatisticsData(0);
        } else {
            updateSRMSStatisticsData(1);
        }
        if (isFg) {
            return this.mFgKeyAppBroadcastQueue;
        }
        return this.mBgKeyAppBroadcastQueue;
    }

    protected void initBroadcastResourceLocked() {
        if (this.mBroadcastResource == null) {
            if (DEBUG_HWTRIM || Log.HWINFO) {
                Log.d(TAG, "init BroadcastResource");
            }
            this.mBroadcastResource = HwFrameworkFactory.getHwResource(11);
        }
    }

    public void checkOrderedBroadcastTimeoutLocked(String actionOrPkg, int timeCost, boolean isInToOut) {
        if (getIawareResourceFeature(2)) {
            if (this.mOrderedBroadcastResource == null) {
                if (DEBUG_HWTRIM || Log.HWINFO) {
                    Log.d(TAG, "init OrderedBroadcastResource");
                }
                this.mOrderedBroadcastResource = HwFrameworkFactory.getHwResource(37);
            }
            if (!(this.mOrderedBroadcastResource == null || isInToOut)) {
                this.mOrderedBroadcastResource.acquire(0, actionOrPkg, 0);
            }
        }
    }

    protected void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
        if (this.mBroadcastResource != null && callerApp != null) {
            int uid = callingUid;
            String pkg = callerPackage;
            int processType = getProcessType(callerApp);
            if ((PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(SYSTEM_DEBUGGABLE, PPPOEStateMachine.PHASE_DEAD)) || processType == 0) && 2 == this.mBroadcastResource.acquire(callingUid, callerPackage, processType) && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Log.i(TAG, "This App send broadcast speed is overload! uid = " + callingUid);
            }
        }
    }

    protected void clearBroadcastResource(ProcessRecord app) {
        if (this.mBroadcastResource != null && app != null) {
            int uid = app.info.uid;
            String pkg = app.info.packageName;
            int processType = getProcessType(app);
            if (PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get(SYSTEM_DEBUGGABLE, PPPOEStateMachine.PHASE_DEAD)) || processType == 0) {
                this.mBroadcastResource.clear(uid, pkg, processType);
            }
        }
    }

    private int getProcessType(ProcessRecord app) {
        if ((app.info.flags & 1) != 0) {
            return 2;
        }
        return 0;
    }

    public boolean isKeyApp(int type, int value, String key) {
        if (this.mBroadcastResource == null || key == null || 1 != this.mBroadcastResource.queryPkgPolicy(type, value, key)) {
            return false;
        }
        if (Log.HWLog) {
            Log.i(TAG, "isKeyApp in whiteList key:" + key + " , type is " + type);
        }
        return true;
    }

    public AbsUserBehaviourRecord getRecordCust() {
        if (this.mCust == null) {
            this.mCust = new HwUserBehaviourRecord(this.mContext);
        }
        return this.mCust;
    }

    private void initAppResourceLocked() {
        if (this.mAppResource == null) {
            Log.i(TAG, "init Appresource");
            this.mAppResource = HwFrameworkFactory.getHwResource(19);
        }
    }

    private void initAppServiceResourceLocked() {
        if (this.mAppServiceResource == null) {
            Log.i(TAG, "init AppServiceResource");
            this.mAppServiceResource = HwFrameworkFactory.getHwResource(18);
        }
    }

    public void initAppAndAppServiceResourceLocked() {
        initAppResourceLocked();
        initAppServiceResourceLocked();
    }

    public boolean isAcquireAppServiceResourceLocked(ServiceRecord sr, ProcessRecord app) {
        if (this.mAppServiceResource == null || sr == null || sr.appInfo.uid <= 0 || sr.appInfo.packageName == null || sr.serviceInfo.name == null || 2 != this.mAppServiceResource.acquire(sr.appInfo.uid, sr.appInfo.packageName, getProcessType(app))) {
            return true;
        }
        Log.i(TAG, "Failed to acquire AppServiceResource:" + sr.serviceInfo.name + " of " + sr.appInfo.packageName + "/" + sr.appInfo.uid);
        return false;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        if (!(this.mAppResource == null || app == null || app.uid <= 0 || app.info == null || app.processName == null || app.startTime <= 0)) {
            int processType = ((app.info.flags & 1) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0;
            Bundle args = new Bundle();
            args.putInt("callingUid", app.uid);
            args.putString(HwGpsPowerTracker.DEL_PKG, app.processName);
            args.putLong("startTime", app.startTime);
            args.putInt("processType", processType);
            args.putBoolean("launchfromActivity", app.launchfromActivity);
            args.putBoolean("topProcess", isTopProcessLocked(app));
            if (2 == this.mAppResource.acquire(null, null, args)) {
                Log.i(TAG, "Failed to acquire AppResource:" + app.info.packageName + "/" + app.uid);
                return false;
            }
        }
        return true;
    }

    private void clearAppServiceResource(ProcessRecord app) {
        if (this.mAppServiceResource != null && app != null) {
            this.mAppServiceResource.clear(app.uid, app.info.packageName, getProcessType(app));
            Log.i(TAG, "clear AppServiceResource of " + app.info.packageName + "/" + app.uid);
        }
    }

    private void clearAppResource(ProcessRecord app) {
        if (this.mAppResource != null && app != null && app.uid > 0 && app.info != null && app.info.packageName != null) {
            int processType = ((app.info.flags & 1) != 0 && (app.info.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0;
            this.mAppResource.clear(app.uid, app.info.packageName, processType);
            Log.i(TAG, "clear Appresource of " + app.info.packageName + "/" + app.uid);
        }
    }

    public void clearAppAndAppServiceResource(ProcessRecord app) {
        clearAppServiceResource(app);
        clearAppResource(app);
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    protected int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        int i = 1;
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = 0;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(restore ? 1 : 0);
            _data.writeString(packageName);
            if (!isOnTop) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeString(reserved);
            b.transact(1002, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "setHeadsetRevertSequenceState transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    protected void registerCtrlSocketForMm(String processname, int pid) {
        if ((HwConnectivityService.MM_PUSH_NAME.equals(processname) || "com.tencent.mobileqq:MSF".equals(processname) || "com.huawei.parentcontrol.parent".equals(processname) || "com.huawei.parentcontrol".equals(processname) || "com.huawei.hidisk".equals(processname)) && PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.mm_socket_ctrl", PPPOEStateMachine.PHASE_DEAD))) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeString(processname);
                data.writeInt(pid);
                IBinder hwcs = ServiceManager.getService("connectivity");
                Log.d(TAG, "registerCtrlSocketForMm end ");
                hwcs.transact(1009, data, reply, 0);
            } catch (Exception e) {
                Flog.e(100, "registerCtrlSocketForMm exception: " + e.toString());
            }
        }
    }

    protected void unregisterCtrlSocketForMm(String processname) {
        if ((HwConnectivityService.MM_PUSH_NAME.equals(processname) || "com.tencent.mobileqq:MSF".equals(processname) || "com.huawei.parentcontrol.parent".equals(processname) || "com.huawei.parentcontrol".equals(processname) || "com.huawei.hidisk".equals(processname)) && PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("ro.config.mm_socket_ctrl", PPPOEStateMachine.PHASE_DEAD))) {
            try {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeString(processname);
                IBinder hwcs = ServiceManager.getService("connectivity");
                Log.d(TAG, "unregisterCtrlSocketForMm end ");
                hwcs.transact(1008, data, reply, 0);
            } catch (Exception e) {
                Flog.e(100, "unregisterCtrlSocketForMm exception: " + e.toString());
            }
        }
    }

    public void trimGLMemory(int level) {
        Slog.i(TAG, " trimGLMemory begin ");
        synchronized (this.mLruProcesses) {
            for (int i = 0; i < this.mLruProcesses.size(); i++) {
                ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                if (app.thread != null) {
                    try {
                        app.thread.scheduleTrimMemory(level);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        Slog.i(TAG, " trimGLMemory end ");
    }

    public void setWindowManager(WindowManagerService wm) {
        super.setWindowManager(wm);
        wm.registerPointerEventListener(new IawarePointerEventListener());
    }

    protected void noteActivityStart(String packageName, String processName, int pid, int uid, boolean started) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mSystemReady) {
            int event;
            Apps.Builder builder = Apps.builder();
            if (started) {
                event = 15005;
            } else {
                event = 85005;
            }
            builder.addEvent(event);
            builder.addCalledApp(packageName, processName, pid, uid);
            CollectData appsData = builder.build();
            long id = Binder.clearCallingIdentity();
            resManager.reportData(appsData);
            Binder.restoreCallingIdentity(id);
        }
    }

    protected void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null) {
            resManager.noteProcessStart(packageName, processName, pid, uid, started, launcherMode, reason);
            if (resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP)) && this.mSystemReady) {
                int event;
                Apps.Builder builder = Apps.builder();
                if (started) {
                    event = 15001;
                } else {
                    event = 85001;
                }
                builder.addEvent(event);
                builder.addLaunchCalledApp(packageName, processName, launcherMode, reason, pid, uid);
                CollectData appsData = builder.build();
                long id = Binder.clearCallingIdentity();
                resManager.reportData(appsData);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public boolean getProcessRecordFromMTM(ProcessInfo procInfo) {
        if (procInfo == null) {
            Slog.e(TAG, "getProcessRecordFromMTM procInfo is null");
            return false;
        }
        synchronized (this) {
            synchronized (this.mPidsSelfLocked) {
                if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                    Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                    return false;
                }
                ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
                if (proc == null) {
                    Slog.e(TAG, "getProcessRecordFromMTM process info is null ,mUid :" + procInfo.mPid);
                    return false;
                }
                boolean z;
                if (procInfo.mType == 0) {
                    procInfo.mType = getAppType(procInfo.mPid, proc.info);
                }
                procInfo.mProcessName = proc.processName;
                procInfo.mCurSchedGroup = proc.curSchedGroup;
                procInfo.mCurAdj = proc.curAdj;
                procInfo.mAdjType = proc.adjType;
                procInfo.mForegroundActivities = proc.foregroundActivities;
                procInfo.mForegroundServices = proc.foregroundServices;
                if (proc.forcingToForeground != null) {
                    z = true;
                } else {
                    z = false;
                }
                procInfo.mForceToForeground = z;
                if (procInfo.mPackageName.size() == 0) {
                    int list_size = proc.pkgList.size();
                    for (int i = 0; i < list_size; i++) {
                        String packagename = (String) proc.pkgList.keyAt(i);
                        if (!procInfo.mPackageName.contains(packagename)) {
                            procInfo.mPackageName.add(packagename);
                        }
                    }
                }
                procInfo.mLru = this.mLruProcesses.lastIndexOf(proc);
                return true;
            }
        }
    }

    public Map<Integer, AwareProcessBaseInfo> getAllProcessBaseInfo() {
        ArrayMap<Integer, AwareProcessBaseInfo> list = new ArrayMap();
        synchronized (this.mPidsSelfLocked) {
            for (int i = 0; i < this.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                baseInfo.mCurAdj = p.curAdj;
                baseInfo.mForegroundActivities = p.foregroundActivities;
                baseInfo.mAdjType = p.adjType;
                baseInfo.mHasShownUi = p.hasShownUi;
                baseInfo.mUid = p.uid;
                list.put(Integer.valueOf(p.pid), baseInfo);
            }
        }
        return list;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AwareProcessBaseInfo getProcessBaseInfo(int pid) {
        Throwable th;
        synchronized (this.mPidsSelfLocked) {
            try {
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                try {
                    baseInfo.mCurAdj = 1001;
                    ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.get(pid);
                    if (p != null) {
                        baseInfo.mUid = p.uid;
                        baseInfo.mCurAdj = p.curAdj;
                        baseInfo.mAdjType = p.adjType;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice) {
        if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
            Slog.e(TAG, "killProcessRecordFromMTM it is failed to get process record ,mUid :" + procInfo.mUid);
            return false;
        }
        synchronized (this.mPidsSelfLocked) {
            int adj = procInfo.mCurAdj;
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromMTM this process has been killed or died before  :" + procInfo.mProcessName);
                return false;
            }
            synchronized (this) {
                removeProcessLocked(proc, false, restartservice, "MTM(adj:" + adj + "," + proc.curAdj + ")");
            }
            return true;
        }
    }

    private int getAppType(int pid, ApplicationInfo info) {
        if (info == null) {
            Slog.e(TAG, "getAppType app info is null");
            return 0;
        }
        int flags = info.flags;
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(info)).intValue();
            if (!((flags & 1) == 0 || (100663296 & hwFlags) == 0)) {
                return 3;
            }
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "getAppType exception: ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            Slog.e(TAG, "getAppType exception: NoSuchFieldException");
        } catch (IllegalArgumentException e3) {
            Slog.e(TAG, "getAppType exception: IllegalArgumentException");
        } catch (IllegalAccessException e4) {
            Slog.e(TAG, "getAppType exception: IllegalAccessException");
        } catch (Exception e5) {
            Slog.e(TAG, "getAppType exception: Exception");
        }
        if (pid == Process.myPid()) {
            return 1;
        }
        if ((flags & 1) != 0) {
            return 2;
        }
        return 4;
    }

    public ArrayList getAMSLru() {
        return this.mLruProcesses;
    }

    public int getAMSLruBypid(int pid) {
        synchronized (this) {
            int size = this.mLruProcesses.size();
            for (int i = 0; i < size; i++) {
                if (((ProcessRecord) this.mLruProcesses.get(i)).pid == pid) {
                    return i;
                }
            }
            return -1;
        }
    }

    public void printLRU(PrintWriter pw) {
        pw.println("  LRU :");
        synchronized (this) {
            int size = this.mLruProcesses.size();
            for (int i = 0; i < size; i++) {
                pw.println("  process " + i + ":" + ((ProcessRecord) this.mLruProcesses.get(i)).processName);
            }
        }
    }

    protected void notifyProcessGroupChange(int pid, int uid) {
        MultiTaskManager handler = MultiTaskManager.getInstance();
        if (handler != null) {
            handler.notifyProcessGroupChange(pid, uid);
        }
    }

    protected void notifyProcessGroupChange(int pid, int uid, int grp) {
        CPUKeyBackground.getInstance().notifyProcessGroupChange(pid, uid, grp);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasDeps(ProcessInfo procInfo, String packageName) {
        if (packageName == null || procInfo == null) {
            Slog.e(TAG, "hasDeps packageName == null || procInfo == null");
            return false;
        }
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "getProcessRecordFromMTM it is failed to get process record ,mPid :" + procInfo.mPid);
                return false;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "hasDeps proc == null");
                return false;
            }
            boolean contains = proc.pkgDeps != null ? proc.pkgDeps.contains(packageName) : false;
        }
    }

    public boolean switchUser(int userId) {
        boolean isStorageLow = false;
        try {
            isStorageLow = AppGlobals.getPackageManager().isStorageLow();
        } catch (RemoteException e) {
            Slog.e(TAG, "check low storage error because e: " + e);
        }
        if (isStorageLow) {
            UiThread.getHandler().post(new Runnable() {
                public void run() {
                    Toast toast = Toast.makeText(HwActivityManagerService.this.mContext, HwActivityManagerService.this.mContext.getResources().getString(17040234), 1);
                    toast.getWindowParams().type = 2006;
                    toast.show();
                }
            });
            return false;
        }
        UserInfo targetUser = this.mUserController.getUserInfo(userId);
        if (!targetUser.isGuest()) {
            return super.switchUser(userId);
        }
        this.mHandler.removeMessages(50);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(50, userId, 0, targetUser.name));
        return true;
    }

    private void addBootFailedLog() {
        ArrayList<Integer> pids = new ArrayList();
        pids.add(Integer.valueOf(Process.myPid()));
        int bootErrorNo = 83886081;
        if (this.mBgBroadcastQueue.mOrderedBroadcasts.size() > 0) {
            BroadcastRecord br = (BroadcastRecord) this.mBgBroadcastQueue.mOrderedBroadcasts.get(0);
            if (!(br == null || br.intent == null || !"android.intent.action.PRE_BOOT_COMPLETED".equals(br.intent.getAction()))) {
                HwBootCheck.addBootInfo("currReceiver is: " + br.curComponent.toShortString());
                pids.add(Integer.valueOf(br.curApp.pid));
                bootErrorNo = 83886082;
            }
        }
        File stack = dumpStackTraces(true, pids, null, null, Watchdog.NATIVE_STACKS_OF_INTEREST);
        if (stack == null) {
            if (ActivityManagerDebugConfig.HWFLOW) {
                Slog.i("ActivityManager_FLOW", "addBootFailedLog dumpStackTraces fail");
            }
            Process.sendSignal(Process.myPid(), 3);
        }
        Watchdog.getInstance().addKernelLog();
        HwBootCheck.addBootInfo(this.mSystemServiceManager.dumpInfo());
        SystemClock.sleep(2000);
        HwBootFail.bootFailError(bootErrorNo, 1, HwBootFail.creatFrameworkBootFailLog(stack, HwBootCheck.getBootInfo()));
    }

    public boolean bootSceneStart(int sceneId, long maxTime) {
        try {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            } else if (HwBootCheck.getHandlerThread().isAlive()) {
                if (ActivityManagerDebugConfig.HWFLOW) {
                    Slog.i("ActivityManager_FLOW", "bootSceneStart :" + sceneId);
                }
                if (!this.mBootCheckHandler.hasMessages(sceneId)) {
                    this.mBootCheckHandler.sendEmptyMessageDelayed(sceneId, maxTime);
                }
                return true;
            } else {
                if (ActivityManagerDebugConfig.HWFLOW) {
                    Slog.w("ActivityManager_FLOW", "mBootCheckThread is not alive");
                }
                return false;
            }
        } catch (Exception ex) {
            Flog.e(100, "bootSceneStart exception: " + ex.toString());
            return false;
        }
    }

    public boolean bootSceneEnd(int sceneId) {
        try {
            if (1000 != Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            }
            if (ActivityManagerDebugConfig.HWFLOW) {
                Slog.i("ActivityManager_FLOW", "bootSceneEnd :" + sceneId);
            }
            if (this.mBootCheckHandler.hasMessages(sceneId)) {
                this.mBootCheckHandler.removeMessages(sceneId);
            }
            return true;
        } catch (Exception ex) {
            Flog.e(100, "bootSceneEnd exception: " + ex.toString());
            return false;
        }
    }

    private void sendMessageToSwitchUser(int userId, String userName) {
        UserInfo mCurrentUserInfo = this.mUserController.getUserInfo(this.mUserController.getCurrentUserIdLocked());
        int targetUserId = userId;
        UserInfo mTargetUserInfo = this.mUserController.getUserInfo(userId);
        this.mUserController.setTargetUserIdLocked(userId);
        Pair<UserInfo, UserInfo> userNames = new Pair(mCurrentUserInfo, mTargetUserInfo);
        this.mHandler.removeMessages(SHOW_SWITCH_DIALOG_MSG);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SHOW_SWITCH_DIALOG_MSG, userNames));
    }

    private void showGuestSwitchDialog(int userId, String userName) {
        cancelDialog();
        ContentResolver cr = this.mContext.getContentResolver();
        int notFirstLogin = System.getIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, 0, userId);
        Slog.i(TAG, "notFirstLogin:" + notFirstLogin + ", userid=" + userId);
        if (notFirstLogin != 0) {
            showGuestResetSessionDialog(userId);
            return;
        }
        System.putIntForUser(cr, SETTING_GUEST_HAS_LOGGED_IN, 1, userId);
        sendMessageToSwitchUser(userId, userName);
    }

    private void showGuestResetSessionDialog(int guestId) {
        this.mNewSessionDialog = new ResetSessionDialog(this.mContext, guestId);
        this.mNewSessionDialog.show();
        LayoutParams lp = this.mNewSessionDialog.getWindow().getAttributes();
        lp.width = -1;
        this.mNewSessionDialog.getWindow().setAttributes(lp);
    }

    private void wipeGuestSession(int userId) {
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager.markGuestForDeletion(userId)) {
            UserInfo newGuest = userManager.createGuest(this.mContext, getGuestName());
            if (newGuest == null) {
                Slog.e(TAG, "Could not create new guest, switching back to owner");
                sendMessageToSwitchUser(0, getUserName(0));
                userManager.removeUser(userId);
                return;
            }
            Slog.d(TAG, "Create new guest, switching to = " + newGuest.id);
            sendMessageToSwitchUser(newGuest.id, newGuest.name);
            System.putIntForUser(this.mContext.getContentResolver(), SETTING_GUEST_HAS_LOGGED_IN, 1, newGuest.id);
            userManager.removeUser(userId);
            return;
        }
        Slog.w(TAG, "Couldn't mark the guest for deletion for user " + userId);
    }

    private String getUserName(int userId) {
        if (this.mUserController == null) {
            return null;
        }
        UserInfo info = this.mUserController.getUserInfo(userId);
        if (info == null) {
            return null;
        }
        return info.name;
    }

    private String getGuestName() {
        return this.mContext.getString(33685837);
    }

    private void cancelDialog() {
        if (this.mNewSessionDialog != null && this.mNewSessionDialog.isShowing()) {
            this.mNewSessionDialog.cancel();
            this.mNewSessionDialog = null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void reportServiceRelationIAware(int relationType, ServiceRecord r, ProcessRecord caller) {
        if (r != null && caller != null && r.name != null && r.appInfo != null && caller.uid != r.appInfo.uid) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                Bundle bundleArgs = new Bundle();
                int callerUid = caller.uid;
                int callerPid = caller.pid;
                String callerProcessName = caller.processName;
                int targetUid = r.appInfo.uid;
                String targetProcessName = r.processName;
                String compName = r.name.flattenToShortString();
                bundleArgs.putInt("callPid", callerPid);
                bundleArgs.putInt("callUid", callerUid);
                bundleArgs.putString("callProcName", callerProcessName);
                bundleArgs.putInt("tgtUid", targetUid);
                bundleArgs.putString("tgtProcName", targetProcessName);
                bundleArgs.putString("compName", compName);
                bundleArgs.putInt("relationType", relationType);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
        if (caller != null && r != null && r.info != null && r.name != null && caller.uid != r.uid) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                if (r.proc != null) {
                    synchronized (this.mAssocMap) {
                        ArrayMap<Integer, Long> pids = (ArrayMap) this.mAssocMap.get(Integer.valueOf(caller.pid));
                        if (pids != null) {
                            Long elaseTime = (Long) pids.get(Integer.valueOf(r.proc.pid));
                            if (elaseTime == null) {
                                pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                            } else if (SystemClock.elapsedRealtime() - elaseTime.longValue() < AppHibernateCst.DELAY_ONE_MINS) {
                                return;
                            }
                        }
                        pids = new ArrayMap();
                        pids.put(Integer.valueOf(r.proc.pid), Long.valueOf(SystemClock.elapsedRealtime()));
                        this.mAssocMap.put(Integer.valueOf(caller.pid), pids);
                    }
                }
                Bundle bundleArgs = new Bundle();
                int callerUid = caller.uid;
                int callerPid = caller.pid;
                String callerProcessName = caller.processName;
                int targetUid = r.uid;
                String targetProcessName = r.info.processName;
                String compName = r.name.flattenToShortString();
                bundleArgs.putInt("callPid", callerPid);
                bundleArgs.putInt("callUid", callerUid);
                bundleArgs.putString("callProcName", callerProcessName);
                bundleArgs.putInt("tgtUid", targetUid);
                bundleArgs.putString("tgtProcName", targetProcessName);
                bundleArgs.putString("compName", compName);
                bundleArgs.putInt("relationType", relationType);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    protected void reportPreviousInfo(int relationType, ProcessRecord prevProc) {
        if (prevProc != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                int prevPid = prevProc.pid;
                int prevUid = prevProc.uid;
                Bundle bundleArgs = new Bundle();
                bundleArgs.putInt("pid", prevPid);
                bundleArgs.putInt("tgtUid", prevUid);
                bundleArgs.putInt("relationType", relationType);
                CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
                long id = Binder.clearCallingIdentity();
                resManager.reportData(data);
                Binder.restoreCallingIdentity(id);
            }
        }
    }

    public void reportProcessDied(int pid) {
        synchronized (this.mAssocMap) {
            this.mAssocMap.remove(Integer.valueOf(pid));
            Iterator<Entry<Integer, ArrayMap<Integer, Long>>> it = this.mAssocMap.entrySet().iterator();
            while (it.hasNext()) {
                ArrayMap<Integer, Long> pids = (ArrayMap) ((Entry) it.next()).getValue();
                pids.remove(Integer.valueOf(pid));
                if (pids.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    public void reportAssocDisable() {
        synchronized (this.mAssocMap) {
            this.mAssocMap.clear();
        }
    }

    public void reportAssocEnable(ArrayMap<Integer, Integer> forePids) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC)) && forePids != null) {
            synchronized (this) {
                for (ProcessRecord proc : this.mLruProcesses) {
                    if (proc != null) {
                        if (proc.foregroundActivities) {
                            forePids.put(Integer.valueOf(proc.pid), Integer.valueOf(proc.uid));
                        }
                        ArrayList<String> pkgs = new ArrayList();
                        int size = proc.pkgList.size();
                        for (int i = 0; i < size; i++) {
                            String pkg = (String) proc.pkgList.keyAt(i);
                            if (!pkgs.contains(pkg)) {
                                pkgs.add(pkg);
                            }
                        }
                        Bundle args = new Bundle();
                        args.putInt("callPid", proc.pid);
                        args.putInt("callUid", proc.uid);
                        args.putString("callProcName", proc.processName);
                        args.putInt("userid", proc.userId);
                        args.putStringArrayList("pkgname", pkgs);
                        args.putInt("relationType", 4);
                        HwSysResManager.getInstance().reportData(new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
                        for (ConnectionRecord cr : proc.connections) {
                            if (!(cr == null || cr.binding == null)) {
                                reportServiceRelationIAware(1, cr.binding.service, proc);
                            }
                        }
                        for (ContentProviderConnection cpc : proc.conProviders) {
                            if (cpc != null) {
                                reportServiceRelationIAware(2, cpc.provider, proc);
                            }
                        }
                    }
                }
                reportHomeProcess(this.mHomeProcess);
            }
        }
    }

    protected void reportHomeProcess(ProcessRecord homeProc) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
            int pid = 0;
            int uid = 0;
            ArrayList<String> pkgs = new ArrayList();
            if (homeProc != null) {
                try {
                    pid = homeProc.pid;
                    uid = homeProc.uid;
                    for (String pkg : homeProc.pkgList.keySet()) {
                        if (!pkgs.contains(pkg)) {
                            pkgs.add(pkg);
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    Slog.i(TAG, "reportHomeProcess error happened.");
                }
            }
            Bundle bundleArgs = new Bundle();
            bundleArgs.putInt("pid", pid);
            bundleArgs.putInt("tgtUid", uid);
            bundleArgs.putStringArrayList("pkgname", pkgs);
            bundleArgs.putInt("relationType", 11);
            CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), bundleArgs);
            long origId = Binder.clearCallingIdentity();
            HwSysResManager.getInstance().reportData(data);
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void setPackageStoppedState(List<String> packageList, boolean stopped) {
        if (packageList != null) {
            int userId = UserHandle.myUserId();
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                synchronized (this) {
                    for (String packageName : packageList) {
                        pm.setPackageStoppedState(packageName, stopped, userId);
                    }
                }
            } catch (RemoteException e) {
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e2);
            }
        }
    }

    public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice) {
        return killProcessRecordFromIAware(procInfo, restartservice, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous) {
        synchronized (this.mPidsSelfLocked) {
            if (procInfo.mPid == MY_PID || procInfo.mPid < 0) {
                Slog.e(TAG, "killProcessRecordFromIAware it is failed to get process record ,mUid :" + procInfo.mUid);
                return false;
            }
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(procInfo.mPid);
            if (proc == null) {
                Slog.e(TAG, "killProcessRecordFromIAware this process has been killed or died before  :" + procInfo.mProcessName);
                return false;
            } else if (proc.curAdj >= 200 || AwareAppMngSort.EXEC_SERVICES.equals(proc.adjType)) {
                String killedProcessName = proc.processName;
                int killedAppUserId = proc.userId;
            } else {
                Slog.e(TAG, "killProcessRecordFromIAware process cleaner kill process: adj changed, new adj:" + proc.curAdj + ", old adj:" + procInfo.mCurAdj + ", pid:" + procInfo.mPid + ", uid:" + procInfo.mUid + ", " + procInfo.mProcessName);
                return false;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean canCleanTaskRecord(String packageName) {
        if (packageName == null) {
            return true;
        }
        synchronized (this) {
            ArrayList<TaskRecord> recentTasks = getRecentTasks();
            if (recentTasks == null) {
                return true;
            }
            int foundNum = 0;
            for (int i = 0; i < recentTasks.size() && foundNum < 1; i++) {
                TaskRecord tr = (TaskRecord) recentTasks.get(i);
                if (!(tr == null || tr.mActivities == null)) {
                    if (!(tr.mActivities.size() <= 0 || tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null)) {
                        if (packageName.equals(tr.getBaseIntent().getComponent().getPackageName())) {
                            return false;
                        } else if (AwareAppMngSort.ACTIVITY_RECENT_TASK.equals(tr.getBaseIntent().getComponent().flattenToShortString())) {
                        }
                    }
                    foundNum++;
                }
            }
        }
    }

    public void cleanActivityByUid(List<String> packageList, int targetUid) {
        synchronized (this) {
            int userId = UserHandle.getUserId(targetUid);
            for (String packageName : packageList) {
                if (canCleanTaskRecord(packageName)) {
                    this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, false, userId);
                }
            }
        }
    }

    public int numOfPidWithActivity(int uid) {
        int count = 0;
        synchronized (this.mPidsSelfLocked) {
            for (int i = 0; i < this.mPidsSelfLocked.size(); i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid && p.hasShownUi) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean cleanPackageRes(List<String> packageList, int targetUid, boolean cleanAlarm) {
        if (packageList == null) {
            return false;
        }
        boolean didSomething = false;
        int userId = UserHandle.getUserId(targetUid);
        synchronized (this) {
            for (String packageName : packageList) {
                int i;
                if (canCleanTaskRecord(packageName) && this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, false, userId)) {
                    didSomething = true;
                }
                if (this.mServices.bringDownDisabledPackageServicesLocked(packageName, null, userId, false, true, true)) {
                    didSomething = true;
                }
                if (packageName == null) {
                    this.mStickyBroadcasts.remove(userId);
                }
                ArrayList<ContentProviderRecord> providers = new ArrayList();
                if (this.mProviderMap.collectPackageProvidersLocked(packageName, null, true, false, userId, providers)) {
                    didSomething = true;
                }
                ArrayList<ContentProviderRecord> providersForClone = new ArrayList();
                this.mProviderMapForClone.collectPackageProvidersLocked(packageName, null, true, false, userId, providersForClone);
                providers.addAll(providersForClone);
                for (i = providers.size() - 1; i >= 0; i--) {
                    cleanProviderLocked(null, (ContentProviderRecord) providers.get(i), true);
                }
                for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
                    didSomething |= this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, null, userId, true);
                }
                if (cleanAlarm && this.mAlms != null) {
                    this.mAlms.removePackageAlarm(packageName);
                }
            }
        }
        return didSomething;
    }

    private final boolean cleanProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
        boolean inLaunching = this.mLaunchingProviders.contains(cpr);
        if (!inLaunching || always) {
            synchronized (cpr) {
                cpr.launchingApp = null;
                cpr.notifyAll();
            }
            ProviderMap providerMap = this.mProviderMap;
            if (cpr.info.applicationInfo.euid != 0) {
                providerMap = this.mProviderMapForClone;
            }
            providerMap.removeProviderByClass(cpr.name, UserHandle.getUserId(cpr.uid));
            String[] names = cpr.info.authority.split(";");
            for (String removeProviderByName : names) {
                providerMap.removeProviderByName(removeProviderByName, UserHandle.getUserId(cpr.uid));
            }
        }
        for (int i = cpr.connections.size() - 1; i >= 0; i--) {
            ContentProviderConnection conn = (ContentProviderConnection) cpr.connections.get(i);
            if (!conn.waiting || !inLaunching || always) {
                ProcessRecord capp = conn.client;
                conn.dead = true;
                if (conn.stableCount > 0) {
                    if (!(capp.persistent || capp.thread == null || capp.pid == 0 || capp.pid == MY_PID)) {
                        capp.kill("depends on provider " + cpr.name.flattenToShortString() + " in dying proc " + (proc != null ? proc.processName : "??"), true);
                    }
                } else if (!(capp.thread == null || conn.provider.provider == null)) {
                    try {
                        capp.thread.unstableProviderDied(conn.provider.provider.asBinder());
                    } catch (RemoteException e) {
                    }
                    cpr.connections.remove(i);
                    if (conn.client.conProviders.remove(conn)) {
                        stopAssociationLocked(capp.uid, capp.processName, cpr.uid, cpr.name);
                    }
                    if (!(proc == null || proc.uid < 10000 || capp.pid == proc.pid || capp.info == null || proc.info == null || capp.info.packageName == null || capp.info.packageName.equals(proc.info.packageName))) {
                        LogPower.push(167, proc.processName, Integer.toString(capp.pid), Integer.toString(proc.pid), new String[]{"provider"});
                    }
                }
            }
        }
        if (inLaunching && always) {
            this.mLaunchingProviders.remove(cpr);
        }
        return inLaunching;
    }

    public void setAlarmManagerExt(AlarmManagerService service) {
        synchronized (this) {
            this.mAlms = service;
        }
    }

    protected void cleanupAlarmLockedExt(ProcessRecord process) {
        if (!isThirdParty(process)) {
            return;
        }
        if (this.mAlms == null) {
            Log.w(TAG, "Could not get instance of AlarmManagerService.");
            return;
        }
        ArrayList<String> array = new ArrayList();
        for (String pkg : process.pkgList.keySet()) {
            array.add(pkg);
        }
        if (array.size() > 0) {
            this.mAlms.cleanupAlarmLocked(array);
        }
    }

    private static boolean isThirdParty(ProcessRecord process) {
        if (process == null || process.pid == ActivityManagerService.MY_PID || (process.info.flags & 1) != 0) {
            return false;
        }
        return true;
    }

    protected void forceValidateHomeButton() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0 || Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 0) {
            Global.putInt(this.mContext.getContentResolver(), "device_provisioned", 1);
            Secure.putInt(this.mContext.getContentResolver(), "user_setup_complete", 1);
            Log.w(TAG, "DEVICE_PROVISIONED or USER_SETUP_COMPLETE set 0 to 1!");
        }
    }

    protected boolean isStartLauncherActivity(Intent intent) {
        if (intent == null) {
            Log.w(TAG, "intent is null, not start launcher!");
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        ComponentName cmp = intent.getComponent();
        if (pm != null && intent.hasCategory("android.intent.category.HOME")) {
            for (ResolveInfo info : pm.queryIntentActivities(mainIntent, 0)) {
                if (info != null && info.priority == 0 && cmp != null && info.activityInfo != null && cmp.getPackageName().equals(info.activityInfo.packageName)) {
                    Log.d(TAG, "info priority is 0, cmp: " + cmp);
                    return true;
                }
            }
        }
        return false;
    }

    private void deleteClonedPackage(String packageName) {
        int i;
        int appId = -1;
        try {
            appId = UserHandle.getAppId(AppGlobals.getPackageManager().getPackageUid(packageName, 268435456, 0));
        } catch (RemoteException e) {
        }
        killPackageProcessesLocked(packageName, appId, 0, -10000, false, true, true, false, "stop " + packageName + "delete cloned app");
        this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, false, 2147383647);
        this.mServices.bringDownDisabledPackageServicesLocked(packageName, null, 2147383647, false, true, true);
        ArrayList<ContentProviderRecord> providersForClone = new ArrayList();
        this.mProviderMapForClone.collectPackageProvidersLocked(packageName, null, true, false, 0, providersForClone);
        for (i = providersForClone.size() - 1; i >= 0; i--) {
            removeDyingProviderLocked(null, (ContentProviderRecord) providersForClone.get(i), true);
        }
        for (i = this.mBroadcastQueues.length - 1; i >= 0; i--) {
            this.mBroadcastQueues[i].cleanupDisabledPackageReceiversLocked(packageName, null, 2147383647, true);
        }
        if (this.mBooted) {
            this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            this.mStackSupervisor.scheduleIdleLocked();
        }
    }

    protected List<ResolveInfo> queryIntentReceivers(ProcessRecord callerApp, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        List<ResolveInfo> list;
        StringBuilder append;
        int i = 0;
        if (callerApp == null || callerApp.info.euid == 0) {
            if ((intent.getHwFlags() & 1) != 0) {
            }
            list = AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, flags, userId).getList();
            append = new StringBuilder().append("collectReceiverComponents, callerApp: ").append(callerApp).append(", intent:").append(intent.getAction()).append(", receiver size:");
            if (list != null) {
                i = list.size();
            }
            Flog.i(104, append.append(i).append(", flags: ").append(Integer.toHexString(flags)).toString());
            return list;
        }
        flags |= 4194304;
        intent.addHwFlags(1);
        list = AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, flags, userId).getList();
        append = new StringBuilder().append("collectReceiverComponents, callerApp: ").append(callerApp).append(", intent:").append(intent.getAction()).append(", receiver size:");
        if (list != null) {
            i = list.size();
        }
        Flog.i(104, append.append(i).append(", flags: ").append(Integer.toHexString(flags)).toString());
        return list;
    }

    public IIntentSender getIntentSender(int type, String packageName, IBinder token, String resultWho, int requestCode, Intent[] intents, String[] resolvedTypes, int flags, Bundle options, int userId) {
        if (intents != null && intents.length > 0) {
            boolean isClonedProcess = isClonedProcess(Binder.getCallingPid());
            boolean isPackageCloned = isPackageCloned(getPackageNameForPid(Binder.getCallingPid()), userId);
            for (int i = 0; i < intents.length; i++) {
                if (intents[i] != null) {
                    if (isClonedProcess) {
                        intents[i].addHwFlags(1);
                    }
                    if (isPackageCloned) {
                        intents[i].addHwFlags(32);
                    }
                }
            }
        }
        return super.getIntentSender(type, packageName, token, resultWho, requestCode, intents, resolvedTypes, flags, options, userId);
    }

    protected void filterRegisterReceiversForEuid(List<BroadcastFilter> registeredReceivers, ProcessRecord callerApp) {
        if (registeredReceivers != null && callerApp != null) {
            Iterator<BroadcastFilter> item = registeredReceivers.iterator();
            while (item.hasNext()) {
                BroadcastFilter filter = (BroadcastFilter) item.next();
                if (!(filter.receiverList == null || filter.receiverList.app == null || !filter.receiverList.app.info.packageName.equals(callerApp.info.packageName) || filter.receiverList.app.info.euid == callerApp.info.euid)) {
                    Slog.d(TAG, "prevent start receiver of package " + filter.receiverList.app.info.packageName + " because euid is different" + "  callerApp euid is " + callerApp.info.euid);
                    item.remove();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isClonedProcess(int pid) {
        if (!IS_SUPPORT_CLONE_APP) {
            return false;
        }
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc == null || proc.info.euid == 0) {
            } else {
                Flog.i(100, "ProcessRecord " + proc + " is a cloned process");
                return true;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getPackageNameForPid(int pid) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc != null) {
                String str = proc.info != null ? proc.info.packageName : "android";
            } else {
                Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
                return null;
            }
        }
    }

    public boolean isPackageCloned(String packageName, int userId) {
        if (!IS_SUPPORT_CLONE_APP || packageName == null || packageName.trim().isEmpty() || this.mCloneAppList == null || this.mCloneAppList.trim().isEmpty() || userId != 0) {
            return false;
        }
        boolean isPackageCloned = (this.mCloneAppList + ";").contains(packageName + ";");
        if (isPackageCloned) {
            Flog.i(100, "App " + packageName + " cloned: " + isPackageCloned);
        }
        return isPackageCloned;
    }

    public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lRegistered = false;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lRegistered = this.mThirdPartyCallbackList.register(aCallBackHandler);
            }
        }
        return lRegistered;
    }

    public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) {
        boolean lUnregistered = false;
        if (aCallBackHandler != null) {
            synchronized (this.mThirdPartyCallbackList) {
                lUnregistered = this.mThirdPartyCallbackList.unregister(aCallBackHandler);
            }
        }
        return lUnregistered;
    }

    public boolean isInMultiWindowMode() {
        boolean z = false;
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                ActivityStack focusedStack = getFocusedStack();
                if (focusedStack == null) {
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                ActivityRecord top = focusedStack.topRunningActivityLocked();
                if (top == null) {
                    Binder.restoreCallingIdentity(origId);
                    return false;
                }
                if (!top.task.mFullscreen) {
                    z = true;
                }
                Binder.restoreCallingIdentity(origId);
                return z;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        synchronized (this.mThirdPartyCallbackList) {
            try {
                int i = this.mThirdPartyCallbackList.beginBroadcast();
                Flog.i(100, "onMultiWindowModeChanged : mThirdPartyCallbackList size : " + i);
                while (i > 0) {
                    i--;
                    try {
                        ((IMWThirdpartyCallback) this.mThirdPartyCallbackList.getBroadcastItem(i)).onModeChanged(isInMultiWindowMode);
                    } catch (RemoteException e) {
                        Flog.e(100, "Error in sending the Callback");
                    }
                }
                this.mThirdPartyCallbackList.finishBroadcast();
            } catch (IllegalStateException e2) {
                Flog.e(100, "beginBroadcast() called while already in a broadcast");
            }
        }
        return;
    }

    public void cleanPackageNotifications(List<String> packageList, int targetUid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                int userId = UserHandle.getUserId(targetUid);
                try {
                    Slog.v(TAG, "cleanupPackageNotifications, userId=" + userId + "|" + packageList);
                    for (String packageName : packageList) {
                        service.cancelAllNotifications(packageName, userId);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    public void cleanNotificationWithPid(List<String> packageList, int targetUid, int pid) {
        if (packageList != null) {
            INotificationManager service = NotificationManager.getService();
            if (service != null) {
                try {
                    StatusBarNotification[] notifications = service.getActiveNotifications("android");
                    int userId = UserHandle.getUserId(targetUid);
                    if (notifications != null) {
                        for (StatusBarNotification notification : notifications) {
                            if (notification.getInitialPid() == pid) {
                                for (String packageName : packageList) {
                                    service.cancelNotificationWithTag(packageName, notification.getTag(), notification.getId(), userId);
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Unable to talk to notification manager. Woe!");
                }
            }
        }
    }

    public boolean hasNotification(int pid) {
        if (pid < 0) {
            return false;
        }
        INotificationManager service = NotificationManager.getService();
        if (service == null) {
            return false;
        }
        try {
            StatusBarNotification[] notifications = service.getActiveNotifications("android");
            if (notifications == null) {
                return false;
            }
            for (StatusBarNotification notification : notifications) {
                if (notification.getInitialPid() == pid) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to talk to notification manager. Woe!");
        }
    }

    public boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if ("com.huawei.android.launcher".equals(packageName)) {
            return true;
        }
        if (this.mContext != null) {
            List<ResolveInfo> outActivities = new ArrayList();
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                ComponentName componentName = pm.getHomeActivities(outActivities);
                if (componentName != null && componentName.getPackageName() != null) {
                    return packageName.equals(componentName.getPackageName());
                } else {
                    for (ResolveInfo info : outActivities) {
                        String homePkg = info.activityInfo.packageName;
                        if (packageName.equals(homePkg)) {
                            Slog.d(TAG, "homePkg is " + homePkg + " ,isLauncher");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    int broadcastIntentInPackage(String packageName, int uid, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String requiredPermission, Bundle options, boolean serialized, boolean sticky, int userId) {
        int broadcastIntentInPackage;
        synchronized (this) {
            if (!(packageName == null || options == null)) {
                if (options.getBoolean("fromSystemUI")) {
                    Slog.d(TAG, "packageName: " + packageName + ", uid: " + uid + ", resolvedType: " + resolvedType + ", resultCode: " + resultCode + ", requiredPermission: " + requiredPermission + ", userId: " + userId);
                    try {
                        AppGlobals.getPackageManager().setPackageStoppedState(packageName, false, UserHandle.getUserId(uid));
                    } catch (RemoteException e) {
                        Slog.w(TAG, "Failed trying to unstop " + packageName + " due to  RemoteException");
                    } catch (IllegalArgumentException e2) {
                        Slog.w(TAG, "Failed trying to unstop package " + packageName + " due to IllegalArgumentException");
                    }
                }
            }
            broadcastIntentInPackage = super.broadcastIntentInPackage(packageName, uid, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermission, options, serialized, sticky, userId);
        }
        return broadcastIntentInPackage;
    }

    private void restartAndNotifyPkgForRog(ActivityStack stack, IHwRogListener listener, int eventType, Object param) {
        try {
            String packageName = listener.getPackageName();
            ActivityRecord starting = stack.restartPackage(packageName);
            switch (eventType) {
                case 1:
                    UpdateRog updateRog = (UpdateRog) param;
                    listener.onRogSwitchStateChanged(updateRog.rogEnable, updateRog.rogInfo);
                    break;
                case 2:
                    listener.onRogInfoUpdated((AppRogInfo) param);
                    break;
                default:
                    try {
                        Slog.w(TAG, "restartAndNotifyPkgForRog->unknown msg:" + eventType);
                        break;
                    } catch (Exception e) {
                        Slog.e(TAG, "restartAndNotifyPkgForRog->notify app exception:" + e);
                        return;
                    }
            }
            if (starting != null && starting.packageName.equalsIgnoreCase(packageName)) {
                stack.ensureActivityConfigurationLocked(starting, 0, false);
                stack.ensureActivitiesVisibleLocked(starting, 0, false);
            }
        } catch (Exception e2) {
            Slog.e(TAG, "restartAndNotifyPkgForRog->get package name exception:" + e2);
        }
    }

    protected void applyRogStateChangedForStack(IHwRogListener listener, boolean rogEnable, AppRogInfo rogInfo, ActivityStack stack) {
        if (listener == null) {
            Slog.w(TAG, "applyRogStateChangedForStack->listener is null");
            return;
        }
        UpdateRog updateRog = new UpdateRog();
        updateRog.rogEnable = rogEnable;
        updateRog.rogInfo = rogInfo;
        restartAndNotifyPkgForRog(stack, listener, 1, updateRog);
    }

    protected void applyRogInfoUpdatedForStack(IHwRogListener listener, AppRogInfo rogInfo, ActivityStack stack) {
        if (listener == null) {
            Slog.w(TAG, "applyRogInfoUpdatedForStack->listener is null");
        } else {
            restartAndNotifyPkgForRog(stack, listener, 2, rogInfo);
        }
    }

    protected void attachRogInfoToApp(ProcessRecord app, ApplicationInfo appInfo) {
        IRogManager rogManager = (IRogManager) LocalServices.getService(IRogManager.class);
        if (rogManager != null) {
            if (app.instrumentationArguments == null) {
                app.instrumentationArguments = new Bundle();
            }
            app.instrumentationArguments.putBoolean("switch_state_key", rogManager.getRogSwitchState());
            app.instrumentationArguments.putParcelable("info_key", rogManager.getSpecifiedAppRogInfo(appInfo.packageName));
        }
    }

    private void setIntentInfo(Intent intent, String pkgName, Bundle bundle, boolean forLast) {
        if (forLast) {
            this.mLastSplitIntent = intent;
            this.mSplitExtras = bundle;
            return;
        }
        this.mCurrentSplitIntent.put(pkgName, intent);
    }

    private Parcelable[] getIntentInfo(String pkgName, boolean forLast) {
        if (forLast) {
            return new Parcelable[]{this.mLastSplitIntent, this.mSplitExtras};
        }
        return new Parcelable[]{(Parcelable) this.mCurrentSplitIntent.get(pkgName), null};
    }

    public void addToEntryStack(String pkgName, IBinder token, int resultCode, Intent resultData) {
        if (this.mSplitActivityEntryStack == null) {
            this.mSplitActivityEntryStack = new HashMap();
        }
        Flog.i(100, "addToEntryStack, activity is " + token);
        Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(pkgName);
        if (pkgStack == null) {
            pkgStack = new Stack();
        }
        pkgStack.push(token);
        this.mSplitActivityEntryStack.put(pkgName, pkgStack);
    }

    public void clearEntryStack(String pkgName, IBinder selfToken) {
        if (this.mSplitActivityEntryStack != null && !this.mSplitActivityEntryStack.isEmpty()) {
            Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(pkgName);
            if (pkgStack != null && !pkgStack.empty() && (selfToken == null || selfToken.equals(pkgStack.peek()))) {
                long ident = Binder.clearCallingIdentity();
                while (!pkgStack.empty()) {
                    IBinder token = (IBinder) pkgStack.pop();
                    if (!(token == null || token.equals(selfToken))) {
                        Flog.i(100, "Clearing entry " + token);
                        this.mWindowManager.setAppVisibility(token, false);
                        finishActivity(token, 0, null, 0);
                    }
                }
                Binder.restoreCallingIdentity(ident);
                if (selfToken != null) {
                    pkgStack.push(selfToken);
                }
            }
        }
    }

    public boolean isTopSplitActivity(String pkgName, IBinder token) {
        if (this.mSplitActivityEntryStack == null || this.mSplitActivityEntryStack.isEmpty() || token == null) {
            return false;
        }
        Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(pkgName);
        if (pkgStack == null || pkgStack.empty()) {
            return false;
        }
        return token.equals(pkgStack.peek());
    }

    public void removeFromEntryStack(String pkgName, IBinder token) {
        if (token != null && this.mSplitActivityEntryStack != null) {
            Stack<IBinder> pkgStack = (Stack) this.mSplitActivityEntryStack.get(pkgName);
            if (pkgStack != null && pkgStack.empty()) {
                pkgStack.remove(token);
            }
        }
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        String action = intent.getAction();
        if (!"android.intent.action.PACKAGE_ADDED".equals(action) && !"android.intent.action.PACKAGE_REMOVED".equals(action)) {
            return false;
        }
        Bundle intentExtras = intent.getExtras();
        boolean z = intentExtras != null ? intentExtras.getBoolean("LimitedPackageBroadcast", false) : false;
        Flog.d(100, "Android Wear-isLimitedPackageBroadcast: limitedPackageBroadcast = " + z);
        return z;
    }

    private boolean isAppMngOomAdjCustomized(String packageName) {
        return AwareDefaultConfigList.getInstance().isAppMngOomAdjCustomized(packageName);
    }

    public void setAndRestoreMaxAdjIfNeed(Set<String> adjCustPkg) {
        if (adjCustPkg != null) {
            synchronized (this) {
                synchronized (this.mPidsSelfLocked) {
                    for (int i = 0; i < this.mPidsSelfLocked.size(); i++) {
                        ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                        if (p != null) {
                            boolean pkgContains = false;
                            for (String pkg : p.pkgList.keySet()) {
                                if (adjCustPkg.contains(pkg)) {
                                    pkgContains = true;
                                    break;
                                }
                            }
                            if (pkgContains) {
                                if (p.maxAdj > AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ) {
                                    p.maxAdj = AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ;
                                }
                            } else if (p.maxAdj == AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ) {
                                p.maxAdj = 1001;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isPkgHasAlarm(List<String> packageList, int targetUid) {
        if (packageList == null) {
            return false;
        }
        synchronized (this) {
            for (String packageName : packageList) {
                if (this.mIntentSenderRecords.size() > 0) {
                    for (WeakReference<PendingIntentRecord> wpir : this.mIntentSenderRecords.values()) {
                        if (wpir != null) {
                            PendingIntentRecord pir = (PendingIntentRecord) wpir.get();
                            if (!(pir == null || pir.key == null || pir.key.packageName == null || !pir.key.packageName.equals(packageName))) {
                                return true;
                            }
                        }
                    }
                    continue;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int preloadApplication(String packageName, int userId) {
        if (Binder.getCallingUid() != 1000) {
            return -1;
        }
        synchronized (this) {
            if (ProcessList.computeEmptyProcessLimit(this.mProcessLimit) <= 4) {
                return -1;
            }
            IPackageManager pm = AppGlobals.getPackageManager();
            if (pm == null) {
                return -1;
            }
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(packageName, 1152, userId);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to get application info: " + packageName);
            }
            if (appInfo == null) {
                Slog.d(TAG, "preloadApplication, get application info failed, packageName = " + packageName);
                return -1;
            }
            ProcessRecord app = getProcessRecordLocked(appInfo.processName, appInfo.uid, true);
            if (app != null && app.thread != null) {
                Slog.d(TAG, "process has started, packageName:" + packageName + ", processName:" + appInfo.processName);
                return -1;
            } else if ((appInfo.flags & 9) == 9) {
                Slog.d(TAG, "preloadApplication, application is persistent, return");
                return -1;
            } else {
                if (app == null) {
                    app = newProcessRecordLocked(appInfo, null, false, 0);
                    updateLruProcessLocked(app, false, null);
                    updateOomAdjLocked();
                }
                try {
                    pm.setPackageStoppedState(packageName, false, UserHandle.getUserId(app.uid));
                } catch (RemoteException e2) {
                    Slog.w(TAG, "RemoteException, Failed trying to unstop package: " + packageName);
                } catch (IllegalArgumentException e3) {
                    Slog.w(TAG, "IllegalArgumentException, Failed trying to unstop package " + packageName);
                }
                if (app.thread == null) {
                    startProcessLocked(app, "start application", app.processName, null, null, null);
                }
            }
        }
    }

    void reportAppForceStopMsg(int userId, String packageName, int callingPid) {
        String STSMANAGER_PKGNAME = "com.huawei.systemmanager";
        String POWERGENIE_PKGNAME = "com.huawei.powergenie";
        String SETTINGS_PKGNAME = WifiProCommonUtils.HUAWEI_SETTINGS;
        boolean killedBySysManager = checkIfPackageNameMatchesPid(callingPid, "com.huawei.systemmanager");
        boolean killedByPowerGenie = checkIfPackageNameMatchesPid(callingPid, "com.huawei.powergenie");
        boolean killedBySettings = checkIfPackageNameMatchesPid(callingPid, WifiProCommonUtils.HUAWEI_SETTINGS);
        if (killedBySysManager || killedByPowerGenie || killedBySettings) {
            if (killedBySysManager) {
                reportAppDiedMsg(userId, packageName, "sysManager");
            } else if (killedByPowerGenie) {
                reportAppDiedMsg(userId, packageName, "powerGenie");
            } else {
                reportAppDiedMsg(userId, packageName, "settings");
            }
        }
    }

    private boolean checkIfPackageNameMatchesPid(int mPid, String targetPackageName) {
        if (targetPackageName.equals(getPackageNameForPid(mPid))) {
            return true;
        }
        return false;
    }

    void reportAppDiedMsg(int userId, String processName, String reason) {
        if (processName != null && !processName.contains(":") && reason != null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(processName).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(String.valueOf(userId)).append(CPUCustBaseConfig.CPUCONFIG_INVALID_STR);
            stringBuffer.append(reason);
            LogIAware.report(2031, stringBuffer.toString());
        }
    }

    void reportAppDiedMsg(AppDiedInfo appDiedInfo) {
        if (appDiedInfo != null) {
            if ("forceStop".equals(appDiedInfo.reason)) {
                reportAppForceStopMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.callerPid);
            } else {
                reportAppDiedMsg(appDiedInfo.userId, appDiedInfo.processName, appDiedInfo.reason);
            }
        }
    }

    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        if (!HwConnectivityService.MM_PKG_NAME.equals(pkg)) {
            return false;
        }
        Slog.d(TAG, " cleanUpRemovedTaskLocked, do not kill process : " + pkg);
        return true;
    }

    protected final boolean cleanUpApplicationRecordLocked(ProcessRecord app, boolean restarting, boolean allowRestart, int index, boolean replacingPid) {
        if (IS_TABLET && this.mSplitActivityEntryStack != null && this.mSplitActivityEntryStack.containsKey(app.info.packageName)) {
            Slog.w(TAG, "Split main entrance killed, clear sub activities for " + app.info.packageName);
            clearEntryStack(app.info.packageName, null);
        }
        return super.cleanUpApplicationRecordLocked(app, restarting, allowRestart, index, replacingPid);
    }

    protected final ProcessRecord getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        return super.getProcessRecordLocked(processName, UserHandle.getUid(handleUserForClone(processName, UserHandle.getUserId(uid)), uid), keepIfLarge);
    }

    protected int[] handleGidsForUser(int[] gids, int userId) {
        if (!IS_SUPPORT_CLONE_APP) {
            return gids;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            List<UserInfo> profiles = this.mUserController.getUserManager().getProfiles(userId, false);
            if (profiles.size() > 1) {
                Iterator<UserInfo> iterator = profiles.iterator();
                while (iterator.hasNext()) {
                    if (((UserInfo) iterator.next()).isManagedProfile()) {
                        iterator.remove();
                    }
                }
                if (profiles.size() > 1) {
                    for (UserInfo ui : profiles) {
                        int[] newGids = new int[(gids.length + 1)];
                        System.arraycopy(gids, 0, newGids, 0, gids.length);
                        if (ui.id != userId) {
                            newGids[gids.length] = UserHandle.getUserGid(ui.id);
                        }
                        gids = newGids;
                    }
                }
            }
            Binder.restoreCallingIdentity(ident);
            return gids;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
        }
    }

    protected final ContentProviderHolder getContentProviderImpl(IApplicationThread caller, String name, IBinder token, boolean stable, int userId) {
        ContentProviderHolder cph = super.getContentProviderImpl(caller, name, token, stable, handleUserForClone(name, userId));
        if (IS_SUPPORT_CLONE_APP && userId != 0 && cph == null) {
            UserInfo ui = this.mUserController.getUserManagerInternal().getUserInfo(userId);
            if (ui != null && ui.isClonedProfile()) {
                return super.getContentProviderImpl(caller, name, token, stable, ui.profileGroupId);
            }
        }
        return cph;
    }

    protected final void removeContentProviderExternalUnchecked(String name, IBinder token, int userId) {
        super.removeContentProviderExternalUnchecked(name, token, handleUserForClone(name, userId));
    }

    protected int handleUserForClone(String name, int userId) {
        if (!IS_SUPPORT_CLONE_APP || userId == 0 || name == null) {
            return userId;
        }
        int newUserId = userId;
        if (userId != this.mUserController.getCurrentUserIdLocked() && sAllowedCrossUserForCloneArrays.contains(name)) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mUserController.getUserInfo(userId);
                if (ui != null && ui.isClonedProfile()) {
                    newUserId = ui.profileGroupId;
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return newUserId;
    }

    final int broadcastIntentLocked(ProcessRecord callerApp, String callerPackage, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {
            intent.putExtra("android.intent.extra.USER_ID", userId);
        }
        return super.broadcastIntentLocked(callerApp, callerPackage, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, handleUserForClone(action, userId));
    }

    public ComponentName startService(IApplicationThread caller, Intent service, String resolvedType, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.startService(caller, service, resolvedType, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    public int stopService(IApplicationThread caller, Intent service, String resolvedType, int userId) {
        return super.stopService(caller, service, resolvedType, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    public int bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.bindService(caller, token, service, resolvedType, connection, flags, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    ComponentName startServiceInPackage(int uid, Intent service, String resolvedType, String callingPackage, int userId) throws TransactionTooLargeException {
        return super.startServiceInPackage(uid, service, resolvedType, callingPackage, handleUserForClone(getTargetFromIntentForClone(service), userId));
    }

    private boolean shouldPreventStartProcess(ProcessRecord app) {
        if (app.userId != 0) {
            for (String processName : this.mContext.getResources().getStringArray(33816583)) {
                if (processName.equals(app.processName)) {
                    Slog.i(TAG, app.processName + " is not allowed for sub user " + app.userId);
                    return true;
                }
            }
            UserInfo userInfo = null;
            long ident = Binder.clearCallingIdentity();
            try {
                userInfo = this.mUserController.getUserInfo(app.userId);
                if (userInfo != null && userInfo.isManagedProfile()) {
                    for (String processName2 : this.mContext.getResources().getStringArray(33816584)) {
                        if (processName2.equals(app.processName)) {
                            Slog.i(TAG, app.processName + " is not allowed for afw user " + app.userId);
                            return true;
                        }
                    }
                }
                if (userInfo != null && userInfo.isClonedProfile()) {
                    for (String processName22 : this.mContext.getResources().getStringArray(33816588)) {
                        if (processName22.equals(app.processName)) {
                            Slog.i(TAG, app.processName + " is not allowed for clone user " + app.userId);
                            return true;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return false;
    }

    private String getTargetFromIntentForClone(Intent intent) {
        if (intent.getAction() == null) {
            return intent.getComponent() != null ? intent.getComponent().getPackageName() : null;
        } else {
            return intent.getAction();
        }
    }
}
