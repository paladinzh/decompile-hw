package com.android.contacts.animate;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ContactListAnimatorHelper extends AnimatorHelper {
    private static final String TAG = ContactListAnimatorHelper.class.getName();
    private float actionBarHeight = 0.0f;
    private View action_bar_container;
    private boolean hasAnimationEnd = false;
    private ValueAnimator mActionAnimator;
    private int mActionBarTransY;
    private Activity mActivity = null;
    private ValueAnimator mAlphaAnimator;
    private AnimatorSet mAnimatorSet;
    private View mContactView;
    private View mContentList;
    private int mContentTransY = 126;
    private View mCustomDragView;
    private View mEmptyView;
    private boolean mFadeOut = true;
    private boolean mIsFadeOut;
    private View mMenu;
    private ValueAnimator mMenuAnimator;
    private View mSearchView;
    private View mTabContainer;
    private int menuHeight = 0;

    public void init(View menu, Activity activity) {
        this.mMenu = menu;
        this.mActivity = activity;
        if (activity != null) {
            initViews();
            Resources res = activity.getResources();
            this.mActionBarTransY = res.getDimensionPixelSize(R.dimen.contact_editor_head_height);
            this.mContentTransY = res.getDimensionPixelSize(R.dimen.contact_list_animation_content_translation);
        }
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                if (ContactListAnimatorHelper.this.mActivity != null) {
                    TypedArray actionbarSizeTypedArray = ContactListAnimatorHelper.this.mActivity.obtainStyledAttributes(new int[]{16843499});
                    ContactListAnimatorHelper.this.actionBarHeight = actionbarSizeTypedArray.getDimension(0, 0.0f);
                    actionbarSizeTypedArray.recycle();
                }
                ContactListAnimatorHelper.this.mAnimatorSet = new AnimatorSet();
                ContactListAnimatorHelper.this.initMenuAni();
                ContactListAnimatorHelper.this.initActionBarAni();
                ContactListAnimatorHelper.this.initAlphaAni();
                ContactListAnimatorHelper.this.mAnimatorSet.addListener(new AnimatorListener() {
                    public void onAnimationStart(Animator arg0) {
                        ContactListAnimatorHelper.this.hasAnimationEnd = false;
                        HwLog.i(ContactListAnimatorHelper.TAG, "action_bar_container.getBackground() 0:" + ContactListAnimatorHelper.this.action_bar_container.getBackground());
                        ContactListAnimatorHelper.this.mCustomDragView.setBackground(ContactListAnimatorHelper.this.action_bar_container.getBackground());
                        if (ContactListAnimatorHelper.this.mAnimatorListener != null) {
                            ContactListAnimatorHelper.this.mAnimatorListener.onAnimationStart();
                        }
                    }

                    public void onAnimationRepeat(Animator arg0) {
                    }

                    public void onAnimationEnd(Animator arg0) {
                    }

                    public void onAnimationCancel(Animator arg0) {
                    }
                });
            }
        });
    }

    private boolean initViews() {
        if (this.mContactView == null) {
            this.mContactView = (ViewGroup) this.mActivity.findViewById(R.id.contact_list_content);
        }
        if (this.mContactView == null) {
            return false;
        }
        if (this.action_bar_container == null) {
            View decorView = this.mActivity.getWindow().getDecorView();
            this.action_bar_container = decorView.findViewById(16909290);
            addHeader(decorView);
        }
        if ((this.action_bar_container instanceof ViewGroup) && this.mTabContainer == null) {
            this.mTabContainer = ((ViewGroup) this.action_bar_container).getChildAt(2);
        }
        if (this.mContentList == null) {
            this.mContentList = this.mContactView.findViewById(16908298);
        }
        if (this.mSearchView == null) {
            this.mSearchView = this.mContactView.findViewById(R.id.contactListsearchlayout);
        }
        if (this.mEmptyView == null) {
            this.mEmptyView = this.mContactView.findViewById(R.id.no_contacts_screen_layout);
        }
        return true;
    }

    private void addHeader(View decorView) {
        if (this.mActivity != null) {
            this.mCustomDragView = new View(this.mActivity);
            if (decorView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) decorView;
                if (vg.getChildCount() > 0) {
                    View child = vg.getChildAt(0);
                    if (child instanceof ViewGroup) {
                        ((ViewGroup) child).addView(this.mCustomDragView, new LayoutParams(-1, this.mActionBarTransY));
                    }
                }
            }
            this.mCustomDragView.setTranslationY((float) (-this.mActionBarTransY));
            this.mCustomDragView.setVisibility(8);
        }
    }

    private void initMenuAni() {
        if (this.mMenu != null && this.mMenuAnimator == null) {
            this.menuHeight = this.mMenu.getResources().getDimensionPixelSize(R.dimen.contact_split_bar_height);
            this.mMenuAnimator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.menuHeight});
            this.mAnimatorSet.play(this.mMenuAnimator);
            this.mMenuAnimator.setTarget(this.mMenu);
            this.mMenuAnimator.setDuration(200);
            this.mMenuAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mActivity.getApplicationContext(), R.interpolator.cubic_bezier_interpolator_type_70_80));
            this.mMenuAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    ContactListAnimatorHelper.this.mMenu.setTranslationY(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
        }
    }

    private void initActionBarAni() {
        if (this.mTabContainer != null && this.mActionAnimator == null) {
            this.mActionAnimator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mActionBarTransY});
            this.mAnimatorSet.play(this.mActionAnimator);
            this.mActionAnimator.setTarget(this.mTabContainer);
            this.mActionAnimator.setDuration(170);
            this.mActionAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mActivity.getApplicationContext(), R.interpolator.cubic_bezier_interpolator_type_10_90));
            this.mActionAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    ContactListAnimatorHelper.this.action_bar_container.setTranslationY(value);
                    if (ContactListAnimatorHelper.this.mCustomDragView != null) {
                        if (ContactListAnimatorHelper.this.mCustomDragView.getVisibility() == 8) {
                            ContactListAnimatorHelper.this.mCustomDragView.setVisibility(0);
                        }
                        ContactListAnimatorHelper.this.mCustomDragView.setTranslationY(value - ((float) ContactListAnimatorHelper.this.mActionBarTransY));
                    }
                    float transY = (-animation.getAnimatedFraction()) * ((float) ContactListAnimatorHelper.this.mContentTransY);
                    if (ContactListAnimatorHelper.this.mContentList != null) {
                        ContactListAnimatorHelper.this.mContentList.setTranslationY(transY);
                    }
                    if (ContactListAnimatorHelper.this.mSearchView != null) {
                        ContactListAnimatorHelper.this.mSearchView.setTranslationY(transY);
                    }
                    if (ContactListAnimatorHelper.this.mEmptyView != null) {
                        ContactListAnimatorHelper.this.mEmptyView.setTranslationY(transY);
                    }
                }
            });
        }
    }

    private void initAlphaAni() {
        if (this.mTabContainer != null && this.mAlphaAnimator == null) {
            this.mAlphaAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            this.mAnimatorSet.play(this.mAlphaAnimator);
            this.mAlphaAnimator.setDuration(200);
            this.mAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            this.mAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    ContactListAnimatorHelper.this.mTabContainer.setAlpha(value);
                    if (ContactListAnimatorHelper.this.mContentList != null) {
                        ContactListAnimatorHelper.this.mContentList.setAlpha(value);
                    }
                    if (ContactListAnimatorHelper.this.mSearchView != null) {
                        ContactListAnimatorHelper.this.mSearchView.setAlpha(value);
                    }
                    if (ContactListAnimatorHelper.this.mEmptyView != null) {
                        ContactListAnimatorHelper.this.mEmptyView.setAlpha(value);
                    }
                    if (ContactListAnimatorHelper.this.mIsFadeOut && animation.getAnimatedFraction() > 0.9f && !ContactListAnimatorHelper.this.hasAnimationEnd) {
                        ContactListAnimatorHelper.this.hasAnimationEnd = true;
                        HwLog.i(ContactListAnimatorHelper.TAG, "animation.getAnimatedFraction():" + animation.getAnimatedFraction());
                        if (ContactListAnimatorHelper.this.mAnimatorListener != null) {
                            ContactListAnimatorHelper.this.mAnimatorListener.onAnimationEnd();
                        }
                    }
                }
            });
        }
    }

    private void playAnimator(boolean isFadeOut) {
        if (this.mAnimatorSet != null) {
            this.mIsFadeOut = isFadeOut;
            if (isFadeOut) {
                this.mAnimatorSet.start();
            } else {
                prepareAnimateReverse();
                if (this.mActionAnimator != null) {
                    this.mActionAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mActivity.getApplicationContext(), R.interpolator.cubic_bezier_interpolator_type_90_10));
                }
                this.mAnimatorSet.reverse();
            }
        }
    }

    public void prepareAnimateReverse() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "prepareAnimateReverse");
        }
        initViews();
        if (this.mMenu != null) {
            this.mMenu.setTranslationY((float) this.menuHeight);
        }
        if (this.action_bar_container != null) {
            this.action_bar_container.setTranslationY((float) this.mActionBarTransY);
        }
        if (this.mTabContainer != null) {
            this.mTabContainer.setAlpha(0.0f);
        }
        if (this.mContentList != null) {
            this.mContentList.setAlpha(0.0f);
        }
        if (this.mSearchView != null) {
            this.mSearchView.setAlpha(0.0f);
        }
        if (this.mEmptyView != null) {
            this.mEmptyView.setAlpha(0.0f);
        }
    }

    public void reset() {
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "reset");
        }
        if (this.mAnimatorSet != null) {
            if (this.mAnimatorSet.isRunning()) {
                this.mAnimatorSet.cancel();
            }
            if (this.mMenu != null) {
                this.mMenu.setTranslationY(0.0f);
            }
            if (this.mTabContainer != null) {
                this.mTabContainer.setAlpha(1.0f);
            }
            if (this.mCustomDragView != null) {
                this.mCustomDragView.setTranslationY(0.0f);
                this.mCustomDragView.setVisibility(8);
            }
            if (this.action_bar_container != null) {
                this.action_bar_container.setTranslationY(0.0f);
            }
            if (this.mContentList != null) {
                this.mContentList.setTranslationY(0.0f);
                this.mContentList.setAlpha(1.0f);
            }
            if (this.mSearchView != null) {
                this.mSearchView.setTranslationY(0.0f);
                this.mSearchView.setAlpha(1.0f);
            }
            if (this.mEmptyView != null) {
                this.mEmptyView.setTranslationY(0.0f);
                this.mEmptyView.setAlpha(1.0f);
            }
        }
    }

    public void play(boolean isFadeOut) {
        if (initViews()) {
            initActionBarAni();
            initAlphaAni();
            if (HwLog.HWFLOW) {
                HwLog.i(TAG, "isFadeOut:" + isFadeOut + " mMenu.getTranslationY():" + this.mMenu.getTranslationY());
                HwLog.i(TAG, "mContentList:" + this.mContentList + " mSearchView:" + this.mSearchView);
            }
            playAnimator(isFadeOut);
            return;
        }
        if (this.mAnimatorListener != null) {
            this.mAnimatorListener.onAnimationEnd();
        }
    }
}
