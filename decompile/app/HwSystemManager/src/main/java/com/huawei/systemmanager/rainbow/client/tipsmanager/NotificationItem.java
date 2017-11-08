package com.huawei.systemmanager.rainbow.client.tipsmanager;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NotificationItem implements Parcelable {
    public static final Creator<NotificationItem> CREATOR = new Creator<NotificationItem>() {
        public NotificationItem createFromParcel(Parcel source) {
            return new NotificationItem(source);
        }

        public NotificationItem[] newArray(int size) {
            return new NotificationItem[size];
        }
    };
    public int notification;
    public int permissionCfg;
    public int permissionCode;
    public String pkgName;

    public NotificationItem() {
        this.pkgName = "";
        this.permissionCode = -1;
        this.permissionCfg = -1;
        this.notification = -1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.pkgName);
        dest.writeInt(this.permissionCode);
        dest.writeInt(this.permissionCfg);
        dest.writeInt(this.notification);
    }

    public String toString() {
        return "NotificationItem [pkgName=" + this.pkgName + ", permissionCode=" + this.permissionCode + ", permissionCfg=" + this.permissionCfg + ", notification=" + this.notification + "]";
    }

    private NotificationItem(Parcel source) {
        this.pkgName = "";
        this.permissionCode = -1;
        this.permissionCfg = -1;
        this.notification = -1;
        this.pkgName = source.readString();
        this.permissionCode = source.readInt();
        this.permissionCfg = source.readInt();
        this.notification = source.readInt();
    }
}
