package com.android.settings.fingerprint;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ConfirmLockPassword.InternalActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.fingerprint.enrollment.FingerprintEnrollActivity;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;

public class FrontFingerprintSettingCheckoutInfo extends Activity implements OnClickListener {
    private long mChallenge = 0;
    private boolean mPreEnrolled = false;
    private Button mSkipButton;
    private Button mTrackFpButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968824);
        SettingsExtUtils.hideActionBarInStartupGuide(this);
        this.mSkipButton = (Button) findViewById(2131886648);
        this.mSkipButton.setOnClickListener(this);
        this.mTrackFpButton = (Button) findViewById(2131886649);
        this.mTrackFpButton.setOnClickListener(this);
        View accountPreference = (View) findViewById(2131886664).getParent();
        if (!(Utils.isCheckAppExist(this, "com.huawei.hwid") && FingerprintUtils.isHwidSupported(this))) {
            accountPreference.setVisibility(8);
        }
        View applock = findViewById(2131886658);
        View hwAccount = findViewById(2131886662);
        if (UserHandle.myUserId() != 0) {
            if (applock != null) {
                applock.setVisibility(8);
            }
            if (hwAccount != null) {
                hwAccount.setVisibility(8);
            }
        }
    }

    public void onClick(View v) {
        if (v == this.mSkipButton) {
            try {
                startActivity(new Intent("com.huawei.hwstartupguide.ACTION_USINGACTIVITY_START"));
                Log.i("FrontFingerprintSettingCheckoutInfo", "skip successfully finished!");
                finish();
            } catch (ActivityNotFoundException ex) {
                Log.e("FrontFingerprintSettingCheckoutInfo", ex.getMessage());
            }
        } else if (v != this.mTrackFpButton) {
        } else {
            if (getFingerprintNum() < 5) {
                this.mChallenge = BiometricManager.open(this).preEnrollSafe();
                this.mPreEnrolled = true;
                Intent intent;
                if (isPinOrPasswordLock()) {
                    Log.d("FrontFingerprintSettingCheckoutInfo", "sending CONFIRM_EXISTING_REQUEST...");
                    intent = new Intent();
                    intent.setClassName("com.android.settings", InternalActivity.class.getName());
                    intent.putExtra("is_from_fingerprint", true);
                    intent.putExtra("has_challenge", true);
                    intent.putExtra("challenge", this.mChallenge);
                    Log.d("FrontFingerprintSettingCheckoutInfo", "Start for password confirm.");
                    startActivityForResult(intent, 20);
                    return;
                }
                Log.d("FrontFingerprintSettingCheckoutInfo", "sending FALLBACK_REQUEST...");
                intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
                intent.putExtra("need_fall_back_result", true);
                intent.putExtra("minimum_quality", 65536);
                intent.putExtra("hide_disabled_prefs", true);
                intent.putExtra("has_challenge", true);
                intent.putExtra("challenge", this.mChallenge);
                intent.putExtra("is_fp_screen_lock", true);
                Log.d("FrontFingerprintSettingCheckoutInfo", "Start for password setting.");
                startActivityForResult(intent, 10);
                return;
            }
            Toast.makeText(this, String.format(getString(2131627655), new Object[]{Integer.valueOf(5)}), 0).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("FrontFingerprintSettingCheckoutInfo", "onActivityResult requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == 10) {
            Log.d("FrontFingerprintSettingCheckoutInfo", "FALLBACK_REQUEST returned!");
            if (isPinOrPasswordLock() && resultCode == 1 && data != null) {
                Log.d("FrontFingerprintSettingCheckoutInfo", "Password was set successfully!");
                startEnrollActivity(data);
            }
        } else if (requestCode == 20) {
            Log.d("FrontFingerprintSettingCheckoutInfo", "CONFIRM_EXISTING_REQUEST returned!");
            if (resultCode == -1) {
                if (data == null) {
                    Log.w("FrontFingerprintSettingCheckoutInfo", "Password confirmation OK but no token available.");
                } else {
                    Log.d("FrontFingerprintSettingCheckoutInfo", "Password was confirmed successfully!");
                    startEnrollActivity(data);
                }
            }
        } else if (requestCode == 201) {
            Log.d("FrontFingerprintSettingCheckoutInfo", "RESQUEST_ENROLL_FINGERPRINT returned!");
            if (resultCode == -1) {
                try {
                    setKeyguardAssociation(true);
                    startActivity(new Intent("com.huawei.hwstartupguide.ACTION_USINGACTIVITY_START"));
                    Log.i("FrontFingerprintSettingCheckoutInfo", "enroll successfully finished!");
                    finish();
                } catch (ActivityNotFoundException ex) {
                    Log.e("FrontFingerprintSettingCheckoutInfo", ex.getMessage());
                }
            }
        }
    }

    private void startEnrollActivity(Intent data) {
        byte[] token = data.getByteArrayExtra("hw_auth_token");
        if (token == null) {
            Log.e("FrontFingerprintSettingCheckoutInfo", "Token is null, failed to start enroll!");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, FingerprintEnrollActivity.class);
        intent.putExtra("hw_auth_token", token);
        startActivityForResult(intent, 201);
    }

    private void setKeyguardAssociation(boolean isChecked) {
        BiometricManager bm = BiometricManager.open(this);
        if (bm == null) {
            Log.e("FrontFingerprintSettingCheckoutInfo", "Unable to initialize the BiometricManager");
            return;
        }
        int i;
        bm.setAssociation(this, "com.android.keyguard", isChecked);
        String str = "fp.wakeup";
        if (isChecked) {
            i = 1;
        } else {
            i = 0;
        }
        bm.setFingerPrintProperty(str, i);
    }

    private int getFingerprintNum() {
        BiometricManager bm = BiometricManager.open(this);
        return bm == null ? 0 : bm.getEnrolledFpNum();
    }

    private boolean isPinOrPasswordLock() {
        return new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) >= 131072;
    }

    protected void onResume() {
        super.onResume();
        if (!(this.mTrackFpButton == null || this.mTrackFpButton.isEnabled())) {
            Log.w("FrontFingerprintSettingCheckoutInfo", "onResume enable enroll button first.");
            this.mTrackFpButton.setEnabled(true);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mPreEnrolled) {
            BiometricManager bm = BiometricManager.open(this);
            if (bm == null) {
                Log.e("FrontFingerprintSettingCheckoutInfo", "Failed to create BiometricManager, cancel postEnroll.");
            } else {
                Log.d("FrontFingerprintSettingCheckoutInfo", "execute postEnroll when destroy activity");
                bm.postEnrollSafe(this.mChallenge);
            }
        }
    }
}
