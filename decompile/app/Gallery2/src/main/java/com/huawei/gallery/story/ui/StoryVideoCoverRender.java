package com.huawei.gallery.story.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.view.MotionEvent;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.StringStaticLayoutTexture;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.ui.UploadedTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class StoryVideoCoverRender {
    private final int ALBUM_NAME_START_AND_END_PADDING = GalleryUtils.dpToPixel(16);
    private final int HEIGHT = GalleryUtils.dpToPixel(40);
    private final int WIDTH = GalleryUtils.dpToPixel(40);
    private final TextPaint mAlbumMainNameMinFontPaint;
    private final TextPaint mAlbumMainNamePaint;
    private final TextPaint mAlbumSubNamePaint;
    private boolean mLayoutRTL = GalleryUtils.isLayoutRTL();
    private final Listener mListener;
    private UploadedTexture mMainTexture;
    private String mMainTitle;
    private volatile Rect mRect = new Rect();
    private StringTexture mSubTexture;
    private String mSubTitle;
    private int mTextLimitWidth = 0;
    private final ResourceTexture mVideoPlayIcon;

    public interface Listener {
        void onClickVideoButton();
    }

    public StoryVideoCoverRender(Context context, Listener listener) {
        this.mListener = listener;
        this.mVideoPlayIcon = new ResourceTexture(context, R.drawable.btn_video_play);
        Resources r = context.getResources();
        this.mAlbumMainNamePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_album_main_name_size), r.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumMainNamePaint.setShadowLayer(4.0f, 0.0f, 0.5f, -16777216);
        this.mAlbumMainNameMinFontPaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_album_main_name_min_size), r.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumMainNameMinFontPaint.setShadowLayer(4.0f, 0.0f, 0.5f, -16777216);
        this.mAlbumSubNamePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_album_sub_name_size), r.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumSubNamePaint.setShadowLayer(4.0f, 0.0f, 0.5f, -16777216);
    }

    public void setTextLimitWidth(int limit) {
        this.mTextLimitWidth = limit - (this.ALBUM_NAME_START_AND_END_PADDING * 2);
    }

    public void render(GLCanvas canvas, int left, int top, int right, int bottom) {
        this.mRect.set(left, top, right, bottom);
        canvas.save();
        canvas.setAlpha(0.25f);
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        canvas.fillRect((float) left, (float) top, (float) (right - left), (float) (bottom - top), -16777216);
        canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1);
        this.mVideoPlayIcon.draw(canvas, centerX - (this.WIDTH / 2), centerY - (this.HEIGHT / 2), this.WIDTH, this.HEIGHT);
        int bottomSlot = GalleryUtils.dpToPixel(10);
        if (this.mLayoutRTL) {
            GLCanvas gLCanvas;
            if (this.mMainTexture != null) {
                gLCanvas = canvas;
                this.mMainTexture.draw(gLCanvas, (right - this.ALBUM_NAME_START_AND_END_PADDING) - this.mMainTexture.getWidth(), ((bottom - bottomSlot) - this.mSubTexture.getHeight()) - this.mMainTexture.getHeight());
            }
            if (this.mSubTexture != null) {
                gLCanvas = canvas;
                this.mSubTexture.draw(gLCanvas, (right - this.ALBUM_NAME_START_AND_END_PADDING) - this.mSubTexture.getWidth(), (bottom - bottomSlot) - this.mSubTexture.getHeight());
            }
        } else {
            if (this.mMainTexture != null) {
                this.mMainTexture.draw(canvas, this.ALBUM_NAME_START_AND_END_PADDING + left, ((bottom - bottomSlot) - this.mSubTexture.getHeight()) - this.mMainTexture.getHeight());
            }
            if (this.mSubTexture != null) {
                this.mSubTexture.draw(canvas, this.ALBUM_NAME_START_AND_END_PADDING + left, (bottom - bottomSlot) - this.mSubTexture.getHeight());
            }
        }
        canvas.restore();
    }

    public void invalidateTitle() {
        if (this.mMainTexture != null) {
            this.mMainTexture.recycle();
        }
        if (this.mSubTexture != null) {
            this.mSubTexture.recycle();
        }
        if (Float.compare(this.mAlbumMainNamePaint.measureText(this.mMainTitle), (float) this.mTextLimitWidth) <= 0) {
            this.mMainTexture = StringTexture.newInstance(this.mMainTitle, (float) this.mTextLimitWidth, this.mAlbumMainNamePaint);
        } else if (Float.compare(this.mAlbumMainNameMinFontPaint.measureText(this.mMainTitle), (float) this.mTextLimitWidth) <= 0) {
            this.mMainTexture = StringTexture.newInstance(this.mMainTitle, (float) this.mTextLimitWidth, this.mAlbumMainNameMinFontPaint);
        } else {
            this.mMainTexture = new StringStaticLayoutTexture(this.mMainTitle, this.mAlbumMainNameMinFontPaint, Alignment.ALIGN_NORMAL, this.mTextLimitWidth, GalleryUtils.getFontHeightOfPaint(this.mAlbumMainNameMinFontPaint) * 2, 2);
        }
        this.mSubTexture = StringTexture.newInstance(this.mSubTitle, (float) this.mTextLimitWidth, this.mAlbumSubNamePaint);
    }

    public void updateTitle(String mainTitle, String subTitle) {
        this.mMainTitle = mainTitle;
        this.mSubTitle = subTitle;
    }

    public void onTouch(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        if (event.getAction() == 1 && eventX > ((float) (this.mRect.centerX() - (this.WIDTH / 2))) && eventX < ((float) (this.mRect.centerX() + (this.WIDTH / 2))) && eventY > ((float) (this.mRect.centerY() - (this.HEIGHT / 2))) && eventY < ((float) (this.mRect.centerY() + (this.HEIGHT / 2)))) {
            this.mListener.onClickVideoButton();
        }
    }
}
