package com.android.contacts.compatibility;

import android.net.Uri;
import android.provider.ContactsContract.Directory;
import com.android.contacts.ContactsUtils;

public class DirectoryCompat {
    public static Uri getContentUri() {
        if (ContactsUtils.FLAG_N_FEATURE) {
            return DirectorySdkCompat.ENTERPRISE_CONTENT_URI;
        }
        return Directory.CONTENT_URI;
    }

    public static boolean isInvisibleDirectory(long directoryId) {
        boolean z = true;
        if (ContactsUtils.FLAG_N_FEATURE) {
            if (!(directoryId == 1 || directoryId == 1000000001)) {
                z = false;
            }
            return z;
        }
        if (directoryId != 1) {
            z = false;
        }
        return z;
    }

    public static boolean isRemoteDirectory(long directoryId) {
        boolean z = false;
        if (ContactsUtils.FLAG_N_FEATURE) {
            return DirectorySdkCompat.isRemoteDirectory(directoryId);
        }
        if (!(directoryId == 0 || directoryId == 1)) {
            z = true;
        }
        return z;
    }

    public static boolean isEnterpriseDirectoryId(long directoryId) {
        if (ContactsUtils.FLAG_N_FEATURE) {
            return DirectorySdkCompat.isEnterpriseDirectoryId(directoryId);
        }
        return false;
    }
}
