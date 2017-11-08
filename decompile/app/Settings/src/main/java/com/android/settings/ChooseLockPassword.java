package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.ErrorTipTextLayout;
import java.util.HashMap;

public class ChooseLockPassword extends SettingsActivity {

    public static class ChooseLockPasswordFragment extends InstrumentedFragment implements OnClickListener, OnEditorActionListener, TextWatcher, Listener {
        private Button mCancelButton;
        private long mChallenge;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private String mChosenPassword;
        private String mCurrentPassword;
        private HwCustChooseLockPassword mCustChooseLockPassword;
        private ErrorTipTextLayout mErrorTextLayout;
        private String mFirstPin;
        private TextView mFooterText;
        private Handler mHandler = new Handler() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void handleMessage(Message msg) {
                Activity activity = ChooseLockPasswordFragment.this.getActivity();
                if (!(activity == null || activity.isFinishing() || msg.what != 1)) {
                    ChooseLockPasswordFragment.this.updateStage((Stage) msg.obj);
                }
            }
        };
        private boolean mHasChallenge;
        private TextView mHeaderText;
        private boolean mHideDrawer = false;
        private boolean mIsAlphaMode;
        boolean mIsForPrivacySpaceMainUser = false;
        boolean mIsForPrivacySpacePrivacyUser = false;
        private PasswordEntryKeyboardHelper mKeyboardHelper;
        private KeyboardView mKeyboardView;
        private LockPatternUtils mLockPatternUtils;
        private Button mNextButton;
        private EditText mPasswordEntry;
        private TextViewInputDisabler mPasswordEntryInputDisabler;
        private int mPasswordMaxLength = 16;
        private int mPasswordMinLength = 4;
        private int mPasswordMinLetters = 0;
        private int mPasswordMinLowerCase = 0;
        private int mPasswordMinNonLetter = 0;
        private int mPasswordMinNumeric = 0;
        private int mPasswordMinSymbols = 0;
        private int mPasswordMinUpperCase = 0;
        private PrivacySpaceSettingsHelper mPrivacySpaceSettingsHelper;
        private int mRequestedQuality = 131072;
        private SaveAndFinishWorker mSaveAndFinishWorker;
        private CheckBox mShowPassword;
        private Stage mUiStage = Stage.Introduction;
        private int mUserId;

        protected enum Stage {
            Introduction(2131628343, 2131628344, 2131624783),
            NeedToConfirm(2131628345, 2131628346, 2131624794),
            ConfirmWrong(2131628351, 2131628351, 2131624783);
            
            public final int alphaHint;
            public final int buttonText;
            public final int numericHint;

