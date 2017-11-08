package com.amap.api.mapcore.util;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;

/* compiled from: ResourcesUtil */
public class bh {
    private static boolean a;

    static {
        a = false;
        a = new File("/system/framework/amap.jar").exists();
    }

    public static AssetManager a(Context context) {
        if (context == null) {
            return null;
        }
        AssetManager assets = context.getAssets();
        if (a) {
            try {
                assets.getClass().getDeclaredMethod("addAssetPath", new Class[]{String.class}).invoke(assets, new Object[]{"/system/framework/amap.jar"});
            } catch (Throwable th) {
                ce.a(th, "ResourcesUtil", "getSelfAssets");
            }
        }
        return assets;
    }
}
