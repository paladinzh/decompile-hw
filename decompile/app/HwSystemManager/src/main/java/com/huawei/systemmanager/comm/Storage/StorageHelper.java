package com.huawei.systemmanager.comm.Storage;

import android.content.Context;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class StorageHelper {
    public static final String TAG = "StorageHelper";
    private final List<Storage> mStorageList = initialPath(GlobalContext.getContext());

    private StorageHelper() {
    }

    private static List<Storage> initialPath(Context context) {
        List<Storage> result = Lists.newArrayList();
        StorageManager sm = (StorageManager) context.getSystemService("storage");
        StorageVolume[] storageVolumes = sm.getVolumeList();
        if (storageVolumes == null) {
            HwLog.e(TAG, "initialPath_M failed! storageVolumes == null");
            return result;
        }
        for (StorageVolume volume : storageVolumes) {
            if (volume.isPrimary()) {
                result.add(new Storage(0, volume.getPath(), true));
            } else if (volume.isRemovable()) {
                String uuid = volume.getUuid();
                if (uuid == null) {
                    HwLog.w(TAG, "volume uuid is null");
                } else {
                    VolumeInfo volumeInfo = sm.findVolumeByUuid(uuid);
                    if (volumeInfo == null) {
                        HwLog.w(TAG, "cannot findvolumebyuuid, uuid:" + uuid);
                    } else {
                        DiskInfo diskInfo = volumeInfo.getDisk();
                        if (diskInfo == null) {
                            HwLog.w(TAG, "diskInfo is null, uuid:" + uuid);
                        } else {
                            int state = volumeInfo.getState();
                            boolean avaliable = checkStorageState(state);
                            String path;
                            if (diskInfo.isSd()) {
                                path = volume.getPath();
                                HwLog.i(TAG, "add sdcard path:" + path + ", state:" + state);
                                result.add(new Storage(1, path, avaliable));
                            } else if (diskInfo.isUsb()) {
                                path = volume.getPath();
                                HwLog.i(TAG, "usb disk path:" + path + ", state:" + state);
                                result.add(new Storage(2, path, avaliable));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean checkStorageState(int state) {
        return state == 2;
    }

    private long getTotalBlockSize(String path) {
        try {
            StatFs stat = new StatFs(path);
            return stat.getBlockSizeLong() * stat.getBlockCountLong();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private long getAvaiableBlockSize(String path) {
        try {
            StatFs stat = new StatFs(path);
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static StorageHelper getStorage() {
        return new StorageHelper();
    }

    public String getInnerRootPath() {
        for (Storage s : this.mStorageList) {
            if (s.getPosition() == 0) {
                return s.getPath();
            }
        }
        return "";
    }

    public List<String> getSdcardRootPath() {
        List<String> list = Lists.newArrayList();
        for (Storage s : this.mStorageList) {
            if (s.getPosition() == 1 && s.isAvaliable()) {
                list.add(s.getPath());
            }
        }
        return list;
    }

    public boolean isSdcardaviliable() {
        for (Storage s : this.mStorageList) {
            if (s.getPosition() == 1 && s.isAvaliable()) {
                return true;
            }
        }
        return false;
    }

    public long getTotalSize(int position) {
        long totalSize = 0;
        for (Storage s : this.mStorageList) {
            if (s.getPosition() == position && s.isAvaliable()) {
                String path = s.getPath();
                if (!TextUtils.isEmpty(path)) {
                    totalSize += getTotalBlockSize(path);
                }
            }
        }
        return totalSize;
    }

    public long getAvalibaleSize(int position) {
        long totalSize = 0;
        for (Storage s : this.mStorageList) {
            if (s.getPosition() == position && s.isAvaliable()) {
                String path = s.getPath();
                if (!TextUtils.isEmpty(path)) {
                    totalSize += getAvaiableBlockSize(path);
                }
            }
        }
        return totalSize;
    }

    public int getUsedPercent(int position) {
        int percent = 0;
        long total = getTotalSize(position);
        long used = total - getAvalibaleSize(position);
        if (total != 0) {
            percent = (int) ((100 * used) / total);
        }
        if (percent != 0 || used <= 0) {
            return percent;
        }
        return 1;
    }

    public int getFreePercent(int position) {
        int percent = 0;
        long total = getTotalSize(position);
        long free = getAvalibaleSize(position);
        if (total != 0) {
            percent = (int) ((100 * free) / total);
        }
        if (percent != 0 || free <= 0) {
            return percent;
        }
        return 1;
    }

    public boolean isDefaultSDCard() {
        return SystemProperties.get("persist.sys.primarysd", "0").equals("1");
    }
}
