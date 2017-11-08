package com.fyusion.sdk.viewer.ext.localfyuse.module;

import android.content.Context;
import android.util.Log;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.processor.FyuseProcessor;
import com.fyusion.sdk.viewer.ext.localfyuse.c;
import com.fyusion.sdk.viewer.f;
import com.fyusion.sdk.viewer.internal.d.b;
import java.io.File;

/* compiled from: Unknown */
public class LocalFyuseModule implements b {
    public void registerComponents(Context context, f fVar, com.fyusion.sdk.viewer.internal.b.b.f fVar2) {
        if (a.a().b("viewer", "local") < 1) {
            Log.w("FyuseSDK", "local component is disabled");
            return;
        }
        fVar.a(File.class, com.fyusion.sdk.viewer.internal.b.c.a.class, new com.fyusion.sdk.viewer.ext.localfyuse.f.a(g.a()));
        fVar.a(new c(FyuseProcessor.getInstance()));
    }
}
