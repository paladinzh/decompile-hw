package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.systemui.R;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.BluetoothController.Callback;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.CastController.CastDevice;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DataSaverController.Listener;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.UserSwitchUtils;

public abstract class PhoneStatusBarPolicy implements Callback, RotationLockControllerCallback, Listener, HwPhoneStatusBarPolicyItf {
    private static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    private final AlarmManager mAlarmManager;
    private BluetoothController mBluetooth;
    private final CastController mCast;
    private final CastController.Callback mCastCallback = new CastController.Callback() {
        public void onCastDevicesChanged() {
            PhoneStatusBarPolicy.this.updateCast();
        }
    };
    protected final Context mContext;
    private boolean mCurrentUserSetup;
    private final DataSaverController mDataSaver;
    private final Handler mHandler = new Handler();
    protected boolean mHasMic;
    private final HotspotController mHotspot;
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() {
        public void onHotspotChanged(boolean enabled) {
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotHotspot, enabled);
        }
    };
    protected final StatusBarIconController mIconController;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SecurityCodeCheck.isValidIntentAndAction(intent)) {
                String action = intent.getAction();
                if (action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
                    PhoneStatusBarPolicy.this.updateAlarm();
                } else if (action.equals("android.media.RINGER_MODE_CHANGED") || action.equals("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION")) {
                    PhoneStatusBarPolicy.this.updateVolumeZen();
                } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                    PhoneStatusBarPolicy.this.updateSimState(intent);
                } else if (action.equals("android.telecom.action.CURRENT_TTY_MODE_CHANGED")) {
                    PhoneStatusBarPolicy.this.updateTTY(intent);
                } else if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                    PhoneStatusBarPolicy.this.updateQuietState();
                    PhoneStatusBarPolicy.this.updateManagedProfile();
                } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                    PhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
                }
            }
        }
    };
    private boolean mManagedProfileFocused = false;
    private boolean mManagedProfileIconVisible = false;
    private boolean mManagedProfileInQuietMode = false;
    private Runnable mRemoveCastIconRunnable = new Runnable() {
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotCast, false);
        }
    };
    private final RotationLockController mRotationLockController;
    State mSimState = State.READY;
    protected final String mSlotAlarmClock;
    private final String mSlotBluetooth;
    private final String mSlotCast;
    private final String mSlotDataSaver;
    protected final String mSlotHeadset;
    private final String mSlotHotspot;
    private final String mSlotManagedProfile;
    private final String mSlotRotate;
    private final String mSlotTty;
    protected final String mSlotVolume;
    private final String mSlotZen;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final UserInfoController mUserInfoController;
    private final UserManager mUserManager;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new SynchronousUserSwitchObserver() {
        public void onUserSwitching(int newUserId) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
                }
            });
        }

        public void onUserSwitchComplete(final int newUserId) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneStatusBarPolicy.this.updateAlarm();
                    PhoneStatusBarPolicy.this.profileChanged(newUserId);
                    PhoneStatusBarPolicy.this.updateQuietState();
                    PhoneStatusBarPolicy.this.updateManagedProfile();
                }
            });
        }

        public void onForegroundProfileSwitch(final int newProfileId) {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                public void run() {
                    PhoneStatusBarPolicy.this.profileChanged(newProfileId);
                }
            });
        }
    };
    protected boolean mVolumeVisible;
    private int mZen;
    private boolean mZenVisible;

    public PhoneStatusBarPolicy(Context context, StatusBarIconController iconController, CastController cast, HotspotController hotspot, UserInfoController userInfoController, BluetoothController bluetooth, RotationLockController rotationLockController, DataSaverController dataSaver) {
        this.mContext = context;
        this.mIconController = iconController;
        this.mCast = cast;
        this.mHotspot = hotspot;
        this.mBluetooth = bluetooth;
        this.mBluetooth.addStateChangedCallback(this);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mUserInfoController = userInfoController;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mRotationLockController = rotationLockController;
        this.mDataSaver = dataSaver;
        this.mSlotCast = context.getString(17039392);
        this.mSlotHotspot = context.getString(17039393);
        this.mSlotBluetooth = context.getString(17039395);
        this.mSlotTty = context.getString(17039397);
        this.mSlotZen = context.getString(17039399);
        this.mSlotVolume = context.getString(17039401);
        this.mSlotAlarmClock = context.getString(17039408);
        this.mSlotManagedProfile = context.getString(17039388);
        this.mSlotRotate = context.getString(17039385);
        this.mSlotHeadset = context.getString(17039386);
        this.mSlotDataSaver = context.getString(17039387);
        this.mRotationLockController.addRotationLockControllerCallback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        filter.addAction("android.media.RINGER_MODE_CHANGED");
        filter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        filter.addAction("android.intent.action.HEADSET_PLUG");
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        filter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter, null, this.mHandler);
        try {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(this.mUserSwitchListener);
        } catch (RemoteException e) {
        }
        this.mIconController.setIcon(this.mSlotTty, R.drawable.stat_sys_tty_mode, null);
        this.mIconController.setIconVisibility(this.mSlotTty, false);
        updateBluetooth();
        this.mIconController.setIcon(this.mSlotAlarmClock, R.drawable.stat_sys_alarm, this.mContext.getString(R.string.accessibility_quick_settings_alarm_set));
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, R.drawable.stat_sys_zen_important, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, R.drawable.stat_sys_ringer_vibrate, null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolumeZen();
        this.mIconController.setIcon(this.mSlotCast, R.drawable.stat_sys_cast, null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mCast.addCallback(this.mCastCallback);
        this.mIconController.setIcon(this.mSlotHotspot, R.drawable.stat_sys_hotspot, this.mContext.getString(R.string.accessibility_status_bar_hotspot));
        this.mIconController.setIconVisibility(this.mSlotHotspot, this.mHotspot.isHotspotEnabled());
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mIconController.setIcon(this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, this.mContext.getString(R.string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, R.drawable.stat_sys_data_saver, context.getString(R.string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mDataSaver.addListener(this);
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void setZenMode(int zen) {
        this.mZen = zen;
        updateVolumeZen();
    }

    protected void updateAlarm() {
        int i;
        AlarmClockInfo alarm = this.mAlarmManager.getNextAlarmClock(-2);
        boolean hasAlarm = alarm != null && alarm.getTriggerTime() > 0;
        boolean zenNone = this.mZen == 2;
        StatusBarIconController statusBarIconController = this.mIconController;
        String str = this.mSlotAlarmClock;
        if (zenNone) {
            i = R.drawable.stat_sys_alarm_dim;
        } else {
            i = R.drawable.stat_sys_alarm;
        }
        statusBarIconController.setIcon(str, i, this.mContext.getString(R.string.accessibility_quick_settings_alarm_set));
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, this.mCurrentUserSetup ? hasAlarm : false);
        HwLog.i("PhoneStatusBarPolicy", "updateAlarm:hasAlarm=" + hasAlarm + ", zenNone=" + zenNone + ", mCurrentUserSetup=" + this.mCurrentUserSetup);
    }

    private final void updateSimState(Intent intent) {
        String stateExtra = intent.getStringExtra("ss");
        if ("ABSENT".equals(stateExtra)) {
            this.mSimState = State.ABSENT;
        } else if ("CARD_IO_ERROR".equals(stateExtra)) {
            this.mSimState = State.CARD_IO_ERROR;
        } else if ("READY".equals(stateExtra)) {
            this.mSimState = State.READY;
        } else if ("LOCKED".equals(stateExtra)) {
            String lockedReason = intent.getStringExtra("reason");
            if ("PIN".equals(lockedReason)) {
                this.mSimState = State.PIN_REQUIRED;
            } else if ("PUK".equals(lockedReason)) {
                this.mSimState = State.PUK_REQUIRED;
            } else {
                this.mSimState = State.NETWORK_LOCKED;
            }
        } else {
            this.mSimState = State.UNKNOWN;
        }
    }

    private final void updateVolumeZen() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        boolean zenVisible = false;
        int zenIconId = 0;
        CharSequence zenDescription = null;
        boolean volumeVisible = false;
        int volumeIconId = 0;
        CharSequence volumeDescription = null;
        if (DndTile.isVisible(this.mContext) || DndTile.isCombinedIcon(this.mContext)) {
            zenVisible = this.mZen != 0;
            zenIconId = R.drawable.ic_statusbar_nodistub;
            zenDescription = this.mContext.getString(R.string.quick_settings_dnd_label);
        } else if (this.mZen == 2) {
            zenVisible = true;
            zenIconId = R.drawable.stat_sys_zen_none;
            zenDescription = this.mContext.getString(R.string.interruption_level_none);
        } else if (this.mZen == 1) {
            zenVisible = true;
            zenIconId = R.drawable.stat_sys_zen_important;
            zenDescription = this.mContext.getString(R.string.interruption_level_priority);
        }
        int ringMode = audioManager.getRingerModeInternal();
        if (DndTile.isVisible(this.mContext) && !DndTile.isCombinedIcon(this.mContext) && ringMode == 0) {
            volumeVisible = true;
            volumeIconId = R.drawable.stat_sys_ringer_silent;
            volumeDescription = this.mContext.getString(R.string.accessibility_ringer_silent);
        } else if (!(this.mZen == 2 || this.mZen == 3 || ringMode != 1)) {
            volumeVisible = true;
            volumeIconId = R.drawable.stat_sys_ringer_vibrate;
            volumeDescription = this.mContext.getString(R.string.accessibility_ringer_vibrate);
        }
        if (zenVisible) {
            this.mIconController.setIcon(this.mSlotZen, zenIconId, zenDescription);
        }
        if (zenVisible != this.mZenVisible) {
            this.mIconController.setIconVisibility(this.mSlotZen, zenVisible);
            this.mZenVisible = zenVisible;
        }
        if (volumeVisible) {
            this.mIconController.setIcon(this.mSlotVolume, volumeIconId, volumeDescription);
        }
        if (volumeVisible != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, volumeVisible);
            this.mVolumeVisible = volumeVisible;
        }
        updateAlarm();
        updateVolumnHuawei(ringMode);
    }

    public void onBluetoothDevicesChanged() {
        updateBluetooth();
    }

    public void onBluetoothStateChange(boolean enabled) {
        updateBluetooth();
    }

    protected final void updateBluetooth() {
        int iconId = R.drawable.stat_sys_data_bluetooth;
        String contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_on);
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        if (this.mBluetooth != null) {
            z = this.mBluetooth.isBluetoothEnabled();
            z2 = this.mBluetooth.isBluetoothConnected();
            z3 = this.mBluetooth.isBluetoothTransfering();
            if (z2) {
                iconId = R.drawable.stat_sys_data_bluetooth_connected;
                contentDescription = this.mContext.getString(R.string.accessibility_bluetooth_connected);
            } else if (z3) {
                iconId = R.drawable.stat_sys_data_bluetooth_connected;
            } else {
                Log.i("PhoneStatusBarPolicy", "updateBluetooth:::setBluetoothBatteryEnable false.");
                this.mBluetooth.setBluetoothBatteryEnable(false);
            }
        }
        Log.i("PhoneStatusBarPolicy", "updateBluetooth::bluetoothEnabled:" + z + ", bluetoothConnected=" + z2 + ", bluetoothTransfering=" + z3);
        if (this.mBluetooth != null) {
            iconId = this.mBluetooth.getSuggestBluetoothIcon(iconId);
        }
        this.mIconController.setIcon(this.mSlotBluetooth, iconId, contentDescription);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, z);
    }

    private final void updateTTY(Intent intent) {
        boolean enabled = intent.getIntExtra("android.telecom.intent.extra.CURRENT_TTY_MODE", 0) != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + enabled);
        }
        if (enabled) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, R.drawable.stat_sys_tty_mode, this.mContext.getString(R.string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    private void updateCast() {
        boolean isCasting = false;
        for (CastDevice device : this.mCast.getCastDevices()) {
            if (device.state != 1) {
                if (device.state == 2) {
                }
            }
            isCasting = true;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: isCasting: " + isCasting);
        }
        this.mHandler.removeCallbacks(this.mRemoveCastIconRunnable);
        if (isCasting) {
            this.mIconController.setIcon(this.mSlotCast, R.drawable.stat_sys_cast, this.mContext.getString(R.string.accessibility_casting));
            this.mIconController.setIconVisibility(this.mSlotCast, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon in 3 sec...");
        }
        this.mHandler.postDelayed(this.mRemoveCastIconRunnable, 3000);
    }

    private void updateQuietState() {
        this.mManagedProfileInQuietMode = false;
        for (UserInfo ui : this.mUserManager.getEnabledProfiles(UserSwitchUtils.getCurrentUser())) {
            if (ui.isManagedProfile() && ui.isQuietModeEnabled()) {
                this.mManagedProfileInQuietMode = true;
                return;
            }
        }
    }

    private void profileChanged(int userId) {
        UserInfo user = null;
        if (userId == -2) {
            try {
                user = ActivityManagerNative.getDefault().getCurrentUser();
            } catch (RemoteException e) {
            }
        } else {
            user = this.mUserManager.getUserInfo(userId);
        }
        this.mManagedProfileFocused = user != null ? user.isManagedProfile() : false;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "profileChanged: mManagedProfileFocused: " + this.mManagedProfileFocused);
        }
    }

    private void updateManagedProfile() {
        boolean showIcon;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateManagedProfile: mManagedProfileFocused: " + this.mManagedProfileFocused);
        }
        if (this.mManagedProfileFocused && !this.mStatusBarKeyguardViewManager.isShowing()) {
            showIcon = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, this.mContext.getString(R.string.accessibility_managed_profile));
        } else if (this.mManagedProfileInQuietMode) {
            showIcon = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status_off, this.mContext.getString(R.string.accessibility_managed_profile));
        } else {
            showIcon = false;
        }
        if (this.mManagedProfileIconVisible != showIcon) {
            this.mIconController.setIconVisibility(this.mSlotManagedProfile, showIcon);
            this.mManagedProfileIconVisible = showIcon;
        }
    }

    public void appTransitionStarting(long startTime, long duration) {
        updateManagedProfile();
    }

    public void notifyKeyguardShowingChanged() {
        updateManagedProfile();
    }

    public void setCurrentUserSetup(boolean userSetup) {
        if (this.mCurrentUserSetup != userSetup) {
            this.mCurrentUserSetup = userSetup;
            updateAlarm();
            updateQuietState();
        }
    }

    public void onRotationLockStateChanged(boolean rotationLocked, boolean affordanceVisible) {
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    private void updateHeadsetPlug(Intent intent) {
        boolean z;
        boolean connected = intent.getIntExtra("state", 0) != 0;
        if (intent.getIntExtra("microphone", 0) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasMic = z;
        if (connected && isEarphoneEnable()) {
            int i;
            Context context = this.mContext;
            if (this.mHasMic) {
                i = R.string.accessibility_status_bar_headset;
            } else {
                i = R.string.accessibility_status_bar_headphones;
            }
            String contentDescription = context.getString(i);
            StatusBarIconController statusBarIconController = this.mIconController;
            String str = this.mSlotHeadset;
            if (this.mHasMic) {
                i = R.drawable.ic_headset_mic;
            } else {
                i = R.drawable.ic_headset;
            }
            statusBarIconController.setIcon(str, i, contentDescription);
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, isDataSaving);
    }
}
