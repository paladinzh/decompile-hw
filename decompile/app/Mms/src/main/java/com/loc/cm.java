package com.loc;

/* compiled from: OfflineFileCache */
public class cm extends cn<String, ch> {
    public cm() {
        super(1048576);
    }

    protected int a(String str, ch chVar) {
        int i = 0;
        if (chVar == null) {
            return i;
        }
        try {
            return (int) chVar.g();
        } catch (Throwable e) {
            e.a(e, "OfflineFileCache", "sizeOf");
            return i;
        }
    }

    protected void a(boolean z, String str, ch chVar, ch chVar2) {
        if (chVar != null) {
            try {
                chVar.b();
            } catch (Throwable e) {
                e.a(e, "OfflineFileCache", "entryRemoved");
            }
        }
        super.a(z, str, chVar, chVar2);
    }
}
