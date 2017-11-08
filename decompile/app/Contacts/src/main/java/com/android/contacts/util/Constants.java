package com.android.contacts.util;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.ContactsContract;

public class Constants {
    private static boolean IS_EXTRA_HUGE;
    private static boolean IS_HUGE_OR_MORE;
    private static boolean IS_LARGE_OR_MORE;
    public static final Uri STRRED_URL = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/starred").buildUpon().build();

    public static boolean isEXTRA_HUGE() {
        return IS_EXTRA_HUGE;
    }

    public static boolean isFontSizeHugeorMore() {
        return IS_HUGE_OR_MORE;
    }

    public static void updateFontSizeSettings(Context context) {
        Resources res = context.getResources();
        if (res == null) {
            IS_EXTRA_HUGE = false;
            IS_HUGE_OR_MORE = false;
            IS_LARGE_OR_MORE = false;
            return;
        }
        float fontScale = res.getConfiguration().fontScale;
        if (fontScale >= 1.30001f) {
            IS_EXTRA_HUGE = true;
        } else {
            IS_EXTRA_HUGE = false;
        }
        if (fontScale >= 1.20001f) {
            IS_HUGE_OR_MORE = true;
        } else {
            IS_HUGE_OR_MORE = false;
        }
        if (fontScale >= 1.15f) {
            IS_LARGE_OR_MORE = true;
        } else {
            IS_LARGE_OR_MORE = false;
        }
    }
}
