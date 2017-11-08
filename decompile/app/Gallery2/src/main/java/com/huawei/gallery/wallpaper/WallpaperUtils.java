package com.huawei.gallery.wallpaper;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperUtils {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<File> queryDownloadedWallpaper(ContentResolver contentResolver) {
        List<File> list = new ArrayList();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable cursor = contentResolver2.query(WallpaperConstant.URI_THEMEINFO, new String[]{"name"}, "type = ? and status = ?", new String[]{String.valueOf(2), String.valueOf(6)}, "startTime ASC");
            GalleryLog.d("WallpaperUtils", String.format("type = %s and status = %s", new Object[]{String.valueOf(2), String.valueOf(6)}));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    File file = new File(getDownloadFileFolderAbsolutePath(), cursor.getString(0));
                    if (file.exists()) {
                        list.add(file);
                    }
                }
            }
            Utils.closeSilently(cursor);
        } catch (RuntimeException e) {
            GalleryLog.d("WallpaperUtils", "RuntimeException has happened when getting download information: " + e.getMessage());
        } catch (Exception e2) {
            GalleryLog.d("WallpaperUtils", "Getting download information failure: " + e2.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return list;
    }

    public static boolean queryDownloadedWallpaperByMediaItem(ContentResolver contentResolver, MediaItem mediaItem) {
        File downloadFileFolderAbsolutePath = getDownloadFileFolderAbsolutePath();
        if (downloadFileFolderAbsolutePath == null || !mediaItem.getFilePath().contains(downloadFileFolderAbsolutePath.toString() + "/HWWallpapers")) {
            return false;
        }
        boolean isDownloadFile = false;
        String targetName = String.format("HWWallpapers/%s.jpg", new Object[]{mediaItem.getName()});
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable cursor = contentResolver2.query(WallpaperConstant.URI_THEMEINFO, new String[]{"name"}, "type = ? and status = ? and name = ?", new String[]{String.valueOf(2), String.valueOf(6), targetName}, "startTime ASC");
            GalleryLog.d("WallpaperUtils", String.format("type = %s and status = %s and name = %s", new Object[]{String.valueOf(2), String.valueOf(6), targetName}));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (targetName.equals(cursor.getString(0))) {
                        isDownloadFile = true;
                    }
                }
            }
            Utils.closeSilently(cursor);
            return isDownloadFile;
        } catch (RuntimeException e) {
            GalleryLog.d("WallpaperUtils", "RuntimeException has happened when getting download information: " + e.getMessage());
            Utils.closeSilently(null);
            return false;
        } catch (Exception e2) {
            GalleryLog.d("WallpaperUtils", "Getting download information failure: " + e2.getMessage());
            Utils.closeSilently(null);
            return false;
        } catch (Throwable th) {
            Utils.closeSilently(null);
            return false;
        }
    }

    public static void deleteDownloadedWallpaper(ContentResolver contentResolver, File file) {
        if (file != null) {
            String selection = "_data=?";
            Uri baseUri = Media.EXTERNAL_CONTENT_URI;
            try {
                GalleryLog.d("WallpaperUtils", "drop data:>>>" + file.getName() + ">>>" + contentResolver.delete(WallpaperConstant.URI_THEMEINFO, "name = ?", new String[]{file.getName()}));
                contentResolver.delete(baseUri, selection, new String[]{file.getAbsolutePath()});
                if (file.exists()) {
                    GalleryLog.d("WallpaperUtils", "Download wallpaper delete: " + file.delete());
                }
            } catch (Exception e) {
                GalleryLog.w("WallpaperUtils", "delete download db failed. " + e.getMessage());
            }
        }
    }

    public static File getDownloadFileFolderAbsolutePath() {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage == null) {
            return null;
        }
        return new File(innerGalleryStorage.getPath(), "HWThemes");
    }
}
