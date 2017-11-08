package com.fyusion.sdk.viewer.internal.c;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.util.Log;
import com.fyusion.sdk.viewer.RequestManager;
import java.util.HashSet;

@TargetApi(11)
/* compiled from: Unknown */
public class k extends Fragment {
    private final a a;
    private final m b;
    private final HashSet<k> c;
    @Nullable
    private RequestManager d;
    @Nullable
    private k e;
    @Nullable
    private Fragment f;

    /* compiled from: Unknown */
    private class a implements m {
        final /* synthetic */ k a;

        private a(k kVar) {
            this.a = kVar;
        }

        public String toString() {
            return super.toString() + "{fragment=" + this.a + "}";
        }
    }

    public k() {
        this(new a());
    }

    @SuppressLint({"ValidFragment"})
    k(a aVar) {
        this.b = new a();
        this.c = new HashSet();
        this.a = aVar;
    }

    private void a(Activity activity) {
        e();
        this.e = l.a().a(activity.getFragmentManager(), null);
        if (this.e != this) {
            this.e.a(this);
        }
    }

    private void a(k kVar) {
        this.c.add(kVar);
    }

    private void b(k kVar) {
        this.c.remove(kVar);
    }

    @TargetApi(17)
    private Fragment d() {
        Fragment fragment = null;
        if (VERSION.SDK_INT >= 17) {
            fragment = getParentFragment();
        }
        return fragment == null ? this.f : fragment;
    }

    private void e() {
        if (this.e != null) {
            this.e.b(this);
            this.e = null;
        }
    }

    a a() {
        return this.a;
    }

    void a(Fragment fragment) {
        this.f = fragment;
        if (fragment != null && fragment.getActivity() != null) {
            a(fragment.getActivity());
        }
    }

    public void a(RequestManager requestManager) {
        this.d = requestManager;
    }

    @Nullable
    public RequestManager b() {
        return this.d;
    }

    public m c() {
        return this.b;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            a(activity);
        } catch (Throwable e) {
            if (Log.isLoggable("RMFragment", 5)) {
                Log.w("RMFragment", "Unable to register fragment with root", e);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.a.c();
        e();
    }

    public void onDetach() {
        super.onDetach();
        e();
    }

    public void onLowMemory() {
        if (this.d != null) {
            this.d.onLowMemory();
        }
    }

    public void onStart() {
        super.onStart();
        this.a.a();
    }

    public void onStop() {
        super.onStop();
        this.a.b();
    }

    public void onTrimMemory(int i) {
        if (this.d != null) {
            this.d.onTrimMemory(i);
        }
    }

    public String toString() {
        return super.toString() + "{parent=" + d() + "}";
    }
}
