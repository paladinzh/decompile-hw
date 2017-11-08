package com.amap.api.mapcore.util;

import android.content.Context;
import com.autonavi.amap.mapcore.VirtualEarthProjection;

/* compiled from: ScaleRotateGestureDetector */
public class an extends am {

    /* compiled from: ScaleRotateGestureDetector */
    public static abstract class a implements com.amap.api.mapcore.util.am.a {
        public abstract boolean a(an anVar);

        public abstract boolean b(an anVar);

        public abstract void c(an anVar);

        public boolean a(am amVar) {
            return a((an) amVar);
        }

        public boolean b(am amVar) {
            return b((an) amVar);
        }

        public void c(am amVar) {
            c((an) amVar);
        }
    }

    public an(Context context, a aVar) {
        super(context, aVar);
    }

    public float j() {
        return (float) (((Math.atan2((double) h(), (double) g()) - Math.atan2((double) e(), (double) d())) * VirtualEarthProjection.MaxLongitude) / 3.141592653589793d);
    }
}
