package tmsdkobf;

/* compiled from: Unknown */
class la {
    private static Object lock = new Object();
    private static la wu;
    private lf nq = ((ln) fe.ad(9)).getPreferenceService("soft_list_sp_name");

    private la() {
    }

    public static la dA() {
        if (wu == null) {
            synchronized (lock) {
                if (wu == null) {
                    wu = new la();
                }
            }
        }
        return wu;
    }

    public void aZ(int i) {
        this.nq.e("soft_list_profile_full_quantity_", i);
    }

    public boolean dB() {
        return this.nq.getBoolean("soft_list_profile_full_upload_", false);
    }

    public int ds() {
        return this.nq.getInt("soft_list_profile_full_quantity_", 0);
    }

    public void y(boolean z) {
        this.nq.d("soft_list_profile_full_upload_", z);
    }
}
