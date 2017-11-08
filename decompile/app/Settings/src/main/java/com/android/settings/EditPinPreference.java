package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

class EditPinPreference extends CustomEditTextPreference implements TextWatcher {
    private String errorMessage;
    private String mEditTextHint;
    private OnPinEnteredListener mPinListener;

    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference editPinPreference, boolean z);
    }

    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        this.mPinListener = listener;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        showOrHideErrorText((TextView) view.findViewById(16908299));
        EditText editText = (EditText) view.findViewById(16908291);
        if (editText != null) {
            editText.setInputType(18);
            editText.setFilters(new InputFilter[]{new LengthFilter(8)});
            editText.addTextChangedListener(this);
            editText.setHint(this.mEditTextHint);
        }
    }

    private void showOrHideErrorText(TextView message) {
        if (this.errorMessage == null || this.errorMessage.length() <= 0) {
            message.setVisibility(8);
            return;
        }
        message.setText(this.errorMessage);
        message.setVisibility(0);
    }

    public void setErrorText(String error) {
        this.errorMessage = error;
    }

    public boolean isDialogOpen() {
        Dialog dialog = getDialog();
        return dialog != null ? dialog.isShowing() : false;
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (this.mPinListener != null) {
            this.mPinListener.onPinEntered(this, positiveResult);
        }
    }

    public void showPinDialog() {
        Dialog dialog = getDialog();
        setDialogPositiveButtonEnabled(dialog);
        if (dialog == null || !dialog.isShowing()) {
            onClick();
        }
    }

    public void setEditTextHint(String message) {
        this.mEditTextHint = message;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        setDialogPositiveButtonEnabled(getDialog());
    }

    private void setDialogPositiveButtonEnabled(Dialog d) {
        if ((d instanceof AlertDialog) && getEditText() != null) {
            ((AlertDialog) d).getButton(-1).setEnabled(getEditText().length() >= 4);
        }
    }

    public void onDialogFragmentStart() {
        super.onDialogFragmentStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && getEditText() != null) {
            Button button = dialog.getButton(-1);
            if (button != null) {
                boolean z;
                if (getEditText().length() >= 4) {
                    z = true;
                } else {
                    z = false;
                }
                button.setEnabled(z);
            }
        }
    }
}
