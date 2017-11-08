package com.huawei.gallery.actionbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.HwWidgetFactoryWrapper;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.Wrapper;
import com.android.gallery3d.util.Wrapper.ReflectCaller;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ActionBarContainerManager {
    public static final ReflectCaller sReflectCaller = new ReflectCaller() {
        public Object run(Object[] para) throws IllegalArgumentException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
            para[0].getClass().getMethod(para[1].toString(), new Class[]{Boolean.TYPE}).invoke(para[0], new Object[]{para[2]});
            return null;
        }
    };
    private final ActionBar mActionBar;
    private final AbstractGalleryActivity mActivity;
    private final int mBarHeightPort;
    private final int mDarkThemeId;
    private boolean mDelayToSetStyle = false;
    private View mFootActionContainer;
    private AnimatorSet mFootAnimatorSet;
    private final View mFootBackgroundView;
    private final int mFwkToolbarResId;
    private final Handler mHandler = new Handler();
    private View mHeadActionContainer;
    private AnimatorSet mHeadAnimatorSet;
    private Drawable mHeadBackgroundDefaultDrawable;
    private final View mHeadBackgroundView;
    private boolean mHeadBackgroundVisible = true;
    public Boolean mIsFootVisible = null;
    public Boolean mIsHeadVisible = null;
    private final ActionBarMenuManager mMenuManager;
    private boolean mScrollTabAnimationIgnoreDisable = false;
    private int mStyle = 0;
    private final int mThemeId;
    private boolean mTransitionAnimationIgnoreDisable = false;

    public ActionBarContainerManager(AbstractGalleryActivity activity) {
        this.mActivity = activity;
        this.mActionBar = this.mActivity.getActionBar();
        this.mMenuManager = new ActionBarMenuManager(this.mActivity);
        this.mHeadBackgroundView = this.mActivity.findViewById(R.id.gallery_head_background);
        this.mFootBackgroundView = this.mActivity.findViewById(R.id.gallery_foot_background);
        this.mDarkThemeId = this.mActivity.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dark", null, null);
        this.mThemeId = this.mActivity.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        this.mFwkToolbarResId = this.mActivity.getResources().getIdentifier("androidhwext:drawable/toolbar_bg", null, null);
        this.mHeadBackgroundDefaultDrawable = getHeadBarBackgroundDrawable();
        this.mBarHeightPort = this.mActivity.getResources().getDimensionPixelSize(this.mActivity.getResources().getIdentifier("navigation_bar_height", "dimen", "android"));
        LayoutParams layoutParams = this.mFootBackgroundView.getLayoutParams();
        layoutParams.height = this.mBarHeightPort;
        this.mFootBackgroundView.setLayoutParams(layoutParams);
    }

    public Drawable getHeadBarBackgroundDrawable() {
        int color = new HwWidgetFactoryWrapper().getPrimaryColor(this.mActivity);
        int lineSplitBottomEmuiId = this.mActivity.getResources().getIdentifier("androidhwext:drawable/line_split_bottom_emui", null, null);
        return new LayerDrawable(new Drawable[]{new ColorDrawable(color), this.mActivity.getResources().getDrawable(lineSplitBottomEmuiId)});
    }

    public void setDefaultBackground(Drawable drawable) {
        this.mHeadBackgroundDefaultDrawable = drawable;
    }

    public void setHeadView(View view) {
        this.mActionBar.setCustomView(view);
    }

    public ActionBarMenuManager getMenuManager() {
        return this.mMenuManager;
    }

    public boolean isActionPanelVisible() {
        return this.mIsFootVisible != null ? this.mIsFootVisible.booleanValue() : true;
    }

    public boolean isHeadBarVisible() {
        return this.mIsHeadVisible != null ? this.mIsHeadVisible.booleanValue() : true;
    }

    public void hideFootActionContainer() {
        setShoudTransition(false, false);
        setActionPanelVisibleWithFadeInAnimationIfNeed(false, false);
    }

    public void hideHeadActionContainer() {
        setShoudTransition(false, false);
        setHeadBarVisibleWithFadeInAnimationIfNeed(false, false);
    }

    public void resetHeadAndFootActionContainer() {
        if (this.mHeadActionContainer == null || this.mFootActionContainer == null) {
            GalleryLog.d("ActionBarContainerManager", "container not initialize!");
            return;
        }
        this.mHeadActionContainer.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        this.mFootActionContainer.setAlpha(WMElement.CAMERASIZEVALUE1B1);
    }

    public void setHeadBackgroundVisible(boolean visible) {
        this.mHeadBackgroundVisible = visible;
    }

    public int getStyle() {
        return this.mStyle;
    }

    public void setStyle(int style) {
        this.mStyle = style;
        if (this.mHeadActionContainer == null || this.mFootActionContainer == null) {
            this.mDelayToSetStyle = true;
            GalleryLog.d("ActionBarContainerManager", "container doesn't initialize finish, delay to set style.");
            return;
        }
        LayoutParams footActionContainerPara = this.mFootActionContainer.getLayoutParams();
        if (style == 1) {
            this.mActionBar.setBackgroundDrawable(null);
            setShoudTransition(false, false);
            this.mActionBar.setSplitBackgroundDrawable(this.mActivity.getDrawable(R.drawable.split_bar_background));
            footActionContainerPara.width = -1;
        } else if (style == 0) {
            if (this.mActionBar.getNavigationMode() != 2 || isActionMenuOnTop()) {
                this.mActionBar.setBackgroundDrawable(this.mHeadBackgroundDefaultDrawable);
            } else {
                this.mActionBar.setStackedBackgroundDrawable(this.mHeadBackgroundDefaultDrawable);
                if (this.mMenuManager.getStyle() != style) {
                    this.mActionBar.setBackgroundDrawable(this.mHeadBackgroundDefaultDrawable);
                }
            }
            this.mActionBar.setSplitBackgroundDrawable(this.mActivity.getResources().getDrawable(this.mFwkToolbarResId));
            footActionContainerPara.width = -2;
        }
        this.mFootActionContainer.setLayoutParams(footActionContainerPara);
        if (style == 1) {
            this.mActivity.getTheme().applyStyle(this.mDarkThemeId, true);
        } else if (style == 0) {
            this.mActivity.getTheme().applyStyle(this.mThemeId, true);
        }
        this.mMenuManager.setStyle(style);
        refreshBackGroundVisibility();
        refreshStatusBarAndNavigationBar();
    }

    private void refreshStatusBarAndNavigationBar() {
        Window window = this.mActivity.getWindow();
        UIUtils.setStatusBarColor(window, UIUtils.getStatusBarColor(window));
        UIUtils.setNavigationBarColor(window, UIUtils.getNavigationBarColor(window));
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mActionBar.addOnMenuVisibilityListener(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mActionBar.removeOnMenuVisibilityListener(listener);
    }

    public void onCreateOptionsMenu(Menu menu) {
        int i;
        int i2 = 8;
        this.mMenuManager.inflaterMenu(menu);
        if (this.mHeadActionContainer == null) {
            this.mHeadActionContainer = this.mActivity.getWindow().getDecorView().findViewById(16909290);
        }
        View view = this.mHeadActionContainer;
        if (isHeadBarVisible()) {
            i = 0;
        } else {
            i = 8;
        }
        view.setVisibility(i);
        if (this.mFootActionContainer == null) {
            this.mFootActionContainer = this.mActivity.getWindow().getDecorView().findViewById(16909293);
        }
        if (this.mDelayToSetStyle) {
            setStyle(this.mStyle);
            this.mDelayToSetStyle = false;
        }
        View view2 = this.mFootActionContainer;
        if (isActionPanelVisible()) {
            i2 = 0;
        }
        view2.setVisibility(i2);
        if (this.mFootActionContainer instanceof ViewGroup) {
            View childView = ((ViewGroup) this.mFootActionContainer).getChildAt(0);
            if (childView != null && (childView.getLayoutParams() instanceof FrameLayout.LayoutParams)) {
                FrameLayout.LayoutParams para = (FrameLayout.LayoutParams) childView.getLayoutParams();
                para.gravity = 1;
                childView.setLayoutParams(para);
            }
        }
        refreshBackGroundVisibility();
    }

    public void onOptionsItemSelected(MenuItem item) {
        this.mMenuManager.onOptionsItemSelected(item);
    }

    public void setMenuClickable(boolean clickable) {
        this.mMenuManager.setMenuClickable(clickable);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mMenuManager.onConfigurationChanged(newConfig);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                ActionBarContainerManager.this.refreshBackGroundVisibility();
            }
        }, 250);
    }

    public void onNavigationBarChanged(boolean show) {
        int i = 0;
        this.mFootBackgroundView.setTranslationY((float) (show ? 0 : this.mBarHeightPort));
        if (this.mFootActionContainer != null) {
            if (MultiWindowStatusHolder.isInMultiMaintained()) {
                this.mFootBackgroundView.setVisibility(8);
                return;
            }
            if (!isActionMenuOnTop() && (this.mFootActionContainer.getLayoutParams() instanceof MarginLayoutParams)) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) this.mFootActionContainer.getLayoutParams();
                int i2 = layoutParams.leftMargin;
                int i3 = layoutParams.topMargin;
                int i4 = layoutParams.rightMargin;
                if (show) {
                    i = this.mBarHeightPort;
                }
                layoutParams.setMargins(i2, i3, i4, i);
                this.mFootActionContainer.setLayoutParams(layoutParams);
            }
        }
    }

    private boolean isActionMenuOnTop() {
        return this.mActivity.getResources().getConfiguration().orientation == 2 && !MultiWindowStatusHolder.isInMultiMaintained();
    }

    private void refreshBackGroundVisibility() {
        boolean isFootVisible = false;
        boolean z = (this.mHeadActionContainer != null && this.mHeadActionContainer.getVisibility() == 0) ? this.mHeadBackgroundVisible : false;
        if (this.mFootActionContainer != null && this.mFootActionContainer.getVisibility() == 0) {
            isFootVisible = true;
        }
        refreshBackGroundVisibility(z, isFootVisible);
    }

    private void refreshBackGroundVisibility(boolean suggestHeadVisible, boolean suggestFootVisible) {
        int i = 0;
        if (this.mHeadBackgroundView != null) {
            int i2;
            boolean isHeadBgVisible = suggestHeadVisible && this.mMenuManager.getStyle() == 1;
            View view = this.mHeadBackgroundView;
            if (isHeadBgVisible) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            view.setVisibility(i2);
        }
        if (this.mFootBackgroundView != null) {
            boolean isFootBgVisible = suggestFootVisible && this.mMenuManager.getStyle() == 1 && !isActionMenuOnTop();
            View view2 = this.mFootBackgroundView;
            if (!isFootBgVisible || MultiWindowStatusHolder.isInMultiMaintained()) {
                i = 8;
            }
            view2.setVisibility(i);
        }
    }

    private void setViewVisibilityWithFadeInAnimationIfNeed(View[] views, boolean visible, boolean isUpToBelow) {
        startAnimationForView(views, visible, isUpToBelow ? UIUtils.getActionBarHeight(this.mActivity) : UIUtils.getFootBarHeight(this.mActivity), isUpToBelow);
    }

    private boolean needDoAnime(boolean isUpToBelow, boolean visible) {
        boolean z = true;
        if (isUpToBelow) {
            if (this.mIsHeadVisible != null && this.mIsHeadVisible.booleanValue() == visible) {
                z = false;
            }
            return z;
        }
        if (this.mIsFootVisible != null && this.mIsFootVisible.booleanValue() == visible) {
            z = false;
        }
        return z;
    }

    private void startAnimationForView(final View[] views, final boolean visible, int defautHeight, final boolean isUpToBelow) {
        if (needDoAnime(isUpToBelow, visible)) {
            cancelAnimation(isUpToBelow);
            setOptionMenuVisibleTrue(visible, isUpToBelow);
            ArrayList<Animator> animators = new ArrayList();
            int length = views.length;
            int i = 0;
            while (i < length) {
                View view = views[i];
                if (view != null) {
                    view.setVisibility(0);
                    animators.add(createItemAnimatorAlpha(view, 350, visible));
                    i++;
                } else {
                    return;
                }
            }
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.playTogether(animators);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    for (View view : views) {
                        int i;
                        if (visible) {
                            i = 0;
                        } else {
                            i = 8;
                        }
                        view.setVisibility(i);
                    }
                    ActionBarContainerManager.this.setOptionMenuVisibleFalse(visible, isUpToBelow);
                    if (MultiWindowStatusHolder.isInMultiMaintained()) {
                        ActionBarContainerManager.this.mFootBackgroundView.setVisibility(8);
                    }
                    super.onAnimationEnd(animation);
                }
            });
            animatorSet.start();
            if (isUpToBelow) {
                this.mHeadAnimatorSet = animatorSet;
            } else {
                this.mFootAnimatorSet = animatorSet;
            }
        }
    }

    private void cancelAnimation(boolean isUpToBelow) {
        if (isUpToBelow) {
            if (this.mHeadAnimatorSet != null && this.mHeadAnimatorSet.isRunning()) {
                this.mHeadAnimatorSet.cancel();
            }
        } else if (this.mFootAnimatorSet != null && this.mFootAnimatorSet.isRunning()) {
            this.mFootAnimatorSet.cancel();
        }
    }

    private void setOptionMenuVisibleTrue(boolean visible, boolean isUpToBelow) {
        if (!visible) {
            return;
        }
        if (isUpToBelow && isActionMenuOnTop()) {
            this.mMenuManager.setOptionMenuVisible(true);
        } else if (!isUpToBelow && !isActionMenuOnTop()) {
            this.mMenuManager.setOptionMenuVisible(true);
        }
    }

    private void setOptionMenuVisibleFalse(boolean visible, boolean isUpToBelow) {
        if (!visible) {
            if (isUpToBelow && isActionMenuOnTop()) {
                this.mMenuManager.setOptionMenuVisible(false);
            } else if (!isUpToBelow && !isActionMenuOnTop()) {
                this.mMenuManager.setOptionMenuVisible(false);
            }
        }
    }

    private static Animator createItemAnimatorAlpha(View target, int duration, boolean visible) {
        float fromYDelta = visible ? 0.0f : WMElement.CAMERASIZEVALUE1B1;
        float toYDelta = visible ? WMElement.CAMERASIZEVALUE1B1 : 0.0f;
        Animator anim = ObjectAnimator.ofFloat(target, "alpha", new float[]{fromYDelta, toYDelta});
        anim.setDuration((long) duration);
        return anim;
    }

    public void setHeadBarVisibleWithFadeInAnimationIfNeed(boolean visible, boolean withAnimation) {
        int i = 0;
        if (this.mHeadActionContainer == null) {
            this.mIsHeadVisible = Boolean.valueOf(visible);
            return;
        }
        if (withAnimation && this.mMenuManager.getStyle() == 1 && !MultiWindowStatusHolder.isInMultiMaintained()) {
            setViewVisibilityWithFadeInAnimationIfNeed(new View[]{this.mHeadBackgroundView, this.mHeadActionContainer}, visible, true);
        } else {
            int i2;
            if (this.mHeadAnimatorSet != null && this.mHeadAnimatorSet.isRunning()) {
                this.mHeadAnimatorSet.cancel();
            }
            View view = this.mHeadActionContainer;
            if (visible) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            view.setVisibility(i2);
            View view2 = this.mHeadBackgroundView;
            if (!(visible && this.mMenuManager.getStyle() == 1)) {
                i = 8;
            }
            view2.setVisibility(i);
        }
        this.mIsHeadVisible = Boolean.valueOf(visible);
    }

    public void setActionPanelVisibleWithFadeInAnimationIfNeed(boolean visible, boolean withAnimation) {
        int i = 0;
        if (this.mFootActionContainer == null) {
            this.mIsFootVisible = Boolean.valueOf(visible);
            this.mMenuManager.setOptionMenuVisible(visible);
        } else if (isActionMenuOnTop()) {
            this.mFootBackgroundView.setVisibility(8);
            this.mFootActionContainer.setVisibility(8);
            this.mIsFootVisible = Boolean.valueOf(visible);
            if (!(withAnimation && this.mMenuManager.getStyle() == 1)) {
                this.mMenuManager.setOptionMenuVisible(visible);
            }
        } else {
            if (withAnimation && this.mMenuManager.getStyle() == 1 && !MultiWindowStatusHolder.isInMultiMaintained()) {
                setViewVisibilityWithFadeInAnimationIfNeed(new View[]{this.mFootActionContainer, this.mFootBackgroundView}, visible, false);
            } else {
                int i2;
                if (this.mFootAnimatorSet != null && this.mFootAnimatorSet.isRunning()) {
                    this.mFootAnimatorSet.cancel();
                }
                View view = this.mFootActionContainer;
                if (visible) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                view.setVisibility(i2);
                View view2 = this.mFootBackgroundView;
                if (!(visible && this.mMenuManager.getStyle() == 1 && !MultiWindowStatusHolder.isInMultiMaintained())) {
                    i = 8;
                }
                view2.setVisibility(i);
                this.mMenuManager.setOptionMenuVisible(visible);
            }
            this.mIsFootVisible = Boolean.valueOf(visible);
        }
    }

    public void setShoudTransition(boolean enabled, boolean fromDisable) {
        if (!fromDisable || !this.mTransitionAnimationIgnoreDisable) {
            if (!fromDisable) {
                this.mTransitionAnimationIgnoreDisable = true;
            }
            Wrapper.runCaller(sReflectCaller, this.mActionBar, "setShoudTransition", Boolean.valueOf(enabled));
            Wrapper.runCaller(sReflectCaller, this.mActionBar, "setAnimationEnable", Boolean.valueOf(enabled));
        }
    }

    public void setScrollTabAnimation(boolean enabled, boolean fromDisable) {
        if (!fromDisable || !this.mScrollTabAnimationIgnoreDisable) {
            if (!fromDisable) {
                this.mScrollTabAnimationIgnoreDisable = true;
            }
            Wrapper.runCaller(sReflectCaller, this.mActionBar, "setScrollTabAnimEnable", Boolean.valueOf(enabled));
        }
    }
}
