package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
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
import com.android.gallery3d.ui.SlotFilter;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.ThemeableNinePatchTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.app.CommonAlbumDataLoader;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.Listener;
import com.huawei.gallery.ui.CommonAlbumSlotView.SlotRenderer;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.HashMap;

public abstract class AbstractCommonAlbumSlotRender implements SlotRenderer {
    protected static final int BACKGROUND_COVER_HEIGHT = GalleryUtils.dpToPixel(24);
    protected final ResourceTexture m3DModelImageIcon;
    protected final ResourceTexture m3DPanoramaIcon;
    protected final GalleryContext mActivity;
    protected final ResourceTexture mAllFocusPhotoIcon;
    protected final NinePatchTexture mBackGroundCoverTexture;
    protected final ResourceTexture mBurstCoverIcon;
    protected final Texture mCheckedFrameIcon;
    protected final ColorTexture mCheckedFrameMask;
    protected final Texture mCheckedOffFrameIcon;
    private final ResourceTexture mCloudPlaceHolderTexture;
    protected CommonAlbumSlidingWindow mDataWindow;
    protected int mDefaultMarin;
    protected NinePatchTexture mFramePhoto;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;
    protected final ResourceTexture mLivePhotoIcon;
    protected final ResourceTexture mMyFavoriteIcon;
    private final ColorTexture mNoThumbColorTexture;
    private final ResourceTexture mNoThumbPicTexture;
    private final Texture mOffTouchedIcon;
    private final Texture mOnTouchedIcon;
    protected int mPhotoWidth;
    private final ColorTexture mPressedCoverTexture;
    protected int mPressedIndex = -1;
    protected final ResourceTexture mRectifyImageIcon;
    protected NinePatchTexture mScreenShotsPhotoFrame;
    protected final SelectionManager mSelectionManager;
    protected SlotFilter mSlotFilter;
    protected final CommonAlbumSlotView mSlotView;
    protected final ResourceTexture mVideoPlayIcon;
    protected final ResourceTexture mVoiceImageIcon;
    protected final ColorTexture mWaitLoadingTexture;
    protected final ResourceTexture mWideAperturePhotoIcon;

    private class MyDataModelListener implements Listener {
        boolean sizeChanged;

        private MyDataModelListener() {
        }

        public void onContentChanged() {
            AbstractCommonAlbumSlotRender.this.mSlotView.invalidate();
        }

        public boolean needVideoTexture() {
            return AbstractCommonAlbumSlotRender.this.isLocalVideoRender();
        }

        public int getSlotWidth() {
            return AbstractCommonAlbumSlotRender.this.mPhotoWidth;
        }

        public void onActiveTextureReady() {
            TraceController.traceBegin("AbstractCommonAlbumSlotRender.onActiveTextureReady, sizeChanged:" + this.sizeChanged);
            if (this.sizeChanged) {
                this.sizeChanged = false;
                AbstractCommonAlbumSlotRender.this.mSlotView.startDeleteSlotAnimationIfNeed();
                AbstractCommonAlbumSlotRender.this.mSlotView.invalidate();
                TraceController.traceEnd();
                return;
            }
            TraceController.traceEnd();
        }

        public void onSizeChanged(int size) {
            this.sizeChanged = true;
            AbstractCommonAlbumSlotRender.this.mSlotView.setSlotCount(size);
            AbstractCommonAlbumSlotRender.this.mSlotView.invalidate();
        }
    }

    protected abstract void renderOverlay(GLCanvas gLCanvas, AlbumEntry albumEntry, int i, int i2);

