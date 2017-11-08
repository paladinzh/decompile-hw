package tmsdkobf;

import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class pu {
    public static on IE;
    private static pu II;
    public int AR = 0;
    public long IF = 0;
    public String IG = "";
    public boolean IH = false;
    public String ja = "";
    public int port = 0;
    public int v = 0;

    public static void a(pu puVar) {
        II = puVar;
    }

    public static pu hm() {
        if (II == null) {
            II = new pu();
        }
        return II;
    }

    public static void release() {
        a(null);
    }

    public void hn() {
        if (IE != null) {
            d.d("TcpConnectInfo", this.ja + "|" + String.valueOf(this.port) + "|" + String.valueOf(this.v) + "|" + String.valueOf(this.IF) + "|" + String.valueOf(this.AR) + "|" + this.IG + "|" + String.valueOf(this.IH));
            IE.b(this.ja, String.valueOf(this.port), String.valueOf(this.v), String.valueOf(this.IF), String.valueOf(this.AR), this.IG, String.valueOf(this.IH));
        }
    }
}
