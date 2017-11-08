package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.UserSwitchUtils;

public class HwCustPhoneStatusBarPolicyImpl extends HwCustPhoneStatusBarPolicy {
    private static final String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_ARGS";
    private static final int EYE_PROTECT_DISABLED = 0;
    private static final int EYE_PROTECT_ENABLED = 1;
    private static final boolean IS_VOLTE_SHOW_ON = SystemProperties.getBoolean("ro.config.hw_volte_show_on", true);
    private static final String PHONE_EYES_ICON = "eyes_protect";
    private static final String PHONE_EYES_PROTECTION_SWITCH = "eyes_protection_mode";
    private static final String PHONE_FOUND_EXTRA = "4";
    private static final String TAG = HwCustPhoneStatusBarPolicyImpl.class.getSimpleName();
    boolean isSlient;
    boolean isVibrate;
    private AudioManager mAudioManager;
    private Context mContext;
    private TelephonyManager mPhone;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case 1:
                case 2:
                    if (HwCustPhoneStatusBarPolicyImpl.this.mRingtone != null) {
                        HwCustPhoneStatusBarPolicyImpl.this.mRingtone.stop();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private Ringtone mRingtone;
    private int maxVolume;
    private final Uri uri = System.DEFAULT_RINGTONE_URI;

    public HwCustPhoneStatusBarPolicyImpl(PhoneStatusBarPolicy phoneStatusBarPolicy, Context context) {
        super(phoneStatusBarPolicy, context);
        this.mContext = context;
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                HwCustPhoneStatusBarPolicyImpl.this.mRingtone = RingtoneManager.getRingtone(HwCustPhoneStatusBarPolicyImpl.this.mContext, HwCustPhoneStatusBarPolicyImpl.this.uri);
                return true;
            }
        });
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        registerPhoneStateListener(this.mContext);
    }

    public void handleMoreActionCust(Intent intent) {
        if ("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT".equals(intent.getAction())) {
            Log.e("HwCustPhoneStatusBarPolicy", "intent.getExtra=" + intent.getExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS));
            if (PHONE_FOUND_EXTRA.equals(intent.getExtra(EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS))) {
                final Vibrator mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
                mVibrator.vibrate(900000);
                if (this.mRingtone != null) {
                    if (this.mAudioManager.getRingerMode() == 0) {
                        this.mAudioManager.setRingerMode(2);
                        this.isSlient = true;
                    } else if (1 == this.mAudioManager.getRingerMode()) {
                        this.mAudioManager.setRingerMode(2);
                        this.isVibrate = true;
                    }
                    this.maxVolume = this.mAudioManager.getStreamMaxVolume(2);
                    this.mAudioManager.setStreamVolume(2, this.maxVolume, 2);
                    this.mRingtone.setStreamType(2);
                    this.mRingtone.play();
                }
                Builder mDialogBuilder = new Builder(this.mContext);
                mDialogBuilder.setTitle(R.string.phone_found_title);
                mDialogBuilder.setIcon(R.drawable.stat_sys_warning);
                mDialogBuilder.setMessage(R.string.phone_found_message);
                mDialogBuilder.setPositiveButton(R.string.phone_found_positivebutton, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent mIntent = new Intent("com.android.intent.phone.found");
                        mIntent.setPackage("com.android.bluetooth");
                        HwCustPhoneStatusBarPolicyImpl.this.mContext.sendBroadcast(mIntent);
                        mVibrator.cancel();
                        HwCustPhoneStatusBarPolicyImpl.this.mRingtone.stop();
                        if (HwCustPhoneStatusBarPolicyImpl.this.isSlient) {
                            HwCustPhoneStatusBarPolicyImpl.this.mAudioManager.setRingerMode(0);
                            HwCustPhoneStatusBarPolicyImpl.this.isSlient = false;
                        } else if (HwCustPhoneStatusBarPolicyImpl.this.isVibrate) {
                            HwCustPhoneStatusBarPolicyImpl.this.mAudioManager.setRingerMode(1);
                            HwCustPhoneStatusBarPolicyImpl.this.isVibrate = false;
                        }
                    }
                });
                AlertDialog dialog = mDialogBuilder.create();
                dialog.setCancelable(false);
                dialog.getWindow().setType(2003);
                LayoutParams attributes = dialog.getWindow().getAttributes();
                attributes.privateFlags |= 16;
                dialog.show();
            }
        }
    }

    private void registerPhoneStateListener(Context context) {
        this.mPhone = (TelephonyManager) context.getSystemService("phone");
        this.mPhone.listen(this.mPhoneStateListener, 32);
    }

    public boolean isVolteShow() {
        return IS_VOLTE_SHOW_ON;
    }

    public void setEyeProtectIcon(StatusBarManager mService) {
        if (mService != null) {
            mService.setIcon(PHONE_EYES_ICON, R.drawable.ic_eye_on_for_phone, 0, null);
            mService.setIconVisibility(PHONE_EYES_ICON, false);
        }
    }

    public void updateEye(StatusBarManager mService) {
        boolean z = true;
        if (mService != null) {
            int currentUserId = UserSwitchUtils.getCurrentUser();
            int state = System.getIntForUser(this.mContext.getContentResolver(), PHONE_EYES_PROTECTION_SWITCH, 0, currentUserId);
            Log.i(TAG, " Eye_protect_state = " + state + " user " + currentUserId);
            String str = PHONE_EYES_ICON;
            if (1 != state) {
                z = false;
            }
            mService.setIconVisibility(str, z);
        }
    }

    public boolean isEyeShowSupport() {
        return SystemProperties.getInt("ro.config.hw_eyes_protection", 0) != 0;
    }
}
