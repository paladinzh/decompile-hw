package com.android.systemui.statusbar.stack;

import android.view.View;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class AmbientState {
    private ActivatableNotificationView mActivatedChild;
    private boolean mDark;
    private boolean mDimmed;
    private boolean mDismissAllInProgress;
    private ArrayList<View> mDraggedViews = new ArrayList();
    private HeadsUpManager mHeadsUpManager;
    private boolean mHideSensitive;
    private int mLayoutHeight;
    private float mMaxHeadsUpTranslation;
    private float mOverScrollBottomAmount;
    private float mOverScrollTopAmount;
    private boolean mOverlap;
    private int mScrollY;
    private boolean mShadeExpanded;
    private int mSpeedBumpIndex = -1;
    private float mStackTranslation;
    private int mTopPadding;

    public int getScrollY() {
        return this.mScrollY;
    }

    public void setScrollY(int scrollY) {
        this.mScrollY = scrollY;
    }

    public void onBeginDrag(View view) {
        this.mDraggedViews.add(view);
    }

    public void onDragFinished(View view) {
        this.mDraggedViews.remove(view);
    }

    public ArrayList<View> getDraggedViews() {
        return this.mDraggedViews;
    }

    public void setDimmed(boolean dimmed) {
        this.mDimmed = dimmed;
    }

    public void setDark(boolean dark) {
        this.mDark = dark;
    }

    public void setHideSensitive(boolean hideSensitive) {
        this.mHideSensitive = hideSensitive;
    }

    public void setActivatedChild(ActivatableNotificationView activatedChild) {
        this.mActivatedChild = activatedChild;
    }

    public boolean isDimmed() {
        return this.mDimmed;
    }

    public boolean isDark() {
        return this.mDark;
    }

    public boolean isHideSensitive() {
        return this.mHideSensitive;
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mActivatedChild;
    }

    public void setOverScrollAmount(float amount, boolean onTop) {
        if (onTop) {
            this.mOverScrollTopAmount = amount;
        } else {
            this.mOverScrollBottomAmount = amount;
        }
    }

    public float getOverScrollAmount(boolean top) {
        return top ? this.mOverScrollTopAmount : this.mOverScrollBottomAmount;
    }

    public int getSpeedBumpIndex() {
        return this.mSpeedBumpIndex;
    }

    public void setSpeedBumpIndex(int speedBumpIndex) {
        this.mSpeedBumpIndex = speedBumpIndex;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    public void setStackTranslation(float stackTranslation) {
        this.mStackTranslation = stackTranslation;
    }

    public void setLayoutHeight(int layoutHeight) {
        this.mLayoutHeight = layoutHeight;
    }

    public float getTopPadding() {
        return (float) this.mTopPadding;
    }

    public void setTopPadding(int topPadding) {
        this.mTopPadding = topPadding;
    }

    public int getInnerHeight() {
        return this.mLayoutHeight - this.mTopPadding;
    }

    public boolean isShadeExpanded() {
        return this.mShadeExpanded;
    }

    public void setShadeExpanded(boolean shadeExpanded) {
        this.mShadeExpanded = shadeExpanded;
    }

    public void setMaxHeadsUpTranslation(float maxHeadsUpTranslation) {
        this.mMaxHeadsUpTranslation = maxHeadsUpTranslation;
    }

    public float getMaxHeadsUpTranslation() {
        return this.mMaxHeadsUpTranslation;
    }

    public void setDismissAllInProgress(boolean dismissAllInProgress) {
        this.mDismissAllInProgress = dismissAllInProgress;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("AmbientState:");
        pw.print(" mScrollY=" + this.mScrollY);
        pw.print(" mOverScrollTopAmount=" + this.mOverScrollTopAmount);
        pw.print(" mOverScrollBottomAmount=" + this.mOverScrollBottomAmount);
        pw.print(" mSpeedBumpIndex=" + this.mSpeedBumpIndex);
        pw.print(" mStackTranslation=" + this.mStackTranslation);
        pw.print(" mLayoutHeight=" + this.mLayoutHeight);
        pw.print(" mTopPadding=" + this.mTopPadding);
        pw.print(" mShadeExpanded=" + this.mShadeExpanded);
        pw.print(" mMaxHeadsUpTranslation=" + this.mMaxHeadsUpTranslation);
        pw.print(" mDismissAllInProgress=" + this.mDismissAllInProgress);
        pw.print(" mDimmed=" + this.mDimmed);
        pw.println();
    }

    public boolean isOverlap() {
        return this.mOverlap;
    }

    public void setOverlap(boolean overlap) {
        this.mOverlap = overlap;
    }
}
