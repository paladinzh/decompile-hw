package com.fyusion.sdk.viewer;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.fyusion.sdk.viewer.internal.b;
import com.fyusion.sdk.viewer.internal.f.d;
import com.fyusion.sdk.viewer.internal.request.a;
import com.fyusion.sdk.viewer.internal.request.c;
import com.fyusion.sdk.viewer.internal.request.e;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import com.fyusion.sdk.viewer.view.FyuseView;
import java.net.URL;

/* compiled from: Unknown */
public class RequestBuilder implements Cloneable {
    private final b a;
    private final RequestManager b;
    private final a<?> c;
    @NonNull
    private a<?> d = this.c;
    @Nullable
    private Object e;
    @Nullable
    private RequestListener f;
    @Nullable
    private boolean g;

    RequestBuilder(b bVar, RequestManager requestManager) {
        this.b = requestManager;
        this.a = (b) d.a((Object) bVar);
        this.c = bVar.a();
    }

    private RequestBuilder a(@NonNull a<?> aVar) {
        d.a((Object) aVar);
        this.d = (this.c != this.d ? this.d : this.d.a()).a((a) aVar);
        return this;
    }

    private RequestBuilder a(@Nullable Object obj) {
        this.e = obj;
        this.g = true;
        return this;
    }

    private com.fyusion.sdk.viewer.internal.request.b a(Target target) {
        return a(target, this.d, this.d.f(), this.d.g(), this.d.h());
    }

    private com.fyusion.sdk.viewer.internal.request.b a(Target target, a<?> aVar, d dVar, int i, int i2) {
        aVar.b();
        return e.a(this.a, this.e, aVar, i, i2, dVar, target, this.f, this.a.b());
    }

    public RequestBuilder clone() {
        try {
            RequestBuilder requestBuilder = (RequestBuilder) super.clone();
            requestBuilder.d = requestBuilder.d.a();
            return requestBuilder;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Target downloadOnly(Target target) {
        return into(target);
    }

    public RequestBuilder highRes(boolean z) {
        return a(c.b(z));
    }

    public <Y extends Target> Y into(@NonNull Y y) {
        com.fyusion.sdk.viewer.internal.f.e.a();
        d.a((Object) y);
        if (this.g) {
            if (y.getRequest() != null) {
                this.b.clear(y);
            }
            this.d.b();
            com.fyusion.sdk.viewer.internal.request.b a = a((Target) y);
            y.setRequest(a);
            this.b.a(y, a);
            return y;
        }
        throw new IllegalArgumentException("You must call #load() before calling #into()");
    }

    public Target into(FyuseView fyuseView) {
        d.a((Object) fyuseView);
        return into(new com.fyusion.sdk.viewer.internal.request.target.a(fyuseView));
    }

    public RequestBuilder listener(@Nullable RequestListener requestListener) {
        this.f = requestListener;
        return this;
    }

    public RequestBuilder load(@Nullable Uri uri) {
        return a((Object) uri);
    }

    public RequestBuilder load(@Nullable Object obj) {
        return a(obj);
    }

    public RequestBuilder load(@Nullable String str) {
        return a((Object) str);
    }

    @Deprecated
    public RequestBuilder load(@Nullable URL url) {
        return a((Object) url);
    }
}
