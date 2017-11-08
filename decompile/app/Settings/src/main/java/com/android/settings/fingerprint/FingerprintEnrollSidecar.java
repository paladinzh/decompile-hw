package com.android.settings.fingerprint;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.EnrollmentCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import com.android.settings.InstrumentedFragment;

public class FingerprintEnrollSidecar extends InstrumentedFragment {
    private boolean mDone;
    private boolean mEnrolling;
    private EnrollmentCallback mEnrollmentCallback = new EnrollmentCallback() {
        public void onEnrollmentProgress(int remaining) {
            boolean z = false;
            if (FingerprintEnrollSidecar.this.mEnrollmentSteps == -1) {
                FingerprintEnrollSidecar.this.mEnrollmentSteps = remaining;
            }
            FingerprintEnrollSidecar.this.mEnrollmentRemaining = remaining;
            FingerprintEnrollSidecar fingerprintEnrollSidecar = FingerprintEnrollSidecar.this;
            if (remaining == 0) {
                z = true;
            }
            fingerprintEnrollSidecar.mDone = z;
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentProgressChange(FingerprintEnrollSidecar.this.mEnrollmentSteps, remaining);
            }
        }

        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentHelp(helpString);
            }
        }

        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            if (FingerprintEnrollSidecar.this.mListener != null) {
                FingerprintEnrollSidecar.this.mListener.onEnrollmentError(errMsgId, errString);
            }
            FingerprintEnrollSidecar.this.mEnrolling = false;
        }
    };
    private CancellationSignal mEnrollmentCancel;
    private int mEnrollmentRemaining = 0;
    private int mEnrollmentSteps = -1;
    private FingerprintManager mFingerprintManager;
    private Handler mHandler = new Handler();
    private Listener mListener;
    private final Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            FingerprintEnrollSidecar.this.cancelEnrollment();
        }
    };
    private byte[] mToken;
    private int mUserId;

    public interface Listener {
        void onEnrollmentError(int i, CharSequence charSequence);

        void onEnrollmentHelp(CharSequence charSequence);

        void onEnrollmentProgressChange(int i, int i2);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mFingerprintManager = (FingerprintManager) activity.getSystemService(FingerprintManager.class);
        this.mToken = activity.getIntent().getByteArrayExtra("hw_auth_token");
        this.mUserId = activity.getIntent().getIntExtra("android.intent.extra.USER_ID", -10000);
        if (this.mToken == null) {
            getActivity().finish();
        }
    }

    public void onStart() {
        super.onStart();
        if (!this.mEnrolling) {
            startEnrollment();
        }
    }

    public void onStop() {
        super.onStop();
        if (!getActivity().isChangingConfigurations()) {
            cancelEnrollment();
        }
    }

    private void startEnrollment() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mEnrollmentSteps = -1;
        this.mEnrollmentCancel = new CancellationSignal();
        if (this.mUserId != -10000) {
            this.mFingerprintManager.setActiveUser(this.mUserId);
        }
        this.mFingerprintManager.enroll(this.mToken, this.mEnrollmentCancel, 0, this.mUserId, this.mEnrollmentCallback);
        this.mEnrolling = true;
    }

    boolean cancelEnrollment() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        if (!this.mEnrolling) {
            return false;
        }
        this.mEnrollmentCancel.cancel();
        this.mEnrolling = false;
        this.mEnrollmentSteps = -1;
        return true;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public int getEnrollmentSteps() {
        return this.mEnrollmentSteps;
    }

    public int getEnrollmentRemaining() {
        return this.mEnrollmentRemaining;
    }

    protected int getMetricsCategory() {
        return 245;
    }
}
