package com.fyusion.sdk.common.ext;

import android.graphics.Bitmap;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.ext.filter.a.l;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* compiled from: Unknown */
public class BitmapFilterGenerator {
    private static ExecutorService b = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "FIG");
        }
    });
    private i a = new i();

    /* compiled from: Unknown */
    public interface Listener {
        void onApplied(Bitmap bitmap);
    }

    public void apply(final Bitmap bitmap, final ImageFilter imageFilter, final Listener listener) {
        if (bitmap != null) {
            b.execute(new Runnable() {
                public void run() {
                    BitmapFilterGenerator.this.a.a(bitmap.getWidth(), bitmap.getHeight());
                    listener.onApplied(BitmapFilterGenerator.this.a.a(bitmap, l.a(imageFilter)));
                }
            });
        }
    }

    public void apply(final Bitmap bitmap, final Collection<ImageFilter> collection, final Listener listener) {
        if (bitmap != null) {
            b.execute(new Runnable() {
                public void run() {
                    BitmapFilterGenerator.this.a.a(bitmap.getWidth(), bitmap.getHeight());
                    l lVar = new l();
                    lVar.a(collection);
                    listener.onApplied(BitmapFilterGenerator.this.a.a(bitmap, lVar));
                }
            });
        }
    }
}
