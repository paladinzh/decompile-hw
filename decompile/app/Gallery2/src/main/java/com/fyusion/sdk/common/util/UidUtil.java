package com.fyusion.sdk.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public class UidUtil {
    private static final List<String> a = Arrays.asList(new String[]{"http", "https"});

    private static void a(boolean z, String str) {
        if (!z) {
            throw new IllegalArgumentException(str);
        }
    }

    public static String parse(String str) {
        return parse(URI.create(str));
    }

    public static String parse(URI uri) {
        boolean z = true;
        boolean z2 = uri.getScheme() != null && a.contains(uri.getScheme().toLowerCase());
        a(z2, "Invalid scheme: " + uri.getScheme());
        a("fyu.se".equals(uri.getHost()), "Invalid host: " + uri.getHost());
        if (uri.getPath() != null) {
            if (!uri.getPath().startsWith("/v/")) {
            }
            a(z, "Invalid path: " + uri.getPath());
            return uri.getPath().substring(3);
        }
        z = false;
        a(z, "Invalid path: " + uri.getPath());
        return uri.getPath().substring(3);
    }

    public static String parse(URL url) throws URISyntaxException {
        return parse(url.toURI());
    }
}
