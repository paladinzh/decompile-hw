package com.loc;

import android.content.Context;
import java.util.List;

/* compiled from: LogDBOperation */
public class an {
    private aj a;

    public an(Context context) {
        this.a = new aj(context, am.c());
    }

    private void a(ap apVar, ao aoVar) {
        aoVar.a(apVar);
        this.a.a((ak) aoVar);
    }

    private void b(ap apVar, ao aoVar) {
        String a = ao.a(apVar.b());
        List b = this.a.b(a, aoVar, true);
        if (b == null || b.size() == 0) {
            aoVar.a(apVar);
            this.a.a((ak) aoVar, true);
            return;
        }
        ap apVar2 = (ap) b.get(0);
        if (apVar.a() != 0) {
            apVar2.b(0);
        } else {
            apVar2.b(apVar2.d() + 1);
        }
        aoVar.a(apVar2);
        this.a.b(a, aoVar);
    }

    private void c(String str, int i) {
        this.a.a(ao.a(str), new ao(i));
    }

    public List<ap> a(int i, int i2) {
        try {
            ak aoVar = new ao(i2);
            return this.a.c(ao.a(i), aoVar);
        } catch (Throwable th) {
            aa.a(th, "LogDB", "ByState");
            return null;
        }
    }

    public void a(ap apVar, int i) {
        try {
            ak aoVar = new ao(i);
            aoVar.a((Object) apVar);
            this.a.b(ao.a(apVar.b()), aoVar);
        } catch (Throwable th) {
            aa.a(th, "LogDB", "updateLogInfo");
        }
    }

    public void a(String str, int i) {
        try {
            c(str, i);
        } catch (Throwable th) {
            aa.a(th, "LogDB", "delLog");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b(ap apVar, int i) {
        try {
            ao aoVar = new ao(i);
            switch (i) {
                case 0:
                    a(apVar, aoVar);
                    break;
                case 1:
                    b(apVar, aoVar);
                    break;
                case 2:
                    b(apVar, aoVar);
                    break;
                default:
                    return;
            }
        } catch (Throwable th) {
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
}
