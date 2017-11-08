package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: LogDBOperation */
public class ak {
    private ai a;

    public ak(Context context) {
        this.a = new ai(context);
    }

    private al a(int i) {
        al agVar;
        switch (i) {
            case 0:
                agVar = new ag();
                break;
            case 1:
                agVar = new aj();
                break;
            case 2:
                agVar = new af();
                break;
            default:
                return null;
        }
        return agVar;
    }

    public void a(String str, int i) {
        try {
            c(str, i);
        } catch (Throwable th) {
            ay.a(th, "LogDB", "delLog");
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
        this.a.a(al.a(str), a(i));
    }

    public void a(am amVar, int i) {
        try {
            ap a = a(i);
            a.a(amVar);
            this.a.b(al.a(amVar.b()), a);
        } catch (Throwable th) {
            ay.a(th, "LogDB", "updateLogInfo");
            th.printStackTrace();
        }
    }

    public List<am> a(int i, int i2) {
        try {
            ap a = a(i2);
            return this.a.c(al.a(i), a);
        } catch (Throwable th) {
            ay.a(th, "LogDB", "ByState");
            th.printStackTrace();
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b(am amVar, int i) {
        try {
            al a = a(i);
            switch (i) {
                case 0:
                    a(amVar, a);
                    break;
                case 1:
                    b(amVar, a);
                    break;
                case 2:
                    b(amVar, a);
                    break;
                default:
                    return;
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a(am amVar, al alVar) {
        alVar.a(amVar);
        this.a.a(alVar);
    }

    private void b(am amVar, al alVar) {
        String a = al.a(amVar.b());
        List c = this.a.c(a, alVar);
        if (c == null || c.size() == 0) {
            alVar.a(amVar);
            this.a.a(alVar);
            return;
        }
        am amVar2 = (am) c.get(0);
        if (amVar.a() != 0) {
            amVar2.b(0);
        } else {
            amVar2.b(amVar2.d() + 1);
        }
        alVar.a(amVar2);
        this.a.b(a, alVar);
    }
}
