package com.android.server.rms.collector;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class MemoryFragReader {
    private static final boolean DEBUG;
    private static final int DEFAULT_AVAILABLE_MEM_BLOCK_SUM = 0;
    private static final long DEFAULT_FRAGLEVEL = -1;
    private static final int DEFAULT_FRAG_PAGE_ORDER = 4;
    private static final int DEFAULT_FRAG_PAGE_ORDER_0 = 0;
    private static final long DEFAULT_SUM_LEVEL = 0;
    private static final int FRAG_ZONE_DMA = 0;
    private static final int INDEX_ZONE_NAME = 3;
    private static final String SPLIT_AVAILABLE_NUM = "\\s+";
    private static final String TAG = "RMS.MemoryFragReader";
    private int[] mInfos = null;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public void readMemFragInfo() {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            this.mInfos = getMemoryAvailableNumInZone(0);
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public int[] getMemFragInfo() {
        if (this.mInfos != null) {
            return (int[]) this.mInfos.clone();
        }
        return new int[0];
    }

    public static long[] getMemoryFragLevelsOfAllZones() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryFragLevelsOfAllZones method");
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length != 0) {
            return getAllZoneFragLevel(zoneMemoryInfoArray);
        }
        Log.e(TAG, "getMemoryFragLevelsOfAllZones,the zoneMemoryInfoArray is empty");
        return new long[0];
    }

    public static int getMemoryAvailableNumInAllZones() {
        int i = 0;
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryFragLevelsOfAllZones method");
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length == 0) {
            Log.e(TAG, "getMemoryAvailableNumInAllZones,the zoneMemoryInfoArray is empty");
            return 0;
        }
        int orderMemorySum = 0;
        int length = zoneMemoryInfoArray.length;
        while (i < length) {
            String zoneMemmoryInfo = zoneMemoryInfoArray[i];
            if (!(zoneMemmoryInfo == null || zoneMemmoryInfo.trim().isEmpty())) {
                orderMemorySum += calculateAvailableMemBlockNum(8, 4, zoneMemmoryInfo.split(SPLIT_AVAILABLE_NUM));
            }
            i++;
        }
        return orderMemorySum;
    }

    private static int calculateAvailableMemBlockNum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the calculateAvailableMemBlockNum method");
        }
        int availableMemBlockSum = 0;
        int pageIndex = startPageIndex;
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                availableMemBlockSum += Integer.parseInt(zoneArray[index]);
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "calculateAvailableMemBlockNum, zone meminfo's value is invalid,pageIndex" + pageIndex + ",zone array index:" + index);
                return 0;
            }
        }
        return availableMemBlockSum;
    }

    private static String[] getZoneMemoryInfoArrayFromBuddyinfo() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getZoneMemoryInfoArrayFromBuddyinfo method");
        }
        String buddyInfoLines = getBuddyInfoLines();
        if (buddyInfoLines != null && !buddyInfoLines.trim().isEmpty()) {
            return buddyInfoLines.split("\n");
        }
        Log.e(TAG, "getZoneMemoryInfoArrayFromBuddyinfo,the buddyInfoLines is empty");
        return new String[0];
    }

    private static String getBuddyInfoLines() {
        if (DEBUG) {
            Log.i(TAG, "invoke the getBuddyInfoLines method");
        }
        String buddyInfoResult = null;
        try {
            buddyInfoResult = ResourceCollector.getBuddyInfo();
            if (DEBUG) {
                Log.i(TAG, "getBuddyInfoLines:buddyinfo:\n" + buddyInfoResult);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException happens,sysrms_jni not contains nativeCalculateFragment");
        } catch (Exception e2) {
            Log.e(TAG, "sysrms_jni not contains nativeCalculateFragment");
        }
        return buddyInfoResult;
    }

    private static long[] getAllZoneFragLevel(String[] zoneMemoryInfoArray) {
        if (DEBUG) {
            Log.i(TAG, "invoke the getAllZoneFragLevel method");
        }
        List<Long> zoneLevelList = new ArrayList();
        for (String zoneInfo : zoneMemoryInfoArray) {
            if (!(zoneInfo == null || zoneInfo.trim().isEmpty())) {
                String[] zoneArray = zoneInfo.split(SPLIT_AVAILABLE_NUM);
                if (zoneArray.length != 0 && zoneArray.length > 3) {
                    zoneLevelList.add(Long.valueOf(calculateZoneFragLevel(zoneArray, 4)));
                }
            }
        }
        if (zoneLevelList.size() == 0) {
            return new long[0];
        }
        long[] resultLevelArray = new long[zoneLevelList.size()];
        for (int index = 0; index < zoneLevelList.size(); index++) {
            resultLevelArray[index] = ((Long) zoneLevelList.get(index)).longValue();
        }
        return resultLevelArray;
    }

    private static long calculateZoneFragLevel(String[] zoneArray, int memoryOrder) {
        if (DEBUG) {
            Log.i(TAG, "invoke the calculateZoneFragLevel method");
        }
        if (zoneArray.length <= memoryOrder + 4) {
            Log.e(TAG, "calculateZoneFragLevel,zoneArray's length is invalid");
            return -1;
        }
        long fragLevelSum = calculateLevelSum(4, 0, zoneArray);
        long orderMemorySum = calculateLevelSum(memoryOrder + 4, memoryOrder, zoneArray);
        long fragLevel = -1;
        if (fragLevelSum != 0) {
            fragLevel = ((fragLevelSum - orderMemorySum) * 100) / fragLevelSum;
        }
        if (DEBUG) {
            Log.i(TAG, "calculateZoneFragLevel,frageLevel:" + fragLevel + ",fragLevelSum:" + fragLevelSum + ",orderMemorySum:" + orderMemorySum);
        }
        return fragLevel;
    }

    private static long calculateLevelSum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the calculateLevelSum method");
        }
        long levelSum = 0;
        int pageIndex = startPageIndex;
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                levelSum += ((long) (1 << pageIndex)) * Long.parseLong(zoneArray[index]);
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "calculateLevelSum, zone meminfo's value is invalid,,pageIndex" + pageIndex + ",zone array index:" + index);
                return 0;
            }
        }
        return levelSum;
    }

    private int[] getMemoryAvailableNumInZone(int zoneIndex) {
        if (DEBUG) {
            Log.i(TAG, "invoke the getMemoryAvailableNumInZone method");
        }
        int[] availableNumArray = new int[0];
        if (zoneIndex < 0) {
            return availableNumArray;
        }
        String[] zoneMemoryInfoArray = getZoneMemoryInfoArrayFromBuddyinfo();
        if (zoneMemoryInfoArray.length == 0) {
            Log.e(TAG, "getMemoryAvailableNumInZone,the zoneMemoryInfoArray is empty");
            return availableNumArray;
        } else if (zoneIndex <= zoneMemoryInfoArray.length - 1) {
            return getAvailableMemBlockNum(4, 0, zoneMemoryInfoArray[0].split(SPLIT_AVAILABLE_NUM));
        } else {
            Log.e(TAG, "getMemoryAvailableNumInZone,the zoneMemoryInfoArray is empty");
            return availableNumArray;
        }
    }

    private int[] getAvailableMemBlockNum(int startIndex, int startPageIndex, String[] zoneArray) {
        if (DEBUG) {
            Log.d(TAG, "invoke the getAvailableMemBlockNum method");
        }
        int pageIndex = startPageIndex;
        List<Integer> memoryNumList = new ArrayList();
        int index = startIndex;
        while (index < zoneArray.length) {
            try {
                memoryNumList.add(Integer.valueOf(Integer.parseInt(zoneArray[index])));
                pageIndex++;
                index++;
            } catch (Exception e) {
                Log.e(TAG, "getAvailableMemBlockNum, zone meminfo's value is invalid,pageIndex" + pageIndex + ",zone array index:" + index);
            }
        }
        if (memoryNumList.size() == 0) {
            return new int[0];
        }
        int[] availableArray = new int[memoryNumList.size()];
        for (index = 0; index < availableArray.length; index++) {
            availableArray[index] = ((Integer) memoryNumList.get(index)).intValue();
        }
        return availableArray;
    }
}
