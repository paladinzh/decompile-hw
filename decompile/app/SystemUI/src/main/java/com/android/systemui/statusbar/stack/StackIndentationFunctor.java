package com.android.systemui.statusbar.stack;

public abstract class StackIndentationFunctor {
    protected int mDistanceToPeekStart;
    protected int mMaxItemsInStack;
    protected int mPeekSize;
    protected boolean mStackStartsAtPeek;
    protected int mTotalTransitionDistance;

    public abstract float getValue(float f);

    StackIndentationFunctor(int maxItemsInStack, int peekSize, int distanceToPeekStart) {
        boolean z = false;
        this.mDistanceToPeekStart = distanceToPeekStart;
        if (this.mDistanceToPeekStart == 0) {
            z = true;
        }
        this.mStackStartsAtPeek = z;
        this.mMaxItemsInStack = maxItemsInStack;
        this.mPeekSize = peekSize;
        updateTotalTransitionDistance();
    }

    private void updateTotalTransitionDistance() {
        this.mTotalTransitionDistance = this.mDistanceToPeekStart + this.mPeekSize;
    }
}
