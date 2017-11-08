package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.support.v4.graphics.ColorUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewDebug.ExportedProperty;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.recents.HwRecentsLockUtils;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.ui.HwLockTaskViewEvent;
import com.android.systemui.recents.events.ui.HwTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.List;
import java.util.Locale;

public class TaskViewHeader extends FrameLayout implements OnClickListener, OnLongClickListener {
    ImageView mAppIconView;
    ImageView mAppInfoView;
    FrameLayout mAppOverlayView;
    TextView mAppTitleView;
    private HighlightColorDrawable mBackground;
    int mCornerRadius;
    Drawable mDarkFreeformIcon;
    Drawable mDarkFullscreenIcon;
    @ExportedProperty(category = "recents")
    float mDimAlpha;
    private Paint mDimLayerPaint;
    int mDisabledTaskBarBackgroundColor;
    ImageView mDismissButton;
    private CountDownTimer mFocusTimerCountDown;
    ProgressBar mFocusTimerIndicator;
    int mFreeformBaseHeight;
    int mFreeformSmallestLength;
    int mHeaderBarHeight;
    int mHeaderButtonPadding;
    int mHighlightHeight;
    ImageView mIconView;
    Drawable mLightFreeformIcon;
    Drawable mLightFullscreenIcon;
    ImageView mLockTaskButton;
    private final int mMaxFloatingWindows;
    ImageView mMoveTaskButton;
    int mMoveTaskTargetStackId;
    ImageView mMusicTaskButton;
    private HighlightColorDrawable mOverlayBackground;
    Task mTask;
    int mTaskBarViewDarkTextColor;
    int mTaskBarViewLightTextColor;
    @ExportedProperty(category = "recents")
    protected Rect mTaskViewRect;
    TextView mTitleView;
    private float[] mTmpHSL;

    private class HighlightColorDrawable extends Drawable {
        private Paint mBackgroundPaint = new Paint();
        private int mColor;
        private float mDimAlpha;
        private Paint mHighlightPaint = new Paint();

        public HighlightColorDrawable() {
            this.mBackgroundPaint.setColor(Color.argb(255, 0, 0, 0));
            this.mBackgroundPaint.setAntiAlias(true);
            this.mHighlightPaint.setColor(Color.argb(255, 255, 255, 255));
            this.mHighlightPaint.setAntiAlias(true);
        }

        public void setColorAndDim(int color, float dimAlpha) {
            if (this.mColor != color || Float.compare(this.mDimAlpha, dimAlpha) != 0) {
                this.mColor = color;
                this.mDimAlpha = dimAlpha;
                this.mBackgroundPaint.setColor(color);
                ColorUtils.colorToHSL(color, TaskViewHeader.this.mTmpHSL);
                TaskViewHeader.this.mTmpHSL[2] = Math.min(1.0f, TaskViewHeader.this.mTmpHSL[2] + ((1.0f - dimAlpha) * 0.075f));
                this.mHighlightPaint.setColor(ColorUtils.HSLToColor(TaskViewHeader.this.mTmpHSL));
                invalidateSelf();
            }
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public void setAlpha(int alpha) {
        }

        public void draw(Canvas canvas) {
            canvas.drawRoundRect(0.0f, 0.0f, (float) TaskViewHeader.this.mTaskViewRect.width(), (float) (Math.max(TaskViewHeader.this.mHighlightHeight, TaskViewHeader.this.mCornerRadius) * 2), (float) TaskViewHeader.this.mCornerRadius, (float) TaskViewHeader.this.mCornerRadius, this.mHighlightPaint);
            canvas.drawRoundRect(0.0f, (float) TaskViewHeader.this.mHighlightHeight, (float) TaskViewHeader.this.mTaskViewRect.width(), (float) (TaskViewHeader.this.getHeight() + TaskViewHeader.this.mCornerRadius), (float) TaskViewHeader.this.mCornerRadius, (float) TaskViewHeader.this.mCornerRadius, this.mBackgroundPaint);
        }

        public int getOpacity() {
            return -1;
        }

        public int getColor() {
            return this.mColor;
        }
    }

