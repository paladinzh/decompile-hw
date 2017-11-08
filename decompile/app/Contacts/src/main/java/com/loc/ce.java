package com.loc;

import android.content.Context;
import android.os.Looper;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: CgiManager */
public class ce {
    Looper a = null;
    a b = null;
    private Context c;
    private int d = 9;
    private ArrayList<cd> e = new ArrayList();
    private int f = -113;
    private TelephonyManager g;
    private Object h;
    private long i = 0;
    private JSONObject j;
    private PhoneStateListener k;
    private CellLocation l;
    private boolean m = false;
    private Object n = new Object();

    /* compiled from: CgiManager */
    class a extends Thread {
        final /* synthetic */ ce a;

        a(ce ceVar) {
            this.a = ceVar;
        }

        public void run() {
            try {
                synchronized (this.a.n) {
                    if (!this.a.m) {
                        Looper.prepare();
                        this.a.a = Looper.myLooper();
                        this.a.q();
                        super.run();
                    }
                }
                if (this.a.a != null) {
                    Looper.loop();
                }
            } catch (Throwable th) {
                e.a(th, "CgiManager", "ListenerThread");
            }
        }
    }

    public ce(Context context, JSONObject jSONObject) {
        if (context != null) {
            p();
            this.g = (TelephonyManager) cw.a(context, "phone");
            this.j = jSONObject;
            this.c = context;
        } else {
            p();
            this.g = (TelephonyManager) cw.a(context, "phone");
            this.j = jSONObject;
            this.c = context;
        }
        try {
            this.d = cw.a(this.g.getCellLocation(), context);
        } catch (Throwable th) {
            e.a(th, "CgiManager", "CgiManager");
            this.d = 9;
        }
        if (this.b == null) {
            this.b = new a(this);
            this.b.setName("listener");
            this.b.start();
        }
    }

