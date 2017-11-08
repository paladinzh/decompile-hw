package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.fingerprint.FingerprintEnrollSidecar.Listener;

public class FingerprintEnrollFindSensor extends FingerprintEnrollBase {
    private FingerprintFindSensorAnimation mAnimation;
    private boolean mLaunchedConfirmLock;
    private boolean mNextClicked;
    private FingerprintEnrollSidecar mSidecar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        setHeaderText(2131624658);
        if (savedInstanceState != null) {
            this.mLaunchedConfirmLock = savedInstanceState.getBoolean("launched_confirm_lock");
            this.mToken = savedInstanceState.getByteArray("hw_auth_token");
        }
        if (this.mToken == null && !this.mLaunchedConfirmLock) {
            launchConfirmLock();
        } else if (this.mToken != null) {
            startLookingForFingerprint();
        }
        this.mAnimation = (FingerprintFindSensorAnimation) findViewById(2131886623);
    }

    protected int getContentView() {
        return 2130968788;
    }

    protected void onStart() {
        super.onStart();
        this.mAnimation.startAnimation();
    }

    private void startLookingForFingerprint() {
        this.mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag("sidecar");
        if (this.mSidecar == null) {
            this.mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction().add(this.mSidecar, "sidecar").commit();
        }
        this.mSidecar.setListener(new Listener() {
            public void onEnrollmentProgressChange(int steps, int remaining) {
                FingerprintEnrollFindSensor.this.mNextClicked = true;
                if (!FingerprintEnrollFindSensor.this.mSidecar.cancelEnrollment()) {
                    FingerprintEnrollFindSensor.this.proceedToEnrolling();
                }
            }

            public void onEnrollmentHelp(CharSequence helpString) {
            }

            public void onEnrollmentError(int errMsgId, CharSequence errString) {
                if (FingerprintEnrollFindSensor.this.mNextClicked && errMsgId == 5) {
                    FingerprintEnrollFindSensor.this.mNextClicked = false;
                    FingerprintEnrollFindSensor.this.proceedToEnrolling();
                }
            }
        });
    }

    protected void onStop() {
        super.onStop();
        this.mAnimation.pauseAnimation();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mAnimation.stopAnimation();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("launched_confirm_lock", this.mLaunchedConfirmLock);
        outState.putByteArray("hw_auth_token", this.mToken);
    }

    protected void onNextButtonClick() {
        this.mNextClicked = true;
        if (this.mSidecar == null || !(this.mSidecar == null || this.mSidecar.cancelEnrollment())) {
            proceedToEnrolling();
        }
    }

    private void proceedToEnrolling() {
        getFragmentManager().beginTransaction().remove(this.mSidecar).commit();
        this.mSidecar = null;
        startActivityForResult(getEnrollingIntent(), 2);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                this.mToken = data.getByteArrayExtra("hw_auth_token");
                overridePendingTransition(2131034139, 2131034140);
                getIntent().putExtra("hw_auth_token", this.mToken);
                startLookingForFingerprint();
                return;
            }
            finish();
        } else if (requestCode != 2) {
            super.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == 1) {
            setResult(1);
            finish();
        } else if (resultCode == 2) {
            setResult(2);
            finish();
        } else if (resultCode == 3) {
            setResult(3);
            finish();
        } else if (((FingerprintManager) getSystemService(FingerprintManager.class)).getEnrolledFingerprints().size() >= getResources().getInteger(17694880)) {
            finish();
        } else {
            startLookingForFingerprint();
        }
    }

    private void launchConfirmLock() {
        boolean launchedConfirmationActivity;
        long challenge = ((FingerprintManager) getSystemService(FingerprintManager.class)).preEnroll();
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
        if (this.mUserId == -10000) {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, getString(2131624642), null, null, challenge);
        } else {
            launchedConfirmationActivity = helper.launchConfirmationActivity(1, getString(2131624642), null, null, challenge, this.mUserId);
        }
        if (launchedConfirmationActivity) {
            this.mLaunchedConfirmLock = true;
        } else {
            finish();
        }
    }

    protected int getMetricsCategory() {
        return 241;
    }
}
