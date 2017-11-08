package com.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* compiled from: Unknown */
final class dp extends BroadcastReceiver {
    final /* synthetic */ dl a;

    private dp(dl dlVar) {
        this.a = dlVar;
    }

    public final void onReceive(Context context, Intent intent) {
        if (!(context == null || intent == null)) {
            try {
                if (!(this.a.i == null || this.a.F == null || this.a.E == null || intent.getAction() == null || !"android.net.wifi.SCAN_RESULTS".equals(intent.getAction()))) {
                    List scanResults = this.a.i.getScanResults();
                    synchronized (this) {
                        this.a.E.clear();
                        this.a.x = System.currentTimeMillis();
                        if (scanResults != null) {
                            if (scanResults.size() > 0) {
                                for (int i = 0; i < scanResults.size(); i++) {
                                    this.a.E.add((ScanResult) scanResults.get(i));
                                }
                            }
                        }
                    }
                    TimerTask dqVar = new dq(this);
                    synchronized (this) {
                        if (this.a.F != null) {
                            this.a.F.cancel();
                            this.a.F = null;
                        }
                        this.a.F = new Timer();
                        this.a.F.schedule(dqVar, (long) dl.I);
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}
