package com.avast.android.sdk.update;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.UpdateResultStructure;
import com.avast.android.sdk.engine.obfuscated.bg;
import com.avast.android.sdk.internal.c;
import com.avast.android.sdk.internal.h;
import java.util.Calendar;

/* compiled from: Unknown */
public abstract class VpsUpdateService extends IntentService {
    private static final a a = new a();
    private ConnectivityManager b;

    /* compiled from: Unknown */
    private static class a extends BroadcastReceiver {
        private boolean a;

        private a() {
        }

        synchronized void a(Context context) {
            if (!this.a) {
                context.getApplicationContext().registerReceiver(this, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                this.a = true;
            }
        }

        synchronized void b(Context context) {
            if (this.a) {
                context.getApplicationContext().unregisterReceiver(this);
                this.a = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            Intent intent2 = new Intent();
            intent2.setComponent(c.a(com.avast.android.sdk.internal.c.a.VPS_UPDATE_SERVICE));
            intent2.putExtra("com.avast.android.sdk.engine.intent.extra.update.SCHEDULE_NEXT_RUN", false);
            context.startService(intent2);
        }
    }

    public VpsUpdateService(String str) {
        super(str);
    }

    protected abstract boolean isUpdateAllowed(NetworkInfo networkInfo);

    protected void onHandleIntent(Intent intent) {
        if (this.b == null) {
            this.b = (ConnectivityManager) getSystemService("connectivity");
        }
        if (intent == null || intent.getBooleanExtra("com.avast.android.sdk.engine.intent.extra.update.SCHEDULE_NEXT_RUN", true)) {
            h.a(getApplicationContext()).a(Calendar.getInstance().getTimeInMillis());
            bg.a(getApplicationContext(), EngineInterface.getEngineConfig());
        }
        if (isUpdateAllowed(this.b.getActiveNetworkInfo())) {
            a.b(this);
            onUpdateStarted();
            publishResult(EngineInterface.update(getApplicationContext(), new a(this)));
            return;
        }
        a.a(this);
    }

    protected abstract void onUpdateStarted();

    protected abstract void publishDownloadProgress(long j, long j2);

    protected abstract void publishResult(UpdateResultStructure updateResultStructure);
}
