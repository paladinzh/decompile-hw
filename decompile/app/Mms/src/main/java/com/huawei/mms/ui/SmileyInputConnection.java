package com.huawei.mms.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.TextView;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditTextWithSmiley.SoftInputListener;
import java.lang.reflect.InvocationTargetException;

public class SmileyInputConnection extends BaseInputConnection {
    private SoftInputListener listener;
    protected DeleterWatcher mDeleterWatcher = null;
    private final TextView mTextView;

    public interface DeleterWatcher {
        boolean onEmptyDelete();
    }

    public void setAppendWatcher(DeleterWatcher watcher) {
        this.mDeleterWatcher = watcher;
    }

    public SmileyInputConnection(TextView textview) {
        super(textview, true);
        this.mTextView = textview;
    }

    public void setSoftInputListener(SoftInputListener l) {
        this.listener = l;
    }

    public Editable getEditable() {
        TextView tv = this.mTextView;
        if (tv != null) {
            return tv.getEditableText();
        }
        return null;
    }

    public boolean beginBatchEdit() {
        this.mTextView.beginBatchEdit();
        return true;
    }

    public boolean endBatchEdit() {
        this.mTextView.endBatchEdit();
        return true;
    }

    public boolean clearMetaKeyStates(int states) {
        Editable content = getEditable();
        if (content == null) {
            return false;
        }
        KeyListener kl = this.mTextView.getKeyListener();
        if (kl != null) {
            try {
                kl.clearMetaKeyState(this.mTextView, content, states);
            } catch (AbstractMethodError e) {
            }
        }
        return true;
    }

    public boolean commitCompletion(CompletionInfo text) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onCommitCompletion(text);
        this.mTextView.endBatchEdit();
        return true;
    }

    public boolean performEditorAction(int actionCode) {
        this.mTextView.onEditorAction(actionCode);
        return true;
    }

    public boolean performContextMenuAction(int id) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onTextContextMenuItem(id);
        this.mTextView.endBatchEdit();
        return true;
    }

    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        if (this.mTextView != null) {
            ExtractedText et = new ExtractedText();
            if (this.mTextView.extractText(request, et)) {
                if ((flags & 1) != 0) {
                    setExtracting(this.mTextView, request);
                }
                return et;
            }
        }
        return null;
    }

    public boolean performPrivateCommand(String action, Bundle data) {
        this.mTextView.onPrivateIMECommand(action, data);
        return true;
    }

    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (this.mTextView == null) {
            return super.commitText(text, newCursorPosition);
        }
        return super.commitText(SmileyParser.getInstance().addSmileySpans(text, SMILEY_TYPE.MESSAGE_EDITTEXT), newCursorPosition);
    }

    public boolean finishComposingText() {
        if (this.listener != null) {
            this.listener.onSoftInputHide();
        }
        return super.finishComposingText();
    }

    private void setExtracting(TextView view, ExtractedTextRequest request) {
        try {
            view.getClass().getMethod("setExtracting", new Class[]{ExtractedTextRequest.class}).invoke(view, new Object[]{request});
        } catch (NoSuchMethodException e) {
            MLog.e("CMInputConnection", "setExtracting NoSuchMethodException" + e);
        } catch (IllegalAccessException e2) {
            MLog.e("CMInputConnection", "setExtracting IllegalAccessException" + e2);
        } catch (IllegalArgumentException e3) {
            MLog.e("CMInputConnection", "setExtracting IllegalArgumentException" + e3);
        } catch (InvocationTargetException e4) {
            MLog.e("CMInputConnection", "setExtracting InvocationTargetException" + e4);
        } catch (Exception e5) {
            MLog.e("CMInputConnection", "setExtracting unknow Exception" + e5);
        }
    }

    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == 67 && event.getAction() == 0 && this.mDeleterWatcher != null && this.mDeleterWatcher.onEmptyDelete()) {
            return true;
        }
        return super.sendKeyEvent(event);
    }

    public boolean deleteSurroundingText(int leftLength, int rightLength) {
        if (leftLength == 1 && rightLength == 0) {
            CharSequence textStr = getTextBeforeCursor(1, 0);
            int len = 0;
            if (!TextUtils.isEmpty(textStr)) {
                len = textStr.length();
            }
            if (len == 0 && this.mDeleterWatcher != null) {
                this.mDeleterWatcher.onEmptyDelete();
            }
        }
        return super.deleteSurroundingText(leftLength, rightLength);
    }
}
