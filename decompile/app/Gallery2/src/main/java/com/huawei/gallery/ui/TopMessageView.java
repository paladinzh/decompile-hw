package com.huawei.gallery.ui;

import android.content.Context;
import android.text.TextPaint;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.BasicTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.ProgressbarDelegate.ScreenNailRoot;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.util.GalleryUtils;

public class TopMessageView extends GLView implements ScreenNailRoot {
    private static final int GAP_SIZE = GalleryUtils.dpToPixel(8);
    private static final int PROGRESS_SIZE = GalleryUtils.dpToPixel(16);
    private int mBackgroundColor;
    private boolean mIsGone = false;
    private boolean mIsUploading = false;
    private boolean mLayoutRTL = GalleryUtils.isLayoutRTL();
    private int mMarginBottom = GalleryUtils.dpToPixel(5);
    private int mMarginH = GalleryUtils.dpToPixel(16);
    private String mMessage;
    private int mMessageLimitSize;
    private StringTexture mMessageTexture;
    private int mPrivateFlag = -1;
    private ScreenNail mProgressBarScreenNail;
    private TextPaint mTextPaint;
    private TextPaint mTextPaintWithShadow;

    public TopMessageView(Context context) {
        int labelColor = context.getResources().getColor(R.color.top_message_text_color);
        int labelSize = GalleryUtils.dpToPixel(13);
        this.mTextPaint = StringTexture.getDefaultPaint((float) labelSize, labelColor);
        this.mTextPaintWithShadow = StringTexture.getDefaultPaint((float) labelSize, labelColor);
        this.mTextPaintWithShadow.setShadowLayer(2.0f, 0.0f, 0.0f, labelColor);
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        boolean z = true;
        super.onLayout(changeSize, left, top, right, bottom);
        if (left < right && top < bottom) {
            z = false;
        }
        this.mIsGone = z;
        this.mMessageLimitSize = (right - left) - (this.mMarginH * 2);
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
    }

    public int getRootBgColor() {
        return this.mBackgroundColor;
    }

    public void invalidate() {
        if (!this.mIsGone) {
            super.invalidate();
        }
    }

    protected void render(GLCanvas canvas) {
        if (!this.mIsGone) {
            super.render(canvas);
            if (this.mPrivateFlag != 0) {
                updateTexture();
            }
            int w = getWidth();
            int h = getHeight();
            canvas.save();
            canvas.fillRect(0.0f, 0.0f, (float) w, (float) h, this.mBackgroundColor);
            int top = h - this.mMarginBottom;
            ScreenNail screenNail = this.mIsUploading ? this.mProgressBarScreenNail : null;
            BasicTexture messageTexture = this.mMessageTexture;
            int contentLength = 0;
            if (screenNail != null) {
                contentLength = screenNail.getWidth() + GAP_SIZE;
            }
            if (messageTexture != null) {
                int height = messageTexture.getHeight();
                int width = messageTexture.getWidth();
                contentLength += width;
                int left = (w - contentLength) / 2;
                int i;
                if (this.mLayoutRTL) {
                    if (screenNail != null) {
                        screenNail.draw(canvas, (w - PROGRESS_SIZE) - left, top - height, PROGRESS_SIZE, PROGRESS_SIZE);
                    }
                    canvas.drawTexture(messageTexture, (w - contentLength) - left, top - height, width, height);
                    i = left;
                } else {
                    if (screenNail != null) {
                        screenNail.draw(canvas, left, top - height, PROGRESS_SIZE, PROGRESS_SIZE);
                        i = (PROGRESS_SIZE + left) + GAP_SIZE;
                    } else {
                        i = left;
                    }
                    canvas.drawTexture(messageTexture, i, top - height, width, height);
                }
            }
            canvas.restore();
        }
    }

    public void setProgressbar(ScreenNail screenNail) {
        this.mProgressBarScreenNail = screenNail;
    }

    public void setUploading(boolean uploading) {
        this.mIsUploading = uploading;
        invalidate();
    }

    public void setMessage(String message) {
        if (message == null || !message.equals(this.mMessage)) {
            this.mMessage = message;
            this.mPrivateFlag |= 1;
            invalidate();
        }
    }

    private void updateTexture() {
        int changeFlags = this.mPrivateFlag;
        this.mPrivateFlag = 0;
        if ((changeFlags & 1) != 0) {
            changeMessageTexture();
        }
    }

    private void changeMessageTexture() {
        StringTexture texture = this.mMessageTexture;
        this.mMessageTexture = generateStringTexture(this.mMessage);
        recycleTexture(texture);
    }

    private StringTexture generateStringTexture(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        return StringTexture.newInstance(content, (float) this.mMessageLimitSize, this.mTextPaint);
    }

    private void recycleTexture(BasicTexture texture) {
        if (texture != null) {
            texture.recycle();
        }
    }
}
