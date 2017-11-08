package com.fyusion.sdk.share.a;

import com.fyusion.sdk.common.internal.analytics.Fyulytics;
import com.fyusion.sdk.common.internal.analytics.g;

/* compiled from: Unknown */
public class a {
    public static void a(String str) {
        Fyulytics.sharedInstance().startEvent(new g(str));
    }

    public static void a(String str, String str2, String str3, int i, boolean z, String str4, String str5) {
        final String str6 = str3;
        final String str7 = str2;
        final int i2 = i;
        final boolean z2 = z;
        final String str8 = str4;
        final String str9 = str5;
        Fyulytics.sharedInstance().endEvent(Fyulytics.makeTimedEventKey("SHARE", str), new com.fyusion.sdk.common.internal.analytics.Fyulytics.a<g>() {
            public void a(g gVar) {
                int i = 0;
                gVar.key = "SHARE";
                gVar.a = str6;
                gVar.b = str7;
                gVar.d = i2;
                if (z2) {
                    i = 1;
                }
                gVar.c = i;
                gVar.status = str8;
                gVar.message = str9;
            }
        });
    }
}
