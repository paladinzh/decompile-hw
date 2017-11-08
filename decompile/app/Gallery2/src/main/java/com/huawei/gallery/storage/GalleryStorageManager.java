package com.huawei.gallery.storage;

import android.content.Context;
import android.util.SparseArray;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.proguard.Keep;
import java.util.ArrayList;

public class GalleryStorageManager {
    private static GalleryStorageManager sGalleryStorageManager;
    private SparseArray<GalleryStorage> mGalleryStorageBucketIDMap = new SparseArray(12);
    private GalleryStorage mInnerGalleryStorage;
    private SparseArray<GalleryStorage> mOuterGalleryStorageCameraBucketIDMap = new SparseArray(4);
    private ArrayList<GalleryStorage> mOuterGalleryStorageList = new ArrayList(4);
    private SparseArray<GalleryStorage> mOuterGalleryStorageScreenshotsBucketIDMap = new SparseArray(4);
    private GalleryStorage mSubUserGalleryStorage;

    private GalleryStorageManager() {
    }

    public static synchronized GalleryStorageManager getInstance() {
        GalleryStorageManager galleryStorageManager;
        synchronized (GalleryStorageManager.class) {
            if (sGalleryStorageManager == null) {
                sGalleryStorageManager = new GalleryStorageManager();
            }
            galleryStorageManager = sGalleryStorageManager;
        }
        return galleryStorageManager;
    }

    public synchronized void updateOuterGalleryStorageList(Context context, GalleryStorage innerGalleryStorage, ArrayList<GalleryStorage> galleryStorageList) {
        GalleryLog.d("GalleryStorageManager", "updateOuterGalleryStorageList innerGalleryStorage:" + innerGalleryStorage + ", galleryStorageList:" + galleryStorageList);
        this.mInnerGalleryStorage = innerGalleryStorage;
        this.mOuterGalleryStorageList.clear();
        this.mGalleryStorageBucketIDMap.clear();
        this.mOuterGalleryStorageCameraBucketIDMap.clear();
        this.mOuterGalleryStorageScreenshotsBucketIDMap.clear();
        galleryStorageList.add(0, innerGalleryStorage);
        this.mSubUserGalleryStorage = null;
        for (GalleryStorage galleryStorage : galleryStorageList) {
            if (galleryStorage != null && galleryStorage.isMounted()) {
                if (galleryStorage instanceof GalleryOuterStorage) {
                    this.mOuterGalleryStorageList.add(galleryStorage);
                    this.mOuterGalleryStorageCameraBucketIDMap.put(galleryStorage.getBucketID(Constant.CAMERA_PATH), galleryStorage);
                    this.mOuterGalleryStorageScreenshotsBucketIDMap.put(galleryStorage.getBucketID("/Pictures/Screenshots"), galleryStorage);
                }
                this.mGalleryStorageBucketIDMap.put(galleryStorage.getBucketID(Constant.CAMERA_PATH), galleryStorage);
                this.mGalleryStorageBucketIDMap.put(galleryStorage.getBucketID("/Pictures/Screenshots"), galleryStorage);
                this.mGalleryStorageBucketIDMap.put(galleryStorage.getRootBucketID(), galleryStorage);
                if (!galleryStorage.isMountedOnCurrentUser()) {
                    this.mSubUserGalleryStorage = galleryStorage;
                }
            }
        }
        if (this.mOuterGalleryStorageList.size() == 1) {
            ((GalleryStorage) this.mOuterGalleryStorageList.get(0)).updateName(context);
        }
    }

    public synchronized GalleryStorage getInnerGalleryStorage() {
        if (this.mInnerGalleryStorage == null) {
            return null;
        }
        GalleryStorage innerGalleryStorage = new GalleryInnerStorage();
        innerGalleryStorage.copy(this.mInnerGalleryStorage);
        return innerGalleryStorage;
    }

