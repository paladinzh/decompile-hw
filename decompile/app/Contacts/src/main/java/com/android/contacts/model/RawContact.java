package com.android.contacts.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.Data;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataItem;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public final class RawContact implements Parcelable {
    public static final Creator<RawContact> CREATOR = new Creator<RawContact>() {
        public RawContact createFromParcel(Parcel parcel) {
            return new RawContact(parcel);
        }

        public RawContact[] newArray(int i) {
            return new RawContact[i];
        }
    };
    private AccountTypeManager mAccountTypeManager;
    private final ArrayList<NamedDataItem> mDataItems;
    private final ContentValues mValues;

    public static final class NamedDataItem implements Parcelable {
        public static final Creator<NamedDataItem> CREATOR = new Creator<NamedDataItem>() {
            public NamedDataItem createFromParcel(Parcel parcel) {
                return new NamedDataItem(parcel);
            }

            public NamedDataItem[] newArray(int i) {
                return new NamedDataItem[i];
            }
        };
        public final ContentValues mContentValues;
        public final Uri mUri;

        public NamedDataItem(Uri uri, ContentValues values) {
            this.mUri = uri;
            this.mContentValues = values;
        }

        public NamedDataItem(Parcel parcel) {
            this.mUri = (Uri) parcel.readParcelable(Uri.class.getClassLoader());
            this.mContentValues = (ContentValues) parcel.readParcelable(ContentValues.class.getClassLoader());
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.mUri, i);
            parcel.writeParcelable(this.mContentValues, i);
        }

        public int hashCode() {
            return Objects.hashCode(this.mUri, this.mContentValues);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            NamedDataItem other = (NamedDataItem) obj;
            if (Objects.equal(this.mUri, other.mUri)) {
                z = Objects.equal(this.mContentValues, other.mContentValues);
            }
            return z;
        }
    }

    public static RawContact createFrom(Entity entity) {
        ContentValues values = entity.getEntityValues();
        ArrayList<NamedContentValues> subValues = entity.getSubValues();
        RawContact rawContact = new RawContact(values);
        for (NamedContentValues subValue : subValues) {
            rawContact.addNamedDataItemValues(subValue.uri, subValue.values);
        }
        return rawContact;
    }

    public RawContact() {
        this(new ContentValues());
    }

    public RawContact(ContentValues values) {
        this.mValues = values;
        this.mDataItems = new ArrayList();
    }

    private RawContact(Parcel parcel) {
        this.mValues = (ContentValues) parcel.readParcelable(ContentValues.class.getClassLoader());
        this.mDataItems = Lists.newArrayList();
        parcel.readTypedList(this.mDataItems, NamedDataItem.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mValues, i);
        parcel.writeTypedList(this.mDataItems);
    }

    public AccountTypeManager getAccountTypeManager(Context context) {
        if (this.mAccountTypeManager == null) {
            this.mAccountTypeManager = AccountTypeManager.getInstance(context);
        }
        return this.mAccountTypeManager;
    }

    public ContentValues getValues() {
        return this.mValues;
    }

    public Long getId() {
        return getValues().getAsLong("_id");
    }

    public String getAccountName() {
        return getValues().getAsString("account_name");
    }

    public String getAccountTypeString() {
        return getValues().getAsString("account_type");
    }

    public String getDataSet() {
        return getValues().getAsString("data_set");
    }

    public AccountType getAccountType(Context context) {
        return getAccountTypeManager(context).getAccountType(getAccountTypeString(), getDataSet());
    }

    private void setAccount(String accountName, String accountType, String dataSet) {
        ContentValues values = getValues();
        if (accountName == null) {
            if (accountType == null && dataSet == null) {
                values.putNull("account_name");
                values.putNull("account_type");
                values.putNull("data_set");
                return;
            }
        } else if (accountType != null) {
            values.put("account_name", accountName);
            values.put("account_type", accountType);
            if (dataSet == null) {
                values.putNull("data_set");
            } else {
                values.put("data_set", dataSet);
            }
            return;
        }
        throw new IllegalArgumentException("Not a valid combination of account name, type, and data set.");
    }

    public void setAccount(AccountWithDataSet accountWithDataSet) {
        setAccount(accountWithDataSet.name, accountWithDataSet.type, accountWithDataSet.dataSet);
    }

    public void setAccountToLocal() {
        setAccount(null, null, null);
    }

    public void addDataItemValues(ContentValues values) {
        addNamedDataItemValues(Data.CONTENT_URI, values);
    }

    public NamedDataItem addNamedDataItemValues(Uri uri, ContentValues values) {
        NamedDataItem namedItem = new NamedDataItem(uri, values);
        this.mDataItems.add(namedItem);
        return namedItem;
    }

    public ArrayList<ContentValues> getContentValues() {
        ArrayList<ContentValues> list = Lists.newArrayListWithCapacity(this.mDataItems.size());
        for (NamedDataItem dataItem : this.mDataItems) {
            if (Data.CONTENT_URI.equals(dataItem.mUri)) {
                list.add(dataItem.mContentValues);
            }
        }
        return list;
    }

    public List<DataItem> getDataItems() {
        ArrayList<DataItem> list = Lists.newArrayListWithCapacity(this.mDataItems.size());
        for (NamedDataItem dataItem : this.mDataItems) {
            if (Data.CONTENT_URI.equals(dataItem.mUri)) {
                list.add(DataItem.createFrom(dataItem.mContentValues));
            }
        }
        return list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RawContact: ").append(this.mValues);
        for (NamedDataItem namedDataItem : this.mDataItems) {
            sb.append("\n  ").append(namedDataItem.mUri);
            sb.append("\n  -> ").append(namedDataItem.mContentValues);
        }
        return sb.toString();
    }

    public int hashCode() {
        return Objects.hashCode(this.mValues, this.mDataItems);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RawContact other = (RawContact) obj;
        if (Objects.equal(this.mValues, other.mValues)) {
            z = Objects.equal(this.mDataItems, other.mDataItems);
        }
        return z;
    }
}
