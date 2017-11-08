package com.android.systemui.recents.tv.views;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.tv.RecentsTvActivity;
import com.android.systemui.recents.tv.animations.DismissAnimationsHolder;
import com.android.systemui.recents.tv.animations.RecentsRowFocusAnimationHolder;
import com.android.systemui.recents.tv.animations.ViewFocusAnimator;

public class TaskCardView extends LinearLayout {
    private ImageView mBadgeView;
    private int mCornerRadius;
    private DismissAnimationsHolder mDismissAnimationsHolder;
    private View mDismissIconView;
    private boolean mDismissState;
    private View mInfoFieldView;
    private RecentsRowFocusAnimationHolder mRecentsRowFocusAnimationHolder;
    private Task mTask;
    private View mThumbnailView;
    private TextView mTitleTextView;
    private boolean mTouchExplorationEnabled;
    private ViewFocusAnimator mViewFocusAnimator;

    public TaskCardView(Context context) {
        this(context, null);
    }

    public TaskCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDismissState = false;
        setLayoutDirection(getResources().getConfiguration().getLayoutDirection());
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mThumbnailView = findViewById(R.id.card_view_thumbnail);
        this.mInfoFieldView = findViewById(R.id.card_info_field);
        this.mTitleTextView = (TextView) findViewById(R.id.card_title_text);
        this.mBadgeView = (ImageView) findViewById(R.id.card_extra_badge);
        this.mDismissIconView = findViewById(R.id.dismiss_icon);
        this.mDismissAnimationsHolder = new DismissAnimationsHolder(this);
        this.mCornerRadius = getResources().getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mRecentsRowFocusAnimationHolder = new RecentsRowFocusAnimationHolder(this, this.mInfoFieldView);
        this.mTouchExplorationEnabled = Recents.getSystemServices().isTouchExplorationEnabled();
        if (this.mTouchExplorationEnabled) {
            this.mDismissIconView.setVisibility(8);
        } else {
            this.mDismissIconView.setVisibility(0);
        }
        this.mViewFocusAnimator = new ViewFocusAnimator(this);
    }

    public void init(Task task) {
        this.mTask = task;
        this.mTitleTextView.setText(task.title);
        this.mBadgeView.setImageDrawable(task.icon);
        setThumbnailView();
        setContentDescription(task.titleDescription);
        this.mDismissState = false;
        this.mDismissAnimationsHolder.reset();
        this.mRecentsRowFocusAnimationHolder.reset();
    }

    public Task getTask() {
        return this.mTask;
    }

    public void getFocusedRect(Rect r) {
        this.mThumbnailView.getFocusedRect(r);
    }

    public Rect getFocusedThumbnailRect() {
        Rect r = new Rect();
        this.mThumbnailView.getGlobalVisibleRect(r);
        return r;
    }

    public static Rect getStartingCardThumbnailRect(Context context, boolean hasFocus, int numberOfTasks) {
        if (numberOfTasks > 1) {
            return getStartingCardThumbnailRectForStartPosition(context, hasFocus);
        }
        return getStartingCardThumbnailRectForFocusedPosition(context, hasFocus);
    }

    private static Rect getStartingCardThumbnailRectForStartPosition(Context context, boolean hasFocus) {
        Resources res = context.getResources();
        int width = res.getDimensionPixelOffset(R.dimen.recents_tv_card_width);
        int totalSpacing = res.getDimensionPixelOffset(R.dimen.recents_tv_gird_card_spacing) * 2;
        if (hasFocus) {
            totalSpacing += res.getDimensionPixelOffset(R.dimen.recents_tv_gird_focused_card_delta);
        }
        int height = res.getDimensionPixelOffset(R.dimen.recents_tv_screenshot_height);
        int topMargin = res.getDimensionPixelOffset(R.dimen.recents_tv_gird_row_top_margin);
        int headerHeight = res.getDimensionPixelOffset(R.dimen.recents_tv_card_extra_badge_size) + res.getDimensionPixelOffset(R.dimen.recents_tv_icon_padding_bottom);
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        return new Rect(((screenWidth / 2) + (width / 2)) + totalSpacing, topMargin + headerHeight, (((screenWidth / 2) + (width / 2)) + totalSpacing) + width, (topMargin + headerHeight) + height);
    }

    private static Rect getStartingCardThumbnailRectForFocusedPosition(Context context, boolean hasFocus) {
        Resources res = context.getResources();
        TypedValue out = new TypedValue();
        res.getValue(R.integer.selected_scale, out, true);
        float scale = hasFocus ? out.getFloat() : 1.0f;
        int width = res.getDimensionPixelOffset(R.dimen.recents_tv_card_width);
        int widthDelta = (int) ((((float) width) * scale) - ((float) width));
        int height = res.getDimensionPixelOffset(R.dimen.recents_tv_screenshot_height);
        int heightDelta = (int) ((((float) height) * scale) - ((float) height));
        int topMargin = res.getDimensionPixelOffset(R.dimen.recents_tv_gird_row_top_margin);
        int headerHeight = res.getDimensionPixelOffset(R.dimen.recents_tv_card_extra_badge_size) + res.getDimensionPixelOffset(R.dimen.recents_tv_icon_padding_bottom);
        int dismissAreaHeight = ((res.getDimensionPixelOffset(R.dimen.recents_tv_dismiss_icon_top_margin) + res.getDimensionPixelOffset(R.dimen.recents_tv_dismiss_icon_bottom_margin)) + res.getDimensionPixelOffset(R.dimen.recents_tv_dismiss_icon_size)) + res.getDimensionPixelOffset(R.dimen.recents_tv_dismiss_text_size);
        int totalHeightDelta = (heightDelta + ((int) ((((float) headerHeight) * scale) - ((float) headerHeight)))) + ((int) ((((float) dismissAreaHeight) * scale) - ((float) dismissAreaHeight)));
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        return new Rect(((screenWidth / 2) - (width / 2)) - (widthDelta / 2), (topMargin - (totalHeightDelta / 2)) + ((int) (((float) headerHeight) * scale)), ((screenWidth / 2) + (width / 2)) + (widthDelta / 2), ((topMargin - (totalHeightDelta / 2)) + ((int) (((float) headerHeight) * scale))) + ((int) (((float) height) * scale)));
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 19:
                if (event.getAction() == 0) {
                    if (isInDismissState()) {
                        setDismissState(false);
                    } else {
                        ((RecentsTvActivity) getContext()).requestPipControlsFocus();
                    }
                }
                return true;
            case 20:
                if (!isInDismissState() && event.getAction() == 0) {
                    setDismissState(true);
                    return true;
                }
            case 21:
            case 22:
                if (isInDismissState()) {
                    return true;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    private void setDismissState(boolean dismissState) {
        if (this.mDismissState != dismissState) {
            this.mDismissState = dismissState;
            if (!this.mTouchExplorationEnabled) {
                if (dismissState) {
                    this.mDismissAnimationsHolder.startEnterAnimation();
                } else {
                    this.mDismissAnimationsHolder.startExitAnimation();
                }
            }
        }
    }

    public boolean isInDismissState() {
        return this.mDismissState;
    }

    public void startDismissTaskAnimation(AnimatorListener listener) {
        this.mDismissState = false;
        this.mDismissAnimationsHolder.startDismissAnimation(listener);
    }

    public ViewFocusAnimator getViewFocusAnimator() {
        return this.mViewFocusAnimator;
    }

    public RecentsRowFocusAnimationHolder getRecentsRowFocusAnimationHolder() {
        return this.mRecentsRowFocusAnimationHolder;
    }

    private void setThumbnailView() {
        ImageView screenshotView = (ImageView) findViewById(R.id.card_view_banner_icon);
        PackageManager pm = getContext().getPackageManager();
        if (this.mTask.thumbnail != null) {
            setAsScreenShotView(this.mTask.thumbnail, screenshotView);
            return;
        }
        Drawable banner = null;
        try {
            if (this.mTask.key != null) {
                banner = pm.getActivityBanner(this.mTask.key.baseIntent);
            }
            if (banner != null) {
                setAsBannerView(banner, screenshotView);
            } else {
                setAsIconView(this.mTask.icon, screenshotView);
            }
        } catch (NameNotFoundException e) {
            Log.e("TaskCardView", "Package not found : " + e);
            setAsIconView(this.mTask.icon, screenshotView);
        }
    }

    private void setAsScreenShotView(Bitmap screenshot, ImageView screenshotView) {
        LayoutParams lp = (LayoutParams) screenshotView.getLayoutParams();
        lp.width = -1;
        lp.height = -1;
        screenshotView.setLayoutParams(lp);
        screenshotView.setClipToOutline(true);
        screenshotView.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) TaskCardView.this.mCornerRadius);
            }
        });
        screenshotView.setImageBitmap(screenshot);
    }

    private void setAsBannerView(Drawable banner, ImageView bannerView) {
        LayoutParams lp = (LayoutParams) bannerView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.recents_tv_banner_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.recents_tv_banner_height);
        bannerView.setLayoutParams(lp);
        bannerView.setImageDrawable(banner);
    }

    private void setAsIconView(Drawable icon, ImageView iconView) {
        LayoutParams lp = (LayoutParams) iconView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.recents_tv_fallback_icon_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.recents_tv_fallback_icon_height);
        iconView.setLayoutParams(lp);
        iconView.setImageDrawable(icon);
    }

    public View getThumbnailView() {
        return this.mThumbnailView;
    }

    public View getInfoFieldView() {
        return this.mInfoFieldView;
    }

    public View getDismissIconView() {
        return this.mDismissIconView;
    }

    public static int getNumberOfVisibleTasks(Context context) {
        Resources res = context.getResources();
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return (int) (Math.ceil(((double) size.x) / (((double) res.getDimensionPixelSize(R.dimen.recents_tv_card_width)) + (((double) res.getDimensionPixelSize(R.dimen.recents_tv_gird_card_spacing)) * 2.0d))) + 1.0d);
    }
}
