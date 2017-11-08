package com.android.contacts.hap.sim.advanced;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimContact;
import java.util.ArrayList;
import java.util.List;

public class AdvancedSimContact extends SimContact {
    public String anr;
    public String anr_custom;
    public String anr_type;
    public String efid;
    public String email;
    public String email_custom;
    public String email_type;
    public String index;
    public String number_custom;
    public String number_type;

    public boolean equals(Object other) {
        if (other == null || !(other instanceof SimContact) || !(other instanceof AdvancedSimContact)) {
            return false;
        }
        AdvancedSimContact otherSim = (AdvancedSimContact) other;
        boolean nameBool = isTextEqualsExcludesNull(otherSim.name, this.name);
        boolean numberBool = isTextEqualsExcludesNull(CommonUtilMethods.extractNetworkPortion(this.number), CommonUtilMethods.extractNetworkPortion(otherSim.number));
        boolean emailBool = isTextEqualsExcludesNull(otherSim.email, this.email);
        boolean anrBool = isTextEqualsExcludesNull(CommonUtilMethods.extractNetworkPortion(this.anr), CommonUtilMethods.extractNetworkPortion(otherSim.anr));
        boolean efidBool = isTextEqualsExcludesNull(otherSim.efid, this.efid);
        boolean indexBool = isTextEqualsExcludesNull(otherSim.index, this.index);
        boolean specialBool = false;
        if (nameBool && emailBool && efidBool && indexBool && !numberBool && !anrBool) {
            specialBool = isTextEqualsExcludesNull(CommonUtilMethods.extractNetworkPortion(this.number), CommonUtilMethods.extractNetworkPortion(otherSim.anr));
        }
        if (specialBool) {
            return true;
        }
        if (!(nameBool && numberBool && emailBool && anrBool && efidBool)) {
            indexBool = false;
        }
        return indexBool;
    }

