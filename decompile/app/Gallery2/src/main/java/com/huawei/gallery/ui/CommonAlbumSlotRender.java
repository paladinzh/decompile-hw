package com.huawei.gallery.ui;

import android.content.Context;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.NinePatchTexture;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;

public class CommonAlbumSlotRender extends AbstractCommonAlbumSlotRender {
    private static final int TAG_BG_OFFSET_TOP = GalleryUtils.dpToPixel(7);
    private static final int TAG_BG_OFFSET_WIDTH = GalleryUtils.dpToPixel(4);
    private static final int TAG_OFFSET_LEFT = GalleryUtils.dpToPixel(5);
    private final ResourceTexture mFavoriteTag;
    private final NinePatchTexture mTagBackground;
    private final int mTagBackgroundWidth = ((TAG_BG_OFFSET_WIDTH + this.mTagString.getWidth()) + (TAG_OFFSET_LEFT * 2));
    private final StringTexture mTagString;
    private final int mTagStringOffsetTop = ((TAG_BG_OFFSET_TOP + (this.mTagString.getHeight() / 10)) + ((this.mTagBackground.getHeight() - this.mTagString.getHeight()) / 2));

    public CommonAlbumSlotRender(GalleryContext context, CommonAlbumSlotView slotView, SelectionManager selectionManager, int placeholderColor) {
        super(context, slotView, selectionManager, placeholderColor);
        Context androidContext = context.getAndroidContext();
        this.mTagString = StringTexture.newInstance(androidContext.getString(R.string.hint_tag_new), (float) GalleryUtils.dpToPixel(9), -1);
        this.mTagBackground = new NinePatchTexture(androidContext, R.drawable.ic_pic_new);
        this.mFavoriteTag = new ResourceTexture(androidContext, R.drawable.ic_gallery_frame_overlay_favorite);
    }

    protected void renderOverlay(GLCanvas canvas, AlbumEntry entry, int width, int height) {
        if (!entry.isNoThumb && !entry.isCloudPlaceHolder) {
            if (entry.mediaType == 4 || entry.isVoiceImage || ((entry.isRefocusPhoto && (entry.item.getRefocusPhotoType() == 1 || WideAperturePhotoUtil.supportPhotoEdit())) || entry.isBurstCover || entry.isMyFavorite || entry.isRectifyImage || entry.is3DModelImage || entry.isLivePhoto || entry.recycleTime != -1)) {
                int top = height - BACKGROUND_COVER_HEIGHT;
                canvas.translate(0.0f, (float) top);
                drawFrame(canvas, this.mBackGroundCoverTexture.getPaddings(), this.mBackGroundCoverTexture, 0, 0, width, BACKGROUND_COVER_HEIGHT);
                canvas.translate(0.0f, (float) (-top));
            }
            int deltaX = this.mDefaultMarin;
            int deltaY = this.mDefaultMarin;
            if (entry.mediaType == 4) {
                drawLeftBottomIcon(canvas, this.mVideoPlayIcon, deltaX, deltaY, width, height);
                deltaX += this.mVideoPlayIcon.getWidth();
            }
            if (entry.isVoiceImage) {
                drawLeftBottomIcon(canvas, this.mVoiceImageIcon, deltaX, deltaY, width, height);
                deltaX += this.mVoiceImageIcon.getWidth();
            }
            if (entry.isRefocusPhoto) {
                if (entry.item.getRefocusPhotoType() == 1) {
                    drawLeftBottomIcon(canvas, this.mAllFocusPhotoIcon, deltaX, deltaY, width, height);
                    deltaX += this.mAllFocusPhotoIcon.getWidth();
                } else if (WideAperturePhotoUtil.supportPhotoEdit()) {
                    drawLeftBottomIcon(canvas, this.mWideAperturePhotoIcon, deltaX, deltaY, width, height);
                    deltaX += this.mWideAperturePhotoIcon.getWidth();
                }
            }
            if (entry.isBurstCover) {
                drawLeftBottomIcon(canvas, this.mBurstCoverIcon, deltaX, deltaY, width, height);
                deltaX += this.mBurstCoverIcon.getWidth();
            }
            if (entry.is3DPanorama) {
                drawLeftBottomIcon(canvas, this.m3DPanoramaIcon, deltaX, deltaY, width, height);
                deltaX += this.m3DPanoramaIcon.getWidth();
            }
            if (entry.isRectifyImage) {
                drawLeftBottomIcon(canvas, this.mRectifyImageIcon, deltaX, deltaY, width, height);
                deltaX += this.mRectifyImageIcon.getWidth();
            }
            if (entry.is3DModelImage) {
                drawLeftBottomIcon(canvas, this.m3DModelImageIcon, deltaX, deltaY, width, height);
                deltaX += this.m3DModelImageIcon.getWidth();
            }
            if (entry.isLivePhoto) {
                drawLeftBottomIcon(canvas, this.mLivePhotoIcon, deltaX, deltaY, width, height);
                deltaX += this.mLivePhotoIcon.getWidth();
            }
            if (entry.isMyFavorite) {
                drawLeftBottomIcon(canvas, this.mMyFavoriteIcon, deltaX, deltaY, width, height);
            }
            if (entry.isNewMagazine) {
                drawTag(canvas, width, height);
            }
            if (entry.isFavoriteMagazine) {
                drawLeftBottomIcon(canvas, this.mFavoriteTag, deltaX, deltaY, width, height);
            }
        }
    }

    private void drawTag(GLCanvas canvas, int width, int height) {
        this.mTagBackground.draw(canvas, 0, TAG_BG_OFFSET_TOP, this.mTagBackgroundWidth, this.mTagBackground.getHeight());
        this.mTagString.draw(canvas, TAG_OFFSET_LEFT, this.mTagStringOffsetTop);
    }
}
