package com.huawei.gallery.kidsmode;

import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.SelectionManager;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.CommonAlbumSlotView;

public class ParentAlbumSlotRender extends AbstractCommonAlbumSlotRender {
    public ParentAlbumSlotRender(GalleryContext context, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(context, slotView, selectionManager, placeholderColor);
    }

    protected void renderOverlay(GLCanvas canvas, AlbumEntry entry, int width, int height) {
        if (entry.mediaType == 4) {
            drawLeftBottomIcon(canvas, this.mVideoPlayIcon, 0, 0, width, height);
        }
    }
}