    public String toString() {
        return "AdvancedSimContact [id=" + this.id + ", name=" + this.name + ", number=" + this.number + ", email=" + this.email + ", anr=" + this.anr + ", number_type=" + this.number_type + ", email_type=" + this.email_type + ", anr_type=" + this.anr_type + ", number_custom=" + this.number_custom + ", email_custom=" + this.email_custom + ", anr_custom=" + this.anr_custom + ", efid=" + this.efid + ", index=" + this.index + "]";
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((((this.name == null ? 0 : this.name.hashCode()) + 31) * 31) + (this.number == null ? 0 : this.number.hashCode())) * 31) + (this.email == null ? 0 : this.email.hashCode())) * 31;
        if (this.anr != null) {
            i = this.anr.hashCode();
        }
        return hashCode + i;
    }

    public void appendTo(List<ContentProviderOperation> list, int backRefId, String aSimAccountName, String aAccountType) {
        Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        builder.withValue("account_name", aSimAccountName);
        builder.withValue("account_type", aAccountType);
        builder.withValue("aggregation_mode", Integer.valueOf(3));
        builder.withValue("sync1", this.efid);
        builder.withValue("sync2", this.index);
        builder.withValue("sync3", hashCode() + "");
        list.add(builder.build());
        Uri uri = Data.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", "true").build();
        if (!TextUtils.isEmpty(this.name)) {
            builder = ContentProviderOperation.newInsert(uri);
            builder.withValueBackReference("raw_contact_id", backRefId);
            builder.withValue("mimetype", "vnd.android.cursor.item/name");
            builder.withValue("data1", this.name);
            list.add(builder.build());
        }
        if (!TextUtils.isEmpty(this.number)) {
            builder = ContentProviderOperation.newInsert(uri);
            builder.withValueBackReference("raw_contact_id", backRefId);
            builder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            builder.withValue("data1", CommonUtilMethods.extractNetworkPortion(this.number));
            if (TextUtils.isEmpty(this.number_type)) {
                builder.withValue("data2", Integer.valueOf(2));
            } else {
                builder.withValue("data2", this.number_type);
                builder.withValue("data3", this.number_custom);
            }
            builder.withValue("data4", fillNormalizedNumber());
            builder.withValue("is_primary", Integer.valueOf(0));
            list.add(builder.build());
        }
        if (!TextUtils.isEmpty(this.anr)) {
            builder = ContentProviderOperation.newInsert(uri);
            builder.withValueBackReference("raw_contact_id", backRefId);
            builder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            builder.withValue("data1", CommonUtilMethods.extractNetworkPortion(this.anr));
            if (TextUtils.isEmpty(this.number_type)) {
                builder.withValue("data2", Integer.valueOf(2));
            } else {
                builder.withValue("data2", this.anr_type);
                builder.withValue("data3", this.anr_custom);
            }
            list.add(builder.build());
        }
        if (!TextUtils.isEmpty(this.email)) {
            builder = ContentProviderOperation.newInsert(uri);
            builder.withValueBackReference("raw_contact_id", backRefId);
            builder.withValue("mimetype", "vnd.android.cursor.item/email_v2");
            builder.withValue("data1", this.email);
            if (TextUtils.isEmpty(this.email_type)) {
                builder.withValue("data2", Integer.valueOf(1));
            } else {
                builder.withValue("data2", this.email_type);
                builder.withValue("data3", this.email_custom);
            }
            builder.withValue("is_primary", Integer.valueOf(1));
            list.add(builder.build());
        }
    }

    protected void updateSimContact(Context aContext, String rawContactId) {
        ContentResolver mContentResolver = aContext.getContentResolver();
        ArrayList<ContentProviderOperation> operationList = new ArrayList();
        String[] selectionArgs = new String[]{rawContactId, "vnd.android.cursor.item/group_membership", "vnd.android.huawei.cursor.item/ringtone"};
        Builder lBuilder = ContentProviderOperation.newDelete(Data.CONTENT_URI);
        lBuilder.withSelection("raw_contact_id=? AND mimetype NOT IN (?,?)", selectionArgs);
        operationList.add(lBuilder.build());
        if (!TextUtils.isEmpty(this.name)) {
            lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            lBuilder.withValue("raw_contact_id", rawContactId);
            lBuilder.withValue("mimetype", "vnd.android.cursor.item/name");
            lBuilder.withValue("data2", this.name);
            operationList.add(lBuilder.build());
        }
        if (!TextUtils.isEmpty(this.number)) {
            lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            lBuilder.withValue("raw_contact_id", rawContactId);
            lBuilder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            lBuilder.withValue("data1", this.number);
            lBuilder.withValue("data2", Integer.valueOf(2));
            lBuilder.withValue("data4", fillNormalizedNumber());
            lBuilder.withValue("is_primary", Integer.valueOf(0));
            operationList.add(lBuilder.build());
        }
        if (!TextUtils.isEmpty(this.anr)) {
            lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            lBuilder.withValue("raw_contact_id", rawContactId);
            lBuilder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            lBuilder.withValue("data1", CommonUtilMethods.extractNetworkPortion(this.anr));
            lBuilder.withValue("data2", Integer.valueOf(2));
            operationList.add(lBuilder.build());
        }
        if (!TextUtils.isEmpty(this.email)) {
            lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            lBuilder.withValue("raw_contact_id", rawContactId);
            lBuilder.withValue("mimetype", "vnd.android.cursor.item/email_v2");
            lBuilder.withValue("data1", this.email);
            lBuilder.withValue("data2", Integer.valueOf(1));
            lBuilder.withValue("is_primary", Integer.valueOf(1));
            operationList.add(lBuilder.build());
        }
        String lUid = hashCode() + "";
        lBuilder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        lBuilder.withSelection("_id=?", new String[]{rawContactId});
        lBuilder.withValue("sync1", this.efid);
        lBuilder.withValue("sync2", this.index);
        lBuilder.withValue("sync3", lUid);
        operationList.add(lBuilder.build());
        try {
            mContentResolver.applyBatch("com.android.contacts", operationList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } catch (SQLiteFullException e3) {
        }
    }

    public int hashCodeForEmptyContact() {
        int result = 31 * 31;
        result = 31 * 961;
        return 31 * 29791;
    }

    public String getUniqueKeyString() {
        return this.efid + ":" + this.index;
    }
}
