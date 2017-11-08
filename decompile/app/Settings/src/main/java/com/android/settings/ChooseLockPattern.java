package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView.OnPatternListener;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChooseLockPattern extends SettingsActivity {

    public static class ChooseLockPatternFragment extends InstrumentedFragment implements OnClickListener, Listener {
        private static final /* synthetic */ int[] -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues = null;
        private final List<Cell> mAnimatePattern = Collections.unmodifiableList(Lists.newArrayList(new Cell[]{Cell.of(0, 0), Cell.of(0, 1), Cell.of(1, 1), Cell.of(2, 1)}));
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        protected OnPatternListener mChooseNewLockPatternListener = new OnPatternListener() {
            public void onPatternStart() {
                ChooseLockPatternFragment.this.mLockPatternView.removeCallbacks(ChooseLockPatternFragment.this.mClearPatternRunnable);
                patternInProgress();
            }

            public void onPatternCleared() {
                ChooseLockPatternFragment.this.mLockPatternView.removeCallbacks(ChooseLockPatternFragment.this.mClearPatternRunnable);
            }

            public void onPatternDetected(List<Cell> pattern) {
                if (ChooseLockPatternFragment.this.mUiStage == Stage.NeedToConfirm || ChooseLockPatternFragment.this.mUiStage == Stage.ConfirmWrong) {
                    if (ChooseLockPatternFragment.this.mChosenPattern == null) {
                        throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
                    } else if (ChooseLockPatternFragment.this.mChosenPattern.equals(pattern)) {
                        ChooseLockPatternFragment.this.updateStage(Stage.ChoiceConfirmed);
                    } else {
                        ChooseLockPatternFragment.this.updateStage(Stage.ConfirmWrong);
                    }
                } else if (ChooseLockPatternFragment.this.mUiStage != Stage.Introduction && ChooseLockPatternFragment.this.mUiStage != Stage.ChoiceTooShort) {
                    Log.e("ChooseLockPattern", "Unexpected stage " + ChooseLockPatternFragment.this.mUiStage + " when " + "entering the pattern.");
                } else if (pattern.size() < 4) {
                    ChooseLockPatternFragment.this.updateStage(Stage.ChoiceTooShort);
                } else {
                    ChooseLockPatternFragment.this.mChosenPattern = new ArrayList(pattern);
                    ChooseLockPatternFragment.this.updateStage(Stage.NeedToConfirm);
                }
            }

            public void onPatternCellAdded(List<Cell> list) {
            }

            private void patternInProgress() {
                ChooseLockPatternFragment.this.mHeaderText.setText(2131628356);
                ChooseLockPatternFragment.this.mFooterText.setVisibility(8);
                ChooseLockPatternFragment.this.mFooterLeftButton.setEnabled(false);
                ChooseLockPatternFragment.this.mFooterRightButton.setEnabled(false);
            }
        };
        protected List<Cell> mChosenPattern = null;
        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                ChooseLockPatternFragment.this.mLockPatternView.clearPattern();
            }
        };
        private String mCurrentPattern;
        private Button mDone;
        private TextView mFooterLeftButton;
        private TextView mFooterRightButton;
        protected TextView mFooterText;
        private boolean mHasChallenge;
        protected TextView mHeaderText;
        private boolean mHideDrawer = false;
        boolean mIsForPrivacySpaceMainUser = false;
        boolean mIsForPrivacySpacePrivacyUser = false;
        protected LockPatternView mLockPatternView;
        private PrivacySpaceSettingsHelper mPrivacySpaceSettingsHelper;
        private Button mReDraw;
        private SaveAndFinishWorker mSaveAndFinishWorker;
        private Stage mUiStage = Stage.Introduction;
        private int mUserId;

        enum LeftButtonMode {
            Cancel(2131624572, true),
            CancelDisabled(2131624572, false),
            Retry(2131625562, true),
            RetryDisabled(2131625562, false),
            Gone(-1, false);
            
            final boolean enabled;
            final int text;

            private LeftButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }
        }

        enum RightButtonMode {
            Continue(2131625563, true),
            ContinueDisabled(2131625563, false),
            Confirm(2131625560, true),
            ConfirmDisabled(2131625560, false),
            Ok(17039370, true);
            
            final boolean enabled;
            final int text;

            private RightButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }
        }

        protected enum Stage {
            Introduction(2131628141, LeftButtonMode.Cancel, RightButtonMode.ContinueDisabled, -1, true),
            HelpScreen(2131625574, LeftButtonMode.Gone, RightButtonMode.Ok, -1, false),
            ChoiceTooShort(2131689538, LeftButtonMode.Retry, RightButtonMode.ContinueDisabled, -1, true),
            FirstChoiceValid(2131625557, LeftButtonMode.Retry, RightButtonMode.Continue, -1, false),
            NeedToConfirm(2131628352, LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled, -1, true),
            ConfirmWrong(2131628353, LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled, -1, true),
            ChoiceConfirmed(2131625559, LeftButtonMode.Cancel, RightButtonMode.Confirm, -1, false);
            
            final int footerMessage;
            final int headerMessage;
            final LeftButtonMode leftMode;
            final boolean patternEnabled;
            final RightButtonMode rightMode;

            private Stage(int headerMessage, LeftButtonMode leftMode, RightButtonMode rightMode, int footerMessage, boolean patternEnabled) {
                this.headerMessage = headerMessage;
                this.leftMode = leftMode;
                this.rightMode = rightMode;
                this.footerMessage = footerMessage;
                this.patternEnabled = patternEnabled;
            }
        }

        private static /* synthetic */ int[] -getcom-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues() {
            if (-com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues != null) {
                return -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues;
            }
            int[] iArr = new int[Stage.values().length];
            try {
                iArr[Stage.ChoiceConfirmed.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Stage.ChoiceTooShort.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Stage.ConfirmWrong.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Stage.FirstChoiceValid.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Stage.HelpScreen.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[Stage.Introduction.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[Stage.NeedToConfirm.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues = iArr;
            return iArr;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case 55:
                    if (resultCode != -1) {
                        getActivity().setResult(1);
                        getActivity().finish();
                    } else {
                        this.mCurrentPattern = data.getStringExtra("password");
                    }
                    updateStage(Stage.Introduction);
                    return;
                default:
                    getActivity().setResult(1);
                    getActivity().finish();
                    return;
            }
        }

        protected void setRightButtonEnabled(boolean enabled) {
            this.mFooterRightButton.setEnabled(enabled);
        }

        protected int getMetricsCategory() {
            return 29;
        }

        public void onCreate(Bundle savedInstanceState) {
            boolean z = true;
            super.onCreate(savedInstanceState);
            this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
            if (getActivity() instanceof ChooseLockPattern) {
                Intent intent = getActivity().getIntent();
                this.mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
                if (intent.getBooleanExtra("for_cred_req_boot", false)) {
                    SaveAndFinishWorker w = new SaveAndFinishWorker();
                    boolean required = getActivity().getIntent().getBooleanExtra("extra_require_password", true);
                    String current = intent.getStringExtra("password");
                    w.setBlocking(true);
                    w.setListener(this);
                    w.start(this.mChooseLockSettingsHelper.utils(), required, false, 0, LockPatternUtils.stringToPattern(current), current, this.mUserId);
                }
                this.mHideDrawer = getActivity().getIntent().getBooleanExtra(":settings:hide_drawer", false);
                this.mPrivacySpaceSettingsHelper = new PrivacySpaceSettingsHelper((Fragment) this);
                int privacySpaceUserType = intent.getIntExtra("hidden_space_main_user_type", -1);
                this.mIsForPrivacySpaceMainUser = privacySpaceUserType == 1;
                if (privacySpaceUserType != 4) {
                    z = false;
                }
                this.mIsForPrivacySpacePrivacyUser = z;
                return;
            }
            throw new SecurityException("Fragment contained in wrong activity");
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(2130968673, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            this.mHeaderText = (TextView) view.findViewById(2131886364);
            this.mLockPatternView = (LockPatternView) view.findViewById(2131886374);
            this.mLockPatternView.setOnPatternListener(this.mChooseNewLockPatternListener);
            this.mLockPatternView.setTactileFeedbackEnabled(this.mChooseLockSettingsHelper.utils().isTactileFeedbackEnabled());
            this.mFooterText = (TextView) view.findViewById(2131886369);
            this.mFooterLeftButton = (TextView) view.findViewById(2131886378);
            this.mFooterRightButton = (TextView) view.findViewById(2131886379);
            this.mFooterLeftButton.setOnClickListener(this);
            this.mFooterRightButton.setOnClickListener(this);
            this.mReDraw = (Button) view.findViewById(2131886376);
            this.mReDraw.setOnClickListener(this);
            this.mDone = (Button) view.findViewById(2131886377);
            this.mDone.setOnClickListener(this);
            ((LinearLayoutWithDefaultTouchRecepient) view.findViewById(2131886373)).setDefaultTouchRecepient(this.mLockPatternView);
            boolean confirmCredentials = getActivity().getIntent().getBooleanExtra("confirm_credentials", true);
            Intent intent = getActivity().getIntent();
            this.mCurrentPattern = intent.getStringExtra("password");
            this.mHasChallenge = intent.getBooleanExtra("has_challenge", false);
            this.mChallenge = intent.getLongExtra("challenge", 0);
            if (savedInstanceState != null) {
                String patternString = savedInstanceState.getString("chosenPattern");
                if (patternString != null) {
                    this.mChosenPattern = LockPatternUtils.stringToPattern(patternString);
                }
                if (this.mCurrentPattern == null) {
                    this.mCurrentPattern = savedInstanceState.getString("currentPattern");
                }
                updateStage(Stage.values()[savedInstanceState.getInt("uiStage")]);
                this.mSaveAndFinishWorker = (SaveAndFinishWorker) getFragmentManager().findFragmentByTag("save_and_finish_worker");
            } else if (confirmCredentials) {
                updateStage(Stage.NeedToConfirm);
                if (!this.mChooseLockSettingsHelper.launchConfirmationActivity(55, getString(2131624724), true, this.mUserId)) {
                    updateStage(Stage.Introduction);
                }
            } else {
                updateStage(Stage.Introduction);
            }
            setHasOptionsMenu(true);
            if (this.mIsForPrivacySpaceMainUser) {
                getActivity().setTitle(getString(2131628750));
            } else if (this.mIsForPrivacySpacePrivacyUser) {
                getActivity().setTitle(getString(2131628751));
            }
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        public void onResume() {
            super.onResume();
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(this);
            }
        }

        public void onPause() {
            super.onPause();
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(null);
            }
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            if (16908332 == item.getItemId()) {
                finish();
            }
            return super.onOptionsItemSelected(item);
        }

        public void finish() {
            Activity activity = getActivity();
            if (activity != null) {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    activity.finish();
                }
            }
        }

        public void handleLeftButton() {
            if (this.mUiStage.leftMode == LeftButtonMode.Retry) {
                retry();
            } else if (this.mUiStage.leftMode == LeftButtonMode.Cancel) {
                getActivity().finish();
            } else {
                throw new IllegalStateException("left footer button pressed, but stage of " + this.mUiStage + " doesn't make sense");
            }
        }

        private void retry() {
            this.mChosenPattern = null;
            this.mLockPatternView.clearPattern();
            updateStage(Stage.Introduction);
        }

        public void handleRightButton() {
            if (this.mUiStage.rightMode == RightButtonMode.Continue) {
                if (this.mUiStage != Stage.FirstChoiceValid) {
                    throw new IllegalStateException("expected ui stage " + Stage.FirstChoiceValid + " when button is " + RightButtonMode.Continue);
                }
                updateStage(Stage.NeedToConfirm);
            } else if (this.mUiStage.rightMode == RightButtonMode.Confirm) {
                if (this.mUiStage != Stage.ChoiceConfirmed) {
                    throw new IllegalStateException("expected ui stage " + Stage.ChoiceConfirmed + " when button is " + RightButtonMode.Confirm);
                }
                startSaveAndFinish();
            } else if (this.mUiStage.rightMode != RightButtonMode.Ok) {
            } else {
                if (this.mUiStage != Stage.HelpScreen) {
                    throw new IllegalStateException("Help screen is only mode with ok button, but stage is " + this.mUiStage);
                }
                this.mLockPatternView.clearPattern();
                this.mLockPatternView.setDisplayMode(DisplayMode.Correct);
                updateStage(Stage.Introduction);
            }
        }

        public void onClick(View v) {
            if (v == this.mFooterLeftButton) {
                handleLeftButton();
            } else if (v == this.mFooterRightButton) {
                handleRightButton();
            } else if (v == this.mReDraw) {
                retry();
            } else if (v == this.mDone) {
                if (this.mIsForPrivacySpacePrivacyUser) {
                    Intent data = new Intent();
                    data.putExtra("choosen_password", LockPatternUtils.patternToString(this.mChosenPattern));
                    getActivity().setResult(-1, data);
                    getActivity().finish();
                    return;
                }
                startSaveAndFinish();
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("uiStage", this.mUiStage.ordinal());
            if (this.mChosenPattern != null) {
                outState.putString("chosenPattern", LockPatternUtils.patternToString(this.mChosenPattern));
            }
            if (this.mCurrentPattern != null) {
                outState.putString("currentPattern", this.mCurrentPattern);
            }
        }

        protected void updateStage(Stage stage) {
            Stage previousStage = this.mUiStage;
            this.mUiStage = stage;
            if (stage == Stage.ChoiceTooShort) {
                this.mHeaderText.setText(getResources().getQuantityString(stage.headerMessage, 4, new Object[]{Integer.valueOf(4)}));
            } else {
                this.mHeaderText.setText(stage.headerMessage);
            }
            if (stage.footerMessage == -1) {
                this.mFooterText.setVisibility(8);
            } else {
                this.mFooterText.setVisibility(0);
                this.mFooterText.setText(stage.footerMessage);
            }
            this.mFooterRightButton.setText(stage.rightMode.text);
            this.mFooterRightButton.setEnabled(stage.rightMode.enabled);
            this.mFooterLeftButton.setVisibility(8);
            if (stage.patternEnabled) {
                this.mLockPatternView.enableInput();
            } else {
                this.mLockPatternView.disableInput();
            }
            this.mLockPatternView.setDisplayMode(DisplayMode.Correct);
            boolean announceAlways = false;
            switch (-getcom-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues()[this.mUiStage.ordinal()]) {
                case 1:
                    this.mReDraw.setVisibility(0);
                    this.mDone.setVisibility(0);
                    this.mDone.setEnabled(true);
                    break;
                case 2:
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    postClearPatternRunnable();
                    announceAlways = true;
                    break;
                case 3:
                    this.mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    postClearPatternRunnable();
                    announceAlways = true;
                    break;
                case 5:
                    this.mLockPatternView.setPattern(DisplayMode.Animate, this.mAnimatePattern);
                    break;
                case 6:
                    this.mLockPatternView.clearPattern();
                    this.mReDraw.setVisibility(8);
                    this.mDone.setVisibility(8);
                    break;
                case 7:
                    this.mLockPatternView.clearPattern();
                    this.mReDraw.setVisibility(0);
                    this.mDone.setVisibility(0);
                    this.mDone.setEnabled(false);
                    break;
            }
            if (previousStage != stage || announceAlways) {
                this.mHeaderText.announceForAccessibility(this.mHeaderText.getText());
            }
        }

        private void postClearPatternRunnable() {
            this.mLockPatternView.removeCallbacks(this.mClearPatternRunnable);
            this.mLockPatternView.postDelayed(this.mClearPatternRunnable, 2000);
        }

        private void startSaveAndFinish() {
            if (this.mSaveAndFinishWorker != null) {
                Log.w("ChooseLockPattern", "startSaveAndFinish with an existing SaveAndFinishWorker.");
                return;
            }
            setRightButtonEnabled(false);
            this.mSaveAndFinishWorker = new SaveAndFinishWorker();
            this.mSaveAndFinishWorker.setListener(this);
            getFragmentManager().beginTransaction().add(this.mSaveAndFinishWorker, "save_and_finish_worker").commit();
            getFragmentManager().executePendingTransactions();
            this.mSaveAndFinishWorker.start(this.mChooseLockSettingsHelper.utils(), getActivity().getIntent().getBooleanExtra("extra_require_password", true), this.mHasChallenge, this.mChallenge, this.mChosenPattern, this.mCurrentPattern, this.mUserId);
        }

        public void onChosenLockSaveFinished(boolean wasSecureBefore, Intent resultData) {
            getActivity().setResult(1, resultData);
            getActivity().finish();
        }
    }

    private static class SaveAndFinishWorker extends SaveChosenLockWorkerBase {
        private List<Cell> mChosenPattern;
        private String mCurrentPattern;
        private boolean mLockVirgin;

        private SaveAndFinishWorker() {
        }

        public void start(LockPatternUtils utils, boolean credentialRequired, boolean hasChallenge, long challenge, List<Cell> chosenPattern, String currentPattern, int userId) {
            prepare(utils, credentialRequired, hasChallenge, challenge, userId);
            this.mCurrentPattern = currentPattern;
            this.mChosenPattern = chosenPattern;
            this.mUserId = userId;
            this.mLockVirgin = !this.mUtils.isPatternEverChosen(this.mUserId);
            start();
        }

        protected Intent saveAndVerifyInBackground() {
            int userId = this.mUserId;
            if (this.mChosenPattern != null && this.mChosenPattern.size() >= 4) {
                this.mUtils.saveLockPattern(this.mChosenPattern, this.mCurrentPattern, userId);
            }
            if (!this.mHasChallenge) {
                return null;
            }
            byte[] token = null;
            try {
                if (this.mChosenPattern != null && this.mChosenPattern.size() >= 4) {
                    token = this.mUtils.verifyPattern(this.mChosenPattern, this.mChallenge, userId);
                }
            } catch (RequestThrottledException e) {
                Log.w("ChooseLockPattern", "saveAndVerifyInBackground RequestThrottledException:", e);
                token = null;
            }
            if (token == null) {
                Log.e("ChooseLockPattern", "critical: no token returned for known good pattern");
            }
            Intent result = new Intent();
            result.putExtra("hw_auth_token", token);
            return result;
        }

        protected void finish(Intent resultData) {
            if (this.mLockVirgin) {
                this.mUtils.setVisiblePatternEnabled(true, this.mUserId);
            }
            super.finish(resultData);
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", getFragmentClass().getName());
        return modIntent;
    }

    public static Intent createIntent(Context context, boolean requirePassword, boolean confirmCredentials, int userId) {
        Intent intent = new Intent(context, ChooseLockPattern.class);
        intent.putExtra("key_lock_method", "pattern");
        intent.putExtra("confirm_credentials", confirmCredentials);
        intent.putExtra("extra_require_password", requirePassword);
        intent.putExtra("android.intent.extra.USER_ID", userId);
        return intent;
    }

    public static Intent createIntent(Context context, boolean requirePassword, String pattern, int userId) {
        Intent intent = createIntent(context, requirePassword, false, userId);
        intent.putExtra("password", pattern);
        return intent;
    }

    public static Intent createIntent(Context context, boolean requirePassword, long challenge, int userId) {
        Intent intent = createIntent(context, requirePassword, false, userId);
        intent.putExtra("has_challenge", true);
        intent.putExtra("challenge", challenge);
        return intent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockPatternFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    Class<? extends Fragment> getFragmentClass() {
        return ChooseLockPatternFragment.class;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        setTitle(getText(2131628757));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
