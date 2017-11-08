package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.util.SparseArray;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.android.gallery3d.util.WhiteList;
import com.huawei.gallery.storage.GalleryInnerStorage;
import com.huawei.gallery.storage.GalleryOuterStorage;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BucketHelper {
    public static final String PRE_LOADED_PATH_PICTURE = (PRE_LOADED_PATH_PREFIX + "/Pictures");
    public static final String PRE_LOADED_PATH_PREFIX = GalleryUtils.getPreloadMediaDirectory();
    public static final String PRE_LOADED_PATH_VIDEO = (PRE_LOADED_PATH_PREFIX + "/Video");
    private static final String[] PROJECTION_BUCKET = new String[]{"bucket_id", "media_type", "bucket_display_name", "_data", "count(1)", "lower(_data)"};
    private static final String[] PROJECTION_BUCKET_IN_ONE_TABLE = new String[]{"bucket_id", "MAX(datetaken)", "bucket_display_name"};
    private static final String[] PROJECTION_FIND_BUCKET = new String[]{"bucket_id", "media_type", "bucket_display_name"};
    private static GalleryApp mApplication;
    private static int mPasteSourceBucketId = 0;
    private static final Comparator<BucketEntry> sBucketEntryComparator = new BucketEntryComparator();
    private static String sBucketGroupByLocalCamera = getBucketGroupByLocalCamera();
    private static String sBucketGroupByLocalCameraExternal = getBucketGroupByLocalCameraExternal();
    private static String sBucketGroupByLocalCameraInternal = getBucketGroupByLocalCameraInternal();
    private static String sBucketGroupByLocalScreenShots = getBucketGroupByLocalScreenshots();
    private static String sBucketGroupByLocalScreenShotsExternal = getBucketGroupByLocalScreenshotsExternal();
    private static String sBucketGroupByLocalScreenShotsInternal = getBucketGroupByLocalScreenshotsInternal();
    private static String sBucketWhereInsideBase = getBucketWhereInsideBase(false);
    private static String sBucketWhereOutsideBase = getBucketWhereOutsideBase(false);

    public static class BucketEntry implements Serializable {
        public int bucketId;
        public String bucketName;
        public String bucketPath;
        public int dateTaken;
        public boolean isHidden;
        public int mediaCount;

        public BucketEntry(int id, String name, boolean isHidden, String path, int count) {
            this.bucketId = id;
            this.bucketName = Utils.ensureNotNull(name);
            this.isHidden = isHidden;
            this.bucketPath = path;
            this.mediaCount = count;
        }

        public BucketEntry(int id, String name) {
            this.bucketId = id;
            this.bucketName = Utils.ensureNotNull(name);
        }

        public int hashCode() {
            return this.bucketId;
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (!(object instanceof BucketEntry)) {
                return false;
            }
            if (this.bucketId == ((BucketEntry) object).bucketId) {
                z = true;
            }
            return z;
        }

        public void addMediaCount(int count) {
            this.mediaCount += count;
        }
    }

    @SuppressWarnings({"SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"})
    private static class BucketEntryComparator implements Comparator<BucketEntry> {
        private BucketEntryComparator() {
        }

        public int compare(BucketEntry left, BucketEntry right) {
            int i = -1;
            GalleryStorage leftGalleryStorage = GalleryStorageManager.getInstance().getGalleryStorageByBucketID(left.bucketId);
            GalleryStorage rightGalleryStorage = GalleryStorageManager.getInstance().getGalleryStorageByBucketID(right.bucketId);
            if (leftGalleryStorage == null || rightGalleryStorage == null) {
                return 0;
            }
            if (leftGalleryStorage instanceof GalleryInnerStorage) {
                return -1;
            }
            if (rightGalleryStorage instanceof GalleryInnerStorage) {
                return 1;
            }
            if (((GalleryOuterStorage) leftGalleryStorage).getIndex() >= ((GalleryOuterStorage) rightGalleryStorage).getIndex()) {
                i = 1;
            }
            return i;
        }
    }

    public static void reset() {
        sBucketGroupByLocalCamera = getBucketGroupByLocalCamera();
        sBucketGroupByLocalCameraInternal = getBucketGroupByLocalCameraInternal();
        sBucketGroupByLocalCameraExternal = getBucketGroupByLocalCameraExternal();
        sBucketGroupByLocalScreenShots = getBucketGroupByLocalScreenshots();
        sBucketGroupByLocalScreenShotsInternal = getBucketGroupByLocalScreenshotsInternal();
        sBucketGroupByLocalScreenShotsExternal = getBucketGroupByLocalScreenshotsExternal();
        sBucketWhereOutsideBase = getBucketWhereOutsideBase(true);
        sBucketWhereInsideBase = getBucketWhereInsideBase(false);
    }

    private static String getBucketWhereOutsideBase(boolean force) {
        return "1) AND (bucket_id IN (SELECT DISTINCT bucket_id FROM files WHERE title='.outside' OR title='.empty_out')OR ( media_type IN (1,3) AND ( storage_id not IN (0,65537) OR bucket_id IN ( " + WhiteList.getBucketIdForWhiteList(force) + " ) ) AND bucket_id NOT IN  (SELECT DISTINCT bucket_id FROM files WHERE title='.inside' OR title='.empty_in') " + " AND bucket_id NOT IN ( " + getExcludeBuckets() + ") ) )";
    }

    private static String getBucketWhereInsideBase(boolean force) {
        return "1) AND (bucket_id IN (SELECT DISTINCT bucket_id FROM files WHERE title='.inside' OR title='.empty_in')OR _data like '" + PRE_LOADED_PATH_PREFIX + "/%' OR " + " ( media_type IN (1,3)  AND ( storage_id = 65537 AND bucket_id NOT IN (" + WhiteList.getBucketIdForWhiteList(force) + ") ) AND bucket_id NOT IN ( " + getExcludeBuckets() + ") ) ) AND bucket_id NOT IN (SELECT DISTINCT bucket_id FROM files WHERE title='.outside' OR title='.empty_out')";
    }

    private static String getExcludeBuckets() {
        String str;
        StringBuilder append = new StringBuilder().append(MediaSetUtils.getCameraBucketId()).append(",").append(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs());
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "," + MediaSetUtils.getScreenshotsBucketID() + "," + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs();
        } else {
            str = "";
        }
        return append.append(str).toString();
    }

    private static String getBucketGroupByLocalCamera() {
        return "1) AND title='.emptyshow' AND bucket_id IN (" + MediaSetUtils.getCameraBucketId() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs() + ") GROUP BY 1,(2";
    }

    private static String getBucketGroupByLocalCameraInternal() {
        return "1) AND title='.emptyshow' AND bucket_id=" + MediaSetUtils.getCameraBucketId() + " GROUP BY 1,(2";
    }

    private static String getBucketGroupByLocalCameraExternal() {
        return "1) AND title='.emptyshow' AND bucket_id IN (" + GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs() + ") GROUP BY 1,(2";
    }

    private static String getBucketGroupByLocalScreenshots() {
        return "1) AND title='.emptyshow' AND bucket_id IN (" + MediaSetUtils.getScreenshotsBucketID() + " , " + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs() + ") GROUP BY 1,(2";
    }

    private static String getBucketGroupByLocalScreenshotsInternal() {
        return "1) AND title='.emptyshow' AND bucket_id=" + MediaSetUtils.getScreenshotsBucketID() + " GROUP BY 1,(2";
    }

    private static String getBucketGroupByLocalScreenshotsExternal() {
        return "1) AND title='.emptyshow' AND bucket_id IN (" + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs() + ") GROUP BY 1,(2";
    }

    public static String getExcludeHiddenWhereClause(Context context) {
        List<String> hiddenBuckets = findAllHiddenBuckets(context);
        int totalCount = hiddenBuckets.size();
        if (totalCount == 0) {
            return "1=1";
        }
        StringBuffer whereClause = new StringBuffer("bucket_id not in (");
        int last = totalCount - 1;
        for (int i = 0; i < totalCount; i++) {
            whereClause.append((String) hiddenBuckets.get(i));
            if (i != last) {
                whereClause.append(", ");
            } else {
                whereClause.append(")");
            }
        }
        return whereClause.toString();
    }

    public static List<String> findAllHiddenBuckets(Context context) {
        Set<Integer> hideStorage = findHiddenBuckets(context.getContentResolver());
        List<String> ret = FilePreference.getAll(context);
        for (Integer bucketId : hideStorage) {
            ret.add(String.valueOf(bucketId));
        }
        return ret;
    }

    private static HashSet<Integer> findHiddenBuckets(ContentResolver resolver) {
        HashSet<Integer> hiddenBuckets = new HashSet();
        Closeable closeable = null;
        try {
            closeable = resolver.query(getFilesContentUri(), PROJECTION_FIND_BUCKET, " title='.hidden' ", null, null);
            if (closeable == null) {
                GalleryLog.w("BucketHelper", "cannot open media database: " + getFilesContentUri());
                return hiddenBuckets;
            }
            while (closeable.moveToNext()) {
                hiddenBuckets.add(Integer.valueOf(closeable.getInt(0)));
            }
            Utils.closeSilently(closeable);
            return hiddenBuckets;
        } catch (SecurityException e) {
            GalleryLog.w("BucketHelper", "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static void setGalleryApp(GalleryApp application) {
        mApplication = application;
    }

    public static BucketEntry[] loadBucketEntries(JobContext jc, ContentResolver resolver, int type, int displayType) {
        if (!ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE) {
            return loadBucketEntriesFromImagesAndVideoTable(jc, resolver, type);
        }
        long start = System.currentTimeMillis();
        BucketEntry[] rs = loadBucketEntriesFromFilesTable(jc, resolver, type, displayType);
        GalleryLog.d("BucketHelper", "loadBucketEntries time:" + (System.currentTimeMillis() - start) + ", displayType:" + displayType);
        return rs;
    }

    private static void updateBucketEntriesFromTable(JobContext jc, ContentResolver resolver, Uri tableUri, HashMap<Integer, BucketEntry> buckets) {
        Closeable closeable = null;
        closeable = resolver.query(tableUri, PROJECTION_BUCKET_IN_ONE_TABLE, "1) GROUP BY (1", null, null);
        if (closeable == null) {
            GalleryLog.w("BucketHelper", "cannot open media database: " + tableUri);
            return;
        }
        while (closeable.moveToNext()) {
            int bucketId = closeable.getInt(0);
            int dateTaken = closeable.getInt(1);
            BucketEntry entry = (BucketEntry) buckets.get(Integer.valueOf(bucketId));
            if (entry == null) {
                entry = new BucketEntry(bucketId, closeable.getString(2));
                buckets.put(Integer.valueOf(bucketId), entry);
                entry.dateTaken = dateTaken;
            } else {
                try {
                    entry.dateTaken = Math.max(entry.dateTaken, dateTaken);
                } catch (SecurityException e) {
                    GalleryLog.w("BucketHelper", "No permission to query!");
                } finally {
                    Utils.closeSilently(closeable);
                }
            }
        }
        Utils.closeSilently(closeable);
    }

    private static BucketEntry[] loadBucketEntriesFromImagesAndVideoTable(JobContext jc, ContentResolver resolver, int type) {
        HashMap<Integer, BucketEntry> buckets = new HashMap(64);
        if ((type & 2) != 0) {
            updateBucketEntriesFromTable(jc, resolver, Media.EXTERNAL_CONTENT_URI, buckets);
        }
        if ((type & 4) != 0) {
            updateBucketEntriesFromTable(jc, resolver, Video.Media.EXTERNAL_CONTENT_URI, buckets);
        }
        BucketEntry[] entries = (BucketEntry[]) buckets.values().toArray(new BucketEntry[buckets.size()]);
        Arrays.sort(entries, new Comparator<BucketEntry>() {
            public int compare(BucketEntry a, BucketEntry b) {
                return b.dateTaken - a.dateTaken;
            }
        });
        return entries;
    }

    private static String getPasteCameraWhereClause(int reloadType) {
        String cameraWhereClause = sBucketGroupByLocalCamera;
        if ((HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT & reloadType) != 0) {
            return sBucketGroupByLocalCameraInternal;
        }
        if ((131072 & reloadType) != 0) {
            return sBucketGroupByLocalCameraExternal;
        }
        if ((262144 & reloadType) != 0) {
            return "0) GROUP BY 1,(2";
        }
        return cameraWhereClause;
    }

    private static String getPasteScreenshotsWhereClause(int reloadType) {
        String screenshotsWhereClause = sBucketGroupByLocalScreenShots;
        if ((1048576 & reloadType) != 0) {
            return sBucketGroupByLocalScreenShotsInternal;
        }
        if ((2097152 & reloadType) != 0) {
            return sBucketGroupByLocalScreenShotsExternal;
        }
        if ((4194304 & reloadType) != 0) {
            return "0) GROUP BY 1,(2";
        }
        return screenshotsWhereClause;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static BucketEntry[] loadBucketEntriesFromFilesTable(JobContext jc, ContentResolver resolver, int type, int showAlbumsType) {
        boolean showHiddenAlbums;
        String whereClauseTmp = getNoSelfAndPreloadClause(type);
        Uri uri = getFilesContentUri();
        String bucketWhereClause = null;
        String orderBy = "MAX(datetaken) DESC";
        switch (showAlbumsType) {
            case 20:
                bucketWhereClause = sBucketWhereOutsideBase + whereClauseTmp + " GROUP BY 1,(2";
                break;
            case 21:
                bucketWhereClause = sBucketWhereInsideBase + whereClauseTmp + " GROUP BY 1,(2";
                break;
            case 22:
                bucketWhereClause = getPasteCameraWhereClause(type);
                break;
            case 24:
                bucketWhereClause = getPasteScreenshotsWhereClause(type);
                break;
        }
        int typeBits = 0;
        if ((type & 2) != 0) {
            typeBits = 2;
        }
        if ((type & 4) != 0) {
            typeBits |= 8;
        }
        SharedPreferences systemPreLoadPreferences = mApplication.getAndroidContext().getSharedPreferences("system_preload_folder", 0);
        if ((type & 256) != 0) {
            showHiddenAlbums = true;
        } else {
            showHiddenAlbums = false;
        }
        HashSet<Integer> hiddenBuckets = findHiddenBuckets(resolver);
        ArrayList<BucketEntry> buffer = new ArrayList();
        BlackList blackList = BlackList.getInstance();
        SparseArray<BucketEntry> map = new SparseArray();
        ArrayList<BucketEntry> whiteBuffer = new ArrayList();
        WhiteList whiteList = WhiteList.getInstance();
        Closeable closeable = null;
        TraceController.printDebugInfo("before query " + bucketWhereClause);
        closeable = resolver.query(uri, PROJECTION_BUCKET, bucketWhereClause, null, orderBy);
        if (closeable == null) {
            GalleryLog.w("BucketHelper", "cannot open local database: " + uri);
            BucketEntry[] bucketEntryArr = new BucketEntry[0];
            return bucketEntryArr;
        }
        TraceController.printDebugInfo("after query record count: " + closeable.getCount());
        while (closeable.moveToNext()) {
            boolean isHidden;
            BucketEntry entry;
            BucketEntry mapEntry;
            int index;
            boolean isNeedContinue = false;
            int dataCount = 0;
            int bucketId = closeable.getInt(0);
            int mediaType = closeable.getInt(1);
            String dataName = closeable.getString(3);
            String lowerPathName = closeable.getString(5);
            String bucketPath = dataName.substring(0, dataName.lastIndexOf("/"));
            switch (showAlbumsType) {
                case 20:
                    if (lowerPathName.endsWith(".empty_out")) {
                        isNeedContinue = true;
                    } else if (((1 << mediaType) & typeBits) != 0) {
                        isNeedContinue = true;
                        if (bucketPath.startsWith(PRE_LOADED_PATH_PREFIX) && systemPreLoadPreferences.getBoolean(bucketPath, false)) {
                            isNeedContinue = false;
                        }
                    }
                case 21:
                    if (lowerPathName.endsWith(".empty_in")) {
                        isNeedContinue = true;
                    } else if (((1 << mediaType) & typeBits) != 0) {
                        isNeedContinue = true;
                        if (bucketPath.startsWith(PRE_LOADED_PATH_PREFIX) && !systemPreLoadPreferences.getBoolean(bucketPath, false)) {
                            isNeedContinue = false;
                        }
                    }
                    if (isNeedContinue) {
                        if (blackList.match(lowerPathName)) {
                            continue;
                        } else {
                            isHidden = false;
                            if (!hiddenBuckets.contains(Integer.valueOf(bucketId))) {
                                if (!(bucketPath.startsWith("/mnt") || bucketPath.startsWith("/storage"))) {
                                }
                            }
                            if (showHiddenAlbums || !isHidden) {
                                if (((1 << mediaType) & typeBits) != 0) {
                                    dataCount = closeable.getInt(4);
                                }
                                entry = new BucketEntry(bucketId, closeable.getString(2), isHidden, bucketPath, dataCount);
                                mapEntry = (BucketEntry) map.get(bucketId);
                                if (mapEntry == null) {
                                    map.put(bucketId, entry);
                                    if (whiteList.match(lowerPathName)) {
                                        try {
                                            buffer.add(entry);
                                        } catch (SecurityException e) {
                                            bucketEntryArr = "BucketHelper";
                                            GalleryLog.w((String) bucketEntryArr, "No permission to query!");
                                            break;
                                        } finally {
                                            Utils.closeSilently(closeable);
                                        }
                                    } else if (bucketId != MediaSetUtils.DOCRECTIFY_BUCKET_ID) {
                                        whiteBuffer.add(0, entry);
                                    } else if (bucketId == MediaSetUtils.MAGAZINE_UNLOCK_BUCKET_ID) {
                                        index = (whiteBuffer.size() > 0 || ((BucketEntry) whiteBuffer.get(0)).bucketId != MediaSetUtils.DOCRECTIFY_BUCKET_ID) ? 0 : 1;
                                        whiteBuffer.add(index, entry);
                                    } else {
                                        whiteBuffer.add(entry);
                                    }
                                } else {
                                    mapEntry.addMediaCount(dataCount);
                                }
                            }
                        }
                    }
                    if (!jc.isCancelled()) {
                        Utils.closeSilently(closeable);
                        return null;
                    }
                    break;
                case 22:
                case 24:
                    if (lowerPathName.endsWith(".emptyshow")) {
                        isNeedContinue = true;
                    }
                    if (isNeedContinue) {
                        if (blackList.match(lowerPathName)) {
                            continue;
                        } else {
                            isHidden = false;
                            if (!hiddenBuckets.contains(Integer.valueOf(bucketId))) {
                                break;
                            }
                            if (!showHiddenAlbums) {
                            }
                            if (((1 << mediaType) & typeBits) != 0) {
                                dataCount = closeable.getInt(4);
                            }
                            entry = new BucketEntry(bucketId, closeable.getString(2), isHidden, bucketPath, dataCount);
                            mapEntry = (BucketEntry) map.get(bucketId);
                            if (mapEntry == null) {
                                mapEntry.addMediaCount(dataCount);
                            } else {
                                map.put(bucketId, entry);
                                if (whiteList.match(lowerPathName)) {
                                    buffer.add(entry);
                                } else if (bucketId != MediaSetUtils.DOCRECTIFY_BUCKET_ID) {
                                    whiteBuffer.add(0, entry);
                                } else if (bucketId == MediaSetUtils.MAGAZINE_UNLOCK_BUCKET_ID) {
                                    whiteBuffer.add(entry);
                                } else {
                                    if (whiteBuffer.size() > 0) {
                                        break;
                                    }
                                    whiteBuffer.add(index, entry);
                                }
                            }
                        }
                    }
                    if (!jc.isCancelled()) {
                        Utils.closeSilently(closeable);
                        return null;
                    }
                    break;
                default:
                    if (((1 << mediaType) & typeBits) != 0) {
                        isNeedContinue = true;
                    }
            }
            if (isNeedContinue) {
                if (blackList.match(lowerPathName)) {
                    isHidden = false;
                    isHidden = !hiddenBuckets.contains(Integer.valueOf(bucketId)) ? true : FilePreference.get(mApplication.getAndroidContext(), String.valueOf(bucketId));
                    if (showHiddenAlbums) {
                    }
                    if (((1 << mediaType) & typeBits) != 0) {
                        dataCount = closeable.getInt(4);
                    }
                    entry = new BucketEntry(bucketId, closeable.getString(2), isHidden, bucketPath, dataCount);
                    mapEntry = (BucketEntry) map.get(bucketId);
                    if (mapEntry == null) {
                        map.put(bucketId, entry);
                        if (whiteList.match(lowerPathName)) {
                            buffer.add(entry);
                        } else if (bucketId != MediaSetUtils.DOCRECTIFY_BUCKET_ID) {
                            whiteBuffer.add(0, entry);
                        } else if (bucketId == MediaSetUtils.MAGAZINE_UNLOCK_BUCKET_ID) {
                            if (whiteBuffer.size() > 0) {
                            }
                            whiteBuffer.add(index, entry);
                        } else {
                            whiteBuffer.add(entry);
                        }
                    } else {
                        mapEntry.addMediaCount(dataCount);
                    }
                } else {
                    continue;
                }
            }
            if (!jc.isCancelled()) {
                Utils.closeSilently(closeable);
                return null;
            }
        }
        ArrayList<BucketEntry> tempBuffer = new ArrayList();
        tempBuffer.addAll(whiteBuffer);
        tempBuffer.addAll(buffer);
        buffer = tempBuffer;
        Utils.closeSilently(closeable);
        if (20 == showAlbumsType) {
            ArrayList<Integer> sortedBucketIds = assignSortIndexForOutsideList(jc, buffer);
            if (sortedBucketIds == null) {
                ArrayList<Integer> arrayList = new ArrayList(512);
                mApplication.getGalleryData().getAlbumIndex(null, arrayList);
            }
            buffer.clear();
            for (Integer intValue : sortedBucketIds) {
                bucketId = intValue.intValue();
                if (map.get(bucketId) != null) {
                    buffer.add((BucketEntry) map.get(bucketId));
                }
            }
        } else if (22 == showAlbumsType || 24 == showAlbumsType) {
            Collections.sort(buffer, sBucketEntryComparator);
        }
        return (BucketEntry[]) buffer.toArray(new BucketEntry[buffer.size()]);
    }

    private static String getNoSelfAndPreloadClause(int type) {
        if ((524288 & type) != 0) {
            return " AND bucket_id not IN (" + getPasteSourceBucketId() + ", " + MediaSetUtils.PRELOAD_PICTURES_BUCKET_ID + ") ";
        }
        return "";
    }

    private static ArrayList<Integer> assignSortIndexForOutsideList(JobContext jc, ArrayList<BucketEntry> buffer) {
        if (buffer == null) {
            return null;
        }
        HashSet<Integer> bucketIds = new HashSet(512);
        ArrayList<Integer> sortedBucketIds = new ArrayList(512);
        mApplication.getGalleryData().getAlbumIndex(bucketIds, sortedBucketIds);
        boolean change = false;
        for (int i = buffer.size() - 1; i >= 0; i--) {
            BucketEntry entry = (BucketEntry) buffer.get(i);
            if (!bucketIds.contains(Integer.valueOf(entry.bucketId))) {
                change = true;
                mApplication.getGalleryData().addMaxAlbumIndex(entry.bucketId, entry.bucketPath);
            }
            if (jc.isCancelled()) {
                break;
            }
        }
        if (change) {
            return null;
        }
        return sortedBucketIds;
    }

    private static String getBucketNameInTable(ContentResolver resolver, Uri tableUri, int bucketId) {
        String string;
        String[] selectionArgs = new String[]{String.valueOf(bucketId)};
        Closeable closeable = null;
        try {
            closeable = resolver.query(tableUri.buildUpon().appendQueryParameter("limit", "1").build(), PROJECTION_BUCKET_IN_ONE_TABLE, "bucket_id = ?", selectionArgs, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            string = closeable.getString(2);
            return string;
        } catch (SecurityException e) {
            string = "BucketHelper";
            GalleryLog.w(string, "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    @TargetApi(11)
    private static Uri getFilesContentUri() {
        return Files.getContentUri("external");
    }

    public static String getBucketName(ContentResolver resolver, int bucketId) {
        String result;
        if (ApiHelper.HAS_MEDIA_PROVIDER_FILES_TABLE) {
            result = getBucketNameInTable(resolver, getFilesContentUri(), bucketId);
            if (result == null) {
                result = "";
            }
            return result;
        }
        result = getBucketNameInTable(resolver, Media.EXTERNAL_CONTENT_URI, bucketId);
        if (result != null) {
            return result;
        }
        result = getBucketNameInTable(resolver, Video.Media.EXTERNAL_CONTENT_URI, bucketId);
        if (result == null) {
            result = "";
        }
        return result;
    }

    public static void setPasteSourceBucketId(int bucketId) {
        mPasteSourceBucketId = bucketId;
    }

    public static int getPasteSourceBucketId() {
        return mPasteSourceBucketId;
    }
}
