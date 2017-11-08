package com.google.android.gms.auth;

import android.content.Intent;

/* compiled from: Unknown */
public class GooglePlayServicesAvailabilityException extends UserRecoverableAuthException {
    private final int zzVn;

    GooglePlayServicesAvailabilityException(int connectionStatusCode, String msg, Intent intent) {
        super(msg, intent);
        this.zzVn = connectionStatusCode;
    }

    public int getConnectionStatusCode() {
        return this.zzVn;
    }
}
