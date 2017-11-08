package com.fyusion.sdk.viewer.internal.b.c;

import android.support.annotation.Nullable;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.d;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public interface g<Model, Data> {

    /* compiled from: Unknown */
    public static class a<Data> {
        public final e a;
        public final List<e> b;
        public final com.fyusion.sdk.viewer.internal.b.a.a<Data> c;

        public a(e eVar, com.fyusion.sdk.viewer.internal.b.a.a<Data> aVar) {
            this(eVar, Collections.emptyList(), aVar);
        }

        public a(e eVar, List<e> list, com.fyusion.sdk.viewer.internal.b.a.a<Data> aVar) {
            this.a = (e) d.a((Object) eVar);
            this.b = (List) d.a((Object) list);
            this.c = (com.fyusion.sdk.viewer.internal.b.a.a) d.a((Object) aVar);
        }
    }

    @Nullable
    a<Data> a(Model model, boolean z);

    boolean a(Model model);
}
