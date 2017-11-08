package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import com.android.mms.ui.HwCustEditTextWithSmiley;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.SmileyInputConnection.DeleterWatcher;

public class EditTextWithSmiley extends EditText {
    private Editable editable;
    private SoftInputListener listener;
    Runnable mEmptyDeleter;
    private HwCustEditTextWithSmiley mHwCustEditTextWithSmiley;
    private SmileyParser parser;

    private class SmileyAsyncTask extends AsyncTask<Object, Void, CharSequence> {
        private int mMax;
        private int mMin;
        private SpannableStringBuilder mNewText;
        private CharSequence mText;

        private SmileyAsyncTask() {
        }

        protected CharSequence doInBackground(Object... params) {
            this.mText = (CharSequence) params[0];
            this.mMin = ((Integer) params[1]).intValue();
            this.mMax = ((Integer) params[2]).intValue();
            CharSequence text = EditTextWithSmiley.this.parser.addSmileySpans(this.mText, SMILEY_TYPE.MESSAGE_EDITTEXT);
            this.mNewText = new SpannableStringBuilder(EditTextWithSmiley.this.getText());
            this.mNewText.replace(this.mMin, this.mMax, text);
            return text;
        }

        protected void onPostExecute(CharSequence result) {
            if (!EditTextWithSmiley.this.isAttachedToWindow() || result == null) {
                MLog.e("CustomEditText", "onPostExecute:: the result is null or view is not attachedToWindow!!");
                return;
            }
            try {
                EditTextWithSmiley.this.setText(this.mNewText);
                EditTextWithSmiley.this.editable = EditTextWithSmiley.this.getText();
                int index = this.mMin + this.mText.length();
                if (EditTextWithSmiley.this.editable.length() >= index) {
                    EditTextWithSmiley.this.setSelection(index);
                } else {
                    EditTextWithSmiley.this.setSelection(EditTextWithSmiley.this.editable.length());
                }
            } catch (IndexOutOfBoundsException e) {
                MLog.e("CustomEditText", "paste exception: min: " + this.mMin + " ;max: " + this.mMax + " ;result: *****");
            }
        }
    }

    public interface SoftInputListener {
        void onSoftInputHide();
    }

    public EditTextWithSmiley(Context context) {
        super(context);
        this.mHwCustEditTextWithSmiley = (HwCustEditTextWithSmiley) HwCustUtils.createObj(HwCustEditTextWithSmiley.class, new Object[0]);
        this.mEmptyDeleter = null;
        init();
    }

    public EditTextWithSmiley(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHwCustEditTextWithSmiley = (HwCustEditTextWithSmiley) HwCustUtils.createObj(HwCustEditTextWithSmiley.class, new Object[0]);
        this.mEmptyDeleter = null;
        init();
    }

    public EditTextWithSmiley(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mHwCustEditTextWithSmiley = (HwCustEditTextWithSmiley) HwCustUtils.createObj(HwCustEditTextWithSmiley.class, new Object[0]);
        this.mEmptyDeleter = null;
        init();
    }

    private void init() {
        this.parser = SmileyParser.getInstance();
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (this.mHwCustEditTextWithSmiley != null && this.mHwCustEditTextWithSmiley.isDisableSmileyInputConnection()) {
            return super.onCreateInputConnection(outAttrs);
        }
        super.onCreateInputConnection(outAttrs);
        SmileyInputConnection ic = new SmileyInputConnection(this);
        ic.setSoftInputListener(this.listener);
        outAttrs.initialSelStart = Selection.getSelectionStart(getEditableText());
        outAttrs.initialSelEnd = Selection.getSelectionEnd(getEditableText());
        outAttrs.initialCapsMode = ic.getCursorCapsMode(getInputType());
        ic.setAppendWatcher(new DeleterWatcher() {
            public boolean onEmptyDelete() {
                return EditTextWithSmiley.this.doEmptyDelete();
            }
        });
        return ic;
    }

    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (IndexOutOfBoundsException e) {
            if (text != null) {
                setText(text.toString());
            }
        }
    }

    public boolean onTextContextMenuItem(int id) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        this.editable = getText();
        if (!isFocused()) {
            selStart = 0;
            selEnd = this.editable.length();
        }
        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        boolean isPasteEvent = false;
        switch (id) {
            case 16908322:
                if (((ClipboardManager) getContext().getSystemService("clipboard")).getText() != null) {
                    new SmileyAsyncTask().execute(new Object[]{text, Integer.valueOf(min), Integer.valueOf(max)});
                }
                ((InputMethodManager) getContext().getSystemService("input_method")).showSoftInput(this, 0);
                isPasteEvent = true;
                break;
        }
        if (isPasteEvent) {
            return true;
        }
        return super.onTextContextMenuItem(id);
    }

    public void setEmptyDeleter(Runnable deleter) {
        this.mEmptyDeleter = deleter;
    }

    public boolean doEmptyDelete() {
        if (this.mEmptyDeleter == null || getSelectionEnd() - getSelectionStart() > 0) {
            return false;
        }
        if (getSelectionStart() != 0 && getText().length() != 0) {
            return false;
        }
        this.mEmptyDeleter.run();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
