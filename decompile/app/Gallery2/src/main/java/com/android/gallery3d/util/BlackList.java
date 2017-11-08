package com.android.gallery3d.util;

import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class BlackList extends CacheableList {
    private static BlackList sInstance;
    private HashSet<Integer> equalsSet;
    private String[] mMountPoints;
    private ArrayList<String> startWithList;

    private BlackList() {
        reset();
    }

    public synchronized void reset() {
        synchronized (this) {
            this.startWithList = new ArrayList();
            this.equalsSet = new HashSet();
            String[] volumePaths = GalleryUtils.getVolumePaths();
            this.mMountPoints = new String[volumePaths.length];
            for (int index = 0; index < volumePaths.length; index++) {
                this.mMountPoints[index] = volumePaths[index].toLowerCase(Locale.US);
            }
            for (String line : Prop4g.sBlackListProp) {
                String line2 = line2.trim();
                if (!(line2.length() == 0 || line2.startsWith("#"))) {
                    if (line2.endsWith("*")) {
                        line2 = line2.substring(0, line2.length() - 1);
                        this.startWithList.add(line2);
                    }
                    addBucketId(line2);
                }
            }
        }
    }

    public synchronized String getOuterVolumeBucketId() {
        ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        int outerGalleryStorageSize = outerGalleryStorageList.size();
        if (outerGalleryStorageSize == 0) {
            return "1";
        }
        int size = this.startWithList.size();
        if (size <= 0) {
            return "1";
        }
        StringBuilder bucketIds = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String path = (String) this.startWithList.get(i);
            int j = 0;
            while (j < outerGalleryStorageSize) {
                bucketIds.append(GalleryUtils.getBucketId(((GalleryStorage) outerGalleryStorageList.get(j)).getPath() + path.substring(0, path.length() - 1)));
                if (i != size - 1 || j != outerGalleryStorageSize - 1) {
                    bucketIds.append(",");
                }
                j++;
            }
        }
        return bucketIds.toString();
    }

    public synchronized boolean match(String filePath) {
        return super.match(filePath);
    }

    protected synchronized boolean onMatchFile(String filePath) {
        int end = filePath.lastIndexOf("/");
        if (end >= 0) {
            filePath = filePath.substring(0, end);
        }
        if (this.equalsSet.contains(Integer.valueOf(filePath.hashCode()))) {
            return true;
        }
        if (!filePath.contains(Prop4g.ANDROID_DATA)) {
            return false;
        }
        return changeFilePathToDir(filePath).startsWith(Prop4g.ANDROID_DATA);
    }

    private String changeFilePathToDir(String filePath) {
        return filePath.substring(filePath.indexOf("/", getSkipLengthFromPath(filePath)), filePath.lastIndexOf("/") + 1);
    }

    private int getSkipLengthFromPath(String path) {
        for (String volumePath : this.mMountPoints) {
            if (path.startsWith(volumePath)) {
                return volumePath.length();
            }
        }
        return 0;
    }

    private void addBucketId(String relativePath) {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        if (innerGalleryStorage != null) {
            addBucketIdWithVolume(relativePath, innerGalleryStorage.getPath());
        }
        for (GalleryStorage galleryStorage : outerGalleryStorageList) {
            addBucketIdWithVolume(relativePath, galleryStorage.getPath());
        }
    }

    private void addBucketIdWithVolume(String relativePath, String volumePath) {
        this.equalsSet.add(Integer.valueOf(GalleryUtils.getBucketId(volumePath + relativePath.substring(0, relativePath.lastIndexOf("/")))));
    }

    public static synchronized BlackList getInstance() {
        BlackList blackList;
        synchronized (BlackList.class) {
            if (sInstance == null) {
                sInstance = new BlackList();
            }
            blackList = sInstance;
        }
        return blackList;
    }
}
