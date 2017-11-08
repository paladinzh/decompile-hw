package tmsdkobf;

import android.content.Context;
import android.content.pm.PackageInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
final class jv {
    private static final HashMap<String, String> uy = new HashMap();
    private static final long uz = (new GregorianCalendar(2040, 0, 1).getTimeInMillis() / 1000);
    private Context mContext;
    private Properties uA;

    static {
        uy.put("AresEngineManager", "aresengine");
        uy.put("QScannerManager", "qscanner");
        uy.put("LocationManager", "phoneservice");
        uy.put("IpDialManager", "phoneservice");
        uy.put("UsefulNumberManager", "phoneservice");
        uy.put("NetworkManager", "network");
        uy.put("TrafficCorrectionManager", "network");
        uy.put("FirewallManager", "network");
        uy.put("NetSettingManager", "netsetting");
        uy.put("OptimizeManager", "optimize");
        uy.put("UpdateManager", "update");
        uy.put("UrlCheckManager", "urlcheck");
        uy.put("PermissionManager", "permission");
        uy.put("SoftwareManager", "software");
        uy.put("AntitheftManager", "antitheft");
        uy.put("PowerSavingManager", "powersaving");
    }

    jv(Properties properties, Context context) {
        this.uA = properties;
        this.mContext = context;
    }

    private String bG(String str) {
        PackageInfo packageInfo;
        String str2 = null;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 64);
        } catch (Exception e) {
            e.printStackTrace();
            packageInfo = null;
        }
        if (packageInfo != null) {
            InputStream byteArrayInputStream = new ByteArrayInputStream(packageInfo.signatures[0].toByteArray());
            try {
                str2 = nb.p(((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(byteArrayInputStream)).getEncoded());
                byteArrayInputStream.close();
            } catch (CertificateException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
        return str2;
    }

    int a(di diVar) {
        return ((qt) ManagerCreatorC.getManager(qt.class)).a(new dj(bG(this.mContext.getPackageName()), l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL))), diVar);
    }

    public boolean cD() {
        String packageName = this.mContext.getPackageName();
        String bG = bG(packageName);
        if (bG == null) {
            return true;
        }
        boolean equals = bG.equals(this.uA.getProperty("signature").toUpperCase().trim());
        if (equals) {
            new nc("tms").a("reportsig", packageName + ":" + bG, true);
        }
        return equals;
    }

    public String cE() {
        return this.uA.getProperty("lc_sdk_channel");
    }

    public String cF() {
        return this.uA.getProperty("lc_sdk_pid");
    }

    public long cG() {
        return Long.parseLong(this.uA.getProperty("expiry.seconds", Long.toString(uz)));
    }
}
