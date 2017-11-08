package com.huawei.gallery.ui;

import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.NinePatchTexture;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.Texture;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;

public class LocalCameraVideoAlbumSlotRender extends AbstractCommonAlbumSlotRender {
    private int mVideoOverlayBottomMargin;
    private int mVideoOverlayLeftMargin;
    private int mVideoTitleTextSize;

    public LocalCameraVideoAlbumSlotRender(GalleryContext context, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(context, slotView, selectionManager, placeholderColor);
        Resources r = context.getResources();
        this.mVideoOverlayLeftMargin = r.getDimensionPixelSize(R.dimen.video_overlay_left_margin);
        this.mVideoOverlayBottomMargin = r.getDimensionPixelSize(R.dimen.video_overlay_bottom_margin);
        this.mVideoTitleTextSize = r.getDimensionPixelSize(R.dimen.video_title_text_size);
        this.mFramePhoto = new NinePatchTexture(context.getAndroidContext(), R.drawable.video_frame);
    }

    protected void renderOverlay(GLCanvas canvas, AlbumEntry entry, int width, int height) {
        drawFrame(canvas, this.mFramePhoto.getPaddings(), this.mFramePhoto, 0, 0, width, height);
        Texture texture = entry.videoTitleTexture;
        int deltaY = this.mVideoOverlayBottomMargin;
        if (texture != null) {
            drawLeftBottomIcon(canvas, texture, this.mVideoOverlayLeftMargin, deltaY, width, height);
        }
        texture = entry.videoDurationTexture;
        deltaY += this.mVideoTitleTextSize + 6;
        if (texture != null) {
            drawLeftBottomIcon(canvas, texture, this.mVideoOverlayLeftMargin, deltaY, width, height);
        }
    }

    protected boolean isLocalVideoRender() {
        return true;
    }
}
