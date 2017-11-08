package com.huawei.gallery.recycle.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.GalleryImage;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.GalleryVideo;
import com.android.gallery3d.data.IRecycle;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.provider.GalleryProvider;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.Base32;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

public class RecycleUtils {
    public static String CLOUD_BUCKET_ALBUM_ID = "CloudBucketAlbumID";
    private static MyPrinter LOG = new MyPrinter("Recycle_RecycleUtilsOperation");
    public static final Uri OPERATION_RECYCLE_URI = Uri.withAppendedPath(GalleryProvider.BASE_URI, "recycle_operation");

    private static class RecycleFileNameFilter implements FilenameFilter {
        private RecycleFileNameFilter() {
        }

        public boolean accept(File dir, String inputName) {
            String name = Base32.decode(inputName);
            if (PhotoShareUtils.isGUIDSupport()) {
                return name.matches("\\d+\\|.+\\|\\d+\\|\\d+\\|.+\\|.+\\|.+\\|.+");
            }
            return name.matches("\\d+\\|.+\\|\\d+\\|\\d+\\|.+\\|.+\\|.+");
        }
    }

    public static void makeTempFileCopy(File file, Bundle bundle) {
        if (!file.exists() || bundle == null) {
            LOG.e("file not exist!");
            return;
        }
        String sourcePath = file.getPath();
        String tempPath = sourcePath + "temp";
        File tempFile = new File(tempPath);
        if (tempFile.exists()) {
            LOG.e("has same temp file! path = " + tempPath);
        }
        if (file.renameTo(tempFile)) {
            bundle.putString("file_source", sourcePath);
            bundle.putString("file_temp", tempPath);
        } else {
            LOG.e("renameTo failed! sourcepath = " + sourcePath + ", renameTopath = " + tempPath);
            if (!file.delete()) {
                LOG.e("delete failed! path = " + sourcePath);
            }
        }
    }

