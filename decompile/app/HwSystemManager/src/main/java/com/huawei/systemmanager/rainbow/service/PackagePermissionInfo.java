package com.huawei.systemmanager.rainbow.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.harassmentinterception.common.ConstValues;
import java.util.Map;
import java.util.Map.Entry;

public class PackagePermissionInfo implements Parcelable {
    public static final Creator<PackagePermissionInfo> CREATOR = new Creator<PackagePermissionInfo>() {
        public PackagePermissionInfo createFromParcel(Parcel source) {
            return new PackagePermissionInfo(source);
        }

        public PackagePermissionInfo[] newArray(int size) {
            return new PackagePermissionInfo[size];
        }
    };
    public int authenticationInfo;
    public String packageName;
    public Map<String, Integer> permission;
    public int versionCode;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(this.versionCode);
        dest.writeString(this.packageName);
        dest.writeInt(this.authenticationInfo);
        dest.writeMap(this.permission);
    }

    private PackagePermissionInfo(Parcel source) {
        this.versionCode = source.readInt();
        this.packageName = source.readString();
        this.authenticationInfo = source.readInt();
        this.permission = source.readHashMap(null);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("PackagePermissionInfo [versionCode=" + this.versionCode + ", packageName=" + this.packageName + ", authenticationInfo=" + this.authenticationInfo + ", permission=");
        if (this.permission != null) {
            for (Entry<String, Integer> entry : this.permission.entrySet()) {
                buf.append("(");
                buf.append((String) entry.getKey());
                buf.append(ConstValues.SEPARATOR_KEYWORDS_EN);
                buf.append(entry.getValue());
                buf.append(") ");
            }
        }
        buf.append("]");
        return buf.toString();
    }
}
