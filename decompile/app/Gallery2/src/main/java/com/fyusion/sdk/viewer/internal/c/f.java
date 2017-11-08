package com.fyusion.sdk.viewer.internal.c;

import android.content.Context;
import android.support.annotation.NonNull;
import com.fyusion.sdk.viewer.internal.c.c.a;

/* compiled from: Unknown */
public class f implements d {
    @NonNull
    public c a(@NonNull Context context, @NonNull a aVar) {
        Object obj = null;
        if (context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == 0) {
            obj = 1;
        }
        return obj == null ? new j() : new e(context, aVar);
    }
}