    public TaskViewHeader(Context context) {
        this(context, null);
    }

    public TaskViewHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTaskViewRect = new Rect();
        this.mMoveTaskTargetStackId = -1;
        this.mTmpHSL = new float[3];
        this.mDimLayerPaint = new Paint();
        setWillNotDraw(false);
        Resources res = context.getResources();
        this.mCornerRadius = res.getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mHighlightHeight = res.getDimensionPixelSize(R.dimen.recents_task_view_highlight);
        this.mTaskBarViewLightTextColor = context.getColor(R.color.recents_task_bar_light_text_color);
        this.mTaskBarViewDarkTextColor = context.getColor(R.color.recents_task_bar_dark_text_color);
        this.mLightFreeformIcon = context.getDrawable(R.drawable.recents_move_task_freeform_light);
        this.mDarkFreeformIcon = context.getDrawable(R.drawable.recents_move_task_freeform_dark);
        this.mLightFullscreenIcon = context.getDrawable(R.drawable.recents_move_task_fullscreen_light);
        this.mDarkFullscreenIcon = context.getDrawable(R.drawable.recents_move_task_fullscreen_dark);
        this.mDisabledTaskBarBackgroundColor = context.getColor(R.color.recents_task_bar_disabled_background_color);
        this.mBackground = new HighlightColorDrawable();
        this.mBackground.setColorAndDim(Color.argb(255, 0, 0, 0), 0.0f);
        setBackground(this.mBackground);
        this.mOverlayBackground = new HighlightColorDrawable();
        this.mDimLayerPaint.setColor(Color.argb(255, 0, 0, 0));
        this.mDimLayerPaint.setAntiAlias(true);
        this.mMaxFloatingWindows = SystemProperties.getInt("ro.config.hw_max_floating_wins", res.getInteger(R.integer.config_max_floating_wins));
        obtainFreeformParams();
    }

    private void obtainFreeformParams() {
        Resources res = this.mContext.getResources();
        float minimalPercent = res.getFraction(34668544, 1, 1);
        int navBar = this.mContext.getResources().getDimensionPixelSize(R.dimen.navigation_bar_height);
        int statusBar = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        WindowManager windowMgr = (WindowManager) this.mContext.getSystemService("window");
        Point screenDims = new Point();
        windowMgr.getDefaultDisplay().getRealSize(screenDims);
        this.mFreeformBaseHeight = (screenDims.y - statusBar) - navBar;
        if (res.getConfiguration().orientation == 2 && !isSw600Dp()) {
            this.mFreeformBaseHeight = screenDims.y - statusBar;
        }
        this.mFreeformSmallestLength = (int) (((float) (screenDims.x > screenDims.y ? screenDims.y : screenDims.x)) * minimalPercent);
    }

    private boolean isSw600Dp() {
        return this.mContext.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public void reset() {
        hideAppOverlay(true);
    }

    protected void onFinishInflate() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        this.mIconView = (ImageView) findViewById(R.id.icon);
        this.mIconView.setOnLongClickListener(this);
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mDismissButton = (ImageView) findViewById(R.id.dismiss_task);
        if (ssp.hasFreeformWorkspaceSupport()) {
            this.mMoveTaskButton = (ImageView) findViewById(R.id.move_task);
        }
        this.mLockTaskButton = (ImageView) findViewById(R.id.lock_task);
        this.mMusicTaskButton = (ImageView) findViewById(R.id.music_task);
        onConfigurationChanged();
    }

