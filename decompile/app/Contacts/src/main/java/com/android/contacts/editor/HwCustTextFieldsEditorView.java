package com.android.contacts.editor;

import android.content.Context;
import android.text.InputFilter;

public class HwCustTextFieldsEditorView {
    protected static final int DEFAULT_SIM_NUM_LENGTH = 20;
    protected static final String TAG = "TextFieldsEditorView";
    Context mContext;

    public HwCustTextFieldsEditorView(Context context) {
        this.mContext = context;
    }

    public int getSimNumLen() {
        return 20;
    }

    public boolean showNumToast() {
        return false;
    }

    public InputFilter getNewNumFilter(int length) {
        return null;
    }

    public void remindNameToast() {
    }
}
