package com.android.contacts.model.dataitem;

import android.content.ContentValues;
import android.content.Context;

public class DataItem {
    private final ContentValues mContentValues;

    protected DataItem(ContentValues values) {
        this.mContentValues = values;
    }

    public static DataItem createFrom(ContentValues values) {
        String mimeType = values.getAsString("mimetype");
        if ("vnd.android.cursor.item/group_membership".equals(mimeType)) {
            return new GroupMembershipDataItem(values);
        }
        if ("vnd.android.cursor.item/name".equals(mimeType)) {
            return new StructuredNameDataItem(values);
        }
        if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
            return new PhoneDataItem(values);
        }
        if ("vnd.android.cursor.item/email_v2".equals(mimeType)) {
            return new EmailDataItem(values);
        }
        if ("vnd.android.cursor.item/postal-address_v2".equals(mimeType)) {
            return new StructuredPostalDataItem(values);
        }
        if ("vnd.android.cursor.item/im".equals(mimeType)) {
            return new ImDataItem(values);
        }
        if ("vnd.android.cursor.item/organization".equals(mimeType)) {
            return new OrganizationDataItem(values);
        }
        if ("vnd.android.cursor.item/nickname".equals(mimeType)) {
            return new NicknameDataItem(values);
        }
        if ("vnd.android.cursor.item/note".equals(mimeType)) {
            return new NoteDataItem(values);
        }
        if ("vnd.android.cursor.item/website".equals(mimeType)) {
            return new WebsiteDataItem(values);
        }
        if ("vnd.android.cursor.item/sip_address".equals(mimeType)) {
            return new SipAddressDataItem(values);
        }
        if ("vnd.android.cursor.item/contact_event".equals(mimeType)) {
            return new EventDataItem(values);
        }
        if ("vnd.android.cursor.item/relation".equals(mimeType)) {
            return new RelationDataItem(values);
        }
        if ("vnd.android.cursor.item/identity".equals(mimeType)) {
            return new IdentityDataItem(values);
        }
        if ("vnd.android.cursor.item/photo".equals(mimeType)) {
            return new PhotoDataItem(values);
        }
        return new DataItem(values);
    }

    public ContentValues getContentValues() {
        return this.mContentValues;
    }

    public void setRawContactId(long rawContactId) {
        this.mContentValues.put("raw_contact_id", Long.valueOf(rawContactId));
    }

    public long getId() {
        return this.mContentValues.getAsLong("_id").longValue();
    }

    public String getMimeType() {
        return this.mContentValues.getAsString("mimetype");
    }

    public void setMimeType(String mimeType) {
        this.mContentValues.put("mimetype", mimeType);
    }

    public boolean isSuperPrimary() {
        Integer superPrimary = this.mContentValues.getAsInteger("is_super_primary");
        if (superPrimary == null || superPrimary.intValue() == 0) {
            return false;
        }
        return true;
    }

    public boolean isManualPrimary() {
        boolean z = true;
        Integer smartPrimary = this.mContentValues.getAsInteger("data5");
        if (smartPrimary == null) {
            return isSuperPrimary();
        }
        if (smartPrimary.intValue() == 1) {
            z = false;
        }
        return z;
    }

    public boolean hasKindTypeColumn(DataKind kind) {
        String key = kind.typeColumn;
        if (key == null || !this.mContentValues.containsKey(key) || this.mContentValues.getAsInteger(key) == null) {
            return false;
        }
        return true;
    }

    public int getKindTypeColumn(DataKind kind) {
        Integer result = this.mContentValues.getAsInteger(kind.typeColumn);
        if (result != null) {
            return result.intValue();
        }
        return -1;
    }

    public String buildDataString(Context context, DataKind kind) {
        String str = null;
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, this.mContentValues);
        if (actionBody != null) {
            str = actionBody.toString();
        }
        return str;
    }
}
