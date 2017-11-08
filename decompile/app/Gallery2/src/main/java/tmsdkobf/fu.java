package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

public class fu {
    private static fu mC = null;
    private Context mContext;
    private boolean mD = true;
    private String mE = "TULog";
    private boolean mF = false;
    private boolean mG = false;
    private boolean mH = false;
    private boolean mI = true;
    private boolean mJ = true;
    private boolean mK = false;
    private boolean mL = false;
    private boolean mM = false;
    private String mN = "xxx.pService";
    private String mO = "_xxx";
    private SparseArray<Integer> mP = null;
    private boolean mQ = false;
    private boolean mR = false;
    private boolean mS = false;
    private Intent mT = null;

    public static synchronized fu u() {
        fu fuVar;
        synchronized (fu.class) {
            if (mC == null) {
                mC = new fu();
            }
            fuVar = mC;
        }
        return fuVar;
    }

    private fu() {
    }

    public void setContext(Context ctx) {
        this.mContext = ctx;
    }

    public void b(boolean is, String tag) {
        this.mD = is;
        this.mE = tag;
    }

    public void c(boolean is) {
        this.mF = is;
    }

    public void d(boolean isOperatorVersion) {
        this.mG = isOperatorVersion;
    }

    public void e(boolean isInclude) {
        this.mH = isInclude;
    }

    public void f(boolean isSupport) {
        this.mI = isSupport;
    }

    public void g(boolean isSupport) {
        this.mJ = isSupport;
    }

    public void h(boolean isSupport) {
        this.mK = isSupport;
    }

    public void i(boolean isSupport) {
        this.mL = isSupport;
    }

    public void j(boolean isSupport) {
        this.mM = isSupport;
    }

    public void af(String servicePreaffix) {
        this.mN = servicePreaffix;
    }

    public void ag(String postffix) {
        this.mO = postffix;
    }

    public void a(int... rids) {
        if (rids != null) {
            if (this.mP == null) {
                this.mP = new SparseArray();
            }
            for (int rid : rids) {
                this.mP.put(rid, Integer.valueOf(0));
            }
        }
    }

    public void k(boolean is) {
        this.mQ = is;
    }

    public void l(boolean has) {
        this.mR = has;
    }

    public void m(boolean isSupport) {
        this.mS = isSupport;
    }

    public void a(Intent userData) {
        this.mT = userData;
    }
}
