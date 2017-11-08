package tmsdkobf;

import android.os.MemoryFile;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class jq {
    private static List<WeakReference<a>> uf = new LinkedList();
    private static jr ug;
    public static volatile qz uh = null;
    private static MemoryFile ui;
    private static volatile boolean uj = false;
    public static int uk = 0;
    private static boolean ul = true;
    private static boolean um = true;
    private static pf un = null;
    public static IDualPhoneInfoFetcher uo = null;

    /* compiled from: Unknown */
    public interface a {
    }

    public static synchronized void a(a aVar) {
        synchronized (jq.class) {
            k(uf);
            uf.add(new WeakReference(aVar));
        }
    }

    public static void a(pf pfVar) {
        un = pfVar;
    }

    private static long bI() {
        int i = 2;
        if (1 == cw()) {
            i = 1;
        } else if (2 != cw()) {
            i = 3;
        }
        return jk.getIdent(i, 4294967296L);
    }

    public static boolean cq() {
        return ul || fw.w().H().booleanValue();
    }

    public static boolean cr() {
        try {
            byte[] bytes = TMSDKContext.class.getName().getBytes("utf-8");
            byte[] bArr = new byte[]{(byte) ((byte) bytes.length)};
            ui = new MemoryFile("tmsdk2-jni-context", 512);
            ui.writeBytes(bArr, 0, 0, 1);
            ui.writeBytes(bytes, 0, 1, bytes.length);
            cs();
            return uj;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void cs() {
        if (!uj) {
            uj = mz.e(TMSDKContext.getApplicaionContext(), TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SDK_LIBNAME));
            d.g("demo", "mIsSdkLibraryLoaded =" + uj);
        }
    }

    public static jr ct() {
        if (ug == null) {
            synchronized (jq.class) {
                if (ug == null) {
                    ug = new jr(bI(), "com.tmsdk.common");
                }
            }
        }
        return ug;
    }

    public static pf cu() {
        return un;
    }

    public static qz cv() {
        return uh;
    }

    public static int cw() {
        return uk;
    }

    public static IDualPhoneInfoFetcher cx() {
        return uo;
    }

    public static boolean getTmsliteSwitch() {
        return fw.w().G().booleanValue();
    }

    private static <T> void k(List<WeakReference<T>> list) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            if (((WeakReference) it.next()).get() == null) {
                it.remove();
            }
        }
    }

    public static void setAutoConnectionSwitch(boolean z) {
        ul = z;
    }

    public static void setDualPhoneInfoFetcher(IDualPhoneInfoFetcher iDualPhoneInfoFetcher) {
        d.g("SdkContextInternal", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        d.g("TrafficCorrection", "setDualPhoneInfoFetcher:[" + iDualPhoneInfoFetcher + "]");
        uo = iDualPhoneInfoFetcher;
    }
}
