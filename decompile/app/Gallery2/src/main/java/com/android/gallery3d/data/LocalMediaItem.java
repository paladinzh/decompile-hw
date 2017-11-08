package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.media.database.CloudRecycleTableOperateHelper;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.report.TimeReporter;
import java.io.Closeable;
import java.io.File;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class LocalMediaItem extends MediaItem {
    public int bucketId;
    public String caption;
    public long dateAddedInSec;
    public long dateModifiedInSec;
    public long dateTakenInMs;
    public String displayName;
    public String filePath = "";
    public long fileSize;
    public int height;
    public int id;
    public boolean isDrm;
    protected int isMyFavorite = -1;
    public boolean is_hdr;
    public double latitude = 0.0d;
    public double longitude = 0.0d;
    protected final GalleryApp mApplication;
    public long mSpecialFileOffset;
    public int mSpecialFileType;
    public String mimeType;
    public int normalizedDate;
    public int width;

    protected abstract ContentValues getValues();

    protected abstract boolean updateFromCursor(Cursor cursor);

    public LocalMediaItem(Path path, long version, GalleryApp app) {
        super(path, version);
        this.mApplication = app;
    }

    public long getDateInMs() {
        return this.dateTakenInMs;
    }

    public long getDateModifiedInSec() {
        return this.dateModifiedInSec;
    }

    public String getName() {
        return this.caption;
    }

    public int getNormalizedDate() {
        return this.normalizedDate;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = this.latitude;
        latLong[1] = this.longitude;
    }

    protected void updateWidthAndHeight() {
    }

    public int getBucketId() {
        return this.bucketId;
    }

    public void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(SmsCheckResult.ESCT_200, this.filePath);
        details.addDetail(1, this.caption);
        details.addDetail(3, Long.valueOf(this.dateTakenInMs));
        details.addDetail(6, Integer.valueOf(this.width));
        details.addDetail(7, Integer.valueOf(this.height));
        if (GalleryUtils.isValidLocation(this.latitude, this.longitude)) {
            details.addDetail(4, new double[]{this.latitude, this.longitude});
        }
        if (this.fileSize > 0) {
            details.addDetail(5, Long.valueOf(this.fileSize));
        }
        return details;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public long getSize() {
        return this.fileSize;
    }

    public boolean isHdr() {
        return this.is_hdr;
    }

    public boolean isDrm() {
        return this.isDrm;
    }

    public boolean canForward() {
        return DrmUtils.canForward(this.filePath);
    }

    public boolean isMyFavorite() {
        TimeReporter.start("LocalMediaItem.isMyFavorite");
        if (-1 == this.isMyFavorite) {
            int i;
            if (this.mApplication.getGalleryData().isMyFavorite(this.filePath)) {
                i = 1;
            } else {
                i = 0;
            }
            this.isMyFavorite = i;
        }
        TimeReporter.end(3);
        if (this.isMyFavorite > 0) {
            return true;
        }
        return false;
    }

    public int getVirtualFlags() {
        int flag = 0;
        if (isMyFavorite()) {
            flag = 1;
        }
        if (isRectifyImage()) {
            flag |= 4;
        }
        if (is3DModelImage()) {
            return flag | 32;
        }
        return flag;
    }

    public void updateMyFavorite(boolean myFavorite) {
        this.isMyFavorite = myFavorite ? 1 : 0;
    }

    public void setAsFavorite(Context context) {
        GalleryUtils.assertNotInRenderThread();
        this.mApplication.getGalleryData().updateFavorite(this.filePath, true);
        this.mApplication.getDataManager().notifyChange(Constant.MYFAVORITE_URI);
        this.isMyFavorite = 1;
    }

    public void cancelFavorite(Context context) {
        GalleryUtils.assertNotInRenderThread();
        this.mApplication.getGalleryData().updateFavorite(this.filePath, false);
        this.mApplication.getDataManager().notifyChange(Constant.MYFAVORITE_URI);
        this.isMyFavorite = 0;
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
            Bitmap screenNailBitmap = super.getScreenNailBitmap(type);
            MediaItem.getBytesBufferPool().recycle(buffer);
            return screenNailBitmap;
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
    }

    public boolean rename(String newName) {
        if (PhotoShareUtils.isGUIDSupport() && callGalleryMediaRename(newName)) {
            return true;
        }
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = getContentUri();
        File oldFile = new File(this.filePath);
        String oldName = oldFile.getName();
        File newFile = new File(oldFile.getParent(), newName + oldName.substring(oldName.lastIndexOf(".")));
        if (oldFile.renameTo(newFile)) {
            if (isMyFavorite()) {
                GalleryData favoriteData = this.mApplication.getGalleryData();
                if (favoriteData.updateFavorite(this.filePath, false) == 0 || favoriteData.updateFavorite(newFile.getAbsolutePath(), true) == 0) {
                    GalleryLog.w("LocalMediaItem", "Constant.MYFAVORITE_URI update failure");
                }
            }
            ContentValues values = new ContentValues();
            values.put("_data", newFile.getAbsolutePath());
            values.put("title", newName);
            values.put("_display_name", newName + oldName.substring(oldName.lastIndexOf(".")));
            int updated = 0;
            try {
                updated = this.mApplication.getContentResolver().update(baseUri, values, null, null);
                if (FyuseFile.isSupport3DPanoramaAPK() && this.mSpecialFileType == 11) {
                    FyuseFile.updateRenamedPath(this.mApplication.getContentResolver(), oldFile, newFile, getSpecialFileType());
                }
            } catch (IllegalArgumentException e) {
                GalleryLog.e("LocalMediaItem", "IllegalArgumentExceptionN in mMediaProvider.update" + e.getMessage());
            } catch (SecurityException e2) {
                GalleryLog.noPermissionForMediaProviderLog("LocalMediaItem");
            }
            if (updated != 0) {
                return true;
            }
            GalleryLog.e("LocalMediaItem", "Unable to update name for " + oldName + " to " + newName);
            if (!newFile.renameTo(oldFile)) {
                GalleryLog.w("LocalMediaItem", "cannot rename");
            }
            return false;
        }
        GalleryLog.w("LocalMediaItem", "cannot rename filePath is illegal: " + this.filePath);
        return false;
    }

    private boolean callGalleryMediaRename(String newName) {
        boolean isImage = false;
        if (this instanceof GalleryMediaItem) {
            GalleryLog.w("LocalMediaItem", "rename GalleryMediaItem: " + this.filePath);
        } else {
            ContentValues values = getGalleryMediaItem(this.mApplication.getAndroidContext());
            if (values.containsKey("_id")) {
                if (values.getAsInteger("media_type").intValue() == 1) {
                    isImage = true;
                }
                ((GalleryMediaItem) this.mApplication.getDataManager().getMediaObject((isImage ? GalleryImage.IMAGE_PATH : GalleryVideo.VIDEO_PATH).getChild(values.getAsInteger("_id").intValue()))).rename(newName);
                return true;
            }
        }
        return false;
    }

    public void delete() {
        if (isMyFavorite()) {
            this.mApplication.getGalleryData().updateFavorite(this.filePath, false);
        }
        ContentResolver resolver = this.mApplication.getContentResolver();
        if (FyuseFile.isSupport3DPanorama() && !FyuseFile.startDeleteFyuseFile(resolver, getFilePath(), getSpecialFileType())) {
        }
    }

    public void recycle(SQLiteDatabase db, Bundle data) {
        ContentValues itemValue = getGalleryMediaItem(this.mApplication.getAndroidContext());
        int galleryId = itemValue.getAsInteger("_id").intValue();
        String uniqueId = itemValue.getAsString("uniqueId");
        GalleryLog.d("LocalMediaItem", "recycle local media item galleryId " + galleryId + ", uniqueId " + uniqueId);
        try {
            ContentValues values = getValues();
            if (PhotoShareUtils.isGUIDSupport() && PhotoShareUtils.getLocalSwitch() && !TextUtils.isEmpty(uniqueId)) {
                values.put("uniqueId", uniqueId);
            }
            LocalRecycledFile.insert(db, this.mApplication.getContentResolver(), galleryId, values, data);
            CloudRecycleTableOperateHelper.moveToRecycleBin(db, this.id, this.mApplication.getAndroidContext());
            delete();
        } catch (SQLiteException e) {
            throw new SQLiteException("recycle local media item error:" + e.getMessage());
        }
    }

    public int getId() {
        return this.id;
    }

    protected int getGalleryMediaId(Context context) {
        ContentValues values = getGalleryMediaItem(context);
        if (values.containsKey("_id")) {
            return values.getAsInteger("_id").intValue();
        }
        return -1;
    }

    public String getUniqueId() {
        String uniqueId = "0";
        ContentValues values = getGalleryMediaItem(this.mApplication.getAndroidContext());
        if (values.containsKey("uniqueId")) {
            return values.getAsString("uniqueId");
        }
        return uniqueId;
    }

    private ContentValues getGalleryMediaItem(Context context) {
        Closeable closeable = null;
        ContentValues values = new ContentValues();
        try {
            closeable = context.getContentResolver().query(GalleryMedia.URI, new String[]{"_id", "media_type", "uniqueId"}, "local_media_id=?", new String[]{this.mPath.getSuffix()}, null);
            if (closeable == null) {
                return values;
            }
            if (closeable.moveToNext()) {
                values.put("_id", Integer.valueOf(closeable.getInt(0)));
                values.put("media_type", Integer.valueOf(closeable.getInt(1)));
                values.put("uniqueId", closeable.getString(2));
            }
            Utils.closeSilently(closeable);
            return values;
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "query gallery_media err " + e.toString());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    protected void parseResolution(String resolution) {
        if (resolution != null) {
            int m = resolution.indexOf(120);
            if (m != -1) {
                try {
                    int w = Integer.parseInt(resolution.substring(0, m));
                    int h = Integer.parseInt(resolution.substring(m + 1));
                    this.width = w;
                    this.height = h;
                } catch (Throwable t) {
                    GalleryLog.w("LocalMediaItem", t);
                }
            }
        }
    }

    protected String checkFilePathNull(String filePath) {
        if (filePath != null) {
            return filePath;
        }
        GalleryLog.e("LocalMediaItem", "local media path is invaild, media item info: " + this);
        return "";
    }
}
