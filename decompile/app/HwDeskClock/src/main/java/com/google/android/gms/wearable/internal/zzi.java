package com.google.android.gms.wearable.internal;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityApi.AddLocalCapabilityResult;
import com.google.android.gms.wearable.CapabilityApi.GetAllCapabilitiesResult;
import com.google.android.gms.wearable.CapabilityApi.GetCapabilityResult;
import com.google.android.gms.wearable.CapabilityApi.RemoveLocalCapabilityResult;
import com.google.android.gms.wearable.CapabilityInfo;

/* compiled from: Unknown */
public class zzi implements CapabilityApi {

    /* compiled from: Unknown */
    public static class zzb implements AddLocalCapabilityResult, RemoveLocalCapabilityResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }

    /* compiled from: Unknown */
    public static class zzc implements CapabilityInfo {
    }

    /* compiled from: Unknown */
    public static class zzd implements GetAllCapabilitiesResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }

    /* compiled from: Unknown */
    public static class zze implements GetCapabilityResult {
        private final Status zzQA;

        public Status getStatus() {
            return this.zzQA;
        }
    }
}
