package com.android.contacts.list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri.Builder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.account.AccountWithDataSet;
import java.util.ArrayList;

public final class ContactListFilter implements Comparable<ContactListFilter>, Parcelable {
    public static final Creator<ContactListFilter> CREATOR = new Creator<ContactListFilter>() {
        public ContactListFilter createFromParcel(Parcel source) {
            return new ContactListFilter(source.readInt(), source.readString(), source.readString(), source.readString(), null);
        }

        public ContactListFilter[] newArray(int size) {
            return new ContactListFilter[size];
        }
    };
    public final String accountName;
    public final String accountType;
    public final String dataSet;
    public final int filterType;
    public long groupId;
    public final Drawable icon;
    public ArrayList<AccountWithDataSet> mAccounts;
    public String mCurrentCompanyName;
    private String mId;
    public boolean mIsFavoritesForWidget;
    public boolean mIsFiterChanged = false;
    public boolean mIsNoCompanyGroup = false;
    public boolean mIsResultBackRequired;
    public boolean mIsShareOrDelete = false;
    public long[] mRemovedContactIds;
    public long[] mSelectedContacts;
    public final int totalContactsCount;
    public final int totalContactsCountInAllAccounts;
    public final int totalRawContactsCount;
    public final int totalRawContactsCountInAllAccounts;

    public ContactListFilter(int filterType, String accountType, String accountName, String dataSet, Drawable icon, long groupId) {
        this.filterType = filterType;
        this.accountType = accountType;
        this.accountName = accountName;
        this.dataSet = dataSet;
        this.icon = icon;
        this.groupId = groupId;
        this.totalRawContactsCount = -1;
        this.totalContactsCount = -1;
        this.totalRawContactsCountInAllAccounts = -1;
        this.totalContactsCountInAllAccounts = -1;
    }

    private ContactListFilter(int filterType, String accountType, String accountName, String dataSet, Drawable icon, long groupId, long[] excludeContactIds, long[] removedContactIds, boolean isResultBackRequired) {
        this.filterType = filterType;
        this.accountType = accountType;
        this.accountName = accountName;
        this.dataSet = dataSet;
        this.icon = icon;
        this.groupId = groupId;
        this.mSelectedContacts = excludeContactIds;
        this.mRemovedContactIds = removedContactIds;
        this.mIsResultBackRequired = isResultBackRequired;
        this.mIsFavoritesForWidget = false;
        this.totalRawContactsCount = -1;
        this.totalContactsCount = -1;
        this.totalRawContactsCountInAllAccounts = -1;
        this.totalContactsCountInAllAccounts = -1;
    }

    public ContactListFilter(int filterType, String accountType, String accountName, String dataSet, Drawable icon) {
        this.filterType = filterType;
        this.accountType = accountType;
        this.accountName = accountName;
        this.dataSet = dataSet;
        this.icon = icon;
        this.totalRawContactsCount = -1;
        this.totalContactsCount = -1;
        this.totalRawContactsCountInAllAccounts = -1;
        this.totalContactsCountInAllAccounts = -1;
    }

    public ContactListFilter(int filterType, String accountType, String accountName, String dataSet, Drawable icon, int totalRawContactsCount, int totalContactsCount, int totalRawContactsCountInAllAccounts, int totalContactsCountInAllAccounts) {
        this.filterType = filterType;
        this.accountType = accountType;
        this.accountName = accountName;
        this.dataSet = dataSet;
        this.icon = icon;
        this.totalRawContactsCount = totalRawContactsCount;
        this.totalContactsCount = totalContactsCount;
        this.totalRawContactsCountInAllAccounts = totalRawContactsCountInAllAccounts;
        this.totalContactsCountInAllAccounts = totalContactsCountInAllAccounts;
    }

    public static ContactListFilter createFilterWithType(int filterType) {
        return new ContactListFilter(filterType, null, null, null, null);
    }

    public static ContactListFilter createFilterWithType(int filterType, int totalRawContactsCount, int totalContactsCount) {
        return new ContactListFilter(filterType, null, null, null, null, -1, -1, totalRawContactsCount, totalContactsCount);
    }

    public static ContactListFilter createAccountFilter(String accountType, String accountName, String dataSet, Drawable icon) {
        return new ContactListFilter(0, accountType, accountName, dataSet, icon);
    }

    public static ContactListFilter createAccountFilter(String accountType, String accountName, String dataSet, Drawable icon, int totalRawContactsCount, int totalContactsCount, int totalRawContactsCountInAllAccounts, int totalContactsCountInAllAccounts) {
        return new ContactListFilter(0, accountType, accountName, dataSet, icon, totalRawContactsCount, totalContactsCount, totalRawContactsCountInAllAccounts, totalContactsCountInAllAccounts);
    }

