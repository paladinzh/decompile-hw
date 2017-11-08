package com.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsStatus.NmeaListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TreeMap;

/* compiled from: Unknown */
public final class dl {
    private static int I = 10000;
    private static String[] J = new String[]{"android.permission.READ_PHONE_STATE", "android.permission.ACCESS_WIFI_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CHANGE_WIFI_STATE", "android.permission.ACCESS_NETWORK_STATE"};
    static String a = "";
    protected static boolean b = true;
    protected static boolean c = false;
    private static dl z = null;
    private dn A = null;
    private do B = null;
    private CellLocation C = null;
    private dp D = null;
    private List E = new ArrayList();
    private Timer F = null;
    private Thread G = null;
    private Looper H = null;
    Object d = new Object();
    boolean e = false;
    private Context f = null;
    private TelephonyManager g = null;
    private LocationManager h = null;
    private WifiManager i = null;
    private SensorManager j = null;
    private String k = "";
    private String l = "";
    private String m = "";
    private boolean n = false;
    private int o = 0;
    private boolean p = false;
    private long q = -1;
    private String r = "";
    private String s = "";
    private int t = 0;
    private int u = 0;
    private int v = 0;
    private String w = "";
    private long x = 0;
    private long y = 0;

    private dl(Context context) {
        if (context != null) {
            this.f = context;
            this.k = Build.MODEL;
            this.g = (TelephonyManager) context.getSystemService("phone");
            this.h = (LocationManager) context.getSystemService(NetUtil.REQ_QUERY_LOCATION);
            this.i = (WifiManager) context.getSystemService("wifi");
            this.j = (SensorManager) context.getSystemService("sensor");
            if (this.g != null && this.i != null) {
                try {
                    this.l = this.g.getDeviceId();
                } catch (Exception e) {
                }
                this.m = this.g.getSubscriberId();
                if (this.i.getConnectionInfo() != null) {
                    this.s = this.i.getConnectionInfo().getMacAddress();
                    if (this.s != null && this.s.length() > 0) {
                        this.s = this.s.replace(":", "");
                    }
                }
                String[] b = b(this.g);
                this.t = Integer.parseInt(b[0]);
                this.u = Integer.parseInt(b[1]);
                this.v = this.g.getNetworkType();
                this.w = context.getPackageName();
                this.n = this.g.getPhoneType() == 2;
            }
        }
    }

    private CellLocation A() {
        CellLocation b;
        if (this.g == null) {
            return null;
        }
        try {
            b = b((List) di.a(this.g, "getAllCellInfo", new Object[0]));
        } catch (NoSuchMethodException e) {
            b = null;
            return b;
        } catch (Exception e2) {
            b = null;
            return b;
        }
        return b;
    }

    private static int a(CellLocation cellLocation, Context context) {
        if (System.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 1 || cellLocation == null) {
            return 9;
        }
        if (cellLocation instanceof GsmCellLocation) {
            return 1;
        }
        try {
            Class.forName("android.telephony.cdma.CdmaCellLocation");
            return 2;
        } catch (Exception e) {
            return 9;
        }
    }

    protected static dl a(Context context) {
        if (z == null && c(context)) {
            Object obj;
            LocationManager locationManager = (LocationManager) context.getSystemService(NetUtil.REQ_QUERY_LOCATION);
            if (locationManager != null) {
                for (String str : locationManager.getAllProviders()) {
                    if (str.equals("passive") || str.equals(GeocodeSearch.GPS)) {
                        obj = 1;
                        break;
                    }
                }
            }
            obj = null;
            if (obj != null) {
                z = new dl(context);
            }
        }
        return z;
    }

