package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.TextView;
import com.amap.api.mapcore.util.fh.a;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.FPoint3;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.autonavi.amap.mapcore.interfaces.IAMap;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Util */
public class eh {
    static final int[] a = new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
    static final double[] b = new double[]{7453.642d, 3742.9905d, 1873.333d, 936.89026d, 468.472d, 234.239d, 117.12d, 58.56d, 29.28d, 14.64d, 7.32d, 3.66d, 1.829d, 0.915d, 0.4575d, 0.228d, 0.1144d};
    private static List<Float> c = new ArrayList(4);
    private static List<Float> d = new ArrayList(4);

    public static Bitmap a(Context context, String str) {
        try {
            InputStream open = ef.a(context).open(str);
            Bitmap decodeStream = BitmapFactory.decodeStream(open);
            open.close();
            return decodeStream;
        } catch (Throwable th) {
            fo.b(th, "Util", "fromAsset");
            return null;
        }
    }

    public static void a(Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }

    public static String a(String str, Object obj) {
        return str + "=" + String.valueOf(obj);
    }

    public static float a(float f, float f2) {
        if (f <= 40.0f) {
            return f;
        }
        if (f2 <= 15.0f) {
            return 40.0f;
        }
        if (f2 <= 16.0f) {
            return 50.0f;
        }
        if (f2 <= 17.0f) {
            return 54.0f;
        }
        if (f2 <= 18.0f) {
            return 57.0f;
        }
        return BitmapDescriptorFactory.HUE_YELLOW;
    }

    public static float a(MapConfig mapConfig, float f) {
        if (f > mapConfig.maxZoomLevel) {
            return mapConfig.maxZoomLevel;
        }
        if (f < mapConfig.minZoomLevel) {
            return mapConfig.minZoomLevel;
        }
        return f;
    }

    public static FloatBuffer a(float[] fArr) {
        try {
            ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
            allocateDirect.order(ByteOrder.nativeOrder());
            FloatBuffer asFloatBuffer = allocateDirect.asFloatBuffer();
            asFloatBuffer.put(fArr);
            asFloatBuffer.position(0);
            return asFloatBuffer;
        } catch (Throwable th) {
            fo.b(th, "Util", "makeFloatBuffer1");
            th.printStackTrace();
            return null;
        }
    }

    public static FloatBuffer a(float[] fArr, FloatBuffer floatBuffer) {
        try {
            floatBuffer.clear();
            floatBuffer.put(fArr);
            floatBuffer.position(0);
            return floatBuffer;
        } catch (Throwable th) {
            fo.b(th, "Util", "makeFloatBuffer2");
            th.printStackTrace();
            return null;
        }
    }

    public static int a(GL10 gl10, Bitmap bitmap) {
        return a(gl10, bitmap, false);
    }

    public static int a(GL10 gl10, Bitmap bitmap, boolean z) {
        return a(gl10, 0, bitmap, z);
    }

    public static int a(GL10 gl10, int i, Bitmap bitmap, boolean z) {
        int b = b(gl10, i, bitmap, z);
        if (bitmap != null) {
            bitmap.recycle();
        }
        return b;
    }

