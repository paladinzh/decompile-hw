package tmsdkobf;

/* compiled from: Unknown */
public class nz {
    private static nz DR = null;
    private nc yq = new nc("Optimus");

    private nz() {
    }

    public static nz fD() {
        if (DR == null) {
            synchronized (nz.class) {
                if (DR == null) {
                    DR = new nz();
                }
            }
        }
        return DR;
    }

    public static void stop() {
        synchronized (nz.class) {
            DR = null;
        }
    }

    public long fE() {
        return this.yq.getLong("optimus_fake_station_time", 0);
    }

    public void r(long j) {
        this.yq.a("optimus_fake_sms_time", j, true);
    }

    public void s(long j) {
        this.yq.a("optimus_fake_station_time", j, true);
    }
}
