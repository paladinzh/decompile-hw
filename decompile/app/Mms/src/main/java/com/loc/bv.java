package com.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.amap.api.services.core.AMapException;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import org.json.JSONObject;

/* compiled from: APS */
public class bv {
    private static int ae = -1;
    public static final StringBuilder c = new StringBuilder();
    private boolean A = false;
    private boolean B = false;
    private long C = 0;
    private long D = 0;
    private int E = 0;
    private String F = "00:00:00:00:00:00";
    private String G = null;
    private cs H = null;
    private Timer I = null;
    private TimerTask J = null;
    private int K = 0;
    private db L = null;
    private dh M = null;
    private int[] N = new int[]{0, 0, 0};
    private String O = null;
    private String P = null;
    private long Q = 0;
    private long R = 0;
    private String S = null;
    private cd T = null;
    private AmapLoc U = null;
    private String V = null;
    private Timer W = null;
    private TimerTask X = null;
    private String Y = null;
    private int Z = 0;
    public boolean a = false;
    private int aa = 0;
    private boolean ab = true;
    private boolean ac = true;
    private long ad = 0;
    co b = null;
    bw d;
    int e = -1;
    boolean f = false;
    AmapLoc g = null;
    Object h = new Object();
    public boolean i = false;
    int j = 12;
    boolean k = true;
    a l = new a(this);
    private Context m = null;
    private ConnectivityManager n = null;
    private cf o = null;
    private ce p;
    private ArrayList<ScanResult> q = new ArrayList();
    private ArrayList<ScanResult> r = new ArrayList();
    private HashMap<String, ArrayList<ScanResult>> s = new HashMap();
    private b t = new b();
    private WifiInfo u = null;
    private JSONObject v = null;
    private AmapLoc w = null;
    private long x = 0;
    private long y = 0;
    private long z = 0;

    /* compiled from: APS */
    class a implements com.loc.bw.a {
        final /* synthetic */ bv a;

        a(bv bvVar) {
            this.a = bvVar;
        }

        public void a(int i) {
            this.a.e = i;
        }
    }

    /* compiled from: APS */
    private class b extends BroadcastReceiver {
        final /* synthetic */ bv a;

        private b(bv bvVar) {
            this.a = bvVar;
        }

        public void onReceive(Context context, Intent intent) {
            Collection collection = null;
            if (context != null && intent != null) {
                try {
                    String action = intent.getAction();
                    if (!TextUtils.isEmpty(action)) {
                        cf a = this.a.o;
                        if (!action.equals("android.net.wifi.SCAN_RESULTS")) {
                            if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                                if (this.a.o != null) {
                                    int i = 4;
                                    try {
                                        i = a.c();
                                    } catch (Throwable th) {
                                        e.a(th, "APS", "onReceive part");
                                    }
                                    if (this.a.r == null) {
                                        this.a.r = new ArrayList();
                                    }
                                    switch (i) {
                                        case 0:
                                            this.a.n();
                                            break;
                                        case 1:
                                            this.a.n();
                                            break;
                                        case 2:
                                        case 3:
                                            break;
                                        case 4:
                                            this.a.n();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } else if (action.equals("android.intent.action.SCREEN_ON")) {
                                this.a.k = true;
                            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                                this.a.k = false;
                                if (this.a.L != null) {
                                    this.a.u();
                                }
                            } else if (!action.equals("android.intent.action.AIRPLANE_MODE") && !action.equals("android.location.GPS_FIX_CHANGE") && action.equals("android.net.conn.CONNECTIVITY_CHANGE") && this.a.A()) {
                                this.a.a(true, 2);
                            }
                        } else if (a != null) {
                            collection = a.a();
                            if (collection != null) {
                                synchronized (this.a.h) {
                                    this.a.r.clear();
                                    this.a.r.addAll(collection);
                                }
                            }
                            this.a.D = cw.b();
                        }
                    }
                } catch (Throwable th2) {
                    e.a(th2, "APS", "onReceive");
                }
            }
        }
    }

    private boolean A() {
        return (this.o == null || this.n == null) ? false : this.o.a(this.n);
    }

    private void B() {
        if (cw.a(this.v, "poiid")) {
            try {
                String string = this.v.getString("poiid");
                if (TextUtils.isEmpty(string)) {
                    this.G = null;
                    return;
                } else if (string.length() <= 32) {
                    this.G = string;
                    return;
                } else {
                    this.G = null;
                    return;
                }
            } catch (Throwable th) {
                e.a(th, "APS", "setPoiid");
                return;
            }
        }
        this.G = null;
    }

    private String C() {
        try {
            return db.a(NumberInfo.VERSION_KEY);
        } catch (Throwable th) {
            e.a(th, "APS", "getCollVer");
            return null;
        }
    }

    private void D() {
        if (this.o != null && this.m != null && this.a) {
            this.o.a(this.a);
        }
    }

    private boolean E() {
        if (this.m == null) {
            c.append("context is null");
            return false;
        } else if (TextUtils.isEmpty(e.e)) {
            c.append("src is null");
            return false;
        } else if (!TextUtils.isEmpty(e.f)) {
            return true;
        } else {
            c.append("license is null");
            return false;
        }
    }

    private void F() {
        if (this.m != null && this.N[0] != 0) {
            SharedPreferences sharedPreferences = this.m.getSharedPreferences("pref", 0);
            if (sharedPreferences != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int append : this.N) {
                    stringBuilder.append(append).append(",");
                }
                try {
                    stringBuilder.deleteCharAt(this.N.length - 1);
                    sharedPreferences.edit().putString("coluphist", r.b(stringBuilder.toString().getBytes("UTF-8")));
                } catch (Throwable th) {
                    e.a(th, "APS", "setColUpHist");
                }
                stringBuilder.delete(0, stringBuilder.length());
            }
        }
    }

    private AmapLoc G() throws Exception {
        AmapLoc amapLoc;
        cd cdVar = null;
        if (c.length() > 0) {
            c.delete(0, c.length());
        }
        try {
            if (!this.A) {
                this.p.f();
                this.p.d();
                cdVar = this.p.b();
            }
            d();
            ArrayList arrayList = this.q;
            if (arrayList != null && arrayList.isEmpty()) {
                this.D = cw.b();
                Collection a = this.o.a();
                if (a != null) {
                    arrayList.addAll(a);
                    synchronized (this.h) {
                        if (this.r != null) {
                            if (this.r.isEmpty()) {
                                this.r.addAll(a);
                            }
                        }
                    }
                }
            }
            e();
        } catch (Throwable th) {
            e.a(th, "APS", "doFirstLocate");
        }
        cd cdVar2 = cdVar;
        String b = b(false);
        if (TextUtils.isEmpty(b)) {
            amapLoc = new AmapLoc();
            amapLoc.b(this.j);
            amapLoc.b(c.toString());
        } else {
            String str = b + "&" + this.ac + "&" + this.ab;
            StringBuilder c = c(true);
            AmapLoc a2 = ci.a().a(str, c);
            if (cw.a(a2)) {
                this.R = 0;
                a2.a(4);
                this.w = a2;
                H();
                return a2;
            }
            AmapLoc a3 = a(f(), false, true);
            if (cw.a(a3)) {
                a3.f("new");
                this.S = c.toString();
                this.T = cdVar2;
                this.x = cw.b();
                this.w = a3;
                ci.a().a(str, c, this.w, this.m, true);
                H();
                amapLoc = a3;
            } else {
                amapLoc = a(b, c.toString());
                if (!cw.a(amapLoc)) {
                    return a3;
                }
                this.S = c.toString();
                amapLoc.f("file");
                amapLoc.a(8);
                amapLoc.b("离线定位结果，在线定位失败原因:" + a3.d());
                this.w = amapLoc;
            }
        }
        return amapLoc;
    }

    private void H() {
        this.U = null;
        this.V = null;
    }

