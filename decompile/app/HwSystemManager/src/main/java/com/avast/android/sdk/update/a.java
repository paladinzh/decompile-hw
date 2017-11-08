package com.avast.android.sdk.update;

import com.avast.android.sdk.engine.ProgressObserver;

/* compiled from: Unknown */
class a implements ProgressObserver {
    final /* synthetic */ VpsUpdateService a;

    a(VpsUpdateService vpsUpdateService) {
        this.a = vpsUpdateService;
    }

    public void onProgressChanged(long j, long j2) {
        this.a.publishDownloadProgress(j, j2);
    }
}
