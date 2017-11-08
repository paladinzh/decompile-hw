package com.huawei.gallery.story.ui;

import android.graphics.RectF;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.StringStaticLayoutTexture;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TiledTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.CommonAlbumSetDataLoader;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;
import com.huawei.gallery.editor.tools.EditorUtils.RenderDelegateWithTexture;
import com.huawei.gallery.ui.CommonAlbumSetSlotRender;
import com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow;
import com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.CommonAlbumSetSlotView;
import com.huawei.gallery.ui.CommonAlbumSlotView.Layout;
import com.huawei.watermark.manager.parse.WMElement;

public class StoryAlbumSetSlotRender extends CommonAlbumSetSlotRender {
    private static final int PADDING = GalleryUtils.dpToPixel(6);
    private StoryAlbumSetSlotSlidingWindow mDataWindow;
    private RectF mSourceRectF = new RectF();
    private RectF mTargetRectF = new RectF();

    public StoryAlbumSetSlotRender(GalleryContext galleryContext, CommonAlbumSetSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(galleryContext, slotView, selectionManager, placeholderColor);
        TiledTexture.prepareResources();
    }

    protected CommonAlbumSetSlotSlidingWindow createAlbumSetSlotSlidingWindow(CommonAlbumSetDataLoader model) {
        this.mDataWindow = new StoryAlbumSetSlotSlidingWindow(this.mActivity, model, 24);
        return this.mDataWindow;
    }

    public void onSlotSizeChanged(Layout layout) {
        super.onSlotSizeChanged(layout);
        if (this.mDataWindow != null) {
            this.mDataWindow.setLayout(layout);
            this.mDataWindow.updateTexture();
        }
    }

    protected void drawContent(GLCanvas canvas, Texture content, int width, int height, int rotation, int x, int y, float alpha) {
        canvas.save();
        RenderDelegate renderDelegate = new RenderDelegateWithTexture(content, null, width, height, rotation);
        RectComputer.computerRect(renderDelegate, this.mSourceRectF, this.mTargetRectF);
        if (!(x == 0 && y == 0)) {
            if (rotation == 0) {
                this.mTargetRectF.offset((float) x, (float) y);
            } else if (rotation == 90) {
                this.mTargetRectF.offset((float) y, (float) (-x));
            } else if (rotation == 180) {
                this.mTargetRectF.offset((float) (-x), (float) (-y));
            } else if (rotation == 270) {
                this.mTargetRectF.offset((float) (-y), (float) x);
            }
        }
        if (rotation != 0) {
            canvas.translate(((float) width) / 2.0f, ((float) height) / 2.0f);
            canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            if (renderDelegate.isOverTurn()) {
                canvas.translate(((float) (-height)) / 2.0f, ((float) (-width)) / 2.0f);
            } else {
                canvas.translate(((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f);
            }
        }
        if (content instanceof BitmapTexture) {
            canvas.drawTexture((BitmapTexture) content, this.mSourceRectF, this.mTargetRectF);
        } else if (content instanceof TiledTexture) {
            ((TiledTexture) content).draw(canvas, this.mSourceRectF, this.mTargetRectF);
        }
        canvas.restore();
    }

    protected void drawText(GLCanvas canvas, AlbumSetEntry entry, int width, int height, boolean isScrolling) {
        if (entry instanceof StoryAlbumSetSlotSlidingWindow.AlbumSetEntry) {
            StoryAlbumSetSlotSlidingWindow.AlbumSetEntry storyEntry = (StoryAlbumSetSlotSlidingWindow.AlbumSetEntry) entry;
            canvas.setAlpha(0.15f);
            canvas.fillRect(0.0f, 0.0f, (float) width, (float) height, -16777216);
            canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1);
            StringStaticLayoutTexture albumMainNameTexture = (StringStaticLayoutTexture) CommonAlbumSetSlotRender.checkContentTexture(storyEntry.albumMainNameTexture, isScrolling);
            StringStaticLayoutTexture albumSubNameTexture = (StringStaticLayoutTexture) CommonAlbumSetSlotRender.checkContentTexture(storyEntry.albumSubNameTexture, isScrolling);
            if (albumMainNameTexture != null && albumSubNameTexture != null) {
                int mainHeight = albumMainNameTexture.getHeight();
                int subHeight = albumSubNameTexture.getHeight();
                if (subHeight <= 1) {
                    albumMainNameTexture.draw(canvas, (width / 2) - (albumMainNameTexture.getWidth() / 2), (height / 2) - (mainHeight / 2));
                } else {
                    albumMainNameTexture.draw(canvas, (width / 2) - (albumMainNameTexture.getWidth() / 2), (height / 2) - (((mainHeight + subHeight) + PADDING) / 2));
                    albumSubNameTexture.draw(canvas, (width / 2) - (albumSubNameTexture.getWidth() / 2), (((height / 2) - (((mainHeight + subHeight) + PADDING) / 2)) + mainHeight) + PADDING);
                }
            } else {
                return;
            }
        }
        super.drawText(canvas, entry, width, height, isScrolling);
    }
}
