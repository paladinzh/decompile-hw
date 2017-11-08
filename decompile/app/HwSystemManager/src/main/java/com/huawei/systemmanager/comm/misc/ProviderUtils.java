package com.huawei.systemmanager.comm.misc;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class ProviderUtils {
    public static int deleteAll(Context ctx, Uri uri) {
        return deleteAll(ctx.getContentResolver(), uri);
    }

    public static int deleteAll(ContentResolver resolver, Uri uri) {
        return resolver.delete(uri, null, null);
    }
}