            private Stage(int hintInAlpha, int hintInNumeric, int nextButtonText) {
                this.alphaHint = hintInAlpha;
                this.numericHint = hintInNumeric;
                this.buttonText = nextButtonText;
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
            Intent intent = getActivity().getIntent();
            if (getActivity() instanceof ChooseLockPassword) {
                this.mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
                this.mRequestedQuality = Math.max(intent.getIntExtra("lockscreen.password_type", this.mRequestedQuality), this.mLockPatternUtils.getRequestedPasswordQuality(this.mUserId));
                boolean z = (262144 == this.mRequestedQuality || 327680 == this.mRequestedQuality) ? true : 393216 == this.mRequestedQuality;
                this.mIsAlphaMode = z;
                this.mPasswordMinLength = Math.max(Math.max(4, intent.getIntExtra("lockscreen.password_min", this.mPasswordMinLength)), this.mLockPatternUtils.getRequestedMinimumPasswordLength(this.mUserId));
                this.mPasswordMaxLength = intent.getIntExtra("lockscreen.password_max", this.mPasswordMaxLength);
                this.mPasswordMinLetters = Math.max(intent.getIntExtra("lockscreen.password_min_letters", this.mPasswordMinLetters), this.mLockPatternUtils.getRequestedPasswordMinimumLetters(this.mUserId));
                if (this.mIsAlphaMode && this.mPasswordMinLetters <= 0) {
                    this.mPasswordMinLetters = 1;
                }
                this.mPasswordMinUpperCase = Math.max(intent.getIntExtra("lockscreen.password_min_uppercase", this.mPasswordMinUpperCase), this.mLockPatternUtils.getRequestedPasswordMinimumUpperCase(this.mUserId));
                this.mPasswordMinLowerCase = Math.max(intent.getIntExtra("lockscreen.password_min_lowercase", this.mPasswordMinLowerCase), this.mLockPatternUtils.getRequestedPasswordMinimumLowerCase(this.mUserId));
                this.mPasswordMinNumeric = Math.max(intent.getIntExtra("lockscreen.password_min_numeric", this.mPasswordMinNumeric), this.mLockPatternUtils.getRequestedPasswordMinimumNumeric(this.mUserId));
                this.mPasswordMinSymbols = Math.max(intent.getIntExtra("lockscreen.password_min_symbols", this.mPasswordMinSymbols), this.mLockPatternUtils.getRequestedPasswordMinimumSymbols(this.mUserId));
                this.mPasswordMinNonLetter = Math.max(intent.getIntExtra("lockscreen.password_min_nonletter", this.mPasswordMinNonLetter), this.mLockPatternUtils.getRequestedPasswordMinimumNonLetter(this.mUserId));
                this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
                this.mHideDrawer = getActivity().getIntent().getBooleanExtra(":settings:hide_drawer", false);
                this.mPrivacySpaceSettingsHelper = new PrivacySpaceSettingsHelper((Fragment) this);
                int PrivacySpaceUserType = intent.getIntExtra("hidden_space_main_user_type", -1);
                if (PrivacySpaceUserType == 1) {
                    z = true;
                } else {
                    z = false;
                }
                this.mIsForPrivacySpaceMainUser = z;
                if (PrivacySpaceUserType == 4) {
                    z = true;
                } else {
                    z = false;
                }
                this.mIsForPrivacySpacePrivacyUser = z;
                if (intent.getBooleanExtra("for_cred_req_boot", false)) {
                    SaveAndFinishWorker w = new SaveAndFinishWorker();
                    boolean required = getActivity().getIntent().getBooleanExtra("extra_require_password", true);
                    String current = intent.getStringExtra("password");
                    w.setBlocking(true);
                    w.setListener(this);
                    w.start(this.mChooseLockSettingsHelper.utils(), required, false, 0, current, current, this.mRequestedQuality, this.mUserId);
                }
                this.mCustChooseLockPassword = (HwCustChooseLockPassword) HwCustUtils.createObj(HwCustChooseLockPassword.class, new Object[]{getActivity()});
                if (this.mCustChooseLockPassword != null) {
                    this.mCustChooseLockPassword.loadCustomPasswordTable();
                }
                setHasOptionsMenu(true);
                return;
            }
            throw new SecurityException("Fragment contained in wrong activity");
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if (getActivity().getActionBar() != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(2130968672, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            int i;
            super.onViewCreated(view, savedInstanceState);
            this.mCancelButton = (Button) view.findViewById(2131886370);
            this.mCancelButton.setOnClickListener(this);
            this.mNextButton = (Button) view.findViewById(2131886371);
            this.mNextButton.setOnClickListener(this);
            this.mFooterText = (TextView) view.findViewById(2131886369);
            if (this.mFooterText != null) {
                this.mFooterText.setVisibility(0);
            }
            boolean z = (262144 == this.mRequestedQuality || 327680 == this.mRequestedQuality) ? true : 393216 == this.mRequestedQuality;
            this.mIsAlphaMode = z;
            this.mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(2131886372);
            this.mPasswordEntry = (EditText) view.findViewById(2131886367);
            this.mPasswordEntry.setOnEditorActionListener(this);
            this.mPasswordEntry.addTextChangedListener(this);
            this.mPasswordEntryInputDisabler = new TextViewInputDisabler(this.mPasswordEntry);
            initShowPasswordCheckBox(view);
            this.mKeyboardHelper = new PasswordEntryKeyboardHelper(getActivity(), this.mKeyboardView, this.mPasswordEntry);
            PasswordEntryKeyboardHelper passwordEntryKeyboardHelper = this.mKeyboardHelper;
            if (this.mIsAlphaMode) {
                i = 0;
            } else {
                i = 1;
            }
            passwordEntryKeyboardHelper.setKeyboardMode(i);
            this.mHeaderText = (TextView) view.findViewById(2131886364);
            this.mKeyboardView.requestFocus();
            int currentType = this.mPasswordEntry.getInputType();
            EditText editText = this.mPasswordEntry;
            if (!this.mIsAlphaMode) {
                currentType = 18;
            }
            editText.setInputType(currentType);
            Intent intent = getActivity().getIntent();
            boolean confirmCredentials = intent.getBooleanExtra("confirm_credentials", true);
            this.mCurrentPassword = intent.getStringExtra("password");
            this.mHasChallenge = intent.getBooleanExtra("has_challenge", false);
            this.mChallenge = intent.getLongExtra("challenge", 0);
            if (savedInstanceState == null) {
                updateStage(Stage.Introduction);
                if (confirmCredentials) {
                    this.mChooseLockSettingsHelper.launchConfirmationActivity(58, getString(2131624724), true, this.mUserId);
                }
            } else {
                this.mFirstPin = savedInstanceState.getString("first_pin");
                String state = savedInstanceState.getString("ui_stage");
                if (state != null) {
                    this.mUiStage = Stage.valueOf(state);
                    updateStage(this.mUiStage);
                }
                if (this.mCurrentPassword == null) {
                    this.mCurrentPassword = savedInstanceState.getString("current_password");
                }
                this.mSaveAndFinishWorker = (SaveAndFinishWorker) getFragmentManager().findFragmentByTag("save_and_finish_worker");
            }
            this.mErrorTextLayout = (ErrorTipTextLayout) view.findViewById(2131886366);
            getActivity().setTitle(getTitle());
        }

        protected int getMetricsCategory() {
            return 28;
        }

        public void onResume() {
            super.onResume();
            updateStage(this.mUiStage);
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(this);
            } else {
                this.mKeyboardView.requestFocus();
            }
        }

        public void onPause() {
            this.mHandler.removeMessages(1);
            if (this.mSaveAndFinishWorker != null) {
                this.mSaveAndFinishWorker.setListener(null);
            }
            super.onPause();
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("ui_stage", this.mUiStage.name());
            outState.putString("first_pin", this.mFirstPin);
            outState.putString("current_password", this.mCurrentPassword);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case 58:
                    if (resultCode != -1) {
                        getActivity().setResult(1);
                        getActivity().finish();
                        return;
                    }
                    this.mCurrentPassword = data.getStringExtra("password");
                    return;
                default:
                    return;
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

        private void initShowPasswordCheckBox(View view) {
            this.mShowPassword = (CheckBox) view.findViewById(2131886368);
            if (this.mShowPassword != null) {
                this.mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                        int start = ChooseLockPasswordFragment.this.mPasswordEntry.getSelectionStart();
                        int stop = ChooseLockPasswordFragment.this.mPasswordEntry.getSelectionEnd();
                        if (checked) {
                            ChooseLockPasswordFragment.this.mPasswordEntry.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        } else {
                            ChooseLockPasswordFragment.this.mPasswordEntry.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                        if (stop <= ChooseLockPasswordFragment.this.mPasswordEntry.length()) {
                            ChooseLockPasswordFragment.this.mPasswordEntry.setSelection(start, stop);
                        }
                    }
                });
            }
        }

