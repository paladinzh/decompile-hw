package com.google.android.gms.location;

import com.google.android.gms.common.api.Status;

@Deprecated
/* compiled from: Unknown */
public final class LocationStatusCodes {
    public static final int ERROR = 1;
    public static final int GEOFENCE_NOT_AVAILABLE = 1000;
    public static final int GEOFENCE_TOO_MANY_GEOFENCES = 1001;
    public static final int GEOFENCE_TOO_MANY_PENDING_INTENTS = 1002;
    public static final int SUCCESS = 0;

    private LocationStatusCodes() {
    }

    public static int zzhy(int i) {
        if (i < 0 || i > 1) {
            if (1000 <= i) {
                if (i > 1002) {
                }
            }
            return 1;
        }
        return i;
    }

    public static Status zzhz(int i) {
        switch (i) {
            case 1:
                i = 13;
                break;
        }
        return new Status(i);
    }
}
