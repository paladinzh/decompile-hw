package com.google.android.gms.common;

import android.content.Intent;

/* compiled from: Unknown */
public class GooglePlayServicesRepairableException extends UserRecoverableException {
    private final int zzVn;

    GooglePlayServicesRepairableException(int connectionStatusCode, String msg, Intent intent) {
        super(msg, intent);
        this.zzVn = connectionStatusCode;
    }

    public int getConnectionStatusCode() {
        return this.zzVn;
    }
}
