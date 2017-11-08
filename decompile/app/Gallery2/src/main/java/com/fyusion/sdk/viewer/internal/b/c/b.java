package com.fyusion.sdk.viewer.internal.b.c;

import com.fyusion.sdk.common.h;
import com.fyusion.sdk.viewer.internal.a;
import com.fyusion.sdk.viewer.internal.b.c.a.c;
import java.io.InputStream;
import org.json.JSONObject;

/* compiled from: Unknown */
public class b {
    private final com.fyusion.sdk.viewer.internal.e.b a = new com.fyusion.sdk.viewer.internal.e.b();

    public a a(h hVar, boolean z, c cVar) {
        return new a(hVar, z, cVar);
    }

    public a a(a aVar, boolean z, c cVar) {
        return a.a(aVar, z, cVar);
    }

    public a a(InputStream inputStream, boolean z, c cVar) throws Exception {
        return inputStream == null ? null : a(a.a((JSONObject) this.a.a(inputStream)), z, cVar);
    }
}
