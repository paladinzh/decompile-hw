package com.android.server.rms.iaware.memory.utils;

import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;

public final class MemoryConstant {
    public static final long APP_AVG_USS = 20480;
    public static final int AWARE_INVAILD_KILL_NUM = 3;
    public static final int AWARE_INVAILD_KILL_THRESHOLD = 5;
    private static final String CONFIG_PROTECT_LRU_DEFAULT = "0 0 0";
    private static long CPU_IDLE_THRESHOLD = 30;
    private static long CPU_NORMAL_THRESHOLD = 60;
    private static long CRITICAL_MEMORY = 614400;
    public static final int DEFAULT_DIRECT_SWAPPINESS = 60;
    public static final int DEFAULT_EXTRA_FREE_KBYTES = SystemProperties.getInt("sys.sysctl.extra_free_kbytes", PROCESSLIST_EXTRA_FREE_KBYTES);
    public static final int DEFAULT_SWAPPINESS = 60;
    public static final int DIRCONSTANT = 50;
    private static long EMERGENCY_MEMORY = 307200;
    private static long IDLE_MEMORY = MB_SIZE;
    public static final int MAX_APPNAME_LEN = 64;
    public static final int MAX_EXTRA_FREE_KBYTES = 200000;
    public static final long MB_SIZE = 1048576;
    public static final String MEM_CONSTANT_AVERAGEAPPUSSNAME = "averageAppUss";
    public static final String MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME = "bigMemCriticalMemory";
    public static final String MEM_CONSTANT_CONFIGNAME = "MemoryConstant";
    public static final String MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME = "defaultCriticalMemory";
    public static final String MEM_CONSTANT_DEFAULTTIMERPERIOD = "defaultTimerPeriod";
    public static final String MEM_CONSTANT_DIRECTSWAPPINESSNAME = "direct_swappiness";
    public static final String MEM_CONSTANT_EMERGEMCYMEMORYNAME = "emergencyMemory";
    public static final String MEM_CONSTANT_EXTRAFREEKBYTESNAME = "extra_free_kbytes";
    public static final String MEM_CONSTANT_HIGHCPULOADNAME = "highCpuLoad";
    public static final String MEM_CONSTANT_LOWCPULOADNAME = "lowCpuLoad";
    public static final String MEM_CONSTANT_MAXTIMERPERIOD = "maxTimerPeriod";
    public static final String MEM_CONSTANT_MINTIMERPERIOD = "minTimerPeriod";
    public static final String MEM_CONSTANT_NORMALMEMORYNAME = "normalMemory";
    public static final String MEM_CONSTANT_NUMTIMERPERIOD = "numTimerPeriod";
    public static final String MEM_CONSTANT_PROCESSLIMIT = "numProcessLimit";
    public static final String MEM_CONSTANT_PROTECTLRULIMIT = "protect_lru_limit";
    public static final String MEM_CONSTANT_PROTECTRATIO = "protect_lru_ratio";
    public static final String MEM_CONSTANT_RAMSIZENAME = "ramsize";
    public static final String MEM_CONSTANT_RESERVEDZRAMNAME = "reservedZram";
    public static final String MEM_CONSTANT_SWAPPINESSNAME = "swappiness";
    public static final String MEM_FILECACHE_ITEM_LEVEL = "level";
    public static final String MEM_FILECACHE_ITEM_NAME = "name";
    public static final String MEM_POLICY_ACTIONNAME = "name";
    public static final String MEM_POLICY_BIGAPPNAME = "appname";
    public static final String MEM_POLICY_BIGMEMAPP = "BigMemApp";
    public static final String MEM_POLICY_CONFIGNAME = "Memoryitem";
    public static final String MEM_POLICY_FEATURENAME = "Memory";
    public static final String MEM_POLICY_FILECACHE = "FileCache";
    public static final String MEM_POLICY_KILLACTION = "kill";
    public static final String MEM_POLICY_QUICKKILLACTION = "quickkill";
    public static final String MEM_POLICY_RECLAIM = "reclaim";
    public static final String MEM_POLICY_SCENE = "scene";
    public static final String MEM_SCENE_BIGMEM = "BigMem";
    public static final String MEM_SCENE_DEFAULT = "default";
    public static final String MEM_SCENE_IDLE = "idle";
    public static final String MEM_SCENE_LAUNCH = "launch";
    public static final long MIN_INTERVAL_OP_TIMEOUT = 10000;
    public static final int MSG_BOOST_SIGKILL_SWITCH = 301;
    public static final int MSG_DIRECT_SWAPPINESS = 303;
    public static final int MSG_MEM_BASE_VALUE = 300;
    public static final int MSG_PROTECTLRU_CONFIG_UPDATE = 308;
    public static final int MSG_PROTECTLRU_SET_FILENODE = 304;
    public static final int MSG_PROTECTLRU_SET_PROTECTRATIO = 307;
    public static final int MSG_PROTECTLRU_SET_PROTECTZONE = 305;
    public static final int MSG_PROTECTLRU_SWITCH = 306;
    public static final int MSG_SWAPPINESS = 302;
    public static final int PROCESSLIST_EXTRA_FREE_KBYTES = 24300;
    public static final int PROTECTLRU_ERROR_LEVEL = -1;
    public static final int PROTECTLRU_FIRST_LEVEL = 1;
    public static final int PROTECTLRU_MAX_LEVEL = 3;
    public static final int PROTECTLRU_STATE_PROTECT = 1;
    public static final int PROTECTLRU_STATE_UNPROTECT = 0;
    public static final long RECLAIM_KILL_GAP_MEMORY = 51200;
    public static final int REPEAT_RECLAIM_TIME_GAP = 600000;
    public static final int RESULT_ACTIVE = 1;
    public static final int RESULT_CONTINUE = 3;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_INACTIVE = 2;
    public static final int RESULT_OK = 0;
    private static long bigMemoryAppLimit = 614400;
    private static int configDirectSwappiness = -1;
    private static int configExtraFreeKbytes = -1;
    private static String configProtectLruLimit = CONFIG_PROTECT_LRU_DEFAULT;
    private static int configProtectLruRatio = 50;
    private static int configSwappiness = -1;
    private static long defaultMemoryLimit = 614400;
    private static long defaultPeriod = 2000;
    private static ArrayMap<Integer, ArraySet<String>> mFileCacheMap = new ArrayMap();
    private static long maxPeriod = AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
    private static long maxReqMem = 921600;
    private static long minPeriod = 500;
    private static int numPeriod = 3;
    private static long reservedZramMemory = 102400;

