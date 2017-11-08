package com.android.settings.widget;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import java.util.List;

public class LinkAccessibilityHelper extends ExploreByTouchHelper {
    private final Rect mTempRect = new Rect();
    private final TextView mView;

    public LinkAccessibilityHelper(TextView view) {
        super(view);
        this.mView = view;
    }

    protected int getVirtualViewAt(float x, float y) {
        CharSequence text = this.mView.getText();
        if (text instanceof Spanned) {
            Spanned spannedText = (Spanned) text;
            int offset = this.mView.getOffsetForPosition(x, y);
            ClickableSpan[] linkSpans = (ClickableSpan[]) spannedText.getSpans(offset, offset, ClickableSpan.class);
            if (linkSpans.length == 1) {
                return spannedText.getSpanStart(linkSpans[0]);
            }
        }
        return Integer.MIN_VALUE;
    }

    protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
        int i = 0;
        CharSequence text = this.mView.getText();
        if (text instanceof Spanned) {
            Spanned spannedText = (Spanned) text;
            ClickableSpan[] linkSpans = (ClickableSpan[]) spannedText.getSpans(0, spannedText.length(), ClickableSpan.class);
            int length = linkSpans.length;
            while (i < length) {
                virtualViewIds.add(Integer.valueOf(spannedText.getSpanStart(linkSpans[i])));
                i++;
            }
        }
    }

    protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
        ClickableSpan span = getSpanForOffset(virtualViewId);
        if (span != null) {
            event.setContentDescription(getTextForSpan(span));
            return;
        }
        Log.e("LinkAccessibilityHelper", "ClickableSpan is null for offset: " + virtualViewId);
        event.setContentDescription(this.mView.getText());
    }

    protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo info) {
        ClickableSpan span = getSpanForOffset(virtualViewId);
        if (span != null) {
            info.setContentDescription(getTextForSpan(span));
        } else {
            Log.e("LinkAccessibilityHelper", "ClickableSpan is null for offset: " + virtualViewId);
            info.setContentDescription(this.mView.getText());
        }
        info.setFocusable(true);
        info.setClickable(true);
        getBoundsForSpan(span, this.mTempRect);
        if (this.mTempRect.isEmpty()) {
            Log.e("LinkAccessibilityHelper", "LinkSpan bounds is empty for: " + virtualViewId);
            this.mTempRect.set(0, 0, 1, 1);
            info.setBoundsInParent(this.mTempRect);
        } else {
            info.setBoundsInParent(getBoundsForSpan(span, this.mTempRect));
        }
        info.addAction(16);
    }

    protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
        if (action == 16) {
            ClickableSpan span = getSpanForOffset(virtualViewId);
            if (span != null) {
                span.onClick(this.mView);
                return true;
            }
            Log.e("LinkAccessibilityHelper", "LinkSpan is null for offset: " + virtualViewId);
        }
        return false;
    }

    private ClickableSpan getSpanForOffset(int offset) {
        CharSequence text = this.mView.getText();
        if (text instanceof Spanned) {
            ClickableSpan[] spans = (ClickableSpan[]) ((Spanned) text).getSpans(offset, offset, ClickableSpan.class);
            if (spans.length == 1) {
                return spans[0];
            }
        }
        return null;
    }

    private CharSequence getTextForSpan(ClickableSpan span) {
        CharSequence text = this.mView.getText();
        if (!(text instanceof Spanned)) {
            return text;
        }
        Spanned spannedText = (Spanned) text;
        return spannedText.subSequence(spannedText.getSpanStart(span), spannedText.getSpanEnd(span));
    }

    private Rect getBoundsForSpan(ClickableSpan span, Rect outRect) {
        CharSequence text = this.mView.getText();
        outRect.setEmpty();
        if (text instanceof Spanned) {
            Spanned spannedText = (Spanned) text;
            int spanStart = spannedText.getSpanStart(span);
            int spanEnd = spannedText.getSpanEnd(span);
            Layout layout = this.mView.getLayout();
            float xStart = layout.getPrimaryHorizontal(spanStart);
            float xEnd = layout.getPrimaryHorizontal(spanEnd);
            int lineStart = layout.getLineForOffset(spanStart);
            int lineEnd = layout.getLineForOffset(spanEnd);
            layout.getLineBounds(lineStart, outRect);
            outRect.left = (int) xStart;
            if (lineEnd == lineStart) {
                outRect.right = (int) xEnd;
            }
            outRect.offset(this.mView.getTotalPaddingLeft(), this.mView.getTotalPaddingTop());
        }
        return outRect;
    }
}
