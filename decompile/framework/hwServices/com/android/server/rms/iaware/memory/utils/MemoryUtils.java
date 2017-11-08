package com.android.server.rms.iaware.memory.utils;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwaredConnection;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareAppMngSortPolicy;
import com.android.server.mtm.iaware.appmng.AwareProcessBlockInfo;
import com.android.server.rms.collector.ResourceCollector;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class MemoryUtils {
    private static final String TAG = "AwareMem_MemoryUtils";

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int groupId) {
        return getAppMngSortPolicy(groupId, 0);
    }

    public static AwareAppMngSortPolicy getAppMngSortPolicy(int groupId, int subType) {
        if (!AwareAppMngSort.checkAppMngEnable() || groupId < 0 || groupId > 3) {
            return null;
        }
        AwareAppMngSort sorted = AwareAppMngSort.getInstance();
        if (sorted == null) {
            return null;
        }
        return sorted.getAppMngSortPolicy(0, subType, groupId);
    }

    public static List<AwareProcessBlockInfo> getAppMngProcGroup(AwareAppMngSortPolicy policy, int groupId) {
        if (policy == null) {
            AwareLog.e(TAG, "getAppMngProcGroup sort policy null!");
            return null;
        }
        List<AwareProcessBlockInfo> processGroups = null;
        switch (groupId) {
            case 0:
                processGroups = policy.getForbidStopProcBlockList();
                break;
            case 1:
                processGroups = policy.getShortageStopProcBlockList();
                break;
            case 2:
                processGroups = policy.getAllowStopProcBlockList();
                break;
            default:
                AwareLog.w(TAG, "getAppMngProcGroup unknown group id!");
                break;
        }
        return processGroups;
    }

    public static int killProcessGroupForQuickKill(int uid, int pid) {
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            int killProcessGroupForQuickKill = ResourceCollector.killProcessGroupForQuickKill(uid, pid);
            return killProcessGroupForQuickKill;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    public static void writeSwappiness(int swappiness) {
        if (swappiness > 200 || swappiness < 0) {
            AwareLog.w(TAG, "invalid swappiness value");
            return;
        }
        AwareLog.i(TAG, "setSwappiness = " + swappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(302);
        buffer.putInt(swappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void writeDirectSwappiness(int directswappiness) {
        if (directswappiness > 200 || directswappiness < 0) {
            AwareLog.w(TAG, "invalid directswappiness value");
            return;
        }
        AwareLog.i(TAG, "setDirectSwappiness = " + directswappiness);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_DIRECT_SWAPPINESS);
        buffer.putInt(directswappiness);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    public static void writeExtraFreeKbytes(int extrafreekbytes) {
        if (extrafreekbytes <= 0 || extrafreekbytes >= MemoryConstant.MAX_EXTRA_FREE_KBYTES) {
            AwareLog.w(TAG, "invalid extrafreekbytes value");
            return;
        }
        int lastExtraFreeKbytes = SystemProperties.getInt("sys.sysctl.extra_free_kbytes", MemoryConstant.PROCESSLIST_EXTRA_FREE_KBYTES);
        if (lastExtraFreeKbytes == extrafreekbytes) {
            AwareLog.d(TAG, "extrafreekbytes is already " + lastExtraFreeKbytes + ", no need to set");
        } else {
            SystemProperties.set("sys.sysctl.extra_free_kbytes", Integer.toString(extrafreekbytes));
        }
    }

    private static void configProtectLru() {
        setProtectLruLimit(MemoryConstant.getConfigProtectLruLimit());
        setProtectLruRatio(MemoryConstant.getConfigProtectLruRatio());
        AwareLog.d(TAG, "onProtectLruConfigUpdate");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_CONFIG_UPDATE);
        buffer.putInt(0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
        setFileProtectLru(MemoryConstant.MSG_PROTECTLRU_SET_FILENODE);
    }

    public static void enableProtectLru() {
        setProtectLruSwitch(true);
    }

    public static void disableProtectLru() {
        setProtectLruSwitch(false);
    }

    public static void onProtectLruConfigUpdate() {
        disableProtectLru();
        configProtectLru();
        enableProtectLru();
    }

    public static void dynamicSetProtectLru(int state) {
        if (state == 1) {
            enableProtectLru();
        } else if (state == 0) {
            disableProtectLru();
        }
    }

    private static void setProtectLruRatio(int ratio) {
        AwareLog.d(TAG, "set ProtectLru ratio = " + ratio);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTRATIO);
        buffer.putInt(ratio);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }

    private static void setFileProtectLru(int commandType) {
        ArrayMap<Integer, ArraySet<String>> filterMap = MemoryConstant.getFileCacheMap();
        if (filterMap != null) {
            AwareLog.i(TAG, "set ProtectLru filterMap size:" + filterMap.size());
            ByteBuffer buffer = ByteBuffer.allocate(272);
            int i = 0;
            while (i < filterMap.size()) {
                int index = ((Integer) filterMap.keyAt(i)).intValue();
                int isDir = 0;
                if (index > 50) {
                    index -= 50;
                    isDir = 1;
                }
                ArraySet<String> filterSet = (ArraySet) filterMap.valueAt(i);
                if (filterSet != null) {
                    for (String filterStr : filterSet) {
                        if (!TextUtils.isEmpty(filterStr)) {
                            byte[] stringBytes = filterStr.getBytes("UTF-8");
                            if (stringBytes.length < 1 || stringBytes.length > 255) {
                                AwareLog.w(TAG, "setPackageProtectLru incorrect filterStr = " + filterStr);
                            } else {
                                try {
                                    AwareLog.d(TAG, "setPackageProtectLru filterStr = " + filterStr);
                                    buffer.clear();
                                    buffer.putInt(commandType);
                                    buffer.putInt(isDir);
                                    buffer.putInt(index);
                                    buffer.putInt(stringBytes.length);
                                    buffer.put(stringBytes);
                                    buffer.putChar('\u0000');
                                    if (sendPacket(buffer) != 0) {
                                        AwareLog.w(TAG, "setPackageProtectLru sendPacket failed");
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    AwareLog.w(TAG, "setPackageProtectLru UTF-8 not supported");
                                }
                            }
                        }
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private static void setProtectLruLimit(String lruConfigStr) {
        if (checkLimitConfigStr(lruConfigStr)) {
            ByteBuffer buffer = ByteBuffer.allocate(268);
            try {
                byte[] stringBytes = lruConfigStr.getBytes("UTF-8");
                if (stringBytes.length < 1 || stringBytes.length > 255) {
                    AwareLog.w(TAG, "setProtectLruLimit incorrect config = " + lruConfigStr);
                    return;
                }
                AwareLog.d(TAG, "setProtectLruLimit configstr=" + lruConfigStr);
                buffer.clear();
                buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SET_PROTECTZONE);
                buffer.putInt(stringBytes.length);
                buffer.put(stringBytes);
                buffer.putChar('\u0000');
                if (sendPacket(buffer) != 0) {
                    AwareLog.w(TAG, "setProtectLruLimit sendPacket failed");
                }
            } catch (UnsupportedEncodingException e) {
                AwareLog.w(TAG, "setProtectLruLimit UTF-8 not supported?!?");
            }
        }
    }

    private static boolean checkLimitConfigStr(String lruConfigStr) {
        if (lruConfigStr == null) {
            return false;
        }
        String[] lruConfigStrArray = lruConfigStr.split(" ");
        if (lruConfigStrArray.length != 3) {
            return false;
        }
        for (String parseInt : lruConfigStrArray) {
            int levelValue = Integer.parseInt(parseInt);
            if (levelValue < 0 || levelValue > 100) {
                AwareLog.w(TAG, "protect lru level value is invalid: " + levelValue);
                return false;
            }
        }
        return true;
    }

    private static int sendPacket(ByteBuffer buffer) {
        if (buffer == null) {
            return -1;
        }
        int retry = 2;
        while (!IAwaredConnection.getInstance().sendPacket(buffer.array(), 0, buffer.position())) {
            retry--;
            if (retry <= 0) {
                return -1;
            }
        }
        return 0;
    }

    private static void setProtectLruSwitch(boolean isEnable) {
        AwareLog.d(TAG, "set ProtectLru switch = " + isEnable);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(MemoryConstant.MSG_PROTECTLRU_SWITCH);
        buffer.putInt(isEnable ? 1 : 0);
        IAwaredConnection.getInstance().sendPacket(buffer.array());
    }
}
