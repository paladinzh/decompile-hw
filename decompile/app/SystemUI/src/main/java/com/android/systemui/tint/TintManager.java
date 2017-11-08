package com.android.systemui.tint;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.HwNavigationBarTransitions;
import com.android.systemui.statusbar.phone.HwPhoneStatusBarTransitions;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.android.immersion.ImmersionStyle;
import java.util.HashMap;
import java.util.Observable;

public class TintManager extends Observable {
    public static final boolean DEBUG = Log.isLoggable("TintManager", 3);
    private static HashMap<String, Integer> mTints = new HashMap();
    private static TintManager sInstance = new TintManager();
    private boolean isDockedStackExists = false;
    private int mAvgKeyguardWallPaperColor = 3;
    private int mAvgWallPaperColor = 3;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (TintManager.DEBUG) {
                    Log.d("TintManager", "ACTION = " + intent.getAction());
                }
                if ("com.android.systemui.action.SET_WALLPAPER_COLOR".equals(intent.getAction())) {
                    int avgColor = intent.getIntExtra("avgcolor", TintManager.this.mAvgWallPaperColor);
                    TintManager.this.mAvgWallPaperColor = avgColor;
                    HwLog.i("TintManager", "avgColor=" + avgColor);
                    TintManager.this.updateBarBgColorWhenWallpaperChanged();
                    TintManager.this.updateBarTint();
                }
            }
        }
    };
    private Context mContext;
    private int mEmuiLightStyle = 0;
    private int mEmuiStyle = -1;
    private boolean mIsFullExpanded = false;
    private boolean mIsLauncherEdit = false;
    private boolean mIsNavigationBarTranslucent = false;
    private boolean mIsRegistered = false;
    private boolean mIsStatusBarTranslucent = false;
    private boolean mIsUsedLiveWallpaper = false;
    private boolean mKeyguardWallPaperUseMask = false;
    private boolean mLastIsLauncher = true;
    private int mLastNaviColor = -1275068417;
    private int mLastStatusColor = -1275068417;
    private boolean mLightBar = false;
    private HwNavigationBarTransitions mNavigationBarTransitions;
    private HwPhoneStatusBarTransitions mPhoneStatusBarTransitions = null;
    private boolean mScreenLocked = false;
    private int mScreenLockedNavBarTintColor = -1275068417;
    private int mScreenLockedStatusBarTintColor = -1275068417;
    private boolean mWallPaperUseMask = false;
    private WallpaperManager mWallpaperManager = null;

    static {
        if (mTints == null) {
            mTints.put("statusBarType", Integer.valueOf(-1275068417));
            mTints.put("navigationBarType", Integer.valueOf(-1275068417));
        }
    }

    public void setScreenLocked(boolean screenLocked) {
        int i = 0;
        if (DEBUG) {
            HwLog.i("TintManager", "setScreenLocked:" + screenLocked);
        }
        this.mScreenLocked = screenLocked;
        if (this.mScreenLocked) {
            if (this.mPhoneStatusBarTransitions != null) {
                HwPhoneStatusBarTransitions hwPhoneStatusBarTransitions = this.mPhoneStatusBarTransitions;
                if (-2 == this.mEmuiStyle) {
                    i = -16777216;
                }
                hwPhoneStatusBarTransitions.setBackgroundColor(i);
            }
        } else if (this.mEmuiStyle == 0 && !this.mLightBar) {
            this.mPhoneStatusBarTransitions.restoreBackgroundColor();
        }
        updateBarBgColorWhenWallpaperChanged();
    }

    private TintManager() {
    }

    public static TintManager getInstance() {
        return sInstance;
    }

    public int getIconColorByType(String type) {
        if ("statusBarType".equals(type)) {
            return this.mLastStatusColor;
        }
        if ("navigationBarType".equals(type)) {
            return this.mLastNaviColor;
        }
        return -1275068417;
    }

    private int getIconColorByTypeInner(String type) {
        int i = -1291845632;
        if (DEBUG) {
            Log.d("TintManager", "getIconColorByType:type=" + type + ",mEmuiStyle=" + this.mEmuiStyle + ",mIsUsedLiveWallpaper=" + this.mIsUsedLiveWallpaper + ",mIsLauncherEdit=" + this.mIsLauncherEdit + ",mIsStatusBarTranslucent=" + this.mIsStatusBarTranslucent + ",mIsNavigationBarTranslucent=" + this.mIsNavigationBarTranslucent + ",mAvgWallPaperColor=" + this.mAvgWallPaperColor);
        }
        if (this.mScreenLocked) {
            return "statusBarType".equals(type) ? this.mScreenLockedStatusBarTintColor : this.mScreenLockedNavBarTintColor;
        } else if (this.mIsFullExpanded && "navigationBarType".equals(type)) {
            return -1275068417;
        } else {
            if ((-1 != this.mEmuiStyle && "navigationBarType".equals(type)) || -2 == this.mEmuiStyle) {
                return -1275068417;
            }
            if (-1 == this.mEmuiStyle && (this.mIsUsedLiveWallpaper || this.mIsLauncherEdit)) {
                return -1275068417;
            }
            if ("statusBarType".equals(type)) {
                if (-1 == this.mEmuiStyle) {
                    if (1 != this.mAvgWallPaperColor) {
                        i = -1275068417;
                    }
                    return i;
                } else if (!isEmuiStyle()) {
                    return this.mLightBar ? -1291845632 : -1275068417;
                }
            } else if ("navigationBarType".equals(type)) {
                if (-1 == this.mEmuiStyle) {
                    if (1 != this.mAvgWallPaperColor) {
                        i = -1275068417;
                    }
                    return i;
                } else if (!isEmuiStyle()) {
                    return -1275068417;
                }
            }
            return ((Integer) mTints.get(type)).intValue();
        }
    }

    public int getIconColorByType(String type, int color) {
        boolean isWhiteBackgroundColor = false;
        if (getIconColorByType(type) == -1291845632) {
            isWhiteBackgroundColor = true;
        }
        if (isWhiteBackgroundColor) {
            if ((color & 16777215) == 16777215) {
                return -1291845632;
            }
        } else if ((color & 16777215) == 0) {
            return -1275068417;
        }
        return color;
    }

    public int getSemiStatusBarBgColor() {
        if (getIconColorByType("statusBarType") == -1275068417) {
            return -16777216;
        }
        return -526345;
    }

    public void updateBarBgColor(int emuiStyle, int statusBarColor, int navigationBarColor, int isEmuiLight) {
        boolean z;
        HwLog.i("TintManager", "updateBarBgColor:emuiStyle=" + emuiStyle + ",statusBarColor=" + String.format("#%08X", new Object[]{Integer.valueOf(statusBarColor)}) + ",navigationBarColor=" + String.format("#%08X", new Object[]{Integer.valueOf(navigationBarColor)}) + ", isEmuiLight=" + isEmuiLight);
        this.mEmuiStyle = emuiStyle;
        this.mEmuiLightStyle = isEmuiLight;
        if (DEBUG) {
            String str = "TintManager";
            StringBuilder append = new StringBuilder().append("mIsEmuiStyle = ");
            if (this.mEmuiStyle == 1) {
                z = true;
            } else {
                z = false;
            }
            Log.d(str, append.append(z).toString());
        }
        if (-1 == this.mEmuiStyle) {
            if (this.mWallpaperManager == null && this.mContext != null) {
                this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService("wallpaper");
            }
            if (this.mWallpaperManager == null || this.mWallpaperManager.getWallpaperInfo() == null) {
                this.mIsUsedLiveWallpaper = false;
            } else {
                this.mIsUsedLiveWallpaper = true;
            }
        }
        if (this.mPhoneStatusBarTransitions != null) {
            if (this.mEmuiStyle == 0) {
                this.mPhoneStatusBarTransitions.restoreBackgroundColor();
            } else {
                if (this.mWallPaperUseMask && -1 == this.mEmuiStyle) {
                    z = true;
                } else {
                    z = false;
                }
                if (!z) {
                    int i;
                    HwPhoneStatusBarTransitions hwPhoneStatusBarTransitions = this.mPhoneStatusBarTransitions;
                    if (-2 == this.mEmuiStyle) {
                        i = -16777216;
                    } else {
                        i = 0;
                    }
                    hwPhoneStatusBarTransitions.setBackgroundColor(i);
                }
            }
        }
        updateNavigationBarColor();
        int statusBarIconTint = getSuggestionColor(statusBarColor);
        int navigationBarIconTint = getSuggestionColor(navigationBarColor);
        Log.d("TintManager", "statusBarIconTint=" + String.format("#%08X", new Object[]{Integer.valueOf(statusBarIconTint)}) + ",navigationBarIconTint=" + String.format("#%08X", new Object[]{Integer.valueOf(navigationBarColor)}));
        mTints.put("statusBarType", Integer.valueOf(statusBarIconTint));
        mTints.put("navigationBarType", Integer.valueOf(navigationBarIconTint));
        updateBarBgColorWhenWallpaperChanged();
    }

    public boolean isEmuiStyle() {
        return this.mEmuiStyle == 1;
    }

    private void updateNavigationBarColor() {
        if (this.mNavigationBarTransitions != null) {
            if (-2 == this.mEmuiStyle) {
                this.mNavigationBarTransitions.setBackgroundColor(-16777216);
            } else if (-1 == this.mEmuiStyle || (1 == this.mEmuiStyle && (1 == this.mEmuiLightStyle || this.mEmuiLightStyle == 0))) {
                this.mNavigationBarTransitions.setBackgroundColor(0);
            } else {
                this.mNavigationBarTransitions.restoreBackgroundColor();
            }
        }
    }

    public boolean isUseTint() {
        return (this.mEmuiStyle == 1 || this.mEmuiStyle == -1) ? true : this.mScreenLocked;
    }

    public void updateBarBgColorWhenWallpaperChanged() {
        if (this.mPhoneStatusBarTransitions != null) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public void runInUI() {
                    if (TintManager.this.mWallPaperUseMask && -1 == TintManager.this.mEmuiStyle) {
                        if (TintManager.this.isOrientionLandscape()) {
                            TintManager.this.mPhoneStatusBarTransitions.setBackgroundDrawable(HwSystemUIApplication.getContext().getDrawable(R.drawable.dark_mask_statusbar_new));
                        } else {
                            TintManager.this.mPhoneStatusBarTransitions.setBackgroundColor(-2 == TintManager.this.mEmuiStyle ? -16777216 : 0);
                        }
                    }
                    TintManager.this.updateBarTint();
                }
            });
        }
    }

    public void setDockedStackExists(boolean isDockedStackExists) {
        this.isDockedStackExists = isDockedStackExists;
    }

    public void updateBarBgColorWhenKeyguardWallpaperChanged() {
        if (this.mPhoneStatusBarTransitions != null) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public void runInUI() {
                    if (TintManager.this.mKeyguardWallPaperUseMask && TintManager.this.isScreenLocked()) {
                        if (TintManager.this.isOrientionLandscape()) {
                            TintManager.this.mPhoneStatusBarTransitions.setBackgroundDrawable(HwSystemUIApplication.getContext().getDrawable(R.drawable.dark_mask_statusbar_new));
                        } else {
                            TintManager.this.mPhoneStatusBarTransitions.setBackgroundColor(-2 == TintManager.this.mEmuiStyle ? -16777216 : 0);
                        }
                    }
                    TintManager.this.mScreenLockedStatusBarTintColor = 1 == TintManager.this.mAvgKeyguardWallPaperColor ? -1291845632 : -1275068417;
                    TintManager.this.updateBarTint();
                }
            });
        }
    }

    public boolean isOrientionLandscape() {
        boolean z = false;
        if (HwSystemUIApplication.getContext() == null || HwSystemUIApplication.getContext().getResources() == null || HwSystemUIApplication.getContext().getResources().getConfiguration() == null) {
            return false;
        }
        if (2 == HwSystemUIApplication.getContext().getResources().getConfiguration().orientation) {
            z = true;
        }
        return z;
    }

    public static int getSuggestionColor(int color) {
        return ImmersionStyle.getSuggestionForgroundColorStyle(color) == 1 ? -1275068417 : -1291845632;
    }

    public void updateBarTint() {
        if (-1 == this.mEmuiStyle && this.isDockedStackExists) {
            HwLog.i("TintManager", "in dockedStack, ignore updateBarTint!!");
            return;
        }
        if (countObservers() > 0 && updateLocalLastIconColor()) {
            try {
                setChanged();
                notifyObservers();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private boolean updateLocalLastIconColor() {
        boolean changed = false;
        int naviColor = getIconColorByTypeInner("navigationBarType");
        if (naviColor != this.mLastNaviColor) {
            changed = true;
            this.mLastNaviColor = naviColor;
        }
        int statusColor = getIconColorByTypeInner("statusBarType");
        if (statusColor != this.mLastStatusColor) {
            changed = true;
            this.mLastStatusColor = statusColor;
        }
        if (this.mLastIsLauncher != isLuncherStyle()) {
            changed = true;
            this.mLastIsLauncher = isLuncherStyle();
        }
        if (changed && DEBUG) {
            HwLog.i("TintManager", "updateLocalLastIconColor localChanged ? " + changed + ", naviColor: " + this.mLastNaviColor + ", statusColor: " + this.mLastStatusColor);
        }
        return changed;
    }

    public void registerBroadcast(Context context) {
        if (context != null && !this.mIsRegistered) {
            this.mContext = context;
            this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, new IntentFilter("com.android.systemui.action.SET_WALLPAPER_COLOR"), "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            this.mIsRegistered = true;
        }
    }

    public void unRegisterBroadcast(Context context) {
        if (context != null) {
            context.unregisterReceiver(this.mBroadcastReceiver);
            this.mIsRegistered = false;
        }
    }

    public void setWallpaperAVGColor(int avgColor, boolean mask) {
        HwLog.i("TintManager", "setWallpaperAVGColor:" + avgColor);
        this.mAvgWallPaperColor = avgColor;
        this.mWallPaperUseMask = mask;
        if (UserSwitchUtils.getCurrentUser() != 0) {
            Intent intent = new Intent("com.android.systemui.action.SET_WALLPAPER_COLOR");
            intent.putExtra("avgcolor", avgColor);
            intent.putExtra("mask", mask);
            HwSystemUIApplication.getContext().sendBroadcastAsUser(intent, UserHandle.OWNER, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
            return;
        }
        updateBarBgColorWhenWallpaperChanged();
    }

    public void setKeyguardWallpaperAVGColor(int avgColor, boolean mask) {
        HwLog.i("TintManager", "setKeyguardWallpaperAVGColor:" + avgColor);
        this.mAvgKeyguardWallPaperColor = avgColor;
        this.mKeyguardWallPaperUseMask = mask;
        if (UserSwitchUtils.getCurrentUser() != 0) {
            Intent intent = new Intent("com.android.systemui.action.SET_KEYGUARD_WALLPAPER_COLOR");
            intent.putExtra("avgcolor", avgColor);
            intent.putExtra("mask", mask);
            HwSystemUIApplication.getContext().sendBroadcastAsUser(intent, UserHandle.OWNER, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
            return;
        }
        updateBarBgColorWhenKeyguardWallpaperChanged();
    }

    public void setPhoneStatusBarTransitions(HwPhoneStatusBarTransitions pst) {
        this.mPhoneStatusBarTransitions = pst;
    }

    public void setNavigationBarTransitions(HwNavigationBarTransitions nbt) {
        this.mNavigationBarTransitions = nbt;
    }

    public void setFullExpanded(boolean fullExpanded) {
        if (DEBUG) {
            Log.d("TintManager", "setFullExpanded: mIsFullExpanded=" + this.mIsFullExpanded + ", fullExpanded=" + fullExpanded + ", mIsNavigationBarTranslucent=" + this.mIsNavigationBarTranslucent);
        }
        if (this.mIsFullExpanded != fullExpanded) {
            if (DEBUG) {
                HwLog.i("TintManager", "setFullExpanded:" + fullExpanded);
            }
            this.mIsFullExpanded = fullExpanded;
            if (!fullExpanded) {
                updateNavigationBarColor();
            } else if (this.mNavigationBarTransitions != null) {
                this.mNavigationBarTransitions.setBackgroundColor(0);
            }
            updateBarTint();
        }
    }

    public void updateBarAlpha(float alpha) {
        if (this.mPhoneStatusBarTransitions == null) {
            HwLog.i("TintManager", "updateBarAlpha:null == mPhoneStatusBarTransitions !");
        } else {
            this.mPhoneStatusBarTransitions.setBackgroundAlpha(alpha);
        }
    }

    public void setNavigationBarVertical(boolean isVertical) {
        if (DEBUG) {
            HwLog.i("TintManager", "setNavigationBarVertical:" + isVertical);
        }
        if (this.mEmuiStyle == 1 && this.mPhoneStatusBarTransitions != null) {
            this.mPhoneStatusBarTransitions.setBackgroundColor(0);
        }
        if (!this.mIsFullExpanded || this.mNavigationBarTransitions == null) {
            updateNavigationBarColor();
        } else {
            this.mNavigationBarTransitions.setBackgroundColor(0);
        }
    }

    public void setLightBar(boolean light) {
        boolean changed;
        if (this.mLightBar != light) {
            changed = true;
        } else {
            changed = false;
        }
        if (changed) {
            HwLog.i("TintManager", "setLightBar:" + light);
            this.mLightBar = light;
            if (this.mPhoneStatusBarTransitions != null && !isEmuiStyle() && !isLuncherStyle()) {
                if (this.mLightBar) {
                    this.mPhoneStatusBarTransitions.setBackgroundColor(0);
                } else {
                    this.mPhoneStatusBarTransitions.restoreBackgroundColor();
                }
                updateBarTint();
            }
        }
    }

    public boolean isStatusBarBlack() {
        return -1291845632 == getIconColorByType("statusBarType");
    }

    public boolean isScreenLocked() {
        return this.mScreenLocked;
    }

    public boolean isLuncherStyle() {
        return -1 == this.mEmuiStyle;
    }

    public int getEmuiStyle() {
        return this.mEmuiStyle;
    }

    public boolean isEmuiLightStyle() {
        return 1 == this.mEmuiLightStyle;
    }
}
