package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;

/* compiled from: Unknown */
public class os {
    public static final boolean EE = TMSDKContext.getStrFromEnvMap(TMSDKContext.USE_IP_LIST).equals("true");
    protected boolean EA = false;
    private final String EF = TMSDKContext.getStrFromEnvMap(TMSDKContext.TCP_SERVER_ADDRESS);
    private ArrayList<String> EG = new ArrayList();
    private ArrayList<String> EH = new ArrayList();
    private ArrayList<String> EI = new ArrayList();
    private volatile int EJ;
    private volatile long EK;
    private ArrayList<String> EL = new ArrayList();
    private ArrayList<String> EM = new ArrayList();
    private ArrayList<String> EN = new ArrayList();
    private ArrayList<String> EO = new ArrayList();
    private Object EP = new Object();
    private int EQ = 0;
    private ArrayList<String> ER = new ArrayList();
    private String ES = null;
    protected on Et;
    private final String TAG = "IpPlot";
    private Context mContext;
    private volatile int mHash;

    public os(Context context, boolean z, on onVar) {
        d.d("IpPlot", "IpPlot() isTest: " + z);
        this.mContext = context;
        this.EA = z;
        this.Et = onVar;
        fX();
        fR();
        fT();
    }

    private long Y() {
        long j;
        synchronized (this.EP) {
            j = this.EK;
        }
        return j;
    }

