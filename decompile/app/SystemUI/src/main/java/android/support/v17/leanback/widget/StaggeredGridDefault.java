package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.StaggeredGrid.Location;

final class StaggeredGridDefault extends StaggeredGrid {
    StaggeredGridDefault() {
    }

    int getRowMax(int rowIndex) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MIN_VALUE;
        }
        int edge;
        int i;
        Location loc;
        if (this.mReversedFlow) {
            edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            if (getLocation(this.mFirstVisibleIndex).row == rowIndex) {
                return edge;
            }
            for (i = this.mFirstVisibleIndex + 1; i <= getLastIndex(); i++) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row == rowIndex) {
                    return edge;
                }
            }
        } else {
            edge = this.mProvider.getEdge(this.mLastVisibleIndex);
            loc = getLocation(this.mLastVisibleIndex);
            if (loc.row == rowIndex) {
                return loc.size + edge;
            }
            for (i = this.mLastVisibleIndex - 1; i >= getFirstIndex(); i--) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row == rowIndex) {
                    return loc.size + edge;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    int getRowMin(int rowIndex) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MAX_VALUE;
        }
        int edge;
        Location loc;
        int i;
        if (this.mReversedFlow) {
            edge = this.mProvider.getEdge(this.mLastVisibleIndex);
            loc = getLocation(this.mLastVisibleIndex);
            if (loc.row == rowIndex) {
                return edge - loc.size;
            }
            for (i = this.mLastVisibleIndex - 1; i >= getFirstIndex(); i--) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row == rowIndex) {
                    return edge - loc.size;
                }
            }
        } else {
            edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            if (getLocation(this.mFirstVisibleIndex).row == rowIndex) {
                return edge;
            }
            for (i = this.mFirstVisibleIndex + 1; i <= getLastIndex(); i++) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row == rowIndex) {
                    return edge;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public int findRowMax(boolean findLarge, int indexLimit, int[] indices) {
        int value;
        int edge = this.mProvider.getEdge(indexLimit);
        Location loc = getLocation(indexLimit);
        int row = loc.row;
        int index = indexLimit;
        int visitedRows = 1;
        int visitRow = row;
        int i;
        if (this.mReversedFlow) {
            value = edge;
            i = indexLimit + 1;
            while (visitedRows < this.mNumRows && i <= this.mLastVisibleIndex) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    if (findLarge) {
                        if (edge <= value) {
                        }
                    } else if (edge < value) {
                    }
                    row = visitRow;
                    value = edge;
                    index = i;
                }
                i++;
            }
        } else {
            value = edge + this.mProvider.getSize(indexLimit);
            i = indexLimit - 1;
            while (visitedRows < this.mNumRows && i >= this.mFirstVisibleIndex) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    int newValue = edge + this.mProvider.getSize(i);
                    if (findLarge) {
                        if (newValue <= value) {
                        }
                    } else if (newValue < value) {
                    }
                    row = visitRow;
                    value = newValue;
                    index = i;
                }
                i--;
            }
        }
        if (indices != null) {
            indices[0] = row;
            indices[1] = index;
        }
        return value;
    }

    public int findRowMin(boolean findLarge, int indexLimit, int[] indices) {
        int value;
        int edge = this.mProvider.getEdge(indexLimit);
        Location loc = getLocation(indexLimit);
        int row = loc.row;
        int index = indexLimit;
        int visitedRows = 1;
        int visitRow = row;
        int i;
        if (this.mReversedFlow) {
            value = edge - this.mProvider.getSize(indexLimit);
            i = indexLimit - 1;
            while (visitedRows < this.mNumRows && i >= this.mFirstVisibleIndex) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    int newValue = edge - this.mProvider.getSize(i);
                    if (findLarge) {
                        if (newValue <= value) {
                        }
                    } else if (newValue < value) {
                    }
                    value = newValue;
                    row = visitRow;
                    index = i;
                }
                i--;
            }
        } else {
            value = edge;
            i = indexLimit + 1;
            while (visitedRows < this.mNumRows && i <= this.mLastVisibleIndex) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    if (findLarge) {
                        if (edge <= value) {
                        }
                    } else if (edge < value) {
                    }
                    value = edge;
                    row = visitRow;
                    index = i;
                }
                i++;
            }
        }
        if (indices != null) {
            indices[0] = row;
            indices[1] = index;
        }
        return value;
    }

    private int findRowEdgeLimitSearchIndex(boolean append) {
        boolean wrapped = false;
        int index;
        int row;
        if (append) {
            for (index = this.mLastVisibleIndex; index >= this.mFirstVisibleIndex; index--) {
                row = getLocation(index).row;
                if (row == 0) {
                    wrapped = true;
                } else if (wrapped && row == this.mNumRows - 1) {
                    return index;
                }
            }
        } else {
            for (index = this.mFirstVisibleIndex; index <= this.mLastVisibleIndex; index++) {
                row = getLocation(index).row;
                if (row == this.mNumRows - 1) {
                    wrapped = true;
                } else if (wrapped && row == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean appendVisibleItemsWithoutCache(int toLimit, boolean oneColumnMode) {
        int itemIndex;
        int rowIndex;
        int edgeLimit;
        boolean edgeLimitIsValid;
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex < 0) {
            int i;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (this.mLocations.size() > 0) {
                i = getLocation(getLastIndex()).row + 1;
            } else {
                i = itemIndex;
            }
            rowIndex = i % this.mNumRows;
            edgeLimit = 0;
            edgeLimitIsValid = false;
        } else if (this.mLastVisibleIndex < getLastIndex()) {
            return false;
        } else {
            itemIndex = this.mLastVisibleIndex + 1;
            rowIndex = getLocation(this.mLastVisibleIndex).row;
            int edgeLimitSearchIndex = findRowEdgeLimitSearchIndex(true);
            if (edgeLimitSearchIndex < 0) {
                edgeLimit = Integer.MIN_VALUE;
                int i2 = 0;
                while (i2 < this.mNumRows) {
                    edgeLimit = this.mReversedFlow ? getRowMin(i2) : getRowMax(i2);
                    if (edgeLimit != Integer.MIN_VALUE) {
                        break;
                    }
                    i2++;
                }
            } else if (this.mReversedFlow) {
                edgeLimit = findRowMin(false, edgeLimitSearchIndex, null);
            } else {
                edgeLimit = findRowMax(true, edgeLimitSearchIndex, null);
            }
            if (!this.mReversedFlow) {
                if (getRowMax(rowIndex) >= edgeLimit) {
                }
                edgeLimitIsValid = true;
            }
            rowIndex++;
            if (rowIndex == this.mNumRows) {
                rowIndex = 0;
                edgeLimit = this.mReversedFlow ? findRowMin(false, null) : findRowMax(true, null);
            }
            edgeLimitIsValid = true;
        }
        boolean filledOne = false;
        loop1:
        while (true) {
            if (rowIndex < this.mNumRows) {
                if (itemIndex == count || (!oneColumnMode && checkAppendOverLimit(toLimit))) {
                    return filledOne;
                }
                int location = this.mReversedFlow ? getRowMin(rowIndex) : getRowMax(rowIndex);
                if (location != Integer.MAX_VALUE && location != Integer.MIN_VALUE) {
                    location += this.mReversedFlow ? -this.mMargin : this.mMargin;
                } else if (rowIndex == 0) {
                    location = this.mReversedFlow ? getRowMin(this.mNumRows - 1) : getRowMax(this.mNumRows - 1);
                    if (!(location == Integer.MAX_VALUE || location == Integer.MIN_VALUE)) {
                        location += this.mReversedFlow ? -this.mMargin : this.mMargin;
                    }
                } else {
                    location = this.mReversedFlow ? getRowMax(rowIndex - 1) : getRowMin(rowIndex - 1);
                }
                int itemIndex2 = itemIndex + 1;
                int size = appendVisibleItemToRow(itemIndex, rowIndex, location);
                filledOne = true;
                if (edgeLimitIsValid) {
                    while (true) {
                        itemIndex = itemIndex2;
                        if (!this.mReversedFlow) {
                            if (location + size >= edgeLimit) {
                                break;
                            }
                        } else if (location - size <= edgeLimit) {
                            break;
                        }
                        if (itemIndex == count || (!oneColumnMode && checkAppendOverLimit(toLimit))) {
                            return true;
                        }
                        location += this.mReversedFlow ? (-size) - this.mMargin : this.mMargin + size;
                        itemIndex2 = itemIndex + 1;
                        size = appendVisibleItemToRow(itemIndex, rowIndex, location);
                    }
                } else {
                    edgeLimitIsValid = true;
                    if (this.mReversedFlow) {
                        edgeLimit = getRowMin(rowIndex);
                        itemIndex = itemIndex2;
                    } else {
                        edgeLimit = getRowMax(rowIndex);
                        itemIndex = itemIndex2;
                    }
                }
                rowIndex++;
            } else if (oneColumnMode) {
                return filledOne;
            } else {
                edgeLimit = this.mReversedFlow ? findRowMin(false, null) : findRowMax(true, null);
                rowIndex = 0;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean prependVisibleItemsWithoutCache(int toLimit, boolean oneColumnMode) {
        int itemIndex;
        int rowIndex;
        int edgeLimit;
        boolean edgeLimitIsValid;
        if (this.mFirstVisibleIndex < 0) {
            int i;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (this.mLocations.size() >= 0) {
                i = (getLocation(getFirstIndex()).row + this.mNumRows) - 1;
            } else {
                i = itemIndex;
            }
            rowIndex = i % this.mNumRows;
            edgeLimit = 0;
            edgeLimitIsValid = false;
        } else if (this.mFirstVisibleIndex > getFirstIndex()) {
            return false;
        } else {
            itemIndex = this.mFirstVisibleIndex - 1;
            rowIndex = getLocation(this.mFirstVisibleIndex).row;
            int edgeLimitSearchIndex = findRowEdgeLimitSearchIndex(false);
            if (edgeLimitSearchIndex < 0) {
                rowIndex--;
                edgeLimit = Integer.MAX_VALUE;
                int i2 = this.mNumRows - 1;
                while (i2 >= 0) {
                    edgeLimit = this.mReversedFlow ? getRowMax(i2) : getRowMin(i2);
                    if (edgeLimit != Integer.MAX_VALUE) {
                        break;
                    }
                    i2--;
                }
            } else if (this.mReversedFlow) {
                edgeLimit = findRowMax(true, edgeLimitSearchIndex, null);
            } else {
                edgeLimit = findRowMin(false, edgeLimitSearchIndex, null);
            }
            if (!this.mReversedFlow) {
                if (getRowMin(rowIndex) <= edgeLimit) {
                }
                edgeLimitIsValid = true;
            }
            rowIndex--;
            if (rowIndex < 0) {
                rowIndex = this.mNumRows - 1;
                if (this.mReversedFlow) {
                    edgeLimit = findRowMax(true, null);
                } else {
                    edgeLimit = findRowMin(false, null);
                }
            }
            edgeLimitIsValid = true;
        }
        boolean filledOne = false;
        int itemIndex2 = itemIndex;
        loop1:
        while (true) {
            if (rowIndex >= 0) {
                if (itemIndex2 < 0 || (!oneColumnMode && checkPrependOverLimit(toLimit))) {
                    return filledOne;
                }
                int location = this.mReversedFlow ? getRowMax(rowIndex) : getRowMin(rowIndex);
                if (location != Integer.MAX_VALUE && location != Integer.MIN_VALUE) {
                    location += this.mReversedFlow ? this.mMargin : -this.mMargin;
                } else if (rowIndex == this.mNumRows - 1) {
                    location = this.mReversedFlow ? getRowMax(0) : getRowMin(0);
                    if (!(location == Integer.MAX_VALUE || location == Integer.MIN_VALUE)) {
                        location += this.mReversedFlow ? this.mMargin : -this.mMargin;
                    }
                } else {
                    location = this.mReversedFlow ? getRowMin(rowIndex + 1) : getRowMax(rowIndex + 1);
                }
                itemIndex = itemIndex2 - 1;
                int size = prependVisibleItemToRow(itemIndex2, rowIndex, location);
                filledOne = true;
                if (edgeLimitIsValid) {
                    while (true) {
                        if (!this.mReversedFlow) {
                            if (location - size <= edgeLimit) {
                                break;
                            }
                        } else if (location + size >= edgeLimit) {
                            break;
                        }
                        if (itemIndex < 0 || (!oneColumnMode && checkPrependOverLimit(toLimit))) {
                            return true;
                        }
                        location += this.mReversedFlow ? this.mMargin + size : (-size) - this.mMargin;
                        itemIndex2 = itemIndex - 1;
                        size = prependVisibleItemToRow(itemIndex, rowIndex, location);
                        itemIndex = itemIndex2;
                    }
                } else {
                    edgeLimitIsValid = true;
                    edgeLimit = this.mReversedFlow ? getRowMax(rowIndex) : getRowMin(rowIndex);
                }
                rowIndex--;
                itemIndex2 = itemIndex;
            } else if (oneColumnMode) {
                return filledOne;
            } else {
                edgeLimit = this.mReversedFlow ? findRowMax(true, null) : findRowMin(false, null);
                rowIndex = this.mNumRows - 1;
            }
        }
        return true;
    }
}
