package com.huawei.gallery.storage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageUtils {
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);

    public static synchronized void updateStorageVolume(Context context, StorageManager storageManager) {
        synchronized (StorageUtils.class) {
            GalleryStorageManager galleryStorageManager = GalleryStorageManager.getInstance();
            int currentUserId = ActivityManager.getCurrentUser();
            StorageVolume[] storageVolumes = StorageManager.getVolumeList(currentUserId, 512);
            int outerIndex = 0;
            ArrayList<GalleryStorage> outerGalleryStorageList = new ArrayList(4);
            GalleryStorage innerGalleryStorage = null;
            for (StorageVolume storageVolume : storageVolumes) {
                if (ApiHelper.HAS_MULTI_USER_STORAGE) {
                    if (storageVolume.isPrimary()) {
                        innerGalleryStorage = new GalleryInnerStorage(context, storageVolume);
                    } else if (storageVolume.isRemovable()) {
                        outerIndex++;
                        outerGalleryStorageList.add(new GalleryOuterStorage(context, storageVolume, outerIndex, true));
                    }
                } else if (storageVolume.isRemovable()) {
                    outerIndex++;
                    outerGalleryStorageList.add(new GalleryOuterStorage(context, storageVolume, outerIndex, true));
                } else {
                    innerGalleryStorage = new GalleryInnerStorage(context, storageVolume);
                }
            }
            updateSubUserStorageVolume(context, storageManager, currentUserId, outerGalleryStorageList);
            galleryStorageManager.updateOuterGalleryStorageList(context, innerGalleryStorage, outerGalleryStorageList);
        }
    }

    private static void updateSubUserStorageVolume(Context context, StorageManager storageManager, int currentUserId, List<GalleryStorage> galleryStorageList) {
        if (IS_SUPPORT_CLONE_APP) {
            int subUserStorageIndex = 0;
            List<UserInfo> profiles = ((UserManager) context.getSystemService("user")).getProfiles(context.getUserId());
            int userCount = profiles == null ? 0 : profiles.size();
            for (int i = 0; i < userCount; i++) {
                UserInfo userInfo = (UserInfo) profiles.get(i);
                if (!(userInfo == null || userInfo.id == currentUserId || userInfo.isManagedProfile())) {
                    StorageVolume[] userStorageVolumes = StorageManager.getVolumeList(userInfo.id, 512);
                    int cnt = userStorageVolumes.length;
                    for (int j = 0; j < cnt; j++) {
                        if (!userStorageVolumes[j].isRemovable()) {
                            subUserStorageIndex--;
                            galleryStorageList.add(new GalleryOuterStorage(context, userStorageVolumes[j], subUserStorageIndex, false));
                        }
                    }
                }
            }
        }
    }

    public static boolean isStorageMounted(StorageVolume volume) {
        if (volume == null) {
            return false;
        }
        try {
            return "mounted".equals(volume.getState());
        } catch (Exception ex) {
            GalleryLog.d("StorageUtils", "isStorageMounted fail, " + ex.getMessage());
            return false;
        }
    }

    public static StorageVolume[] getVolumeList(Context context, StorageManager storageManager) {
        if (context == null || storageManager == null) {
            return new StorageVolume[0];
        }
        StorageVolume[] storageVolumes;
        if (IS_SUPPORT_CLONE_APP) {
            List<UserInfo> profiles = ((UserManager) context.getSystemService("user")).getProfiles(context.getUserId());
            List<StorageVolume> storageVolumeList = new ArrayList(4);
            int currentUserId = ActivityManager.getCurrentUser();
            int userCount = profiles == null ? 0 : profiles.size();
            for (int i = 0; i < userCount; i++) {
                UserInfo userInfo = (UserInfo) profiles.get(i);
                if (userInfo != null) {
                    StorageVolume[] userStorageVolumes = StorageManager.getVolumeList(userInfo.id, 512);
                    if (userInfo.id == currentUserId) {
                        storageVolumeList.addAll(Arrays.asList(userStorageVolumes));
                    } else if (!userInfo.isManagedProfile()) {
                        int cnt = userStorageVolumes.length;
                        for (int j = 0; j < cnt; j++) {
                            if (!userStorageVolumes[j].isRemovable()) {
                                storageVolumeList.add(userStorageVolumes[j]);
                            }
                        }
                    }
                }
            }
            storageVolumes = (StorageVolume[]) storageVolumeList.toArray(new StorageVolume[storageVolumeList.size()]);
        } else {
            storageVolumes = storageManager.getVolumeList();
        }
        if (storageVolumes == null) {
            storageVolumes = new StorageVolume[0];
        }
        return storageVolumes;
    }

    public static String[] getVolumePaths(Context context, StorageManager storageManager) {
        if (context == null || storageManager == null) {
            return new String[0];
        }
        StorageVolume[] storageVolumes = getVolumeList(context, storageManager);
        int count = storageVolumes.length;
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            paths[i] = storageVolumes[i].getPath();
        }
        return paths;
    }
}
