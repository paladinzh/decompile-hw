package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.Grid.Location;
import android.support.v4.util.CircularIntArray;

class SingleRow extends Grid {
    private Object[] mTmpItem = new Object[1];
    private final Location mTmpLocation = new Location(0);

    SingleRow() {
        setNumRows(1);
    }

    public final Location getLocation(int index) {
        return this.mTmpLocation;
    }

    int getStartIndexForAppend() {
        if (this.mLastVisibleIndex >= 0) {
            return this.mLastVisibleIndex + 1;
        }
        if (this.mStartIndex != -1) {
            return Math.min(this.mStartIndex, this.mProvider.getCount() - 1);
        }
        return 0;
    }

    int getStartIndexForPrepend() {
        if (this.mFirstVisibleIndex >= 0) {
            return this.mFirstVisibleIndex - 1;
        }
        if (this.mStartIndex != -1) {
            return Math.min(this.mStartIndex, this.mProvider.getCount() - 1);
        }
        return this.mProvider.getCount() - 1;
    }

    protected final boolean prependVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkPrependOverLimit(toLimit)) {
            return false;
        }
        boolean filledOne = false;
        for (int index = getStartIndexForPrepend(); index >= 0; index--) {
            int edge;
            int size = this.mProvider.createItem(index, false, this.mTmpItem);
            if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                edge = this.mReversedFlow ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                this.mFirstVisibleIndex = index;
                this.mLastVisibleIndex = index;
            } else {
                if (this.mReversedFlow) {
                    edge = (this.mProvider.getEdge(index + 1) + this.mMargin) + size;
                } else {
                    edge = (this.mProvider.getEdge(index + 1) - this.mMargin) - size;
                }
                this.mFirstVisibleIndex = index;
            }
            this.mProvider.addItem(this.mTmpItem[0], index, size, 0, edge);
            filledOne = true;
            if (oneColumnMode || checkPrependOverLimit(toLimit)) {
                break;
            }
        }
        return filledOne;
    }

    protected final boolean appendVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
            return false;
        }
        boolean filledOne = false;
        for (int index = getStartIndexForAppend(); index < this.mProvider.getCount(); index++) {
            int edge;
            int size = this.mProvider.createItem(index, true, this.mTmpItem);
            if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                edge = this.mReversedFlow ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                this.mFirstVisibleIndex = index;
                this.mLastVisibleIndex = index;
            } else {
                if (this.mReversedFlow) {
                    edge = (this.mProvider.getEdge(index - 1) - this.mProvider.getSize(index - 1)) - this.mMargin;
                } else {
                    edge = (this.mProvider.getEdge(index - 1) + this.mProvider.getSize(index - 1)) + this.mMargin;
                }
                this.mLastVisibleIndex = index;
            }
            this.mProvider.addItem(this.mTmpItem[0], index, size, 0, edge);
            filledOne = true;
            if (oneColumnMode || checkAppendOverLimit(toLimit)) {
                break;
            }
        }
        return filledOne;
    }

    public final CircularIntArray[] getItemPositionsInRows(int startPos, int endPos) {
        this.mTmpItemPositionsInRows[0].clear();
        this.mTmpItemPositionsInRows[0].addLast(startPos);
        this.mTmpItemPositionsInRows[0].addLast(endPos);
        return this.mTmpItemPositionsInRows;
    }

    protected final int findRowMin(boolean findLarge, int indexLimit, int[] indices) {
        if (indices != null) {
            indices[0] = 0;
            indices[1] = indexLimit;
        }
        if (this.mReversedFlow) {
            return this.mProvider.getEdge(indexLimit) - this.mProvider.getSize(indexLimit);
        }
        return this.mProvider.getEdge(indexLimit);
    }

    protected final int findRowMax(boolean findLarge, int indexLimit, int[] indices) {
        if (indices != null) {
            indices[0] = 0;
            indices[1] = indexLimit;
        }
        if (this.mReversedFlow) {
            return this.mProvider.getEdge(indexLimit);
        }
        return this.mProvider.getEdge(indexLimit) + this.mProvider.getSize(indexLimit);
    }
}
