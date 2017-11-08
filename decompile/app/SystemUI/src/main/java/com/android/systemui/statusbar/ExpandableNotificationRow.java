package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.Chronometer;
import android.widget.ImageView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.NotificationColorUtil;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationChildrenContainer;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackScrollState;
import com.android.systemui.statusbar.stack.StackStateAnimator;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ExpandableNotificationRow extends ActivatableNotificationView {
    private static final Property<ExpandableNotificationRow, Float> TRANSLATE_CONTENT = new FloatProperty<ExpandableNotificationRow>("translate") {
        public void setValue(ExpandableNotificationRow object, float value) {
            object.setTranslation(value);
        }

        public Float get(ExpandableNotificationRow object) {
            return Float.valueOf(object.getTranslation());
        }
    };
    private String mAppName;
    private View mChildAfterViewWhenDismissed;
    private NotificationChildrenContainer mChildrenContainer;
    private ViewStub mChildrenContainerStub;
    private boolean mChildrenExpanded;
    private HwCustExpandableNotificationRow mCust = ((HwCustExpandableNotificationRow) HwCustUtils.createObj(HwCustExpandableNotificationRow.class, new Object[0]));
    private boolean mDismissed;
    private Entry mEntry;
    private OnClickListener mExpandClickListener = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i("ExpandableNotificationRow", "click notification: " + ExpandableNotificationRow.this.getStatusBarNotification().getKey());
            boolean nowExpanded;
            if (ExpandableNotificationRow.this.mShowingPublic || !ExpandableNotificationRow.this.mGroupManager.isSummaryOfGroup(ExpandableNotificationRow.this.mStatusBarNotification)) {
                if (v.isAccessibilityFocused()) {
                    ExpandableNotificationRow.this.mPrivateLayout.setFocusOnVisibilityChange();
                }
                ExpandableNotificationRow.this.setUserLocked(false);
                if (ExpandableNotificationRow.this.isPinned()) {
                    nowExpanded = !ExpandableNotificationRow.this.mExpandedWhenPinned;
                    ExpandableNotificationRow.this.mExpandedWhenPinned = nowExpanded;
                } else {
                    nowExpanded = !ExpandableNotificationRow.this.isExpanded();
                    ExpandableNotificationRow.this.setUserExpanded(nowExpanded);
                }
                ExpandableNotificationRow.this.notifyHeightChanged(true);
                ExpandableNotificationRow.this.mOnExpandClickListener.onExpandClicked(ExpandableNotificationRow.this.mEntry, nowExpanded);
                MetricsLogger.action(ExpandableNotificationRow.this.mContext, 407, nowExpanded);
                return;
            }
            ExpandableNotificationRow.this.setUserLocked(false);
            ExpandableNotificationRow.this.mGroupExpansionChanging = true;
            boolean wasExpanded = ExpandableNotificationRow.this.mGroupManager.isGroupExpanded(ExpandableNotificationRow.this.mStatusBarNotification);
            nowExpanded = ExpandableNotificationRow.this.mGroupManager.toggleGroupExpansion(ExpandableNotificationRow.this.mStatusBarNotification);
            ExpandableNotificationRow.this.mOnExpandClickListener.onExpandClicked(ExpandableNotificationRow.this.mEntry, nowExpanded);
            MetricsLogger.action(ExpandableNotificationRow.this.mContext, 408, nowExpanded);
            ExpandableNotificationRow.this.logExpansionEvent(true, wasExpanded);
        }
    };
    private boolean mExpandable;
    private boolean mExpandedWhenPinned;
    private FalsingManager mFalsingManager;
    private boolean mForceUnlocked;
    private boolean mGroupExpansionChanging;
    private NotificationGroupManager mGroupManager;
    private View mGroupParentWhenDismissed;
    private NotificationGuts mGuts;
    private ViewStub mGutsStub;
    private boolean mHasUserChangedExpansion;
    private int mHeadsUpHeight;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHeadsupDisappearRunning;
    private boolean mHideSensitiveForIntrinsicHeight;
    private boolean mIconAnimationRunning;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsHeadsUp;
    private boolean mIsPinned;
    private boolean mIsSummaryWithChildren;
    private boolean mIsSystemChildExpanded;
    private boolean mIsSystemExpanded;
    private boolean mJustClicked;
    private boolean mKeepInParent;
    private boolean mLastChronometerRunning = true;
    private ExpansionLogger mLogger;
    private String mLoggingKey;
    private int mMaxExpandHeight;
    private int mMaxHeadsUpHeight;
    private int mMaxHeadsUpHeightLegacy;
    private int mNotificationColor;
    private int mNotificationMaxHeight;
    private int mNotificationMinHeight;
    private int mNotificationMinHeightLegacy;
    private ExpandableNotificationRow mNotificationParent;
    private OnClickListener mOnClickListener;
    private OnExpandClickListener mOnExpandClickListener;
    private boolean mOnKeyguard;
    private NotificationContentView mPrivateLayout;
    private NotificationContentView mPublicLayout;
    private boolean mRefocusOnDismiss;
    private boolean mRemoved;
    private boolean mSensitive;
    private boolean mSensitiveHiddenInGeneral;
    private NotificationSettingsIconRow mSettingsIconRow;
    private ViewStub mSettingsIconRowStub;
    private boolean mShowingPublic;
    private boolean mShowingPublicInitialized;
    protected StatusBarNotification mStatusBarNotification;
    private Animator mTranslateAnim;
    private ArrayList<View> mTranslateableViews;
    private boolean mUserExpanded;
    private boolean mUserLocked;
    private View mVetoButton;

    public interface ExpansionLogger {
        void logNotificationExpansion(String str, boolean z, boolean z2);
    }

    public interface OnExpandClickListener {
        void onExpandClicked(Entry entry, boolean z);
    }

    public boolean isGroupExpansionChanging() {
        if (isChildInGroup()) {
            return this.mNotificationParent.isGroupExpansionChanging();
        }
        return this.mGroupExpansionChanging;
    }

    public void setGroupExpansionChanging(boolean changing) {
        this.mGroupExpansionChanging = changing;
    }

    public void setActualHeightAnimating(boolean animating) {
        if (this.mPrivateLayout != null) {
            this.mPrivateLayout.setContentHeightAnimating(animating);
        }
    }

    public NotificationContentView getPrivateLayout() {
        return this.mPrivateLayout;
    }

    public NotificationContentView getPublicLayout() {
        return this.mPublicLayout;
    }

    public void setIconAnimationRunning(boolean running) {
        setIconAnimationRunning(running, this.mPublicLayout);
        setIconAnimationRunning(running, this.mPrivateLayout);
        if (this.mIsSummaryWithChildren) {
            setIconAnimationRunningForChild(running, this.mChildrenContainer.getHeaderView());
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                ((ExpandableNotificationRow) notificationChildren.get(i)).setIconAnimationRunning(running);
            }
        }
        this.mIconAnimationRunning = running;
    }

    private void setIconAnimationRunning(boolean running, NotificationContentView layout) {
        if (layout != null) {
            View contractedChild = layout.getContractedChild();
            View expandedChild = layout.getExpandedChild();
            View headsUpChild = layout.getHeadsUpChild();
            setIconAnimationRunningForChild(running, contractedChild);
            setIconAnimationRunningForChild(running, expandedChild);
            setIconAnimationRunningForChild(running, headsUpChild);
        }
    }

    private void setIconAnimationRunningForChild(boolean running, View child) {
        if (child != null) {
            setIconRunning((ImageView) child.findViewById(16908294), running);
            setIconRunning((ImageView) child.findViewById(16908356), running);
        }
    }

    private void setIconRunning(ImageView imageView, boolean running) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                if (running) {
                    animationDrawable.start();
                } else {
                    animationDrawable.stop();
                }
            } else if (drawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animationDrawable2 = (AnimatedVectorDrawable) drawable;
                if (running) {
                    animationDrawable2.start();
                } else {
                    animationDrawable2.stop();
                }
            }
        }
    }

    public void onNotificationUpdated(Entry entry) {
        this.mEntry = entry;
        this.mStatusBarNotification = entry.notification;
        this.mPrivateLayout.onNotificationUpdated(entry);
        this.mPublicLayout.onNotificationUpdated(entry);
        this.mShowingPublicInitialized = false;
        updateNotificationColor();
        updateClearability();
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.recreateNotificationHeader(this.mExpandClickListener, this.mEntry.notification);
            this.mChildrenContainer.onNotificationUpdated();
        }
        if (this.mIconAnimationRunning) {
            setIconAnimationRunning(true);
        }
        if (this.mNotificationParent != null) {
            this.mNotificationParent.updateChildrenHeaderAppearance();
        }
        onChildrenCountChanged();
        this.mPublicLayout.updateExpandButtons(true);
        updateLimits();
    }

    private void updateLimits() {
        updateLimitsForView(this.mPrivateLayout);
        updateLimitsForView(this.mPublicLayout);
    }

    private void updateLimitsForView(NotificationContentView layout) {
        int headsUpheight;
        boolean headsUpCustom = false;
        boolean customView = layout.getContractedChild() != null ? layout.getContractedChild().getId() != 16909230 : false;
        boolean beforeN = this.mEntry.targetSdk < 24;
        int minHeight = (customView && beforeN && !this.mIsSummaryWithChildren) ? this.mNotificationMinHeightLegacy : this.mNotificationMinHeight;
        if (!(layout.getHeadsUpChild() == null || layout.getHeadsUpChild().getId() == 16909230)) {
            headsUpCustom = true;
        }
        if (headsUpCustom && beforeN) {
            headsUpheight = this.mMaxHeadsUpHeightLegacy;
        } else {
            headsUpheight = this.mMaxHeadsUpHeight;
        }
        layout.setHeights(minHeight, headsUpheight, this.mNotificationMaxHeight);
    }

    public StatusBarNotification getStatusBarNotification() {
        return this.mStatusBarNotification;
    }

    public boolean isHeadsUp() {
        return this.mIsHeadsUp;
    }

    public void setHeadsUp(boolean isHeadsUp) {
        int intrinsicBefore = getIntrinsicHeight();
        this.mIsHeadsUp = isHeadsUp;
        this.mPrivateLayout.setHeadsUp(isHeadsUp);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateGroupOverflow();
        }
        if (intrinsicBefore != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
    }

    public void setGroupManager(NotificationGroupManager groupManager) {
        this.mGroupManager = groupManager;
        this.mPrivateLayout.setGroupManager(groupManager);
    }

    public void setRemoteInputController(RemoteInputController r) {
        this.mPrivateLayout.setRemoteInputController(r);
    }

    public void setAppName(String appName) {
        this.mAppName = appName;
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.setAppName(this.mAppName);
        }
    }

    public void addChildNotification(ExpandableNotificationRow row, int childIndex) {
        if (this.mChildrenContainer == null) {
            this.mChildrenContainerStub.inflate();
        }
        this.mChildrenContainer.addNotification(row, childIndex);
        row.setIsChildInGroup(true, this);
    }

    public void afterAddNotification() {
        onChildrenCountChanged();
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.afterAddNotification();
        }
    }

    public void removeChildNotification(ExpandableNotificationRow row) {
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.removeNotification(row);
        }
        onChildrenCountChanged();
        row.setIsChildInGroup(false, null);
    }

    public boolean isChildInGroup() {
        return this.mNotificationParent != null;
    }

    public ExpandableNotificationRow getNotificationParent() {
        return this.mNotificationParent;
    }

    public void setIsChildInGroup(boolean isChildInGroup, ExpandableNotificationRow parent) {
        boolean childInGroup = BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS ? isChildInGroup : false;
        if (!childInGroup) {
            parent = null;
        }
        this.mNotificationParent = parent;
        this.mPrivateLayout.setIsChildInGroup(childInGroup);
        resetBackgroundAlpha();
        updateBackgroundForGroupState();
        updateClickAndFocus();
        if (this.mNotificationParent != null) {
            this.mNotificationParent.updateBackgroundForGroupState();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (SystemUiUtil.allowLogEvent(ev)) {
            HwLog.i("ExpandableNotificationRow", "onInterceptTouchEvent: " + ev + ", " + this.mStatusBarNotification.getKey());
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (SystemUiUtil.allowLogEvent(event)) {
            HwLog.i("ExpandableNotificationRow", "onTouchEvent: " + event + ", " + this.mStatusBarNotification.getKey());
        }
        if (event.getActionMasked() == 0 && isChildInGroup() && !isGroupExpanded()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    protected boolean handleSlideBack() {
        if (this.mSettingsIconRow == null || !this.mSettingsIconRow.isVisible()) {
            return false;
        }
        animateTranslateNotification(0.0f);
        return true;
    }

    protected boolean shouldHideBackground() {
        return super.shouldHideBackground();
    }

    public boolean isSummaryWithChildren() {
        return this.mIsSummaryWithChildren;
    }

    public boolean areChildrenExpanded() {
        return this.mChildrenExpanded;
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        return this.mChildrenContainer == null ? null : this.mChildrenContainer.getNotificationChildren();
    }

    public int getNumberOfNotificationChildren() {
        if (this.mChildrenContainer == null) {
            return 0;
        }
        return this.mChildrenContainer.getNotificationChildren().size();
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> childOrder) {
        return this.mChildrenContainer != null ? this.mChildrenContainer.applyChildOrder(childOrder) : false;
    }

    public void getChildrenStates(StackScrollState resultState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.getState(resultState, resultState.getViewStateForView(this));
        }
    }

    public void applyChildrenState(StackScrollState state) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.applyState(state);
        }
    }

    public void prepareExpansionChanged(StackScrollState state) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.prepareExpansionChanged(state);
        }
    }

    public void startChildAnimation(StackScrollState finalState, StackStateAnimator stateAnimator, long delay, long duration) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.startAnimationToState(finalState, stateAnimator, delay, duration);
        }
    }

    public ExpandableNotificationRow getViewAtPosition(float y) {
        if (!this.mIsSummaryWithChildren || !this.mChildrenExpanded) {
            return this;
        }
        ExpandableNotificationRow view = this.mChildrenContainer.getViewAtPosition(y);
        if (view != null) {
            this = view;
        }
        return this;
    }

    public NotificationGuts getGuts() {
        return this.mGuts;
    }

    public void setPinned(boolean pinned) {
        int intrinsicHeight = getIntrinsicHeight();
        this.mIsPinned = pinned;
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
        if (pinned) {
            setIconAnimationRunning(true);
            this.mExpandedWhenPinned = false;
        } else if (this.mExpandedWhenPinned) {
            setUserExpanded(true);
        }
        setChronometerRunning(this.mLastChronometerRunning);
    }

    public boolean isPinned() {
        return this.mIsPinned;
    }

    public int getPinnedHeadsUpHeight(boolean atLeastMinHeight) {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getIntrinsicHeight();
        }
        if (this.mExpandedWhenPinned) {
            return Math.max(getMaxExpandHeight(), this.mHeadsUpHeight);
        }
        if (atLeastMinHeight) {
            return Math.max(getCollapsedHeight(), this.mHeadsUpHeight);
        }
        return this.mHeadsUpHeight;
    }

    public void setJustClicked(boolean justClicked) {
        this.mJustClicked = justClicked;
    }

    public boolean wasJustClicked() {
        return this.mJustClicked;
    }

    public void setChronometerRunning(boolean running) {
        this.mLastChronometerRunning = running;
        setChronometerRunning(running, this.mPrivateLayout);
        setChronometerRunning(running, this.mPublicLayout);
        if (this.mChildrenContainer != null) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                ((ExpandableNotificationRow) notificationChildren.get(i)).setChronometerRunning(running);
            }
        }
    }

    private void setChronometerRunning(boolean running, NotificationContentView layout) {
        if (layout != null) {
            running = !running ? isPinned() : true;
            View contractedChild = layout.getContractedChild();
            View expandedChild = layout.getExpandedChild();
            View headsUpChild = layout.getHeadsUpChild();
            setChronometerRunningForChild(running, contractedChild);
            setChronometerRunningForChild(running, expandedChild);
            setChronometerRunningForChild(running, headsUpChild);
        }
    }

    private void setChronometerRunningForChild(boolean running, View child) {
        if (child != null) {
            View chronometer = child.findViewById(16909227);
            if (chronometer instanceof Chronometer) {
                ((Chronometer) chronometer).setStarted(running);
            }
        }
    }

    public NotificationHeaderView getNotificationHeader() {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getHeaderView();
        }
        return this.mPrivateLayout.getNotificationHeader();
    }

    private NotificationHeaderView getVisibleNotificationHeader() {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getHeaderView();
        }
        return getShowingLayout().getVisibleNotificationHeader();
    }

    public void setOnExpandClickListener(OnExpandClickListener onExpandClickListener) {
        this.mOnExpandClickListener = onExpandClickListener;
    }

    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        this.mOnClickListener = l;
        updateClickAndFocus();
    }

    private void updateClickAndFocus() {
        boolean isGroupExpanded = isChildInGroup() ? isGroupExpanded() : true;
        boolean z = this.mOnClickListener != null ? isGroupExpanded : false;
        if (isFocusable() != isGroupExpanded) {
            setFocusable(isGroupExpanded);
        }
        if (isClickable() != z) {
            setClickable(z);
        }
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void reInflateViews() {
        initDimens();
        if (this.mIsSummaryWithChildren && this.mChildrenContainer != null) {
            this.mChildrenContainer.reInflateViews(this.mExpandClickListener, this.mEntry.notification);
        }
        if (this.mGuts != null) {
            View oldGuts = this.mGuts;
            int index = indexOfChild(oldGuts);
            removeView(oldGuts);
            this.mGuts = (NotificationGuts) LayoutInflater.from(this.mContext).inflate(R.layout.notification_guts, this, false);
            this.mGuts.setVisibility(oldGuts.getVisibility());
            addView(this.mGuts, index);
        }
        if (this.mSettingsIconRow != null) {
            View oldSettings = this.mSettingsIconRow;
            int settingsIndex = indexOfChild(oldSettings);
            removeView(oldSettings);
            this.mSettingsIconRow = (NotificationSettingsIconRow) LayoutInflater.from(this.mContext).inflate(R.layout.notification_settings_icon_row, this, false);
            this.mSettingsIconRow.setNotificationRowParent(this);
            this.mSettingsIconRow.setAppName(this.mAppName);
            this.mSettingsIconRow.setVisibility(oldSettings.getVisibility());
            addView(this.mSettingsIconRow, settingsIndex);
        }
        this.mPrivateLayout.reInflateViews();
        this.mPublicLayout.reInflateViews();
    }

    public void setContentBackground(int customBackgroundColor, boolean animate, NotificationContentView notificationContentView) {
        if (getShowingLayout() == notificationContentView) {
            setTintColor(customBackgroundColor, animate);
        }
    }

    public void closeRemoteInput() {
        this.mPrivateLayout.closeRemoteInput();
        this.mPublicLayout.closeRemoteInput();
    }

    public void setSingleLineWidthIndention(int indention) {
        this.mPrivateLayout.setSingleLineWidthIndention(indention);
    }

    public int getNotificationColor() {
        return this.mNotificationColor;
    }

    public boolean shouldShowNodetails() {
        if (this.mStatusBarNotification == null) {
            return false;
        }
        Notification notification = this.mStatusBarNotification.getNotification();
        if (notification != null && notification.contentIntent == null) {
            NotificationContentView showingLayout = getShowingLayout();
            if (showingLayout == null) {
                return false;
            }
            NotificationHeaderView headerView = showingLayout.getVisibleNotificationHeader();
            if (headerView == null) {
                return false;
            }
            if (isExpandButtonVisible(headerView)) {
                float x = this.mActiveX - SystemUiUtil.getX(headerView, this);
                float y = this.mActiveY - SystemUiUtil.getY(headerView, this);
                if (!headerView.pointInView(x, y, 0.0f)) {
                    HwLog.i("ExpandableNotificationRow", "shouldShowNodetails: point is not in headerView, return true!");
                    return true;
                } else if (isNotExpandablePoint(headerView, x, y)) {
                    HwLog.i("ExpandableNotificationRow", "shouldShowNodetails: point is not expanable in headerView, return true!");
                    return true;
                }
            }
            HwLog.i("ExpandableNotificationRow", "shouldShowNodetails: expandButton is not visible, return true!");
            return true;
        }
        return false;
    }

    private boolean isExpandButtonVisible(NotificationHeaderView headerView) {
        ImageView expandButton = (ImageView) headerView.findViewById(16909228);
        if (expandButton == null || expandButton.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    private boolean isNotExpandablePoint(NotificationHeaderView headerView, float x, float y) {
        ImageView expandButton = (ImageView) headerView.findViewById(16909228);
        if (expandButton != null) {
            int width = expandButton.getWidth();
            int height = expandButton.getHeight();
            float pointX = expandButton.getX();
            float pointY = expandButton.getY();
            if (y > ((float) (height * 2)) + pointY) {
                return true;
            }
            if (y <= ((float) height) + pointY) {
                return false;
            }
            if (x < pointX - ((float) width) || x > ((float) (width * 2)) + pointX) {
                return true;
            }
        }
        return false;
    }

    private void updateNotificationColor() {
        this.mNotificationColor = NotificationColorUtil.resolveContrastColor(this.mContext, getStatusBarNotification().getNotification().color);
    }

    public HybridNotificationView getSingleLineView() {
        return this.mPrivateLayout.getSingleLineView();
    }

    public boolean isOnKeyguard() {
        return this.mOnKeyguard;
    }

    public void removeAllChildren() {
        ArrayList<ExpandableNotificationRow> clonedList = new ArrayList(this.mChildrenContainer.getNotificationChildren());
        for (int i = 0; i < clonedList.size(); i++) {
            ExpandableNotificationRow row = (ExpandableNotificationRow) clonedList.get(i);
            if (!row.keepInParent()) {
                this.mChildrenContainer.removeNotification(row);
                row.setIsChildInGroup(false, null);
            }
        }
        onChildrenCountChanged();
    }

    public void setForceUnlocked(boolean forceUnlocked) {
        this.mForceUnlocked = forceUnlocked;
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = getNotificationChildren();
            if (notificationChildren != null) {
                for (ExpandableNotificationRow child : notificationChildren) {
                    if (child != null) {
                        child.setForceUnlocked(forceUnlocked);
                    }
                }
            }
        }
    }

    public void setDismissed(boolean dismissed, boolean fromAccessibility) {
        this.mDismissed = dismissed;
        this.mGroupParentWhenDismissed = this.mNotificationParent;
        this.mRefocusOnDismiss = fromAccessibility;
        this.mChildAfterViewWhenDismissed = null;
        if (isChildInGroup()) {
            List<ExpandableNotificationRow> notificationChildren = this.mNotificationParent.getNotificationChildren();
            if (notificationChildren != null) {
                int i = notificationChildren.indexOf(this);
                if (i != -1 && i < notificationChildren.size() - 1) {
                    this.mChildAfterViewWhenDismissed = (View) notificationChildren.get(i + 1);
                }
            }
        }
    }

    public boolean isDismissed() {
        return this.mDismissed;
    }

    public boolean keepInParent() {
        return this.mKeepInParent;
    }

    public void setKeepInParent(boolean keepInParent) {
        this.mKeepInParent = keepInParent;
    }

    public boolean isRemoved() {
        return this.mRemoved;
    }

    public void setRemoved() {
        this.mRemoved = true;
        this.mPrivateLayout.setRemoved();
    }

    public NotificationChildrenContainer getChildrenContainer() {
        return this.mChildrenContainer;
    }

    public void setHeadsupDisappearRunning(boolean running) {
        this.mHeadsupDisappearRunning = running;
        this.mPrivateLayout.setHeadsupDisappearRunning(running);
    }

    public View getChildAfterViewWhenDismissed() {
        return this.mChildAfterViewWhenDismissed;
    }

    public View getGroupParentWhenDismissed() {
        return this.mGroupParentWhenDismissed;
    }

    public ExpandableNotificationRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFalsingManager = FalsingManager.getInstance(context);
        initDimens();
    }

    private void initDimens() {
        this.mNotificationMinHeightLegacy = getFontScaledHeight(R.dimen.notification_min_height_legacy);
        this.mNotificationMinHeight = getFontScaledHeight(R.dimen.notification_min_height);
        this.mNotificationMaxHeight = getFontScaledHeight(R.dimen.notification_max_height);
        this.mMaxHeadsUpHeightLegacy = getFontScaledHeight(R.dimen.notification_max_heads_up_height_legacy);
        this.mMaxHeadsUpHeight = getFontScaledHeight(R.dimen.notification_max_heads_up_height);
        this.mIncreasedPaddingBetweenElements = getResources().getDimensionPixelSize(R.dimen.notification_divider_height_increased);
    }

    private int getFontScaledHeight(int dimenId) {
        int dimensionPixelSize = getResources().getDimensionPixelSize(dimenId);
        return (int) (((float) dimensionPixelSize) * Math.max(1.0f, getResources().getDisplayMetrics().scaledDensity / getResources().getDisplayMetrics().density));
    }

    public void reset() {
        super.reset();
        boolean wasExpanded = isExpanded();
        this.mExpandable = false;
        this.mHasUserChangedExpansion = false;
        this.mUserLocked = false;
        this.mShowingPublic = false;
        this.mSensitive = false;
        this.mShowingPublicInitialized = false;
        this.mIsSystemExpanded = false;
        this.mOnKeyguard = false;
        this.mPublicLayout.reset();
        this.mPrivateLayout.reset();
        resetHeight();
        resetTranslation();
        logExpansionEvent(false, wasExpanded);
    }

    public void resetHeight() {
        this.mMaxExpandHeight = 0;
        this.mHeadsUpHeight = 0;
        onHeightReset();
        requestLayout();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPublicLayout = (NotificationContentView) findViewById(R.id.expandedPublic);
        this.mPublicLayout.setContainingNotification(this);
        this.mPrivateLayout = (NotificationContentView) findViewById(R.id.expanded);
        this.mPrivateLayout.setExpandClickListener(this.mExpandClickListener);
        this.mPrivateLayout.setContainingNotification(this);
        this.mPublicLayout.setExpandClickListener(this.mExpandClickListener);
        this.mSettingsIconRowStub = (ViewStub) findViewById(R.id.settings_icon_row_stub);
        this.mSettingsIconRowStub.setOnInflateListener(new OnInflateListener() {
            public void onInflate(ViewStub stub, View inflated) {
                ExpandableNotificationRow.this.mSettingsIconRow = (NotificationSettingsIconRow) inflated;
                ExpandableNotificationRow.this.mSettingsIconRow.setNotificationRowParent(ExpandableNotificationRow.this);
                ExpandableNotificationRow.this.mSettingsIconRow.setAppName(ExpandableNotificationRow.this.mAppName);
            }
        });
        this.mGutsStub = (ViewStub) findViewById(R.id.notification_guts_stub);
        this.mGutsStub.setOnInflateListener(new OnInflateListener() {
            public void onInflate(ViewStub stub, View inflated) {
                ExpandableNotificationRow.this.mGuts = (NotificationGuts) inflated;
                ExpandableNotificationRow.this.mGuts.setClipTopAmount(ExpandableNotificationRow.this.getClipTopAmount());
                ExpandableNotificationRow.this.mGuts.setActualHeight(ExpandableNotificationRow.this.getActualHeight());
                ExpandableNotificationRow.this.mGutsStub = null;
            }
        });
        this.mChildrenContainerStub = (ViewStub) findViewById(R.id.child_container_stub);
        this.mChildrenContainerStub.setOnInflateListener(new OnInflateListener() {
            public void onInflate(ViewStub stub, View inflated) {
                ExpandableNotificationRow.this.mChildrenContainer = (NotificationChildrenContainer) inflated;
                ExpandableNotificationRow.this.mChildrenContainer.setNotificationParent(ExpandableNotificationRow.this);
                ExpandableNotificationRow.this.mChildrenContainer.onNotificationUpdated();
                ExpandableNotificationRow.this.mTranslateableViews.add(ExpandableNotificationRow.this.mChildrenContainer);
            }
        });
        this.mVetoButton = findViewById(R.id.veto);
        this.mTranslateableViews = new ArrayList();
        for (int i = 0; i < getChildCount(); i++) {
            this.mTranslateableViews.add(getChildAt(i));
        }
        this.mTranslateableViews.remove(this.mVetoButton);
        this.mTranslateableViews.remove(this.mSettingsIconRowStub);
        this.mTranslateableViews.remove(this.mChildrenContainerStub);
        this.mTranslateableViews.remove(this.mGutsStub);
    }

    public void updateChildBackground(boolean isChildInGroup) {
        setChild(isChildInGroup);
        setIsLastChild(false);
        if (!(this.mBackgroundNormal == null || this.mBackgroundDimmed == null)) {
            if (isChildInGroup) {
                this.mBackgroundNormal.setCustomBackground((int) R.drawable.notification_child_material_bg);
                this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_child_material_bg_dim);
            } else {
                this.mBackgroundNormal.setCustomBackground((int) R.drawable.notification_material_bg);
                this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_material_bg_dim);
            }
        }
        updateActionsBackground();
    }

    public void updateLastChildBackground() {
        setChild(false);
        setIsLastChild(true);
        if (!(this.mBackgroundNormal == null || this.mBackgroundDimmed == null)) {
            this.mBackgroundNormal.setCustomBackground((int) R.drawable.notification_last_child_material_bg);
            this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_last_child_material_bg_dim);
        }
        updateActionsBackground();
    }

    private void updateActionsBackground() {
        View privateExpandedView = this.mPrivateLayout.getExpandedChild();
        setActionsBackground(this.mPublicLayout.getExpandedChild(), true);
        setActionsBackground(privateExpandedView, false);
    }

    private void setActionsBackground(View view, boolean isPublic) {
        if (view == null) {
            HwLog.w("ExpandableNotificationRow", "setActionsBackground,null == view," + (isPublic ? "mPublicLayout" : "mPrivateLayout"));
            return;
        }
        View actions = view.findViewById(16909210);
        if (actions == null) {
            HwLog.w("ExpandableNotificationRow", "setActionsBackground,null == actions");
            return;
        }
        if (isChild()) {
            actions.setBackground(getContext().getResources().getDrawable(R.drawable.actions_container_background_bottom_right_angle));
        } else {
            actions.setBackground(getContext().getResources().getDrawable(R.drawable.actions_container_background));
        }
        if (isLastChild()) {
            actions.setBackground(getContext().getResources().getDrawable(R.drawable.actions_container_background));
        }
    }

    public void resetTranslation() {
        if (this.mTranslateableViews != null) {
            for (int i = 0; i < this.mTranslateableViews.size(); i++) {
                ((View) this.mTranslateableViews.get(i)).setTranslationX(0.0f);
            }
        }
        invalidateOutline();
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.resetState();
        }
    }

    public void animateTranslateNotification(float leftTarget) {
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.cancel();
        }
        this.mTranslateAnim = getTranslateViewAnimator(leftTarget, null);
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.start();
        }
    }

    public void setTranslation(float translationX) {
        if (!areGutsExposed()) {
            for (int i = 0; i < this.mTranslateableViews.size(); i++) {
                if (this.mTranslateableViews.get(i) != null) {
                    ((View) this.mTranslateableViews.get(i)).setTranslationX(translationX);
                }
            }
            invalidateOutline();
            if (this.mSettingsIconRow != null) {
                this.mSettingsIconRow.updateVerticalLocation();
                this.mSettingsIconRow.updateSettingsIcons(translationX, (float) getMeasuredWidth());
            }
        }
    }

    public float getTranslation() {
        if (this.mTranslateableViews == null || this.mTranslateableViews.size() <= 0) {
            return 0.0f;
        }
        return ((View) this.mTranslateableViews.get(0)).getTranslationX();
    }

    public Animator getTranslateViewAnimator(final float leftTarget, AnimatorUpdateListener listener) {
        if (this.mTranslateAnim != null) {
            this.mTranslateAnim.cancel();
        }
        if (areGutsExposed()) {
            return null;
        }
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(this, TRANSLATE_CONTENT, new float[]{leftTarget});
        if (listener != null) {
            translateAnim.addUpdateListener(listener);
        }
        translateAnim.addListener(new AnimatorListenerAdapter() {
            boolean cancelled = false;

            public void onAnimationCancel(Animator anim) {
                this.cancelled = true;
            }

            public void onAnimationEnd(Animator anim) {
                if (!this.cancelled && ExpandableNotificationRow.this.mSettingsIconRow != null && leftTarget == 0.0f) {
                    ExpandableNotificationRow.this.mSettingsIconRow.resetState();
                    ExpandableNotificationRow.this.mTranslateAnim = null;
                }
            }
        });
        this.mTranslateAnim = translateAnim;
        return translateAnim;
    }

    public float getSpaceForGear() {
        if (this.mSettingsIconRow != null) {
            return this.mSettingsIconRow.getSpaceForGear();
        }
        return 0.0f;
    }

    public NotificationSettingsIconRow getSettingsRow() {
        if (this.mSettingsIconRow == null) {
            this.mSettingsIconRowStub.inflate();
        }
        return this.mSettingsIconRow;
    }

    public void inflateGuts() {
        if (this.mGuts == null) {
            this.mGutsStub.inflate();
        }
        updateGutsBackground();
    }

    private void updateGutsBackground() {
        if (this.mGuts == null) {
            HwLog.w("ExpandableNotificationRow", "updateGutsBackground,null == mGuts");
            return;
        }
        View moreView = this.mGuts.findViewById(R.id.more_settings_done);
        if (moreView == null) {
            HwLog.w("ExpandableNotificationRow", "updateGutsBackground,null == moreView");
            return;
        }
        if (isChild()) {
            this.mGuts.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_background_right_angle));
            moreView.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_bottom_background_right_angle));
        } else {
            this.mGuts.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_background));
            moreView.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_bottom_background_bottom_radious));
        }
        if (isLastChild()) {
            this.mGuts.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_background_bottom_radious));
            moreView.setBackground(getContext().getResources().getDrawable(R.drawable.notification_guts_bottom_background_bottom_radious));
        }
    }

    private void updateChildrenVisibility() {
        int i;
        int i2 = 0;
        NotificationContentView notificationContentView = this.mPrivateLayout;
        if (this.mShowingPublic || this.mIsSummaryWithChildren) {
            i = 4;
        } else {
            i = 0;
        }
        notificationContentView.setVisibility(i);
        if (this.mChildrenContainer != null) {
            NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
            if (this.mShowingPublic || !this.mIsSummaryWithChildren) {
                i = 4;
            } else {
                i = 0;
            }
            notificationChildrenContainer.setVisibility(i);
            NotificationChildrenContainer notificationChildrenContainer2 = this.mChildrenContainer;
            if (this.mShowingPublic || !this.mIsSummaryWithChildren) {
                i2 = 4;
            }
            notificationChildrenContainer2.updateHeaderVisibility(i2);
        }
        updateLimits();
    }

    public boolean onRequestSendAccessibilityEventInternal(View child, AccessibilityEvent event) {
        if (!super.onRequestSendAccessibilityEventInternal(child, event)) {
            return false;
        }
        AccessibilityEvent record = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(record);
        dispatchPopulateAccessibilityEvent(record);
        event.appendRecord(record);
        return true;
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        super.setDark(dark, fade, delay);
        NotificationContentView showing = getShowingLayout();
        if (showing != null) {
            showing.setDark(dark, fade, delay);
        }
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setDark(dark, fade, delay);
        }
    }

    public boolean isExpandable() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return this.mExpandable;
        }
        return !this.mChildrenExpanded;
    }

    public void setExpandable(boolean expandable) {
        this.mExpandable = expandable;
        this.mPrivateLayout.updateExpandButtons(isExpandable());
    }

    public void setClipToActualHeight(boolean clipToActualHeight) {
        boolean z;
        boolean z2 = true;
        if (clipToActualHeight) {
            z = true;
        } else {
            z = isUserLocked();
        }
        super.setClipToActualHeight(z);
        NotificationContentView showingLayout = getShowingLayout();
        if (!clipToActualHeight) {
            z2 = isUserLocked();
        }
        showingLayout.setClipToActualHeight(z2);
    }

    public boolean hasUserChangedExpansion() {
        return this.mHasUserChangedExpansion;
    }

    public boolean isUserExpanded() {
        return this.mUserExpanded;
    }

    public void setUserExpanded(boolean userExpanded) {
        setUserExpanded(userExpanded, false);
    }

    public void setUserExpanded(boolean userExpanded, boolean allowChildExpansion) {
        HwLog.i("ExpandableNotificationRow", "setUserExpanded: " + userExpanded + ", " + allowChildExpansion);
        this.mFalsingManager.setNotificationExpanded();
        boolean wasExpanded;
        if (this.mIsSummaryWithChildren && !this.mShowingPublic && allowChildExpansion) {
            wasExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
            this.mGroupManager.setGroupExpanded(this.mStatusBarNotification, userExpanded);
            logExpansionEvent(true, wasExpanded);
        } else if (!userExpanded || this.mExpandable) {
            wasExpanded = isExpanded();
            this.mHasUserChangedExpansion = true;
            this.mUserExpanded = userExpanded;
            logExpansionEvent(true, wasExpanded);
        }
    }

    public void resetUserExpansion() {
        this.mHasUserChangedExpansion = false;
        this.mUserExpanded = false;
    }

    public boolean isUserLocked() {
        return this.mUserLocked && !this.mForceUnlocked;
    }

    public void setUserLocked(boolean userLocked) {
        this.mUserLocked = userLocked;
        this.mPrivateLayout.setUserExpanding(userLocked);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setUserLocked(userLocked);
            if (userLocked || !(userLocked || isGroupExpanded())) {
                updateBackgroundForGroupState();
            }
        }
    }

    public boolean isSystemExpanded() {
        return this.mIsSystemExpanded;
    }

    public void setSystemExpanded(boolean expand) {
        if (expand != this.mIsSystemExpanded) {
            boolean wasExpanded = isExpanded();
            this.mIsSystemExpanded = expand;
            notifyHeightChanged(false);
            logExpansionEvent(false, wasExpanded);
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.updateGroupOverflow();
            }
        }
    }

    public void setOnKeyguard(boolean onKeyguard) {
        if (onKeyguard != this.mOnKeyguard) {
            boolean wasExpanded = isExpanded();
            this.mOnKeyguard = onKeyguard;
            logExpansionEvent(false, wasExpanded);
            if (wasExpanded != isExpanded()) {
                if (this.mIsSummaryWithChildren) {
                    this.mChildrenContainer.updateGroupOverflow();
                }
                notifyHeightChanged(false);
            }
            if (!onKeyguard) {
                setUserLocked(false);
            }
        }
    }

    public boolean isClearable() {
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                if (!((ExpandableNotificationRow) notificationChildren.get(i)).isClearable()) {
                    return false;
                }
            }
        }
        if (this.mCust == null || !this.mCust.isCustomUnClearable(this.mStatusBarNotification, this.mContext)) {
            return HwExpandableNotificationRowHelper.isClearable(this.mStatusBarNotification);
        }
        return false;
    }

    public int getIntrinsicHeight() {
        if (isUserLocked()) {
            return getActualHeight();
        }
        if (this.mGuts != null && this.mGuts.areGutsExposed()) {
            return this.mGuts.getHeight();
        }
        if (isChildInGroup() && !isGroupExpanded()) {
            return this.mPrivateLayout.getMinHeight();
        }
        if (this.mSensitive && this.mHideSensitiveForIntrinsicHeight) {
            return getMinHeight();
        }
        if (this.mIsSummaryWithChildren && !this.mOnKeyguard) {
            return this.mChildrenContainer.getIntrinsicHeight();
        }
        if (this.mIsHeadsUp || this.mHeadsupDisappearRunning) {
            if (isPinned() || this.mHeadsupDisappearRunning) {
                return getPinnedHeadsUpHeight(true);
            }
            if (isExpanded()) {
                return Math.max(getMaxExpandHeight(), this.mHeadsUpHeight);
            }
            return Math.max(getCollapsedHeight(), this.mHeadsUpHeight);
        } else if (isExpanded()) {
            return getMaxExpandHeight();
        } else {
            return getCollapsedHeight();
        }
    }

    public boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    private void onChildrenCountChanged() {
        boolean z = (!BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS || this.mChildrenContainer == null) ? false : this.mChildrenContainer.getNotificationChildCount() > 0;
        this.mIsSummaryWithChildren = z;
        if (this.mIsSummaryWithChildren && this.mChildrenContainer.getHeaderView() == null) {
            this.mChildrenContainer.recreateNotificationHeader(this.mExpandClickListener, this.mEntry.notification);
        }
        getShowingLayout().updateBackgroundColor(false);
        this.mPrivateLayout.updateExpandButtons(isExpandable());
        updateChildrenHeaderAppearance();
        updateChildrenVisibility();
    }

    public void updateChildrenHeaderAppearance() {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateChildrenHeaderAppearance();
        }
    }

    public boolean isExpanded() {
        return isExpanded(false);
    }

    public boolean isExpanded(boolean allowOnKeyguard) {
        if (this.mOnKeyguard && !allowOnKeyguard) {
            return false;
        }
        if (hasUserChangedExpansion() || (!isSystemExpanded() && !isSystemChildExpanded())) {
            return isUserExpanded();
        }
        return true;
    }

    private boolean isSystemChildExpanded() {
        return this.mIsSystemChildExpanded;
    }

    public void setSystemChildExpanded(boolean expanded) {
        this.mIsSystemChildExpanded = expanded;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateMaxHeights();
        if (this.mSettingsIconRow != null) {
            this.mSettingsIconRow.updateVerticalLocation();
        }
    }

    private void updateMaxHeights() {
        int intrinsicBefore = getIntrinsicHeight();
        View expandedChild = this.mPrivateLayout.getExpandedChild();
        if (expandedChild == null) {
            expandedChild = this.mPrivateLayout.getContractedChild();
        }
        if (expandedChild != null) {
            this.mMaxExpandHeight = expandedChild.getHeight();
        } else {
            HwLog.w("ExpandableNotificationRow", "updateMaxHeights::getContractedChild is null!");
        }
        View headsUpChild = this.mPrivateLayout.getHeadsUpChild();
        if (headsUpChild == null) {
            headsUpChild = this.mPrivateLayout.getContractedChild();
        }
        if (headsUpChild != null) {
            this.mHeadsUpHeight = headsUpChild.getHeight();
        } else {
            HwLog.w("ExpandableNotificationRow", "updateMaxHeights::getContractedChild headsUpChild is null!");
        }
        if (intrinsicBefore != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
    }

    public void notifyHeightChanged(boolean needsAnimation) {
        super.notifyHeightChanged(needsAnimation);
        getShowingLayout().requestSelectLayout(!needsAnimation ? isUserLocked() : true);
    }

    public void setSensitive(boolean sensitive, boolean hideSensitive) {
        this.mSensitive = sensitive;
        this.mSensitiveHiddenInGeneral = hideSensitive;
    }

    public void setHideSensitiveForIntrinsicHeight(boolean hideSensitive) {
        this.mHideSensitiveForIntrinsicHeight = hideSensitive;
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                ((ExpandableNotificationRow) notificationChildren.get(i)).setHideSensitiveForIntrinsicHeight(hideSensitive);
            }
        }
    }

    public void setHideSensitive(boolean hideSensitive, boolean animated, long delay, long duration) {
        int i = 0;
        boolean oldShowingPublic = this.mShowingPublic;
        if (!this.mSensitive) {
            hideSensitive = false;
        }
        this.mShowingPublic = hideSensitive;
        if ((!this.mShowingPublicInitialized || this.mShowingPublic != oldShowingPublic) && this.mPublicLayout.getChildCount() != 0) {
            if (animated) {
                animateShowingPublic(delay, duration);
            } else {
                this.mPublicLayout.animate().cancel();
                this.mPrivateLayout.animate().cancel();
                if (this.mChildrenContainer != null) {
                    this.mChildrenContainer.animate().cancel();
                    this.mChildrenContainer.setAlpha(1.0f);
                }
                this.mPublicLayout.setAlpha(1.0f);
                this.mPrivateLayout.setAlpha(1.0f);
                NotificationContentView notificationContentView = this.mPublicLayout;
                if (!this.mShowingPublic) {
                    i = 4;
                }
                notificationContentView.setVisibility(i);
                updateChildrenVisibility();
            }
            getShowingLayout().updateBackgroundColor(animated);
            this.mPrivateLayout.updateExpandButtons(isExpandable());
            updateClearability();
            this.mShowingPublicInitialized = true;
        }
    }

    private void animateShowingPublic(long delay, long duration) {
        View[] privateViews = this.mIsSummaryWithChildren ? new View[]{this.mChildrenContainer} : new View[]{this.mPrivateLayout};
        View[] publicViews = new View[]{this.mPublicLayout};
        View[] hiddenChildren = this.mShowingPublic ? privateViews : publicViews;
        View[] shownChildren = this.mShowingPublic ? publicViews : privateViews;
        for (final View hiddenView : hiddenChildren) {
            hiddenView.setVisibility(0);
            hiddenView.animate().cancel();
            hiddenView.animate().alpha(0.0f).setStartDelay(delay).setDuration(duration).withEndAction(new Runnable() {
                public void run() {
                    hiddenView.setVisibility(4);
                }
            });
        }
        for (View showView : shownChildren) {
            showView.setVisibility(0);
            showView.setAlpha(0.0f);
            showView.animate().cancel();
            showView.animate().alpha(1.0f).setStartDelay(delay).setDuration(duration);
        }
    }

    public boolean mustStayOnScreen() {
        return this.mIsHeadsUp;
    }

    private void updateClearability() {
        this.mVetoButton.setVisibility(canViewBeDismissed() ? 0 : 8);
    }

    private boolean canViewBeDismissed() {
        return isClearable() && !(this.mShowingPublic && this.mSensitiveHiddenInGeneral);
    }

    public void makeActionsVisibile() {
        setUserExpanded(true, true);
        if (isChildInGroup()) {
            this.mGroupManager.setGroupExpanded(this.mStatusBarNotification, true);
        }
        notifyHeightChanged(false);
    }

    public void setChildrenExpanded(boolean expanded, boolean animate) {
        this.mChildrenExpanded = expanded;
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.setChildrenExpanded(expanded);
        }
        updateBackgroundForGroupState();
        updateClickAndFocus();
    }

    public int getMaxExpandHeight() {
        return this.mMaxExpandHeight;
    }

    public boolean areGutsExposed() {
        return this.mGuts != null ? this.mGuts.areGutsExposed() : false;
    }

    public boolean isContentExpandable() {
        return getShowingLayout().isContentExpandable();
    }

    protected View getContentView() {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer;
        }
        return getShowingLayout();
    }

    public int getExtraBottomPadding() {
        if (this.mIsSummaryWithChildren && isGroupExpanded()) {
            return this.mIncreasedPaddingBetweenElements;
        }
        return 0;
    }

    public void setActualHeight(int height, boolean notifyListeners) {
        super.setActualHeight(height, notifyListeners);
        if (this.mGuts == null || !this.mGuts.areGutsExposed()) {
            int contentHeight = Math.max(getMinHeight(), height);
            this.mPrivateLayout.setContentHeight(contentHeight);
            this.mPublicLayout.setContentHeight(contentHeight);
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.setActualHeight(height);
            }
            if (this.mGuts != null) {
                this.mGuts.setActualHeight(height);
            }
            return;
        }
        this.mGuts.setActualHeight(height);
    }

    public int getMaxContentHeight() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getShowingLayout().getMaxHeight();
        }
        return this.mChildrenContainer.getMaxContentHeight();
    }

    public int getMinHeight() {
        if (this.mIsHeadsUp && this.mHeadsUpManager.isTrackingHeadsUp()) {
            return getPinnedHeadsUpHeight(false);
        }
        if (this.mIsSummaryWithChildren && !isGroupExpanded() && !this.mShowingPublic) {
            return this.mChildrenContainer.getMinHeight();
        }
        if (this.mIsHeadsUp) {
            return this.mHeadsUpHeight;
        }
        return getShowingLayout().getMinHeight();
    }

    public int getCollapsedHeight() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getMinHeight();
        }
        return this.mChildrenContainer.getCollapsedHeight();
    }

    public void setClipTopAmount(int clipTopAmount) {
        super.setClipTopAmount(clipTopAmount);
        this.mPrivateLayout.setClipTopAmount(clipTopAmount);
        this.mPublicLayout.setClipTopAmount(clipTopAmount);
        if (this.mGuts != null) {
            this.mGuts.setClipTopAmount(clipTopAmount);
        }
    }

    public NotificationContentView getShowingLayout() {
        return this.mShowingPublic ? this.mPublicLayout : this.mPrivateLayout;
    }

    public void setShowingLegacyBackground(boolean showing) {
        super.setShowingLegacyBackground(showing);
        this.mPrivateLayout.setShowingLegacyBackground(showing);
        this.mPublicLayout.setShowingLegacyBackground(showing);
    }

    protected void updateBackgroundTint() {
        super.updateBackgroundTint();
        updateBackgroundForGroupState();
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                ((ExpandableNotificationRow) notificationChildren.get(i)).updateBackgroundForGroupState();
            }
        }
    }

    public void onFinishedExpansionChange() {
        this.mGroupExpansionChanging = false;
        updateBackgroundForGroupState();
    }

    public void updateBackgroundForGroupState() {
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> children = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < children.size(); i++) {
                ((ExpandableNotificationRow) children.get(i)).updateBackgroundForGroupState();
            }
        } else if (isChildInGroup()) {
            int childColor = getShowingLayout().getBackgroundColorForExpansionState();
            if (!isGroupExpanded()) {
                if (this.mNotificationParent.isGroupExpansionChanging() || this.mNotificationParent.isUserLocked()) {
                    if (childColor != 0) {
                    }
                }
            }
        }
        updateOutline();
        updateBackground();
    }

    public int getPositionOfChild(ExpandableNotificationRow childRow) {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getPositionInLinearLayout(childRow);
        }
        return 0;
    }

    public void setExpansionLogger(ExpansionLogger logger, String key) {
        this.mLogger = logger;
        this.mLoggingKey = key;
    }

    public void onExpandedByGesture(boolean userExpanded) {
        int event = 409;
        if (this.mGroupManager.isSummaryOfGroup(getStatusBarNotification())) {
            event = 410;
        }
        MetricsLogger.action(this.mContext, event, userExpanded);
    }

    public float getIncreasedPaddingAmount() {
        if (this.mIsSummaryWithChildren) {
            if (isGroupExpanded()) {
                return 1.0f;
            }
            if (isUserLocked()) {
                return this.mChildrenContainer.getGroupExpandFraction();
            }
        }
        return 0.0f;
    }

    protected boolean disallowSingleClick(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        NotificationHeaderView header = getVisibleNotificationHeader();
        if (header != null) {
            return header.isInTouchRect(x - getTranslation(), y);
        }
        return super.disallowSingleClick(event);
    }

    private void logExpansionEvent(boolean userAction, boolean wasExpanded) {
        boolean nowExpanded = isExpanded();
        if (this.mIsSummaryWithChildren) {
            nowExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
        }
        if (wasExpanded != nowExpanded && this.mLogger != null) {
            this.mLogger.logNotificationExpansion(this.mLoggingKey, userAction, nowExpanded);
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (canViewBeDismissed()) {
            info.addAction(AccessibilityAction.ACTION_DISMISS);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case 1048576:
                NotificationStackScrollLayout.performDismiss(this, this.mGroupManager, true);
                return true;
            default:
                return false;
        }
    }

    public boolean shouldRefocusOnDismiss() {
        return !this.mRefocusOnDismiss ? isAccessibilityFocused() : true;
    }

    public int getNotificationMinHeight() {
        return this.mNotificationMinHeight;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        if (this.mChildrenContainer != null) {
            this.mChildrenContainer.dump(fd, pw, args);
        }
    }
}