    private void I() {
        if (!ct.i()) {
            J();
        } else if (cc.a[1] > AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST) {
            J();
        } else if (this.W == null || this.X == null) {
            this.X = new TimerTask(this) {
                final /* synthetic */ bv a;

                {
                    this.a = r1;
                }

                public void run() {
                    if (cc.a[1] <= AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST) {
                        ArrayList b;
                        int size;
                        int i;
                        ArrayList b2;
                        Iterator it;
                        Thread.currentThread().setPriority(1);
                        if (cw.a(this.a.v, "fetchoffdatamobile")) {
                            try {
                                boolean equals = "1".equals(this.a.v.getString("fetchoffdatamobile"));
                            } catch (Throwable th) {
                                e.a(th, "APS", "timerTaskO part");
                            }
                            b = cl.a().b();
                            if (b != null) {
                                size = b.size();
                                if (size > 0) {
                                    if (this.a.Y == null) {
                                        this.a.Y = this.a.b(true);
                                    }
                                    i = 0;
                                    while (i < size && i < 20) {
                                        cc.a(this.a.m, this.a.Y, ((ck) b.get(i)).a(), 1, 0, r7 != 0, true);
                                        i++;
                                    }
                                }
                            }
                            this.a.L();
                            b2 = cj.a().b(this.a.m, 1);
                            if (b2 != null) {
                                if (b2.size() > 0) {
                                    it = b2.iterator();
                                    while (it.hasNext()) {
                                        cc.a(this.a.Y, (String) it.next(), 1, 0);
                                    }
                                }
                            }
                            return;
                        }
                        int i2 = 0;
                        b = cl.a().b();
                        if (b != null) {
                            size = b.size();
                            if (size > 0) {
                                if (this.a.Y == null) {
                                    this.a.Y = this.a.b(true);
                                }
                                i = 0;
                                while (i < size) {
                                    if (i2 != 0) {
                                    }
                                    cc.a(this.a.m, this.a.Y, ((ck) b.get(i)).a(), 1, 0, i2 != 0, true);
                                    i++;
                                }
                            }
                        }
                        this.a.L();
                        try {
                            b2 = cj.a().b(this.a.m, 1);
                            if (b2 != null) {
                                if (b2.size() > 0) {
                                    it = b2.iterator();
                                    while (it.hasNext()) {
                                        cc.a(this.a.Y, (String) it.next(), 1, 0);
                                    }
                                }
                            }
                        } catch (Throwable th2) {
                            e.a(th2, "APS", "timerTaskO");
                        }
                        return;
                    }
                    this.a.J();
                }
            };
            this.W = new Timer("T-O", false);
            this.W.schedule(this.X, 0, Constant.MINUTE);
        }
    }

    private void J() {
        if (this.X != null) {
            this.X.cancel();
            this.X = null;
        }
        if (this.W != null) {
            this.W.cancel();
            this.W.purge();
            this.W = null;
        }
    }

    private void K() {
        this.Z = 0;
        this.aa = 0;
    }

    private void L() {
        if (this.m != null && cc.a[0] != 0) {
            SharedPreferences sharedPreferences = this.m.getSharedPreferences("pref", 0);
            if (sharedPreferences != null) {
                StringBuilder stringBuilder = new StringBuilder();
                String str = "activityoffdl";
                for (int append : cc.a) {
                    stringBuilder.append(append).append(",");
                }
                try {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    sharedPreferences.edit().putString(str, cw.c(stringBuilder.toString())).commit();
                } catch (Throwable th) {
                    e.a(th, "APS", "setOffDlHist");
                }
                stringBuilder.delete(0, stringBuilder.length());
            }
        }
    }

    private double[] M() {
        double[] dArr = new double[2];
        double h;
        if (cw.a(this.w)) {
            dArr[0] = this.w.i();
            h = this.w.h();
            dArr[1] = h;
        } else if (cw.a(this.g)) {
            dArr[0] = this.g.i();
            h = this.g.h();
            dArr[1] = h;
        } else {
            dArr[0] = 0.0d;
            dArr[1] = 0.0d;
        }
        return dArr;
    }

    private void N() {
        try {
            this.S = null;
            this.w = null;
            this.R = 0;
            this.x = 0;
            bz.a().b();
        } catch (Throwable th) {
            e.a(th, "APS", "cleanCache");
        }
    }

    private int a(boolean z, int i) {
        if (z) {
            c(i);
        } else {
            y();
        }
        return !q() ? -1 : this.L.g();
    }

    private AmapLoc a(String str, String str2) {
        int i = 0;
        if (!ct.i()) {
            return null;
        }
        if (str != null && str.equals(this.V) && this.U != null) {
            return this.U;
        }
        I();
        ArrayList b = cl.a().b();
        try {
            int i2;
            AmapLoc a;
            if (cc.b()) {
                ArrayList a2 = cc.a(str, false);
                if (a2 != null) {
                    int size = a2.size();
                    for (i2 = 0; i2 < size; i2++) {
                        String str3 = (String) a2.get(i2);
                        a = a(str, str2, null, str3.substring(str3.lastIndexOf(File.separator) + 1, str3.length()), 0);
                        if (cw.a(a)) {
                            this.V = str;
                            this.U = a;
                            return a;
                        }
                    }
                }
            }
            i2 = b.size();
            if (i2 != 0) {
                while (i < i2) {
                    a = a(str, str2, null, ((ck) b.get(i)).a(), 0);
                    if (cw.a(a)) {
                        this.V = str;
                        this.U = a;
                        return a;
                    }
                    i++;
                }
            }
        } catch (Throwable th) {
            e.a(th, "APS", "getPureOfflineLocation");
        }
        return null;
    }

    private AmapLoc a(String str, String str2, double[] dArr, String str3, int i) {
        if (!cw.k()) {
            return null;
        }
        double[] dArr2;
        if (TextUtils.isEmpty(str3)) {
            if (dArr == null) {
                dArr = M();
            }
            if (dArr[0] == 0.0d || dArr[1] == 0.0d) {
                return null;
            }
            dArr2 = dArr;
        } else {
            dArr2 = dArr;
        }
        cw.b();
        return cc.a(dArr2, str3, str, str2, i, this.m, new int[]{this.aa, this.Z});
    }

    private AmapLoc a(String str, boolean z, boolean z2) throws Exception {
        AmapLoc amapLoc;
        if (this.m == null) {
            c.append("context is null");
            amapLoc = new AmapLoc();
            amapLoc.b(1);
            amapLoc.b(c.toString());
            return amapLoc;
        } else if (str == null || str.length() == 0) {
            amapLoc = new AmapLoc();
            amapLoc.b(3);
            amapLoc.b(c.toString());
            return amapLoc;
        } else {
            amapLoc = new AmapLoc();
            cq cqVar = new cq();
            try {
                byte[] a = this.b.a(this.m, this.v, this.H, e.a());
                if (a != null) {
                    this.ad = cw.a();
                    String str2 = new String(a, "UTF-8");
                    if (str2.contains("\"status\":\"0\"")) {
                        return cqVar.b(str2);
                    }
                    String a2 = cg.a(a);
                    if (a2 != null) {
                        AmapLoc a3 = cqVar.a(a2);
                        if (cw.a(a3)) {
                            if (a3.E() == null) {
                            }
                            if (a3.a() == 0 && a3.b() == 0) {
                                if ("-5".equals(a3.m()) || "1".equals(a3.m()) || "2".equals(a3.m()) || "14".equals(a3.m()) || "24".equals(a3.m()) || ThemeUtil.SET_NULL_STR.equals(a3.m())) {
                                    a3.a(5);
                                } else {
                                    a3.a(6);
                                }
                                a3.b(a3.m());
                            }
                            a3.a(this.ac);
                            a3.b(this.ab);
                            return a3;
                        } else if (a3 == null) {
                            amapLoc = new AmapLoc();
                            amapLoc.b(6);
                            c.append("location is null");
                            amapLoc.b(c.toString());
                            return amapLoc;
                        } else {
                            this.O = a3.n();
                            a3.b(6);
                            c.append("location faile retype:" + a3.m() + " rdesc:" + (this.O == null ? "null" : this.O));
                            a3.b(c.toString());
                            return a3;
                        }
                    }
                    amapLoc = new AmapLoc();
                    amapLoc.b(5);
                    c.append("decrypt response data error");
                    amapLoc.b(c.toString());
                    return amapLoc;
                }
                amapLoc = new AmapLoc();
                amapLoc.b(4);
                c.append("please check the network");
                amapLoc.b(c.toString());
                return amapLoc;
            } catch (Throwable th) {
                e.a(th, "APS", "getApsLoc");
                amapLoc = new AmapLoc();
                amapLoc.b(4);
                c.append("please check the network");
                amapLoc.b(c.toString());
                return amapLoc;
            }
        }
    }

