package com.loc;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import java.util.List;
import org.json.JSONObject;

/* compiled from: WifiManagerWrapper */
public class cf {
    private WifiManager a;
    private JSONObject b;
    private Context c;

    public cf(Context context, WifiManager wifiManager, JSONObject jSONObject) {
        this.a = wifiManager;
        this.b = jSONObject;
        this.c = context;
    }

    private boolean a(WifiInfo wifiInfo) {
        return (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getBSSID()) || wifiInfo.getSSID() == null || wifiInfo.getBSSID().equals("00:00:00:00:00:00") || wifiInfo.getBSSID().contains(" :") || TextUtils.isEmpty(wifiInfo.getSSID())) ? false : true;
    }

    public List<ScanResult> a() {
        try {
            return this.a == null ? null : this.a.getScanResults();
        } catch (Throwable th) {
            e.a(th, "WifiManagerWrapper", "getScanResults");
            return null;
        }
    }

    public void a(JSONObject jSONObject) {
        this.b = jSONObject;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(boolean z) {
        Context context = this.c;
        if (this.a != null && context != null && z && cw.c() > 17) {
            String str = "autoenablewifialwaysscan";
            if (cw.a(this.b, str)) {
                try {
                    if ("0".equals(this.b.getString(str))) {
                        return;
                    }
                } catch (Throwable th) {
                    e.a(th, "WifiManagerWrapper", "enableWifiAlwaysScan1");
                }
            }
            ContentResolver contentResolver = context.getContentResolver();
            String str2 = "android.provider.Settings$Global";
            try {
                if (((Integer) cu.a(str2, "getInt", new Object[]{contentResolver, "wifi_scan_always_enabled"}, new Class[]{ContentResolver.class, String.class})).intValue() == 0) {
                    cu.a(str2, "putInt", new Object[]{contentResolver, "wifi_scan_always_enabled", Integer.valueOf(1)}, new Class[]{ContentResolver.class, String.class, Integer.TYPE});
                }
            } catch (Throwable th2) {
                e.a(th2, "WifiManagerWrapper", "enableWifiAlwaysScan");
            }
        }
    }

    public boolean a(ConnectivityManager connectivityManager) {
        boolean z = false;
        WifiManager wifiManager = this.a;
        if (wifiManager == null) {
            return false;
        }
        if (f()) {
            try {
                if (co.a(connectivityManager.getActiveNetworkInfo()) == 1 && a(wifiManager.getConnectionInfo())) {
                    z = true;
                }
            } catch (Throwable th) {
                e.a(th, "WifiManagerWrapper", "wifiAccess");
            }
        }
        return z;
    }

    public WifiInfo b() {
        return this.a == null ? null : this.a.getConnectionInfo();
    }

    public int c() {
        return this.a == null ? 4 : this.a.getWifiState();
    }

    public boolean d() {
        return this.a == null ? false : this.a.startScan();
    }

    public boolean e() {
        try {
            if (String.valueOf(cu.a(this.a, "startScanActive", new Object[0])).equals("true")) {
                return true;
            }
        } catch (Throwable th) {
            e.a(th, "WifiManagerWrapper", "startScanActive");
        }
        return false;
    }

    public boolean f() {
        boolean z = false;
        WifiManager wifiManager = this.a;
        if (wifiManager == null) {
            return z;
        }
        boolean equals;
        try {
            z = wifiManager.isWifiEnabled();
        } catch (Throwable th) {
            e.a(th, "WifiManagerWrapper", "wifiEnabled1");
        }
        if (!z && cw.c() > 17) {
            try {
                equals = String.valueOf(cu.a(wifiManager, "isScanAlwaysAvailable", new Object[0])).equals("true");
            } catch (Throwable th2) {
                e.a(th2, "WifiManagerWrapper", "wifiEnabled");
            }
            return equals;
        }
        equals = z;
        return equals;
    }
}
