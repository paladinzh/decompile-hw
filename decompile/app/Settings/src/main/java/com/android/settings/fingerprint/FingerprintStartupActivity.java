package com.android.settings.fingerprint;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ConfirmLockPassword.InternalActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.fingerprint.enrollment.FingerprintCalibrationIntroActivity;
import com.android.settings.fingerprint.enrollment.FingerprintEnrollActivity;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.navigation.NaviUtils;
import com.huawei.cust.HwCustUtils;

public class FingerprintStartupActivity extends FingerprintStartupBaseActivity implements OnClickListener {
    private static final int[] DESC_FUNC_TITILES = new int[]{2131886652, 2131886656, 2131886660, 2131886664};
    private long mChallenge = 0;
    private TextView mEnrollButton;
    private TextView mFuncDesc;
    private TextView mFuncTitle;
    private int mGuideState = 0;
    private HwCustFingerprintStartupActivity mHwCustFingerprintStartupActivity;
    private boolean mInRelaunching = false;
    private boolean mPreEnrolled = false;
    private TextView mSkipButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (Utils.isTablet()) {
                setRequestedOrientation(-1);
            } else {
                setRequestedOrientation(1);
            }
            prepareContentView();
            this.mHwCustFingerprintStartupActivity = (HwCustFingerprintStartupActivity) HwCustUtils.createObj(HwCustFingerprintStartupActivity.class, new Object[]{this});
            updateContentView();
            initState();
            setSystemUiVisibilityChangeListener();
            return;
        }
        Log.d("FingerprintSettingsStartActivity", "Current mode is not startup mode.");
        finish();
    }

    public void onClick(View v) {
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (v == this.mSkipButton) {
                try {
                    if (this.mHwCustFingerprintStartupActivity == null || !this.mHwCustFingerprintStartupActivity.isSupportVoliceWakeUp()) {
                        setResult(0);
                        Log.i("FingerprintSettingsStartActivity", "skip successfully finished!");
                        finish();
                    } else {
                        this.mHwCustFingerprintStartupActivity.startActivityForVoliceWakeUp();
                    }
                } catch (ActivityNotFoundException ex) {
                    Log.e("FingerprintSettingsStartActivity", ex.getMessage());
                }
            } else if (v == this.mEnrollButton) {
                BiometricManager bm = BiometricManager.open(this);
                if (bm.needSensorCalibration()) {
                    Intent caliIntent = new Intent();
                    caliIntent.setClass(this, FingerprintCalibrationIntroActivity.class);
                    startActivityForResult(caliIntent, 202);
                    return;
                } else if (getFingerprintNum() < 5) {
                    this.mChallenge = bm.preEnrollSafe();
                    this.mPreEnrolled = true;
                    Intent intent;
                    if (isPinOrPasswordLock()) {
                        Log.d("FingerprintSettingsStartActivity", "sending CONFIRM_EXISTING_REQUEST...");
                        intent = new Intent();
                        intent.setClassName("com.android.settings", InternalActivity.class.getName());
                        intent.putExtra("is_from_fingerprint", true);
                        intent.putExtra("has_challenge", true);
                        intent.putExtra("challenge", this.mChallenge);
                        intent.putExtra("return_credentials", true);
                        Log.d("FingerprintSettingsStartActivity", "Start for password confirm.");
                        startActivityForResult(intent, 20);
                    } else {
                        Log.d("FingerprintSettingsStartActivity", "sending FALLBACK_REQUEST...");
                        intent = new Intent();
                        intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
                        intent.putExtra("need_fall_back_result", true);
                        intent.putExtra("minimum_quality", 65536);
                        intent.putExtra("hide_disabled_prefs", true);
                        intent.putExtra("has_challenge", true);
                        intent.putExtra("challenge", this.mChallenge);
                        intent.putExtra("is_fp_screen_lock", true);
                        Log.d("FingerprintSettingsStartActivity", "Start for password setting.");
                        startActivityForResult(intent, 10);
                    }
                } else {
                    Toast.makeText(this, String.format(getString(2131627655), new Object[]{Integer.valueOf(5)}), 0).show();
                }
            }
            return;
        }
        Log.d("FingerprintSettingsStartActivity", "Current mode is not startup mode.");
    }

    private void updateContentView() {
        this.mSkipButton = (TextView) findViewById(2131886328);
        if (this.mSkipButton != null) {
            this.mSkipButton.setText(2131624550);
            this.mSkipButton.setVisibility(0);
            this.mSkipButton.setOnClickListener(this);
            this.mSkipButton.setCompoundDrawables(null, null, null, null);
        }
        this.mEnrollButton = (TextView) findViewById(2131886329);
        if (this.mEnrollButton != null) {
            this.mEnrollButton.setText(2131628759);
            this.mEnrollButton.setVisibility(0);
            this.mEnrollButton.setOnClickListener(this);
        }
        this.mFuncTitle = (TextView) findViewById(2131886356);
        if (this.mFuncTitle != null) {
            this.mFuncTitle.setText(2131627867);
        }
        this.mFuncDesc = (TextView) findViewById(2131886638);
        if (this.mFuncDesc != null) {
            this.mFuncDesc.setText(2131627868);
        }
        ScrollView container = (ScrollView) findViewById(2131886639);
        View descView = LayoutInflater.from(this).inflate(2130968811, null);
        if (descView != null) {
            container.addView(descView);
        }
        if (!FingerprintUtils.isPackageInstalled(this, "com.huawei.hidisk")) {
            TextView stBoxTitle = (TextView) findViewById(2131886656);
            if (stBoxTitle != null) {
                stBoxTitle.setVisibility(8);
            }
            TextView stBoxDesc = (TextView) findViewById(2131886657);
            if (stBoxDesc != null) {
                stBoxDesc.setVisibility(8);
            }
        }
        if (!FingerprintUtils.shouldShowAppLock(this, UserHandle.myUserId())) {
            TextView appLockTitle = (TextView) findViewById(2131886660);
            if (appLockTitle != null) {
                appLockTitle.setVisibility(8);
            }
            TextView appLockDesc = (TextView) findViewById(2131886661);
            if (appLockDesc != null) {
                appLockDesc.setVisibility(8);
            }
        }
        if (!(FingerprintUtils.isPackageInstalled(this, "com.huawei.hwid") && FingerprintUtils.isHwidSupported(this))) {
            TextView hwAccountTitle = (TextView) findViewById(2131886664);
            if (hwAccountTitle != null) {
                hwAccountTitle.setVisibility(8);
            }
            TextView hwAccountDesc = (TextView) findViewById(2131886665);
            if (hwAccountDesc != null) {
                hwAccountDesc.setVisibility(8);
            }
        }
        if (!FingerprintUtils.isQuickHwpayOn()) {
            TextView hwpayTitle = (TextView) findViewById(2131886678);
            if (hwpayTitle != null) {
                hwpayTitle.setVisibility(8);
            }
            TextView hwpayDesc = (TextView) findViewById(2131886679);
            if (hwpayDesc != null) {
                hwpayDesc.setVisibility(8);
            }
        }
    }

    private void initState() {
        this.mInRelaunching = false;
        Bundle last = getLastNonConfigurationInstance();
        if (last != null && (last instanceof Bundle)) {
            Bundle bundle = last;
            this.mGuideState = bundle.getInt("guide_state", 0);
            this.mPreEnrolled = bundle.getBoolean("pre_enroll_state", false);
            this.mChallenge = bundle.getLong("pre_enroll_challenge", 0);
            Log.i("FingerprintSettingsStartActivity", "restored from last-non-configuration instance, mGuideState = " + this.mGuideState + ", mPreEnrolled = " + this.mPreEnrolled + ", mChallenge = " + this.mChallenge);
        }
    }

    private boolean isPinOrPasswordLock() {
        boolean z = false;
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId()) >= 131072) {
                z = true;
            }
            return z;
        }
        Log.d("FingerprintSettingsStartActivity", "Current mode is not startup mode.");
        return false;
    }

    private int getFingerprintNum() {
        BiometricManager bm = BiometricManager.open(this);
        return bm == null ? 0 : bm.getEnrolledFpNum();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("FingerprintSettingsStartActivity", "onActivityResult requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (requestCode == 10) {
                Log.d("FingerprintSettingsStartActivity", "FALLBACK_REQUEST returned!");
                if (isPinOrPasswordLock() && resultCode == 1 && data != null) {
                    Log.d("FingerprintSettingsStartActivity", "Password was set successfully!");
                    startEnrollActivity(data);
                }
            } else if (requestCode == 20) {
                Log.d("FingerprintSettingsStartActivity", "CONFIRM_EXISTING_REQUEST returned!");
                if (resultCode == -1) {
                    if (data == null) {
                        Log.w("FingerprintSettingsStartActivity", "Password confirmation OK but no token available.");
                        return;
                    } else {
                        Log.d("FingerprintSettingsStartActivity", "Password was confirmed successfully!");
                        startEnrollActivity(data);
                    }
                }
            } else if (requestCode == 201 || requestCode == 202) {
                Log.d("FingerprintSettingsStartActivity", "RESQUEST_ENROLL_FINGERPRINT returned!");
                if (resultCode == -1) {
                    Log.d("FingerprintSettingsStartActivity", "Fingerprint enrolled successfully!");
                    try {
                        setKeyguardAssociation(true);
                        if (this.mHwCustFingerprintStartupActivity == null || !this.mHwCustFingerprintStartupActivity.isSupportVoliceWakeUp()) {
                            setResult(-1);
                            Log.i("FingerprintSettingsStartActivity", "enroll successfully finished!");
                            finish();
                        } else {
                            this.mHwCustFingerprintStartupActivity.startActivityForVoliceWakeUp();
                        }
                    } catch (ActivityNotFoundException ex) {
                        Log.e("FingerprintSettingsStartActivity", ex.getMessage());
                    }
                }
            } else if (requestCode == 1000) {
                Log.d("FingerprintSettingsStartActivity", "OOBE_SHOW_TOUCH_REQUEST returned!");
                if (resultCode == 2000) {
                    this.mGuideState = 1;
                } else {
                    Log.i("FingerprintSettingsStartActivity", "fingerprint startup finished unexpectedly!");
                    finish();
                }
            }
            return;
        }
        Log.w("FingerprintSettingsStartActivity", "Current mode is not startup mode.");
    }

    private void setKeyguardAssociation(boolean isChecked) {
        BiometricManager bm = BiometricManager.open(this);
        if (bm == null) {
            Log.e("FingerprintSettingsStartActivity", "Unable to initialize the BiometricManager");
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

    protected void onDestroy() {
        super.onDestroy();
        if (this.mInRelaunching) {
            Log.w("FingerprintSettingsStartActivity", "activity is relaunching, do not postEnroll!");
            this.mInRelaunching = false;
            return;
        }
        if (this.mPreEnrolled) {
            BiometricManager bm = BiometricManager.open(this);
            if (bm == null) {
                Log.e("FingerprintSettingsStartActivity", "Failed to create BiometricManager, cancel postEnroll.");
            } else {
                Log.d("FingerprintSettingsStartActivity", "execute postEnroll when destroy activity");
                bm.postEnrollSafe(this.mChallenge);
            }
        }
    }

    public Object onRetainNonConfigurationInstance() {
        this.mInRelaunching = true;
        Bundle bundle = new Bundle();
        bundle.putInt("guide_state", this.mGuideState);
        bundle.putBoolean("pre_enroll_state", this.mPreEnrolled);
        bundle.putLong("pre_enroll_challenge", this.mChallenge);
        return bundle;
    }

    protected void onResume() {
        super.onResume();
        if (this.mGuideState == 0) {
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                this.mGuideState = 1;
            } else {
                startTouchActivity();
            }
        }
        if (!(this.mEnrollButton == null || this.mEnrollButton.isEnabled())) {
            Log.w("FingerprintSettingsStartActivity", "onResume enable enroll button first.");
            this.mEnrollButton.setEnabled(true);
        }
    }

    private void startEnrollActivity(Intent data) {
        byte[] token = data.getByteArrayExtra("hw_auth_token");
        if (token == null) {
            Log.e("FingerprintSettingsStartActivity", "Token is null, failed to start enroll!");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, FingerprintEnrollActivity.class);
        intent.putExtra("hw_auth_token", token);
        startActivityForResult(intent, 201);
    }

    private void startTouchActivity() {
        Intent intent = new Intent();
        intent.setClass(this, FingerprintStartupTouchActivity.class);
        startActivityForResult(intent, 1000);
    }
}
