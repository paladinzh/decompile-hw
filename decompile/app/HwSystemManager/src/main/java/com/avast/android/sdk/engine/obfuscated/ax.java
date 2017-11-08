package com.avast.android.sdk.engine.obfuscated;

import java.util.Comparator;

/* compiled from: Unknown */
class ax implements Comparator<aw> {
    ax() {
    }

    public int a(aw awVar, aw awVar2) {
        int i = 0;
        if (awVar == null) {
            return -1;
        }
        if (awVar2 == null) {
            return 1;
        }
        if (awVar.equals(awVar2)) {
            return 0;
        }
        if (awVar.c() >= awVar2.c()) {
            i = 1;
        }
        return i == 0 ? 1 : -1;
    }

    public /* synthetic */ int compare(Object obj, Object obj2) {
        return a((aw) obj, (aw) obj2);
    }
}
