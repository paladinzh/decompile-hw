package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Property;
import android.view.View;
import com.android.systemui.recents.misc.Utilities;
import java.util.ArrayList;

public class TaskViewTransform {
    public static final Property<View, Rect> LTRB = new Property<View, Rect>(Rect.class, "leftTopRightBottom") {
        private Rect mTmpRect = new Rect();

        public void set(View v, Rect ltrb) {
            v.setLeftTopRightBottom(ltrb.left, ltrb.top, ltrb.right, ltrb.bottom);
        }

        public Rect get(View v) {
            this.mTmpRect.set(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            return this.mTmpRect;
        }
    };
    public float alpha = 1.0f;
    public float dimAlpha = 0.0f;
    public RectF rect = new RectF();
    public float scale = 1.0f;
    public float translationZ = 0.0f;
    public float viewOutlineAlpha = 0.0f;
    public boolean visible = false;

    public void fillIn(TaskView tv) {
        this.translationZ = tv.getTranslationZ();
        this.scale = tv.getScaleX();
        this.alpha = tv.getAlpha();
        this.visible = true;
        this.dimAlpha = tv.getDimAlpha();
        this.viewOutlineAlpha = tv.getViewBounds().getAlpha();
        this.rect.set((float) tv.getLeft(), (float) tv.getTop(), (float) tv.getRight(), (float) tv.getBottom());
    }

    public void copyFrom(TaskViewTransform other) {
        this.translationZ = other.translationZ;
        this.scale = other.scale;
        this.alpha = other.alpha;
        this.visible = other.visible;
        this.dimAlpha = other.dimAlpha;
        this.viewOutlineAlpha = other.viewOutlineAlpha;
        this.rect.set(other.rect);
    }

    public boolean isSame(TaskViewTransform other) {
        return (this.translationZ == other.translationZ && this.scale == other.scale && other.alpha == this.alpha && this.dimAlpha == other.dimAlpha && this.visible == other.visible) ? this.rect.equals(other.rect) : false;
    }

    public void reset() {
        this.translationZ = 0.0f;
        this.scale = 1.0f;
        this.alpha = 1.0f;
        this.dimAlpha = 0.0f;
        this.viewOutlineAlpha = 0.0f;
        this.visible = false;
        this.rect.setEmpty();
    }

    public boolean hasAlphaChangedFrom(float v) {
        return Float.compare(this.alpha, v) != 0;
    }

    public boolean hasScaleChangedFrom(float v) {
        return Float.compare(this.scale, v) != 0;
    }

    public boolean hasTranslationZChangedFrom(float v) {
        return Float.compare(this.translationZ, v) != 0;
    }

    public boolean hasRectChangedFrom(View v) {
        if (((int) this.rect.left) == v.getLeft() && ((int) this.rect.right) == v.getRight() && ((int) this.rect.top) == v.getTop() && ((int) this.rect.bottom) == v.getBottom()) {
            return false;
        }
        return true;
    }

    public void applyToTaskView(TaskView v, ArrayList<Animator> animators, AnimationProps animation, boolean allowShadows) {
        if (this.visible) {
            if (animation.isImmediate()) {
                if (allowShadows && hasTranslationZChangedFrom(v.getTranslationZ())) {
                    v.setTranslationZ(this.translationZ);
                }
                if (hasScaleChangedFrom(v.getScaleX())) {
                    v.setScaleX(this.scale);
                    v.setScaleY(this.scale);
                }
                if (hasAlphaChangedFrom(v.getAlpha())) {
                    v.setAlpha(this.alpha);
                }
                if (hasRectChangedFrom(v)) {
                    v.setLeftTopRightBottom((int) this.rect.left, (int) this.rect.top, (int) this.rect.right, (int) this.rect.bottom);
                }
            } else {
                PropertyValuesHolder[] propertyValuesHolderArr;
                if (allowShadows && hasTranslationZChangedFrom(v.getTranslationZ())) {
                    animators.add(animation.apply(3, ObjectAnimator.ofFloat(v, View.TRANSLATION_Z, new float[]{v.getTranslationZ(), this.translationZ})));
                }
                if (hasScaleChangedFrom(v.getScaleX())) {
                    propertyValuesHolderArr = new PropertyValuesHolder[2];
                    propertyValuesHolderArr[0] = PropertyValuesHolder.ofFloat(View.SCALE_X, new float[]{v.getScaleX(), this.scale});
                    propertyValuesHolderArr[1] = PropertyValuesHolder.ofFloat(View.SCALE_Y, new float[]{v.getScaleX(), this.scale});
                    animators.add(animation.apply(5, ObjectAnimator.ofPropertyValuesHolder(v, propertyValuesHolderArr)));
                }
                if (hasAlphaChangedFrom(v.getAlpha())) {
                    animators.add(animation.apply(4, ObjectAnimator.ofFloat(v, View.ALPHA, new float[]{v.getAlpha(), this.alpha})));
                }
                if (hasRectChangedFrom(v)) {
                    Rect fromViewRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                    this.rect.round(new Rect());
                    propertyValuesHolderArr = new PropertyValuesHolder[1];
                    propertyValuesHolderArr[0] = PropertyValuesHolder.ofObject(LTRB, Utilities.RECT_EVALUATOR, new Rect[]{fromViewRect, toViewRect});
                    animators.add(animation.apply(6, ObjectAnimator.ofPropertyValuesHolder(v, propertyValuesHolderArr)));
                }
            }
        }
    }

    public static void reset(TaskView v) {
        v.setTranslationX(0.0f);
        v.setTranslationY(0.0f);
        v.setTranslationZ(0.0f);
        v.setScaleX(1.0f);
        v.setScaleY(1.0f);
        v.setAlpha(1.0f);
        v.getViewBounds().setClipBottom(0);
        v.setLeftTopRightBottom(0, 0, 0, 0);
    }

    public String toString() {
        return "R: " + this.rect + " V: " + this.visible;
    }
}