    private void a(String str, ArrayList<String> arrayList) {
        if (arrayList == null) {
            d.c("IpPlot", "printList() " + str + " is null");
        } else if (arrayList.size() > 0) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty((CharSequence) arrayList.get(i))) {
                    d.d("IpPlot", "printList() " + str + "[" + i + "]: " + ((String) arrayList.get(i)));
                }
            }
        } else {
            d.d("IpPlot", "printList() " + str + ".size <= 0");
        }
    }

    private void a(ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3) {
        if (arrayList != null) {
            d.e("IpPlot", "loadList() cmvips");
            this.EL = y(arrayList);
        }
        if (arrayList2 != null) {
            d.e("IpPlot", "loadList() univips");
            this.EM = y(arrayList2);
        }
        if (arrayList3 != null) {
            d.e("IpPlot", "loadList() ctvips");
            this.EN = y(arrayList3);
        }
    }

    private void a(boolean z, ArrayList<String> arrayList) {
        if (arrayList != null) {
            switch (gf()) {
                case 0:
                    arrayList.addAll(!z ? this.EL : this.EG);
                    break;
                case 1:
                    arrayList.addAll(!z ? this.EM : this.EH);
                    break;
                case 2:
                    arrayList.addAll(!z ? this.EN : this.EI);
                    break;
            }
        }
    }

    private void b(boolean z, ArrayList<String> arrayList) {
        if (arrayList != null) {
            int gf = gf();
            if (gf != 0) {
                arrayList.addAll(!z ? this.EL : this.EG);
            }
            if (1 != gf) {
                arrayList.addAll(!z ? this.EM : this.EH);
            }
            if (2 != gf) {
                arrayList.addAll(!z ? this.EN : this.EI);
            }
        }
    }

    private String cN(String str) {
        while (str.startsWith(" ")) {
            str = str.substring(1, str.length()).trim();
        }
        while (str.endsWith(" ")) {
            str = str.substring(0, str.length() - 1).trim();
        }
        return str;
    }

    private boolean cO(String str) {
        String cN = cN(str);
        if (cN.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String[] split = cN.split("\\.");
            return split.length >= 4 && Integer.parseInt(split[0]) < 255 && Integer.parseInt(split[1]) < 255 && Integer.parseInt(split[2]) < 255 && Integer.parseInt(split[3]) < 255;
        }
    }

    private void fR() {
        AtomicLong atomicLong = new AtomicLong();
        AtomicReference atomicReference = new AtomicReference();
        AtomicReference atomicReference2 = new AtomicReference();
        AtomicReference atomicReference3 = new AtomicReference();
        this.Et.a(atomicLong, atomicReference, atomicReference2, atomicReference3);
        synchronized (this.EP) {
            if ((atomicLong.get() <= 0 ? 1 : null) == null) {
                this.EK = atomicLong.get();
            }
            this.mHash = this.Et.ao();
            this.EJ = this.Et.ap();
            a((ArrayList) atomicReference.get(), (ArrayList) atomicReference2.get(), (ArrayList) atomicReference3.get());
        }
    }

    private boolean fS() {
        boolean z = true;
        long Y = Y();
        if (!(Y > 0)) {
            return false;
        }
        if (System.currentTimeMillis() <= Y) {
            z = false;
        }
        return z;
    }

    private void fU() {
        d.e("IpPlot", "printWrokingIpList()");
        synchronized (this.EP) {
            int size = this.ER.size();
            for (int i = 0; i < size; i++) {
                if (!TextUtils.isEmpty((CharSequence) this.ER.get(i))) {
                    d.d("IpPlot", "printWrokingIpList() mWorkingIpList[" + i + "]: " + ((String) this.ER.get(i)));
                }
            }
        }
    }

    private void fV() {
        if (this.EP != null) {
            synchronized (this.EP) {
                this.EQ = 0;
                this.ER.clear();
            }
            return;
        }
        this.EQ = 0;
    }

    private void fW() {
        synchronized (this.EP) {
            fV();
            a(true, this.ER);
            z(this.ER);
            b(true, this.ER);
            d.d("IpPlot", "resetToDefaultList()");
            fU();
            this.EK = 0;
            this.Et.a(0, new ArrayList(), new ArrayList(), new ArrayList());
        }
    }

    private void fX() {
        synchronized (this.EP) {
            if (this.EA) {
                this.EG.clear();
                this.EH.clear();
                this.EI.clear();
                this.EO.clear();
            } else {
                this.EG.clear();
                this.EH.clear();
                this.EI.clear();
                this.EO.clear();
            }
            this.EO.add(this.EF);
        }
    }

    private boolean gb() {
        boolean fS = fS();
        if (fS) {
            d.d("IpPlot", "checkIpListTimeOut() iplist timeout");
            fW();
        }
        return fS;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String gc() {
        d.d("IpPlot", "getIpInIpList()");
        synchronized (this.ER) {
            int size = this.ER.size();
            String str;
            if (size > 0) {
                if (this.EQ >= size) {
                    this.EQ = 0;
                }
                str = "";
                try {
                    str = (String) this.ER.get(this.EQ);
                    d.e("IpPlot", "getIpInIpList() mCurIpIdx: " + this.EQ + " ip: " + str);
                    if (TextUtils.isEmpty(str)) {
                        str = getDomain();
                        return str;
                    } else if (!(cO(str) || str.equals(this.EF))) {
                        str = getDomain();
                        return str;
                    }
                } catch (Throwable th) {
                    d.c("IpPlot", th);
                    return getDomain();
                }
            }
            str = getDomain();
            return str;
        }
    }

    private String getDomain() {
        return this.EF;
    }

    private int gf() {
        int J;
        Object obj = null;
        if (4 == ml.AX) {
            obj = 1;
        }
        if (obj == null) {
            J = h.J(this.mContext);
            if (-1 == J) {
                d.e("IpPlot", "getOper() unknowOper");
                J = 2;
            }
        } else {
            d.e("IpPlot", "getOper() usingWifi");
            J = 2;
        }
        d.e("IpPlot", "getOper() oper:" + J);
        return J;
    }

    private final ArrayList<String> y(ArrayList<String> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            return new ArrayList();
        }
        for (int size = arrayList.size() - 1; size > 0; size--) {
            int random = (int) (((double) (size + 1)) * Math.random());
            String str = (String) arrayList.get(size);
            arrayList.set(size, arrayList.get(random));
            arrayList.set(random, str);
        }
        return arrayList;
    }

    private void z(ArrayList<String> arrayList) {
        if (arrayList != null) {
            arrayList.addAll(this.EO);
        }
    }

    public int W() {
        return this.mHash;
    }

    public int X() {
        return this.EJ;
    }

    public void a(int i, int i2, int i3, ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3) {
        d.d("IpPlot", "handleNewIpList() hash: " + i + " hashSeqNo: " + i2 + " validperiod: " + i3);
        a("cmvips", (ArrayList) arrayList);
        a("unvips", (ArrayList) arrayList2);
        a("ctvips", (ArrayList) arrayList3);
        synchronized (this.EP) {
            t(i, i2);
            a(arrayList, arrayList2, arrayList3);
            this.EK = System.currentTimeMillis() + ((long) (i3 * 1000));
            this.Et.a(this.EK, (ArrayList) arrayList, (ArrayList) arrayList2, (ArrayList) arrayList3);
        }
        fT();
    }

    public boolean ae() {
        return this.EA;
    }

    public void cM(String str) {
        synchronized (this.EP) {
            this.ES = str;
        }
    }

    protected void fT() {
        d.e("IpPlot", "refreshWorkingList()");
        synchronized (this.EP) {
            if (this.ER == null) {
            } else if (gb()) {
            } else {
                fV();
                a(false, this.ER);
                z(this.ER);
                b(false, this.ER);
                fU();
                if (this.ER.size() <= this.EO.size()) {
                    d.e("IpPlot", "refreshWorkingList() only domain");
                    fW();
                    d.d("IpPlot", "refreshWorkingList() resetToDefaultList()");
                    fU();
                }
                Iterator it = this.ER.iterator();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    if (!(str == null || cO(str) || str.equals(this.EF))) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void fY() {
        d.d("IpPlot", "handleNetworkChange()");
        fT();
    }

    public String fZ() {
        return "http://" + ga();
    }

    public String ga() {
        if (EE) {
            gb();
            String gc = gc();
            d.d("IpPlot", "getIp() ip: " + gc);
            return gc;
        }
        gc = getDomain();
        d.d("IpPlot", "getIp() domain: " + gc);
        return gc;
    }

    public int gd() {
        if (this.ER == null) {
            return 0;
        }
        synchronized (this.ER) {
            if (this.ER == null) {
                return 0;
            }
            int size = this.ER.size();
            return size;
        }
    }

    public boolean ge() {
        boolean z = false;
        d.e("IpPlot", "gotoNextIp()");
        synchronized (this.ER) {
            this.EQ++;
            if (this.EQ >= this.ER.size()) {
                this.EQ = 0;
                z = true;
            }
            d.d("IpPlot", "gotoNextIp() size: " + this.ER.size() + " mCurIpIdx: " + this.EQ);
        }
        return z;
    }

    public void n(boolean z) {
        d.d("IpPlot", "setIsTest() isTest: " + z);
        this.EA = z;
        fX();
        fT();
    }

    public void t(int i, int i2) {
        synchronized (this.EP) {
            this.mHash = i;
            this.EJ = i2;
            this.Et.b(i, i2);
        }
    }
}
