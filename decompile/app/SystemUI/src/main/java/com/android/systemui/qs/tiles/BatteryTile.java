package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.BatteryInfo;
import com.android.settingslib.BatteryInfo.BatteryDataParser;
import com.android.settingslib.BatteryInfo.Callback;
import com.android.settingslib.graph.UsageView;
import com.android.systemui.BatteryMeterDrawable;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.Icon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import fyusion.vislib.BuildConfig;
import java.text.NumberFormat;

public class BatteryTile extends QSTile<State> implements BatteryStateChangeCallback {
    private final BatteryController mBatteryController;
    private final BatteryDetail mBatteryDetail = new BatteryDetail();
    private boolean mCharging;
    private boolean mDetailShown;
    private int mLevel;
    private boolean mPluggedIn;
    private boolean mPowerSave;

    private final class BatteryDetail implements DetailAdapter, OnClickListener, OnAttachStateChangeListener {
        private View mCurrentView;
        private final BatteryMeterDrawable mDrawable;
        private final BroadcastReceiver mReceiver;

        private BatteryDetail() {
            this.mDrawable = new BatteryMeterDrawable(BatteryTile.this.mHost.getContext(), new Handler(), BatteryTile.this.mHost.getContext().getColor(R.color.batterymeter_frame_color));
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    BatteryDetail.this.postBindView();
                }
            };
        }

        public CharSequence getTitle() {
            return BatteryTile.this.mContext.getString(R.string.battery_panel_title, new Object[]{Integer.valueOf(BatteryTile.this.mLevel)});
        }

        public Boolean getToggleState() {
            return null;
        }

        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(BatteryTile.this.mContext).inflate(R.layout.battery_detail, parent, false);
            }
            this.mCurrentView = convertView;
            this.mCurrentView.addOnAttachStateChangeListener(this);
            bindView();
            return convertView;
        }

        private void postBindView() {
            if (this.mCurrentView != null) {
                this.mCurrentView.post(new Runnable() {
                    public void run() {
                        BatteryDetail.this.bindView();
                    }
                });
            }
        }

        private void bindView() {
            if (this.mCurrentView != null) {
                this.mDrawable.onBatteryLevelChanged(100, false, false);
                this.mDrawable.onPowerSaveChanged(true);
                this.mDrawable.disableShowPercent();
                ((ImageView) this.mCurrentView.findViewById(16908294)).setImageDrawable(this.mDrawable);
                ((Checkable) this.mCurrentView.findViewById(16908311)).setChecked(BatteryTile.this.mPowerSave);
                BatteryInfo.getBatteryInfo(BatteryTile.this.mContext, new Callback() {
                    public void onBatteryInfoLoaded(BatteryInfo info) {
                        if (BatteryDetail.this.mCurrentView != null) {
                            BatteryDetail.this.bindBatteryInfo(info);
                        }
                    }
                });
                TextView batterySaverTitle = (TextView) this.mCurrentView.findViewById(16908310);
                TextView batterySaverSummary = (TextView) this.mCurrentView.findViewById(16908304);
                if (BatteryTile.this.mCharging) {
                    this.mCurrentView.findViewById(R.id.switch_container).setAlpha(0.7f);
                    batterySaverTitle.setTextSize(2, 14.0f);
                    batterySaverTitle.setText(R.string.battery_detail_charging_summary);
                    this.mCurrentView.findViewById(16908311).setVisibility(8);
                    this.mCurrentView.findViewById(R.id.switch_container).setClickable(false);
                } else {
                    this.mCurrentView.findViewById(R.id.switch_container).setAlpha(1.0f);
                    batterySaverTitle.setTextSize(2, 16.0f);
                    batterySaverTitle.setText(R.string.battery_detail_switch_title);
                    batterySaverSummary.setText(R.string.battery_detail_switch_summary);
                    this.mCurrentView.findViewById(16908311).setVisibility(0);
                    this.mCurrentView.findViewById(R.id.switch_container).setClickable(true);
                    this.mCurrentView.findViewById(R.id.switch_container).setOnClickListener(this);
                }
            }
        }

        private void bindBatteryInfo(BatteryInfo info) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(info.batteryPercentString, new RelativeSizeSpan(2.6f), 17);
            if (info.remainingLabel != null) {
                if (BatteryTile.this.mContext.getResources().getBoolean(R.bool.quick_settings_wide)) {
                    builder.append(' ');
                } else {
                    builder.append('\n');
                }
                builder.append(info.remainingLabel);
            }
            ((TextView) this.mCurrentView.findViewById(R.id.charge_and_estimation)).setText(builder);
            info.bindHistory((UsageView) this.mCurrentView.findViewById(R.id.battery_usage), new BatteryDataParser[0]);
        }

        public void onClick(View v) {
            BatteryTile.this.mBatteryController.setPowerSaveMode(!BatteryTile.this.mPowerSave);
        }

        public Intent getSettingsIntent() {
            return new Intent("android.intent.action.POWER_USAGE_SUMMARY");
        }

        public void setToggleState(boolean state) {
        }

        public int getMetricsCategory() {
            return 274;
        }

        public void onViewAttachedToWindow(View v) {
            if (!BatteryTile.this.mDetailShown) {
                BatteryTile.this.mDetailShown = true;
                v.getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.TIME_TICK"));
            }
        }

        public void onViewDetachedFromWindow(View v) {
            if (BatteryTile.this.mDetailShown) {
                BatteryTile.this.mDetailShown = false;
                v.getContext().unregisterReceiver(this.mReceiver);
            }
        }
    }

    public BatteryTile(Host host) {
        super(host);
        this.mBatteryController = host.getBatteryController();
    }

    public State newTileState() {
        return new State();
    }

    public DetailAdapter getDetailAdapter() {
        return this.mBatteryDetail;
    }

    public int getMetricsCategory() {
        return 261;
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mBatteryController.addStateChangedCallback(this);
        } else {
            this.mBatteryController.removeStateChangedCallback(this);
        }
    }

    public void setDetailListening(boolean listening) {
        super.setDetailListening(listening);
        if (!listening) {
            this.mBatteryDetail.mCurrentView = null;
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.intent.action.POWER_USAGE_SUMMARY");
    }

    protected void handleClick() {
        showDetail(true);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.battery);
    }

    protected void handleUpdateState(State state, Object arg) {
        String string;
        String percentage = NumberFormat.getPercentInstance().format(((double) (arg != null ? ((Integer) arg).intValue() : this.mLevel)) / 100.0d);
        state.icon = new Icon() {
            public Drawable getDrawable(Context context) {
                BatteryMeterDrawable drawable = new BatteryMeterDrawable(context, new Handler(Looper.getMainLooper()), context.getColor(R.color.batterymeter_frame_color));
                drawable.onBatteryLevelChanged(BatteryTile.this.mLevel, BatteryTile.this.mPluggedIn, BatteryTile.this.mCharging);
                drawable.onPowerSaveChanged(BatteryTile.this.mPowerSave);
                return drawable;
            }

            public int getPadding() {
                return BatteryTile.this.mHost.getContext().getResources().getDimensionPixelSize(R.dimen.qs_battery_padding);
            }
        };
        state.label = percentage;
        StringBuilder append = new StringBuilder().append(this.mContext.getString(R.string.accessibility_quick_settings_battery, new Object[]{percentage})).append(",");
        if (this.mPowerSave) {
            string = this.mContext.getString(R.string.battery_saver_notification_title);
        } else if (this.mCharging) {
            string = this.mContext.getString(R.string.expanded_header_battery_charging);
        } else {
            string = BuildConfig.FLAVOR;
        }
        state.contentDescription = append.append(string).append(",").append(this.mContext.getString(R.string.accessibility_battery_details)).toString();
        string = Button.class.getName();
        state.expandedAccessibilityClassName = string;
        state.minimalAccessibilityClassName = string;
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        this.mLevel = level;
        this.mPluggedIn = pluggedIn;
        this.mCharging = charging;
        refreshState(Integer.valueOf(level));
        if (this.mDetailShown) {
            this.mBatteryDetail.postBindView();
        }
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
        this.mPowerSave = isPowerSave;
        refreshState(null);
        if (this.mDetailShown) {
            this.mBatteryDetail.postBindView();
        }
    }

    public boolean isAvailable() {
        return false;
    }
}
