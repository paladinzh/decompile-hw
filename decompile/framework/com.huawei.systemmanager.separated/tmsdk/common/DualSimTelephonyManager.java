package tmsdk.common;

import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.d;
import tmsdkobf.jq;
import tmsdkobf.nh;
import tmsdkobf.qz;

/* compiled from: Unknown */
public class DualSimTelephonyManager implements tmsdkobf.jq.a {
    private static DualSimTelephonyManager zN;
    private static final String[] zO = new String[]{"phone1", "phone2", "phoneEX"};
    private ArrayList<a> zP = new ArrayList(2);

    /* compiled from: Unknown */
    static class a {
        public WeakReference<PhoneStateListener> zQ;
        public int zR;
        public boolean zS;
        public TelephonyManager zT;

        public a(PhoneStateListener phoneStateListener, int i, boolean z, TelephonyManager telephonyManager) {
            this.zQ = new WeakReference(phoneStateListener);
            this.zR = i;
            this.zS = z;
            this.zT = telephonyManager;
        }
    }

    private DualSimTelephonyManager() {
        jq.a((tmsdkobf.jq.a) this);
    }

    private a a(PhoneStateListener phoneStateListener, int i, int i2) {
        TelephonyManager telephonyManager;
        switch (i2) {
            case -1:
            case 0:
                telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
                break;
            case 1:
                qz qzVar = jq.uh;
                if (qzVar == null || !qzVar.il()) {
                    telephonyManager = getSecondTelephonyManager();
                    break;
                }
                telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
                break;
                break;
            default:
                return null;
        }
        TelephonyManager telephonyManager2 = telephonyManager;
        if (telephonyManager2 != null) {
            try {
                telephonyManager2.listen(phoneStateListener, i);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return new a(phoneStateListener, i, i2 == 1, telephonyManager2);
    }

    public static ITelephony getDefaultTelephony() {
        ITelephony iTelephony;
        qz qzVar = jq.uh;
        if (qzVar == null) {
            iTelephony = null;
        } else {
            ITelephony cz = qzVar.cz(0);
            if (cz != null) {
                return cz;
            }
            iTelephony = cz;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService("phone");
            if (telephonyManager == null) {
                return null;
            }
            Method declaredMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
            if (declaredMethod == null) {
                return null;
            }
            declaredMethod.setAccessible(true);
            iTelephony = (ITelephony) declaredMethod.invoke(telephonyManager, (Object[]) null);
            return iTelephony;
        } catch (Throwable e) {
            d.a("DualSimTelephonyManager", "getDefaultTelephony", e);
        } catch (Throwable e2) {
            d.a("DualSimTelephonyManager", "getDefaultTelephony", e2);
        } catch (Throwable e22) {
            d.a("DualSimTelephonyManager", "getDefaultTelephony", e22);
        } catch (Throwable e222) {
            d.a("DualSimTelephonyManager", "getDefaultTelephony", e222);
        } catch (Throwable e2222) {
            d.a("DualSimTelephonyManager", "getDefaultTelephony", e2222);
        }
    }

    public static synchronized DualSimTelephonyManager getInstance() {
        DualSimTelephonyManager dualSimTelephonyManager;
        synchronized (DualSimTelephonyManager.class) {
            if (zN == null) {
                zN = new DualSimTelephonyManager();
            }
            dualSimTelephonyManager = zN;
        }
        return dualSimTelephonyManager;
    }

    public static ITelephony getSecondTelephony() {
        qz qzVar = jq.uh;
        if (qzVar != null) {
            ITelephony cz = qzVar.cz(1);
            if (cz != null) {
                return cz;
            }
        }
        for (String str : zO) {
            if (nh.checkService(str) != null) {
                IBinder service = nh.getService(str);
                if (service != null) {
                    return Stub.asInterface(service);
                }
            }
        }
        return null;
    }

    public TelephonyManager getSecondTelephonyManager() {
        qz qzVar = jq.uh;
        if (qzVar != null) {
            String ih = qzVar.ih();
            if (!(ih == null || nh.checkService(ih) == null)) {
                return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(ih);
            }
        }
        try {
            for (String str : zO) {
                if (nh.checkService(str) != null) {
                    return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(str);
                }
            }
        } catch (Exception e) {
            d.d("DualSimTelephonyManager", e);
        }
        try {
            for (String str2 : zO) {
                if (nh.checkService(str2) != null) {
                    return (TelephonyManager) TMSDKContext.getApplicaionContext().getSystemService(str2);
                }
            }
        } catch (Exception e2) {
            d.c("DualSimTelephonyManager", e2);
        }
        return null;
    }

    public void handleSdkContextEvent(int i) {
        if (i == 1) {
            reListenPhoneState();
        }
    }

    public boolean listenPhonesState(int i, PhoneStateListener phoneStateListener, int i2) {
        a aVar = null;
        a a = a(phoneStateListener, i2, i);
        if (a == null) {
            return false;
        }
        Iterator it = this.zP.iterator();
        boolean z = false;
        while (!z && it.hasNext()) {
            aVar = (a) it.next();
            if (aVar.zS == (i == 1) && aVar.zQ.get() == phoneStateListener) {
                z = true;
            }
        }
        if (z) {
            if (i2 != 0) {
                aVar.zR = Integer.valueOf(i2).intValue();
            } else {
                it.remove();
            }
        } else if (i2 != 0) {
            this.zP.add(a);
        }
        return true;
    }

    public void reListenPhoneState() {
        Iterator it = this.zP.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            PhoneStateListener phoneStateListener = (PhoneStateListener) aVar.zQ.get();
            if (phoneStateListener != null) {
                if (aVar.zT != null) {
                    aVar.zT.listen(phoneStateListener, 0);
                }
                a a = a(phoneStateListener, aVar.zR, !aVar.zS ? 1 : 0);
                if (a != null) {
                    aVar.zT = a.zT;
                }
            }
        }
    }
}
