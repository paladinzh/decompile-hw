package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.mms.attachment.ui.PagingAwareViewPager;
import com.android.mms.attachment.utils.UiUtils;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;

public class MediaPickerPanel extends ViewGroup {
    private static boolean sIsLayoutChanging = false;
    private boolean isPanelTouchEnabled = true;
    private int mActionBarHeight = HwMessageUtils.getSplitActionBarHeight(getContext());
    private ImageView mBottomSplitImage;
    private int mCurrentDesiredHeight;
    private final int mDefaultViewPagerHeight = getResources().getDimensionPixelSize(R.dimen.mediapicker_default_chooser_height);
    private boolean mExpanded;
    private boolean mFullScreen;
    private boolean mFullScreenOnly;
    private final Handler mHandler = new MyHandler();
    private MediaPicker mMediaPicker;
    private ImageView mSplitImage;
    private LinearLayout mTabStrip;
    private TouchHandler mTouchHandler;
    private PagingAwareViewPager mViewPager;

    static class MyHandler extends Handler {
        MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10001:
                    MessageUtils.setIsMediaPanelInScrollingStatus(false);
                    return;
                case 10002:
                    MediaPickerPanel.sIsLayoutChanging = false;
                    return;
                default:
                    return;
            }
        }
    }

    private class TouchHandler implements OnTouchListener {
        private final float mBigFlingThresholdPx;
        private boolean mCanChildViewSwipeDown = false;
        private MotionEvent mDownEvent;
        private int mDownHeight = -1;
        private final float mFlingThresholdPx;
        private boolean mMoved = false;
        private boolean mMovedDown = false;
        private final int mTouchSlop;

        TouchHandler() {
            Resources resources = MediaPickerPanel.this.getContext().getResources();
            ViewConfiguration configuration = ViewConfiguration.get(MediaPickerPanel.this.getContext());
            this.mFlingThresholdPx = (float) resources.getDimensionPixelSize(R.dimen.mediapicker_fling_threshold);
            this.mBigFlingThresholdPx = (float) resources.getDimensionPixelSize(R.dimen.mediapicker_big_fling_threshold);
            this.mTouchSlop = configuration.getScaledTouchSlop();
        }

        public boolean onInterceptTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case 0:
                    MediaPickerPanel.this.mTouchHandler.onTouch(MediaPickerPanel.this, ev);
                    this.mCanChildViewSwipeDown = MediaPickerPanel.this.mMediaPicker.canSwipeDownChooser();
                    return false;
                case 2:
                    if (MediaPickerPanel.this.mMediaPicker.isChooserHandlingTouch()) {
                        if (shouldAllowRecaptureTouch(ev)) {
                            boolean isFullScreen;
                            MediaPickerPanel.this.mMediaPicker.stopChooserTouchHandling();
                            if (MediaPickerPanel.this.mMediaPicker.getSelectedChooser() instanceof MapMediaChooser) {
                                isFullScreen = MediaPickerPanel.this.isFullScreen();
                            } else {
                                isFullScreen = false;
                            }
                            if (!isFullScreen) {
                                MediaPickerPanel.this.mViewPager.setPagingEnabled(true);
                            }
                            return false;
                        }
                        MediaPickerPanel.this.mViewPager.setPagingEnabled(false);
                        return false;
                    } else if (this.mCanChildViewSwipeDown) {
                        return false;
                    } else {
                        if ((!MediaPickerPanel.this.mFullScreen && this.mMoved) || this.mMovedDown || MediaPickerPanel.sIsLayoutChanging) {
                            return true;
                        }
                        MediaPickerPanel.this.mTouchHandler.onTouch(MediaPickerPanel.this, ev);
                        return MediaPickerPanel.this.mFullScreen ? this.mMovedDown : this.mMoved;
                    }
                default:
                    return false;
            }
        }

        private boolean shouldAllowRecaptureTouch(MotionEvent ev) {
            boolean z = false;
            long elapsedMs = ev.getEventTime() - ev.getDownTime();
            if (this.mDownEvent == null || elapsedMs == 0 || elapsedMs > 500) {
                return false;
            }
            if (Math.max(Math.abs(ev.getRawX() - this.mDownEvent.getRawX()), Math.abs(ev.getRawY() - this.mDownEvent.getRawY())) / (((float) elapsedMs) / 1000.0f) > this.mFlingThresholdPx) {
                z = true;
            }
            return z;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            float dx;
            float dy;
            switch (motionEvent.getAction()) {
                case 0:
                    if (HwMessageUtils.isSplitOn() && (MediaPickerPanel.this.mMediaPicker.getActivity() instanceof ConversationList)) {
                        if (((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment() instanceof RcsGroupChatComposeMessageFragment) {
                            ((RcsGroupChatComposeMessageFragment) ((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment()).removeSupportScale();
                        } else {
                            ((ComposeMessageFragment) ((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment()).removeSupportScale();
                        }
                    } else if (MediaPickerPanel.this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                        ((RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(MediaPickerPanel.this.mMediaPicker.getActivity(), "Mms_UI_GCCMF")).removeSupportScale();
                    } else {
                        ((ComposeMessageFragment) FragmentTag.getFragmentByTag(MediaPickerPanel.this.mMediaPicker.getActivity(), "Mms_UI_CMF")).removeSupportScale();
                    }
                    this.mDownHeight = MediaPickerPanel.this.getHeight();
                    this.mDownEvent = MotionEvent.obtain(motionEvent);
                    return true;
                case 1:
                    if (HwMessageUtils.isSplitOn() && (MediaPickerPanel.this.mMediaPicker.getActivity() instanceof ConversationList)) {
                        if (((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment() instanceof RcsGroupChatComposeMessageFragment) {
                            ((RcsGroupChatComposeMessageFragment) ((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment()).setSupportScale();
                        } else {
                            ((ComposeMessageFragment) ((ConversationList) MediaPickerPanel.this.mMediaPicker.getActivity()).getRightFragment()).setSupportScale();
                        }
                    } else if (MediaPickerPanel.this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                        ((RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(MediaPickerPanel.this.mMediaPicker.getActivity(), "Mms_UI_GCCMF")).setSupportScale();
                    } else {
                        ((ComposeMessageFragment) FragmentTag.getFragmentByTag(MediaPickerPanel.this.mMediaPicker.getActivity(), "Mms_UI_CMF")).setSupportScale();
                    }
                    if (this.mMoved && this.mDownEvent != null) {
                        dx = motionEvent.getRawX() - this.mDownEvent.getRawX();
                        dy = motionEvent.getRawY() - this.mDownEvent.getRawY();
                        float yVelocity = dy / (((float) (motionEvent.getEventTime() - this.mDownEvent.getEventTime())) / 1000.0f);
                        boolean handled = false;
                        if ((dx == 0.0f || Math.abs(dy) / Math.abs(dx) > 1.1f) && Math.abs(yVelocity) > this.mFlingThresholdPx) {
                            if (yVelocity < 0.0f && MediaPickerPanel.this.mExpanded) {
                                MediaPickerPanel.this.setFullScreenView(true, true);
                                handled = true;
                            } else if (yVelocity > 0.0f) {
                                if (!MediaPickerPanel.this.mFullScreen || yVelocity >= this.mBigFlingThresholdPx) {
                                    MediaPickerPanel.this.setExpanded(false, true, -1);
                                } else {
                                    MediaPickerPanel.this.setFullScreenView(false, true);
                                }
                                handled = true;
                            }
                        }
                        if (!handled) {
                            MediaPickerPanel.this.refreshSystemUIStateForCamera();
                            MediaPickerPanel.this.setDesiredHeight(MediaPickerPanel.this.getDesiredHeight(), true);
                        }
                        resetState();
                        MessageUtils.setIsMediaPanelInScrollingStatus(false);
                        StatisticalHelper.reportEvent(MediaPickerPanel.this.getContext(), 2263, String.valueOf(MediaPickerPanel.this.mViewPager.getCurrentItem()));
                        break;
                    }
                    return false;
                    break;
                case 2:
                    if (!MediaPickerPanel.this.isPanelTouchEnabled) {
                        return true;
                    }
                    if (this.mDownEvent == null) {
                        return this.mMoved;
                    }
                    dx = this.mDownEvent.getRawX() - motionEvent.getRawX();
                    dy = this.mDownEvent.getRawY() - motionEvent.getRawY();
                    if (Math.abs(dy) > ((float) this.mTouchSlop) && Math.abs(dy) / Math.abs(dx) > 1.1f) {
                        MediaPickerPanel.this.setDesiredHeight((int) (((float) this.mDownHeight) + dy), false);
                        this.mMoved = true;
                        if (dy < ((float) (-this.mTouchSlop))) {
                            this.mMovedDown = true;
                        }
                        if (MediaPickerPanel.this.mMediaPicker.getIsCameraChooser()) {
                            CameraMediaChooser cameraMediaChooser = (CameraMediaChooser) MediaPickerPanel.this.mMediaPicker.getSelectedChooser();
                            if (cameraMediaChooser.getScrollFullScreenState() && dy < ((float) (-this.mTouchSlop))) {
                                cameraMediaChooser.setScrollFullScreenState(false);
                            }
                        }
                    }
                    MessageUtils.setIsMediaPanelInScrollingStatus(true);
                    MediaPickerPanel.this.mHandler.removeMessages(10001);
                    MediaPickerPanel.this.mHandler.sendEmptyMessageDelayed(10001, 5000);
                    return this.mMoved;
                default:
                    MessageUtils.setIsMediaPanelInScrollingStatus(false);
                    break;
            }
            return this.mMoved;
        }

        private void resetState() {
            this.mDownEvent = null;
            this.mDownHeight = -1;
            this.mMoved = false;
            this.mMovedDown = false;
            this.mCanChildViewSwipeDown = false;
            MediaPickerPanel.this.updateViewPager();
        }
    }

    public MediaPickerPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTabStrip = (LinearLayout) findViewById(R.id.mediapicker_tabstrip);
        this.mViewPager = (PagingAwareViewPager) findViewById(R.id.mediapicker_view_pager);
        this.mSplitImage = (ImageView) findViewById(R.id.mediapicker_view_split);
        this.mBottomSplitImage = (ImageView) findViewById(R.id.mediapicker_bottom_view_split);
        this.mTouchHandler = new TouchHandler();
        setOnTouchListener(this.mTouchHandler);
        this.mViewPager.setOnTouchListener(this.mTouchHandler);
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            private boolean mLandscapeMode = UiUtils.isInLandscape();

            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                MediaPickerPanel.sIsLayoutChanging = true;
                MediaPickerPanel.this.mHandler.removeMessages(10002);
                MediaPickerPanel.this.mHandler.sendEmptyMessageDelayed(10002, 100);
                boolean newLandscapeMode = UiUtils.isInLandscape();
                if (this.mLandscapeMode != newLandscapeMode) {
                    this.mLandscapeMode = newLandscapeMode;
                    if (MediaPickerPanel.this.mMediaPicker.getIsCameraChooser() && MediaPickerPanel.this.mExpanded) {
                        if (this.mLandscapeMode) {
                            MessageUtils.setWindowSystemUiVisibility(MediaPickerPanel.this.mMediaPicker, false);
                            MessageUtils.setNavigationBarDefaultColor(MediaPickerPanel.this.mMediaPicker, false);
                        } else {
                            MessageUtils.setWindowSystemUiVisibility(MediaPickerPanel.this.mMediaPicker, true);
                            MessageUtils.setNavigationBarDefaultColor(MediaPickerPanel.this.mMediaPicker, true);
                        }
                    }
                    if (MediaPickerPanel.this.mExpanded) {
                        MediaPickerPanel.this.setLayoutExpanded(MediaPickerPanel.this.mExpanded, false, true);
                    }
                }
            }
        });
    }

    private void setLayoutExpanded(boolean expanded, boolean animate, boolean force) {
        if (this.mViewPager != null) {
            int viewPagerPosition = this.mViewPager.getCurrentItem();
            if (!(!MessageUtils.isNeedLayoutRtl() || this.mMediaPicker == null || this.mMediaPicker.getPagerAdapter() == null)) {
                viewPagerPosition = (this.mMediaPicker.getPagerAdapter().getCount() - viewPagerPosition) - 1;
            }
            setExpanded(expanded, animate, viewPagerPosition, force);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int tabStripHeight;
        int viewPagerHeight;
        int requestedHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mMediaPicker.getChooserShowsActionBarInFullScreen()) {
            requestedHeight -= this.mActionBarHeight;
        }
        int desiredHeight = Math.min(this.mCurrentDesiredHeight, requestedHeight);
        if (this.mExpanded && desiredHeight == 0) {
            desiredHeight = 1;
        } else if (!this.mExpanded && desiredHeight == 0) {
            this.mViewPager.setVisibility(8);
            this.mViewPager.setAdapter(null);
        }
        measureChild(this.mSplitImage, widthMeasureSpec, heightMeasureSpec);
        measureChild(this.mBottomSplitImage, widthMeasureSpec, heightMeasureSpec);
        measureChild(this.mTabStrip, widthMeasureSpec, heightMeasureSpec);
        if (requiresFullScreen()) {
            tabStripHeight = this.mTabStrip.getMeasuredHeight();
        } else if (this.mMediaPicker.getIsCameraChooser()) {
            tabStripHeight = Math.min(this.mTabStrip.getMeasuredHeight(), getResources().getDisplayMetrics().heightPixels - desiredHeight);
        } else {
            tabStripHeight = Math.min(this.mTabStrip.getMeasuredHeight(), requestedHeight - desiredHeight);
        }
        int tabAdjustedDesiredHeight = desiredHeight - tabStripHeight;
        if (tabAdjustedDesiredHeight <= 1) {
            viewPagerHeight = this.mDefaultViewPagerHeight;
        } else {
            viewPagerHeight = tabAdjustedDesiredHeight;
        }
        measureChild(this.mViewPager, widthMeasureSpec, MeasureSpec.makeMeasureSpec(viewPagerHeight, 1073741824));
        setMeasuredDimension(this.mViewPager.getMeasuredWidth(), desiredHeight);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int y = top;
        int width = Math.abs(right - left);
        int splitImageHeight = this.mSplitImage.getMeasuredHeight();
        if (this.mSplitImage.getVisibility() == 0) {
            this.mSplitImage.layout(0, top, width, top + splitImageHeight);
            y = top + splitImageHeight;
        }
        int viewPagerHeight = this.mViewPager.getMeasuredHeight();
        this.mViewPager.layout(0, y, width, y + viewPagerHeight);
        y += viewPagerHeight;
        this.mBottomSplitImage.layout(0, y, width, this.mBottomSplitImage.getMeasuredHeight() + y);
        this.mTabStrip.layout(0, y, width, this.mTabStrip.getMeasuredHeight() + y);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBarHeight = HwMessageUtils.getSplitActionBarHeight(getContext());
        if (newConfig.orientation == 1) {
            this.mSplitImage.setVisibility(0);
        }
    }

    void onChooserChanged() {
        if (this.mFullScreen) {
            setDesiredHeight(getDesiredHeight(), true);
        }
    }

    void setFullScreenOnly(boolean fullScreenOnly) {
        this.mFullScreenOnly = fullScreenOnly;
    }

    public boolean isFullScreen() {
        return this.mFullScreen;
    }

    void setMediaPicker(MediaPicker mediaPicker) {
        this.mMediaPicker = mediaPicker;
    }

    private int getDesiredHeight() {
        if (this.mFullScreen) {
            int fullHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            if (isAttachedToWindow()) {
                View composeContainer = getRootView().findViewById(R.id.mms_compose_message_view);
                if (!(composeContainer == null || this.mMediaPicker.getIsCameraChooser())) {
                    fullHeight -= UiUtils.getMeasuredBoundsOnScreen(composeContainer).top;
                }
            }
            if (this.mMediaPicker.getChooserShowsActionBarInFullScreen()) {
                return fullHeight - this.mActionBarHeight;
            }
            return fullHeight;
        } else if (this.mExpanded) {
            return -2;
        } else {
            return 0;
        }
    }

    private void setupViewPager(int startingPage) {
        this.mViewPager.setVisibility(0);
        if (this.mMediaPicker.getPagerAdapter() != null) {
            if (startingPage >= 0 && startingPage < this.mMediaPicker.getPagerAdapter().getCount()) {
                this.mViewPager.setAdapter(this.mMediaPicker.getPagerAdapter());
                PagingAwareViewPager pagingAwareViewPager = this.mViewPager;
                if (MessageUtils.isNeedLayoutRtl()) {
                    startingPage = (this.mMediaPicker.getPagerAdapter().getCount() - 1) - startingPage;
                }
                pagingAwareViewPager.setCurrentItem(startingPage);
            }
            updateViewPager();
        }
    }

    void setExpanded(boolean expanded, boolean animate, int startingPage) {
        setExpanded(expanded, animate, startingPage, false);
    }

    private void setExpanded(boolean expanded, final boolean animate, int startingPage, boolean force) {
        if (expanded != this.mExpanded || force) {
            this.mFullScreen = false;
            this.mExpanded = expanded;
            this.mHandler.post(new Runnable() {
                public void run() {
                    MediaPickerPanel.this.setDesiredHeight(MediaPickerPanel.this.getDesiredHeight(), animate);
                }
            });
            if (expanded) {
                setupViewPager(startingPage);
                this.mMediaPicker.dispatchOpened();
            } else {
                this.mMediaPicker.dispatchDismissed();
            }
            if (expanded && requiresFullScreen()) {
                setFullScreenView(true, animate);
            }
        }
    }

    private boolean requiresFullScreen() {
        return !this.mFullScreenOnly ? UiUtils.isInLandscape() : true;
    }

    private void setDesiredHeight(int height, final boolean animate) {
        final int startHeight = this.mCurrentDesiredHeight;
        if (height == -2) {
            height = measureHeight();
        }
        clearAnimation();
        if (animate) {
            final int deltaHeight = height - startHeight;
            Animation animation = new Animation() {
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    MediaPickerPanel.this.mCurrentDesiredHeight = (int) (((float) startHeight) + (((float) deltaHeight) * interpolatedTime));
                    if (MediaPickerPanel.this.isUpdateUIForCamera(MediaPickerPanel.this.mCurrentDesiredHeight)) {
                        MediaPickerPanel.this.updateSystemUIForCamera(animate, true);
                    }
                    MediaPickerPanel.this.requestLayout();
                }

                public boolean willChangeBounds() {
                    return true;
                }
            };
            animation.setDuration((long) UiUtils.MEDIAPICKER_TRANSITION_DURATION);
            animation.setInterpolator(UiUtils.EASE_OUT_INTERPOLATOR);
            startAnimation(animation);
        } else {
            this.mCurrentDesiredHeight = height;
            if (isUpdateUIForCamera(this.mCurrentDesiredHeight)) {
                updateSystemUIForCamera(animate, false);
            } else {
                updateSystemUIForCamera(animate, true);
            }
        }
        requestLayout();
    }

    private boolean isUpdateUIForCamera(int currentDesiteHeight) {
        int maxHeight = getContext().getResources().getDisplayMetrics().heightPixels - getContext().getResources().getDimensionPixelSize(R.dimen.camera_meidapicker_screen_height_margin);
        if (this.mCurrentDesiredHeight < maxHeight || maxHeight <= 0) {
            return false;
        }
        return true;
    }

    private void updateSystemUIForCamera(boolean animate, boolean isDisplay) {
        if (this.mMediaPicker.getIsCameraChooser()) {
            CameraMediaChooser cameraMediaChooser = (CameraMediaChooser) this.mMediaPicker.getSelectedChooser();
            if (animate) {
                if (cameraMediaChooser.getScrollFullScreenState()) {
                    MessageUtils.setWindowSystemUiVisibility(this.mMediaPicker, false);
                    MessageUtils.setNavigationBarDefaultColor(this.mMediaPicker, false);
                } else {
                    MessageUtils.setWindowSystemUiVisibility(this.mMediaPicker, true);
                    MessageUtils.setNavigationBarDefaultColor(this.mMediaPicker, true);
                }
            } else if (isDisplay) {
                MessageUtils.setWindowSystemUiVisibility(this.mMediaPicker, true);
                MessageUtils.setNavigationBarDefaultColor(this.mMediaPicker, true);
            } else {
                MessageUtils.setWindowSystemUiVisibility(this.mMediaPicker, false);
                MessageUtils.setNavigationBarDefaultColor(this.mMediaPicker, false);
            }
            return;
        }
        MessageUtils.setWindowSystemUiVisibility(this.mMediaPicker, true);
        MessageUtils.setNavigationBarDefaultColor(this.mMediaPicker, true);
    }

    private int measureHeight() {
        int measureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE, Integer.MIN_VALUE);
        measureChild(this.mTabStrip, measureSpec, measureSpec);
        return this.mDefaultViewPagerHeight + this.mTabStrip.getMeasuredHeight();
    }

    public void setFullScreenView(boolean fullScreen, boolean animate) {
        if (fullScreen != this.mFullScreen) {
            if (!requiresFullScreen() || fullScreen) {
                this.mFullScreen = fullScreen;
                if (fullScreen) {
                    this.mSplitImage.setVisibility(8);
                } else {
                    this.mSplitImage.setVisibility(0);
                    if (this.mMediaPicker != null && this.mMediaPicker.isAdded() && this.mMediaPicker.getIsCameraChooser()) {
                        ((CameraMediaChooser) this.mMediaPicker.getSelectedChooser()).setScrollFullScreenState(false);
                    }
                }
                setDesiredHeight(getDesiredHeight(), animate);
                this.mMediaPicker.dispatchFullScreen(this.mFullScreen);
                updateViewPager();
                return;
            }
            if (this.mMediaPicker != null && this.mMediaPicker.isAdded() && this.mMediaPicker.getIsCameraChooser()) {
                ((CameraMediaChooser) this.mMediaPicker.getSelectedChooser()).setScrollFullScreenState(false);
            }
            setExpanded(false, true, -1);
        }
    }

    private void refreshSystemUIStateForCamera() {
        if (this.mMediaPicker != null && this.mMediaPicker.isAdded() && this.mMediaPicker.getIsCameraChooser()) {
            CameraMediaChooser cameraMediaChooser = (CameraMediaChooser) this.mMediaPicker.getSelectedChooser();
            if (!cameraMediaChooser.getScrollFullScreenState() && this.mFullScreen) {
                cameraMediaChooser.setScrollFullScreenState(true);
            }
        }
    }

    private void updateViewPager() {
        this.mViewPager.setPagingEnabled(!this.mFullScreen);
    }

    public void updateViewPager(boolean scrollable) {
        this.mViewPager.setPagingEnabled(scrollable);
    }

    public void setPanelTouchEnabled(boolean enabled) {
        this.isPanelTouchEnabled = enabled;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !this.mTouchHandler.onInterceptTouchEvent(ev) ? super.onInterceptTouchEvent(ev) : true;
    }
}
