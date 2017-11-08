package tmsdkobf;

/* compiled from: Unknown */
public class ht {
    private static Object lock = new Object();
    private static ht qZ;
    private lf nq = ((ln) fe.ad(9)).getPreferenceService("prfle_cnfg_dao");

    private ht() {
    }

    private String aN(int i) {
        return "profile_quantity_" + i;
    }

    private String aO(int i) {
        return "profile_last_enqueue_key_" + i;
    }

    public static ht bD() {
        if (qZ == null) {
            synchronized (lock) {
                if (qZ == null) {
                    qZ = new ht();
                }
            }
        }
        return qZ;
    }

    private ak bj(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        fq fqVar = new fq(mo.cw(str));
        fqVar.ae("UTF-8");
        return (ak) fqVar.a(new ak(), 0, false);
    }

    private String c(ak akVar) {
        if (akVar == null) {
            return "";
        }
        fr frVar = new fr();
        frVar.ae("UTF-8");
        frVar.a((fs) akVar, 0);
        return mo.bytesToHexString(frVar.toByteArray());
    }

    public void a(ak akVar) {
        if (akVar != null) {
            this.nq.m(aO(akVar.bf), c(akVar));
        }
    }

    public boolean aJ(int i) {
        return this.nq.getBoolean("prf_upl_exception_" + i, false);
    }

    public ak aK(int i) {
        String string = this.nq.getString(aO(i), null);
        return string != null ? bj(string) : null;
    }

    public int aL(int i) {
        return this.nq.getInt(aN(i), 0);
    }

    public void aM(int i) {
        this.nq.e(aN(i), 0);
    }

    public void b(int i, boolean z) {
        this.nq.d("prf_upl_exception_" + i, z);
    }

    public boolean b(ak akVar) {
        return hu.a(aK(akVar.bf), akVar);
    }

    public int bE() {
        return this.nq.getInt("profile_task_id", 0);
    }

    public void bF() {
        int bE = bE();
        if (bE < 0) {
            bE = 0;
        }
        this.nq.e("profile_task_id", bE + 1);
    }

    public void e(int i, int i2) {
        this.nq.e(aN(i), aL(i) + i2);
    }

    public void f(int i, int i2) {
        this.nq.e(aN(i), aL(i) - i2);
    }

    public void u(boolean z) {
        this.nq.d("profile_soft_list_upload_opened", z);
    }
}
