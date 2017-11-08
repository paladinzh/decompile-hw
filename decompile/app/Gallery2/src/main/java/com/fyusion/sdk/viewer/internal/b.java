package com.fyusion.sdk.viewer.internal;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import com.fyusion.sdk.viewer.f;
import com.fyusion.sdk.viewer.internal.request.c;

/* compiled from: Unknown */
public class b extends ContextWrapper {
    private final Handler a = new Handler(Looper.getMainLooper());
    private final f b;
    private final Context c;
    private final c d;
    private com.fyusion.sdk.viewer.internal.b.b.f e;

    public b(Context context, f fVar, c cVar, com.fyusion.sdk.viewer.internal.b.b.f fVar2) {
        super(context);
        this.c = context;
        this.b = fVar;
        this.d = cVar;
        this.e = fVar2;
    }

    public c a() {
        return this.d;
    }

    public void a(int i) {
    }

    public com.fyusion.sdk.viewer.internal.b.b.f b() {
        return this.e;
    }

    public f c() {
        return this.b;
    }
}
