package com.android.contacts.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.ContactsUtils;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.test.NeededForTesting;
import com.android.contacts.util.Objects;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class ValuesDelta implements Parcelable {
    public static final Creator<ValuesDelta> CREATOR = new Creator<ValuesDelta>() {
        public ValuesDelta createFromParcel(Parcel in) {
            ValuesDelta values = new ValuesDelta();
            values.readFromParcel(in);
            return values;
        }

        public ValuesDelta[] newArray(int size) {
            return new ValuesDelta[size];
        }
    };
    private static int sNextInsertId = -1;
    protected ContentValues mAfter;
    protected ContentValues mBefore;
    private boolean mFromTemplate;
    protected String mIdColumn = "_id";

    protected ValuesDelta() {
    }

    public static ValuesDelta fromBefore(ContentValues before) {
        ValuesDelta entry = new ValuesDelta();
        entry.mBefore = before;
        entry.mAfter = new ContentValues();
        return entry;
    }

    public static ValuesDelta fromAfter(ContentValues after) {
        ValuesDelta entry = new ValuesDelta();
        entry.mBefore = null;
        entry.mAfter = after;
        ContentValues contentValues = entry.mAfter;
        String str = entry.mIdColumn;
        int i = sNextInsertId;
        sNextInsertId = i - 1;
        contentValues.put(str, Integer.valueOf(i));
        return entry;
    }

    @NeededForTesting
    public ContentValues getAfter() {
        return this.mAfter;
    }

    public boolean containsKey(String key) {
        if (this.mAfter == null || !this.mAfter.containsKey(key)) {
            return this.mBefore != null ? this.mBefore.containsKey(key) : false;
        } else {
            return true;
        }
    }

    public String getAsString(String key) {
        if (this.mAfter != null && this.mAfter.containsKey(key)) {
            return this.mAfter.getAsString(key);
        }
        if (this.mBefore == null || !this.mBefore.containsKey(key)) {
            return null;
        }
        return this.mBefore.getAsString(key);
    }

    public byte[] getAsByteArray(String key) {
        if (this.mAfter != null && this.mAfter.containsKey(key)) {
            return this.mAfter.getAsByteArray(key);
        }
        if (this.mBefore == null || !this.mBefore.containsKey(key)) {
            return null;
        }
        return this.mBefore.getAsByteArray(key);
    }

    public Long getAsLong(String key) {
        if (this.mAfter != null && this.mAfter.containsKey(key)) {
            return this.mAfter.getAsLong(key);
        }
        if (this.mBefore == null || !this.mBefore.containsKey(key)) {
            return null;
        }
        return this.mBefore.getAsLong(key);
    }

    public Integer getAsInteger(String key) {
        return getAsInteger(key, null);
    }

    public Integer getAsInteger(String key, Integer defaultValue) {
        if (this.mAfter != null && this.mAfter.containsKey(key)) {
            return this.mAfter.getAsInteger(key);
        }
        if (this.mBefore == null || !this.mBefore.containsKey(key)) {
            return defaultValue;
        }
        return this.mBefore.getAsInteger(key);
    }

    public String getMimetype() {
        return getAsString("mimetype");
    }

    public Long getId() {
        return getAsLong(this.mIdColumn);
    }

    public void setIdColumn(String idColumn) {
        this.mIdColumn = idColumn;
    }

    public boolean isPrimary() {
        Long isPrimary = getAsLong("is_primary");
        if (isPrimary == null || isPrimary.longValue() == 0) {
            return false;
        }
        return true;
    }

    public void setFromTemplate(boolean isFromTemplate) {
        this.mFromTemplate = isFromTemplate;
    }

    public boolean isFromTemplate() {
        return this.mFromTemplate;
    }

    public boolean isSuperPrimary() {
        Long isSuperPrimary = getAsLong("is_super_primary");
        if (isSuperPrimary == null || isSuperPrimary.longValue() == 0) {
            return false;
        }
        return true;
    }

    public boolean beforeExists() {
        return this.mBefore != null ? this.mBefore.containsKey(this.mIdColumn) : false;
    }

    public boolean isVisible() {
        return this.mAfter != null;
    }

    public boolean isDelete() {
        return beforeExists() && this.mAfter == null;
    }

    public boolean isTransient() {
        return this.mBefore == null && this.mAfter == null;
    }

    public boolean isUpdate() {
        if (!beforeExists() || this.mAfter == null || this.mAfter.size() == 0) {
            return false;
        }
        for (String key : this.mAfter.keySet()) {
            Object newValue = this.mAfter.get(key);
            Object oldValue = this.mBefore.get(key);
            if (oldValue != null && "vnd.android.cursor.item/phone_v2".equals(this.mBefore.getAsString("mimetype")) && "data1".equals(key)) {
                oldValue = PhoneNumberFormatter.parsePhoneNumber(String.valueOf(oldValue)).replace(HwCustPreloadContacts.EMPTY_STRING, "");
                if (newValue != null) {
                    newValue = PhoneNumberFormatter.parsePhoneNumber(String.valueOf(newValue)).replace(HwCustPreloadContacts.EMPTY_STRING, "");
                }
            }
            if (oldValue == null) {
                if (!(newValue == null || "data2".equals(key))) {
                    return true;
                }
            } else if (!oldValue.equals(newValue)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNoop() {
        return beforeExists() && this.mAfter != null && this.mAfter.size() == 0;
    }

    public boolean isInsert() {
        return (beforeExists() || this.mAfter == null) ? false : true;
    }

    public void markDeleted() {
        this.mAfter = null;
    }

    private void ensureUpdate() {
        if (this.mAfter == null) {
            this.mAfter = new ContentValues();
        }
    }

    public void put(String key, String value) {
        ensureUpdate();
        this.mAfter.put(key, value);
    }

    public void put(String key, byte[] value) {
        ensureUpdate();
        this.mAfter.put(key, value);
    }

    public void put(String key, int value) {
        ensureUpdate();
        this.mAfter.put(key, Integer.valueOf(value));
    }

    public void put(String key, long value) {
        ensureUpdate();
        this.mAfter.put(key, Long.valueOf(value));
    }

    public void putNull(String key) {
        ensureUpdate();
        this.mAfter.putNull(key);
    }

    public Set<String> keySet() {
        HashSet<String> keys = Sets.newHashSet();
        if (this.mBefore != null) {
            for (Entry<String, Object> entry : this.mBefore.valueSet()) {
                keys.add((String) entry.getKey());
            }
        }
        if (this.mAfter != null) {
            for (Entry<String, Object> entry2 : this.mAfter.valueSet()) {
                keys.add((String) entry2.getKey());
            }
        }
        return keys;
    }

    public ContentValues getCompleteValues() {
        ContentValues values = new ContentValues();
        if (this.mBefore != null) {
            values.putAll(this.mBefore);
        }
        if (this.mAfter != null) {
            values.putAll(this.mAfter);
        }
        if (values.containsKey("data1")) {
            values.remove("group_sourceid");
        }
        return values;
    }

    public static ValuesDelta mergeAfter(ValuesDelta local, ValuesDelta remote) {
        if (local == null && (remote.isDelete() || remote.isTransient())) {
            return null;
        }
        if (local == null) {
            local = new ValuesDelta();
        }
        if (local.beforeExists()) {
            local.mAfter = remote.mAfter;
        } else {
            local.mAfter = remote.getCompleteValues();
        }
        return local;
    }

    public boolean equals(Object object) {
        boolean z = false;
        if (!(object instanceof ValuesDelta)) {
            return false;
        }
        ValuesDelta other = (ValuesDelta) object;
        if (subsetEquals(other)) {
            z = other.subsetEquals(this);
        }
        return z;
    }

    public boolean isValueEqual(ValuesDelta object) {
        boolean z = false;
        String mimeType = getMimetype();
        if (!mimeType.equals(object.getMimetype())) {
            return false;
        }
        if ("vnd.android.cursor.item/contact_event".equals(mimeType) || "vnd.android.cursor.item/relation".equals(mimeType)) {
            if (Objects.equal(getAsString("data1"), object.getAsString("data1"))) {
                z = Objects.equal(getAsString("data2"), object.getAsString("data2"));
            }
            return z;
        } else if ("vnd.android.cursor.item/im".equals(mimeType)) {
            if (Objects.equal(getAsString("data1"), object.getAsString("data1"))) {
                z = Objects.equal(getAsString("data5"), object.getAsString("data5"));
            }
            return z;
        } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
            return Objects.equal(ContactsUtils.removeDashesAndBlanks(getAsString("data1")), ContactsUtils.removeDashesAndBlanks(object.getAsString("data1")));
        } else {
            return Objects.equal(getAsString("data1"), object.getAsString("data1"));
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((this.mAfter == null ? 0 : this.mAfter.hashCode()) + 31) * 31) + (this.mBefore == null ? 0 : this.mBefore.hashCode())) * 31;
        if (this.mIdColumn != null) {
            i = this.mIdColumn.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    public void toString(StringBuilder builder) {
        builder.append("{ ");
        builder.append("IdColumn=");
        builder.append(this.mIdColumn);
        builder.append(", FromTemplate=");
        builder.append(this.mFromTemplate);
        builder.append(", ");
        for (String key : keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(getAsString(key));
            builder.append(", ");
        }
        builder.append("}");
    }

    public boolean subsetEquals(ValuesDelta other) {
        for (String key : keySet()) {
            String ourValue = getAsString(key);
            String theirValue = other.getAsString(key);
            if (ourValue == null) {
                if (theirValue != null) {
                    return false;
                }
            } else if (!ourValue.equals(theirValue)) {
                return false;
            }
        }
        return true;
    }

    public Builder buildDiff(Uri targetUri) {
        Builder builder;
        if (isInsert()) {
            this.mAfter.remove(this.mIdColumn);
            builder = ContentProviderOperation.newInsert(targetUri);
            builder.withValues(this.mAfter);
            return builder;
        } else if (isDelete()) {
            builder = ContentProviderOperation.newDelete(targetUri);
            builder.withSelection(this.mIdColumn + "=" + getId(), null);
            return builder;
        } else if (!isUpdate()) {
            return null;
        } else {
            builder = ContentProviderOperation.newUpdate(targetUri);
            builder.withSelection(this.mIdColumn + "=" + getId(), null);
            builder.withValues(this.mAfter);
            return builder;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBefore, flags);
        dest.writeParcelable(this.mAfter, flags);
        dest.writeString(this.mIdColumn);
    }

    public void readFromParcel(Parcel source) {
        ClassLoader loader = getClass().getClassLoader();
        this.mBefore = (ContentValues) source.readParcelable(loader);
        this.mAfter = (ContentValues) source.readParcelable(loader);
        this.mIdColumn = source.readString();
    }

    public void setGroupRowId(long groupId) {
        put("data1", groupId);
    }

    public Long getGroupRowId() {
        return getAsLong("data1");
    }

    public void setPhoto(byte[] value) {
        put("data15", value);
    }

    public byte[] getPhoto() {
        return getAsByteArray("data15");
    }

    public void setSuperPrimary(boolean val) {
        if (val) {
            put("is_super_primary", 1);
        } else {
            put("is_super_primary", 0);
        }
    }

    public void setPhoneticFamilyName(String value) {
        put("data9", value);
    }

    public void setPhoneticMiddleName(String value) {
        put("data8", value);
    }

    public void setPhoneticGivenName(String value) {
        put("data7", value);
    }

    public String getPhoneticFamilyName() {
        return getAsString("data9");
    }

    public String getPhoneticMiddleName() {
        return getAsString("data8");
    }

    public String getPhoneticGivenName() {
        return getAsString("data7");
    }

    public String getDisplayName() {
        return getAsString("data1");
    }

    public void setDisplayName(String name) {
        if (name == null) {
            putNull("data1");
        } else {
            put("data1", name);
        }
    }

    public String getPhoneNumber() {
        return getAsString("data1");
    }

    public String getPhoneNormalizedNumber() {
        return getAsString("data4");
    }

    public int getPhoneType() {
        if (getAsInteger("data2") == null) {
            return -1;
        }
        return getAsInteger("data2").intValue();
    }

    public String getEmailData() {
        return getAsString("data1");
    }

    public int getEmailType() {
        if (getAsInteger("data2") == null) {
            return -1;
        }
        return getAsInteger("data2").intValue();
    }
}
