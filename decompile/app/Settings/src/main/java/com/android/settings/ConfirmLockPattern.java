package com.android.settings;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternChecker.OnCheckCallback;
import com.android.internal.widget.LockPatternChecker.OnVerifyCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.android.settings.TimeKeeperAdapter.TimerCallback;
import com.android.settings.TimeKeeperAdapter.TimerRemainMsg;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class ConfirmLockPattern extends ConfirmDeviceCredentialBaseActivity {

    public static class ConfirmLockPatternFragment extends ConfirmDeviceCredentialBaseFragment {
        private static final /* synthetic */ int[] -com-android-settings-ConfirmLockPattern$StageSwitchesValues = null;
        TimerCallback mCallBack = new TimerCallback() {
            public void onTimeTick(TimerRemainMsg msg) {
                ConfirmLockPatternFragment.this.updateDetailTextView(msg);
            }

            public void onTimeFinish() {
                ConfirmLockPatternFragment.this.updateStage(Stage.NeedToUnlock);
            }
        };
        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                ConfirmLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private OnPatternListener mConfirmExistingLockPatternListener = new OnPatternListener() {
            public void onPatternStart() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCleared() {
                ConfirmLockPatternFragment.this.mLockPatternView.removeCallbacks(ConfirmLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<Cell> list) {
            }

            public void onPatternDetected(List<Cell> pattern) {
                ConfirmLockPatternFragment.this.mLockPatternView.setEnabled(false);
                if (ConfirmLockPatternFragment.this.mPendingLockCheck != null) {
                    ConfirmLockPatternFragment.this.mPendingLockCheck.cancel(false);
                }
                boolean verifyChallenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getBooleanExtra("has_challenge", false);
                Intent intent = new Intent();
                if (!verifyChallenge) {
                    startCheckPattern(pattern, intent);
                } else if (isInternalActivity()) {
                    startVerifyPattern(pattern, intent);
                } else {
                    ConfirmLockPatternFragment.this.onPatternChecked(pattern, false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                }
            }

            private boolean isInternalActivity() {
                return ConfirmLockPatternFragment.this.getActivity() instanceof InternalActivity;
            }

            private void startVerifyPattern(List<Cell> pattern, Intent intent) {
                AsyncTask verifyPattern;
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                int localUserId = ConfirmLockPatternFragment.this.mUserId;
                long challenge = ConfirmLockPatternFragment.this.getActivity().getIntent().getLongExtra("challenge", 0);
                final Intent intent2 = intent;
                final List<Cell> list = pattern;
                OnVerifyCallback onVerifyCallback = new OnVerifyCallback() {
                    public void onVerified(byte[] token, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        boolean matched = false;
                        if (token != null) {
                            matched = true;
                            if (ConfirmLockPatternFragment.this.mReturnCredentials) {
                                intent2.putExtra("hw_auth_token", token);
                            }
                        }
                        ConfirmLockPatternFragment.this.onPatternChecked(list, matched, intent2, timeoutMs, localEffectiveUserId);
                    }
                };
                ConfirmLockPatternFragment confirmLockPatternFragment = ConfirmLockPatternFragment.this;
                if (localEffectiveUserId == localUserId) {
                    verifyPattern = LockPatternChecker.verifyPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, challenge, localUserId, onVerifyCallback);
                } else {
                    verifyPattern = LockPatternChecker.verifyTiedProfileChallenge(ConfirmLockPatternFragment.this.mLockPatternUtils, LockPatternUtils.patternToString(pattern), true, challenge, localUserId, onVerifyCallback);
                }
                confirmLockPatternFragment.mPendingLockCheck = verifyPattern;
            }

            private void startCheckPattern(final List<Cell> pattern, final Intent intent) {
                if (pattern.size() < 4) {
                    ConfirmLockPatternFragment.this.onPatternChecked(pattern, false, intent, 0, ConfirmLockPatternFragment.this.mEffectiveUserId);
                    return;
                }
                final int localEffectiveUserId = ConfirmLockPatternFragment.this.mEffectiveUserId;
                ConfirmLockPatternFragment.this.mPendingLockCheck = LockPatternChecker.checkPattern(ConfirmLockPatternFragment.this.mLockPatternUtils, pattern, localEffectiveUserId, new OnCheckCallback() {
                    public void onChecked(boolean matched, int timeoutMs) {
                        ConfirmLockPatternFragment.this.mPendingLockCheck = null;
                        if (matched && AnonymousClass3.this.isInternalActivity() && ConfirmLockPatternFragment.this.mReturnCredentials) {
                            intent.putExtra("type", 2);
                            intent.putExtra("password", LockPatternUtils.patternToString(pattern));
                        }
                        ConfirmLockPatternFragment.this.onPatternChecked(pattern, matched, intent, timeoutMs, localEffectiveUserId);
                    }
                });
            }
        };
        private CountDownTimer mCountdownTimer;
        private TextView mDeletePrivacyUserTip;
        private TextView mDetailsTextView;
        private int mEffectiveUserId;
        private TextView mErrorTextView;
        private TextView mHeaderTextView;
        private View mLeftSpacerLandscape;
        private LockPatternUtils mLockPatternUtils;
        private LockPatternView mLockPatternView;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private View mRightSpacerLandscape;
        private TimeKeeperAdapter mTimeKeeper;

        private static /* synthetic */ int[] -getcom-android-settings-ConfirmLockPattern$StageSwitchesValues() {
            if (-com-android-settings-ConfirmLockPattern$StageSwitchesValues != null) {
                return -com-android-settings-ConfirmLockPattern$StageSwitchesValues;
            }
            int[] iArr = new int[Stage.values().length];
            try {
                iArr[Stage.LockedOut.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Stage.NeedToUnlock.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Stage.NeedToUnlockWrong.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -com-android-settings-ConfirmLockPattern$StageSwitchesValues = iArr;
            return iArr;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            this.mEffectiveUserId = Utils.getUserIdFromBundle(getActivity(), getActivity().getIntent().getExtras());
            this.mTimeKeeper = new TimeKeeperAdapter(getActivity(), "lock_screen", false);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            boolean z;
            View view = inflater.inflate(2130968686, null);
            this.mHeaderTextView = (TextView) view.findViewById(2131886364);
            this.mLockPatternView = (LockPatternView) view.findViewById(2131886374);
            this.mDetailsTextView = (TextView) view.findViewById(2131886401);
            this.mErrorTextView = (TextView) view.findViewById(2131886398);
            this.mLeftSpacerLandscape = view.findViewById(2131886404);
            this.mRightSpacerLandscape = view.findViewById(2131886405);
            ((LinearLayoutWithDefaultTouchRecepient) view.findViewById(2131886373)).setDefaultTouchRecepient(this.mLockPatternView);
            this.mLockPatternView.setTactileFeedbackEnabled(this.mLockPatternUtils.isTactileFeedbackEnabled());
            LockPatternView lockPatternView = this.mLockPatternView;
            if (this.mLockPatternUtils.isVisiblePatternEnabled(this.mEffectiveUserId)) {
                z = false;
            } else {
                z = true;
            }
            lockPatternView.setInStealthMode(z);
            this.mLockPatternView.setOnPatternListener(this.mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);
            if (savedInstanceState == null && !this.mLockPatternUtils.isLockPatternEnabled(this.mEffectiveUserId)) {
                getActivity().setResult(-1);
                getActivity().finish();
            }
            getActivity().setTitle(getString(2131628342));
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (!TextUtils.isEmpty(intent.getStringExtra("privacy_user_name"))) {
                    this.mDeletePrivacyUserTip = (TextView) view.findViewById(2131886406);
                    this.mDeletePrivacyUserTip.setVisibility(0);
                    this.mDeletePrivacyUserTip.setText(String.format(getString(2131628775, new Object[]{privacyUserName}), new Object[0]));
                    getActivity().setTitle(2131628774);
                }
            }
            return view;
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (this.mCancelButton != null) {
                this.mCancelButton.setVisibility(8);
            }
        }

        public void onSaveInstanceState(Bundle outState) {
        }

        public void onPause() {
            super.onPause();
            if (this.mCountdownTimer != null) {
                this.mCountdownTimer.cancel();
            }
            this.mTimeKeeper.unregisterObserver();
        }

        protected int getMetricsCategory() {
            return 31;
        }

        public void onResume() {
            super.onResume();
            if (this.mTimeKeeper.getRemainingChance() > 0) {
                updateDetailTextView(null);
                this.mLockPatternView.setEnabled(true);
                return;
            }
            this.mLockPatternView.setEnabled(false);
            this.mTimeKeeper.registerObserver(this.mCallBack);
            updateDetailTextView(this.mTimeKeeper.getTimerRemainMsg());
        }

        private void updateDetailTextView(TimerRemainMsg msg) {
            if (this.mDetailsTextView == null) {
                Log.e("ConfirmLockPatternFragment", "mDetailsTextView is null");
                return;
            }
            if (msg == null) {
                this.mDetailsTextView.setText("");
            } else {
                this.mDetailsTextView.setText(getResources().getQuantityString(msg.mStringResourceId, msg.mRemainTime, new Object[]{Integer.valueOf(msg.mRemainTime)}));
            }
        }

        protected void onShowError() {
        }

        public void startEnterAnimation() {
            super.startEnterAnimation();
            this.mLockPatternView.setAlpha(1.0f);
        }

        private void updateStage(Stage stage) {
            switch (-getcom-android-settings-ConfirmLockPattern$StageSwitchesValues()[stage.ordinal()]) {
                case 1:
                    this.mTimeKeeper.registerObserver(this.mCallBack);
                    this.mLockPatternView.clearPattern();
                    this.mLockPatternView.setEnabled(false);
                    break;
                case 2:
                    this.mHeaderTextView.setText("");
                    this.mDetailsTextView.setText("");
                    this.mTimeKeeper.unregisterObserver();
                    this.mErrorTextView.setText("");
                    if (isProfileChallenge()) {
                        updateErrorMessage(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(this.mEffectiveUserId));
                    }
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    this.mLockPatternView.clearPattern();
                    break;
                case 3:
                    this.mErrorTextView.setText(2131625549);
                    this.mDetailsTextView.setText(getString(2131628354));
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    this.mLockPatternView.setEnabled(true);
                    this.mLockPatternView.enableInput();
                    break;
                default:
                    Log.w("ConfirmLockPatternFragment", "updateStage unknown id");
                    break;
            }
            this.mHeaderTextView.announceForAccessibility(this.mHeaderTextView.getText());
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 2000);
        }

        protected void authenticationSucceeded() {
        }

        private void startDisappearAnimation(Intent intent) {
            getActivity().setResult(-1, intent);
            HwCustSplitUtils splitter = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
            if (splitter.reachSplitSize()) {
                splitter.setExitWhenContentGone(false);
            }
            getActivity().finish();
        }

        public void onFingerprintIconVisibilityChanged(boolean visible) {
            int i = 8;
            if (this.mLeftSpacerLandscape != null && this.mRightSpacerLandscape != null) {
                int i2;
                View view = this.mLeftSpacerLandscape;
                if (visible) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                view.setVisibility(i2);
                View view2 = this.mRightSpacerLandscape;
                if (!visible) {
                    i = 0;
                }
                view2.setVisibility(i);
            }
        }

        private void onPatternChecked(List<Cell> list, boolean matched, Intent intent, int timeoutMs, int effectiveUserId) {
            this.mLockPatternView.setEnabled(true);
            if (matched) {
                this.mTimeKeeper.resetErrorCount(getActivity());
                startDisappearAnimation(intent);
                checkForPendingIntent();
            } else if (this.mTimeKeeper.addErrorCount() <= 0) {
                updateStage(Stage.LockedOut);
            } else {
                updateStage(Stage.NeedToUnlockWrong);
                postClearPatternRunnable();
            }
        }

        protected int getLastTryErrorMessage() {
            return 2131624776;
        }
    }

    public static class InternalActivity extends ConfirmLockPattern {
    }

    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", ConfirmLockPatternFragment.class.getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ConfirmLockPatternFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }
}
