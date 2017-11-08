package com.huawei.mms.crypto.account.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

@SuppressLint({"ValidFragment"})
public class PwdCheckFragment extends CheckFragment {
    protected String mAccountName;
    private boolean mDisplayPwd = false;
    private TextView mDisplayPwdText;
    protected PwdCheckListener mPwdCheckListener;
    protected EditText mPwdEdit;

    public PwdCheckFragment(String accountName) {
        this.mAccountName = accountName;
    }

    private void updatePwdInputType() {
        boolean z = false;
        MLog.i("PwdCheckFragment", "updatePwdInputType");
        if (this.mPwdEdit == null || this.mDisplayPwdText == null) {
            MLog.e("PwdCheckFragment", "updatePwdInputType, error params");
            return;
        }
        if (!this.mDisplayPwd) {
            z = true;
        }
        this.mDisplayPwd = z;
        int index = this.mPwdEdit.getSelectionStart();
        if (this.mDisplayPwd) {
            this.mPwdEdit.setInputType(145);
            this.mDisplayPwdText.setBackgroundResource(R.drawable.cloud_backup);
        } else {
            this.mPwdEdit.setInputType(129);
            this.mDisplayPwdText.setBackgroundResource(R.drawable.chat_body_input_box_on);
        }
        if (index >= 0) {
            this.mPwdEdit.setSelection(index);
        }
    }

    public void onAttach(Activity activity) {
        MLog.i("PwdCheckFragment", "onAttach");
        try {
            this.mPwdCheckListener = (PwdCheckListener) activity;
        } catch (Exception e) {
            MLog.e("PwdCheckFragment", "onAttach exception", (Throwable) e);
        }
        super.onAttach(activity);
    }

    protected boolean isPwdInvalid() {
        if (this.mPwdEdit == null) {
            return true;
        }
        String pwd = this.mPwdEdit.getText().toString();
        CharSequence error = this.mPwdEdit.getError();
        if (TextUtils.isEmpty(pwd) || !TextUtils.isEmpty(error)) {
            return true;
        }
        return false;
    }

    public void handlePwdError() {
        this.mPwdEdit.setError(getString(R.string.CS_error_login_pwd_message));
        this.mPwdEdit.selectAll();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.pwd_check_dialog, null);
        ((TextView) view.findViewById(R.id.user_name)).setText(this.mAccountName);
        initPwdCheckView(view);
        Dialog dialog = new Builder(getActivity()).setTitle(R.string.CS_input_password_title).setView(view).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                PwdCheckFragment.this.setDialogCanDismiss(false);
                if (PwdCheckFragment.this.isPwdInvalid()) {
                    MLog.d("PwdCheckFragment", "password is invalid");
                    return;
                }
                if (PwdCheckFragment.this.mPwdCheckListener != null) {
                    PwdCheckFragment.this.mPwdCheckListener.onPwdVerifyStart(PwdCheckFragment.this.mPwdEdit.getText().toString());
                }
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (PwdCheckFragment.this.mDialog != null) {
                    PwdCheckFragment.this.mDialog.dismiss();
                }
                PwdCheckFragment.this.getActivity().finish();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        this.mDialog = dialog;
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                if (PwdCheckFragment.this.mDialog != null) {
                    Button btn = ((AlertDialog) PwdCheckFragment.this.mDialog).getButton(-1);
                    if (btn != null) {
                        btn.setEnabled(false);
                    }
                }
            }
        });
        return dialog;
    }

    protected void initPwdCheckView(View view) {
        ((TextView) view.findViewById(R.id.forget_pwd)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (PwdCheckFragment.this.mPwdCheckListener != null) {
                    PwdCheckFragment.this.mPwdCheckListener.onForgetPwd();
                }
            }
        });
        this.mPwdEdit = (EditText) view.findViewById(R.id.input_password);
        this.mPwdEdit.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void afterTextChanged(Editable arg0) {
                if (!(PwdCheckFragment.this.mDialog == null || PwdCheckFragment.this.mPwdEdit == null || !(PwdCheckFragment.this.mDialog instanceof AlertDialog))) {
                    if (TextUtils.isEmpty(PwdCheckFragment.this.mPwdEdit.getText().toString())) {
                        ((AlertDialog) PwdCheckFragment.this.mDialog).getButton(-1).setEnabled(false);
                    } else {
                        ((AlertDialog) PwdCheckFragment.this.mDialog).getButton(-1).setEnabled(true);
                    }
                }
            }
        });
        this.mDisplayPwdText = (TextView) view.findViewById(R.id.display_pass);
        ((LinearLayout) view.findViewById(R.id.display_pass_layout)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                PwdCheckFragment.this.updatePwdInputType();
            }
        });
    }

    public void onPause() {
        MLog.i("PwdCheckFragment", "onPause");
        setDialogCanDismiss(true);
        super.onPause();
    }

    public void onDestroyView() {
        MLog.i("PwdCheckFragment", "onDestroyView");
        setDialogCanDismiss(true);
        if (this.mDialog != null) {
            this.mDialog.dismiss();
        }
        super.onDestroyView();
    }

    public void onDismiss(DialogInterface dialog) {
        MLog.i("PwdCheckFragment", "onDismiss");
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        MLog.i("PwdCheckFragment", "onCancel");
        getActivity().finish();
        super.onCancel(dialog);
    }
}
