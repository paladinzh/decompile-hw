package com.fyusion.sdk.share;

import android.content.Intent;

/* compiled from: Unknown */
public interface CallbackManager {

    /* compiled from: Unknown */
    public static class Factory {
        public static CallbackManager create() {
            return new CallbackManagerImplementation();
        }
    }

    boolean onActivityResult(int i, int i2, Intent intent);
}
