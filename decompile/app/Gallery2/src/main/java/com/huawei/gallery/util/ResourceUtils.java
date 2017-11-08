package com.huawei.gallery.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.util.GalleryLog;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class ResourceUtils {
    private static HashMap<Integer, SoftReference<Drawable>> sDrawableCache = new HashMap();
    private static ResourceTexture sInner;
    private static ResourceTexture sOuter;

    public static ResourceTexture getOuterTexture(Context context) {
        if (sOuter == null) {
            sOuter = new ResourceTexture(context, R.drawable.btn_default_normal_emui);
        }
        return sOuter;
    }

    public static ResourceTexture getInnerTexture(Context context) {
        if (sInner == null) {
            sInner = new ResourceTexture(context, R.drawable.btn_default_normal);
        }
        return sInner;
    }

    public static Drawable getDrawable(Resources res, Integer drawId) {
        Drawable drawable;
        SoftReference<Drawable> drawableWeakReference = (SoftReference) sDrawableCache.get(drawId);
        if (drawableWeakReference != null) {
            drawable = (Drawable) drawableWeakReference.get();
            GalleryLog.d("ResourceUtils", "cached drawable: " + drawable);
            if (drawable != null) {
                return drawable;
            }
        }
        drawable = res.getDrawable(drawId.intValue());
        sDrawableCache.put(drawId, new SoftReference(drawable));
        return drawable;
    }
}
