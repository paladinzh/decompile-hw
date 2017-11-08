package com.huawei.gallery.story.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextPaint;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.StringStaticLayoutTexture;
import com.android.gallery3d.ui.StringTexture;
import com.android.gallery3d.ui.Texture;
import com.android.gallery3d.ui.TiledTexture;
import com.android.gallery3d.ui.TiledTexture.Uploader;
import com.huawei.gallery.app.CommonAlbumSetDataLoader;
import com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow;
import com.huawei.gallery.ui.CommonAlbumSlotView.Layout;
import com.huawei.watermark.manager.parse.WMElement;

public class StoryAlbumSetSlotSlidingWindow extends CommonAlbumSetSlotSlidingWindow {
    private TextPaint mAlbumMainNamePaint;
    private TextPaint mAlbumSubNamePaint;
    private Layout mLayout;
    private Rect mTempRect = new Rect();
    private Uploader mUpload;

    public static class AlbumSetEntry extends com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry {
        public StringStaticLayoutTexture albumMainNameTexture;
        public StringStaticLayoutTexture albumSubNameTexture;
    }

    public StoryAlbumSetSlotSlidingWindow(GalleryContext activity, CommonAlbumSetDataLoader source, int cacheSize) {
        super(activity, source, cacheSize);
        Resources r = this.mContext.getResources();
        this.mAlbumMainNamePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_albumSet_main_name_size_big), r.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumMainNamePaint.setShadowLayer(12.0f, 0.0f, 2.0f, Color.argb(76, 0, 0, 0));
        this.mAlbumSubNamePaint = StringTexture.getDefaultPaint((float) r.getDimensionPixelSize(R.dimen.story_tag_albumSet_sub_name_size), r.getColor(R.color.photoshare_tag_albumSet_name_color));
        this.mAlbumSubNamePaint.setShadowLayer(12.0f, 0.0f, 2.0f, Color.argb(76, 0, 0, 0));
    }

    public void setLayout(Layout layout) {
        this.mLayout = layout;
    }

    public void setGLRoot(GLRoot glRoot) {
        super.setGLRoot(glRoot);
        this.mUpload = new Uploader(glRoot);
    }

    protected com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry[] createDataArray(int cacheSize) {
        return new AlbumSetEntry[cacheSize];
    }

    protected com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry createData() {
        return new AlbumSetEntry();
    }

    protected void uploadBgTextureInSlot(int index) {
        if (index < this.mContentEnd && index >= this.mContentStart) {
            AlbumSetEntry entry = this.mData[index % this.mData.length];
            addTextureToUpload(entry.content);
            if (this.mTextureUploader != null) {
                if (entry.albumSubNameTexture != null) {
                    this.mTextureUploader.addBgTexture(entry.albumSubNameTexture);
                }
                if (entry.albumMainNameTexture != null) {
                    this.mTextureUploader.addBgTexture(entry.albumMainNameTexture);
                }
            }
        }
    }

    protected void updateTextureUploadQueue() {
        if (this.mIsActive && this.mUpload != null && this.mTextureUploader != null) {
            int i;
            this.mUpload.clear();
            this.mTextureUploader.clear();
            int n = this.mActiveEnd;
            for (i = this.mActiveStart; i < n; i++) {
                AlbumSetEntry entry = this.mData[i % this.mData.length];
                addTextureToUpload(entry.content);
                if (entry.albumSubNameTexture != null) {
                    this.mTextureUploader.addFgTexture(entry.albumSubNameTexture);
                }
                if (entry.albumMainNameTexture != null) {
                    this.mTextureUploader.addFgTexture(entry.albumMainNameTexture);
                }
            }
            int range = Math.max(this.mContentEnd - this.mActiveEnd, this.mActiveStart - this.mContentStart);
            for (i = 0; i < range; i++) {
                uploadBgTextureInSlot(this.mActiveEnd + i);
                uploadBgTextureInSlot((this.mActiveStart - i) - 1);
            }
        }
    }

    protected void freeAlbumNameTexture(com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry) {
        super.freeAlbumNameTexture(entry);
        if (entry instanceof AlbumSetEntry) {
            AlbumSetEntry entry1 = (AlbumSetEntry) entry;
            if (entry1.albumMainNameTexture != null) {
                entry1.albumMainNameTexture.recycle();
            }
            if (entry1.albumSubNameTexture != null) {
                entry1.albumSubNameTexture.recycle();
            }
        }
    }

    protected void updateAlbumNameTexture(com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry) {
        super.updateAlbumNameTexture(entry);
        if (entry instanceof AlbumSetEntry) {
            AlbumSetEntry entry1 = (AlbumSetEntry) entry;
            int index = entry1.index;
            if (entry1.album != null) {
                entry1.albumMainNameTexture = getStringStaticLayoutTextureInstance(entry1.album.getName(), getMainNamePaint(index), getTextWidth(index), getTextHeight(index));
                entry1.albumSubNameTexture = getStringStaticLayoutTextureInstance(entry1.album.getSubName(), this.mAlbumSubNamePaint, getTextWidth(index), getTextHeight(index));
            }
        }
    }

    private StringStaticLayoutTexture getStringStaticLayoutTextureInstance(String text, TextPaint paint, int width, int height) {
        Rect rect = StringStaticLayoutTexture.dealWithLayout(text, paint, width, height, 2);
        return new StringStaticLayoutTexture(text, paint, rect.width(), rect.height(), 2);
    }

    protected void freeBitmapTexture(com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry) {
        if (entry instanceof AlbumSetEntry) {
            AlbumSetEntry entry1 = (AlbumSetEntry) entry;
            if (entry1.content instanceof TiledTexture) {
                entry1.content.recycle();
            }
        }
    }

    private Bitmap cropBitmap(int index, Bitmap bitmap, int rotation) {
        if (this.mLayout == null || bitmap == null) {
            return bitmap;
        }
        this.mTempRect = this.mLayout.getSlotRect(index, this.mTempRect);
        int w = this.mTempRect.width();
        int h = this.mTempRect.height();
        if (w == 0 || h == 0) {
            return bitmap;
        }
        if (rotation % 90 != 0 || rotation % 180 == 0) {
            this.mTempRect.set(0, 0, w, h);
        } else {
            this.mTempRect.set(0, 0, h, w);
        }
        float scale = Math.min(WMElement.CAMERASIZEVALUE1B1, Math.min(((float) bitmap.getWidth()) / ((float) this.mTempRect.width()), ((float) bitmap.getHeight()) / ((float) this.mTempRect.height())));
        w = (int) (((float) this.mTempRect.width()) * scale);
        h = (int) (((float) this.mTempRect.height()) * scale);
        scale = Math.max(WMElement.CAMERASIZEVALUE1B1, Math.max(((float) w) / 512.0f, ((float) h) / 512.0f));
        Matrix matrix = new Matrix();
        matrix.setScale(WMElement.CAMERASIZEVALUE1B1 / scale, WMElement.CAMERASIZEVALUE1B1 / scale);
        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() / 2) - (w / 2), (bitmap.getHeight() / 2) - (h / 2), w, h, matrix, false);
    }

    protected void updateBitmapTexture(com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry, Bitmap bitmap) {
        if (entry instanceof AlbumSetEntry) {
            AlbumSetEntry entry1 = (AlbumSetEntry) entry;
            Bitmap cropBitmap = cropBitmap(entry.index, bitmap, entry.rotation);
            if (cropBitmap != null) {
                entry1.content = new TiledTexture(cropBitmap);
            }
        }
    }

    protected void uploadTexture(com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry) {
        if (entry instanceof AlbumSetEntry) {
            AlbumSetEntry entry1 = (AlbumSetEntry) entry;
            if (this.mTextureUploader != null) {
                addTextureToUpload(entry1.content);
                if (entry1.albumSubNameTexture != null) {
                    this.mTextureUploader.addFgTexture(entry1.albumSubNameTexture);
                }
                if (entry1.albumMainNameTexture != null) {
                    this.mTextureUploader.addFgTexture(entry1.albumMainNameTexture);
                }
            }
        }
    }

    private void addTextureToUpload(Texture texture) {
        if (this.mUpload != null && (texture instanceof TiledTexture)) {
            this.mUpload.addTexture((TiledTexture) texture);
        }
    }

    protected boolean needForceStartTask() {
        return true;
    }

    private int getTextWidth(int index) {
        if (this.mListener != null) {
            return this.mListener.getSlotWidth(index);
        }
        return 1;
    }

    private int getTextHeight(int index) {
        if (this.mListener != null) {
            return this.mListener.getSlotHeight(index);
        }
        return 1;
    }

    protected void freeSlotContent() {
        super.freeSlotContent();
        if (this.mUpload != null) {
            this.mUpload.clear();
        }
    }

    private TextPaint getMainNamePaint(int index) {
        if (this.mListener != null) {
            TextPaint textPaint = this.mListener.getMainNamePaint(index);
            if (textPaint != null) {
                return textPaint;
            }
        }
        return this.mAlbumMainNamePaint;
    }

    private void updateTexture(int index) {
        com.huawei.gallery.ui.CommonAlbumSetSlotSlidingWindow.AlbumSetEntry entry = getAlbumSetEntry(index);
        if (entry != null) {
            freeAlbumNameTexture(entry);
            updateAlbumNameTexture(entry);
            freeBitmapTexture(entry);
            updateBitmapTexture(entry, entry.contentLoader.getBitmap());
            uploadBgTextureInSlot(index);
        }
    }

    public void updateTexture() {
        int i;
        for (i = this.mActiveStart; i <= this.mActiveEnd; i++) {
            updateTexture(i);
        }
        for (i = this.mActiveEnd + 1; i <= this.mContentEnd; i++) {
            updateTexture(i);
        }
        for (i = this.mContentStart; i < this.mActiveStart; i++) {
            updateTexture(i);
        }
    }

    protected void recycleLoaderBitmap(Bitmap bitmap, MediaItem mItem) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
}
