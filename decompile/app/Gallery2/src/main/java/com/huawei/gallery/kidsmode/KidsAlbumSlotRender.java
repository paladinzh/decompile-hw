package com.huawei.gallery.kidsmode;

import android.graphics.Rect;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.Texture;
import com.huawei.gallery.ui.AbstractCommonAlbumSlotRender;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.CommonAlbumSlotView;

public class KidsAlbumSlotRender extends AbstractCommonAlbumSlotRender {
    private ResourceTexture mFramePhoto;
    private ResourceTexture mVideoPlayIcon;

    public KidsAlbumSlotRender(GalleryContext context, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(context, slotView, selectionManager, placeholderColor);
        this.mVideoPlayIcon = new ResourceTexture(context.getAndroidContext(), R.drawable.btn_play);
        this.mFramePhoto = new ResourceTexture(context.getAndroidContext(), R.drawable.thumbnail_1);
    }

    protected void renderOverlay(GLCanvas canvas, AlbumEntry entry, int width, int height) {
        drawFrame(canvas, new Rect(0, 0, 0, 0), this.mFramePhoto, 0, 0, width, height);
        if (entry.mediaType == 4) {
            drawCenterIcon(canvas, this.mVideoPlayIcon, width, height);
        }
    }

    protected void drawCenterIcon(GLCanvas canvas, Texture icon, int width, int height) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        icon.draw(canvas, (width / 2) - (w / 2), (height / 2) - (h / 2), w, h);
    }

    public void setPressedIndex(int index) {
    }
}
