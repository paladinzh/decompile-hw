package com.google.android.gms.wearable;

import com.google.android.gms.common.api.Result;

/* compiled from: Unknown */
public interface CapabilityApi {

    /* compiled from: Unknown */
    public interface CapabilityListener {
        void onCapabilityChanged(CapabilityInfo capabilityInfo);
    }

    /* compiled from: Unknown */
    public interface AddLocalCapabilityResult extends Result {
    }

    /* compiled from: Unknown */
    public interface GetAllCapabilitiesResult extends Result {
    }

    /* compiled from: Unknown */
    public interface GetCapabilityResult extends Result {
    }

    /* compiled from: Unknown */
    public interface RemoveLocalCapabilityResult extends Result {
    }
}
