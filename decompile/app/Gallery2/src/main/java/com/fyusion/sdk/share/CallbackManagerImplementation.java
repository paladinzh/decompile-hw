package com.fyusion.sdk.share;

import android.content.Intent;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class CallbackManagerImplementation implements CallbackManager {
    private Map<Integer, ActivityCallback> a = new HashMap();

    public boolean onActivityResult(int i, int i2, Intent intent) {
        ActivityCallback activityCallback = (ActivityCallback) this.a.get(Integer.valueOf(i));
        return activityCallback == null ? false : activityCallback.onActivityResult(i2, intent);
    }

    public void registerCallback(int i, ActivityCallback activityCallback) {
        this.a.put(Integer.valueOf(i), activityCallback);
    }
}
