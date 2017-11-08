package com.android.gallery3d.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BucketHelper;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.provider.GalleryDBHelper;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class GalleryData {
    private static final String[] PROJECTION = new String[]{"_data", "is_favorite", "volume_id"};
    private int INDEX_DATA = 0;
    private int INDEX_IS_FAVORITE = 1;
    private int INDEX_VOLUME_ID = 2;
    private final Context mContext;
    private Set<String> mFilePaths = new HashSet(10);
    private final GalleryDBHelper mGalleryDB;
    private WhiteList mWhiteList;

    public static class FavoriteWhereClause {
        public String[] mPaths;
        public String mWhereClause;
    }

    public GalleryData(Context context) {
        this.mContext = context;
        this.mGalleryDB = new GalleryDBHelper(this.mContext, "gallery_data.db");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        synchronized (GalleryData.class) {
            long startTime = System.currentTimeMillis();
            if (pref.getBoolean("should-migrate", true)) {
                long startMigrate = System.currentTimeMillis();
                upgradeFavoriteData(this.mGalleryDB.getWritableDatabase());
                GalleryLog.d("GalleryData", " upgradeFavoriteData time cost: " + (System.currentTimeMillis() - startMigrate));
                pref.edit().putBoolean("should-migrate", false).commit();
            }
            GalleryLog.d("GalleryData", " total time cost: " + (System.currentTimeMillis() - startTime));
        }
        this.mWhiteList = WhiteList.getInstance();
    }

    private void upgradeFavoriteData(SQLiteDatabase db) {
        Uri uri = MergedMedia.URI.buildUpon().appendPath("media_file").build();
        Closeable closeable = null;
        try {
            ContentResolver resolver = this.mContext.getContentResolver();
            closeable = resolver.query(uri, PROJECTION, " 1=1 ", null, null);
            if (closeable == null) {
                GalleryLog.d("GalleryData", "there is not any old data.");
                return;
            }
            Set<String> newData = loadNewData();
            while (closeable.moveToNext()) {
                String filePath = closeable.getString(this.INDEX_DATA);
                if (newData.contains(filePath)) {
                    GalleryLog.d("GalleryData", "file added to favorite again already." + filePath);
                } else {
                    ContentValues values = new ContentValues();
                    values.put("_data", filePath);
                    values.put("is_favorite", Integer.valueOf(closeable.getInt(this.INDEX_IS_FAVORITE)));
                    values.put("volume_id", Integer.valueOf(closeable.getInt(this.INDEX_VOLUME_ID)));
                    this.mGalleryDB.insert("media_file", values);
                }
            }
            GalleryLog.d("GalleryData", resolver.delete(uri, null, null) + " record have been migrated");
            Utils.closeSilently(closeable);
        } catch (Throwable e) {
            GalleryLog.d("GalleryData", "migrated failed. " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private Set<String> loadNewData() {
        Set<String> ret = new HashSet(100);
        try {
            Closeable cursor = this.mGalleryDB.query("media_file", new String[]{"_data"}, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ret.add(cursor.getString(0));
                }
            }
            Utils.closeSilently(cursor);
            return ret;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    private static String getVolumeIdCacheDir(Context context, String data) {
        String[] volumePaths = GalleryUtils.getVolumePaths();
        if (data == null || volumePaths.length <= 0) {
            return null;
        }
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null && data.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX)) {
            data = innerGalleryStorage.getPath();
        }
        for (String path : volumePaths) {
            if (data.startsWith(path)) {
                return path + "/Android/data/" + context.getPackageName() + "/VolumeId";
            }
        }
        return null;
    }

    private static int getVolumeId(Context context, GalleryStorage galleryStorage) {
        String volumePath = null;
        String VolumeIdPath = null;
        if (galleryStorage != null) {
            volumePath = galleryStorage.getPath();
        }
        if (volumePath != null) {
            VolumeIdPath = volumePath + "/Android/data/" + context.getPackageName() + "/VolumeId";
        }
        if (VolumeIdPath != null) {
            File dir = new File(VolumeIdPath);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    try {
                        return Integer.parseInt(files[0].getName());
                    } catch (NumberFormatException e) {
                        GalleryLog.w("GalleryData", "getVolumeId exception:" + e);
                    }
                }
            }
        }
        return 0;
    }

    private static String getSQLVolumeIds(Context context) {
        ArrayList<GalleryStorage> galleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            galleryStorageList.add(0, innerGalleryStorage);
        }
        StringBuffer stringBuffer = new StringBuffer();
        int size = galleryStorageList.size();
        for (int index = 0; index < size; index++) {
            stringBuffer.append(getVolumeId(context, (GalleryStorage) galleryStorageList.get(index)));
            if (index != size - 1) {
                stringBuffer.append(",");
            }
        }
        return stringBuffer.toString();
    }

    private static int getVolumeId(Context context, String data) {
        String cacheDir = getVolumeIdCacheDir(context, data);
        if (cacheDir == null) {
            GalleryLog.w("GalleryData", "can not find the path of data which used for volume id file.");
            return 0;
        }
        File dir = new File(cacheDir);
        if (dir.exists() || dir.mkdirs()) {
            File[] files = dir.listFiles();
            if (files == null || files.length <= 0) {
                int name = new Random().nextInt();
                if (name == 0) {
                    name = 1;
                }
                try {
                    if (new File(dir, String.valueOf(name)).createNewFile()) {
                        GalleryLog.w("GalleryData", "create volume id dir failed. name is" + name);
                    }
                    return name;
                } catch (IOException e) {
                    GalleryLog.w("GalleryData", "create volume id file failed.");
                    return 0;
                }
            }
            try {
                return Integer.parseInt(files[0].getName());
            } catch (NumberFormatException e2) {
                GalleryLog.w("GalleryData", "getVolumeId exception:" + e2);
                return 0;
            }
        }
        GalleryLog.w("GalleryData", "create volume id dir failed.");
        return 0;
    }

    private static ArrayList<Integer> getVolumeIds(Context context) {
        ArrayList<Integer> ids = new ArrayList();
        String[] volumePaths = GalleryUtils.getVolumePaths();
        if (volumePaths == null || volumePaths.length <= 0) {
            return ids;
        }
        for (String path : volumePaths) {
            File dir = new File(path + "/Android/data/" + context.getPackageName() + "/VolumeId");
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    try {
                        ids.add(Integer.valueOf(files[0].getName()));
                    } catch (NumberFormatException e) {
                        GalleryLog.w("GalleryData", "getVolumeIds exception:" + e);
                    }
                }
            }
        }
        return ids;
    }

    private boolean favoritePathExist(String data, int volumeId) {
        Closeable closeable = null;
        try {
            closeable = this.mGalleryDB.query("media_file", new String[]{"_id"}, "_data = ? AND volume_id = ?", new String[]{data, String.valueOf(volumeId)}, null, null, null, null);
            if (closeable == null) {
                return false;
            }
            boolean result = closeable.getCount() > 0;
            Utils.closeSilently(closeable);
            return result;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public boolean isMyFavorite(String filePath) {
        if (filePath == null) {
            return false;
        }
        boolean contains;
        synchronized (this) {
            contains = this.mFilePaths.contains(filePath);
        }
        return contains;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void updateFilePaths(String path, boolean isFavorite) {
        if (path != null) {
            if (isFavorite) {
                this.mFilePaths.add(path);
            } else {
                this.mFilePaths.remove(path);
            }
        }
    }

    public int updateFavorite(String filePath, boolean isFavorite) {
        if (filePath == null) {
            return 0;
        }
        int volumeId = getVolumeId(this.mContext, filePath);
        if (volumeId == 0) {
            return 0;
        }
        if (isFavorite) {
            updateFilePaths(filePath, true);
            boolean itemExist = favoritePathExist(filePath, volumeId);
            ContentValues values = new ContentValues();
            if (itemExist) {
                values.put("is_favorite", Integer.valueOf(1));
                this.mGalleryDB.update("media_file", values, "_data = ? AND volume_id = ?", new String[]{filePath, String.valueOf(volumeId)});
            } else {
                values.put("_data", filePath);
                values.put("is_favorite", Integer.valueOf(1));
                values.put("volume_id", Integer.valueOf(volumeId));
                this.mGalleryDB.insert("media_file", values);
            }
            return 1;
        }
        updateFilePaths(filePath, false);
        this.mGalleryDB.delete("media_file", "_data = ? AND volume_id = ?", new String[]{filePath, String.valueOf(volumeId)});
        return 1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<String> queryFavorite(boolean force) {
        synchronized (this) {
            if (!force) {
                ArrayList<String> arrayList = new ArrayList(this.mFilePaths);
                return arrayList;
            }
        }
    }

    private synchronized void updateFilePathSync(Set<String> paths) {
        this.mFilePaths = paths;
    }

    public FavoriteWhereClause getFavoriteWhereClause() {
        ArrayList<String> paths = queryFavorite(false);
        if (paths.size() <= 0) {
            return null;
        }
        StringBuffer where = new StringBuffer();
        where.append("_data IN(");
        for (int i = 0; i < paths.size(); i++) {
            where.append("?");
            if (i != paths.size() - 1) {
                where.append(",");
            }
        }
        where.append(" )");
        FavoriteWhereClause result = new FavoriteWhereClause();
        result.mWhereClause = where.toString();
        result.mPaths = (String[]) paths.toArray(new String[paths.size()]);
        return result;
    }

    public void addMaxAlbumIndex(int bucketID, String albumPath) {
        int volumeId = getVolumeId(this.mContext, albumPath);
        if (volumeId != 0) {
            this.mGalleryDB.addAlbumSortIndex(bucketID, volumeId);
        }
    }

    public void dropAlbumIndex(int bucketID, String albumPath) {
        if (getVolumeId(this.mContext, albumPath) != 0) {
            this.mGalleryDB.delete("bucket", "bucket_id = ? AND volume_id = ?", new String[]{String.valueOf(bucketID), String.valueOf(getVolumeId(this.mContext, albumPath))});
        }
    }

    public void exchangeAlbumIndex(int fromBucketId, int toBucketId) {
        this.mGalleryDB.exchangeAlbumSortIndex(fromBucketId, toBucketId, getSQLVolumeIds(this.mContext));
    }

    public void getAlbumIndex(HashSet<Integer> bucketIds, ArrayList<Integer> sortedBucketIds) {
        StringBuffer where = new StringBuffer();
        where.append("volume_id IN(");
        where.append(getSQLVolumeIds(this.mContext));
        where.append(" )");
        Cursor cursor = this.mGalleryDB.query("bucket", new String[]{"bucket_id", "album_sort_index"}, where.toString(), null, null, null, "album_sort_index DESC", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    int bucketId = cursor.getInt(0);
                    if (bucketIds != null) {
                        bucketIds.add(Integer.valueOf(bucketId));
                    }
                    if (sortedBucketIds != null) {
                        sortedBucketIds.add(Integer.valueOf(bucketId));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
    }

    public boolean isWhiteListBucketId(String bucketPath) {
        return this.mWhiteList.match((bucketPath + File.separator + "name").toLowerCase(Locale.US));
    }
}
