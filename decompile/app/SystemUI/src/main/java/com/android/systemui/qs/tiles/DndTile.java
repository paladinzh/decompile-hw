package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeController.Callback;
import com.android.systemui.volume.ZenModePanel;

public class DndTile extends QSTile<BooleanState> {
    private static final Icon TOTAL_SILENCE = ResourceIcon.get(R.drawable.ic_qs_dnd_on_total_silence);
    private static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    private static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    private final ZenModeController mController;
    private final DndDetailAdapter mDetailAdapter;
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_dnd_disable_animation, R.drawable.ic_qs_dnd_off);
    private final AnimationIcon mDisableTotalSilence = new AnimationIcon(R.drawable.ic_dnd_total_silence_disable_animation, R.drawable.ic_qs_dnd_off);
    private boolean mListening;
    private final OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("DndTileCombinedIcon".equals(key) || "DndTileVisible".equals(key)) {
                DndTile.this.refreshState();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DndTile.setVisible(DndTile.this.mContext, intent.getBooleanExtra("visible", false));
            DndTile.this.refreshState();
        }
    };
    private boolean mShowingDetail;
    private final Callback mZenCallback = new Callback() {
        public void onZenChanged(int zen) {
            DndTile.this.refreshState(Integer.valueOf(zen));
        }
    };
    private final ZenModePanel.Callback mZenModePanelCallback = new ZenModePanel.Callback() {
        public void onPrioritySettings() {
            DndTile.this.mHost.startActivityDismissingKeyguard(DndTile.ZEN_PRIORITY_SETTINGS);
        }

        public void onInteraction() {
        }

        public void onExpanded(boolean expanded) {
        }
    };

    private final class DndDetailAdapter implements DetailAdapter, OnAttachStateChangeListener {
        private DndDetailAdapter() {
        }

        public CharSequence getTitle() {
            return DndTile.this.mContext.getString(R.string.quick_settings_dnd_label);
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(((BooleanState) DndTile.this.mState).value);
        }

        public Intent getSettingsIntent() {
            return DndTile.ZEN_SETTINGS;
        }

        public void setToggleState(boolean state) {
            MetricsLogger.action(DndTile.this.mContext, 166, state);
            if (!state) {
                DndTile.this.mController.setZen(0, null, DndTile.this.TAG);
                DndTile.this.showDetail(false);
            }
        }

        public int getMetricsCategory() {
            return 149;
        }

        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            ZenModePanel zmp;
            if (convertView != null) {
                zmp = (ZenModePanel) convertView;
            } else {
                zmp = (ZenModePanel) LayoutInflater.from(context).inflate(R.layout.zen_mode_panel, parent, false);
            }
            if (convertView == null) {
                zmp.init(DndTile.this.mController);
                zmp.addOnAttachStateChangeListener(this);
                zmp.setCallback(DndTile.this.mZenModePanelCallback);
            }
            return zmp;
        }

        public void onViewAttachedToWindow(View v) {
            DndTile.this.mShowingDetail = true;
        }

        public void onViewDetachedFromWindow(View v) {
            DndTile.this.mShowingDetail = false;
        }
    }

    public DndTile(Host host) {
        super(host);
        this.mController = host.getZenModeController();
        this.mDetailAdapter = new DndDetailAdapter();
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("com.android.systemui.dndtile.SET_VISIBLE"));
    }

    public static void setVisible(Context context, boolean visible) {
        Prefs.putBoolean(context, "DndTileVisible", visible);
    }

    public static boolean isVisible(Context context) {
        return Prefs.getBoolean(context, "DndTileVisible", false);
    }

    public static void setCombinedIcon(Context context, boolean combined) {
        Prefs.putBoolean(context, "DndTileCombinedIcon", combined);
    }

    public static boolean isCombinedIcon(Context context) {
        return Prefs.getBoolean(context, "DndTileCombinedIcon", false);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public Intent getLongClickIntent() {
        return ZEN_SETTINGS;
    }

    public void handleClick() {
        if (this.mController.isVolumeRestricted()) {
            this.mHost.collapsePanels();
            SysUIToast.makeText(this.mContext, this.mContext.getString(17040670), 1).show();
            return;
        }
        boolean z;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (((BooleanState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        if (((BooleanState) this.mState).value) {
            this.mController.setZen(0, null, this.TAG);
        } else {
            this.mController.setZen(Prefs.getInt(this.mContext, "DndFavoriteZen", 3), null, this.TAG);
            showDetail(true);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_dnd_label);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        int zen = arg instanceof Integer ? ((Integer) arg).intValue() : this.mController.getZen();
        boolean newValue = zen != 0;
        boolean valueChanged = state.value != newValue;
        state.value = newValue;
        checkIfRestrictionEnforcedByAdminOnly(state, "no_adjust_volume");
        switch (zen) {
            case 1:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_dnd_on);
                state.label = this.mContext.getString(R.string.quick_settings_dnd_priority_label);
                state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_dnd_priority_on);
                break;
            case 2:
                state.icon = TOTAL_SILENCE;
                state.label = this.mContext.getString(R.string.quick_settings_dnd_none_label);
                state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_dnd_none_on);
                break;
            case 3:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_dnd_on);
                state.label = this.mContext.getString(R.string.quick_settings_dnd_alarms_label);
                state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_dnd_alarms_on);
                break;
            default:
                state.icon = TOTAL_SILENCE.equals(state.icon) ? this.mDisableTotalSilence : this.mDisable;
                state.label = this.mContext.getString(R.string.quick_settings_dnd_label);
                state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_dnd);
                break;
        }
        if (this.mShowingDetail && !state.value) {
            showDetail(false);
        }
        if (valueChanged) {
            fireToggleStateChanged(state.value);
        }
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 118;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_dnd_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_dnd_changed_off);
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            if (this.mListening) {
                this.mController.addCallback(this.mZenCallback);
                Prefs.registerListener(this.mContext, this.mPrefListener);
            } else {
                this.mController.removeCallback(this.mZenCallback);
                Prefs.unregisterListener(this.mContext, this.mPrefListener);
            }
        }
    }

    public boolean isAvailable() {
        return false;
    }
}
