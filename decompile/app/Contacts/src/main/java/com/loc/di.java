package com.loc;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.telephony.NeighboringCellInfo;
import android.text.TextUtils;
import com.google.android.gms.common.ConnectionResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/* compiled from: Unknown */
public class di {
    private Context a;
    private int b = 0;
    private int c = 0;
    private int d = 0;
    private int e = 0;
    private int f = 0;
    private int g = 0;

    protected di(Context context) {
        this.a = context;
        a(768);
        b(768);
    }

    private static int a(int i, int i2) {
        return i >= i2 ? i2 : i;
    }

    protected static da a(Location location, dl dlVar, int i, byte b, long j, boolean z) {
        da daVar = new da();
        if (i <= 0 || i > 3 || dlVar == null) {
            return null;
        }
        int i2;
        int i3;
        Object obj = (i == 1 || i == 3) ? 1 : null;
        Object obj2 = (i == 2 || i == 3) ? 1 : null;
        Object bytes = dlVar.o().getBytes();
        System.arraycopy(bytes, 0, daVar.c, 0, a(bytes.length, daVar.c.length));
        bytes = dlVar.f().getBytes();
        System.arraycopy(bytes, 0, daVar.g, 0, a(bytes.length, daVar.g.length));
        bytes = dlVar.g().getBytes();
        System.arraycopy(bytes, 0, daVar.a, 0, a(bytes.length, daVar.a.length));
        bytes = dlVar.h().getBytes();
        System.arraycopy(bytes, 0, daVar.b, 0, a(bytes.length, daVar.b.length));
        daVar.d = (short) ((short) dlVar.p());
        daVar.e = (short) ((short) dlVar.q());
        daVar.f = (byte) ((byte) dlVar.r());
        bytes = dlVar.s().getBytes();
        System.arraycopy(bytes, 0, daVar.h, 0, a(bytes.length, daVar.h.length));
        long j2 = j / 1000;
        bytes = (location != null && dlVar.e()) ? 1 : null;
        cy cyVar;
        if (bytes != null) {
            cyVar = new cy();
            cyVar.b = (int) j2;
            cz czVar = new cz();
            czVar.a = (int) (location.getLongitude() * 1000000.0d);
            czVar.b = (int) (location.getLatitude() * 1000000.0d);
            czVar.c = (int) location.getAltitude();
            czVar.d = (int) location.getAccuracy();
            czVar.e = (int) location.getSpeed();
            czVar.f = (short) ((short) ((int) location.getBearing()));
            if (!Build.MODEL.equals("sdk")) {
                if (!dl.b(dlVar.x()) || !db.b) {
                    czVar.g = (byte) 0;
                    czVar.h = (byte) b;
                    if (czVar.d > 25) {
                        czVar.h = (byte) 5;
                    }
                    czVar.i = System.currentTimeMillis();
                    czVar.j = dlVar.n();
                    cyVar.c = czVar;
                    i2 = 1;
                    daVar.j.add(cyVar);
                }
            }
            czVar.g = (byte) 1;
            czVar.h = (byte) b;
            if (czVar.d > 25) {
                czVar.h = (byte) 5;
            }
            czVar.i = System.currentTimeMillis();
            czVar.j = dlVar.n();
            cyVar.c = czVar;
            i2 = 1;
            daVar.j.add(cyVar);
        } else if (!z) {
            return null;
        } else {
            cyVar = new cy();
            cyVar.b = (int) j2;
            dd ddVar = new dd();
            ddVar.a = (byte) dlVar.w();
            for (i2 = 0; i2 < ddVar.a; i2++) {
                de deVar = new de();
                deVar.a = (byte) ((byte) dlVar.b(i2).length());
                System.arraycopy(dlVar.b(i2).getBytes(), 0, deVar.b, 0, deVar.a);
                deVar.c = dlVar.c(i2);
                deVar.d = dlVar.d(i2);
                deVar.e = dlVar.e(i2);
                deVar.f = dlVar.f(i2);
                deVar.g = (byte) dlVar.g(i2);
                deVar.h = (byte) ((byte) dlVar.h(i2).length());
                System.arraycopy(dlVar.h(i2).getBytes(), 0, deVar.i, 0, deVar.h);
                deVar.j = (byte) dlVar.i(i2);
                ddVar.b.add(deVar);
            }
            cyVar.g = ddVar;
            i2 = 1;
            daVar.j.add(cyVar);
        }
        if (!(!dlVar.c() || dlVar.i() || obj == null || z)) {
            cy cyVar2 = new cy();
            cyVar2.b = (int) j2;
            eg egVar = new eg();
            List a = dlVar.a(location.getSpeed());
            if (a != null && a.size() >= 3) {
                egVar.a = (short) ((short) ((Integer) a.get(0)).intValue());
                egVar.b = ((Integer) a.get(1)).intValue();
            }
            egVar.c = (byte) dlVar.k();
            List l = dlVar.l();
            egVar.d = (byte) ((byte) l.size());
            for (i3 = 0; i3 < l.size(); i3++) {
                dk dkVar = new dk();
                dkVar.a = (short) ((short) ((NeighboringCellInfo) l.get(i3)).getLac());
                dkVar.b = ((NeighboringCellInfo) l.get(i3)).getCid();
                dkVar.c = (byte) ((byte) ((NeighboringCellInfo) l.get(i3)).getRssi());
                egVar.e.add(dkVar);
            }
            cyVar2.d = egVar;
            i2 = 2;
            daVar.j.add(cyVar2);
        }
        i3 = i2;
        if (dlVar.c() && dlVar.i() && obj != null && !z) {
            cy cyVar3 = new cy();
            cyVar3.b = (int) j2;
            dj djVar = new dj();
            List b2 = dlVar.b(location.getSpeed());
            if (b2 != null && b2.size() >= 6) {
                djVar.a = ((Integer) b2.get(3)).intValue();
                djVar.b = ((Integer) b2.get(4)).intValue();
                djVar.c = (short) ((short) ((Integer) b2.get(0)).intValue());
                djVar.d = (short) ((short) ((Integer) b2.get(1)).intValue());
                djVar.e = ((Integer) b2.get(2)).intValue();
                djVar.f = (byte) dlVar.k();
            }
            cyVar3.e = djVar;
            i3++;
            daVar.j.add(cyVar3);
        }
        if (!(!dlVar.d() || obj2 == null || z)) {
            cyVar2 = new cy();
            df dfVar = new df();
            List t = dlVar.t();
            cyVar2.b = (int) (((Long) t.get(0)).longValue() / 1000);
            dfVar.a = (byte) ((byte) (t.size() - 1));
            for (int i4 = 1; i4 < t.size(); i4++) {
                List list = (List) t.get(i4);
                if (list != null && list.size() >= 3) {
                    dg dgVar = new dg();
                    obj2 = ((String) list.get(0)).getBytes();
                    System.arraycopy(obj2, 0, dgVar.a, 0, a(obj2.length, dgVar.a.length));
                    dgVar.b = (short) ((short) ((Integer) list.get(1)).intValue());
                    bytes = ((String) list.get(2)).getBytes();
                    System.arraycopy(bytes, 0, dgVar.c, 0, a(bytes.length, dgVar.c.length));
                    dfVar.b.add(dgVar);
                }
            }
            cyVar2.f = dfVar;
            i3++;
            daVar.j.add(cyVar2);
        }
        daVar.i = (short) ((short) i3);
        return (i3 < 2 && !z) ? null : daVar;
    }

