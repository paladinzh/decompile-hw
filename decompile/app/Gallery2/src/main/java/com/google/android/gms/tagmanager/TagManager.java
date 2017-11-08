package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.Uri;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* compiled from: Unknown */
public class TagManager {
    private static TagManager XB;
    private final DataLayer TN;
    private final r Wj;
    private final ConcurrentMap<n, Boolean> XA;
    private final a Xz;
    private final Context mContext;

    /* compiled from: Unknown */
    interface a {
    }

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.tagmanager.TagManager$3 */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] XD = new int[a.values().length];

        static {
            try {
                XD[a.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                XD[a.CONTAINER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                XD[a.CONTAINER_DEBUG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    TagManager(Context context, a containerHolderLoaderProvider, DataLayer dataLayer) {
        if (context != null) {
            this.mContext = context.getApplicationContext();
            this.Xz = containerHolderLoaderProvider;
            this.XA = new ConcurrentHashMap();
            this.TN = dataLayer;
            this.TN.a(new b(this) {
                final /* synthetic */ TagManager XC;

                {
                    this.XC = r1;
                }

                public void v(Map<String, Object> map) {
                    Object obj = map.get("event");
                    if (obj != null) {
                        this.XC.bE(obj.toString());
                    }
                }
            });
            this.TN.a(new d(this.mContext));
            this.Wj = new r();
            return;
        }
        throw new NullPointerException("context cannot be null");
    }

    private void bE(String str) {
        for (n ba : this.XA.keySet()) {
            ba.ba(str);
        }
    }

    public static TagManager getInstance(Context context) {
        TagManager tagManager;
        synchronized (TagManager.class) {
            if (XB == null) {
                if (context != null) {
                    XB = new TagManager(context, new a() {
                    }, new DataLayer(new v(context)));
                } else {
                    bh.t("TagManager.getInstance requires non-null context.");
                    throw new NullPointerException();
                }
            }
            tagManager = XB;
        }
        return tagManager;
    }

    synchronized boolean f(Uri uri) {
        ce ju = ce.ju();
        if (!ju.f(uri)) {
            return false;
        }
        String containerId = ju.getContainerId();
        switch (AnonymousClass3.XD[ju.jv().ordinal()]) {
            case 1:
                for (n nVar : this.XA.keySet()) {
                    if (nVar.getContainerId().equals(containerId)) {
                        nVar.bc(null);
                        nVar.refresh();
                    }
                }
                break;
            case 2:
            case 3:
                for (n nVar2 : this.XA.keySet()) {
                    if (nVar2.getContainerId().equals(containerId)) {
                        nVar2.bc(ju.jw());
                        nVar2.refresh();
                    } else if (nVar2.iF() != null) {
                        nVar2.bc(null);
                        nVar2.refresh();
                    }
                }
                break;
        }
        return true;
    }
}
