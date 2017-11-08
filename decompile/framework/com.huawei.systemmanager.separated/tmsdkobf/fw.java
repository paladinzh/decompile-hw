package tmsdkobf;

import android.content.Context;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class fw {
    private static fw ng = null;
    private final String nh = "ConfigInfo";
    private final String ni = "check_imsi";
    private final String nj = "rqd";
    private final String nk = "sk";
    private final String nl = "first_run_time";
    private final String nm = "app_code_version";
    private final String nn = "app_code_old_version";
    private final String no = "report_usage_info_time";
    private ln np = ((ln) fe.ad(9));

    private fw(Context context) {
    }

    public static fw w() {
        if (ng == null) {
            synchronized (fw.class) {
                if (ng == null) {
                    ng = new fw(TMSDKContext.getApplicaionContext());
                }
            }
        }
        return ng;
    }

    public long A() {
        return x().getLong("ad", 0);
    }

    public long B() {
        return x().getLong("sl", 0);
    }

    public Boolean C() {
        return Boolean.valueOf(x().getBoolean("a_s", true));
    }

    public Boolean D() {
        return Boolean.valueOf(x().getBoolean("sr_s", true));
    }

    public Boolean E() {
        return Boolean.valueOf(x().getBoolean("opt_s", true));
    }

    public Boolean F() {
        return Boolean.valueOf(x().getBoolean("ps_s", true));
    }

    public Boolean G() {
        return Boolean.valueOf(x().getBoolean("tmslite_switch", false));
    }

    public Boolean H() {
        return Boolean.valueOf(x().getBoolean("ac_swi", false));
    }

    public Boolean I() {
        return Boolean.valueOf(x().getBoolean("per_s", false));
    }

    public Boolean J() {
        return Boolean.valueOf(x().getBoolean("per_other_s", false));
    }

    public Boolean K() {
        return Boolean.valueOf(x().getBoolean("ht_swi", false));
    }

    public Boolean L() {
        return Boolean.valueOf(x().getBoolean("virus_update", false));
    }

    public long M() {
        return x().getLong("st_lastime", 0);
    }

    public long N() {
        return x().getLong("st_vaildtime", 0);
    }

    public void a(Boolean bool) {
        x().d("a_s", bool.booleanValue());
    }

    public void ah(int i) {
        x().e("ae", i);
    }

    public void ai(int i) {
        x().e("st_count", i);
    }

    public void b(long j) {
        x().d("ad", j);
    }

    public void b(Boolean bool) {
        x().d("sr_s", bool.booleanValue());
    }

    public void c(long j) {
        x().d("sl", j);
    }

    public void c(Boolean bool) {
        x().d("wifi_s", bool.booleanValue());
    }

    public void d(long j) {
        x().d("st_lastime", j);
    }

    public void d(Boolean bool) {
        x().d("opt_s", bool.booleanValue());
    }

    public void e(long j) {
        x().d("st_vaildtime", j);
    }

    public void e(Boolean bool) {
        x().d("ps_s", bool.booleanValue());
    }

    public void f(Boolean bool) {
        x().d("tmslite_switch", bool.booleanValue());
    }

    public void g(Boolean bool) {
        x().d("per_s", bool.booleanValue());
    }

    public int getStartCount() {
        return x().getInt("st_count", 0);
    }

    public void h(Boolean bool) {
        x().d("per_other_s", bool.booleanValue());
    }

    public void i(Boolean bool) {
        x().d("ac_swi", bool.booleanValue());
    }

    public void j(Boolean bool) {
        x().d("ht_swi", bool.booleanValue());
    }

    public void k(Boolean bool) {
        x().d("virus_update", bool.booleanValue());
    }

    public lf x() {
        return this.np.getPreferenceService("ConfigInfo");
    }

    public lf y() {
        return this.np.getPreferenceService("sk");
    }

    public int z() {
        return x().getInt("ae", -1);
    }
}