        private String getTitle() {
            if (this.mIsForPrivacySpaceMainUser) {
                return getString(2131628750);
            }
            int i;
            if (this.mIsForPrivacySpacePrivacyUser) {
                if (this.mIsAlphaMode) {
                    i = 2131628753;
                } else {
                    i = 2131628752;
                }
                return getString(i);
            }
            if (this.mIsAlphaMode) {
                i = 2131628756;
            } else {
                i = 2131628755;
            }
            return getString(i);
        }

        protected String getHeaderText() {
            if (this.mIsAlphaMode) {
                return getString(2131628348, new Object[]{Integer.valueOf(this.mPasswordMinLength), Integer.valueOf(this.mPasswordMaxLength), Integer.valueOf(this.mPasswordMinLetters)});
            }
            return getString(2131628347, new Object[]{Integer.valueOf(this.mPasswordMinLength), Integer.valueOf(this.mPasswordMaxLength)});
        }

        protected void updateStage(Stage stage) {
            Stage previousStage = this.mUiStage;
            this.mUiStage = stage;
            updateUi();
            if (previousStage != stage) {
                this.mHeaderText.announceForAccessibility(this.mHeaderText.getText());
            }
        }

        private String validatePassword(String password) {
            int i;
            if (password.length() < this.mPasswordMinLength) {
                if (this.mIsAlphaMode) {
                    i = 2131624781;
                } else {
                    i = 2131624782;
                }
                return getString(i, new Object[]{Integer.valueOf(this.mPasswordMinLength)});
            } else if (password.length() > this.mPasswordMaxLength) {
                if (this.mIsAlphaMode) {
                    i = 2131624784;
                } else {
                    i = 2131624785;
                }
                return getString(i, new Object[]{Integer.valueOf(this.mPasswordMaxLength + 1)});
            } else {
                int letters = 0;
                int numbers = 0;
                int lowercase = 0;
                int symbols = 0;
                int uppercase = 0;
                int nonletter = 0;
                for (int i2 = 0; i2 < password.length(); i2++) {
                    char c = password.charAt(i2);
                    if (c < ' ' || c > '') {
                        return getString(2131624788);
                    }
                    if (c >= '0' && c <= '9') {
                        numbers++;
                        nonletter++;
                    } else if (c >= 'A' && c <= 'Z') {
                        letters++;
                        uppercase++;
                    } else if (c < 'a' || c > 'z') {
                        symbols++;
                        nonletter++;
                    } else {
                        letters++;
                        lowercase++;
                    }
                }
                String result;
                if (131072 == this.mRequestedQuality || 196608 == this.mRequestedQuality) {
                    if (letters > 0 || symbols > 0) {
                        return getString(2131624786);
                    }
                    int sequence = LockPatternUtils.maxLengthSequence(password);
                    if (196608 == this.mRequestedQuality && sequence > 3) {
                        return getString(2131624793);
                    }
                    if (this.mCustChooseLockPassword != null) {
                        result = this.mCustChooseLockPassword.showSimplePasswordWarning(password);
                        if (result != null) {
                            return result;
                        }
                    }
                } else if (393216 != this.mRequestedQuality) {
                    boolean alphabetic = 262144 == this.mRequestedQuality;
                    boolean alphanumeric = 327680 == this.mRequestedQuality;
                    if ((alphabetic || alphanumeric) && letters == 0) {
                        return getResources().getQuantityString(2131689533, 1, new Object[]{Integer.valueOf(1)});
                    } else if (alphanumeric && numbers == 0) {
                        return getResources().getQuantityString(2131689531, 1, new Object[]{Integer.valueOf(1)});
                    } else if (this.mCustChooseLockPassword != null) {
                        result = this.mCustChooseLockPassword.showSimplePasswordWarning(password);
                        if (result != null) {
                            return result;
                        }
                    }
                } else if (letters < this.mPasswordMinLetters) {
                    return String.format(getResources().getQuantityString(2131689518, this.mPasswordMinLetters, new Object[]{Integer.valueOf(this.mPasswordMinLetters)}), new Object[]{Integer.valueOf(this.mPasswordMinLetters)});
                } else if (numbers < this.mPasswordMinNumeric) {
                    return String.format(getResources().getQuantityString(2131689517, this.mPasswordMinNumeric, new Object[]{Integer.valueOf(this.mPasswordMinNumeric)}), new Object[]{Integer.valueOf(this.mPasswordMinNumeric)});
                } else if (lowercase < this.mPasswordMinLowerCase) {
                    return String.format(getResources().getQuantityString(2131689512, this.mPasswordMinLowerCase, new Object[]{Integer.valueOf(this.mPasswordMinLowerCase)}), new Object[]{Integer.valueOf(this.mPasswordMinLowerCase)});
                } else if (uppercase < this.mPasswordMinUpperCase) {
                    return String.format(getResources().getQuantityString(2131689516, this.mPasswordMinUpperCase, new Object[]{Integer.valueOf(this.mPasswordMinUpperCase)}), new Object[]{Integer.valueOf(this.mPasswordMinUpperCase)});
                } else if (symbols < this.mPasswordMinSymbols) {
                    return String.format(getResources().getQuantityString(2131689519, this.mPasswordMinSymbols, new Object[]{Integer.valueOf(this.mPasswordMinSymbols)}), new Object[]{Integer.valueOf(this.mPasswordMinSymbols)});
                } else if (nonletter < this.mPasswordMinNonLetter) {
                    return String.format(getResources().getQuantityString(2131689520, this.mPasswordMinNonLetter, new Object[]{Integer.valueOf(this.mPasswordMinNonLetter)}), new Object[]{Integer.valueOf(this.mPasswordMinNonLetter)});
                } else if (this.mCustChooseLockPassword != null) {
                    result = this.mCustChooseLockPassword.showSimplePasswordWarning(password);
                    if (result != null) {
                        return result;
                    }
                }
                if (!this.mLockPatternUtils.checkPasswordHistory(password, this.mUserId)) {
                    return null;
                }
                if (this.mIsAlphaMode) {
                    i = 2131624792;
                } else {
                    i = 2131624787;
                }
                return getString(i);
            }
        }

