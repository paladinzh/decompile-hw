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

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void b(boolean z, String str) {
        this.mD = z;
        this.mE = str;
    }

    public void c(boolean z) {
        this.mF = z;
    }

    public void d(boolean z) {
        this.mG = z;
    }

    public void e(boolean z) {
        this.mH = z;
    }

    public void f(boolean z) {
        this.mI = z;
    }

    public void g(boolean z) {
        this.mJ = z;
    }

    public void h(boolean z) {
        this.mK = z;
    }

    public void i(boolean z) {
        this.mL = z;
    }

    public void j(boolean z) {
        this.mM = z;
    }

    public void af(String str) {
        this.mN = str;
    }

    public void ag(String str) {
        this.mO = str;
    }

    public void a(int... iArr) {
        if (iArr != null) {
            if (this.mP == null) {
                this.mP = new SparseArray();
            }
            for (int put : iArr) {
                this.mP.put(put, Integer.valueOf(0));
            }
        }
    }

    public void k(boolean z) {
        this.mQ = z;
    }

    public void l(boolean z) {
        this.mR = z;
    }

    public void m(boolean z) {
        this.mS = z;
    }

    public void a(Intent intent) {
        this.mT = intent;
    }
}
