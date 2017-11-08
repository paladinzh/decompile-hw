package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.ProgressObserver;

/* compiled from: Unknown */
class w implements ProgressObserver {
    final /* synthetic */ ProgressObserver a;

    w(ProgressObserver progressObserver) {
        this.a = progressObserver;
    }

    public void onProgressChanged(long j, long j2) {
        if (this.a != null) {
            if ((j2 >= 1 ? 1 : null) != null) {
                long j3 = 2 * j2;
                this.a.onProgressChanged((j3 / 10) + j, j3);
            }
        }
    }
}
