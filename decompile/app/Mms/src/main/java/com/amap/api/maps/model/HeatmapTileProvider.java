package com.amap.api.maps.model;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.mapcore.util.ay;
import com.amap.api.maps.AMapException;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class HeatmapTileProvider implements TileProvider {
    public static final Gradient DEFAULT_GRADIENT = new Gradient(a, b);
    public static final double DEFAULT_OPACITY = 0.6d;
    public static final int DEFAULT_RADIUS = 12;
    private static final int[] a = new int[]{Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)};
    private static final float[] b = new float[]{0.2f, ContentUtil.FONT_SIZE_NORMAL};
    private c c;
    private Collection<WeightedLatLng> d;
    private ay e;
    private int f;
    private Gradient g;
    private int[] h;
    private double[] i;
    private double j;
    private double[] k;

    public static class Builder {
        private Collection<WeightedLatLng> a;
        private int b = 12;
        private Gradient c = HeatmapTileProvider.DEFAULT_GRADIENT;
        private double d = HeatmapTileProvider.DEFAULT_OPACITY;

        public Builder data(Collection<LatLng> collection) {
            return weightedData(HeatmapTileProvider.d(collection));
        }

        public Builder weightedData(Collection<WeightedLatLng> collection) {
            this.a = collection;
            return this;
        }

        public Builder radius(int i) {
            this.b = Math.max(10, Math.min(i, 50));
            return this;
        }

        public Builder gradient(Gradient gradient) {
            this.c = gradient;
            return this;
        }

        public Builder transparency(double d) {
            this.d = Math.max(0.0d, Math.min(d, WeightedLatLng.DEFAULT_INTENSITY));
            return this;
        }

        public HeatmapTileProvider build() {
            if (this.a != null && this.a.size() != 0) {
                return new HeatmapTileProvider();
            }
            try {
                throw new AMapException("No input points.");
            } catch (AMapException e) {
                Log.e(MapTilsCacheAndResManager.AUTONAVI_PATH, e.getErrorMessage());
                e.printStackTrace();
                return null;
            }
        }
    }

    private HeatmapTileProvider(Builder builder) {
        this.d = builder.a;
        this.f = builder.b;
        this.g = builder.c;
        if (this.g == null || !this.g.isAvailable()) {
            this.g = DEFAULT_GRADIENT;
        }
        this.j = builder.d;
        this.i = a(this.f, ((double) this.f) / 3.0d);
        a(this.g);
        c(this.d);
    }

    private void c(Collection<WeightedLatLng> collection) {
        Collection arrayList = new ArrayList();
        for (WeightedLatLng weightedLatLng : collection) {
            if (weightedLatLng.latLng.latitude < 85.0d && weightedLatLng.latLng.latitude > -85.0d) {
                arrayList.add(weightedLatLng);
            }
        }
        this.d = arrayList;
        this.e = a(this.d);
        this.c = new c(this.e);
        for (WeightedLatLng weightedLatLng2 : this.d) {
            this.c.a(weightedLatLng2);
        }
        this.k = a(this.f);
    }

    private static Collection<WeightedLatLng> d(Collection<LatLng> collection) {
        Collection arrayList = new ArrayList();
        for (LatLng weightedLatLng : collection) {
            arrayList.add(new WeightedLatLng(weightedLatLng));
        }
        return arrayList;
    }

    public Tile getTile(int i, int i2, int i3) {
        Collection a;
        double d;
        ay ayVar;
        Collection<WeightedLatLng> a2;
        double[][] dArr;
        DPoint point;
        int i4;
        int i5;
        double[] dArr2;
        double pow = WeightedLatLng.DEFAULT_INTENSITY / Math.pow(2.0d, (double) i3);
        double d2 = (((double) this.f) * pow) / 256.0d;
        double d3 = ((2.0d * d2) + pow) / ((double) ((this.f * 2) + 256));
        double d4 = (((double) i) * pow) - d2;
        double d5 = (((double) (i + 1)) * pow) + d2;
        double d6 = (((double) i2) * pow) - d2;
        double d7 = (pow * ((double) (i2 + 1))) + d2;
        double d8 = 0.0d;
        Collection arrayList = new ArrayList();
        if (d4 < 0.0d) {
            d8 = -1.0d;
            arrayList = this.c.a(new ay(WeightedLatLng.DEFAULT_INTENSITY + d4, WeightedLatLng.DEFAULT_INTENSITY, d6, d7));
        } else if (d5 > WeightedLatLng.DEFAULT_INTENSITY) {
            a = this.c.a(new ay(0.0d, d5 - WeightedLatLng.DEFAULT_INTENSITY, d6, d7));
            d = WeightedLatLng.DEFAULT_INTENSITY;
            ayVar = new ay(d4, d5, d6, d7);
            if (ayVar.a(new ay(this.e.a - d2, this.e.c + d2, this.e.b - d2, d2 + this.e.d))) {
                return TileProvider.NO_TILE;
            }
            a2 = this.c.a(ayVar);
            if (!a2.isEmpty()) {
                return TileProvider.NO_TILE;
            }
            int i6 = (this.f * 2) + 256;
            int i7 = (this.f * 2) + 256;
            dArr = (double[][]) Array.newInstance(Double.TYPE, new int[]{i6, i7});
            for (WeightedLatLng weightedLatLng : a2) {
                point = weightedLatLng.getPoint();
                i4 = (int) ((point.x - d4) / d3);
                i5 = (int) ((point.y - d6) / d3);
                dArr2 = dArr[i4];
                dArr2[i5] = dArr2[i5] + weightedLatLng.intensity;
            }
            for (WeightedLatLng weightedLatLng2 : r20) {
                point = weightedLatLng2.getPoint();
                i4 = (int) (((point.x + d) - d4) / d3);
                i5 = (int) ((point.y - d6) / d3);
                dArr2 = dArr[i4];
                dArr2[i5] = dArr2[i5] + weightedLatLng2.intensity;
            }
            return a(a(a(dArr, this.i), this.h, this.k[i3]));
        }
        a = arrayList;
        d = d8;
        ayVar = new ay(d4, d5, d6, d7);
        if (ayVar.a(new ay(this.e.a - d2, this.e.c + d2, this.e.b - d2, d2 + this.e.d))) {
            return TileProvider.NO_TILE;
        }
        a2 = this.c.a(ayVar);
        if (!a2.isEmpty()) {
            return TileProvider.NO_TILE;
        }
        int i62 = (this.f * 2) + 256;
        int i72 = (this.f * 2) + 256;
        dArr = (double[][]) Array.newInstance(Double.TYPE, new int[]{i62, i72});
        for (WeightedLatLng weightedLatLng22 : a2) {
            point = weightedLatLng22.getPoint();
            i4 = (int) ((point.x - d4) / d3);
            i5 = (int) ((point.y - d6) / d3);
            dArr2 = dArr[i4];
            dArr2[i5] = dArr2[i5] + weightedLatLng22.intensity;
        }
        for (WeightedLatLng weightedLatLng222 : r20) {
            point = weightedLatLng222.getPoint();
            i4 = (int) (((point.x + d) - d4) / d3);
            i5 = (int) ((point.y - d6) / d3);
            dArr2 = dArr[i4];
            dArr2[i5] = dArr2[i5] + weightedLatLng222.intensity;
        }
        return a(a(a(dArr, this.i), this.h, this.k[i3]));
    }

    private void a(Gradient gradient) {
        this.g = gradient;
        this.h = gradient.generateColorMap(this.j);
    }

    private double[] a(int i) {
        int i2 = 11;
        double[] dArr = new double[21];
        for (int i3 = 5; i3 < 11; i3++) {
            dArr[i3] = a(this.d, this.e, i, (int) (Math.pow(2.0d, (double) i3) * 1280.0d));
            if (i3 == 5) {
                for (int i4 = 0; i4 < i3; i4++) {
                    dArr[i4] = dArr[i3];
                }
            }
        }
        while (i2 < 21) {
            dArr[i2] = dArr[10];
            i2++;
        }
        return dArr;
    }

    private static Tile a(Bitmap bitmap) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, byteArrayOutputStream);
        return new Tile(256, 256, byteArrayOutputStream.toByteArray());
    }

    static ay a(Collection<WeightedLatLng> collection) {
        Iterator it = collection.iterator();
        WeightedLatLng weightedLatLng = (WeightedLatLng) it.next();
        double d = weightedLatLng.getPoint().x;
        double d2 = weightedLatLng.getPoint().x;
        double d3 = weightedLatLng.getPoint().y;
        double d4 = weightedLatLng.getPoint().y;
        while (it.hasNext()) {
            weightedLatLng = (WeightedLatLng) it.next();
            double d5 = weightedLatLng.getPoint().x;
            double d6 = weightedLatLng.getPoint().y;
            if (d5 < d) {
                d = d5;
            }
            if (d5 > d2) {
                d2 = d5;
            }
            if (d6 < d3) {
                d3 = d6;
            }
            if (d6 > d4) {
                d4 = d6;
            }
        }
        return new ay(d, d2, d3, d4);
    }

    static double[] a(int i, double d) {
        double[] dArr = new double[((i * 2) + 1)];
        for (int i2 = -i; i2 <= i; i2++) {
            dArr[i2 + i] = Math.exp(((double) ((-i2) * i2)) / ((2.0d * d) * d));
        }
        return dArr;
    }

    static double[][] a(double[][] dArr, double[] dArr2) {
        int i;
        int i2;
        int i3;
        int i4;
        int floor = (int) Math.floor(((double) dArr2.length) / 2.0d);
        int length = dArr.length;
        int i5 = (floor + (length - (floor * 2))) - 1;
        double[][] dArr3 = (double[][]) Array.newInstance(Double.TYPE, new int[]{length, length});
        for (i = 0; i < length; i++) {
            for (i2 = 0; i2 < length; i2++) {
                double d = dArr[i][i2];
                if (d != 0.0d) {
                    int i6;
                    if (i5 >= i + floor) {
                        i6 = i + floor;
                    } else {
                        i6 = i5;
                    }
                    i3 = i6 + 1;
                    if (floor <= i - floor) {
                        i6 = i - floor;
                    } else {
                        i6 = floor;
                    }
                    while (i6 < i3) {
                        double[] dArr4 = dArr3[i6];
                        dArr4[i2] = dArr4[i2] + (dArr2[i6 - (i - floor)] * d);
                        i6++;
                    }
                }
            }
        }
        double[][] dArr5 = (double[][]) Array.newInstance(Double.TYPE, new int[]{i4, i4});
        for (i4 = floor; i4 < i5 + 1; i4++) {
            for (i = 0; i < length; i++) {
                d = dArr3[i4][i];
                if (d != 0.0d) {
                    if (i5 >= i + floor) {
                        i2 = i + floor;
                    } else {
                        i2 = i5;
                    }
                    i3 = i2 + 1;
                    if (floor <= i - floor) {
                        i2 = i - floor;
                    } else {
                        i2 = floor;
                    }
                    while (i2 < i3) {
                        dArr4 = dArr5[i4 - floor];
                        int i7 = i2 - floor;
                        dArr4[i7] = dArr4[i7] + (dArr2[i2 - (i - floor)] * d);
                        i2++;
                    }
                }
            }
        }
        return dArr5;
    }

    static Bitmap a(double[][] dArr, int[] iArr, double d) {
        int i = iArr[iArr.length - 1];
        double length = ((double) (iArr.length - 1)) / d;
        int length2 = dArr.length;
        int[] iArr2 = new int[(length2 * length2)];
        for (int i2 = 0; i2 < length2; i2++) {
            for (int i3 = 0; i3 < length2; i3++) {
                double d2 = dArr[i3][i2];
                int i4 = (i2 * length2) + i3;
                int i5 = (int) (d2 * length);
                if (d2 == 0.0d) {
                    iArr2[i4] = 0;
                } else if (i5 >= iArr.length) {
                    iArr2[i4] = i;
                } else {
                    iArr2[i4] = iArr[i5];
                }
            }
        }
        Bitmap createBitmap = Bitmap.createBitmap(length2, length2, Config.ARGB_8888);
        createBitmap.setPixels(iArr2, 0, length2, 0, 0, length2, length2);
        return createBitmap;
    }

    static double a(Collection<WeightedLatLng> collection, ay ayVar, int i, int i2) {
        double d = ayVar.a;
        double d2 = ayVar.c;
        double d3 = ayVar.b;
        double d4 = ayVar.d;
        double d5 = ((double) ((int) (((double) (i2 / (i * 2))) + 0.5d))) / (d2 - d > d4 - d3 ? d2 - d : d4 - d3);
        LongSparseArray longSparseArray = new LongSparseArray();
        d4 = 0.0d;
        for (WeightedLatLng weightedLatLng : collection) {
            LongSparseArray longSparseArray2;
            int i3 = (int) ((weightedLatLng.getPoint().x - d) * d5);
            int i4 = (int) ((weightedLatLng.getPoint().y - d3) * d5);
            LongSparseArray longSparseArray3 = (LongSparseArray) longSparseArray.get((long) i3);
            if (longSparseArray3 != null) {
                longSparseArray2 = longSparseArray3;
            } else {
                longSparseArray3 = new LongSparseArray();
                longSparseArray.put((long) i3, longSparseArray3);
                longSparseArray2 = longSparseArray3;
            }
            Double d6 = (Double) longSparseArray2.get((long) i4);
            if (d6 == null) {
                d6 = Double.valueOf(0.0d);
            }
            Double valueOf = Double.valueOf(weightedLatLng.intensity + d6.doubleValue());
            longSparseArray2.put((long) i4, valueOf);
            if (valueOf.doubleValue() > d4) {
                d2 = valueOf.doubleValue();
            } else {
                d2 = d4;
            }
            d4 = d2;
        }
        return d4;
    }

    public int getTileHeight() {
        return 256;
    }

    public int getTileWidth() {
        return 256;
    }
}