    protected AbstractCommonAlbumSlotRender(GalleryContext galleryContext, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        this.mActivity = galleryContext;
        this.mSelectionManager = selectionManager;
        this.mSlotView = slotView;
        this.mWaitLoadingTexture = new ColorTexture(placeholderColor);
        this.mPressedCoverTexture = new ColorTexture(654311424);
        Context context = galleryContext.getAndroidContext();
        this.mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_video);
        this.mCheckedFrameIcon = HwTextureFactory.buildCheckedFrameIcon(context, R.drawable.btn_gallery_toggle_on);
        this.mCheckedOffFrameIcon = HwTextureFactory.buildCheckedOffFrameIcon(context, R.drawable.btn_gallery_toggle_off);
        this.mMyFavoriteIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_favorite);
        this.mBurstCoverIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_burst);
        this.m3DPanoramaIcon = new ResourceTexture(context, R.drawable.ic_unlock_3d);
        this.mVoiceImageIcon = new ResourceTexture(galleryContext.getAndroidContext(), R.drawable.ic_gallery_frame_overlay_soundphoto);
        this.mLivePhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_camera_livephoto);
        this.mCheckedFrameMask = new ColorTexture(-2130706433);
        this.mAllFocusPhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_allfocus);
        this.mWideAperturePhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_aperture);
        this.mRectifyImageIcon = new ResourceTexture(galleryContext.getAndroidContext(), R.drawable.ic_gallery_frame_overlay_rectify);
        this.m3DModelImageIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_3d_portrait);
        this.mOffTouchedIcon = HwTextureFactory.buildCheckedOffPressFrameIcon(context, R.drawable.btn_gallery_toggle_off_pressed);
        this.mOnTouchedIcon = HwTextureFactory.buildCheckedPressFrameIcon(context, R.drawable.btn_gallery_toggle_on_pressed);
        this.mNoThumbColorTexture = new ColorTexture(context.getResources().getColor(R.color.cloud_placeholder));
        this.mNoThumbPicTexture = new ResourceTexture(context, R.drawable.ic_gallery_list_damage_day);
        this.mCloudPlaceHolderTexture = new ResourceTexture(context, R.drawable.pic_gallery_album_empty_day);
        this.mBackGroundCoverTexture = new NinePatchTexture(context, R.drawable.bg_frame_bottom);
        this.mDefaultMarin = context.getResources().getDimensionPixelSize(R.dimen.layout_margin_default_4dp);
        this.mScreenShotsPhotoFrame = new ThemeableNinePatchTexture(galleryContext.getAndroidContext(), R.drawable.bg_frame_border);
    }

    protected void drawLeftBottomIcon(GLCanvas canvas, Texture icon, int xDelta, int yDelta, int width, int height) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        icon.draw(canvas, xDelta, (height - h) - yDelta, w, h);
    }

    protected void drawContent(GLCanvas canvas, Texture content, int width, int height, int rotation, boolean isPreview) {
        canvas.save(2);
        height = Math.min(width, height);
        width = height;
        if (rotation != 0) {
            canvas.translate(((float) height) / 2.0f, ((float) height) / 2.0f);
            canvas.rotate((float) rotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            canvas.translate(((float) (-height)) / 2.0f, ((float) (-height)) / 2.0f);
        }
        float scale = Math.min(((float) height) / ((float) content.getWidth()), ((float) height) / ((float) content.getHeight()));
        canvas.scale(scale, scale, WMElement.CAMERASIZEVALUE1B1);
        if (isPreview) {
            canvas.setAlpha(0.3f);
        }
        content.draw(canvas, 0, 0);
        canvas.restore();
    }

    protected void drawFrame(GLCanvas canvas, Rect padding, Texture frame, int x, int y, int width, int height) {
        frame.draw(canvas, x - padding.left, y - padding.top, (padding.left + width) + padding.right, (padding.top + height) + padding.bottom);
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

    public void setModel(CommonAlbumDataLoader model) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setListener(null);
            this.mSlotView.setSlotCount(0);
            this.mDataWindow = null;
        }
        if (model != null) {
            this.mDataWindow = new CommonAlbumSlidingWindow(this.mActivity, model, 96);
            this.mDataWindow.setListener(new MyDataModelListener());
            this.mSlotView.setSlotCount(model.size());
        }
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
    }

    public void renderSlot(GLCanvas canvas, int index, int pass, int width, int height) {
        if ((this.mSlotFilter == null || this.mSlotFilter.acceptSlot(index)) && this.mDataWindow.isActiveSlot(index)) {
            AlbumEntry entry = this.mDataWindow.get(index);
            if (entry != null) {
                TraceController.traceBegin("AbstractCommonAlbumSlotRender.renderSlot:" + entry.path);
                renderSlot(canvas, entry, pass, width, height);
                renderSelectedEntry(canvas, index, entry, width, height, entry.isPhotoSharePreView);
                TraceController.traceEnd();
            }
        }
    }

    public void renderTopTitle(GLCanvas canvas, int left, int top, int right, int bottom) {
    }

    public Bitmap getContentBitmap(int index) {
        AlbumEntry entry = this.mDataWindow.get(index);
        if (entry == null || entry.bitmapTexture == null || entry.bitmapTexture.getBitmap() == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        Bitmap bitmap = entry.bitmapTexture.getBitmap();
        matrix.setRotate((float) entry.rotation, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void renderSlot(GLCanvas canvas, AlbumEntry entry, int pass, int width, int height) {
        if (entry != null) {
            Texture content = entry.content;
            if (entry.isCloudPlaceHolder || entry.isNoThumb) {
                content = this.mNoThumbColorTexture;
            } else if (content == null) {
                content = this.mWaitLoadingTexture;
            }
            drawContent(canvas, content, width, height, entry.rotation, entry.isPhotoSharePreView);
            if (entry.isScreenShots) {
                drawFrame(canvas, this.mScreenShotsPhotoFrame.getPaddings(), this.mScreenShotsPhotoFrame, 0, 0, width, height);
            }
            if (entry.isCloudPlaceHolder) {
                drawMiddleCenterIcon(canvas, this.mCloudPlaceHolderTexture, width, height);
            } else if (entry.isNoThumb) {
                drawMiddleCenterIcon(canvas, this.mNoThumbPicTexture, width, height);
            }
            renderOverlay(canvas, entry, width, height);
        }
    }

    private static void drawMiddleCenterIcon(GLCanvas canvas, Texture icon, int width, int height) {
        icon.draw(canvas, (width - icon.getWidth()) / 2, (height - icon.getHeight()) / 2, icon.getWidth(), icon.getHeight());
    }

    public boolean isCornerPressed(float x, float y, Rect slot, int scrollPosition, boolean isTitle) {
        Rect corner = new Rect();
        corner.set(slot.right - ((int) Math.max((float) this.mCheckedFrameIcon.getWidth(), ((float) slot.width()) * 0.5f)), slot.bottom - ((int) Math.max((float) this.mCheckedFrameIcon.getHeight(), ((float) slot.height()) * 0.5f)), slot.right, slot.bottom);
        return corner.contains((int) x, (int) (((float) scrollPosition) + y));
    }

    private void renderSelectedEntry(GLCanvas canvas, int index, AlbumEntry entry, int width, int height, boolean isPreview) {
        if (this.mPressedIndex == index) {
            this.mPressedCoverTexture.draw(canvas, 0, 0, width, height);
        }
        if (this.mSelectionManager.inSelectionMode() && !this.mSelectionManager.inSingleMode()) {
            boolean selected = isPreview ? false : isSelectedStatus(entry);
            if (selected) {
                drawContent(canvas, this.mCheckedFrameMask, width, height, 0, false);
            }
            Texture checkFrameIcon = this.mPressedIndex == index ? selected ? this.mOnTouchedIcon : this.mOffTouchedIcon : selected ? this.mCheckedFrameIcon : this.mCheckedOffFrameIcon;
            int iconTop = (height - checkFrameIcon.getHeight()) - this.mDefaultMarin;
            int iconLeft = (width - checkFrameIcon.getWidth()) - this.mDefaultMarin;
            canvas.translate((float) iconLeft, (float) iconTop);
            drawContent(canvas, checkFrameIcon, checkFrameIcon.getWidth(), checkFrameIcon.getHeight(), 0, false);
            canvas.translate((float) (-iconLeft), (float) (-iconTop));
        }
    }

    public void setSlotFilter(SlotFilter slotFilter) {
        this.mSlotFilter = slotFilter;
    }

    protected boolean isSelectedStatus(AlbumEntry entry) {
        if (this.mHighlightItemPath != null && this.mHighlightItemPath == entry.path) {
            return true;
        }
        if (this.mInSelectionMode && this.mSelectionManager.isItemSelected(entry.path)) {
            return true;
        }
        return false;
    }

    protected boolean isLocalVideoRender() {
        return false;
    }

    public static AbstractCommonAlbumSlotRender getSlotRender(boolean needCommonSlot, GalleryContext galleryContext, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        if (needCommonSlot) {
            return new CommonAlbumSlotRender(galleryContext, slotView, selectionManager, placeholderColor);
        }
        return new LocalCameraVideoAlbumSlotRender(galleryContext, slotView, selectionManager, placeholderColor);
    }

    public Path getItemPath(Object index) {
        if (this.mDataWindow == null) {
            return null;
        }
        AlbumEntry entry = this.mDataWindow.get(((Integer) index).intValue());
        if (entry == null) {
            return null;
        }
        return entry.path;
    }

    public void prepareVisibleRangeItemIndex(HashMap<Path, Object> visiblePathMap, HashMap<Object, Object> visibleIndexMap) {
        if (this.mDataWindow != null) {
            this.mDataWindow.prepareVisibleRangeItemIndex(visiblePathMap, visibleIndexMap);
        }
    }

    public void freeVisibleRangeItem(HashMap<Path, Object> visiblePathMap) {
        if (this.mDataWindow != null) {
            this.mDataWindow.freeVisibleRangeItem(visiblePathMap);
        }
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
