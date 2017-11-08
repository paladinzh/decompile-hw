package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;

public class InstantSharingTile extends QSTile<BooleanState> {
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_instantshare_on2off, R.drawable.ic_instantshare_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_instantshare_off2on, R.drawable.ic_instantshare_tile_on);
    OnChangeListener mStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            InstantSharingTile.this.onObserverChanged();
        }
    };
    private BroadcastReceiver mStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!SecurityCodeCheck.isValidIntentAndAction(intent)) {
                HwLog.e("InstantSharingTile", "mStateChangeReceiver::null intent or null action");
            } else if ("com.huawei.instantshare.action.INSTANTSHARE_STATE_CHANGED".equals(intent.getAction())) {
                HwLog.i("InstantSharingTile", "mStateChangeReceiver::INSTANTSHARE_STATE_CHANGED");
                InstantSharingTile.this.onObserverChanged();
            } else {
                HwLog.e("InstantSharingTile", "mStateChangeReceiver::invalid intent action");
            }
        }
    };

    public InstantSharingTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        boolean newState = !((BooleanState) this.mState).value;
        HwLog.i("InstantSharingTile", "click tile current state:" + ((BooleanState) this.mState).value + ", new state=" + newState);
        if (!this.mProcessingState) {
            int i;
            this.mProcessingState = true;
            refreshState(Boolean.valueOf(newState));
            if (newState) {
                i = 1;
            } else {
                i = 0;
            }
            startService(i);
        }
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean isInstantShareOn = arg != null ? ((Boolean) arg).booleanValue() : isInstantShareOn();
        state.label = this.mContext.getString(R.string.huawei_share_widget_name);
        int i = this.mProcessingState ? 3 : isInstantShareOn ? 1 : 0;
        state.labelTint = i;
        state.value = isInstantShareOn;
        Icon icon = this.mProcessingState ? ResourceIcon.get(R.drawable.ic_instantshare_tile_process) : isInstantShareOn ? this.mEnable : this.mDisable;
        state.icon = icon;
        state.textChangedDelay = (long) (isInstantShareOn ? 167 : 83);
        state.contentDescription = this.mContext.getString(R.string.huawei_share_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return new Intent("com.huawei.instantshare.action.START_QUICKSTART_WHOLE").setPackage("com.huawei.android.instantshare");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.huawei_share_widget_name);
    }

    public void setListening(boolean listening) {
        if (listening) {
            SystemUIObserver.getObserver(16).addOnChangeListener(this.mStateChangeListener);
            this.mContext.registerReceiverAsUser(this.mStateChangeReceiver, new UserHandle(UserSwitchUtils.getCurrentUser()), new IntentFilter("com.huawei.instantshare.action.INSTANTSHARE_STATE_CHANGED"), null, null);
            return;
        }
        SystemUIObserver.getObserver(16).removeOnChangeListener(this.mStateChangeListener);
        this.mContext.unregisterReceiver(this.mStateChangeReceiver);
        this.mProcessingState = false;
    }

    public boolean isAvailable() {
        return SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.instantshare");
    }

    private boolean isInstantShareOn() {
        return ((Boolean) SystemUIObserver.get(16)).booleanValue();
    }

    private void startService(int newState) {
        Intent intent = new Intent("com.huawei.instantshare.action.CHANGE_DISCOVERABLE_STATUS");
        intent.putExtra("new_status", newState);
        intent.setPackage("com.huawei.android.instantshare");
        try {
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        } catch (SecurityException e) {
            HwLog.e("InstantSharingTile", "startService occur SecurityException:" + e);
        } catch (Exception e2) {
            HwLog.e("InstantSharingTile", "startService occur some exception:" + e2);
        }
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_instant_sharing_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_instant_sharing_changed_off);
    }
}
