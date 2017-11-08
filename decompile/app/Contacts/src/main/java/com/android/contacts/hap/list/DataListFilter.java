package com.android.contacts.hap.list;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public final class DataListFilter implements Comparable<DataListFilter>, Parcelable {
    public static final Creator<DataListFilter> CREATOR = new Creator<DataListFilter>() {
        public DataListFilter createFromParcel(Parcel source) {
            return new DataListFilter(source.readInt(), source.readString(), source.readString(), source.readString(), null, source.readLong(), source.readString(), source.readInt() != 0, null);
        }

        public DataListFilter[] newArray(int size) {
            return new DataListFilter[size];
        }
    };
    public final String accountName;
    public final String accountType;
    public final String dataSet;
    public final int filterType;
    public long groupId;
    public final boolean groupReadOnly;
    public String groupSourceId;
    public final Drawable icon;
    public final String title;

    public DataListFilter(int filterType, String accountType, String accountName, String dataSet, Drawable icon, long groupId, String groupSourceId, boolean groupReadOnly, String title) {
        this.filterType = filterType;
        this.accountType = accountType;
        this.accountName = accountName;
        this.dataSet = dataSet;
        this.icon = icon;
        this.groupId = groupId;
        this.groupSourceId = groupSourceId;
        this.groupReadOnly = groupReadOnly;
        this.title = title;
    }

    public String toString() {
        switch (this.filterType) {
            case -9:
                return "no-msgplus-rcse";
            case -8:
                return "mail-no-msgplus-rcse";
            case -7:
                return "rcse";
            case -6:
                return "messageplus";
            case -5:
                return "message";
            case -4:
                return "mail";
            case -3:
                return "number";
            case -2:
                return "group_mail";
            case -1:
                return "group_message";
            default:
                return super.toString();
        }
    }

    public int compareTo(DataListFilter another) {
        int res;
        if (this.accountName == null || another.accountName == null) {
            res = (this.accountName == null ? "" : this.accountName).compareTo(another.accountName == null ? "" : another.accountName);
        } else {
            res = this.accountName.compareTo(another.accountName);
        }
        if (res != 0) {
            return res;
        }
        if (this.accountType == null || another.accountType == null) {
            res = (this.accountType == null ? "" : this.accountType).compareTo(another.accountType == null ? "" : another.accountType);
        } else {
            res = this.accountType.compareTo(another.accountType);
        }
        if (res != 0) {
            return res;
        }
        if (this.filterType != another.filterType) {
            return this.filterType - another.filterType;
        }
        return (this.title != null ? this.title : "").compareTo(another.title != null ? another.title : "");
    }

    public int hashCode() {
        int code = this.filterType;
        if (!(this.accountType == null || this.accountName == null)) {
            code = (((code * 31) + this.accountType.hashCode()) * 31) + this.accountName.hashCode();
        }
        if (this.dataSet != null) {
            code = (code * 31) + this.dataSet.hashCode();
        }
        if (this.groupSourceId != null) {
            return (code * 31) + this.groupSourceId.hashCode();
        }
        if (this.groupId != 0) {
            return (code * 31) + Long.valueOf(this.groupId).intValue();
        }
        return code;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (this == other) {
            return true;
        }
        if (!(other instanceof DataListFilter)) {
            return false;
        }
        DataListFilter otherFilter = (DataListFilter) other;
        if (this.filterType != otherFilter.filterType || !TextUtils.equals(this.accountName, otherFilter.accountName) || !TextUtils.equals(this.accountType, otherFilter.accountType) || !TextUtils.equals(this.dataSet, otherFilter.dataSet)) {
            return false;
        }
        if (this.groupSourceId != null && otherFilter.groupSourceId != null) {
            return this.groupSourceId.equals(otherFilter.groupSourceId);
        }
        if (this.groupId != otherFilter.groupId) {
            z = false;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeInt(this.filterType);
        dest.writeString(this.accountName);
        dest.writeString(this.accountType);
        dest.writeString(this.dataSet);
        dest.writeLong(this.groupId);
        dest.writeString(this.groupSourceId);
        if (this.groupReadOnly) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    public int describeContents() {
        return 0;
    }
}
