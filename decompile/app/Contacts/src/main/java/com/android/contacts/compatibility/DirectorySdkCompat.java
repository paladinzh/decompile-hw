package com.android.contacts.compatibility;

import android.net.Uri;
import android.provider.ContactsContract.Directory;

public class DirectorySdkCompat {
    public static final Uri ENTERPRISE_CONTENT_URI = Directory.ENTERPRISE_CONTENT_URI;

    public static boolean isRemoteDirectory(long directoryId) {
        return Directory.isRemoteDirectory(directoryId);
    }

    public static boolean isEnterpriseDirectoryId(long directoryId) {
        return Directory.isEnterpriseDirectoryId(directoryId);
    }
}
