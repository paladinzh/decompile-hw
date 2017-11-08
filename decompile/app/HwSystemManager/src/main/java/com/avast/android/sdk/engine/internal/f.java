package com.avast.android.sdk.engine.internal;

import android.content.Context;
import android.text.TextUtils;
import com.avast.android.sdk.engine.EngineConfig;
import com.avast.android.sdk.engine.InvalidConfigException;
import com.avast.android.sdk.internal.h;

/* compiled from: Unknown */
public class f {
    private final h a;

    public f(Context context) {
        this.a = h.a(context.getApplicationContext());
    }

    public void a(EngineConfig engineConfig) throws InvalidConfigException {
        if (engineConfig != null) {
            String b = this.a.b();
            if (!TextUtils.isEmpty(b) && !b.equals(engineConfig.getGuid())) {
                throw new InvalidConfigException("Current GUID (" + engineConfig.getGuid() + ") " + "is different from the previous one (" + b + ")");
            }
            return;
        }
        throw new InvalidConfigException("Configuration can't be null");
    }
}
