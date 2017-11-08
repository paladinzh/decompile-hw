package com.android.contacts.model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.test.NeededForTesting;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;

public class RawContactDelta implements Parcelable {
    public static final Creator<RawContactDelta> CREATOR = new Creator<RawContactDelta>() {
        public RawContactDelta createFromParcel(Parcel in) {
            RawContactDelta state = new RawContactDelta();
            state.readFromParcel(in);
            return state;
        }

        public RawContactDelta[] newArray(int size) {
            return new RawContactDelta[size];
        }
    };
    private boolean isFromCalllog = true;
    private Uri mContactsQueryUri = RawContacts.CONTENT_URI;
    private final HashMap<String, ArrayList<ValuesDelta>> mEntries = Maps.newHashMap();
    private String mExtraValue;
    private ArrayList<String> mExtraValueList = new ArrayList();
    private HasExtraIntent mHasExtraIntent = new HasExtraIntent();
    private ArrayList<String> mHasExtraIntentList = new ArrayList();
    private ValuesDelta mValues;

    static class HasExtraIntent {
        boolean lHasExtra;
        String lMimetype;

        HasExtraIntent() {
        }
    }

    public boolean isFromCalllog() {
        return this.isFromCalllog;
    }

    public void setFromCalllog(boolean isFromCalllog) {
        this.isFromCalllog = isFromCalllog;
    }

    public RawContactDelta(ValuesDelta values) {
        this.mValues = values;
    }

    public static RawContactDelta fromBefore(RawContact before) {
        RawContactDelta rawContactDelta = new RawContactDelta();
        rawContactDelta.mValues = ValuesDelta.fromBefore(before.getValues());
        rawContactDelta.mValues.setIdColumn("_id");
        for (ContentValues values : before.getContentValues()) {
            rawContactDelta.addEntry(ValuesDelta.fromBefore(values));
        }
        return rawContactDelta;
    }

    public static RawContactDelta mergeAfter(RawContactDelta local, RawContactDelta remote) {
        ValuesDelta remoteValues = remote.mValues;
        if (local == null && (remoteValues.isDelete() || remoteValues.isTransient())) {
            return null;
        }
        if (local == null) {
            local = new RawContactDelta();
        }
        local.mValues = ValuesDelta.mergeAfter(local.mValues, remote.mValues);
        for (ArrayList<ValuesDelta> mimeEntries : remote.mEntries.values()) {
            for (ValuesDelta remoteEntry : mimeEntries) {
                ValuesDelta localEntry = local.getEntry(remoteEntry.getId());
                ValuesDelta merged = ValuesDelta.mergeAfter(localEntry, remoteEntry);
                if (localEntry == null && merged != null) {
                    local.addEntry(merged);
                }
            }
        }
        return local;
    }

    public ValuesDelta getValues() {
        return this.mValues;
    }

    public boolean isContactInsert() {
        return this.mValues.isInsert();
    }

