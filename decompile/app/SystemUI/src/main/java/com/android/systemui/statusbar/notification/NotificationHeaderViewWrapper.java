package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import java.util.Stack;

public class NotificationHeaderViewWrapper extends NotificationViewWrapper {
    protected int mColor;
    private ImageView mExpandButton;
    private ImageView mIcon;
    private final PorterDuffColorFilter mIconColorFilter = new PorterDuffColorFilter(0, Mode.SRC_ATOP);
    private final int mIconDarkAlpha;
    private final int mIconDarkColor = -1;
    protected final ViewInvertHelper mInvertHelper;
    private NotificationHeaderView mNotificationHeader;
    protected final ViewTransformationHelper mTransformationHelper;

    protected NotificationHeaderViewWrapper(Context ctx, View view, ExpandableNotificationRow row) {
        super(view, row);
        this.mIconDarkAlpha = ctx.getResources().getInteger(R.integer.doze_small_icon_alpha);
        this.mInvertHelper = new ViewInvertHelper(ctx, 700);
        this.mTransformationHelper = new ViewTransformationHelper();
        resolveHeaderViews();
        updateInvertHelper();
    }

    protected void resolveHeaderViews() {
        this.mIcon = (ImageView) this.mView.findViewById(16908294);
        this.mExpandButton = (ImageView) this.mView.findViewById(16909228);
        this.mColor = resolveColor(this.mExpandButton);
        this.mNotificationHeader = (NotificationHeaderView) this.mView.findViewById(16909220);
    }

    private int resolveColor(ImageView icon) {
        if (!(icon == null || icon.getDrawable() == null)) {
            ColorFilter filter = icon.getDrawable().getColorFilter();
            if (filter instanceof PorterDuffColorFilter) {
                return ((PorterDuffColorFilter) filter).getColor();
            }
        }
        return 0;
    }

    public void notifyContentUpdated(StatusBarNotification notification) {
        super.notifyContentUpdated(notification);
        ArraySet<View> previousViews = this.mTransformationHelper.getAllTransformingViews();
        resolveHeaderViews();
        updateInvertHelper();
        updateTransformedTypes();
        addRemainingTransformTypes();
        updateCropToPaddingForImageViews();
        ArraySet<View> currentViews = this.mTransformationHelper.getAllTransformingViews();
        for (int i = 0; i < previousViews.size(); i++) {
            View view = (View) previousViews.valueAt(i);
            if (!currentViews.contains(view)) {
                this.mTransformationHelper.resetTransformedView(view);
            }
        }
    }

    private void addRemainingTransformTypes() {
        this.mTransformationHelper.addRemainingTransformTypes(this.mView);
    }

