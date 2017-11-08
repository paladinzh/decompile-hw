package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

public class KeyguardPreviewContainer extends FrameLayout {
    private Drawable mBlackBarDrawable = new Drawable() {
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.clipRect(0, KeyguardPreviewContainer.this.getHeight() - KeyguardPreviewContainer.this.getPaddingBottom(), KeyguardPreviewContainer.this.getWidth(), KeyguardPreviewContainer.this.getHeight());
            canvas.drawColor(-16777216);
            canvas.restore();
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return -1;
        }
    };

    public KeyguardPreviewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(this.mBlackBarDrawable);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, 0, 0, insets.getStableInsetBottom());
        return super.onApplyWindowInsets(insets);
    }
}
