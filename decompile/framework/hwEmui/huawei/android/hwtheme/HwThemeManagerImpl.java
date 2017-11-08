package huawei.android.hwtheme;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ActivityThreadUtils;
import android.app.AppGlobals;
import android.app.LoadedApk;
import android.app.Notification;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageParser.Activity;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfoUtils;
import android.content.res.AbsResourcesImpl;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.content.res.HwResources;
import android.content.res.HwResourcesImpl;
import android.content.res.IHwConfiguration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hwtheme.HwThemeManager;
import android.hwtheme.HwThemeManager.IHwThemeManager;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayAdjustments;
import android.view.WindowManager;
import com.huawei.hsm.permission.StubController;
import com.huawei.utils.reflect.EasyInvokeFactory;
import huawei.android.hwutil.IconCache;
import huawei.android.hwutil.IconCache.CacheEntry;
import huawei.android.provider.HwSettings;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import huawei.com.android.internal.widget.HwLockPatternUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HwThemeManagerImpl implements IHwThemeManager {
    private static final String ALARM_CLASS_POSTFIX = "_alarm.";
    private static final String CALENDAR_CLASS_POSTFIX = "_calendar.";
    static final String CURRENT_HOMEWALLPAPER_NAME = "home_wallpaper_0.jpg";
    static final String CURRENT_HOMEWALLPAPER_NAME_PNG = "home_wallpaper_0.png";
    static final String CUST_THEME_NAME = "hw_def_theme";
    static final String CUST_WALLPAPER = "/data/cust/wallpaper/wallpaper1.jpg";
    static final String CUST_WALLPAPER_DIR = "/data/cust/wallpaper/";
    static final String CUST_WALLPAPER_FILE_NAME = "wallpaper1.jpg";
    static final boolean DEBUG = false;
    private static final String DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final ComponentName DRAWERHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static final String EMAIL_CLASS_POSTFIX = "_email.";
    private static final int HWTHEME_DISABLED = 0;
    private static final String HWTHEME_GET_NOTIFICATION_INFO = "com.huawei.hwtheme.permission.GET_NOTIFICATION_INFO";
    private static final String IPACKAGE_MANAGER_DESCRIPTOR = "huawei.com.android.server.IPackageManager";
    private static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final String KEY_DISPLAY_MODE = "display_mode";
    private static final String MESSAGE_CLASS_POSTFIX = "_message.";
    private static final String NOTIFI_CLASS_POSTFIX = "_notification.";
    static final String PATH_DATASKIN_WALLPAPER = "/data/themes/";
    private static final String PROP_WALLPAPER = "ro.config.wallpaper";
    private static final String RINGTONE_CLASS_POSTFIX = "_ringtone.";
    private static final ComponentName SIMPLEHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static final int STYLE_DATA = 1;
    static final String TAG = "HwThemeManagerImpl";
    private static final String THEME_FONTS_BASE_PATH = "/data/skin/fonts/";
    private static final ComponentName UNIHOME_COMPONENT = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private static ResolveInfoUtils resolveInfoUtils = ((ResolveInfoUtils) EasyInvokeFactory.getInvokeUtils(ResolveInfoUtils.class));
    private static boolean sIsDefaultThemeOk = true;
    public static final boolean sIsHwTheme;
    private static final boolean sIsHwtFlipFontOn = (SystemProperties.getInt("ro.config.hwtheme", 0) != 0);
    private static final int transaction_pmGetResourcePackageName = 1004;
    private final IHwConfiguration lastHwConfig = initConfigurationEx();
    private Locale lastLocale = Locale.getDefault();
    private List<ComponentState> mDisablelaunchers = new ArrayList();
    private Map<Integer, Integer> mLauncherMap = new HashMap();
    private Object mLockForClone = new Object();
    private IPackageManager mPackageManagerService;
    private HashMap<String, String> mPackageNameMap = new HashMap();
    private CacheEntry mTempRemovedEntry;

    private static class ComponentState {
        int mSetState = 2;
        ComponentName mlauncherComponent;

        public ComponentState(ComponentName name) {
            this.mlauncherComponent = name;
        }

        public void setComponentEnable(int userId) {
            Log.d(HwThemeManagerImpl.TAG, "updateSimpleUIConfig mlauncherComponent =" + this.mlauncherComponent + ",mSetState=" + this.mSetState);
            try {
                AppGlobals.getPackageManager().setComponentEnabledSetting(this.mlauncherComponent, this.mSetState, 1, userId);
            } catch (Exception e) {
                Log.e(HwThemeManagerImpl.TAG, "setComponentEnabledSetting  because e: " + e);
            }
        }
    }

    static {
        boolean z;
        if (SystemProperties.getInt("ro.config.hwtheme", 0) > 0) {
            z = true;
        } else {
            z = false;
        }
        sIsHwTheme = z;
    }

    public IHwConfiguration initConfigurationEx() {
        IHwConfiguration tmpExtraConfig = null;
        try {
            return (IHwConfiguration) Class.forName("android.content.res.ConfigurationEx").newInstance();
        } catch (Exception e) {
            Log.e("Configuration", "reflection exception is " + e);
            return tmpExtraConfig;
        }
    }

    public void initForThemeFont(Configuration config) {
        ((ConfigurationEx) this.lastHwConfig).hwtheme = ((ConfigurationEx) config.extraConfig).hwtheme;
        if (config.locale != null) {
            this.lastLocale = (Locale) config.locale.clone();
        }
    }

    public HwThemeManagerImpl() {
        initLauncherComponent();
    }

    public boolean isHwTheme() {
        return sIsHwTheme;
    }

    private void setThemeWallpaper(String fn, Context ctx) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        File file = new File(fn);
        if (file.exists()) {
            WallpaperManager wpm = (WallpaperManager) ctx.getSystemService("wallpaper");
            InputStream inputStream = null;
            try {
                InputStream ips = new FileInputStream(file);
                try {
                    wpm.setStream(ips);
                    if (ips != null) {
                        try {
                            ips.close();
                        } catch (Exception e3) {
                        }
                    }
                    inputStream = ips;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = ips;
                    Log.w(TAG, "pwm setwallpaper not found err:", e);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e5) {
                        }
                    }
                    return;
                } catch (IOException e6) {
                    e2 = e6;
                    inputStream = ips;
                    try {
                        Log.w(TAG, "pwm setwallpaper io err:", e2);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e7) {
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e8) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = ips;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e9) {
                e = e9;
                Log.w(TAG, "pwm setwallpaper not found err:", e);
                if (inputStream != null) {
                    inputStream.close();
                }
                return;
            } catch (IOException e10) {
                e2 = e10;
                Log.w(TAG, "pwm setwallpaper io err:", e2);
                if (inputStream != null) {
                    inputStream.close();
                }
                return;
            }
            return;
        }
        Log.w(TAG, "pwm setwallpaper stopped");
    }

    private boolean isFileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    public void setTheme(String theme_path) {
    }

    public boolean installHwTheme(String themePath) {
        return installHwTheme(themePath, false, UserHandle.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper) {
        return installHwTheme(themePath, setwallpaper, UserHandle.myUserId());
    }

    public boolean installHwTheme(String themePath, boolean setwallpaper, int userId) {
        return pmInstallHwTheme(1002, "pmInstallHwTheme", themePath, setwallpaper, userId);
    }

    public boolean installDefaultHwTheme(Context ctx) {
        return installDefaultHwTheme(ctx, UserHandle.myUserId());
    }

    public boolean installDefaultHwTheme(Context ctx, int userId) {
        String defname = getDefaultHwThemePack(ctx);
        Log.w(TAG, "the default theme: " + defname);
        if (!isFileExists(defname)) {
            return false;
        }
        pmCreateThemeFolder(HwLockPatternUtils.transaction_setActiveVisitorPasswordState, "pmCreateThemeFolder", null, false, userId);
        return installHwTheme(defname, false, userId);
    }

    public boolean makeIconCache(boolean clearall) {
        return true;
    }

    public boolean saveIconToCache(Bitmap bitmap, String fn, boolean clearold) {
        return true;
    }

    private String getDefaultHwThemePack(Context ctx) {
        String colors_themes = Systemex.getString(ctx.getContentResolver(), "colors_themes");
        if (TextUtils.isEmpty(colors_themes)) {
            Log.w(TAG, "colors and themes is empty!");
            return Systemex.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String[] colorsAndThemes = colors_themes.split(";");
        HashMap<String, String> mHwColorThemes = new HashMap();
        for (String str : colorsAndThemes) {
            String[] color_theme = str.split(",");
            if (color_theme.length != 2 || color_theme[0].isEmpty()) {
                Log.w(TAG, "invalid color and theme : " + str);
            } else {
                mHwColorThemes.put(color_theme[0].toLowerCase(Locale.US), color_theme[1]);
            }
        }
        if (mHwColorThemes.isEmpty()) {
            Log.w(TAG, "has no valid color-theme!");
            return Systemex.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String mColor = SystemProperties.get("ro.config.devicecolor");
        String mBackColor = SystemProperties.get("ro.config.backcolor");
        if (mColor == null) {
            return Systemex.getString(ctx.getContentResolver(), CUST_THEME_NAME);
        }
        String hwThemePath = (String) mHwColorThemes.get(mColor.toLowerCase(Locale.US));
        if (isFileExists(hwThemePath)) {
            Systemex.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
            Log.w(TAG, "The TP color: " + mColor + ", Theme path: " + hwThemePath);
            return hwThemePath;
        }
        if (hwThemePath == null && !TextUtils.isEmpty(mBackColor)) {
            String mGroupColor = mColor + "+" + mBackColor;
            hwThemePath = (String) mHwColorThemes.get(mGroupColor.toLowerCase(Locale.US));
            if (isFileExists(hwThemePath)) {
                Systemex.putString(ctx.getContentResolver(), CUST_THEME_NAME, hwThemePath);
                Log.w(TAG, "The group color: " + mGroupColor + ", Theme path: " + hwThemePath);
                return hwThemePath;
            }
        }
        return Systemex.getString(ctx.getContentResolver(), CUST_THEME_NAME);
    }

    public void updateConfiguration() {
        try {
            Configuration curConfig = new Configuration();
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            curConfig.extraConfig.setConfigItem(1, curConfig.extraConfig.getConfigItem(1) + 1);
            ActivityManagerNative.getDefault().updateConfiguration(curConfig);
        } catch (RemoteException e) {
        }
    }

    public Resources getResources(AssetManager assets, DisplayMetrics dm, Configuration config, DisplayAdjustments displayAdjustments, IBinder token) {
        if (sIsHwTheme) {
            return new HwResources(assets, dm, config, displayAdjustments, token);
        }
        return new Resources(assets, dm, config, displayAdjustments);
    }

    public Resources getResources() {
        if (sIsHwTheme) {
            return new HwResources();
        }
        return new Resources();
    }

    public Resources getResources(boolean system) {
        if (sIsHwTheme) {
            return new HwResources(system);
        }
        return new Resources();
    }

    public Resources getResources(ClassLoader classLoader) {
        if (sIsHwTheme) {
            return new HwResources(classLoader);
        }
        return new Resources(classLoader);
    }

    public AbsResourcesImpl getHwResourcesImpl() {
        return new HwResourcesImpl();
    }

    public InputStream getDefaultWallpaperIS(Context context, int userId) {
        InputStream inputStream = null;
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
        try {
            String path = SystemProperties.get(PROP_WALLPAPER);
            if (!TextUtils.isEmpty(path)) {
                File googleCustWallpaperFile = new File(path);
                if (googleCustWallpaperFile.exists()) {
                    InputStream is = new FileInputStream(googleCustWallpaperFile);
                    try {
                        wallpaperManager.setStream(is);
                        return is;
                    } catch (IOException e) {
                        inputStream = is;
                    }
                }
            }
            if (new File(CUST_WALLPAPER_DIR, CUST_WALLPAPER_FILE_NAME).exists()) {
                inputStream = new FileInputStream(CUST_WALLPAPER);
            } else {
                File file = new File(PATH_DATASKIN_WALLPAPER + userId + "/wallpaper/", CURRENT_HOMEWALLPAPER_NAME);
                if (!file.exists()) {
                    file = new File(PATH_DATASKIN_WALLPAPER + userId + "/wallpaper/", CURRENT_HOMEWALLPAPER_NAME_PNG);
                }
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                }
            }
        } catch (IOException e2) {
        }
        if (inputStream == null) {
            inputStream = context.getResources().openRawResource(17302107);
        }
        return inputStream;
    }

    @Deprecated
    public Resources updateHwtResource(ActivityThread mainThread, Resources res, String mResDir, LoadedApk loadApk) {
        Resources nr = res;
        if (!sIsHwTheme || mResDir == null) {
            return nr;
        }
        if (res == null || !res.getAssets().isUpToDate()) {
            return ActivityThreadUtils.getTopLevelResources(mainThread, mResDir, 0, null, loadApk);
        }
        return nr;
    }

    public Resources updateHwtResource(ActivityThread mainThread, Resources res, String mResDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, LoadedApk loadApk) {
        Resources nr = res;
        if (!sIsHwTheme || mResDir == null) {
            return nr;
        }
        if (res == null || !res.getAssets().isUpToDate()) {
            return ActivityThreadUtils.getTopLevelResources(mainThread, mResDir, splitResDirs, overlayDirs, libDirs, 0, null, loadApk);
        }
        return nr;
    }

    public void updateIconCache(PackageItemInfo packageItemInfo, String name, String packageName, int icon, int packageIcon) {
        if (sIsHwTheme) {
            String resPackageName = packageName;
            String idAndPackageName = (icon != 0 ? icon : packageIcon) + "#" + packageName;
            boolean isActivityIcon = (icon == 0 || name == null) ? false : true;
            CacheEntry ce;
            if (!IconCache.contains(idAndPackageName)) {
                String tmpName;
                if (name != null) {
                    tmpName = name;
                } else {
                    tmpName = packageName;
                }
                String lc = tmpName != null ? tmpName.toLowerCase() : null;
                if (lc != null && lc.indexOf("shortcut") < 0 && lc.indexOf(".cts") < 0) {
                    ce = new CacheEntry();
                    ce.name = tmpName;
                    ce.type = isActivityIcon ? 1 : 0;
                    IconCache.add(idAndPackageName, ce);
                }
            } else if (isActivityIcon && IconCache.get(idAndPackageName).type != 1) {
                String nameLC = name.toLowerCase();
                if (nameLC.indexOf("shortcut") < 0 && nameLC.indexOf(".cts") < 0) {
                    ce = new CacheEntry();
                    ce.name = name;
                    ce.type = 1;
                    IconCache.update(idAndPackageName, ce);
                }
            }
        }
    }

    public void updateResolveInfoIconCache(ResolveInfo resolveInfo, int icon, String resolvePackageName) {
        if (!(icon == 0 || resolveInfo == null)) {
            if (resolvePackageName != null) {
                updateIconCache(null, null, resolvePackageName, icon, 0);
                return;
            }
            ComponentInfo ci = resolveInfoUtils.getComponentInfo(resolveInfo);
            if (ci != null) {
                updateIconCache(ci, ci.name, ci.packageName, icon, 0);
            }
        }
    }

    public void removeIconCache(String name, String packageName, int icon, int packageIcon) {
        if (sIsHwTheme) {
            int id = icon != 0 ? icon : packageIcon;
            String idAndPackageName = id + "#" + getResourcePackageName(1004, "getResourcesPackageName", packageName, id);
            if (IconCache.contains(idAndPackageName)) {
                this.mTempRemovedEntry = IconCache.get(idAndPackageName);
                IconCache.remove(idAndPackageName);
            }
        }
    }

    public void restoreIconCache(String packageName, int icon) {
        if (this.mTempRemovedEntry != null) {
            IconCache.add(icon + "#" + packageName, this.mTempRemovedEntry);
            this.mTempRemovedEntry = null;
        }
    }

    public int getThemeColor(int[] data, int index, TypedValue value, Resources resources, boolean flag) {
        TypedValue mValue = value;
        if (sIsHwTheme && flag && value.resourceId != 0) {
            return resources.getColor(value.resourceId);
        }
        return data[index + 1];
    }

    public int getHwThemeLauncherIconSize(ActivityManager am, Resources resources) {
        if (!sIsHwTheme) {
            return am.getLauncherLargeIconSize();
        }
        int iconSize;
        Resources res = resources;
        int configIconSize = SystemProperties.getInt("ro.config.app_big_icon_size", -1);
        int multiResSize = SystemProperties.getInt("persist.sys.res.icon_size", -1);
        if (configIconSize > 0 && multiResSize > 0) {
            configIconSize = multiResSize;
        }
        if (configIconSize == -1) {
            iconSize = (int) resources.getDimension(34472075);
        } else {
            iconSize = configIconSize;
        }
        return iconSize;
    }

    public boolean isTRingtones(String path) {
        return path.indexOf(RINGTONE_CLASS_POSTFIX) > 0;
    }

    public boolean isTNotifications(String path) {
        if (path.indexOf(NOTIFI_CLASS_POSTFIX) > 0 || path.indexOf(CALENDAR_CLASS_POSTFIX) > 0 || path.indexOf(EMAIL_CLASS_POSTFIX) > 0 || path.indexOf(MESSAGE_CLASS_POSTFIX) > 0) {
            return true;
        }
        return false;
    }

    public boolean isTAlarms(String path) {
        return path.indexOf(ALARM_CLASS_POSTFIX) > 0;
    }

    public IHwConfiguration initHwConfiguration() {
        return new ConfigurationEx();
    }

    private String getResourcePackageNameFromMap(String packageName) {
        String str;
        synchronized (this.mPackageNameMap) {
            str = (String) this.mPackageNameMap.get(packageName);
        }
        return str;
    }

    private void addResourcePackageName(String oldPkgName, String newPkgName) {
        synchronized (this.mPackageNameMap) {
            this.mPackageNameMap.put(oldPkgName, newPkgName);
        }
    }

    public int getShadowcolor(TypedArray a, int attr) {
        return a.getColor(attr, 0);
    }

    public void applyDefaultHwTheme(boolean checkState, Context mContext) {
        applyDefaultHwTheme(checkState, mContext, UserHandle.myUserId());
    }

    public void applyDefaultHwTheme(boolean checkState, Context mContext, int userId) {
        boolean skinExist = new File(HwThemeManager.HWT_PATH_THEME + "/" + userId).exists();
        boolean installFlagExist = isFileExists(HwThemeManager.HWT_PATH_SKIN_INSTALL_FLAG);
        if (!sIsHwTheme) {
            return;
        }
        if (!skinExist || ((checkState && !sIsDefaultThemeOk) || installFlagExist)) {
            setIsDefaultThemeOk(installDefaultHwTheme(mContext, userId));
        }
    }

    private static void setIsDefaultThemeOk(boolean isOk) {
        sIsDefaultThemeOk = isOk;
    }

    public void addSimpleUIConfig(Activity activity) {
        if (activity.metaData.getBoolean("simpleuimode", false)) {
            ActivityInfo activityInfo = activity.info;
            activityInfo.configChanges |= StubController.PERMISSION_SMSLOG_WRITE;
        }
    }

    private IPackageManager getPackageManager() {
        if (this.mPackageManagerService == null) {
            this.mPackageManagerService = Stub.asInterface(ServiceManager.getService("package"));
        }
        return this.mPackageManagerService;
    }

    private boolean pmCreateThemeFolder(int code, String transactName, String paramName, boolean paramValue, int userId) {
        IBinder pmsBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        if (pmsBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeInt(userId);
                pmsBinder.transact(code, data, reply, 0);
                reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        return false;
    }

    private boolean pmInstallHwTheme(int code, String transactName, String paramName, boolean paramValue, int userId) {
        int i = 0;
        IBinder pmsBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean result = false;
        if (pmsBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeString(paramName);
                if (paramValue) {
                    i = 1;
                }
                data.writeInt(i);
                data.writeInt(userId);
                pmsBinder.transact(code, data, reply, 0);
                reply.readException();
                result = reply.readInt() != 0;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        return result;
    }

    private void initLauncherComponent() {
        this.mDisablelaunchers.clear();
        this.mLauncherMap.clear();
        this.mDisablelaunchers.add(new ComponentState(UNIHOME_COMPONENT));
        this.mDisablelaunchers.add(new ComponentState(SIMPLEHOME_COMPONENT));
        this.mDisablelaunchers.add(new ComponentState(DRAWERHOME_COMPONENT));
        this.mLauncherMap.put(Integer.valueOf(0), Integer.valueOf(0));
        this.mLauncherMap.put(Integer.valueOf(1), Integer.valueOf(0));
        this.mLauncherMap.put(Integer.valueOf(2), Integer.valueOf(1));
        this.mLauncherMap.put(Integer.valueOf(4), Integer.valueOf(2));
    }

    private void enableLaunchers(int type, int userId) {
        int i = 0;
        while (i < this.mDisablelaunchers.size()) {
            ComponentState componentstate = (ComponentState) this.mDisablelaunchers.get(i);
            componentstate.mSetState = i == type ? 1 : 2;
            componentstate.setComponentEnable(userId);
            i++;
        }
    }

    public void retrieveSimpleUIConfig(ContentResolver cr, Configuration config, int userId) {
        int simpleuiVal = System.getIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, 0, ActivityManager.getCurrentUser());
        int launcherConfig = SystemProperties.getInt("ro.config.hw_simpleui_enable", 0);
        Log.d(TAG, "updateSimpleUIConfig simpleuiVal =" + simpleuiVal + ",launcherConfig=" + launcherConfig);
        if (simpleuiVal == 0) {
            simpleuiVal = launcherConfig == 0 ? 1 : launcherConfig;
        }
        config.extraConfig.setConfigItem(2, simpleuiVal);
        try {
            enableLaunchers(((Integer) this.mLauncherMap.get(Integer.valueOf(simpleuiVal))).intValue(), userId);
        } catch (Exception e) {
            Log.d(TAG, "retrieveSimpleUIConfig enableLaunchers e=" + e);
        }
    }

    public void updateSimpleUIConfig(ContentResolver cr, Configuration config, int configChanges) {
        if ((StubController.PERMISSION_SMSLOG_WRITE & configChanges) != 0) {
            Log.w(TAG, "updateSimpleUIConfig SIMPLEUI_MODE putIntForUser =" + config.extraConfig.getConfigItem(2));
            System.putIntForUser(cr, HwSettings.System.SIMPLEUI_MODE, config.extraConfig.getConfigItem(2), ActivityManager.getCurrentUser());
        }
    }

    public Bitmap getThemeBitmap(Resources res, int id, Rect padding) {
        TypedValue outValue = new TypedValue();
        res.getValue(id, outValue, true);
        return res.getThemeBitmap(outValue, id, padding);
    }

    public Bitmap getThemeBitmap(Resources res, int id) {
        return getThemeBitmap(res, id, null);
    }

    private static boolean isNoThemeFont() {
        File fontdir = new File(THEME_FONTS_BASE_PATH, "");
        if (!fontdir.exists() || fontdir.list() == null || fontdir.list().length == 0) {
            return true;
        }
        return false;
    }

    public void setThemeFont() {
        if (sIsHwtFlipFontOn) {
            Canvas.freeCaches();
            Canvas.freeTextLayoutCaches();
        }
    }

    public boolean setThemeFontOnConfigChg(Configuration newConfig) {
        Locale locale = null;
        if (sIsHwtFlipFontOn) {
            boolean isHwThemeChanged = false;
            if (!(this.lastHwConfig == null || newConfig == null)) {
                if ((StubController.PERMISSION_CALLLOG_WRITE & this.lastHwConfig.updateFrom(newConfig.extraConfig)) != 0) {
                    isHwThemeChanged = true;
                }
            }
            boolean isLocaleChanged = false;
            if (!(newConfig == null || newConfig.locale == null || (this.lastLocale != null && this.lastLocale.equals(newConfig.locale)))) {
                if (newConfig.locale != null) {
                    locale = (Locale) newConfig.locale.clone();
                }
                this.lastLocale = locale;
                isLocaleChanged = true;
            }
            if (isHwThemeChanged || isLocaleChanged) {
                setThemeFont();
                return true;
            }
        }
        return false;
    }

    public boolean isTargetFamily(String familyName) {
        return familyName != null ? familyName.equals("chnfzxh") : false;
    }

    public boolean shouldUseAdditionalChnFont(String familyName) {
        if (isTargetFamily(familyName)) {
            String curLang = Locale.getDefault().getLanguage();
            if (isNoThemeFont() && (curLang.contains("zh") || curLang.contains("en"))) {
                return true;
            }
        }
        return false;
    }

    public Bitmap generateBitmap(Context context, Bitmap bm, int width, int height) {
        if (bm == null) {
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(FreezeScreenScene.WINDOW_PARAM);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        bm.setDensity(metrics.noncompatDensityDpi);
        Point size = new Point();
        display.getRealSize(size);
        int max = size.x > size.y ? size.x : size.y;
        int min = size.x < size.y ? size.x : size.y;
        if (width <= 0 || height <= 0 || ((bm.getWidth() == width && bm.getHeight() == height) || ((bm.getWidth() == min && bm.getHeight() == max) || (bm.getWidth() == max && bm.getHeight() == max)))) {
            return bm;
        }
        try {
            Bitmap newbm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            if (newbm == null) {
                Log.w(TAG, "Can't generate default bitmap, newbm = null");
                return bm;
            }
            newbm.setDensity(metrics.noncompatDensityDpi);
            Canvas c = new Canvas(newbm);
            Rect targetRect = new Rect();
            targetRect.right = bm.getWidth();
            targetRect.bottom = bm.getHeight();
            int deltaw = width - targetRect.right;
            int deltah = height - targetRect.bottom;
            if (deltaw > 0 || deltah > 0) {
                float scale = Math.max(((float) width) / ((float) targetRect.right), ((float) height) / ((float) targetRect.bottom));
                targetRect.right = (int) (((float) targetRect.right) * scale);
                targetRect.bottom = (int) (((float) targetRect.bottom) * scale);
                deltaw = width - targetRect.right;
                deltah = height - targetRect.bottom;
            }
            targetRect.offset(deltaw / 2, deltah / 2);
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            c.drawBitmap(bm, null, targetRect, paint);
            bm.recycle();
            return newbm;
        } catch (OutOfMemoryError e) {
            Log.w(TAG, "Can't generate default bitmap", e);
            return bm;
        }
    }

    private String getResourcePackageName(int code, String transactName, String packageName, int icon) {
        String name = getResourcePackageNameFromMap(packageName);
        if (name != null) {
            return name;
        }
        IBinder resBinder = getPackageManager().asBinder();
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result = null;
        if (resBinder != null) {
            try {
                data.writeInterfaceToken("huawei.com.android.server.IPackageManager");
                data.writeString(packageName);
                data.writeInt(icon);
                data.writeInt(UserHandle.myUserId());
                resBinder.transact(code, data, reply, 0);
                reply.readException();
                result = reply.readString();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }
        reply.recycle();
        data.recycle();
        if (!(result == null || result.equals(""))) {
            addResourcePackageName(packageName, result);
        }
        return result;
    }

    public Drawable getJoinBitmap(Context context, Drawable srcDraw, int backgroundId) {
        context.enforceCallingOrSelfPermission(HWTHEME_GET_NOTIFICATION_INFO, "getJoinBitmap");
        if (HwThemeManager.isHwTheme()) {
            Resources r = context.getResources();
            if (r != null) {
                return r.getImpl().getHwResourcesImpl().getJoinBitmap(srcDraw, backgroundId);
            }
        }
        return null;
    }

    public Drawable getClonedDrawable(Context context, Drawable drawable) {
        if (!IS_SUPPORT_CLONE_APP) {
            return drawable;
        }
        Drawable clone = context.getResources().getDrawable(33751335);
        if (clone == null) {
            return drawable;
        }
        if (drawable == null) {
            clone.setColorFilter(context.getColor(33882311), Mode.SRC_IN);
            clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
            return clone;
        }
        int cloneWidth;
        int cloneHeight;
        BitmapDrawable mergedDrawable;
        Float markBgRatio = Float.valueOf(Float.parseFloat(context.getResources().getString(34471943)));
        Float markBgPercentage = Float.valueOf(Float.parseFloat(context.getResources().getString(34471944)));
        int srcWidth = drawable.getIntrinsicWidth();
        int srcHeight = drawable.getIntrinsicHeight();
        int markBgRadius = Math.min((int) (((float) srcWidth) * markBgPercentage.floatValue()), (int) (((float) srcHeight) * markBgPercentage.floatValue()));
        if (clone.getIntrinsicWidth() >= clone.getIntrinsicHeight()) {
            cloneWidth = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneHeight = (int) (((((double) cloneWidth) * 1.0d) * ((double) clone.getIntrinsicHeight())) / ((double) clone.getIntrinsicWidth()));
        } else {
            cloneHeight = (int) (((float) markBgRadius) / markBgRatio.floatValue());
            cloneWidth = (int) (((((double) cloneHeight) * 1.0d) * ((double) clone.getIntrinsicWidth())) / ((double) clone.getIntrinsicHeight()));
        }
        synchronized (this.mLockForClone) {
            Bitmap newBitMap = Bitmap.createBitmap(srcWidth, srcWidth, Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitMap);
            drawable.setBounds(0, 0, srcWidth, srcWidth);
            drawable.draw(canvas);
            Paint mPaint = new Paint();
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
            mPaint.setAntiAlias(true);
            canvas.save();
            mPaint.setColor(context.getColor(33882311));
            canvas.translate((float) (srcWidth - markBgRadius), (float) (srcHeight - markBgRadius));
            canvas.drawCircle(((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, ((float) markBgRadius) / 2.0f, mPaint);
            canvas.translate(((float) (markBgRadius - cloneWidth)) / 2.0f, ((float) (markBgRadius - cloneHeight)) / 2.0f);
            canvas.scale((((float) cloneWidth) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) clone.getIntrinsicWidth()), (((float) cloneHeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) clone.getIntrinsicHeight()));
            clone.setBounds(0, 0, clone.getIntrinsicWidth(), clone.getIntrinsicHeight());
            clone.draw(canvas);
            canvas.restore();
            mPaint.setXfermode(null);
            mergedDrawable = new BitmapDrawable(context.getResources(), newBitMap);
            if (drawable instanceof BitmapDrawable) {
                mergedDrawable.setTargetDensity(((BitmapDrawable) drawable).getBitmap().getDensity());
            }
        }
        return mergedDrawable;
    }

    public Drawable getHwBadgeDrawable(Notification notification, Context context, Drawable drawable) {
        if (!notification.extras.getBoolean("com.huawei.isIntentProtectedApp")) {
            return null;
        }
        Drawable trustSpace = context.getResources().getDrawable(33751344);
        trustSpace.setColorFilter(context.getColor(33882311), Mode.SRC_IN);
        trustSpace.setBounds(0, 0, trustSpace.getIntrinsicWidth(), trustSpace.getIntrinsicHeight());
        return trustSpace;
    }
}
