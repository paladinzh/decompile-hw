package com.android.contacts.interactions;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.MoreContactUtils;
import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
class PhoneNumberInteraction$PhoneItem implements Parcelable, Collapsible<PhoneNumberInteraction$PhoneItem> {
    public static final Creator<PhoneNumberInteraction$PhoneItem> CREATOR = new Creator<PhoneNumberInteraction$PhoneItem>() {
        public PhoneNumberInteraction$PhoneItem createFromParcel(Parcel in) {
            return new PhoneNumberInteraction$PhoneItem(in);
        }

        public PhoneNumberInteraction$PhoneItem[] newArray(int size) {
            return new PhoneNumberInteraction$PhoneItem[size];
        }
    };
    String accountType;
    String dataSet;
    long id;
    String label;
    String mimeType;
    String phoneNumber;
    int subScriptionId;
    long type;

    private PhoneNumberInteraction$PhoneItem(Parcel in) {
        this.id = in.readLong();
        this.phoneNumber = in.readString();
        this.accountType = in.readString();
        this.dataSet = in.readString();
        this.type = in.readLong();
        this.label = in.readString();
        this.mimeType = in.readString();
        this.subScriptionId = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.accountType);
        dest.writeString(this.dataSet);
        dest.writeLong(this.type);
        dest.writeString(this.label);
        dest.writeString(this.mimeType);
        dest.writeInt(this.subScriptionId);
    }

    public int describeContents() {
        return 0;
    }

    public boolean collapseWith(PhoneNumberInteraction$PhoneItem phoneItem) {
        if (shouldCollapseWith(phoneItem)) {
            return true;
        }
        return false;
    }

    public boolean shouldCollapseWith(PhoneNumberInteraction$PhoneItem phoneItem) {
        if (this.subScriptionId != phoneItem.subScriptionId) {
            return false;
        }
        return MoreContactUtils.shouldCollapse("vnd.android.cursor.item/phone_v2", this.phoneNumber, "vnd.android.cursor.item/phone_v2", phoneItem.phoneNumber);
    }

    public String toString() {
        return this.phoneNumber;
    }
}
