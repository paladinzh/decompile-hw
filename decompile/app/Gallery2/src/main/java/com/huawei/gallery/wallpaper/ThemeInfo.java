package com.huawei.gallery.wallpaper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThemeInfo {
    public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.android.thememanager.ContentProvider/themeInfo");
    public static final String PATH_CACHE_THEME = ("HWThemes" + SLASH + ".cache" + SLASH);
    private static final String[] PROJECTION = new String[]{"title", "cn_title", "author", "designer", "screen", "version", "os_version", "font", "cn_font", "package_path", "package_name"};
    private static final String SLASH = File.separator;
    static FilenameFilter mHomeFilenameFilter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
            return filename.contains("home");
        }
    };
    private String mAuthor = "Google";
    private String mDesigner = "Google";
    private String mFont = "Batang";
    private String mFontCN = "";
    private String mOsversion = "4.0.3";
    private String mPackageName = "";
    private String mPackagePath = "";
    private String mScreen = "";
    private String mTitle = "Default";
    private String mTitleCN = "";
    private String mVersion = "1.0";

    public ThemeInfo(Cursor cursor) {
        this.mTitle = cursor.getString(cursor.getColumnIndex("title"));
        this.mTitleCN = cursor.getString(cursor.getColumnIndex("cn_title"));
        this.mAuthor = cursor.getString(cursor.getColumnIndex("author"));
        this.mDesigner = cursor.getString(cursor.getColumnIndex("designer"));
        this.mScreen = cursor.getString(cursor.getColumnIndex("screen"));
        this.mVersion = cursor.getString(cursor.getColumnIndex("version"));
        this.mOsversion = cursor.getString(cursor.getColumnIndex("os_version"));
        this.mFont = cursor.getString(cursor.getColumnIndex("font"));
        this.mFontCN = cursor.getString(cursor.getColumnIndex("cn_font"));
        this.mPackagePath = cursor.getString(cursor.getColumnIndex("package_path"));
        this.mPackageName = cursor.getString(cursor.getColumnIndex("package_name"));
    }

    public String getCacheInstalledPath() {
        String innerSdcard;
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage == null) {
            innerSdcard = Environment.getExternalStorageDirectory().getPath();
        } else {
            innerSdcard = innerGalleryStorage.getPath();
        }
        return innerSdcard + SLASH + PATH_CACHE_THEME + this.mPackageName + SLASH;
    }

    public String getWallpaperInstalledPath() {
        return getCacheInstalledPath() + "wallpaper" + SLASH;
    }

    public File[] getWallpaper() {
        File[] files = new File(getWallpaperInstalledPath()).listFiles(mHomeFilenameFilter);
        if (files == null) {
            return new File[0];
        }
        return files;
    }

    public static List<ThemeInfo> getThemeInstallInfo(Context context) {
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, PROJECTION, null, null, null);
        List<ThemeInfo> results = new ArrayList();
        if (cursor == null) {
            return results;
        }
        while (cursor.moveToNext()) {
            try {
                results.add(new ThemeInfo(cursor));
            } catch (Exception ex) {
                GalleryLog.i("themeInfo", "An exception has occurred." + ex.getMessage());
            } finally {
                cursor.close();
            }
        }
        return results;
    }

    public String toString() {
        return String.format(Locale.US, "ThemeInfo: title=%s, packageName=%s, packagePath=%s, installedCachecPath:%s ", new Object[]{this.mTitle, this.mPackageName, this.mPackagePath, getWallpaperInstalledPath()});
    }
}