    public String toString() {
        switch (this.filterType) {
            case -21:
                return "add_company_members";
            case -14:
                return "export_contacts";
            case -11:
                return "add_members_to_group";
            case -10:
                return "remove_from_favorits";
            case -9:
                return "add_to_favorits";
            case -8:
                return "copy_from _sim";
            case -7:
                return "copy_contacts";
            case -6:
                return "single";
            case -5:
                return "with_phones";
            case -4:
                return "starred";
            case -3:
                return "custom";
            case -2:
                return "all_accounts";
            case -1:
                return "default";
            case 0:
                return "account: " + this.accountType + (this.dataSet != null ? "/" + this.dataSet : "") + HwCustPreloadContacts.EMPTY_STRING + this.accountName;
            default:
                return super.toString();
        }
    }

    public int compareTo(ContactListFilter another) {
        int res = this.accountName.compareTo(another.accountName);
        if (res != 0) {
            return res;
        }
        res = this.accountType.compareTo(another.accountType);
        if (res != 0) {
            return res;
        }
        return this.filterType - another.filterType;
    }

    public int hashCode() {
        int code = this.filterType;
        if (this.accountType != null) {
            code = (((code * 31) + this.accountType.hashCode()) * 31) + this.accountName.hashCode();
        }
        if (this.dataSet != null) {
            return (code * 31) + this.dataSet.hashCode();
        }
        return code;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ContactListFilter)) {
            return false;
        }
        ContactListFilter otherFilter = (ContactListFilter) other;
        return this.filterType == otherFilter.filterType && TextUtils.equals(this.accountName, otherFilter.accountName) && TextUtils.equals(this.accountType, otherFilter.accountType) && TextUtils.equals(this.dataSet, otherFilter.dataSet);
    }

    public static void storeToPreferences(SharedPreferences prefs, ContactListFilter filter) {
        String str = null;
        if (filter == null || filter.filterType != -6) {
            Editor putString = prefs.edit().putInt("filter.type", filter == null ? -1 : filter.filterType).putString("filter.accountName", filter == null ? null : filter.accountName).putString("filter.accountType", filter == null ? null : filter.accountType);
            String str2 = "filter.dataSet";
            if (filter != null) {
                str = filter.dataSet;
            }
            putString.putString(str2, str).apply();
        }
    }

    public static ContactListFilter restoreDefaultPreferences(SharedPreferences prefs) {
        ContactListFilter filter = restoreFromPreferences(prefs);
        if (filter == null) {
            filter = createFilterWithType(-2);
        }
        if (filter.filterType == 1 || filter.filterType == -6) {
            return createFilterWithType(-2);
        }
        return filter;
    }

    public static ContactListFilter restoreFromPreferences(SharedPreferences prefs) {
        int filterType = prefs.getInt("filter.type", -1);
        if (filterType == -1) {
            return null;
        }
        return new ContactListFilter(filterType, prefs.getString("filter.accountType", null), prefs.getString("filter.accountName", null), prefs.getString("filter.dataSet", null), null);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.filterType);
        dest.writeString(this.accountName);
        dest.writeString(this.accountType);
        dest.writeString(this.dataSet);
    }

    public int describeContents() {
        return 0;
    }

    public String getId() {
        if (this.mId == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.filterType);
            if (this.accountType != null) {
                sb.append('-').append(this.accountType);
            }
            if (this.dataSet != null) {
                sb.append('/').append(this.dataSet);
            }
            if (this.accountName != null) {
                sb.append('-').append(this.accountName.replace('-', '_'));
            }
            this.mId = sb.toString();
        }
        return this.mId;
    }

    public Builder addAccountQueryParameterToUrl(Builder uriBuilder) {
        if (this.filterType != 0) {
            throw new IllegalStateException("filterType must be FILTER_TYPE_ACCOUNT");
        }
        uriBuilder.appendQueryParameter("account_name", this.accountName);
        uriBuilder.appendQueryParameter("account_type", this.accountType);
        if (!TextUtils.isEmpty(this.dataSet)) {
            uriBuilder.appendQueryParameter("data_set", this.dataSet);
        }
        return uriBuilder;
    }

    public static ContactListFilter createFromIntent(int filterType, Intent intent) {
        if (filterType == -1) {
            return null;
        }
        return new ContactListFilter(filterType, intent.getStringExtra("extra_account_type"), intent.getStringExtra("extra_account_name"), intent.getStringExtra("extra_account_data_set"), null, intent.getLongExtra("extra_group_id", -1), intent.getLongArrayExtra("selected_members_raw_contact_ids"), intent.getLongArrayExtra("removed_members_raw_contact_ids"), intent.getBooleanExtra("result_required_back", false));
    }

    public boolean isFilterTypeSame(int filterType) {
        return this.filterType == filterType;
    }
}