    public enum MemActionType {
        ACTION_KILL,
        ACTION_COMPRESS,
        ACTION_RECLAIM
    }

    public enum MemLevel {
        MEM_LOW,
        MEM_CRITICAL
    }

    public static final long getIdleThresHold() {
        return CPU_IDLE_THRESHOLD;
    }

    public static final void setIdleThresHold(long idleLoad) {
        CPU_IDLE_THRESHOLD = idleLoad;
    }

    public static final long getNormalThresHold() {
        return CPU_NORMAL_THRESHOLD;
    }

    public static final void setNormalThresHold(long normalLoad) {
        CPU_NORMAL_THRESHOLD = normalLoad;
    }

    public static final long getReservedZramSpace() {
        return reservedZramMemory;
    }

    public static final void setReservedZramSpace(long reserved) {
        reservedZramMemory = reserved;
    }

    public static final long getIdleMemory() {
        return IDLE_MEMORY;
    }

    public static final void setIdleMemory(long idleMemory) {
        IDLE_MEMORY = idleMemory;
    }

    public static final long getEmergencyMemory() {
        return EMERGENCY_MEMORY;
    }

    public static final void setEmergencyMemory(long emergemcyMemory) {
        EMERGENCY_MEMORY = emergemcyMemory;
    }

    public static final long getCriticalMemory() {
        return CRITICAL_MEMORY;
    }

    public static final void setDefaultCriticalMemory(long criticalMemory) {
        CRITICAL_MEMORY = criticalMemory;
        defaultMemoryLimit = criticalMemory;
    }

    public static final void enableBigMemCriticalMemory() {
        CRITICAL_MEMORY = bigMemoryAppLimit;
    }

    public static final void disableBigMemCriticalMemory() {
        CRITICAL_MEMORY = defaultMemoryLimit;
    }

    public static final void setBigMemoryAppCriticalMemory(long bigMemLimit) {
        bigMemoryAppLimit = bigMemLimit;
    }

    public static final void setMaxTimerPeriod(long maxTimerPeriod) {
        maxPeriod = maxTimerPeriod;
    }

    public static final long getMaxTimerPeriod() {
        return maxPeriod;
    }

    public static final void setMinTimerPeriod(long minTimerPeriod) {
        minPeriod = minTimerPeriod;
    }

    public static final long getMinTimerPeriod() {
        return minPeriod;
    }

    public static final void setDefaultTimerPeriod(long defaultTimerPeriod) {
        defaultPeriod = defaultTimerPeriod;
    }

    public static final long getDefaultTimerPeriod() {
        return defaultPeriod;
    }

    public static final void setNumTimerPeriod(int numTimerPeriod) {
        numPeriod = numTimerPeriod;
    }

    public static final int getNumTimerPeriod() {
        return numPeriod;
    }

    public static final long getMiddleWater() {
        return (EMERGENCY_MEMORY + CRITICAL_MEMORY) / 2;
    }

    public static final long getMaxReqMem() {
        return maxReqMem;
    }

    public static final void setFileCacheMap(ArrayMap<Integer, ArraySet<String>> fileCacheMap) {
        mFileCacheMap = fileCacheMap;
    }

    public static final ArrayMap<Integer, ArraySet<String>> getFileCacheMap() {
        return mFileCacheMap;
    }

    public static final int getConfigExtraFreeKbytes() {
        return configExtraFreeKbytes;
    }

    public static final void setConfigExtraFreeKbytes(int extraFreeKbytes) {
        configExtraFreeKbytes = extraFreeKbytes;
    }

    public static final int getConfigSwappiness() {
        return configSwappiness;
    }

    public static final void setConfigSwappiness(int swappiness) {
        configSwappiness = swappiness;
    }

    public static final int getConfigDirectSwappiness() {
        return configDirectSwappiness;
    }

    public static final void setConfigDirectSwappiness(int directswappiness) {
        configDirectSwappiness = directswappiness;
    }

    public static final String getConfigProtectLruLimit() {
        return configProtectLruLimit;
    }

    public static final void setConfigProtectLruLimit(String protectLruLimit) {
        configProtectLruLimit = protectLruLimit;
    }

    public static final String getConfigProtectLruDefault() {
        return CONFIG_PROTECT_LRU_DEFAULT;
    }

    public static final void setConfigProtectLruRatio(int ratio) {
        configProtectLruRatio = ratio;
    }

    public static final int getConfigProtectLruRatio() {
        return configProtectLruRatio;
    }
}
