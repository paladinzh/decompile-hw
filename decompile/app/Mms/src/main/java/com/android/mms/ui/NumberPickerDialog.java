package com.android.mms.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {
    private final OnNumberSetListener mCallback;
    private TextView mInputTextHint;
    private int mMaxNumberSet = 0;
    private int mMinNumberSet = 0;
    private final EditText mNumberEditText;
    private final TextWatcher mNumberWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
            boolean z;
            Editable number = NumberPickerDialog.this.mNumberEditText.getText();
            boolean isNotValid = TextUtils.isEmpty(number) || !NumberPickerDialog.this.isValidNumber(number.toString());
            NumberPickerDialog.this.updateHintTextView(isNotValid);
            Button positiveBtn = NumberPickerDialog.this.getButton(-1);
            if (isNotValid) {
                z = false;
            } else {
                z = true;
            }
            positiveBtn.setEnabled(z);
        }
    };

    public interface OnNumberSetListener {
        void onNumberSet(int i);
    }

    public NumberPickerDialog(Context context, int theme, OnNumberSetListener callBack, int number, int rangeMin, int rangeMax, int title) {
        super(context, theme);
        this.mCallback = callBack;
        setTitle(title);
        setButton(-1, context.getText(R.string.set), this);
        setButton(-2, context.getText(R.string.no), (OnClickListener) null);
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.number_picker_dialog, null);
        setView(view);
        this.mNumberEditText = (EditText) view.findViewById(R.id.number_picker);
        this.mInputTextHint = (TextView) view.findViewById(R.id.txt_hint_messages);
        this.mMaxNumberSet = rangeMax;
        this.mMinNumberSet = rangeMin;
        this.mInputTextHint.setText(getContext().getResources().getString(R.string.input_number_invalid, new Object[]{Integer.valueOf(this.mMinNumberSet), Integer.valueOf(this.mMaxNumberSet)}));
        String numberStr = String.valueOf(number);
        boolean z = TextUtils.isEmpty(numberStr) || !isValidNumber(numberStr);
        updateHintTextView(z);
        this.mNumberEditText.setText(numberStr);
        this.mNumberEditText.setSelection(numberStr.length());
        this.mNumberEditText.addTextChangedListener(this.mNumberWatcher);
        getWindow().setSoftInputMode(5);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallback != null) {
            this.mNumberEditText.clearFocus();
            this.mCallback.onNumberSet(Integer.parseInt(this.mNumberEditText.getText().toString()));
            dialog.dismiss();
        }
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putString(HarassNumberUtil.NUMBER, this.mNumberEditText.getText().toString());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mNumberEditText.setText(savedInstanceState.getString(HarassNumberUtil.NUMBER));
    }

    private boolean isValidNumber(String number) {
        try {
            int iNum = Integer.parseInt(number.toString());
            if (iNum > this.mMaxNumberSet || iNum < this.mMinNumberSet) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MLog.e("Mms:NumberPickerDialog", "isValidNumber integer.valueOf change exception" + e);
            return false;
        }
    }

    private void updateHintTextView(boolean visibility) {
        if (visibility) {
            this.mInputTextHint.setVisibility(0);
        } else {
            this.mInputTextHint.setVisibility(4);
        }
    }
}
