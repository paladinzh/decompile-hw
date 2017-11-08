package com.huawei.cspcommon.util;

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class SearchContract$DataSearch implements BaseColumns {
    public static final Uri EMAIL_CONTENT_FILTER_URI = Email.CONTENT_FILTER_URI;
    public static final Uri PHONE_CONTENT_FILTER_URI = Phone.CONTENT_FILTER_URI;

    private SearchContract$DataSearch() {
    }
}
