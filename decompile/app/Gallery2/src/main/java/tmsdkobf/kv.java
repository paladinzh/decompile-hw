package tmsdkobf;

/* compiled from: Unknown */
public class kv {
    private static Object lock = new Object();
    private static kv wm;
    private lf nq = ((ln) fe.ad(9)).getPreferenceService("kv_profile_sp_name");
    private Boolean wn = null;

    private kv() {
    }

    public static kv dr() {
        if (wm == null) {
            synchronized (lock) {
                if (wm == null) {
                    wm = new kv();
                }
            }
        }
        return wm;
    }

    public void bd(int i) {
        this.nq.e("kv_profile_full_quantity_", ds() + i);
    }

    public int ds() {
        return this.nq.getInt("kv_profile_full_quantity_", 1000);
    }

    public boolean dt() {
        if (this.wn == null) {
            this.wn = Boolean.valueOf(this.nq.getBoolean("kv_profile_all_report", true));
        }
        return this.wn.booleanValue();
    }

    public void x(boolean z) {
        this.wn = Boolean.valueOf(z);
        this.nq.d("kv_profile_all_report", z);
    }
}