    private String a(int i, int i2, int i3) throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("e", i);
        jSONObject.put("d", i2);
        jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, i3);
        return jSONObject.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private StringBuilder a(Object obj) {
        NetworkInfo activeNetworkInfo;
        int i;
        boolean z;
        StringBuilder stringBuilder = new StringBuilder();
        String str = "0";
        String str2 = "0";
        String str3 = "0";
        String str4 = "0";
        String str5 = "0";
        String str6 = e.i;
        e.b = "888888888888888";
        e.c = "888888888888888";
        e.d = "";
        int a = cw.a(-32768, 32767);
        String str7 = "";
        String str8 = "";
        String str9 = "";
        String str10 = e.e;
        String str11 = e.f;
        if (!this.ac) {
            str10 = "UC_nlp_20131029";
            str11 = "BKZCHMBBSSUK7U8GLUKHBB56CCFF78U";
        }
        String str12 = str10;
        str10 = str11;
        StringBuilder stringBuilder2 = new StringBuilder();
        StringBuilder stringBuilder3 = new StringBuilder();
        CharSequence stringBuilder4 = new StringBuilder();
        ce ceVar = this.p;
        int c = ceVar.c();
        TelephonyManager e = ceVar.e();
        ArrayList a2 = ceVar.a();
        String str13 = c != 2 ? str : "1";
        if (e != null) {
            if (TextUtils.isEmpty(e.b)) {
                e.b = "888888888888888";
                try {
                    e.b = q.q(this.m);
                } catch (Throwable th) {
                    e.a(th, "APS", "getApsReq part4");
                }
            } else if ("888888888888888".equals(e.b)) {
                e.b = "888888888888888";
                try {
                    e.b = q.q(this.m);
                } catch (Throwable th2) {
                    e.a(th2, "APS", "getApsReq part3");
                }
            }
            if (TextUtils.isEmpty(e.b)) {
                e.b = "888888888888888";
            }
            if (TextUtils.isEmpty(e.c)) {
                e.c = "888888888888888";
                try {
                    e.c = e.getSubscriberId();
                } catch (Throwable th22) {
                    e.a(th22, "APS", "getApsReq part2");
                }
            } else if ("888888888888888".equals(e.c)) {
                e.c = "888888888888888";
                try {
                    e.c = e.getSubscriberId();
                } catch (Throwable th222) {
                    e.a(th222, "APS", "getApsReq part1");
                }
            }
            if (TextUtils.isEmpty(e.c)) {
                e.c = "888888888888888";
            }
        }
        try {
            activeNetworkInfo = this.n.getActiveNetworkInfo();
        } catch (Throwable th2222) {
            e.a(th2222, "APS", "getApsReq part");
            activeNetworkInfo = null;
        }
        if (cw.a(activeNetworkInfo) == -1) {
            this.u = null;
            str = str8;
            str8 = str7;
        } else {
            str = cw.b(e);
            if (p()) {
                if (a(this.u)) {
                    str11 = "2";
                    if (!p()) {
                        n();
                    }
                    str8 = str;
                    str = str11;
                }
            }
            str11 = "1";
            if (p()) {
                n();
            }
            str8 = str;
            str = str11;
        }
        B();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"");
        stringBuilder.append("GBK").append("\"?>");
        stringBuilder.append("<Cell_Req ver=\"3.0\"><HDR version=\"3.0\" cdma=\"");
        stringBuilder.append(str13);
        stringBuilder.append("\" gtype=\"").append(str2);
        if (str2.equals("1")) {
            stringBuilder.append("\" gmock=\"").append(!this.B ? "0" : "1");
        }
        stringBuilder.append("\" glong=\"").append(str3);
        stringBuilder.append("\" glat=\"").append(str4);
        stringBuilder.append("\" precision=\"").append(str5);
        stringBuilder.append("\"><src>").append(str12);
        stringBuilder.append("</src><license>").append(str10);
        stringBuilder.append("</license><key>").append(str6);
        stringBuilder.append("</key><clientid>").append(e.h);
        stringBuilder.append("</clientid><imei>").append(e.b);
        stringBuilder.append("</imei><imsi>").append(e.c);
        stringBuilder.append("</imsi><reqid>").append(a);
        stringBuilder.append("</reqid><smac>").append(this.F);
        stringBuilder.append("</smac><sdkv>").append(c());
        stringBuilder.append("</sdkv><corv>").append(C());
        stringBuilder.append("</corv><poiid>").append(this.G);
        stringBuilder.append("</poiid></HDR><DRR phnum=\"").append(e.d);
        stringBuilder.append("\" nettype=\"").append(str8);
        stringBuilder.append("\" inftype=\"").append(str).append("\">");
        if (!a2.isEmpty()) {
            StringBuilder stringBuilder5 = new StringBuilder();
            cd cdVar;
            switch (c) {
                case 1:
                    K();
                    cdVar = (cd) a2.get(0);
                    stringBuilder5.delete(0, stringBuilder5.length());
                    stringBuilder5.append("<mcc>").append(cdVar.a).append("</mcc>");
                    stringBuilder5.append("<mnc>").append(cdVar.b).append("</mnc>");
                    stringBuilder5.append("<lac>").append(cdVar.c).append("</lac>");
                    stringBuilder5.append("<cellid>").append(cdVar.d);
                    stringBuilder5.append("</cellid>");
                    stringBuilder5.append("<signal>").append(cdVar.j);
                    stringBuilder5.append("</signal>");
                    str9 = stringBuilder5.toString();
                    for (i = 1; i < a2.size(); i++) {
                        cdVar = (cd) a2.get(i);
                        stringBuilder2.append(cdVar.c).append(",");
                        stringBuilder2.append(cdVar.d).append(",");
                        stringBuilder2.append(cdVar.j);
                        if (i < a2.size() - 1) {
                            stringBuilder2.append("*");
                        }
                    }
                    break;
                case 2:
                    cdVar = (cd) a2.get(0);
                    stringBuilder5.delete(0, stringBuilder5.length());
                    stringBuilder5.append("<mcc>").append(cdVar.a).append("</mcc>");
                    stringBuilder5.append("<sid>").append(cdVar.g).append("</sid>");
                    stringBuilder5.append("<nid>").append(cdVar.h).append("</nid>");
                    stringBuilder5.append("<bid>").append(cdVar.i).append("</bid>");
                    if (cdVar.f > 0 && cdVar.e > 0) {
                        this.Z = cdVar.f;
                        this.aa = cdVar.e;
                        stringBuilder5.append("<lon>").append(cdVar.f).append("</lon>");
                        stringBuilder5.append("<lat>").append(cdVar.e).append("</lat>");
                    } else {
                        K();
                    }
                    stringBuilder5.append("<signal>").append(cdVar.j).append("</signal>");
                    str11 = stringBuilder5.toString();
                    break;
                default:
                    K();
                    break;
            }
        }
        if (p()) {
            if (a(this.u)) {
                stringBuilder4.append(this.u.getBSSID()).append(",");
                int rssi = this.u.getRssi();
                if (rssi < -128 || rssi > 127) {
                    rssi = 0;
                }
                stringBuilder4.append(rssi).append(",");
                str7 = this.u.getSSID();
                try {
                    rssi = this.u.getSSID().getBytes("UTF-8").length;
                } catch (Throwable th22222) {
                    e.a(th22222, "APS", "getApsReq");
                    rssi = 32;
                }
                stringBuilder4.append((rssi < 32 ? str7 : "unkwn").replace("*", "."));
            }
            List list = this.q;
            int min = Math.min(list.size(), 15);
            for (i = 0; i < min; i++) {
                ScanResult scanResult = (ScanResult) list.get(i);
                stringBuilder3.append(scanResult.BSSID).append(",");
                stringBuilder3.append(scanResult.level).append(",");
                stringBuilder3.append(scanResult.SSID).append("*");
            }
        } else {
            n();
        }
        stringBuilder.append(str9);
        stringBuilder.append(String.format(Locale.US, "<nb>%s</nb>", new Object[]{stringBuilder2}));
        if (stringBuilder3.length() != 0) {
            stringBuilder3.deleteCharAt(stringBuilder3.length() - 1);
            stringBuilder.append("<macs>");
            stringBuilder.append(String.format(Locale.US, "<![CDATA[%s]]>", new Object[]{stringBuilder3}));
            stringBuilder.append("</macs>");
            stringBuilder.append("<macsage>").append(cw.b() - this.D);
            stringBuilder.append("</macsage>");
        } else {
            stringBuilder3.append(stringBuilder4);
            stringBuilder.append("<macs>");
            stringBuilder.append(String.format(Locale.US, "<![CDATA[%s]]>", new Object[]{stringBuilder4}));
            stringBuilder.append("</macs>");
        }
        stringBuilder.append("<mmac>");
        stringBuilder.append(String.format(Locale.US, "<![CDATA[%s]]>", new Object[]{stringBuilder4}));
        stringBuilder.append("</mmac>").append("</DRR></Cell_Req>");
        a(stringBuilder);
        if (cw.a(this.v, "reversegeo")) {
            try {
                z = this.v.getBoolean("reversegeo");
            } catch (Throwable th222222) {
                e.a(th222222, "APS", "getApsReq part");
            }
            if (z) {
                this.H.b = (short) 2;
            } else {
                this.H.b = (short) 0;
            }
            if (cw.a(this.v, "multi")) {
                try {
                    if (this.v.getString("multi").equals("1")) {
                        this.H.b = (short) 1;
                    }
                } catch (Throwable th2222222) {
                    e.a(th2222222, "APS", "getApsReq");
                }
            }
            this.H.c = str12;
            this.H.d = str10;
            this.H.f = cw.f();
            this.H.g = "android" + cw.g();
            if (TextUtils.isEmpty(e.k)) {
                e.k = cw.b(this.m);
            }
            this.H.h = e.k;
            this.H.i = str13;
            this.H.j = str2;
            this.H.k = this.B ? "0" : "1";
            this.H.l = str3;
            this.H.m = str4;
            this.H.n = str5;
            this.H.o = str6;
            this.H.p = e.b;
            this.H.q = e.c;
            this.H.s = String.valueOf(a);
            this.H.t = this.F;
            this.H.v = c();
            this.H.w = C();
            this.H.F = this.G;
            this.H.u = e.d;
            this.H.x = str8;
            this.H.y = str;
            this.H.z = String.valueOf(c);
            this.H.A = str9;
            this.H.B = stringBuilder2.toString();
            this.H.D = stringBuilder3.toString();
            this.H.E = String.valueOf(cw.b() - this.D);
            this.H.C = stringBuilder4.toString();
            stringBuilder2.delete(0, stringBuilder2.length());
            stringBuilder3.delete(0, stringBuilder3.length());
            stringBuilder4.delete(0, stringBuilder4.length());
            return stringBuilder;
        }
        z = true;
        if (z) {
            this.H.b = (short) 2;
        } else {
            this.H.b = (short) 0;
        }
        if (cw.a(this.v, "multi")) {
            if (this.v.getString("multi").equals("1")) {
                this.H.b = (short) 1;
            }
        }
        this.H.c = str12;
        this.H.d = str10;
        this.H.f = cw.f();
        this.H.g = "android" + cw.g();
        if (TextUtils.isEmpty(e.k)) {
            e.k = cw.b(this.m);
        }
        this.H.h = e.k;
        this.H.i = str13;
        this.H.j = str2;
        if (this.B) {
        }
        this.H.k = this.B ? "0" : "1";
        this.H.l = str3;
        this.H.m = str4;
        this.H.n = str5;
        this.H.o = str6;
        this.H.p = e.b;
        this.H.q = e.c;
        this.H.s = String.valueOf(a);
        this.H.t = this.F;
        this.H.v = c();
        this.H.w = C();
        this.H.F = this.G;
        this.H.u = e.d;
        this.H.x = str8;
        this.H.y = str;
        this.H.z = String.valueOf(c);
        this.H.A = str9;
        this.H.B = stringBuilder2.toString();
        this.H.D = stringBuilder3.toString();
        this.H.E = String.valueOf(cw.b() - this.D);
        this.H.C = stringBuilder4.toString();
        stringBuilder2.delete(0, stringBuilder2.length());
        stringBuilder3.delete(0, stringBuilder3.length());
        stringBuilder4.delete(0, stringBuilder4.length());
        return stringBuilder;
    }

    private void a(SharedPreferences sharedPreferences) {
        if (this.m != null && sharedPreferences != null) {
            String b;
            String str = "smac";
            if (sharedPreferences.contains(str)) {
                try {
                    b = r.b(sharedPreferences.getString(str, null).getBytes("UTF-8"));
                } catch (Throwable th) {
                    e.a(th, "APS", "getSmac");
                    sharedPreferences.edit().remove(str).commit();
                }
                if (!(TextUtils.isEmpty(b) || b.equals("00:00:00:00:00:00"))) {
                    this.F = b;
                }
            }
            b = null;
            this.F = b;
        }
    }

    private void a(StringBuilder stringBuilder) {
        int i = 0;
        if (stringBuilder != null) {
            String[] strArr = new String[]{" phnum=\"\"", " nettype=\"\"", " nettype=\"UNKWN\"", " inftype=\"\"", "<macs><![CDATA[]]></macs>", "<nb></nb>", "<mmac><![CDATA[]]></mmac>", " gtype=\"0\"", " gmock=\"0\"", " glong=\"0.0\"", " glat=\"0.0\"", " precision=\"0.0\"", " glong=\"0\"", " glat=\"0\"", " precision=\"0\"", "<smac>null</smac>", "<smac>00:00:00:00:00:00</smac>", "<imei>000000000000000</imei>", "<imsi>000000000000000</imsi>", "<mcc>000</mcc>", "<mcc>0</mcc>", "<lac>0</lac>", "<cellid>0</cellid>", "<key></key>", "<poiid></poiid>", "<poiid>null</poiid>"};
            int length = strArr.length;
            while (i < length) {
                String str = strArr[i];
                while (stringBuilder.indexOf(str) != -1) {
                    int indexOf = stringBuilder.indexOf(str);
                    stringBuilder.delete(indexOf, str.length() + indexOf);
                }
                i++;
            }
            while (stringBuilder.indexOf("*<") != -1) {
                stringBuilder.deleteCharAt(stringBuilder.indexOf("*<"));
            }
        }
    }

    private boolean a(int i) {
        int i2 = 20;
        try {
            i2 = WifiManager.calculateSignalLevel(i, 20);
        } catch (Throwable e) {
            e.a(e, "APS", "wifiSigFine");
        }
        return i2 >= 1;
    }

    private boolean a(long j) {
        if (cw.b() - j >= 800) {
            return false;
        }
        long j2 = 0;
        if (cw.a(this.w)) {
            j2 = cw.a() - this.w.k();
        }
        return (j2 > 10000 ? 1 : (j2 == 10000 ? 0 : -1)) <= 0;
    }

    private boolean a(WifiInfo wifiInfo) {
        return (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getBSSID()) || wifiInfo.getSSID() == null || wifiInfo.getBSSID().equals("00:00:00:00:00:00") || wifiInfo.getBSSID().contains(" :") || TextUtils.isEmpty(wifiInfo.getSSID())) ? false : true;
    }

    private void b(int i) {
        int i2 = 70254591;
        if (q()) {
            try {
                z();
                switch (i) {
                    case 2:
                        if (A()) {
                            i2 = 2083520511;
                            break;
                        }
                    case 1:
                        i2 = 674234367;
                        break;
                }
                this.L.a(null, a(1, i2, 1));
                this.M = this.L.e();
                if (this.M != null) {
                    byte[] a = this.M.a();
                    Object a2 = this.b.a(a, this.m, "http://cgicol.amap.com/collection/writedata?ver=v1.0_ali&", false);
                    if (q()) {
                        if (!TextUtils.isEmpty(a2)) {
                            if (a2.equals("true")) {
                                this.L.a(this.M, a(1, i2, 1));
                                String a3 = cw.a(0, "yyyyMMdd");
                                if (a3.equals(String.valueOf(this.N[0]))) {
                                    int[] iArr = this.N;
                                    iArr[1] = a.length + iArr[1];
                                } else {
                                    this.N[0] = Integer.parseInt(a3);
                                    this.N[1] = a.length;
                                }
                                this.N[2] = this.N[2] + 1;
                                F();
                            }
                        }
                        this.K++;
                        this.L.a(this.M, a(1, i2, 0));
                    }
                }
            } catch (Throwable th) {
                e.a(th, "APS", "up");
            }
            t();
            if (q() && this.L.g() == 0) {
                y();
            }
            if (this.K >= 3) {
                y();
            }
        }
    }

    private void b(SharedPreferences sharedPreferences) {
        int i = 0;
        if (this.m != null) {
            SharedPreferences sharedPreferences2 = this.m.getSharedPreferences("pref", 0);
            if (sharedPreferences2 != null && sharedPreferences2.contains("coluphist")) {
                try {
                    String[] split = r.b(sharedPreferences2.getString("coluphist", null).getBytes("UTF-8")).split(",");
                    while (i < 3) {
                        this.N[i] = Integer.parseInt(split[i]);
                        i++;
                    }
                } catch (Throwable th) {
                    e.a(th, "APS", "getColUpHist");
                    sharedPreferences2.edit().remove("coluphist").commit();
                }
            }
        }
    }

    private void c(final int i) {
        t();
        if (this.J == null) {
            this.J = new TimerTask(this) {
                final /* synthetic */ bv b;

                public void run() {
                    int i = 1;
                    Thread.currentThread().setPriority(1);
                    if (cw.b() - this.b.z < 10000) {
                        i = 0;
                    }
                    if (i != 0) {
                        if (this.b.A()) {
                            this.b.b(i);
                        } else {
                            this.b.y();
                        }
                    }
                }
            };
        }
        if (this.I == null) {
            this.I = new Timer("T-U", false);
            this.I.schedule(this.J, 2000, 2000);
        }
    }

    private void c(SharedPreferences sharedPreferences) {
        if (sharedPreferences != null) {
            String str = "activityoffdl";
            if (sharedPreferences.contains(str)) {
                try {
                    String[] split = cw.d(sharedPreferences.getString(str, null)).split(",");
                    for (int i = 0; i < 2; i++) {
                        cc.a[i] = Integer.parseInt(split[i]);
                    }
                } catch (Throwable th) {
                    e.a(th, "APS", "getOffDlHist");
                    sharedPreferences.edit().remove(str).commit();
                }
            }
        }
    }

    private void i() {
        try {
            this.o = new cf(this.m, (WifiManager) cw.a(this.m, "wifi"), this.v);
            this.n = (ConnectivityManager) cw.a(this.m, "connectivity");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            intentFilter.addAction("android.location.GPS_FIX_CHANGE");
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.m.registerReceiver(this.t, intentFilter);
            o();
        } catch (Throwable th) {
            e.a(th, "APS", "initBroadcastListener");
        }
    }

    private void j() {
        this.p = new ce(this.m, this.v);
        this.p.h();
    }

    private void k() {
        Object obj = 1;
        long b = cw.b();
        if (l()) {
            List list = this.r;
            if ((b - this.y < 10000 ? 1 : null) == null) {
                synchronized (this.h) {
                    list.clear();
                }
            }
            o();
            if (b - this.y >= 10000) {
                obj = null;
            }
            if (obj == null) {
                for (int i = 20; i > 0 && list.isEmpty(); i--) {
                    try {
                        Thread.sleep(150);
                    } catch (Throwable th) {
                        e.a(th, "APS", "mayWaitForWifi");
                    }
                }
            }
            synchronized (this.h) {
            }
            if (list.isEmpty() && this.o != null) {
                Collection a = this.o.a();
                if (a != null) {
                    list.addAll(a);
                }
            }
        }
    }

    private boolean l() {
        boolean z = true;
        if (!TextUtils.isEmpty(this.G)) {
            return true;
        }
        if (p()) {
            if (this.C != 0) {
                if (cw.b() - this.C >= 3000) {
                    if (!(cw.b() - this.D >= 1500)) {
                    }
                }
            }
            return z;
        }
        z = false;
        return z;
    }

    private boolean m() {
        boolean z = true;
        if (this.x == 0) {
            return true;
        }
        if (cw.b() - this.x <= 20000) {
            z = false;
        }
        return z;
    }

    private void n() {
        this.D = 0;
        this.q.clear();
        this.u = null;
        synchronized (this.h) {
            this.r.clear();
            this.s.clear();
        }
    }

    private void o() {
        boolean z = false;
        if (p()) {
            boolean equals;
            if (cw.c() < 18 && cw.c() > 3 && cw.a(this.v, "wifiactivescan")) {
                try {
                    equals = "1".equals(this.v.getString("wifiactivescan"));
                } catch (Throwable th) {
                    e.a(th, "APS", "updateWifi part1");
                }
                if (equals) {
                    try {
                        z = this.o.e();
                        if (z) {
                            this.C = cw.b();
                        }
                    } catch (Throwable th2) {
                        e.a(th2, "APS", "updateWifi part");
                    }
                }
                if (!z) {
                    try {
                        if (this.o.d()) {
                            this.C = cw.b();
                        }
                    } catch (Throwable th22) {
                        e.a(th22, "APS", "updateWifi");
                    }
                }
            }
            equals = false;
            if (equals) {
                z = this.o.e();
                if (z) {
                    this.C = cw.b();
                }
            }
            if (z) {
                if (this.o.d()) {
                    this.C = cw.b();
                }
            }
        }
    }

    private boolean p() {
        return this.o == null ? false : this.o.f();
    }

    private boolean q() {
        return this.L != null;
    }

    private boolean r() {
        boolean z = false;
        try {
            if (q()) {
                z = this.L.d();
            }
        } catch (Throwable th) {
            e.a(th, "APS", "collStarted");
        }
        return z;
    }

    private void s() {
        if (q()) {
            Object obj = 1;
            if (cw.a(this.v, "coll")) {
                try {
                    if (this.v.getString("coll").equals("0")) {
                        obj = null;
                    }
                } catch (Throwable th) {
                    e.a(th, "APS", "start3rdCM");
                }
            }
            if (obj == null) {
                u();
            } else if (!r()) {
                try {
                    this.L.b(e.m * 1000);
                    z();
                    t();
                    this.L.a();
                } catch (Throwable th2) {
                    e.a(th2, "APS", "start3rdCM");
                }
            }
        }
    }

    private void t() {
        if (!q()) {
            return;
        }
        if (!q() || this.L.g() <= 0) {
            try {
                if (q()) {
                    if (!this.L.f()) {
                    }
                }
            } catch (Throwable th) {
                e.a(th, "APS", "collFileSwitch");
            }
        }
    }

    private void u() {
        if (r()) {
            e.m = 20;
            try {
                this.L.c();
            } catch (Throwable th) {
                e.a(th, "APS", "stop3rdCM");
            }
        }
    }

    private void v() {
        if (this.m != null && !TextUtils.isEmpty(this.F)) {
            Object b;
            SharedPreferences sharedPreferences = this.m.getSharedPreferences("pref", 0);
            try {
                b = r.b(this.F.getBytes("UTF-8"));
            } catch (Throwable th) {
                e.a(th, "APS", "setSmac");
                b = null;
            }
            if (!TextUtils.isEmpty(b)) {
                sharedPreferences.edit().putString("smac", b).commit();
            }
        }
    }

    private void w() {
        e.e = "";
        e.f = "";
        e.h = "";
    }

    private void x() {
        Object obj = null;
        List list = this.r;
        try {
            if (cw.a(this.v, "wait1stwifi")) {
                obj = this.v.getString("wait1stwifi");
            }
            if (TextUtils.isEmpty(obj) || !obj.equals("1")) {
                return;
            }
        } catch (Throwable th) {
            e.a(th, "APS", "wait1StWifi part");
        }
        synchronized (this.h) {
            list.clear();
        }
        o();
        for (int i = 20; i > 0 && list.isEmpty(); i--) {
            try {
                Thread.sleep(150);
            } catch (Throwable th2) {
                e.a(th2, "APS", "wait1StWifi");
            }
        }
        synchronized (this.h) {
        }
        if (list.isEmpty() && this.o != null) {
            list.addAll(this.o.a());
        }
    }

    private void y() {
        if (this.J != null) {
            this.J.cancel();
            this.J = null;
        }
        if (this.I != null) {
            this.I.cancel();
            this.I.purge();
            this.I = null;
        }
    }

    private void z() {
        if (q()) {
            try {
                this.L.a(768);
            } catch (Throwable th) {
                e.a(th, "APS", "setCollSize");
            }
        }
    }

    public AmapLoc a(AmapLoc amapLoc, String... strArr) {
        return (strArr == null || strArr.length == 0) ? bz.a().a(amapLoc) : !strArr[0].equals("shake") ? !strArr[0].equals("fusion") ? amapLoc : bz.a().b(amapLoc) : bz.a().a(amapLoc);
    }

    public synchronized AmapLoc a(boolean z) throws Exception {
        Object obj = 1;
        synchronized (this) {
            if (c.length() > 0) {
                c.delete(0, c.length());
            }
            AmapLoc G;
            if (E()) {
                boolean z2 = !cw.a(this.v, "reversegeo") ? true : this.v.getBoolean("reversegeo");
                boolean z3 = !cw.a(this.v, "isOffset") ? true : this.v.getBoolean("isOffset");
                if (z3 != this.ac || z2 != this.ab) {
                    N();
                }
                this.ac = z3;
                this.ab = z2;
                this.E++;
                this.A = cw.a(this.m);
                if (z) {
                    G = G();
                    return G;
                }
                if (this.E == 2) {
                    t();
                    D();
                    if (this.m != null) {
                        SharedPreferences sharedPreferences = this.m.getSharedPreferences("pref", 0);
                        b(sharedPreferences);
                        c(sharedPreferences);
                        a(sharedPreferences);
                    }
                    I();
                }
                if (this.E == 1 && p()) {
                    if (this.r.isEmpty()) {
                        this.D = cw.b();
                        Collection a = this.o.a();
                        synchronized (this.h) {
                            if (!(this.r == null || a == null)) {
                                this.r.addAll(a);
                            }
                        }
                    }
                    x();
                }
                if (a(this.x)) {
                    if (cw.a(this.w)) {
                        this.w.a(2);
                        G = this.w;
                        return G;
                    }
                }
                this.p.f();
                if (!z) {
                    k();
                    this.y = cw.b();
                }
                try {
                    d();
                    e();
                } catch (Throwable th) {
                    e.a(th, "APS", "getLocation");
                }
                String b = b(false);
                int i;
                if (TextUtils.isEmpty(b)) {
                    if (!this.f) {
                        g();
                    }
                    for (i = 4; i > 0 && this.e != 0; i--) {
                        SystemClock.sleep(500);
                    }
                    if (this.e == 0) {
                        this.w = this.d.d();
                        if (this.w != null) {
                            G = this.w;
                            return G;
                        }
                    }
                    G = new AmapLoc();
                    G.b(this.j);
                    G.b(c.toString());
                    return G;
                }
                Object obj2;
                boolean m;
                Object obj3;
                Object obj4;
                String str;
                String str2;
                AmapLoc amapLoc;
                String str3 = "";
                StringBuilder c = c(false);
                cd b2 = this.A ? null : this.p.b();
                if (!(b2 == null && this.T == null)) {
                    if (this.T == null || !this.T.a(b2)) {
                        obj2 = 1;
                        m = m();
                        if (this.w != null) {
                            obj3 = null;
                        } else {
                            i = this.q.size();
                            if (this.w.j() > 299.0f || i <= 5) {
                                obj4 = null;
                            } else {
                                i = 1;
                            }
                            obj3 = obj4;
                        }
                        if (this.w != null && this.S != null && obj3 == null && r6 == null) {
                            z3 = ci.a().b(this.S, c);
                            if (!z3) {
                                if (this.R != 0) {
                                    if ((cw.b() - this.R < 3000 ? 1 : null) == null) {
                                    }
                                }
                            }
                            if (this.p.a(this.A)) {
                                this.p.h();
                            }
                            if (cw.a(this.w)) {
                                this.w.f("mem");
                                this.w.a(2);
                                G = this.w;
                                return G;
                            }
                        }
                        z3 = false;
                        if (z3) {
                            this.R = cw.b();
                        } else {
                            this.R = 0;
                        }
                        if (this.P != null && !b.equals(this.P)) {
                            if (cw.a() - this.Q < 3000) {
                                obj = null;
                            }
                            if (obj != null) {
                                str = this.P;
                                str2 = str + "&" + this.ac + "&" + this.ab;
                                G = (obj3 == null && !m) ? ci.a().a(str2, c) : null;
                                if ((m && !cw.a(G)) || obj3 != null) {
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.w.f("new");
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                } else if (m) {
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                } else {
                                    this.R = 0;
                                    G.a(4);
                                    this.w = G;
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (!cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.Q = cw.a();
                            this.P = b;
                        } else if (this.P == null) {
                            this.Q = cw.a();
                        } else {
                            this.Q = cw.a();
                            this.P = b;
                        }
                        str = b;
                        str2 = str + "&" + this.ac + "&" + this.ab;
                        if (obj3 == null) {
                            if (!m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                }
                obj2 = null;
                m = m();
                if (this.w != null) {
                    i = this.q.size();
                    if (this.w.j() > 299.0f) {
                    }
                    obj4 = null;
                    obj3 = obj4;
                } else {
                    obj3 = null;
                }
                if (this.w != null) {
                    z3 = ci.a().b(this.S, c);
                    if (z3) {
                        if (this.R != 0) {
                            if (cw.b() - this.R < 3000) {
                            }
                            if ((cw.b() - this.R < 3000 ? 1 : null) == null) {
                            }
                        }
                        if (z3) {
                            this.R = cw.b();
                        } else {
                            this.R = 0;
                        }
                        if (this.P != null) {
                            if (cw.a() - this.Q < 3000) {
                                obj = null;
                            }
                            if (obj != null) {
                                this.Q = cw.a();
                                this.P = b;
                                str = b;
                                str2 = str + "&" + this.ac + "&" + this.ab;
                                if (obj3 == null) {
                                    if (m) {
                                        this.w = a(f(), false, false);
                                        if (cw.a(this.w)) {
                                            this.w.f("new");
                                            this.S = c.toString();
                                            this.T = b2;
                                            this.x = cw.b();
                                            H();
                                        }
                                        ci.a().a(str2, c, this.w, this.m, true);
                                        cl.a().a(this.m, str, this.w);
                                        if (cw.a(this.w)) {
                                            G = a(str, c.toString());
                                            if (cw.a(G)) {
                                                this.S = c.toString();
                                                amapLoc = this.w;
                                                this.w = G;
                                                this.w.a(8);
                                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                            }
                                        }
                                        c.delete(0, c.length());
                                        G = this.w;
                                        return G;
                                    }
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.w.f("new");
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                    ci.a().a(str2, c, this.w, this.m, true);
                                    cl.a().a(this.m, str, this.w);
                                    if (cw.a(this.w)) {
                                        G = a(str, c.toString());
                                        if (cw.a(G)) {
                                            this.S = c.toString();
                                            amapLoc = this.w;
                                            this.w = G;
                                            this.w.a(8);
                                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                        }
                                    }
                                    c.delete(0, c.length());
                                    G = this.w;
                                    return G;
                                }
                                if (m) {
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.w.f("new");
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                    ci.a().a(str2, c, this.w, this.m, true);
                                    cl.a().a(this.m, str, this.w);
                                    if (cw.a(this.w)) {
                                        G = a(str, c.toString());
                                        if (cw.a(G)) {
                                            this.S = c.toString();
                                            amapLoc = this.w;
                                            this.w = G;
                                            this.w.a(8);
                                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                        }
                                    }
                                    c.delete(0, c.length());
                                    G = this.w;
                                    return G;
                                }
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            str = this.P;
                            str2 = str + "&" + this.ac + "&" + this.ab;
                            if (obj3 == null) {
                                if (m) {
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.w.f("new");
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                    ci.a().a(str2, c, this.w, this.m, true);
                                    cl.a().a(this.m, str, this.w);
                                    if (cw.a(this.w)) {
                                        G = a(str, c.toString());
                                        if (cw.a(G)) {
                                            this.S = c.toString();
                                            amapLoc = this.w;
                                            this.w = G;
                                            this.w.a(8);
                                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                        }
                                    }
                                    c.delete(0, c.length());
                                    G = this.w;
                                    return G;
                                }
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            if (m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        if (this.P == null) {
                            this.Q = cw.a();
                            this.P = b;
                        } else {
                            this.Q = cw.a();
                        }
                        str = b;
                        str2 = str + "&" + this.ac + "&" + this.ab;
                        if (obj3 == null) {
                            if (m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    if (this.p.a(this.A)) {
                        this.p.h();
                    }
                    if (cw.a(this.w)) {
                        this.w.f("mem");
                        this.w.a(2);
                        G = this.w;
                        return G;
                    }
                    if (z3) {
                        this.R = 0;
                    } else {
                        this.R = cw.b();
                    }
                    if (this.P != null) {
                        if (cw.a() - this.Q < 3000) {
                            obj = null;
                        }
                        if (obj != null) {
                            str = this.P;
                            str2 = str + "&" + this.ac + "&" + this.ab;
                            if (obj3 == null) {
                                if (m) {
                                    this.w = a(f(), false, false);
                                    if (cw.a(this.w)) {
                                        this.w.f("new");
                                        this.S = c.toString();
                                        this.T = b2;
                                        this.x = cw.b();
                                        H();
                                    }
                                    ci.a().a(str2, c, this.w, this.m, true);
                                    cl.a().a(this.m, str, this.w);
                                    if (cw.a(this.w)) {
                                        G = a(str, c.toString());
                                        if (cw.a(G)) {
                                            this.S = c.toString();
                                            amapLoc = this.w;
                                            this.w = G;
                                            this.w.a(8);
                                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                        }
                                    }
                                    c.delete(0, c.length());
                                    G = this.w;
                                    return G;
                                }
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            if (m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.Q = cw.a();
                        this.P = b;
                        str = b;
                        str2 = str + "&" + this.ac + "&" + this.ab;
                        if (obj3 == null) {
                            if (m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    if (this.P == null) {
                        this.Q = cw.a();
                    } else {
                        this.Q = cw.a();
                        this.P = b;
                    }
                    str = b;
                    str2 = str + "&" + this.ac + "&" + this.ab;
                    if (obj3 == null) {
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    if (m) {
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    this.w = a(f(), false, false);
                    if (cw.a(this.w)) {
                        this.w.f("new");
                        this.S = c.toString();
                        this.T = b2;
                        this.x = cw.b();
                        H();
                    }
                    ci.a().a(str2, c, this.w, this.m, true);
                    cl.a().a(this.m, str, this.w);
                    if (cw.a(this.w)) {
                        G = a(str, c.toString());
                        if (cw.a(G)) {
                            this.S = c.toString();
                            amapLoc = this.w;
                            this.w = G;
                            this.w.a(8);
                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                        }
                    }
                    c.delete(0, c.length());
                    G = this.w;
                    return G;
                }
                z3 = false;
                if (z3) {
                    this.R = cw.b();
                } else {
                    this.R = 0;
                }
                if (this.P != null) {
                    if (cw.a() - this.Q < 3000) {
                        obj = null;
                    }
                    if (obj != null) {
                        this.Q = cw.a();
                        this.P = b;
                        str = b;
                        str2 = str + "&" + this.ac + "&" + this.ab;
                        if (obj3 == null) {
                            if (m) {
                                this.w = a(f(), false, false);
                                if (cw.a(this.w)) {
                                    this.w.f("new");
                                    this.S = c.toString();
                                    this.T = b2;
                                    this.x = cw.b();
                                    H();
                                }
                                ci.a().a(str2, c, this.w, this.m, true);
                                cl.a().a(this.m, str, this.w);
                                if (cw.a(this.w)) {
                                    G = a(str, c.toString());
                                    if (cw.a(G)) {
                                        this.S = c.toString();
                                        amapLoc = this.w;
                                        this.w = G;
                                        this.w.a(8);
                                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                    }
                                }
                                c.delete(0, c.length());
                                G = this.w;
                                return G;
                            }
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    str = this.P;
                    str2 = str + "&" + this.ac + "&" + this.ab;
                    if (obj3 == null) {
                        if (m) {
                            this.w = a(f(), false, false);
                            if (cw.a(this.w)) {
                                this.w.f("new");
                                this.S = c.toString();
                                this.T = b2;
                                this.x = cw.b();
                                H();
                            }
                            ci.a().a(str2, c, this.w, this.m, true);
                            cl.a().a(this.m, str, this.w);
                            if (cw.a(this.w)) {
                                G = a(str, c.toString());
                                if (cw.a(G)) {
                                    this.S = c.toString();
                                    amapLoc = this.w;
                                    this.w = G;
                                    this.w.a(8);
                                    this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                                }
                            }
                            c.delete(0, c.length());
                            G = this.w;
                            return G;
                        }
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    if (m) {
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    this.w = a(f(), false, false);
                    if (cw.a(this.w)) {
                        this.w.f("new");
                        this.S = c.toString();
                        this.T = b2;
                        this.x = cw.b();
                        H();
                    }
                    ci.a().a(str2, c, this.w, this.m, true);
                    cl.a().a(this.m, str, this.w);
                    if (cw.a(this.w)) {
                        G = a(str, c.toString());
                        if (cw.a(G)) {
                            this.S = c.toString();
                            amapLoc = this.w;
                            this.w = G;
                            this.w.a(8);
                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                        }
                    }
                    c.delete(0, c.length());
                    G = this.w;
                    return G;
                }
                if (this.P == null) {
                    this.Q = cw.a();
                    this.P = b;
                } else {
                    this.Q = cw.a();
                }
                str = b;
                str2 = str + "&" + this.ac + "&" + this.ab;
                if (obj3 == null) {
                    if (m) {
                        this.w = a(f(), false, false);
                        if (cw.a(this.w)) {
                            this.w.f("new");
                            this.S = c.toString();
                            this.T = b2;
                            this.x = cw.b();
                            H();
                        }
                        ci.a().a(str2, c, this.w, this.m, true);
                        cl.a().a(this.m, str, this.w);
                        if (cw.a(this.w)) {
                            G = a(str, c.toString());
                            if (cw.a(G)) {
                                this.S = c.toString();
                                amapLoc = this.w;
                                this.w = G;
                                this.w.a(8);
                                this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                            }
                        }
                        c.delete(0, c.length());
                        G = this.w;
                        return G;
                    }
                    this.w = a(f(), false, false);
                    if (cw.a(this.w)) {
                        this.w.f("new");
                        this.S = c.toString();
                        this.T = b2;
                        this.x = cw.b();
                        H();
                    }
                    ci.a().a(str2, c, this.w, this.m, true);
                    cl.a().a(this.m, str, this.w);
                    if (cw.a(this.w)) {
                        G = a(str, c.toString());
                        if (cw.a(G)) {
                            this.S = c.toString();
                            amapLoc = this.w;
                            this.w = G;
                            this.w.a(8);
                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                        }
                    }
                    c.delete(0, c.length());
                    G = this.w;
                    return G;
                }
                if (m) {
                    this.w = a(f(), false, false);
                    if (cw.a(this.w)) {
                        this.w.f("new");
                        this.S = c.toString();
                        this.T = b2;
                        this.x = cw.b();
                        H();
                    }
                    ci.a().a(str2, c, this.w, this.m, true);
                    cl.a().a(this.m, str, this.w);
                    if (cw.a(this.w)) {
                        G = a(str, c.toString());
                        if (cw.a(G)) {
                            this.S = c.toString();
                            amapLoc = this.w;
                            this.w = G;
                            this.w.a(8);
                            this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                        }
                    }
                    c.delete(0, c.length());
                    G = this.w;
                    return G;
                }
                this.w = a(f(), false, false);
                if (cw.a(this.w)) {
                    this.w.f("new");
                    this.S = c.toString();
                    this.T = b2;
                    this.x = cw.b();
                    H();
                }
                ci.a().a(str2, c, this.w, this.m, true);
                cl.a().a(this.m, str, this.w);
                if (cw.a(this.w)) {
                    G = a(str, c.toString());
                    if (cw.a(G)) {
                        this.S = c.toString();
                        amapLoc = this.w;
                        this.w = G;
                        this.w.a(8);
                        this.w.b("离线定位，在线定位失败原因:" + amapLoc.d());
                    }
                }
                c.delete(0, c.length());
                G = this.w;
                return G;
            }
            G = new AmapLoc();
            G.b(1);
            G.b(c.toString());
            return G;
        }
    }

    public void a() {
        Object obj = 1;
        if (ct.a()) {
            if (cw.b() - ct.c() < ct.b()) {
                obj = null;
            }
            if (obj == null && this.w != null) {
                if (this.w.b() == 2 || this.w.b() == 4) {
                    try {
                        b(false);
                        c(true);
                        a(f(), false, true);
                    } catch (Throwable th) {
                        e.a(th, "APS", "fusionLocation");
                    }
                }
            }
        }
    }

    public synchronized void a(Context context) {
        if (context != null) {
            if (TextUtils.isEmpty(e.k)) {
                e.k = cw.b(context);
            }
            if (this.m == null) {
                this.m = context.getApplicationContext();
                this.b = co.a(context);
                try {
                    this.L = db.a(this.m);
                } catch (Throwable th) {
                    e.a(th, "APS", "setExtra");
                }
                this.z = cw.b();
                i();
                j();
                e.n = true;
                this.H = new cs();
                this.p.d();
                ci.a().a(context);
                cl.a().a(context);
                this.i = true;
            }
        }
    }

    public void a(String str) {
        if (!TextUtils.isEmpty(str) && str.contains("##")) {
            String[] split = str.split("##");
            if (split.length == 4) {
                e.e = split[0];
                e.f = split[1];
                e.h = split[2];
                e.i = split[3];
                return;
            }
            w();
            return;
        }
        w();
    }

    public void a(JSONObject jSONObject) {
        this.v = jSONObject;
        if (cw.a(jSONObject, "collwifiscan")) {
            try {
                Object string = jSONObject.getString("collwifiscan");
                if (TextUtils.isEmpty(string)) {
                    e.m = 20;
                } else {
                    e.m = Integer.parseInt(string) / 1000;
                }
                if (r()) {
                    this.L.b(e.m * 1000);
                }
            } catch (Throwable th) {
                e.a(th, "APS", "setExtra");
            }
        }
        if (this.p != null) {
            this.p.a(jSONObject);
        }
        if (this.o != null) {
            this.o.a(jSONObject);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String b(boolean z) {
        if (this.A) {
            this.p.g();
        } else {
            this.p.j();
        }
        String str = "";
        String str2 = "";
        String str3 = "network";
        if (p()) {
            this.u = this.o.b();
        } else {
            n();
        }
        str2 = "";
        int c = this.p.c();
        ArrayList a = this.p.a();
        List list = this.q;
        if (a == null || a.isEmpty()) {
            if (list != null) {
                if (!list.isEmpty()) {
                }
            }
            c.append("⊗ lstCgi & ⊗ wifis");
            this.j = 12;
            return str;
        }
        cd cdVar;
        StringBuilder stringBuilder;
        switch (c) {
            case 1:
                if (!a.isEmpty()) {
                    cdVar = (cd) a.get(0);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(cdVar.a).append("#");
                    stringBuilder.append(cdVar.b).append("#");
                    stringBuilder.append(cdVar.c).append("#");
                    stringBuilder.append(cdVar.d).append("#");
                    stringBuilder.append(str3).append("#");
                    str = (list.isEmpty() && !a(this.u)) ? "cgi" : "cgiwifi";
                    stringBuilder.append(str);
                    str = stringBuilder.toString();
                    break;
                }
                break;
            case 2:
                if (!a.isEmpty()) {
                    cdVar = (cd) a.get(0);
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(cdVar.a).append("#");
                    stringBuilder.append(cdVar.b).append("#");
                    stringBuilder.append(cdVar.g).append("#");
                    stringBuilder.append(cdVar.h).append("#");
                    stringBuilder.append(cdVar.i).append("#");
                    stringBuilder.append(str3).append("#");
                    str = (list.isEmpty() && !a(this.u)) ? "cgi" : "cgiwifi";
                    stringBuilder.append(str);
                    str = stringBuilder.toString();
                    break;
                }
                break;
            case 9:
                Object obj = (list.isEmpty() && !a(this.u)) ? null : 1;
                if (!z) {
                    if (a(this.u) && list.isEmpty()) {
                        this.j = 2;
                        c.append("⊗ around wifi(s) & has access wifi");
                    } else if (list.size() == 1) {
                        this.j = 2;
                        if (a(this.u)) {
                            if (this.u.getBSSID().equals(((ScanResult) list.get(0)).BSSID)) {
                                c.append("same access wifi & around wifi 1");
                            }
                        } else {
                            c.append("⊗ access wifi & around wifi 1");
                        }
                    }
                    obj = null;
                }
                str = String.format(Locale.US, "#%s#", new Object[]{str3});
                if (obj == null) {
                    if (str3.equals("network")) {
                        str = "";
                        this.j = 2;
                        c.append("is network & no wifi");
                        break;
                    }
                }
                str = str + "wifi";
                break;
                break;
            default:
                this.j = 11;
                c.append("get cgi failure");
                break;
        }
        if (!TextUtils.isEmpty(str)) {
            if (!str.startsWith("#")) {
                str = "#" + str;
            }
            str = cw.j() + str;
        }
    }

    public synchronized void b() {
        this.i = false;
        e.n = false;
        u();
        this.L = null;
        this.M = null;
        this.S = null;
        H();
        if (this.d != null) {
            this.d.a();
            this.d = null;
            this.f = false;
            this.e = -1;
        }
        y();
        try {
            cj.a().a(this.m, 1);
        } catch (Throwable th) {
            e.a(th, "APS", "destroy part");
        }
        bz.a().b();
        cw.i();
        try {
            if (this.m != null) {
                this.m.unregisterReceiver(this.t);
            }
            this.t = null;
        } catch (Throwable th2) {
            this.t = null;
        }
        if (this.p != null) {
            this.p.i();
        }
        ci.a().c();
        cl.a().c();
        cc.a();
        J();
        this.x = 0;
        this.Q = 0;
        n();
        this.w = null;
        this.m = null;
        ae = -1;
    }

    public synchronized void b(Context context) {
        try {
            if (ae == -1) {
                ae = 1;
                n.a(this.m, e.a("2.4.0"));
                ct.a(context);
            }
        } catch (Throwable th) {
            e.a(th, "APS", "initAuth");
        }
    }

    public String c() {
        return "2.4.0";
    }

    public synchronized StringBuilder c(boolean z) {
        StringBuilder stringBuilder;
        Object obj = null;
        synchronized (this) {
            ce ceVar = this.p;
            if (this.A) {
                ceVar.g();
            }
            stringBuilder = new StringBuilder(700);
            int c = ceVar.c();
            ArrayList a = ceVar.a();
            switch (c) {
                case 1:
                    for (c = 1; c < a.size(); c++) {
                        stringBuilder.append("#").append(((cd) a.get(c)).b);
                        stringBuilder.append("|").append(((cd) a.get(c)).c);
                        stringBuilder.append("|").append(((cd) a.get(c)).d);
                    }
                    break;
            }
            if (((!z && TextUtils.isEmpty(this.F)) || this.F.equals("00:00:00:00:00:00")) && this.u != null) {
                this.F = this.u.getMacAddress();
                v();
                if (TextUtils.isEmpty(this.F)) {
                    this.F = "00:00:00:00:00:00";
                }
            }
            if (p()) {
                String str = "";
                if (a(this.u)) {
                    str = this.u.getBSSID();
                }
                String str2 = str;
                List list = this.q;
                int size = list.size();
                for (c = 0; c < size; c++) {
                    str = "nb";
                    if (str2.equals(((ScanResult) list.get(c)).BSSID)) {
                        str = "access";
                        int i = 1;
                    }
                    stringBuilder.append(String.format(Locale.US, "#%s,%s", new Object[]{((ScanResult) list.get(c)).BSSID, str}));
                }
                if (obj == null && !TextUtils.isEmpty(str2)) {
                    stringBuilder.append("#").append(str2);
                    stringBuilder.append(",access");
                }
            } else {
                n();
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(0);
            }
        }
        return stringBuilder;
    }

    public synchronized void d() {
        List list = this.q;
        Collection collection = this.r;
        list.clear();
        synchronized (this.h) {
            if (collection != null) {
                if (collection.size() > 0) {
                    list.addAll(collection);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void e() {
        if (this.q != null) {
            if (!this.q.isEmpty()) {
                boolean z;
                Object hashtable;
                List list;
                int size;
                int i;
                ScanResult scanResult;
                int length;
                TreeMap treeMap;
                if ((cw.b() - this.D <= 3600000 ? 1 : null) == null) {
                    n();
                }
                boolean h = cw.h();
                if (cw.a(this.v, "nbssid")) {
                    try {
                        if (this.v.getString("nbssid").equals("1")) {
                            h = true;
                        } else if (this.v.getString("nbssid").equals("0")) {
                            h = false;
                        }
                        z = h;
                    } catch (Throwable th) {
                        e.a(th, "APS", "setWifiOrder part");
                    }
                    hashtable = new Hashtable();
                    list = this.q;
                    size = list.size();
                    for (i = 0; i < size; i++) {
                        scanResult = (ScanResult) list.get(i);
                        if (!cw.a(scanResult)) {
                            if (size > 20) {
                                if (a(scanResult.level)) {
                                    continue;
                                }
                            }
                            if (TextUtils.isEmpty(scanResult.SSID)) {
                                scanResult.SSID = "unkwn";
                            } else if (z) {
                                scanResult.SSID = String.valueOf(i);
                            } else {
                                scanResult.SSID = scanResult.SSID.replace("*", ".");
                                try {
                                    length = scanResult.SSID.getBytes("UTF-8").length;
                                } catch (Throwable th2) {
                                    e.a(th2, "APS", "setWifiOrder");
                                    length = 32;
                                }
                                if (length >= 32) {
                                    scanResult.SSID = String.valueOf(i);
                                }
                            }
                            hashtable.put(Integer.valueOf((scanResult.level * 30) + i), scanResult);
                        }
                    }
                    treeMap = new TreeMap(Collections.reverseOrder());
                    treeMap.putAll(hashtable);
                    list.clear();
                    for (Object obj : treeMap.keySet()) {
                        list.add(treeMap.get(obj));
                    }
                    hashtable.clear();
                    treeMap.clear();
                }
                z = h;
                hashtable = new Hashtable();
                list = this.q;
                size = list.size();
                for (i = 0; i < size; i++) {
                    scanResult = (ScanResult) list.get(i);
                    if (!cw.a(scanResult)) {
                        if (size > 20) {
                            if (a(scanResult.level)) {
                                continue;
                            }
                        }
                        if (TextUtils.isEmpty(scanResult.SSID)) {
                            scanResult.SSID = "unkwn";
                        } else if (z) {
                            scanResult.SSID = scanResult.SSID.replace("*", ".");
                            length = scanResult.SSID.getBytes("UTF-8").length;
                            if (length >= 32) {
                                scanResult.SSID = String.valueOf(i);
                            }
                        } else {
                            scanResult.SSID = String.valueOf(i);
                        }
                        hashtable.put(Integer.valueOf((scanResult.level * 30) + i), scanResult);
                    }
                }
                treeMap = new TreeMap(Collections.reverseOrder());
                treeMap.putAll(hashtable);
                list.clear();
                while (r1.hasNext()) {
                    list.add(treeMap.get(obj));
                }
                hashtable.clear();
                treeMap.clear();
            }
        }
    }

    public synchronized String f() {
        if (this.p.a(this.A)) {
            this.p.h();
        }
        try {
            StringBuilder a = a(null);
            if (a == null) {
                c.append("get parames is null");
                return null;
            }
            return a.toString();
        } catch (Throwable th) {
            e.a(th, "APS", "getApsReq");
            c.append("get parames error:" + th.getMessage());
            return null;
        }
    }

    public synchronized void g() {
        if (this.E >= 1) {
            if (!this.f) {
                if (this.d == null) {
                    this.d = new bw(this.m.getApplicationContext());
                    this.d.a(this.l);
                }
                try {
                    if (this.d != null) {
                        this.d.b();
                    }
                    this.f = true;
                } catch (Throwable th) {
                    e.a(th, "APS", "bindService");
                    this.f = true;
                }
            }
        }
    }

    public void h() {
        if (this.k && !r()) {
            s();
        }
    }
}
