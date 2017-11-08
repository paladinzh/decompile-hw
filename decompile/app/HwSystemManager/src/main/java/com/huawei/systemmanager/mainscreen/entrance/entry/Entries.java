package com.huawei.systemmanager.mainscreen.entrance.entry;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import java.util.List;

public class Entries {
    public static List<AbsEntrance> getEntries() {
        return AbroadUtils.isAbroad() ? getEntriesAbroad() : getEntriesNormal();
    }

    private static List<AbsEntrance> getEntriesNormal() {
        if (Utility.isWifiOnlyMode()) {
            return getWifiOnlyEntriesNormal();
        }
        if (Utility.isDataOnlyMode()) {
            return getDataOnleyEntryNormal();
        }
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryNetwork());
        entryList.add(new EntryHarassment());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryPermission());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryAppLocker());
        entryList.add(new EntryProtect());
        return entryList;
    }

    private static List<AbsEntrance> getEntriesAbroad() {
        if (Utility.isWifiOnlyMode()) {
            return getWifiOnlyEntryAbroad();
        }
        if (Utility.isDataOnlyMode()) {
            return getDataOnlyEntryAbroad();
        }
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryNetwork());
        entryList.add(new EntryHarassment());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryPermission());
        entryList.add(new EntryAddView());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryAppLocker());
        entryList.add(new EntryProtect());
        return entryList;
    }

    private static List<AbsEntrance> getWifiOnlyEntriesNormal() {
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryPermission());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryAddView());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryAppLocker());
        return entryList;
    }

    private static List<AbsEntrance> getWifiOnlyEntryAbroad() {
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryAddView());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryAppLocker());
        entryList.add(new EntryPermission());
        entryList.add(new EntryStartupMgr());
        return entryList;
    }

    private static List<AbsEntrance> getDataOnleyEntryNormal() {
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryNetwork());
        entryList.add(new EntryPermission());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryAddView());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryAppLocker());
        return entryList;
    }

    private static List<AbsEntrance> getDataOnlyEntryAbroad() {
        List<AbsEntrance> entryList = Lists.newArrayList();
        entryList.add(new EntryStorageClean());
        entryList.add(new EntryPowerMgr());
        entryList.add(new EntryNetwork());
        entryList.add(new EntryVirusScan());
        entryList.add(new EntryAddView());
        entryList.add(new EntryAppLocker());
        entryList.add(new EntryPermission());
        entryList.add(new EntryStartupMgr());
        entryList.add(new EntryStartupMgr());
        return entryList;
    }
}
