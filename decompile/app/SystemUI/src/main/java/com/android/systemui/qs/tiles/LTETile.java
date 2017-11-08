package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.statusbar.phone.HwCustPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.android.telephony.TelephonyManagerEx;
import huawei.cust.HwCustUtils;

public class LTETile extends QSTile<BooleanState> {
    private static final boolean IS_LTET_HIDE = SystemProperties.getBoolean("ro.config.hw_hide_lte", false);
    private static final boolean IS_LTE_REMOVED = SystemProperties.getBoolean("ro.config.only_delete_lte", false);
    private static final boolean IS_LTE_SHOWN = SystemProperties.getBoolean("ro.config.toolbox_show_lte", false);
    private AnimationIcon mDisable4G = new AnimationIcon(R.drawable.ic_lte_on2off, R.drawable.ic_lte_tile_off);
    private AnimationIcon mDisableLTE = new AnimationIcon(R.drawable.ic_product_lte_on2off, R.drawable.ic_product_lte_tile_off);
    private AnimationIcon mEnable4G = new AnimationIcon(R.drawable.ic_lte_off2on, R.drawable.ic_lte_tile_on);
    private AnimationIcon mEnableLTE = new AnimationIcon(R.drawable.ic_product_lte_off2on, R.drawable.ic_product_lte_tile_on);
    private HwCustPhoneStatusBar mHwCustPhoneStatusBar = ((HwCustPhoneStatusBar) HwCustUtils.createObj(HwCustPhoneStatusBar.class, new Object[]{this.mContext}));
    private ContentObserver mLTESwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (LTETile.this.mContext != null) {
                HwLog.i(LTETile.this.TAG, "mLTESwitchObserver::onChange");
                LTETile.this.onObserverChanged();
            }
        }
    };
    OnChangeListener mStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            LTETile.this.mProcessingState = false;
            LTETile.this.refreshState();
        }
    };

    public LTETile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        if (!isLTEDisable()) {
            boolean newState = !((BooleanState) this.mState).value;
            if (!this.mProcessingState) {
                TelephonyManagerEx.setLteServiceAbility(newState ? 1 : 0);
                this.mProcessingState = true;
                refreshState(Boolean.valueOf(newState));
            }
        }
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        CharSequence string;
        int i = 0;
        boolean isLTEModeOn = arg != null ? ((Boolean) arg).booleanValue() : isLTEModeOn();
        boolean isStateChanged = false;
        if (isLTEModeOn != this.mLastState) {
            isStateChanged = true;
        }
        this.mLastState = isLTEModeOn;
        if (isLTEDisable()) {
            state.labelTint = 2;
        } else if (this.mProcessingState) {
            state.labelTint = 3;
        } else {
            state.labelTint = isLTEModeOn ? 1 : 0;
        }
        state.icon = getDrawableIcon(state.labelTint, isStateChanged);
        if (IS_LTE_SHOWN) {
            string = this.mContext.getString(R.string.product_lte_widget_name);
        } else {
            string = this.mContext.getString(R.string.lte_widget_name);
        }
        state.label = string;
        state.value = isLTEModeOn;
        if (isStateChanged) {
            i = 83;
        }
        state.textChangedDelay = (long) i;
        state.contentDescription = state.label;
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        if (isLTEDisable()) {
            return null;
        }
        if (SystemUiUtil.isChinaTelecomArea()) {
            return new Intent("android.settings.SETTINGS").setPackage("com.android.settings").addFlags(32768);
        }
        if (IS_LTE_REMOVED || SystemUiUtil.isWifiOnly(this.mContext)) {
            return new Intent("android.settings.DATA_ROAMING_SETTINGS").setPackage("com.android.phone");
        }
        return new Intent("android.settings.WIRELESS_SETTINGS").setPackage("com.android.settings").addFlags(32768);
    }

    public CharSequence getTileLabel() {
        if (IS_LTE_SHOWN) {
            return this.mContext.getString(R.string.product_lte_widget_name);
        }
        return this.mContext.getString(R.string.lte_widget_name);
    }

    public void setListening(boolean listening) {
        if (listening) {
            SystemUIObserver.getObserver(2).addOnChangeListener(this.mStateChangeListener);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), true, this.mLTESwitchObserver);
            return;
        }
        SystemUIObserver.getObserver(2).removeOnChangeListener(this.mStateChangeListener);
        this.mContext.getContentResolver().unregisterContentObserver(this.mLTESwitchObserver);
        this.mProcessingState = false;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (IS_LTET_HIDE || IS_LTE_REMOVED || ((SystemUiUtil.isNeedRemove4GSwitch() && SystemUiUtil.isChinaMobileArea()) || (this.mHwCustPhoneStatusBar != null && this.mHwCustPhoneStatusBar.isRemoveEnable4G(this.mContext)))) {
            HwLog.i(this.TAG, "IS_LTE_REMOVED:" + IS_LTE_REMOVED + "IS_LTET_HIDE:" + IS_LTET_HIDE);
            return false;
        }
        if (!SystemUiUtil.isWifiOnly(this.mContext)) {
            z = true;
        }
        return z;
    }

    private boolean isLTEModeOn() {
        return 1 == TelephonyManagerEx.getLteServiceAbility();
    }

    private boolean isLTEDisable() {
        if (((Boolean) SystemUIObserver.get(2)).booleanValue() || !SystemUiUtil.isSimCardReady(this.mContext) || SystemUiUtil.isCalling(this.mContext)) {
            return true;
        }
        if (SystemUiUtil.isCurrentSupportLTE(this.mContext)) {
            return false;
        }
        return true;
    }

    private Icon getDrawableIcon(int labelTint, boolean stateChanged) {
        Icon drawableIcon;
        switch (labelTint) {
            case 1:
                if (IS_LTE_SHOWN) {
                    return stateChanged ? this.mEnableLTE : new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_product_lte_tile_on));
                }
                return stateChanged ? this.mEnable4G : new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_lte_tile_on));
            case 2:
                if (IS_LTE_SHOWN) {
                    drawableIcon = new DrawableIcon(this.mHost.getContext().getDrawable(R.drawable.ic_product_lte_tile_disable));
                } else {
                    drawableIcon = new DrawableIcon(this.mHost.getContext().getDrawable(R.drawable.ic_lte_tile_disable));
                }
                return drawableIcon;
            case 3:
                if (IS_LTE_SHOWN) {
                    drawableIcon = new DrawableIcon(this.mHost.getContext().getDrawable(R.drawable.ic_product_lte_tile_process));
                } else {
                    drawableIcon = new DrawableIcon(this.mHost.getContext().getDrawable(R.drawable.ic_lte_tile_process));
                }
                return drawableIcon;
            default:
                if (IS_LTE_SHOWN) {
                    if (stateChanged) {
                        drawableIcon = this.mDisableLTE;
                    } else {
                        drawableIcon = new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_product_lte_tile_off));
                    }
                    return drawableIcon;
                }
                return stateChanged ? this.mDisable4G : new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_lte_tile_off));
        }
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            if (IS_LTE_SHOWN) {
                return this.mContext.getString(R.string.accessibility_quick_settings_LTE_changed_on);
            }
            return this.mContext.getString(R.string.accessibility_quick_settings_4G_changed_on);
        } else if (IS_LTE_SHOWN) {
            return this.mContext.getString(R.string.accessibility_quick_settings_LTE_changed_off);
        } else {
            return this.mContext.getString(R.string.accessibility_quick_settings_4G_changed_off);
        }
    }
}
