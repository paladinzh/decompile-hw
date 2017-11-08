package com.google.android.gms.tagmanager;

import com.android.gallery3d.gadget.XmlUtils;
import com.google.android.gms.internal.d$a;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/* compiled from: Unknown */
class dl {
    private static by<d$a> a(by<d$a> byVar) {
        try {
            return new by(di.r(bO(di.j((d$a) byVar.getObject()))), byVar.jr());
        } catch (Throwable e) {
            bh.c("Escape URI: unsupported encoding", e);
            return byVar;
        }
    }

    private static by<d$a> a(by<d$a> byVar, int i) {
        if (q((d$a) byVar.getObject())) {
            switch (i) {
                case 12:
                    return a(byVar);
                default:
                    bh.t("Unsupported Value Escaping: " + i);
                    return byVar;
            }
        }
        bh.t("Escaping can only be applied to strings.");
        return byVar;
    }

    static by<d$a> a(by<d$a> byVar, int... iArr) {
        by a;
        for (int a2 : iArr) {
            a = a(a, a2);
        }
        return a;
    }

    static String bO(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, XmlUtils.INPUT_ENCODING).replaceAll("\\+", "%20");
    }

    private static boolean q(d$a d_a) {
        return di.o(d_a) instanceof String;
    }
}
