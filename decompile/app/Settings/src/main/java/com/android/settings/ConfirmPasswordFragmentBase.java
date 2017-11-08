package com.android.settings;

import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.settings.TimeKeeperAdapter.TimerCallback;
import com.android.settings.TimeKeeperAdapter.TimerRemainMsg;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.ErrorTipTextLayout;

public class ConfirmPasswordFragmentBase extends OptionsMenuFragment implements OnClickListener, OnEditorActionListener, TextWatcher {
    TimerCallback mCallBack = new TimerCallback() {
        public void onTimeTick(TimerRemainMsg msg) {
            ConfirmPasswordFragmentBase.this.updateLockView(msg);
        }

        public void onTimeFinish() {
            ConfirmPasswordFragmentBase.this.updateViews(true);
        }
    };
    protected Button mCancelButton;
    protected Button mContinueButton;
    private ErrorTipTextLayout mErrorTextLayout;
    private InputMethodManager mImm;
    protected boolean mIsAlpha;
    private Button mLockView;
    private FrameLayout mNormalView;
    protected EditText mPasswordEntry;
    private CheckBox mShowPassword;
    private TimeKeeperAdapter mTimeKeeper;

    protected boolean isAlphaMode() {
        return true;
    }

    protected boolean needRestore() {
        return false;
    }

    protected String getTimeKeeperName() {
        return "lock_screen";
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTimeKeeper = new TimeKeeperAdapter(getActivity(), getTimeKeeperName(), needRestore());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int i;
        View view = inflater.inflate(2130968688, null);
        this.mIsAlpha = isAlphaMode();
        this.mContinueButton = (Button) view.findViewById(2131886371);
        this.mContinueButton.setOnClickListener(this);
        this.mContinueButton.setEnabled(false);
        this.mCancelButton = (Button) view.findViewById(2131886400);
        this.mCancelButton.setVisibility(0);
        this.mCancelButton.setOnClickListener(this);
        this.mNormalView = (FrameLayout) view.findViewById(2131886365);
        this.mPasswordEntry = (EditText) view.findViewById(2131886367);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setHint(getHint());
        EditText editText = this.mPasswordEntry;
        if (this.mIsAlpha) {
            i = 129;
        } else {
            i = 18;
        }
        editText.setInputType(i);
        this.mShowPassword = (CheckBox) view.findViewById(2131886368);
        this.mShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                int start = ConfirmPasswordFragmentBase.this.mPasswordEntry.getSelectionStart();
                int stop = ConfirmPasswordFragmentBase.this.mPasswordEntry.getSelectionEnd();
                if (checked) {
                    ConfirmPasswordFragmentBase.this.mPasswordEntry.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    ConfirmPasswordFragmentBase.this.mPasswordEntry.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                ConfirmPasswordFragmentBase.this.mPasswordEntry.setSelection(start, stop);
            }
        });
        this.mLockView = (Button) view.findViewById(2131886407);
        this.mErrorTextLayout = (ErrorTipTextLayout) view.findViewById(2131886366);
        this.mImm = (InputMethodManager) getActivity().getSystemService("input_method");
        getActivity().getWindow().setSoftInputMode(16);
        getActivity().setTitle(getTitle());
        return view;
    }

    protected int getTitle() {
        return 2131628108;
    }

    private String getHint() {
        if (this.mIsAlpha) {
            return getString(2131628343);
        }
        return getString(2131628344);
    }

    public void onResume() {
        super.onResume();
        updateViews(false);
    }

    public void onPause() {
        super.onPause();
        this.mTimeKeeper.unregisterObserver();
    }

    private void updateViews(boolean clearErrMsg) {
        if (this.mTimeKeeper.getRemainingChance() > 0) {
            showNormalView(clearErrMsg);
        } else {
            showLockView();
        }
    }

    private void showNormalView(boolean clearErrMsg) {
        boolean z = true;
        if (isAdded()) {
            this.mLockView.setVisibility(8);
            this.mNormalView.setVisibility(0);
            this.mPasswordEntry.setFocusable(true);
            this.mPasswordEntry.setFocusableInTouchMode(true);
            this.mPasswordEntry.requestFocus();
            this.mImm.showSoftInput(this.mPasswordEntry, 1);
            if (clearErrMsg) {
                this.mPasswordEntry.selectAll();
                hideError();
            }
            Button button = this.mContinueButton;
            if (this.mPasswordEntry.getText().toString().length() <= 0) {
                z = false;
            }
            button.setEnabled(z);
            this.mTimeKeeper.unregisterObserver();
            return;
        }
        Log.e("ConfirmPasswordFragment", "showNormalView, fragment is not added.");
    }

    private void showLockView() {
        if (isAdded()) {
            this.mPasswordEntry.setFocusable(false);
            this.mPasswordEntry.clearFocus();
            this.mImm.hideSoftInputFromWindow(this.mPasswordEntry.getWindowToken(), 2);
            this.mNormalView.setVisibility(8);
            this.mLockView.setVisibility(0);
            this.mContinueButton.setEnabled(false);
            this.mTimeKeeper.registerObserver(this.mCallBack);
            updateLockView(this.mTimeKeeper.getTimerRemainMsg());
            return;
        }
        Log.e("ConfirmPasswordFragment", "showLockView, fragment is not added.");
    }

    private void updateLockView(TimerRemainMsg msg) {
        if (this.mLockView == null) {
            Log.e("ConfirmPasswordFragment", "mLockView is null.");
        } else if (msg != null) {
            this.mLockView.setText(getResources().getQuantityString(msg.mStringResourceId, msg.mRemainTime, new Object[]{Integer.valueOf(msg.mRemainTime)}));
        }
    }

    protected int getMetricsCategory() {
        return 30;
    }

    private void handleNext() {
        String password = this.mPasswordEntry.getText().toString();
        if (!TextUtils.isEmpty(password) && !doCheckPassword(password)) {
            onPasswordChecked(false, null);
        }
    }

    protected boolean doCheckPassword(String password) {
        return false;
    }

    protected void passwordMatches(Intent data) {
        if (getActivity() != null) {
            getActivity().setResult(-1, data);
            HwCustSplitUtils splitter = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
            if (splitter.reachSplitSize()) {
                splitter.setExitWhenContentGone(false);
            }
            getActivity().finish();
        }
    }

    protected void onCancel() {
        if (getActivity() != null) {
            getActivity().setResult(0);
            getActivity().finish();
        }
    }

    protected void onPasswordChecked(boolean matched, Intent data) {
        if (matched) {
            passwordMatches(data);
            this.mTimeKeeper.resetErrorCount(getActivity());
            return;
        }
        int chanceLeft = this.mTimeKeeper.addErrorCount();
        if (chanceLeft <= 0) {
            updateViews(false);
            return;
        }
        this.mPasswordEntry.selectAll();
        showError(getResources().getQuantityString(2131689534, chanceLeft, new Object[]{Integer.valueOf(chanceLeft)}));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 2131886371:
                handleNext();
                return;
            case 2131886400:
                onCancel();
                return;
            default:
                return;
        }
    }

    private void hideError() {
        if (this.mErrorTextLayout != null) {
            this.mErrorTextLayout.setError(null);
        }
    }

    private void showError(String msg) {
        if (this.mErrorTextLayout != null) {
            this.mErrorTextLayout.setError(msg);
        }
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != 0 && actionId != 6 && actionId != 5) {
            return false;
        }
        handleNext();
        return true;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        boolean z = false;
        Button button = this.mContinueButton;
        if (this.mPasswordEntry.getText().length() > 0) {
            z = true;
        }
        button.setEnabled(z);
        hideError();
    }
}
