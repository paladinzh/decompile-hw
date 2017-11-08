package com.amap.api.maps.model;

import android.graphics.Color;
import android.util.Log;
import com.amap.api.maps.AMapException;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.util.HashMap;

public class Gradient {
    private int a;
    private int[] b;
    private float[] c;
    private boolean d;

    private class a {
        final /* synthetic */ Gradient a;
        private final int b;
        private final int c;
        private final float d;

        private a(Gradient gradient, int i, int i2, float f) {
            this.a = gradient;
            this.b = i;
            this.c = i2;
            this.d = f;
        }
    }

    public Gradient(int[] iArr, float[] fArr) {
        this(iArr, fArr, 1000);
    }

    private Gradient(int[] iArr, float[] fArr, int i) {
        int i2 = 1;
        this.d = true;
        if (iArr == null || fArr == null) {
            try {
                throw new AMapException("colors and startPoints should not be null");
            } catch (AMapException e) {
                this.d = false;
                Log.e(MapTilsCacheAndResManager.AUTONAVI_PATH, e.getErrorMessage());
                e.printStackTrace();
            }
        } else if (iArr.length != fArr.length) {
            throw new AMapException("colors and startPoints should be same length");
        } else if (iArr.length != 0) {
            while (i2 < fArr.length) {
                if (fArr[i2] <= fArr[i2 - 1]) {
                    throw new AMapException("startPoints should be in increasing order");
                }
                i2++;
            }
            this.a = i;
            this.b = new int[iArr.length];
            this.c = new float[fArr.length];
            System.arraycopy(iArr, 0, this.b, 0, iArr.length);
            System.arraycopy(fArr, 0, this.c, 0, fArr.length);
            this.d = true;
        } else {
            throw new AMapException("No colors have been defined");
        }
    }

    private HashMap<Integer, a> a() {
        HashMap<Integer, a> hashMap = new HashMap();
        if (this.c[0] != 0.0f) {
            int argb = Color.argb(0, Color.red(this.b[0]), Color.green(this.b[0]), Color.blue(this.b[0]));
            hashMap.put(Integer.valueOf(0), new a(argb, this.b[0], this.c[0] * ((float) this.a)));
        }
        for (int i = 1; i < this.b.length; i++) {
            hashMap.put(Integer.valueOf((int) (((float) this.a) * this.c[i - 1])), new a(this.b[i - 1], this.b[i], (this.c[i] - this.c[i - 1]) * ((float) this.a)));
        }
        if (this.c[this.c.length - 1] != 1.0f) {
            int length = this.c.length - 1;
            hashMap.put(Integer.valueOf((int) (((float) this.a) * this.c[length])), new a(this.b[length], this.b[length], ((float) this.a) * (1.0f - this.c[length])));
        }
        return hashMap;
    }

    protected int[] generateColorMap(double d) {
        int i = 0;
        HashMap a = a();
        int[] iArr = new int[this.a];
        int i2 = 0;
        a aVar = (a) a.get(Integer.valueOf(0));
        int i3 = 0;
        while (i2 < this.a) {
            int i4;
            a aVar2;
            if (a.containsKey(Integer.valueOf(i2))) {
                i4 = i2;
                aVar2 = (a) a.get(Integer.valueOf(i2));
            } else {
                aVar2 = aVar;
                i4 = i3;
            }
            iArr[i2] = a(aVar2.b, aVar2.c, ((float) (i2 - i4)) / aVar2.d);
            i2++;
            i3 = i4;
            aVar = aVar2;
        }
        if (d != WeightedLatLng.DEFAULT_INTENSITY) {
            while (i < this.a) {
                i3 = iArr[i];
                iArr[i] = Color.argb((int) (((double) Color.alpha(i3)) * d), Color.red(i3), Color.green(i3), Color.blue(i3));
                i++;
            }
        }
        return iArr;
    }

    static int a(int i, int i2, float f) {
        int i3 = 0;
        int alpha = (int) ((((float) (Color.alpha(i2) - Color.alpha(i))) * f) + ((float) Color.alpha(i)));
        float[] fArr = new float[3];
        Color.RGBToHSV(Color.red(i), Color.green(i), Color.blue(i), fArr);
        float[] fArr2 = new float[3];
        Color.RGBToHSV(Color.red(i2), Color.green(i2), Color.blue(i2), fArr2);
        if (fArr[0] - fArr2[0] > 180.0f) {
            fArr2[0] = fArr2[0] + 360.0f;
        } else if (fArr2[0] - fArr[0] > 180.0f) {
            fArr[0] = fArr[0] + 360.0f;
        }
        float[] fArr3 = new float[3];
        while (i3 < 3) {
            fArr3[i3] = ((fArr2[i3] - fArr[i3]) * f) + fArr[i3];
            i3++;
        }
        return Color.HSVToColor(alpha, fArr3);
    }

    protected boolean isAvailable() {
        return this.d;
    }
}
