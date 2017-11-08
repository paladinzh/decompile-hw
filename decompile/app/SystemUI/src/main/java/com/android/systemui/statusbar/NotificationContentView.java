package com.android.systemui.statusbar;

import android.app.Notification.Action;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Rect;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.MmsUtils;
import com.android.systemui.utils.SystemUiUtil;

public class NotificationContentView extends FrameLayout {
    private boolean mAnimate;
    private int mAnimationStartVisibleType = -1;
    private boolean mBeforeN;
    private final Rect mClipBounds = new Rect();
    private boolean mClipToActualHeight = true;
    private int mClipTopAmount;
    private ExpandableNotificationRow mContainingNotification;
    private int mContentHeight;
    private int mContentHeightAtAnimationStart = -1;
    private View mContractedChild;
    private NotificationViewWrapper mContractedWrapper;
    private boolean mDark;
    private final OnPreDrawListener mEnableAnimationPredrawListener = new OnPreDrawListener() {
        public boolean onPreDraw() {
            NotificationContentView.this.post(new Runnable() {
                public void run() {
                    NotificationContentView.this.mAnimate = true;
                }
            });
            NotificationContentView.this.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
        }
    };
    private OnClickListener mExpandClickListener;
    private boolean mExpandable;
    private View mExpandedChild;
    private RemoteInputView mExpandedRemoteInput;
    private NotificationViewWrapper mExpandedWrapper;
    private boolean mFocusOnVisibilityChange;
    private boolean mForceSelectNextLayout = true;
    private NotificationGroupManager mGroupManager;
    private View mHeadsUpChild;
    private int mHeadsUpHeight;
    private RemoteInputView mHeadsUpRemoteInput;
    private NotificationViewWrapper mHeadsUpWrapper;
    private boolean mHeadsupDisappearRunning;
    private HybridGroupManager mHybridGroupManager = new HybridGroupManager(getContext(), this);
    private boolean mIsChildInGroup;
    private boolean mIsHeadsUp;
    private final int mMinContractedHeight = getResources().getDimensionPixelSize(R.dimen.min_notification_layout_height);
    private final int mNotificationContentMarginEnd = getResources().getDimensionPixelSize(17104960);
    private int mNotificationMaxHeight;
    private PendingIntent mPreviousExpandedRemoteInputIntent;
    private PendingIntent mPreviousHeadsUpRemoteInputIntent;
    private RemoteInputController mRemoteInputController;
    private boolean mShowingLegacyBackground;
    private HybridNotificationView mSingleLineView;
    private int mSingleLineWidthIndention;
    private int mSmallHeight;
    private StatusBarNotification mStatusBarNotification;
    private int mTransformationStartVisibleType;
    private boolean mUserExpanding;
    private int mVisibleType = 0;

