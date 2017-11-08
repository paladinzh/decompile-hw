package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$LocalCameraAlbumPage;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.TimeBucketPageViewMode;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.ColorTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.HwTextureFactory;
import com.android.gallery3d.ui.NinePatchTexture;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotFilter;
import com.android.gallery3d.ui.SlotPreviewView.SlotPreviewRender;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TimeAxisSelectionManager;
import com.android.gallery3d.ui.UploadedTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.app.MediaItemsDataLoader;
import com.huawei.gallery.app.TimeBucketItemsDataLoader;
import com.huawei.gallery.data.AbsGroupData;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;
import com.huawei.gallery.ui.ListSlotRenderData.Listener;
import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import com.huawei.gallery.ui.ListSlotView.SlotRenderer;
import com.huawei.gallery.ui.TimeAxisLabel.BaseSpec;
import com.huawei.gallery.ui.TimeAxisLabel.TitleSpec;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import java.util.HashMap;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class ListSlotRender implements SlotRenderer, Listener, SlotPreviewRender {
    private final ResourceTexture m3DModelImageIcon;
    private final ResourceTexture m3DPanoramaIcon;
    protected final GalleryContext mActivity;
    private final ResourceTexture mAllFocusPhotoIcon;
    private final NinePatchTexture mBackGroundCoverTexture;
    protected BaseSpec mBaseSpec;
    private int mBottomIconMargin;
    private final ResourceTexture mBurstCoverIcon;
    private final Texture mCheckedFrameIcon;
    private final ColorTexture mCheckedFrameMask;
    private final Texture mCheckedFrameWhiteIcon;
    private final Texture mCheckedOffFrameIcon;
    private final Texture mCheckedOffFrameWhiteIcon;
    private final ColorTexture mCloudOrNoThumbPlaceHolderColorTexture;
    private final ResourceTexture mCloudPlaceHolderTexture;
    protected ListSlotRenderData mDataWindow;
    private ItemCoordinate mFocusIndex = null;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;
    protected boolean mIsLayoutRtl;
    private final ResourceTexture mLivePhotoIcon;
    private int mMarginLeft = GalleryUtils.dpToPixel(16);
    private int mMarginTop = GalleryUtils.dpToPixel(3);
    private int mMarginbottom = GalleryUtils.dpToPixel(10);
    private final ResourceTexture mMyFavoriteIcon;
    private final ResourceTexture mNoThumbTexture;
    private final Texture mOffTouchedIcon;
    private final Texture mOffTouchedWhiteIcon;
    private final Texture mOnTouchedIcon;
    private final Texture mOnTouchedWhiteIcon;
    private final NinePatchTexture mOverflowTitleBg;
    private final ColorTexture mOverlayTexture;
    private final ColorTexture mPressedCoverTexture;
    protected ItemCoordinate mPressedIndex = null;
    private final ResourceTexture mRadioOnIcon;
    private final ResourceTexture mRadioOnPressedIcon;
    private final ResourceTexture mRectifyImageIcon;
    private final SelectionManager mSelectionManager;
    private boolean mSizeChanged;
    private SlotFilter mSlotFilter;
    protected ListSlotView mSlotView;
    private String mStartToEndDate;
    protected TitleSpec mTitleSpec;
    private final ResourceTexture mUpLoadFailedIcon;
    private boolean mUpdateFocus = false;
    private final ResourceTexture mVideoPlayIcon;
    private TimeBucketPageViewMode mViewMode;
    private final ResourceTexture mVoiceImageIcon;
    protected final ColorTexture mWaitLoadingColorTexture;
    private final ResourceTexture mWaitToUploadTexture;
    private final ResourceTexture mWideAperturePhotoIcon;

    public ListSlotRender(GalleryContext activity, ListSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        Context context = activity.getAndroidContext();
        this.mActivity = activity;
        this.mSlotView = slotView;
        this.mSelectionManager = selectionManager;
        this.mTitleSpec = getTitleSpec(context);
        this.mWaitLoadingColorTexture = new ColorTexture(placeholderColor);
        this.mOverlayTexture = new ColorTexture(this.mTitleSpec.background_color);
        this.mPressedCoverTexture = new ColorTexture(654311424);
        this.mCheckedFrameMask = new ColorTexture(-2130706433);
        this.mVideoPlayIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_video);
        this.mCheckedFrameIcon = HwTextureFactory.buildCheckedFrameIcon(context, R.drawable.btn_gallery_toggle_on);
        this.mCheckedOffFrameIcon = HwTextureFactory.buildCheckedOffFrameIcon(context, R.drawable.btn_gallery_toggle_off);
        this.mRadioOnIcon = new ResourceTexture(context, R.drawable.btn_radio_on_emui);
        this.mRadioOnPressedIcon = new ResourceTexture(context, R.drawable.btn_radio_on_pressed_emui);
        this.mMyFavoriteIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_favorite);
        this.mBurstCoverIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_burst);
        this.mVoiceImageIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_soundphoto);
        this.mLivePhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_camera_livephoto);
        this.m3DPanoramaIcon = new ResourceTexture(context, R.drawable.ic_unlock_3d);
        this.mUpLoadFailedIcon = new ResourceTexture(context, R.drawable.ic_gallery_upload_error);
        this.mIsLayoutRtl = GalleryUtils.isLayoutRTL();
        this.mAllFocusPhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_allfocus);
        this.mWideAperturePhotoIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_aperture);
        this.mOffTouchedIcon = HwTextureFactory.buildCheckedOffPressFrameIcon(context, R.drawable.btn_gallery_toggle_off_pressed);
        this.mOnTouchedIcon = HwTextureFactory.buildCheckedPressFrameIcon(context, R.drawable.btn_gallery_toggle_on_pressed);
        this.mOffTouchedWhiteIcon = HwTextureFactory.buildCheckedOffPressFrameIcon(context, R.drawable.btn_gallery_check_off_pressed);
        this.mOnTouchedWhiteIcon = HwTextureFactory.buildCheckedPressFrameIcon(context, R.drawable.btn_gallery_check_on_pressed);
        this.mCheckedFrameWhiteIcon = HwTextureFactory.buildCheckedFrameIcon(context, R.drawable.btn_gallery_check_on);
        this.mCheckedOffFrameWhiteIcon = HwTextureFactory.buildCheckedOffFrameIcon(context, R.drawable.btn_gallery_check_off);
        this.mCloudOrNoThumbPlaceHolderColorTexture = new ColorTexture(context.getResources().getColor(R.color.cloud_placeholder));
        this.mCloudPlaceHolderTexture = new ResourceTexture(context, R.drawable.pic_gallery_album_empty_day);
        this.mWaitToUploadTexture = new ResourceTexture(context, R.drawable.ic_gallery_sync);
        this.mNoThumbTexture = new ResourceTexture(context, R.drawable.ic_gallery_list_damage_day);
        this.mBackGroundCoverTexture = new NinePatchTexture(context, R.drawable.bg_frame_bottom);
        this.mOverflowTitleBg = new NinePatchTexture(context, R.drawable.btn_timetag);
        this.mBottomIconMargin = this.mActivity.getResources().getDimensionPixelSize(R.dimen.layout_margin_default_4dp);
        this.mMarginLeft = this.mActivity.getResources().getDimensionPixelSize(R.dimen.time_line_overflow_title_start_margin);
        this.mRectifyImageIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_rectify);
        this.m3DModelImageIcon = new ResourceTexture(context, R.drawable.ic_gallery_frame_overlay_3d_portrait);
    }

    public void setListSlotView(ListSlotView slotView) {
        if (slotView != null) {
            this.mSlotView = slotView;
        }
    }

    public void setPressedIndex(ItemCoordinate index) {
        if (this.mPressedIndex == null || !this.mPressedIndex.equals(index)) {
            this.mPressedIndex = index;
            this.mSlotView.invalidate();
        }
    }

    public void setModel(MediaItemsDataLoader model) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setListener(null);
            this.mSlotView.updateCountAndMode(null, TimeBucketPageViewMode.DAY);
            this.mDataWindow = null;
            this.mBaseSpec = null;
        }
        if (model != null) {
            this.mDataWindow = createDataWindow(model);
            this.mDataWindow.setListener(this);
            this.mSlotView.updateCountAndMode(model.getGroupDatas(), TimeBucketPageViewMode.DAY);
            this.mBaseSpec = this.mDataWindow.getCurrentSpec();
        }
        this.mViewMode = TimeBucketPageViewMode.DAY;
    }

    protected ListSlotRenderData createDataWindow(MediaItemsDataLoader model) {
        Utils.assertTrue(model instanceof TimeBucketItemsDataLoader);
        return new TimeBucketSlidingWindow(this.mActivity, (TimeBucketItemsDataLoader) model, SmsCheckResult.ESCT_216, getTitleSpec(this.mActivity.getAndroidContext()));
    }

    public void updateGlRoot(GLRoot glRoot) {
        this.mDataWindow.updateGlRoot(glRoot);
    }

    private void resetFocusIndex() {
        this.mUpdateFocus = false;
        this.mFocusIndex = null;
    }

    private boolean isSelectedStatus(MediaItemEntry entry, ItemCoordinate index) {
        if (this.mHighlightItemPath != null && this.mHighlightItemPath == entry.path) {
            return true;
        }
        if (this.mInSelectionMode && (this.mSelectionManager instanceof TimeAxisSelectionManager) && ((TimeAxisSelectionManager) this.mSelectionManager).isItemSelected(index, entry.path)) {
            return true;
        }
        return false;
    }

    private void drawSelectionIcon(GLCanvas canvas, ItemCoordinate index, MediaItemEntry entry, int width, int height) {
        boolean selected = isSelectedStatus(entry, index);
        if (selected) {
            drawContent(canvas, this.mCheckedFrameMask, width, height, 0, canvas.getAlpha());
        }
        boolean singleSelectMode = this.mSelectionManager.inSingleMode();
        Texture checkedFrameIcon = (this.mPressedIndex == null || !this.mPressedIndex.equals(index)) ? singleSelectMode ? this.mRadioOnIcon : selected ? this.mCheckedFrameIcon : this.mCheckedOffFrameIcon : singleSelectMode ? this.mRadioOnPressedIcon : selected ? this.mOnTouchedIcon : this.mOffTouchedIcon;
        drawRightBottomIcon(canvas, checkedFrameIcon, width, height);
    }

    private void drawBottomBackgroundColor(GLCanvas canvas, int width, int height) {
        int backGroundHeight = (int) (this.mActivity.getResources().getDisplayMetrics().density * 24.0f);
        int iconTop = height - backGroundHeight;
        canvas.translate(0.0f, (float) iconTop);
        drawFrame(canvas, this.mBackGroundCoverTexture.getPaddings(), this.mBackGroundCoverTexture, 0, 0, width, backGroundHeight);
        canvas.translate(0.0f, (float) (-iconTop));
    }

    private void drawIndicatedIcon(GLCanvas canvas, MediaItemEntry entry, int width, int height) {
        if (this.mViewMode != TimeBucketPageViewMode.MONTH && !entry.isNoThumb && !entry.isCloudPlaceHolder && !entry.isUploadFailed) {
            boolean isSupportAllFocus;
            if (!entry.isSupportFlag(16)) {
                isSupportAllFocus = false;
            } else if (entry.mItem.getRefocusPhotoType() != 1) {
                isSupportAllFocus = WideAperturePhotoUtil.supportPhotoEdit();
            } else {
                isSupportAllFocus = true;
            }
            if (entry.isSupportFlag(8) || entry.isSupportFlag(1) || r6 || entry.isSupportFlag(4) || entry.isSupportFlag(256) || entry.isSupportFlag(2) || entry.isSupportFlag(512) || entry.isSupportFlag(1024) || entry.isSupportFlag(2048) || (!this.mSelectionManager.inSelectionMode() && (entry.isWaitToUpload || entry.isPreview))) {
                drawBottomBackgroundColor(canvas, width, height);
                int deltaX = this.mBottomIconMargin;
                int deltaY = this.mBottomIconMargin;
                if (entry.isSupportFlag(8)) {
                    drawLeftBottomIcon(canvas, this.mVideoPlayIcon, deltaX, deltaY, width, height);
                    deltaX += this.mVideoPlayIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(1)) {
                    drawLeftBottomIcon(canvas, this.mVoiceImageIcon, deltaX, deltaY, width, height);
                    deltaX += this.mVoiceImageIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(16)) {
                    if (entry.mItem.getRefocusPhotoType() == 1) {
                        drawLeftBottomIcon(canvas, this.mAllFocusPhotoIcon, deltaX, deltaY, width, height);
                        deltaX += this.mAllFocusPhotoIcon.getWidth() + this.mBottomIconMargin;
                    } else if (WideAperturePhotoUtil.supportPhotoEdit()) {
                        drawLeftBottomIcon(canvas, this.mWideAperturePhotoIcon, deltaX, deltaY, width, height);
                        deltaX += this.mWideAperturePhotoIcon.getWidth() + this.mBottomIconMargin;
                    }
                }
                if (entry.isSupportFlag(4)) {
                    drawLeftBottomIcon(canvas, this.mBurstCoverIcon, deltaX, deltaY, width, height);
                    deltaX += this.mBurstCoverIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(256)) {
                    drawLeftBottomIcon(canvas, this.m3DPanoramaIcon, deltaX, deltaY, width, height);
                    deltaX += this.m3DPanoramaIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(512)) {
                    drawLeftBottomIcon(canvas, this.mRectifyImageIcon, deltaX, deltaY, width, height);
                    deltaX += this.mRectifyImageIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(1024)) {
                    drawLeftBottomIcon(canvas, this.m3DModelImageIcon, deltaX, deltaY, width, height);
                    deltaX += this.m3DModelImageIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(2048)) {
                    drawLeftBottomIcon(canvas, this.mLivePhotoIcon, deltaX, deltaY, width, height);
                    deltaX += this.mLivePhotoIcon.getWidth() + this.mBottomIconMargin;
                }
                if (entry.isSupportFlag(2)) {
                    drawLeftBottomIcon(canvas, this.mMyFavoriteIcon, deltaX, deltaY, width, height);
                }
                if (!this.mSelectionManager.inSelectionMode() && ((entry.isWaitToUpload || entry.isPreview) && needDrawUpLoadIcon())) {
                    drawRightBottomIcon(canvas, this.mWaitToUploadTexture, width, height);
                }
            }
        }
    }

    protected boolean needDrawUpLoadIcon() {
        return true;
    }

    private void renderOverlayTexture(GLCanvas canvas, int x, int y, int w, int h) {
        this.mOverlayTexture.draw(canvas, x, y, w, h);
    }

    public int renderTopTitleOverflow(GLCanvas canvas, ItemCoordinate visibleStart, boolean isScrolling, int offsetY, int topMargin) {
        if (visibleStart.isTitle()) {
            BaseEntry baseEntry = null;
            Texture content = null;
            if (!this.mDataWindow.isActiveSlot(visibleStart)) {
                visibleStart.group++;
            }
            if (this.mDataWindow.isActiveSlot(visibleStart)) {
                baseEntry = this.mDataWindow.get(visibleStart);
            }
            if (baseEntry instanceof TitleEntry) {
                content = ((TitleEntry) baseEntry).mSimpleTitleTexture;
            }
            if (content == null) {
                return 0;
            }
            int h = content.getHeight();
            int w = content.getWidth();
            int slotViewWidth = this.mSlotView.getWidth();
            int width = w + (this.mMarginLeft * 2);
            int height = this.mOverflowTitleBg.getHeight();
            int Showingheight = Math.min(this.mMarginTop + height, offsetY);
            this.mOverflowTitleBg.draw(canvas, this.mIsLayoutRtl ? slotViewWidth - width : 0, (Showingheight - height) + topMargin, width, height);
            content.draw(canvas, this.mIsLayoutRtl ? (slotViewWidth - w) - this.mMarginLeft : this.mMarginLeft, ((Showingheight - h) - this.mMarginbottom) + topMargin);
        }
        return 0;
    }

    public void clearOldTitleData() {
        if (this.mDataWindow != null && this.mDataWindow.mOldTitleData != null) {
            this.mDataWindow.mOldTitleData.freeEntry();
        }
    }

    protected void renderTitle(GLCanvas canvas, ItemCoordinate coordinate, Texture content, boolean coverLine, boolean needShowIcon, int width, int height, boolean fromTo) {
        if (this.mBaseSpec != null) {
            int x;
            int slotViewWidth = this.mSlotView.getWidth();
            if (coverLine) {
                int h = ((this.mBaseSpec.getGroupTitleHeight() / 2) - (this.mTitleSpec.time_line_icon / 2)) + this.mBaseSpec.label_top_margin;
                if (this.mIsLayoutRtl) {
                    x = slotViewWidth + (this.mTitleSpec.time_line_icon / 2);
                } else {
                    x = -this.mTitleSpec.time_line_start_padding;
                }
                if (h > 0 && needOverlayWhenRendTitle()) {
                    renderOverlayTexture(canvas, x, 0, this.mTitleSpec.time_line_icon, h);
                }
            }
            if (this.mIsLayoutRtl) {
                x = (this.mTitleSpec.time_line_width + slotViewWidth) - content.getWidth();
            } else {
                x = this.mTitleSpec.time_line_width;
            }
            float scale = ((float) width) / ((float) slotViewWidth);
            if (!(this.mSlotView.beBiggerView() && fromTo) && (this.mSlotView.beBiggerView() || fromTo)) {
                content.draw(canvas, x, 0, (int) (((float) content.getWidth()) * scale), (int) (((float) content.getHeight()) * scale));
            } else if (content instanceof BitmapTexture) {
                float y = ((float) content.getHeight()) - (((float) content.getHeight()) / scale);
                GLCanvas gLCanvas = canvas;
                gLCanvas.drawTexture((BitmapTexture) content, new RectF(0.0f, y, (float) content.getWidth(), ((float) content.getHeight()) + y), new RectF((float) x, 0.0f, (float) (content.getWidth() + x), ((float) content.getHeight()) / scale));
            } else {
                content.draw(canvas, x, 0);
            }
            renderTitleSelectedIcon(canvas, coordinate, slotViewWidth, this.mBaseSpec.label_height / 2);
        }
    }

    private void drawTitleRight(GLCanvas canvas, Texture icon, int right, int centerVertical) {
        int marginRight = GalleryUtils.dpToPixel(16);
        if (!this.mIsLayoutRtl) {
            marginRight = (right - marginRight) - icon.getWidth();
        }
        icon.draw(canvas, marginRight, Math.round((((float) centerVertical) - (((float) icon.getHeight()) / 2.0f)) - 2.0f));
    }

    private void renderTitleSelectedIcon(GLCanvas canvas, ItemCoordinate coordinate, int right, int centerVertical) {
        if (this.mInSelectionMode && coordinate.isTitle() && (this.mSelectionManager instanceof TimeAxisSelectionManager)) {
            boolean selected = ((TimeAxisSelectionManager) this.mSelectionManager).inSelectAllGroupMode(coordinate);
            Texture icon = (this.mPressedIndex == null || !this.mPressedIndex.equals(coordinate)) ? selected ? this.mCheckedFrameWhiteIcon : this.mCheckedOffFrameWhiteIcon : selected ? this.mOnTouchedWhiteIcon : this.mOffTouchedWhiteIcon;
            drawTitleRight(canvas, icon, right, centerVertical);
        }
    }

    protected static Texture checkTexture(boolean isScrolling, Texture texture) {
        if (isScrolling && (texture instanceof UploadedTexture) && ((UploadedTexture) texture).isUploading()) {
            return null;
        }
        return texture;
    }

    public int renderSlotPreview(GLCanvas canvas, int photoWidth, int PhotoHeight) {
        if (this.mPressedIndex == null || !acceptSlot(this.mPressedIndex) || !this.mDataWindow.isActiveSlot(this.mPressedIndex) || this.mPressedIndex.isTitle()) {
            return 0;
        }
        BaseEntry baseEntry = this.mDataWindow.get(this.mPressedIndex);
        if (!(baseEntry instanceof MediaItemEntry)) {
            return 0;
        }
        Texture content = checkTexture(false, baseEntry.content);
        if (baseEntry.isNoThumb || baseEntry.isCloudPlaceHolder) {
            content = this.mCloudOrNoThumbPlaceHolderColorTexture;
        } else if (content == null) {
            content = this.mWaitLoadingColorTexture;
        }
        drawContent(canvas, content, photoWidth, PhotoHeight, ((MediaItemEntry) baseEntry).rotation, canvas.getAlpha());
        return 0;
    }

    public int renderSlot(GLCanvas canvas, ItemCoordinate index, boolean coverLine, boolean isScrolling, int width, int height, boolean fromTo, boolean needShowIcon) {
        if (!acceptSlot(index) || !this.mDataWindow.isActiveSlot(index, fromTo)) {
            return 0;
        }
        return renderSlot(canvas, this.mDataWindow.get(index, fromTo), index, coverLine, isScrolling, width, height, needShowIcon, fromTo);
    }

    public Bitmap getContentBitmap(ItemCoordinate index) {
        BaseEntry baseEntry = this.mDataWindow.get(index);
        if (baseEntry == null || baseEntry.bitmapTexture == null || baseEntry.bitmapTexture.getBitmap() == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        Bitmap bitmap = baseEntry.bitmapTexture.getBitmap();
        matrix.setRotate((float) baseEntry.rotation, ((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public int renderSlot(GLCanvas canvas, BaseEntry baseEntry, ItemCoordinate index, boolean coverLine, boolean isScrolling, int width, int height, boolean needShowIcon, boolean fromTo) {
        if (baseEntry == null) {
            return 0;
        }
        Texture content = baseEntry.content;
        if ((content instanceof UploadedTexture) && ((UploadedTexture) content).isUploading()) {
            ((UploadedTexture) content).onSetIsUploading(false);
        }
        if (baseEntry.isCloudPlaceHolder || baseEntry.isNoThumb) {
            content = this.mCloudOrNoThumbPlaceHolderColorTexture;
        } else if (content == null) {
            content = this.mWaitLoadingColorTexture;
        }
        if (index.isTitle()) {
            renderTitle(canvas, index, content, coverLine, needShowIcon, width, height, fromTo);
            return 0;
        } else if (!(baseEntry instanceof MediaItemEntry)) {
            return 0;
        } else {
            float alpha;
            MediaItemEntry entry = (MediaItemEntry) baseEntry;
            if (entry.isUploadFailed) {
                alpha = 0.15f;
            } else {
                alpha = canvas.getAlpha();
            }
            drawContent(canvas, content, width, height, entry.rotation, alpha);
            if (!needShowIcon) {
                return 0;
            }
            if (entry.isCloudPlaceHolder) {
                drawMiddleCenterIcon(canvas, this.mCloudPlaceHolderTexture, width, height, this.mViewMode);
            } else if (entry.isNoThumb) {
                drawMiddleCenterIcon(canvas, this.mNoThumbTexture, width, height, this.mViewMode);
            } else if (entry.isUploadFailed) {
                drawMiddleCenterIcon(canvas, this.mUpLoadFailedIcon, width, height, TimeBucketPageViewMode.DAY);
            }
            drawIndicatedIcon(canvas, entry, width, height);
            if (this.mPressedIndex != null && this.mPressedIndex.equals(index)) {
                this.mPressedCoverTexture.draw(canvas, 0, 0, width, height);
                if (this.mInSelectionMode) {
                    drawSelectionIcon(canvas, index, entry, width, height);
                }
            }
            if (this.mSelectionManager.inSelectionMode()) {
                drawSelectionIcon(canvas, index, entry, width, height);
            }
            return 0;
        }
    }

    public int renderSlot(GLCanvas canvas, BaseEntry baseEntry, ItemCoordinate index, boolean coverLine, boolean isScrolling, int width, int height) {
        return renderSlot(canvas, baseEntry, index, coverLine, isScrolling, width, height, true, false);
    }

    public void prepareDrawing() {
        this.mInSelectionMode = this.mSelectionManager.inSelectionMode();
    }

    public void onVisibleRangeChanged(ItemCoordinate visibleStart, ItemCoordinate visibleEnd) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    public void onVisibleRangeChanged(ItemCoordinate visibleStart, ItemCoordinate visibleEnd, ItemCoordinate visibleTitleStart, ItemCoordinate visibleTitleEnd) {
        if (this.mDataWindow != null) {
            this.mDataWindow.setActiveWindow(visibleStart, visibleEnd, visibleTitleStart, visibleTitleEnd);
        }
    }

    public void onSlotSizeChanged(int width, int height) {
    }

    public boolean onScale(boolean beBigger) {
        return this.mDataWindow.scale(beBigger);
    }

    public boolean isCornerPressed(float x, float y, Rect slot, int scrollPosition, boolean isTitle) {
        Rect corner = new Rect();
        if (isTitle) {
            int offsetX = (int) Math.max((float) this.mCheckedFrameIcon.getWidth(), ((float) slot.width()) * 0.2f);
            int offsetY = Math.max(this.mCheckedFrameIcon.getHeight(), slot.height());
            if (this.mIsLayoutRtl) {
                corner.set(0, slot.top, offsetX, slot.top + offsetY);
            } else {
                corner.set(slot.right - offsetX, slot.top, slot.right, slot.top + offsetY);
            }
        } else {
            corner.set(slot.right - ((int) Math.max((float) this.mCheckedFrameIcon.getWidth(), ((float) slot.width()) * 0.5f)), slot.bottom - ((int) Math.max((float) this.mCheckedFrameIcon.getHeight(), ((float) slot.height()) * 0.5f)), slot.right, slot.bottom);
        }
        return corner.contains((int) x, (int) (((float) scrollPosition) + y));
    }

    private static void drawMiddleCenterIcon(GLCanvas canvas, Texture icon, int width, int height, TimeBucketPageViewMode viewMode) {
        int scale;
        if (viewMode == TimeBucketPageViewMode.MONTH) {
            scale = 2;
        } else {
            scale = 1;
        }
        icon.draw(canvas, (width - (icon.getWidth() / scale)) / 2, (height - (icon.getHeight() / scale)) / 2, icon.getWidth() / scale, icon.getHeight() / scale);
    }

    private static void drawLeftBottomIcon(GLCanvas canvas, Texture icon, int xDelta, int yDelta, int width, int height) {
        int w = icon.getWidth();
        int h = icon.getHeight();
        icon.draw(canvas, xDelta, (height - h) - yDelta, w, h);
    }

    private void drawRightBottomIcon(GLCanvas canvas, Texture texture, int width, int height) {
        int iconTop = (height - texture.getHeight()) - this.mBottomIconMargin;
        int iconLeft = (width - texture.getWidth()) - this.mBottomIconMargin;
        canvas.translate((float) iconLeft, (float) iconTop);
        drawContent(canvas, texture, texture.getWidth(), texture.getHeight(), 0, canvas.getAlpha());
        canvas.translate((float) (-iconLeft), (float) (-iconTop));
    }

    protected void drawContent(GLCanvas canvas, Texture content, int width, int height, int rotation, float alpha) {
        canvas.save(-1);
        height = Math.min(width, height);
        width = height;
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

    protected static void drawFrame(GLCanvas canvas, Rect padding, Texture frame, int x, int y, int width, int height) {
        frame.draw(canvas, x - padding.left, y - padding.top, (padding.left + width) + padding.right, (padding.top + height) + padding.bottom);
    }

    public void onContentChanged() {
        this.mSlotView.invalidate();
    }

    public void onSizeChanged(int size, ArrayList<AbsGroupData> groupDatas, TimeBucketPageViewMode mode) {
        this.mSizeChanged = true;
        this.mViewMode = mode;
        this.mSlotView.updateCountAndMode(groupDatas, mode);
        this.mBaseSpec = this.mDataWindow.getCurrentSpec();
        if (this.mUpdateFocus && this.mFocusIndex != null) {
            this.mSlotView.setCenterIndex(this.mFocusIndex);
            resetFocusIndex();
        }
        this.mSlotView.invalidate();
    }

    public void resume() {
        this.mDataWindow.resume();
    }

    public void pause(boolean needFreeSlotContent) {
        this.mDataWindow.pause(needFreeSlotContent);
    }

    public void destroy() {
        this.mDataWindow.destroy();
    }

    public boolean isTitleEntryAddressDrew(ItemCoordinate index) {
        TitleEntry titleEntry = getTitleEntryByItemCoordinate(index);
        if (titleEntry == null) {
            return false;
        }
        return titleEntry.isAddressDrew();
    }

    public RectF getAddressRect(ItemCoordinate index) {
        TitleEntry titleEntry = getTitleEntryByItemCoordinate(index);
        if (titleEntry == null || titleEntry.mAddressRect == null) {
            return null;
        }
        this.mStartToEndDate = titleEntry.getTimeTitle();
        return titleEntry.mAddressRect;
    }

    private TitleEntry getTitleEntryByItemCoordinate(ItemCoordinate index) {
        if (this.mDataWindow.isActiveSlot(index) && index.isTitle()) {
            return (TitleEntry) this.mDataWindow.get(index);
        }
        return null;
    }

    public String getStartToEndDate() {
        return this.mStartToEndDate;
    }

    public void onActiveTextureReady() {
        if (this.mSizeChanged) {
            this.mSizeChanged = false;
            this.mSlotView.startDeleteSlotAnimationIfNeed();
        }
    }

    public TimeBucketPageViewMode getViewMode() {
        return this.mViewMode;
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

    public Path getItemPath(Object index) {
        ItemCoordinate targetIndex = (ItemCoordinate) index;
        if (this.mDataWindow == null || !this.mDataWindow.isActiveSlot(targetIndex)) {
            return null;
        }
        BaseEntry baseEntry = this.mDataWindow.get(targetIndex);
        if (baseEntry == null) {
            return null;
        }
        return baseEntry.path;
    }

    public void setSlotFilter(SlotFilter slotFilter) {
        this.mSlotFilter = slotFilter;
    }

    protected boolean acceptSlot(ItemCoordinate index) {
        if (this.mSlotFilter == null || index.isTitle()) {
            return true;
        }
        return this.mSlotFilter.acceptSlot(this.mDataWindow.getItemIndex(index));
    }

    protected boolean needOverlayWhenRendTitle() {
        return true;
    }

    protected TitleSpec getTitleSpec(Context context) {
        return Config$LocalCameraAlbumPage.get(context).titleLabelSpec;
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
