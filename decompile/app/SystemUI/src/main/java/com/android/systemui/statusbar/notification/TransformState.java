package com.android.systemui.statusbar.notification;

import android.util.ArraySet;
import android.util.Pools.SimplePool;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation;

public class TransformState {
    private static SimplePool<TransformState> sInstancePool = new SimplePool(40);
    private int[] mOwnPosition = new int[2];
    private float mTransformationEndX = -1.0f;
    private float mTransformationEndY = -1.0f;
    protected View mTransformedView;

    public void initFrom(View view) {
        this.mTransformedView = view;
    }

    public void transformViewFrom(TransformState otherState, float transformationAmount) {
        this.mTransformedView.animate().cancel();
        if (!sameAs(otherState)) {
            CrossFadeHelper.fadeIn(this.mTransformedView, transformationAmount);
        } else if (this.mTransformedView.getVisibility() == 4) {
            this.mTransformedView.setAlpha(1.0f);
            this.mTransformedView.setVisibility(0);
        }
        transformViewFullyFrom(otherState, transformationAmount);
    }

    public void transformViewFullyFrom(TransformState otherState, float transformationAmount) {
        transformViewFrom(otherState, 17, null, transformationAmount);
    }

    public void transformViewVerticalFrom(TransformState otherState, CustomTransformation customTransformation, float transformationAmount) {
        transformViewFrom(otherState, 16, customTransformation, transformationAmount);
    }

    public void transformViewVerticalFrom(TransformState otherState, float transformationAmount) {
        transformViewFrom(otherState, 16, null, transformationAmount);
    }

