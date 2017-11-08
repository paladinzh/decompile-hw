package com.android.contacts.editor;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.R;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustTextFieldsEditorViewImpl extends HwCustTextFieldsEditorView {
    private int mNumMax = 20;
    private long oneTime = 0;
    private Toast toast = null;
    private long twoTime = 0;

    private class AddNumLengthFilter implements InputFilter {
        private AddNumLengthFilter() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int keep = HwCustTextFieldsEditorViewImpl.this.mNumMax - (dest.length() - (dend - dstart));
            if (keep <= 0) {
                Toast.makeText(HwCustTextFieldsEditorViewImpl.this.mContext, HwCustTextFieldsEditorViewImpl.this.mContext.getString(R.string.limit_sim_num_length_remind, new Object[]{Integer.valueOf(HwCustTextFieldsEditorViewImpl.this.mNumMax)}), 0).show();
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                keep += start;
                Toast.makeText(HwCustTextFieldsEditorViewImpl.this.mContext, HwCustTextFieldsEditorViewImpl.this.mContext.getString(R.string.limit_sim_num_length_remind, new Object[]{Integer.valueOf(HwCustTextFieldsEditorViewImpl.this.mNumMax)}), 0).show();
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

    public HwCustTextFieldsEditorViewImpl(Context context) {
        super(context);
    }

    public void remindNameToast() {
        if (!"true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_limit_name_length_remind"))) {
            return;
        }
        if (this.toast == null) {
            this.toast = Toast.makeText(this.mContext, this.mContext.getString(R.string.limit_sim_name_length_remind), 0);
            this.toast.show();
            this.oneTime = System.currentTimeMillis();
            return;
        }
        this.twoTime = System.currentTimeMillis();
        if (this.twoTime - this.oneTime > 0) {
            this.toast.show();
            this.oneTime = this.twoTime;
        }
    }

    public int getSimNumLen() {
        Log.i("TextFieldsEditorView", "custom sim number length new contact store sim");
        return Systemex.getInt(this.mContext.getContentResolver(), "sim_number_length", 20);
    }

    public boolean showNumToast() {
        return "true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_limit_number_length_remind"));
    }

    public InputFilter getNewNumFilter(int length) {
        this.mNumMax = length;
        return new AddNumLengthFilter();
    }
}
