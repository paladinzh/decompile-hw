package com.android.dialer.greeting;

import android.net.Uri;
import android.provider.BaseColumns;

public final class GreetingContract$Greetings implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.voicemail/greeting");

    public static Uri buildSourceUri(String packageName) {
        return CONTENT_URI.buildUpon().appendQueryParameter("source_package", packageName).build();
    }
}
