package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: LogDBOperation */
public class bq {
    private bj a;

    public bq(Context context) {
        this.a = new bj(context, bp.c());
    }

    private br a(int i) {
        br bmVar;
        switch (i) {
            case 0:
                bmVar = new bm();
                break;
            case 1:
                bmVar = new bo();
                break;
            case 2:
                bmVar = new bl();
                break;
            default:
                return null;
        }
        return bmVar;
    }

    public void a(String str, int i) {
        try {
            c(str, i);
        } catch (Throwable th) {
            av.a(th, "LogDB", "delLog");
            th.printStackTrace();
        }
    }

    public void b(String str, int i) {
        try {
            c(str, i);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void c(String str, int i) {
        this.a.a(br.a(str), a(i));
    }

    public void a(bs bsVar, int i) {
        try {
            bk a = a(i);
            a.a((Object) bsVar);
            this.a.b(br.a(bsVar.b()), a);
        } catch (Throwable th) {
            av.a(th, "LogDB", "updateLogInfo");
            th.printStackTrace();
        }
    }

    public List<bs> a(int i, int i2) {
        try {
            bk a = a(i2);
            return this.a.c(br.a(i), a);
        } catch (Throwable th) {
            av.a(th, "LogDB", "ByState");
            th.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b(bs bsVar, int i) {
        try {
            br a = a(i);
            switch (i) {
                case 0:
                    a(bsVar, a);
                    break;
                case 1:
                    b(bsVar, a);
                    break;
                case 2:
                    b(bsVar, a);
                    break;
                default:
                    return;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a(bs bsVar, br brVar) {
        brVar.a(bsVar);
        this.a.a((bk) brVar);
    }

    private void b(bs bsVar, br brVar) {
        String a = br.a(bsVar.b());
        List b = this.a.b(a, brVar, true);
        if (b == null || b.size() == 0) {
            brVar.a(bsVar);
            this.a.a((bk) brVar, true);
            return;
        }
        bs bsVar2 = (bs) b.get(0);
        if (bsVar.a() != 0) {
            bsVar2.b(0);
        } else {
            bsVar2.b(bsVar2.d() + 1);
        }
        brVar.a(bsVar2);
        this.a.b(a, brVar);
    }
}
