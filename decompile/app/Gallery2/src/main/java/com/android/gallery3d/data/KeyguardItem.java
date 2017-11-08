package com.android.gallery3d.data;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.provider.MediaStore.Images.Media;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.extfile.FyuseFile;
import java.io.Closeable;
import java.io.FileNotFoundException;

public class KeyguardItem extends MediaItem {
    static final Path ITEMPATH = Path.fromString("/keyguard/item");
    static final String[] PROJECTION = new String[]{"_id", "path", "name", "date_modified", "isCustom", "isNew", "isHidden", "width", "height", "isFavorite", "picFormat"};
    public String caption;
    public long dateModifiedInSec;
    public String filePath;
    public int height;
    public int id;
    public int isHidden;
    public int isKeyguardLike;
    public int isLatest;
    private final GalleryApp mApplication;
    public int picFormat = -1;
    public int rotation;
    public int type;
    public int width;

    public static class KeyguardImageRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        public KeyguardImageRequest(GalleryApp application, Path path, long timeModified, int type, String localFilePath) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type));
            this.mLocalFilePath = localFilePath;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                DrmUtils.inDrmMode(options);
            }
            if (type == 2) {
                if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                    DrmUtils.inPreviewMode(options);
                } else {
                    ExifInterface exif = new ExifInterface();
                    byte[] thumbData = null;
                    try {
                        exif.readExif(this.mLocalFilePath);
                        thumbData = exif.getThumbnail();
                    } catch (FileNotFoundException e) {
                        GalleryLog.w("Keyguard_Item", "failed to find file to read thumbnail: " + this.mLocalFilePath);
                    } catch (Throwable t) {
                        GalleryLog.w("Keyguard_Item", "fail to get exif thumb: " + this.mLocalFilePath + "." + t.getMessage());
                    }
                    if (thumbData != null) {
                        Bitmap bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            }
            return DecodeUtils.decodeThumbnail(jc, this.mLocalFilePath, options, targetSize, type);
        }

        public String workContent() {
            return "decode thumnail from file: " + this.mLocalFilePath;
        }
    }

    public static class KeyguardLargeImageRequest extends BaseJob<BitmapRegionDecoder> {
        String mLocalFilePath;

        public KeyguardLargeImageRequest(String localFilePath) {
            this.mLocalFilePath = localFilePath;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            return DecodeUtils.createBitmapRegionDecoder(jc, this.mLocalFilePath, false);
        }

        public String workContent() {
            return "create region decoder for " + this.mLocalFilePath;
        }
    }

    public KeyguardItem(GalleryApp application, Path path, Cursor cursor) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        loadFromCursor(cursor);
    }

    public KeyguardItem(Path path, GalleryApp application, int id) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        Cursor cursor = KeyguardSet.getItemCursor(this.mApplication.getContentResolver(), Keyguard.URI, PROJECTION, id);
        if (cursor == null) {
            throw new RuntimeException("cannot get cursor for: " + path);
        }
        try {
            if (cursor.moveToNext()) {
                loadFromCursor(cursor);
                return;
            }
            throw new RuntimeException("cannot find data for: " + path);
        } finally {
            cursor.close();
        }
    }

    private void loadFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.caption = cursor.getString(2);
        this.filePath = cursor.getString(1);
        this.dateModifiedInSec = cursor.getLong(3);
        this.type = cursor.getInt(4);
        this.isLatest = cursor.getInt(5);
        this.isHidden = cursor.getInt(6);
        this.width = cursor.getInt(7);
        this.height = cursor.getInt(8);
        this.isKeyguardLike = cursor.getInt(9);
        setRotation();
        this.picFormat = cursor.getInt(10);
    }

    private boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(0));
        this.caption = (String) uh.update(this.caption, cursor.getString(2));
        this.filePath = (String) uh.update(this.filePath, cursor.getString(1));
        this.dateModifiedInSec = uh.update(this.dateModifiedInSec, cursor.getLong(3));
        this.type = uh.update(this.type, cursor.getInt(4));
        this.isLatest = uh.update(this.isLatest, cursor.getInt(5));
        this.isHidden = uh.update(this.isHidden, cursor.getInt(6));
        this.width = uh.update(this.width, cursor.getInt(7));
        this.height = uh.update(this.height, cursor.getInt(8));
        this.isKeyguardLike = uh.update(this.isKeyguardLike, cursor.getInt(9));
        this.picFormat = uh.update(this.picFormat, cursor.getInt(10));
        return uh.isUpdated();
    }

    public void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            setRotation();
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
    }

    public Job<Bitmap> requestImage(int type) {
        return new KeyguardImageRequest(this.mApplication, this.mPath, this.dateModifiedInSec, type, this.filePath);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new KeyguardLargeImageRequest(this.filePath);
    }

    public Bitmap getScreenNailBitmap(int type) {
        if (type != 1) {
            return null;
        }
        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
        try {
            if (this.mApplication.getImageCacheService().getImageData(this.mPath, this.dateModifiedInSec, type, buffer)) {
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer.data, buffer.offset, buffer.length, options);
                if (bitmap != null) {
                    updateWidthAndHeight();
                    return bitmap;
                }
                MediaItem.getBytesBufferPool().recycle(buffer);
                return null;
            }
            MediaItem.getBytesBufferPool().recycle(buffer);
            return null;
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
    }

    private void updateWidthAndHeight() {
        if (this.width <= 0 || this.height <= 0) {
            Point bounds = GalleryUtils.decodeBounds(this.filePath);
            if (bounds != null) {
                GalleryLog.v("Keyguard_Item", "decode bounds for keyguard.");
                this.width = bounds.x;
                this.height = bounds.y;
            }
        }
    }

    public String getName() {
        return this.caption;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public long getDateModifiedInSec() {
        return this.dateModifiedInSec;
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        this.mApplication.getContentResolver().delete(Keyguard.URI, "_id=?", new String[]{String.valueOf(this.id)});
    }

    public int getSupportedOperations() {
        return 65;
    }

    public String getMimeType() {
        return "image";
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getRotation() {
        return this.rotation;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setRotation() {
        try {
            Closeable c = this.mApplication.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"orientation"}, " _data = ?", new String[]{this.filePath}, null);
            if (c != null && c.moveToNext()) {
                this.rotation = c.getInt(0);
            }
            Utils.closeSilently(c);
        } catch (Exception e) {
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    public int getExtraTag() {
        int flags = 0;
        if (this.isLatest == 1) {
            flags = 1;
        }
        if (this.isHidden == 1) {
            flags |= 2;
        }
        if (this.isKeyguardLike == 1) {
            return flags | 4;
        }
        return flags;
    }

    public int getSpecialFileType() {
        return is3DPanorama() ? 20 : 0;
    }

    public boolean is3DPanorama() {
        if (FyuseFile.isSupport3DPanoramaSDK() && this.picFormat == 11) {
            return true;
        }
        return false;
    }

    public void setAsKeyguardLike(Context context) {
        GalleryUtils.assertNotInRenderThread();
        Keyguard.updateKeyguardLikeFlag(context.getContentResolver(), this.id, true);
    }

    public void cancelKeyguardLike(Context context) {
        GalleryUtils.assertNotInRenderThread();
        Keyguard.updateKeyguardLikeFlag(context.getContentResolver(), this.id, false);
    }

    public void updateItemWidthAndHeight(Context context, int width, int height) {
        GalleryUtils.assertNotInRenderThread();
        Keyguard.updateItemWidthAndHeight(context.getContentResolver(), this.id, width, height);
    }
}
