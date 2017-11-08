package com.google.android.gms.location.places.personalized;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.places.PlacesStatusCodes;

/* compiled from: Unknown */
public final class zzd extends com.google.android.gms.common.data.zzd<PlaceUserData> implements Result {
    private final Status zzUX;

    public zzd(DataHolder dataHolder) {
        this(dataHolder, PlacesStatusCodes.zzhU(dataHolder.getStatusCode()));
    }

    private zzd(DataHolder dataHolder, Status status) {
        super(dataHolder, PlaceUserData.CREATOR);
        boolean z = dataHolder == null || dataHolder.getStatusCode() == status.getStatusCode();
        zzx.zzac(z);
        this.zzUX = status;
    }

    public static zzd zzaZ(Status status) {
        return new zzd(null, status);
    }

    public Status getStatus() {
        return this.zzUX;
    }
}
