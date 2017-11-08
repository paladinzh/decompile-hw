package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserManager;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternChecker.OnCheckCallback;
import com.android.internal.widget.LockPatternChecker.OnVerifyCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.fingerprint.FingerprintUiHelper;
import com.android.settings.fingerprint.FingerprintUiHelper.Callback;

public class ConfirmLockPassword extends ConfirmDeviceCredentialBaseActivity {
    private static final int[] DETAIL_TEXTS = new int[]{2131625535, 2131625536, 2131625538, 2131625539, 2131625541, 2131625542, 2131625544, 2131625545};

    public static class ConfirmLockPasswordFragment extends ConfirmPasswordFragmentBase implements Callback {
        private boolean mAllowFpAuthentication;
        private TextView mDeletePrivacyUserTip;
        private TextView mDetailsTextView;
        private int mEffectiveUserId;
        private FingerprintUiHelper mFingerprintHelper;
        private ImageView mFingerprintIcon;
        private Handler mHandler = new Handler();
        private TextView mHeaderTextView;
        private boolean mIsStrongAuthRequired;
        private LockPatternUtils mLockPatternUtils;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        protected boolean mReturnCredentials = false;
        private int mStoredQuality;
        protected int mUserId;
        private boolean mUsingFingerprint = false;

        protected boolean isAlphaMode() {
            if (262144 == this.mStoredQuality || 327680 == this.mStoredQuality || 393216 == this.mStoredQuality) {
                return true;
            }
            return false;
        }

        protected boolean needRestore() {
            return false;
        }

        protected int getTitle() {
            return 2131628874;
        }

        public void onCreate(Bundle savedInstanceState) {
            boolean z = false;
            super.onCreate(savedInstanceState);
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            this.mAllowFpAuthentication = getActivity().getIntent().getBooleanExtra("com.android.settings.ConfirmCredentials.allowFpAuthentication", false);
            Intent intent = getActivity().getIntent();
            this.mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
            this.mEffectiveUserId = UserManager.get(getActivity()).getCredentialOwnerProfile(this.mUserId);
            this.mStoredQuality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mEffectiveUserId);
            this.mReturnCredentials = intent.getBooleanExtra("return_credentials", false);
            this.mIsStrongAuthRequired = isFingerprintDisallowedByStrongAuth();
            if (!(!this.mAllowFpAuthentication || isFingerprintDisabledByAdmin(this.mEffectiveUserId) || this.mReturnCredentials)) {
                z = true;
            }
            this.mAllowFpAuthentication = z;
        }