    public synchronized ArrayList<GalleryStorage> getOuterGalleryStorageList() {
        ArrayList<GalleryStorage> galleryStorageArrayList;
        galleryStorageArrayList = new ArrayList(4);
        for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
            GalleryStorage outerGalleryStorage = new GalleryOuterStorage();
            outerGalleryStorage.copy(galleryStorage);
            galleryStorageArrayList.add(outerGalleryStorage);
        }
        return galleryStorageArrayList;
    }

    public synchronized ArrayList<GalleryStorage> getInnerAndOuterStorage() {
        ArrayList<GalleryStorage> outers;
        GalleryStorage inner = getInnerGalleryStorage();
        outers = getOuterGalleryStorageList();
        outers.add(0, inner);
        return outers;
    }

    public synchronized GalleryStorage getSubUserGalleryStorage() {
        if (this.mSubUserGalleryStorage == null) {
            return null;
        }
        GalleryStorage galleryStorage = new GalleryOuterStorage();
        galleryStorage.copy(this.mSubUserGalleryStorage);
        return galleryStorage;
    }

    public synchronized ArrayList<GalleryStorage> getOuterGalleryStorageListMountedOnCurrentUser() {
        ArrayList<GalleryStorage> galleryStorageArrayList;
        galleryStorageArrayList = new ArrayList(4);
        for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
            if (galleryStorage.isMountedOnCurrentUser()) {
                GalleryStorage outerGalleryStorage = new GalleryOuterStorage();
                outerGalleryStorage.copy(galleryStorage);
                galleryStorageArrayList.add(outerGalleryStorage);
            }
        }
        return galleryStorageArrayList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean equalsOfOuterGalleryStorageList(ArrayList<GalleryStorage> outerGalleryStorageArrayList) {
        if (outerGalleryStorageArrayList != null) {
            if (outerGalleryStorageArrayList.size() == this.mOuterGalleryStorageList.size()) {
                for (int index = 0; index < outerGalleryStorageArrayList.size(); index++) {
                    if (!((GalleryStorage) this.mOuterGalleryStorageList.get(index)).equals(outerGalleryStorageArrayList.get(index))) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public synchronized GalleryStorage getGalleryStorageByBucketID(int bucketID) {
        return (GalleryStorage) this.mGalleryStorageBucketIDMap.get(bucketID);
    }

    public String getOuterGalleryStorageCameraBucketIDs() {
        return getOuterGalleryStorageBucketIDs(Constant.CAMERA_PATH);
    }

    public String getOuterGalleryStorageScreenshotsBucketIDs() {
        return getOuterGalleryStorageBucketIDs("/Pictures/Screenshots");
    }

    public synchronized String getOuterGalleryStorageBucketIDs(String path) {
        if (this.mOuterGalleryStorageList.size() == 0) {
            return "0";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(((GalleryStorage) this.mOuterGalleryStorageList.get(0)).getBucketID(path));
        int size = this.mOuterGalleryStorageList.size();
        for (int index = 1; index < size; index++) {
            GalleryStorage galleryStorage = (GalleryStorage) this.mOuterGalleryStorageList.get(index);
            stringBuffer.append(",");
            stringBuffer.append(galleryStorage.getBucketID(path));
        }
        return stringBuffer.toString();
    }

    public String getOuterGalleryStorageCameraBucketIDsMountedOnCurrentUser() {
        return getOuterGalleryStorageBucketIDsMountedOnCurrentUser(Constant.CAMERA_PATH);
    }

    public String getOuterGalleryStorageScreenshotsBucketIDsMountedOnCurrentUser() {
        return getOuterGalleryStorageBucketIDsMountedOnCurrentUser("/Pictures/Screenshots");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String getOuterGalleryStorageBucketIDsMountedOnCurrentUser(String path) {
        if (this.mOuterGalleryStorageList.size() == 0) {
            return "0";
        }
        StringBuffer stringBuffer = new StringBuffer();
        boolean isBucketIDFound = false;
        int size = this.mOuterGalleryStorageList.size();
        for (int index = 0; index < size; index++) {
            GalleryStorage galleryStorage = (GalleryStorage) this.mOuterGalleryStorageList.get(index);
            if (galleryStorage.isMountedOnCurrentUser()) {
                if (isBucketIDFound) {
                    stringBuffer.append(",");
                }
                stringBuffer.append(galleryStorage.getBucketID(path));
                isBucketIDFound = true;
            }
        }
    }

    public String getSubUserCameraBucketId() {
        return getSubUserBucketId(Constant.CAMERA_PATH);
    }

    public String getSubUserScreenshotsBucketId() {
        return getSubUserBucketId("/Pictures/Screenshots");
    }

    public synchronized String getSubUserBucketId(String path) {
        return this.mSubUserGalleryStorage == null ? "0" : "" + this.mSubUserGalleryStorage.getBucketID(path);
    }

    public synchronized ArrayList<Integer> getOuterGalleryStorageBucketIDsByArrayList(String path) {
        ArrayList<Integer> result = new ArrayList();
        if (this.mOuterGalleryStorageList.size() == 0) {
            return result;
        }
        result.add(Integer.valueOf(((GalleryStorage) this.mOuterGalleryStorageList.get(0)).getBucketID(path)));
        int size = this.mOuterGalleryStorageList.size();
        for (int index = 1; index < size; index++) {
            result.add(Integer.valueOf(((GalleryStorage) this.mOuterGalleryStorageList.get(index)).getBucketID(path)));
        }
        return result;
    }

    public synchronized boolean isOuterGalleryStorageCameraBucketID(int bucketID) {
        return this.mOuterGalleryStorageCameraBucketIDMap.get(bucketID) != null;
    }

    public boolean isOuterGalleryStorageCameraBucketID(String bucketID) {
        return isOuterGalleryStorageBucketID(Constant.CAMERA_PATH, bucketID);
    }

    public synchronized boolean isOuterGalleryStorageScreenshotsBucketID(int bucketID) {
        return this.mOuterGalleryStorageScreenshotsBucketIDMap.get(bucketID) != null;
    }

    public boolean isOuterGalleryStorageScreenshotsBucketID(String bucketID) {
        return isOuterGalleryStorageBucketID("/Pictures/Screenshots", bucketID);
    }

    public synchronized boolean isOuterGalleryStorageBucketID(String path, String bucketID) {
        if (bucketID == null) {
            return false;
        }
        try {
            int bucketIdInt = Integer.parseInt(bucketID);
            for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
                if (bucketIdInt == galleryStorage.getBucketID(path)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<String> getOuterGalleryStorageCameraBucketPathList() {
        return getOuterGalleryStorageBucketPathList(Constant.CAMERA_PATH);
    }

    public ArrayList<String> getOuterGalleryStorageScreenshotsBucketPathList() {
        return getOuterGalleryStorageBucketPathList("/Pictures/Screenshots");
    }

    public synchronized ArrayList<String> getOuterGalleryStorageBucketPathList(String path) {
        ArrayList<String> pathList;
        pathList = new ArrayList(4);
        for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
            pathList.add("/local/image/" + galleryStorage.getBucketID(path));
        }
        return pathList;
    }

    public synchronized boolean hasAnyMountedOuterGalleryStorage() {
        for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
            if (galleryStorage.isMounted() && galleryStorage.isRemovable()) {
                return true;
            }
        }
        return false;
    }

    @Keep
    public synchronized GalleryStorage getGalleryStorageByPath(String path) {
        if (path == null) {
            return null;
        }
        if (this.mInnerGalleryStorage == null || !path.startsWith(this.mInnerGalleryStorage.getPath())) {
            for (GalleryStorage galleryStorage : this.mOuterGalleryStorageList) {
                if (path.startsWith(galleryStorage.getPath())) {
                    return galleryStorage;
                }
            }
            return null;
        }
        return this.mInnerGalleryStorage;
    }
}
