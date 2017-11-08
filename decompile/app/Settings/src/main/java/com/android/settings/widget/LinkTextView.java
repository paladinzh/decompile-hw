package com.android.settings.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class LinkTextView extends TextView {
    private LinkAccessibilityHelper mAccessibilityHelper;

    public LinkTextView(Context context) {
        this(context, null);
    }

    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAccessibilityHelper = new LinkAccessibilityHelper(this);
        setAccessibilityDelegate(this.mAccessibilityHelper);
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if ((text instanceof Spanned) && ((ClickableSpan[]) ((Spanned) text).getSpans(0, text.length(), ClickableSpan.class)).length > 0) {
            setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    protected boolean dispatchHoverEvent(@NonNull MotionEvent event) {
        if (this.mAccessibilityHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }
}