        public void handleNext() {
            if (this.mSaveAndFinishWorker == null) {
                this.mChosenPassword = this.mPasswordEntry.getText().toString();
                if (!TextUtils.isEmpty(this.mChosenPassword)) {
                    String errorMsg = null;
                    if (this.mUiStage == Stage.Introduction) {
                        errorMsg = validatePassword(this.mChosenPassword);
                        if (errorMsg == null) {
                            this.mFirstPin = this.mChosenPassword;
                            this.mPasswordEntry.setText("");
                            updateStage(Stage.NeedToConfirm);
                        }
                    } else if (this.mUiStage == Stage.NeedToConfirm) {
                        if (!this.mFirstPin.equals(this.mChosenPassword)) {
                            CharSequence tmp = this.mPasswordEntry.getText();
                            if (tmp != null) {
                                Selection.setSelection((Spannable) tmp, 0, tmp.length());
                            }
                            updateStage(Stage.ConfirmWrong);
                            errorMsg = getString(this.mIsAlphaMode ? this.mUiStage.alphaHint : this.mUiStage.numericHint);
                        } else if (this.mIsForPrivacySpacePrivacyUser) {
                            Intent data = new Intent();
                            data.putExtra("choosen_password", this.mChosenPassword);
                            getActivity().setResult(-1, data);
                            getActivity().finish();
                            return;
                        } else {
                            startSaveAndFinish();
                        }
                    }
                    if (errorMsg != null) {
                        showError(errorMsg, this.mUiStage);
                    } else {
                        hideError();
                    }
                }
            }
        }

