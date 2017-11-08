package com.fyusion.sdk.processor;

import android.util.Log;

/* compiled from: Unknown */
public abstract class a implements Runnable {
    protected volatile boolean a;

    protected abstract void a();

    protected abstract void a(RuntimeException runtimeException);

    public void run() {
        try {
            if (this.a) {
                Log.d("BaseProcessorJob", "run is cancelled");
            } else {
                a();
            }
        } catch (RuntimeException e) {
            a(e);
        }
    }
}
