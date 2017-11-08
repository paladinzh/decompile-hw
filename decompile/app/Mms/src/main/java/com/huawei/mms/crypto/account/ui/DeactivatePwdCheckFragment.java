package com.huawei.mms.crypto.account.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

@SuppressLint({"ValidFragment"})
public class DeactivatePwdCheckFragment extends PwdCheckFragment {
    private EditText mAccountNameView;
    private EditText mPassWordView;

    private class AccountTextWatcher implements TextWatcher {
        private AccountTextWatcher() {
        }

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!(DeactivatePwdCheckFragment.this.mDialog == null || DeactivatePwdCheckFragment.this.mAccountNameView == null || DeactivatePwdCheckFragment.this.mPassWordView == null || !(DeactivatePwdCheckFragment.this.mDialog instanceof AlertDialog))) {
                if (TextUtils.isEmpty(DeactivatePwdCheckFragment.this.mAccountNameView.getText().toString()) || TextUtils.isEmpty(DeactivatePwdCheckFragment.this.mPassWordView.getText().toString())) {
                    ((AlertDialog) DeactivatePwdCheckFragment.this.mDialog).getButton(-1).setEnabled(false);
                } else {
                    ((AlertDialog) DeactivatePwdCheckFragment.this.mDialog).getButton(-1).setEnabled(true);
                }
            }
        }
    }

    public DeactivatePwdCheckFragment(String accountName) {
        super(accountName);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MLog.d("DeactivatePwdCheckFragment", "onCreateDialog");
        View view = View.inflate(getActivity(), R.layout.deactivate_pwd_check_dialog, null);
        initPwdCheckView(view);
        ((TextView) view.findViewById(R.id.unbind_tip)).setText(String.format(getString(R.string.already_bind_tip), new Object[]{this.mAccountName}));
        this.mAccountNameView = (EditText) view.findViewById(R.id.input_account);
        this.mPassWordView = (EditText) view.findViewById(R.id.input_password);
        this.mAccountNameView.addTextChangedListener(new AccountTextWatcher());
        this.mPassWordView.addTextChangedListener(new AccountTextWatcher());
        Dialog dialog = new Builder(getActivity()).setTitle(R.string.bind_account_failed).setView(view).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                DeactivatePwdCheckFragment.this.setDialogCanDismiss(false);
                if (DeactivatePwdCheckFragment.this.isUserNameWrong()) {
                    MLog.d("DeactivatePwdCheckFragment", "user name is wrong");
                    DeactivatePwdCheckFragment.this.showAccountNameInvalidDialog();
                } else if (DeactivatePwdCheckFragment.this.isPwdInvalid()) {
                    MLog.d("DeactivatePwdCheckFragment", "password is invalid");
                } else {
                    if (DeactivatePwdCheckFragment.this.mPwdCheckListener != null) {
                        DeactivatePwdCheckFragment.this.mPwdCheckListener.onPwdVerifyStart(DeactivatePwdCheckFragment.this.mAccountNameView.getText().toString(), DeactivatePwdCheckFragment.this.mPwdEdit.getText().toString());
                    }
                }
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (DeactivatePwdCheckFragment.this.mDialog != null) {
                    DeactivatePwdCheckFragment.this.mDialog.dismiss();
                }
                DeactivatePwdCheckFragment.this.getActivity().finish();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        this.mDialog = dialog;
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                if (DeactivatePwdCheckFragment.this.mDialog != null) {
                    Button btn = ((AlertDialog) DeactivatePwdCheckFragment.this.mDialog).getButton(-1);
                    if (btn != null) {
                        btn.setEnabled(false);
                    }
                }
            }
        });
        return dialog;
    }

    private void showAccountNameInvalidDialog() {
        Builder builder = new Builder(getActivity());
        builder.setMessage(R.string.account_name_error_tip_message);
        builder.setTitle(R.string.CS_prompt_dialog_title);
        builder.setPositiveButton(17039370, null);
        builder.show();
    }

    private boolean isUserNameWrong() {
        if (this.mAccountNameView == null) {
            MLog.e("DeactivatePwdCheckFragment", "isUserNameWrong, mAccountNameView is null");
            return true;
        } else if (TextUtils.isEmpty(this.mAccountNameView.getText().toString())) {
            return true;
        } else {
            return false;
        }
    }
}
