package com.android.contacts.compatibility;

import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.ContactsUtils;

public class ContactsCompat {
    private static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(Contacts.CONTENT_URI, "filter_enterprise");

    private ContactsCompat() {
    }

    public static Uri getContentUri() {
        if (ContactsUtils.FLAG_N_FEATURE) {
            return ENTERPRISE_CONTENT_FILTER_URI;
        }
        return Contacts.CONTENT_FILTER_URI;
    }

    public static boolean isEnterpriseContactId(long contactId) {
        boolean z = false;
        if (CompatUtils.isLollipopCompatible()) {
            return Contacts.isEnterpriseContactId(contactId);
        }
        if (contactId >= 1000000000 && contactId < 9223372034707292160L) {
            z = true;
        }
        return z;
    }
}
