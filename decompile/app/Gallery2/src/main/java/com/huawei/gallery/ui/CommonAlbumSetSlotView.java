package com.huawei.gallery.ui;

import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.huawei.gallery.ui.CommonAlbumSlotView.Layout;
import com.huawei.gallery.ui.CommonAlbumSlotView.Spec;
import java.util.ArrayList;

public class CommonAlbumSetSlotView extends CommonAlbumSlotView {
    protected SlotRenderer mSlotRenderer;

    public interface SlotRenderer extends com.huawei.gallery.ui.CommonAlbumSlotView.SlotRenderer {
        void onSlotSizeChanged(Layout layout);
    }

    public CommonAlbumSetSlotView(GalleryContext activity, Spec spec) {
        super(activity, spec);
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        this.mSlotRenderer = slotDrawer;
        if (this.mSlotRenderer != null) {
            this.mSlotRenderer.onSlotSizeChanged(this.mLayout);
        }
        super.setSlotRenderer(slotDrawer);
    }

    public void setSlotCoverItems(ArrayList<MediaItem> arrayList) {
    }
}
