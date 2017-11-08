package com.google.android.gms.wearable.internal;

import com.google.android.gms.common.api.zzc.zzb;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;

/* compiled from: Unknown */
final class zzbm$zzt extends zzbm$zzb<SendMessageResult> {
    public zzbm$zzt(zzb<SendMessageResult> zzb) {
        super(zzb);
    }

    public void zza(SendMessageResponse sendMessageResponse) {
        zzR(new zzay.zzb(zzbj.zzfx(sendMessageResponse.statusCode), sendMessageResponse.zzaBk));
    }
}
