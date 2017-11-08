package com.huawei.gallery.ui;

import android.graphics.Rect;
import android.os.ConditionVariable;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.Paper;
import com.android.gallery3d.ui.ScrollerHelper;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.gallery.ui.SlotView.DeleteSlotAnimation;
import com.huawei.gallery.ui.SlotView.DownShiftAnimation;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.ui.SlotView.SlotUIListener;
import com.huawei.gallery.util.SyncUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.HashMap;
import javax.microedition.khronos.opengles.GL11;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class ListSlotView extends SlotView {
    private static final int TITLE_TOP_OFFSET = GalleryUtils.dpToPixel(5);
    private boolean mBeBiggerView = false;
    private boolean mDoSelect = false;
    private boolean mDownInScrolling;
    private boolean mFirstFling = true;
    private boolean mFirstScroll = true;
    private final GestureDetector mGestureDetector;
    private ItemCoordinate mIndexDown = null;
    private ItemCoordinate mIndexUp = null;
    private final Layout mLayout;
    private Listener mListener;
    private int mOverscrollEffect = 2;
    private final Paper mPaper = new Paper(false);
    private boolean mPreviewMode;
    private SlotRenderer mRenderer;
    private final ScaleGestureDetector mScaleDetector;
    private ScrollOverListener mScrollOverListener;
    private ScrollerHelper mScroller;
    private boolean mSelectionMode = false;
    private ModeShiftAnimation mShiftAnimation = null;
    private SlotScrollBarView mSlotScrollBar;
    private final Rect mTempRect = new Rect();
    private ItemCoordinate mTimeModeChangeItem = null;
    private TimeBucketPageViewMode mViewMode = TimeBucketPageViewMode.DAY;

    public interface Listener extends SlotUIListener {
        boolean enableScrollSelection();

        int getOverflowMarginTop();

        void onCancel();

        void onDown(ItemCoordinate itemCoordinate);

        void onLongTap(MotionEvent motionEvent);

        void onMove(MotionEvent motionEvent);

        void onResetMainView();

        void onScroll(ItemCoordinate itemCoordinate);

        void onSingleTapUp(ItemCoordinate itemCoordinate, boolean z);

        void onTouchDown(MotionEvent motionEvent);

        void onTouchUp(MotionEvent motionEvent);

        void onUp(boolean z);

        void renderHeadCover(GLCanvas gLCanvas, int i, int i2, int i3, int i4);
    }

    public interface ScrollOverListener {
        float getOffset();

        void onScrollOver(float f);

        void onScrollOverBegin();

        void onScrollOverDone();
    }

    public interface SlotRenderer extends CornerPressedListener, SlotRenderInterface {
        void clearOldTitleData();

        boolean onScale(boolean z);

        void onSlotSizeChanged(int i, int i2);

        void onVisibleRangeChanged(ItemCoordinate itemCoordinate, ItemCoordinate itemCoordinate2);

        void onVisibleRangeChanged(ItemCoordinate itemCoordinate, ItemCoordinate itemCoordinate2, ItemCoordinate itemCoordinate3, ItemCoordinate itemCoordinate4);

        void prepareDrawing();

        int renderSlot(GLCanvas gLCanvas, BaseEntry baseEntry, ItemCoordinate itemCoordinate, boolean z, boolean z2, int i, int i2);

        int renderSlot(GLCanvas gLCanvas, ItemCoordinate itemCoordinate, boolean z, boolean z2, int i, int i2, boolean z3, boolean z4);

        int renderTopTitleOverflow(GLCanvas gLCanvas, ItemCoordinate itemCoordinate, boolean z, int i, int i2);
    }

    public static class ItemCoordinate {
        public int group = -1;
        public int subIndex = -1;

        public ItemCoordinate(int group, int subIndex) {
            this.group = group;
            this.subIndex = subIndex;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == null) {
                return false;
            }
            ItemCoordinate coordinate = (ItemCoordinate) o;
            if (this.group == coordinate.group && this.subIndex == coordinate.subIndex) {
                z = true;
            }
            return z;
        }

        protected ItemCoordinate clone() {
            return new ItemCoordinate(this.group, this.subIndex);
        }

        public String toString() {
            return "(" + this.group + ", " + this.subIndex + ")";
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public boolean isSmall(ItemCoordinate coordinate) {
            boolean z = true;
            if (this.group < coordinate.group) {
                return true;
            }
            if (this.group > coordinate.group) {
                return false;
            }
            if (this.subIndex > coordinate.subIndex) {
                z = false;
            }
            return z;
        }

        public boolean isLarge(ItemCoordinate coordinate) {
            boolean z = true;
            if (this.group > coordinate.group) {
                return true;
            }
            if (this.group < coordinate.group) {
                return false;
            }
            if (this.subIndex < coordinate.subIndex) {
                z = false;
            }
            return z;
        }

        public boolean isTitle() {
            return this.group >= 0 && this.subIndex == -1;
        }
    }

    private class Layout extends AbsLayout {
        private final ItemCoordinate DEFAULT_RANGE = new ItemCoordinate(0, -1);
        private ItemCoordinate mEndVisibleCoordinate = new ItemCoordinate(0, -1);
        private GroupCount mGroupCount = new GroupCount();
        private ArrayList<Integer> mGroupStartPosition = new ArrayList();
        private Spec mSpec;
        private ItemCoordinate mStartVisibleCoordinate = new ItemCoordinate(0, -1);
        public int mTitleHeight;
        private int mTitleWidth;

        private class GroupCount {
            private ArrayList<Integer> mGroup;

            private GroupCount() {
                this.mGroup = null;
            }

            public synchronized int getCount(int index) {
                if (this.mGroup == null || index < 0 || index >= this.mGroup.size()) {
                    return -1;
                }
                return ((Integer) this.mGroup.get(index)).intValue();
            }

            public synchronized int size() {
                if (this.mGroup == null) {
                    return 0;
                }
                return this.mGroup.size();
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private boolean isSame(ArrayList<Integer> group) {
                if (this.mGroup == null || group == null || this.mGroup.size() != group.size()) {
                    return false;
                }
                for (int i = 0; i < group.size(); i++) {
                    if (this.mGroup.get(i) != group.get(i)) {
                        return false;
                    }
                }
                return true;
            }

            public synchronized boolean update(ArrayList<Integer> group) {
                if (isSame(group)) {
                    return false;
                }
                this.mGroup = group;
                return true;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public synchronized boolean invalidItem(ItemCoordinate index) {
                if (!(this.mGroup == null || index == null)) {
                    if (index.group >= 0 && index.group < this.mGroup.size()) {
                        if (index.subIndex >= -1 && index.subIndex < ((Integer) this.mGroup.get(index.group)).intValue()) {
                            return false;
                        }
                    }
                }
            }

            public synchronized int getSubContentLen(int groupCount) {
                if (this.mGroup == null) {
                    return 0;
                }
                int total = this.mGroup.size();
                if (groupCount > total) {
                    return 0;
                }
                int contentLen = ListSlotView.this.mHeadCoverHeight;
                for (int i = 0; i < groupCount; i++) {
                    int count = ((((Integer) this.mGroup.get(i)).intValue() + Layout.this.mUnitCount) - 1) / Layout.this.mUnitCount;
                    contentLen = (contentLen + Layout.this.mTitleHeight) + ((Layout.this.mSlotHeight * count) + ((count - 1) * Layout.this.mSlotHeightGap));
                    if (i == total - 1) {
                        contentLen += Layout.this.mSlotHeightGap;
                    }
                }
                return contentLen;
            }

            public synchronized void updateContentLen() {
                if (this.mGroup == null) {
                    Layout.this.mContentLength = 0;
                    return;
                }
                int total = this.mGroup.size();
                int contentLen = 0;
                Layout.this.mGroupStartPosition.clear();
                for (int i = 0; i < total; i++) {
                    Layout.this.mGroupStartPosition.add(Integer.valueOf(contentLen));
                    int count = ((((Integer) this.mGroup.get(i)).intValue() + Layout.this.mUnitCount) - 1) / Layout.this.mUnitCount;
                    contentLen = (contentLen + Layout.this.mTitleHeight) + ((Layout.this.mSlotHeight * count) + ((count - 1) * Layout.this.mSlotHeightGap));
                    if (i == total - 1) {
                        contentLen += Layout.this.mSlotHeightGap;
                    }
                }
                Layout.this.mContentLength = contentLen;
            }

            public synchronized boolean moveToLast(ItemCoordinate coordinate) {
                if (this.mGroup == null) {
                    return false;
                }
                int i = coordinate.subIndex - 1;
                coordinate.subIndex = i;
                if (i < -1) {
                    i = coordinate.group - 1;
                    coordinate.group = i;
                    if (i < 0) {
                        return false;
                    }
                    if (coordinate.group >= this.mGroup.size()) {
                        return false;
                    }
                    coordinate.subIndex = ((Integer) this.mGroup.get(coordinate.group)).intValue() - 1;
                }
                return true;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public synchronized boolean moveToNext(ItemCoordinate coordinate) {
                if (this.mGroup != null && !this.mGroup.isEmpty()) {
                    int i = coordinate.subIndex + 1;
                    coordinate.subIndex = i;
                    if (i > ((Integer) this.mGroup.get(coordinate.group)).intValue() - 1) {
                        int total = this.mGroup.size();
                        int i2 = coordinate.group + 1;
                        coordinate.group = i2;
                        if (i2 > total) {
                            return false;
                        }
                        coordinate.subIndex = -1;
                    }
                    return true;
                }
            }
        }

        public Rect getTargetSlotRect(Object index, Rect rect) {
            return getSlotRect((ItemCoordinate) index, rect);
        }

        public boolean isPreVisibleStartIndex(Object index) {
            return ((ItemCoordinate) index).isSmall(this.mStartVisibleCoordinate);
        }

        public Layout(Spec spec) {
            super();
            this.mSpec = spec;
        }

        protected Layout clone() {
            Layout layout = new Layout(this.mSpec);
            copyAllParameters(layout);
            layout.mStartVisibleCoordinate = this.mStartVisibleCoordinate.clone();
            layout.mEndVisibleCoordinate = this.mEndVisibleCoordinate.clone();
            ArrayList<Integer> group = new ArrayList();
            group.addAll(this.mGroupCount.mGroup);
            layout.mGroupCount.update(group);
            layout.mGroupStartPosition = new ArrayList();
            layout.mGroupStartPosition.addAll(this.mGroupStartPosition);
            layout.mTitleWidth = this.mTitleWidth;
            layout.mTitleHeight = this.mTitleHeight;
            return layout;
        }

        public boolean setSlotCount(ArrayList<Integer> group, boolean forceInit) {
            boolean initLayout = this.mGroupCount.update(group);
            if (forceInit || initLayout) {
                initLayoutParameters(ListSlotView.this.mViewMode, null);
            }
            return true;
        }

        public boolean isValidItem(ItemCoordinate itemCoordinate) {
            if (itemCoordinate == null) {
                return false;
            }
            if ((itemCoordinate.group < this.mGroupCount.size() || itemCoordinate.group >= 0) && itemCoordinate.subIndex < this.mGroupCount.getCount(itemCoordinate.group)) {
                return true;
            }
            return false;
        }

        public Rect getSlotRect(ItemCoordinate itemCoordinate, Rect rect) {
            if (itemCoordinate == null) {
                return rect;
            }
            int index = itemCoordinate.group;
            int subIndex = itemCoordinate.subIndex;
            int y = this.mGroupCount.getSubContentLen(index);
            if (subIndex != -1) {
                int x;
                int row = subIndex / this.mUnitCount;
                int col = subIndex - (this.mUnitCount * row);
                if (ListSlotView.this.mIsLayoutRtl) {
                    x = ListSlotView.this.getWidth() - ((this.mSlotWidth + this.mSlotWidthGap) * col);
                } else {
                    x = col * (this.mSlotWidth + this.mSlotWidthGap);
                }
                y = (y + this.mTitleHeight) + ((this.mSlotHeight + this.mSlotHeightGap) * row);
                int width = this.mSlotWidth + getSizeDelta(col);
                if (ListSlotView.this.mIsLayoutRtl) {
                    rect.set(x - width, y, x, this.mSlotHeight + y);
                    rect.offset(-getOffset(col), 0);
                } else {
                    rect.set(x, y, x + width, this.mSlotHeight + y);
                    rect.offset(getOffset(col), 0);
                }
            } else {
                rect.set(0, y, this.mTitleWidth + 0, this.mTitleHeight + y);
            }
            return rect;
        }

        private void initLayoutParameters(TimeBucketPageViewMode mode, Layout oldLayout) {
            if (TimeBucketPageViewMode.MONTH == ListSlotView.this.mViewMode) {
                this.mSlotWidthGap = 0;
                this.mSlotHeightGap = 0;
                this.mUnitCount = isPort() ? this.mSpec.slotPortCountByMonth : this.mSpec.slotLandCountByMonth;
                this.mTitleHeight = this.mSpec.titleHeight;
            } else {
                this.mSlotWidthGap = this.mSpec.slotGap;
                this.mSlotHeightGap = this.mSpec.slotGap;
                this.mUnitCount = isPort() ? this.mSpec.slotPortCountByDay : this.mSpec.slotLandCountByDay;
                this.mTitleHeight = this.mSpec.titleHeight;
            }
            this.mSlotWidth = (this.mWidth - (this.mSlotWidthGap * (this.mUnitCount - 1))) / this.mUnitCount;
            this.mSlotHeight = this.mSlotWidth;
            this.mTitleWidth = this.mWidth;
            apportionMargin();
            if (ListSlotView.this.mRenderer != null) {
                ListSlotView.this.mRenderer.onSlotSizeChanged(this.mSlotWidth, this.mSlotHeight);
            }
            this.mGroupCount.updateContentLen();
            if (!updatePositionWithFocus(oldLayout)) {
                updateVisibleSlotRange();
            }
            if (ListSlotView.this.mSlotScrollBar != null) {
                ListSlotView.this.mSlotScrollBar.updateContentLen(ListSlotView.this.mLayout.getScrollLimit());
            }
        }

        private boolean updatePositionWithFocus(Layout oldLayout) {
            if (oldLayout == null) {
                return false;
            }
            ItemCoordinate visibleStart = oldLayout.getCoordinateByPosition(this.mScrollPosition, false);
            if (visibleStart == null) {
                return false;
            }
            int newScrollPosition = (int) ((((float) (visibleStart.subIndex == -1 ? this.mTitleHeight : this.mSlotHeight)) * (((float) (this.mScrollPosition - oldLayout.getSlotRect(visibleStart, ListSlotView.this.mTempRect).top)) / ((float) (visibleStart.subIndex == -1 ? oldLayout.mTitleHeight : oldLayout.mSlotHeight)))) + ((float) getSlotRect(visibleStart, ListSlotView.this.mTempRect).top));
            boolean scrollPositionChanged = newScrollPosition != this.mScrollPosition;
            setScrollPosition(newScrollPosition);
            return scrollPositionChanged;
        }

        public void setSize(int width, int height) {
            Layout layout = ListSlotView.this.mLayout.clone();
            this.mWidth = width;
            this.mHeight = height;
            initLayoutParameters(ListSlotView.this.mViewMode, layout);
        }

        private ItemCoordinate getCoordinateByPosition(int position, boolean findEnd, boolean forceOneItem) {
            ItemCoordinate visibleCoordinate = new ItemCoordinate(0, -1);
            int size = this.mGroupStartPosition.size();
            if (size <= 0) {
                return null;
            }
            if (!forceOneItem && position > this.mContentLength) {
                return null;
            }
            int group = 0;
            while (group < size && position != ((Integer) this.mGroupStartPosition.get(group)).intValue()) {
                if (position < ((Integer) this.mGroupStartPosition.get(group)).intValue()) {
                    group--;
                    break;
                }
                group++;
            }
            group = Math.max(0, Math.min(group, size - 1));
            visibleCoordinate.group = group;
            int subCount = this.mGroupCount.getCount(group);
            if (subCount < 0) {
                return null;
            }
            position = (position - ((Integer) this.mGroupStartPosition.get(group)).intValue()) - this.mTitleHeight;
            if (position <= 0) {
                return visibleCoordinate;
            }
            int rowIndex = Math.min(position / (this.mSlotHeight + this.mSlotHeightGap), Math.max(((this.mUnitCount + subCount) - 1) / this.mUnitCount, 1) - 1);
            if (findEnd) {
                visibleCoordinate.subIndex = Math.min(subCount, this.mUnitCount * (rowIndex + 1));
            } else {
                visibleCoordinate.subIndex = this.mUnitCount * rowIndex;
            }
            return visibleCoordinate;
        }

        private ItemCoordinate getCoordinateByPosition(int position, boolean findEnd) {
            return getCoordinateByPosition(position, findEnd, false);
        }

        private void updateVisibleSlotRange() {
            int startPosition = Math.max(0, this.mScrollPosition - ListSlotView.this.mHeadCoverHeight);
            setVisibleRange(getCoordinateByPosition(startPosition, false), getCoordinateByPosition(startPosition + this.mHeight, true, true));
        }

        public void setScrollPosition(int position) {
            if (this.mScrollPosition != position) {
                this.mScrollPosition = position;
                updateVisibleSlotRange();
                if (ListSlotView.this.mSlotScrollBar != null) {
                    ListSlotView.this.mSlotScrollBar.updateContentOffset(this.mScrollPosition);
                }
            }
        }

        public int getRow(ItemCoordinate itemCoordinate) {
            int i;
            int i2 = 1;
            int group = itemCoordinate.group;
            int row = 0;
            for (int i3 = 0; i3 < group; i3++) {
                int count = this.mGroupCount.getCount(i3);
                int i4 = row + (count / this.mUnitCount);
                if (count % this.mUnitCount != 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                row = i4 + i;
            }
            int index = itemCoordinate.subIndex;
            i = ((index + 1) / this.mUnitCount) + row;
            if ((index + 1) % this.mUnitCount == 0) {
                i2 = 0;
            }
            return i + i2;
        }

        public int getGroupIndex(int row) {
            int rowIndex = 0;
            for (int i = 0; i < this.mGroupCount.size(); i++) {
                int i2;
                int count = this.mGroupCount.getCount(i);
                int i3 = rowIndex + (count / this.mUnitCount);
                if (count % this.mUnitCount != 0) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                rowIndex = i3 + i2;
                if (rowIndex >= row) {
                    return i;
                }
            }
            return this.mGroupCount.size() - 1;
        }

        public int getGroupRowIndex(int row) {
            int i;
            int i2 = 1;
            int rowIndex = 0;
            for (int i3 = 0; i3 < this.mGroupCount.size(); i3++) {
                int count = this.mGroupCount.getCount(i3);
                int i4 = rowIndex + (count / this.mUnitCount);
                if (count % this.mUnitCount != 0) {
                    i = 1;
                } else {
                    i = 0;
                }
                rowIndex = i4 + i;
                if (rowIndex >= row) {
                    i = count / this.mUnitCount;
                    if (count % this.mUnitCount == 0) {
                        i2 = 0;
                    }
                    return row - (rowIndex - (i2 + i));
                }
            }
            i = this.mGroupCount.getCount(this.mGroupCount.size() - 1) / this.mUnitCount;
            if (this.mGroupCount.getCount(this.mGroupCount.size() - 1) % this.mUnitCount == 0) {
                i2 = 0;
            }
            return row - (rowIndex - (i2 + i));
        }

        private void setVisibleRange(ItemCoordinate startRange, ItemCoordinate endRange) {
            if (startRange == null || endRange == null || !startRange.isSmall(endRange)) {
                ItemCoordinate itemCoordinate = this.DEFAULT_RANGE;
                this.mEndVisibleCoordinate = itemCoordinate;
                this.mStartVisibleCoordinate = itemCoordinate;
            } else {
                this.mStartVisibleCoordinate = startRange;
                this.mEndVisibleCoordinate = endRange;
            }
            if (ListSlotView.this.mRenderer != null) {
                ListSlotView.this.mRenderer.onVisibleRangeChanged(this.mStartVisibleCoordinate, this.mEndVisibleCoordinate);
            }
        }

        public int getAbsSlotIndex(ItemCoordinate slotIndex) {
            if (slotIndex == null) {
                return 0;
            }
            int index = slotIndex.group;
            int subIndex = slotIndex.subIndex;
            int result = 0;
            for (int i = 0; i < index; i++) {
                result += this.mGroupCount.getCount(i);
            }
            if (subIndex != -1) {
                result += subIndex;
            }
            return result;
        }

        public ItemCoordinate getItemCoordinateByAbsIndex(int absIndex) {
            int index = 0;
            int group = 0;
            int size = this.mGroupCount.size();
            while (group < size) {
                int count = this.mGroupCount.getCount(group);
                index += count;
                if (index > absIndex) {
                    index -= count;
                    break;
                }
                group++;
            }
            if (group >= size) {
                return new ItemCoordinate(group - 1, this.mGroupCount.getCount(group - 1) - 1);
            }
            return new ItemCoordinate(group, absIndex - index);
        }

        public ItemCoordinate getVisibleStart() {
            return this.mStartVisibleCoordinate;
        }

        public ItemCoordinate getVisibleEnd() {
            return this.mEndVisibleCoordinate;
        }

        public ItemCoordinate getSlotIndexByScrollPositionY(int y) {
            return getCoordinateByPosition(this.mScrollPosition + y, false);
        }

        private int getPositionByCoordinate(ItemCoordinate itemCoordinate) {
            if (itemCoordinate == null || this.mGroupStartPosition.size() == 0) {
                return 0;
            }
            if (-1 == itemCoordinate.subIndex) {
                return ((Integer) this.mGroupStartPosition.get(itemCoordinate.group)).intValue();
            }
            return ((this.mSlotHeight + this.mSlotHeightGap) * (itemCoordinate.subIndex / this.mUnitCount)) + (((Integer) this.mGroupStartPosition.get(itemCoordinate.group)).intValue() + this.mTitleHeight);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ItemCoordinate getSlotIndexByPosition(float x, float y) {
            if (this.mContentLength == 0) {
                return null;
            }
            int absoluteX = Math.round(x);
            int absoluteY = (Math.round(y) + this.mScrollPosition) - ListSlotView.this.mHeadCoverHeight;
            if (absoluteX < 0 || absoluteY < 0) {
                return null;
            }
            ItemCoordinate itemCoordinate = getCoordinateByPosition(absoluteY, false);
            if (itemCoordinate == null) {
                return null;
            }
            int position = getPositionByCoordinate(itemCoordinate);
            if (itemCoordinate.subIndex == -1) {
                if (absoluteY - position <= 0 || absoluteY - position > this.mTitleHeight) {
                    return null;
                }
            } else if (absoluteY - position <= this.mSlotHeightGap) {
                return null;
            }
            if (itemCoordinate.subIndex == -1) {
                return itemCoordinate;
            }
            int columnIdx;
            if (ListSlotView.this.mIsLayoutRtl) {
                columnIdx = (this.mUnitCount - (absoluteX / (this.mSlotWidth + this.mSlotWidthGap))) - 1;
            } else {
                columnIdx = absoluteX / (this.mSlotWidth + this.mSlotWidthGap);
            }
            if (columnIdx >= this.mUnitCount || this.mUnitCount - (absoluteX / (this.mSlotWidth + this.mSlotWidthGap)) < 0 || absoluteX % (this.mSlotWidth + this.mSlotWidthGap) <= this.mSlotWidthGap) {
                return null;
            }
            itemCoordinate.subIndex += columnIdx;
            if (itemCoordinate.subIndex >= this.mGroupCount.getCount(itemCoordinate.group)) {
                return null;
            }
            return itemCoordinate;
        }

        public void transformRect(ItemCoordinate index, ItemCoordinate downIndex, float progress, Rect target) {
            int dy;
            int unitCount = this.mUnitCount;
            int width = this.mSlotWidth;
            int height = this.mSlotHeight;
            float[] shift = new float[2];
            int dx = (downIndex.subIndex % unitCount) - (index.subIndex % unitCount);
            if (index.group < downIndex.group) {
                dy = ((downIndex.subIndex / unitCount) + ((this.mGroupCount.getCount(index.group) / unitCount) + 1)) - (index.subIndex / unitCount);
            } else if (index.group > downIndex.group) {
                dy = (downIndex.subIndex / unitCount) - ((index.subIndex / unitCount) + ((this.mGroupCount.getCount(downIndex.group) / unitCount) + 1));
            } else {
                dy = (downIndex.subIndex / unitCount) - (index.subIndex / unitCount);
            }
            float distance = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
            shift[0] = dx == 0 ? 0.0f : ((((float) width) / ((24.0f * distance) * 2.25f)) * Math.signum((float) dx)) * progress;
            shift[1] = dy == 0 ? 0.0f : ((((float) height) / ((24.0f * distance) * 2.25f)) * Math.signum((float) dy)) * progress;
            boolean neighbors = Math.abs(dx) < 2 && Math.abs(dy) < 2;
            if (neighbors) {
                float scale = Math.min(WMElement.CAMERASIZEVALUE1B1, (distance / ((float) width)) + (0.99f + ((WMElement.CAMERASIZEVALUE1B1 - progress) * 0.00999999f)));
                shift[0] = (((dx == 0 ? 0.0f : Math.signum((float) dx)) * ((float) target.width())) * (WMElement.CAMERASIZEVALUE1B1 - scale)) + shift[0];
                shift[1] = (((dy == 0 ? 0.0f : Math.signum((float) dy)) * ((float) target.height())) * (WMElement.CAMERASIZEVALUE1B1 - scale)) + shift[1];
                ListSlotView.this.scaleRect(target, scale);
                ListSlotView.this.translateRect(target, shift);
                return;
            }
            ListSlotView.this.translateRect(target, shift);
        }
    }

    public static class SlotAnimation extends Animation {
        public float mProgress = 0.0f;

        protected void onCalculate(float progress) {
            this.mProgress = progress;
        }
    }

    private class ModeShiftAnimation extends SlotAnimation {
        private int mFromColumn = 0;
        private final Layout mFromLayout;
        private int mFromRow = 0;
        private SlotAnimation mTitleHideAnimation = new SlotAnimation();
        private SlotAnimation mTitleShowAnimation = new SlotAnimation();
        private int mToColumn = 0;
        private int mToRow = 0;

        public ModeShiftAnimation() {
            setInterpolator(new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
            setDuration(500);
            this.mTitleShowAnimation.setDuration(250);
            this.mTitleShowAnimation.setInterpolator(new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
            this.mTitleShowAnimation.setDelay(250);
            this.mTitleHideAnimation.setDuration(100);
            this.mTitleHideAnimation.setInterpolator(new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
            this.mFromLayout = ListSlotView.this.mLayout.clone();
        }

        public void start() {
            this.mTitleShowAnimation.start();
            this.mTitleHideAnimation.start();
            super.start();
        }

        public void forceStop() {
            this.mTitleHideAnimation.forceStop();
            this.mTitleShowAnimation.forceStop();
            super.forceStop();
        }

        public boolean calculate(long currentTimeMillis) {
            this.mTitleHideAnimation.calculate(currentTimeMillis);
            this.mTitleShowAnimation.calculate(currentTimeMillis);
            return super.calculate(currentTimeMillis);
        }

        private void getTitleTargetRect(ItemCoordinate slotIndex, Rect target) {
            int top;
            ItemCoordinate delegate = slotIndex.clone();
            delegate.subIndex = 0;
            Rect delegateTo = getTargetWithForce(delegate);
            int left = delegateTo.left;
            int bottom = delegateTo.top;
            delegate.subIndex = this.mFromLayout.mUnitCount - 1;
            int right = getTargetWithForce(delegate).right;
            float scale = ((float) (right - left)) / ((float) this.mFromLayout.mTitleWidth);
            if (slotIndex.group <= 0) {
                top = (int) (((float) bottom) - (((float) this.mFromLayout.mTitleHeight) * scale));
            } else {
                delegate.group--;
                delegate.subIndex = this.mFromLayout.mGroupCount.getCount(delegate.group) - 1;
                top = getTargetWithForce(delegate).bottom;
            }
            target.set(left, top, right, bottom);
        }

        private void getTitleFromRect(ItemCoordinate slotIndex, Rect fromRect) {
            int top;
            ItemCoordinate delegate = slotIndex.clone();
            delegate.subIndex = 0;
            Rect delegateFrom = getSourceWithForce(delegate);
            int left = delegateFrom.left;
            int bottom = delegateFrom.top;
            delegate.subIndex = ListSlotView.this.mLayout.mUnitCount - 1;
            int right = getSourceWithForce(delegate).right;
            float scale = ((float) (right - left)) / ((float) ListSlotView.this.mLayout.mTitleWidth);
            if (slotIndex.group <= 0) {
                top = (int) (((float) bottom) - (((float) ListSlotView.this.mLayout.mTitleHeight) * scale));
            } else {
                delegate.group--;
                delegate.subIndex = ListSlotView.this.mLayout.mGroupCount.getCount(delegate.group) - 1;
                top = getSourceWithForce(delegate).bottom;
            }
            fromRect.set(left, top, right, bottom);
        }

        public void applyFromTo(GLCanvas canvas, ItemCoordinate slotIndex, Rect target) {
            Rect fromRect = new Rect();
            Rect toRect = new Rect();
            if (slotIndex.isTitle()) {
                canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1 - this.mTitleHideAnimation.mProgress);
                fromRect = this.mFromLayout.getSlotRect(slotIndex, fromRect);
                getTitleTargetRect(slotIndex, toRect);
            } else {
                canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1 - this.mProgress);
                fromRect = this.mFromLayout.getSlotRect(slotIndex, fromRect);
                toRect = getTargetWithForce(slotIndex);
            }
            fromRect.set(fromRect.left, (fromRect.top - this.mFromLayout.mScrollPosition) + ListSlotView.this.mLayout.mScrollPosition, fromRect.right, (fromRect.bottom - this.mFromLayout.mScrollPosition) + ListSlotView.this.mLayout.mScrollPosition);
            target.set(getTargetRect(toRect, fromRect));
            ListSlotView.this.mRenderer.onSlotSizeChanged(target.width(), target.height());
        }

        public void applyToFrom(GLCanvas canvas, ItemCoordinate slotIndex, Rect target) {
            Rect fromRect = new Rect();
            Rect toRect = ListSlotView.this.mLayout.getSlotRect(slotIndex, new Rect());
            if (slotIndex.isTitle()) {
                canvas.setAlpha(this.mTitleShowAnimation.mProgress);
                getTitleFromRect(slotIndex, fromRect);
            } else {
                canvas.setAlpha(this.mProgress);
                fromRect = getSourceWithForce(slotIndex);
            }
            fromRect.set(fromRect.left, (fromRect.top - this.mFromLayout.mScrollPosition) + ListSlotView.this.mLayout.mScrollPosition, fromRect.right, (fromRect.bottom - this.mFromLayout.mScrollPosition) + ListSlotView.this.mLayout.mScrollPosition);
            target.set(getTargetRect(toRect, fromRect));
            ListSlotView.this.mRenderer.onSlotSizeChanged(target.width(), target.height());
        }

        private Rect getTargetWithForce(ItemCoordinate slotIndex) {
            Rect rect = new Rect();
            int toRow = (this.mFromLayout.getRow(slotIndex) - this.mFromRow) + this.mToRow;
            int toColumn = ((slotIndex.subIndex % this.mFromLayout.mUnitCount) - this.mFromColumn) + this.mToColumn;
            int toGroup = ListSlotView.this.mLayout.getGroupIndex(toRow);
            int toGroupRow = ListSlotView.this.mLayout.getGroupRowIndex(toRow);
            int y = ListSlotView.this.mLayout.mGroupCount.getSubContentLen(toGroup);
            if (slotIndex.isTitle()) {
                rect.set(0, y, ListSlotView.this.mLayout.mTitleWidth + 0, ListSlotView.this.mLayout.mTitleHeight + y);
            } else {
                int x;
                if (ListSlotView.this.mIsLayoutRtl) {
                    x = ListSlotView.this.getWidth() - ((ListSlotView.this.mLayout.mSlotWidth + ListSlotView.this.mLayout.mSlotWidthGap) * toColumn);
                } else {
                    x = toColumn * (ListSlotView.this.mLayout.mSlotWidth + ListSlotView.this.mLayout.mSlotWidthGap);
                }
                y = (y + ListSlotView.this.mLayout.mTitleHeight) + ((toGroupRow - 1) * (ListSlotView.this.mLayout.mSlotHeight + ListSlotView.this.mLayout.mSlotHeightGap));
                int width = ListSlotView.this.mLayout.mSlotWidth + ListSlotView.this.mLayout.getSizeDelta(toColumn);
                if (ListSlotView.this.mIsLayoutRtl) {
                    rect.set(x - width, y, x, ListSlotView.this.mLayout.mSlotHeight + y);
                    rect.offset(-ListSlotView.this.mLayout.getOffset(toColumn), 0);
                } else {
                    rect.set(x, y, x + width, ListSlotView.this.mLayout.mSlotHeight + y);
                    rect.offset(ListSlotView.this.mLayout.getOffset(toColumn), 0);
                }
            }
            return rect;
        }

        private Rect getSourceWithForce(ItemCoordinate slotIndex) {
            Rect rect = new Rect();
            int toColumn = slotIndex.subIndex % ListSlotView.this.mLayout.mUnitCount;
            int fromRow = (ListSlotView.this.mLayout.getRow(slotIndex) - this.mToRow) + this.mFromRow;
            int fromColumn = (toColumn - this.mToColumn) + this.mFromColumn;
            int fromGroup = this.mFromLayout.getGroupIndex(fromRow);
            int fromGroupRow = this.mFromLayout.getGroupRowIndex(fromRow);
            int y = this.mFromLayout.mGroupCount.getSubContentLen(fromGroup);
            if (slotIndex.isTitle()) {
                rect.set(0, y, this.mFromLayout.mTitleWidth + 0, this.mFromLayout.mTitleHeight + y);
            } else {
                int x;
                if (ListSlotView.this.mIsLayoutRtl) {
                    x = ListSlotView.this.getWidth() - ((this.mFromLayout.mSlotWidth + this.mFromLayout.mSlotWidthGap) * fromColumn);
                } else {
                    x = fromColumn * (this.mFromLayout.mSlotWidth + this.mFromLayout.mSlotWidthGap);
                }
                y = (y + this.mFromLayout.mTitleHeight) + ((fromGroupRow - 1) * (this.mFromLayout.mSlotHeight + this.mFromLayout.mSlotHeightGap));
                int width = this.mFromLayout.mSlotWidth + this.mFromLayout.getSizeDelta(fromColumn);
                if (ListSlotView.this.mIsLayoutRtl) {
                    rect.set(x - width, y, x, this.mFromLayout.mSlotHeight + y);
                    rect.offset(-this.mFromLayout.getOffset(toColumn), 0);
                } else {
                    rect.set(x, y, x + width, this.mFromLayout.mSlotHeight + y);
                    rect.offset(this.mFromLayout.getOffset(toColumn), 0);
                }
            }
            return rect;
        }

        private Rect getTargetRect(Rect to, Rect from) {
            return new Rect((int) (((float) from.left) + (((float) (to.left - from.left)) * this.mProgress)), (int) (((float) from.top) + (((float) (to.top - from.top)) * this.mProgress)), (int) (((float) from.right) + (((float) (to.right - from.right)) * this.mProgress)), (int) (((float) from.bottom) + (((float) (to.bottom - from.bottom)) * this.mProgress)));
        }

        private int getStartAbsIndex() {
            ItemCoordinate index = this.mFromLayout.mStartVisibleCoordinate.clone();
            if (index.isTitle()) {
                index.subIndex++;
            }
            return this.mFromLayout.getAbsSlotIndex(index);
        }

        private int getEndAbsIndex() {
            ItemCoordinate index = this.mFromLayout.mEndVisibleCoordinate.clone();
            if (index.isTitle()) {
                index.subIndex++;
            }
            return this.mFromLayout.getAbsSlotIndex(index);
        }

        private int getAbsIndex() {
            if (ListSlotView.this.mTimeModeChangeItem == null) {
                return getStartAbsIndex();
            }
            return this.mFromLayout.getAbsSlotIndex(ListSlotView.this.mTimeModeChangeItem.clone());
        }

        private int getCenterPoint(int absIndex) {
            Rect rect = this.mFromLayout.getSlotRect(this.mFromLayout.getItemCoordinateByAbsIndex(absIndex), new Rect());
            return (rect.top + rect.bottom) / 2;
        }

        public void updateVisibleRange() {
            int absIndex = getAbsIndex();
            int top = getCenterPoint(absIndex) - ListSlotView.this.mScroller.getPosition();
            Rect rect = ListSlotView.this.mLayout.getSlotRect(ListSlotView.this.mLayout.getItemCoordinateByAbsIndex(absIndex), new Rect());
            ListSlotView.this.setScrollPosition(((rect.top + rect.bottom) / 2) - top);
            initPara(absIndex);
        }

        private void initPara(int forceIndex) {
            ItemCoordinate oldItem = this.mFromLayout.getItemCoordinateByAbsIndex(forceIndex);
            this.mFromRow = this.mFromLayout.getRow(oldItem);
            this.mFromColumn = oldItem.subIndex % this.mFromLayout.mUnitCount;
            this.mToRow = ListSlotView.this.mLayout.getRow(ListSlotView.this.mLayout.getItemCoordinateByAbsIndex(forceIndex));
            float f = ((float) this.mFromColumn) / ((float) this.mFromLayout.mUnitCount);
            float maxDistance = WMElement.CAMERASIZEVALUE1B1;
            for (int i = 0; i < ListSlotView.this.mLayout.mUnitCount; i++) {
                float distance = Math.abs(f - (((float) i) / ((float) ListSlotView.this.mLayout.mUnitCount)));
                if (distance < maxDistance) {
                    this.mToColumn = i;
                    maxDistance = distance;
                }
            }
        }
    }

    private class MyGestureListener implements OnGestureListener {
        private boolean isDown;

        private MyGestureListener() {
            this.isDown = false;
        }

        public void onShowPress(MotionEvent e) {
            GLRoot root = ListSlotView.this.getGLRoot();
            if (!this.isDown) {
                ItemCoordinate index = ListSlotView.this.mIndexDown;
                if (index != null) {
                    this.isDown = true;
                    ListSlotView.this.mListener.onDown(index);
                }
            }
        }

        private synchronized void cancelDown(boolean byLongPress) {
            if (this.isDown) {
                this.isDown = false;
                ListSlotView.this.mListener.onUp(byLongPress);
            }
        }

        public boolean onDown(MotionEvent e) {
            ListSlotView.this.mIndexUp = null;
            e.offsetLocation(0.0f, -ListSlotView.this.getViewOffset());
            ListSlotView.this.mIndexDown = ListSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (ListSlotView.this.mPreviewMode) {
                return true;
            }
            cancelDown(false);
            if (ListSlotView.this.mFirstFling) {
                ListSlotView.this.mFirstFling = false;
            }
            int scrollLimit = ListSlotView.this.mLayout.getScrollLimit();
            if (scrollLimit == 0) {
                return false;
            }
            if (ListSlotView.this.getViewOffset() > 0.0f) {
                return true;
            }
            float velocity = velocityY;
            ListSlotView.this.mSlotScrollBar.enterFastScrollMode(velocityY);
            ListSlotView.this.mScroller.fling((int) (-velocityY), 0, scrollLimit);
            ListSlotView.this.invalidate();
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float distance = distanceY;
            if (ListSlotView.this.mPreviewMode) {
                return true;
            }
            cancelDown(false);
            if (ListSlotView.this.mFirstScroll) {
                ListSlotView.this.mFirstScroll = false;
                ListSlotView.this.mDoSelect = Math.abs(distanceX) > Math.abs(distanceY);
            }
            if (ListSlotView.this.mListener.enableScrollSelection() && ListSlotView.this.mDoSelect) {
                ListSlotView.this.mListener.onScroll(ListSlotView.this.mLayout.getSlotIndexByPosition(e2.getX(), e2.getY() - ListSlotView.this.getViewOffset()));
                ReportToBigData.report(SmsCheckResult.ESCT_176);
            } else {
                if (ListSlotView.this.mScrollOverListener != null && ListSlotView.this.mScrollY == 0) {
                    if (e2.getPointerCount() == 1) {
                        ListSlotView.this.mScrollOverListener.onScrollOver(distanceY);
                    }
                    if (ListSlotView.this.mScrollOverListener.getOffset() > 0.0f) {
                        return true;
                    }
                }
                int overDistance = ListSlotView.this.mScroller.startScroll(Math.round(distanceY), 0, ListSlotView.this.mLayout.getScrollLimit(), ListSlotView.this.mLayout.mHeight - ListSlotView.this.mLayout.mTitleHeight);
                if (ListSlotView.this.mOverscrollEffect == 0 && overDistance != 0) {
                    ListSlotView.this.mPaper.overScroll((float) overDistance);
                }
                ListSlotView.this.invalidate();
            }
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            ListSlotView.this.mIndexUp = ListSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            ListSlotView.this.updateTimeModeChangeForceIndex(e.getX(), e.getY());
            cancelDown(false);
            if (!(ListSlotView.this.mDownInScrolling || ListSlotView.this.mIndexUp == null)) {
                boolean isCornerPressed = false;
                if (ListSlotView.this.mRenderer != null) {
                    isCornerPressed = ListSlotView.this.mRenderer.isCornerPressed(e.getX(), e.getY(), ListSlotView.this.mLayout.getSlotRect(ListSlotView.this.mIndexUp, new Rect()), ListSlotView.this.mLayout.mScrollPosition, ListSlotView.this.mIndexUp.isTitle());
                }
                if (ListSlotView.this.mIndexUp.isTitle() || !ListSlotView.this.mRenderer.onScale(true)) {
                    ListSlotView.this.mListener.onSingleTapUp(ListSlotView.this.mIndexUp, isCornerPressed);
                }
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
            cancelDown(true);
            if (!ListSlotView.this.mDownInScrolling) {
                ListSlotView.this.lockRendering();
                try {
                    e.offsetLocation(0.0f, -ListSlotView.this.getViewOffset());
                    ListSlotView.this.mListener.onLongTap(e);
                } finally {
                    ListSlotView.this.unlockRendering();
                }
            }
        }
    }

    private class MyScaleListener extends SimpleOnScaleGestureListener {
        private float mAccScale;

        private MyScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (ListSlotView.this.mPreviewMode) {
                return false;
            }
            this.mAccScale = WMElement.CAMERASIZEVALUE1B1;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (ListSlotView.this.mPreviewMode) {
                return false;
            }
            this.mAccScale *= detector.getScaleFactor();
            ListSlotView.this.updateTimeModeChangeForceIndex(detector.getFocusX(), detector.getFocusY());
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            if (!ListSlotView.this.mPreviewMode) {
                if ((this.mAccScale > 1.2f || this.mAccScale < 0.8f) && !ListSlotView.this.mSelectionMode) {
                    boolean beBigger = false;
                    if (this.mAccScale > 1.2f) {
                        beBigger = true;
                    } else if (this.mAccScale < 0.8f) {
                        beBigger = false;
                    }
                    ListSlotView.this.mRenderer.onScale(beBigger);
                    if ((ListSlotView.this.mViewMode == TimeBucketPageViewMode.DAY && !beBigger) || (ListSlotView.this.mViewMode == TimeBucketPageViewMode.MONTH && beBigger)) {
                        ReportToBigData.report(13, String.format("{Week_Month:%s}", new Object[]{"TwoFingers"}));
                    }
                }
            }
        }
    }

    public class MyScrollListener implements com.android.gallery3d.ui.SlotScrollBarView.Listener {
        public void updateScrollPosition(int offset, int contentLen) {
            ListSlotView.this.setScrollPosition((int) ((((float) ListSlotView.this.mLayout.getScrollLimit()) * ((float) offset)) / ((float) contentLen)));
            ListSlotView.this.invalidate();
        }

        public void onDown() {
            ListSlotView.this.setScrollPosition(ListSlotView.this.mScrollY);
            ListSlotView.this.invalidate();
        }
    }

    public static class Spec {
        public int slotGap = -1;
        public int slotLandCountByDay = -1;
        public int slotLandCountByMonth = -1;
        public int slotPortCountByDay = -1;
        public int slotPortCountByMonth = -1;
        public int titleHeight = -1;
    }

    public ListSlotView(final GalleryContext activity, Spec slotViewSpec) {
        super(activity.getAndroidContext());
        GalleryContext context = activity;
        ConditionVariable lock = SyncUtils.runWithConditionVariable(new Runnable() {
            public void run() {
                boolean z = true;
                TraceController.beginSection("ScrollerHelper");
                ListSlotView.this.mScroller = new ScrollerHelper(activity.getAndroidContext());
                ScrollerHelper -get14 = ListSlotView.this.mScroller;
                if (ListSlotView.this.mOverscrollEffect != 1) {
                    z = false;
                }
                -get14.setOverfling(z);
                TraceController.endSection();
            }
        });
        this.mGestureDetector = new GestureDetector(activity.getAndroidContext(), new MyGestureListener());
        this.mScaleDetector = new ScaleGestureDetector(activity.getAndroidContext(), new MyScaleListener());
        this.mLayout = new Layout(slotViewSpec);
        this.mCurrentLayout = this.mLayout;
        lock.block();
    }

    public void setScrollBar(SlotScrollBarView scrollBar) {
        this.mSlotScrollBar = scrollBar;
        this.mSlotScrollBar.setScrollListener(new MyScrollListener());
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        this.mRenderer = slotDrawer;
        this.mCurrentSlotRender = slotDrawer;
        if (this.mRenderer != null) {
            this.mRenderer.onSlotSizeChanged(this.mLayout.mSlotWidth, this.mLayout.mSlotHeight);
            this.mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void updateSelectionMode(boolean selectionMode) {
        this.mSelectionMode = selectionMode;
    }

    public boolean isScrolling() {
        return (this.mFirstScroll && this.mFirstFling) ? false : true;
    }

    public void updatePreviewMode(boolean previewMode) {
        this.mPreviewMode = previewMode;
    }

    public Rect getAnimRect() {
        if (!visibleItem(this.mIndexUp)) {
            return null;
        }
        Rect rt = getSlotRect(this.mIndexUp);
        rt.offset(0, -this.mScrollY);
        rt.offset(this.mBounds.left, this.mBounds.top);
        return rt;
    }

    public void setIndexUp(ItemCoordinate indexUp) {
        this.mIndexUp = indexUp;
    }

    public void setHeadCoverHeight(int height) {
        this.mHeadCoverHeight = height;
    }

    public ItemCoordinate getSlotIndexByPosition(float x, float y) {
        return this.mLayout.getSlotIndexByPosition(x, y);
    }

    public Rect getPreviewImageRect(float x, float y) {
        ItemCoordinate index = this.mLayout.getSlotIndexByPosition(x, y);
        if (index == null || index.isTitle()) {
            return null;
        }
        Rect rect = getSlotRect(index);
        int left = (rect.left + this.mBounds.left) - this.mScrollX;
        int top = (rect.top + this.mBounds.top) - this.mScrollY;
        rect.set(left, top, left + rect.width(), top + rect.height());
        return rect;
    }

    public int getSlotWidthGap() {
        return this.mLayout.mSlotWidthGap;
    }

    public int getSlotHeightGap() {
        return this.mLayout.mSlotHeightGap;
    }

    public int getSlotWidthByWeek() {
        int unitCount = this.mLayout.isPort() ? this.mLayout.mSpec.slotPortCountByDay : this.mLayout.mSpec.slotLandCountByDay;
        return (this.mLayout.mWidth - (this.mLayout.mSpec.slotGap * (unitCount - 1))) / unitCount;
    }

    public int getSlotHeightByWeek() {
        return getSlotWidthByWeek();
    }

    public int getSlotWidthByMonth() {
        int unitCount = this.mLayout.isPort() ? this.mLayout.mSpec.slotPortCountByMonth : this.mLayout.mSpec.slotLandCountByMonth;
        return (this.mLayout.mWidth - (this.mLayout.mSpec.slotGap * (unitCount - 1))) / unitCount;
    }

    public int getSlotHeightByMonth() {
        return getSlotWidthByMonth();
    }

    private boolean visibleItem(ItemCoordinate index) {
        boolean z = false;
        if (index == null) {
            return false;
        }
        if (this.mLayout.mStartVisibleCoordinate.isSmall(index)) {
            z = index.isSmall(this.mLayout.mEndVisibleCoordinate);
        }
        return z;
    }

    public void setCenterIndex(ItemCoordinate startIndex) {
        if (!this.mLayout.mGroupCount.invalidItem(startIndex)) {
            Rect rect = this.mLayout.getSlotRect(startIndex, this.mTempRect);
            setScrollPosition(((rect.top + rect.bottom) - getHeight()) / 2);
        }
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, this.mLayout.getScrollLimit());
        this.mScroller.setPosition(position);
        updateScrollPosition(position, false);
        if (this.mSlotScrollBar != null) {
            this.mSlotScrollBar.updateContentOffset(position);
        }
    }

    public TimeBucketPageViewMode getViewMode() {
        return this.mViewMode;
    }

    public boolean beBiggerView() {
        return this.mBeBiggerView;
    }

    public void addComponent(GLView view) {
        throw new UnsupportedOperationException();
    }

    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (changeSize) {
            GalleryLog.printDFXLog("ListSlotView onLayout  for DFX");
            this.mLayout.setSize(r - l, b - t);
            setScrollPosition(this.mLayout.mScrollPosition);
            if (this.mOverscrollEffect == 0) {
                this.mPaper.setSize(r - l, b - t);
            }
        }
    }

    public void clearAnimation() {
        super.clearAnimation();
        boolean invalidate = false;
        if (this.mShiftAnimation != null) {
            this.mShiftAnimation.forceStop();
            invalidate = true;
        }
        if (invalidate) {
            invalidate();
        }
    }

    public boolean isAnimating() {
        return this.mShiftAnimation != null ? this.mShiftAnimation.isAnimating() : false;
    }

    private void updateScrollPosition(int position, boolean force) {
        if (this.mShiftAnimation != null && this.mShiftAnimation.isActive() && this.mViewMode == TimeBucketPageViewMode.DAY) {
            this.mRenderer.onVisibleRangeChanged(this.mLayout.getItemCoordinateByAbsIndex(this.mShiftAnimation.getStartAbsIndex()), this.mLayout.getItemCoordinateByAbsIndex(this.mShiftAnimation.getEndAbsIndex()), getVisibleStart(), getVisibleEnd());
        }
        if (force || position != this.mScrollY) {
            this.mScrollY = position;
            this.mLayout.setScrollPosition(position);
        }
    }

    public Rect getSlotRect(ItemCoordinate itemCoordinate) {
        return this.mLayout.getSlotRect(itemCoordinate, new Rect());
    }

    private float getViewOffset() {
        return this.mScrollOverListener != null ? this.mScrollOverListener.getOffset() : 0.0f;
    }

    protected boolean onTouch(MotionEvent event) {
        boolean z = false;
        float viewOffset = getViewOffset();
        if (viewOffset > 0.0f) {
            if (this.mScrollY != 0) {
                this.mScrollOverListener.onScrollOver(WMElement.CAMERASIZEVALUE1B1);
            }
            int eventAction = event.getAction();
            if (eventAction == 3 || eventAction == 1) {
                this.mScrollOverListener.onScrollOverDone();
            } else {
                event.offsetLocation(0.0f, viewOffset);
            }
        }
        if (event.getAction() == 0) {
            event.setDownTime(event.getEventTime());
        }
        this.mGestureDetector.onTouchEvent(event);
        this.mScaleDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case 0:
                GalleryLog.d("ListSlotView", "action_down: pressure value:" + event.getPressure());
                this.mFirstScroll = true;
                this.mFirstFling = true;
                this.mDoSelect = false;
                if (!this.mScroller.isFinished()) {
                    z = true;
                }
                this.mDownInScrolling = z;
                this.mScroller.forceFinished();
                this.mListener.onTouchDown(event);
                if (this.mScrollOverListener != null && this.mScrollY == 0) {
                    this.mScrollOverListener.onScrollOverBegin();
                    break;
                }
            case 1:
                this.mFirstScroll = true;
                this.mFirstFling = true;
                this.mDoSelect = false;
                this.mPaper.onRelease();
                this.mListener.onTouchUp(event);
                this.mScroller.release(this.mLayout.getScrollLimit());
                invalidate();
                break;
            case 2:
                event.offsetLocation(0.0f, -viewOffset);
                this.mListener.onMove(event);
                invalidate();
                break;
            case 3:
                this.mPaper.onRelease();
                this.mScroller.release(this.mLayout.getScrollLimit());
                this.mListener.onCancel();
                invalidate();
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setScrollOverListener(ScrollOverListener listener) {
        this.mScrollOverListener = listener;
    }

    private void getTitleOffset(ItemCoordinate index) {
        ItemCoordinate itemCoordinate = this.mLayout.getSlotIndexByScrollPositionY(this.mLayout.mTitleHeight - TITLE_TOP_OFFSET);
        if (itemCoordinate != null) {
            if (itemCoordinate.isTitle()) {
                index.group = Math.max(0, itemCoordinate.group - 1);
            } else {
                index.group = itemCoordinate.group;
            }
            index.subIndex = -1;
        }
    }

    private void renderTopTitle(GLCanvas canvas, boolean isScrolling) {
        int offsetY = (this.mScroller.getPosition() - this.mHeadCoverHeight) + (this.mListener == null ? 0 : this.mListener.getOverflowMarginTop());
        if (offsetY > 0) {
            ItemCoordinate index = new ItemCoordinate(0, -1);
            getTitleOffset(index);
            if (this.mListener != null) {
                this.mRenderer.renderTopTitleOverflow(canvas, index, isScrolling, offsetY, this.mListener.getOverflowMarginTop());
            }
        }
    }

    protected void render(GLCanvas canvas) {
        super.render(canvas);
        if (this.mRenderer != null) {
            ItemCoordinate renderCoordinate;
            ItemCoordinate startCoordinate;
            this.mRenderer.prepareDrawing();
            long animTime = AnimationTime.get();
            boolean isScrolling = this.mScroller.advanceAnimation(animTime);
            boolean more = isScrolling;
            if (this.mSlotScrollBar != null) {
                if (this.mScroller.getPosition() > this.mLayout.getScrollLimit()) {
                    this.mSlotScrollBar.updateContentLen(this.mScroller.getPosition());
                }
                if (isScrolling) {
                    this.mSlotScrollBar.show();
                } else {
                    this.mSlotScrollBar.hide();
                }
            }
            int oldX = this.mScrollY;
            updateScrollPosition(this.mScroller.getPosition(), false);
            boolean paperActive = false;
            if (this.mOverscrollEffect == 0) {
                int newX = this.mScrollY;
                int limit = this.mLayout.getScrollLimit();
                if (oldX <= 0 || newX != 0) {
                    if (oldX < limit && newX == limit) {
                    }
                    paperActive = this.mPaper.advanceAnimation();
                }
                float v = this.mScroller.getCurrVelocity();
                if (newX == limit) {
                    v = -v;
                }
                if (!Float.isNaN(v)) {
                    this.mPaper.edgeReached(v);
                }
                paperActive = this.mPaper.advanceAnimation();
            }
            more = (isScrolling | paperActive) | needRenderSlotAnimationMore(animTime);
            if (this.mShiftAnimation != null) {
                boolean result = this.mShiftAnimation.calculate(animTime);
                if (!result) {
                    this.mShiftAnimation = null;
                    this.mRenderer.onSlotSizeChanged(this.mLayout.mSlotWidth, this.mLayout.mSlotHeight);
                }
                more |= result;
            }
            GL11 gl = canvas.getGLInstance();
            gl.glEnable(2960);
            gl.glClear(1024);
            gl.glStencilOp(7680, 7680, 7681);
            gl.glStencilFunc(519, 1, 1);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 0);
            gl.glStencilFunc(514, 1, 1);
            gl.glStencilOp(7680, 7680, 7681);
            canvas.translate(0.0f, (float) (-this.mScrollY));
            renderDeletedItem(canvas);
            if (this.mShiftAnimation != null && this.mShiftAnimation.isActive()) {
                renderCoordinate = this.mShiftAnimation.mFromLayout.mEndVisibleCoordinate.clone();
                startCoordinate = this.mShiftAnimation.mFromLayout.mStartVisibleCoordinate.clone();
                while (startCoordinate.isSmall(renderCoordinate)) {
                    renderItemFromTo(canvas, renderCoordinate, paperActive, isScrolling);
                    if (!this.mShiftAnimation.mFromLayout.mGroupCount.moveToLast(renderCoordinate)) {
                        break;
                    }
                }
            }
            renderCoordinate = this.mLayout.mEndVisibleCoordinate.clone();
            startCoordinate = this.mLayout.mStartVisibleCoordinate.clone();
            while (startCoordinate.isSmall(renderCoordinate)) {
                renderItemToFrom(canvas, renderCoordinate, paperActive, isScrolling);
                if (!this.mLayout.mGroupCount.moveToLast(renderCoordinate)) {
                    break;
                }
            }
            canvas.translate(0.0f, (float) this.mScrollY);
            renderTopTitle(canvas, isScrolling);
            gl.glDisable(2960);
            if (this.mListener != null) {
                this.mListener.renderHeadCover(canvas, 0, -this.mScrollY, getWidth(), this.mHeadCoverHeight - this.mScrollY);
            }
            if (more) {
                invalidate();
            }
        }
    }

    private void renderDeletedItem(GLCanvas canvas) {
        DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
        if (deleteSlotAnimation != null) {
            HashMap<Path, Object> visiblePathEntryMap = deleteSlotAnimation.getVisibleItemPathMap();
            HashMap<Object, Object> visibleIndexEntryMap = deleteSlotAnimation.getVisibleItemIndexMap();
            if (visiblePathEntryMap != null && visibleIndexEntryMap != null) {
                Layout fromLayout = (Layout) deleteSlotAnimation.getFromLayout();
                if (fromLayout != null) {
                    BaseEntry baseEntry;
                    ItemCoordinate renderIndex = this.mLayout.getVisibleEnd().clone();
                    ItemCoordinate startIndex = this.mLayout.getVisibleStart().clone();
                    while (renderIndex.isLarge(startIndex) && this.mLayout.mContentLength > 0) {
                        if (startIndex.isTitle()) {
                            baseEntry = (BaseEntry) visibleIndexEntryMap.get(startIndex);
                            if (baseEntry != null) {
                                baseEntry.guessDeleted = false;
                            }
                        } else {
                            Path targetPath = this.mRenderer.getItemPath(startIndex);
                            if (targetPath != null) {
                                baseEntry = (BaseEntry) visiblePathEntryMap.get(targetPath);
                                if (baseEntry != null) {
                                    baseEntry.guessDeleted = false;
                                }
                            }
                        }
                        if (!this.mLayout.mGroupCount.moveToNext(startIndex)) {
                            break;
                        }
                    }
                    renderIndex = fromLayout.getVisibleEnd().clone();
                    startIndex = fromLayout.getVisibleStart().clone();
                    while (renderIndex.isLarge(startIndex)) {
                        baseEntry = (BaseEntry) visibleIndexEntryMap.get(startIndex);
                        if (baseEntry != null && baseEntry.guessDeleted) {
                            Rect rect = fromLayout.getSlotRect(startIndex, this.mTempRect);
                            rect.set(rect.left, (rect.top - fromLayout.mScrollPosition) + this.mLayout.mScrollPosition, rect.right, (rect.bottom - fromLayout.mScrollPosition) + this.mLayout.mScrollPosition);
                            canvas.save(3);
                            canvas.setAlpha(deleteSlotAnimation.getAlpha());
                            if (!startIndex.isTitle()) {
                                deleteSlotAnimation.applyDeletedItem(rect);
                            }
                            canvas.translate((float) rect.left, (float) rect.top, 0.0f);
                            this.mRenderer.renderSlot(canvas, baseEntry, startIndex, false, false, rect.width(), rect.height());
                            canvas.restore();
                        }
                        if (!fromLayout.mGroupCount.moveToNext(startIndex)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private int renderItemFromTo(GLCanvas canvas, ItemCoordinate index, boolean paperActive, boolean isScrolling) {
        Rect rect = new Rect();
        if (!this.mShiftAnimation.mFromLayout.isValidItem(index)) {
            return 0;
        }
        ItemCoordinate item;
        boolean z;
        canvas.save(3);
        this.mShiftAnimation.applyFromTo(canvas, index, rect);
        if (paperActive) {
            canvas.multiplyMatrix(this.mPaper.getTransform(rect, (float) this.mScrollY), 0);
        } else {
            canvas.translate((float) rect.left, (float) rect.top, 0.0f);
        }
        if (index.isTitle()) {
            item = index;
        } else {
            item = this.mLayout.getItemCoordinateByAbsIndex(this.mShiftAnimation.mFromLayout.getAbsSlotIndex(index));
        }
        SlotRenderer slotRenderer = this.mRenderer;
        int i = rect.right - rect.left;
        int i2 = rect.bottom - rect.top;
        if (this.mShiftAnimation.isActive()) {
            z = false;
        } else {
            z = true;
        }
        int result = slotRenderer.renderSlot(canvas, item, false, isScrolling, i, i2, true, z);
        canvas.restore();
        return result;
    }

    private int renderItemToFrom(GLCanvas canvas, ItemCoordinate index, boolean paperActive, boolean isScrolling) {
        if (!this.mLayout.isValidItem(index)) {
            return 0;
        }
        canvas.save(3);
        Rect rect = this.mLayout.getSlotRect(index, this.mTempRect);
        if (this.mShiftAnimation != null && this.mShiftAnimation.isActive()) {
            this.mShiftAnimation.applyToFrom(canvas, index, rect);
        }
        if (!paperActive || index.isTitle()) {
            DownShiftAnimation downShiftAnimation = this.mDownShiftAnimation;
            if (!(downShiftAnimation == null || index.isTitle())) {
                downShiftAnimation.apply(index, rect);
            }
            DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
            if (deleteSlotAnimation != null) {
                deleteSlotAnimation.apply(index, rect);
            }
            canvas.translate((float) rect.left, (float) rect.top, 0.0f);
        } else {
            canvas.multiplyMatrix(this.mPaper.getTransform(rect, (float) this.mScrollY), 0);
        }
        SlotRenderer slotRenderer = this.mRenderer;
        int i = rect.right - rect.left;
        int i2 = rect.bottom - rect.top;
        boolean z = this.mShiftAnimation == null || !this.mShiftAnimation.isActive();
        int result = slotRenderer.renderSlot(canvas, index, false, isScrolling, i, i2, false, z);
        canvas.restore();
        return result;
    }

    public void transformRect(Object index, Object downIndex, float progress, Rect target) {
        this.mLayout.transformRect((ItemCoordinate) index, (ItemCoordinate) downIndex, progress, target);
    }

    public float getScaleFactor() {
        return 0.935f;
    }

    private void updateTimeModeChangeForceIndex(float x, float y) {
        if (this.mIndexUp != null) {
            this.mTimeModeChangeItem = this.mIndexUp.clone();
        }
        while (x > 0.0f && y > 0.0f) {
            ItemCoordinate item = this.mLayout.getSlotIndexByPosition(x, y);
            if (item != null) {
                this.mTimeModeChangeItem = item.clone();
                if (item.isTitle()) {
                    this.mTimeModeChangeItem.subIndex = 0;
                    return;
                }
                return;
            }
            x = (x - ((float) this.mLayout.mSlotWidth)) - ((float) this.mLayout.mSlotWidthGap);
        }
    }

    public boolean updateCountAndMode(ArrayList<AbsGroupData> groupDatas, TimeBucketPageViewMode mode) {
        boolean forceInit = false;
        if (this.mViewMode != mode) {
            forceInit = true;
        }
        if (forceInit && this.mLayout.mGroupCount.size() > 0) {
            this.mShiftAnimation = new ModeShiftAnimation();
            this.mShiftAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd() {
                    ListSlotView.this.mRenderer.clearOldTitleData();
                }
            });
            this.mBeBiggerView = this.mViewMode.beBiggerView(mode);
            this.mShiftAnimation.start();
            if (this.mListener != null) {
                this.mListener.onResetMainView();
            }
        }
        this.mViewMode = mode;
        ArrayList<Integer> group = new ArrayList();
        int groupCount = groupDatas != null ? groupDatas.size() : 0;
        for (int i = 0; i < groupCount; i++) {
            group.add(Integer.valueOf(((AbsGroupData) groupDatas.get(i)).count));
        }
        boolean changed = this.mLayout.setSlotCount(group, forceInit);
        if (!forceInit || this.mShiftAnimation == null) {
            setScrollPosition(this.mScrollY);
        } else {
            this.mShiftAnimation.updateVisibleRange();
        }
        return changed;
    }

    public ItemCoordinate getItemCoordinate(int index) {
        return this.mLayout.getItemCoordinateByAbsIndex(index);
    }

    public ItemCoordinate getVisibleStart() {
        return this.mLayout.getVisibleStart();
    }

    public ItemCoordinate getVisibleEnd() {
        return this.mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return this.mScrollX;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public AbsLayout cloneLayout() {
        return this.mLayout.clone();
    }

    public SlotUIListener getSlotUIListener() {
        return this.mListener;
    }
}
