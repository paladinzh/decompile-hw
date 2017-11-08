package com.huawei.mms.ui;

import android.content.Context;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class CustEditText extends EditText {
    private OnKeyListener mOnKeyListener;

    public CustEditText(Context context) {
        super(context);
    }

    public CustEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        super.onCreateInputConnection(outAttrs);
        SmileyInputConnection ic = new SmileyInputConnection(this);
        outAttrs.initialSelStart = Selection.getSelectionStart(getEditableText());
        outAttrs.initialSelEnd = Selection.getSelectionEnd(getEditableText());
        outAttrs.initialCapsMode = ic.getCursorCapsMode(getInputType());
        return ic;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.mOnKeyListener != null) {
            this.mOnKeyListener.onKey(this, keyCode, event);
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setCustOnKeyListener(OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
    }

    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }
}
