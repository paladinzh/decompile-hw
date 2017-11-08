package com.google.android.gms.wearable;

import java.io.IOException;

/* compiled from: Unknown */
public class ChannelIOException extends IOException {
    private final int zzaYO;
    private final int zzaYP;

    public ChannelIOException(String message, int closeReason, int appSpecificErrorCode) {
        super(message);
        this.zzaYO = closeReason;
        this.zzaYP = appSpecificErrorCode;
    }
}