    public ValuesDelta getPrimaryEntry(String mimeType) {
        ValuesDelta valuesDelta = null;
        ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) {
            return null;
        }
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isPrimary()) {
                return entry;
            }
        }
        if (mimeEntries.size() > 0) {
            valuesDelta = (ValuesDelta) mimeEntries.get(0);
        }
        return valuesDelta;
    }

    @NeededForTesting
    public ValuesDelta getSuperPrimaryEntry(String mimeType, boolean forceSelection) {
        ValuesDelta valuesDelta = null;
        ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) {
            return null;
        }
        ValuesDelta primary = null;
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isSuperPrimary()) {
                return entry;
            }
            if (entry.isPrimary()) {
                primary = entry;
            }
        }
        if (!forceSelection) {
            return null;
        }
        if (primary != null) {
            return primary;
        }
        if (mimeEntries.size() > 0) {
            valuesDelta = (ValuesDelta) mimeEntries.get(0);
        }
        return valuesDelta;
    }

    public AccountType getRawContactAccountType(Context context) {
        ContentValues entityValues = getValues().getCompleteValues();
        return AccountTypeManager.getInstance(context).getAccountType(entityValues.getAsString("account_type"), entityValues.getAsString("data_set"));
    }

    public Long getRawContactId() {
        return getValues().getAsLong("_id");
    }

    public String getSortKey() {
        return getValues().getAsString("sort_key");
    }

    public String getAccountName() {
        return getValues().getAsString("account_name");
    }

    public String getAccountType() {
        return getValues().getAsString("account_type");
    }

    public String getDataSet() {
        return getValues().getAsString("data_set");
    }

    public AccountType getAccountType(AccountTypeManager manager) {
        return manager.getAccountType(getAccountType(), getDataSet());
    }

    public boolean isVisible() {
        return getValues().isVisible();
    }

    private ArrayList<ValuesDelta> getMimeEntries(String mimeType, boolean lazyCreate) {
        ArrayList<ValuesDelta> mimeEntries = (ArrayList) this.mEntries.get(mimeType);
        if (mimeEntries != null || !lazyCreate) {
            return mimeEntries;
        }
        mimeEntries = Lists.newArrayList();
        this.mEntries.put(mimeType, mimeEntries);
        return mimeEntries;
    }

    public ArrayList<ValuesDelta> getMimeEntries(String mimeType) {
        return getMimeEntries(mimeType, false);
    }

    public int getMimeEntriesCount(String mimeType, boolean onlyVisible) {
        ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType);
        if (mimeEntries == null) {
            return 0;
        }
        int count = 0;
        for (ValuesDelta child : mimeEntries) {
            if (!onlyVisible || child.isVisible()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasMimeEntries(String mimeType) {
        return this.mEntries.containsKey(mimeType);
    }

    public ValuesDelta addEntry(ValuesDelta entry) {
        getMimeEntries(entry.getMimetype(), true).add(entry);
        return entry;
    }

    public ArrayList<ContentValues> getContentValues() {
        ArrayList<ContentValues> values = Lists.newArrayList();
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta entry : mimeEntries) {
                if (!entry.isDelete()) {
                    values.add(entry.getCompleteValues());
                }
            }
        }
        return values;
    }

    public ValuesDelta getEntry(Long childId) {
        if (childId == null) {
            return null;
        }
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta entry : mimeEntries) {
                if (childId.equals(entry.getId())) {
                    return entry;
                }
            }
        }
        return null;
    }

    public int getEntryCount(boolean onlyVisible) {
        int count = 0;
        for (String mimeType : this.mEntries.keySet()) {
            count += getMimeEntriesCount(mimeType, onlyVisible);
        }
        return count;
    }

    public HashMap<String, ArrayList<ValuesDelta>> getEntries() {
        return this.mEntries;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((this.mContactsQueryUri == null ? 0 : this.mContactsQueryUri.hashCode()) + 31) * 31) + (this.mEntries == null ? 0 : this.mEntries.hashCode())) * 31;
        if (this.mValues != null) {
            i = this.mValues.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object object) {
        if (!(object instanceof RawContactDelta)) {
            return false;
        }
        RawContactDelta other = (RawContactDelta) object;
        if (!other.mValues.equals(this.mValues)) {
            return false;
        }
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                if (!other.containsEntry(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean containsEntry(ValuesDelta entry) {
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                if (child.equals(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void markDeleted() {
        this.mValues.markDeleted();
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                child.markDeleted();
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n(");
        builder.append("Uri=");
        builder.append(this.mContactsQueryUri);
        builder.append(", Values=");
        builder.append(this.mValues != null ? this.mValues.toString() : "null");
        builder.append(", Entries={");
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                builder.append("\n\t");
                child.toString(builder);
            }
        }
        builder.append("\n})\n");
        return builder.toString();
    }

    private void possibleAdd(ArrayList<ContentProviderOperation> diff, Builder builder) {
        if (builder != null) {
            diff.add(builder.build());
        }
    }

    public void buildAssert(ArrayList<ContentProviderOperation> buildInto) {
        if (!this.mValues.isInsert()) {
            Long beforeId = this.mValues.getId();
            Long beforeVersion = this.mValues.getAsLong("version");
            if (beforeId != null && beforeVersion != null) {
                Builder builder = ContentProviderOperation.newAssertQuery(this.mContactsQueryUri);
                builder.withSelection("_id=" + beforeId, null);
                builder.withValue("version", beforeVersion);
                buildInto.add(builder.build());
            }
        }
    }

    public void buildDiff(ArrayList<ContentProviderOperation> buildInto) {
        int firstIndex = buildInto.size();
        boolean isContactInsert = this.mValues.isInsert();
        boolean isContactDelete = this.mValues.isDelete();
        boolean isContactUpdate = (isContactInsert || isContactDelete) ? false : true;
        Long beforeId = this.mValues.getId();
        if (isContactInsert) {
            this.mValues.put("aggregation_mode", 2);
        }
        possibleAdd(buildInto, this.mValues.buildDiff(this.mContactsQueryUri));
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                Builder builder;
                if (!isContactDelete) {
                    if (this.mContactsQueryUri.equals(Profile.CONTENT_RAW_CONTACTS_URI)) {
                        builder = child.buildDiff(Uri.withAppendedPath(Profile.CONTENT_URI, MapTilsCacheAndResManager.AUTONAVI_DATA_PATH));
                    } else {
                        builder = child.buildDiff(Data.CONTENT_URI);
                    }
                    if (builder == null || !child.isInsert()) {
                        if (isContactInsert && builder != null) {
                            throw new IllegalArgumentException("When parent insert, child must be also");
                        }
                    } else if (isContactInsert) {
                        builder.withValueBackReference("raw_contact_id", firstIndex);
                    } else {
                        builder.withValue("raw_contact_id", beforeId);
                    }
                    possibleAdd(buildInto, builder);
                }
            }
        }
        if ((buildInto.size() > firstIndex) && isContactUpdate) {
            buildInto.add(firstIndex, buildSetAggregationMode(beforeId, 2).build());
            buildInto.add(buildSetAggregationMode(beforeId, 0).build());
        } else if (isContactInsert) {
            builder = ContentProviderOperation.newUpdate(this.mContactsQueryUri);
            builder.withValue("aggregation_mode", Integer.valueOf(0));
            builder.withSelection("_id=?", new String[1]);
            builder.withSelectionBackReference(0, firstIndex);
            buildInto.add(builder.build());
        }
    }

    protected Builder buildSetAggregationMode(Long beforeId, int mode) {
        Builder builder = ContentProviderOperation.newUpdate(this.mContactsQueryUri);
        builder.withValue("aggregation_mode", Integer.valueOf(mode));
        builder.withSelection("_id=" + beforeId, null);
        return builder;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getEntryCount(false));
        dest.writeParcelable(this.mValues, flags);
        dest.writeParcelable(this.mContactsQueryUri, flags);
        for (ArrayList<ValuesDelta> mimeEntries : this.mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                dest.writeParcelable(child, flags);
            }
        }
    }

    public void readFromParcel(Parcel source) {
        ClassLoader loader = getClass().getClassLoader();
        int size = source.readInt();
        this.mValues = (ValuesDelta) source.readParcelable(loader);
        this.mContactsQueryUri = (Uri) source.readParcelable(loader);
        for (int i = 0; i < size; i++) {
            addEntry((ValuesDelta) source.readParcelable(loader));
        }
    }

    public void setProfileQueryUri() {
        this.mContactsQueryUri = Profile.CONTENT_RAW_CONTACTS_URI;
    }

    public String getExtraMimetype() {
        return this.mHasExtraIntent.lMimetype;
    }

    public boolean getExtraBoolean() {
        return this.mHasExtraIntent.lHasExtra;
    }

    public void setHasExtra(String aMimetype, boolean aHasExtra) {
        this.mHasExtraIntent.lMimetype = aMimetype;
        this.mHasExtraIntent.lHasExtra = aHasExtra;
        if (!this.mHasExtraIntentList.contains(this.mHasExtraIntent.lMimetype)) {
            this.mHasExtraIntentList.add(this.mHasExtraIntent.lMimetype);
        }
    }

    public void setExtraValue(String aValue) {
        this.mExtraValue = aValue;
        this.mExtraValueList.add(this.mExtraValue);
    }

    public String getExtraValue() {
        return this.mExtraValue;
    }
}
