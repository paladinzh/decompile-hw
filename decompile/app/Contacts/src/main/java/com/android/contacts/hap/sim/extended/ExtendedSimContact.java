package com.android.contacts.hap.sim.extended;

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

public class ExtendedSimContact extends SimContact {
    public String efid;
    public String index;

    public boolean equals(Object other) {
        if (!super.equals(other) || !(other instanceof ExtendedSimContact)) {
            return false;
        }
        ExtendedSimContact otherSim = (ExtendedSimContact) other;
        boolean efidBool = isTextEqualsExcludesNull(otherSim.efid, this.efid);
        boolean indexBool = isTextEqualsExcludesNull(otherSim.index, this.index);
        if (!efidBool) {
            indexBool = false;
        }
        return indexBool;
    }

    public String toString() {
        return "ExtendedSimContact [id=" + this.id + ", name=" + this.name + ", number=" + this.number + ", efid=" + this.efid + ", index=" + this.index + "]";
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((this.name == null ? 0 : this.name.hashCode()) + 31) * 31;
        if (this.number != null) {
            i = this.number.hashCode();
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
            builder.withValue("data2", this.name);
            list.add(builder.build());
        }
        if (!TextUtils.isEmpty(this.number)) {
            builder = ContentProviderOperation.newInsert(uri);
            builder.withValueBackReference("raw_contact_id", backRefId);
            builder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
            builder.withValue("data1", CommonUtilMethods.extractNetworkPortion(this.number));
            builder.withValue("data2", Integer.valueOf(2));
            builder.withValue("data4", fillNormalizedNumber());
            builder.withValue("is_primary", Integer.valueOf(0));
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
            lBuilder.withValue("data1", CommonUtilMethods.extractNetworkPortion(this.number));
            lBuilder.withValue("data2", Integer.valueOf(2));
            lBuilder.withValue("data4", fillNormalizedNumber());
            lBuilder.withValue("is_primary", Integer.valueOf(0));
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

    public String getUniqueKeyString() {
        return this.efid + ":" + this.index;
    }
}
