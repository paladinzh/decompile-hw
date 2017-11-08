package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.telephony.HwCarrierConfigManager;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseCallState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.cust.HwCustUtils;
import java.util.Observable;
import java.util.Observer;

public class HwPhoneStatusBarPolicy extends PhoneStatusBarPolicy implements Observer {
    private static final boolean IS_AMR_WB_SHOW_HD = SystemProperties.getBoolean("ro.config.amr_wb_show_hd", false);
    private static final boolean IS_CHANGE_VOLTE_ICON = SystemProperties.getBoolean("ro.config.hw_change_volte_icon", false);
    private static final boolean IS_SHOW_VOWIFI_ICON = SystemProperties.getBoolean("ro.config.hw_vowifi_show_icon", false);
    private boolean mEarphoneEnable = true;
    OnChangeListener mEyeComfortStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwPhoneStatusBarPolicy.this.updateEyeComfortIcon(((Boolean) SystemUIObserver.get(17)).booleanValue());
        }
    };
    private boolean mEyeComfortVisible;
    private boolean mIsCallActived = false;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onPreciseCallStateChanged(final PreciseCallState callState) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                int foregroundCallState;

                public boolean runInThread() {
                    if (callState != null) {
                        this.foregroundCallState = callState.getForegroundCallState();
                    } else {
                        HwLog.i("HwPhoneStatusBarPolicy", "callState is null!");
                    }
                    return true;
                }

                public void runInUI() {
                    boolean z = true;
                    boolean z2 = false;
                    HwPhoneStatusBarPolicy hwPhoneStatusBarPolicy = HwPhoneStatusBarPolicy.this;
                    if (1 != this.foregroundCallState) {
                        z = false;
                    }
                    hwPhoneStatusBarPolicy.mIsCallActived = z;
                    HwLog.i("HwPhoneStatusBarPolicy", "mSpeechCodecWB is " + HwPhoneStatusBarPolicy.this.mSpeechCodecWB + " mIsCallActived is " + HwPhoneStatusBarPolicy.this.mIsCallActived);
                    HwPhoneStatusBarPolicy hwPhoneStatusBarPolicy2 = HwPhoneStatusBarPolicy.this;
                    if (HwPhoneStatusBarPolicy.this.mIsCallActived) {
                        z2 = HwPhoneStatusBarPolicy.this.mSpeechCodecWB;
                    }
                    hwPhoneStatusBarPolicy2.updateUnicomCall(z2);
                }
            });
        }
    };
    private HwCustPhoneStatusBarPolicy mPhoneStatusBarPolicyCust;
    private boolean mSpeechCodecWB = false;
    private BroadcastReceiver mStatusBarIconsReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            if (intent == null || intent.getAction() == null) {
                HwLog.e("HwPhoneStatusBarPolicy", "mStatusBarIconsReceiver::onReceive::intent or action is null!");
                return;
            }
            String action = intent.getAction();
            if ("android.nfc.action.ADAPTER_STATE_CHANGED".equals(action)) {
                HwPhoneStatusBarPolicy.this.updateNfc(intent);
            } else if ("huawei.intent.action.IMS_SERVICE_STATE_CHANGED".equals(action)) {
                String volteState = intent.getStringExtra("state");
                boolean vowifiState = intent.getBooleanExtra("vowifi_state", false);
                Log.i("HwPhoneStatusBarPolicy", "volteState= " + volteState + "; vowifiState= " + vowifiState);
                if (HwPhoneStatusBarPolicy.this.mPhoneStatusBarPolicyCust != null && HwPhoneStatusBarPolicy.this.mPhoneStatusBarPolicyCust.isVolteShow()) {
                    if (vowifiState) {
                        r5 = HwPhoneStatusBarPolicy.this;
                        if (!vowifiState) {
                            z = true;
                        }
                        r5.updateVolte(z);
                    } else {
                        HwPhoneStatusBarPolicy.this.updateVolte("REGISTERED".equals(volteState));
                    }
                }
            } else if (action.equals("android.intent.action.ALARM_CHANGED")) {
                HwPhoneStatusBarPolicy.this.updateAlarm(intent);
            } else if ("huawei.intent.action.IMS_SERVICE_VOWIFI_STATE_CHANGED".equals(action)) {
                String vowifiState2 = intent.getStringExtra("state");
                HwLog.i("HwPhoneStatusBarPolicy", "vowifi state is " + vowifiState2);
                if (HwPhoneStatusBarPolicy.IS_SHOW_VOWIFI_ICON) {
                    HwPhoneStatusBarPolicy.this.updateVoWifi("REGISTERED".equals(vowifiState2));
                } else {
                    HwLog.i("HwPhoneStatusBarPolicy", "do not show vowifi icon");
                }
            } else if ("com.huawei.telephony.SPEECH_CODEC_WB".equals(action)) {
                HwPhoneStatusBarPolicy.this.mSpeechCodecWB = intent.getBooleanExtra("speechCodecWb", false);
                HwLog.i("HwPhoneStatusBarPolicy", "mSpeechCodecWB is " + HwPhoneStatusBarPolicy.this.mSpeechCodecWB + " mIsCallActived is " + HwPhoneStatusBarPolicy.this.mIsCallActived);
                r5 = HwPhoneStatusBarPolicy.this;
                if (HwPhoneStatusBarPolicy.this.mIsCallActived) {
                    z = HwPhoneStatusBarPolicy.this.mSpeechCodecWB;
                }
                r5.updateUnicomCall(z);
            }
            if (HwPhoneStatusBarPolicy.this.mPhoneStatusBarPolicyCust != null) {
                HwPhoneStatusBarPolicy.this.mPhoneStatusBarPolicyCust.handleMoreActionCust(intent);
            }
        }
    };

    public HwPhoneStatusBarPolicy(Context context, StatusBarIconController iconController, CastController cast, HotspotController hotspot, UserInfoController userInfoController, BluetoothController bluetooth, RotationLockController rotationLockController, DataSaverController dataSaver) {
        super(context, iconController, cast, hotspot, userInfoController, bluetooth, rotationLockController, dataSaver);
        init(context);
        TintManager.getInstance().addObserver(this);
    }

    private void init(Context context) {
        boolean z;
        if (1 == System.getIntForUser(context.getContentResolver(), "hwfeature_earphone", 1, UserSwitchUtils.getCurrentUser())) {
            z = true;
        } else {
            z = false;
        }
        this.mEarphoneEnable = z;
        this.mPhoneStatusBarPolicyCust = (HwCustPhoneStatusBarPolicy) HwCustUtils.createObj(HwCustPhoneStatusBarPolicy.class, new Object[]{this, this.mContext});
        IntentFilter voWifiFilter = new IntentFilter();
        voWifiFilter.addAction("huawei.intent.action.IMS_SERVICE_VOWIFI_STATE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mStatusBarIconsReceiver, UserHandle.ALL, voWifiFilter, "com.huawei.ims.permission.GET_IMS_SERVICE_VOWIFI_STATE", null);
        if (IS_AMR_WB_SHOW_HD) {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (telephonyManager != null) {
                telephonyManager.listen(this.mPhoneStateListener, 2048);
            }
        }
        IntentFilter filter = new IntentFilter();
        if (this.mEarphoneEnable) {
            filter.addAction("android.intent.action.HEADSET_PLUG");
        }
        filter.addAction("android.nfc.action.ADAPTER_STATE_CHANGED");
        filter.addAction("huawei.intent.action.IMS_SERVICE_STATE_CHANGED");
        filter.addAction("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
        filter.addAction("android.intent.action.ALARM_CHANGED");
        if (IS_AMR_WB_SHOW_HD) {
            filter.addAction("com.huawei.telephony.SPEECH_CODEC_WB");
        }
        this.mContext.registerReceiverAsUser(this.mStatusBarIconsReceiver, UserHandle.ALL, filter, null, null);
        SystemUIObserver.getObserver(17).addOnChangeListener(this.mEyeComfortStateChangeListener);
        if (IS_AMR_WB_SHOW_HD) {
            initUnicomHDIcon();
        }
        initVolteIcon();
        initEyeComfortIcon();
        initNfcIcon();
    }

    private void initNfcIcon() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            boolean isNfcEnable = false;

            public boolean runInThread() {
                this.isNfcEnable = SystemUiUtil.isNFCEnable(HwPhoneStatusBarPolicy.this.mContext);
                HwLog.i("HwPhoneStatusBarPolicy", "initNfcIcon::nfc=" + this.isNfcEnable);
                return true;
            }

            public void runInUI() {
                HwPhoneStatusBarPolicy.this.mIconController.setIcon("nfc", R.drawable.stat_sys_nfc, HwPhoneStatusBarPolicy.this.mContext.getString(R.string.accessibility_nfc_on));
                HwPhoneStatusBarPolicy.this.mIconController.setIconVisibility("nfc", this.isNfcEnable);
            }
        });
    }

    public void updateHeadsetIcon() {
        StatusBarIcon headSetIcon = this.mIconController.getIcon(this.mIconController.getSlotIndex(this.mSlotHeadset));
        if (headSetIcon != null && headSetIcon.visible) {
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
        }
    }

    protected final void updateNfc(Intent intent) {
        int state = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1);
        boolean isNfcOn = 3 == state;
        HwLog.i("HwPhoneStatusBarPolicy", "updateNfc::state=" + state + ", isNfcOn=" + isNfcOn);
        this.mIconController.setIcon("nfc", R.drawable.stat_sys_nfc, this.mContext.getString(R.string.accessibility_nfc_on));
        this.mIconController.setIconVisibility("nfc", isNfcOn);
    }

    private void updateVoWifi(boolean registered) {
        HwLog.i("HwPhoneStatusBarPolicy", "updateVoWifi: registered=" + registered);
        if (registered) {
            this.mIconController.setIcon("vowifi_call", R.drawable.ic_statusbar_vowifi, this.mContext.getString(R.string.accessibility_statusbar_vowifi_on));
            this.mIconController.setIconVisibility("vowifi_call", true);
            return;
        }
        this.mIconController.setIconVisibility("vowifi_call", false);
    }

    private void updateVolte(boolean registered) {
        HwLog.i("HwPhoneStatusBarPolicy", "updateVolte: registered=" + registered);
        if (!registered) {
            this.mIconController.setIconVisibility("volte_call", false);
        } else if (IS_CHANGE_VOLTE_ICON) {
            this.mIconController.setIcon("volte_call", R.drawable.stat_sys_hd, this.mContext.getString(R.string.accessibility_statusbar_volte_on));
            this.mIconController.setIconVisibility("volte_call", true);
        } else {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                int rule = 1;

                public boolean runInThread() {
                    HwCarrierConfigManager hwCarrierConfigMgr = HwCarrierConfigManager.getDefault();
                    if (!(hwCarrierConfigMgr == null || HwPhoneStatusBarPolicy.this.mContext == null)) {
                        this.rule = hwCarrierConfigMgr.getVolteIconRule(HwPhoneStatusBarPolicy.this.mContext, HwTelephonyManager.getDefault().getDefault4GSlotId(), 48);
                    }
                    return true;
                }

                public void runInUI() {
                    HwLog.i("HwPhoneStatusBarPolicy", "updateVolte: rule=" + this.rule);
                    if (this.rule == 0) {
                        HwPhoneStatusBarPolicy.this.mIconController.setIconVisibility("volte_call", false);
                        return;
                    }
                    int iconRes = R.drawable.ic_statusbar_hd;
                    if (1 == this.rule) {
                        iconRes = R.drawable.ic_statusbar_hd;
                    } else if (2 == this.rule) {
                        iconRes = R.drawable.ic_statusbar_volte;
                    }
                    HwPhoneStatusBarPolicy.this.mIconController.setIcon("volte_call", iconRes, HwPhoneStatusBarPolicy.this.mContext.getString(R.string.accessibility_statusbar_volte_on));
                    HwPhoneStatusBarPolicy.this.mIconController.setIconVisibility("volte_call", true);
                }
            });
        }
    }

    private void initVolteIcon() {
        if (this.mPhoneStatusBarPolicyCust == null || this.mPhoneStatusBarPolicyCust.isVolteShow()) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                boolean isImsRegistered = false;

                public boolean runInThread() {
                    TelephonyManager tm = (TelephonyManager) HwPhoneStatusBarPolicy.this.mContext.getSystemService("phone");
                    if (tm != null) {
                        this.isImsRegistered = tm.isImsRegistered();
                    } else {
                        HwLog.i("HwPhoneStatusBarPolicy", "initVolte::tm is null!");
                    }
                    return true;
                }

                public void runInUI() {
                    HwPhoneStatusBarPolicy.this.updateVolte(this.isImsRegistered);
                }
            });
        } else {
            HwLog.i("HwPhoneStatusBarPolicy", "initVolteIcon: Do not show VolteIcon!");
        }
    }

    public void update(Observable observable, Object data) {
        updateBluetooth();
    }

    public void updateVolumnHuawei(int ringMode) {
        boolean volumeVisible = false;
        int volumeIconId = 0;
        CharSequence volumeDescription = null;
        if (1 == ringMode) {
            volumeVisible = true;
            volumeIconId = R.drawable.stat_sys_ringer_vibrate;
            volumeDescription = this.mContext.getString(R.string.accessibility_ringer_vibrate);
        } else if (ringMode == 0) {
            volumeVisible = true;
            volumeIconId = R.drawable.stat_sys_ringer_silent;
            volumeDescription = this.mContext.getString(R.string.accessibility_ringer_silent);
        }
        if (volumeVisible) {
            this.mIconController.setIcon(this.mSlotVolume, volumeIconId, volumeDescription);
        }
        if (volumeVisible != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, volumeVisible);
            this.mVolumeVisible = volumeVisible;
        }
    }

    private void initEyeComfortIcon() {
        this.mEyeComfortVisible = ((Boolean) SystemUIObserver.get(17)).booleanValue();
        this.mIconController.setIcon("eyes_protect", R.drawable.ic_statusbar_eyecare, this.mContext.getString(R.string.accessibility_eye_comfort_on));
        this.mIconController.setIconVisibility("eyes_protect", this.mEyeComfortVisible);
    }

    private void updateEyeComfortIcon(boolean isOn) {
        if (isOn != this.mEyeComfortVisible) {
            this.mIconController.setIcon("eyes_protect", R.drawable.ic_statusbar_eyecare, this.mContext.getString(R.string.accessibility_eye_comfort_on));
            this.mIconController.setIconVisibility("eyes_protect", isOn);
            this.mEyeComfortVisible = isOn;
        }
    }

    private final void updateAlarm(Intent intent) {
        updateAlarm();
    }

    public boolean isEarphoneEnable() {
        return this.mEarphoneEnable;
    }

    private void initUnicomHDIcon() {
        this.mIconController.setIcon("unicom_call", R.drawable.stat_sys_hdlt, null);
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            boolean isShowHD = false;

            public boolean runInThread() {
                TelephonyManager tm = (TelephonyManager) HwPhoneStatusBarPolicy.this.mContext.getSystemService("phone");
                int amrCode = System.getInt(HwPhoneStatusBarPolicy.this.mContext.getContentResolver(), "wb_show_hd", 1);
                if (tm != null && tm.isOffhook() && amrCode == 2) {
                    this.isShowHD = true;
                }
                return true;
            }

            public void runInUI() {
                HwLog.i("HwPhoneStatusBarPolicy", "isShowHD is " + this.isShowHD);
                HwPhoneStatusBarPolicy.this.updateUnicomCall(this.isShowHD);
            }
        });
    }

    private void updateUnicomCall(boolean isShow) {
        this.mIconController.setIconVisibility("unicom_call", isShow);
    }
}
