package com.fyusion.sdk.viewer.internal.d;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class a {
    private final Context a;

    public a(Context context) {
        this.a = context;
    }

    private static b a(String str) {
        Object newInstance;
        try {
            try {
                newInstance = Class.forName(str).newInstance();
                if (newInstance instanceof b) {
                    return (b) newInstance;
                }
                throw new RuntimeException("Expected instanceof ViewerModule, but found: " + newInstance);
            } catch (Throwable e) {
                throw new RuntimeException("Unable to instantiate ViewerModule implementation for " + newInstance, e);
            }
        } catch (Throwable e2) {
            throw new IllegalArgumentException("Unable to find ViewerModule implementation", e2);
        }
    }

    public List<b> a() {
        if (Log.isLoggable("ManifestParser", 3)) {
            Log.d("ManifestParser", "Loading Viewer modules");
        }
        List<b> arrayList = new ArrayList();
        try {
            ApplicationInfo applicationInfo = this.a.getPackageManager().getApplicationInfo(this.a.getPackageName(), 128);
            if (applicationInfo.metaData == null) {
                return arrayList;
            }
            for (String str : applicationInfo.metaData.keySet()) {
                if ("ViewerModule".equals(applicationInfo.metaData.get(str))) {
                    arrayList.add(a(str));
                    if (Log.isLoggable("ManifestParser", 3)) {
                        Log.d("ManifestParser", "Loaded Viewer module: " + str);
                    }
                }
            }
            if (Log.isLoggable("ManifestParser", 3)) {
                Log.d("ManifestParser", "Finished loading Viewer modules");
            }
            return arrayList;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to find metadata to parse ViewerModules", e);
        }
    }
}
