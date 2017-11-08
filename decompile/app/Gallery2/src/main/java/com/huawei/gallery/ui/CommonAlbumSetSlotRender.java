package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.HwTextureFactory;
import com.android.gallery3d.ui.NinePatchTexture;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TiledTexture;
import com.android.gallery3d.ui.UploadedTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.CommonAlbumSetDataLoader;
import com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry;
import com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.Listener;
import com.huawei.gallery.ui.CommonAlbumSetSlotView.SlotRenderer;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.CommonAlbumSlotView.Layout;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.HashMap;

public class CommonAlbumSetSlotRender implements SlotRenderer {
    private static final int BACKGROUND_COVER_HEIGHT = GalleryUtils.dpToPixel(24);
    protected final GalleryContext mActivity;
    private final NinePatchTexture mBackGroundCoverTexture;
    protected final Texture mCheckedFrameIcon;
    protected final Texture mCheckedOffFrameIcon;
    protected CommonAlbumSetSlotSlidingWindow mDataWindow;
    private int mDefaultMargin;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;
    protected Layout mLayout;
    private final ResourceTexture mNoThumbPicTexture;
    private final Texture mOffTouchedIcon;
    private final Texture mOnTouchedIcon;
    private int mPhotoHeight;
    private int mPhotoWidth;
    protected int mPressedIndex = -1;
    private Rect mRect = new Rect();
    protected final SelectionManager mSelectionManager;
    protected final CommonAlbumSetSlotView mSlotView;
    protected final ColorTexture mWaitLoadingTexture;

    private class MyDataModelListener implements Listener {
        boolean sizeChanged;

        private MyDataModelListener() {
        }

        public void onContentChanged() {
            CommonAlbumSetSlotRender.this.mSlotView.invalidate();
        }

        public int getSlotWidth(int index) {
            if (CommonAlbumSetSlotRender.this.mLayout != null) {
                Rect rect = CommonAlbumSetSlotRender.this.mLayout.getSlotRect(index, CommonAlbumSetSlotRender.this.mRect);
                if (rect.width() != 0) {
                    return rect.width();
                }
            }
            return CommonAlbumSetSlotRender.this.mPhotoWidth;
        }

        public int getSlotHeight(int index) {
            if (CommonAlbumSetSlotRender.this.mLayout != null) {
                Rect rect = CommonAlbumSetSlotRender.this.mLayout.getSlotRect(index, CommonAlbumSetSlotRender.this.mRect);
                if (rect.width() != 0) {
                    return rect.height();
                }
            }
            return CommonAlbumSetSlotRender.this.mPhotoHeight;
        }

        public TextPaint getMainNamePaint(int index) {
            return CommonAlbumSetSlotRender.this.mLayout.getTextPaint(index);
        }

        public void onActiveTextureReady() {
            TraceController.traceBegin("CommonAlbumSetSlotRender.onActiveTextureReady, sizeChanged:" + this.sizeChanged);
            if (this.sizeChanged) {
                this.sizeChanged = false;
                CommonAlbumSetSlotRender.this.mSlotView.invalidate();
                TraceController.traceEnd();
            }
        }

        public void onSizeChanged(int size) {
            this.sizeChanged = true;
            CommonAlbumSetSlotRender.this.mSlotView.setSlotCoverItems(CommonAlbumSetSlotRender.this.mDataWindow.getCoverItems());
            CommonAlbumSetSlotRender.this.mSlotView.setSlotCount(size);
            CommonAlbumSetSlotRender.this.mSlotView.invalidate();
        }
    }

