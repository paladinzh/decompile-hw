package com.avast.android.sdk.internal;

import android.content.Context;
import android.os.Bundle;
import com.avast.android.sdk.engine.EngineConfig;
import com.avast.android.sdk.engine.obfuscated.b;
import com.avast.android.sdk.engine.obfuscated.c;
import com.avast.android.shepherd.Shepherd;
import com.avast.android.shepherd.Shepherd.Sdk;

/* compiled from: Unknown */
public class a {
    private static a a;

    private a(Context context, EngineConfig engineConfig) {
        Bundle bundle = new Bundle();
        bundle.putString(Shepherd.BUNDLE_PARAMS_INSTALLATION_GUID_KEY, engineConfig.getGuid());
        bundle.putString(Shepherd.BUNDLE_PARAMS_SDK_API_KEY_KEY, engineConfig.getApiKey());
        bundle.putString(Shepherd.BUNDLE_PARAMS_HARDWARE_ID_STRING_KEY, b.a(context));
        bundle.putString(Shepherd.BUNDLE_PARAMS_PROFILE_ID_STRING_KEY, c.a(context));
        Shepherd.initSdk(Sdk.AV_SDK, context.getApplicationContext(), bundle);
    }

    public static synchronized a a(Context context, EngineConfig engineConfig) {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a(context, engineConfig);
            }
            aVar = a;
        }
        return aVar;
    }

    public void a() {
        Shepherd.ping();
    }

    public void a(String str) {
        Bundle bundle = new Bundle();
        bundle.putString(Shepherd.BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY, str);
        Shepherd.updateSdkParams(Sdk.AV_SDK, bundle);
    }
}