    protected static File a(Context context) {
        return new File(Environment.getExternalStorageDirectory().getPath() + ("/Android/data/" + context.getPackageName() + "/files/"));
    }

    public static Object a(Object obj, String str, Object... objArr) {
        Class cls = obj.getClass();
        Class[] clsArr = new Class[objArr.length];
        int length = objArr.length;
        for (int i = 0; i < length; i++) {
            clsArr[i] = objArr[i].getClass();
            if (clsArr[i] == Integer.class) {
                clsArr[i] = Integer.TYPE;
            }
        }
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        if (!declaredMethod.isAccessible()) {
            declaredMethod.setAccessible(true);
        }
        return declaredMethod.invoke(obj, objArr);
    }

    private static ArrayList a(File[] fileArr) {
        int i = 0;
        ArrayList arrayList = new ArrayList();
        while (i < fileArr.length) {
            if (fileArr[i].isFile() && fileArr[i].getName().length() == 10 && TextUtils.isDigitsOnly(fileArr[i].getName())) {
                arrayList.add(fileArr[i]);
            }
            i++;
        }
        return arrayList;
    }

    protected static byte[] a(BitSet bitSet) {
        byte[] bArr = new byte[(bitSet.size() / 8)];
        for (int i = 0; i < bitSet.size(); i++) {
            int i2 = i / 8;
            bArr[i2] = (byte) ((byte) (((!bitSet.get(i) ? 0 : 1) << (7 - (i % 8))) | bArr[i2]));
        }
        return bArr;
    }

