package com.android.contacts.hap.roaming;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.MoreContactUtils;

public class RoamingPhoneItem implements Parcelable, Collapsible<RoamingPhoneItem> {
    public static final Creator<RoamingPhoneItem> CREATOR = new Creator<RoamingPhoneItem>() {
        public RoamingPhoneItem createFromParcel(Parcel in) {
            return new RoamingPhoneItem(in);
        }

        public RoamingPhoneItem[] newArray(int size) {
            return new RoamingPhoneItem[size];
        }
    };
    String country;
    int formDetail_constant;
    boolean isFromDetail;
    String phoneNumber;
    boolean sendReport;
    int sendReport_constant;
    int subScriptionId;

    public RoamingPhoneItem() {
        this.formDetail_constant = 1;
        this.sendReport_constant = 1;
    }

    private RoamingPhoneItem(Parcel in) {
        boolean z = true;
        this.formDetail_constant = 1;
        this.sendReport_constant = 1;
        this.phoneNumber = in.readString();
        this.country = in.readString();
        this.subScriptionId = in.readInt();
        this.isFromDetail = in.readInt() == this.formDetail_constant;
        if (in.readInt() != this.sendReport_constant) {
            z = false;
        }
        this.sendReport = z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        dest.writeString(this.phoneNumber);
        dest.writeString(this.country);
        dest.writeInt(this.subScriptionId);
        dest.writeInt(this.isFromDetail ? this.formDetail_constant : 0);
        if (this.sendReport) {
            i = this.sendReport_constant;
        }
        dest.writeInt(i);
    }

    public int describeContents() {
        return 0;
    }

    public boolean collapseWith(RoamingPhoneItem phoneItem) {
        if (shouldCollapseWith(phoneItem)) {
            return true;
        }
        return false;
    }

    public boolean shouldCollapseWith(RoamingPhoneItem phoneItem) {
        if (this.subScriptionId != phoneItem.subScriptionId) {
            return false;
        }
        return MoreContactUtils.shouldCollapse("vnd.android.cursor.item/phone_v2", this.phoneNumber, "vnd.android.cursor.item/phone_v2", phoneItem.phoneNumber);
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getCountry() {
        return this.country;
    }

    public String toString() {
        return this.phoneNumber;
    }
}