    public NotificationContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        reset();
    }

    public void setHeights(int smallHeight, int headsUpMaxHeight, int maxHeight) {
        this.mSmallHeight = smallHeight;
        this.mHeadsUpHeight = headsUpMaxHeight;
        this.mNotificationMaxHeight = maxHeight;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size;
        LayoutParams layoutParams;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean hasFixedHeight = heightMode == 1073741824;
        boolean isHeightLimited = heightMode == Integer.MIN_VALUE;
        int maxSize = Integer.MAX_VALUE;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (hasFixedHeight || isHeightLimited) {
            maxSize = MeasureSpec.getSize(heightMeasureSpec);
        }
        int maxChildHeight = 0;
        if (this.mExpandedChild != null) {
            int spec;
            size = Math.min(maxSize, this.mNotificationMaxHeight);
            layoutParams = this.mExpandedChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                size = Math.min(maxSize, layoutParams.height);
            }
            if (size == Integer.MAX_VALUE) {
                spec = MeasureSpec.makeMeasureSpec(0, 0);
            } else {
                spec = MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
            }
            this.mExpandedChild.measure(widthMeasureSpec, spec);
            maxChildHeight = Math.max(0, this.mExpandedChild.getMeasuredHeight());
        }
        if (this.mContractedChild != null) {
            int heightSpec;
            size = Math.min(maxSize, this.mSmallHeight);
            if (shouldContractedBeFixedSize()) {
                heightSpec = MeasureSpec.makeMeasureSpec(size, 1073741824);
            } else {
                heightSpec = MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
            }
            this.mContractedChild.measure(widthMeasureSpec, heightSpec);
            int measuredHeight = this.mContractedChild.getMeasuredHeight();
            if (measuredHeight < this.mMinContractedHeight) {
                heightSpec = MeasureSpec.makeMeasureSpec(this.mMinContractedHeight, 1073741824);
                this.mContractedChild.measure(widthMeasureSpec, heightSpec);
            }
            maxChildHeight = Math.max(maxChildHeight, measuredHeight);
            if (updateContractedHeaderWidth()) {
                this.mContractedChild.measure(widthMeasureSpec, heightSpec);
            }
            if (this.mExpandedChild != null && this.mContractedChild.getMeasuredHeight() > this.mExpandedChild.getMeasuredHeight()) {
                this.mExpandedChild.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mContractedChild.getMeasuredHeight(), 1073741824));
            }
        }
        if (this.mHeadsUpChild != null) {
            size = Math.min(maxSize, this.mHeadsUpHeight);
            layoutParams = this.mHeadsUpChild.getLayoutParams();
            if (layoutParams.height >= 0) {
                size = Math.min(size, layoutParams.height);
            }
            this.mHeadsUpChild.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE));
            maxChildHeight = Math.max(maxChildHeight, this.mHeadsUpChild.getMeasuredHeight());
        }
        if (this.mSingleLineView != null) {
            int singleLineWidthSpec = widthMeasureSpec;
            if (!(this.mSingleLineWidthIndention == 0 || MeasureSpec.getMode(widthMeasureSpec) == 0)) {
                singleLineWidthSpec = MeasureSpec.makeMeasureSpec((width - this.mSingleLineWidthIndention) + this.mSingleLineView.getPaddingEnd(), Integer.MIN_VALUE);
            }
            this.mSingleLineView.measure(singleLineWidthSpec, MeasureSpec.makeMeasureSpec(maxSize, Integer.MIN_VALUE));
            maxChildHeight = Math.max(maxChildHeight, this.mSingleLineView.getMeasuredHeight());
        }
        setMeasuredDimension(width, Math.min(maxChildHeight, maxSize));
    }

    private boolean updateContractedHeaderWidth() {
        NotificationHeaderView contractedHeader = this.mContractedWrapper.getNotificationHeader();
        if (contractedHeader != null) {
            int paddingEnd;
            int i;
            int paddingTop;
            if (this.mExpandedChild == null || this.mExpandedWrapper.getNotificationHeader() == null) {
                paddingEnd = this.mNotificationContentMarginEnd;
                if (contractedHeader.getPaddingEnd() != paddingEnd) {
                    if (contractedHeader.isLayoutRtl()) {
                        i = paddingEnd;
                    } else {
                        i = contractedHeader.getPaddingLeft();
                    }
                    paddingTop = contractedHeader.getPaddingTop();
                    if (contractedHeader.isLayoutRtl()) {
                        paddingEnd = contractedHeader.getPaddingLeft();
                    }
                    contractedHeader.setPadding(i, paddingTop, paddingEnd, contractedHeader.getPaddingBottom());
                    contractedHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
            }
            NotificationHeaderView expandedHeader = this.mExpandedWrapper.getNotificationHeader();
            int expandedSize = expandedHeader.getMeasuredWidth() - expandedHeader.getPaddingEnd();
            if (expandedSize != contractedHeader.getMeasuredWidth() - expandedHeader.getPaddingEnd()) {
                paddingEnd = contractedHeader.getMeasuredWidth() - expandedSize;
                if (contractedHeader.isLayoutRtl()) {
                    i = paddingEnd;
                } else {
                    i = contractedHeader.getPaddingLeft();
                }
                paddingTop = contractedHeader.getPaddingTop();
                if (contractedHeader.isLayoutRtl()) {
                    paddingEnd = contractedHeader.getPaddingLeft();
                }
                contractedHeader.setPadding(i, paddingTop, paddingEnd, contractedHeader.getPaddingBottom());
                contractedHeader.setShowWorkBadgeAtEnd(true);
                return true;
            }
        }
        return false;
    }

    private boolean shouldContractedBeFixedSize() {
        return this.mBeforeN ? this.mContractedWrapper instanceof NotificationCustomViewWrapper : false;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int previousHeight = 0;
        if (this.mExpandedChild != null) {
            previousHeight = this.mExpandedChild.getHeight();
        }
        super.onLayout(changed, left, top, right, bottom);
        if (!(previousHeight == 0 || this.mExpandedChild.getHeight() == previousHeight)) {
            this.mContentHeightAtAnimationStart = previousHeight;
        }
        updateClipping();
        invalidateOutline();
        selectLayout(false, this.mForceSelectNextLayout);
        this.mForceSelectNextLayout = false;
        updateExpandButtons(this.mExpandable);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    public void reset() {
        if (this.mContractedChild != null) {
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        this.mPreviousExpandedRemoteInputIntent = null;
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.onNotificationUpdateOrReset();
            if (this.mExpandedRemoteInput.isActive()) {
                this.mPreviousExpandedRemoteInputIntent = this.mExpandedRemoteInput.getPendingIntent();
            }
        }
        if (this.mExpandedChild != null) {
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
            this.mExpandedRemoteInput = null;
        }
        this.mPreviousHeadsUpRemoteInputIntent = null;
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.onNotificationUpdateOrReset();
            if (this.mHeadsUpRemoteInput.isActive()) {
                this.mPreviousHeadsUpRemoteInputIntent = this.mHeadsUpRemoteInput.getPendingIntent();
            }
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
            this.mHeadsUpRemoteInput = null;
        }
        this.mContractedChild = null;
        this.mExpandedChild = null;
        this.mHeadsUpChild = null;
    }

    public View getContractedChild() {
        return this.mContractedChild;
    }

    public View getExpandedChild() {
        return this.mExpandedChild;
    }

    public View getHeadsUpChild() {
        return this.mHeadsUpChild;
    }

    public void setContractedChild(View child) {
        if (this.mContractedChild != null) {
            this.mContractedChild.animate().cancel();
            removeView(this.mContractedChild);
        }
        addView(child);
        this.mContractedChild = child;
        this.mContractedWrapper = NotificationViewWrapper.wrap(getContext(), child, this.mContainingNotification);
        this.mContractedWrapper.setDark(this.mDark, false, 0);
    }

    public void setExpandedChild(View child) {
        if (this.mExpandedChild != null) {
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
        }
        addView(child);
        this.mExpandedChild = child;
        this.mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), child, this.mContainingNotification);
    }

    public void setHeadsUpChild(View child) {
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
        }
        addView(child);
        this.mHeadsUpChild = child;
        this.mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), child, this.mContainingNotification);
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateVisibility();
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
    }

    private void setVisible(boolean isVisible) {
        if (isVisible) {
            getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
            getViewTreeObserver().addOnPreDrawListener(this.mEnableAnimationPredrawListener);
            return;
        }
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
        this.mAnimate = false;
    }

    private void focusExpandButtonIfNecessary() {
        if (this.mFocusOnVisibilityChange) {
            NotificationHeaderView header = getVisibleNotificationHeader();
            if (header != null) {
                ImageView expandButton = header.getExpandButton();
                if (expandButton != null) {
                    expandButton.requestAccessibilityFocus();
                }
            }
            this.mFocusOnVisibilityChange = false;
        }
    }

    public void setContentHeight(int contentHeight) {
        this.mContentHeight = Math.max(Math.min(contentHeight, getHeight()), getMinHeight());
        selectLayout(this.mAnimate, false);
        int minHeightHint = getMinContentHeightHint();
        NotificationViewWrapper wrapper = getVisibleWrapper(this.mVisibleType);
        if (wrapper != null) {
            wrapper.setContentHeight(this.mContentHeight, minHeightHint);
        }
        wrapper = getVisibleWrapper(this.mTransformationStartVisibleType);
        if (wrapper != null) {
            wrapper.setContentHeight(this.mContentHeight, minHeightHint);
        }
        updateClipping();
        invalidateOutline();
    }

    private int getMinContentHeightHint() {
        if (this.mIsChildInGroup && isVisibleOrTransitioning(3)) {
            return this.mContext.getResources().getDimensionPixelSize(17105218);
        }
        if (!(this.mHeadsUpChild == null || this.mExpandedChild == null)) {
            boolean transitioningBetweenHunAndExpanded;
            if (isTransitioningFromTo(2, 1)) {
                transitioningBetweenHunAndExpanded = true;
            } else {
                transitioningBetweenHunAndExpanded = isTransitioningFromTo(1, 2);
            }
            boolean z = !isVisibleOrTransitioning(0) ? !this.mIsHeadsUp ? this.mHeadsupDisappearRunning : true : false;
            if (transitioningBetweenHunAndExpanded || r1) {
                return Math.min(this.mHeadsUpChild.getHeight(), this.mExpandedChild.getHeight());
            }
        }
        if (this.mVisibleType == 1 && this.mContentHeightAtAnimationStart >= 0 && this.mExpandedChild != null) {
            return Math.min(this.mContentHeightAtAnimationStart, this.mExpandedChild.getHeight());
        }
        int hint;
        if (this.mHeadsUpChild != null && isVisibleOrTransitioning(2)) {
            hint = this.mHeadsUpChild.getHeight();
        } else if (this.mExpandedChild != null) {
            hint = this.mExpandedChild.getHeight();
        } else if (this.mContractedChild != null) {
            hint = this.mContractedChild.getHeight() + this.mContext.getResources().getDimensionPixelSize(17105218);
        } else {
            HwLog.w("NotificationContentView", "getMinContentHeightHint::mContractedChild is null");
            hint = this.mContext.getResources().getDimensionPixelSize(17105218);
        }
        if (this.mExpandedChild != null && isVisibleOrTransitioning(1)) {
            hint = Math.min(hint, this.mExpandedChild.getHeight());
        }
        return hint;
    }

    private boolean isTransitioningFromTo(int from, int to) {
        if ((this.mTransformationStartVisibleType == from || this.mAnimationStartVisibleType == from) && this.mVisibleType == to) {
            return true;
        }
        return false;
    }

    private boolean isVisibleOrTransitioning(int type) {
        if (this.mVisibleType == type || this.mTransformationStartVisibleType == type || this.mAnimationStartVisibleType == type) {
            return true;
        }
        return false;
    }

    private void updateContentTransformation() {
        int visibleType = calculateVisibleType();
        if (visibleType != this.mVisibleType) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            TransformableView shownView = getTransformableViewForVisibleType(visibleType);
            TransformableView hiddenView = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            shownView.transformFrom(hiddenView, 0.0f);
            getViewForVisibleType(visibleType).setVisibility(0);
            hiddenView.transformTo(shownView, 0.0f);
            this.mVisibleType = visibleType;
            updateBackgroundColor(true);
        }
        if (this.mForceSelectNextLayout) {
            forceUpdateVisibilities();
        }
        if (this.mTransformationStartVisibleType == -1 || this.mVisibleType == this.mTransformationStartVisibleType || getViewForVisibleType(this.mTransformationStartVisibleType) == null) {
            updateViewVisibilities(visibleType);
            updateBackgroundColor(false);
            return;
        }
        shownView = getTransformableViewForVisibleType(this.mVisibleType);
        hiddenView = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
        float transformationAmount = calculateTransformationAmount();
        shownView.transformFrom(hiddenView, transformationAmount);
        hiddenView.transformTo(shownView, transformationAmount);
        updateBackgroundTransformation(transformationAmount);
    }

    private void updateBackgroundTransformation(float transformationAmount) {
        int endColor = getBackgroundColor(this.mVisibleType);
        int startColor = getBackgroundColor(this.mTransformationStartVisibleType);
        if (endColor != startColor) {
            if (startColor == 0) {
                startColor = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            if (endColor == 0) {
                endColor = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            endColor = NotificationUtils.interpolateColors(startColor, endColor, transformationAmount);
        }
        this.mContainingNotification.updateBackgroundAlpha(transformationAmount);
        this.mContainingNotification.setContentBackground(endColor, false, this);
    }

    private float calculateTransformationAmount() {
        int startHeight = getViewForVisibleType(this.mTransformationStartVisibleType) != null ? getViewForVisibleType(this.mTransformationStartVisibleType).getHeight() : 0;
        return Math.min(1.0f, ((float) Math.abs(this.mContentHeight - startHeight)) / ((float) Math.abs((getViewForVisibleType(this.mVisibleType) != null ? getViewForVisibleType(this.mVisibleType).getHeight() : 0) - startHeight)));
    }

    public int getMaxHeight() {
        if (this.mExpandedChild != null) {
            return this.mExpandedChild.getHeight();
        }
        if (this.mIsHeadsUp && this.mHeadsUpChild != null) {
            return this.mHeadsUpChild.getHeight();
        }
        if (this.mContractedChild != null) {
            return this.mContractedChild.getHeight();
        }
        HwLog.w("NotificationContentView", "getMaxHeight::mContractedChild is null!");
        return 0;
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean likeGroupExpanded) {
        if (likeGroupExpanded || !this.mIsChildInGroup || isGroupExpanded()) {
            if (this.mContractedChild != null) {
                return this.mContractedChild.getHeight();
            }
            HwLog.w("NotificationContentView", "getMinHeight::mContractedChild is null!");
        }
        return this.mSingleLineView == null ? 0 : this.mSingleLineView.getHeight();
    }

    private boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    public void setClipTopAmount(int clipTopAmount) {
        this.mClipTopAmount = clipTopAmount;
        updateClipping();
    }

    private void updateClipping() {
        if (this.mClipToActualHeight) {
            this.mClipBounds.set(0, this.mClipTopAmount, getWidth(), this.mContentHeight);
            setClipBounds(this.mClipBounds);
            return;
        }
        setClipBounds(null);
    }

    public void setClipToActualHeight(boolean clipToActualHeight) {
        this.mClipToActualHeight = clipToActualHeight;
        updateClipping();
    }

    private void selectLayout(boolean animate, boolean force) {
        if (this.mContractedChild != null) {
            if (this.mUserExpanding) {
                updateContentTransformation();
            } else {
                boolean changedType;
                int visibleType = calculateVisibleType();
                if (visibleType != this.mVisibleType) {
                    changedType = true;
                } else {
                    changedType = false;
                }
                if (changedType || force) {
                    View visibleView = getViewForVisibleType(visibleType);
                    if (visibleView != null) {
                        visibleView.setVisibility(0);
                        if (!MmsUtils.isMmsNotification(this.mStatusBarNotification)) {
                            transferRemoteInputFocus(visibleType);
                        }
                    }
                    NotificationViewWrapper visibleWrapper = getVisibleWrapper(visibleType);
                    if (visibleWrapper != null) {
                        visibleWrapper.setContentHeight(this.mContentHeight, getMinContentHeightHint());
                    }
                    if (!animate || ((visibleType != 1 || this.mExpandedChild == null) && ((visibleType != 2 || this.mHeadsUpChild == null) && ((visibleType != 3 || this.mSingleLineView == null) && visibleType != 0)))) {
                        updateViewVisibilities(visibleType);
                    } else {
                        animateToVisibleType(visibleType);
                    }
                    this.mVisibleType = visibleType;
                    if (changedType) {
                        focusExpandButtonIfNecessary();
                    }
                    updateBackgroundColor(animate);
                }
            }
        }
    }

    private void forceUpdateVisibilities() {
        boolean contractedVisible = this.mVisibleType != 0 ? this.mTransformationStartVisibleType == 0 : true;
        boolean expandedVisible = this.mVisibleType != 1 ? this.mTransformationStartVisibleType == 1 : true;
        boolean headsUpVisible = this.mVisibleType != 2 ? this.mTransformationStartVisibleType == 2 : true;
        boolean singleLineVisible = this.mVisibleType != 3 ? this.mTransformationStartVisibleType == 3 : true;
        if (contractedVisible) {
            this.mContractedWrapper.setVisible(true);
        } else if (this.mContractedChild != null) {
            this.mContractedChild.setVisibility(4);
        }
        if (this.mExpandedChild != null) {
            if (expandedVisible) {
                this.mExpandedWrapper.setVisible(true);
            } else {
                this.mExpandedChild.setVisibility(4);
            }
        }
        if (this.mHeadsUpChild != null) {
            if (headsUpVisible) {
                this.mHeadsUpWrapper.setVisible(true);
            } else {
                this.mHeadsUpChild.setVisibility(4);
            }
        }
        if (this.mSingleLineView == null) {
            return;
        }
        if (singleLineVisible) {
            this.mSingleLineView.setVisible(true);
        } else {
            this.mSingleLineView.setVisibility(4);
        }
    }

    public void updateBackgroundColor(boolean animate) {
        int customBackgroundColor = getBackgroundColor(this.mVisibleType);
        this.mContainingNotification.resetBackgroundAlpha();
        this.mContainingNotification.setContentBackground(customBackgroundColor, animate, this);
    }

    public int getVisibleType() {
        return this.mVisibleType;
    }

    public int getBackgroundColorForExpansionState() {
        int visibleType;
        if (this.mContainingNotification.isGroupExpanded() || this.mContainingNotification.isUserLocked()) {
            visibleType = calculateVisibleType();
        } else {
            visibleType = getVisibleType();
        }
        return getBackgroundColor(visibleType);
    }

    public int getBackgroundColor(int visibleType) {
        NotificationViewWrapper currentVisibleWrapper = getVisibleWrapper(visibleType);
        if (currentVisibleWrapper != null) {
            return currentVisibleWrapper.getCustomBackgroundColor();
        }
        return 0;
    }

    private void updateViewVisibilities(int visibleType) {
        this.mContractedWrapper.setVisible(visibleType == 0);
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setVisible(visibleType == 1);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setVisible(visibleType == 2);
        }
        if (this.mSingleLineView != null) {
            this.mSingleLineView.setVisible(visibleType == 3);
        }
    }

    private void animateToVisibleType(int visibleType) {
        TransformableView shownView = getTransformableViewForVisibleType(visibleType);
        final TransformableView hiddenView = getTransformableViewForVisibleType(this.mVisibleType);
        if (shownView == hiddenView || hiddenView == null) {
            shownView.setVisible(true);
            return;
        }
        this.mAnimationStartVisibleType = this.mVisibleType;
        shownView.transformFrom(hiddenView);
        View visibleView = getViewForVisibleType(visibleType);
        if (visibleView != null) {
            visibleView.setVisibility(0);
        } else {
            HwLog.w("NotificationContentView", "animateToVisibleType::visibleView is null!");
        }
        hiddenView.transformTo(shownView, new Runnable() {
            public void run() {
                if (hiddenView != NotificationContentView.this.getTransformableViewForVisibleType(NotificationContentView.this.mVisibleType)) {
                    hiddenView.setVisible(false);
                }
                NotificationContentView.this.mAnimationStartVisibleType = -1;
            }
        });
    }

    private void transferRemoteInputFocus(int visibleType) {
        HwLog.i("NotificationContentView", "transferRemoteInputFocus: " + visibleType);
        if (visibleType == 2 && this.mHeadsUpRemoteInput != null && this.mExpandedRemoteInput != null && this.mExpandedRemoteInput.isActive()) {
            this.mHeadsUpRemoteInput.stealFocusFrom(this.mExpandedRemoteInput);
        }
        if (visibleType == 1 && this.mExpandedRemoteInput != null && this.mHeadsUpRemoteInput != null && this.mHeadsUpRemoteInput.isActive()) {
            this.mExpandedRemoteInput.stealFocusFrom(this.mHeadsUpRemoteInput);
        }
    }

    private TransformableView getTransformableViewForVisibleType(int visibleType) {
        switch (visibleType) {
            case 1:
                return this.mExpandedWrapper;
            case 2:
                return this.mHeadsUpWrapper;
            case 3:
                return this.mSingleLineView;
            default:
                return this.mContractedWrapper;
        }
    }

    private View getViewForVisibleType(int visibleType) {
        switch (visibleType) {
            case 1:
                return this.mExpandedChild;
            case 2:
                return this.mHeadsUpChild;
            case 3:
                return this.mSingleLineView;
            default:
                return this.mContractedChild;
        }
    }

    private NotificationViewWrapper getVisibleWrapper(int visibleType) {
        switch (visibleType) {
            case 0:
                return this.mContractedWrapper;
            case 1:
                return this.mExpandedWrapper;
            case 2:
                return this.mHeadsUpWrapper;
            default:
                return null;
        }
    }

    public int calculateVisibleType() {
        if (this.mUserExpanding) {
            int height;
            int collapsedVisualType;
            if (!this.mIsChildInGroup || isGroupExpanded() || this.mContainingNotification.isExpanded(true)) {
                height = this.mContainingNotification.getMaxContentHeight();
            } else {
                height = this.mContainingNotification.getShowingLayout().getMinHeight();
            }
            if (height == 0) {
                height = this.mContentHeight;
            }
            int expandedVisualType = getVisualTypeForHeight((float) height);
            if (!this.mIsChildInGroup || isGroupExpanded()) {
                collapsedVisualType = getVisualTypeForHeight((float) this.mContainingNotification.getCollapsedHeight());
            } else {
                collapsedVisualType = 3;
            }
            if (this.mTransformationStartVisibleType != collapsedVisualType) {
                expandedVisualType = collapsedVisualType;
            }
            return expandedVisualType;
        }
        int intrinsicHeight = this.mContainingNotification.getIntrinsicHeight();
        int viewHeight = this.mContentHeight;
        if (intrinsicHeight != 0) {
            viewHeight = Math.min(this.mContentHeight, intrinsicHeight);
        }
        return getVisualTypeForHeight((float) viewHeight);
    }

    private int getVisualTypeForHeight(float viewHeight) {
        boolean noExpandedChild;
        if (this.mExpandedChild == null) {
            noExpandedChild = true;
        } else {
            noExpandedChild = false;
        }
        if (!noExpandedChild && viewHeight == ((float) this.mExpandedChild.getHeight()) && (!this.mIsHeadsUp || !MmsUtils.isMmsNotification(this.mStatusBarNotification))) {
            return 1;
        }
        if (!this.mUserExpanding && this.mIsChildInGroup && !isGroupExpanded()) {
            return 3;
        }
        if ((!this.mIsHeadsUp && !this.mHeadsupDisappearRunning) || this.mHeadsUpChild == null) {
            return (noExpandedChild || !(this.mContractedChild == null || viewHeight > ((float) this.mContractedChild.getHeight()) || (this.mIsChildInGroup && !isGroupExpanded() && this.mContainingNotification.isExpanded(true)))) ? 0 : 1;
        } else {
            if (viewHeight <= ((float) this.mHeadsUpChild.getHeight()) || noExpandedChild) {
                return 2;
            }
            return 1;
        }
    }

    public boolean isContentExpandable() {
        return this.mExpandedChild != null;
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (this.mContractedChild != null) {
            this.mDark = dark;
            if (this.mVisibleType == 0 || !dark) {
                this.mContractedWrapper.setDark(dark, fade, delay);
            }
            if (this.mVisibleType == 1 || !(this.mExpandedChild == null || dark)) {
                this.mExpandedWrapper.setDark(dark, fade, delay);
            }
            if (this.mVisibleType == 2 || !(this.mHeadsUpChild == null || dark)) {
                this.mHeadsUpWrapper.setDark(dark, fade, delay);
            }
            if (this.mSingleLineView != null && (this.mVisibleType == 3 || !dark)) {
                this.mSingleLineView.setDark(dark, fade, delay);
            }
        }
    }

    public void setHeadsUp(boolean headsUp) {
        this.mIsHeadsUp = headsUp;
        selectLayout(false, true);
        updateExpandButtons(this.mExpandable);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setShowingLegacyBackground(boolean showing) {
        this.mShowingLegacyBackground = showing;
        updateShowingLegacyBackground();
    }

    private void updateShowingLegacyBackground() {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setShowingLegacyBackground(this.mShowingLegacyBackground);
        }
    }

    public void setIsChildInGroup(boolean isChildInGroup) {
        this.mIsChildInGroup = isChildInGroup;
        updateSingleLineView();
    }

    public void onNotificationUpdated(Entry entry) {
        boolean z;
        this.mStatusBarNotification = entry.notification;
        if (entry.targetSdk < 24) {
            z = true;
        } else {
            z = false;
        }
        this.mBeforeN = z;
        updateSingleLineView();
        applyRemoteInput(entry);
        if (this.mContractedChild != null) {
            this.mContractedWrapper.notifyContentUpdated(entry.notification);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.notifyContentUpdated(entry.notification);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.notifyContentUpdated(entry.notification);
        }
        updateShowingLegacyBackground();
        this.mForceSelectNextLayout = true;
        setDark(this.mDark, false, 0);
        this.mPreviousExpandedRemoteInputIntent = null;
        this.mPreviousHeadsUpRemoteInputIntent = null;
    }

    private void updateSingleLineView() {
        if (this.mIsChildInGroup) {
            this.mSingleLineView = this.mHybridGroupManager.bindFromNotification(this.mSingleLineView, this.mStatusBarNotification);
        } else if (this.mSingleLineView != null) {
            removeView(this.mSingleLineView);
            this.mSingleLineView = null;
        }
    }

    private void applyRemoteInput(Entry entry) {
        if (this.mRemoteInputController != null) {
            boolean hasRemoteInput = false;
            Action[] actions = entry.notification.getNotification().actions;
            if (actions != null) {
                for (Action a : actions) {
                    if (a.getRemoteInputs() != null) {
                        for (RemoteInput ri : a.getRemoteInputs()) {
                            if (ri.getAllowFreeFormInput()) {
                                hasRemoteInput = true;
                                break;
                            }
                        }
                    }
                }
            }
            View bigContentView = this.mExpandedChild;
            if (bigContentView != null) {
                this.mExpandedRemoteInput = applyRemoteInput(bigContentView, entry, hasRemoteInput, this.mPreviousExpandedRemoteInputIntent);
            } else {
                this.mExpandedRemoteInput = null;
            }
            View headsUpContentView = this.mHeadsUpChild;
            if (headsUpContentView != null) {
                this.mHeadsUpRemoteInput = applyRemoteInput(headsUpContentView, entry, hasRemoteInput, this.mPreviousHeadsUpRemoteInputIntent);
            } else {
                this.mHeadsUpRemoteInput = null;
            }
        }
    }

    private RemoteInputView applyRemoteInput(View view, Entry entry, boolean hasRemoteInput, PendingIntent existingPendingIntent) {
        View actionContainerCandidate = view.findViewById(16909214);
        if (!(actionContainerCandidate instanceof FrameLayout)) {
            return null;
        }
        View viewActions = actionContainerCandidate.findViewById(16909210);
        if (viewActions != null) {
            viewActions.setBackground(getContext().getResources().getDrawable(R.drawable.actions_container_background));
        }
        RemoteInputView existing = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
        if (existing != null) {
            existing.onNotificationUpdateOrReset();
        }
        if (existing == null && hasRemoteInput) {
            FrameLayout actionContainer = (FrameLayout) actionContainerCandidate;
            RemoteInputView riv = RemoteInputView.inflate(SystemUiUtil.getHwThemeLightContext(this.mContext), actionContainer, entry, this.mRemoteInputController);
            actionContainer.addView(riv, new FrameLayout.LayoutParams(-1, -1));
            riv.setTopView(view);
            riv.setVisibility(4);
            existing = riv;
        }
        if (hasRemoteInput) {
            existing.setTopView(view);
            if (existingPendingIntent != null || existing.isActive()) {
                Action[] actions = entry.notification.getNotification().actions;
                if (existingPendingIntent != null) {
                    existing.setPendingIntent(existingPendingIntent);
                }
                if (existing.updatePendingIntentFromActions(actions)) {
                    if (!existing.isActive()) {
                        existing.focus();
                    }
                } else if (existing.isActive()) {
                    existing.close();
                }
            }
        }
        return existing;
    }

    public void closeRemoteInput() {
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.close();
        }
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.close();
        }
    }

    public void setGroupManager(NotificationGroupManager groupManager) {
        this.mGroupManager = groupManager;
    }

    public void setRemoteInputController(RemoteInputController r) {
        this.mRemoteInputController = r;
    }

    public void setExpandClickListener(OnClickListener expandClickListener) {
        this.mExpandClickListener = expandClickListener;
    }

    public void updateExpandButtons(boolean expandable) {
        this.mExpandable = expandable;
        if (!(this.mExpandedChild == null || this.mExpandedChild.getHeight() == 0)) {
            if ((!this.mIsHeadsUp || this.mHeadsUpChild == null) && this.mContractedChild != null) {
                if (this.mExpandedChild.getHeight() == this.mContractedChild.getHeight()) {
                    expandable = false;
                }
            } else if (this.mHeadsUpChild != null && this.mExpandedChild.getHeight() == this.mHeadsUpChild.getHeight()) {
                expandable = false;
            }
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.updateExpandability(expandable, this.mExpandClickListener);
        }
        if (this.mContractedChild != null) {
            this.mContractedWrapper.updateExpandability(expandable, this.mExpandClickListener);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.updateExpandability(expandable, this.mExpandClickListener);
        }
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView header = null;
        if (this.mContractedChild != null) {
            header = this.mContractedWrapper.getNotificationHeader();
        }
        if (header == null && this.mExpandedChild != null) {
            header = this.mExpandedWrapper.getNotificationHeader();
        }
        if (header != null || this.mHeadsUpChild == null) {
            return header;
        }
        return this.mHeadsUpWrapper.getNotificationHeader();
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationViewWrapper wrapper = getVisibleWrapper(this.mVisibleType);
        if (wrapper == null) {
            return null;
        }
        return wrapper.getNotificationHeader();
    }

    public void setContainingNotification(ExpandableNotificationRow containingNotification) {
        this.mContainingNotification = containingNotification;
    }

    public void requestSelectLayout(boolean needsAnimation) {
        selectLayout(needsAnimation, false);
    }

    public void reInflateViews() {
        if (this.mIsChildInGroup && this.mSingleLineView != null) {
            removeView(this.mSingleLineView);
            this.mSingleLineView = null;
            updateSingleLineView();
        }
    }

    public void setUserExpanding(boolean userExpanding) {
        this.mUserExpanding = userExpanding;
        if (userExpanding) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            return;
        }
        this.mTransformationStartVisibleType = -1;
        this.mVisibleType = calculateVisibleType();
        updateViewVisibilities(this.mVisibleType);
        updateBackgroundColor(false);
    }

    public void setSingleLineWidthIndention(int singleLineWidthIndention) {
        if (singleLineWidthIndention != this.mSingleLineWidthIndention) {
            this.mSingleLineWidthIndention = singleLineWidthIndention;
            this.mContainingNotification.forceLayout();
            forceLayout();
        }
    }

    public HybridNotificationView getSingleLineView() {
        return this.mSingleLineView;
    }

    public void setRemoved() {
        if (this.mExpandedRemoteInput != null) {
            this.mExpandedRemoteInput.setRemoved();
        }
        if (this.mHeadsUpRemoteInput != null) {
            this.mHeadsUpRemoteInput.setRemoved();
        }
    }

    public void setContentHeightAnimating(boolean animating) {
        if (!animating) {
            this.mContentHeightAtAnimationStart = -1;
        }
    }

    public void setHeadsupDisappearRunning(boolean headsupDisappearRunning) {
        this.mHeadsupDisappearRunning = headsupDisappearRunning;
        selectLayout(false, true);
    }

    public void setFocusOnVisibilityChange() {
        this.mFocusOnVisibilityChange = true;
    }

    public boolean isSingleView() {
        return this.mVisibleType == 3;
    }
}
