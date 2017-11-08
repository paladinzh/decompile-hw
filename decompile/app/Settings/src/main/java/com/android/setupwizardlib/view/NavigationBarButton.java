package com.android.setupwizardlib.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.Button;

public class NavigationBarButton extends Button {

    private static class TintedDrawable extends LayerDrawable {
        private ColorStateList mTintList = null;

        public static TintedDrawable wrap(Drawable drawable) {
            if (drawable instanceof TintedDrawable) {
                return (TintedDrawable) drawable;
            }
            return new TintedDrawable(drawable);
        }

        public TintedDrawable(Drawable wrapped) {
            super(new Drawable[]{wrapped});
        }

        public boolean isStateful() {
            return true;
        }

        public boolean setState(int[] stateSet) {
            return !super.setState(stateSet) ? updateState() : true;
        }

        public void setTintListCompat(ColorStateList colors) {
            this.mTintList = colors;
            if (updateState()) {
                invalidateSelf();
            }
        }

        private boolean updateState() {
            if (this.mTintList == null) {
                return false;
            }
            setColorFilter(this.mTintList.getColorForState(getState(), 0), Mode.SRC_IN);
            return true;
        }
    }

    public NavigationBarButton(Context context) {
        super(context);
        init();
    }

    public NavigationBarButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (VERSION.SDK_INT >= 17) {
            Drawable[] drawables = getCompoundDrawablesRelative();
            for (int i = 0; i < drawables.length; i++) {
                if (drawables[i] != null) {
                    drawables[i] = TintedDrawable.wrap(drawables[i].mutate());
                }
            }
            setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        }
    }

    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (left != null) {
            left = TintedDrawable.wrap(left.mutate());
        }
        if (top != null) {
            top = TintedDrawable.wrap(top.mutate());
        }
        if (right != null) {
            right = TintedDrawable.wrap(right.mutate());
        }
        if (bottom != null) {
            bottom = TintedDrawable.wrap(bottom.mutate());
        }
        super.setCompoundDrawables(left, top, right, bottom);
        tintDrawables();
    }

    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        if (start != null) {
            start = TintedDrawable.wrap(start.mutate());
        }
        if (top != null) {
            top = TintedDrawable.wrap(top.mutate());
        }
        if (end != null) {
            end = TintedDrawable.wrap(end.mutate());
        }
        if (bottom != null) {
            bottom = TintedDrawable.wrap(bottom.mutate());
        }
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        tintDrawables();
    }

    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);
        tintDrawables();
    }

    private void tintDrawables() {
        ColorStateList textColors = getTextColors();
        if (textColors != null) {
            for (Drawable drawable : getAllCompoundDrawables()) {
                if (drawable instanceof TintedDrawable) {
                    ((TintedDrawable) drawable).setTintListCompat(textColors);
                }
            }
            invalidate();
        }
    }

    private Drawable[] getAllCompoundDrawables() {
        Drawable[] drawables = new Drawable[6];
        Drawable[] compoundDrawables = getCompoundDrawables();
        drawables[0] = compoundDrawables[0];
        drawables[1] = compoundDrawables[1];
        drawables[2] = compoundDrawables[2];
        drawables[3] = compoundDrawables[3];
        if (VERSION.SDK_INT >= 17) {
            Drawable[] compoundDrawablesRelative = getCompoundDrawablesRelative();
            drawables[4] = compoundDrawablesRelative[0];
            drawables[5] = compoundDrawablesRelative[2];
        }
        return drawables;
    }
}