    private CellLocation a(List<?> list) {
        int i;
        CellLocation gsmCellLocation;
        Throwable th;
        CellLocation cellLocation;
        int i2;
        CellLocation cellLocation2;
        if (list == null || list.isEmpty()) {
            return null;
        }
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        int i3 = 0;
        CellLocation cellLocation3 = null;
        int i4 = 0;
        CellLocation cellLocation4 = null;
        while (i3 < list.size()) {
            Object obj = list.get(i3);
            if (obj != null) {
                try {
                    Class loadClass = systemClassLoader.loadClass("android.telephony.CellInfoGsm");
                    Class loadClass2 = systemClassLoader.loadClass("android.telephony.CellInfoWcdma");
                    Class loadClass3 = systemClassLoader.loadClass("android.telephony.CellInfoLte");
                    Class loadClass4 = systemClassLoader.loadClass("android.telephony.CellInfoCdma");
                    i = !loadClass.isInstance(obj) ? !loadClass2.isInstance(obj) ? !loadClass3.isInstance(obj) ? !loadClass4.isInstance(obj) ? 0 : 4 : 3 : 2 : 1;
                    if (i > 0) {
                        Object obj2 = null;
                        if (i == 1) {
                            obj2 = loadClass.cast(obj);
                        } else if (i == 2) {
                            obj2 = loadClass2.cast(obj);
                        } else if (i == 3) {
                            obj2 = loadClass3.cast(obj);
                        } else if (i == 4) {
                            obj2 = loadClass4.cast(obj);
                        }
                        try {
                            obj = cu.a(obj2, "getCellIdentity", new Object[0]);
                            if (obj != null) {
                                if (i != 4) {
                                    int b;
                                    int b2;
                                    if (i != 3) {
                                        b = cu.b(obj, "getLac", new Object[0]);
                                        b2 = cu.b(obj, "getCid", new Object[0]);
                                        gsmCellLocation = new GsmCellLocation();
                                        try {
                                            gsmCellLocation.setLacAndCid(b, b2);
                                        } catch (Throwable th2) {
                                            cellLocation3 = gsmCellLocation;
                                            th = th2;
                                            cellLocation = cellLocation4;
                                            i2 = i;
                                        }
                                    } else {
                                        b = cu.b(obj, "getTac", new Object[0]);
                                        b2 = cu.b(obj, "getCi", new Object[0]);
                                        gsmCellLocation = new GsmCellLocation();
                                        gsmCellLocation.setLacAndCid(b, b2);
                                    }
                                    cellLocation3 = gsmCellLocation;
                                } else {
                                    gsmCellLocation = new CdmaCellLocation();
                                    try {
                                        gsmCellLocation.setCellLocationData(cu.b(obj, "getBasestationId", new Object[0]), cu.b(obj, "getLatitude", new Object[0]), cu.b(obj, "getLongitude", new Object[0]), cu.b(obj, "getSystemId", new Object[0]), cu.b(obj, "getNetworkId", new Object[0]));
                                        cellLocation4 = gsmCellLocation;
                                    } catch (Throwable th3) {
                                        cellLocation = gsmCellLocation;
                                        th = th3;
                                        i2 = i;
                                    }
                                }
                                gsmCellLocation = cellLocation3;
                                break;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            cellLocation = cellLocation4;
                            i2 = i;
                            e.a(th, "CgiManager", "getCgi");
                            cellLocation2 = cellLocation3;
                            i3++;
                            cellLocation3 = cellLocation2;
                            i4 = i2;
                            cellLocation4 = cellLocation;
                        }
                    }
                    cellLocation = cellLocation4;
                    cellLocation2 = cellLocation3;
                    i2 = i;
                } catch (Throwable th22) {
                    Throwable th5 = th22;
                    cellLocation = cellLocation4;
                    i2 = i4;
                    th = th5;
                    e.a(th, "CgiManager", "getCgi");
                    cellLocation2 = cellLocation3;
                    i3++;
                    cellLocation3 = cellLocation2;
                    i4 = i2;
                    cellLocation4 = cellLocation;
                }
            } else {
                cellLocation = cellLocation4;
                cellLocation2 = cellLocation3;
                i2 = i4;
            }
            i3++;
            cellLocation3 = cellLocation2;
            i4 = i2;
            cellLocation4 = cellLocation;
        }
        i = i4;
        gsmCellLocation = cellLocation3;
        if (i == 4) {
            gsmCellLocation = cellLocation4;
        }
        return gsmCellLocation;
    }

    private void a(int i) {
        if (i != -113) {
            this.f = i;
            switch (this.d) {
                case 1:
                case 2:
                    if (!this.e.isEmpty()) {
                        try {
                            ((cd) this.e.get(0)).j = this.f;
                            break;
                        } catch (Throwable th) {
                            e.a(th, "CgiManager", "hdlCgiSigStrenChange");
                            break;
                        }
                    }
                    break;
            }
            return;
        }
        this.f = -113;
    }

    private cd b(NeighboringCellInfo neighboringCellInfo) {
        if (cw.c() < 5) {
            return null;
        }
        try {
            cd cdVar = new cd(1);
            String[] a = cw.a(this.g);
            cdVar.a = a[0];
            cdVar.b = a[1];
            cdVar.c = cu.b(neighboringCellInfo, "getLac", new Object[0]);
            cdVar.d = neighboringCellInfo.getCid();
            cdVar.j = cw.a(neighboringCellInfo.getRssi());
            return cdVar;
        } catch (Throwable th) {
            e.a(th, "CgiManager", "getGsm");
            return null;
        }
    }

    private void b(CellLocation cellLocation) {
        if (cellLocation != null && this.g != null) {
            this.e.clear();
            if (a(cellLocation)) {
                this.d = 1;
                this.e.add(d(cellLocation));
                List<NeighboringCellInfo> neighboringCellInfo = this.g.getNeighboringCellInfo();
                if (neighboringCellInfo != null && !neighboringCellInfo.isEmpty()) {
                    for (NeighboringCellInfo neighboringCellInfo2 : neighboringCellInfo) {
                        if (a(neighboringCellInfo2)) {
                            cd b = b(neighboringCellInfo2);
                            if (!(b == null || this.e.contains(b))) {
                                this.e.add(b);
                            }
                        }
                    }
                }
            }
        }
    }

    private void c(CellLocation cellLocation) {
        Object obj = null;
        if (cellLocation != null) {
            this.e.clear();
            if (cw.c() >= 5) {
                try {
                    if (this.h != null) {
                        Field declaredField = cellLocation.getClass().getDeclaredField("mGsmCellLoc");
                        if (!declaredField.isAccessible()) {
                            declaredField.setAccessible(true);
                        }
                        CellLocation cellLocation2 = (GsmCellLocation) declaredField.get(cellLocation);
                        if (cellLocation2 != null) {
                            if (a(cellLocation2)) {
                                b(cellLocation2);
                                int i = 1;
                                if (r0 != null) {
                                    return;
                                }
                            }
                        }
                        Object obj2 = null;
                        if (obj2 != null) {
                            return;
                        }
                    }
                } catch (Throwable th) {
                    e.a(th, "CgiManager", "hdlCdmaLocChange");
                }
                if (a(cellLocation)) {
                    this.d = 2;
                    String[] a = cw.a(this.g);
                    cd cdVar = new cd(2);
                    cdVar.a = a[0];
                    cdVar.b = a[1];
                    cdVar.g = cu.b(cellLocation, "getSystemId", new Object[0]);
                    cdVar.h = cu.b(cellLocation, "getNetworkId", new Object[0]);
                    cdVar.i = cu.b(cellLocation, "getBaseStationId", new Object[0]);
                    cdVar.j = this.f;
                    cdVar.e = cu.b(cellLocation, "getBaseStationLatitude", new Object[0]);
                    cdVar.f = cu.b(cellLocation, "getBaseStationLongitude", new Object[0]);
                    if (cdVar.e >= 0) {
                        if (cdVar.f >= 0) {
                            if (cdVar.e == Integer.MAX_VALUE) {
                                cdVar.e = 0;
                                cdVar.f = 0;
                            } else if (cdVar.f == Integer.MAX_VALUE) {
                                cdVar.e = 0;
                                cdVar.f = 0;
                            } else if (cdVar.e == cdVar.f && cdVar.e > 0) {
                                cdVar.e = 0;
                                cdVar.f = 0;
                            } else {
                                int i2 = 1;
                            }
                            if (obj == null) {
                            }
                            if (!this.e.contains(cdVar)) {
                                this.e.add(cdVar);
                            }
                        }
                    }
                    cdVar.e = 0;
                    cdVar.f = 0;
                    if (obj == null) {
                    }
                    if (this.e.contains(cdVar)) {
                        this.e.add(cdVar);
                    }
                }
            }
        }
    }

    private cd d(CellLocation cellLocation) {
        GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
        cd cdVar = new cd(1);
        String[] a = cw.a(this.g);
        cdVar.a = a[0];
        cdVar.b = a[1];
        cdVar.c = gsmCellLocation.getLac();
        cdVar.d = gsmCellLocation.getCid();
        cdVar.j = this.f;
        return cdVar;
    }

    public static int k() {
        int i = 0;
        try {
            Class.forName("android.telephony.MSimTelephonyManager");
            i = 1;
        } catch (Throwable th) {
        }
        if (i != 0) {
            return i;
        }
        try {
            Class.forName("android.telephony.TelephonyManager2");
            return 2;
        } catch (Throwable th2) {
            return i;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void l() {
        if (!cw.a(this.c)) {
            if (this.g != null) {
                CellLocation m = m();
                if (!a(m)) {
                    m = n();
                }
                if (a(m)) {
                    this.l = m;
                }
            }
        }
        if (a(this.l)) {
            switch (cw.a(this.l, this.c)) {
                case 1:
                    b(this.l);
                    break;
                case 2:
                    c(this.l);
                    break;
            }
        }
    }

    private CellLocation m() {
        CellLocation cellLocation = null;
        TelephonyManager telephonyManager = this.g;
        if (telephonyManager == null) {
            return cellLocation;
        }
        CellLocation cellLocation2;
        try {
            cellLocation2 = telephonyManager.getCellLocation();
        } catch (Throwable th) {
            e.a(th, "CgiManager", "getSim1Cgi4");
            cellLocation2 = cellLocation;
        }
        if (a(cellLocation2)) {
            return cellLocation2;
        }
        try {
            cellLocation2 = a((List) cu.a(telephonyManager, "getAllCellInfo", new Object[0]));
        } catch (NoSuchMethodException e) {
        } catch (Throwable th2) {
            e.a(th2, "CgiManager", "getSim1Cgi2");
        }
        if (a(cellLocation2)) {
            return cellLocation2;
        }
        Object a;
        try {
            a = cu.a(telephonyManager, "getCellLocationExt", Integer.valueOf(1));
            if (a != null) {
                cellLocation2 = (CellLocation) a;
            }
        } catch (NoSuchMethodException e2) {
        } catch (Throwable th22) {
            e.a(th22, "CgiManager", "getSim1Cgi1");
        }
        if (a(cellLocation2)) {
            return cellLocation2;
        }
        try {
            a = cu.a(telephonyManager, "getCellLocationGemini", Integer.valueOf(1));
            if (a != null) {
                cellLocation2 = (CellLocation) a;
            }
        } catch (NoSuchMethodException e3) {
        } catch (Throwable th222) {
            e.a(th222, "CgiManager", "getSim1Cgi");
        }
        return !a(cellLocation2) ? cellLocation2 : cellLocation2;
    }

    private CellLocation n() {
        Object cast;
        CellLocation a;
        CellLocation a2;
        Object obj = this.h;
        if (obj == null) {
            return null;
        }
        String str;
        List list;
        try {
            Class o = o();
            if (o.isInstance(obj)) {
                cast = o.cast(obj);
                str = "getCellLocation";
                a = cu.a(cast, str, new Object[0]);
                if (a == null) {
                    try {
                        a = cu.a(cast, str, Integer.valueOf(1));
                    } catch (NoSuchMethodException e) {
                    } catch (Throwable th) {
                        e.a(th, "CgiManager", "getSim2Cgi14");
                    }
                }
                if (a == null) {
                    try {
                        a2 = cu.a(cast, "getCellLocationGemini", Integer.valueOf(1));
                    } catch (NoSuchMethodException e2) {
                    } catch (Throwable th2) {
                        e.a(th2, "CgiManager", "getSim2Cgi13");
                    }
                    if (a2 == null) {
                        try {
                            list = (List) cu.a(cast, "getAllCellInfo", new Object[0]);
                        } catch (Throwable th22) {
                            e.a(th22, "CgiManager", "getSim2Cgi12");
                            list = null;
                            a2 = a(list);
                            if (a2 == null) {
                            }
                            if (a2 != null) {
                                a2 = a2;
                                return a2;
                            }
                            a2 = null;
                            return a2;
                        } catch (Throwable th222) {
                            e.a(th222, "CgiManager", "getSim2Cgi1");
                            list = null;
                            a2 = a(list);
                            if (a2 == null) {
                            }
                            if (a2 != null) {
                                a2 = a2;
                                return a2;
                            }
                            a2 = null;
                            return a2;
                        }
                        a2 = a(list);
                        if (a2 == null) {
                        }
                    }
                }
                a2 = a;
                if (a2 == null) {
                    list = (List) cu.a(cast, "getAllCellInfo", new Object[0]);
                    a2 = a(list);
                    if (a2 == null) {
                    }
                }
            } else {
                a2 = null;
            }
        } catch (NoSuchMethodException e3) {
            a = null;
            if (a == null) {
                a = cu.a(cast, str, Integer.valueOf(1));
            }
            if (a == null) {
                a2 = cu.a(cast, "getCellLocationGemini", Integer.valueOf(1));
                if (a2 == null) {
                    list = (List) cu.a(cast, "getAllCellInfo", new Object[0]);
                    a2 = a(list);
                    if (a2 == null) {
                    }
                }
                if (a2 != null) {
                    a2 = a2;
                    return a2;
                }
            }
            a2 = a;
            if (a2 == null) {
                list = (List) cu.a(cast, "getAllCellInfo", new Object[0]);
                a2 = a(list);
                if (a2 == null) {
                }
            }
            if (a2 != null) {
                a2 = a2;
                return a2;
            }
        } catch (Throwable th2222) {
            e.a(th2222, "CgiManager", "getSim2Cgi");
        }
        if (a2 != null) {
            a2 = a2;
            return a2;
        }
        a2 = null;
        return a2;
    }

    private Class<?> o() {
        String str;
        Class<?> cls = null;
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        switch (k()) {
            case 0:
                str = "android.telephony.TelephonyManager";
                break;
            case 1:
                str = "android.telephony.MSimTelephonyManager";
                break;
            case 2:
                str = "android.telephony.TelephonyManager2";
                break;
            default:
                str = cls;
                break;
        }
        try {
            cls = systemClassLoader.loadClass(str);
        } catch (Throwable th) {
            e.a(th, "CgiManager", "getSim2TmClass");
        }
        return cls;
    }

    private void p() {
        Object obj = 1;
        JSONObject jSONObject = this.j;
        if (jSONObject != null) {
            try {
                if (jSONObject.has("cellupdate") && jSONObject.getString("cellupdate").equals("0")) {
                    obj = null;
                }
            } catch (Throwable th) {
                e.a(th, "CgiManager", "updateCgi1");
            }
        }
        if (obj != null) {
            try {
                CellLocation.requestLocationUpdate();
            } catch (Throwable th2) {
                e.a(th2, "CgiManager", "updateCgi");
            }
            this.i = cw.b();
        }
    }

    private void q() {
        int i = 0;
        this.k = new PhoneStateListener(this) {
            final /* synthetic */ ce a;

            {
                this.a = r1;
            }

            public void onCellLocationChanged(CellLocation cellLocation) {
                try {
                    if (this.a.a(cellLocation)) {
                        this.a.l = cellLocation;
                    }
                } catch (Throwable th) {
                    e.a(th, "CgiManager", "initPhoneStateListener7");
                }
            }

            public void onServiceStateChanged(ServiceState serviceState) {
                try {
                    switch (serviceState.getState()) {
                        case 0:
                            this.a.p();
                            return;
                        case 1:
                            this.a.r();
                            return;
                        default:
                            return;
                    }
                } catch (Throwable th) {
                    e.a(th, "CgiManager", "initPhoneStateListener4");
                }
                e.a(th, "CgiManager", "initPhoneStateListener4");
            }

            public void onSignalStrengthChanged(int i) {
                int i2 = -113;
                try {
                    switch (this.a.d) {
                        case 1:
                            i2 = cw.a(i);
                            break;
                        case 2:
                            i2 = cw.a(i);
                            break;
                    }
                    this.a.a(i2);
                } catch (Throwable th) {
                    e.a(th, "CgiManager", "initPhoneStateListener6");
                }
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                if (signalStrength != null) {
                    int i = -113;
                    try {
                        switch (this.a.d) {
                            case 1:
                                i = cw.a(signalStrength.getGsmSignalStrength());
                                break;
                            case 2:
                                i = signalStrength.getCdmaDbm();
                                break;
                        }
                        this.a.a(i);
                    } catch (Throwable th) {
                        e.a(th, "CgiManager", "initPhoneStateListener5");
                    }
                }
            }
        };
        String str = "android.telephony.PhoneStateListener";
        String str2 = "";
        if (cw.c() >= 7) {
            try {
                i = cu.b(str, "LISTEN_SIGNAL_STRENGTHS");
            } catch (Throwable th) {
                e.a(th, "CgiManager", "initPhoneStateListener2");
            }
        } else {
            try {
                i = cu.b(str, "LISTEN_SIGNAL_STRENGTH");
            } catch (Throwable th2) {
                e.a(th2, "CgiManager", "initPhoneStateListener3");
            }
        }
        if (i != 0) {
            try {
                this.g.listen(this.k, i | 16);
            } catch (Throwable th3) {
                e.a(th3, "CgiManager", "initPhoneStateListener1");
            }
        } else {
            this.g.listen(this.k, 16);
        }
        try {
            switch (k()) {
                case 0:
                    this.h = cw.a(this.c, "phone2");
                    return;
                case 1:
                    this.h = cw.a(this.c, "phone_msim");
                    return;
                case 2:
                    this.h = cw.a(this.c, "phone2");
                    return;
                default:
                    return;
            }
        } catch (Throwable th32) {
            e.a(th32, "CgiManager", "initPhoneStateListener");
        }
        e.a(th32, "CgiManager", "initPhoneStateListener");
    }

    private void r() {
        this.l = null;
        this.d = 9;
        this.e.clear();
    }

    public ArrayList<cd> a() {
        return this.e;
    }

    public void a(JSONObject jSONObject) {
        this.j = jSONObject;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(CellLocation cellLocation) {
        boolean z = false;
        if (cellLocation == null) {
            return false;
        }
        boolean z2 = true;
        switch (cw.a(cellLocation, this.c)) {
            case 1:
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                if (!(gsmCellLocation.getLac() == -1 || gsmCellLocation.getLac() == 0 || gsmCellLocation.getLac() > 65535 || gsmCellLocation.getCid() == -1 || gsmCellLocation.getCid() == 0 || gsmCellLocation.getCid() == 65535 || gsmCellLocation.getCid() >= 268435455)) {
                }
            case 2:
                try {
                    if (cu.b(cellLocation, "getSystemId", new Object[0]) <= 0 || cu.b(cellLocation, "getNetworkId", new Object[0]) < 0 || cu.b(cellLocation, "getBaseStationId", new Object[0]) < 0) {
                        z2 = false;
                    }
                } catch (Throwable th) {
                    e.a(th, "CgiManager", "cgiUseful");
                }
                z = z2;
                break;
        }
        z = z2;
        if (!z) {
            this.d = 9;
        }
        return z;
    }

    public boolean a(NeighboringCellInfo neighboringCellInfo) {
        if (neighboringCellInfo == null) {
            return false;
        }
        boolean z = true;
        if (neighboringCellInfo.getLac() == -1 || neighboringCellInfo.getLac() == 0 || neighboringCellInfo.getLac() > 65535 || neighboringCellInfo.getCid() == -1 || neighboringCellInfo.getCid() == 0 || neighboringCellInfo.getCid() == 65535 || neighboringCellInfo.getCid() >= 268435455) {
            z = false;
        }
        return z;
    }

    public boolean a(boolean z) {
        if (z || this.i == 0) {
            return false;
        }
        return ((cw.b() - this.i) > 30000 ? 1 : ((cw.b() - this.i) == 30000 ? 0 : -1)) >= 0;
    }

    public cd b() {
        ArrayList arrayList = this.e;
        return arrayList.size() < 1 ? null : (cd) arrayList.get(0);
    }

    public int c() {
        return this.d;
    }

    public CellLocation d() {
        CellLocation cellLocation = null;
        if (this.g == null) {
            return null;
        }
        try {
            cellLocation = this.g.getCellLocation();
            if (a(cellLocation)) {
                this.l = cellLocation;
            }
        } catch (Throwable th) {
            e.a(th, "CgiManager", "getCellLocation");
        }
        return cellLocation;
    }

    public TelephonyManager e() {
        return this.g;
    }

    public void f() {
        l();
    }

    public void g() {
        r();
    }

    public void h() {
        p();
    }

    public void i() {
        if (!(this.g == null || this.k == null)) {
            try {
                this.g.listen(this.k, 0);
            } catch (Throwable th) {
                e.a(th, "CgiManager", "destroy");
            }
        }
        this.k = null;
        synchronized (this.n) {
            this.m = true;
            if (this.a != null) {
                this.a.quit();
                this.a = null;
            }
        }
        this.b = null;
        this.e.clear();
        this.f = -113;
        this.g = null;
        this.h = null;
    }

    public void j() {
        switch (this.d) {
            case 1:
                if (!this.e.isEmpty()) {
                    return;
                }
                break;
            case 2:
                if (!this.e.isEmpty()) {
                    return;
                }
                break;
            default:
                return;
        }
        this.d = 9;
    }
}
