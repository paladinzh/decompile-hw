package com.android.mms.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import com.android.mms.MmsConfig;
import com.android.mms.util.SignatureUtil;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.mms.ui.EditTextWithSmiley;
import huawei.android.widget.CounterTextLayout;

public class SignatureEditDialog extends AlertDialog implements OnClickListener {
    private CounterTextLayout mCounterSignatureEditError;
    EditTextWithSmiley mEditText;
    Preference mPreference;
    TextWatcher mSignatureTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(SignatureUtil.deleteNewlineSymbol(s.toString()))) {
                SignatureEditDialog.this.getButton(-1).setEnabled(false);
            } else {
                SignatureEditDialog.this.getButton(-1).setEnabled(true);
            }
        }
    };
    SmileyParser parser = SmileyParser.getInstance();

    public SignatureEditDialog(Context context, int theme, int title, Preference signatureEditText) {
        super(context, theme);
        this.mPreference = signatureEditText;
        initView(theme, title);
        getWindow().setSoftInputMode(37);
    }

    private void initView(int theme, int title) {
        setTitle(title);
        setButton(-1, getContext().getText(R.string.yes), this);
        setButton(-2, getContext().getText(R.string.no), (OnClickListener) null);
        View view = ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.signature_edit, null);
        this.mEditText = (EditTextWithSmiley) view.findViewById(R.id.signature_edit);
        this.mCounterSignatureEditError = (CounterTextLayout) view.findViewById(R.id.signature_edit_counter_error);
        this.mCounterSignatureEditError.setMaxLength(50);
        this.mEditText.addTextChangedListener(this.mSignatureTextWatcher);
        this.mEditText.requestFocus();
        setView(view);
    }

    public void show() {
        super.show();
        String signatureString = SignatureUtil.getSignature(getContext(), MmsConfig.getDefaultSignatureText());
        CharSequence text = this.parser.addSmileySpans(signatureString, SMILEY_TYPE.MESSAGE_EDITTEXT);
        if (signatureString != null) {
            this.mEditText.setText(text);
            this.mEditText.setSelection(text.length());
            return;
        }
        this.mEditText.setText("");
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            String signatureString = SignatureUtil.deleteNewlineSymbol(this.mEditText.getText().toString());
            SignatureUtil.putSignature(getContext(), signatureString);
            this.mPreference.setSummary(this.parser.addSmileySpans(signatureString, SMILEY_TYPE.LIST_TEXTVIEW));
        }
    }
}