    public CommonAlbumSetSlotRender(GalleryContext galleryContext, CommonAlbumSetSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        this.mActivity = galleryContext;
        this.mSelectionManager = selectionManager;
        this.mSlotView = slotView;
        this.mWaitLoadingTexture = new ColorTexture(placeholderColor);
        Context context = galleryContext.getAndroidContext();
        this.mCheckedFrameIcon = HwTextureFactory.buildCheckedFrameIcon(context, R.drawable.btn_gallery_toggle_on);
        this.mCheckedOffFrameIcon = HwTextureFactory.buildCheckedOffFrameIcon(context, R.drawable.btn_gallery_toggle_off);
        this.mOffTouchedIcon = HwTextureFactory.buildCheckedOffPressFrameIcon(context, R.drawable.btn_gallery_toggle_off_pressed);
        this.mOnTouchedIcon = HwTextureFactory.buildCheckedPressFrameIcon(context, R.drawable.btn_gallery_toggle_on_pressed);
        this.mNoThumbPicTexture = new ResourceTexture(context, R.drawable.album_face_empty);
        this.mBackGroundCoverTexture = new NinePatchTexture(context, R.drawable.bg_frame_bottom);
        this.mDefaultMargin = context.getResources().getDimensionPixelSize(R.dimen.layout_margin_default_4dp);
    }

    protected void drawContent(GLCanvas canvas, Texture content, int width, int height, int rotation, int x, int y, float alpha) {
        canvas.save(-1);
        height = Math.min(width, height);
        width = height;
        canvas.translate((float) x, (float) y);
        if (rotation != 0) {
            canvas.translate(((float) height) / 2.0f, ((float) height) / 2.0f);
            canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            canvas.translate(((float) (-height)) / 2.0f, ((float) (-height)) / 2.0f);
        }
        float scale = Math.min(((float) height) / ((float) content.getWidth()), ((float) height) / ((float) content.getHeight()));
        canvas.scale(scale, scale, WMElement.CAMERASIZEVALUE1B1);
        canvas.setAlpha(alpha);
        content.draw(canvas, 0, 0);
        canvas.restore();
    }

    public void setPressedIndex(int index) {
        if (this.mPressedIndex != index) {
            this.mPressedIndex = index;
            this.mSlotView.invalidate();
        }
    }

    public void setHighlightItemPath(Path path) {
        if (this.mHighlightItemPath != path) {
            this.mHighlightItemPath = path;
            this.mSlotView.invalidate();
        }
    }

