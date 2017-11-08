package com.android.contacts.model.account;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.google.common.base.Objects;

public class AccountTypeWithDataSet {
    private static final String[] ID_PROJECTION = new String[]{"_id"};
    private static final Uri RAW_CONTACTS_URI_LIMIT_1 = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("limit", CallInterceptDetails.BRANDED_STATE).build();
    public final String accountType;
    public final String dataSet;

    private AccountTypeWithDataSet(String accountType, String dataSet) {
        String str = null;
        if (TextUtils.isEmpty(accountType)) {
            accountType = null;
        }
        this.accountType = accountType;
        if (!TextUtils.isEmpty(dataSet)) {
            str = dataSet;
        }
        this.dataSet = str;
    }

    public static AccountTypeWithDataSet get(String accountType, String dataSet) {
        return new AccountTypeWithDataSet(accountType, dataSet);
    }

    public boolean hasData(Context context) {
        String selection;
        String[] args;
        String BASE_SELECTION = "account_type = ?";
        if (TextUtils.isEmpty(this.dataSet)) {
            selection = "account_type = ? AND data_set IS NULL";
            args = new String[]{this.accountType};
        } else {
            selection = "account_type = ? AND data_set = ?";
            args = new String[]{this.accountType, this.dataSet};
        }
        Cursor c = context.getContentResolver().query(RAW_CONTACTS_URI_LIMIT_1, ID_PROJECTION, selection, args, null);
        if (c == null) {
            return false;
        }
        try {
            boolean moveToFirst = c.moveToFirst();
            return moveToFirst;
        } finally {
            c.close();
        }
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof AccountTypeWithDataSet)) {
            return false;
        }
        AccountTypeWithDataSet other = (AccountTypeWithDataSet) o;
        if (Objects.equal(this.accountType, other.accountType)) {
            z = Objects.equal(this.dataSet, other.dataSet);
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = this.accountType == null ? 0 : this.accountType.hashCode();
        if (this.dataSet != null) {
            i = this.dataSet.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "[" + this.accountType + "/" + this.dataSet + "]";
    }
}
