package com.android.systemui.battery;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.PowerModeController;
import com.android.systemui.statusbar.policy.PowerModeController.CallBack;
import com.android.systemui.tint.TintImageView;
import com.android.systemui.tint.TintTextView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.NumberLocationPercent;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.events.AppHandler;

public class HwBatteryManager implements BatteryStateChangeCallback, CallBack {
    static boolean DEBUG = false;
    static boolean IS_CUSTOM_SUPER_CHARGE = SystemProperties.getBoolean("ro.config.super_charge_icon", false);
    private BatteryController mBatteryController;
    private BatteryView[] mBatteryViews;
    private boolean mCharging;
    private Context mContext = HwSystemUIApplication.getContext();
    private Handler mHandler = new Handler();
    private int mLevel;
    private NotificationManager mNotificationManager;
    private OnChangeListener mOnChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwBatteryManager.this.update();
        }
    };
    private int mPercentColor = -1;
    private PercentColor[] mPercentColors;
    private boolean mPlugged;
    private int mPluggedColor;
    private PowerModeController mPowerModeController;
    private boolean mPowerSave;
    private boolean mPowerSaveEnabled;
    private boolean mQuickStatus;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("huawei.intent.action.BATTERY_QUICK_CHARGE".equals(intent.getAction())) {
                String mQuickChargeLevel = intent.getStringExtra("quick_charge_status");
                HwBatteryManager.this.mQuickStatus = "1".equals(mQuickChargeLevel);
                HwBatteryManager.this.mSuperQuickStatus = "2".equals(mQuickChargeLevel);
                BatteryStateInfo.setQuickCharge(mQuickChargeLevel);
                AppHandler.sendMessage(100, "huawei.intent.action.BATTERY_QUICK_CHARGE");
                HwLog.i("HwBatteryManager", "mQuickStatus = " + HwBatteryManager.this.mQuickStatus + " mSuperQuickStatus = " + HwBatteryManager.this.mSuperQuickStatus);
                if (!HwBatteryManager.this.mQuickStatus) {
                    HwBatteryManager.this.mHandler.removeCallbacks(HwBatteryManager.this.mUpdateRunnable);
                    HwBatteryManager.this.mNotificationManager.cancel(2015);
                }
                HwBatteryManager.this.update();
            }
        }
    };
    private boolean mShowPercent;
    private boolean mShowPercentIn;
    private boolean mSuperQuickStatus;
    private final Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            HwBatteryManager.this.mNotificationManager.cancel(2015);
            HwLog.i("HwBatteryManager", "cancel notification id = 2015");
        }
    };
    private boolean mUsePluggedColor;

    class BatteryView {
        private TintImageView mBorderView;
        private ImageView mInsideChargeLevelView;
        private TintImageView mInsideChargeView;
        private TintImageView mInsideLevelView;
        private TintTextView mInsidePercentView;
        private TintImageView mInsideSaveView;
        private TintImageView mInsideWarnningCenterView;
        private TintImageView mInsideWarnningLeftView;
        private boolean mIsBgCallingLayout;
        private boolean mIsValid = false;
        private TintImageView mOutsideChargeView;
        private TintTextView mOutsidePercentView;
        private TintImageView mOutsideSaveView;
        final /* synthetic */ HwBatteryManager this$0;

        public BatteryView(HwBatteryManager this$0, View container) {
            boolean z = true;
            this.this$0 = this$0;
            if (container != null) {
                this.mIsValid = true;
                if (container != HwPhoneStatusBar.getInstance().getCallingLayout()) {
                    z = false;
                }
                this.mIsBgCallingLayout = z;
                this.mBorderView = (TintImageView) container.findViewById(R.id.battery_border);
                this.mInsideLevelView = (TintImageView) container.findViewById(R.id.battery_inside_level);
                this.mInsideChargeLevelView = (ImageView) container.findViewById(R.id.battery_inside_charge_level);
                this.mInsidePercentView = (TintTextView) container.findViewById(R.id.battery_inside_percent);
                this.mOutsidePercentView = (TintTextView) container.findViewById(R.id.battery_outside_percent);
                this.mInsideChargeView = (TintImageView) container.findViewById(R.id.battery_inside_charge);
                this.mOutsideChargeView = (TintImageView) container.findViewById(R.id.battery_outside_charge);
                this.mInsideSaveView = (TintImageView) container.findViewById(R.id.battery_inside_save);
                this.mOutsideSaveView = (TintImageView) container.findViewById(R.id.battery_outside_save);
                this.mInsideWarnningCenterView = (TintImageView) container.findViewById(R.id.battery_inside_warning_center);
                this.mInsideWarnningLeftView = (TintImageView) container.findViewById(R.id.battery_inside_warning_left);
                this.mInsideLevelView.setImageResource(R.drawable.stat_sys_battery_new_svg);
                this.mInsideChargeLevelView.setImageResource(R.drawable.stat_sys_battery_charge_new_svg);
                if (this.mIsBgCallingLayout) {
                    this.mBorderView.setIsResever(false);
                    this.mInsideLevelView.setIsResever(false);
                    this.mInsidePercentView.setIsResever(false);
                    this.mOutsidePercentView.setIsResever(false);
                    this.mInsideChargeView.setIsResever(false);
                    this.mOutsideChargeView.setIsResever(false);
                    this.mInsideWarnningCenterView.setIsResever(false);
                    this.mInsideWarnningLeftView.setIsResever(false);
                    this.mInsideSaveView.setIsResever(false);
                    this.mOutsideSaveView.setIsResever(false);
                }
            }
        }

        private void update() {
            int i = 0;
            if (this.mIsValid) {
                int i2;
                this.this$0.mShowPercent = ((Boolean) SystemUIObserver.get(8)).booleanValue();
                this.this$0.mShowPercentIn = ((Boolean) SystemUIObserver.get(9)).booleanValue();
                boolean showOutsidePercentView = (this.this$0.isShowFullCharged() && !this.this$0.mShowPercent) || (this.this$0.mShowPercent && !this.this$0.mShowPercentIn);
                boolean -get11 = this.this$0.mShowPercent ? this.this$0.mShowPercentIn : false;
                boolean showInsideChargeView = this.this$0.mCharging && !-get11;
                boolean z = this.this$0.mCharging ? -get11 : false;
                boolean showInsideLevelView = (this.this$0.mCharging || -get11) ? false : true;
                boolean showInsideChargeLevelView = this.this$0.mCharging;
                boolean showInsideSaveView = (-get11 || !this.this$0.mPowerSave || this.this$0.mPlugged) ? false : true;
                boolean showOutsideSaveView = -get11 && this.this$0.mPowerSave && !this.this$0.mPlugged;
                boolean showInsideWarnningCenterView = showInsideLevelView && this.this$0.isWarnningLevel() && !this.this$0.mPowerSave && !this.this$0.mPlugged;
                boolean showInsideWarnningLeftView = -get11 && this.this$0.isWarnningLevel() && !this.this$0.mPlugged;
                HwLog.d("HwBatteryManager", "showOutsidePercentView=" + showOutsidePercentView + ", showInsidePercentView=" + -get11 + ", showInsideChargeView=" + showInsideChargeView + ", showOutsideChargeView=" + z + ", showInsideLevelView=" + showInsideLevelView + ", showInsideChargeLevelView=" + showInsideChargeLevelView + ", showInsideWarnningCenterView=" + showInsideWarnningCenterView + ", showInsideWarnningLeftView=" + showInsideWarnningLeftView);
                TintTextView tintTextView = this.mOutsidePercentView;
                if (showOutsidePercentView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintTextView.setVisibility(i2);
                tintTextView = this.mInsidePercentView;
                if (-get11) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintTextView.setVisibility(i2);
                TintImageView tintImageView = this.mInsideChargeView;
                if (showInsideChargeView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                tintImageView = this.mOutsideChargeView;
                if (z) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                tintImageView = this.mInsideLevelView;
                if (showInsideLevelView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                ImageView imageView = this.mInsideChargeLevelView;
                if (showInsideChargeLevelView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                imageView.setVisibility(i2);
                tintImageView = this.mInsideSaveView;
                if (showInsideSaveView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                tintImageView = this.mOutsideSaveView;
                if (showOutsideSaveView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                tintImageView = this.mInsideWarnningCenterView;
                if (showInsideWarnningCenterView) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                tintImageView.setVisibility(i2);
                TintImageView tintImageView2 = this.mInsideWarnningLeftView;
                if (!showInsideWarnningLeftView) {
                    i = 8;
                }
                tintImageView2.setVisibility(i);
                updatePercent();
                updateLevel();
                updateCharge();
                updateBorderViewDescription();
            }
        }

        private void updateBorderViewDescription() {
            int i;
            TintImageView tintImageView = this.mBorderView;
            Context -get1 = this.this$0.mContext;
            if (this.this$0.mCharging) {
                i = R.string.accessibility_battery_level_charging;
            } else {
                i = R.string.accessibility_battery_level;
            }
            tintImageView.setContentDescription(-get1.getString(i, new Object[]{Integer.valueOf(this.this$0.mLevel)}));
        }

        private void updateLevel() {
            this.mInsideLevelView.setIsResever(isLevelUseTint());
            this.mInsideLevelView.setImageLevel(this.this$0.mLevel);
            TintImageView tintImageView = this.mInsideLevelView;
            int i = (!this.this$0.mPowerSave || this.this$0.mPlugged) ? R.drawable.stat_sys_battery_new_svg : R.drawable.stat_sys_battery_new_save_svg;
            tintImageView.setImageResource(i);
            this.mInsideLevelView.update(null, null);
            this.mInsideChargeLevelView.setImageLevel(this.this$0.mLevel);
        }

        private void updatePercent() {
            this.mInsidePercentView.setText(NumberLocationPercent.getFormatnumberString(this.this$0.mLevel, this.this$0.mContext));
            String level = NumberLocationPercent.getPercentage(Double.valueOf((double) this.this$0.mLevel).doubleValue(), 0);
            this.mOutsidePercentView.setText(this.this$0.mContext.getString(R.string.status_bar_settings_battery_meter_format, new Object[]{level}).trim());
            if ((this.this$0.mPlugged && this.this$0.mUsePluggedColor) || (this.this$0.mPlugged && this.this$0.isBitLowLevel())) {
                this.mInsidePercentView.setTextColor(this.this$0.mPluggedColor);
            } else {
                this.mInsidePercentView.setTextColor(this.this$0.mPercentColor);
            }
            translationY4InsidePercent();
        }

        private void translationY4InsidePercent() {
            FontMetrics fontMetrics = this.mInsidePercentView.getPaint().getFontMetrics();
            this.mInsidePercentView.setTranslationY((fontMetrics.top - fontMetrics.ascent) / 4.0f);
        }

        void updateCharge() {
            if (HwBatteryManager.DEBUG) {
                HwLog.i("HwBatteryManager", "mQuickStatus = " + this.this$0.mQuickStatus + " mSuperQuickStatus = " + this.this$0.mSuperQuickStatus + ", custom=" + HwBatteryManager.IS_CUSTOM_SUPER_CHARGE);
            }
            if (this.this$0.mQuickStatus) {
                this.mInsideChargeView.setImageResource(R.drawable.ic_statusbar_battery_charge_double);
                this.mOutsideChargeView.setImageResource(R.drawable.ic_statusbar_battery_charge_double_figure);
            } else if (this.this$0.mSuperQuickStatus) {
                this.mInsideChargeView.setImageResource(HwBatteryManager.IS_CUSTOM_SUPER_CHARGE ? R.drawable.ic_statusbar_battery_supercharge : R.drawable.ic_statusbar_battery_charge_quick);
                this.mOutsideChargeView.setImageResource(HwBatteryManager.IS_CUSTOM_SUPER_CHARGE ? R.drawable.ic_statusbar_battery_supercharge_figure : R.drawable.ic_statusbar_battery_charge_quick_figure);
            } else {
                this.mInsideChargeView.setImageResource(R.drawable.ic_statusbar_battery_charge);
                this.mOutsideChargeView.setImageResource(R.drawable.ic_statusbar_battery_charge_figure);
            }
        }

        private boolean isLevelUseTint() {
            boolean z = false;
            if (this.this$0.mPowerSave) {
                if (!this.this$0.isBitLowLevel()) {
                    z = true;
                }
                return z;
            } else if (this.mIsBgCallingLayout) {
                return false;
            } else {
                if (!this.this$0.isLowLevel()) {
                    z = true;
                }
                return z;
            }
        }
    }

    private static class PercentColor {
        int mColor;
        int mLevel;

        public PercentColor(int level, int color) {
            this.mLevel = level;
            this.mColor = color;
        }

        public int getLevel() {
            return this.mLevel;
        }

        public int getColor() {
            return this.mColor;
        }

        public static PercentColor createFromColorStr(String colorStr) {
            String[] items = colorStr.split(":");
            if (items == null || items.length < 2) {
                return null;
            }
            return new PercentColor(Integer.parseInt(items[0]), Color.parseColor(items[1]));
        }

        public static PercentColor[] createFromColorStrs(String colorStrs) {
            String[] items = colorStrs.split(";");
            PercentColor[] colors = new PercentColor[items.length];
            for (int i = 0; i < items.length; i++) {
                colors[i] = createFromColorStr(items[i]);
            }
            return colors;
        }

        public static int getColor(PercentColor[] colors, int level) {
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].getLevel() >= level) {
                    return colors[i].getColor();
                }
            }
            return colors[colors.length - 1].getColor();
        }
    }

    public HwBatteryManager(BatteryController batteryController, PowerModeController powerModeController, StatusBarWindowView statusBar) {
        this.mBatteryController = batteryController;
        this.mPowerModeController = powerModeController;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mPowerSave = this.mPowerModeController.isPowerSave();
        initColor();
        initView();
        register();
        update();
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        this.mLevel = level;
        this.mCharging = charging;
        this.mPlugged = pluggedIn;
        this.mPercentColor = PercentColor.getColor(this.mPercentColors, this.mLevel);
        HwLog.i("HwBatteryManager", "onBatteryLevelChanged: level=" + level + ", pluggedIn=" + pluggedIn + ", charging=" + charging + ", PercentColor=" + this.mPercentColor);
        update();
    }

    public void onSupperPowerSaveChanged(boolean supperPowerSave) {
    }

    public void onSaveChanged(boolean save) {
        this.mPowerSave = save;
        HwLog.i("HwBatteryManager", "onSaveChanged: PowerSave=" + this.mPowerSave);
        update();
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
        this.mPowerSaveEnabled = this.mBatteryController.isPowerSave();
        HwLog.i("HwBatteryManager", "onPowerSaveChanged: PowerSave=" + this.mPowerSaveEnabled);
        update();
    }

    private void initColor() {
        this.mUsePluggedColor = ((Boolean) SystemUIObserver.get(10)).booleanValue();
        String pluggedColorStr = System.getString(this.mContext.getContentResolver(), "plugged_battery_percent_color");
        String percentColorStr = System.getString(this.mContext.getContentResolver(), "battery_percent_color");
        HwLog.i("HwBatteryManager", "initColor: pluggedPercentColor=" + pluggedColorStr + ", precentColor=" + percentColorStr);
        if (pluggedColorStr == null || pluggedColorStr.length() <= 0) {
            pluggedColorStr = "#b3ffffff";
        }
        if (percentColorStr == null || percentColorStr.length() <= 0) {
            percentColorStr = "10:#ff3320;20:#ff9b1a;100:#b3ffffff";
        }
        this.mPluggedColor = Color.parseColor(pluggedColorStr);
        this.mPercentColors = PercentColor.createFromColorStrs(percentColorStr);
    }

    private void initView() {
        this.mBatteryViews = new BatteryView[]{new BatteryView(this, HwPhoneStatusBar.getInstance().getCallingLayout()), new BatteryView(this, HwPhoneStatusBar.getInstance().getStatusBarView()), new BatteryView(this, HwPhoneStatusBar.getInstance().getCoverStatusBarView()), new BatteryView(this, HwPhoneStatusBar.getInstance().getKeyguardStatusBarView())};
    }

    public void register() {
        this.mBatteryController.addStateChangedCallback(this);
        this.mPowerModeController.register(this);
        SystemUIObserver.getObserver(8).addOnChangeListener(this.mOnChangeListener);
        SystemUIObserver.getObserver(9).addOnChangeListener(this.mOnChangeListener);
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("huawei.intent.action.BATTERY_QUICK_CHARGE"));
    }

    public void update() {
        for (BatteryView view : this.mBatteryViews) {
            view.update();
        }
    }

    private boolean isShowFullCharged() {
        return this.mLevel >= 100 ? this.mPlugged : false;
    }

    public boolean canOpenFlashLight() {
        return this.mLevel >= 5;
    }

    private boolean isWarnningLevel() {
        return this.mLevel <= 5;
    }

    private boolean isLowLevel() {
        return this.mLevel <= 20;
    }

    private boolean isBitLowLevel() {
        return this.mLevel > 5 && this.mLevel <= 20;
    }
}
