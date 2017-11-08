package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;

public class ChoosePasswordFragmentBase extends InstrumentedFragment implements OnClickListener, OnEditorActionListener {
    private boolean hasEntryOneRequestChanged = false;
    protected Button mCancelButton;
    protected String[] mEntryErrMsgs = new String[]{null, null};
    protected TextView mFooterText;
    protected TextView mHeaderText;
    protected boolean mIsAlphaMode = true;
    protected PasswordEntryKeyboardHelper mKeyboardHelper;
    protected KeyboardView mKeyboardView;
    protected Button mNextButton;
    private EditText[] mPasswordEntries = new EditText[]{null, null};
    protected int mPasswordMaxLength = 16;
    protected int mPasswordMinLength = 4;
    protected int mPasswordMinLetters = 0;
    protected int mPasswordMinLowerCase = 0;
    protected int mPasswordMinNonLetter = 0;
    protected int mPasswordMinNumeric = 0;
    protected int mPasswordMinSymbols = 0;
    protected int mPasswordMinUpperCase = 0;
    private CheckBox mShowPassword;
    protected TextWatcher mTextWatcher1 = new TextWatcher() {
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable arg0) {
            ChoosePasswordFragmentBase.this.hasEntryOneRequestChanged = false;
            ChoosePasswordFragmentBase.this.updateStage();
        }
    };
    protected TextWatcher mTextWatcher2 = new TextWatcher() {
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        public void afterTextChanged(Editable arg0) {
            ChoosePasswordFragmentBase.this.updateStage();
        }
    };
    protected State mUiStage = State.Introduction;

    protected enum State {
        Introduction,
        InvalidPassword,
        EmptyPassword,
        CannotConfirm,
        InvalidPasswordBoth,
        NeedToConfirm,
        ConfirmWrong,
        Finish
    }

    protected boolean isAlphaMode() {
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        int currentType;
        int i;
        if (SettingsExtUtils.isStartupGuideMode(getActivity().getContentResolver())) {
            view = inflater.inflate(2130968675, null);
        } else {
            view = inflater.inflate(2130968674, null);
        }
        Activity activity = getActivity();
        this.mCancelButton = (Button) view.findViewById(2131886370);
        this.mCancelButton.setOnClickListener(this);
        this.mNextButton = (Button) view.findViewById(2131886371);
        this.mNextButton.setOnClickListener(this);
        this.mIsAlphaMode = isAlphaMode();
        EditText entry = (EditText) view.findViewById(2131886381);
        if (this.mIsAlphaMode) {
            currentType = entry.getInputType();
        } else {
            currentType = 18;
        }
        entry.setOnEditorActionListener(this);
        entry.addTextChangedListener(this.mTextWatcher1);
        entry.setInputType(currentType);
        entry.setHint(this.mIsAlphaMode ? 2131628343 : 2131628344);
        this.mPasswordEntries[0] = entry;
        entry = (EditText) view.findViewById(2131886382);
        entry.setOnEditorActionListener(this);
        entry.addTextChangedListener(this.mTextWatcher2);
        entry.setInputType(currentType);
        if (this.mIsAlphaMode) {
            i = 2131628345;
        } else {
            i = 2131628346;
        }
        entry.setHint(i);
        entry.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View arg0, boolean hasFocus) {
                if (hasFocus) {
                    ChoosePasswordFragmentBase.this.updateStage();
                }
            }
        });
        this.mPasswordEntries[1] = entry;
        this.mShowPassword = (CheckBox) view.findViewById(2131886368);
        this.mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                for (EditText view : ChoosePasswordFragmentBase.this.mPasswordEntries) {
                    int start = view.getSelectionStart();
                    int stop = view.getSelectionEnd();
                    if (checked) {
                        view.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        view.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    view.setSelection(start, stop);
                }
            }
        });
        this.mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(2131886372);
        this.mKeyboardHelper = new PasswordEntryKeyboardHelper(activity, this.mKeyboardView, entry);
        PasswordEntryKeyboardHelper passwordEntryKeyboardHelper = this.mKeyboardHelper;
        if (this.mIsAlphaMode) {
            i = 0;
        } else {
            i = 1;
        }
        passwordEntryKeyboardHelper.setKeyboardMode(i);
        this.mKeyboardView.requestFocus();
        this.mHeaderText = (TextView) view.findViewById(2131886364);
        String headerText = getHeaderText();
        if (TextUtils.isEmpty(headerText)) {
            this.mHeaderText.setVisibility(8);
        } else {
            this.mHeaderText.setText(headerText);
        }
        this.mFooterText = (TextView) view.findViewById(2131886369);
        this.mFooterText.setText(getFooterText());
        Intent intent = activity.getIntent();
        boolean confirmCredentials = true;
        if (intent != null) {
            confirmCredentials = intent.getBooleanExtra("confirm_credentials", true);
        }
        if (savedInstanceState == null) {
            this.mUiStage = State.Introduction;
            if (confirmCredentials) {
                launchConfirmation();
                Log.d("ChoosePasswordFragmentBase", "Need to confirm password.");
            }
        }
        return view;
    }

    public void onResume() {
        super.onResume();
        updateStage();
        if (this.mKeyboardView != null) {
            this.mKeyboardView.requestFocus();
        }
    }

    protected String getHeaderText() {
        return "";
    }

    protected String getFooterText() {
        return "";
    }

    protected void launchConfirmation() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected int getMetricsCategory() {
        return 28;
    }

    private void updateStage() {
        if (getActivity() != null) {
            String password1 = this.mPasswordEntries[0].getText().toString();
            String password2 = this.mPasswordEntries[1].getText().toString();
            if (TextUtils.isEmpty(password1) && TextUtils.isEmpty(password2)) {
                this.mUiStage = State.Introduction;
                this.mEntryErrMsgs[0] = null;
                this.mEntryErrMsgs[1] = null;
            } else if (TextUtils.isEmpty(password1)) {
                validatePassword(1);
                this.mUiStage = State.EmptyPassword;
                this.mEntryErrMsgs[1] = getActivity().getString(2131628351);
            } else if (!TextUtils.isEmpty(password2)) {
                boolean isPassword1Valid = validatePassword(0);
                boolean isPassword2Valid = validatePassword(1);
                if (isPassword1Valid && isPassword2Valid) {
                    if (password1.equals(password2)) {
                        this.mUiStage = State.Finish;
                    } else {
                        this.mUiStage = State.NeedToConfirm;
                    }
                } else if (isPassword1Valid) {
                    this.mUiStage = State.ConfirmWrong;
                } else if (isPassword2Valid) {
                    this.mUiStage = State.CannotConfirm;
                } else {
                    this.mUiStage = State.InvalidPasswordBoth;
                }
            } else if (validatePassword(0)) {
                this.mUiStage = State.NeedToConfirm;
            } else if (entryOneHasNotFinished() && isPasswordOneTooShort(password1) && TextUtils.isEmpty(getErrMsgForPolicyNotMatch(password1))) {
                this.mEntryErrMsgs[0] = null;
                this.mUiStage = State.NeedToConfirm;
            } else {
                this.mUiStage = State.InvalidPassword;
            }
            Log.d("ChoosePasswordFragmentBase", "Current stage is: " + this.mUiStage);
            updateUi();
        }
    }

    private boolean entryOneHasNotFinished() {
        return this.mPasswordEntries[0].hasFocus();
    }

    protected boolean isPasswordOneTooShort(String password) {
        return password.length() < this.mPasswordMinLength;
    }

    protected boolean isPasswordOneTooLong(String password) {
        return password.length() > this.mPasswordMaxLength;
    }

    protected String getErrMsgForIllegalChar(String password) {
        return null;
    }

    protected String getErrMsgForPolicyNotMatch(String password) {
        return null;
    }

    private boolean validatePassword(int entryIndex) {
        String str = null;
        if (getActivity() == null || entryIndex > this.mPasswordEntries.length || !isAdded()) {
            return false;
        }
        String password = this.mPasswordEntries[entryIndex].getText().toString();
        boolean valid = true;
        switch (entryIndex) {
            case 0:
                String errMsg = getErrMsgForIllegalChar(password);
                if (!TextUtils.isEmpty(errMsg)) {
                    valid = false;
                } else if (isPasswordOneTooLong(password)) {
                    errMsg = getResources().getString(2131628349);
                    valid = false;
                } else if (isPasswordOneTooShort(password)) {
                    int i;
                    Resources resources = getResources();
                    if (this.mIsAlphaMode) {
                        i = 2131689532;
                    } else {
                        i = 2131689531;
                    }
                    errMsg = resources.getQuantityString(i, this.mPasswordMinLength, new Object[]{Integer.valueOf(this.mPasswordMinLength)});
                    valid = false;
                } else if (entryOneHasNotFinished()) {
                    errMsg = null;
                    valid = true;
                } else {
                    errMsg = getErrMsgForPolicyNotMatch(password);
                    if (!TextUtils.isEmpty(errMsg)) {
                        valid = false;
                    }
                }
                this.mEntryErrMsgs[0] = errMsg;
                break;
            case 1:
                valid = this.mPasswordEntries[0].getText().toString().startsWith(password);
                String[] strArr = this.mEntryErrMsgs;
                if (!valid) {
                    str = getActivity().getString(2131628351);
                }
                strArr[1] = str;
                break;
        }
        return valid;
    }

    private void changeFocusIfNeeded() {
        if (!this.hasEntryOneRequestChanged) {
            this.mPasswordEntries[0].requestFocus();
            this.hasEntryOneRequestChanged = true;
        }
    }

    private void updateUi() {
        if (State.InvalidPassword == this.mUiStage) {
            changeFocusIfNeeded();
        }
        if (State.Finish == this.mUiStage) {
            this.mNextButton.setEnabled(true);
        } else {
            this.mNextButton.setEnabled(false);
        }
        this.mPasswordEntries[0].setError(this.mEntryErrMsgs[0]);
        this.mPasswordEntries[1].setError(this.mEntryErrMsgs[1]);
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != 0 && actionId != 6 && actionId != 5) {
            return false;
        }
        handleNext();
        return true;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 2131886370:
                handleCancel();
                return;
            case 2131886371:
                handleNext();
                return;
            default:
                return;
        }
    }

    private void handleNext() {
        if (State.Finish == this.mUiStage) {
            doHandleNext(this.mPasswordEntries[0].getText().toString());
        }
    }

    public void doHandleNext(String password) {
    }

    public void handleCancel() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
