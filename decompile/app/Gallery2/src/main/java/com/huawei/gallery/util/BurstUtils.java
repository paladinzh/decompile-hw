package com.huawei.gallery.util;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.FilterSource;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.media.GalleryMedia;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BurstUtils {
    public static final Pattern BURST_PATTERN = Pattern.compile("([^/]*)_BURST(\\d{3})_COVER.JPG$");
    public static final Pattern BURST_PATTERN_OTHERS = Pattern.compile("(([^/]*)_BURST)(\\d{3})$");

    public static Path getBurstSetPath(String displayName, int bucketId, boolean isCloudData, ContentResolver contentResolver) {
        boolean z = true;
        if (displayName == null) {
            return null;
        }
        String filePathUpper = displayName.toUpperCase(Locale.getDefault());
        if (!filePathUpper.endsWith("_COVER.JPG")) {
            return null;
        }
        Matcher matchedCover = BURST_PATTERN.matcher(filePathUpper);
        if (!matchedCover.find()) {
            return null;
        }
        Path setPath;
        String burstId = matchedCover.group(1);
        if (isBurst(bucketId, burstId, contentResolver, isCloudData)) {
            if (isCloudData) {
                z = false;
            }
            setPath = FilterSource.getSetPath(bucketId, burstId, z);
        } else {
            setPath = null;
        }
        return setPath;
    }

    private static boolean isBurst(int bucketId, String burstId, ContentResolver contentResolver, boolean isCloudData) {
        Uri uri;
        if (isCloudData) {
            try {
                uri = GalleryMedia.URI;
            } catch (Throwable th) {
                GalleryLog.w("BurstUtils", "query fail." + th.getMessage());
            } finally {
                Utils.closeSilently(null);
            }
        } else {
            uri = Media.EXTERNAL_CONTENT_URI;
        }
        ContentResolver contentResolver2 = contentResolver;
        Closeable cursor = contentResolver2.query(uri, new String[]{"count(*)"}, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG' ", new String[]{String.valueOf(bucketId), burstId}, null);
        if (cursor == null || !cursor.moveToNext() || cursor.getInt(0) < 2) {
            Utils.closeSilently(cursor);
            return false;
        }
        Utils.closeSilently(cursor);
        return true;
    }

    public static boolean isAllBurstFileUploaded(int bucketId, String burstId, ContentResolver contentResolver) {
        Closeable closeable = null;
        try {
            ContentResolver contentResolver2 = contentResolver;
            closeable = contentResolver2.query(GalleryMedia.URI, new String[]{"count(*)"}, "bucket_id = ? AND _display_name LIKE ?||'_BURST%.JPG'  AND cloud_media_id = -1", new String[]{String.valueOf(bucketId), burstId}, null);
            if (closeable != null && closeable.moveToNext() && closeable.getInt(0) == 0) {
                return true;
            }
            Utils.closeSilently(closeable);
            return false;
        } catch (Throwable th) {
            GalleryLog.w("BurstUtils", "query fail." + th.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static ArrayList<Path> getPathFromBurstCover(MediaObject mo, DataManager manager, int action) {
        ArrayList<Path> singlePath = new ArrayList();
        if (mo == null) {
            return singlePath;
        }
        singlePath.add(mo.getPath());
        if (!(mo instanceof MediaItem)) {
            return singlePath;
        }
        MediaItem mi = (MediaItem) mo;
        if (!mi.isBurstCover()) {
            return singlePath;
        }
        ArrayList<Path> extendsPaths = new ArrayList();
        extendsPaths.add(mi.getBurstSetPath());
        return extendsPaths;
    }
}
