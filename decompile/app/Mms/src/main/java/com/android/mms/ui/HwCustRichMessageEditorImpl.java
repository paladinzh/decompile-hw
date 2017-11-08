package com.android.mms.ui;

import android.telephony.SmsMessage;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import com.huawei.mms.ui.EditTextWithSmiley;

public class HwCustRichMessageEditorImpl extends HwCustRichMessageEditor {
    private static final String TAG = "HwCustRichMessageEditorImpl";
    private Toast mExceedMessageSizeToast = null;
    private EditTextWithSmiley mSmsEditorText = null;

    private class TextLengthFilter implements InputFilter {
        private int mMaxLength;

        public TextLengthFilter(EditTextWithSmiley editText, int max) {
            this.mMaxLength = max;
            HwCustRichMessageEditorImpl.this.mSmsEditorText = editText;
            if (HwCustRichMessageEditorImpl.this.mSmsEditorText != null) {
                HwCustRichMessageEditorImpl.this.mExceedMessageSizeToast = Toast.makeText(HwCustRichMessageEditorImpl.this.mSmsEditorText.getContext(), R.string.exceed_editor_size_limitation, 0);
            }
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (!(HwCustRichMessageEditorImpl.this.mSmsEditorText == null || !HwCustComposeMessageImpl.getCTNetworkRoaming(HwCustRichMessageEditorImpl.this.mSmsEditorText.getContext()) || HwCustRichMessageEditorImpl.this.mSmsEditorText.getText() == null)) {
                String editTextContent = HwCustRichMessageEditorImpl.this.mSmsEditorText.getText().toString();
                StringBuffer sbf = new StringBuffer();
                sbf.append(editTextContent).append(source);
                int sourceEncodingType = SmsMessage.calculateLength(sbf, false)[3];
                if (sourceEncodingType == 1) {
                    this.mMaxLength = HwCustMmsConfigImpl.getMaxCTRoamingMultipartSms7Bit();
                } else if (sourceEncodingType == 3) {
                    this.mMaxLength = HwCustMmsConfigImpl.getMaxCTRoamingMultipartSms16Bit();
                }
            }
            int keep = this.mMaxLength - (dest.length() - (dend - dstart));
            if (keep < end - start) {
                HwCustRichMessageEditorImpl.this.mExceedMessageSizeToast.show();
            }
            if (keep <= 0) {
                return "";
            }
            if (keep >= end - start) {
                return null;
            }
            return source.subSequence(start, start + keep);
        }
    }

    public InputFilter createInputFilter(EditTextWithSmiley editText, InputFilter inputFilter) {
        if (HwCustMmsConfigImpl.isCTRoamingMultipartSmsLimit()) {
            return new TextLengthFilter(editText, MmsConfig.getMaxTextLimit());
        }
        return inputFilter;
    }

    public boolean getSaveMmsEmailAdress() {
        return HwCustMmsConfigImpl.getSaveMmsEmailAdress();
    }
}
