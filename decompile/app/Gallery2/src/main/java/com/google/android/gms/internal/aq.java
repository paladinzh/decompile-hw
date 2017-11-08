package com.google.android.gms.internal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class aq {
    public static final ar lW = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            String str = (String) map.get("urls");
            if (str != null) {
                String[] split = str.split(",");
                Map hashMap = new HashMap();
                PackageManager packageManager = ddVar.getContext().getPackageManager();
                for (String str2 : split) {
                    String[] split2 = str2.split(";", 2);
                    hashMap.put(str2, Boolean.valueOf(packageManager.resolveActivity(new Intent(split2.length <= 1 ? "android.intent.action.VIEW" : split2[1].trim(), Uri.parse(split2[0].trim())), HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT) != null));
                }
                ddVar.a("openableURLs", hashMap);
                return;
            }
            da.w("URLs missing in canOpenURLs GMSG.");
        }
    };
    public static final ar lX = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            String str = (String) map.get("u");
            if (str != null) {
                Uri a;
                Uri parse = Uri.parse(str);
                try {
                    l bc = ddVar.bc();
                    if (bc != null) {
                        if (bc.a(parse)) {
                            a = bc.a(parse, ddVar.getContext());
                            new cy(ddVar.getContext(), ddVar.bd().pU, a.toString()).start();
                            return;
                        }
                    }
                } catch (m e) {
                    da.w("Unable to append parameter to URL: " + str);
                }
                a = parse;
                new cy(ddVar.getContext(), ddVar.bd().pU, a.toString()).start();
                return;
            }
            da.w("URL missing from click GMSG.");
        }
    };
    public static final ar lY = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            bo ba = ddVar.ba();
            if (ba != null) {
                ba.close();
            } else {
                da.w("A GMSG tried to close something that wasn't an overlay.");
            }
        }
    };
    public static final ar lZ = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            bo ba = ddVar.ba();
            if (ba != null) {
                ba.g("1".equals(map.get("custom_close")));
            } else {
                da.w("A GMSG tried to use a custom close button on something that wasn't an overlay.");
            }
        }
    };
    public static final ar ma = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            String str = (String) map.get("u");
            if (str != null) {
                new cy(ddVar.getContext(), ddVar.bd().pU, str).start();
            } else {
                da.w("URL missing from httpTrack GMSG.");
            }
        }
    };
    public static final ar mb = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            da.u("Received log message: " + ((String) map.get("string")));
        }
    };
    public static final ar mc = new as();
    public static final ar md = new ar() {
        public void a(dd ddVar, Map<String, String> map) {
            String str = (String) map.get("ty");
            String str2 = (String) map.get("td");
            try {
                int parseInt = Integer.parseInt((String) map.get("tx"));
                int parseInt2 = Integer.parseInt(str);
                int parseInt3 = Integer.parseInt(str2);
                l bc = ddVar.bc();
                if (bc != null) {
                    bc.y().a(parseInt, parseInt2, parseInt3);
                }
            } catch (NumberFormatException e) {
                da.w("Could not parse touch parameters from gmsg.");
            }
        }
    };
    public static final ar me = new at();
}
