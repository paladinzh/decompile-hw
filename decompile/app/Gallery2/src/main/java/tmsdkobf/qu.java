package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
final class qu extends BaseManagerC {
    public static String TAG = "WupSessionManagerImpl";
    private qr KD;
    private Context mContext;

    qu() {
    }

    private cr v(Context context) {
        cr crVar = new cr();
        crVar.b(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL)));
        crVar.e(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT));
        crVar.f(0);
        py b = TMServiceFactory.getSystemInfoService().b(context.getPackageName(), 1);
        if (b != null && b.hA()) {
            crVar.f(1);
        }
        return crVar;
    }

    public int a(String str, AtomicReference<et> atomicReference) {
        qs cw = qp.cw(17);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("checkrequest", new es(str, null, 0, 2));
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cw.KA, "checkresponse", new et());
        if (a2 != null) {
            atomicReference.set((et) a2);
        }
        return 0;
    }

    public int a(List<eg> list, ArrayList<cp> arrayList, boolean z, int i) {
        qs cw = qp.cw(6);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.cx(i));
        hashMap.put("vecSoftFeature", list);
        cw.Ky = hashMap;
        cw.KB = z;
        int a = this.KD.a(cw);
        if (a != 0) {
            return a;
        }
        int i2;
        Object arrayList2 = new ArrayList();
        arrayList2.add(new cp());
        try {
            Object a2 = this.KD.a(cw.KA, "vecAnalyseInfo", arrayList2);
            if (a2 != null) {
                arrayList.clear();
                arrayList.addAll((Collection) a2);
            }
            i2 = 0;
        } catch (Throwable th) {
            i2 = ErrorCode.ERR_WUP;
        }
        return i2;
    }

    public int a(List<String> list, AtomicReference<du> atomicReference) {
        qs cw = qp.cw(18);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        int size = list.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(new es((String) list.get(i), null, 0, 2));
        }
        hashMap.put("reqtemp", new ds(arrayList));
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cw.KA, "rsptemp", new du());
        if (a2 != null) {
            atomicReference.set((du) a2);
        }
        return 0;
    }

    public int a(dj djVar, di diVar) {
        qs cw = qp.cw(20);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("licinfo", djVar);
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cw.KA, "outinfo", new dt());
        if (a2 == null) {
            return -2;
        }
        diVar.ir = (dt) a2;
        a2 = this.KD.a(cw.KA, "", Integer.valueOf(0));
        return a2 != null ? ((Integer) a2).intValue() : -2;
    }

    public int a(dr drVar) {
        qs cw = qp.cw(0);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("softreportinfo", drVar);
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        return a == 0 ? 0 : a;
    }

    public int a(ew ewVar, AtomicReference<ez> atomicReference, ArrayList<ey> arrayList, int i) {
        qs cw = qp.cw(2);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.cx(i));
        hashMap.put("clientinfo", ewVar);
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cw.KA, "serverinfo", new ez());
        if (a2 != null) {
            atomicReference.set((ez) a2);
        }
        a2 = new ArrayList();
        a2.add(new ey());
        a2 = this.KD.a(cw.KA, "virusinfos", a2);
        if (a2 != null) {
            arrayList.clear();
            arrayList.addAll((Collection) a2);
        }
        return 0;
    }

    public int b(da daVar, AtomicReference<dh> atomicReference) {
        qs cw = qp.cw(9);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("deviceinfo", daVar);
        cw.Ky = hashMap;
        int a = this.KD.a(cw, true);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cw.KA, "guidinfo", new dh());
        if (a2 != null) {
            atomicReference.set((dh) a2);
        }
        return 0;
    }

    public int getSingletonType() {
        return 1;
    }

    public int ib() {
        this.KD.Kr = true;
        try {
            qs cw = qp.cw(1);
            HashMap hashMap = new HashMap(3);
            hashMap.put("phonetype", this.KD.hS());
            hashMap.put("userinfo", this.KD.hU());
            hashMap.put("channelinfo", v(this.mContext));
            cw.Ky = hashMap;
            int a = this.KD.a(cw);
            if (a == 0) {
                return a;
            }
            this.KD.Kr = false;
            return a;
        } finally {
            this.KD.Kr = false;
        }
    }

    public qo ic() {
        return this.KD;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.KD = new qr(this.mContext);
    }

    public int x(List<ed> list) {
        qs cw = qp.cw(12);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hV());
        hashMap.put("vecSmsReport", list);
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        return a == 0 ? 0 : a;
    }

    public int y(List<em> list) {
        qs cw = qp.cw(13);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hV());
        hashMap.put("vecTelReport", list);
        cw.Ky = hashMap;
        int a = this.KD.a(cw);
        return a == 0 ? 0 : a;
    }
}
