package com.huawei.netassistant.common;

import android.net.NetworkStats.Entry;
import android.net.NetworkTemplate;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseBooleanArray;

public class ParcelableAppItem implements Comparable<ParcelableAppItem>, Parcelable {
    public static final Creator<ParcelableAppItem> CREATOR = new Creator<ParcelableAppItem>() {
        public ParcelableAppItem createFromParcel(Parcel in) {
            return new ParcelableAppItem(in);
        }

        public ParcelableAppItem[] newArray(int size) {
            return new ParcelableAppItem[size];
        }
    };
    public int appType = 0;
    public long backgroundbytes;
    public long foregroundbytes;
    public final int key;
    public long mobiletotal;
    public SparseBooleanArray uids = new SparseBooleanArray();
    public long wifitotal;

    public ParcelableAppItem(int key) {
        this.key = key;
    }

    public ParcelableAppItem(Parcel parcel) {
        this.key = parcel.readInt();
        this.uids = parcel.readSparseBooleanArray();
        this.mobiletotal = parcel.readLong();
        this.wifitotal = parcel.readLong();
        this.backgroundbytes = parcel.readLong();
        this.foregroundbytes = parcel.readLong();
        this.appType = parcel.readInt();
    }

    public void addUid(int uid) {
        this.uids.put(uid, true);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.key);
        dest.writeSparseBooleanArray(this.uids);
        dest.writeLong(this.mobiletotal);
        dest.writeLong(this.wifitotal);
        dest.writeLong(this.backgroundbytes);
        dest.writeLong(this.foregroundbytes);
        dest.writeInt(this.appType);
    }

    public int describeContents() {
        return 0;
    }

    public int compareTo(ParcelableAppItem another) {
        if (another.mobiletotal != this.mobiletotal) {
            return Long.compare(another.mobiletotal, this.mobiletotal);
        }
        return Long.compare(another.wifitotal, this.wifitotal);
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public void setTrafficData(int uid, Entry entry, NetworkTemplate template) {
        addUid(uid);
        if (entry.set == 0) {
            this.backgroundbytes += entry.rxBytes + entry.txBytes;
        } else if (entry.set == 1) {
            this.foregroundbytes += entry.rxBytes + entry.txBytes;
        }
        if (template.getMatchRule() == 7) {
            this.wifitotal += entry.rxBytes + entry.txBytes;
        } else {
            this.mobiletotal += entry.rxBytes + entry.txBytes;
        }
    }

    public void setTrafficWithNoKey(int collapseKey, Entry entry, NetworkTemplate template) {
        addUid(collapseKey);
        if (entry.set == 0) {
            this.backgroundbytes += entry.rxBytes + entry.txBytes;
        } else if (entry.set == 1) {
            this.foregroundbytes += entry.rxBytes + entry.txBytes;
        }
        if (template.getMatchRule() == 7) {
            this.wifitotal += entry.rxBytes + entry.txBytes;
        } else {
            this.mobiletotal += entry.rxBytes + entry.txBytes;
        }
    }
}
