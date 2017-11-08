package com.fyusion.sdk.viewer.internal.b.b.a;

import android.content.Context;
import com.fyusion.sdk.viewer.internal.b.b.a.e.a;
import java.io.File;

/* compiled from: Unknown */
public final class i extends e {

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.viewer.internal.b.b.a.i$1 */
    class AnonymousClass1 implements a {
        final /* synthetic */ File a;
        final /* synthetic */ String b;

        AnonymousClass1(File file, String str) {
            this.a = file;
            this.b = str;
        }

        public File a() {
            return this.a != null ? this.b == null ? this.a : new File(this.a, this.b) : null;
        }
    }

    public i(Context context, String str, int i, File file) {
        super(new AnonymousClass1(file, str), i);
    }
}
