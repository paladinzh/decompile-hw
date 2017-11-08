package com.huawei.gallery.editor.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class EditorAnimation {
    public static final CubicBezierInterpolator sInterPolator = new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f);

    public interface Delegate {
        int getAnimationDuration();

        View getAnimationTargetView();

        int getTipHeight();

        boolean isPort();
    }

    public interface EditorAnimationListener {
        void onAnimationEnd();
    }

    public static boolean startEditorAnimation(Delegate delegate, int direction, int duration, EditorAnimationListener listener, int delay) {
        ViewGroup container = (ViewGroup) delegate.getAnimationTargetView();
        if (container == null) {
            return false;
        }
        int height = delegate.getTipHeight();
        if (height == 0) {
            height = container.getHeight();
        }
        ArrayList animators = new ArrayList();
        if (direction == 1) {
            if (delegate.isPort()) {
                container.setTranslationY((float) height);
            } else {
                container.setTranslationX((float) height);
            }
            container.setAlpha(0.0f);
        } else {
            if (delegate.isPort()) {
                container.setTranslationY(0.0f);
            } else {
                container.setTranslationX(0.0f);
            }
            container.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        }
        animators.add(createAlphaAnimator(container, direction, duration, delay));
        animators.add(createTranlationAnimator(container, direction, duration, height, delegate.isPort(), delay));
        startAnimatorsAsAnimatorSet(animators, sInterPolator, listener, delay);
        return true;
    }

    private static void startAnimatorsAsAnimatorSet(Animator animator, TimeInterpolator interpolator, EditorAnimationListener listener) {
        startAnimatorsAsAnimatorSet(animator, interpolator, listener, 0);
    }

    private static void startAnimatorsAsAnimatorSet(Animator animator, TimeInterpolator interpolator, EditorAnimationListener listener, int delay) {
        ArrayList animators = new ArrayList();
        animators.add(animator);
        startAnimatorsAsAnimatorSet(animators, interpolator, listener, delay);
    }

    private static void startAnimatorsAsAnimatorSet(ArrayList<Animator> animators, TimeInterpolator interpolator, final EditorAnimationListener listener, int delay) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(interpolator);
        animatorSet.playTogether(animators);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });
        animatorSet.setStartDelay((long) delay);
        animatorSet.start();
    }

    private static Animator createTranlationAnimator(View target, int direction, int duration, int delta, boolean isPort, int delay) {
        Animator anim;
        String property = isPort ? "translationY" : "translationX";
        if (direction == 1) {
            anim = ObjectAnimator.ofFloat(target, property, new float[]{(float) delta, 0.0f});
        } else {
            anim = ObjectAnimator.ofFloat(target, property, new float[]{0.0f, (float) delta});
        }
        anim.setDuration((long) duration);
        return anim;
    }

    private static Animator createAlphaAnimator(View target, int direction, int duration, int delay) {
        Animator anim;
        if (direction == 1) {
            anim = ObjectAnimator.ofFloat(target, "alpha", new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1});
        } else {
            anim = ObjectAnimator.ofFloat(target, "alpha", new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f});
        }
        anim.setDuration((long) duration);
        return anim;
    }

    public static boolean startFadeAnimationForViewGroup(View view, int direction, int delay, EditorAnimationListener listener) {
        startFadeAnimationForViewGroup(view, direction, SmsCheckResult.ESCT_200, delay, listener);
        return true;
    }

    public static boolean startFadeAnimationForViewGroup(View view, int direction, int duration, int delay, EditorAnimationListener listener) {
        if (view == null) {
            return false;
        }
        if (direction == 1) {
            view.setAlpha(0.0f);
        } else {
            view.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        }
        startAnimatorsAsAnimatorSet(createAlphaAnimator(view, direction, duration, delay), sInterPolator, listener, delay);
        return true;
    }

    public static boolean startTranslationAnimationForViewGroup(View view, int director, int delta, int delay, boolean isPort, EditorAnimationListener listener) {
        if (view == null) {
            return false;
        }
        startAnimatorsAsAnimatorSet(createTranlationAnimator(view, director, 300, delta, isPort, delay), sInterPolator, listener);
        return true;
    }

    public static boolean startAnimationForAllChildView(View view, int duration, int tipHeight, int direction, int delay, int intervalTime, EditorAnimationListener listener, boolean isPort) {
        return startAnimationForAllChildView(view, duration, tipHeight, direction, delay, intervalTime, -1, listener, isPort);
    }

    public static boolean startAnimationForAllChildView(View view, int duration, int tipHeight, int direction, int delay, int intervalTime, int listenerIndex, EditorAnimationListener listener, boolean isPort) {
        if (view == null) {
            return false;
        }
        int i;
        ViewGroup container = (ViewGroup) view;
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        int height = isPort ? container.getHeight() : container.getWidth();
        if (height == 0) {
            height = tipHeight;
        }
        for (i = 0; i < container.getChildCount(); i++) {
            View childView = container.getChildAt(i);
            if (childView.getVisibility() != 4) {
                setChildViewTranslation(direction, isPort, height, childView);
            }
        }
        ArrayList<Animator> animators = new ArrayList();
        int startTimeCount = 0;
        for (i = 0; i < container.getChildCount(); i++) {
            childView = container.getChildAt(i);
            if (childView.getVisibility() != 4) {
                Animator animator;
                final EditorAnimationListener editorAnimationListener;
                if (childView instanceof LinearLayout) {
                    ViewGroup linearLayout = (ViewGroup) childView;
                    for (int j = 0; j < linearLayout.getChildCount(); j++) {
                        View linearLayoutChildView = linearLayout.getChildAt(j);
                        if (isPort ? EditorUtils.isOnParentRightSide(linearLayoutChildView) : EditorUtils.isOnParentBottomSide(linearLayoutChildView)) {
                            animator = createItemAnimator(linearLayoutChildView, intervalTime * startTimeCount, duration, direction, height, isPort);
                            animators.add(animator);
                            if (listenerIndex == startTimeCount) {
                                GalleryLog.v("EditorAnimation", "LinearLayout Animation");
                                editorAnimationListener = listener;
                                animator.addListener(new AnimatorListenerAdapter() {
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        if (editorAnimationListener != null) {
                                            editorAnimationListener.onAnimationEnd();
                                        }
                                    }
                                });
                            }
                            startTimeCount++;
                        }
                    }
                } else {
                    if (isPort ? EditorUtils.isOnParentRightSide(childView) : EditorUtils.isOnParentBottomSide(childView)) {
                        animator = createItemAnimator(childView, intervalTime * startTimeCount, duration, direction, height, isPort);
                        animators.add(animator);
                        if (listenerIndex == startTimeCount) {
                            GalleryLog.v("EditorAnimation", "not linearlayout Animation");
                            editorAnimationListener = listener;
                            animator.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (editorAnimationListener != null) {
                                        editorAnimationListener.onAnimationEnd();
                                    }
                                }
                            });
                        }
                        startTimeCount++;
                    }
                }
            }
        }
        startAnimatorSet(delay, listenerIndex, listener, animators);
        return true;
    }

    private static void startAnimatorSet(int delay, int listenerIndex, final EditorAnimationListener listener, ArrayList<Animator> animators) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(animators);
        if (listenerIndex == -1) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (listener != null) {
                        listener.onAnimationEnd();
                    }
                }
            });
        }
        animatorSet.setStartDelay((long) delay);
        animatorSet.start();
    }

    private static void setChildViewTranslation(int direction, boolean isPort, int height, View childView) {
        int i = 0;
        if (childView instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) childView;
            for (int j = 0; j < linearLayout.getChildCount(); j++) {
                View linearLayoutChildView = linearLayout.getChildAt(j);
                if (isPort) {
                    if (EditorUtils.isOnParentRightSide(linearLayoutChildView)) {
                        linearLayoutChildView.setTranslationY((float) (direction == 1 ? height : 0));
                    } else {
                        linearLayoutChildView.setTranslationY((float) (direction == 1 ? 0 : height));
                    }
                } else if (EditorUtils.isOnParentBottomSide(linearLayoutChildView)) {
                    linearLayoutChildView.setTranslationX((float) (direction == 1 ? height : 0));
                } else {
                    linearLayoutChildView.setTranslationX((float) (direction == 1 ? 0 : height));
                }
            }
            linearLayout.setTranslationY(0.0f);
            linearLayout.setTranslationX(0.0f);
        } else if (isPort) {
            if (EditorUtils.isOnParentRightSide(childView)) {
                if (direction != 1) {
                    height = 0;
                }
                childView.setTranslationY((float) height);
                return;
            }
            if (direction != 1) {
                i = height;
            }
            childView.setTranslationY((float) i);
        } else if (EditorUtils.isOnParentBottomSide(childView)) {
            if (direction != 1) {
                height = 0;
            }
            childView.setTranslationX((float) height);
        } else {
            if (direction != 1) {
                i = height;
            }
            childView.setTranslationX((float) i);
        }
    }

    private static Animator createItemAnimator(View target, int delay, int duration, int direction, int delta, boolean isPort) {
        int fromYDelta = 0;
        int toYDelta = 0;
        if (direction == 1) {
            fromYDelta = delta;
        } else if (direction == 2) {
            toYDelta = delta;
        }
        Animator anim = ObjectAnimator.ofFloat(target, isPort ? "translationY" : "translationX", new float[]{(float) fromYDelta, (float) toYDelta});
        anim.setDuration((long) duration);
        anim.setStartDelay((long) delay);
        return anim;
    }
}