    protected static byte[] a(byte[] bArr) {
        byte[] bArr2 = null;
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(bArr);
            gZIPOutputStream.finish();
            gZIPOutputStream.close();
            bArr2 = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return bArr2;
        } catch (Exception e) {
            return bArr2;
        }
    }

    protected static byte[] a(byte[] bArr, int i) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        int indexOf = new String(bArr).indexOf(0);
        if (indexOf <= 0) {
            i = 1;
        } else if (indexOf + 1 <= i) {
            i = indexOf + 1;
        }
        Object obj = new byte[i];
        System.arraycopy(bArr, 0, obj, 0, i);
        obj[i - 1] = null;
        return obj;
    }

    public static int b(Object obj, String str, Object... objArr) {
        Class cls = obj.getClass();
        Class[] clsArr = new Class[objArr.length];
        int length = objArr.length;
        for (int i = 0; i < length; i++) {
            clsArr[i] = objArr[i].getClass();
            if (clsArr[i] == Integer.class) {
                clsArr[i] = Integer.TYPE;
            }
        }
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        if (!declaredMethod.isAccessible()) {
            declaredMethod.setAccessible(true);
        }
        return ((Integer) declaredMethod.invoke(obj, objArr)).intValue();
    }

    protected static BitSet b(byte[] bArr) {
        BitSet bitSet = new BitSet(bArr.length << 3);
        int i = 0;
        for (byte b : bArr) {
            int i2 = 7;
            while (i2 >= 0) {
                int i3 = i + 1;
                bitSet.set(i, ((b & (1 << i2)) >> i2) == 1);
                i2--;
                i = i3;
            }
        }
        return bitSet;
    }

    private File c(long j) {
        boolean z = false;
        if (Process.myUid() == 1000) {
            return null;
        }
        File file;
        boolean equals;
        try {
            equals = "mounted".equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            equals = z;
        }
        if (c() && !r0) {
            file = null;
        } else {
            if (!(d() > ((long) (this.d / 2)) ? true : z)) {
                return null;
            }
            File file2 = new File(a(this.a).getPath() + File.separator + "carrierdata");
            if (!file2.exists() || !file2.isDirectory()) {
                file2.mkdirs();
            }
            file = new File(file2.getPath() + File.separator + j);
            try {
                z = file.createNewFile();
            } catch (IOException e2) {
            }
        }
        return !z ? null : file;
    }

    protected static boolean c() {
        if (VERSION.SDK_INT >= 9) {
            try {
                return ((Boolean) Environment.class.getMethod("isExternalStorageRemovable", null).invoke(null, null)).booleanValue();
            } catch (Exception e) {
            }
        }
        return true;
    }

    protected static long d() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) statFs.getBlockSize()) * ((long) statFs.getAvailableBlocks());
    }

    private File e() {
        boolean equals;
        if (Process.myUid() == 1000) {
            return null;
        }
        File file;
        try {
            equals = "mounted".equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            equals = false;
        }
        if (!c() || r0) {
            File file2 = new File(a(this.a).getPath() + File.separator + "carrierdata");
            if (file2.exists() && file2.isDirectory()) {
                File[] listFiles = file2.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    ArrayList a = a(listFiles);
                    if (a.size() == 1) {
                        if ((((File) a.get(0)).length() >= ((long) this.f) ? 1 : 0) == 0) {
                            file = (File) a.get(0);
                            return file;
                        }
                    } else if (a.size() >= 2) {
                        file = (File) a.get(0);
                        File file3 = (File) a.get(1);
                        if (file.getName().compareTo(file3.getName()) <= 0) {
                            file = file3;
                        }
                        return file;
                    }
                }
            }
        }
        file = null;
        return file;
    }

    private int f() {
        int i = 0;
        if (Process.myUid() == 1000) {
            return 0;
        }
        boolean equals;
        try {
            equals = "mounted".equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            equals = false;
        }
        if (!c() || r0) {
            File file = new File(a(this.a).getPath() + File.separator + "carrierdata");
            if (file.exists() && file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    ArrayList a = a(listFiles);
                    if (a.size() == 1) {
                        if (((File) a.get(0)).length() > 0) {
                            i = 1;
                        }
                        i = i == 0 ? 10 : 1;
                    } else if (a.size() >= 2) {
                        i = 2;
                    }
                }
            }
        }
        return i;
    }

    private File g() {
        boolean equals;
        if (Process.myUid() == 1000) {
            return null;
        }
        File a;
        try {
            equals = "mounted".equals(Environment.getExternalStorageState());
        } catch (Exception e) {
            equals = false;
        }
        if (!c() || r0) {
            a = a(this.a);
            if (a != null) {
                File file = new File(a.getPath() + File.separator + "carrierdata");
                if (file.exists() && file.isDirectory()) {
                    File[] listFiles = file.listFiles();
                    if (listFiles != null && listFiles.length > 0) {
                        ArrayList a2 = a(listFiles);
                        if (a2.size() >= 2) {
                            a = (File) a2.get(0);
                            File file2 = (File) a2.get(1);
                            if (a.getName().compareTo(file2.getName()) > 0) {
                                a = file2;
                            }
                            return a;
                        }
                    }
                }
            }
        }
        a = null;
        return a;
    }

    protected int a() {
        return this.b;
    }

    protected synchronized File a(long j) {
        File e;
        e = e();
        if (e == null) {
            e = c(j);
        }
        return e;
    }

    protected void a(int i) {
        this.b = i;
        this.d = (((this.b << 3) * ConnectionResult.DRIVE_EXTERNAL_STORAGE_REQUIRED) + this.b) + 4;
        if (this.b == 256 || this.b == 768) {
            this.f = this.d / 100;
            return;
        }
        if (this.b == 8736) {
            this.f = this.d - 5000;
        }
    }

    protected File b() {
        return g();
    }

    protected void b(int i) {
        this.c = i;
        this.e = (((this.c << 3) * 1000) + this.c) + 4;
        this.g = this.e;
    }

    protected synchronized boolean b(long j) {
        int f = f();
        return f != 0 ? f != 1 ? f == 2 : c(j) != null : false;
    }
}
