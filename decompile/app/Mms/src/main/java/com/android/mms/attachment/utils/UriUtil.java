package com.android.mms.attachment.utils;

import android.net.Uri;
import android.text.TextUtils;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class UriUtil {
    private static final HashSet<String> SMS_MMS_SCHEMES = new HashSet(Arrays.asList(new String[]{"sms", "mms", "smsto", "smsto"}));
    private static final HashSet<String> SUPPORTED_SCHEME = new HashSet(Arrays.asList(new String[]{"android.resource", "content", "file", "bugle"}));

    public static Uri getUriForResourceFile(String path) {
        return TextUtils.isEmpty(path) ? null : Uri.fromFile(new File(path));
    }

    public static String getFilePathFromUri(Uri uri) {
        if (isFileUri(uri)) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isFileUri(Uri uri) {
        return uri != null ? TextUtils.equals(uri.getScheme(), "file") : false;
    }

    public static boolean isLocalUri(Uri uri) {
        return SUPPORTED_SCHEME.contains(uri.getScheme());
    }
}
