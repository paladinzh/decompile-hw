package com.fyusion.sdk.processor.a;

import com.fyusion.sdk.common.internal.analytics.Fyulytics;
import com.fyusion.sdk.common.internal.analytics.Fyulytics.a;
import com.fyusion.sdk.common.internal.analytics.f;

/* compiled from: Unknown */
public class b {
    public static void a(String str) {
        Fyulytics.sharedInstance().startEvent(new f(str));
    }

    public static void a(String str, final int i, final boolean z) {
        Fyulytics.sharedInstance().endEvent(Fyulytics.makeTimedEventKey("PROCESSOR", str), new a<f>() {
            public void a(f fVar) {
                int i = 0;
                fVar.a = i;
                if (z) {
                    i = 1;
                }
                fVar.b = i;
                fVar.status = "0";
            }
        });
    }

    public static void a(String str, final String str2, final String str3) {
        Fyulytics.sharedInstance().endEvent(Fyulytics.makeTimedEventKey("PROCESSOR", str), new a<f>() {
            public void a(f fVar) {
                fVar.status = str2;
                fVar.message = str3;
            }
        });
    }
}
