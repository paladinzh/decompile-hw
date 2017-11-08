package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSDetailItems.Item;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.CastController.CastDevice;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.LinkedHashMap;
import java.util.Set;

public class CastTile extends QSTile<BooleanState> {
    private static final Intent CAST_SETTINGS = new Intent("android.settings.CAST_SETTINGS");
    private final Callback mCallback = new Callback();
    private final CastController mController;
    private final CastDetailAdapter mDetailAdapter;
    private final KeyguardMonitor mKeyguard;

    private final class Callback implements com.android.systemui.statusbar.policy.CastController.Callback, com.android.systemui.statusbar.policy.KeyguardMonitor.Callback {
        private Callback() {
        }

        public void onCastDevicesChanged() {
            CastTile.this.refreshState();
        }

        public void onKeyguardChanged() {
            CastTile.this.refreshState();
        }
    }

    private final class CastDetailAdapter implements DetailAdapter, com.android.systemui.qs.QSDetailItems.Callback {
        private QSDetailItems mItems;
        private final LinkedHashMap<String, CastDevice> mVisibleOrder;

        private CastDetailAdapter() {
            this.mVisibleOrder = new LinkedHashMap();
        }

        public CharSequence getTitle() {
            return CastTile.this.mContext.getString(R.string.quick_settings_cast_title);
        }

        public Boolean getToggleState() {
            return null;
        }

        public Intent getSettingsIntent() {
            return CastTile.CAST_SETTINGS;
        }

        public void setToggleState(boolean state) {
        }

        public int getMetricsCategory() {
            return 151;
        }

        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            this.mItems = QSDetailItems.convertOrInflate(context, convertView, parent);
            this.mItems.setTagSuffix("Cast");
            if (convertView == null) {
                if (CastTile.DEBUG) {
                    Log.d(CastTile.this.TAG, "addOnAttachStateChangeListener");
                }
                this.mItems.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                    public void onViewAttachedToWindow(View v) {
                        if (CastTile.DEBUG) {
                            Log.d(CastTile.this.TAG, "onViewAttachedToWindow");
                        }
                    }

                    public void onViewDetachedFromWindow(View v) {
                        if (CastTile.DEBUG) {
                            Log.d(CastTile.this.TAG, "onViewDetachedFromWindow");
                        }
                        CastDetailAdapter.this.mVisibleOrder.clear();
                    }
                });
            }
            this.mItems.setEmptyState(R.drawable.ic_qs_cast_detail_empty, R.string.quick_settings_cast_detail_empty_text);
            this.mItems.setCallback(this);
            updateItems(CastTile.this.mController.getCastDevices());
            CastTile.this.mController.setDiscovering(true);
            return this.mItems;
        }

        private void updateItems(Set<CastDevice> devices) {
            if (this.mItems != null) {
                Item[] itemArr = null;
                if (!(devices == null || devices.isEmpty())) {
                    CastDevice device;
                    Item item;
                    for (CastDevice device2 : devices) {
                        if (device2.state == 2) {
                            item = new Item();
                            item.icon = R.drawable.ic_qs_cast_on;
                            item.line1 = CastTile.this.getDeviceName(device2);
                            item.line2 = CastTile.this.mContext.getString(R.string.quick_settings_connected);
                            item.tag = device2;
                            item.canDisconnect = true;
                            itemArr = new Item[]{item};
                            break;
                        }
                    }
                    if (itemArr == null) {
                        for (CastDevice device22 : devices) {
                            this.mVisibleOrder.put(device22.id, device22);
                        }
                        itemArr = new Item[devices.size()];
                        int i = 0;
                        for (String id : this.mVisibleOrder.keySet()) {
                            device22 = (CastDevice) this.mVisibleOrder.get(id);
                            if (devices.contains(device22)) {
                                item = new Item();
                                item.icon = R.drawable.ic_qs_cast_off;
                                item.line1 = CastTile.this.getDeviceName(device22);
                                if (device22.state == 1) {
                                    item.line2 = CastTile.this.mContext.getString(R.string.quick_settings_connecting);
                                }
                                item.tag = device22;
                                int i2 = i + 1;
                                itemArr[i] = item;
                                i = i2;
                            }
                        }
                    }
                }
                this.mItems.setItems(itemArr);
            }
        }

        public void onDetailItemClick(Item item) {
            if (item != null && item.tag != null) {
                MetricsLogger.action(CastTile.this.mContext, 157);
                CastTile.this.mController.startCasting(item.tag);
            }
        }

        public void onDetailItemDisconnect(Item item) {
            if (item != null && item.tag != null) {
                MetricsLogger.action(CastTile.this.mContext, 158);
                CastTile.this.mController.stopCasting(item.tag);
            }
        }
    }

    public CastTile(Host host) {
        super(host);
        this.mController = host.getCastController();
        this.mDetailAdapter = new CastDetailAdapter();
        this.mKeyguard = host.getKeyguardMonitor();
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (this.mController != null) {
            if (DEBUG) {
                Log.d(this.TAG, "setListening " + listening);
            }
            if (listening) {
                this.mController.addCallback(this.mCallback);
                this.mKeyguard.addCallback(this.mCallback);
            } else {
                this.mController.setDiscovering(false);
                this.mController.removeCallback(this.mCallback);
                this.mKeyguard.removeCallback(this.mCallback);
            }
        }
    }

    protected void handleUserSwitch(int newUserId) {
        super.handleUserSwitch(newUserId);
        if (this.mController != null) {
            this.mController.setCurrentUserId(newUserId);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.CAST_SETTINGS");
    }

    protected void handleClick() {
        if (!this.mKeyguard.isSecure() || this.mKeyguard.canSkipBouncer()) {
            MetricsLogger.action(this.mContext, getMetricsCategory());
            showDetail(true);
            return;
        }
        this.mHost.startRunnableDismissingKeyguard(new Runnable() {
            public void run() {
                MetricsLogger.action(CastTile.this.mContext, CastTile.this.getMetricsCategory());
                CastTile.this.showDetail(true);
                CastTile.this.mHost.openPanels();
            }
        });
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_cast_title);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        int i;
        state.label = this.mContext.getString(R.string.quick_settings_cast_title);
        state.contentDescription = state.label;
        state.value = false;
        state.autoMirrorDrawable = false;
        Set<CastDevice> devices = this.mController.getCastDevices();
        boolean connecting = false;
        for (CastDevice device : devices) {
            if (device.state == 2) {
                state.value = true;
                state.label = getDeviceName(device);
                state.contentDescription += "," + this.mContext.getString(R.string.accessibility_cast_name, new Object[]{state.label});
            } else if (device.state == 1) {
                connecting = true;
            }
        }
        if (!state.value && connecting) {
            state.label = this.mContext.getString(R.string.quick_settings_connecting);
        }
        if (state.value) {
            i = R.drawable.ic_qs_cast_on;
        } else {
            i = R.drawable.ic_qs_cast_off;
        }
        state.icon = ResourceIcon.get(i);
        this.mDetailAdapter.updateItems(devices);
        String name = Button.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
        state.contentDescription += "," + this.mContext.getString(R.string.accessibility_quick_settings_open_details);
    }

    public int getMetricsCategory() {
        return 114;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return null;
        }
        return this.mContext.getString(R.string.accessibility_casting_turned_off);
    }

    private String getDeviceName(CastDevice device) {
        if (device.name != null) {
            return device.name;
        }
        return this.mContext.getString(R.string.quick_settings_cast_device_default_name);
    }

    public boolean isAvailable() {
        return false;
    }
}
