package com.android.settings;

import android.content.Context;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class LinkifyUtils {

    public interface OnClickListener {
        void onClick();
    }

    private LinkifyUtils() {
    }

    public static boolean linkify(final Context context, TextView textView, StringBuilder text, final OnClickListener listener) {
        Context localContext = context;
        int beginIndex = text.indexOf("LINK_BEGIN");
        if (beginIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(beginIndex, "LINK_BEGIN".length() + beginIndex);
        int endIndex = text.indexOf("LINK_END");
        if (endIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(endIndex, "LINK_END".length() + endIndex);
        textView.setText(text.toString(), BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        ((Spannable) textView.getText()).setSpan(new ClickableSpan() {
            public void onClick(View widget) {
                if (listener != null) {
                    listener.onClick();
                }
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(context.getResources().getColor(2131427515));
            }
        }, beginIndex, endIndex, 33);
        return true;
    }

    public static StringBuilder deleteLink(StringBuilder text) {
        int beginIndex = text.indexOf("LINK_BEGIN");
        if (beginIndex == -1) {
            return text;
        }
        text.delete(beginIndex, text.length());
        return text;
    }
}
