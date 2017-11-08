package com.huawei.systemmanager.applock.password;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil.TimeKeeperSuffix;
import com.huawei.systemmanager.applock.utils.compatibility.EditTextUtil;
import com.huawei.systemmanager.applock.utils.compatibility.QuestionCompatibilityUtil;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeObserver;
import com.huawei.timekeeper.TimeTickInfo;

public class PasswordProtectVerifyFragment extends PasswordProtectFragmentBase implements TimeObserver {
    private EditText mAnswer = null;
    private RelativeLayout mLockoutLayout = null;
    private TextView mLockoutTextView = null;
    private TextView mQuestion = null;
    private TimeKeeper mTimeKeeper = null;

    protected int getProtectFragmentLayoutID() {
        return R.layout.app_lock_protection_verify_layout;
    }

    protected int getProtectFragmentTitle() {
        return R.string.applock_protect_verify_title;
    }

    protected int getStartButtonText() {
        return R.string.common_cancel;
    }

    protected int getEndButtonText() {
        return R.string.common_finish;
    }

    protected void startButtonClick() {
        getActivity().finish();
    }

    protected void endButtonClick() {
        if (AppLockPwdUtils.verifyPasswordProtection(this.mAppContext, this.mAnswer.getText().toString().trim())) {
            startActivity(new Intent(this.mAppContext, ResetPasswordAfterVerifyActivity.class));
            AuthRetryUtil.resetTimeKeeper(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_ANSWER);
            getActivity().finish();
            return;
        }
        int leftTime = this.mTimeKeeper.addErrorCount();
        if (leftTime == 0) {
            updateAnswerAndLockoutBtn(true);
            registerTimeKeeperCallback();
        } else {
            this.mAnswer.selectAll();
            this.mAnswer.setError(this.mAppContext.getResources().getQuantityString(R.plurals.applock_verify_answer_failed, leftTime, new Object[]{Integer.valueOf(leftTime)}));
        }
    }

    protected void initSubViews(View view) {
        this.mQuestion = (TextView) view.findViewById(R.id.app_lock_protect_verify_question);
        this.mAnswer = (EditText) view.findViewById(R.id.app_lock_protect_verify_answer);
        EditTextUtil.disableCopyAndPaste(this.mAnswer);
        this.mAnswer.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable arg0) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean z = false;
                PasswordProtectVerifyFragment.this.mAnswer.setError(null);
                if (!PasswordProtectVerifyFragment.this.isLockOut()) {
                    Button button = PasswordProtectVerifyFragment.this.mEndButton;
                    if (s.toString().trim().length() != 0) {
                        z = true;
                    }
                    button.setEnabled(z);
                }
            }
        });
        this.mLockoutLayout = (RelativeLayout) view.findViewById(R.id.app_lock_protect_verify_lockout_layout);
        this.mLockoutTextView = (TextView) view.findViewById(R.id.app_lock_protect_verify_lockout_textview);
    }

    public void onResume() {
        super.onResume();
        if (this.mTimeKeeper == null) {
            this.mTimeKeeper = AuthRetryUtil.getTimeKeeper(this.mAppContext, TimeKeeperSuffix.SUFFIX_APPLOCK_ANSWER);
        }
        checkAndStartLockout();
    }

    public void onPause() {
        unregisterTimeKeeperCallback();
        super.onPause();
    }

    public void onTimeTick(TimeTickInfo timeTickInfo) {
        this.mLockoutTextView.setText(AuthRetryUtil.getRemainingLockoutTime(this.mAppContext, timeTickInfo));
    }

    public void onTimeFinish() {
        this.mAnswer.setVisibility(0);
        this.mAnswer.setError(null);
        this.mAnswer.setText(null);
        this.mLockoutLayout.setVisibility(8);
        unregisterTimeKeeperCallback();
    }

    private boolean isLockOut() {
        if (this.mTimeKeeper.getRemainingChance() <= 0) {
            return true;
        }
        return false;
    }

    private void checkAndStartLockout() {
        boolean lockout = false;
        this.mQuestion.setText(QuestionCompatibilityUtil.getVerifyQuestion(this.mAppContext));
        if (this.mTimeKeeper.getRemainingChance() <= 0) {
            lockout = true;
        }
        updateAnswerAndLockoutBtn(lockout);
        if (lockout) {
            registerTimeKeeperCallback();
        }
    }

    private void updateAnswerAndLockoutBtn(boolean lockout) {
        int i;
        int i2 = 8;
        EditText editText = this.mAnswer;
        if (lockout) {
            i = 8;
        } else {
            i = 0;
        }
        editText.setVisibility(i);
        RelativeLayout relativeLayout = this.mLockoutLayout;
        if (lockout) {
            i2 = 0;
        }
        relativeLayout.setVisibility(i2);
        if (lockout) {
            this.mEndButton.setEnabled(false);
        }
    }

    private void registerTimeKeeperCallback() {
        if (this.mTimeKeeper != null) {
            this.mTimeKeeper.registerObserver(this);
        }
    }

    private void unregisterTimeKeeperCallback() {
        if (this.mTimeKeeper != null && this.mTimeKeeper.isObserverRegistered(this)) {
            this.mTimeKeeper.unregisterObserver(this);
        }
    }
}