        private boolean isFingerprintDisabledByAdmin(int userId) {
            if ((((DevicePolicyManager) getActivity().getSystemService("device_policy")).getKeyguardDisabledFeatures(null, userId) & 32) != 0) {
                return true;
            }
            return false;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            this.mFingerprintIcon = (ImageView) view.findViewById(2131886399);
            if (!SettingsExtUtils.isStartupGuideMode(getActivity().getContentResolver())) {
                this.mFingerprintHelper = new FingerprintUiHelper(this.mFingerprintIcon, (TextView) view.findViewById(2131886398), this, this.mEffectiveUserId);
            }
            this.mHeaderTextView = (TextView) view.findViewById(2131886364);
            this.mDetailsTextView = (TextView) view.findViewById(2131886401);
            this.mIsAlpha = isAlphaMode();
            getActivity().setTitle(getTitle());
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                CharSequence headerMessage = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.header");
                CharSequence detailsMessage = intent.getCharSequenceExtra("com.android.settings.ConfirmCredentials.details");
                if (TextUtils.isEmpty(detailsMessage)) {
                    detailsMessage = getString(getDefaultDetails());
                }
                boolean isProfile = Utils.isManagedProfile(UserManager.get(getActivity()), this.mEffectiveUserId);
                if (this.mHeaderTextView != null) {
                    if (!isProfile || TextUtils.isEmpty(headerMessage)) {
                        this.mHeaderTextView.setVisibility(8);
                    } else {
                        this.mHeaderTextView.setText(headerMessage);
                        this.mHeaderTextView.setVisibility(0);
                    }
                }
                if (this.mDetailsTextView != null) {
                    if (!isProfile || TextUtils.isEmpty(detailsMessage)) {
                        this.mDetailsTextView.setVisibility(8);
                    } else {
                        this.mPasswordEntry.setHintTextColor(getActivity().getResources().getColor(2131427430));
                        this.mDetailsTextView.setText(detailsMessage);
                        this.mDetailsTextView.setTextColor(getActivity().getResources().getColor(2131427499));
                        this.mDetailsTextView.setVisibility(0);
                    }
                }
                if (!TextUtils.isEmpty(intent.getStringExtra("privacy_user_name"))) {
                    this.mDeletePrivacyUserTip = (TextView) view.findViewById(2131886406);
                    this.mDeletePrivacyUserTip.setVisibility(0);
                    this.mDeletePrivacyUserTip.setText(String.format(getString(2131628775, new Object[]{privacyUserName}), new Object[0]));
                    getActivity().setTitle(2131628728);
                }
            }
            return view;
        }

        public void onResume() {
            super.onResume();
            if (this.mAllowFpAuthentication && this.mFingerprintHelper != null) {
                this.mFingerprintHelper.startListening();
            }
        }

        public void onPause() {
            super.onPause();
            if (this.mAllowFpAuthentication && this.mFingerprintHelper != null) {
                this.mFingerprintHelper.stopListening();
            }
        }

        public void onAuthenticated() {
            if (getActivity() != null && getActivity().isResumed()) {
                ((TrustManager) getActivity().getSystemService("trust")).setDeviceLockedForUser(this.mEffectiveUserId, false);
                getActivity().setResult(-1, new Intent());
                getActivity().finish();
                checkForPendingIntent();
            }
        }

        private void checkForPendingIntent() {
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

        public void onFingerprintIconVisibilityChanged(boolean visible) {
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int credentialOwnerUserId = Utils.getCredentialOwnerUserId(getActivity(), Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras()));
            if (Utils.isManagedProfile(UserManager.get(getActivity()), credentialOwnerUserId)) {
                setWorkChallengeBackground(view, credentialOwnerUserId);
            }
        }

        private int getDefaultDetails() {
            int i;
            int i2 = 1;
            boolean isProfile = Utils.isManagedProfile(UserManager.get(getActivity()), this.mEffectiveUserId);
            if (this.mIsStrongAuthRequired) {
                i = 1;
            } else {
                i = 0;
            }
            i = ((isProfile ? 1 : 0) << 1) + (i << 2);
            if (!this.mIsAlpha) {
                i2 = 0;
            }
            return ConfirmLockPassword.DETAIL_TEXTS[i + i2];
        }

        protected boolean doCheckPassword(String password) {
            boolean verifyChallenge = getActivity().getIntent().getBooleanExtra("has_challenge", false);
            Intent intent = new Intent();
            if (!verifyChallenge) {
                startCheckPassword(password, intent);
                return true;
            } else if (!isInternalActivity()) {
                return false;
            } else {
                startVerifyPassword(password, intent);
                return true;
            }
        }

        private boolean isInternalActivity() {
            return getActivity() instanceof InternalActivity;
        }

        private void startVerifyPassword(String pin, Intent intent) {
            AsyncTask verifyPassword;
            long challenge = getActivity().getIntent().getLongExtra("challenge", 0);
            int localEffectiveUserId = this.mEffectiveUserId;
            int localUserId = this.mUserId;
            final Intent intent2 = intent;
            OnVerifyCallback onVerifyCallback = new OnVerifyCallback() {
                public void onVerified(byte[] token, int timeoutMs) {
                    if (token == null) {
                        Log.e("ConfirmLockPassword.startVerify", "token is null");
                    }
                    ConfirmLockPasswordFragment.this.mPendingLockCheck = null;
                    boolean matched = false;
                    if (token != null) {
                        matched = true;
                        if (ConfirmLockPasswordFragment.this.mReturnCredentials) {
                            intent2.putExtra("hw_auth_token", token);
                        }
                    }
                    ConfirmLockPasswordFragment.this.onPasswordChecked(matched, intent2);
                }
            };
            if (localEffectiveUserId == localUserId) {
                verifyPassword = LockPatternChecker.verifyPassword(this.mLockPatternUtils, pin, challenge, localUserId, onVerifyCallback);
            } else {
                verifyPassword = LockPatternChecker.verifyTiedProfileChallenge(this.mLockPatternUtils, pin, false, challenge, localUserId, onVerifyCallback);
            }
            this.mPendingLockCheck = verifyPassword;
        }

        private void startCheckPassword(final String pin, final Intent intent) {
            this.mPendingLockCheck = LockPatternChecker.checkPassword(this.mLockPatternUtils, pin, this.mEffectiveUserId, new OnCheckCallback() {
                public void onChecked(boolean matched, int timeoutMs) {
                    ConfirmLockPasswordFragment.this.mPendingLockCheck = null;
                    if (matched && ConfirmLockPasswordFragment.this.isInternalActivity() && ConfirmLockPasswordFragment.this.mReturnCredentials) {
                        int i;
                        Intent intent = intent;
                        String str = "type";
                        if (ConfirmLockPasswordFragment.this.mIsAlpha) {
                            i = 0;
                        } else {
                            i = 3;
                        }
                        intent.putExtra(str, i);
                        intent.putExtra("password", pin);
                    }
                    ConfirmLockPasswordFragment.this.onPasswordChecked(matched, intent);
                    if (matched) {
                        ConfirmLockPasswordFragment.this.checkForPendingIntent();
                    }
                }
            });
        }

        private boolean isFingerprintDisallowedByStrongAuth() {
            return (this.mLockPatternUtils.isFingerprintAllowedForUser(this.mEffectiveUserId) && KeyStore.getInstance().state(this.mUserId) == State.UNLOCKED) ? false : true;
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
    }

    public static class InternalActivity extends ConfirmLockPassword {
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", ConfirmLockPasswordFragment.class.getName());
        return modIntent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
    }

    protected void onStop() {
        super.onStop();
        SettingsExtUtils.checkHideSoftInput(this);
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ConfirmLockPasswordFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
