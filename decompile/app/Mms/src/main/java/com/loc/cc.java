package com.loc;

import android.content.Context;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.autonavi.aps.amapapi.model.AmapLoc;
import com.loc.bx.c;
import com.loc.ch.a;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import org.json.JSONObject;

/* compiled from: Off */
public class cc {
    public static final int[] a = new int[]{0, 0};
    static int b = 213891;
    private static volatile String c = null;
    private static Hashtable<String, Long> d = new Hashtable();
    private static cm e = new cm();
    private static Hashtable<String, String> f = new Hashtable();
    private static TelephonyManager g = null;

    private cc() {
    }

    static int a(int i) {
        int i2 = 0;
        int[] iArr = new int[32];
        int i3 = 0;
        while (i2 < 4) {
            iArr[i2] = (i >> (i2 * 8)) & 255;
            iArr[i2] = ((iArr[i2] << 4) & 240) + ((iArr[i2] >> 4) & 15);
            i3 += (iArr[i2] & 255) << ((3 - i2) * 8);
            i2++;
        }
        return b + i3;
    }

    private static int a(int i, ch chVar, String str, int[] iArr, int i2, int i3, String str2, int i4) {
        int i5 = i4 + 1;
        if (i5 > 25) {
            return -1;
        }
        int i6 = (((((i2 + i3) / 2) - i) / 16) * 16) + i;
        int a = a(chVar, str, iArr, i6, str2);
        if (i2 == i6 && i6 == i3) {
            if (a != 0) {
                i2 = -1;
            }
            return i2;
        } else if (a == Integer.MAX_VALUE) {
            return -1;
        } else {
            if (a == 0) {
                return i6;
            }
            if (a < 0) {
                return a(i, chVar, str, iArr, i2, i6, str2, i5);
            }
            return a(i, chVar, str, iArr, i6 + 16, i3, str2, i5);
        }
    }

    private static int a(ch chVar, String str, int[] iArr, int i, String str2) {
        try {
            chVar.a((long) i);
            int i2;
            int i3;
            if (str2.equals("gsm")) {
                i2 = iArr[0];
                i3 = iArr[1];
                int d = chVar.d();
                int e = chVar.e();
                return i2 >= d ? i2 <= d ? i3 >= e ? i3 <= e ? 0 : 1 : -1 : 1 : -1;
            } else if (str2.equals("cdma")) {
                r2 = new int[]{iArr[0], iArr[1], iArr[2]};
                int[] iArr2 = new int[3];
                for (i2 = 0; i2 < 3; i2++) {
                    iArr2[i2] = chVar.d();
                    if (r2[i2] < iArr2[i2]) {
                        return -1;
                    }
                    if (r2[i2] > iArr2[i2]) {
                        return 1;
                    }
                }
                return 0;
            } else {
                if (str2.equals("wifi")) {
                    byte[] b = cw.b(str);
                    int[] iArr3 = new int[6];
                    i3 = 0;
                    while (i3 < 6) {
                        iArr3[i3] = b[i3] >= (byte) 0 ? b[i3] : b[i3] + 256;
                        i3++;
                    }
                    r2 = new int[6];
                    for (i2 = 0; i2 < 6; i2++) {
                        r2[i2] = chVar.f();
                        if (iArr3[i2] < r2[i2]) {
                            return -1;
                        }
                        if (iArr3[i2] > r2[i2]) {
                            return 1;
                        }
                    }
                    return 0;
                }
                return Integer.MAX_VALUE;
            }
        } catch (Throwable th) {
            e.a(th, "Off", "cmpItem");
        }
    }

    private static int a(String str) {
        if (TextUtils.isEmpty(str) || !str.contains("cgi")) {
            return 9;
        }
        String[] split = str.split("#");
        return split.length != 7 ? split.length == 8 ? 2 : 9 : 1;
    }

    private static AmapLoc a(Hashtable<String, String> hashtable, Hashtable<String, String> hashtable2, int i, int i2) {
        String str;
        ArrayList a;
        AmapLoc amapLoc;
        bx bxVar = new bx();
        if (!hashtable.isEmpty()) {
            for (Entry value : hashtable.entrySet()) {
                str = (String) value.getValue();
                int i3 = !str.contains("access") ? 0 : 1;
                if (str.contains("|")) {
                    try {
                        bxVar.a(i3 == 0 ? 2 : 1, str.substring(0, str.lastIndexOf("|")));
                    } catch (Throwable th) {
                        e.a(th, "Off", "calLoc part3");
                    }
                }
            }
        }
        if (!hashtable2.isEmpty()) {
            for (Entry value2 : hashtable2.entrySet()) {
                str = (String) value2.getValue();
                if (str.contains("|")) {
                    try {
                        bxVar.a(0, str.substring(0, str.lastIndexOf("|")));
                    } catch (Throwable th2) {
                        e.a(th2, "Off", "calLoc part2");
                    }
                }
            }
        }
        try {
            a = bxVar.a((double) i2, (double) i);
        } catch (Throwable th22) {
            e.a(th22, "Off", "calLoc part4");
            a = null;
        }
        if (a == null || a.isEmpty()) {
            amapLoc = null;
        } else {
            c cVar = (c) a.get(0);
            AmapLoc amapLoc2 = new AmapLoc();
            amapLoc2.c("network");
            amapLoc2.b(cVar.a);
            amapLoc2.a(cVar.b);
            amapLoc2.a((float) cVar.c);
            amapLoc2.k(cVar.d);
            amapLoc2.x("0");
            amapLoc2.a(cw.a());
            a.clear();
            amapLoc = amapLoc2;
        }
        if (!cw.a(amapLoc)) {
            return null;
        }
        amapLoc.f("file");
        return amapLoc;
    }

