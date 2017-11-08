package com.android.server.pfw.autostartup.datamgr;

import android.os.Environment;
import android.text.TextUtils;
import com.android.server.pfw.log.HwPFWLogger;
import huawei.android.pfw.HwPFWStartupSetting;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class HsmCacheData {
    private static final String ALLOW_NUM_STR = "1";
    private static final String FORBID_NUM_STR = "0";
    private static final String TAG = "HsmCacheData";
    private final AssistObj[] mAssistObj = new AssistObj[]{new AssistObj(new Object(), "startup_receiver_setting.list"), new AssistObj(new Object(), "startup_service_provider_setting.list")};
    private File mPfwDir = new File(new File(Environment.getDataDirectory(), "system"), "pfw");
    private Map<String, Boolean> mReceiverSettings = new HashMap();
    private Map<String, Boolean> mServiceProviderSettings = new HashMap();
    private final Object mSettingLock = new Object();
    private final AssistObj mWidgetAssist = new AssistObj(new Object(), "startup_cached_widget.list");
    private List<String> mWidgetPkgs = new ArrayList();

    private static class AssistObj {
        final Object mFileLock;
        final String mFileName;

        AssistObj(Object lock, String fileName) {
            this.mFileLock = lock;
            this.mFileName = fileName;
        }
    }

    public HsmCacheData() {
        if (!this.mPfwDir.mkdirs()) {
            HwPFWLogger.w(TAG, "Fail to make dir " + this.mPfwDir.getPath());
        }
    }

    public void loadCache() {
        loadDiskFileSettingLocked(0, this.mReceiverSettings);
        loadDiskFileSettingLocked(1, this.mServiceProviderSettings);
    }

    public void loadWidgetCache() {
        loadDiskWidgetLocked(this.mWidgetPkgs);
    }

    public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) {
        if (clearFirst) {
            clearCacheLocked();
        }
        for (HwPFWStartupSetting setting : settings) {
            if (setting.valid()) {
                updateSingleSettingLocked(setting);
            }
        }
    }

    public void removeStartupSetting(String pkgName) {
        removeSingleSettingLocked(pkgName);
    }

    public int retrieveStartupSetting(String pkgName, int type) {
        if (type == 0) {
            return retrieveSingleSettingLocked(this.mReceiverSettings, pkgName);
        }
        if (1 == type) {
            return retrieveSingleSettingLocked(this.mServiceProviderSettings, pkgName);
        }
        return 2;
    }

    public void updateWidgetPkgList(List<String> pkgSet) {
        updateWidgetPkgListLocked(pkgSet);
    }

    public boolean isWidgetExistPkg(String pkgName) {
        return isWidgetExistPkgLocked(pkgName);
    }

    public void flushCacheDataToDisk() {
        HwPFWLogger.d(TAG, "flushCacheDataToDisk");
        writeDiskFileSettingLocked(0, this.mReceiverSettings);
        writeDiskFileSettingLocked(1, this.mServiceProviderSettings);
        writeDiskWidgetLocked(this.mWidgetPkgs);
    }

    private void clearCacheLocked() {
        synchronized (this.mSettingLock) {
            this.mReceiverSettings.clear();
            this.mServiceProviderSettings.clear();
        }
    }

    private void updateSingleSettingLocked(HwPFWStartupSetting setting) {
        synchronized (this.mSettingLock) {
            switch (setting.getTypeValue()) {
                case 0:
                    this.mReceiverSettings.put(setting.getPackageName(), Boolean.valueOf(getAllowValueEx(setting.getAllowValue())));
                    break;
                case 1:
                    this.mServiceProviderSettings.put(setting.getPackageName(), Boolean.valueOf(getAllowValueEx(setting.getAllowValue())));
                    break;
                default:
                    HwPFWLogger.w(TAG, "updateSingleSettingLocked un-support type value, ignore it.");
                    break;
            }
        }
    }

    private void removeSingleSettingLocked(String pkgName) {
        synchronized (this.mSettingLock) {
            this.mReceiverSettings.remove(pkgName);
            this.mServiceProviderSettings.remove(pkgName);
        }
    }

    private void updateWidgetPkgListLocked(List<String> pkgList) {
        synchronized (this.mSettingLock) {
            this.mWidgetPkgs.clear();
            this.mWidgetPkgs.addAll(pkgList);
        }
    }

    private boolean isWidgetExistPkgLocked(String pkgName) {
        boolean contains;
        synchronized (this.mSettingLock) {
            contains = this.mWidgetPkgs.contains(pkgName);
        }
        return contains;
    }

    private boolean getAllowValueEx(int value) {
        return 1 == value;
    }

    private int retrieveSingleSettingLocked(Map<String, Boolean> typeSettings, String pkgName) {
        int i;
        synchronized (this.mSettingLock) {
            Boolean bObj = (Boolean) typeSettings.get(pkgName);
            if (bObj == null) {
                i = 2;
            } else if (bObj.booleanValue()) {
                i = 1;
            } else {
                i = 0;
            }
        }
        return i;
    }

    private void loadDiskFileSettingLocked(int type, Map<String, Boolean> cache) {
        synchronized (this.mAssistObj[type].mFileLock) {
            List<String> lines = new CacheFileRW(new File(this.mPfwDir, this.mAssistObj[type].mFileName)).readFileLines();
        }
        synchronized (this.mSettingLock) {
            for (String line : lines) {
                fromStringLine(line, cache);
            }
        }
    }

    private void loadDiskWidgetLocked(List<String> pkgList) {
        synchronized (this.mWidgetAssist.mFileLock) {
            List<String> lines = new CacheFileRW(new File(this.mPfwDir, this.mWidgetAssist.mFileName)).readFileLines();
        }
        synchronized (this.mSettingLock) {
            pkgList.addAll(lines);
        }
    }

    private void writeDiskFileSettingLocked(int type, Map<String, Boolean> cache) {
        List<String> result = new ArrayList();
        synchronized (this.mSettingLock) {
            for (Entry<String, Boolean> entry : cache.entrySet()) {
                result.add(toStringLine(entry));
            }
            HwPFWLogger.d(TAG, "writeDiskFileSettingLocked type: " + type + ", writeToFile " + result);
        }
        synchronized (this.mAssistObj[type].mFileLock) {
            new CacheFileRW(new File(this.mPfwDir, this.mAssistObj[type].mFileName)).writeFileLines(result);
        }
    }

    private void writeDiskWidgetLocked(List<String> pkgList) {
        synchronized (this.mWidgetAssist.mFileLock) {
            new CacheFileRW(new File(this.mPfwDir, this.mWidgetAssist.mFileName)).writeFileLines(pkgList);
        }
    }

    private void fromStringLine(String line, Map<String, Boolean> cache) {
        if (!TextUtils.isEmpty(line)) {
            int lastIndex_ = line.lastIndexOf("_");
            if (lastIndex_ < 0 || lastIndex_ != line.length() - 2) {
                HwPFWLogger.w(TAG, "fromStringLine invalid record line: " + line);
            } else {
                cache.put(line.substring(0, lastIndex_), Boolean.valueOf("1".equals(line.substring(lastIndex_ + 1))));
            }
        }
    }

    private String toStringLine(Entry<String, Boolean> entry) {
        return ((String) entry.getKey()) + "_" + (((Boolean) entry.getValue()).booleanValue() ? "1" : "0");
    }

    public String toString() {
        return "\nHsmCacheData ======================\n==================Receiver: " + this.mReceiverSettings + "\n==================ServiceProvider: " + this.mServiceProviderSettings;
    }
}
