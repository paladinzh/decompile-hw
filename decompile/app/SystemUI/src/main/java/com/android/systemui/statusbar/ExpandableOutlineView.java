package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.android.systemui.R;

public abstract class ExpandableOutlineView extends ExpandableView {
    private boolean mCustomOutline;
    private float mOutlineAlpha = -1.0f;
    private final Rect mOutlineRect = new Rect();
    ViewOutlineProvider mProvider = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            int translation = (int) ExpandableOutlineView.this.getTranslation();
            if (ExpandableOutlineView.this.mCustomOutline) {
                outline.setRoundRect(ExpandableOutlineView.this.mOutlineRect, (float) ExpandableOutlineView.this.getResources().getDimensionPixelSize(R.dimen.hw_notification_card_radius));
            } else {
                outline.setRoundRect(translation, ExpandableOutlineView.this.mClipTopAmount, ExpandableOutlineView.this.getWidth() + translation, Math.max(ExpandableOutlineView.this.getActualHeight(), ExpandableOutlineView.this.mClipTopAmount), (float) ExpandableOutlineView.this.getResources().getDimensionPixelSize(R.dimen.hw_notification_card_radius));
            }
            outline.setAlpha(ExpandableOutlineView.this.mOutlineAlpha);
        }
    };

    public ExpandableOutlineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOutlineProvider(this.mProvider);
    }

    public void setActualHeight(int actualHeight, boolean notifyListeners) {
        super.setActualHeight(actualHeight, notifyListeners);
        invalidateOutline();
    }

    public void setClipTopAmount(int clipTopAmount) {
        super.setClipTopAmount(clipTopAmount);
        invalidateOutline();
    }

    protected void setOutlineAlpha(float alpha) {
        if (alpha != this.mOutlineAlpha) {
            this.mOutlineAlpha = alpha;
            invalidateOutline();
        }
    }

    public float getOutlineAlpha() {
        return this.mOutlineAlpha;
    }

    protected void setOutlineRect(RectF rect) {
        if (rect != null) {
            setOutlineRect(rect.left, rect.top, rect.right, rect.bottom);
            return;
        }
        this.mCustomOutline = false;
        setClipToOutline(false);
        invalidateOutline();
    }

    public int getOutlineTranslation() {
        return this.mCustomOutline ? this.mOutlineRect.left : (int) getTranslation();
    }

    public void updateOutline() {
        if (!this.mCustomOutline) {
            ViewOutlineProvider viewOutlineProvider;
            boolean hasOutline = true;
            if (isChildInGroup()) {
                hasOutline = isGroupExpanded() && !isGroupExpansionChanging();
            } else if (isSummaryWithChildren()) {
                hasOutline = isGroupExpanded() ? isGroupExpansionChanging() : true;
            }
            if (hasOutline) {
                viewOutlineProvider = this.mProvider;
            } else {
                viewOutlineProvider = null;
            }
            setOutlineProvider(viewOutlineProvider);
        }
    }

    protected void setOutlineRect(float left, float top, float right, float bottom) {
        this.mCustomOutline = true;
        setClipToOutline(true);
        this.mOutlineRect.set((int) left, (int) top, (int) right, (int) bottom);
        this.mOutlineRect.bottom = (int) Math.max(top, (float) this.mOutlineRect.bottom);
        this.mOutlineRect.right = (int) Math.max(left, (float) this.mOutlineRect.right);
        invalidateOutline();
    }
}
