package android.support.v17.leanback.widget;

import android.support.v4.util.CircularArray;
import android.support.v4.util.CircularIntArray;

abstract class StaggeredGrid extends Grid {
    protected int mFirstIndex = -1;
    protected CircularArray<Location> mLocations = new CircularArray(64);
    protected Object mPendingItem;
    protected int mPendingItemSize;
    private Object[] mTmpItem = new Object[1];

    public static class Location extends android.support.v17.leanback.widget.Grid.Location {
        public int offset;
        public int size;

        public Location(int row, int offset, int size) {
            super(row);
            this.offset = offset;
            this.size = size;
        }
    }

    protected abstract boolean appendVisibleItemsWithoutCache(int i, boolean z);

    protected abstract boolean prependVisibleItemsWithoutCache(int i, boolean z);

    StaggeredGrid() {
    }

    public final int getFirstIndex() {
        return this.mFirstIndex;
    }

    public final int getLastIndex() {
        return (this.mFirstIndex + this.mLocations.size()) - 1;
    }

    public final Location getLocation(int index) {
        if (this.mLocations.size() == 0) {
            return null;
        }
        return (Location) this.mLocations.get(index - this.mFirstIndex);
    }

    protected final boolean prependVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkPrependOverLimit(toLimit)) {
            return false;
        }
        try {
            if (prependVisbleItemsWithCache(toLimit, oneColumnMode)) {
                return true;
            }
            boolean prependVisibleItemsWithoutCache = prependVisibleItemsWithoutCache(toLimit, oneColumnMode);
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
            return prependVisibleItemsWithoutCache;
        } finally {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
        }
    }

    protected final boolean prependVisbleItemsWithCache(int toLimit, boolean oneColumnMode) {
        if (this.mLocations.size() == 0) {
            return false;
        }
        int edge;
        int offset;
        int itemIndex;
        int count = this.mProvider.getCount();
        int firstIndex = getFirstIndex();
        if (this.mFirstVisibleIndex >= 0) {
            edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            offset = getLocation(this.mFirstVisibleIndex).offset;
            itemIndex = this.mFirstVisibleIndex - 1;
        } else {
            edge = Integer.MAX_VALUE;
            offset = 0;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (itemIndex > getLastIndex() || itemIndex < getFirstIndex() - 1) {
                this.mLocations.clear();
                return false;
            } else if (itemIndex < getFirstIndex()) {
                return false;
            }
        }
        while (itemIndex >= this.mFirstIndex) {
            Location loc = getLocation(itemIndex);
            int rowIndex = loc.row;
            int size = this.mProvider.createItem(itemIndex, false, this.mTmpItem);
            if (size != loc.size) {
                this.mLocations.removeFromStart((itemIndex + 1) - this.mFirstIndex);
                this.mFirstIndex = this.mFirstVisibleIndex;
                this.mPendingItem = this.mTmpItem[0];
                this.mPendingItemSize = size;
                return false;
            }
            this.mFirstVisibleIndex = itemIndex;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = itemIndex;
            }
            this.mProvider.addItem(this.mTmpItem[0], itemIndex, size, rowIndex, edge - offset);
            if (!oneColumnMode && checkPrependOverLimit(toLimit)) {
                return true;
            }
            edge = this.mProvider.getEdge(itemIndex);
            offset = loc.offset;
            if (rowIndex == 0 && oneColumnMode) {
                return true;
            }
            itemIndex--;
        }
        return false;
    }

    private int calculateOffsetAfterLastItem(int row) {
        int offset;
        int cachedIndex = getLastIndex();
        boolean foundCachedItemInSameRow = false;
        while (cachedIndex >= this.mFirstIndex) {
            if (getLocation(cachedIndex).row == row) {
                foundCachedItemInSameRow = true;
                break;
            }
            cachedIndex--;
        }
        if (!foundCachedItemInSameRow) {
            cachedIndex = getLastIndex();
        }
        if (isReversedFlow()) {
            offset = (-getLocation(cachedIndex).size) - this.mMargin;
        } else {
            offset = getLocation(cachedIndex).size + this.mMargin;
        }
        for (int i = cachedIndex + 1; i <= getLastIndex(); i++) {
            offset -= getLocation(i).offset;
        }
        return offset;
    }

    protected final int prependVisibleItemToRow(int itemIndex, int rowIndex, int edge) {
        if (this.mFirstVisibleIndex < 0 || (this.mFirstVisibleIndex == getFirstIndex() && this.mFirstVisibleIndex == itemIndex + 1)) {
            Object item;
            Location location = this.mFirstIndex >= 0 ? getLocation(this.mFirstIndex) : null;
            int oldFirstEdge = this.mProvider.getEdge(this.mFirstIndex);
            Location loc = new Location(rowIndex, 0, 0);
            this.mLocations.addFirst(loc);
            if (this.mPendingItem != null) {
                loc.size = this.mPendingItemSize;
                item = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                loc.size = this.mProvider.createItem(itemIndex, false, this.mTmpItem);
                item = this.mTmpItem[0];
            }
            this.mFirstVisibleIndex = itemIndex;
            this.mFirstIndex = itemIndex;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = itemIndex;
            }
            int thisEdge = !this.mReversedFlow ? edge - loc.size : edge + loc.size;
            if (location != null) {
                location.offset = oldFirstEdge - thisEdge;
            }
            this.mProvider.addItem(item, itemIndex, loc.size, rowIndex, thisEdge);
            return loc.size;
        }
        throw new IllegalStateException();
    }

    protected final boolean appendVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
            return false;
        }
        try {
            if (appendVisbleItemsWithCache(toLimit, oneColumnMode)) {
                return true;
            }
            boolean appendVisibleItemsWithoutCache = appendVisibleItemsWithoutCache(toLimit, oneColumnMode);
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
            return appendVisibleItemsWithoutCache;
        } finally {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
        }
    }

    protected final boolean appendVisbleItemsWithCache(int toLimit, boolean oneColumnMode) {
        if (this.mLocations.size() == 0) {
            return false;
        }
        int itemIndex;
        int edge;
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex >= 0) {
            itemIndex = this.mLastVisibleIndex + 1;
            edge = this.mProvider.getEdge(this.mLastVisibleIndex);
        } else {
            edge = Integer.MAX_VALUE;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (itemIndex > getLastIndex() + 1 || itemIndex < getFirstIndex()) {
                this.mLocations.clear();
                return false;
            } else if (itemIndex > getLastIndex()) {
                return false;
            }
        }
        int lastIndex = getLastIndex();
        while (itemIndex < count && itemIndex <= lastIndex) {
            Location loc = getLocation(itemIndex);
            if (edge != Integer.MAX_VALUE) {
                edge += loc.offset;
            }
            int rowIndex = loc.row;
            int size = this.mProvider.createItem(itemIndex, true, this.mTmpItem);
            if (size != loc.size) {
                loc.size = size;
                this.mLocations.removeFromEnd(lastIndex - itemIndex);
                lastIndex = itemIndex;
            }
            this.mLastVisibleIndex = itemIndex;
            if (this.mFirstVisibleIndex < 0) {
                this.mFirstVisibleIndex = itemIndex;
            }
            this.mProvider.addItem(this.mTmpItem[0], itemIndex, size, rowIndex, edge);
            if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
                return true;
            }
            if (edge == Integer.MAX_VALUE) {
                edge = this.mProvider.getEdge(itemIndex);
            }
            if (rowIndex == this.mNumRows - 1 && oneColumnMode) {
                return true;
            }
            itemIndex++;
        }
        return false;
    }

    protected final int appendVisibleItemToRow(int itemIndex, int rowIndex, int location) {
        if (this.mLastVisibleIndex < 0 || (this.mLastVisibleIndex == getLastIndex() && this.mLastVisibleIndex == itemIndex - 1)) {
            int offset;
            Object item;
            if (this.mLastVisibleIndex >= 0) {
                offset = location - this.mProvider.getEdge(this.mLastVisibleIndex);
            } else if (this.mLocations.size() <= 0 || itemIndex != getLastIndex() + 1) {
                offset = 0;
            } else {
                offset = calculateOffsetAfterLastItem(rowIndex);
            }
            Location loc = new Location(rowIndex, offset, 0);
            this.mLocations.addLast(loc);
            if (this.mPendingItem != null) {
                loc.size = this.mPendingItemSize;
                item = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                loc.size = this.mProvider.createItem(itemIndex, true, this.mTmpItem);
                item = this.mTmpItem[0];
            }
            if (this.mLocations.size() == 1) {
                this.mLastVisibleIndex = itemIndex;
                this.mFirstVisibleIndex = itemIndex;
                this.mFirstIndex = itemIndex;
            } else if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = itemIndex;
                this.mFirstVisibleIndex = itemIndex;
            } else {
                this.mLastVisibleIndex++;
            }
            this.mProvider.addItem(item, itemIndex, loc.size, rowIndex, location);
            return loc.size;
        }
        throw new IllegalStateException();
    }

    public final CircularIntArray[] getItemPositionsInRows(int startPos, int endPos) {
        int i;
        for (i = 0; i < this.mNumRows; i++) {
            this.mTmpItemPositionsInRows[i].clear();
        }
        if (startPos >= 0) {
            i = startPos;
            while (i <= endPos) {
                CircularIntArray row = this.mTmpItemPositionsInRows[getLocation(i).row];
                if (row.size() <= 0 || row.getLast() != i - 1) {
                    row.addLast(i);
                    row.addLast(i);
                } else {
                    row.popLast();
                    row.addLast(i);
                }
                i++;
            }
        }
        return this.mTmpItemPositionsInRows;
    }

    public void invalidateItemsAfter(int index) {
        super.invalidateItemsAfter(index);
        this.mLocations.removeFromEnd((getLastIndex() - index) + 1);
        if (this.mLocations.size() == 0) {
            this.mFirstIndex = -1;
        }
    }
}
