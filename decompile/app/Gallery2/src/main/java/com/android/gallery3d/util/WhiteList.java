package com.android.gallery3d.util;

import com.android.gallery3d.data.BucketHelper;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class WhiteList extends CacheableList {
    private static HashSet<Integer> sEqualsSet = new HashSet();
    private static WhiteList sInstance;
    private static String sWhiteListBucketIdsInString = null;
    private static String sWhiteListBucketIdsWithoutPreLoadedInString = null;

    private WhiteList() {
    }

    protected boolean onMatchFile(String filePath) {
        return sEqualsSet.contains(Integer.valueOf(filePath.substring(0, filePath.lastIndexOf("/")).hashCode()));
    }

    public static synchronized WhiteList getInstance() {
        WhiteList whiteList;
        synchronized (WhiteList.class) {
            if (sInstance == null) {
                sInstance = new WhiteList();
            }
            whiteList = sInstance;
        }
        return whiteList;
    }

    public static String getBucketIdForWhiteList() {
        return getBucketIdForWhiteList(false);
    }

    @SuppressWarnings({"DM_CONVERT_CASE"})
    public static String getBucketIdForWhiteList(boolean force) {
        if (sWhiteListBucketIdsInString != null && !force) {
            return sWhiteListBucketIdsInString;
        }
        ArrayList<String> equalsList = new ArrayList();
        for (String line : Prop4g.sWhiteListProp) {
            String line2 = line2.trim().toUpperCase();
            if (line2.endsWith("*")) {
                equalsList.add(line2.substring(0, line2.length() - 2));
            } else {
                equalsList.add(line2.substring(0, line2.length() - 1));
            }
        }
        sWhiteListBucketIdsInString = transArrayToString(generateBucketIdForWhiteList(equalsList, false));
        return sWhiteListBucketIdsInString;
    }

    public static String getBucketIdForWhiteListWithoutPreLoadedPath() {
        return getBucketIdForWhiteListWithoutPreLoadedPath(false);
    }

    @SuppressWarnings({"DM_CONVERT_CASE"})
    public static String getBucketIdForWhiteListWithoutPreLoadedPath(boolean force) {
        if (sWhiteListBucketIdsWithoutPreLoadedInString != null && !force) {
            return sWhiteListBucketIdsWithoutPreLoadedInString;
        }
        ArrayList<String> equalsList = new ArrayList();
        for (String line : Prop4g.sWhiteListProp) {
            String line2 = line2.trim().toUpperCase();
            if (line2.endsWith("*")) {
                equalsList.add(line2.substring(0, line2.length() - 2));
            } else {
                equalsList.add(line2.substring(0, line2.length() - 1));
            }
        }
        sWhiteListBucketIdsWithoutPreLoadedInString = transArrayToString(generateBucketIdForWhiteList(equalsList, true));
        return sWhiteListBucketIdsWithoutPreLoadedInString;
    }

    private static ArrayList<Integer> generateBucketIdForWhiteList(ArrayList<String> paths, boolean noPreLoadedPath) {
        if (!noPreLoadedPath) {
            sEqualsSet.clear();
        }
        ArrayList<Integer> whiteListBucketIds = new ArrayList();
        for (String path : paths) {
            if (!(noPreLoadedPath && (path.startsWith(Prop4g.MAGAZINE_UNLOCK.toUpperCase()) || path.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX.toUpperCase())))) {
                int bucketId;
                if (path.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX.toUpperCase())) {
                    bucketId = GalleryUtils.getBucketId(BucketHelper.PRE_LOADED_PATH_PICTURE);
                    whiteListBucketIds.add(Integer.valueOf(bucketId));
                    sEqualsSet.add(Integer.valueOf(bucketId));
                    bucketId = GalleryUtils.getBucketId(BucketHelper.PRE_LOADED_PATH_VIDEO);
                    whiteListBucketIds.add(Integer.valueOf(bucketId));
                    sEqualsSet.add(Integer.valueOf(bucketId));
                } else {
                    GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
                    if (innerGalleryStorage != null) {
                        bucketId = GalleryUtils.getBucketId(innerGalleryStorage.getPath() + path.substring(0, path.length()));
                        whiteListBucketIds.add(Integer.valueOf(bucketId));
                        if (!noPreLoadedPath) {
                            sEqualsSet.add(Integer.valueOf(bucketId));
                        }
                    }
                    for (GalleryStorage galleryStorage : GalleryStorageManager.getInstance().getOuterGalleryStorageList()) {
                        bucketId = GalleryUtils.getBucketId(galleryStorage.getPath() + path.substring(0, path.length()));
                        whiteListBucketIds.add(Integer.valueOf(bucketId));
                        if (!noPreLoadedPath) {
                            sEqualsSet.add(Integer.valueOf(bucketId));
                        }
                    }
                }
            }
        }
        return whiteListBucketIds;
    }

    private static String transArrayToString(ArrayList<Integer> lists) {
        StringBuilder buffer = new StringBuilder();
        Iterator<?> it = lists.iterator();
        while (it.hasNext()) {
            buffer.append(it.next());
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    public static void updatePreloadedPathsForWhiteList(String basePreloadPath) {
        Prop4g.sWhiteListProp[0] = basePreloadPath + "/Pictures/";
        Prop4g.sWhiteListProp[1] = basePreloadPath + "/Video/";
    }
}
