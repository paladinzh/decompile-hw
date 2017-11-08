package com.android.systemui.statusbar.phone;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.qs.TouchAnimator.Builder;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.tuner.TunerService;

public class QuickStatusBarHeader extends BaseStatusBarHeader implements NextAlarmChangeCallback, OnClickListener, OnUserInfoChangedListener {
    private ActivityStarter mActivityStarter;
    private boolean mAlarmShowing;
    private TextView mAlarmStatus;
    private View mAlarmStatusCollapsed;
    private TouchAnimator mAlarmTranslation;
    private float mDateScaleFactor;
    private TouchAnimator mDateSizeAnimator;
    private ViewGroup mDateTimeAlarmGroup;
    private float mDateTimeAlarmTranslation;
    private ViewGroup mDateTimeGroup;
    private float mDateTimeTranslation;
    private TextView mEmergencyOnly;
    protected ExpandableIndicator mExpandIndicator;
    private boolean mExpanded;
    private float mExpansionAmount;
    private TouchAnimator mFirstHalfAnimator;
    protected float mGearTranslation;
    private QuickQSPanel mHeaderQsPanel;
    private QSTileHost mHost;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private AlarmClockInfo mNextAlarm;
    private NextAlarmController mNextAlarmController;
    private QSPanel mQsPanel;
    private TouchAnimator mSecondHalfAnimator;
    protected TouchAnimator mSettingsAlpha;
    private SettingsButton mSettingsButton;
    protected View mSettingsContainer;
    private boolean mShowEmergencyCallsOnly;
    private boolean mShowFullAlarm;

    final /* synthetic */ class -void_onClick_android_view_View_v_LambdaImpl0 implements Runnable {
        private /* synthetic */ QuickStatusBarHeader val$this;

        public /* synthetic */ -void_onClick_android_view_View_v_LambdaImpl0(QuickStatusBarHeader quickStatusBarHeader) {
            this.val$this = quickStatusBarHeader;
        }

        public void run() {
            this.val$this.-com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$1();
        }
    }

