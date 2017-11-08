package com.android.server.wm;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.HwSlog;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.SurfaceControl;
import android.view.WindowManager.LayoutParams;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.UiThread;
import com.android.server.am.ActivityRecord;
import com.android.server.input.HwInputManagerService;
import com.android.server.input.InputManagerService;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.SlideTouchEvent;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.WindowManagerService.H;
import com.huawei.forcerotation.HwForceRotationManager;
import huawei.android.app.IHwWindowCallback;
import huawei.android.app.IHwWindowCallback.Stub;
import huawei.android.os.HwGeneralManager;
import java.util.ArrayList;
import java.util.Map.Entry;

public class HwWindowManagerService extends WindowManagerService {
    static final boolean DEBUG = false;
    private static final int FORBIDDEN_ADDVIEW_BROADCAST = 1;
    private static final int IBINDER_CODE_FREEZETHAWROTATION = 208;
    private static final int IBINDER_CODE_IS_KEYGUARD_DISABLE = 1000;
    private static final long MSG_ROG_FREEZE_TIME_DELEAYED = 6000;
    public static final int ROG_FREEZE_TIMEOUT = 100;
    private static final int SET_NAVIBAR_SHOWLEFT_TRANSACTION = 2201;
    private static final int SINGLE_HAND_STATE = 1989;
    private static final int SINGLE_HAND_SWITCH = 1990;
    static final String TAG = HwWindowManagerService.class.getSimpleName();
    public static final int UPDATE_NAVIGATIONBAR = 99;
    private static final int UPDATE_WINDOW_STATE = 0;
    private boolean IS_SUPPORT_PRESSURE = false;
    final int TRANSACTION_GETTOUCHCOUNTINFO = 1006;
    final int TRANSACTION_getVisibleWindows = 1005;
    final int TRANSACTION_isDimLayerVisible = 1007;
    final int TRANSACTION_isIMEVisble = 1004;
    final int TRANSACTION_registerWindowCallback = 1002;
    final int TRANSACTION_unRegisterWindowCallback = 1003;
    AppWindowToken mFocusedAppForNavi = null;
    final Handler mHandler = new H(this);
    private HandlerThread mHandlerThread = new HandlerThread("hw_ops_handler_thread");
    private boolean mHasRecord = false;
    private Handler mHwHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 99:
                    HwWindowManagerService hwWindowManagerService = HwWindowManagerService.this;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    hwWindowManagerService.updateNavigationBar(z);
                    return;
                case 100:
                    Slog.d(HwWindowManagerService.TAG, "ROG_FREEZE_TIMEOUT");
                    SurfaceControl.unfreezeDisplay();
                    return;
                default:
                    return;
            }
        }
    };
    private RectF mImeDockShownFrame = new RectF();
    boolean mIsCoverOpen = true;
    boolean mLayoutNaviBar = false;
    private LockPatternUtils mLockPatternUtils;
    private OpsUpdateHandler mOpsHandler;
    private final Runnable mReevaluateStatusBarSize = new Runnable() {
        public void run() {
            synchronized (HwWindowManagerService.this.mWindowMap) {
                HwWindowManagerService.this.mIgnoreFrozen = true;
                if (HwWindowManagerService.this.mLayoutNaviBar) {
                    HwWindowManagerService.this.mLayoutNaviBar = false;
                    HwWindowManagerService.this.mCurNaviConfiguration = HwWindowManagerService.this.computeNewConfigurationLocked();
                    if (HwWindowManagerService.this.mWallpaperControllerLocked.getWallpaperTarget() != null) {
                        HwWindowManagerService.this.mWallpaperControllerLocked.updateWallpaperVisibility();
                    }
                    HwWindowManagerService.this.performhwLayoutAndPlaceSurfacesLocked();
                } else {
                    HwWindowManagerService.this.performhwLayoutAndPlaceSurfacesLocked();
                }
            }
        }
    };
    private SingleHandAdapter mSingleHandAdapter;
    private int mSingleHandSwitch;
    private boolean mSplitMode = false;
    private boolean mTaskChanged = false;
    private int mTempOrientation = -2;
    private AppWindowToken mTempToken = null;
    private final Handler mUiHandler;

    private class OpsUpdateHandler extends Handler {
        public OpsUpdateHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    HwWindowManagerService.this.updateAppOpsState();
                    return;
                case 1:
                    HwWindowManagerService.this.sendForbiddenBroadcast(msg.getData());
                    return;
                default:
                    return;
            }
        }
    }

    private void getVisibleWindows(ArrayMap<Integer, Integer> windows) {
        if (windows != null) {
            synchronized (this.mWindowMap) {
                for (WindowState win : this.mWindowMap.values()) {
                    if (!(win == null || win.mAttrs == null || win.mSession == null || windows.containsKey(Integer.valueOf(win.mSession.mPid)) || win.mAppOp != 24)) {
                        if (this.mAppOps == null) {
                            return;
                        } else {
                            windows.put(Integer.valueOf(win.mSession.mPid), Integer.valueOf(this.mAppOps.startOpNoThrow(win.mAppOp, win.getOwningUid(), win.getOwningPackage())));
                        }
                    }
                }
            }
        }
    }

    private void updateVisibleWindows(int eventType, int mode, int pid) {
        Bundle args = new Bundle();
        args.putInt("window", pid);
        args.putInt("windowmode", mode);
        args.putInt("relationType", eventType);
        CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
        long id = Binder.clearCallingIdentity();
        HwSysResManager.getInstance().reportData(data);
        Binder.restoreCallingIdentity(id);
    }

    private void updateVisibleWindowsOps(int eventType, String pkgName) {
        Bundle args = new Bundle();
        args.putString("pkgname", pkgName);
        args.putInt("relationType", eventType);
        CollectData data = new CollectData(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args);
        long id = Binder.clearCallingIdentity();
        HwSysResManager.getInstance().reportData(data);
        Binder.restoreCallingIdentity(id);
    }

    protected void addWindowReport(WindowState win, int mode) {
        if (win != null && win.mAppOp == 24 && win.mSession != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindows(8, mode, win.mSession.mPid);
            }
        }
    }

    protected void removeWindowReport(WindowState win) {
        if (win != null && win.mAppOp == 24 && win.mSession != null) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindows(9, 3, win.mSession.mPid);
            }
        }
    }

    protected void updateAppOpsStateReport(int ops, String packageName) {
        if (ops == 24) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RESOURCE_APPASSOC))) {
                updateVisibleWindowsOps(10, packageName);
            }
        }
    }

    public HwWindowManagerService(Context context, InputManagerService inputManager, boolean haveInputMethods, boolean showBootMsgs, boolean onlyCore) {
        super(context, inputManager, haveInputMethods, showBootMsgs, onlyCore);
        this.mHandlerThread.start();
        this.mOpsHandler = new OpsUpdateHandler(this.mHandlerThread.getLooper());
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUiHandler = UiThread.getHandler();
    }

    private boolean judgeSingleHandSwitchBySize() {
        return this.mContext.getResources().getBoolean(34406401);
    }

    protected void setCropOnSingleHandMode(int singleHandleMode, boolean isMultiWindowApp, int dw, int dh, Rect crop) {
        float verticalBlank = ((float) dh) * 0.25f;
        float horizontalBlank = ((float) dw) * 0.25f;
        if (singleHandleMode == 1) {
            crop.right -= (int) horizontalBlank;
        } else {
            crop.left += (int) horizontalBlank;
        }
        if (isMultiWindowApp) {
            if (crop.top == 0) {
                crop.top += (int) (((float) dh) * 0.25f);
            } else {
                crop.top = (int) ((((float) crop.top) * SlideTouchEvent.SCALE) + (((float) dh) * 0.25f));
            }
            crop.bottom = (int) ((((float) crop.bottom) * SlideTouchEvent.SCALE) + (((float) dh) * 0.25f));
            return;
        }
        if (crop.top > 0) {
            crop.top = (int) ((((float) crop.top) * SlideTouchEvent.SCALE) + verticalBlank);
        } else {
            crop.top = (int) verticalBlank;
        }
        if (crop.bottom < dh) {
            crop.bottom = (int) (((float) crop.bottom) + (((float) (dh - crop.bottom)) * 0.25f));
        }
    }

    protected void hwProcessOnMatrix(int rotation, int width, int height, Rect frame, Matrix outMatrix) {
        switch (rotation) {
            case 1:
            case 3:
                outMatrix.postRotate(90.0f);
                outMatrix.postTranslate((float) width, 0.0f);
                return;
            default:
                return;
        }
    }

    public int addWindow(Session session, IWindow client, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        if (attrs.type == 2101) {
            attrs.token = null;
        }
        return super.addWindow(session, client, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

    public void setCoverManagerState(boolean isCoverOpen) {
        this.mIsCoverOpen = isCoverOpen;
    }

    public boolean isCoverOpen() {
        return this.mIsCoverOpen;
    }

    public void freezeOrThawRotation(int rotation) {
        if (!checkCallingPermission("android.permission.SET_ORIENTATION", "freezeRotation()")) {
            throw new SecurityException("Requires SET_ORIENTATION permission");
        } else if (rotation < -1 || rotation > 3) {
            throw new IllegalArgumentException("Rotation argument must be -1 or a valid rotation constant.");
        } else {
            Slog.v(TAG, "freezeRotationTemporarily: rotation=" + rotation);
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                ((HwPhoneWindowManager) this.mPolicy).freezeOrThawRotation(rotation);
            }
            super.updateRotationUnchecked(false, false);
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        int result;
        switch (code) {
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL /*201*/:
                data.enforceInterface("android.view.IWindowManager");
                result = getCurrentFloatWindowTotal(data.readString());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION /*202*/:
                data.enforceInterface("android.view.IWindowManager");
                result = getInitFloatPosition(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT /*203*/:
                data.enforceInterface("android.view.IWindowManager");
                rotateWithHoldDialog();
                reply.writeNoException();
                return true;
            case WifiProCommonUtils.HTTP_REACHALBE_GOOLE /*204*/:
                data.enforceInterface("android.view.IWindowManager");
                this.mHwHandler.sendMessage(this.mHwHandler.obtainMessage(99, data.readInt(), 0));
                reply.writeNoException();
                return true;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_SYSCALL_EX /*205*/:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).swipeFromTop();
                }
                reply.writeNoException();
                return true;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_EXCEPTION_EX /*206*/:
                data.enforceInterface("android.view.IWindowManager");
                boolean isTopIsFullscreen = false;
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    isTopIsFullscreen = ((HwPhoneWindowManager) this.mPolicy).isTopIsFullscreen();
                }
                reply.writeInt(isTopIsFullscreen ? 1 : 0);
                return true;
            case GnssConnectivityLogManager.CHR_GNSS_HAL_EVENT_INJECT_EX /*207*/:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).showHwTransientBars();
                }
                return true;
            case 209:
                data.enforceInterface("android.view.IWindowManager");
                freezeOrThawRotation(data.readInt());
                reply.writeNoException();
                return true;
            case 1001:
                data.enforceInterface("android.view.IWindowManager");
                boolean result2 = this.mLockPatternUtils.isLockScreenDisabled(0);
                reply.writeNoException();
                reply.writeInt(result2 ? 1 : 0);
                return true;
            case 1002:
                data.enforceInterface("android.view.IWindowManager");
                IHwWindowCallback hwWindowCallback = Stub.asInterface(data.readStrongBinder());
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).setHwWindowCallback(hwWindowCallback);
                }
                reply.writeNoException();
                return true;
            case 1003:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).setHwWindowCallback(null);
                }
                reply.writeNoException();
                return true;
            case 1004:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mContext.checkPermission("com.huawei.permission.HUAWEI_IME_STATE_ACCESS", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeInt(-1);
                    return true;
                }
                boolean isVisibleLw = this.mInputMethodWindow != null ? this.mInputMethodWindow.isVisibleLw() : false;
                HwSlog.d(TAG, "imeVis=" + isVisibleLw);
                reply.writeNoException();
                reply.writeInt(isVisibleLw ? 1 : 0);
                return true;
            case 1005:
                data.enforceInterface("android.view.IWindowManager");
                ArrayMap<Integer, Integer> windows = new ArrayMap();
                getVisibleWindows(windows);
                reply.writeInt(windows.size());
                for (Entry<Integer, Integer> win : windows.entrySet()) {
                    reply.writeInt(((Integer) win.getKey()).intValue());
                    reply.writeInt(((Integer) win.getValue()).intValue());
                }
                reply.writeNoException();
                return true;
            case 1006:
                data.enforceInterface("android.view.IWindowManager");
                if (!(this.mPolicy instanceof HwPhoneWindowManager)) {
                    Slog.w(TAG, "onTransct->current is not hw pwm");
                    return true;
                } else if (this.mContext.checkPermission("com.huawei.permission.GET_TOUCH_COUNT_INFO", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeIntArray(((HwPhoneWindowManager) this.mPolicy).getDefaultTouchCountInfo());
                    return true;
                } else {
                    reply.writeIntArray(((HwPhoneWindowManager) this.mPolicy).getTouchCountInfo());
                    reply.writeNoException();
                    return true;
                }
            case 1007:
                data.enforceInterface("android.view.IWindowManager");
                result = isDLayerVisible();
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            case SINGLE_HAND_SWITCH /*1990*/:
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(this.mLazyModeOn);
                return true;
            case 1991:
                Slog.i(TAG, "mSingleHandSwitch =" + this.mSingleHandSwitch);
                this.mSingleHandSwitch = judgeSingleHandSwitchBySize() ? 1 : 0;
                data.enforceInterface("android.view.IWindowManager");
                reply.writeNoException();
                reply.writeInt(this.mSingleHandSwitch);
                return true;
            case SET_NAVIBAR_SHOWLEFT_TRANSACTION /*2201*/:
                data.enforceInterface("android.view.IWindowManager");
                if (this.mContext.checkPermission("com.huawei.permission.NAVIBAR_LEFT_WHENLAND", Binder.getCallingPid(), Binder.getCallingUid()) != 0) {
                    reply.writeInt(-1);
                    return true;
                }
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    ((HwPhoneWindowManager) this.mPolicy).setNavibarAlignLeftWhenLand(data.readInt() == 1);
                }
                return true;
            default:
                try {
                    return super.onTransact(code, data, reply, flags);
                } catch (RuntimeException e) {
                    if (!(e instanceof SecurityException)) {
                        Slog.w(TAG, "Window Manager Crash");
                    }
                    throw e;
                }
        }
        if (e instanceof SecurityException) {
            Slog.w(TAG, "Window Manager Crash");
        }
        throw e;
    }

    public int isDLayerVisible() {
        return getDefaultDisplayContentLocked().getDockedDividerController().mDimLayer.mShowing ? 1 : 0;
    }

    private void updateNavigationBar(boolean minNaviBar) {
        this.mPolicy.updateNavigationBar(minNaviBar);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setFocusedAppForNavi(IBinder token) {
        synchronized (this.mWindowMap) {
            if (token == null) {
                this.mFocusedAppForNavi = null;
            } else {
                AppWindowToken newFocus = findAppWindowToken(token);
                if (newFocus == null) {
                    Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                    return;
                }
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    HwPhoneWindowManager policy = this.mPolicy;
                    if (policy.getHwWindowCallback() != null) {
                        ActivityRecord r = ActivityRecord.forToken(newFocus.token);
                        if (r != null) {
                            Slog.d(TAG, "setFocuedApp r: " + r + ",pkgName=" + r.info.applicationInfo.packageName);
                        } else {
                            Slog.d(TAG, "setFocuedApp r: " + r);
                        }
                        if (!(r == null || r.info.applicationInfo.packageName.equals("com.android.gallery3d"))) {
                            try {
                                Slog.d(TAG, "setFocuedApp focusedAppChanged");
                                policy.getHwWindowCallback().focusedAppChanged();
                            } catch (Exception ex) {
                                Slog.w(TAG, "mIHwWindowCallback focusedAppChanged", ex);
                            }
                        }
                    }
                }
                this.mFocusedAppForNavi = newFocus;
            }
        }
    }

    public int[] setNewConfiguration(Configuration config) {
        if (checkCallingPermission("android.permission.MANAGE_APP_TOKENS", "setNewConfiguration()")) {
            synchronized (this.mWindowMap) {
                if (this.mPolicy instanceof HwPhoneWindowManager) {
                    HwPhoneWindowManager policy = this.mPolicy;
                    if (!(policy.getHwWindowCallback() == null || (this.mCurConfiguration.diff(config) & 128) == 0)) {
                        Slog.v(TAG, "setNewConfiguration notify window callback");
                        try {
                            policy.getHwWindowCallback().handleConfigurationChanged();
                        } catch (Exception ex) {
                            Slog.w(TAG, "mIHwWindowCallback handleConfigurationChanged", ex);
                        }
                    }
                }
            }
            return super.setNewConfiguration(config);
        }
        throw new SecurityException("Requires MANAGE_APP_TOKENS permission");
    }

    public void setNaviBarFlag() {
        this.mPolicy.setInputMethodWindowVisible(this.mInputMethodWindow == null ? false : this.mInputMethodWindow.isVisibleLw());
        if (this.mFocusedAppForNavi != null) {
            this.mPolicy.setNaviBarFlag(this.mFocusedAppForNavi.navigationBarHide);
        }
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
        synchronized (this.mWindowMap) {
            this.mLayoutNaviBar = layoutNaviBar;
            this.mH.post(this.mReevaluateStatusBarSize);
        }
    }

    public Configuration getCurNaviConfiguration() {
        return this.mCurNaviConfiguration;
    }

    private void rotateWithHoldDialog() {
        this.mHandler.removeMessages(17);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(17));
        this.mHandler.removeMessages(11);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(11));
    }

    protected void sendUpdateAppOpsState() {
        this.mOpsHandler.removeMessages(0);
        this.mOpsHandler.sendEmptyMessage(0);
    }

    protected void setAppOpHideHook(WindowState win, boolean visible) {
        if (!visible) {
            setAppOpVisibilityChecked(win, visible);
        }
    }

    protected void setAppOpVisibilityLwHook(WindowState win, int mode) {
        LayoutParams attrs = win.mAttrs;
        if (mode == 0 || mode == 3) {
            setAppOpVisibilityChecked(win, true);
        } else {
            setAppOpVisibilityChecked(win, false);
        }
    }

    private boolean setAppOpVisibilityChecked(WindowState win, boolean visible) {
        if (visible) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else if (allowAnyway(win)) {
            setWinAndChildrenVisibility(win, true);
            return true;
        } else {
            setWinAndChildrenVisibility(win, false);
            sendForbiddenMessage(win);
            return false;
        }
    }

    private void setWinAndChildrenVisibility(WindowState win, boolean visible) {
        if (win != null) {
            win.setAppOpVisibilityLw(visible);
            ArrayList<WindowState> children = win.mChildWindows;
            if (children != null) {
                int N = children.size();
                Slog.i(TAG, "this win:" + win + " hase children size:" + N);
                for (int i = 0; i < N; i++) {
                    setWinAndChildrenVisibility((WindowState) children.get(i), visible);
                }
            }
        }
    }

    private boolean allowAnyway(WindowState win) {
        if (win == null || isWinInTopTask(win)) {
            return true;
        }
        if (!checkFullWindowWithoutTransparent(win.mAttrs)) {
            return false;
        }
        Slog.i(TAG, "checkFullWindowWithoutTransparent = true , don't allow anyway," + win);
        return false;
    }

    private boolean isWinInTopTask(WindowState win) {
        boolean isTop = false;
        if (this.mFocusedApp == null) {
            return false;
        }
        ActivityRecord r = ActivityRecord.forToken(this.mFocusedApp.token);
        if (r == null || r.info == null || r.info.applicationInfo == null) {
            return false;
        }
        if (win.getOwningUid() == r.info.applicationInfo.uid) {
            isTop = true;
        }
        if (isTop) {
            Slog.i(TAG, "there is a top app's flow view:" + win);
        }
        return isTop;
    }

    private void sendForbiddenMessage(WindowState win) {
        Message msg = this.mOpsHandler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putInt("uid", win.getOwningUid());
        bundle.putString(ControlScope.PACKAGE_ELEMENT_KEY, win.getOwningPackage());
        msg.setData(bundle);
        this.mOpsHandler.sendMessage(msg);
    }

    private void sendForbiddenBroadcast(Bundle data) {
        Intent preventIntent = new Intent("com.android.server.wm.addview.preventnotify");
        preventIntent.putExtras(data);
        this.mContext.sendBroadcastAsUser(preventIntent, UserHandle.ALL);
    }

    private boolean checkFullWindowWithoutTransparent(LayoutParams attrs) {
        if (-1 == attrs.width && -1 == attrs.height && 0.0d != ((double) attrs.alpha)) {
            return true;
        }
        return false;
    }

    public void moveTaskToTop(int taskId) {
        super.moveTaskToTop(taskId);
        this.mTaskChanged = true;
        Slog.v(TAG, "moveTaskToTop mTaskChanged:" + this.mTaskChanged);
    }

    public void setFocusedApp(IBinder token, boolean moveFocusNow) {
        synchronized (this.mWindowMap) {
            AppWindowToken appWindowToken;
            if (token == null) {
                appWindowToken = null;
            } else {
                appWindowToken = findAppWindowToken(token);
                if (appWindowToken == null) {
                    Slog.w(TAG, "Attempted to set focus to non-existing app token: " + token);
                }
            }
            if ((this.mFocusedApp != appWindowToken) && appWindowToken != null) {
                int requestedOrientation = appWindowToken.requestedOrientation;
                if (!(requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8)) {
                    if (requestedOrientation == 11) {
                    }
                }
                Slog.i(TAG, "setFocusedApp token: " + token + " requestedOrientation: " + requestedOrientation);
                Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
            }
        }
        super.setFocusedApp(token, moveFocusNow);
        if (this.mTaskChanged) {
            this.mTaskChanged = false;
            Slog.v(TAG, "setFocusedApp update app ops, mTaskChanged set to:" + this.mTaskChanged);
            sendUpdateAppOpsState();
        }
    }

    protected void setVisibleFromParent(WindowState win) {
        if (parentHiddenByAppOp(win)) {
            Slog.i(TAG, "parent is hidden by app ops, should also hide this win:" + win);
            setWinAndChildrenVisibility(win, false);
        }
    }

    private boolean parentHiddenByAppOp(WindowState win) {
        if (win == null || win.mAttachedWindow == null) {
            return false;
        }
        if (win.mAttachedWindow.mAppOpVisibility) {
            return parentHiddenByAppOp(win.mAttachedWindow);
        }
        return true;
    }

    public int getCurrentFloatWindowTotal(String titlePrefix) {
        ArrayList<WindowState> hwFloatWindows = getFloatWindowList();
        int count = hwFloatWindows.size();
        if (titlePrefix == null) {
            return count;
        }
        for (int i = 0; i < count; i++) {
            if (titlePrefix.equals(((WindowState) hwFloatWindows.get(i)).mAttrs.getTitle().toString())) {
                return i;
            }
        }
        return count;
    }

    private ArrayList<WindowState> getFloatWindowList() {
        String prefix = "com.huawei.FloatWindow";
        ArrayList<WindowState> hwFloatWindows = new ArrayList();
        ArrayList<WindowState> windows = getDefaultWindowListLocked();
        int N = windows.size();
        for (int i = 0; i < N; i++) {
            WindowState win = (WindowState) windows.get(i);
            if (win.mAttrs.getTitle().toString().startsWith("com.huawei.FloatWindow")) {
                hwFloatWindows.add(win);
            }
        }
        return hwFloatWindows;
    }

    public int getInitFloatPosition(int initX, int initY, int offsetX, int offsetY) {
        ArrayList<WindowState> hwFloatWindows = getFloatWindowList();
        int posX = initX;
        int posY = initY;
        int count = hwFloatWindows.size();
        if (count == 0) {
            return (initX << 16) | initY;
        }
        DisplayInfo displayInfo = getDefaultDisplayContentLocked().getDisplayInfo();
        int dw = displayInfo.appWidth;
        int dh = displayInfo.appHeight;
        for (int i = count - 1; i >= 0; i--) {
            WindowState win = (WindowState) hwFloatWindows.get(i);
            int left = win.mFrame.left;
            int top = win.mFrame.top;
            int right = win.mFrame.right;
            int bottom = win.mFrame.bottom;
            posX = initX;
            posY = initY;
            if ((offsetX >> 2) + left > 0 && right - offsetX < dw) {
                posX = left + offsetX;
                if (top > 0 && top < (dw >> 1) && bottom < dh) {
                    posY = top + offsetY;
                    break;
                }
                posX = initX;
                posY = initY;
            }
        }
        return (posX << 16) | posY;
    }

    public void systemReady() {
        int i = 0;
        super.systemReady();
        if (judgeSingleHandSwitchBySize()) {
            i = 1;
        }
        this.mSingleHandSwitch = i;
        if (this.mSingleHandSwitch > 0) {
            this.mSingleHandAdapter = new SingleHandAdapter(this.mContext, this.mHandler, this.mUiHandler, this);
            this.mSingleHandAdapter.registerLocked();
        }
        this.IS_SUPPORT_PRESSURE = HwGeneralManager.getInstance().isSupportForce();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setAppOrientation(IApplicationToken token, int requestedOrientation) {
        super.setAppOrientation(token, requestedOrientation);
        synchronized (this.mWindowMap) {
            if (findAppWindowToken(token.asBinder()) == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + token);
                return;
            }
            if (!(requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8)) {
                if (requestedOrientation == 11) {
                }
            }
            Slog.i(TAG, "setAppOrientation token: " + token + " requestedOrientation: " + requestedOrientation);
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", AppHibernateCst.INVALID_PKG);
        }
    }

    void computeScreenConfigurationLocked(Configuration config) {
        super.computeScreenConfigurationLocked(config);
        if (this.IS_SUPPORT_PRESSURE) {
            DisplayInfo displayInfo1 = getDefaultDisplayContentLocked().getDisplayInfo();
            this.mInputManager.setDisplayWidthAndHeight(displayInfo1.logicalWidth, displayInfo1.logicalHeight);
        }
    }

    public int getLazyMode() {
        return this.mLazyModeOn;
    }

    public void setLazyMode(int lazyMode) {
        Slog.i(TAG, "cur: " + this.mLazyModeOn + " to: " + lazyMode);
        if (this.mLazyModeOn != lazyMode) {
            this.mLazyModeOn = lazyMode;
        }
    }

    public int getNsdWindowInfo(IBinder token) {
        synchronized (this.mWindowMap) {
            WindowState window = (WindowState) this.mWindowMap.get(token);
            if (window == null) {
                return 0;
            } else if (window.isVisibleNow()) {
                int i = window.mLayer;
                return i;
            } else {
                return 0;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getNsdWindowTitle(IBinder token) {
        synchronized (this.mWindowMap) {
            WindowState window = (WindowState) this.mWindowMap.get(token);
            if (window == null || !window.isVisibleNow()) {
            } else {
                String charSequence = window.mAttrs.getTitle().toString();
                return charSequence;
            }
        }
    }

    protected void checkKeyguardDismissDoneLocked() {
        if (this.mKeyguardDismissDoneCallback != null) {
            if (this.mKeyguardWin != null) {
                int delay = 0;
                boolean z = !this.mKeyguardAttachWallpaper;
                if (!(z || this.mTopWallpaperWin == null)) {
                    z = this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceShown ? this.mTopWallpaperAnimLayer != this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceLayer : true;
                    delay = !this.mTopWallpaperWin.mWinAnimator.mSurfaceController.mSurfaceShown ? 60 : 0;
                }
                if (!this.mKeyguardWin.mHasSurface && r1) {
                    this.mKeyguardWin = null;
                    this.mTopWallpaperWin = null;
                    this.mH.removeMessages(100);
                    this.mH.sendEmptyMessageDelayed(101, (long) delay);
                }
            } else if (!this.mPolicy.isStatusBarKeyguardShowing()) {
                this.mH.removeMessages(100);
                this.mH.sendEmptyMessageDelayed(101, 0);
            }
        }
    }

    public void setCurrentUser(int newUserId, int[] currentProfileIds) {
        super.setCurrentUser(newUserId, currentProfileIds);
        synchronized (this.mWindowMap) {
            ((HwInputManagerService) this.mInputManager).setCurrentUser(newUserId, currentProfileIds);
            if (this.mPolicy instanceof HwPhoneWindowManager) {
                ((HwPhoneWindowManager) this.mPolicy).setCurrentUser(newUserId, currentProfileIds);
            }
        }
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        super.setForcedDisplayDensityAndSize(displayId, density, width, height);
        Slog.d(TAG, "setForcedDisplayDensityAndSize size: " + width + "x" + height);
        Slog.d(TAG, "setForcedDisplayDensityAndSize density: " + density);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != 0) {
            throw new SecurityException("Must hold permission android.permission.WRITE_SECURE_SETTINGS");
        } else if (displayId != 0) {
            throw new IllegalArgumentException("Can only set the default display");
        } else {
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mWindowMap) {
                    DisplayContent displayContent = getDisplayContentLocked(displayId);
                    if (displayContent != null) {
                        width = Math.min(Math.max(width, 200), displayContent.mInitialDisplayWidth * 2);
                        height = Math.min(Math.max(height, 200), displayContent.mInitialDisplayHeight * 2);
                        displayContent.mBaseDisplayWidth = width;
                        displayContent.mBaseDisplayHeight = height;
                        displayContent.mBaseDisplayDensity = density;
                        this.mHwHandler.removeMessages(100);
                        this.mHwHandler.sendEmptyMessageDelayed(100, MSG_ROG_FREEZE_TIME_DELEAYED);
                        updateResourceConfiguration(displayId, density, width, height);
                        reconfigureDisplayLocked(displayContent);
                        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                        if (screenRotationAnimation != null) {
                            screenRotationAnimation.kill();
                        }
                        Global.putString(this.mContext.getContentResolver(), "display_size_forced", width + "," + height);
                        Global.putString(this.mContext.getContentResolver(), "display_density_forced", Integer.toString(density));
                        SystemProperties.set("persist.sys.realdpi", density + AppHibernateCst.INVALID_PKG);
                        SystemProperties.set("persist.sys.rog.width", width + AppHibernateCst.INVALID_PKG);
                        SystemProperties.set("persist.sys.rog.height", height + AppHibernateCst.INVALID_PKG);
                    }
                }
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
        Configuration mTempResourceConfiguration = new Configuration(this.mCurConfiguration);
        DisplayMetrics mTempMetrics = this.mContext.getResources().getDisplayMetrics();
        mTempResourceConfiguration.densityDpi = density;
        mTempResourceConfiguration.screenWidthDp = (width * 160) / density;
        mTempResourceConfiguration.smallestScreenWidthDp = (width * 160) / density;
        mTempMetrics.density = ((float) density) / 160.0f;
        mTempMetrics.densityDpi = density;
        this.mContext.getResources().updateConfiguration(mTempResourceConfiguration, mTempMetrics);
    }

    public boolean detectSafeMode() {
        if (!HwDeviceManager.disallowOp(10)) {
            return super.detectSafeMode();
        }
        Slog.i(TAG, "safemode is disabled by dpm");
        this.mSafeMode = false;
        this.mPolicy.setSafeMode(this.mSafeMode);
        return this.mSafeMode;
    }

    public boolean isSplitMode() {
        return this.mSplitMode;
    }

    public void setSplittable(boolean splittable) {
        this.mSplitMode = splittable;
    }

    public int getLayerIndex(String appName, int windowType) {
        WindowList windows = ((DisplayContent) this.mDisplayContents.valueAt(0)).getWindowList();
        for (int index = 0; index < windows.size(); index++) {
            WindowState ws = (WindowState) windows.get(index);
            if (ws.getWindowTag().toString().indexOf(appName) > -1 && ws.mAttrs.type == windowType) {
                return ws.mLayer;
            }
        }
        return -1;
    }

    protected boolean isDisplayOkForAnimation(int width, int height, int transit, AppWindowToken atoken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported()) {
            return okToDisplay();
        }
        if (!forceRotationManager.isForceRotationSwitchOpen(this.mContext)) {
            return okToDisplay();
        }
        if (width <= height || ((transit != 7 && transit != 6) || !forceRotationManager.isAppForceLandRotatable(atoken.appPackageName, atoken.appToken.asBinder()))) {
            return okToDisplay();
        }
        return false;
    }

    protected boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext)) {
            return false;
        }
        int or = aToken.requestedOrientation;
        if (!(aToken == this.mTempToken && or == this.mTempOrientation)) {
            this.mHasRecord = forceRotationManager.saveOrUpdateForceRotationAppInfo(aToken.appPackageName, aToken.appComponentName, aToken.appToken.asBinder(), or);
            this.mTempToken = aToken;
            this.mTempOrientation = or;
        }
        if (!this.mHasRecord) {
            return false;
        }
        if (or != 1 && or != 7 && or != 9 && or != 12) {
            return false;
        }
        forceRotationManager.showToastIfNeeded(aToken.appPackageName, aToken.appPid, aToken.appProcessName, aToken.appToken.asBinder());
        return true;
    }

    public void showWallpaperIfNeed(WindowState w) {
        HwForceRotationManager forceRotationManager = HwForceRotationManager.getDefault();
        if (!forceRotationManager.isForceRotationSupported() || !forceRotationManager.isForceRotationSwitchOpen(this.mContext)) {
            return;
        }
        if (w == null || w.isInMultiWindowMode()) {
            Slog.d(TAG, "window is in multiwindow mode");
        } else if (w.mAppToken != null && forceRotationManager.isAppForceLandRotatable(w.mAppToken.appPackageName, w.mAppToken.appToken.asBinder())) {
            DisplayContent dc = getDefaultDisplayContentLocked();
            if (dc != null) {
                Display dp = dc.getDisplay();
                if (dp != null) {
                    DisplayMetrics dm = new DisplayMetrics();
                    dp.getMetrics(dm);
                    LayoutParams layoutParams;
                    if (dm.widthPixels < dm.heightPixels) {
                        layoutParams = w.mAttrs;
                        layoutParams.flags &= -1048577;
                    } else {
                        layoutParams = w.mAttrs;
                        layoutParams.flags |= 1048576;
                    }
                }
            }
        }
    }

    public void prepareForForceRotation(IBinder token, String packageName, int pid, String processName) {
        synchronized (this.mWindowMap) {
            AppWindowToken aToken = findAppWindowToken(token);
            if (aToken == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + token);
                return;
            }
            aToken.appPackageName = packageName;
            aToken.appPid = pid;
            aToken.appProcessName = processName;
        }
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, String componentName) {
        synchronized (this.mWindowMap) {
            AppWindowToken aToken = findAppWindowToken(appToken);
            if (aToken == null) {
                Slog.w(TAG, "Attempted to set orientation of non-existing app token: " + appToken);
                return;
            }
            aToken.appPackageName = packageName;
            aToken.appComponentName = componentName;
        }
    }
}