    public static int b(GL10 gl10, int i, Bitmap bitmap, boolean z) {
        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        }
        if (i == 0) {
            int[] iArr = new int[]{0};
            gl10.glGenTextures(1, iArr, 0);
            i = iArr[0];
        }
        gl10.glEnable(3553);
        gl10.glBindTexture(3553, i);
        gl10.glTexParameterf(3553, 10241, 9729.0f);
        gl10.glTexParameterf(3553, 10240, 9729.0f);
        if (z) {
            gl10.glTexParameterf(3553, 10242, 10497.0f);
            gl10.glTexParameterf(3553, 10243, 10497.0f);
        } else {
            gl10.glTexParameterf(3553, 10242, 33071.0f);
            gl10.glTexParameterf(3553, 10243, 33071.0f);
        }
        GLUtils.texImage2D(3553, 0, bitmap, 0);
        gl10.glDisable(3553);
        return i;
    }

    public static int a(int i) {
        int log = (int) (Math.log((double) i) / Math.log(2.0d));
        if ((1 << log) < i) {
            return 1 << (log + 1);
        }
        return 1 << log;
    }

    public static String a(String... strArr) {
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder();
        int length = strArr.length;
        int i2 = 0;
        while (i < length) {
            stringBuilder.append(strArr[i]);
            if (i2 != strArr.length - 1) {
                stringBuilder.append(",");
            }
            i2++;
            i++;
        }
        return stringBuilder.toString();
    }

    public static int a(Object[] objArr) {
        return Arrays.hashCode(objArr);
    }

    public static Bitmap a(Bitmap bitmap, int i, int i2) {
        Bitmap bitmap2 = null;
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        try {
            bitmap2 = Bitmap.createBitmap(i, i2, !bitmap.hasAlpha() ? Config.RGB_565 : Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap2);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return bitmap2;
    }

    public static Bitmap a(Bitmap bitmap, float f) {
        if (bitmap != null) {
            return Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * f), (int) (((float) bitmap.getHeight()) * f), true);
        }
        return null;
    }

    public static String a(Context context) {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return context.getCacheDir().toString() + File.separator;
        }
        File file;
        if (MapsInitializer.sdcardDir == null || MapsInitializer.sdcardDir.equals("")) {
            file = new File(Environment.getExternalStorageDirectory(), MapTilsCacheAndResManager.AUTONAVI_PATH);
        } else {
            file = new File(MapsInitializer.sdcardDir);
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file, MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
        if (!file2.exists()) {
            file2.mkdir();
        }
        return file2.toString() + File.separator;
    }

    public static String b(Context context) {
        String a = a(context);
        if (a == null) {
            return null;
        }
        File file = new File(a, "VMAP2");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.toString() + File.separator;
    }

    public static String b(int i) {
        if (i >= 1000) {
            return (i / 1000) + "km";
        }
        return i + "m";
    }

    public static boolean c(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        State state = activeNetworkInfo.getState();
        if (state == null || state == State.DISCONNECTED || state == State.DISCONNECTING) {
            return false;
        }
        return true;
    }

    public static boolean a() {
        return VERSION.SDK_INT >= 8;
    }

    public static boolean b() {
        return VERSION.SDK_INT >= 9;
    }

    public static boolean c() {
        return VERSION.SDK_INT >= 11;
    }

    public static boolean d() {
        return VERSION.SDK_INT >= 12;
    }

    public static void a(GL10 gl10, int i) {
        gl10.glDeleteTextures(1, new int[]{i}, 0);
    }

    public static String a(InputStream inputStream) {
        try {
            return new String(b(inputStream), "utf-8");
        } catch (Throwable th) {
            fo.b(th, "Util", "decodeAssetResData");
            th.printStackTrace();
            return null;
        }
    }

    public static byte[] b(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[2048];
        while (true) {
            int read = inputStream.read(bArr, 0, 2048);
            if (read == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    public static String a(File file) {
        FileInputStream fileInputStream;
        BufferedReader bufferedReader;
        Throwable e;
        FileInputStream fileInputStream2;
        BufferedReader bufferedReader2 = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            fileInputStream = new FileInputStream(file);
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));
                while (true) {
                    try {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        stringBuffer.append(readLine);
                    } catch (FileNotFoundException e2) {
                        e = e2;
                        fileInputStream2 = fileInputStream;
                    } catch (IOException e3) {
                        e = e3;
                        bufferedReader2 = bufferedReader;
                    } catch (Throwable th) {
                        e = th;
                        bufferedReader2 = bufferedReader;
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e42) {
                                e42.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e5) {
                                e5.printStackTrace();
                            }
                        }
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                bufferedReader = null;
                fileInputStream2 = fileInputStream;
                try {
                    fo.b(e, "Util", "readFile fileNotFound");
                    e.printStackTrace();
                    if (fileInputStream2 != null) {
                        try {
                            fileInputStream2.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e42222) {
                                    e42222.printStackTrace();
                                }
                            }
                        } catch (Throwable th3) {
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e52) {
                                    e52.printStackTrace();
                                }
                            }
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e422222) {
                            e422222.printStackTrace();
                        }
                    }
                    return stringBuffer.toString();
                } catch (Throwable th4) {
                    e = th4;
                    fileInputStream = fileInputStream2;
                    bufferedReader2 = bufferedReader;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e522) {
                            e522.printStackTrace();
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (IOException e5222) {
                                    e5222.printStackTrace();
                                }
                            }
                        } catch (Throwable th5) {
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (IOException e52222) {
                                    e52222.printStackTrace();
                                }
                            }
                        }
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e522222) {
                            e522222.printStackTrace();
                        }
                    }
                    throw e;
                }
            } catch (IOException e7) {
                e = e7;
                try {
                    fo.b(e, "Util", "readFile io");
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e4222222) {
                            e4222222.printStackTrace();
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (IOException e42222222) {
                                    e42222222.printStackTrace();
                                }
                            }
                        } catch (Throwable th6) {
                            if (bufferedReader2 != null) {
                                try {
                                    bufferedReader2.close();
                                } catch (IOException e5222222) {
                                    e5222222.printStackTrace();
                                }
                            }
                        }
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e422222222) {
                            e422222222.printStackTrace();
                        }
                    }
                    return stringBuffer.toString();
                } catch (Throwable th7) {
                    e = th7;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (bufferedReader2 != null) {
                        bufferedReader2.close();
                    }
                    throw e;
                }
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            bufferedReader = null;
            fo.b(e, "Util", "readFile fileNotFound");
            e.printStackTrace();
            if (fileInputStream2 != null) {
                fileInputStream2.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return stringBuffer.toString();
        } catch (IOException e9) {
            e = e9;
            fileInputStream = null;
            fo.b(e, "Util", "readFile io");
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            return stringBuffer.toString();
        } catch (Throwable th8) {
            e = th8;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            throw e;
        }
        return stringBuffer.toString();
    }

    public static boolean a(LatLng latLng, List<LatLng> list) {
        double d = latLng.longitude;
        double d2 = latLng.latitude;
        double d3 = latLng.latitude;
        if (list.size() < 3) {
            return false;
        }
        boolean z;
        if (!((LatLng) list.get(0)).equals(list.get(list.size() - 1))) {
            list.add(list.get(0));
        }
        int i = 0;
        int i2 = 0;
        while (i < list.size() - 1) {
            double d4 = ((LatLng) list.get(i)).longitude;
            double d5 = ((LatLng) list.get(i)).latitude;
            double d6 = ((LatLng) list.get(i + 1)).longitude;
            double d7 = ((LatLng) list.get(i + 1)).latitude;
            if (b(d, d2, d4, d5, d6, d7)) {
                return true;
            }
            int i3 = Math.abs(d7 - d5) < 1.0E-9d ? i2 : !b(d4, d5, d, d2, VirtualEarthProjection.MaxLongitude, d3) ? !b(d6, d7, d, d2, VirtualEarthProjection.MaxLongitude, d3) ? !a(d4, d5, d6, d7, d, d2, (double) VirtualEarthProjection.MaxLongitude, d3) ? i2 : i2 + 1 : d7 > d5 ? i2 + 1 : i2 : d5 > d7 ? i2 + 1 : i2;
            i++;
            i2 = i3;
        }
        if (i2 % 2 == 0) {
            z = false;
        } else {
            z = true;
        }
        return z;
    }

    public static double a(double d, double d2, double d3, double d4, double d5, double d6) {
        return ((d3 - d) * (d6 - d2)) - ((d5 - d) * (d4 - d2));
    }

    public static boolean b(double d, double d2, double d3, double d4, double d5, double d6) {
        return Math.abs(a(d, d2, d3, d4, d5, d6)) < 1.0E-9d && (d - d3) * (d - d5) <= 0.0d && (d2 - d4) * (d2 - d6) <= 0.0d;
    }

    public static boolean a(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        double d9 = ((d3 - d) * (d8 - d6)) - ((d4 - d2) * (d7 - d5));
        if (d9 == 0.0d) {
            return false;
        }
        double d10 = (((d2 - d6) * (d7 - d5)) - ((d - d5) * (d8 - d6))) / d9;
        d9 = (((d2 - d6) * (d3 - d)) - ((d - d5) * (d4 - d2))) / d9;
        if (d10 < 0.0d || d10 > WeightedLatLng.DEFAULT_INTENSITY || d9 < 0.0d || d9 > WeightedLatLng.DEFAULT_INTENSITY) {
            return false;
        }
        return true;
    }

    public static List<FPoint> a(FPoint[] fPointArr, List<FPoint> list, boolean z) {
        List arrayList = new ArrayList();
        List<FPoint> arrayList2 = new ArrayList(list);
        int i = 0;
        while (i < 4) {
            arrayList.clear();
            int size = arrayList2.size();
            int i2 = 0;
            while (true) {
                int i3;
                if (z) {
                    i3 = size;
                } else {
                    i3 = size - 1;
                }
                if (i2 >= i3) {
                    break;
                }
                FPoint fPoint = (FPoint) arrayList2.get(i2 % size);
                FPoint fPoint2 = (FPoint) arrayList2.get((i2 + 1) % size);
                if (i2 == 0 && a(fPoint, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    arrayList.add(fPoint);
                }
                if (a(fPoint, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    if (a(fPoint2, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                        arrayList.add(fPoint2);
                    } else {
                        arrayList.add(a(fPointArr[i], fPointArr[(i + 1) % fPointArr.length], fPoint, fPoint2));
                    }
                } else if (a(fPoint2, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    arrayList.add(a(fPointArr[i], fPointArr[(i + 1) % fPointArr.length], fPoint, fPoint2));
                    arrayList.add(fPoint2);
                }
                i2++;
            }
            arrayList2.clear();
            for (i3 = 0; i3 < arrayList.size(); i3++) {
                arrayList2.add(arrayList.get(i3));
            }
            byte b = (byte) (i + 1);
        }
        return arrayList2;
    }

    public static List<FPoint> b(FPoint[] fPointArr, List<FPoint> list, boolean z) {
        List arrayList = new ArrayList();
        List<FPoint> arrayList2 = new ArrayList(list);
        int i = 0;
        while (i < 4) {
            int i2;
            arrayList.clear();
            int size = arrayList2.size();
            int i3 = 0;
            while (true) {
                if (z) {
                    i2 = size;
                } else {
                    i2 = size - 1;
                }
                if (i3 >= i2) {
                    break;
                }
                FPoint3 fPoint3 = (FPoint3) arrayList2.get(i3 % size);
                FPoint3 fPoint32 = (FPoint3) arrayList2.get((i3 + 1) % size);
                if (i3 == 0 && a((FPoint) fPoint3, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    arrayList.add(fPoint3);
                }
                if (a((FPoint) fPoint3, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    if (a((FPoint) fPoint32, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                        arrayList.add(fPoint32);
                    } else {
                        arrayList.add(a(fPointArr[i], fPointArr[(i + 1) % fPointArr.length], fPoint3, fPoint32));
                    }
                } else if (a((FPoint) fPoint32, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                    arrayList.add(a(fPointArr[i], fPointArr[(i + 1) % fPointArr.length], fPoint3, fPoint32));
                    arrayList.add(fPoint32);
                }
                i3++;
            }
            arrayList2.clear();
            for (i2 = 0; i2 < arrayList.size(); i2++) {
                arrayList2.add(arrayList.get(i2));
            }
            byte b = (byte) (i + 1);
        }
        return arrayList2;
    }

    private static float a(float f) {
        if (f == 50.0f) {
            return 0.082f;
        }
        if (f == 54.0f) {
            return 0.15f;
        }
        if (f == 57.0f) {
            return 0.2f;
        }
        if (f == BitmapDescriptorFactory.HUE_YELLOW) {
            return 0.25f;
        }
        return 0.0f;
    }

    public static FPoint[] a(l lVar, boolean z) {
        int i;
        int i2;
        float a = (float) (((double) a(lVar.getCameraAngle())) * ((double) lVar.getMapHeight()));
        if (z) {
            i = 100;
            i2 = 10;
        } else {
            i2 = 0;
            i = 0;
        }
        FPoint[] fPointArr = new FPoint[4];
        FPoint fPoint = new FPoint();
        lVar.a(-i, (int) (a - ((float) i2)), fPoint);
        fPointArr[0] = fPoint;
        FPoint fPoint2 = new FPoint();
        lVar.a(lVar.getMapWidth() + i, (int) (a - ((float) i2)), fPoint2);
        fPointArr[1] = fPoint2;
        FPoint fPoint3 = new FPoint();
        lVar.a(lVar.getMapWidth() + i, lVar.getMapHeight() + i, fPoint3);
        fPointArr[2] = fPoint3;
        fPoint3 = new FPoint();
        lVar.a(-i, i + lVar.getMapHeight(), fPoint3);
        fPointArr[3] = fPoint3;
        return fPointArr;
    }

    private static FPoint3 a(FPoint fPoint, FPoint fPoint2, FPoint3 fPoint3, FPoint3 fPoint32) {
        FPoint3 fPoint33 = new FPoint3(0.0f, 0.0f, fPoint3.colorIndex);
        double d = (double) (((fPoint2.y - fPoint.y) * (fPoint.x - fPoint3.x)) - ((fPoint2.x - fPoint.x) * (fPoint.y - fPoint3.y)));
        double d2 = (double) (((fPoint2.y - fPoint.y) * (fPoint32.x - fPoint3.x)) - ((fPoint2.x - fPoint.x) * (fPoint32.y - fPoint3.y)));
        fPoint33.x = (float) (((double) fPoint3.x) + ((((double) (fPoint32.x - fPoint3.x)) * d) / d2));
        fPoint33.y = (float) (((d * ((double) (fPoint32.y - fPoint3.y))) / d2) + ((double) fPoint3.y));
        return fPoint33;
    }

    private static FPoint a(FPoint fPoint, FPoint fPoint2, FPoint fPoint3, FPoint fPoint4) {
        FPoint fPoint5 = new FPoint(0.0f, 0.0f);
        double d = (double) (((fPoint2.y - fPoint.y) * (fPoint.x - fPoint3.x)) - ((fPoint2.x - fPoint.x) * (fPoint.y - fPoint3.y)));
        double d2 = (double) (((fPoint2.y - fPoint.y) * (fPoint4.x - fPoint3.x)) - ((fPoint2.x - fPoint.x) * (fPoint4.y - fPoint3.y)));
        fPoint5.x = (float) (((double) fPoint3.x) + ((((double) (fPoint4.x - fPoint3.x)) * d) / d2));
        fPoint5.y = (float) (((d * ((double) (fPoint4.y - fPoint3.y))) / d2) + ((double) fPoint3.y));
        return fPoint5;
    }

    public static boolean a(FPoint fPoint, FPoint[] fPointArr) {
        if (fPointArr == null) {
            return false;
        }
        for (int i = 0; i < fPointArr.length; i = (byte) (i + 1)) {
            if (!a(fPoint, fPointArr[i], fPointArr[(i + 1) % fPointArr.length])) {
                return false;
            }
        }
        return true;
    }

    private static boolean a(FPoint fPoint, FPoint fPoint2, FPoint fPoint3) {
        if (((double) (((fPoint3.x - fPoint2.x) * (fPoint.y - fPoint2.y)) - ((fPoint.x - fPoint2.x) * (fPoint3.y - fPoint2.y)))) >= 0.0d) {
            return true;
        }
        return false;
    }

    public static Bitmap a(int i, int i2, int i3, int i4, GL10 gl10) {
        try {
            int[] iArr = new int[(i3 * i4)];
            int[] iArr2 = new int[(i3 * i4)];
            Buffer wrap = IntBuffer.wrap(iArr);
            wrap.position(0);
            gl10.glReadPixels(i, i2, i3, i4, 6408, 5121, wrap);
            for (int i5 = 0; i5 < i4; i5++) {
                for (int i6 = 0; i6 < i3; i6++) {
                    int i7 = iArr[(i5 * i3) + i6];
                    iArr2[(((i4 - i5) - 1) * i3) + i6] = ((i7 & -16711936) | ((i7 << 16) & 16711680)) | ((i7 >> 16) & 255);
                }
            }
            Bitmap createBitmap = Bitmap.createBitmap(i3, i4, Config.ARGB_8888);
            createBitmap.setPixels(iArr2, 0, i3, 0, 0, i3, i4);
            return createBitmap;
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImpGLSurfaceView", "SavePixels");
            th.printStackTrace();
            return null;
        }
    }

    public static fh e() {
        try {
            if (g.f != null) {
                return g.f;
            }
            return new a("3dmap", "4.1.2", g.d).a(new String[]{"com.amap.api.maps", "com.amap.api.mapcore", "com.autonavi.amap.mapcore", "com.amap.api.3dmap.admic", "com.amap.api.trace", "com.amap.api.trace.core"}).a("4.1.2").a();
        } catch (Throwable th) {
            return null;
        }
    }

    private static void b(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                b(((ViewGroup) view).getChildAt(i));
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setHorizontallyScrolling(false);
        }
    }

    public static Bitmap a(View view) {
        try {
            b(view);
            view.destroyDrawingCache();
            view.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            Bitmap drawingCache = view.getDrawingCache();
            if (drawingCache == null) {
                return null;
            }
            return drawingCache.copy(Config.ARGB_8888, false);
        } catch (Throwable th) {
            fo.b(th, "Utils", "getBitmapFromView");
            th.printStackTrace();
            return null;
        }
    }

    public static DPoint a(LatLng latLng) {
        double d = (latLng.longitude / 360.0d) + 0.5d;
        double sin = Math.sin(Math.toRadians(latLng.latitude));
        return new DPoint(d * WeightedLatLng.DEFAULT_INTENSITY, (((Math.log((WeightedLatLng.DEFAULT_INTENSITY + sin) / (WeightedLatLng.DEFAULT_INTENSITY - sin)) * 0.5d) / -6.283185307179586d) + 0.5d) * WeightedLatLng.DEFAULT_INTENSITY);
    }

    public static float[] a(l lVar, int i, FPoint fPoint, float f, int i2, int i3, float f2, float f3) throws RemoteException {
        float f4 = fPoint.x;
        float f5 = fPoint.y;
        float[] fArr = new float[12];
        float mapPerPixelUnitLength = lVar.getMapConfig().getMapPerPixelUnitLength() * ((float) i2);
        float mapPerPixelUnitLength2 = lVar.getMapConfig().getMapPerPixelUnitLength() * ((float) i3);
        float[] fArr2 = new float[16];
        float[] fArr3 = new float[4];
        Matrix.setIdentityM(fArr2, 0);
        if (i == 3) {
            Matrix.translateM(fArr2, 0, f4, f5, 0.0f);
            Matrix.rotateM(fArr2, 0, lVar.c().getMapAngle(), 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            Matrix.translateM(fArr2, 0, -f4, -f5, 0.0f);
            Matrix.translateM(fArr2, 0, f4 - (mapPerPixelUnitLength / 2.0f), f5 - (mapPerPixelUnitLength2 / 2.0f), 0.0f);
            Matrix.rotateM(fArr2, 0, -lVar.c().getCameraHeaderAngle(), WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
            Matrix.translateM(fArr2, 0, (mapPerPixelUnitLength / 2.0f) - f4, (mapPerPixelUnitLength2 / 2.0f) - f5, 0.0f);
        } else if (i != 1) {
            Matrix.translateM(fArr2, 0, f4, f5, 0.0f);
            Matrix.rotateM(fArr2, 0, lVar.c().getMapAngle(), 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            Matrix.rotateM(fArr2, 0, -lVar.c().getCameraHeaderAngle(), WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f);
            Matrix.rotateM(fArr2, 0, f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            Matrix.translateM(fArr2, 0, -f4, -f5, 0.0f);
        } else {
            Matrix.translateM(fArr2, 0, f4, f5, 0.0f);
            Matrix.rotateM(fArr2, 0, f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            Matrix.translateM(fArr2, 0, -f4, -f5, 0.0f);
        }
        float[] fArr4 = new float[]{f4 - (mapPerPixelUnitLength * f2), ((WMElement.CAMERASIZEVALUE1B1 - f3) * mapPerPixelUnitLength2) + f5, 0.0f, WMElement.CAMERASIZEVALUE1B1};
        Matrix.multiplyMV(fArr4, 0, fArr2, 0, fArr3, 0);
        fArr[0] = fArr4[0];
        fArr[1] = fArr4[1];
        fArr[2] = fArr4[2];
        fArr3[0] = ((WMElement.CAMERASIZEVALUE1B1 - f2) * mapPerPixelUnitLength) + f4;
        fArr3[1] = ((WMElement.CAMERASIZEVALUE1B1 - f3) * mapPerPixelUnitLength2) + f5;
        fArr3[2] = 0.0f;
        fArr3[3] = WMElement.CAMERASIZEVALUE1B1;
        Matrix.multiplyMV(fArr4, 0, fArr2, 0, fArr3, 0);
        fArr[3] = fArr4[0];
        fArr[4] = fArr4[1];
        fArr[5] = fArr4[2];
        fArr3[0] = ((WMElement.CAMERASIZEVALUE1B1 - f2) * mapPerPixelUnitLength) + f4;
        fArr3[1] = f5 - (mapPerPixelUnitLength2 * f3);
        fArr3[2] = 0.0f;
        fArr3[3] = WMElement.CAMERASIZEVALUE1B1;
        Matrix.multiplyMV(fArr4, 0, fArr2, 0, fArr3, 0);
        fArr[6] = fArr4[0];
        fArr[7] = fArr4[1];
        fArr[8] = fArr4[2];
        fArr3[0] = f4 - (mapPerPixelUnitLength * f2);
        fArr3[1] = f5 - (mapPerPixelUnitLength2 * f3);
        fArr3[2] = 0.0f;
        fArr3[3] = WMElement.CAMERASIZEVALUE1B1;
        Matrix.multiplyMV(fArr4, 0, fArr2, 0, fArr3, 0);
        fArr[9] = fArr4[0];
        fArr[10] = fArr4[1];
        fArr[11] = fArr4[2];
        return fArr;
    }

    public static boolean a(Rect rect, int i, int i2) {
        return rect.contains(i, i2);
    }

    public static Pair<Float, IPoint> a(af afVar, MapProjection mapProjection, MapConfig mapConfig) {
        LatLngBounds latLngBounds = afVar.f;
        int i = afVar.width;
        int i2 = afVar.height;
        return a(mapProjection, mapConfig, Math.max(afVar.h, 1), Math.max(afVar.i, 1), Math.max(afVar.j, 1), Math.max(afVar.k, 1), latLngBounds, i, i2);
    }

    public static Pair<Float, IPoint> a(MapProjection mapProjection, MapConfig mapConfig, int i, int i2, int i3, int i4, LatLngBounds latLngBounds, int i5, int i6) {
        IPoint iPoint = new IPoint();
        IPoint iPoint2 = new IPoint();
        MapProjection.lonlat2Geo(latLngBounds.northeast.longitude, latLngBounds.northeast.latitude, iPoint);
        MapProjection.lonlat2Geo(latLngBounds.southwest.longitude, latLngBounds.southwest.latitude, iPoint2);
        int i7 = iPoint.x - iPoint2.x;
        int i8 = iPoint2.y - iPoint.y;
        int i9 = i5 - (i + i2);
        int i10 = i6 - (i3 + i4);
        if (i7 < 0 && i8 < 0) {
            return null;
        }
        int i11;
        float a;
        if (i9 <= 0) {
            i9 = 1;
        }
        if (i10 > 0) {
            i11 = i10;
        } else {
            i11 = 1;
        }
        mapProjection.setMapAngle(0.0f);
        mapProjection.setCameraHeaderAngle(0.0f);
        mapProjection.recalculate();
        IPoint iPoint3 = new IPoint();
        IPoint iPoint4 = new IPoint();
        mapProjection.getLatLng2Pixel(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude, iPoint3);
        mapProjection.getLatLng2Pixel(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude, iPoint4);
        double d = (double) (iPoint3.x - iPoint4.x);
        double d2 = (double) (iPoint4.y - iPoint3.y);
        if (d <= 0.0d) {
            d = WeightedLatLng.DEFAULT_INTENSITY;
        }
        if (d2 <= 0.0d) {
            d2 = WeightedLatLng.DEFAULT_INTENSITY;
        }
        d = Math.log(((double) i9) / d) / Math.log(2.0d);
        d2 = Math.min(d, Math.log(((double) i11) / d2) / Math.log(2.0d));
        Object obj = Math.abs(d2 - d) < 1.0E-7d ? 1 : null;
        float a2 = a(mapConfig, (float) (((double) mapProjection.getMapZoomer()) + Math.floor(d2)));
        while (true) {
            a = a(mapConfig, (float) (((double) a2) + 0.01d));
            mapProjection.setMapZoomer(a);
            mapProjection.recalculate();
            mapProjection.getLatLng2Pixel(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude, iPoint3);
            mapProjection.getLatLng2Pixel(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude, iPoint4);
            d = (double) (iPoint3.x - iPoint4.x);
            d2 = (double) (iPoint4.y - iPoint3.y);
            if (d <= 0.0d) {
                d = WeightedLatLng.DEFAULT_INTENSITY;
            }
            if (d2 <= 0.0d) {
                d2 = WeightedLatLng.DEFAULT_INTENSITY;
            }
            if (obj == null) {
                if (d2 >= ((double) i11)) {
                    break;
                }
                if (a < mapConfig.maxZoomLevel) {
                    break;
                }
                a2 = a;
            } else {
                if (d >= ((double) i9)) {
                    break;
                }
                if (a < mapConfig.maxZoomLevel) {
                    break;
                }
                a2 = a;
            }
        }
        a = (float) (((double) a) - 0.01d);
        if (obj == null) {
            i10 = (int) (((double) iPoint2.x) + (((((double) (i2 - i)) + d) * ((double) i7)) / (d * 2.0d)));
            i9 = (int) (((double) iPoint.y) + ((((double) ((i6 / 2) - i3)) / d2) * ((double) i8)));
        } else {
            i10 = (int) (((double) iPoint2.x) + ((((double) ((i5 / 2) - i)) / d) * ((double) i7)));
            i9 = (int) (((double) iPoint.y) + (((((double) (i4 - i3)) + d2) * ((double) i8)) / (d2 * 2.0d)));
        }
        return new Pair(Float.valueOf(a), new IPoint(i10, i9));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(ed edVar, ed edVar2) {
        float min = Math.min(edVar.a, edVar2.a);
        float min2 = Math.min(edVar.c, edVar2.c);
        if (min < 0.0f) {
            edVar.a -= min;
            edVar.b -= min;
            edVar2.a -= min;
            edVar2.b -= min;
        }
        if (min2 < 0.0f) {
            edVar.c -= min2;
            edVar.d -= min2;
            edVar2.c -= min2;
            edVar2.d -= min2;
        }
        if (edVar.d <= edVar2.d && edVar.c >= edVar2.c && edVar.b <= edVar2.b) {
            boolean z;
            if (edVar.a >= edVar2.a) {
                z = true;
            } else {
                z = false;
            }
        }
        if (edVar2.d > edVar.d || edVar2.c < edVar.c || edVar2.b > edVar.b || edVar2.a < edVar.a) {
            return Math.min(edVar.b, edVar2.b) - Math.max(edVar.a, edVar2.a) >= 0.0f && Math.min(edVar.d, edVar2.d) - Math.max(edVar.c, edVar2.c) >= 0.0f;
        }
        return true;
    }

    public static boolean a(int i, int i2) {
        if (i > 0 && i2 > 0) {
            return true;
        }
        Log.w("3dmap", "the map must have a size");
        return false;
    }

    private static Float[] a(List<Float> list, List<Float> list2, List<Float> list3, List<Float> list4, float f, float f2) {
        Float[] fArr = new Float[2];
        float floatValue = ((Float) Collections.min(list)).floatValue();
        float floatValue2 = ((Float) Collections.max(list)).floatValue();
        float floatValue3 = ((Float) Collections.min(list2)).floatValue();
        float floatValue4 = ((Float) Collections.max(list2)).floatValue();
        floatValue = Math.abs(floatValue2 - floatValue);
        floatValue2 = Math.abs(floatValue3 - floatValue4);
        floatValue3 = ((Float) Collections.min(list3)).floatValue();
        float floatValue5 = ((Float) Collections.max(list3)).floatValue();
        float floatValue6 = ((Float) Collections.min(list4)).floatValue();
        float floatValue7 = ((Float) Collections.max(list4)).floatValue();
        if (floatValue > Math.abs(floatValue5 - floatValue3)) {
            floatValue4 = Math.abs(floatValue5 - floatValue3);
        } else {
            floatValue4 = floatValue;
        }
        if (floatValue2 > Math.abs(floatValue7 - floatValue6)) {
            floatValue = Math.abs(floatValue7 - floatValue6);
        } else {
            floatValue = floatValue2;
        }
        float f3 = floatValue3 + (floatValue4 / 2.0f);
        floatValue2 = (floatValue / 2.0f) + floatValue6;
        floatValue3 = floatValue5 - (floatValue4 / 2.0f);
        floatValue4 = floatValue7 - (floatValue / 2.0f);
        floatValue = f <= f3 ? f3 : f;
        if (floatValue >= floatValue3) {
            floatValue = floatValue3;
        }
        if (f2 < floatValue4) {
            floatValue4 = f2;
        }
        if (floatValue4 <= floatValue2) {
            floatValue4 = floatValue2;
        }
        fArr[0] = Float.valueOf(floatValue);
        fArr[1] = Float.valueOf(floatValue4);
        return fArr;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized Integer[] a(IPoint[] iPointArr, IPoint[] iPointArr2, int i, int i2) {
        int i3 = 0;
        synchronized (eh.class) {
            if (!(iPointArr == null || iPointArr2 == null)) {
                if (iPointArr.length == 2) {
                    List arrayList = new ArrayList(4);
                    List arrayList2 = new ArrayList(4);
                    for (int i4 = 0; i4 < iPointArr2.length; i4++) {
                        IPoint iPoint = iPointArr2[i4];
                        arrayList.add(i4, Float.valueOf((float) iPoint.x));
                        arrayList2.add(i4, Float.valueOf((float) iPoint.y));
                    }
                    List arrayList3 = new ArrayList(2);
                    List arrayList4 = new ArrayList(2);
                    while (i3 < iPointArr.length) {
                        IPoint iPoint2 = iPointArr[i3];
                        arrayList3.add(i3, Float.valueOf((float) iPoint2.x));
                        arrayList4.add(i3, Float.valueOf((float) iPoint2.y));
                        i3++;
                    }
                    Float[] a = a(arrayList, arrayList2, arrayList3, arrayList4, (float) i, (float) i2);
                    Integer[] numArr = new Integer[2];
                    float floatValue = a[0].floatValue();
                    float floatValue2 = a[1].floatValue();
                    numArr[0] = Integer.valueOf((int) floatValue);
                    numArr[1] = Integer.valueOf((int) floatValue2);
                    return numArr;
                }
            }
        }
    }

    public static float a(MapProjection mapProjection, MapConfig mapConfig, IPoint iPoint, IPoint iPoint2, int i, int i2) {
        mapProjection.setMapAngle(0.0f);
        mapProjection.setCameraHeaderAngle(0.0f);
        mapProjection.recalculate();
        IPoint iPoint3 = new IPoint();
        IPoint iPoint4 = new IPoint();
        a(mapProjection, iPoint, iPoint3);
        a(mapProjection, iPoint2, iPoint4);
        double d = (double) (iPoint3.x - iPoint4.x);
        double d2 = (double) (iPoint4.y - iPoint3.y);
        if (d <= 0.0d) {
            d = WeightedLatLng.DEFAULT_INTENSITY;
        }
        if (d2 <= 0.0d) {
            d2 = WeightedLatLng.DEFAULT_INTENSITY;
        }
        d = Math.log(((double) i) / d) / Math.log(2.0d);
        d2 = Math.max(d, Math.log(((double) i2) / d2) / Math.log(2.0d));
        Object obj = Math.abs(d2 - d) < 1.0E-7d ? 1 : null;
        float a = a(mapConfig, (float) (Math.floor(d2) + ((double) mapProjection.getMapZoomer())));
        do {
            a = a(mapConfig, (float) (((double) a) + 0.1d));
            mapProjection.setMapZoomer(a);
            mapProjection.recalculate();
            a(mapProjection, iPoint, iPoint3);
            a(mapProjection, iPoint2, iPoint4);
            d2 = (double) (iPoint3.x - iPoint4.x);
            double d3 = (double) (iPoint4.y - iPoint3.y);
            if (obj == null) {
                if (d3 >= ((double) i2)) {
                }
            } else if (d2 >= ((double) i)) {
            }
            return (float) (((double) a) - 0.1d);
        } while (a < mapConfig.maxZoomLevel);
        return a;
    }

    private static void a(MapProjection mapProjection, IPoint iPoint, IPoint iPoint2) {
        mapProjection.recalculate();
        FPoint fPoint = new FPoint();
        mapProjection.geo2Map(iPoint.x, iPoint.y, fPoint);
        mapProjection.map2Win(fPoint.x, fPoint.y, iPoint2);
    }

    public static FPoint[] a(IAMap iAMap, boolean z, MapProjection mapProjection) {
        int i;
        int i2;
        float a = (float) (((double) a(iAMap.getCameraAngle())) * ((double) iAMap.getMapHeight()));
        if (z) {
            i = 100;
            i2 = 10;
        } else {
            i2 = 0;
            i = 0;
        }
        FPoint[] fPointArr = new FPoint[4];
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(-i, (int) (a - ((float) i2)), fPoint);
        fPointArr[0] = fPoint;
        FPoint fPoint2 = new FPoint();
        mapProjection.win2Map(iAMap.getMapWidth() + i, (int) (a - ((float) i2)), fPoint2);
        fPointArr[1] = fPoint2;
        FPoint fPoint3 = new FPoint();
        mapProjection.win2Map(iAMap.getMapWidth() + i, iAMap.getMapHeight() + i, fPoint3);
        fPointArr[2] = fPoint3;
        fPoint3 = new FPoint();
        mapProjection.win2Map(-i, i + iAMap.getMapHeight(), fPoint3);
        fPointArr[3] = fPoint3;
        return fPointArr;
    }
}
