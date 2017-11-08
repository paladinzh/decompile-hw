package com.fyusion.sdk.viewer.internal.c;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.RequestManager;
import com.fyusion.sdk.viewer.internal.f.e;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class l implements Callback {
    private static final l c = new l();
    final Map<FragmentManager, k> a = new HashMap();
    final Map<android.support.v4.app.FragmentManager, o> b = new HashMap();
    private volatile RequestManager d;
    private final Handler e = new Handler(Looper.getMainLooper(), this);

    l() {
    }

    public static l a() {
        return c;
    }

    private RequestManager b(Context context) {
        if (this.d == null) {
            synchronized (this) {
                if (this.d == null) {
                    this.d = new RequestManager(FyuseViewer.get(context), new b(), new g());
                }
            }
        }
        return this.d;
    }

    @TargetApi(17)
    private static void b(Activity activity) {
        if (VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @TargetApi(11)
    public RequestManager a(Activity activity) {
        if (e.c() || VERSION.SDK_INT < 11) {
            return a(activity.getApplicationContext());
        }
        b(activity);
        return a((Context) activity, activity.getFragmentManager(), null);
    }

    public RequestManager a(Context context) {
        if (context != null) {
            if (e.b() && !(context instanceof Application)) {
                if (context instanceof FragmentActivity) {
                    return a((FragmentActivity) context);
                }
                if (context instanceof Activity) {
                    return a((Activity) context);
                }
                if (context instanceof ContextWrapper) {
                    return a(((ContextWrapper) context).getBaseContext());
                }
            }
            return b(context);
        }
        throw new IllegalArgumentException("You cannot start a load on a null Context");
    }

    @TargetApi(11)
    RequestManager a(Context context, FragmentManager fragmentManager, Fragment fragment) {
        k a = a(fragmentManager, fragment);
        RequestManager b = a.b();
        if (b != null) {
            return b;
        }
        b = new RequestManager(FyuseViewer.get(context), a.a(), a.c());
        a.a(b);
        return b;
    }

    RequestManager a(Context context, android.support.v4.app.FragmentManager fragmentManager, android.support.v4.app.Fragment fragment) {
        o a = a(fragmentManager, fragment);
        RequestManager b = a.b();
        if (b != null) {
            return b;
        }
        b = new RequestManager(FyuseViewer.get(context), a.a(), a.c());
        a.a(b);
        return b;
    }

    public RequestManager a(FragmentActivity fragmentActivity) {
        if (e.c()) {
            return a(fragmentActivity.getApplicationContext());
        }
        b((Activity) fragmentActivity);
        return a((Context) fragmentActivity, fragmentActivity.getSupportFragmentManager(), null);
    }

    @TargetApi(17)
    k a(FragmentManager fragmentManager, Fragment fragment) {
        k kVar = (k) fragmentManager.findFragmentByTag("com.fyusion.manager");
        if (kVar != null) {
            return kVar;
        }
        kVar = (k) this.a.get(fragmentManager);
        if (kVar != null) {
            return kVar;
        }
        kVar = new k();
        kVar.a(fragment);
        this.a.put(fragmentManager, kVar);
        fragmentManager.beginTransaction().add(kVar, "com.fyusion.manager").commitAllowingStateLoss();
        this.e.obtainMessage(1, fragmentManager).sendToTarget();
        return kVar;
    }

    o a(android.support.v4.app.FragmentManager fragmentManager, android.support.v4.app.Fragment fragment) {
        o oVar = (o) fragmentManager.findFragmentByTag("com.fyusion.manager");
        if (oVar != null) {
            return oVar;
        }
        oVar = (o) this.b.get(fragmentManager);
        if (oVar != null) {
            return oVar;
        }
        oVar = new o();
        oVar.a(fragment);
        this.b.put(fragmentManager, oVar);
        fragmentManager.beginTransaction().add(oVar, "com.fyusion.manager").commitAllowingStateLoss();
        this.e.obtainMessage(2, fragmentManager).sendToTarget();
        return oVar;
    }

    public boolean handleMessage(Message message) {
        Object obj = null;
        boolean z = true;
        Object remove;
        switch (message.what) {
            case 1:
                FragmentManager fragmentManager = (FragmentManager) message.obj;
                remove = this.a.remove(fragmentManager);
                break;
            case 2:
                android.support.v4.app.FragmentManager fragmentManager2 = (android.support.v4.app.FragmentManager) message.obj;
                remove = this.b.remove(fragmentManager2);
                break;
            default:
                z = false;
                remove = null;
                break;
        }
        if (z && r1 == null && Log.isLoggable("RMRetriever", 5)) {
            Log.w("RMRetriever", "Failed to remove expected request manager fragment, manager: " + obj);
        }
        return z;
    }
}
