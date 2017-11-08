package com.huawei.gallery.story.ui;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.ui.CommonAlbumSetSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class StoryAlbumSetSlotView extends CommonAlbumSetSlotView {
    private final TextPaint mAlbumBiggerPaint;
    private final TextPaint mAlbumFullPaint;
    private final TextPaint mAlbumSmallerPaint;

    public class Layout extends com.huawei.gallery.ui.CommonAlbumSlotView.Layout {
        private ArrayList<Integer> mDataType = new ArrayList();
        private boolean mIsPort = true;
        private ArrayList<Integer> mLevelColumnsLand = new ArrayList();
        private ArrayList<Integer> mLevelColumnsPort = new ArrayList();
        private ArrayList<Integer> mLevelSumLand = new ArrayList();
        private ArrayList<Integer> mLevelSumPort = new ArrayList();
        private HashMap<Integer, Rect> mRects = new HashMap();
        private ArrayList<Integer> mSlotLengthLand = new ArrayList();
        private ArrayList<Integer> mSlotLengthPort = new ArrayList();

        public Layout(Spec spec) {
            super(spec);
        }

        protected Layout clone() {
            Layout layout = new Layout(this.mSpec);
            copyAllParameters(layout);
            layout.mVisibleStart = this.mVisibleStart;
            layout.mVisibleEnd = this.mVisibleEnd;
            layout.mSlotCount = this.mSlotCount;
            layout.mVerticalPadding = this.mVerticalPadding;
            layout.mHorizontalPadding = this.mHorizontalPadding;
            layout.mDataType = cloneList(this.mDataType);
            layout.mLevelColumnsPort = cloneList(this.mLevelColumnsPort);
            layout.mLevelSumPort = cloneList(this.mLevelSumPort);
            layout.mSlotLengthPort = cloneList(this.mSlotLengthPort);
            layout.mLevelColumnsLand = cloneList(this.mLevelColumnsLand);
            layout.mLevelSumLand = cloneList(this.mLevelSumLand);
            layout.mSlotLengthLand = cloneList(this.mSlotLengthLand);
            layout.mRects = new HashMap();
            for (Entry<Integer, Rect> entry : this.mRects.entrySet()) {
                int key = ((Integer) entry.getKey()).intValue();
                layout.mRects.put(Integer.valueOf(key), (Rect) this.mRects.get(Integer.valueOf(key)));
            }
            layout.mIsPort = this.mIsPort;
            return layout;
        }

        private ArrayList<Integer> cloneList(ArrayList<Integer> src) {
            ArrayList<Integer> result = new ArrayList();
            for (Integer integer : src) {
                result.add(integer);
            }
            return result;
        }

        private boolean isDataDataTypeChange(ArrayList<Integer> dataType) {
            if (dataType.size() != this.mDataType.size()) {
                return true;
            }
            for (int i = 0; i < dataType.size(); i++) {
                if (this.mDataType.get(i) != dataType.get(i)) {
                    return true;
                }
            }
            return false;
        }

        public void updateDataType(ArrayList<Integer> dataType) {
            if (isDataDataTypeChange(dataType)) {
                this.mDataType.clear();
                for (int i = 0; i < dataType.size(); i++) {
                    this.mDataType.add((Integer) dataType.get(i));
                }
                updateColumns();
            }
        }

        private int getLimitLength() {
            if (isPort()) {
                return this.mWidth;
            }
            return (this.mWidth - this.mSlotWidthGap) / 2;
        }

        private void initSlotRect() {
            this.mRects.clear();
            for (int index = 0; index < this.mSlotCount; index++) {
                int row = getRowByIndex(index);
                int col = getColByIndex(index, row);
                Rect rect = new Rect();
                int x = getX(row, col);
                int y = row * (this.mSlotHeight + this.mSlotHeightGap);
                if (GalleryUtils.isLayoutRTL()) {
                    x = this.mWidth - x;
                    rect.set(x - getSlotWidth(row, col), y, x, this.mSlotHeight + y);
                } else {
                    rect.set(x, y, getSlotWidth(row, col) + x, this.mSlotHeight + y);
                }
                GalleryLog.d("StoryAlbumSetSlotView", "index = " + index + ", rect = " + rect);
                this.mRects.put(Integer.valueOf(index), rect);
            }
        }

        protected void onInitLayoutfinish(com.huawei.gallery.ui.CommonAlbumSlotView.Layout oldLayout) {
            apportionMargin();
            if (StoryAlbumSetSlotView.this.mSlotRenderer != null) {
                StoryAlbumSetSlotView.this.mSlotRenderer.onSlotSizeChanged(this);
            }
            if (!updatePositionWithFocus(oldLayout)) {
                updateVisibleSlotRange();
            }
            if (StoryAlbumSetSlotView.this.mSlotScrollBar != null) {
                StoryAlbumSetSlotView.this.mSlotScrollBar.updateContentLen(StoryAlbumSetSlotView.this.mLayout.getScrollLimit());
            }
        }

        protected void initLayoutParameters(com.huawei.gallery.ui.CommonAlbumSlotView.Layout oldLayout) {
            this.mSlotWidthGap = this.mSpec.slot_gap;
            this.mSlotHeightGap = this.mSlotWidthGap;
            int count;
            if (isPort()) {
                this.mUnitCount = this.mSpec.port_slot_count;
                this.mSlotHeight = (this.mWidth - this.mSlotWidthGap) / 2;
                this.mSlotWidth = (this.mWidth - ((this.mUnitCount - 1) * this.mSlotWidthGap)) / this.mUnitCount;
                count = this.mLevelColumnsPort.size();
                this.mContentLength = (this.mSlotHeight * count) + ((count + 1) * this.mSlotHeightGap);
                this.mIsPort = true;
            } else {
                this.mUnitCount = this.mSpec.land_slot_count;
                this.mSlotHeight = (this.mHeight - this.mSlotHeightGap) / 2;
                this.mSlotWidth = (this.mWidth - ((this.mUnitCount - 1) * this.mSlotWidthGap)) / this.mUnitCount;
                count = this.mLevelColumnsLand.size();
                this.mContentLength = (this.mSlotHeight * count) + ((count + 1) * this.mSlotHeightGap);
                this.mIsPort = false;
            }
            initSlotRect();
            onInitLayoutfinish(oldLayout);
        }

        private int getIntArray(ArrayList<Integer> src, int index) {
            if (index >= src.size() || index < 0) {
                return 0;
            }
            return ((Integer) src.get(index)).intValue();
        }

        private ArrayList<Integer> getLevelSum() {
            return this.mIsPort ? this.mLevelSumPort : this.mLevelSumLand;
        }

        private ArrayList<Integer> getLevelColumns() {
            return this.mIsPort ? this.mLevelColumnsPort : this.mLevelColumnsLand;
        }

        private ArrayList<Integer> getSlotLength() {
            return this.mIsPort ? this.mSlotLengthPort : this.mSlotLengthLand;
        }

        protected void updateVisibleSlotRange() {
            ArrayList<Integer> levelSum = getLevelSum();
            int position = this.mScrollPosition;
            int startRow = position / (this.mSlotHeight + this.mSlotHeightGap);
            int start = Math.max(0, getIntArray(levelSum, startRow - 1));
            int endRow = ((((this.mHeight + position) + this.mSlotHeight) + this.mSlotHeightGap) - 1) / (this.mSlotHeight + this.mSlotHeightGap);
            int flag = getIntArray(levelSum, endRow - 1);
            int i = this.mSlotCount;
            if (flag == 0) {
                flag = start + (this.mUnitCount * (endRow - startRow));
            }
            setVisibleRange(start, Math.min(i, flag));
        }

        public Rect getSlotRect(int index, Rect rect) {
            Rect ret = (Rect) this.mRects.get(Integer.valueOf(index));
            if (ret != null) {
                rect.set(ret);
            } else {
                rect.set(0, 0, 0, 0);
            }
            return rect;
        }

        public TextPaint getTextPaint(int index) {
            Rect rect = getSlotRect(index, StoryAlbumSetSlotView.this.mTempRect);
            float width = (float) rect.width();
            float height = (float) rect.height();
            if (Float.compare(width, 0.0f) == 0 || Float.compare(height, 0.0f) == 0) {
                return null;
            }
            float scale = width / height;
            if (scale < WMElement.CAMERASIZEVALUE1B1) {
                return StoryAlbumSetSlotView.this.mAlbumSmallerPaint;
            }
            if (scale > 2.0f) {
                return this.mIsPort ? StoryAlbumSetSlotView.this.mAlbumFullPaint : StoryAlbumSetSlotView.this.mAlbumBiggerPaint;
            }
            return this.mIsPort ? StoryAlbumSetSlotView.this.mAlbumBiggerPaint : StoryAlbumSetSlotView.this.mAlbumSmallerPaint;
        }

        public int getSlotIndexByPosition(float x, float y) {
            if (this.mDataType.size() == 0) {
                return -1;
            }
            int absoluteX = Math.round(x);
            int absoluteY = Math.round(y) + this.mScrollPosition;
            if (absoluteX < 0 || absoluteY < 0) {
                return -1;
            }
            if (StoryAlbumSetSlotView.this.mIsLayoutRtl) {
                absoluteX = this.mWidth - absoluteX;
            }
            ArrayList<Integer> levelSum = getLevelSum();
            int rowIdx = absoluteY / (this.mSlotHeight + this.mSlotHeightGap);
            int dx = 0;
            int start = getIntArray(levelSum, rowIdx - 1);
            int end = getIntArray(levelSum, rowIdx);
            for (int i = 0; i < end - start; i++) {
                dx += getSlotWidth(rowIdx, i);
                if (dx > absoluteX) {
                    return i + start;
                }
                dx += this.mSlotWidthGap;
            }
            return -1;
        }

        private int getRowByIndex(int index) {
            ArrayList<Integer> levelColumns = getLevelColumns();
            int tag = index;
            for (int i = 0; i < levelColumns.size(); i++) {
                if (tag < ((Integer) levelColumns.get(i)).intValue()) {
                    return i;
                }
                tag -= ((Integer) levelColumns.get(i)).intValue();
            }
            return -1;
        }

        private int getColByIndex(int index, int row) {
            if (row == -1) {
                row = getRowByIndex(index);
            }
            if (row == 0) {
                return index;
            }
            return index - getIntArray(getLevelSum(), row - 1);
        }

        private int getX(int row, int col) {
            int x = (this.mSlotWidthGap * col) + 0;
            for (int i = 0; i < col; i++) {
                x += getSlotWidth(row, i);
            }
            return x;
        }

        private int getSlotWidth(int row, int col) {
            int count = 0;
            ArrayList<Integer> levelSum = getLevelSum();
            ArrayList<Integer> slotLength = getSlotLength();
            ArrayList<Integer> levelColumns = getLevelColumns();
            int start = getIntArray(levelSum, row - 1);
            int end = getIntArray(levelSum, row);
            int size = end - start;
            if (size == 0 || col >= size) {
                return 0;
            }
            for (int i = start; i < end; i++) {
                count += ((Integer) slotLength.get(i)).intValue();
            }
            int slotWidth = 0;
            int tag = ((Integer) slotLength.get(start + col)).intValue();
            if (this.mIsPort) {
                if (count != 3 || ((Integer) levelColumns.get(row)).intValue() != 2) {
                    slotWidth = ((this.mWidth - ((size - 1) * this.mSlotWidthGap)) / count) * tag;
                } else if (tag == 2) {
                    slotWidth = ((this.mWidth * 2) / 3) - (this.mSlotWidthGap / 3);
                } else if (tag == 1) {
                    slotWidth = (this.mWidth / 3) - ((this.mSlotWidthGap * 2) / 3);
                }
            } else if (count != 4 || ((Integer) levelColumns.get(row)).intValue() != 3) {
                slotWidth = ((this.mWidth - ((size - 1) * this.mSlotWidthGap)) / count) * tag;
            } else if (tag == 2) {
                slotWidth = (this.mWidth / 2) - (this.mSlotWidthGap / 2);
            } else if (tag == 1) {
                slotWidth = (this.mWidth / 4) - ((this.mSlotWidthGap * 3) / 4);
            }
            return Math.min(getLimitLength(), slotWidth);
        }

        private void clearParams() {
            this.mSlotLengthPort.clear();
            this.mLevelColumnsPort.clear();
            this.mLevelSumPort.clear();
            this.mSlotLengthLand.clear();
            this.mLevelColumnsLand.clear();
            this.mLevelSumLand.clear();
        }

        public boolean setSlotCount(int slotCount) {
            if (slotCount == this.mSlotCount) {
                initLayoutParameters(null);
            }
            return super.setSlotCount(slotCount);
        }

        private void updateColumns() {
            clearParams();
            int i = 0;
            while (i < this.mDataType.size()) {
                if (i == 0) {
                    this.mLevelColumnsPort.add(Integer.valueOf(1));
                    this.mLevelSumPort.add(Integer.valueOf(1));
                    this.mSlotLengthPort.add(Integer.valueOf(1));
                    i++;
                } else if (i == 1) {
                    this.mLevelColumnsPort.add(Integer.valueOf(1));
                    this.mLevelSumPort.add(Integer.valueOf(((Integer) this.mLevelSumPort.get(this.mLevelSumPort.size() - 1)).intValue() + 1));
                    this.mSlotLengthPort.add(Integer.valueOf(1));
                    i++;
                } else {
                    int column = calculatePort(i);
                    this.mLevelColumnsPort.add(Integer.valueOf(column));
                    this.mLevelSumPort.add(Integer.valueOf(((Integer) this.mLevelSumPort.get(this.mLevelSumPort.size() - 1)).intValue() + column));
                    i += column;
                }
            }
            i = 0;
            while (i < this.mDataType.size()) {
                if (i != 0) {
                    column = calculateLand(i);
                    this.mLevelColumnsLand.add(Integer.valueOf(column));
                    this.mLevelSumLand.add(Integer.valueOf(((Integer) this.mLevelSumLand.get(this.mLevelSumLand.size() - 1)).intValue() + column));
                    i += column;
                } else if (this.mDataType.size() < 2) {
                    this.mLevelColumnsLand.add(Integer.valueOf(1));
                    this.mLevelSumLand.add(Integer.valueOf(1));
                    this.mSlotLengthLand.add(Integer.valueOf(1));
                    i++;
                } else {
                    this.mLevelColumnsLand.add(Integer.valueOf(2));
                    this.mLevelSumLand.add(Integer.valueOf(2));
                    this.mSlotLengthLand.add(Integer.valueOf(1));
                    this.mSlotLengthLand.add(Integer.valueOf(1));
                    i += 2;
                }
            }
        }

        private int calculatePort(int i) {
            int a0 = getDataType(i);
            int a1 = getDataType(i + 1);
            int a2 = getDataType(i + 2);
            if (a0 == 0) {
                return 0;
            }
            if (a1 == 0) {
                this.mSlotLengthPort.add(i, Integer.valueOf(1));
                return 1;
            } else if (a2 != 0) {
                return reallyCaculatePort(i, a0, a1, a2);
            } else {
                if (a0 == a1 && a0 == 1) {
                    this.mSlotLengthPort.add(i, Integer.valueOf(1));
                    return 1;
                }
                if (a0 == 1) {
                    this.mSlotLengthPort.add(i, Integer.valueOf(2));
                    this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                } else if (a1 == 1) {
                    this.mSlotLengthPort.add(i, Integer.valueOf(1));
                    this.mSlotLengthPort.add(i + 1, Integer.valueOf(2));
                } else {
                    this.mSlotLengthPort.add(i, Integer.valueOf(1));
                    this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                }
                return 2;
            }
        }

        private int reallyCaculatePort(int i, int a0, int a1, int a2) {
            if (a0 == 1) {
                if (a1 != 1) {
                    this.mSlotLengthPort.add(i, Integer.valueOf(2));
                    this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                    return 2;
                } else if (a2 == 1) {
                    this.mSlotLengthPort.add(i, Integer.valueOf(1));
                    this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                    return 2;
                } else {
                    this.mSlotLengthPort.add(i, Integer.valueOf(1));
                    return 1;
                }
            } else if (a1 == 1) {
                this.mSlotLengthPort.add(i, Integer.valueOf(1));
                this.mSlotLengthPort.add(i + 1, Integer.valueOf(2));
                return 2;
            } else if (a2 == 1) {
                this.mSlotLengthPort.add(i, Integer.valueOf(1));
                this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                return 2;
            } else {
                this.mSlotLengthPort.add(i, Integer.valueOf(1));
                this.mSlotLengthPort.add(i + 1, Integer.valueOf(1));
                this.mSlotLengthPort.add(i + 2, Integer.valueOf(1));
                return 3;
            }
        }

        private int calculateLand(int i) {
            int a0 = getDataType(i);
            int a1 = getDataType(i + 1);
            int a2 = getDataType(i + 2);
            if (a0 == 0) {
                return 0;
            }
            if (a1 == 0) {
                this.mSlotLengthLand.add(i, Integer.valueOf(1));
                return 1;
            } else if (a2 != 0) {
                return reallyCaculateLand(i, a0, a1, a2);
            } else {
                this.mSlotLengthLand.add(i, Integer.valueOf(1));
                this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                return 2;
            }
        }

        private int reallyCaculateLand(int i, int a0, int a1, int a2) {
            if (a0 != 1) {
                if (a1 == 1) {
                    if (a2 == 1) {
                        this.mSlotLengthLand.add(i, Integer.valueOf(1));
                        this.mSlotLengthLand.add(i + 1, Integer.valueOf(2));
                        this.mSlotLengthLand.add(i + 2, Integer.valueOf(2));
                    } else {
                        this.mSlotLengthLand.add(i, Integer.valueOf(1));
                        this.mSlotLengthLand.add(i + 1, Integer.valueOf(2));
                        this.mSlotLengthLand.add(i + 2, Integer.valueOf(1));
                    }
                } else if (a2 == 1) {
                    this.mSlotLengthLand.add(i, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 2, Integer.valueOf(2));
                } else {
                    this.mSlotLengthLand.add(i, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 2, Integer.valueOf(1));
                }
                return 3;
            } else if (a1 != 1) {
                if (a2 == 1) {
                    this.mSlotLengthLand.add(i, Integer.valueOf(2));
                    this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 2, Integer.valueOf(2));
                } else {
                    this.mSlotLengthLand.add(i, Integer.valueOf(2));
                    this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                    this.mSlotLengthLand.add(i + 2, Integer.valueOf(1));
                }
                return 3;
            } else if (a2 == 1) {
                this.mSlotLengthLand.add(i, Integer.valueOf(1));
                this.mSlotLengthLand.add(i + 1, Integer.valueOf(1));
                return 2;
            } else {
                this.mSlotLengthLand.add(i, Integer.valueOf(2));
                this.mSlotLengthLand.add(i + 1, Integer.valueOf(2));
                this.mSlotLengthLand.add(i + 2, Integer.valueOf(1));
                return 3;
            }
        }

        private int getDataType(int index) {
            if (index >= this.mDataType.size() || index < 0) {
                return 0;
            }
            return ((Integer) this.mDataType.get(index)).intValue();
        }
    }

    public StoryAlbumSetSlotView(GalleryContext activity, Spec spec) {
        super(activity, spec);
        Resources res = activity.getResources();
        this.mAlbumFullPaint = StringTexture.getDefaultPaint((float) res.getDimensionPixelSize(R.dimen.story_tag_albumSet_main_name_size_big), res.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumFullPaint.setShadowLayer(12.0f, 0.0f, 2.0f, Color.argb(76, 0, 0, 0));
        this.mAlbumBiggerPaint = StringTexture.getDefaultPaint((float) res.getDimensionPixelSize(R.dimen.story_tag_albumSet_main_name_size_middle), res.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumBiggerPaint.setShadowLayer(12.0f, 0.0f, 2.0f, Color.argb(76, 0, 0, 0));
        this.mAlbumSmallerPaint = StringTexture.getDefaultPaint((float) res.getDimensionPixelSize(R.dimen.story_tag_albumSet_main_name_size_small), res.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumSmallerPaint.setShadowLayer(12.0f, 0.0f, 2.0f, Color.argb(76, 0, 0, 0));
    }

    public com.huawei.gallery.ui.CommonAlbumSlotView.Layout createLayout(Spec spec) {
        return new Layout(spec);
    }

    private boolean isTurnOver(int rotation) {
        return rotation % 90 == 0 && rotation % 180 != 0;
    }

    public void setSlotCoverItems(ArrayList<MediaItem> items) {
        if (items != null && items.size() != 0) {
            ArrayList<Integer> results = new ArrayList();
            for (int i = 0; i < items.size(); i++) {
                MediaItem item = (MediaItem) items.get(i);
                int ret = 2;
                if (item.getHeight() < item.getWidth()) {
                    if (!isTurnOver(item.getRotation())) {
                        ret = 1;
                    }
                } else if (isTurnOver(item.getRotation())) {
                    ret = 1;
                }
                results.add(Integer.valueOf(ret));
            }
            if (this.mLayout instanceof Layout) {
                ((Layout) this.mLayout).updateDataType(results);
            }
        }
    }
}