    private void a(BroadcastReceiver broadcastReceiver) {
        if (broadcastReceiver != null && this.f != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
            this.f.registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private static void a(WifiManager wifiManager) {
        if (wifiManager != null) {
            try {
                di.a(wifiManager, "startScanActive", new Object[0]);
            } catch (Exception e) {
                wifiManager.startScan();
            }
        }
    }

    static /* synthetic */ void a(dl dlVar, NmeaListener nmeaListener) {
        if (dlVar.h != null && nmeaListener != null) {
            dlVar.h.addNmeaListener(nmeaListener);
        }
    }

    static /* synthetic */ void a(dl dlVar, PhoneStateListener phoneStateListener) {
        if (dlVar.g != null) {
            dlVar.g.listen(phoneStateListener, 273);
        }
    }

    private static void a(List list) {
        if (list != null && list.size() > 0) {
            Object hashMap = new HashMap();
            for (int i = 0; i < list.size(); i++) {
                ScanResult scanResult = (ScanResult) list.get(i);
                if (scanResult.SSID == null) {
                    scanResult.SSID = "null";
                }
                hashMap.put(Integer.valueOf(scanResult.level), scanResult);
            }
            TreeMap treeMap = new TreeMap(Collections.reverseOrder());
            treeMap.putAll(hashMap);
            list.clear();
            for (Integer num : treeMap.keySet()) {
                list.add(treeMap.get(num));
            }
            hashMap.clear();
            treeMap.clear();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(CellLocation cellLocation) {
        boolean z = false;
        if (cellLocation == null) {
            return false;
        }
        boolean z2 = true;
        switch (a(cellLocation, this.f)) {
            case 1:
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                if (!(gsmCellLocation.getLac() == -1 || gsmCellLocation.getLac() == 0 || gsmCellLocation.getLac() > 65535 || gsmCellLocation.getCid() == -1 || gsmCellLocation.getCid() == 0 || gsmCellLocation.getCid() == 65535 || gsmCellLocation.getCid() >= 268435455)) {
                }
            case 2:
                try {
                    if (di.b(cellLocation, "getSystemId", new Object[0]) > 0 && di.b(cellLocation, "getNetworkId", new Object[0]) >= 0) {
                        if (di.b(cellLocation, "getBaseStationId", new Object[0]) < 0) {
                            z2 = false;
                        }
                    }
                } catch (Exception e) {
                }
            default:
                z = z2;
                break;
        }
        return z;
    }

    private static boolean a(Object obj) {
        try {
            Method declaredMethod = WifiManager.class.getDeclaredMethod("isScanAlwaysAvailable", null);
            if (declaredMethod != null) {
                return ((Boolean) declaredMethod.invoke(obj, null)).booleanValue();
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static int b(Object obj) {
        try {
            Method declaredMethod = Sensor.class.getDeclaredMethod("getMinDelay", null);
            if (declaredMethod != null) {
                return ((Integer) declaredMethod.invoke(obj, null)).intValue();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private static CellLocation b(List list) {
        int i;
        CellLocation gsmCellLocation;
        int i2;
        if (list == null || list.isEmpty()) {
            return null;
        }
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        int i3 = 0;
        CellLocation cellLocation = null;
        Object obj = null;
        CellLocation cellLocation2 = null;
        while (i3 < list.size()) {
            CellLocation cellLocation3;
            CellLocation cellLocation4;
            Object obj2 = list.get(i3);
            if (obj2 != null) {
                try {
                    Class loadClass = systemClassLoader.loadClass("android.telephony.CellInfoGsm");
                    Class loadClass2 = systemClassLoader.loadClass("android.telephony.CellInfoWcdma");
                    Class loadClass3 = systemClassLoader.loadClass("android.telephony.CellInfoLte");
                    Class loadClass4 = systemClassLoader.loadClass("android.telephony.CellInfoCdma");
                    i = !loadClass.isInstance(obj2) ? !loadClass2.isInstance(obj2) ? !loadClass3.isInstance(obj2) ? !loadClass4.isInstance(obj2) ? 0 : 4 : 3 : 2 : 1;
                    if (i > 0) {
                        obj = null;
                        if (i == 1) {
                            obj = loadClass.cast(obj2);
                        } else if (i == 2) {
                            obj = loadClass2.cast(obj2);
                        } else if (i == 3) {
                            obj = loadClass3.cast(obj2);
                        } else if (i == 4) {
                            obj = loadClass4.cast(obj2);
                        }
                        try {
                            obj2 = di.a(obj, "getCellIdentity", new Object[0]);
                            if (obj2 != null) {
                                if (i != 4) {
                                    int b;
                                    int b2;
                                    if (i != 3) {
                                        b = di.b(obj2, "getLac", new Object[0]);
                                        b2 = di.b(obj2, "getCid", new Object[0]);
                                        gsmCellLocation = new GsmCellLocation();
                                        try {
                                            gsmCellLocation.setLacAndCid(b, b2);
                                        } catch (Exception e) {
                                            cellLocation = gsmCellLocation;
                                            i2 = i;
                                        }
                                    } else {
                                        b = di.b(obj2, "getTac", new Object[0]);
                                        b2 = di.b(obj2, "getCi", new Object[0]);
                                        gsmCellLocation = new GsmCellLocation();
                                        gsmCellLocation.setLacAndCid(b, b2);
                                    }
                                    cellLocation = gsmCellLocation;
                                    break;
                                }
                                gsmCellLocation = new CdmaCellLocation();
                                try {
                                    gsmCellLocation.setCellLocationData(di.b(obj2, "getBasestationId", new Object[0]), di.b(obj2, "getLatitude", new Object[0]), di.b(obj2, "getLongitude", new Object[0]), di.b(obj2, "getSystemId", new Object[0]), di.b(obj2, "getNetworkId", new Object[0]));
                                    cellLocation2 = gsmCellLocation;
                                    break;
                                } catch (Exception e2) {
                                    cellLocation2 = gsmCellLocation;
                                    i2 = i;
                                }
                            }
                        } catch (Exception e3) {
                            i2 = i;
                        }
                    }
                    cellLocation3 = cellLocation2;
                    cellLocation4 = cellLocation;
                    int i4 = i;
                } catch (Exception e4) {
                }
                i3++;
                cellLocation = cellLocation4;
                obj = r1;
                cellLocation2 = cellLocation3;
            }
            cellLocation3 = cellLocation2;
            cellLocation4 = cellLocation;
            Object obj3 = obj;
            i3++;
            cellLocation = cellLocation4;
            obj = obj3;
            cellLocation2 = cellLocation3;
        }
        i = obj;
        return i != 4 ? cellLocation : cellLocation2;
    }

    private void b(BroadcastReceiver broadcastReceiver) {
        if (broadcastReceiver != null && this.f != null) {
            try {
                this.f.unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
            }
        }
    }

    protected static boolean b(Context context) {
        if (context == null) {
            return true;
        }
        boolean z;
        if (!Secure.getString(context.getContentResolver(), "mock_location").equals("0")) {
            PackageManager packageManager = context.getPackageManager();
            List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(128);
            String str = "android.permission.ACCESS_MOCK_LOCATION";
            String packageName = context.getPackageName();
            z = false;
            for (ApplicationInfo applicationInfo : installedApplications) {
                if (z) {
                    break;
                }
                boolean z2;
                try {
                    String[] strArr = packageManager.getPackageInfo(applicationInfo.packageName, 4096).requestedPermissions;
                    if (strArr != null) {
                        int length = strArr.length;
                        int i = 0;
                        while (i < length) {
                            if (!strArr[i].equals(str)) {
                                i++;
                            } else if (!applicationInfo.packageName.equals(packageName)) {
                                z2 = true;
                                z = z2;
                            }
                        }
                    }
                } catch (Exception e) {
                    z2 = z;
                }
            }
        } else {
            z = false;
        }
        return z;
    }

    private static String[] b(TelephonyManager telephonyManager) {
        String str = null;
        int i = 0;
        if (telephonyManager != null) {
            str = telephonyManager.getNetworkOperator();
        }
        String[] strArr = new String[]{"0", "0"};
        if (TextUtils.isDigitsOnly(str) && str.length() > 4) {
            strArr[0] = str.substring(0, 3);
            char[] toCharArray = str.substring(3).toCharArray();
            while (i < toCharArray.length && Character.isDigit(toCharArray[i])) {
                i++;
            }
            strArr[1] = str.substring(3, i + 3);
        }
        return strArr;
    }

    private static boolean c(Context context) {
        try {
            String[] strArr = context.getPackageManager().getPackageInfo(context.getPackageName(), 4096).requestedPermissions;
            for (Object obj : J) {
                boolean z;
                if (!(strArr == null || obj == null)) {
                    for (String equals : strArr) {
                        if (equals.equals(obj)) {
                            z = true;
                            break;
                        }
                    }
                }
                z = false;
                if (!z) {
                    return false;
                }
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void z() {
        if (this.i != null) {
            try {
                if (b) {
                    a(this.i);
                }
            } catch (Exception e) {
            }
        }
    }

    protected final List a(float f) {
        List arrayList = new ArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(f) <= ContentUtil.FONT_SIZE_NORMAL) {
            f = ContentUtil.FONT_SIZE_NORMAL;
        }
        if (c()) {
            CellLocation cellLocation = (CellLocation) j().get(1);
            if (cellLocation != null && (cellLocation instanceof GsmCellLocation)) {
                arrayList.add(Integer.valueOf(((GsmCellLocation) cellLocation).getLac()));
                arrayList.add(Integer.valueOf(((GsmCellLocation) cellLocation).getCid()));
                if (((double) (currentTimeMillis - ((Long) j().get(0)).longValue())) <= 50000.0d / ((double) f)) {
                    arrayList.add(Integer.valueOf(1));
                } else {
                    arrayList.add(Integer.valueOf(0));
                }
            }
        }
        return arrayList;
    }

    protected final List a(boolean z) {
        int i = 1;
        int i2 = 0;
        List arrayList = new ArrayList();
        if (!d()) {
            return new ArrayList();
        }
        List arrayList2 = new ArrayList();
        synchronized (this) {
            if (!z) {
                if ((System.currentTimeMillis() - this.x >= 3500 ? 1 : 0) != 0) {
                    i = 0;
                }
                if (i == 0) {
                }
            }
            arrayList2.add(Long.valueOf(this.x));
            while (i2 < this.E.size()) {
                arrayList.add(this.E.get(i2));
                i2++;
            }
            arrayList2.add(arrayList);
        }
        return arrayList2;
    }

    protected final void a() {
        b();
        this.e = true;
        this.G = new dm(this, "");
        this.G.start();
    }

    protected final void a(int i) {
        if (i != I) {
            synchronized (this) {
                this.E.clear();
            }
            if (this.D != null) {
                b(this.D);
                this.D = null;
            }
            if (this.F != null) {
                this.F.cancel();
                this.F = null;
            }
            if (i >= 5000) {
                I = i;
                this.F = new Timer();
                this.D = new dp();
                a(this.D);
                z();
            }
        }
    }

    protected final String b(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return "null";
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null || ((Sensor) sensorList.get(i)).getName() == null || ((Sensor) sensorList.get(i)).getName().length() <= 0) ? "null" : ((Sensor) sensorList.get(i)).getName();
    }

    protected final List b(float f) {
        List arrayList = new ArrayList();
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(f) <= ContentUtil.FONT_SIZE_NORMAL) {
            f = ContentUtil.FONT_SIZE_NORMAL;
        }
        if (c()) {
            CellLocation cellLocation = (CellLocation) j().get(1);
            if (cellLocation != null && (cellLocation instanceof CdmaCellLocation)) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                arrayList.add(Integer.valueOf(cdmaCellLocation.getSystemId()));
                arrayList.add(Integer.valueOf(cdmaCellLocation.getNetworkId()));
                arrayList.add(Integer.valueOf(cdmaCellLocation.getBaseStationId()));
                arrayList.add(Integer.valueOf(cdmaCellLocation.getBaseStationLongitude()));
                arrayList.add(Integer.valueOf(cdmaCellLocation.getBaseStationLatitude()));
                if (((double) (currentTimeMillis - ((Long) j().get(0)).longValue())) <= 50000.0d / ((double) f)) {
                    arrayList.add(Integer.valueOf(1));
                } else {
                    arrayList.add(Integer.valueOf(0));
                }
            }
        }
        return arrayList;
    }

    protected final void b() {
        synchronized (this.d) {
            this.e = false;
            if (this.A != null) {
                PhoneStateListener phoneStateListener = this.A;
                if (this.g != null) {
                    this.g.listen(phoneStateListener, 0);
                }
                this.A = null;
            }
            if (this.B != null) {
                NmeaListener nmeaListener = this.B;
                if (!(this.h == null || nmeaListener == null)) {
                    this.h.removeNmeaListener(nmeaListener);
                }
                this.B = null;
            }
            if (this.F != null) {
                this.F.cancel();
                this.F = null;
            }
            if (this.H != null) {
                this.H.quit();
                this.H = null;
            }
            if (this.G != null) {
                this.G.interrupt();
                this.G = null;
            }
        }
    }

    protected final double c(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return 0.0d;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null) ? 0.0d : (double) ((Sensor) sensorList.get(i)).getMaximumRange();
    }

    protected final boolean c() {
        CellLocation cellLocation = null;
        if (this.g != null && this.g.getSimState() == 5 && this.p) {
            return true;
        }
        if (this.g != null) {
            try {
                cellLocation = this.g.getCellLocation();
            } catch (Exception e) {
            }
            if (cellLocation != null) {
                this.y = System.currentTimeMillis();
                this.C = cellLocation;
                return true;
            }
        }
        return false;
    }

    protected final int d(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return 0;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null) ? 0 : b(sensorList.get(i));
    }

    protected final boolean d() {
        if (this.i != null) {
            if (this.i.isWifiEnabled() || a(this.i)) {
                return true;
            }
        }
        return false;
    }

    protected final int e(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return 0;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null) ? 0 : (int) (((double) ((Sensor) sensorList.get(i)).getPower()) * 100.0d);
    }

    protected final boolean e() {
        try {
            if (this.h != null && this.h.isProviderEnabled(GeocodeSearch.GPS)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    protected final double f(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return 0.0d;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null) ? 0.0d : (double) ((Sensor) sensorList.get(i)).getResolution();
    }

    protected final String f() {
        if (this.k == null) {
            this.k = Build.MODEL;
        }
        return this.k == null ? "" : this.k;
    }

    protected final byte g(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return Byte.MAX_VALUE;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null || ((Sensor) sensorList.get(i)).getType() > 127) ? Byte.MAX_VALUE : (byte) ((Sensor) sensorList.get(i)).getType();
    }

    protected final String g() {
        if (this.l == null && this.f != null) {
            this.g = (TelephonyManager) this.f.getSystemService("phone");
            if (this.g != null) {
                try {
                    this.l = this.g.getDeviceId();
                } catch (Exception e) {
                }
            }
        }
        return this.l == null ? "" : this.l;
    }

    protected final String h() {
        if (this.m == null && this.f != null) {
            this.g = (TelephonyManager) this.f.getSystemService("phone");
            if (this.g != null) {
                this.m = this.g.getSubscriberId();
            }
        }
        return this.m == null ? "" : this.m;
    }

    protected final String h(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return "null";
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null || ((Sensor) sensorList.get(i)).getVendor() == null || ((Sensor) sensorList.get(i)).getVendor().length() <= 0) ? "null" : ((Sensor) sensorList.get(i)).getVendor();
    }

    protected final byte i(int i) {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return Byte.MAX_VALUE;
        }
        List sensorList = this.j.getSensorList(-1);
        return (sensorList == null || sensorList.get(i) == null || ((Sensor) sensorList.get(i)).getType() > 127) ? Byte.MAX_VALUE : (byte) ((Sensor) sensorList.get(i)).getVersion();
    }

    protected final boolean i() {
        return this.n;
    }

    protected final List j() {
        if (System.getInt(this.f.getContentResolver(), "airplane_mode_on", 0) == 1) {
            return new ArrayList();
        }
        if (!c()) {
            return new ArrayList();
        }
        List arrayList = new ArrayList();
        if (!a(this.C)) {
            CellLocation A = A();
            if (a(A)) {
                this.y = System.currentTimeMillis();
                arrayList.add(Long.valueOf(this.y));
                arrayList.add(r0);
                return arrayList;
            }
        }
        Object obj = this.C;
        arrayList.add(Long.valueOf(this.y));
        arrayList.add(obj);
        return arrayList;
    }

    protected final byte k() {
        return !c() ? Byte.MIN_VALUE : (byte) this.o;
    }

    protected final List l() {
        List arrayList = new ArrayList();
        if (this.g == null || !c() || this.g.getSimState() == 1) {
            return arrayList;
        }
        int i = 0;
        for (NeighboringCellInfo neighboringCellInfo : this.g.getNeighboringCellInfo()) {
            if (i > 15) {
                break;
            } else if (!(neighboringCellInfo.getLac() == 0 || neighboringCellInfo.getLac() == 65535 || neighboringCellInfo.getCid() == 65535 || neighboringCellInfo.getCid() == 268435455)) {
                arrayList.add(neighboringCellInfo);
                i++;
            }
        }
        return arrayList;
    }

    protected final List m() {
        Object obj = 1;
        List arrayList = new ArrayList();
        long j = -1;
        String str = "";
        if (e()) {
            j = this.q;
            str = this.r;
        }
        String str2 = str;
        long j2 = j;
        String str3 = str2;
        if ((j2 > 0 ? 1 : null) == null) {
            j2 = System.currentTimeMillis() / 1000;
        }
        if (j2 > 2147483647L) {
            obj = null;
        }
        if (obj == null) {
            j2 /= 1000;
        }
        arrayList.add(Long.valueOf(j2));
        arrayList.add(str3);
        return arrayList;
    }

    protected final long n() {
        long j = this.q;
        if ((j > 0 ? 1 : null) == null) {
            return 0;
        }
        int length = String.valueOf(j).length();
        while (length != 13) {
            long j2 = length <= 13 ? j * 10 : j / 10;
            j = j2;
            length = String.valueOf(j2).length();
        }
        return j;
    }

    protected final String o() {
        if (this.s == null && this.f != null) {
            this.i = (WifiManager) this.f.getSystemService("wifi");
            if (!(this.i == null || this.i.getConnectionInfo() == null)) {
                this.s = this.i.getConnectionInfo().getMacAddress();
                if (this.s != null && this.s.length() > 0) {
                    this.s = this.s.replace(":", "");
                }
            }
        }
        return this.s == null ? "" : this.s;
    }

    protected final int p() {
        return this.t;
    }

    protected final int q() {
        return this.u;
    }

    protected final int r() {
        return this.v;
    }

    protected final String s() {
        if (this.w == null && this.f != null) {
            this.w = this.f.getPackageName();
        }
        return this.w == null ? "" : this.w;
    }

    protected final List t() {
        int i = 0;
        List arrayList = new ArrayList();
        if (d()) {
            List a = a(true);
            List list = (List) a.get(1);
            long longValue = ((Long) a.get(0)).longValue();
            a(list);
            arrayList.add(Long.valueOf(longValue));
            if (list != null && list.size() > 0) {
                while (i < list.size()) {
                    ScanResult scanResult = (ScanResult) list.get(i);
                    if (arrayList.size() - 1 >= 40) {
                        break;
                    }
                    if (scanResult != null) {
                        List arrayList2 = new ArrayList();
                        arrayList2.add(scanResult.BSSID.replace(":", ""));
                        arrayList2.add(Integer.valueOf(scanResult.level));
                        arrayList2.add(scanResult.SSID);
                        arrayList.add(arrayList2);
                    }
                    i++;
                }
            }
        }
        return arrayList;
    }

    protected final void u() {
        synchronized (this) {
            this.E.clear();
        }
        if (this.D != null) {
            b(this.D);
            this.D = null;
        }
        if (this.F != null) {
            this.F.cancel();
            this.F = null;
        }
        this.F = new Timer();
        this.D = new dp();
        a(this.D);
        z();
    }

    protected final void v() {
        synchronized (this) {
            this.E.clear();
        }
        if (this.D != null) {
            b(this.D);
            this.D = null;
        }
        if (this.F != null) {
            this.F.cancel();
            this.F = null;
        }
    }

    protected final byte w() {
        ArrayList arrayList = new ArrayList();
        if (this.j == null) {
            return (byte) 0;
        }
        List sensorList = this.j.getSensorList(-1);
        return sensorList != null ? (byte) sensorList.size() : (byte) 0;
    }

    protected final Context x() {
        return this.f;
    }
}
