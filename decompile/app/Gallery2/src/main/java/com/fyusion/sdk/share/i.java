package com.fyusion.sdk.share;

import android.net.Uri;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.ProcessItem;

/* compiled from: Unknown */
class i {
    private h a;
    private Uri b;
    private ProcessItem c;

    i(Uri uri) {
        this.b = uri;
    }

    public Uri a() {
        return this.b;
    }

    void a(@NonNull ProcessItem processItem) {
        this.c = processItem;
    }

    public void a(h hVar) {
        this.a = hVar;
    }

    public h b() {
        return this.a;
    }

    void c() {
        this.c.cancel();
        this.c.waitForRunners();
    }
}
