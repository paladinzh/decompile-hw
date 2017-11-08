package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.AlertDialog.Builder;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserManager;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.fingerprint.FingerprintUiHelper;
import com.android.settings.fingerprint.FingerprintUiHelper.Callback;

public abstract class ConfirmDeviceCredentialBaseFragment extends OptionsMenuFragment implements Callback {
    private boolean mAllowFpAuthentication;
    protected Button mCancelButton;
    protected int mEffectiveUserId;
    protected TextView mErrorTextView;
    private FingerprintUiHelper mFingerprintHelper;
    protected ImageView mFingerprintIcon;
    protected final Handler mHandler = new Handler();
    protected boolean mIsStrongAuthRequired;
    protected LockPatternUtils mLockPatternUtils;
    private final Runnable mResetErrorRunnable = new Runnable() {
        public void run() {
            ConfirmDeviceCredentialBaseFragment.this.mErrorTextView.setText("");
        }
    };
    protected boolean mReturnCredentials = false;
    protected int mUserId;

    protected abstract void authenticationSucceeded();

    protected abstract int getLastTryErrorMessage();

    protected abstract void onShowError();

    public void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        this.mAllowFpAuthentication = getActivity().getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.allowFpAuthentication", false);
        this.mReturnCredentials = getActivity().getIntent().getBooleanExtra("return_credentials", false);
        this.mUserId = Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras());
        this.mEffectiveUserId = UserManager.get(getActivity()).getCredentialOwnerProfile(this.mUserId);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
        this.mIsStrongAuthRequired = isFingerprintDisallowedByStrongAuth();
        if (!(!this.mAllowFpAuthentication || isFingerprintDisabledByAdmin() || this.mReturnCredentials || this.mIsStrongAuthRequired)) {
            z = true;
        }
        this.mAllowFpAuthentication = z;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mCancelButton = (Button) view.findViewById(2131886400);
        this.mFingerprintIcon = (ImageView) view.findViewById(2131886399);
        this.mFingerprintHelper = new FingerprintUiHelper(this.mFingerprintIcon, (TextView) view.findViewById(2131886398), this, this.mEffectiveUserId);
        this.mCancelButton.setVisibility(getActivity().getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.showCancelButton", false) ? 0 : 8);
        this.mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ConfirmDeviceCredentialBaseFragment.this.getActivity().finish();
            }
        });
        int credentialOwnerUserId = Utils.getCredentialOwnerUserId(getActivity(), Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras()));
        if (Utils.isManagedProfile(UserManager.get(getActivity()), credentialOwnerUserId)) {
            setWorkChallengeBackground(view, credentialOwnerUserId);
        }
    }

    private boolean isFingerprintDisabledByAdmin() {
        if ((((DevicePolicyManager) getActivity().getSystemService("device_policy")).getKeyguardDisabledFeatures(null, this.mEffectiveUserId) & 32) != 0) {
            return true;
        }
        return false;
    }

    private boolean isFingerprintDisallowedByStrongAuth() {
        return (this.mLockPatternUtils.isFingerprintAllowedForUser(this.mEffectiveUserId) && KeyStore.getInstance().state(this.mUserId) == State.UNLOCKED) ? false : true;
    }

    public void onResume() {
        super.onResume();
        if (this.mAllowFpAuthentication) {
            this.mFingerprintHelper.startListening();
        }
        if (isProfileChallenge()) {
            updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mAllowFpAuthentication) {
            this.mFingerprintHelper.stopListening();
        }
    }

    public void onAuthenticated() {
        if (getActivity() != null && getActivity().isResumed()) {
            ((TrustManager) getActivity().getSystemService("trust")).setDeviceLockedForUser(this.mEffectiveUserId, false);
            authenticationSucceeded();
            authenticationSucceeded();
            checkForPendingIntent();
        }
    }

    public void onFingerprintIconVisibilityChanged(boolean visible) {
    }

    public void prepareEnterAnimation() {
    }

    public void startEnterAnimation() {
    }

    protected void checkForPendingIntent() {
        int taskId = getActivity().getIntent().getIntExtra("android.intent.extra.TASK_ID", -1);
        if (taskId != -1) {
            try {
                IActivityManager activityManager = ActivityManagerNative.getDefault();
                ActivityOptions options = ActivityOptions.makeBasic();
                options.setLaunchStackId(-1);
                activityManager.startActivityFromRecents(taskId, options.toBundle());
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        IntentSender intentSender = (IntentSender) getActivity().getIntent().getParcelableExtra("android.intent.extra.INTENT");
        if (intentSender != null) {
            try {
                getActivity().startIntentSenderForResult(intentSender, -1, null, 0, 0, 0);
            } catch (SendIntentException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void setWorkChallengeBackground(View baseView, int userId) {
        View mainContent = getActivity().findViewById(2131887151);
        if (mainContent != null) {
            mainContent.setPadding(0, 0, 0, 0);
        }
        baseView.setBackground(new ColorDrawable(((DevicePolicyManager) getActivity().getSystemService("device_policy")).getOrganizationColorForUser(userId)));
        ImageView imageView = (ImageView) baseView.findViewById(2131886397);
        if (imageView != null) {
            Drawable image = getResources().getDrawable(2130838755);
            image.setColorFilter(getResources().getColor(2131427462), Mode.DARKEN);
            imageView.setImageDrawable(image);
            Point screenSize = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
            imageView.setLayoutParams(new LayoutParams(-1, screenSize.y));
        }
    }

    protected boolean isProfileChallenge() {
        return Utils.isManagedProfile(UserManager.get(getContext()), this.mEffectiveUserId);
    }

    protected void updateErrorMessage(int numAttempts) {
        int maxAttempts = this.mLockPatternUtils.getMaximumFailedPasswordsForWipe(this.mEffectiveUserId);
        if (maxAttempts > 0 && numAttempts > 0) {
            int remainingAttempts = maxAttempts - numAttempts;
            if (remainingAttempts == 1) {
                showDialog(getActivity().getString(2131624775), getActivity().getString(getLastTryErrorMessage()), 17039370, false);
            } else if (remainingAttempts <= 0) {
                showDialog(null, getActivity().getString(2131624779), 2131624780, true);
            }
            if (this.mErrorTextView != null) {
                showError(getActivity().getString(2131624774, new Object[]{Integer.valueOf(numAttempts), Integer.valueOf(maxAttempts)}), 0);
            }
        }
    }

    protected void showError(CharSequence msg, long timeout) {
        this.mErrorTextView.setText(msg);
        onShowError();
        this.mHandler.removeCallbacks(this.mResetErrorRunnable);
        if (timeout != 0) {
            this.mHandler.postDelayed(this.mResetErrorRunnable, timeout);
        }
    }

    private void showDialog(String title, String message, int buttonString, final boolean dismiss) {
        new Builder(getActivity()).setTitle(title).setMessage(message).setPositiveButton(buttonString, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (dismiss) {
                    ConfirmDeviceCredentialBaseFragment.this.getActivity().finish();
                }
            }
        }).create().show();
    }
}
