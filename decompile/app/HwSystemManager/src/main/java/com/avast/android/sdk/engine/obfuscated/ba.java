package com.avast.android.sdk.engine.obfuscated;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.util.ArrayList;
import java.util.HashMap;

/* compiled from: Unknown */
public class ba {
    private static final Object f = new Object();
    private static ba g;
    private final Context a;
    private final HashMap<BroadcastReceiver, ArrayList<IntentFilter>> b = new HashMap();
    private final HashMap<String, ArrayList<b>> c = new HashMap();
    private final ArrayList<a> d = new ArrayList();
    private final Handler e;

    /* compiled from: Unknown */
    private static class a {
        final Intent a;
        final ArrayList<b> b;
    }

    /* compiled from: Unknown */
    private static class b {
        final IntentFilter a;
        final BroadcastReceiver b;

        b(IntentFilter intentFilter, BroadcastReceiver broadcastReceiver) {
            this.a = intentFilter;
            this.b = broadcastReceiver;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(128);
            stringBuilder.append("Receiver{");
            stringBuilder.append(this.b);
            stringBuilder.append(" filter=");
            stringBuilder.append(this.a);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }
    }

    private ba(Context context) {
        this.a = context;
        this.e = new bb(this, context.getMainLooper());
    }

    public static ba a(Context context) {
        ba baVar;
        synchronized (f) {
            if (g == null) {
                g = new ba(context.getApplicationContext());
            }
            baVar = g;
        }
        return baVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a() {
        while (true) {
            synchronized (this.b) {
                int size = this.d.size();
                if (size > 0) {
                    a[] aVarArr = new a[size];
                    this.d.toArray(aVarArr);
                    this.d.clear();
                } else {
                    return;
                }
            }
        }
    }

    public void a(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        synchronized (this.b) {
            b bVar = new b(intentFilter, broadcastReceiver);
            ArrayList arrayList = (ArrayList) this.b.get(broadcastReceiver);
            if (arrayList == null) {
                arrayList = new ArrayList(1);
                this.b.put(broadcastReceiver, arrayList);
            }
            arrayList.add(intentFilter);
            for (int i = 0; i < intentFilter.countActions(); i++) {
                String action = intentFilter.getAction(i);
                arrayList = (ArrayList) this.c.get(action);
                if (arrayList == null) {
                    arrayList = new ArrayList(1);
                    this.c.put(action, arrayList);
                }
                arrayList.add(bVar);
            }
        }
    }
}
