package com.avast.android.sdk.engine.obfuscated;

import android.annotation.TargetApi;
import android.content.Context;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.UrlSource;
import com.avast.android.sdk.shield.webshield.UrlAction;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/* compiled from: Unknown */
public class be extends Thread {
    private final Context a;
    private final LinkedBlockingQueue<String> b = new LinkedBlockingQueue();
    private AtomicBoolean c = new AtomicBoolean(false);
    private final a d;

    /* compiled from: Unknown */
    public interface a {
        UrlAction a(String str);

        void a(String str, List<UrlCheckResultStructure> list);

        void b(String str);
    }

    public be(Context context, a aVar) {
        if (context == null || aVar == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        this.a = context.getApplicationContext();
        this.d = aVar;
    }

    public void a() {
        this.c.set(true);
        interrupt();
    }

    public void a(String str) {
        this.b.offer(str);
    }

    @TargetApi(21)
    public void run() {
        while (!this.c.get()) {
            try {
                String str = (String) this.b.take();
                UrlAction a = this.d.a(str);
                if (!UrlAction.ALLOW.equals(a)) {
                    if (UrlAction.BLOCK.equals(a)) {
                        this.d.b(str);
                    } else {
                        this.d.a(str, EngineInterface.checkUrl(this.a, null, str, UrlSource.CHROME_M));
                    }
                }
            } catch (InterruptedException e) {
                if (this.c.get()) {
                    return;
                }
            }
        }
    }
}
