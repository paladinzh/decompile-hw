package com.google.android.gms.wearable.internal;

import com.google.android.gms.common.api.zzc.zzb;
import com.google.android.gms.wearable.NodeApi.GetConnectedNodesResult;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
final class zzbm$zzj extends zzbm$zzb<GetConnectedNodesResult> {
    public zzbm$zzj(zzb<GetConnectedNodesResult> zzb) {
        super(zzb);
    }

    public void zza(GetConnectedNodesResponse getConnectedNodesResponse) {
        List arrayList = new ArrayList();
        arrayList.addAll(getConnectedNodesResponse.zzbaC);
        zzR(new zzba.zzb(zzbj.zzfx(getConnectedNodesResponse.statusCode), arrayList));
    }
}