    public QuickStatusBarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mEmergencyOnly = (TextView) findViewById(R.id.header_emergency_calls_only);
        this.mDateTimeAlarmGroup = (ViewGroup) findViewById(R.id.date_time_alarm_group);
        this.mDateTimeAlarmGroup.findViewById(R.id.empty_time_view).setVisibility(8);
        this.mDateTimeGroup = (ViewGroup) findViewById(R.id.date_time_group);
        this.mDateTimeGroup.setPivotX(0.0f);
        this.mDateTimeGroup.setPivotY(0.0f);
        this.mShowFullAlarm = getResources().getBoolean(R.bool.quick_settings_show_full_alarm);
        this.mExpandIndicator = (ExpandableIndicator) findViewById(R.id.expand_indicator);
        this.mHeaderQsPanel = (QuickQSPanel) findViewById(R.id.quick_qs_panel);
        this.mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        this.mSettingsContainer = findViewById(R.id.settings_button_container);
        this.mSettingsButton.setOnClickListener(this);
        this.mAlarmStatusCollapsed = findViewById(R.id.alarm_status_collapsed);
        this.mAlarmStatus = (TextView) findViewById(R.id.alarm_status);
        this.mAlarmStatus.setOnClickListener(this);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) this.mMultiUserSwitch.findViewById(R.id.multi_user_avatar);
        ((RippleDrawable) this.mSettingsButton.getBackground()).setForceSoftware(true);
        ((RippleDrawable) this.mExpandIndicator.getBackground()).setForceSoftware(true);
        updateResources();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateResources();
    }

    private void updateResources() {
        FontSizeUtils.updateFontSize(this.mAlarmStatus, R.dimen.qs_date_collapsed_size);
        FontSizeUtils.updateFontSize(this.mEmergencyOnly, R.dimen.qs_emergency_calls_only_text_size);
        this.mGearTranslation = this.mContext.getResources().getDimension(R.dimen.qs_header_gear_translation);
        this.mDateTimeTranslation = this.mContext.getResources().getDimension(R.dimen.qs_date_anim_translation);
        this.mDateTimeAlarmTranslation = this.mContext.getResources().getDimension(R.dimen.qs_date_alarm_anim_translation);
        this.mDateScaleFactor = this.mContext.getResources().getDimension(R.dimen.qs_date_text_size) / this.mContext.getResources().getDimension(R.dimen.qs_date_collapsed_text_size);
        updateDateTimePosition();
        this.mSecondHalfAnimator = new Builder().addFloat(this.mShowFullAlarm ? this.mAlarmStatus : findViewById(R.id.date), "alpha", 0.0f, 1.0f).addFloat(this.mEmergencyOnly, "alpha", 0.0f, 1.0f).setStartDelay(0.5f).build();
        if (this.mShowFullAlarm) {
            this.mFirstHalfAnimator = new Builder().addFloat(this.mAlarmStatusCollapsed, "alpha", 1.0f, 0.0f).setEndDelay(0.5f).build();
        }
        this.mDateSizeAnimator = new Builder().addFloat(this.mDateTimeGroup, "scaleX", 1.0f, this.mDateScaleFactor).addFloat(this.mDateTimeGroup, "scaleY", 1.0f, this.mDateScaleFactor).setStartDelay(0.36f).build();
        updateSettingsAnimator();
    }

    protected void updateSettingsAnimator() {
        int i = 0;
        float[] fArr = new float[]{-this.mGearTranslation, 0.0f};
        fArr = new float[]{-90.0f, 0.0f};
        fArr = new float[]{0.0f, 1.0f};
        fArr = new float[]{0.0f, 1.0f};
        this.mSettingsAlpha = new Builder().addFloat(this.mSettingsContainer, "translationY", -this.mGearTranslation, 0.0f).addFloat(this.mMultiUserSwitch, "translationY", fArr).addFloat(this.mSettingsButton, "rotation", fArr).addFloat(this.mSettingsContainer, "alpha", fArr).addFloat(this.mMultiUserSwitch, "alpha", fArr).setStartDelay(0.7f).build();
        boolean isRtl = isLayoutRtl();
        if (isRtl && this.mDateTimeGroup.getWidth() == 0) {
            this.mDateTimeGroup.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    QuickStatusBarHeader.this.mDateTimeGroup.setPivotX((float) QuickStatusBarHeader.this.getWidth());
                    QuickStatusBarHeader.this.mDateTimeGroup.removeOnLayoutChangeListener(this);
                }
            });
            return;
        }
        ViewGroup viewGroup = this.mDateTimeGroup;
        if (isRtl) {
            i = this.mDateTimeGroup.getWidth();
        }
        viewGroup.setPivotX((float) i);
    }

    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
        this.mHeaderQsPanel.setExpanded(expanded);
        updateEverything();
    }

    public void onNextAlarmChanged(AlarmClockInfo nextAlarm) {
        boolean z;
        boolean z2 = true;
        this.mNextAlarm = nextAlarm;
        if (nextAlarm != null) {
            this.mAlarmStatus.setText(KeyguardStatusView.formatNextAlarm(getContext(), nextAlarm));
            this.mAlarmStatus.setContentDescription(this.mContext.getString(R.string.accessibility_quick_settings_alarm, new Object[]{alarmString}));
            this.mAlarmStatusCollapsed.setContentDescription(this.mContext.getString(R.string.accessibility_quick_settings_alarm, new Object[]{alarmString}));
        }
        boolean z3 = this.mAlarmShowing;
        if (nextAlarm != null) {
            z = true;
        } else {
            z = false;
        }
        if (z3 != z) {
            if (nextAlarm == null) {
                z2 = false;
            }
            this.mAlarmShowing = z2;
            updateEverything();
        }
    }

    public void setExpansion(float headerExpansionFraction) {
        boolean z;
        this.mExpansionAmount = headerExpansionFraction;
        this.mSecondHalfAnimator.setPosition(headerExpansionFraction);
        if (this.mShowFullAlarm) {
            this.mFirstHalfAnimator.setPosition(headerExpansionFraction);
        }
        this.mDateSizeAnimator.setPosition(headerExpansionFraction);
        this.mAlarmTranslation.setPosition(headerExpansionFraction);
        this.mSettingsAlpha.setPosition(headerExpansionFraction);
        updateAlarmVisibilities();
        ExpandableIndicator expandableIndicator = this.mExpandIndicator;
        if (headerExpansionFraction > 0.93f) {
            z = true;
        } else {
            z = false;
        }
        expandableIndicator.setExpanded(z);
    }

    protected void onDetachedFromWindow() {
        setListening(false);
        this.mHost.getUserInfoController().remListener(this);
        this.mHost.getNetworkController().removeEmergencyListener(this);
        super.onDetachedFromWindow();
    }

    private void updateAlarmVisibilities() {
        int i;
        int i2 = 0;
        TextView textView = this.mAlarmStatus;
        if (this.mAlarmShowing && this.mShowFullAlarm) {
            i = 0;
        } else {
            i = 4;
        }
        textView.setVisibility(i);
        View view = this.mAlarmStatusCollapsed;
        if (!this.mAlarmShowing) {
            i2 = 4;
        }
        view.setVisibility(i2);
    }

    private void updateDateTimePosition() {
        Builder builder = new Builder();
        ViewGroup viewGroup = this.mDateTimeAlarmGroup;
        String str = "translationY";
        float[] fArr = new float[2];
        fArr[0] = 0.0f;
        fArr[1] = this.mAlarmShowing ? this.mDateTimeAlarmTranslation : this.mDateTimeTranslation;
        this.mAlarmTranslation = builder.addFloat(viewGroup, str, fArr).build();
        this.mAlarmTranslation.setPosition(this.mExpansionAmount);
    }

    public void setListening(boolean listening) {
        if (listening != this.mListening) {
            this.mHeaderQsPanel.setListening(listening);
            this.mListening = listening;
            updateListeners();
        }
    }

    public void updateEverything() {
        updateDateTimePosition();
        updateVisibilities();
        setClickable(false);
    }

    protected void updateVisibilities() {
        int i;
        int i2 = 0;
        updateAlarmVisibilities();
        TextView textView = this.mEmergencyOnly;
        if (this.mExpanded && this.mShowEmergencyCallsOnly) {
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
        view = this.mSettingsContainer.findViewById(R.id.tuner_icon);
        if (TunerService.isTunerEnabled(this.mContext)) {
            i = 0;
        } else {
            i = 4;
        }
        view.setVisibility(i);
        MultiUserSwitch multiUserSwitch = this.mMultiUserSwitch;
        if (!(this.mExpanded && this.mMultiUserSwitch.hasMultipleUsers())) {
            i2 = 4;
        }
        multiUserSwitch.setVisibility(i2);
    }

    private void updateListeners() {
        if (this.mListening) {
            this.mNextAlarmController.addStateChangedCallback(this);
        } else {
            this.mNextAlarmController.removeStateChangedCallback(this);
        }
    }

    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    public void setQSPanel(QSPanel qsPanel) {
        this.mQsPanel = qsPanel;
        setupHost(qsPanel.getHost());
        if (this.mQsPanel != null) {
            this.mMultiUserSwitch.setQsPanel(qsPanel);
        }
    }

    public void setupHost(QSTileHost host) {
        this.mHost = host;
        host.setHeaderView(this.mExpandIndicator);
        this.mHeaderQsPanel.setQSPanelAndHeader(this.mQsPanel, this);
        this.mHeaderQsPanel.setHost(host, null);
        setUserInfoController(host.getUserInfoController());
        setBatteryController(host.getBatteryController());
        setNextAlarmController(host.getNextAlarmController());
        if (this.mHost.getNetworkController().hasVoiceCallingFeature()) {
            this.mHost.getNetworkController().addEmergencyListener(this);
        }
    }

    public void onClick(View v) {
        if (v == this.mSettingsButton) {
            MetricsLogger.action(this.mContext, 406);
            if (this.mSettingsButton.isTunerClick()) {
                this.mHost.startRunnableDismissingKeyguard(new -void_onClick_android_view_View_v_LambdaImpl0());
            } else {
                startSettingsActivity();
            }
        } else if (v == this.mAlarmStatus && this.mNextAlarm != null) {
            this.mActivityStarter.startPendingIntentDismissingKeyguard(this.mNextAlarm.getShowIntent());
        }
    }

    /* synthetic */ void -com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$1() {
        post(new QuickStatusBarHeader$-void_-com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$1__LambdaImpl0());
    }

    /* synthetic */ void -com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$2() {
        if (TunerService.isTunerEnabled(this.mContext)) {
            TunerService.showResetRequest(this.mContext, new QuickStatusBarHeader$-void_-com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$2__LambdaImpl0());
        } else {
            Toast.makeText(getContext(), R.string.tuner_toast, 1).show();
            TunerService.setTunerEnabled(this.mContext, true);
        }
        startSettingsActivity();
    }

    /* synthetic */ void -com_android_systemui_statusbar_phone_QuickStatusBarHeader_lambda$3() {
        startSettingsActivity();
    }

    private void startSettingsActivity() {
        this.mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    public void setNextAlarmController(NextAlarmController nextAlarmController) {
        this.mNextAlarmController = nextAlarmController;
    }

    public void setBatteryController(BatteryController batteryController) {
    }

    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(this);
    }

    public void setCallback(Callback qsPanelCallback) {
        this.mHeaderQsPanel.setCallback(qsPanelCallback);
    }

    public void setEmergencyCallsOnly(boolean show) {
        if (show != this.mShowEmergencyCallsOnly) {
            this.mShowEmergencyCallsOnly = show;
            if (this.mExpanded) {
                updateEverything();
            }
        }
    }

    public void onUserInfoChanged(String name, Drawable picture) {
        this.mMultiUserAvatar.setImageDrawable(picture);
    }

    public void onDetailsAnimateStarted() {
        setVisibility(4);
    }
}
