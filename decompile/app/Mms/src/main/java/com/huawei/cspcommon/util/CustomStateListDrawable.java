package com.huawei.cspcommon.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import java.lang.reflect.Field;

public class CustomStateListDrawable extends StateListDrawable {
    private static final int[] DISABLE_STATE = new int[]{-16842910};
    private static final int[] ENABLE_FOCUS_STATE = new int[]{16842910, 16842908};
    private static final int[] ENABLE_PRESSED_STATE = new int[]{16842919, 16842910};
    private static final int[] ENABLE_STATE = new int[]{16842910};
    private static final int[] FOCUS_STATE = new int[]{16842908};
    private static final Field INDICATOR_CURRENT_DRAWABLE_FIELD = getField("mCurrDrawable");
    private static final int[] PRESSED_STATE = new int[]{16842919};
    private static final String TAG = CustomStateListDrawable.class.getSimpleName();
    private int mCurrentIndex = -1;
    private Drawable mUnable;

    public void draw(Canvas canvas) {
        Drawable d = getCurrent();
        if (d == null || !d.equals(this.mUnable)) {
            super.draw(canvas);
            return;
        }
        d.mutate().setAlpha(77);
        d.draw(canvas);
    }

    public boolean selectDrawable(int idx) {
        if (this.mCurrentIndex == idx) {
            return false;
        }
        Drawable mCurDrawable = getCurrent();
        if (mCurDrawable != null) {
            mCurDrawable.setVisible(false, false);
        }
        DrawableContainerState constantState = (DrawableContainerState) getConstantState();
        if (constantState == null || idx < 0 || idx >= constantState.getChildCount()) {
            setCurrDrawable(null);
            this.mCurrentIndex = -1;
        } else {
            Drawable d = constantState.getChild(idx);
            setCurrDrawable(d);
            this.mCurrentIndex = idx;
            if (d != null) {
                d.mutate();
                d.setVisible(isVisible(), true);
                d.setDither(true);
                d.setState(getState());
                d.setLevel(getLevel());
                d.setBounds(getBounds());
            }
        }
        invalidateSelf();
        return true;
    }

    private void setCurrDrawable(Drawable drawable) {
        try {
            INDICATOR_CURRENT_DRAWABLE_FIELD.set(this, drawable);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            try {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
    }

    private static Field getField(String methodName) {
        Field field = null;
        try {
            field = DrawableContainer.class.getDeclaredField(methodName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return field;
        }
    }

    public static CustomStateListDrawable createStateDrawable(Context context, BitmapDrawable normal) {
        CustomStateListDrawable bg = new CustomStateListDrawable();
        BitmapDrawable lNormal = normal;
        Bitmap bitmap = normal.getBitmap();
        BitmapDrawable pressed = new BitmapDrawable(context.getResources(), bitmap);
        pressed.mutate().setAlpha(128);
        BitmapDrawable focused = new BitmapDrawable(context.getResources(), bitmap);
        focused.mutate().setAlpha(128);
        bg.mUnable = new BitmapDrawable(context.getResources(), bitmap);
        bg.addState(ENABLE_PRESSED_STATE, pressed);
        bg.addState(ENABLE_FOCUS_STATE, focused);
        bg.addState(FOCUS_STATE, focused);
        bg.addState(PRESSED_STATE, pressed);
        bg.addState(ENABLE_STATE, normal);
        bg.addState(DISABLE_STATE, bg.mUnable);
        bg.addState(StateSet.WILD_CARD, normal);
        return bg;
    }
}
