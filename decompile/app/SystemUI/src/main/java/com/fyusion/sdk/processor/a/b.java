package com.fyusion.sdk.processor.a;

import com.fyusion.sdk.common.a.a.f;
import com.fyusion.sdk.common.a.a.f.a;
import com.fyusion.sdk.common.a.a.h;

/* compiled from: Unknown */
public class b {
    public static void a(String str) {
        f.a().b(new h(str));
    }

    public static void a(String str, final int i, final boolean z) {
        f.a().a(f.a("PROCESSOR", str), new a<h>() {
            public void a(h hVar) {
                int i = 0;
                hVar.a = i;
                if (z) {
                    i = 1;
                }
                hVar.b = i;
                hVar.i = "0";
            }
        });
    }

    public static void a(String str, final String str2, final String str3) {
        f.a().a(f.a("PROCESSOR", str), new a<h>() {
            public void a(h hVar) {
                hVar.i = str2;
                hVar.j = str3;
            }
        });
    }
}
