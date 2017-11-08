package com.android.mms.directory;

import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;

public class DirectoryQuery {
    public static final Uri ENTERPRISE_CONTENT_FILTER_URI = Uri.withAppendedPath(Contacts.CONTENT_URI, "filter_enterprise");
    private static final String[] PROJECTION = new String[]{"_id", "packageName", "typeResourceId", "displayName", "photoSupport"};
    private static final String[] PROJECTION_MATCH_ENTERPRISE_CONTACTS = new String[]{"contact_id", "display_name", "data1", "sort_key", "sort_key_alt", "lookup"};
    public static final Uri URI = Directory.ENTERPRISE_CONTENT_URI;

    public static String[] getProjection() {
        return (String[]) PROJECTION.clone();
    }

    public static String[] getEnterpriseProjection() {
        return (String[]) PROJECTION_MATCH_ENTERPRISE_CONTACTS.clone();
    }

    public static boolean isEnterpriseDirectoryId(long directoryId) {
        return Directory.isEnterpriseDirectoryId(directoryId);
    }

    public static boolean isEnterpriseContactId(long contactId) {
        return contactId >= 1000000000 && contactId < 9223372034707292160L;
    }
}
