package com.google.android.gms.wearable;

import com.google.android.gms.common.api.CommonStatusCodes;

/* compiled from: Unknown */
public final class WearableStatusCodes extends CommonStatusCodes {
    private WearableStatusCodes() {
    }

    public static String getStatusCodeString(int statusCode) {
        switch (statusCode) {
            case 4000:
                return "TARGET_NODE_NOT_CONNECTED";
            case 4001:
                return "DUPLICATE_LISTENER";
            case 4002:
                return "UNKNOWN_LISTENER";
            case 4003:
                return "DATA_ITEM_TOO_LARGE";
            case 4004:
                return "INVALID_TARGET_NODE";
            case 4005:
                return "ASSET_UNAVAILABLE";
            default:
                return CommonStatusCodes.getStatusCodeString(statusCode);
        }
    }
}
