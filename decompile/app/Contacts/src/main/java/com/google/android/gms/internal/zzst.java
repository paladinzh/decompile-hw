package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public class zzst extends IOException {
    public zzst(String str) {
        super(str);
    }

    static zzst zzJs() {
        return new zzst("While parsing a protocol message, the input ended unexpectedly in the middle of a field.  This could mean either than the input has been truncated or that an embedded message misreported its own length.");
    }

    static zzst zzJt() {
        return new zzst("CodedInputStream encountered an embedded string or message which claimed to have negative size.");
    }

    static zzst zzJu() {
        return new zzst("CodedInputStream encountered a malformed varint.");
    }

    static zzst zzJv() {
        return new zzst("Protocol message contained an invalid tag (zero).");
    }

    static zzst zzJw() {
        return new zzst("Protocol message end-group tag did not match expected tag.");
    }

    static zzst zzJx() {
        return new zzst("Protocol message tag had invalid wire type.");
    }

    static zzst zzJy() {
        return new zzst("Protocol message had too many levels of nesting.  May be malicious.  Use CodedInputStream.setRecursionLimit() to increase the depth limit.");
    }
}
