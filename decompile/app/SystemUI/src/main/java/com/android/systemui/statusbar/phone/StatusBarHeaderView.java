package com.android.systemui.statusbar.phone;

import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.NetworkController.EmergencyListener;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback;
import com.android.systemui.tuner.TunerService;
import java.text.NumberFormat;

public class StatusBarHeaderView extends BaseStatusBarHeader implements OnClickListener, BatteryStateChangeCallback, NextAlarmChangeCallback, EmergencyListener {
    private ActivityStarter mActivityStarter;
    private boolean mAlarmShowing;
    private TextView mAlarmStatus;
    private boolean mAllowExpand = true;
    private TextView mAmPm;
    private float mAvatarCollapsedScaleFactor;
    private BatteryController mBatteryController;
    private TextView mBatteryLevel;
    private boolean mCaptureValues;
    private final Rect mClipBounds = new Rect();
    private View mClock;
    private float mClockCollapsedScaleFactor;
    private int mClockCollapsedSize;
    private int mClockExpandedSize;
    private int mClockMarginBottomCollapsed;
    private int mClockMarginBottomExpanded;
    private int mCollapsedHeight;
    private final LayoutValues mCollapsedValues = new LayoutValues();
    private float mCurrentT;
    private final LayoutValues mCurrentValues = new LayoutValues();
    private TextView mDateCollapsed;
    private TextView mDateExpanded;
    private View mDateGroup;
    private boolean mDetailTransitioning;
    private TextView mEmergencyCallsOnly;
    private boolean mExpanded;
    private int mExpandedHeight;
    private final LayoutValues mExpandedValues = new LayoutValues();
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    private int mMultiUserCollapsedMargin;
    private int mMultiUserExpandedMargin;
    private MultiUserSwitch mMultiUserSwitch;
    private int mMultiUserSwitchWidthCollapsed;
    private int mMultiUserSwitchWidthExpanded;
    private AlarmClockInfo mNextAlarm;
    private NextAlarmController mNextAlarmController;
    private QSPanel mQSPanel;
    private View mQsDetailHeader;
    private ImageView mQsDetailHeaderProgress;
    private Switch mQsDetailHeaderSwitch;
    private TextView mQsDetailHeaderTitle;
    private final Callback mQsPanelCallback = new Callback() {
        private boolean mScanState;

        public void onToggleStateChanged(final boolean state) {
            StatusBarHeaderView.this.post(new Runnable() {
                public void run() {
                    AnonymousClass1.this.handleToggleStateChanged(state);
                }
            });
        }

        public void onShowingDetail(final DetailAdapter detail, int x, int y) {
            StatusBarHeaderView.this.mDetailTransitioning = true;
            StatusBarHeaderView.this.post(new Runnable() {
                public void run() {
                    AnonymousClass1.this.handleShowingDetail(detail);
                }
            });
        }

        public void onScanStateChanged(final boolean state) {
            StatusBarHeaderView.this.post(new Runnable() {
                public void run() {
                    AnonymousClass1.this.handleScanStateChanged(state);
                }
            });
        }

        private void handleToggleStateChanged(boolean state) {
            StatusBarHeaderView.this.mQsDetailHeaderSwitch.setChecked(state);
        }

        private void handleScanStateChanged(boolean state) {
            if (this.mScanState != state) {
                this.mScanState = state;
                Animatable anim = (Animatable) StatusBarHeaderView.this.mQsDetailHeaderProgress.getDrawable();
                if (state) {
                    StatusBarHeaderView.this.mQsDetailHeaderProgress.animate().alpha(1.0f);
                    anim.start();
                } else {
                    StatusBarHeaderView.this.mQsDetailHeaderProgress.animate().alpha(0.0f);
                    anim.stop();
                }
            }
        }

        private void handleShowingDetail(final DetailAdapter detail) {
            boolean z;
            boolean showingDetail = detail != null;
            View -get3 = StatusBarHeaderView.this.mClock;
            if (showingDetail) {
                z = false;
            } else {
                z = true;
            }
            transition(-get3, z);
            -get3 = StatusBarHeaderView.this.mDateGroup;
            if (showingDetail) {
                z = false;
            } else {
                z = true;
            }
            transition(-get3, z);
            if (StatusBarHeaderView.this.mAlarmShowing) {
                -get3 = StatusBarHeaderView.this.mAlarmStatus;
                if (showingDetail) {
                    z = false;
                } else {
                    z = true;
                }
                transition(-get3, z);
            }
            transition(StatusBarHeaderView.this.mQsDetailHeader, showingDetail);
            StatusBarHeaderView.this.mShowingDetail = showingDetail;
            if (showingDetail) {
                StatusBarHeaderView.this.mQsDetailHeaderTitle.setText(detail.getTitle());
                Boolean toggleState = detail.getToggleState();
                if (toggleState == null) {
                    StatusBarHeaderView.this.mQsDetailHeaderSwitch.setVisibility(4);
                    StatusBarHeaderView.this.mQsDetailHeader.setClickable(false);
                    return;
                }
                StatusBarHeaderView.this.mQsDetailHeaderSwitch.setVisibility(0);
                StatusBarHeaderView.this.mQsDetailHeaderSwitch.setChecked(toggleState.booleanValue());
                StatusBarHeaderView.this.mQsDetailHeader.setClickable(true);
                StatusBarHeaderView.this.mQsDetailHeader.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        boolean checked = !StatusBarHeaderView.this.mQsDetailHeaderSwitch.isChecked();
                        StatusBarHeaderView.this.mQsDetailHeaderSwitch.setChecked(checked);
                        detail.setToggleState(checked);
                    }
                });
                return;
            }
            StatusBarHeaderView.this.mQsDetailHeader.setClickable(false);
        }

        private void transition(final View v, final boolean in) {
            int i = 0;
            if (in) {
                v.bringToFront();
                v.setVisibility(0);
            }
            if (v.hasOverlappingRendering()) {
                v.animate().withLayer();
            }
            ViewPropertyAnimator animate = v.animate();
            if (in) {
                i = 1;
            }
            animate.alpha((float) i).withEndAction(new Runnable() {
                public void run() {
                    if (!in) {
                        v.setVisibility(4);
                    }
                    StatusBarHeaderView.this.mDetailTransitioning = false;
                }
            }).start();
        }
    };
    private SettingsButton mSettingsButton;
    private View mSettingsContainer;
    private boolean mShowEmergencyCallsOnly;
    private boolean mShowingDetail;
    private View mSignalCluster;
    private boolean mSignalClusterDetached;
    private LinearLayout mSystemIcons;
    private ViewGroup mSystemIconsContainer;
    private View mSystemIconsSuperContainer;
    private TextView mTime;

    private static final class LayoutValues {
        float alarmStatusAlpha;
        float avatarScale;
        float avatarX;
        float avatarY;
        float batteryLevelAlpha;
        float batteryX;
        float batteryY;
        float clockY;
        float dateCollapsedAlpha;
        float dateExpandedAlpha;
        float dateY;
        float emergencyCallsOnlyAlpha;
        float settingsAlpha;
        float settingsRotation;
        float settingsTranslation;
        float signalClusterAlpha;
        float timeScale;

        private LayoutValues() {
            this.timeScale = 1.0f;
        }

        public void interpoloate(LayoutValues v1, LayoutValues v2, float t) {
            this.timeScale = (v1.timeScale * (1.0f - t)) + (v2.timeScale * t);
            this.clockY = (v1.clockY * (1.0f - t)) + (v2.clockY * t);
            this.dateY = (v1.dateY * (1.0f - t)) + (v2.dateY * t);
            this.avatarScale = (v1.avatarScale * (1.0f - t)) + (v2.avatarScale * t);
            this.avatarX = (v1.avatarX * (1.0f - t)) + (v2.avatarX * t);
            this.avatarY = (v1.avatarY * (1.0f - t)) + (v2.avatarY * t);
            this.batteryX = (v1.batteryX * (1.0f - t)) + (v2.batteryX * t);
            this.batteryY = (v1.batteryY * (1.0f - t)) + (v2.batteryY * t);
            this.settingsTranslation = (v1.settingsTranslation * (1.0f - t)) + (v2.settingsTranslation * t);
            float t1 = Math.max(0.0f, t - 0.5f) * 2.0f;
            this.settingsRotation = (v1.settingsRotation * (1.0f - t1)) + (v2.settingsRotation * t1);
            this.emergencyCallsOnlyAlpha = (v1.emergencyCallsOnlyAlpha * (1.0f - t1)) + (v2.emergencyCallsOnlyAlpha * t1);
            float t2 = Math.min(1.0f, 2.0f * t);
            this.signalClusterAlpha = (v1.signalClusterAlpha * (1.0f - t2)) + (v2.signalClusterAlpha * t2);
            float t3 = Math.max(0.0f, t - 0.7f) / 0.3f;
            this.batteryLevelAlpha = (v1.batteryLevelAlpha * (1.0f - t3)) + (v2.batteryLevelAlpha * t3);
            this.settingsAlpha = (v1.settingsAlpha * (1.0f - t3)) + (v2.settingsAlpha * t3);
            this.dateExpandedAlpha = (v1.dateExpandedAlpha * (1.0f - t3)) + (v2.dateExpandedAlpha * t3);
            this.dateCollapsedAlpha = (v1.dateCollapsedAlpha * (1.0f - t3)) + (v2.dateCollapsedAlpha * t3);
            this.alarmStatusAlpha = (v1.alarmStatusAlpha * (1.0f - t3)) + (v2.alarmStatusAlpha * t3);
        }
    }

    public StatusBarHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = findViewById(R.id.system_icons_super_container);
        this.mSystemIconsContainer = (ViewGroup) findViewById(R.id.system_icons_container);
        this.mSystemIconsSuperContainer.setOnClickListener(this);
        this.mDateGroup = findViewById(R.id.date_group);
        this.mClock = findViewById(R.id.clock);
        this.mTime = (TextView) findViewById(R.id.time_view);
        this.mAmPm = (TextView) findViewById(R.id.am_pm_view);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) findViewById(R.id.multi_user_avatar);
        this.mDateCollapsed = (TextView) findViewById(R.id.date_collapsed);
        this.mDateExpanded = (TextView) findViewById(R.id.date_expanded);
        this.mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        this.mSettingsContainer = findViewById(R.id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        this.mQsDetailHeader = findViewById(R.id.qs_detail_header);
        this.mQsDetailHeader.setAlpha(0.0f);
        this.mQsDetailHeaderTitle = (TextView) this.mQsDetailHeader.findViewById(16908310);
        this.mQsDetailHeaderSwitch = (Switch) this.mQsDetailHeader.findViewById(16908311);
        this.mQsDetailHeaderProgress = (ImageView) findViewById(R.id.qs_detail_header_progress);
        this.mEmergencyCallsOnly = (TextView) findViewById(R.id.header_emergency_calls_only);
        this.mBatteryLevel = (TextView) findViewById(R.id.battery_level);
        this.mAlarmStatus = (TextView) findViewById(R.id.alarm_status);
        this.mAlarmStatus.setOnClickListener(this);
        this.mSignalCluster = findViewById(R.id.signal_cluster);
        this.mSystemIcons = (LinearLayout) findViewById(R.id.system_icons);
        loadDimens();
        updateVisibilities();
        updateClockScale();
        updateAvatarScale();
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (right - left != oldRight - oldLeft) {
                    StatusBarHeaderView.this.setClipping((float) StatusBarHeaderView.this.getHeight());
                }
                StatusBarHeaderView.this.mTime.setPivotX((float) (StatusBarHeaderView.this.getLayoutDirection() == 1 ? StatusBarHeaderView.this.mTime.getWidth() : 0));
                StatusBarHeaderView.this.mTime.setPivotY((float) StatusBarHeaderView.this.mTime.getBaseline());
                StatusBarHeaderView.this.updateAmPmTranslation();
            }
        });
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRect(StatusBarHeaderView.this.mClipBounds);
            }
        });
        requestCaptureValues();
        ((RippleDrawable) getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mSystemIconsSuperContainer.getBackground()).setForceSoftware(true);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mCaptureValues) {
            if (this.mExpanded) {
                captureLayoutValues(this.mExpandedValues);
            } else {
                captureLayoutValues(this.mCollapsedValues);
            }
            this.mCaptureValues = false;
            updateLayoutValues(this.mCurrentT);
        }
        this.mAlarmStatus.setX((float) (this.mDateGroup.getLeft() + this.mDateCollapsed.getRight()));
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(this.mBatteryLevel, R.dimen.battery_level_text_size);
        FontSizeUtils.updateFontSize(this.mEmergencyCallsOnly, R.dimen.qs_emergency_calls_only_text_size);
        FontSizeUtils.updateFontSize(this.mDateCollapsed, R.dimen.qs_date_collapsed_size);
        FontSizeUtils.updateFontSize(this.mDateExpanded, R.dimen.qs_date_collapsed_size);
        FontSizeUtils.updateFontSize(this.mAlarmStatus, R.dimen.qs_date_collapsed_size);
        FontSizeUtils.updateFontSize(this, 16908310, R.dimen.qs_detail_header_text_size);
        FontSizeUtils.updateFontSize(this, 16908311, R.dimen.qs_detail_header_text_size);
        FontSizeUtils.updateFontSize(this.mAmPm, R.dimen.qs_time_collapsed_size);
        FontSizeUtils.updateFontSize(this, R.id.empty_time_view, R.dimen.qs_time_expanded_size);
        this.mEmergencyCallsOnly.setText(17040036);
        this.mClockCollapsedSize = getResources().getDimensionPixelSize(R.dimen.qs_time_collapsed_size);
        this.mClockExpandedSize = getResources().getDimensionPixelSize(R.dimen.qs_time_expanded_size);
        this.mClockCollapsedScaleFactor = ((float) this.mClockCollapsedSize) / ((float) this.mClockExpandedSize);
        updateClockScale();
        updateClockCollapsedMargin();
    }

    private void updateClockCollapsedMargin() {
        Resources res = getResources();
        float largeFactor = (MathUtils.constrain(getResources().getConfiguration().fontScale, 1.0f, 1.3f) - 1.0f) / 0.29999995f;
        this.mClockMarginBottomCollapsed = Math.round(((1.0f - largeFactor) * ((float) res.getDimensionPixelSize(R.dimen.clock_collapsed_bottom_margin))) + (((float) res.getDimensionPixelSize(R.dimen.clock_collapsed_bottom_margin_large_text)) * largeFactor));
        requestLayout();
    }

    private void requestCaptureValues() {
        this.mCaptureValues = true;
        requestLayout();
    }

    private void loadDimens() {
        this.mCollapsedHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        this.mExpandedHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height_expanded);
        this.mMultiUserExpandedMargin = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_expanded_margin);
        this.mMultiUserCollapsedMargin = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_collapsed_margin);
        this.mClockMarginBottomExpanded = getResources().getDimensionPixelSize(R.dimen.clock_expanded_bottom_margin);
        updateClockCollapsedMargin();
        this.mMultiUserSwitchWidthCollapsed = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_width_collapsed);
        this.mMultiUserSwitchWidthExpanded = getResources().getDimensionPixelSize(R.dimen.multi_user_switch_width_expanded);
        this.mAvatarCollapsedScaleFactor = ((float) getResources().getDimensionPixelSize(R.dimen.multi_user_avatar_collapsed_size)) / ((float) this.mMultiUserAvatar.getLayoutParams().width);
        this.mClockCollapsedSize = getResources().getDimensionPixelSize(R.dimen.qs_time_collapsed_size);
        this.mClockExpandedSize = getResources().getDimensionPixelSize(R.dimen.qs_time_expanded_size);
        this.mClockCollapsedScaleFactor = ((float) this.mClockCollapsedSize) / ((float) this.mClockExpandedSize);
    }

    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    public void setListening(boolean listening) {
        if (listening != this.mListening) {
            this.mListening = listening;
            updateListeners();
        }
    }

    public void setExpanded(boolean expanded) {
        if (!this.mAllowExpand) {
            expanded = false;
        }
        boolean changed = expanded != this.mExpanded;
        this.mExpanded = expanded;
        if (changed) {
            updateEverything();
        }
    }

    public void updateEverything() {
        updateHeights();
        updateVisibilities();
        updateSystemIconsLayoutParams();
        updateClickTargets();
        updateMultiUserSwitch();
        updateClockScale();
        updateAvatarScale();
        updateClockLp();
        requestCaptureValues();
    }

    private void updateHeights() {
        int height = this.mExpanded ? this.mExpandedHeight : this.mCollapsedHeight;
        LayoutParams lp = getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            setLayoutParams(lp);
        }
    }

    private void updateVisibilities() {
        int i;
        int i2 = 8;
        int i3 = 0;
        TextView textView = this.mDateCollapsed;
        if (this.mExpanded && this.mAlarmShowing) {
            i = 0;
        } else {
            i = 4;
        }
        textView.setVisibility(i);
        textView = this.mDateExpanded;
        if (this.mExpanded && this.mAlarmShowing) {
            i = 4;
        } else {
            i = 0;
        }
        textView.setVisibility(i);
        textView = this.mAlarmStatus;
        if (this.mExpanded && this.mAlarmShowing) {
            i = 0;
        } else {
            i = 4;
        }
        textView.setVisibility(i);
        View view = this.mSettingsContainer;
        if (this.mExpanded) {
            i = 0;
        } else {
            i = 4;
        }
        view.setVisibility(i);
        view = this.mQsDetailHeader;
        if (this.mExpanded && this.mShowingDetail) {
            i = 0;
        } else {
            i = 4;
        }
        view.setVisibility(i);
        if (this.mSignalCluster != null) {
            updateSignalClusterDetachment();
        }
        textView = this.mEmergencyCallsOnly;
        if (this.mExpanded && this.mShowEmergencyCallsOnly) {
            i = 0;
        } else {
            i = 8;
        }
        textView.setVisibility(i);
        TextView textView2 = this.mBatteryLevel;
        if (this.mExpanded) {
            i2 = 0;
        }
        textView2.setVisibility(i2);
        View findViewById = this.mSettingsContainer.findViewById(R.id.tuner_icon);
        if (!TunerService.isTunerEnabled(this.mContext)) {
            i3 = 4;
        }
        findViewById.setVisibility(i3);
    }

    private void updateSignalClusterDetachment() {
        boolean detached = this.mExpanded;
        if (detached != this.mSignalClusterDetached) {
            if (detached) {
                getOverlay().add(this.mSignalCluster);
            } else {
                reattachSignalCluster();
            }
        }
        this.mSignalClusterDetached = detached;
    }

    private void reattachSignalCluster() {
        getOverlay().remove(this.mSignalCluster);
        this.mSystemIcons.addView(this.mSignalCluster, 1);
    }

    private void updateSystemIconsLayoutParams() {
        int rule;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.mSystemIconsSuperContainer.getLayoutParams();
        if (this.mExpanded) {
            rule = this.mSettingsContainer.getId();
        } else {
            rule = this.mMultiUserSwitch.getId();
        }
        if (rule != lp.getRules()[16]) {
            lp.addRule(16, rule);
            this.mSystemIconsSuperContainer.setLayoutParams(lp);
        }
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mBatteryController.addStateChangedCallback(this);
            this.mNextAlarmController.addStateChangedCallback(this);
            return;
        }
        this.mBatteryController.removeStateChangedCallback(this);
        this.mNextAlarmController.removeStateChangedCallback(this);
    }

    private void updateAvatarScale() {
        if (this.mExpanded) {
            this.mMultiUserAvatar.setScaleX(1.0f);
            this.mMultiUserAvatar.setScaleY(1.0f);
            return;
        }
        this.mMultiUserAvatar.setScaleX(this.mAvatarCollapsedScaleFactor);
        this.mMultiUserAvatar.setScaleY(this.mAvatarCollapsedScaleFactor);
    }

    private void updateClockScale() {
        int i;
        TextView textView = this.mTime;
        if (this.mExpanded) {
            i = this.mClockExpandedSize;
        } else {
            i = this.mClockCollapsedSize;
        }
        textView.setTextSize(0, (float) i);
        this.mTime.setScaleX(1.0f);
        this.mTime.setScaleY(1.0f);
        updateAmPmTranslation();
    }

    private void updateAmPmTranslation() {
        int i = 1;
        boolean rtl = getLayoutDirection() == 1;
        TextView textView = this.mAmPm;
        if (!rtl) {
            i = -1;
        }
        textView.setTranslationX(((float) (i * this.mTime.getWidth())) * (1.0f - this.mTime.getScaleX()));
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        this.mBatteryLevel.setText(NumberFormat.getPercentInstance().format(((double) level) / 100.0d));
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
    }

    public void onNextAlarmChanged(AlarmClockInfo nextAlarm) {
        this.mNextAlarm = nextAlarm;
        if (nextAlarm != null) {
            this.mAlarmStatus.setText(KeyguardStatusView.formatNextAlarm(getContext(), nextAlarm));
        }
        this.mAlarmShowing = nextAlarm != null;
        updateEverything();
        requestCaptureValues();
    }

    private void updateClickTargets() {
        boolean z = false;
        this.mMultiUserSwitch.setClickable(this.mExpanded);
        this.mMultiUserSwitch.setFocusable(this.mExpanded);
        this.mSystemIconsSuperContainer.setClickable(this.mExpanded);
        this.mSystemIconsSuperContainer.setFocusable(this.mExpanded);
        TextView textView = this.mAlarmStatus;
        if (!(this.mNextAlarm == null || this.mNextAlarm.getShowIntent() == null)) {
            z = true;
        }
        textView.setClickable(z);
    }

    private void updateClockLp() {
        int marginBottom;
        if (this.mExpanded) {
            marginBottom = this.mClockMarginBottomExpanded;
        } else {
            marginBottom = this.mClockMarginBottomCollapsed;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) this.mDateGroup.getLayoutParams();
        if (marginBottom != lp.bottomMargin) {
            lp.bottomMargin = marginBottom;
            this.mDateGroup.setLayoutParams(lp);
        }
    }

    private void updateMultiUserSwitch() {
        int marginEnd;
        int width;
        if (this.mExpanded) {
            marginEnd = this.mMultiUserExpandedMargin;
            width = this.mMultiUserSwitchWidthExpanded;
        } else {
            marginEnd = this.mMultiUserCollapsedMargin;
            width = this.mMultiUserSwitchWidthCollapsed;
        }
        MarginLayoutParams lp = (MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        if (marginEnd != lp.getMarginEnd() || lp.width != width) {
            lp.setMarginEnd(marginEnd);
            lp.width = width;
            this.mMultiUserSwitch.setLayoutParams(lp);
        }
    }

    public void setExpansion(float t) {
        if (!this.mExpanded) {
            t = 0.0f;
        }
        this.mCurrentT = t;
        float height = ((float) this.mCollapsedHeight) + (((float) (this.mExpandedHeight - this.mCollapsedHeight)) * t);
        if (height < ((float) this.mCollapsedHeight)) {
            height = (float) this.mCollapsedHeight;
        }
        if (height > ((float) this.mExpandedHeight)) {
            height = (float) this.mExpandedHeight;
        }
        setClipping(height);
        updateLayoutValues(t);
    }

    private void updateLayoutValues(float t) {
        if (!this.mCaptureValues) {
            this.mCurrentValues.interpoloate(this.mCollapsedValues, this.mExpandedValues, t);
            applyLayoutValues(this.mCurrentValues);
        }
    }

    private void setClipping(float height) {
        this.mClipBounds.set(getPaddingLeft(), 0, getWidth() - getPaddingRight(), (int) height);
        setClipBounds(this.mClipBounds);
        invalidateOutline();
    }

    public void setCallback(Callback qsPanelCallback) {
    }

    public void onClick(View v) {
        if (v == this.mSettingsButton) {
            if (this.mSettingsButton.isTunerClick()) {
                if (TunerService.isTunerEnabled(this.mContext)) {
                    TunerService.showResetRequest(this.mContext, new Runnable() {
                        public void run() {
                            StatusBarHeaderView.this.startSettingsActivity();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), R.string.tuner_toast, 1).show();
                    TunerService.setTunerEnabled(this.mContext, true);
                }
            }
            startSettingsActivity();
        } else if (v == this.mSystemIconsSuperContainer) {
            startBatteryActivity();
        } else if (v == this.mAlarmStatus && this.mNextAlarm != null) {
            PendingIntent showIntent = this.mNextAlarm.getShowIntent();
            if (showIntent != null) {
                this.mActivityStarter.startPendingIntentDismissingKeyguard(showIntent);
            }
        }
    }

    private void startSettingsActivity() {
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    private void startBatteryActivity() {
        this.mActivityStarter.startActivity(new Intent("android.intent.action.POWER_USAGE_SUMMARY"), true);
    }

    public void setQSPanel(QSPanel qsp) {
        this.mQSPanel = qsp;
        if (this.mQSPanel != null) {
            this.mQSPanel.setCallback(this.mQsPanelCallback);
        }
        this.mMultiUserSwitch.setQsPanel(qsp);
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public void setEmergencyCallsOnly(boolean show) {
        if (show != this.mShowEmergencyCallsOnly) {
            this.mShowEmergencyCallsOnly = show;
            if (this.mExpanded) {
                updateEverything();
                requestCaptureValues();
            }
        }
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    private void captureLayoutValues(LayoutValues target) {
        int i;
        float f = 1.0f;
        float f2 = 0.0f;
        target.timeScale = this.mExpanded ? 1.0f : this.mClockCollapsedScaleFactor;
        target.clockY = (float) this.mClock.getBottom();
        target.dateY = (float) this.mDateGroup.getTop();
        target.emergencyCallsOnlyAlpha = getAlphaForVisibility(this.mEmergencyCallsOnly);
        target.alarmStatusAlpha = getAlphaForVisibility(this.mAlarmStatus);
        target.dateCollapsedAlpha = getAlphaForVisibility(this.mDateCollapsed);
        target.dateExpandedAlpha = getAlphaForVisibility(this.mDateExpanded);
        target.avatarScale = this.mMultiUserAvatar.getScaleX();
        target.avatarX = (float) (this.mMultiUserSwitch.getLeft() + this.mMultiUserAvatar.getLeft());
        target.avatarY = (float) (this.mMultiUserSwitch.getTop() + this.mMultiUserAvatar.getTop());
        if (getLayoutDirection() == 0) {
            target.batteryX = (float) (this.mSystemIconsSuperContainer.getLeft() + this.mSystemIconsContainer.getRight());
        } else {
            target.batteryX = (float) (this.mSystemIconsSuperContainer.getLeft() + this.mSystemIconsContainer.getLeft());
        }
        target.batteryY = (float) (this.mSystemIconsSuperContainer.getTop() + this.mSystemIconsContainer.getTop());
        target.batteryLevelAlpha = getAlphaForVisibility(this.mBatteryLevel);
        target.settingsAlpha = getAlphaForVisibility(this.mSettingsContainer);
        if (this.mExpanded) {
            i = 0;
        } else {
            i = this.mMultiUserSwitch.getLeft() - this.mSettingsContainer.getLeft();
        }
        target.settingsTranslation = (float) i;
        if (this.mSignalClusterDetached) {
            f = 0.0f;
        }
        target.signalClusterAlpha = f;
        if (!this.mExpanded) {
            f2 = 90.0f;
        }
        target.settingsRotation = f2;
    }

    private float getAlphaForVisibility(View v) {
        return (v == null || v.getVisibility() == 0) ? 1.0f : 0.0f;
    }

    private void applyAlpha(View v, float alpha) {
        if (v != null && v.getVisibility() != 8) {
            if (alpha == 0.0f) {
                v.setVisibility(4);
            } else {
                v.setVisibility(0);
                v.setAlpha(alpha);
            }
        }
    }

    private void applyLayoutValues(LayoutValues values) {
        this.mTime.setScaleX(values.timeScale);
        this.mTime.setScaleY(values.timeScale);
        this.mClock.setY(values.clockY - ((float) this.mClock.getHeight()));
        this.mDateGroup.setY(values.dateY);
        this.mAlarmStatus.setY(values.dateY - ((float) this.mAlarmStatus.getPaddingTop()));
        this.mMultiUserAvatar.setScaleX(values.avatarScale);
        this.mMultiUserAvatar.setScaleY(values.avatarScale);
        this.mMultiUserAvatar.setX(values.avatarX - ((float) this.mMultiUserSwitch.getLeft()));
        this.mMultiUserAvatar.setY(values.avatarY - ((float) this.mMultiUserSwitch.getTop()));
        if (getLayoutDirection() == 0) {
            this.mSystemIconsSuperContainer.setX(values.batteryX - ((float) this.mSystemIconsContainer.getRight()));
        } else {
            this.mSystemIconsSuperContainer.setX(values.batteryX - ((float) this.mSystemIconsContainer.getLeft()));
        }
        this.mSystemIconsSuperContainer.setY(values.batteryY - ((float) this.mSystemIconsContainer.getTop()));
        if (this.mSignalCluster != null && this.mExpanded) {
            if (getLayoutDirection() == 0) {
                this.mSignalCluster.setX(this.mSystemIconsSuperContainer.getX() - ((float) this.mSignalCluster.getWidth()));
            } else {
                this.mSignalCluster.setX(this.mSystemIconsSuperContainer.getX() + ((float) this.mSystemIconsSuperContainer.getWidth()));
            }
            this.mSignalCluster.setY((this.mSystemIconsSuperContainer.getY() + ((float) (this.mSystemIconsSuperContainer.getHeight() / 2))) - ((float) (this.mSignalCluster.getHeight() / 2)));
        } else if (this.mSignalCluster != null) {
            this.mSignalCluster.setTranslationX(0.0f);
            this.mSignalCluster.setTranslationY(0.0f);
        }
        if (!this.mSettingsButton.isAnimating()) {
            this.mSettingsContainer.setTranslationY(this.mSystemIconsSuperContainer.getTranslationY());
            this.mSettingsContainer.setTranslationX(values.settingsTranslation);
            this.mSettingsButton.setRotation(values.settingsRotation);
        }
        applyAlpha(this.mEmergencyCallsOnly, values.emergencyCallsOnlyAlpha);
        if (!(this.mShowingDetail || this.mDetailTransitioning)) {
            applyAlpha(this.mAlarmStatus, values.alarmStatusAlpha);
        }
        applyAlpha(this.mDateCollapsed, values.dateCollapsedAlpha);
        applyAlpha(this.mDateExpanded, values.dateExpandedAlpha);
        applyAlpha(this.mBatteryLevel, values.batteryLevelAlpha);
        applyAlpha(this.mSettingsContainer, values.settingsAlpha);
        applyAlpha(this.mSignalCluster, values.signalClusterAlpha);
        if (!this.mExpanded) {
            this.mTime.setScaleX(1.0f);
            this.mTime.setScaleY(1.0f);
        }
        updateAmPmTranslation();
    }

    public void onDetailsAnimateStarted() {
        setVisibility(4);
    }
}