    private void updateCropToPaddingForImageViews() {
        Stack<View> stack = new Stack();
        stack.push(this.mView);
        while (!stack.isEmpty()) {
            View child = (View) stack.pop();
            if (child instanceof ImageView) {
                ((ImageView) child).setCropToPadding(true);
            } else if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                for (int i = 0; i < group.getChildCount(); i++) {
                    stack.push(group.getChildAt(i));
                }
            }
        }
    }

    protected void updateInvertHelper() {
        this.mInvertHelper.clearTargets();
        for (int i = 0; i < this.mNotificationHeader.getChildCount(); i++) {
            View child = this.mNotificationHeader.getChildAt(i);
            if (child != this.mIcon) {
                this.mInvertHelper.addTarget(child);
            }
        }
    }

    protected void updateTransformedTypes() {
        this.mTransformationHelper.reset();
        this.mTransformationHelper.addTransformedView(0, this.mNotificationHeader);
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (dark != this.mDark || !this.mDarkInitialized) {
            super.setDark(dark, fade, delay);
            if (fade) {
                this.mInvertHelper.fade(dark, delay);
            } else {
                this.mInvertHelper.update(dark);
            }
            if (!(this.mIcon == null || this.mRow.isChildInGroup())) {
                boolean hadColorFilter = this.mNotificationHeader.getOriginalIconColor() != -1;
                if (fade) {
                    if (hadColorFilter) {
                        fadeIconColorFilter(this.mIcon, dark, delay);
                        fadeIconAlpha(this.mIcon, dark, delay);
                    } else {
                        fadeGrayscale(this.mIcon, dark, delay);
                    }
                } else if (hadColorFilter) {
                    updateIconColorFilter(this.mIcon, dark);
                    updateIconAlpha(this.mIcon, dark);
                } else {
                    updateGrayscale(this.mIcon, dark);
                }
            }
        }
    }

    private void fadeIconColorFilter(final ImageView target, boolean dark, long delay) {
        startIntensityAnimation(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationHeaderViewWrapper.this.updateIconColorFilter(target, ((Float) animation.getAnimatedValue()).floatValue());
            }
        }, dark, delay, null);
    }

    private void fadeIconAlpha(final ImageView target, boolean dark, long delay) {
        startIntensityAnimation(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                target.setImageAlpha((int) (((1.0f - t) * 255.0f) + (((float) NotificationHeaderViewWrapper.this.mIconDarkAlpha) * t)));
            }
        }, dark, delay, null);
    }

    protected void fadeGrayscale(final ImageView target, final boolean dark, long delay) {
        startIntensityAnimation(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                NotificationHeaderViewWrapper.this.updateGrayscaleMatrix(((Float) animation.getAnimatedValue()).floatValue());
                target.setColorFilter(new ColorMatrixColorFilter(NotificationHeaderViewWrapper.this.mGrayscaleColorMatrix));
            }
        }, dark, delay, new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!dark) {
                    target.setColorFilter(null);
                }
            }
        });
    }

    private void updateIconColorFilter(ImageView target, boolean dark) {
        updateIconColorFilter(target, dark ? 1.0f : 0.0f);
    }

    private void updateIconColorFilter(ImageView target, float intensity) {
        this.mIconColorFilter.setColor(interpolateColor(this.mColor, -1, intensity));
        Drawable iconDrawable = target.getDrawable();
        if (iconDrawable != null) {
            iconDrawable.mutate().setColorFilter(this.mIconColorFilter);
        }
    }

    private void updateIconAlpha(ImageView target, boolean dark) {
        target.setImageAlpha(dark ? this.mIconDarkAlpha : 255);
    }

    protected void updateGrayscale(ImageView target, boolean dark) {
        if (dark) {
            updateGrayscaleMatrix(1.0f);
            target.setColorFilter(new ColorMatrixColorFilter(this.mGrayscaleColorMatrix));
            return;
        }
        target.setColorFilter(null);
    }

    public void updateExpandability(boolean expandable, OnClickListener onClickListener) {
        this.mExpandButton.setVisibility(expandable ? 0 : 8);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (!expandable) {
            onClickListener = null;
        }
        notificationHeaderView.setOnClickListener(onClickListener);
    }

    private static int interpolateColor(int source, int target, float t) {
        int aSource = Color.alpha(source);
        int rSource = Color.red(source);
        int gSource = Color.green(source);
        int bSource = Color.blue(source);
        return Color.argb((int) ((((float) aSource) * (1.0f - t)) + (((float) Color.alpha(target)) * t)), (int) ((((float) rSource) * (1.0f - t)) + (((float) Color.red(target)) * t)), (int) ((((float) gSource) * (1.0f - t)) + (((float) Color.green(target)) * t)), (int) ((((float) bSource) * (1.0f - t)) + (((float) Color.blue(target)) * t)));
    }

    public NotificationHeaderView getNotificationHeader() {
        return this.mNotificationHeader;
    }

    public TransformState getCurrentState(int fadingView) {
        return this.mTransformationHelper.getCurrentState(fadingView);
    }

    public void transformTo(TransformableView notification, Runnable endRunnable) {
        this.mTransformationHelper.transformTo(notification, endRunnable);
    }

    public void transformTo(TransformableView notification, float transformationAmount) {
        this.mTransformationHelper.transformTo(notification, transformationAmount);
    }

    public void transformFrom(TransformableView notification) {
        this.mTransformationHelper.transformFrom(notification);
    }

    public void transformFrom(TransformableView notification, float transformationAmount) {
        this.mTransformationHelper.transformFrom(notification, transformationAmount);
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.mTransformationHelper.setVisible(visible);
        if (this.mExpandButton != null) {
            float f;
            ImageView imageView = this.mExpandButton;
            if (visible) {
                f = 1.0f;
            } else {
                f = 0.0f;
            }
            imageView.setAlpha(f);
        }
    }
}
