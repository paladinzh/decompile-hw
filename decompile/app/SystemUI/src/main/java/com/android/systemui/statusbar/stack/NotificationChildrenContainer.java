package com.android.systemui.statusbar.stack;

import android.app.Notification.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationHeaderUtil;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NotificationChildrenContainer extends ViewGroup {
    private int mActualHeight;
    private int mChildPadding;
    private final List<ExpandableNotificationRow> mChildren;
    private boolean mChildrenExpanded;
    private float mCollapsedBottompadding;
    private int mDividerHeight;
    private int mDividerPadding;
    private final List<View> mDividers;
    private ViewState mGroupOverFlowState;
    private int mHeaderHeight;
    private NotificationHeaderUtil mHeaderUtil;
    private ViewState mHeaderViewState;
    private final HybridGroupManager mHybridGroupManager;
    private int mMaxNotificationHeight;
    private boolean mNeverAppliedGroupState;
    private NotificationHeaderView mNotificationHeader;
    private int mNotificationHeaderMargin;
    private NotificationViewWrapper mNotificationHeaderWrapper;
    private ExpandableNotificationRow mNotificationParent;
    private int mNotificatonTopPadding;
    private ViewInvertHelper mOverflowInvertHelper;
    private TextView mOverflowNumber;
    private int mRealHeight;
    private boolean mUserLocked;

    public NotificationChildrenContainer(Context context) {
        this(context, null);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDividers = new ArrayList();
        this.mChildren = new ArrayList();
        initDimens();
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
    }

    private void initDimens() {
        this.mChildPadding = getResources().getDimensionPixelSize(R.dimen.notification_children_padding);
        this.mDividerHeight = Math.max(1, getResources().getDimensionPixelSize(R.dimen.hw_notification_child_divider_height));
        this.mDividerPadding = getResources().getDimensionPixelSize(R.dimen.hw_notification_child_divider_padding);
        this.mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.notification_header_height);
        this.mMaxNotificationHeight = getResources().getDimensionPixelSize(R.dimen.notification_max_height);
        this.mNotificationHeaderMargin = getResources().getDimensionPixelSize(17104962);
        this.mNotificatonTopPadding = getResources().getDimensionPixelSize(R.dimen.notification_children_container_top_padding);
        this.mCollapsedBottompadding = (float) getResources().getDimensionPixelSize(17104963);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = Math.min(this.mChildren.size(), 8);
        for (int i = 0; i < childCount; i++) {
            View child = (View) this.mChildren.get(i);
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            ((View) this.mDividers.get(i)).layout(0, 0, getWidth(), this.mDividerHeight);
            if (i == 0) {
                ((View) this.mDividers.get(i)).setPadding(0, 0, 0, 0);
            } else {
                ((View) this.mDividers.get(i)).setPadding(this.mDividerPadding, 0, this.mDividerPadding, 0);
            }
        }
        if (this.mOverflowNumber != null) {
            if (getLayoutDirection() == 1) {
                this.mOverflowNumber.layout(0, 0, this.mOverflowNumber.getMeasuredWidth(), this.mOverflowNumber.getMeasuredHeight());
            } else {
                this.mOverflowNumber.layout(getWidth() - this.mOverflowNumber.getMeasuredWidth(), 0, getWidth(), this.mOverflowNumber.getMeasuredHeight());
            }
        }
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.layout(0, 0, this.mNotificationHeader.getMeasuredWidth(), this.mNotificationHeader.getMeasuredHeight());
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ownMaxHeight = this.mMaxNotificationHeight;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean hasFixedHeight = heightMode == 1073741824;
        boolean isHeightLimited = heightMode == Integer.MIN_VALUE;
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (hasFixedHeight || isHeightLimited) {
            ownMaxHeight = Math.min(ownMaxHeight, size);
        }
        int newHeightSpec = MeasureSpec.makeMeasureSpec(ownMaxHeight, Integer.MIN_VALUE);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mOverflowNumber != null) {
            this.mOverflowNumber.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), newHeightSpec);
        }
        int dividerHeightSpec = MeasureSpec.makeMeasureSpec(this.mDividerHeight, 1073741824);
        int height = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        int childCount = Math.min(this.mChildren.size(), 8);
        int collapsedChildren = getMaxAllowedVisibleChildren(true);
        int overflowIndex = childCount > collapsedChildren ? collapsedChildren - 1 : -1;
        ExpandableNotificationRow lastRow = null;
        int i = 0;
        while (i < childCount) {
            int i2;
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            child.updateChildBackground(true);
            if (!(i == overflowIndex) || this.mOverflowNumber == null) {
                i2 = 0;
            } else {
                i2 = this.mOverflowNumber.getMeasuredWidth();
            }
            child.setSingleLineWidthIndention(i2);
            child.measure(widthMeasureSpec, newHeightSpec);
            ((View) this.mDividers.get(i)).measure(widthMeasureSpec, dividerHeightSpec);
            if (child.getVisibility() != 8) {
                height += child.getMeasuredHeight() + this.mDividerHeight;
                lastRow = child;
            }
            i++;
        }
        if (lastRow != null) {
            lastRow.updateLastChildBackground();
        }
        this.mRealHeight = height;
        if (heightMode != 0) {
            height = Math.min(height, size);
        }
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        setMeasuredDimension(width, height);
    }

    public boolean pointInView(float localX, float localY, float slop) {
        if (localX < (-slop) || localY < (-slop) || localX >= ((float) (this.mRight - this.mLeft)) + slop || localY >= ((float) this.mRealHeight) + slop) {
            return false;
        }
        return true;
    }

    public void addNotification(ExpandableNotificationRow row, int childIndex) {
        HwLog.i("NotificationChildrenContainer", "addNotification: group=" + row.getStatusBarNotification().getGroupKey() + ", key=" + row.getStatusBarNotification().getKey() + ", index=" + childIndex);
        int newIndex = (childIndex < 0 || childIndex > this.mChildren.size()) ? this.mChildren.size() : childIndex;
        if (row.getParent() != null) {
            HwLog.w("NotificationChildrenContainer", "addNotification failed");
            return;
        }
        row.updateChildBackground(true);
        this.mChildren.add(newIndex, row);
        addView(row, 0);
        row.setUserLocked(this.mUserLocked);
        View divider = inflateDivider();
        addView(divider, 0);
        this.mDividers.add(newIndex, divider);
        updateGroupOverflow();
    }

    public void afterAddNotification() {
        this.mNotificationParent.setUserLocked(false);
    }

    public void removeNotification(ExpandableNotificationRow row) {
        HwLog.i("NotificationChildrenContainer", "removeNotification: group=" + row.getStatusBarNotification().getGroupKey() + ", key=" + row.getStatusBarNotification().getKey());
        row.updateChildBackground(false);
        int childIndex = this.mChildren.indexOf(row);
        this.mChildren.remove(row);
        removeView(row);
        final View divider = (View) this.mDividers.remove(childIndex);
        removeView(divider);
        getOverlay().add(divider);
        CrossFadeHelper.fadeOut(divider, new Runnable() {
            public void run() {
                NotificationChildrenContainer.this.getOverlay().remove(divider);
            }
        });
        row.setSystemChildExpanded(false);
        row.setUserLocked(false);
        updateGroupOverflow();
        if (!row.isRemoved()) {
            this.mHeaderUtil.restoreNotificationHeader(row);
        }
    }

    public int getNotificationChildCount() {
        return this.mChildren.size();
    }

    public void recreateNotificationHeader(OnClickListener listener, StatusBarNotification notification) {
        try {
            Builder builder = Builder.recoverBuilder(getContext(), this.mNotificationParent.getStatusBarNotification().getNotification());
            if (builder != null) {
                RemoteViews header = builder.makeNotificationHeader();
                if (this.mNotificationHeader == null) {
                    this.mNotificationHeader = (NotificationHeaderView) header.apply(getContext(), this);
                    this.mNotificationHeader.findViewById(16909228).setVisibility(0);
                    this.mNotificationHeader.setOnClickListener(listener);
                    this.mNotificationHeaderWrapper = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeader, this.mNotificationParent);
                    addView(this.mNotificationHeader, 0);
                    invalidate();
                } else {
                    header.reapply(getContext(), this.mNotificationHeader);
                    this.mNotificationHeaderWrapper.notifyContentUpdated(notification);
                    setChildrenExpanded(this.mChildrenExpanded);
                }
                updateChildrenHeaderAppearance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateChildrenHeaderAppearance() {
        this.mHeaderUtil.updateChildrenHeaderAppearance();
    }

    public void updateGroupOverflow() {
        int childCount = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        if (childCount > maxAllowedVisibleChildren) {
            this.mOverflowNumber = this.mHybridGroupManager.bindOverflowNumber(this.mOverflowNumber, childCount - maxAllowedVisibleChildren);
            if (this.mOverflowInvertHelper == null) {
                this.mOverflowInvertHelper = new ViewInvertHelper(this.mOverflowNumber, 700);
            }
            if (this.mGroupOverFlowState == null) {
                this.mGroupOverFlowState = new ViewState();
                this.mNeverAppliedGroupState = true;
            }
        } else if (this.mOverflowNumber != null) {
            removeView(this.mOverflowNumber);
            if (isShown()) {
                final View removedOverflowNumber = this.mOverflowNumber;
                addTransientView(removedOverflowNumber, getTransientViewCount());
                CrossFadeHelper.fadeOut(removedOverflowNumber, new Runnable() {
                    public void run() {
                        NotificationChildrenContainer.this.removeTransientView(removedOverflowNumber);
                    }
                });
            }
            this.mOverflowNumber = null;
            this.mOverflowInvertHelper = null;
            this.mGroupOverFlowState = null;
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGroupOverflow();
    }

    private View inflateDivider() {
        return LayoutInflater.from(this.mContext).inflate(R.layout.hw_notification_children_divider, this, false);
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        return this.mChildren;
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> childOrder) {
        if (childOrder == null) {
            return false;
        }
        boolean result = false;
        int i = 0;
        while (i < this.mChildren.size() && i < childOrder.size()) {
            ExpandableNotificationRow desiredChild = (ExpandableNotificationRow) childOrder.get(i);
            if (((ExpandableNotificationRow) this.mChildren.get(i)) != desiredChild) {
                this.mChildren.remove(desiredChild);
                this.mChildren.add(i, desiredChild);
                result = true;
            }
            i++;
        }
        updateExpansionStates();
        return result;
    }

    private void updateExpansionStates() {
        if (!this.mChildrenExpanded && !this.mUserLocked) {
            int size = this.mChildren.size();
            for (int i = 0; i < size; i++) {
                boolean z;
                ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
                if (i == 0 && size == 1) {
                    z = true;
                } else {
                    z = false;
                }
                child.setSystemChildExpanded(z);
            }
        }
    }

    public int getIntrinsicHeight() {
        return getIntrinsicHeight((float) getMaxAllowedVisibleChildren());
    }

    private int getIntrinsicHeight(float maxAllowedVisibleChildren) {
        int intrinsicHeight = this.mNotificationHeaderMargin;
        int visibleChildren = 0;
        int childCount = this.mChildren.size();
        boolean firstChild = true;
        float expandFactor = 0.0f;
        if (this.mUserLocked) {
            expandFactor = getGroupExpandFraction();
        }
        for (int i = 0; i < childCount && ((float) visibleChildren) < maxAllowedVisibleChildren; i++) {
            if (firstChild) {
                if (this.mUserLocked) {
                    intrinsicHeight = (int) (((float) intrinsicHeight) + NotificationUtils.interpolate(0.0f, (float) (this.mNotificatonTopPadding + this.mDividerHeight), expandFactor));
                } else {
                    int i2;
                    if (this.mChildrenExpanded) {
                        i2 = this.mNotificatonTopPadding + this.mDividerHeight;
                    } else {
                        i2 = 0;
                    }
                    intrinsicHeight += i2;
                }
                firstChild = false;
            } else if (this.mUserLocked) {
                intrinsicHeight = (int) (((float) intrinsicHeight) + NotificationUtils.interpolate((float) this.mChildPadding, (float) this.mDividerHeight, expandFactor));
            } else {
                intrinsicHeight += this.mChildrenExpanded ? this.mDividerHeight : this.mChildPadding;
            }
            intrinsicHeight += ((ExpandableNotificationRow) this.mChildren.get(i)).getIntrinsicHeight();
            visibleChildren++;
        }
        if (this.mUserLocked) {
            return (int) (((float) intrinsicHeight) + NotificationUtils.interpolate(this.mCollapsedBottompadding, 0.0f, expandFactor));
        }
        if (this.mChildrenExpanded) {
            return intrinsicHeight;
        }
        return (int) (((float) intrinsicHeight) + this.mCollapsedBottompadding);
    }

    public void getState(StackScrollState resultState, StackViewState parentState) {
        boolean childrenExpanded;
        float translationZ;
        int childCount = this.mChildren.size();
        int yPosition = this.mNotificationHeaderMargin;
        boolean firstChild = true;
        int lastVisibleIndex = getMaxAllowedVisibleChildren() - 1;
        int firstOverflowIndex = lastVisibleIndex + 1;
        float expandFactor = 0.0f;
        if (this.mUserLocked) {
            expandFactor = getGroupExpandFraction();
            firstOverflowIndex = getMaxAllowedVisibleChildren(true);
        }
        if (this.mNotificationParent.isGroupExpansionChanging()) {
            childrenExpanded = false;
        } else {
            childrenExpanded = this.mChildrenExpanded;
        }
        int parentHeight = parentState.height;
        int i = 0;
        while (i < childCount) {
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            if (firstChild) {
                if (this.mUserLocked) {
                    yPosition = (int) (((float) yPosition) + NotificationUtils.interpolate(0.0f, (float) (this.mNotificatonTopPadding + this.mDividerHeight), expandFactor));
                } else {
                    yPosition += this.mChildrenExpanded ? this.mNotificatonTopPadding + this.mDividerHeight : 0;
                }
                firstChild = false;
            } else if (this.mUserLocked) {
                yPosition = (int) (((float) yPosition) + NotificationUtils.interpolate((float) this.mChildPadding, (float) this.mDividerHeight, expandFactor));
            } else {
                yPosition += this.mChildrenExpanded ? this.mDividerHeight : this.mChildPadding;
            }
            StackViewState childState = resultState.getViewStateForView(child);
            int intrinsicHeight = child.getIntrinsicHeight();
            if (!childrenExpanded) {
                childState.hidden = false;
                childState.height = intrinsicHeight;
                childState.isBottomClipped = false;
            } else if (updateChildStateForExpandedGroup(child, parentHeight, childState, yPosition)) {
                childState.isBottomClipped = true;
            }
            childState.yTranslation = (float) yPosition;
            if (childrenExpanded) {
                translationZ = this.mNotificationParent.getTranslationZ();
            } else {
                translationZ = 0.0f;
            }
            childState.zTranslation = translationZ;
            childState.dimmed = parentState.dimmed;
            childState.dark = parentState.dark;
            childState.hideSensitive = parentState.hideSensitive;
            childState.belowSpeedBump = parentState.belowSpeedBump;
            childState.clipTopAmount = 0;
            childState.alpha = 0.0f;
            if (i < firstOverflowIndex) {
                childState.alpha = 1.0f;
            } else if (expandFactor == 1.0f && i <= lastVisibleIndex) {
                childState.alpha = (((float) this.mActualHeight) - childState.yTranslation) / ((float) childState.height);
                childState.alpha = Math.max(0.0f, Math.min(1.0f, childState.alpha));
            }
            childState.location = parentState.location;
            yPosition += intrinsicHeight;
            i++;
        }
        if (this.mOverflowNumber != null) {
            ExpandableNotificationRow overflowView = (ExpandableNotificationRow) this.mChildren.get(Math.min(getMaxAllowedVisibleChildren(true), childCount) - 1);
            this.mGroupOverFlowState.copyFrom(resultState.getViewStateForView(overflowView));
            ViewState viewState;
            if (this.mChildrenExpanded) {
                viewState = this.mGroupOverFlowState;
                viewState.yTranslation += (float) this.mNotificationHeaderMargin;
                this.mGroupOverFlowState.alpha = 0.0f;
            } else if (this.mUserLocked) {
                View singleLineView = overflowView.getSingleLineView();
                if (singleLineView != null) {
                    View mirrorView = singleLineView.getTextView();
                    if (mirrorView.getVisibility() == 8) {
                        mirrorView = singleLineView.getTitleView();
                    }
                    if (mirrorView.getVisibility() == 8) {
                        mirrorView = singleLineView;
                    }
                    viewState = this.mGroupOverFlowState;
                    viewState.yTranslation += NotificationUtils.getRelativeYOffset(mirrorView, overflowView);
                    this.mGroupOverFlowState.alpha = mirrorView.getAlpha();
                }
            }
        }
        if (this.mNotificationHeader != null) {
            if (this.mHeaderViewState == null) {
                this.mHeaderViewState = new ViewState();
            }
            this.mHeaderViewState.initFrom(this.mNotificationHeader);
            ViewState viewState2 = this.mHeaderViewState;
            if (childrenExpanded) {
                translationZ = this.mNotificationParent.getTranslationZ();
            } else {
                translationZ = 0.0f;
            }
            viewState2.zTranslation = translationZ;
        }
    }

    private boolean updateChildStateForExpandedGroup(ExpandableNotificationRow child, int parentHeight, StackViewState childState, int yPosition) {
        boolean z;
        int intrinsicHeight = child.getIntrinsicHeight();
        int newHeight = intrinsicHeight;
        if (intrinsicHeight == 0) {
            z = true;
        } else {
            z = false;
        }
        childState.hidden = z;
        childState.height = intrinsicHeight;
        if (childState.height == intrinsicHeight || childState.hidden) {
            return false;
        }
        return true;
    }

    private int getMaxAllowedVisibleChildren() {
        return getMaxAllowedVisibleChildren(false);
    }

    private int getMaxAllowedVisibleChildren(boolean likeCollapsed) {
        if (!likeCollapsed && (this.mChildrenExpanded || this.mNotificationParent.isUserLocked())) {
            return 8;
        }
        if (this.mNotificationParent.isOnKeyguard() || (!this.mNotificationParent.isExpanded() && !this.mNotificationParent.isHeadsUp())) {
            return 2;
        }
        return 5;
    }

    public void applyState(StackScrollState state) {
        int childCount = this.mChildren.size();
        ViewState tmpState = new ViewState();
        float expandFraction = 0.0f;
        if (this.mUserLocked) {
            expandFraction = getGroupExpandFraction();
        }
        boolean dividersVisible = this.mUserLocked;
        for (int i = 0; i < childCount; i++) {
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            StackViewState viewState = state.getViewStateForView(child);
            state.applyState(child, viewState);
            View divider = (View) this.mDividers.get(i);
            tmpState.initFrom(divider);
            tmpState.yTranslation = viewState.yTranslation - ((float) this.mDividerHeight);
            float alpha = (!this.mChildrenExpanded || viewState.alpha == 0.0f) ? 0.0f : 1.0f;
            if (this.mUserLocked && viewState.alpha != 0.0f) {
                alpha = NotificationUtils.interpolate(0.0f, 1.0f, Math.min(viewState.alpha, expandFraction));
            }
            tmpState.hidden = dividersVisible;
            tmpState.alpha = alpha;
            state.applyViewState(divider, tmpState);
            child.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            child.setOutlineProvider(null);
        }
        if (this.mOverflowNumber != null) {
            state.applyViewState(this.mOverflowNumber, this.mGroupOverFlowState);
            this.mNeverAppliedGroupState = false;
        }
        if (this.mNotificationHeader != null) {
            state.applyViewState(this.mNotificationHeader, this.mHeaderViewState);
        }
    }

    public void prepareExpansionChanged(StackScrollState state) {
    }

    public void startAnimationToState(StackScrollState state, StackStateAnimator stateAnimator, long baseDelay, long duration) {
        int childCount = this.mChildren.size();
        ViewState tmpState = new ViewState();
        float expandFraction = getGroupExpandFraction();
        boolean dividersVisible = this.mUserLocked;
        for (int i = childCount - 1; i >= 0; i--) {
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            StackViewState viewState = state.getViewStateForView(child);
            stateAnimator.startStackAnimations(child, viewState, state, -1, baseDelay);
            View divider = (View) this.mDividers.get(i);
            tmpState.initFrom(divider);
            tmpState.yTranslation = viewState.yTranslation - ((float) this.mDividerHeight);
            float alpha = (!this.mChildrenExpanded || viewState.alpha == 0.0f) ? 0.0f : 1.0f;
            if (this.mUserLocked && viewState.alpha != 0.0f) {
                alpha = NotificationUtils.interpolate(0.0f, 1.0f, Math.min(viewState.alpha, expandFraction));
            }
            tmpState.hidden = dividersVisible;
            tmpState.alpha = alpha;
            stateAnimator.startViewAnimations(divider, tmpState, baseDelay, duration);
            child.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            child.setOutlineProvider(null);
        }
        if (this.mOverflowNumber != null) {
            if (this.mNeverAppliedGroupState) {
                alpha = this.mGroupOverFlowState.alpha;
                this.mGroupOverFlowState.alpha = 0.0f;
                state.applyViewState(this.mOverflowNumber, this.mGroupOverFlowState);
                this.mGroupOverFlowState.alpha = alpha;
                this.mNeverAppliedGroupState = false;
            }
            stateAnimator.startViewAnimations(this.mOverflowNumber, this.mGroupOverFlowState, baseDelay, duration);
        }
        if (this.mNotificationHeader != null) {
            state.applyViewState(this.mNotificationHeader, this.mHeaderViewState);
        }
    }

    public ExpandableNotificationRow getViewAtPosition(float y) {
        int count = this.mChildren.size();
        for (int childIdx = 0; childIdx < count; childIdx++) {
            ExpandableNotificationRow slidingChild = (ExpandableNotificationRow) this.mChildren.get(childIdx);
            float childTop = slidingChild.getTranslationY();
            float bottom = childTop + ((float) slidingChild.getActualHeight());
            if (y >= childTop + ((float) slidingChild.getClipTopAmount()) && y <= bottom) {
                return slidingChild;
            }
        }
        return null;
    }

    public void setChildrenExpanded(boolean childrenExpanded) {
        this.mChildrenExpanded = childrenExpanded;
        updateExpansionStates();
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.setExpanded(childrenExpanded);
        }
        int count = this.mChildren.size();
        for (int childIdx = 0; childIdx < count; childIdx++) {
            ((ExpandableNotificationRow) this.mChildren.get(childIdx)).setChildrenExpanded(childrenExpanded, false);
        }
    }

    public void setNotificationParent(ExpandableNotificationRow parent) {
        this.mNotificationParent = parent;
        this.mHeaderUtil = new NotificationHeaderUtil(this.mNotificationParent);
    }

    public NotificationHeaderView getHeaderView() {
        return this.mNotificationHeader;
    }

    public void updateHeaderVisibility(int visiblity) {
        if (this.mNotificationHeader != null) {
            this.mNotificationHeader.setVisibility(visiblity);
        }
    }

    public int getMaxContentHeight() {
        int maxContentHeight = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        int visibleChildren = 0;
        int childCount = this.mChildren.size();
        for (int i = 0; i < childCount && visibleChildren < 8; i++) {
            int maxExpandHeight;
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            if (child.isExpanded(true)) {
                maxExpandHeight = child.getMaxExpandHeight();
            } else {
                maxExpandHeight = child.getShowingLayout().getMinHeight(true);
            }
            maxContentHeight = (int) (((float) maxContentHeight) + ((float) maxExpandHeight));
            visibleChildren++;
        }
        if (visibleChildren > 0) {
            return maxContentHeight + (this.mDividerHeight * visibleChildren);
        }
        return maxContentHeight;
    }

    public void setActualHeight(int actualHeight) {
        if (this.mUserLocked) {
            this.mActualHeight = actualHeight;
            float fraction = getGroupExpandFraction();
            int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
            int childCount = this.mChildren.size();
            for (int i = 0; i < childCount; i++) {
                int maxExpandHeight;
                ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
                if (child.isExpanded(true)) {
                    maxExpandHeight = child.getMaxExpandHeight();
                } else {
                    maxExpandHeight = child.getShowingLayout().getMinHeight(true);
                }
                float childHeight = (float) maxExpandHeight;
                if (i < maxAllowedVisibleChildren) {
                    child.setActualHeight((int) NotificationUtils.interpolate((float) child.getShowingLayout().getMinHeight(false), childHeight, fraction), false);
                } else {
                    child.setActualHeight((int) childHeight, false);
                }
            }
        }
    }

    public float getGroupExpandFraction() {
        int visibleChildrenExpandedHeight = getVisibleChildrenExpandHeight();
        int minExpandHeight = getCollapsedHeight();
        return Math.max(0.0f, Math.min(1.0f, ((float) (this.mActualHeight - minExpandHeight)) / ((float) (visibleChildrenExpandedHeight - minExpandHeight))));
    }

    private int getVisibleChildrenExpandHeight() {
        int intrinsicHeight = (this.mNotificationHeaderMargin + this.mNotificatonTopPadding) + this.mDividerHeight;
        int visibleChildren = 0;
        int childCount = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        for (int i = 0; i < childCount && visibleChildren < maxAllowedVisibleChildren; i++) {
            int maxExpandHeight;
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            if (child.isExpanded(true)) {
                maxExpandHeight = child.getMaxExpandHeight();
            } else {
                maxExpandHeight = child.getShowingLayout().getMinHeight(true);
            }
            intrinsicHeight = (int) (((float) intrinsicHeight) + ((float) maxExpandHeight));
            visibleChildren++;
        }
        return intrinsicHeight;
    }

    public int getMinHeight() {
        return getMinHeight(2);
    }

    public int getCollapsedHeight() {
        return getMinHeight(getMaxAllowedVisibleChildren(true));
    }

    private int getMinHeight(int maxAllowedVisibleChildren) {
        int minExpandHeight = this.mNotificationHeaderMargin;
        int visibleChildren = 0;
        boolean firstChild = true;
        int childCount = this.mChildren.size();
        for (int i = 0; i < childCount && visibleChildren < maxAllowedVisibleChildren; i++) {
            if (firstChild) {
                firstChild = false;
            } else {
                minExpandHeight += this.mChildPadding;
            }
            ExpandableNotificationRow child = (ExpandableNotificationRow) this.mChildren.get(i);
            if (child.getSingleLineView() != null) {
                minExpandHeight += child.getSingleLineView().getHeight();
            }
            visibleChildren++;
        }
        return (int) (((float) minExpandHeight) + this.mCollapsedBottompadding);
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        if (this.mOverflowNumber != null) {
            this.mOverflowInvertHelper.setInverted(dark, fade, delay);
        }
        this.mNotificationHeaderWrapper.setDark(dark, fade, delay);
    }

    public void reInflateViews(OnClickListener listener, StatusBarNotification notification) {
        removeView(this.mNotificationHeader);
        this.mNotificationHeader = null;
        recreateNotificationHeader(listener, notification);
        initDimens();
        for (int i = 0; i < this.mDividers.size(); i++) {
            View prevDivider = (View) this.mDividers.get(i);
            int index = indexOfChild(prevDivider);
            removeView(prevDivider);
            View divider = inflateDivider();
            addView(divider, index);
            this.mDividers.set(i, divider);
        }
        removeView(this.mOverflowNumber);
        this.mOverflowNumber = null;
        this.mOverflowInvertHelper = null;
        this.mGroupOverFlowState = null;
        updateGroupOverflow();
    }

    public void setUserLocked(boolean userLocked) {
        this.mUserLocked = userLocked;
        int childCount = this.mChildren.size();
        for (int i = 0; i < childCount; i++) {
            ((ExpandableNotificationRow) this.mChildren.get(i)).setUserLocked(userLocked);
        }
    }

    public void onNotificationUpdated() {
        this.mHybridGroupManager.setOverflowNumberColor(this.mOverflowNumber, this.mNotificationParent.getNotificationColor());
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mGroupOverFlowState != null) {
            this.mGroupOverFlowState.dump(fd, pw, args);
        }
        pw.println("mUserLocked=" + this.mUserLocked + ",  mOverflowNumber=" + this.mOverflowNumber);
    }

    public int getPositionInLinearLayout(View childInGroup) {
        int position = this.mNotificationHeaderMargin + this.mNotificatonTopPadding;
        for (int i = 0; i < this.mChildren.size(); i++) {
            boolean notGone;
            View child = (ExpandableNotificationRow) this.mChildren.get(i);
            if (child.getVisibility() != 8) {
                notGone = true;
            } else {
                notGone = false;
            }
            if (notGone) {
                position += this.mDividerHeight;
            }
            if (child == childInGroup) {
                return position;
            }
            if (notGone) {
                position += child.getIntrinsicHeight();
            }
        }
        return 0;
    }
}
