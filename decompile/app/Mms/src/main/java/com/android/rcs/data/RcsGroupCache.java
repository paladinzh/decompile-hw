package com.android.rcs.data;

import android.util.LruCache;
import com.autonavi.amap.mapcore.VTMCDataCache;

public class RcsGroupCache {
    private static RcsGroupCache mRcsGroupCache = new RcsGroupCache();
    private LruCache<String, RcsGroupData> mGroupData;

    public static class RcsGroupData {
        private String mAddress;
        private int mNotifySilent;
        private String[] mRcsIds;
        private int mStatus;
        private String mSubject;
        private long mThreadId;

        public void setSubject(String subject) {
            this.mSubject = subject;
        }

        public String getSubject() {
            return this.mSubject;
        }

        public void setStatus(int status) {
            this.mStatus = status;
        }

        public int getStatus() {
            return this.mStatus;
        }

        public void setGroupThreadId(long threadId) {
            this.mThreadId = threadId;
        }

        public void setRcsIds(String[] rcsIds) {
            if (rcsIds != null) {
                this.mRcsIds = (String[]) rcsIds.clone();
            }
        }

        public String[] getRcsIds() {
            if (this.mRcsIds != null) {
                return (String[]) this.mRcsIds.clone();
            }
            return null;
        }

        public void setAddress(String address) {
            this.mAddress = address;
        }

        public String getAddress() {
            return this.mAddress;
        }

        public void setNotifySilent(int notifySilent) {
            this.mNotifySilent = notifySilent;
        }

        public int getNotifySilent() {
            return this.mNotifySilent;
        }
    }

    public static RcsGroupCache getInstance() {
        return mRcsGroupCache;
    }

    private RcsGroupCache() {
        this.mGroupData = null;
        this.mGroupData = new LruCache(VTMCDataCache.MAXSIZE);
    }

    public RcsGroupData getGroupData(String recipientIds) {
        if (recipientIds != null) {
            return (RcsGroupData) this.mGroupData.get(recipientIds);
        }
        return null;
    }

    public void putGroupData(String recipientIds, RcsGroupData groupData) {
        if (recipientIds != null && groupData != null) {
            this.mGroupData.put(recipientIds, groupData);
        }
    }

    public void clear() {
        this.mGroupData.evictAll();
    }
}
