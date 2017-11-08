package com.android.contacts.hap.interactions;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.MoreContactUtils;
import com.google.common.annotations.VisibleForTesting;

@VisibleForTesting
class EmailAddressInteraction$EmailItem implements Parcelable, Collapsible<EmailAddressInteraction$EmailItem> {
    public static final Creator<EmailAddressInteraction$EmailItem> CREATOR = new Creator<EmailAddressInteraction$EmailItem>() {
        public EmailAddressInteraction$EmailItem createFromParcel(Parcel aIn) {
            return new EmailAddressInteraction$EmailItem(aIn);
        }

        public EmailAddressInteraction$EmailItem[] newArray(int aSize) {
            return new EmailAddressInteraction$EmailItem[aSize];
        }
    };
    String mEmailAddress;
    long mId;

    public EmailAddressInteraction$EmailItem(Parcel aIn) {
        this.mId = aIn.readLong();
        this.mEmailAddress = aIn.readString();
    }

    public boolean collapseWith(EmailAddressInteraction$EmailItem aEmailItem) {
        if (shouldCollapseWith(aEmailItem)) {
            return true;
        }
        return false;
    }

    public boolean shouldCollapseWith(EmailAddressInteraction$EmailItem aEmailItem) {
        return MoreContactUtils.shouldCollapse("vnd.android.cursor.item/email_v2", this.mEmailAddress, "vnd.android.cursor.item/email_v2", aEmailItem.mEmailAddress);
    }

    public String toString() {
        return this.mEmailAddress;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel aDest, int aFlags) {
        aDest.writeLong(this.mId);
        aDest.writeString(this.mEmailAddress);
    }
}