    private void transformViewFrom(TransformState otherState, int transformationFlags, CustomTransformation customTransformation, float transformationAmount) {
        float interpolatedValue;
        float transformationStartScaleX;
        float transformationStartScaleY;
        int[] otherPosition;
        View transformedView = this.mTransformedView;
        boolean transformX = (transformationFlags & 1) != 0;
        boolean transformY = (transformationFlags & 16) != 0;
        boolean transformScale = transformScale();
        if (!(transformationAmount == 0.0f || ((transformX && getTransformationStartX() == -1.0f) || ((transformY && getTransformationStartY() == -1.0f) || (transformScale && getTransformationStartScaleX() == -1.0f))))) {
            if (transformScale && getTransformationStartScaleY() == -1.0f) {
            }
            interpolatedValue = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(transformationAmount);
            if (transformX) {
                transformedView.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), 0.0f, interpolatedValue));
            }
            if (transformY) {
                transformedView.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), 0.0f, interpolatedValue));
            }
            if (transformScale) {
                transformationStartScaleX = getTransformationStartScaleX();
                if (transformationStartScaleX != -1.0f) {
                    transformedView.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, 1.0f, interpolatedValue));
                }
                transformationStartScaleY = getTransformationStartScaleY();
                if (transformationStartScaleY != -1.0f) {
                    transformedView.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, 1.0f, interpolatedValue));
                }
            }
        }
        if (transformationAmount != 0.0f) {
            otherPosition = otherState.getLaidOutLocationOnScreen();
        } else {
            otherPosition = otherState.getLocationOnScreen();
        }
        int[] ownStablePosition = getLaidOutLocationOnScreen();
        if (customTransformation == null || !customTransformation.initTransformation(this, otherState)) {
            if (transformX) {
                setTransformationStartX((float) (otherPosition[0] - ownStablePosition[0]));
            }
            if (transformY) {
                setTransformationStartY((float) (otherPosition[1] - ownStablePosition[1]));
            }
            View otherView = otherState.getTransformedView();
            if (!transformScale || otherView.getWidth() == transformedView.getWidth()) {
                setTransformationStartScaleX(-1.0f);
            } else {
                setTransformationStartScaleX((((float) otherView.getWidth()) * otherView.getScaleX()) / ((float) transformedView.getWidth()));
                transformedView.setPivotX(0.0f);
            }
            if (!transformScale || otherView.getHeight() == transformedView.getHeight()) {
                setTransformationStartScaleY(-1.0f);
            } else {
                setTransformationStartScaleY((((float) otherView.getHeight()) * otherView.getScaleY()) / ((float) transformedView.getHeight()));
                transformedView.setPivotY(0.0f);
            }
        }
        if (!transformX) {
            setTransformationStartX(-1.0f);
        }
        if (!transformY) {
            setTransformationStartY(-1.0f);
        }
        if (!transformScale) {
            setTransformationStartScaleX(-1.0f);
            setTransformationStartScaleY(-1.0f);
        }
        setClippingDeactivated(transformedView, true);
        interpolatedValue = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(transformationAmount);
        if (transformX) {
            transformedView.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), 0.0f, interpolatedValue));
        }
        if (transformY) {
            transformedView.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), 0.0f, interpolatedValue));
        }
        if (transformScale) {
            transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                transformedView.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, 1.0f, interpolatedValue));
            }
            transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                transformedView.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, 1.0f, interpolatedValue));
            }
        }
    }

    protected boolean transformScale() {
        return false;
    }

    public boolean transformViewTo(TransformState otherState, float transformationAmount) {
        this.mTransformedView.animate().cancel();
        if (sameAs(otherState)) {
            if (this.mTransformedView.getVisibility() == 0) {
                this.mTransformedView.setAlpha(0.0f);
                this.mTransformedView.setVisibility(4);
            }
            return false;
        }
        CrossFadeHelper.fadeOut(this.mTransformedView, transformationAmount);
        transformViewFullyTo(otherState, transformationAmount);
        return true;
    }

    public void transformViewFullyTo(TransformState otherState, float transformationAmount) {
        transformViewTo(otherState, 17, null, transformationAmount);
    }

    public void transformViewVerticalTo(TransformState otherState, CustomTransformation customTransformation, float transformationAmount) {
        transformViewTo(otherState, 16, customTransformation, transformationAmount);
    }

    public void transformViewVerticalTo(TransformState otherState, float transformationAmount) {
        transformViewTo(otherState, 16, null, transformationAmount);
    }

    private void transformViewTo(TransformState otherState, int transformationFlags, CustomTransformation customTransformation, float transformationAmount) {
        View transformedView = this.mTransformedView;
        boolean transformX = (transformationFlags & 1) != 0;
        boolean transformY = (transformationFlags & 16) != 0;
        boolean transformScale = transformScale();
        if (transformationAmount == 0.0f) {
            float start;
            if (transformX) {
                float transformationStartX = getTransformationStartX();
                if (transformationStartX != -1.0f) {
                    start = transformationStartX;
                } else {
                    start = transformedView.getTranslationX();
                }
                setTransformationStartX(start);
            }
            if (transformY) {
                float transformationStartY = getTransformationStartY();
                if (transformationStartY != -1.0f) {
                    start = transformationStartY;
                } else {
                    start = transformedView.getTranslationY();
                }
                setTransformationStartY(start);
            }
            View otherView = otherState.getTransformedView();
            if (!transformScale || otherView.getWidth() == transformedView.getWidth()) {
                setTransformationStartScaleX(-1.0f);
            } else {
                setTransformationStartScaleX(transformedView.getScaleX());
                transformedView.setPivotX(0.0f);
            }
            if (!transformScale || otherView.getHeight() == transformedView.getHeight()) {
                setTransformationStartScaleY(-1.0f);
            } else {
                setTransformationStartScaleY(transformedView.getScaleY());
                transformedView.setPivotY(0.0f);
            }
            setClippingDeactivated(transformedView, true);
        }
        float interpolatedValue = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(transformationAmount);
        int[] otherStablePosition = otherState.getLaidOutLocationOnScreen();
        int[] ownPosition = getLaidOutLocationOnScreen();
        if (transformX) {
            float endX = (float) (otherStablePosition[0] - ownPosition[0]);
            if (customTransformation != null && customTransformation.customTransformTarget(this, otherState)) {
                endX = this.mTransformationEndX;
            }
            transformedView.setTranslationX(NotificationUtils.interpolate(getTransformationStartX(), endX, interpolatedValue));
        }
        if (transformY) {
            float endY = (float) (otherStablePosition[1] - ownPosition[1]);
            if (customTransformation != null && customTransformation.customTransformTarget(this, otherState)) {
                endY = this.mTransformationEndY;
            }
            transformedView.setTranslationY(NotificationUtils.interpolate(getTransformationStartY(), endY, interpolatedValue));
        }
        if (transformScale) {
            otherView = otherState.getTransformedView();
            float transformationStartScaleX = getTransformationStartScaleX();
            if (transformationStartScaleX != -1.0f) {
                transformedView.setScaleX(NotificationUtils.interpolate(transformationStartScaleX, ((float) otherView.getWidth()) / ((float) transformedView.getWidth()), interpolatedValue));
            }
            float transformationStartScaleY = getTransformationStartScaleY();
            if (transformationStartScaleY != -1.0f) {
                transformedView.setScaleY(NotificationUtils.interpolate(transformationStartScaleY, ((float) otherView.getHeight()) / ((float) transformedView.getHeight()), interpolatedValue));
            }
        }
    }

    public static void setClippingDeactivated(View transformedView, boolean deactivated) {
        if (transformedView.getParent() instanceof ViewGroup) {
            ViewGroup view = (ViewGroup) transformedView.getParent();
            while (true) {
                ExpandableNotificationRow expandableNotificationRow;
                ArraySet<View> clipSet = (ArraySet) view.getTag(R.id.clip_children_set_tag);
                if (clipSet == null) {
                    clipSet = new ArraySet();
                    view.setTag(R.id.clip_children_set_tag, clipSet);
                }
                Boolean clipChildren = (Boolean) view.getTag(R.id.clip_children_tag);
                if (clipChildren == null) {
                    clipChildren = Boolean.valueOf(view.getClipChildren());
                    view.setTag(R.id.clip_children_tag, clipChildren);
                }
                Boolean clipToPadding = (Boolean) view.getTag(R.id.clip_to_padding_tag);
                if (clipToPadding == null) {
                    clipToPadding = Boolean.valueOf(view.getClipToPadding());
                    view.setTag(R.id.clip_to_padding_tag, clipToPadding);
                }
                if (view instanceof ExpandableNotificationRow) {
                    expandableNotificationRow = (ExpandableNotificationRow) view;
                } else {
                    expandableNotificationRow = null;
                }
                if (deactivated) {
                    clipSet.add(transformedView);
                    view.setClipChildren(false);
                    view.setClipToPadding(false);
                    if (expandableNotificationRow != null && expandableNotificationRow.isChildInGroup()) {
                        expandableNotificationRow.setClipToActualHeight(false);
                    }
                } else {
                    clipSet.remove(transformedView);
                    if (clipSet.isEmpty()) {
                        view.setClipChildren(clipChildren.booleanValue());
                        view.setClipToPadding(clipToPadding.booleanValue());
                        view.setTag(R.id.clip_children_set_tag, null);
                        if (expandableNotificationRow != null) {
                            expandableNotificationRow.setClipToActualHeight(true);
                        }
                    }
                }
                if (expandableNotificationRow == null || expandableNotificationRow.isChildInGroup()) {
                    ViewParent parent = view.getParent();
                    if (parent instanceof ViewGroup) {
                        view = (ViewGroup) parent;
                    } else {
                        return;
                    }
                }
                return;
            }
        }
    }

    public int[] getLaidOutLocationOnScreen() {
        int[] location = getLocationOnScreen();
        location[0] = (int) (((float) location[0]) - this.mTransformedView.getTranslationX());
        location[1] = (int) (((float) location[1]) - this.mTransformedView.getTranslationY());
        return location;
    }

    public int[] getLocationOnScreen() {
        this.mTransformedView.getLocationOnScreen(this.mOwnPosition);
        return this.mOwnPosition;
    }

    protected boolean sameAs(TransformState otherState) {
        return false;
    }

    public static TransformState createFrom(View view) {
        if (view instanceof TextView) {
            TextViewTransformState result = TextViewTransformState.obtain();
            result.initFrom(view);
            return result;
        } else if (view.getId() == 16909214) {
            ActionListTransformState result2 = ActionListTransformState.obtain();
            result2.initFrom(view);
            return result2;
        } else if (view instanceof NotificationHeaderView) {
            HeaderTransformState result3 = HeaderTransformState.obtain();
            result3.initFrom(view);
            return result3;
        } else if (view instanceof ImageView) {
            ImageTransformState result4 = ImageTransformState.obtain();
            result4.initFrom(view);
            return result4;
        } else if (view instanceof ProgressBar) {
            ProgressTransformState result5 = ProgressTransformState.obtain();
            result5.initFrom(view);
            return result5;
        } else {
            TransformState result6 = obtain();
            result6.initFrom(view);
            return result6;
        }
    }

    public void recycle() {
        reset();
        if (getClass() == TransformState.class) {
            sInstancePool.release(this);
        }
    }

    public void setTransformationEndY(float transformationEndY) {
        this.mTransformationEndY = transformationEndY;
    }

    public float getTransformationStartX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_x_tag);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_y_tag);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleX() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_x_tag);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public float getTransformationStartScaleY() {
        Object tag = this.mTransformedView.getTag(R.id.transformation_start_scale_y_tag);
        return tag == null ? -1.0f : ((Float) tag).floatValue();
    }

    public void setTransformationStartX(float transformationStartX) {
        this.mTransformedView.setTag(R.id.transformation_start_x_tag, Float.valueOf(transformationStartX));
    }

    public void setTransformationStartY(float transformationStartY) {
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, Float.valueOf(transformationStartY));
    }

    private void setTransformationStartScaleX(float startScaleX) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, Float.valueOf(startScaleX));
    }

    private void setTransformationStartScaleY(float startScaleY) {
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, Float.valueOf(startScaleY));
    }

    protected void reset() {
        this.mTransformedView = null;
        this.mTransformationEndX = -1.0f;
        this.mTransformationEndY = -1.0f;
    }

    public void setVisible(boolean visible, boolean force) {
        if (force || this.mTransformedView.getVisibility() != 8) {
            if (this.mTransformedView.getVisibility() != 8) {
                this.mTransformedView.setVisibility(visible ? 0 : 4);
            }
            this.mTransformedView.animate().cancel();
            this.mTransformedView.setAlpha(visible ? 1.0f : 0.0f);
            resetTransformedView();
        }
    }

    public void prepareFadeIn() {
        resetTransformedView();
    }

    protected void resetTransformedView() {
        this.mTransformedView.setTranslationX(0.0f);
        this.mTransformedView.setTranslationY(0.0f);
        this.mTransformedView.setScaleX(1.0f);
        this.mTransformedView.setScaleY(1.0f);
        setClippingDeactivated(this.mTransformedView, false);
        abortTransformation();
    }

    public void abortTransformation() {
        this.mTransformedView.setTag(R.id.transformation_start_x_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_y_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_scale_x_tag, Float.valueOf(-1.0f));
        this.mTransformedView.setTag(R.id.transformation_start_scale_y_tag, Float.valueOf(-1.0f));
    }

    public static TransformState obtain() {
        TransformState instance = (TransformState) sInstancePool.acquire();
        if (instance != null) {
            return instance;
        }
        return new TransformState();
    }

    public View getTransformedView() {
        return this.mTransformedView;
    }
}
