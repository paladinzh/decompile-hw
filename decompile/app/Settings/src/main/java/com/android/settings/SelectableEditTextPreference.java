package com.android.settings;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class SelectableEditTextPreference extends CustomEditTextPreference {
    private int mSelectionMode;

    public SelectableEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setInitialSelectionMode(int selectionMode) {
        this.mSelectionMode = selectionMode;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        EditText editText = getEditText(view);
        int length = editText.getText() != null ? editText.getText().length() : 0;
        if (!TextUtils.isEmpty(editText.getText())) {
            switch (this.mSelectionMode) {
                case 0:
                    editText.setSelection(length);
                    return;
                case 1:
                    editText.setSelection(0);
                    return;
                case 2:
                    editText.setSelection(0, length);
                    return;
                default:
                    return;
            }
        }
    }
}
