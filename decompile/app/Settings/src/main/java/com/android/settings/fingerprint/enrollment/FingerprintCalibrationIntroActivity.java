package com.android.settings.fingerprint.enrollment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SettingsExtUtils;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.navigation.LazyLoadingAnimationContainer;
import com.android.settings.navigation.NaviUtils;

public class FingerprintCalibrationIntroActivity extends Activity implements OnClickListener {
    private ImageView mAnimBgImage;
    private ImageView mAnimImage;
    private BiometricManager mBiometricManager;
    private LazyLoadingAnimationContainer mCaliTipAnime;
    private long mChallenge;
    private Bundle mExtraBundle;
    private boolean mHasChallenge = false;
    private LockPatternUtils mLockPatternUtils;
    private Button mStartButton;
    private StatusBarManager mStatusBarManager;
    private byte[] mToken;
    private int mTransferRequestCode = -1;
    private int mUserId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void onResume() {
        super.onResume();
        update();
    }

    protected void onPause() {
        super.onPause();
        pause();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    private void init() {
        prepare();
        initView();
        initAnimation();
    }

    private void update() {
        if (!(this.mCaliTipAnime == null || this.mCaliTipAnime.isRunning())) {
            this.mCaliTipAnime.start();
        }
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (this.mStatusBarManager == null) {
                this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
            }
            this.mStatusBarManager.disable(16777216 | 2097152);
        }
    }

    private void pause() {
        if (this.mCaliTipAnime != null && this.mCaliTipAnime.isRunning()) {
            this.mCaliTipAnime.stop();
        }
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (this.mStatusBarManager == null) {
                this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
            }
            this.mStatusBarManager.disable(0);
        }
    }

    private void prepare() {
        Intent intent = getIntent();
        this.mExtraBundle = intent.getBundleExtra("fp_fragment_bundle");
        this.mTransferRequestCode = intent.getIntExtra("request_code", -1);
        this.mUserId = intent.getIntExtra("android.intent.extra.USER", UserHandle.myUserId());
        this.mToken = intent.getByteArrayExtra("hw_auth_token");
        this.mLockPatternUtils = new LockPatternUtils(this);
        this.mBiometricManager = BiometricManager.open(this);
    }

    private void initView() {
        setContentView(2130968781);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(2131628952);
        }
        this.mAnimBgImage = (ImageView) findViewById(2131886591);
        if (this.mAnimBgImage != null) {
            this.mAnimBgImage.setImageResource(2130838164);
        }
        this.mAnimImage = (ImageView) findViewById(2131886592);
        this.mStartButton = (Button) findViewById(2131886589);
        if (this.mStartButton != null) {
            this.mStartButton.setOnClickListener(this);
        }
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mStartButton.getLayoutParams().width = Math.max(this.mStartButton.getLayoutParams().width, displayMetrics.widthPixels / 3);
    }

    private void initAnimation() {
        this.mCaliTipAnime = new LazyLoadingAnimationContainer(this.mAnimImage);
        this.mCaliTipAnime.setTag("CaliIntro");
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mCaliTipAnime.addAllFrames(EnrollAnimeRes.CALIBRATION_FRONT_TIP_RES, 1000);
        }
        this.mCaliTipAnime.start();
    }

    public void onClick(View view) {
        if (view == this.mStartButton) {
            Intent intent = new Intent();
            intent.setClass(this, FingerprintCalibrationActivity.class);
            startActivityForResult(intent, 3000);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    private void destroy() {
        if (this.mHasChallenge) {
            this.mBiometricManager.postEnrollSafe(this.mChallenge);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleActivityResult(requestCode, resultCode, data);
    }

    private void handleActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("FingerprintCalibrationIntroActivity", "handleActivityResult requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == 3000) {
            if (resultCode == 4000) {
                if (this.mToken == null) {
                    this.mChallenge = this.mBiometricManager.preEnrollSafe();
                    this.mHasChallenge = true;
                    if (isPasswordQualified()) {
                        FingerprintUtils.startConfirmPasswordForResult(this, this.mChallenge, this.mUserId);
                    } else {
                        FingerprintUtils.startChoosePasswordForResult(this, this.mChallenge, this.mUserId);
                    }
                } else {
                    startEnroll(3002);
                }
            }
        } else if (requestCode == 3001 || requestCode == 3003) {
            if (resultCode == 0 || data == null) {
                Log.i("FingerprintCalibrationIntroActivity", "confirm existing password, no data included in result!");
                finish();
                return;
            }
            Log.i("FingerprintCalibrationIntroActivity", "confirm existing password get Token");
            this.mToken = data.getByteArrayExtra("hw_auth_token");
            if (this.mToken == null) {
                Log.e("FingerprintCalibrationIntroActivity", "CONFIRM_EXISTING_REQUEST token is null!");
            }
            startEnroll(3002);
        } else if (requestCode == 3002) {
            Log.i("FingerprintCalibrationIntroActivity", "REQ_CALIBRATION_START_ENROLL result handled");
            setResult(resultCode, data);
            finish();
        }
    }

    private void startEnroll(int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, FingerprintEnrollActivity.class);
        intent.putExtra("request_code", this.mTransferRequestCode);
        intent.putExtra("android.intent.extra.USER", this.mUserId);
        if (this.mToken != null) {
            intent.putExtra("hw_auth_token", this.mToken);
        } else {
            Log.e("FingerprintCalibrationIntroActivity", "Challenge token should always exist when Enrollment starts.");
        }
        startActivityForResult(intent, requestCode);
    }

    private boolean isPasswordQualified() {
        return this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mUserId) >= 131072;
    }
}
