package com.huawei.gallery.kidsmode;

import com.android.gallery3d.app.GalleryContext;
import com.huawei.gallery.ui.CommonAlbumSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.Layout;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;

public class KidsAlbumSlotView extends CommonAlbumSlotView {

    public class KidsLayout extends Layout {

        private class SlotNumAndGap {
            public int slotGap;
            public int slotNumber;

            private SlotNumAndGap() {
                this.slotNumber = -1;
                this.slotGap = -1;
            }
        }

        public KidsLayout(Spec spec) {
            super(spec);
        }

        protected void initLayoutParameters(Layout layout) {
            this.mSlotWidth = this.mSpec.slotWidth;
            this.mSlotHeight = this.mSpec.slotHeight;
            if (KidsAlbumSlotView.this.mRenderer != null) {
                KidsAlbumSlotView.this.mRenderer.onSlotSizeChanged(this.mSlotWidth, this.mSlotHeight);
            }
            int[] padding = new int[2];
            int[] gap = new int[2];
            initLayoutParameters(this.mHeight, this.mWidth, this.mSlotHeight, this.mSlotWidth, padding, gap);
            this.mVerticalPadding.startAnimateTo(padding[1]);
            this.mHorizontalPadding.startAnimateTo(padding[0]);
            this.mSlotWidthGap = gap[0];
            this.mSlotHeightGap = gap[1];
            updateVisibleSlotRange();
            if (KidsAlbumSlotView.this.mSlotScrollBar != null) {
                KidsAlbumSlotView.this.mSlotScrollBar.updateContentLen(getScrollLimit());
            }
        }

        private boolean isSlotGapOK(int slotGap, int slotSize) {
            return ((double) (((float) slotGap) / ((float) slotSize))) >= 0.01d;
        }

        private void calSlotNumAndGap(int lineLength, int unitSize, SlotNumAndGap slotData) {
            int unitCount = lineLength / unitSize;
            if (unitCount <= 1) {
                slotData.slotGap = 0;
                slotData.slotNumber = 1;
                return;
            }
            int slotGap;
            while (true) {
                slotGap = (lineLength - (unitCount * unitSize)) / Math.max(1, unitCount - 1);
                if (isSlotGapOK(slotGap, unitSize) || unitCount == 1) {
                    slotData.slotGap = slotGap;
                    slotData.slotNumber = unitCount;
                } else {
                    unitCount--;
                }
            }
            slotData.slotGap = slotGap;
            slotData.slotNumber = unitCount;
        }

        private void initLayoutParameters(int majorLength, int minorLength, int majorUnitSize, int minorUnitSize, int[] padding, int[] gap) {
            int leftPadding = this.mSpec.slot_horizontal_padding;
            int rightPadding = this.mSpec.slot_horizontal_padding;
            int topPadding = this.mSpec.slot_vertical_padding;
            SlotNumAndGap slotNumAndGap = new SlotNumAndGap();
            calSlotNumAndGap((minorLength - leftPadding) - rightPadding, minorUnitSize, slotNumAndGap);
            this.mUnitCount = slotNumAndGap.slotNumber;
            gap[0] = slotNumAndGap.slotGap;
            gap[1] = gap[0];
            padding[0] = leftPadding - gap[0];
            padding[1] = topPadding - gap[1];
            int count = ((this.mSlotCount + this.mUnitCount) - 1) / this.mUnitCount;
            this.mContentLength = ((count * majorUnitSize) + ((count + 1) * gap[1])) + padding[1];
        }
    }

    public KidsAlbumSlotView(GalleryContext activity, Spec spec) {
        super(activity, spec);
    }

    protected Layout createLayout(Spec spec) {
        return new KidsLayout(spec);
    }
}