    private void updateLayoutParams(View icon, View title, View secondaryButton, View thirdButton, View forthButton, View button) {
        int i;
        setLayoutParams(new LayoutParams(-1, this.mHeaderBarHeight, 48));
        icon.setLayoutParams(new LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388611));
        LayoutParams lp = new LayoutParams(-1, -2, 8388627);
        lp.setMarginStart(this.mHeaderBarHeight);
        if (HwRecentsHelper.getPlayingMusicUid(getContext(), this.mTask)) {
            if (this.mMoveTaskButton != null) {
                i = this.mHeaderBarHeight * 4;
            } else {
                i = this.mHeaderBarHeight * 3;
            }
            lp.setMarginEnd(i);
        } else {
            if (this.mMoveTaskButton != null) {
                i = this.mHeaderBarHeight * 3;
            } else {
                i = this.mHeaderBarHeight * 2;
            }
            lp.setMarginEnd(i);
        }
        title.setLayoutParams(lp);
        if (secondaryButton != null) {
            lp = new LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613);
            lp.setMarginEnd(this.mHeaderBarHeight);
            secondaryButton.setLayoutParams(lp);
            secondaryButton.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
        }
        if (thirdButton != null) {
            lp = new LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613);
            lp.setMarginEnd(this.mHeaderBarHeight * 2);
            thirdButton.setLayoutParams(lp);
            thirdButton.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
        }
        if (forthButton != null) {
            lp = new LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613);
            if (this.mMoveTaskButton != null) {
                i = this.mHeaderBarHeight * 3;
            } else {
                i = this.mHeaderBarHeight * 2;
            }
            lp.setMarginEnd(i);
            forthButton.setLayoutParams(lp);
            forthButton.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
        }
        button.setLayoutParams(new LayoutParams(this.mHeaderBarHeight, this.mHeaderBarHeight, 8388613));
        button.setPadding(this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding, this.mHeaderButtonPadding);
    }

    public void onConfigurationChanged() {
        Resources res = getResources();
        this.mTaskBarViewLightTextColor = getContext().getColor(R.color.recents_task_bar_light_text_color);
        this.mTaskBarViewDarkTextColor = getContext().getColor(R.color.recents_task_bar_dark_text_color);
        int headerBarHeight = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land);
        int headerButtonPadding = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding_tablet_land, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding_tablet_land);
        if (!(headerBarHeight == this.mHeaderBarHeight && headerButtonPadding == this.mHeaderButtonPadding)) {
            this.mHeaderBarHeight = headerBarHeight;
            this.mHeaderButtonPadding = headerButtonPadding;
            updateLayoutParams(this.mIconView, this.mTitleView, this.mLockTaskButton, this.mMoveTaskButton, this.mMusicTaskButton, this.mDismissButton);
            if (this.mAppOverlayView != null) {
                updateLayoutParams(this.mAppIconView, this.mAppTitleView, null, null, null, this.mAppInfoView);
            }
        }
        obtainFreeformParams();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        onTaskViewSizeChanged(this.mTaskViewRect.width(), this.mTaskViewRect.height());
    }

    public void onTaskViewSizeChanged(int width, int height) {
        int i;
        int i2 = 4;
        this.mTaskViewRect.set(0, 0, width, height);
        boolean showTitle = true;
        boolean showMoveIcon = true;
        boolean showDismissIcon = true;
        int rightInset = width - getMeasuredWidth();
        if (this.mTask != null && this.mTask.isFreeformTask()) {
            int moveTaskWidth;
            int appIconWidth = this.mIconView.getMeasuredWidth();
            int titleWidth = (int) this.mTitleView.getPaint().measureText(this.mTask.title);
            int dismissWidth = this.mDismissButton.getMeasuredWidth();
            if (this.mMoveTaskButton != null) {
                moveTaskWidth = this.mMoveTaskButton.getMeasuredWidth();
            } else {
                moveTaskWidth = 0;
            }
            showTitle = width >= ((appIconWidth + dismissWidth) + moveTaskWidth) + titleWidth;
            showMoveIcon = width >= (appIconWidth + dismissWidth) + moveTaskWidth;
            showDismissIcon = width >= appIconWidth + dismissWidth;
        }
        TextView textView = this.mTitleView;
        if (showTitle) {
            i = 0;
        } else {
            i = 4;
        }
        textView.setVisibility(i);
        if (this.mMoveTaskButton != null) {
            ImageView imageView = this.mMoveTaskButton;
            if (showMoveIcon) {
                i = 0;
            } else {
                i = 4;
            }
            imageView.setVisibility(i);
            this.mMoveTaskButton.setTranslationX((float) rightInset);
        }
        ImageView imageView2 = this.mDismissButton;
        if (showDismissIcon) {
            i2 = 0;
        }
        imageView2.setVisibility(i2);
        this.mDismissButton.setTranslationX((float) rightInset);
        setLeftTopRightBottom(0, 0, width, getMeasuredHeight());
    }

    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        canvas.drawRoundRect(0.0f, 0.0f, (float) this.mTaskViewRect.width(), (float) (getHeight() + this.mCornerRadius), (float) this.mCornerRadius, (float) this.mCornerRadius, this.mDimLayerPaint);
    }

    public void startFocusTimerIndicator(int duration) {
        if (this.mFocusTimerIndicator != null) {
            this.mFocusTimerIndicator.setVisibility(0);
            this.mFocusTimerIndicator.setMax(duration);
            this.mFocusTimerIndicator.setProgress(duration);
            if (this.mFocusTimerCountDown != null) {
                this.mFocusTimerCountDown.cancel();
            }
            this.mFocusTimerCountDown = new CountDownTimer((long) duration, 30) {
                public void onTick(long millisUntilFinished) {
                    TaskViewHeader.this.mFocusTimerIndicator.setProgress((int) millisUntilFinished);
                }

                public void onFinish() {
                }
            }.start();
        }
    }

    public void cancelFocusTimerIndicator() {
        if (!(this.mFocusTimerIndicator == null || this.mFocusTimerCountDown == null)) {
            this.mFocusTimerCountDown.cancel();
            this.mFocusTimerIndicator.setProgress(0);
            this.mFocusTimerIndicator.setVisibility(4);
        }
    }

    public ImageView getIconView() {
        return this.mIconView;
    }

    int getSecondaryColor(int primaryColor, boolean useLightOverlayColor) {
        return Utilities.getColorWithOverlay(primaryColor, useLightOverlayColor ? -1 : -16777216, 0.8f);
    }

    public void setDimAlpha(float dimAlpha) {
        if (Float.compare(this.mDimAlpha, dimAlpha) != 0) {
            this.mDimAlpha = dimAlpha;
            this.mTitleView.setAlpha(1.0f - dimAlpha);
            updateBackgroundColor(this.mBackground.getColor(), dimAlpha);
        }
    }

    private void updateBackgroundColor(int color, float dimAlpha) {
        if (this.mTask != null) {
            this.mBackground.setColorAndDim(color, dimAlpha);
            ColorUtils.colorToHSL(color, this.mTmpHSL);
            this.mTmpHSL[2] = Math.min(1.0f, this.mTmpHSL[2] + ((1.0f - dimAlpha) * -0.0625f));
            this.mOverlayBackground.setColorAndDim(ColorUtils.HSLToColor(this.mTmpHSL), dimAlpha);
            this.mDimLayerPaint.setAlpha((int) (255.0f * dimAlpha));
            invalidate();
        }
    }

    public void bindToTask(Task t, boolean touchExplorationEnabled, boolean disabledInSafeMode) {
        int primaryColor;
        this.mTask = t;
        if (disabledInSafeMode) {
            primaryColor = this.mDisabledTaskBarBackgroundColor;
        } else {
            primaryColor = t.colorPrimary;
        }
        if (this.mBackground.getColor() != primaryColor) {
            updateBackgroundColor(primaryColor, this.mDimAlpha);
        }
        if (!this.mTitleView.getText().toString().equals(t.title)) {
            this.mTitleView.setText(t.title);
        }
        this.mTitleView.setContentDescription(t.titleDescription);
        this.mTitleView.setTextColor(t.useLightOnPrimaryColor ? this.mTaskBarViewLightTextColor : this.mTaskBarViewDarkTextColor);
        this.mDismissButton.setOnClickListener(this);
        this.mDismissButton.setClickable(false);
        ((RippleDrawable) this.mDismissButton.getBackground()).setForceSoftware(true);
        if (this.mMoveTaskButton != null) {
            ImageView imageView;
            Drawable drawable;
            if (t.isFreeformTask()) {
                this.mMoveTaskTargetStackId = 1;
                imageView = this.mMoveTaskButton;
                if (t.useLightOnPrimaryColor) {
                    drawable = this.mLightFullscreenIcon;
                } else {
                    drawable = this.mDarkFullscreenIcon;
                }
                imageView.setImageDrawable(drawable);
            } else {
                this.mMoveTaskTargetStackId = 2;
                imageView = this.mMoveTaskButton;
                if (t.useLightOnPrimaryColor) {
                    drawable = this.mLightFreeformIcon;
                } else {
                    drawable = this.mDarkFreeformIcon;
                }
                imageView.setImageDrawable(drawable);
            }
            this.mMoveTaskButton.setOnClickListener(this);
            this.mMoveTaskButton.setClickable(false);
            ((RippleDrawable) this.mMoveTaskButton.getBackground()).setForceSoftware(true);
        }
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            if (this.mFocusTimerIndicator == null) {
                this.mFocusTimerIndicator = (ProgressBar) Utilities.findViewStubById((View) this, (int) R.id.focus_timer_indicator_stub).inflate();
            }
            this.mFocusTimerIndicator.getProgressDrawable().setColorFilter(getSecondaryColor(t.colorPrimary, t.useLightOnPrimaryColor), Mode.SRC_IN);
        }
        if (touchExplorationEnabled) {
            this.mIconView.setContentDescription(t.appInfoDescription);
            this.mIconView.setOnClickListener(this);
            this.mIconView.setClickable(true);
        }
        updateDismissView();
        updateLockView(true);
        updateMusicView();
    }

    public void onTaskDataLoaded() {
        if (this.mTask.icon != null) {
            this.mIconView.setImageDrawable(this.mTask.icon);
        }
    }

    void unbindFromTask(boolean touchExplorationEnabled) {
        this.mTask = null;
        this.mIconView.setImageDrawable(null);
        if (touchExplorationEnabled) {
            this.mIconView.setClickable(false);
        }
    }

    public void startNoUserInteractionAnimation() {
        int duration = getResources().getInteger(R.integer.recents_task_enter_from_app_duration);
        this.mIconView.setVisibility(0);
        if (this.mIconView.getVisibility() == 0) {
            this.mIconView.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration((long) duration).start();
        } else {
            this.mIconView.setAlpha(1.0f);
        }
        this.mDismissButton.setVisibility(0);
        this.mDismissButton.setClickable(true);
        if (this.mDismissButton.getVisibility() == 0) {
            this.mDismissButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration((long) duration).start();
        } else {
            this.mDismissButton.setAlpha(1.0f);
        }
        if (this.mMoveTaskButton != null) {
            if (this.mMoveTaskButton.getVisibility() == 0) {
                this.mMoveTaskButton.setVisibility(0);
                this.mMoveTaskButton.setClickable(true);
                this.mMoveTaskButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration((long) duration).start();
            } else {
                this.mMoveTaskButton.setAlpha(1.0f);
            }
        }
        this.mLockTaskButton.setOnClickListener(this);
        this.mLockTaskButton.setVisibility(0);
        if (this.mLockTaskButton.getVisibility() == 0) {
            this.mLockTaskButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration((long) duration).start();
        } else {
            this.mLockTaskButton.setAlpha(1.0f);
        }
        if (this.mMusicTaskButton != null && HwRecentsHelper.getPlayingMusicUid(getContext(), this.mTask)) {
            this.mMusicTaskButton.setVisibility(0);
            if (this.mMusicTaskButton.getVisibility() == 0) {
                this.mMusicTaskButton.animate().alpha(1.0f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration((long) duration).start();
            } else {
                this.mMusicTaskButton.setAlpha(1.0f);
            }
            ((AnimationDrawable) this.mMusicTaskButton.getDrawable()).start();
        }
    }

    public void setNoUserInteractionState() {
        this.mIconView.setVisibility(0);
        this.mIconView.animate().cancel();
        this.mIconView.setAlpha(1.0f);
        this.mDismissButton.setVisibility(0);
        this.mDismissButton.animate().cancel();
        this.mDismissButton.setAlpha(1.0f);
        this.mDismissButton.setClickable(true);
        if (this.mMoveTaskButton != null) {
            this.mMoveTaskButton.setVisibility(0);
            this.mMoveTaskButton.animate().cancel();
            this.mMoveTaskButton.setAlpha(1.0f);
            this.mMoveTaskButton.setClickable(true);
        }
        this.mLockTaskButton.setVisibility(0);
        this.mLockTaskButton.animate().cancel();
        this.mLockTaskButton.setAlpha(1.0f);
        this.mLockTaskButton.setOnClickListener(this);
        if (this.mMusicTaskButton != null && HwRecentsHelper.getPlayingMusicUid(getContext(), this.mTask)) {
            this.mMusicTaskButton.setVisibility(0);
            this.mMusicTaskButton.animate().cancel();
            this.mMusicTaskButton.setAlpha(1.0f);
            this.mMusicTaskButton.setOnClickListener(this);
            ((AnimationDrawable) this.mMusicTaskButton.getDrawable()).start();
        }
    }

    public void resetNoUserInteractionState() {
        this.mIconView.setVisibility(8);
        this.mIconView.setAlpha(0.0f);
        this.mDismissButton.setVisibility(8);
        this.mDismissButton.setAlpha(0.0f);
        this.mDismissButton.setClickable(false);
        if (this.mMoveTaskButton != null) {
            this.mMoveTaskButton.setVisibility(8);
            this.mMoveTaskButton.setAlpha(0.0f);
            this.mMoveTaskButton.setClickable(false);
        }
        this.mLockTaskButton.setVisibility(8);
        this.mLockTaskButton.setAlpha(0.0f);
        this.mLockTaskButton.setOnClickListener(null);
        if (this.mMusicTaskButton != null) {
            this.mMusicTaskButton.setVisibility(8);
            this.mMusicTaskButton.setAlpha(0.0f);
            this.mMusicTaskButton.setOnClickListener(null);
        }
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        return new int[0];
    }

    public void onClick(View v) {
        if (v == this.mIconView) {
            if (this.mTask == null) {
                HwLog.e("TaskViewHeader", "mTask = null");
                return;
            }
            EventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (v == this.mDismissButton) {
            TaskView tv = (TaskView) Utilities.findParent(this, TaskView.class);
            BDReporter.e(getContext(), 38, "pkg:" + tv.getTask().packageName);
            tv.dismissTask();
            MetricsLogger.histogram(getContext(), "overview_task_dismissed_source", 2);
        } else if (v == this.mMoveTaskButton) {
            launchTask();
        } else if (v == this.mAppInfoView) {
            BDReporter.c(getContext(), 364);
            EventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (v == this.mAppIconView) {
            hideAppOverlay(false);
        } else if (v == this.mLockTaskButton) {
            if (this.mTask == null) {
                HwLog.e("TaskViewHeader", "mTask = null");
                return;
            }
            this.mTask.isLocked = !this.mTask.isLocked;
            HwRecentsLockUtils.insertOrUpdate(getContext(), this.mTask);
            updateLockView(false);
            announceForAccessibility(getContext().getString(this.mTask.isLocked ? R.string.accessibility_recents_item_locked : R.string.accessibility_recents_item_unlock, new Object[]{this.mTask.title}));
            BDReporter.e(getContext(), 2, "pkg:" + this.mTask.packageName + ",lock:" + this.mTask.isLocked);
            EventBus.getDefault().send(new HwLockTaskViewEvent(this.mTask));
        }
    }

    public boolean onLongClick(View v) {
        if (v == this.mIconView) {
            BDReporter.c(getContext(), 364);
            EventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
            return true;
        } else if (v != this.mAppIconView) {
            return false;
        } else {
            hideAppOverlay(false);
            return true;
        }
    }

    private void hideAppOverlay(boolean immediate) {
        if (this.mAppOverlayView != null) {
            if (immediate) {
                this.mAppOverlayView.setVisibility(8);
            } else {
                Animator revealAnim = ViewAnimationUtils.createCircularReveal(this.mAppOverlayView, this.mIconView.getLeft() + (this.mIconView.getWidth() / 2), this.mIconView.getTop() + (this.mIconView.getHeight() / 2), (float) getWidth(), 0.0f);
                revealAnim.setDuration(250);
                revealAnim.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                revealAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        TaskViewHeader.this.mAppOverlayView.setVisibility(8);
                    }
                });
                revealAnim.start();
            }
        }
    }

    private void launchTask() {
        if (this.mMoveTaskTargetStackId == 2) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                int floatingWindowsNums = 0;
                Rect launchFreeformBound = new Rect();

                public boolean runInThread() {
                    Rect topBound = new Rect();
                    try {
                        List<RecentTaskInfo> recentTask = ActivityManagerNative.getDefault().getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), 5, UserSwitchUtils.getCurrentUser()).getList();
                        if (recentTask != null && recentTask.size() > 0) {
                            for (RecentTaskInfo task : recentTask) {
                                if (task.stackId == 2) {
                                    if (this.floatingWindowsNums == 0) {
                                        topBound = ActivityManagerNative.getDefault().getTaskBounds(task.id);
                                    }
                                    this.floatingWindowsNums++;
                                }
                            }
                        }
                    } catch (RemoteException e) {
                    }
                    if (this.floatingWindowsNums < TaskViewHeader.this.mMaxFloatingWindows) {
                        this.launchFreeformBound = TaskViewHeader.this.getLaunchFreeformBound(this.floatingWindowsNums, topBound);
                    }
                    return true;
                }

                public void runInUI() {
                    if (this.floatingWindowsNums < TaskViewHeader.this.mMaxFloatingWindows) {
                        EventBus.getDefault().send(new LaunchTaskEvent((TaskView) Utilities.findParent(TaskViewHeader.this, TaskView.class), TaskViewHeader.this.mTask, this.launchFreeformBound, TaskViewHeader.this.mMoveTaskTargetStackId, false));
                        return;
                    }
                    Toast.makeText(TaskViewHeader.this.getContext(), R.string.exceeded_max_floating_windows, 0).show();
                }
            });
            return;
        }
        TaskView tv = (TaskView) Utilities.findParent(this, TaskView.class);
        EventBus.getDefault().send(new LaunchTaskEvent(tv, this.mTask, new Rect(), this.mMoveTaskTargetStackId, false));
    }

    private Rect getLaunchFreeformBound(int currentFreeformCount, Rect topBound) {
        Resources res = this.mContext.getResources();
        int freeformPaddingLeft = res.getDimensionPixelSize(R.dimen.freeform_padding_left);
        int freeformPaddingTop = res.getDimensionPixelSize(R.dimen.freeform_padding_top);
        int defaultHeight = (int) (((float) this.mFreeformBaseHeight) * (currentFreeformCount == 0 ? res.getFraction(R.fraction.freeform_first_window_default_height_percent, 1, 1) : res.getFraction(R.fraction.freeform_other_window_default_height_percent, 1, 1)));
        int left = topBound.left < 0 ? freeformPaddingLeft : topBound.left + freeformPaddingLeft;
        int top = topBound.top < 0 ? freeformPaddingTop : topBound.top + freeformPaddingTop;
        int width = this.mTaskViewRect.width() < this.mFreeformSmallestLength ? this.mFreeformSmallestLength : this.mTaskViewRect.width();
        int height = defaultHeight < this.mFreeformSmallestLength ? this.mFreeformSmallestLength : defaultHeight;
        if (width + left > res.getDisplayMetrics().widthPixels) {
            left = freeformPaddingLeft;
        }
        if (height + top > res.getDisplayMetrics().heightPixels) {
            top = freeformPaddingTop;
        }
        return new Rect(left, top, width + left, height + top);
    }

    protected void onAttachedToWindow() {
        EventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onBusEvent(HwLockTaskViewEvent event) {
        if (this.mTask != null && !TextUtils.isEmpty(this.mTask.packageName)) {
            Task task = event.mTask;
            if (!(task == null || TextUtils.isEmpty(task.packageName) || task.key.id == this.mTask.key.id || !task.packageName.equals(this.mTask.packageName))) {
                this.mTask.isLocked = task.isLocked;
                updateLockView(false);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onBusEvent(HwTaskViewsDismissedEvent event) {
        if (this.mTask != null && !TextUtils.isEmpty(this.mTask.packageName)) {
            Task task = event.mTask;
            if (!(task == null || TextUtils.isEmpty(task.packageName) || !task.packageName.equals(this.mTask.packageName))) {
                this.mTask.isLocked = false;
                updateLockView(false);
            }
        }
    }

    private void updateMusicView() {
        if (HwRecentsHelper.getPlayingMusicUid(getContext(), this.mTask)) {
            this.mMusicTaskButton.getDrawable().setTint(this.mTask.useLightOnPrimaryColor ? -1 : 1711276032);
            if (SystemUiUtil.isFaAr(Locale.getDefault().getLanguage())) {
                this.mMusicTaskButton.setScaleType(ScaleType.FIT_END);
                return;
            } else {
                this.mMusicTaskButton.setScaleType(ScaleType.FIT_START);
                return;
            }
        }
        this.mMusicTaskButton.setVisibility(8);
    }

    private void updateDismissView() {
        this.mDismissButton.setImageDrawable(getContext().getDrawable(R.drawable.abc_btn_switch_to_on_mtrl_00012));
        this.mDismissButton.getDrawable().setTint(this.mTask.useLightOnPrimaryColor ? -1 : 1711276032);
        this.mDismissButton.setContentDescription(this.mTask.dismissDescription);
    }

    private void updateLockView(boolean useDatabaseState) {
        Drawable drawable;
        String lockDescFormat;
        if (useDatabaseState) {
            this.mTask.isLocked = HwRecentsLockUtils.isLocked(this.mTask.packageName, false);
        }
        ImageView imageView = this.mLockTaskButton;
        if (this.mTask.isLocked) {
            drawable = getContext().getDrawable(R.drawable.abc_cab_background_top_mtrl_alpha);
        } else {
            drawable = getContext().getDrawable(R.drawable.abc_ic_star_half_black_36dp);
        }
        imageView.setImageDrawable(drawable);
        this.mLockTaskButton.getDrawable().setTint(this.mTask.useLightOnPrimaryColor ? -1 : 1711276032);
        if (this.mTask.isLocked) {
            lockDescFormat = this.mContext.getString(R.string.accessibility_recents_item_locked);
        } else {
            lockDescFormat = this.mContext.getString(R.string.accessibility_recents_item_will_be_locked);
        }
        this.mLockTaskButton.setContentDescription(String.format(lockDescFormat, new Object[]{this.mTask.titleDescription}));
    }
}
