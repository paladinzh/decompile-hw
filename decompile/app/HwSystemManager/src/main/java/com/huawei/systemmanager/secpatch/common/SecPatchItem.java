package com.huawei.systemmanager.secpatch.common;

import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult.SecPatchDetail;
import java.io.Serializable;
import java.util.Comparator;

public class SecPatchItem implements Comparator<SecPatchItem>, Serializable {
    private static final long serialVersionUID = 4489320257776749239L;
    public String mDigest;
    public String mDigest_en;
    public String mFixed_version;
    public String mOcid;
    public String mPver = null;
    public String mSid = "";
    public String mSrc;

    public void copyfromSecPatchDetail(SecPatchDetail secPatch) {
        this.mSid = secPatch.mSid;
        this.mOcid = secPatch.mOcid;
        this.mSrc = secPatch.mSrc;
        this.mDigest = secPatch.mDigest;
        this.mDigest_en = secPatch.mDigest_en;
        this.mFixed_version = secPatch.mFixed_version;
    }

    public void parseFrom(Cursor cursor) {
        if (cursor != null) {
            this.mPver = cursor.getString(cursor.getColumnIndex("pver"));
            this.mSid = cursor.getString(cursor.getColumnIndex("sid"));
            this.mOcid = cursor.getString(cursor.getColumnIndex("ocid"));
            this.mSrc = cursor.getString(cursor.getColumnIndex("src"));
            this.mDigest = cursor.getString(cursor.getColumnIndex("digest"));
            this.mDigest_en = cursor.getString(cursor.getColumnIndex("digest_en"));
        }
    }

    public ContentValues getAsContentValues() {
        ContentValues values = new ContentValues();
        values.put("pver", this.mPver);
        values.put("sid", this.mSid);
        values.put("ocid", this.mOcid);
        values.put("src", this.mSrc);
        values.put("digest", this.mDigest);
        values.put("digest_en", this.mDigest_en);
        values.put(ConstValues.COL_FIXED_VERSION, this.mFixed_version);
        return values;
    }

    public ContentValues getAsContentValues(String updateStatus) {
        ContentValues values = getAsContentValues();
        values.put(ConstValues.COL_UPDATED, updateStatus);
        return values;
    }

    public boolean isSameVersion(String version) {
        return this.mPver.equalsIgnoreCase(version);
    }

    public void updatePverToFixversion() {
        this.mPver = this.mFixed_version;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("mPver is [").append(this.mPver).append("] ");
        buf.append("mSid is [").append(this.mSid).append("] ");
        buf.append("mOcid is [").append(this.mOcid).append("] ");
        buf.append("mSrc is [").append(this.mSrc).append("] ");
        buf.append("mDigest is [").append(this.mDigest).append("] ");
        buf.append("mDigest_en is [").append(this.mDigest_en).append("] ");
        buf.append("mFixed_version is [").append(this.mFixed_version).append("] ");
        buf.append("} ");
        return buf.toString();
    }

    public int compare(SecPatchItem item1, SecPatchItem item2) {
        if (item1.mPver == null || item2.mPver == null) {
            return 0;
        }
        if (item1.mPver.equalsIgnoreCase(item2.mPver)) {
            return item2.mSid.compareToIgnoreCase(item1.mSid);
        }
        return item2.mPver.compareToIgnoreCase(item1.mPver);
    }
}
