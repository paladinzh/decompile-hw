package com.huawei.powergenie.modules.displaypower;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.policy.PolicyProvider;
import java.util.ArrayList;
import java.util.HashMap;

public class BacklightControl {
    private static final boolean LIMIT_LCD_MANUAL = SystemProperties.getBoolean("ro.config.limit_lcd_manual", false);
    private HashMap<Integer, Policy> mActionMapPolicy = new HashMap();
    private int mAllSceneRatio = 100;
    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (BacklightControl.this.isManualBrightnessMode()) {
                Log.d("BacklightControl", "brightness mode chg, manual mode");
                BacklightControl.this.handleManualBrightnessMode();
            }
        }
    };
    private ContentResolver mContentResolver;
    private final Context mContext;
    private final ICoreContext mCoreContext;
    private boolean mCurAuto = false;
    private int mCurRatio = 100;
    private ArrayList<String> mFuzzyMatchPkgList = new ArrayList();
    private boolean mHasPolicy = false;
    private final IDeviceState mIDeviceState;
    private final IScenario mIScenario;
    private boolean mIsReStartAfterCrash = false;
    private String mLauncherApp = null;
    private int mLowBatRadio = 100;
    private final int mModId;
    private int mParentActionID = 0;
    private HashMap<String, Policy> mPkgMapPolicy = new HashMap();
    private PowerManager mPm;
    private final IPolicy mPolicy;
    private ArrayList<Integer> mRegisterActionId = new ArrayList();
    private SetBacklightHandler mSetBacklightHandler;
    private int mThermalRadio = 100;

    private final class Policy {
        private boolean mAuto = false;
        private int mRatio = 100;

        public void setRatio(int ratio) {
            this.mRatio = ratio;
        }

        public void setAuto(boolean auto) {
            this.mAuto = auto;
        }

        public int getRatio() {
            return this.mRatio;
        }

        public boolean getAuto() {
            return this.mAuto;
        }
    }

    private final class SetBacklightHandler extends Handler {
        public SetBacklightHandler(Looper looper) {
            super(looper);
            BacklightControl.this.mCoreContext.configBrightnessRange(false);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    BacklightControl.this.mCoreContext.configBrightnessRange(false);
                    break;
                case 102:
                    BacklightControl.this.mCoreContext.configBrightnessRange(true);
                    break;
                case 103:
                    break;
                case 104:
                case 105:
                    break;
                default:
                    return;
            }
        }
    }

    protected BacklightControl(ICoreContext coreContext, int modId) {
        this.mModId = modId;
        this.mCoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mPolicy = (IPolicy) coreContext.getService("policy");
        this.mIScenario = (IScenario) this.mCoreContext.getService("scenario");
        this.mIDeviceState = (IDeviceState) this.mCoreContext.getService("device");
        this.mPm = (PowerManager) coreContext.getContext().getSystemService("power");
        this.mContentResolver = this.mContext.getContentResolver();
    }

    protected void handleStart(int mode) {
        this.mCurRatio = 100;
        this.mCurAuto = false;
        this.mAllSceneRatio = 100;
        this.mParentActionID = 0;
        loadPolicy(mode);
        addAllActions(true);
        if (!(LIMIT_LCD_MANUAL || this.mContentResolver.acquireProvider(System.getUriFor("screen_brightness_mode")) == null)) {
            this.mContentResolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), true, this.mBrightnessModeObserver, -1);
        }
        HandlerThread thread = new HandlerThread("setBacklight", 10);
        thread.start();
        this.mSetBacklightHandler = new SetBacklightHandler(thread.getLooper());
    }

    protected void handleBacklightAction(PowerAction action) {
        int actionId = action.getActionId();
        switch (actionId) {
            case 230:
                this.mLauncherApp = action.getPkgName();
                break;
            case 254:
                setThermalRatio((int) action.getExtraLong());
                return;
            case 324:
                this.mIsReStartAfterCrash = true;
                return;
            case 333:
                setLowBatteryRatio(true);
                return;
            case 334:
                setLowBatteryRatio(false);
                return;
            case 350:
                handlePowerMode(action.getExtraInt());
                return;
            case 358:
                return;
        }
        int mode = this.mPolicy.getPowerMode();
        if (mode != 4 && mode != 1) {
            handleFrontAction(actionId, action.getPkgName());
        }
    }

    private void addAllActions(boolean isAdd) {
        this.mRegisterActionId.clear();
        for (Integer actionId : this.mActionMapPolicy.keySet()) {
            if (actionId != null) {
                if (isAdd) {
                    this.mCoreContext.addAction(this.mModId, actionId.intValue());
                    this.mRegisterActionId.add(actionId);
                } else {
                    this.mCoreContext.removeAction(this.mModId, actionId.intValue());
                }
            }
        }
    }

    private void handleFrontAction(int actionId, String pkgName) {
        if (!this.mHasPolicy) {
            return;
        }
        if (this.mIDeviceState.isScreenOff() && "com.ss.android.article.news".equals(pkgName)) {
            Log.i("BacklightControl", "Auto front app can't to adjust backlight : " + pkgName);
        } else if (this.mCoreContext.isMultiWinDisplay() && !"com.huawei.hwmwlauncher".equals(pkgName)) {
            Log.i("BacklightControl", "multi win scene, can't to adjust backlight.");
        } else if (isNeedAdjust(pkgName)) {
            Policy policy = (Policy) this.mPkgMapPolicy.get(pkgName);
            int id = actionId;
            if (policy == null && pkgName != null) {
                for (String pkg : this.mFuzzyMatchPkgList) {
                    if (pkgName.toLowerCase().contains(pkg)) {
                        policy = (Policy) this.mPkgMapPolicy.get("*" + pkg);
                        break;
                    }
                }
            }
            if (policy == null) {
                id = processSubSceneAction(actionId);
                if (!this.mRegisterActionId.contains(Integer.valueOf(id))) {
                    id = 208;
                }
                policy = (Policy) this.mActionMapPolicy.get(Integer.valueOf(id));
            }
            boolean isAutoAdj = false;
            int ratio = this.mAllSceneRatio;
            if (policy != null) {
                ratio = Math.min(ratio, policy.getRatio());
                isAutoAdj = policy.getAuto();
            }
            setBacklight(101, ratio, isAutoAdj);
        } else {
            Log.i("BacklightControl", "can't adjust backlight when app launch from other scene.");
        }
    }

    private void setBacklight(int event, int ratio, boolean auto) {
        if (event == 101) {
            this.mSetBacklightHandler.removeMessages(101);
        }
        this.mSetBacklightHandler.sendMessageDelayed(this.mSetBacklightHandler.obtainMessage(event, ratio, 0, Boolean.valueOf(auto)), 0);
    }

    private int processSubSceneAction(int actionId) {
        int id = actionId;
        switch (actionId) {
            case 246:
                break;
            case 247:
                return this.mParentActionID;
            default:
                this.mParentActionID = actionId;
                break;
        }
        return actionId;
    }

    protected void handleManualBrightnessMode() {
        setBacklight(104, 100, false);
    }

    private void handlePowerMode(int newMode) {
        setBacklight(105, this.mAllSceneRatio, false);
        if (newMode == 1) {
            this.mCoreContext.configBrightnessRange(true);
            Log.i("BacklightControl", "Backlight control in low battery mode...");
            setBacklight(105, 56, false);
            return;
        }
        this.mCoreContext.configBrightnessRange(false);
        loadPolicy(newMode);
        addAllActions(true);
    }

    private boolean isNeedAdjust(String pkgName) {
        String lastFrontApp = this.mIScenario.getTopBgPkg();
        if ((pkgName != null && pkgName.equals(this.mLauncherApp)) || (lastFrontApp != null && lastFrontApp.equals(this.mLauncherApp))) {
            return true;
        }
        if ((pkgName == null || !pkgName.equals("com.android.systemui")) && (lastFrontApp == null || !lastFrontApp.equals("com.android.systemui"))) {
            return false;
        }
        return true;
    }

    private boolean isManualBrightnessMode() {
        if (System.getInt(this.mContentResolver, "screen_brightness_mode", 0) == 0) {
            return true;
        }
        return false;
    }

    private boolean loadPolicy(int mode) {
        Policy item;
        this.mActionMapPolicy.clear();
        this.mPkgMapPolicy.clear();
        this.mFuzzyMatchPkgList.clear();
        Cursor cursor = this.mContext.getContentResolver().query(PolicyProvider.BACKLIGHT_URI, null, "power_mode=? OR power_mode=? ", new String[]{Integer.toString(mode), "0"}, null);
        if (cursor == null) {
            Log.w("BacklightControl", "backlight table is not exist. ");
            return false;
        }
        int idCol = cursor.getColumnIndex("action_id");
        int typeCol = cursor.getColumnIndex("policy_type");
        int valueCol = cursor.getColumnIndex("policy_value");
        int pkgCol = cursor.getColumnIndex("pkg_name");
        while (cursor.moveToNext()) {
            Policy item2;
            int actionId = cursor.getInt(idCol);
            int policyType = cursor.getInt(typeCol);
            int policyValue = cursor.getInt(valueCol);
            String pkgName = cursor.getString(pkgCol);
            if (TextUtils.isEmpty(pkgName)) {
                item2 = (Policy) this.mActionMapPolicy.get(Integer.valueOf(actionId));
                if (item2 == null) {
                    item = new Policy();
                    try {
                        this.mActionMapPolicy.put(Integer.valueOf(actionId), item);
                        item2 = item;
                    } catch (RuntimeException e) {
                        RuntimeException ex = e;
                        item2 = item;
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        item2 = item;
                    }
                }
            } else {
                if (pkgName.startsWith("*") && pkgName.length() > 1) {
                    this.mFuzzyMatchPkgList.add(pkgName.substring(1));
                }
                item2 = (Policy) this.mPkgMapPolicy.get(pkgName);
                if (item2 == null) {
                    item = new Policy();
                    this.mPkgMapPolicy.put(pkgName, item);
                    item2 = item;
                }
            }
            if (policyType == 1) {
                item2.setRatio(policyValue);
            } else if (policyType == 2) {
                try {
                    item2.setAuto(1 == policyValue);
                } catch (RuntimeException e2) {
                    ex = e2;
                }
            } else {
                Log.e("BacklightControl", "error policy type : " + policyType);
            }
        }
        cursor.close();
        if (this.mActionMapPolicy.isEmpty() || !this.mPkgMapPolicy.isEmpty()) {
            this.mHasPolicy = true;
        } else {
            this.mHasPolicy = false;
            Log.i("BacklightControl", "no any policy for power mode: " + mode);
        }
        return true;
        try {
            Log.e("BacklightControl", "RuntimeException:", ex);
            cursor.close();
            if (this.mActionMapPolicy.isEmpty()) {
            }
            this.mHasPolicy = true;
            return true;
        } catch (Throwable th3) {
            th2 = th3;
            cursor.close();
            throw th2;
        }
    }

    private void adjustLcdBacklight(int ratio, boolean auto, boolean forceSet) {
        if (ratio <= 0 || ratio > 100) {
            ratio = 100;
        }
        if (this.mIsReStartAfterCrash || this.mCurRatio != ratio || this.mCurAuto != auto) {
            if (this.mIsReStartAfterCrash) {
                Log.i("BacklightControl", "PG restart after crash restore ratio:" + ratio + " auto:" + auto);
                this.mIsReStartAfterCrash = false;
            }
            if (!LIMIT_LCD_MANUAL && isManualBrightnessMode()) {
                if (forceSet) {
                    Log.i("BacklightControl", "manual brightness mode set lcd:" + ratio + " auto:" + auto);
                } else {
                    Log.i("BacklightControl", "manual brightness mode do nothing, limit lcd:" + ratio + " auto:" + auto);
                    return;
                }
            }
            Log.i("BacklightControl", "Ratio: " + ratio + " Auto: " + auto);
            this.mCoreContext.setLcdRatio(ratio, auto);
            if (this.mCurRatio != ratio) {
                this.mCurRatio = ratio;
            }
            if (this.mCurAuto != auto) {
                this.mCurAuto = auto;
            }
        }
    }

    private void setThermalRatio(int lcdRatio) {
        if (lcdRatio > 100 || lcdRatio < 15) {
            Log.d("BacklightControl", "thermal backlight ratio is out of range");
            return;
        }
        this.mThermalRadio = lcdRatio;
        this.mAllSceneRatio = Math.min(this.mThermalRadio, this.mLowBatRadio);
        if (!(this.mCurRatio == this.mAllSceneRatio && this.mHasPolicy)) {
            setBacklight(102, this.mAllSceneRatio, this.mCurAuto);
        }
    }

    private void setLowBatteryRatio(boolean isWarning) {
        if (isWarning) {
            this.mLowBatRadio = 60;
        } else {
            this.mLowBatRadio = 100;
        }
        this.mAllSceneRatio = Math.min(this.mThermalRadio, this.mLowBatRadio);
        if (this.mCurRatio != this.mAllSceneRatio) {
            setBacklight(103, this.mAllSceneRatio, this.mCurAuto);
        }
    }
}
