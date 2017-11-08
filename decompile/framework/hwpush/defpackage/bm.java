package defpackage;

import android.content.Context;
import java.util.HashMap;

/* renamed from: bm */
final class bm implements Runnable {
    final /* synthetic */ al bY;
    final /* synthetic */ int bZ;
    final /* synthetic */ HashMap ca;
    final /* synthetic */ Context val$context;

    bm(Context context, al alVar, int i, HashMap hashMap) {
        this.val$context = context;
        this.bY = alVar;
        this.bZ = i;
        this.ca = hashMap;
    }

    public void run() {
        bl.a(this.val$context, this.bY, this.bZ, this.ca);
    }
}
