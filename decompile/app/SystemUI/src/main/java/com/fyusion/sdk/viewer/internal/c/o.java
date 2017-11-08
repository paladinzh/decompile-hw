package com.fyusion.sdk.viewer.internal.c;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.fyusion.sdk.viewer.RequestManager;
import java.util.HashSet;

/* compiled from: Unknown */
public class o extends Fragment {
    private final a a;
    private final m b;
    private final HashSet<o> c;
    @Nullable
    private o d;
    @Nullable
    private RequestManager e;
    @Nullable
    private Fragment f;

    /* compiled from: Unknown */
    private class a implements m {
        final /* synthetic */ o a;

        private a(o oVar) {
            this.a = oVar;
        }

        public String toString() {
            return super.toString() + "{fragment=" + this.a + "}";
        }
    }

    public o() {
        this(new a());
    }

    @SuppressLint({"ValidFragment"})
    public o(a aVar) {
        this.b = new a();
        this.c = new HashSet();
        this.a = aVar;
    }

    private void a(FragmentActivity fragmentActivity) {
        e();
        this.d = l.a().a(fragmentActivity.getSupportFragmentManager(), null);
        if (this.d != this) {
            this.d.a(this);
        }
    }

    private void a(o oVar) {
        this.c.add(oVar);
    }

    private void b(o oVar) {
        this.c.remove(oVar);
    }

    private Fragment d() {
        Fragment parentFragment = getParentFragment();
        return parentFragment == null ? this.f : parentFragment;
    }

    private void e() {
        if (this.d != null) {
            this.d.b(this);
            this.d = null;
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
        this.e = requestManager;
    }

    @Nullable
    public RequestManager b() {
        return this.e;
    }

    public m c() {
        return this.b;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            a(getActivity());
        } catch (Throwable e) {
            if (Log.isLoggable("SupportRMFragment", 5)) {
                Log.w("SupportRMFragment", "Unable to register fragment with root", e);
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
        this.f = null;
        e();
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.e != null) {
            this.e.onLowMemory();
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

    public String toString() {
        return super.toString() + "{parent=" + d() + "}";
    }
}
