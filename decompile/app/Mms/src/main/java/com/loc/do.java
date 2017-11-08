package com.loc;

import android.location.GpsStatus.NmeaListener;

/* compiled from: Unknown */
final class do implements NmeaListener {
    private /* synthetic */ dl a;

    private do(dl dlVar) {
        this.a = dlVar;
    }

    public final void onNmeaReceived(long j, String str) {
        try {
            this.a.q = j;
            this.a.r = str;
        } catch (Exception e) {
        }
    }
}