    private static void rollback(Bundle bundle) {
        String sourcePath = bundle.getString("file_source");
        String renameToPath = bundle.getString("file_renameto");
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(renameToPath)) {
            LOG.e("rollback failed!");
            return;
        }
        if (!new File(renameToPath).renameTo(new File(sourcePath))) {
            LOG.e("rollback failed! renameToFile fail! sourcepath = " + renameToPath + ", renameTopath = " + sourcePath);
        }
    }

    private static void deleteTempFile(Bundle bundle) {
        String tempPath = bundle.getString("file_temp");
        if (!TextUtils.isEmpty(tempPath)) {
            File tempFile = new File(tempPath);
            if (tempFile.exists() && !tempFile.delete()) {
                LOG.w("delete temp tile fail " + tempFile.getName());
            }
        }
    }

    public static void executeDbOperation(SQLiteDatabase db, MediaObject item, ContentValues values) {
        if (supportRecycle()) {
            Bundle data = getOperationFromContentValues(values);
            if (data == null) {
                throw new RuntimeException("executeDbOperation input carrierObject type not right");
            }
            db.beginTransaction();
            try {
                executeDbOperation(db, item, data);
                db.setTransactionSuccessful();
                deleteTempFile(data);
            } catch (SQLiteException e) {
                rollback(data);
                LOG.d(e.getMessage());
            } finally {
                db.endTransaction();
            }
            return;
        }
        LOG.d("executeDbOperation not in recycle");
    }

    private static void setOperationToContentValues(ContentValues values, Bundle data) {
        if (values == null || data == null) {
            LOG.e("setOperation has null object");
            return;
        }
        values.put("recycle_flag", Integer.valueOf(data.getInt("recycle_flag", 0)));
        values.put("file_renameto", data.getString("file_renameto", ""));
        values.put("file_source", data.getString("file_source", ""));
        values.put("recycle_file_name", data.getString("recycle_file_name", ""));
        values.put("recycle_time", Long.valueOf(data.getLong("recycle_time", -1)));
        values.put("title", data.getString("title", ""));
        if (PhotoShareUtils.isGUIDSupport()) {
            values.put("delete_recycle_file", Boolean.valueOf(data.getBoolean("delete_recycle_file", false)));
            values.put("rename_recycle_file", Boolean.valueOf(data.getBoolean("rename_recycle_file", false)));
        }
    }

    private static Bundle getOperationFromContentValues(ContentValues values) {
        if (values == null) {
            LOG.e("getOperation has null object");
            return null;
        }
        Bundle data = new Bundle();
        data.putInt("recycle_flag", values.getAsInteger("recycle_flag").intValue());
        data.putString("file_source", values.getAsString("file_source"));
        data.putString("file_renameto", values.getAsString("file_renameto"));
        data.putString("recycle_file_name", values.getAsString("recycle_file_name"));
        data.putLong("recycle_time", values.getAsLong("recycle_time").longValue());
        data.putString("title", values.getAsString("title"));
        if (PhotoShareUtils.isGUIDSupport()) {
            data.putBoolean("delete_recycle_file", values.getAsBoolean("delete_recycle_file").booleanValue());
            data.putBoolean("rename_recycle_file", values.getAsBoolean("rename_recycle_file").booleanValue());
        }
        return data;
    }

    public static void delete(ContentResolver resolver, MediaObject itemObject, Bundle data) {
        int flag = 0;
        if (data != null) {
            flag = data.getInt("recycle_flag", 0);
        }
        switch (flag) {
            case 1:
            case 2:
            case 3:
                ContentValues values = new ContentValues();
                setOperationToContentValues(values, data);
                values.put("opera_params", itemObject.getPath().toString());
                resolver.update(OPERATION_RECYCLE_URI, values, null, null);
                return;
            default:
                itemObject.delete();
                return;
        }
    }

    private static void executeDbOperation(SQLiteDatabase db, MediaObject itemObject, Bundle data) {
        int flag = 0;
        if (data != null) {
            flag = data.getInt("recycle_flag", 0);
        }
        switch (flag) {
            case 1:
            case 3:
                if (itemObject instanceof IRecycle) {
                    ((IRecycle) itemObject).onDeleteThrough(db, data);
                    return;
                }
                return;
            case 2:
                itemObject.recycle(db, data);
                return;
            default:
                itemObject.delete();
                return;
        }
    }

    public static boolean supportRecycle() {
        return PhotoShareUtils.isCloudRecycleAlbumSupport();
    }

    private static ArrayList<File> getRecycledFiles(GalleryStorage storage) {
        File recycleFolder = new File(storage.getPath() + "/Pictures/.Gallery2/recycle/");
        if (!recycleFolder.exists()) {
            return null;
        }
        File[] tempFile = recycleFolder.listFiles(new RecycleFileNameFilter());
        if (tempFile == null) {
            return null;
        }
        ArrayList<File> files = new ArrayList(Arrays.asList(tempFile));
        if (files.isEmpty()) {
            return null;
        }
        return files;
    }

    public static void recoverLocalRecycleTable(ContentResolver resolver) {
        ArrayList<GalleryStorage> storages = GalleryStorageManager.getInstance().getInnerAndOuterStorage();
        ArrayList<String> infosInTable = LocalRecycledFile.getFileInfosInTable(resolver);
        int storageSize = storages.size();
        for (int i = 0; i < storageSize; i++) {
            GalleryStorage storage = (GalleryStorage) storages.get(i);
            ArrayList<File> files = getRecycledFiles(storage);
            if (files != null) {
                ArrayList<LinkedHashMap> recycleFileInfos = new ArrayList();
                int fileSize = files.size();
                for (int j = 0; j < fileSize; j++) {
                    LinkedHashMap info = getOriginFileInfoFromRecycleName(((File) files.get(j)).getName());
                    if (info != null) {
                        String recycleName = (String) info.get(Integer.valueOf(4));
                        String singleRecycleTimeAndName = String.valueOf((Long) info.get(Integer.valueOf(3))) + "|" + recycleName;
                        if (infosInTable.contains(singleRecycleTimeAndName)) {
                            infosInTable.remove(singleRecycleTimeAndName);
                        } else {
                            recycleFileInfos.add(info);
                        }
                    }
                }
                if (!recycleFileInfos.isEmpty()) {
                    LocalRecycledFile.selfRecoverInsert(resolver, recycleFileInfos, storage);
                }
            }
        }
        if (!infosInTable.isEmpty()) {
            LocalRecycledFile.selfRecoverDelete(resolver, infosInTable);
        }
    }

    public static LinkedHashMap getOriginFileInfoFromRecycleName(String inputName) {
        String name = Base32.decode(inputName);
        if (PhotoShareUtils.isGUIDSupport()) {
            if (!name.matches("\\d+\\|.+\\|\\d+\\|\\d+\\|.+\\|.+\\|.+\\|.+")) {
                return null;
            }
        } else if (!name.matches("\\d+\\|.+\\|\\d+\\|\\d+\\|.+\\|.+\\|.+")) {
            return null;
        }
        LinkedHashMap info = new LinkedHashMap();
        String[] tempInfo = name.split("\\|");
        String mimeType = tempInfo[6];
        info.put(Integer.valueOf(0), Integer.valueOf(Integer.parseInt(tempInfo[0])));
        info.put(Integer.valueOf(1), Integer.valueOf(Integer.parseInt(tempInfo[1])));
        info.put(Integer.valueOf(2), Integer.valueOf(Integer.parseInt(tempInfo[2])));
        info.put(Integer.valueOf(3), Long.valueOf(Long.parseLong(tempInfo[3])));
        info.put(Integer.valueOf(4), tempInfo[4]);
        info.put(Integer.valueOf(5), tempInfo[5]);
        info.put(Integer.valueOf(6), mimeType);
        if (PhotoShareUtils.isGUIDSupport()) {
            info.put(Integer.valueOf(7), tempInfo[7]);
        }
        return info;
    }

    public static String getRecycleName(MediaItem item, long recycleTime, String name) {
        LinkedHashMap targetName = new LinkedHashMap();
        String filePath = item.getFilePath();
        String surfix = ".gallery";
        int index = filePath.lastIndexOf(46);
        if (index != -1) {
            surfix = filePath.substring(index);
        }
        targetName.put(Integer.valueOf(0), Integer.valueOf(item.getMediaType() == 2 ? 1 : item.getMediaType()));
        if (item instanceof GalleryMediaItem) {
            targetName.put(Integer.valueOf(1), Integer.valueOf(item.getRotation()));
            targetName.put(Integer.valueOf(2), Integer.valueOf(((GalleryMediaItem) item).getLocalMediaId()));
        } else {
            targetName.put(Integer.valueOf(1), Integer.valueOf(item.getRotation()));
            targetName.put(Integer.valueOf(2), Integer.valueOf(item.getId()));
        }
        targetName.put(Integer.valueOf(3), Long.valueOf(recycleTime));
        targetName.put(Integer.valueOf(4), name);
        targetName.put(Integer.valueOf(5), surfix);
        targetName.put(Integer.valueOf(6), item.getMimeType());
        if (PhotoShareUtils.isGUIDSupport()) {
            if (item instanceof GalleryMediaItem) {
                targetName.put(Integer.valueOf(7), ((GalleryMediaItem) item).getUniqueId());
            } else if (item instanceof LocalMediaItem) {
                targetName.put(Integer.valueOf(7), ((LocalMediaItem) item).getUniqueId());
            } else {
                targetName.put(Integer.valueOf(7), "0");
                LOG.w("wrong media item in getRecycleName");
            }
        }
        return getRecycleNameFromMap(targetName);
    }

    public static String getLimitedName(String name) {
        int originLength = name.length();
        int decodedLength = Base32.encode(name).length();
        if (((double) decodedLength) <= 80.0d) {
            return name;
        }
        return getLimitedName(name.substring(0, (int) Math.floor(((double) originLength) * (80.0d / ((double) decodedLength)))));
    }

    public static String getRecycleNameFromMap(LinkedHashMap map) {
        return Base32.encode(map.values().toString().replaceAll(", ", "|").replaceAll("\\[", "").replaceAll("\\]", ""));
    }

    public static String getOriginDisplayName(String fileName) {
        return dealWithPrefix(dealWithSuffix(fileName));
    }

    private static String dealWithSuffix(String fileName) {
        if (!supportRecycle() || PhotoShareUtils.isGUIDSupport()) {
            return fileName;
        }
        if (!TextUtils.isEmpty(fileName)) {
            String upperCase = fileName.toUpperCase(Locale.ENGLISH);
            int end = upperCase.length();
            if (upperCase.contains(".JPG@")) {
                end = upperCase.lastIndexOf(".JPG@") + ".JPG".length();
            }
            fileName = fileName.substring(0, end);
        }
        return fileName;
    }

    private static String dealWithPrefix(String fileName) {
        if (supportRecycle() && !TextUtils.isEmpty(fileName)) {
            String temp = fileName.toUpperCase(Locale.ENGLISH);
            if ((temp.matches("(([^/]*)_BURST)(\\d{3}).JPG$") || temp.matches("([^/]*)_BURST(\\d{3})_COVER.JPG$")) && fileName.contains("IMG")) {
                return fileName.substring(fileName.indexOf("IMG"));
            }
        }
        return fileName;
    }

    public static String getGalleryRecycleBinDir(String sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath == null");
        }
        ArrayList<GalleryStorage> galleryStorages = GalleryStorageManager.getInstance().getInnerAndOuterStorage();
        int size = galleryStorages.size();
        for (int i = 0; i < size; i++) {
            GalleryStorage storage = (GalleryStorage) galleryStorages.get(i);
            if (storage != null && sourcePath.startsWith(storage.getPath())) {
                return storage.getPath() + "/Pictures/.Gallery2/recycle/";
            }
        }
        throw new IllegalArgumentException("galleryStorages list is illegal");
    }

    public static String mergeRecycleItemPathId(int localMediaId, int cloudMediaId) {
        return localMediaId + "_" + cloudMediaId;
    }

    public static String[] splitRecycleItemPathId(String recycleItemId) {
        String[] idArray = recycleItemId.split("_");
        if (idArray.length == 2) {
            return idArray;
        }
        throw new IllegalArgumentException("recycleItemId is illegal");
    }

    public static void startAsyncAlbumInfo() {
        if (PhotoShareUtils.getServer() != null) {
            try {
                LOG.d("async album ret = " + PhotoShareUtils.getServer().startAsyncAlbumInfo());
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        }
    }

    public static boolean isInvalidUniqueId(String uniqueId) {
        return !"0".equals(uniqueId) ? TextUtils.isEmpty(uniqueId) : true;
    }

    public static synchronized String getPreferenceValue(String preferencdName, String key) {
        synchronized (RecycleUtils.class) {
            if (GalleryUtils.getContext() == null) {
                String str = "";
                return str;
            }
            str = GalleryUtils.getContext().getSharedPreferences(preferencdName, 0).getString(key, "");
            return str;
        }
    }

    public static synchronized HashMap<String, String> getAllPreferenceValue(String preferencdName) {
        HashMap<String, String> hashMap;
        synchronized (RecycleUtils.class) {
            if (GalleryUtils.getContext() == null) {
                HashMap hashMap2 = new HashMap();
            }
            hashMap = (HashMap) GalleryUtils.getContext().getSharedPreferences(preferencdName, 0).getAll();
        }
        return hashMap;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void setPreferenceValue(String preferencdName, HashMap<String, String> contents) {
        synchronized (RecycleUtils.class) {
            if (GalleryUtils.getContext() == null || contents == null) {
            } else {
                Editor editor = GalleryUtils.getContext().getSharedPreferences(preferencdName, 0).edit();
                for (String key : contents.keySet()) {
                    editor.putString(key, (String) contents.get(key));
                }
                editor.apply();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SparseIntArray queryMediaIdTypeArray(ContentResolver resolver, String whereClause, String[] selectionArgs) {
        SparseIntArray idMediaTypeArray = new SparseIntArray();
        try {
            ContentResolver contentResolver = resolver;
            Closeable cursor = contentResolver.query(GalleryMedia.URI, new String[]{"_id", "media_type"}, whereClause, selectionArgs, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    idMediaTypeArray.put(cursor.getInt(0), cursor.getInt(1));
                }
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            LOG.w("query media id & type error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return idMediaTypeArray;
    }

    public static void recyclePhotoItem(DataManager dm, SparseIntArray idMediaTypeArray, MenuExecutor menuExecutor) {
        Bundle data = new Bundle();
        data.putInt("recycle_flag", 2);
        data.putInt("key-pastestate", 2);
        for (int i = 0; i < idMediaTypeArray.size(); i++) {
            recyclePhotoItem(dm, data, (idMediaTypeArray.valueAt(i) == 1 ? GalleryImage.IMAGE_PATH : GalleryVideo.VIDEO_PATH).getChild(idMediaTypeArray.keyAt(i)), menuExecutor);
        }
    }

    public static void recyclePhotoItem(DataManager dm, Bundle data, Path path, MenuExecutor menuExecutor) {
        ArrayList<Path> pathList = new ArrayList();
        pathList.add(path);
        dm.initPaste(data, menuExecutor, pathList);
        dm.paste(path, data, menuExecutor);
    }

    public static String getPrintableContentValues(ContentValues values) {
        if (values == null) {
            return "";
        }
        values.remove("latitude");
        values.remove("longitude");
        return values.toString();
    }
}
