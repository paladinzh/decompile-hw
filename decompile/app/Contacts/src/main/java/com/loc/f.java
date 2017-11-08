package com.loc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.amap.api.fence.Fence;
import com.amap.api.location.AMapLocation;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/* compiled from: FenceManager */
public class f {
    Context a;
    private Hashtable<PendingIntent, ArrayList<Fence>> b = new Hashtable();

    public f(Context context) {
        this.a = context;
    }

    private void a(PendingIntent pendingIntent, Fence fence, int i) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("fenceid", fence.b);
        bundle.putInt("event", i);
        intent.putExtras(bundle);
        try {
            pendingIntent.send(this.a, 0, intent);
        } catch (Throwable th) {
            e.a(th, "FenceManager", "fcIntent");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(PendingIntent pendingIntent, List<String> list) {
        boolean z = false;
        if (b() || list == null || list.isEmpty() || !this.b.containsKey(pendingIntent)) {
            return false;
        }
        Iterator it = ((ArrayList) this.b.get(pendingIntent)).iterator();
        while (it != null && it.hasNext()) {
            boolean z2;
            Fence fence = (Fence) it.next();
            if (list.contains(fence.b) || a(fence)) {
                it.remove();
                z2 = true;
            } else {
                z2 = z;
            }
            z = z2;
        }
        return z;
    }

    private boolean a(Fence fence) {
        if (fence.b() != -1) {
            if (!(fence.b() > cw.b())) {
                return true;
            }
        }
        return false;
    }

    private boolean a(List<String> list) {
        boolean z = false;
        if (b() || list == null || list.isEmpty()) {
            return false;
        }
        Iterator it = this.b.entrySet().iterator();
        while (it != null && it.hasNext()) {
            Entry entry = (Entry) it.next();
            Iterator it2 = ((ArrayList) entry.getValue()).iterator();
            while (it2 != null && it2.hasNext()) {
                boolean z2;
                Fence fence = (Fence) it2.next();
                if (list.contains(fence.b) || a(fence)) {
                    it2.remove();
                    z2 = true;
                } else {
                    z2 = z;
                }
                z = z2;
            }
            if (((ArrayList) entry.getValue()).isEmpty()) {
                it.remove();
            }
        }
        return z;
    }

    private boolean b() {
        return this.b.isEmpty();
    }

    public void a() {
        this.b.clear();
    }

    public void a(AMapLocation aMapLocation) {
        if (!b()) {
            Iterator it = this.b.entrySet().iterator();
            while (it != null && it.hasNext()) {
                Entry entry = (Entry) it.next();
                Iterator it2 = ((ArrayList) entry.getValue()).iterator();
                while (it2.hasNext()) {
                    Fence fence = (Fence) it2.next();
                    if (!a(fence)) {
                        float a = cw.a(new double[]{fence.d, fence.c, aMapLocation.getLatitude(), aMapLocation.getLongitude()});
                        float accuracy = aMapLocation.getAccuracy();
                        accuracy = accuracy >= 500.0f ? a - (fence.e + 500.0f) : a - (accuracy + fence.e);
                        Object obj = null;
                        if (accuracy > 0.0f) {
                            if (fence.g != 0) {
                                obj = 1;
                            }
                            fence.g = 0;
                        } else {
                            if (fence.g != 1) {
                                obj = 1;
                            }
                            fence.g = 1;
                        }
                        if (obj != null) {
                            switch (fence.g) {
                                case 0:
                                    fence.h = -1;
                                    if ((fence.a() & 2) != 2) {
                                        break;
                                    }
                                    a((PendingIntent) entry.getKey(), fence, 2);
                                    break;
                                case 1:
                                    fence.h = cw.b();
                                    if ((fence.a() & 1) != 1) {
                                        break;
                                    }
                                    a((PendingIntent) entry.getKey(), fence, 1);
                                    break;
                                default:
                                    break;
                            }
                        } else if ((fence.a() & 4) == 4) {
                            if ((fence.h <= 0 ? 1 : null) == null) {
                                if ((cw.b() - fence.h <= fence.c() ? 1 : null) == null) {
                                    fence.h = cw.b();
                                    a((PendingIntent) entry.getKey(), fence, 4);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean a(PendingIntent pendingIntent) {
        if (pendingIntent == null || !this.b.containsKey(pendingIntent)) {
            return false;
        }
        List arrayList = new ArrayList();
        Iterator it = ((ArrayList) this.b.get(pendingIntent)).iterator();
        while (it.hasNext()) {
            arrayList.add(((Fence) it.next()).b);
        }
        return a(arrayList);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(PendingIntent pendingIntent, String str) {
        if (pendingIntent == null || !this.b.containsKey(pendingIntent) || TextUtils.isEmpty(str)) {
            return false;
        }
        List arrayList = new ArrayList();
        arrayList.add(str);
        return a(pendingIntent, arrayList);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(Fence fence, PendingIntent pendingIntent) {
        if (pendingIntent == null || fence == null || TextUtils.isEmpty(fence.b) || fence.e < 100.0f || fence.e > 1000.0f) {
            return false;
        }
        if ((!b() && !this.b.containsKey(pendingIntent)) || fence.a() == 0 || fence.a() > 7) {
            return false;
        }
        Iterator it = this.b.entrySet().iterator();
        int i = 0;
        while (it != null && it.hasNext()) {
            i = ((ArrayList) ((Entry) it.next()).getValue()).size() + i;
        }
        if (i > 20) {
            return false;
        }
        fence.g = -1;
        ArrayList arrayList;
        if (b()) {
            arrayList = new ArrayList();
            arrayList.add(fence);
            this.b.put(pendingIntent, arrayList);
        } else {
            arrayList = (ArrayList) this.b.get(pendingIntent);
            Iterator it2 = arrayList.iterator();
            Fence fence2 = null;
            while (it2.hasNext()) {
                Fence fence3 = (Fence) it2.next();
                if (!fence3.b.equals(fence.b)) {
                    fence3 = fence2;
                }
                fence2 = fence3;
            }
            if (fence2 != null) {
                arrayList.remove(fence2);
            }
            arrayList.add(fence);
            this.b.put(pendingIntent, arrayList);
        }
        return true;
    }
}