    public static AmapLoc a(double[] dArr, String str, String str2, String str3, int i, Context context, int[] iArr) {
        Throwable e;
        if (TextUtils.isEmpty(str2)) {
            return null;
        }
        if (str2.contains(GeocodeSearch.GPS)) {
            return null;
        }
        int i2;
        int i3;
        int i4;
        String stringBuilder;
        int indexOf;
        String str4;
        cm cmVar;
        ch chVar;
        File file;
        Object obj;
        ch chVar2;
        a aVar;
        int i5;
        long c;
        long g;
        int i6;
        AmapLoc a;
        int a2 = a(str2);
        String a3 = a(a2, str2);
        Hashtable hashtable = new Hashtable();
        a(a2, str2, str3, hashtable);
        Hashtable hashtable2 = new Hashtable();
        a(str3, hashtable2);
        StringBuilder c2 = c();
        String[] a4 = dArr != null ? a(dArr[0], dArr[1], str) : a(0.0d, 0.0d, str);
        int length = a4.length / 2;
        if (1 <= i) {
            if (i > 3) {
            }
            e.o = hashtable.size();
            i2 = 0;
            i3 = 0;
            i4 = 0;
            while (i4 < a4.length && e.n) {
                if (i4 < length) {
                    if (i3 <= 0 && !hashtable.isEmpty()) {
                    }
                    if (i == 1) {
                        if (i == 2) {
                            if (i4 > 8) {
                                if (i4 < 25) {
                                    continue;
                                    i4++;
                                    i3 = i3;
                                    i2 = i2;
                                }
                            }
                            if (i4 > 33) {
                            }
                        }
                    } else if (i4 != 0) {
                    }
                    stringBuilder = c2.toString();
                    if (i4 < length) {
                        switch (a2) {
                            case 1:
                                iArr[0] = 0;
                                iArr[1] = 0;
                                break;
                            case 2:
                                break;
                            default:
                                iArr[0] = 0;
                                iArr[1] = 0;
                                break;
                        }
                        stringBuilder = stringBuilder + a3 + File.separator;
                        stringBuilder = a4[i4].startsWith("-") ? stringBuilder + a4[i4].substring(0, 3) + "," : stringBuilder + a4[i4].substring(0, 4) + ",";
                        indexOf = a4[i4].indexOf(",") + 1;
                        stringBuilder = a4[i4].substring(indexOf, indexOf + 1).startsWith("-") ? stringBuilder + a4[i4].substring(indexOf, indexOf + 3) : stringBuilder + a4[i4].substring(indexOf, indexOf + 4);
                        str4 = (stringBuilder + File.separator) + a4[i4];
                        if (str4.equals(c)) {
                            cmVar = e;
                            chVar = (ch) cmVar.b(str4);
                            file = new File(str4);
                            if (chVar == null) {
                                obj = 1;
                                chVar2 = chVar;
                            } else if (file.getParentFile().exists() && !file.isDirectory() && file.exists()) {
                                aVar = new a();
                                try {
                                    chVar = new ch(file, aVar);
                                } catch (Throwable e2) {
                                    e.a(e2, "Off", "search part1");
                                } catch (Throwable e22) {
                                    e.a(e22, "Off", "search part3");
                                    chVar = null;
                                }
                                obj = null;
                                chVar2 = chVar;
                            }
                            i5 = 0;
                            if (chVar2 == null) {
                                try {
                                    chVar2.a(0);
                                    c = chVar2.c();
                                    if (i4 < length) {
                                        i5 = chVar2.d();
                                    }
                                    try {
                                        g = chVar2.g();
                                        i6 = 8;
                                        if (i4 < length) {
                                            i6 = ((i5 * 4) + 2) + 8;
                                        }
                                        if ((c + 7776000000L < cw.a() ? 1 : null) != null) {
                                            if (chVar2 != null) {
                                                if (obj != null) {
                                                    try {
                                                        chVar2.b();
                                                    } catch (Throwable e222) {
                                                        e.a(e222, "Off", "search part6");
                                                    }
                                                } else {
                                                    cmVar.c(str4);
                                                }
                                            }
                                            file.delete();
                                            d.remove(a4[i4]);
                                        } else {
                                            if ((g > 8 ? 1 : null) == null || (g - ((long) i6)) % 16 != 0) {
                                                if (chVar2 != null) {
                                                    try {
                                                        chVar2.b();
                                                    } catch (Throwable e2222) {
                                                        e.a(e2222, "Off", "search part7");
                                                    }
                                                }
                                                file.delete();
                                                d.remove(a4[i4]);
                                            } else {
                                                int i7;
                                                Object obj2;
                                                Object obj3;
                                                double[] a5;
                                                Entry entry;
                                                if (i4 < length && !hashtable.isEmpty() && i3 < e.o) {
                                                    i7 = 1;
                                                } else {
                                                    obj2 = null;
                                                }
                                                if (i4 >= length && !hashtable2.isEmpty() && i2 < 15) {
                                                    int i8 = 1;
                                                } else {
                                                    obj3 = null;
                                                }
                                                if (obj2 == null) {
                                                    i7 = i3;
                                                } else {
                                                    try {
                                                        i7 = i3;
                                                        for (Entry entry2 : hashtable.entrySet()) {
                                                            try {
                                                                a5 = a(i6, chVar2, ((String) entry2.getKey()).toString(), 0);
                                                                if (a5 != null) {
                                                                    i7++;
                                                                    hashtable.put(((String) entry2.getKey()).toString(), ((a5[0] + "|" + a5[1]) + "|" + a5[2] + "|") + ((String) hashtable.get(((String) entry2.getKey()).toString())));
                                                                    if (i7 < e.o) {
                                                                    }
                                                                }
                                                                i7 = i7;
                                                            } catch (Throwable th) {
                                                                e2222 = th;
                                                            }
                                                        }
                                                    } catch (Throwable th2) {
                                                        e2222 = th2;
                                                        i7 = i3;
                                                        e.a(e2222, "Off", "search part8");
                                                        i3 = i7;
                                                        if (chVar2 == null) {
                                                            if (chVar2.a()) {
                                                                try {
                                                                    chVar2.b();
                                                                } catch (Throwable e22222) {
                                                                    e.a(e22222, "Off", "search part9");
                                                                }
                                                            } else if (obj == null) {
                                                                cmVar.b(str4, chVar2);
                                                            }
                                                        }
                                                        i4++;
                                                        i3 = i3;
                                                        i2 = i2;
                                                    }
                                                }
                                                if (obj3 != null) {
                                                    Iterator it = hashtable2.entrySet().iterator();
                                                    while (it != null && it.hasNext()) {
                                                        entry2 = (Entry) it.next();
                                                        a5 = a(i6, chVar2, ((String) entry2.getKey()).toString(), 1);
                                                        if (a5 != null) {
                                                            i2++;
                                                            hashtable2.put(((String) entry2.getKey()).toString(), ((a5[0] + "|" + a5[1]) + "|" + a5[2] + "|") + ((String) hashtable2.get(((String) entry2.getKey()).toString())));
                                                            if (i2 < 15) {
                                                            }
                                                        }
                                                        i2 = i2;
                                                    }
                                                }
                                                i3 = i7;
                                                if (chVar2 == null) {
                                                    if (chVar2.a()) {
                                                        chVar2.b();
                                                    } else if (obj == null) {
                                                        cmVar.b(str4, chVar2);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Throwable e222222) {
                                        e.a(e222222, "Off", "search part5");
                                        if (obj != null) {
                                            cmVar.c(str4);
                                        }
                                    }
                                } catch (Throwable e2222222) {
                                    e.a(e2222222, "Off", "search part4");
                                    if (obj != null) {
                                        cmVar.c(str4);
                                    }
                                }
                            }
                        }
                    }
                    i4++;
                    i3 = i3;
                    i2 = i2;
                }
                if (i4 >= length) {
                    if (i2 <= 0 && !hashtable2.isEmpty()) {
                    }
                    if (i == 1) {
                        if (i4 != 0) {
                        }
                    } else if (i == 2) {
                        if (i4 > 8) {
                            if (i4 < 25) {
                                continue;
                                i4++;
                                i3 = i3;
                                i2 = i2;
                            }
                        }
                        if (i4 > 33) {
                        }
                    }
                    stringBuilder = c2.toString();
                    if (i4 < length) {
                        switch (a2) {
                            case 1:
                                iArr[0] = 0;
                                iArr[1] = 0;
                                break;
                            case 2:
                                break;
                            default:
                                iArr[0] = 0;
                                iArr[1] = 0;
                                break;
                        }
                        stringBuilder = stringBuilder + a3 + File.separator;
                        if (a4[i4].startsWith("-")) {
                        }
                        indexOf = a4[i4].indexOf(",") + 1;
                        if (a4[i4].substring(indexOf, indexOf + 1).startsWith("-")) {
                        }
                        str4 = (stringBuilder + File.separator) + a4[i4];
                        if (str4.equals(c)) {
                            cmVar = e;
                            chVar = (ch) cmVar.b(str4);
                            file = new File(str4);
                            if (chVar == null) {
                                aVar = new a();
                                chVar = new ch(file, aVar);
                                obj = null;
                                chVar2 = chVar;
                            } else {
                                obj = 1;
                                chVar2 = chVar;
                            }
                            i5 = 0;
                            if (chVar2 == null) {
                                chVar2.a(0);
                                c = chVar2.c();
                                if (i4 < length) {
                                    i5 = chVar2.d();
                                }
                                g = chVar2.g();
                                i6 = 8;
                                if (i4 < length) {
                                    i6 = ((i5 * 4) + 2) + 8;
                                }
                                if (c + 7776000000L < cw.a()) {
                                }
                                if ((c + 7776000000L < cw.a() ? 1 : null) != null) {
                                    if (g > 8) {
                                    }
                                    if ((g > 8 ? 1 : null) == null) {
                                    }
                                    if (chVar2 != null) {
                                        chVar2.b();
                                    }
                                    file.delete();
                                    d.remove(a4[i4]);
                                } else {
                                    if (chVar2 != null) {
                                        if (obj != null) {
                                            cmVar.c(str4);
                                        } else {
                                            chVar2.b();
                                        }
                                    }
                                    file.delete();
                                    d.remove(a4[i4]);
                                }
                            }
                        }
                    }
                    i4++;
                    i3 = i3;
                    i2 = i2;
                }
                if (i4 >= length && i3 <= 0) {
                }
                if (i == 1) {
                    if (i == 2) {
                        if (i4 > 8) {
                            if (i4 < 25) {
                                continue;
                                i4++;
                                i3 = i3;
                                i2 = i2;
                            }
                        }
                        if (i4 > 33) {
                        }
                    }
                } else if (i4 != 0) {
                }
                stringBuilder = c2.toString();
                if (i4 < length) {
                    switch (a2) {
                        case 1:
                            iArr[0] = 0;
                            iArr[1] = 0;
                            break;
                        case 2:
                            break;
                        default:
                            iArr[0] = 0;
                            iArr[1] = 0;
                            break;
                    }
                    stringBuilder = stringBuilder + a3 + File.separator;
                    if (a4[i4].startsWith("-")) {
                    }
                    indexOf = a4[i4].indexOf(",") + 1;
                    if (a4[i4].substring(indexOf, indexOf + 1).startsWith("-")) {
                    }
                    str4 = (stringBuilder + File.separator) + a4[i4];
                    if (str4.equals(c)) {
                        cmVar = e;
                        chVar = (ch) cmVar.b(str4);
                        file = new File(str4);
                        if (chVar == null) {
                            obj = 1;
                            chVar2 = chVar;
                        } else {
                            aVar = new a();
                            chVar = new ch(file, aVar);
                            obj = null;
                            chVar2 = chVar;
                        }
                        i5 = 0;
                        if (chVar2 == null) {
                            chVar2.a(0);
                            c = chVar2.c();
                            if (i4 < length) {
                                i5 = chVar2.d();
                            }
                            g = chVar2.g();
                            i6 = 8;
                            if (i4 < length) {
                                i6 = ((i5 * 4) + 2) + 8;
                            }
                            if (c + 7776000000L < cw.a()) {
                            }
                            if ((c + 7776000000L < cw.a() ? 1 : null) != null) {
                                if (chVar2 != null) {
                                    if (obj != null) {
                                        chVar2.b();
                                    } else {
                                        cmVar.c(str4);
                                    }
                                }
                                file.delete();
                                d.remove(a4[i4]);
                            } else {
                                if (g > 8) {
                                }
                                if ((g > 8 ? 1 : null) == null) {
                                }
                                if (chVar2 != null) {
                                    chVar2.b();
                                }
                                file.delete();
                                d.remove(a4[i4]);
                            }
                        }
                    }
                }
                i4++;
                i3 = i3;
                i2 = i2;
            }
            c2.delete(0, c2.length());
            a = a(hashtable, hashtable2, iArr[0], iArr[1]);
            return cw.a(a) ? null : a;
        }
        i = 1;
        e.o = hashtable.size();
        i2 = 0;
        i3 = 0;
        i4 = 0;
        while (i4 < a4.length) {
            if (i4 < length) {
            }
            if (i4 >= length) {
            }
            if (i == 1) {
                if (i4 != 0) {
                    c2.delete(0, c2.length());
                    a = a(hashtable, hashtable2, iArr[0], iArr[1]);
                    if (cw.a(a)) {
                    }
                }
            } else if (i == 2) {
                if (i4 > 8) {
                    if (i4 < 25) {
                        continue;
                        i4++;
                        i3 = i3;
                        i2 = i2;
                    }
                }
                if (i4 > 33) {
                    c2.delete(0, c2.length());
                    a = a(hashtable, hashtable2, iArr[0], iArr[1]);
                    if (cw.a(a)) {
                    }
                }
            }
            stringBuilder = c2.toString();
            if (i4 < length) {
                switch (a2) {
                    case 1:
                        iArr[0] = 0;
                        iArr[1] = 0;
                        break;
                    case 2:
                        break;
                    default:
                        iArr[0] = 0;
                        iArr[1] = 0;
                        break;
                }
                stringBuilder = stringBuilder + a3 + File.separator;
                if (a4[i4].startsWith("-")) {
                }
                indexOf = a4[i4].indexOf(",") + 1;
                if (a4[i4].substring(indexOf, indexOf + 1).startsWith("-")) {
                }
                str4 = (stringBuilder + File.separator) + a4[i4];
                if (str4.equals(c)) {
                    cmVar = e;
                    chVar = (ch) cmVar.b(str4);
                    file = new File(str4);
                    if (chVar == null) {
                        aVar = new a();
                        chVar = new ch(file, aVar);
                        obj = null;
                        chVar2 = chVar;
                    } else {
                        obj = 1;
                        chVar2 = chVar;
                    }
                    i5 = 0;
                    if (chVar2 == null) {
                        chVar2.a(0);
                        c = chVar2.c();
                        if (i4 < length) {
                            i5 = chVar2.d();
                        }
                        g = chVar2.g();
                        i6 = 8;
                        if (i4 < length) {
                            i6 = ((i5 * 4) + 2) + 8;
                        }
                        if (c + 7776000000L < cw.a()) {
                        }
                        if ((c + 7776000000L < cw.a() ? 1 : null) != null) {
                            if (g > 8) {
                            }
                            if ((g > 8 ? 1 : null) == null) {
                            }
                            if (chVar2 != null) {
                                chVar2.b();
                            }
                            file.delete();
                            d.remove(a4[i4]);
                        } else {
                            if (chVar2 != null) {
                                if (obj != null) {
                                    cmVar.c(str4);
                                } else {
                                    chVar2.b();
                                }
                            }
                            file.delete();
                            d.remove(a4[i4]);
                        }
                    }
                }
            }
            i4++;
            i3 = i3;
            i2 = i2;
        }
        c2.delete(0, c2.length());
        a = a(hashtable, hashtable2, iArr[0], iArr[1]);
        if (cw.a(a)) {
        }
    }

    private static String a(int i, String str) {
        String[] split = str.split("#");
        switch (i) {
            case 1:
                return split[1] + "_" + split[2];
            case 2:
                return split[3];
            default:
                return null;
        }
    }

    private static String a(String str, String str2, int i) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return null;
        }
        StringBuilder c = c();
        int indexOf;
        switch (i) {
            case 1:
                c.append(a(a(str), str)).append(File.separator);
                if (str2.startsWith("-")) {
                    c.append(str2.substring(0, 4));
                } else {
                    c.append(str2.substring(0, 3));
                }
                c.append(",");
                indexOf = str2.indexOf(",") + 1;
                if (str2.substring(indexOf, indexOf + 1).startsWith("-")) {
                    c.append(str2.substring(indexOf, indexOf + 4));
                } else {
                    c.append(str2.substring(indexOf, indexOf + 3));
                }
                c.append(File.separator).append(str2);
                break;
            case 2:
                c.append("wifi").append(File.separator);
                c.append(str2.substring(0, 3)).append(",");
                indexOf = str2.indexOf(",") + 1;
                c.append(str2.substring(indexOf, indexOf + 3));
                c.append(File.separator).append(str2);
                break;
            default:
                return null;
        }
        return c.toString();
    }

    public static ArrayList<String> a(String str, boolean z) {
        ArrayList<String> arrayList = null;
        if (f.isEmpty()) {
            return null;
        }
        int a = a(str);
        String[] split = str.split("#");
        switch (a) {
            case 1:
            case 2:
                ArrayList<String> arrayList2 = null;
                for (String str2 : f.keySet()) {
                    if (((String) f.get(str2)).contains("," + split[3] + ",")) {
                        if (arrayList2 == null) {
                            arrayList2 = new ArrayList();
                        }
                        arrayList2.add(str2);
                        if (z) {
                            return arrayList2;
                        }
                    }
                    arrayList2 = arrayList2;
                }
                arrayList = arrayList2;
                break;
        }
        return arrayList;
    }

    public static void a() {
        e.a();
        d.clear();
        f.clear();
        a[0] = 0;
        a[1] = 0;
    }

    private static void a(int i, String str, String str2, Hashtable<String, String> hashtable) {
        String[] split = str.split("#");
        switch (i) {
            case 1:
                hashtable.put(split[3] + "|" + split[4], "access");
                if (!TextUtils.isEmpty(str2) && str2.split("#").length <= 0) {
                    return;
                }
                return;
            case 2:
                hashtable.put(split[3] + "|" + split[4] + "|" + split[5], "access");
                return;
            default:
                return;
        }
    }

    private static void a(String str, Hashtable<String, String> hashtable) {
        if (!TextUtils.isEmpty(str)) {
            String[] strArr = new String[2];
            for (String str2 : str.split("#")) {
                if (str2.contains(",")) {
                    String[] split = str2.split(",");
                    hashtable.put(split[0], split[1]);
                }
            }
        }
    }

    private static boolean a(Context context, String str, int i, boolean z, boolean z2) {
        boolean z3 = z ? i >= 25 : i != 1;
        if (!str.contains("cgi") && z3) {
            return false;
        }
        if ((!str.contains("wifi") && !z3) || a[1] > AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST) {
            return false;
        }
        NetworkInfo c = cw.c(context);
        if (co.a(c) == -1) {
            return false;
        }
        if (c.getType() != 1 && z2) {
            return false;
        }
        if (!(c.getType() == 1 || z2 || g != null)) {
            g = (TelephonyManager) cw.a(context, "phone");
        }
        return true;
    }

    public static boolean a(Context context, String str, String str2, int i, int i2, boolean z, boolean z2) {
        if (!a(context, str, i, false, z)) {
            return false;
        }
        if (i2 == 0) {
            return a(context, str, str2, i, z2);
        }
        int i3 = i2 != 1 ? 24 : 8;
        String[] a = a(0.0d, 0.0d, str2);
        for (int i4 = 1; i4 < i3; i4++) {
            a(context, str, a[i4], i, z2);
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean a(Context context, String str, String str2, int i, boolean z) {
        GZIPInputStream gZIPInputStream;
        boolean z2;
        InputStream inputStream;
        InputStream inputStream2;
        GZIPInputStream gZIPInputStream2;
        Throwable e;
        HttpURLConnection httpURLConnection;
        boolean z3;
        HttpURLConnection httpURLConnection2;
        Throwable th;
        String[] strArr = new String[2];
        if (!a(str, str2, i, strArr)) {
            return false;
        }
        if (d.containsKey(strArr[1])) {
            if ((cw.a() - ((Long) d.get(strArr[1])).longValue() >= 86400000 ? 1 : null) == null) {
                return false;
            }
            d.remove(strArr[1]);
        }
        boolean z4 = false;
        InputStream inputStream3 = null;
        GZIPInputStream gZIPInputStream3 = null;
        HttpURLConnection httpURLConnection3 = null;
        try {
            cw.b();
            HashMap hashMap = new HashMap();
            hashMap.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
            httpURLConnection3 = co.a(context).a(context, "https://offline.aps.amap.com/LoadOfflineData/getData", hashMap, strArr[0].getBytes("UTF-8"));
            if (httpURLConnection3 != null) {
                int responseCode = httpURLConnection3.getResponseCode();
                if (responseCode == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                    for (Entry entry : httpURLConnection3.getHeaderFields().entrySet()) {
                        if ("code".equals(entry.getKey())) {
                            responseCode = Integer.parseInt((String) ((List) entry.getValue()).get(0));
                            break;
                        }
                    }
                    responseCode = 0;
                    if (responseCode != 260) {
                        if (e.n) {
                            d.put(strArr[1], Long.valueOf(cw.a()));
                        }
                        gZIPInputStream = null;
                        z2 = false;
                        inputStream = null;
                    } else {
                        c = strArr[1];
                        Object obj = 1;
                        inputStream2 = httpURLConnection3.getInputStream();
                        try {
                            gZIPInputStream2 = new GZIPInputStream(inputStream2);
                            try {
                                File file = new File(strArr[1]);
                                if (file.exists() && !file.delete()) {
                                    obj = null;
                                }
                                if (obj != null && e.n) {
                                    File parentFile = file.getParentFile();
                                    if (!parentFile.exists()) {
                                        parentFile.mkdirs();
                                    }
                                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file), 2048);
                                    byte[] bArr = new byte[2048];
                                    while (true) {
                                        int read = gZIPInputStream2.read(bArr, 0, 2048);
                                        if (read == -1) {
                                            break;
                                        }
                                        bufferedOutputStream.write(bArr, 0, read);
                                    }
                                    bufferedOutputStream.flush();
                                    bufferedOutputStream.close();
                                    z4 = true;
                                    d.put(strArr[1], Long.valueOf(cw.a()));
                                    String a = cw.a(0, "yyyyMMdd");
                                    if (a.equals(String.valueOf(a[0]))) {
                                        a[1] = a[1] + 1;
                                    } else {
                                        a[0] = Integer.parseInt(a);
                                        a[1] = 1;
                                    }
                                }
                                boolean z5 = z4;
                                inputStream = inputStream2;
                                z2 = z5;
                                gZIPInputStream = gZIPInputStream2;
                            } catch (UnknownHostException e2) {
                                e = e2;
                                gZIPInputStream3 = gZIPInputStream2;
                                inputStream3 = inputStream2;
                                httpURLConnection = httpURLConnection3;
                                z3 = z4;
                                httpURLConnection2 = httpURLConnection;
                            } catch (SocketException e3) {
                                e = e3;
                            } catch (SocketTimeoutException e4) {
                                e = e4;
                            } catch (EOFException e5) {
                                e = e5;
                            } catch (Throwable th2) {
                                e = th2;
                            }
                        } catch (UnknownHostException e6) {
                            e = e6;
                            inputStream3 = inputStream2;
                            httpURLConnection = httpURLConnection3;
                            z3 = false;
                            httpURLConnection2 = httpURLConnection;
                            try {
                                e.a(e, "Off", "c 2 part2");
                                if (gZIPInputStream3 != null) {
                                    try {
                                        gZIPInputStream3.close();
                                    } catch (Throwable e7) {
                                        e.a(e7, "Off", "c 2 part7");
                                    }
                                }
                                if (inputStream3 != null) {
                                    try {
                                        inputStream3.close();
                                    } catch (Throwable e72) {
                                        e.a(e72, "Off", "c 2 part8");
                                    }
                                }
                                if (httpURLConnection2 != null) {
                                    try {
                                        httpURLConnection2.disconnect();
                                    } catch (Throwable e722) {
                                        e.a(e722, "Off", "c 2 part9");
                                    }
                                }
                                z4 = z3;
                                if (!TextUtils.isEmpty(c)) {
                                    c = null;
                                }
                                return z4;
                            } catch (Throwable th3) {
                                e722 = th3;
                                httpURLConnection3 = httpURLConnection2;
                                inputStream2 = inputStream3;
                                gZIPInputStream2 = gZIPInputStream3;
                                if (gZIPInputStream2 != null) {
                                    try {
                                        gZIPInputStream2.close();
                                    } catch (Throwable th4) {
                                        e.a(th4, "Off", "c 2 part7");
                                    }
                                }
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Throwable th42) {
                                        e.a(th42, "Off", "c 2 part8");
                                    }
                                }
                                if (httpURLConnection3 != null) {
                                    try {
                                        httpURLConnection3.disconnect();
                                    } catch (Throwable th422) {
                                        e.a(th422, "Off", "c 2 part9");
                                    }
                                }
                                throw e722;
                            }
                        } catch (SocketException e8) {
                            e722 = e8;
                            gZIPInputStream2 = null;
                            try {
                                e.a(e722, "Off", "c 2 part3");
                                if (gZIPInputStream2 != null) {
                                    try {
                                        gZIPInputStream2.close();
                                    } catch (Throwable e7222) {
                                        e.a(e7222, "Off", "c 2 part7");
                                    }
                                }
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Throwable e72222) {
                                        e.a(e72222, "Off", "c 2 part8");
                                    }
                                }
                                if (httpURLConnection3 != null) {
                                    try {
                                        httpURLConnection3.disconnect();
                                    } catch (Throwable e722222) {
                                        e.a(e722222, "Off", "c 2 part9");
                                    }
                                }
                                if (TextUtils.isEmpty(c)) {
                                    c = null;
                                }
                                return z4;
                            } catch (Throwable th5) {
                                e722222 = th5;
                                if (gZIPInputStream2 != null) {
                                    gZIPInputStream2.close();
                                }
                                if (inputStream2 != null) {
                                    inputStream2.close();
                                }
                                if (httpURLConnection3 != null) {
                                    httpURLConnection3.disconnect();
                                }
                                throw e722222;
                            }
                        } catch (SocketTimeoutException e9) {
                            e722222 = e9;
                            gZIPInputStream2 = null;
                            e.a(e722222, "Off", "c 2 part4");
                            if (gZIPInputStream2 != null) {
                                try {
                                    gZIPInputStream2.close();
                                } catch (Throwable e7222222) {
                                    e.a(e7222222, "Off", "c 2 part7");
                                }
                            }
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (Throwable e72222222) {
                                    e.a(e72222222, "Off", "c 2 part8");
                                }
                            }
                            if (httpURLConnection3 != null) {
                                try {
                                    httpURLConnection3.disconnect();
                                } catch (Throwable e722222222) {
                                    e.a(e722222222, "Off", "c 2 part9");
                                }
                            }
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (EOFException e10) {
                            e722222222 = e10;
                            gZIPInputStream2 = null;
                            e.a(e722222222, "Off", "c 2 part5");
                            if (gZIPInputStream2 != null) {
                                try {
                                    gZIPInputStream2.close();
                                } catch (Throwable e7222222222) {
                                    e.a(e7222222222, "Off", "c 2 part7");
                                }
                            }
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (Throwable e72222222222) {
                                    e.a(e72222222222, "Off", "c 2 part8");
                                }
                            }
                            if (httpURLConnection3 != null) {
                                try {
                                    httpURLConnection3.disconnect();
                                } catch (Throwable e722222222222) {
                                    e.a(e722222222222, "Off", "c 2 part9");
                                }
                            }
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (Throwable th6) {
                            e722222222222 = th6;
                            gZIPInputStream2 = null;
                            if (gZIPInputStream2 != null) {
                                gZIPInputStream2.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection3 != null) {
                                httpURLConnection3.disconnect();
                            }
                            throw e722222222222;
                        }
                    }
                    if (z) {
                        try {
                            b(strArr[1]);
                        } catch (Throwable e11) {
                            th = e11;
                            inputStream3 = inputStream;
                            httpURLConnection2 = httpURLConnection3;
                            z3 = z2;
                            gZIPInputStream3 = gZIPInputStream;
                            e722222222222 = th;
                            e.a(e722222222222, "Off", "c 2 part2");
                            if (gZIPInputStream3 != null) {
                                gZIPInputStream3.close();
                            }
                            if (inputStream3 != null) {
                                inputStream3.close();
                            }
                            if (httpURLConnection2 != null) {
                                httpURLConnection2.disconnect();
                            }
                            z4 = z3;
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (Throwable e112) {
                            inputStream2 = inputStream;
                            z4 = z2;
                            th = e112;
                            gZIPInputStream2 = gZIPInputStream;
                            e722222222222 = th;
                            e.a(e722222222222, "Off", "c 2 part3");
                            if (gZIPInputStream2 != null) {
                                gZIPInputStream2.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection3 != null) {
                                httpURLConnection3.disconnect();
                            }
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (Throwable e1122) {
                            inputStream2 = inputStream;
                            z4 = z2;
                            th = e1122;
                            gZIPInputStream2 = gZIPInputStream;
                            e722222222222 = th;
                            e.a(e722222222222, "Off", "c 2 part4");
                            if (gZIPInputStream2 != null) {
                                gZIPInputStream2.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection3 != null) {
                                httpURLConnection3.disconnect();
                            }
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (Throwable e11222) {
                            inputStream2 = inputStream;
                            z4 = z2;
                            th = e11222;
                            gZIPInputStream2 = gZIPInputStream;
                            e722222222222 = th;
                            e.a(e722222222222, "Off", "c 2 part5");
                            if (gZIPInputStream2 != null) {
                                gZIPInputStream2.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection3 != null) {
                                httpURLConnection3.disconnect();
                            }
                            if (TextUtils.isEmpty(c)) {
                                c = null;
                            }
                            return z4;
                        } catch (Throwable th7) {
                            gZIPInputStream2 = gZIPInputStream;
                            inputStream2 = inputStream;
                            e722222222222 = th7;
                            if (gZIPInputStream2 != null) {
                                gZIPInputStream2.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection3 != null) {
                                httpURLConnection3.disconnect();
                            }
                            throw e722222222222;
                        }
                    }
                } else if (responseCode != MsgUrlService.RESULT_SERVER_ERROR) {
                    gZIPInputStream = null;
                    z2 = false;
                    inputStream = null;
                } else {
                    gZIPInputStream = null;
                    z2 = false;
                    inputStream = null;
                }
                if (gZIPInputStream != null) {
                    try {
                        gZIPInputStream.close();
                    } catch (Throwable e7222222222222) {
                        e.a(e7222222222222, "Off", "c 2 part7");
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e72222222222222) {
                        e.a(e72222222222222, "Off", "c 2 part8");
                    }
                }
                if (httpURLConnection3 != null) {
                    try {
                        httpURLConnection3.disconnect();
                    } catch (Throwable e722222222222222) {
                        e.a(e722222222222222, "Off", "c 2 part9");
                    }
                }
                z4 = z2;
                if (TextUtils.isEmpty(c)) {
                    c = null;
                }
                return z4;
            }
            if (httpURLConnection3 != null) {
                try {
                    httpURLConnection3.disconnect();
                } catch (Throwable e7222222222222222) {
                    e.a(e7222222222222222, "Off", "c 2 part9");
                }
            }
            return false;
        } catch (UnknownHostException e12) {
            e7222222222222222 = e12;
            httpURLConnection = httpURLConnection3;
            z3 = false;
            httpURLConnection2 = httpURLConnection;
            e.a(e7222222222222222, "Off", "c 2 part2");
            if (gZIPInputStream3 != null) {
                gZIPInputStream3.close();
            }
            if (inputStream3 != null) {
                inputStream3.close();
            }
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            z4 = z3;
            if (TextUtils.isEmpty(c)) {
                c = null;
            }
            return z4;
        } catch (SocketException e13) {
            e7222222222222222 = e13;
            inputStream2 = null;
            gZIPInputStream2 = null;
        } catch (SocketTimeoutException e14) {
            e7222222222222222 = e14;
            inputStream2 = null;
            gZIPInputStream2 = null;
        } catch (EOFException e15) {
            e7222222222222222 = e15;
            inputStream2 = null;
            gZIPInputStream2 = null;
        } catch (Throwable th8) {
            e7222222222222222 = th8;
            inputStream2 = null;
            gZIPInputStream2 = null;
        }
    }

    public static boolean a(String str, String str2, int i, int i2) {
        boolean z = true;
        if (TextUtils.isEmpty(str2)) {
            return false;
        }
        if (i2 == 0) {
            String a = a(str, str2, i);
            if (a != null) {
                File file = new File(a);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
                if (f.containsKey(a)) {
                    f.remove(a);
                }
                if (d.containsKey(a)) {
                    d.remove(a);
                }
            }
            return true;
        } else if (i2 != 1 && i2 != 2) {
            return false;
        } else {
            String[] a2 = a(0.0d, 0.0d, str2);
            boolean z2 = i2 != 1 ? i2 != 2 ? false : true : true;
            if (i == 1) {
                z = false;
            } else if (i != 2) {
                return false;
            }
            Hashtable hashtable = f;
            Hashtable hashtable2 = d;
            for (z = 
/*
Method generation error in method: com.loc.cc.a(java.lang.String, java.lang.String, int, int):boolean
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r0_2 'z' boolean) = (r0_0 'z' boolean), (r0_1 'z' boolean) binds: {(r0_0 'z' boolean)=B:9:0x001c, (r0_1 'z' boolean)=B:30:0x0060} in method: com.loc.cc.a(java.lang.String, java.lang.String, int, int):boolean
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:184)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:61)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:128)
	at jadx.core.codegen.RegionGen.connectElseIf(RegionGen.java:146)
	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:124)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:57)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 26 more

*/

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private static boolean a(String str, String str2, int i, String[] strArr) {
                Object a;
                Throwable th;
                long j;
                JSONObject jSONObject;
                Throwable th2;
                long j2 = 0;
                RandomAccessFile randomAccessFile = null;
                if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || strArr == null || strArr.length != 2) {
                    return false;
                }
                StringBuilder c = c();
                int indexOf;
                switch (i) {
                    case 1:
                        a = a(a(str), str);
                        c.append(a).append(File.separator);
                        if (str2.startsWith("-")) {
                            c.append(str2.substring(0, 4));
                        } else {
                            c.append(str2.substring(0, 3));
                        }
                        c.append(",");
                        indexOf = str2.indexOf(",") + 1;
                        if (str2.substring(indexOf, indexOf + 1).startsWith("-")) {
                            c.append(str2.substring(indexOf, indexOf + 4));
                        } else {
                            c.append(str2.substring(indexOf, indexOf + 3));
                        }
                        c.append(File.separator).append(str2);
                        break;
                    case 2:
                        a = "wifi";
                        c.append(a).append(File.separator);
                        c.append(str2.substring(0, 3)).append(",");
                        indexOf = str2.indexOf(",") + 1;
                        c.append(str2.substring(indexOf, indexOf + 3));
                        c.append(File.separator).append(str2);
                        break;
                    default:
                        return false;
                }
                strArr[1] = c.toString();
                c.delete(0, c.length());
                File file = new File(strArr[1]);
                if (file.exists() && file.isFile()) {
                    RandomAccessFile randomAccessFile2;
                    try {
                        randomAccessFile2 = new RandomAccessFile(file, "r");
                        try {
                            randomAccessFile2.seek(0);
                            j2 = randomAccessFile2.readLong();
                            if (randomAccessFile2 != null) {
                                try {
                                    randomAccessFile2.close();
                                } catch (Throwable th3) {
                                    e.a(th3, "Off", "getRequestParams part3");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            th3 = e;
                            try {
                                e.a(th3, "Off", "getRequestParams part1");
                                if (randomAccessFile2 != null) {
                                    try {
                                        randomAccessFile2.close();
                                    } catch (Throwable th32) {
                                        e.a(th32, "Off", "getRequestParams part3");
                                    }
                                }
                                j = j2;
                                jSONObject = new JSONObject();
                                jSONObject.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
                                jSONObject.put("geohash", str2);
                                jSONObject.put("t", String.valueOf(j));
                                jSONObject.put(NumberInfo.TYPE_KEY, a);
                                jSONObject.put("imei", e.b);
                                jSONObject.put("imsi", e.c);
                                jSONObject.put(NumberInfo.SOURCE_KEY, e.e);
                                jSONObject.put("license", e.f);
                                strArr[0] = jSONObject.toString();
                                return true;
                            } catch (Throwable th4) {
                                th2 = th4;
                                randomAccessFile = randomAccessFile2;
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (Throwable th322) {
                                        e.a(th322, "Off", "getRequestParams part3");
                                    }
                                }
                                throw th2;
                            }
                        } catch (Throwable th5) {
                            th322 = th5;
                            randomAccessFile = randomAccessFile2;
                            try {
                                e.a(th322, "Off", "getRequestParams part2");
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (Throwable th3222) {
                                        e.a(th3222, "Off", "getRequestParams part3");
                                    }
                                }
                                j = j2;
                                jSONObject = new JSONObject();
                                jSONObject.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
                                jSONObject.put("geohash", str2);
                                jSONObject.put("t", String.valueOf(j));
                                jSONObject.put(NumberInfo.TYPE_KEY, a);
                                jSONObject.put("imei", e.b);
                                jSONObject.put("imsi", e.c);
                                jSONObject.put(NumberInfo.SOURCE_KEY, e.e);
                                jSONObject.put("license", e.f);
                                strArr[0] = jSONObject.toString();
                                return true;
                            } catch (Throwable th6) {
                                th2 = th6;
                                if (randomAccessFile != null) {
                                    randomAccessFile.close();
                                }
                                throw th2;
                            }
                        }
                    } catch (FileNotFoundException e2) {
                        th3222 = e2;
                        randomAccessFile2 = null;
                        e.a(th3222, "Off", "getRequestParams part1");
                        if (randomAccessFile2 != null) {
                            randomAccessFile2.close();
                        }
                        j = j2;
                        jSONObject = new JSONObject();
                        jSONObject.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
                        jSONObject.put("geohash", str2);
                        jSONObject.put("t", String.valueOf(j));
                        jSONObject.put(NumberInfo.TYPE_KEY, a);
                        jSONObject.put("imei", e.b);
                        jSONObject.put("imsi", e.c);
                        jSONObject.put(NumberInfo.SOURCE_KEY, e.e);
                        jSONObject.put("license", e.f);
                        strArr[0] = jSONObject.toString();
                        return true;
                    } catch (Throwable th7) {
                        th3222 = th7;
                        e.a(th3222, "Off", "getRequestParams part2");
                        if (randomAccessFile != null) {
                            randomAccessFile.close();
                        }
                        j = j2;
                        jSONObject = new JSONObject();
                        jSONObject.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
                        jSONObject.put("geohash", str2);
                        jSONObject.put("t", String.valueOf(j));
                        jSONObject.put(NumberInfo.TYPE_KEY, a);
                        jSONObject.put("imei", e.b);
                        jSONObject.put("imsi", e.c);
                        jSONObject.put(NumberInfo.SOURCE_KEY, e.e);
                        jSONObject.put("license", e.f);
                        strArr[0] = jSONObject.toString();
                        return true;
                    }
                }
                j = j2;
                jSONObject = new JSONObject();
                try {
                    jSONObject.put("v", String.valueOf(ContentUtil.FONT_SIZE_NORMAL));
                    jSONObject.put("geohash", str2);
                    jSONObject.put("t", String.valueOf(j));
                    jSONObject.put(NumberInfo.TYPE_KEY, a);
                    jSONObject.put("imei", e.b);
                    jSONObject.put("imsi", e.c);
                    jSONObject.put(NumberInfo.SOURCE_KEY, e.e);
                    jSONObject.put("license", e.f);
                } catch (Throwable th22) {
                    e.a(th22, "Off", "getRequestParams part4");
                }
                strArr[0] = jSONObject.toString();
                return true;
            }

            private static double[] a(int i, ch chVar, String str, int i2) {
                String str2;
                int i3;
                Throwable th;
                double[] dArr = null;
                int[] iArr = null;
                if (i2 != 0) {
                    str2 = "wifi";
                } else {
                    String[] split = str.split("\\|");
                    iArr = new int[split.length];
                    for (i3 = 0; i3 < split.length; i3++) {
                        iArr[i3] = Integer.parseInt(split[i3]);
                    }
                    str2 = split.length != 2 ? "cdma" : "gsm";
                }
                try {
                    if ((chVar.g() <= ((long) i) ? 1 : null) == null) {
                        chVar.a((long) i);
                        i3 = a(i, chVar, str, iArr, i, ((int) chVar.g()) - 16, str2, 0);
                        if (i3 != -1) {
                            chVar.a((long) (i3 + 6));
                            double[] dArr2 = new double[3];
                            try {
                                dArr2[0] = ((double) a(chVar.e())) / 1000000.0d;
                                dArr2[1] = ((double) a(chVar.e())) / 1000000.0d;
                                dArr2[2] = (double) chVar.d();
                                return (cw.a(dArr2[1]) && cw.b(dArr2[0])) ? dArr2 : null;
                            } catch (Throwable th2) {
                                dArr = dArr2;
                                th = th2;
                                e.a(th, "Off", "binS");
                                return dArr;
                            }
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    e.a(th, "Off", "binS");
                    return dArr;
                }
            }

            public static String[] a(double d, double d2, String str) {
                String a;
                int i;
                String[] strArr = new String[50];
                if (TextUtils.isEmpty(str)) {
                    a = cb.a(d, d2);
                    str = cb.a(d, d2);
                } else {
                    a = str;
                }
                strArr[0] = a;
                strArr[25] = str;
                String[] a2 = cb.a(a);
                for (i = 1; i < 25; i++) {
                    strArr[i] = a2[i - 1];
                }
                a2 = cb.a(str);
                for (i = 26; i < 50; i++) {
                    strArr[i] = a2[i - 26];
                }
                return strArr;
            }

            private static void b(String str) {
                Throwable th;
                if (!f.containsKey(str) || TextUtils.isEmpty((CharSequence) f.get(str))) {
                    File file = new File(str);
                    if (file.exists() && file.isFile()) {
                        RandomAccessFile randomAccessFile;
                        try {
                            randomAccessFile = new RandomAccessFile(file, "r");
                            try {
                                randomAccessFile.seek(8);
                                int readUnsignedShort = randomAccessFile.readUnsignedShort();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < readUnsignedShort; i++) {
                                    int readInt = randomAccessFile.readInt();
                                    if (stringBuilder.indexOf("," + readInt) == -1) {
                                        stringBuilder.append(",").append(readInt);
                                    }
                                    if (i == readUnsignedShort - 1) {
                                        stringBuilder.append(",");
                                    }
                                }
                                f.put(str, stringBuilder.toString());
                                stringBuilder.delete(0, stringBuilder.length());
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (Throwable th2) {
                                        e.a(th2, "Off", "loadFcFea part3");
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                th2 = e;
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        } catch (FileNotFoundException e2) {
                            th2 = e2;
                            randomAccessFile = null;
                            try {
                                e.a(th2, "Off", "loadFcFea part1");
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (Throwable th22) {
                                        e.a(th22, "Off", "loadFcFea part3");
                                    }
                                }
                            } catch (Throwable th4) {
                                th22 = th4;
                                if (randomAccessFile != null) {
                                    try {
                                        randomAccessFile.close();
                                    } catch (Throwable th5) {
                                        e.a(th5, "Off", "loadFcFea part3");
                                    }
                                }
                                throw th22;
                            }
                        } catch (Throwable th6) {
                            th22 = th6;
                            randomAccessFile = null;
                            if (randomAccessFile != null) {
                                randomAccessFile.close();
                            }
                            throw th22;
                        }
                    }
                }
            }

            public static boolean b() {
                return !f.isEmpty();
            }

            private static StringBuilder c() {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cw.e());
                stringBuilder.append("offline").append(File.separator);
                stringBuilder.append(cw.j()).append(File.separator).append("s").append(File.separator);
                return stringBuilder;
            }
        }
