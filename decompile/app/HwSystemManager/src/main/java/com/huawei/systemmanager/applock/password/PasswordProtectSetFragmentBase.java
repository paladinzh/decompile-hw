package com.huawei.systemmanager.applock.password;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.compatibility.EditTextUtil;
import com.huawei.systemmanager.applock.utils.compatibility.QuestionCompatibilityUtil;
import com.huawei.systemmanager.applock.utils.compatibility.QuestionItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Timer;
import java.util.TimerTask;

public abstract class PasswordProtectSetFragmentBase extends PasswordProtectFragmentBase {
    private static final String TAG = "PasswordProtectSetFragmentBase";
    private EditText mAnswerEditText = null;
    private AlertDialog mCustomDialog = null;
    private EditText mCustomEditText = null;
    private QuestionArrayAdapter mQuestionAdapter = null;
    private Spinner mSpinner = null;
    private String mTempCustomQuestion = null;

    private static class QuestionArrayAdapter extends ArrayAdapter<QuestionItem> {
        public QuestionArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        public QuestionArrayAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public long getItemId(int position) {
            return (long) ((QuestionItem) getItem(position)).questionIndex();
        }
    }

    protected int getProtectFragmentLayoutID() {
        return R.layout.app_lock_protection_set_layout;
    }

    protected int getEndButtonText() {
        return R.string.common_finish;
    }

    protected void initSubViews(View view) {
        this.mSpinner = (Spinner) view.findViewById(R.id.app_lock_password_question_spinner);
        this.mAnswerEditText = (EditText) view.findViewById(R.id.app_lock_password_answer);
        EditTextUtil.disableCopyAndPaste(this.mAnswerEditText);
        this.mAnswerEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable arg0) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean z = false;
                PasswordProtectSetFragmentBase.this.mAnswerEditText.setError(null);
                Button button = PasswordProtectSetFragmentBase.this.mEndButton;
                if (s.toString().trim().length() != 0) {
                    z = true;
                }
                button.setEnabled(z);
            }
        });
        this.mQuestionAdapter = new QuestionArrayAdapter(this.mAppContext, R.layout.multi_spinner_dropdown_item, 16908308);
        this.mSpinner.setAdapter(this.mQuestionAdapter);
        this.mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                HwLog.w(PasswordProtectSetFragmentBase.TAG, "onItemSelected position: " + position + ", id: " + id + ", itemId: " + PasswordProtectSetFragmentBase.this.mSpinner.getAdapter().getItemId(position));
                if (100 == id) {
                    PasswordProtectSetFragmentBase.this.showCustomDialog();
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void onResume() {
        super.onResume();
        if (this.mQuestionAdapter.isEmpty()) {
            reloadQuestionAdapter();
        }
    }

    public void onPause() {
        if (this.mCustomDialog != null) {
            this.mCustomDialog.dismiss();
            this.mCustomDialog = null;
        }
        super.onPause();
    }

    protected void setQuestionAndAnswer() {
        int questionIdx = (int) this.mSpinner.getSelectedItemId();
        String answer = this.mAnswerEditText.getText().toString();
        if (-1 == questionIdx && !TextUtils.isEmpty(this.mTempCustomQuestion)) {
            AppLockPwdUtils.setCustomProtectionQuestion(this.mAppContext, this.mTempCustomQuestion);
        }
        AppLockPwdUtils.setQuestionAndAnswer(this.mAppContext, questionIdx, answer);
    }

    private void showCustomDialog() {
        LayoutInflater factory = LayoutInflater.from(this.mAppContext);
        final Timer timer = new Timer();
        View customView = factory.inflate(R.layout.app_lock_custom_question, null);
        this.mCustomDialog = new Builder(getActivity()).setTitle(R.string.applock_protect_question_Custom).setView(customView).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PasswordProtectSetFragmentBase.this.mTempCustomQuestion = PasswordProtectSetFragmentBase.this.mCustomEditText.getText().toString().trim();
                PasswordProtectSetFragmentBase.this.reloadQuestionAdapter();
            }
        }).setNegativeButton(17039360, null).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                timer.cancel();
                PasswordProtectSetFragmentBase.this.reloadQuestionAdapter();
            }
        }).show();
        this.mCustomEditText = (EditText) customView.findViewById(R.id.custom_question);
        EditTextUtil.disableCopyAndPaste(this.mCustomEditText);
        timer.schedule(new TimerTask() {
            public void run() {
                ((InputMethodManager) PasswordProtectSetFragmentBase.this.mAppContext.getSystemService("input_method")).showSoftInput(PasswordProtectSetFragmentBase.this.mCustomEditText, 0);
            }
        }, 300);
        this.mCustomDialog.getButton(-1).setEnabled(false);
        this.mCustomEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean z = false;
                Button button = PasswordProtectSetFragmentBase.this.mCustomDialog.getButton(-1);
                if (s.toString().trim().length() != 0) {
                    z = true;
                }
                button.setEnabled(z);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void reloadQuestionAdapter() {
        this.mQuestionAdapter.clear();
        this.mQuestionAdapter.addAll(QuestionCompatibilityUtil.getProtectionQuestionList(this.mAppContext, this.mTempCustomQuestion));
        if (!this.mQuestionAdapter.isEmpty()) {
            this.mSpinner.setSelection(0);
        }
        this.mQuestionAdapter.notifyDataSetChanged();
    }
}