    public void setModel(CommonAlbumSetDataLoader model) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setListener(null);
            this.mSlotView.setSlotCoverItems(null);
            this.mSlotView.setSlotCount(0);
            this.mDataWindow = null;
        }
        if (model != null) {
            this.mDataWindow = createAlbumSetSlotSlidingWindow(model);
            this.mDataWindow.setListener(new MyDataModelListener());
            this.mSlotView.setSlotCoverItems(model.getCoverItems());
            this.mSlotView.setSlotCount(model.size());
        }
    }

    protected CommonAlbumSetSlotSlidingWindow createAlbumSetSlotSlidingWindow(CommonAlbumSetDataLoader model) {
        return new CommonAlbumSetSlotSlidingWindow(this.mActivity, model, 96);
    }

    public void setGLRoot(GLRoot glRoot) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setGLRoot(glRoot);
        }
    }

    public void resume() {
        this.mSlotView.clearAnimation();
        this.mDataWindow.resume();
    }

    public void pause(boolean needFreeSlotContent) {
        this.mDataWindow.pause(needFreeSlotContent);
    }

    public void destroy() {
        this.mDataWindow.destroy();
    }

    public void prepareDrawing() {
        this.mInSelectionMode = this.mSelectionManager.inSelectionMode();
    }

    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    public void onSlotSizeChanged(int width, int height) {
        this.mPhotoWidth = width;
        this.mPhotoHeight = height;
    }

    public void onSlotSizeChanged(Layout layout) {
        this.mPhotoWidth = layout.mSlotWidth;
        this.mPhotoHeight = layout.mSlotHeight;
        this.mLayout = layout;
    }

    public void renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        renderSlot(canvas, index, pass, width, height, false);
    }

    public void renderSlot(GLCanvas canvas, AlbumEntry entry, int pass, int width, int height) {
    }

    public void renderTopTitle(GLCanvas canvas, int left, int top, int right, int bottom) {
    }

    public void renderSlot(GLCanvas canvas, int index, int pass, int width, int height, boolean isScrolling) {
        AlbumSetEntry entry = this.mDataWindow.get(index);
        if (entry != null) {
            TraceController.traceBegin("CommonAlbumSetSlotRender.renderSlot:" + entry.setPath);
            renderSlot(canvas, entry, pass, width, height, isScrolling);
            renderSelectedEntry(canvas, index, entry, width);
            TraceController.traceEnd();
        }
    }

    protected static Texture checkContentTexture(Texture texture, boolean isScrolling) {
        Texture texture2 = null;
        if (!isScrolling) {
            return texture;
        }
        if (texture instanceof TiledTexture) {
            if (!((TiledTexture) texture).isReady()) {
                texture = null;
            }
            return texture;
        } else if (!(texture instanceof UploadedTexture)) {
            return texture;
        } else {
            if (((UploadedTexture) texture).isLoaded()) {
                texture2 = texture;
            }
            return texture2;
        }
    }

    private void renderSlot(GLCanvas canvas, AlbumSetEntry entry, int pass, int width, int height, boolean isScrolling) {
        if (entry != null && width != 0 && height != 0) {
            Texture content = checkContentTexture(entry.content, isScrolling);
            if (entry.isNoThumb) {
                this.mNoThumbPicTexture.draw(canvas, null, 0, width, height);
            } else if (content == null) {
                this.mWaitLoadingTexture.draw(canvas, null, 0, width, height);
            } else {
                drawContent(canvas, content, width, height, entry.rotation, 0, 0, WMElement.CAMERASIZEVALUE1B1);
            }
            drawText(canvas, entry, width, height, isScrolling);
        }
    }

    protected void drawText(GLCanvas canvas, AlbumSetEntry entry, int width, int height, boolean isScrolling) {
        if (entry != null) {
            Texture texture = entry.albumNameTexture;
            if (texture != null) {
                int nameWidth = texture.getWidth();
                int nameHeight = texture.getHeight();
                int coverHeight = BACKGROUND_COVER_HEIGHT;
                this.mBackGroundCoverTexture.draw(canvas, 0, height - coverHeight, width, coverHeight);
                texture.draw(canvas, this.mDefaultMargin, (height - this.mDefaultMargin) - nameHeight, nameWidth, nameHeight);
            }
        }
    }

    private void renderSelectedEntry(GLCanvas canvas, int index, AlbumSetEntry entry, int width) {
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            boolean selected = isSelectedStatus(entry);
            Texture checkFrameIcon = this.mPressedIndex == index ? selected ? this.mOnTouchedIcon : this.mOffTouchedIcon : selected ? this.mCheckedFrameIcon : this.mCheckedOffFrameIcon;
            int iconTop = this.mDefaultMargin;
            int iconLeft = (width - this.mDefaultMargin) - checkFrameIcon.getWidth();
            canvas.translate((float) iconLeft, (float) iconTop);
            drawContent(canvas, checkFrameIcon, checkFrameIcon.getWidth(), checkFrameIcon.getHeight(), 0, 0, 0, WMElement.CAMERASIZEVALUE1B1);
            canvas.translate((float) (-iconLeft), (float) (-iconTop));
        }
    }

    protected boolean isSelectedStatus(AlbumSetEntry entry) {
        if (this.mHighlightItemPath != null && this.mHighlightItemPath == entry.setPath) {
            return true;
        }
        if (this.mInSelectionMode && this.mSelectionManager.isItemSelected(entry.setPath)) {
            return true;
        }
        return false;
    }

    public boolean isCornerPressed(float x, float y, Rect slot, int scrollPosition, boolean isTitle) {
        return false;
    }

    public Path getItemPath(Object index) {
        if (this.mDataWindow == null) {
            return null;
        }
        AlbumSetEntry entry = this.mDataWindow.get(((Integer) index).intValue());
        if (entry == null) {
            return null;
        }
        return entry.setPath;
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> hashMap, HashMap<Object, Object> hashMap2) {
    }

    public void freeVisibleRangeItem(HashMap<Path, Object> hashMap) {
    }

    public void onLoadingStarted() {
        if (this.mDataWindow != null) {
            this.mDataWindow.onLoadingStarted();
        }
    }

    public void onLoadingFinished() {
        if (this.mDataWindow != null) {
            this.mDataWindow.onLoadingFinished();
        }
    }
}
