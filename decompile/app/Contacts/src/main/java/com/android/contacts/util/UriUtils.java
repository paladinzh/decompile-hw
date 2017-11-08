package com.android.contacts.util;

import android.net.Uri;
import java.util.List;

public class UriUtils {
    private UriUtils() {
    }

    public static boolean areEqual(Uri uri1, Uri uri2) {
        if (uri1 == null && uri2 == null) {
            return true;
        }
        if (uri1 == null || uri2 == null) {
            return false;
        }
        return uri1.equals(uri2);
    }

    public static Uri parseUriOrNull(String uriString) {
        if (uriString == null) {
            return null;
        }
        return Uri.parse(uriString);
    }

    public static String uriToString(Uri uri) {
        return uri == null ? null : uri.toString();
    }

    public static boolean isEncodedContactUri(Uri uri) {
        return uri.getLastPathSegment().equals("encoded");
    }

    public static String getLookupKeyFromUri(Uri lookupUri) {
        String str = null;
        if (lookupUri == null || isEncodedContactUri(lookupUri)) {
            return null;
        }
        List<String> segments = lookupUri.getPathSegments();
        if (segments.size() >= 3) {
            str = Uri.encode((String) segments.get(2));
        }
        return str;
    }
}
