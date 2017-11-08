package com.android.systemui.statusbar.phone;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.utils.HwLog;
import com.huawei.keyguard.support.RemoteLockUtils;

public class HwQuickStatusBarHeader extends BaseStatusBarHeader implements NextAlarmChangeCallback, OnClickListener, OnUserInfoChangedListener {
    private ActivityStarter mActivityStarter;
    private View mClockView;
    private View mDateView;
    private View mEditButton;
    protected ExpandableIndicator mExpandIndicator;
    private boolean mExpanded;
    private QSTileHost mHost;
    private boolean mIsSuperPowerModeOn;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private NextAlarmController mNextAlarmController;
    private View mQSHeader;
    private QSPanel mQsPanel;
    private SettingsButton mSettingsButton;
    private boolean mShowEmergencyCallsOnly;

    final /* synthetic */ class -void_onFinishInflate__LambdaImpl0 implements OnClickListener {
        private /* synthetic */ HwQuickStatusBarHeader val$this;

        public /* synthetic */ -void_onFinishInflate__LambdaImpl0(HwQuickStatusBarHeader hwQuickStatusBarHeader) {
            this.val$this = hwQuickStatusBarHeader;
        }

        public void onClick(View arg0) {
            this.val$this.-com_android_systemui_statusbar_phone_HwQuickStatusBarHeader_lambda$1(arg0);
        }
    }

    class ShowEditRunner implements Runnable {
        ShowEditRunner() {
        }

        public void run() {
            HwQuickStatusBarHeader.this.mQsPanel.showEdit(HwQuickStatusBarHeader.this.mEditButton);
        }
    }

    public HwQuickStatusBarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mQSHeader = findViewById(R.id.quick_qs_header);
        this.mExpandIndicator = (ExpandableIndicator) findViewById(R.id.expand_indicator);
        this.mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        this.mSettingsButton.setOnClickListener(this);
        this.mEditButton = findViewById(R.id.edit_button);
        this.mEditButton.setOnClickListener(new -void_onFinishInflate__LambdaImpl0());
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) this.mMultiUserSwitch.findViewById(R.id.multi_user_avatar);
        this.mClockView = findViewById(R.id.status_bar_header_clock);
        this.mDateView = findViewById(R.id.status_bar_header_date);
        this.mClockView.setOnClickListener(this);
        this.mDateView.setOnClickListener(this);
        updateResources();
    }

    /* synthetic */ void -com_android_systemui_statusbar_phone_HwQuickStatusBarHeader_lambda$1(View view) {
        this.mHost.startRunnableDismissingKeyguard(new ShowEditRunner());
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
        LayoutParams lp = (LayoutParams) this.mQSHeader.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        this.mQSHeader.setLayoutParams(lp);
        updateDateTimePosition();
        updateSettingsAnimator();
    }

    protected void updateSettingsAnimator() {
    }

    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
        updateEverything();
    }

    public void onNextAlarmChanged(AlarmClockInfo nextAlarm) {
    }

    public void setExpansion(float headerExpansionFraction) {
        updateAlarmVisibilities();
        this.mExpandIndicator.setExpanded(headerExpansionFraction > 0.93f);
    }

    protected void onDetachedFromWindow() {
        setListening(false);
        this.mHost.getUserInfoController().remListener(this);
        this.mHost.getNetworkController().removeEmergencyListener(this);
        super.onDetachedFromWindow();
    }

    private void updateAlarmVisibilities() {
    }

    private void updateDateTimePosition() {
    }

    public void setListening(boolean listening) {
        if (listening != this.mListening) {
            this.mListening = listening;
            updateListeners();
        }
    }

    public void updateEverything() {
        updateDateTimePosition();
        updateVisibilities();
    }

    protected void updateVisibilities() {
        updateAlarmVisibilities();
        refreshUserView();
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
        if (qsPanel != null) {
            this.mQsPanel = qsPanel;
            setupHost(this.mQsPanel.getHost());
            this.mMultiUserSwitch.setQsPanel(this.mQsPanel);
        }
    }

    public void setupHost(QSTileHost host) {
        this.mHost = host;
        host.setHeaderView(this.mExpandIndicator);
        setUserInfoController(host.getUserInfoController());
        setBatteryController(host.getBatteryController());
        setNextAlarmController(host.getNextAlarmController());
        if (this.mHost.getNetworkController().hasVoiceCallingFeature()) {
            this.mHost.getNetworkController().addEmergencyListener(this);
        }
    }

    public void onClick(View v) {
        if (v == this.mSettingsButton) {
            if (this.mSettingsButton.isTunerClick()) {
                if (TunerService.isTunerEnabled(getContext())) {
                    TunerService.setTunerEnabled(getContext(), false);
                }
                HwLog.w("QuickStatusBarHeader", "SettingsButton::onClick: always cancel the tuner function!");
                return;
            }
            HwLog.i("QuickStatusBarHeader", "SettingsButton::onClick: startSettingsActivity");
            startSettingsActivity();
        } else if (v == this.mClockView) {
            startDeskClockActivity();
        } else if (v == this.mDateView) {
            startCalendarActivity();
        }
    }

    private void startDeskClockActivity() {
        if (!SystemProperties.getBoolean("sys.super_power_save", false)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.deskclock", "com.android.deskclock.AlarmsMainActivity");
            try {
                this.mActivityStarter.startActivity(intent, true);
            } catch (ActivityNotFoundException e) {
                HwLog.e("QuickStatusBarHeader", "startDeskClockActivity:: activity not found, " + e);
            }
        }
    }

    private void startCalendarActivity() {
        if (!SystemProperties.getBoolean("sys.super_power_save", false)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.calendar", "com.android.calendar.AllInOneActivity");
            try {
                this.mActivityStarter.startActivity(intent, true);
            } catch (ActivityNotFoundException e) {
                HwLog.e("QuickStatusBarHeader", "startCalendarActivity:: activity not found, " + e);
            }
        }
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

    public void setCallback(Callback qsPanelCallback) {
    }

    public void onModeChanged(boolean isSuperpowerMode) {
        int i;
        int i2 = 8;
        this.mIsSuperPowerModeOn = isSuperpowerMode;
        SettingsButton settingsButton = this.mSettingsButton;
        if (isSuperpowerMode) {
            i = 8;
        } else {
            i = 0;
        }
        settingsButton.setVisibility(i);
        View view = this.mEditButton;
        if (isSuperpowerMode) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        ExpandableIndicator expandableIndicator = this.mExpandIndicator;
        if (!isSuperpowerMode) {
            i2 = 0;
        }
        expandableIndicator.setVisibility(i2);
        refreshUserView();
    }

    public void refreshUserView() {
        MultiUserSwitch multiUserSwitch = this.mMultiUserSwitch;
        int i = (this.mIsSuperPowerModeOn || !this.mMultiUserSwitch.hasMultipleUsers() || RemoteLockUtils.isDeviceRemoteLocked(getContext())) ? 8 : 0;
        multiUserSwitch.setVisibility(i);
    }

    public void onDetailsAnimateStarted() {
        setVisibility(4);
    }
}
