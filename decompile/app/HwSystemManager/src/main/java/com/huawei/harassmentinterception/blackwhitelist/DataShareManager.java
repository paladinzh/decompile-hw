package com.huawei.harassmentinterception.blackwhitelist;

import com.huawei.harassmentinterception.common.CommonObject.ParcelableBlacklistItem;
import java.util.ArrayList;

public class DataShareManager {
    private static DataShareManager sManager = null;
    private ArrayList<ParcelableBlacklistItem> mBlacklistBuff = new ArrayList();
    private ArrayList<ParcelableBlacklistItem> mWhitelistBuff = new ArrayList();

    public static synchronized DataShareManager getInstance() {
        DataShareManager dataShareManager;
        synchronized (DataShareManager.class) {
            if (sManager == null) {
                sManager = new DataShareManager();
            }
            dataShareManager = sManager;
        }
        return dataShareManager;
    }

    public static synchronized void destory() {
        synchronized (DataShareManager.class) {
            if (sManager != null) {
                sManager.clearWhitelistBuff();
                sManager.clearBlacklistBuff();
                sManager = null;
            }
        }
    }

    public boolean isWhitelistBuffEmpty() {
        return this.mWhitelistBuff.isEmpty();
    }

    public boolean isBlacklistBuffEmpty() {
        return this.mBlacklistBuff.isEmpty();
    }

    public ArrayList<ParcelableBlacklistItem> getWhitelistBuff() {
        return this.mWhitelistBuff;
    }

    public void setWhitelistBuff(ArrayList<ParcelableBlacklistItem> buff) {
        this.mWhitelistBuff = buff;
    }

    public ArrayList<ParcelableBlacklistItem> getBlacklistBuff() {
        return this.mBlacklistBuff;
    }

    public void setBlacklistBuff(ArrayList<ParcelableBlacklistItem> buff) {
        this.mBlacklistBuff = buff;
    }

    public ArrayList<ParcelableBlacklistItem> copyWhitelistBuff() {
        return (ArrayList) this.mWhitelistBuff.clone();
    }

    public ArrayList<ParcelableBlacklistItem> copyBlacklistBuff() {
        return (ArrayList) this.mBlacklistBuff.clone();
    }

    public void clearBlacklistBuff() {
        this.mBlacklistBuff.clear();
    }

    public void clearWhitelistBuff() {
        this.mWhitelistBuff.clear();
    }
}