        protected void setNextEnabled(boolean enabled) {
            this.mNextButton.setEnabled(enabled);
        }

        protected void setNextText(int text) {
            this.mNextButton.setText(text);
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case 2131886370:
                    getActivity().finish();
                    return;
                case 2131886371:
                    handleNext();
                    return;
                default:
                    Log.w("ChooseLockPassword", "onClick unknown id");
                    return;
            }
        }

        private void hideError() {
            if (this.mErrorTextLayout != null) {
                this.mErrorTextLayout.setError(null);
            }
            this.mFooterText.setVisibility(0);
        }

        private void showError(String msg) {
            this.mFooterText.setVisibility(4);
            if (this.mErrorTextLayout != null) {
                this.mErrorTextLayout.setError(msg);
            }
        }

        private void showError(String msg, Stage next) {
            this.mFooterText.setVisibility(4);
            showError(msg);
            Message mesg = this.mHandler.obtainMessage(1, next);
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(mesg, 3000);
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId != 0 && actionId != 6 && actionId != 5) {
                return false;
            }
            handleNext();
            return true;
        }

        private void updateUi() {
            boolean z = false;
            boolean canInput = this.mSaveAndFinishWorker == null;
            String password = this.mPasswordEntry.getText().toString();
            int length = password.length();
            if (this.mUiStage == Stage.Introduction) {
                if (length < this.mPasswordMinLength) {
                    this.mHeaderText.setText(getHeaderText());
                    hideError();
                    setNextEnabled(false);
                } else {
                    String error = validatePassword(password);
                    if (error != null) {
                        showError(error);
                        setNextEnabled(false);
                    } else {
                        hideError();
                        setNextEnabled(true);
                    }
                }
            } else if (this.mUiStage == Stage.NeedToConfirm) {
                boolean z2;
                this.mHeaderText.setText(this.mIsAlphaMode ? this.mUiStage.alphaHint : this.mUiStage.numericHint);
                hideError();
                if (!canInput || length <= 0) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                setNextEnabled(z2);
            } else {
                if (canInput && length > 0) {
                    z = true;
                }
                setNextEnabled(z);
            }
            setNextText(this.mUiStage.buttonText);
            this.mPasswordEntryInputDisabler.setInputEnabled(canInput);
        }

        public void afterTextChanged(Editable s) {
            if (this.mUiStage == Stage.ConfirmWrong) {
                this.mUiStage = Stage.NeedToConfirm;
            }
            updateUi();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        private void startSaveAndFinish() {
            if (this.mSaveAndFinishWorker != null) {
                Log.w("ChooseLockPassword", "startSaveAndFinish with an existing SaveAndFinishWorker.");
                return;
            }
            this.mPasswordEntryInputDisabler.setInputEnabled(false);
            setNextEnabled(false);
            this.mSaveAndFinishWorker = new SaveAndFinishWorker();
            this.mSaveAndFinishWorker.setListener(this);
            getFragmentManager().beginTransaction().add(this.mSaveAndFinishWorker, "save_and_finish_worker").commit();
            getFragmentManager().executePendingTransactions();
            this.mSaveAndFinishWorker.start(this.mLockPatternUtils, getActivity().getIntent().getBooleanExtra("extra_require_password", true), this.mHasChallenge, this.mChallenge, this.mChosenPassword, this.mCurrentPassword, this.mRequestedQuality, this.mUserId);
        }

        public void onChosenLockSaveFinished(boolean wasSecureBefore, Intent resultData) {
            getActivity().setResult(1, resultData);
            getActivity().finish();
        }
    }

    private static class SaveAndFinishWorker extends SaveChosenLockWorkerBase {
        private String mChosenPassword;
        private String mCurrentPassword;
        private int mRequestedQuality;

        private SaveAndFinishWorker() {
        }

        public void start(LockPatternUtils utils, boolean required, boolean hasChallenge, long challenge, String chosenPassword, String currentPassword, int requestedQuality, int userId) {
            prepare(utils, required, hasChallenge, challenge, userId);
            this.mChosenPassword = chosenPassword;
            this.mCurrentPassword = currentPassword;
            this.mRequestedQuality = requestedQuality;
            this.mUserId = userId;
            start();
        }

        protected Intent saveAndVerifyInBackground() {
            int i = 0;
            Intent result = null;
            this.mUtils.saveLockPassword(this.mChosenPassword, this.mCurrentPassword, this.mRequestedQuality, this.mUserId);
            if (getActivity() != null) {
                System.putIntForUser(getActivity().getContentResolver(), "lockscreen.password_type", this.mRequestedQuality, UserHandle.myUserId());
                if (this.mHasChallenge) {
                    byte[] verifyPassword;
                    try {
                        verifyPassword = this.mUtils.verifyPassword(this.mChosenPassword, this.mChallenge, this.mUserId);
                    } catch (RequestThrottledException e) {
                        Log.w("ChooseLockPassword", "saveAndVerifyInBackground fail:", e);
                        verifyPassword = null;
                    }
                    if (verifyPassword == null) {
                        Log.e("ChooseLockPassword", "critical: no token returned for known good password.");
                        HashMap<Short, Object> map = new HashMap();
                        boolean isAplpha = (262144 == this.mRequestedQuality || 327680 == this.mRequestedQuality) ? true : 393216 == this.mRequestedQuality;
                        Short valueOf = Short.valueOf((short) 0);
                        if (isAplpha) {
                            i = 1;
                        }
                        map.put(valueOf, Integer.valueOf(i));
                        RadarReporter.reportRadar(907018008, map);
                    }
                    result = new Intent();
                    result.putExtra("hw_auth_token", verifyPassword);
                }
                if (BiometricManager.open(getActivity()).getEnrolledFpNum(this.mUserId) > 0) {
                    Log.w("ChooseLockPassword", "fingerprint already exist before password setup!");
                    if (FingerprintUtils.getKeyguardAssociationStatus(getActivity(), this.mUserId) == -1) {
                        Log.w("ChooseLockPassword", "restore keyguard association!");
                        FingerprintUtils.setKeyguardAssociationStatus(getActivity(), this.mUserId, true);
                    }
                }
                return result;
            }
            Log.e("ChooseLockPassword", "getActivity is null");
            return null;
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", getFragmentClass().getName());
        return modIntent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, boolean confirmCredentials) {
        Intent intent = new Intent().setClass(context, ChooseLockPassword.class);
        intent.putExtra("lockscreen.password_type", quality);
        intent.putExtra("lockscreen.password_min", minLength);
        intent.putExtra("lockscreen.password_max", maxLength);
        intent.putExtra("confirm_credentials", confirmCredentials);
        intent.putExtra("extra_require_password", requirePasswordToDecrypt);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, String password) {
        Intent intent = createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, false);
        intent.putExtra("password", password);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, String password, int userId) {
        Intent intent = createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, password);
        intent.putExtra("android.intent.extra.USER_ID", userId);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, long challenge) {
        Intent intent = createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, false);
        intent.putExtra("has_challenge", true);
        intent.putExtra("challenge", challenge);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength, int maxLength, boolean requirePasswordToDecrypt, long challenge, int userId) {
        Intent intent = createIntent(context, quality, minLength, maxLength, requirePasswordToDecrypt, challenge);
        intent.putExtra("android.intent.extra.USER_ID", userId);
        return intent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockPasswordFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    Class<? extends Fragment> getFragmentClass() {
        return ChooseLockPasswordFragment.class;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isTablet()) {
            setRequestedOrientation(1);
        }
        setTitle(getText(2131625522));
    }

    protected void onStop() {
        super.onStop();
        SettingsExtUtils.checkHideSoftInput(this);
    }
}
