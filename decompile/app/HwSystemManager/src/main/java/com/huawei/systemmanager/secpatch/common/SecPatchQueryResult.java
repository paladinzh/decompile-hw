package com.huawei.systemmanager.secpatch.common;

import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class SecPatchQueryResult {
    private static final String TAG = "SecPatchQueryResult";
    public String mInfo = null;
    public List<SecPatchVerInfo> mSecPatchVerList = null;
    public int mSrvCode = -1;

    public static class SecPatchDetail {
        public String mDigest;
        public String mDigest_en;
        public String mFixed_version;
        public String mOcid;
        public String mSid;
        public String mSrc;

        public SecPatchDetail(String sid, String ocid, String src, String digest, String digest_en, String fix_version) {
            this.mSid = sid;
            this.mOcid = ocid;
            this.mSrc = src;
            this.mDigest = digest;
            this.mDigest_en = digest_en;
            this.mFixed_version = fix_version;
        }
    }

    public static class SecPatchVerInfo {
        public String mPver = null;
        public List<SecPatchDetail> mSecPatchList;

        public SecPatchVerInfo(String pver, List<SecPatchDetail> secPatchList) {
            this.mPver = pver;
            this.mSecPatchList = secPatchList;
        }
    }

    public SecPatchQueryResult(int srvCode) {
        this.mSrvCode = srvCode;
    }

    public SecPatchQueryResult(int srvCode, String info) {
        this.mSrvCode = srvCode;
        this.mInfo = info;
    }

    public SecPatchQueryResult(int srvCode, String info, List<SecPatchVerInfo> secPatchVerList) {
        this.mSrvCode = srvCode;
        this.mInfo = info;
        this.mSecPatchVerList = secPatchVerList;
    }

    public boolean isValid() {
        return this.mSecPatchVerList != null;
    }

    public boolean isEmpty() {
        if (isValid()) {
            return this.mSecPatchVerList.isEmpty();
        }
        return true;
    }

    public boolean isPatchToBeFixed() {
        return 211 == this.mSrvCode;
    }

    public boolean isPatchAll() {
        return 210 == this.mSrvCode;
    }

    public List<SecPatchItem> getSecPatchList() {
        if (isValid()) {
            List<SecPatchItem> secPatchList = new ArrayList();
            if (isEmpty()) {
                HwLog.e(TAG, "getSecPatchList: Empty result");
                return secPatchList;
            }
            for (SecPatchVerInfo verInfo : this.mSecPatchVerList) {
                String pver = verInfo.mPver;
                if (verInfo.mSecPatchList.isEmpty()) {
                    HwLog.w(TAG, "getSecPatchList: No sec patch in " + pver);
                } else {
                    for (SecPatchDetail patchDetail : verInfo.mSecPatchList) {
                        SecPatchItem item = new SecPatchItem();
                        item.mPver = pver;
                        item.copyfromSecPatchDetail(patchDetail);
                        secPatchList.add(item);
                    }
                }
            }
            return secPatchList;
        }
        HwLog.e(TAG, "getSecPatchList: Invalid result");
        return null;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("mInfo is [").append(this.mInfo).append("] ");
        buf.append("mSrvCode is [").append(this.mSrvCode).append("] ");
        buf.append("} ");
        return buf.toString();
    }
}
