package com.avast.android.sdk.engine.obfuscated;

import android.content.Context;
import com.avast.android.sdk.engine.obfuscated.bm.a;

/* compiled from: Unknown */
public class bk {

    /* compiled from: Unknown */
    /* renamed from: com.avast.android.sdk.engine.obfuscated.bk$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[bj.values().length];

        static {
            try {
                a[bj.TEST.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[bj.STAGE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[bj.SANDBOX.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static p a(Context context) {
        return a(context, bj.PRODUCTION);
    }

    public static p a(Context context, bj bjVar) {
        p pVar = new p();
        t a = bl.a(context);
        pVar.a(a.a());
        pVar.a(a(bjVar));
        pVar.a(a);
        return pVar;
    }

    private static String a(bj bjVar) {
        switch (AnonymousClass1.a[bjVar.ordinal()]) {
            case 1:
                return "https://auth-test.ff.avast.com:443";
            case 2:
                return "https://auth.ff.avast.com:443";
            case 3:
                return "https://auth.ff.avast.com:443";
            default:
                return "https://auth.ff.avast.com:443";
        }
    }
}
