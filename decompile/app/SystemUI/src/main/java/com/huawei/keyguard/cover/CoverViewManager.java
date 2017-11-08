package com.huawei.keyguard.cover;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.cover.ICoverViewDelegate;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import com.android.huawei.coverscreen.HwCustCoverScreen;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$layout;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.support.FingerprintNavigator;
import com.huawei.keyguard.support.RogUtils;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class CoverViewManager {
    private static CoverViewManager sInstance;
    private ArrayList<String> mApkCovers = new ArrayList();
    private WeakReference<ViewGroup> mCachedCoverBarView = new WeakReference(null);
    private Context mContext;
    private boolean mCoverAdded = false;
    private ViewGroup mCoverBarView = null;
    private Class<?> mCoverManagerClass;
    private Object mCoverManagerObject;
    private CoverScreen mCoverScreenView;
    private IWindowManager mIWindowManager;
    private int mIsLargeCover = -1;
    private PowerManager mPM;

    private IWindowManager getWindowManager() {
        if (this.mIWindowManager == null) {
            this.mIWindowManager = Stub.asInterface(ServiceManager.getService("window"));
        }
        if (this.mIWindowManager == null) {
            HwLog.w("CoverViewManager", "mIWindowManager is null");
        }
        return this.mIWindowManager;
    }

    public boolean isCoverAdded() {
        boolean z = true;
        synchronized (this) {
            if (!this.mCoverAdded && this.mApkCovers.size() <= 0) {
                z = false;
            }
        }
        return z;
    }

    private void onApkCoverRemoved() {
        adjustStatusBarClockView();
    }

    private void onApkCoverAdded() {
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (wm == null) {
            HwLog.e("CoverViewManager", "AddCoverScreen fail when get WINDOW_SERVICE.");
            return;
        }
        if (this.mCoverBarView != null) {
            wm.removeViewImmediate(this.mCoverBarView);
        } else {
            this.mCoverBarView = getCoverBarView();
        }
        addCoverBarWnd(wm, this.mCoverBarView);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCoverChanged(String pkg, boolean added) {
        if (TextUtils.isEmpty(pkg)) {
            HwLog.e("CoverViewManager", "unknow cover change..." + added);
            return;
        }
        synchronized (this) {
            if (added) {
                if (this.mApkCovers.contains(pkg)) {
                    this.mApkCovers.remove(pkg);
                    HwLog.e("CoverViewManager", "cover already added..." + pkg);
                }
                this.mApkCovers.add(0, pkg);
            } else if (this.mApkCovers.contains(pkg)) {
                this.mApkCovers.remove(pkg);
            } else {
                HwLog.w("CoverViewManager", "remove unknow cover..." + pkg);
            }
            if (!this.mCoverAdded) {
            }
        }
    }

    private int getApkCoverSize() {
        int size;
        synchronized (this) {
            size = this.mApkCovers.size();
        }
        return size;
    }

    private boolean isAlarmCover() {
        boolean z = false;
        synchronized (this) {
            if (this.mApkCovers.size() > 0) {
                z = "com.android.deskclock".equals(this.mApkCovers.get(0));
            }
        }
        return z;
    }

    public void setCoverAdded(boolean added) {
        synchronized (this) {
            this.mCoverAdded = added;
            if (!this.mCoverAdded) {
                this.mApkCovers.clear();
            }
        }
    }

    public static synchronized CoverViewManager getInstance(Context context) {
        CoverViewManager coverViewManager;
        synchronized (CoverViewManager.class) {
            if (sInstance == null) {
                sInstance = new CoverViewManager(context);
            }
            coverViewManager = sInstance;
        }
        return coverViewManager;
    }

    private CoverViewManager(Context context) {
        this.mContext = context;
        this.mPM = (PowerManager) context.getSystemService("power");
        initClazzObject();
    }

    public static final Rect getCoverWindowSize(Context context) {
        String location = SystemProperties.get("ro.config.huawei_smallwindow");
        String[] split = location == null ? null : location.replaceAll(" ", BuildConfig.FLAVOR).split(",");
        if (split != null) {
            try {
                if (split.length == 4) {
                    return RogUtils.checkRectSize(context, new Rect(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
                }
            } catch (NumberFormatException e) {
                HwLog.d("CoverViewManager", "Invalid parameter." + location);
            }
        }
        return new Rect(0, 0, 512, 512);
    }

    public void addCoverScreenWindow() {
        if (this.mContext == null) {
            HwLog.e("CoverViewManager", "AddCoverScreen skiped");
        } else if (this.mCoverScreenView == null || this.mCoverBarView == null) {
            WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
            if (wm == null) {
                HwLog.e("CoverViewManager", "AddCoverScreen fail when get WINDOW_SERVICE.");
                return;
            }
            LayoutInflater inflater = LayoutInflater.from(this.mContext);
            if (inflater != null) {
                if (this.mCoverScreenView != null) {
                    HwLog.e("CoverViewManager", "Add CoverView skiped as view already exists");
                } else {
                    addCoverWnd(wm, inflater);
                }
                if (this.mCoverBarView != null) {
                    HwLog.e("CoverViewManager", "Add CoverBarView skiped as view already exists");
                } else {
                    addCoverBarWnd(wm, getCoverBarView());
                }
                if (this.mPM.isScreenOn()) {
                    this.mPM.userActivity(SystemClock.uptimeMillis(), false);
                }
            }
        } else {
            HwLog.e("CoverViewManager", "Add Cover skiped as already exists");
        }
    }

    private void addCoverBarWnd(WindowManager wm, ViewGroup barView) {
        if (barView == null || wm == null) {
            HwLog.w("CoverViewManager", "addCoverBarView skipped" + barView);
            return;
        }
        adjustStatusBarClockView();
        LayoutParams barParams = new LayoutParams(2101);
        barParams.width = -1;
        barParams.height = -2;
        barParams.flags |= 67111704;
        barParams.privateFlags |= -2147483632;
        barParams.isEmuiStyle = 1;
        barParams.format = -3;
        barParams.gravity = 51;
        if (ActivityManager.isHighEndGfx()) {
            barParams.flags |= 16777216;
            barParams.privateFlags |= 2;
        }
        barParams.setTitle("CoverBarView");
        wm.addView(barView, barParams);
        barView.setVisibility(0);
        this.mCoverBarView = barView;
        HwLog.i("CoverViewManager", "Add CoverStatusBar");
    }

    private ViewGroup getCoverBarView() {
        ViewGroup cached = (ViewGroup) this.mCachedCoverBarView.get();
        if (cached != null) {
            HwLog.w("CoverViewManager", "getCoverBarView from cache");
            return cached;
        }
        View barView;
        if (isLargeCover(this.mContext)) {
            barView = HwKeyguardPolicy.getInst().getCoverStatusBarView();
        } else {
            barView = null;
        }
        if (barView == null) {
            return null;
        }
        ViewGroup parent = (ViewGroup) barView.getParent();
        if (parent != null) {
            parent.removeView(barView);
        }
        if (this.mCoverBarView != null) {
            this.mCoverBarView.removeAllViews();
        }
        RelativeLayout statusBar = this.mCoverBarView == null ? new RelativeLayout(this.mContext) : (RelativeLayout) this.mCoverBarView;
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(-1, -2);
        Rect rect = getCoverWindowSize(this.mContext);
        int marginLeft = this.mContext.getResources().getDimensionPixelSize(R$dimen.cover_statusbar_leftmargin);
        int marginRight = this.mContext.getResources().getDimensionPixelSize(R$dimen.cover_statusbar_rightmargin);
        int marginTop = this.mContext.getResources().getDimensionPixelSize(R$dimen.cover_statusbar_topmargin);
        HwCustCoverScreen hwCustCoverScreen = (HwCustCoverScreen) HwCustUtils.createObj(HwCustCoverScreen.class, new Object[]{this.mContext});
        if (hwCustCoverScreen != null) {
            param.topMargin = hwCustCoverScreen.getTopMarginWithStatusbar(rect.top + marginTop);
            param.leftMargin = hwCustCoverScreen.getLeftMarginWithStatusbar(rect.left + marginLeft);
            param.rightMargin = hwCustCoverScreen.getRightMarginWithStatusbar(rect.left + marginRight);
        } else {
            param.leftMargin = rect.left;
            param.rightMargin = rect.left;
            param.topMargin = rect.top;
        }
        statusBar.addView(barView, param);
        return statusBar;
    }

    public void adjustStatusBarClockView() {
        int i = 0;
        View v = null;
        if (this.mCoverBarView != null) {
            v = this.mCoverBarView.findViewWithTag("hw_statusbar_clock");
        }
        if (v == null) {
            HwLog.w("CoverViewManager", "adjustClockView skiped as view not find.");
            return;
        }
        boolean hideClock = isAlarmCover() || (getApkCoverSize() == 0 && !MusicInfo.getInst().needShowMusicView());
        HwLog.w("CoverViewManager", "adjustClockView set visible: " + getApkCoverSize() + " " + hideClock);
        if (hideClock) {
            i = 8;
        }
        v.setVisibility(i);
    }

    private void addCoverWnd(WindowManager wm, LayoutInflater inflater) {
        CoverScreen coverScreenView = (CoverScreen) inflater.inflate(R$layout.cover_screen, null);
        if (coverScreenView == null) {
            HwLog.e("CoverViewManager", "AddCoverScreen fail to inflate view.");
            return;
        }
        coverScreenView.setSystemUiVisibility(65536);
        HwCustCoverScreen hwCustCoverScreen = (HwCustCoverScreen) HwCustUtils.createObj(HwCustCoverScreen.class, new Object[]{this.mContext});
        if (hwCustCoverScreen != null) {
            hwCustCoverScreen.setCoverViewBackground(coverScreenView);
        }
        LayoutParams coverScreenparams = new LayoutParams(2100);
        coverScreenparams.height = -1;
        coverScreenparams.width = -1;
        coverScreenparams.setTitle("CoverKgScreen");
        coverScreenparams.flags |= 67242752;
        coverScreenparams.privateFlags |= -2147483632;
        coverScreenparams.isEmuiStyle = 1;
        coverScreenparams.inputFeatures |= 4;
        if (ActivityManager.isHighEndGfx()) {
            coverScreenparams.flags |= 16777216;
            coverScreenparams.privateFlags |= 2;
        }
        coverScreenparams.userActivityTimeout = 10000;
        coverScreenparams.screenOrientation = 5;
        dofreezeTrans(209, 0);
        wm.addView(coverScreenView, coverScreenparams);
        this.mCoverScreenView = coverScreenView;
    }

    public void removeCoverScreenWindow() {
        if (this.mContext == null) {
            HwLog.w("CoverViewManager", "handleRemoveCoverScreen, context is null");
            return;
        }
        synchronized (this) {
            this.mApkCovers.clear();
        }
        removeCoverScreenWindowInner();
        FingerprintNavigator.getInst().checkUnexcecuteNavigation(this.mContext);
        HwLog.v("CoverViewManager", "Keyguard view manager handle cover removed");
    }

    public void removeBarView() {
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (wm != null) {
            if (this.mCoverBarView != null) {
                this.mCoverBarView.setVisibility(8);
                wm.removeViewImmediate(this.mCoverBarView);
            }
            this.mCachedCoverBarView = new WeakReference(this.mCoverBarView);
            this.mCoverBarView = null;
        }
    }

    private void removeCoverScreenWindowInner() {
        if (this.mCoverScreenView == null) {
            HwLog.w("CoverViewManager", "Remove coverview skiped as no cover view");
            return;
        }
        this.mCoverScreenView.setVisibility(8);
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (wm == null) {
            HwLog.w("CoverViewManager", "Remove coverview skiped as no WindowManager");
            return;
        }
        wm.removeViewImmediate(this.mCoverScreenView);
        this.mCoverScreenView = null;
        boolean isKeyguardLocked = isKeyguardLocked(this.mContext);
        if (isKeyguardLocked) {
            this.mPM.userActivity(SystemClock.uptimeMillis(), false);
        }
        dofreezeTrans(209, -1);
        if (!isKeyguardLocked) {
            ((StatusBarManager) this.mContext.getApplicationContext().getSystemService("statusbar")).disable(0);
        }
    }

    public static boolean isKeyguardLocked(Context context) {
        if (context == null) {
            HwLog.w("CoverViewManager", "input context is null");
            return false;
        }
        KeyguardManager kgm = (KeyguardManager) context.getSystemService("keyguard");
        if (kgm != null) {
            return kgm.isKeyguardLocked();
        }
        HwLog.w("CoverViewManager", "keyguardmanager is null");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dofreezeTrans(int code, int rot) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder wmBinder = getWindowManager().asBinder();
            if (wmBinder != null) {
                data.writeInterfaceToken("android.view.IWindowManager");
                data.writeInt(rot);
                wmBinder.transact(code, data, reply, 0);
            }
            reply.recycle();
            data.recycle();
        } catch (RemoteException re) {
            HwLog.w("CoverViewManager", "dofreezeTrans err:", re);
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public static boolean isWindowedCover(Context context) {
        return Global.getInt(context.getContentResolver(), "cover_type", 0) == 0;
    }

    private void initClazzObject() {
        try {
            this.mCoverManagerClass = Class.forName("android.cover.CoverManager");
            this.mCoverManagerObject = this.mCoverManagerClass.newInstance();
        } catch (ClassNotFoundException e) {
            HwLog.e("CoverViewManager", "init : ClassNotFoundException = " + e.getMessage());
        } catch (InstantiationException e2) {
            HwLog.e("CoverViewManager", "init : InstantiationException = " + e2.getMessage());
        } catch (IllegalAccessException e3) {
            HwLog.e("CoverViewManager", "init : IllegalAccessException = " + e3.getMessage());
        } catch (Exception e4) {
            HwLog.e("CoverViewManager", "init : Exception = " + e4.getMessage());
        }
    }

    public void registCoverBinder(ICoverViewDelegate.Stub binder) {
        boolean showCover = false;
        if (binder != null) {
            try {
                if (!(this.mCoverManagerClass == null || this.mCoverManagerObject == null)) {
                    this.mCoverManagerClass.getMethod("setCoverViewBinder", new Class[]{IBinder.class, Context.class}).invoke(this.mCoverManagerObject, new Object[]{binder, this.mContext});
                    if (!isCoverOpen()) {
                        showCover = isWindowedCover(this.mContext);
                    }
                    if (showCover) {
                        GlobalContext.getUIHandler().post(new Runnable() {
                            public void run() {
                                CoverViewManager.this.addCoverScreenWindow();
                            }
                        });
                    }
                    HwLog.i("CoverViewManager", "registCoverBinder and show cover :" + showCover);
                    return;
                }
            } catch (NoSuchMethodException e) {
                HwLog.e("CoverViewManager", "CoverManager::setCoverViewBinder Value: setCoverBinder method " + e);
            } catch (IllegalArgumentException e2) {
                HwLog.e("CoverViewManager", "CoverManager::setCoverViewBinder Value: method has wrong parameter " + e2);
            } catch (Exception e3) {
                HwLog.e("CoverViewManager", "CoverManager::setCoverViewBinder Value: other reflect exception " + e3);
            }
        }
        HwLog.e("CoverViewManager", "Keyguard set cover binder fail as null-obj" + binder + " clazz: " + this.mCoverManagerClass);
    }

    public boolean isCoverOpen() {
        Boolean coverOpen = Boolean.valueOf(true);
        try {
            if (this.mCoverManagerClass == null || this.mCoverManagerObject == null) {
                return coverOpen.booleanValue();
            }
            coverOpen = (Boolean) this.mCoverManagerClass.getMethod("isCoverOpen", new Class[0]).invoke(this.mCoverManagerObject, new Object[0]);
            return coverOpen.booleanValue();
        } catch (NoSuchMethodException e) {
            HwLog.e("CoverViewManager", "isCoverOpen : NoSuchMethodException = " + e.getMessage());
        } catch (IllegalArgumentException e2) {
            HwLog.e("CoverViewManager", "isCoverOpen : IllegalArgumentException = " + e2.getMessage());
        } catch (IllegalAccessException e3) {
            HwLog.e("CoverViewManager", "isCoverOpen : IllegalAccessException = " + e3.getMessage());
        } catch (InvocationTargetException e4) {
            HwLog.e("CoverViewManager", "isCoverOpen : InvocationTargetException = " + e4.getMessage());
        } catch (Exception e5) {
            HwLog.e("CoverViewManager", "isCoverOpen : Exception = " + e5.getMessage());
        }
    }

    public void removeCoverScreen() {
        try {
            if (this.mCoverManagerClass != null && this.mCoverManagerObject != null) {
                this.mCoverManagerClass.getMethod("removeCover", new Class[0]).invoke(this.mCoverManagerObject, new Object[0]);
            }
        } catch (NoSuchMethodException e) {
            HwLog.e("CoverViewManager", "LockConfig::removeConverScreen Value: Systemex hasn't removeCover method " + e);
        } catch (IllegalArgumentException e2) {
            HwLog.e("CoverViewManager", "LockConfig::removeConverScreen Value: method has wrong parameter " + e2);
        } catch (Exception e3) {
            HwLog.e("CoverViewManager", "LockConfig::removeConverScreen Value: other reflect exception " + e3);
        }
    }

    public boolean isLargeCover(Context context) {
        if (this.mIsLargeCover != -1) {
            return this.mIsLargeCover == 1;
        }
        String windowLayout = SystemProperties.get("ro.config.huawei_smallwindow", null);
        if (context == null || TextUtils.isEmpty(windowLayout)) {
            HwLog.e("CoverViewManager", "context == null, exception!");
            return true;
        }
        try {
            String[] layoutStr = windowLayout.split(",");
            if (layoutStr.length != 4) {
                return true;
            }
            Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            int disWidth = displayMetrics.widthPixels;
            int coverWidth = Integer.parseInt(layoutStr[2]) - Integer.parseInt(layoutStr[0]);
            int originDisWidth = SystemProperties.getInt("persist.sys.default.res.xres", 1080);
            HwLog.i("CoverViewManager", "isLargeCoverWindowRect coverWidth:" + coverWidth + " disWidth:" + disWidth + " originDisWidth:" + originDisWidth);
            this.mIsLargeCover = ((double) coverWidth) > ((double) originDisWidth) * 0.8d ? 1 : 0;
            return this.mIsLargeCover == 1;
        } catch (RuntimeException e) {
            return true;
        }
    }
}
