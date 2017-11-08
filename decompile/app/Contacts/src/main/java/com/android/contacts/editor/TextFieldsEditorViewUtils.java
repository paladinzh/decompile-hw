package com.android.contacts.editor;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;

public class TextFieldsEditorViewUtils extends HwCustTextFieldsEditorView {
    private int mNumMax = 20;
    private Toast mToast = null;

    private class AddNumLengthFilter implements InputFilter {
        private AddNumLengthFilter() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int keep = TextFieldsEditorViewUtils.this.mNumMax - (dest.length() - (dend - dstart));
            if (TextFieldsEditorViewUtils.this.mToast == null) {
                TextFieldsEditorViewUtils.this.mToast = Toast.makeText(TextFieldsEditorViewUtils.this.mContext, TextFieldsEditorViewUtils.this.mContext.getResources().getQuantityString(R.plurals.limit_sim_num_length_remind, TextFieldsEditorViewUtils.this.mNumMax, new Object[]{Integer.valueOf(TextFieldsEditorViewUtils.this.mNumMax)}), 0);
            } else {
                TextFieldsEditorViewUtils.this.mToast.setText(TextFieldsEditorViewUtils.this.mContext.getResources().getQuantityString(R.plurals.limit_sim_num_length_remind, TextFieldsEditorViewUtils.this.mNumMax, new Object[]{Integer.valueOf(TextFieldsEditorViewUtils.this.mNumMax)}));
            }
            if (keep <= 0) {
                TextFieldsEditorViewUtils.this.mToast.show();
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                keep += start;
                TextFieldsEditorViewUtils.this.mToast.show();
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    keep--;
                    if (keep == start) {
                        return "";
                    }
                }
                return source.subSequence(start, keep);
            }
        }
    }

    public TextFieldsEditorViewUtils(Context context) {
        super(context);
    }

    public void remindNameToast() {
        Toast.makeText(this.mContext, this.mContext.getString(R.string.limit_sim_name_length_remind), 0).show();
    }

    public int getSimNumLen() {
        HwLog.i("TextFieldsEditorView", "custom sim number length new contact store sim");
        return Systemex.getInt(this.mContext.getContentResolver(), "sim_number_length", 20);
    }

    public boolean showNumToast() {
        return true;
    }

    public InputFilter getNewNumFilter(int length) {
        this.mNumMax = length;
        return new AddNumLengthFilter();
    }
}
