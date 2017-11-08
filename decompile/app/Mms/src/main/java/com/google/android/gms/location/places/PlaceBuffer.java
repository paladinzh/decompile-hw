package com.google.android.gms.location.places;

import android.content.Context;
import android.support.annotation.Nullable;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.AbstractDataBuffer;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.location.places.internal.zzr;

/* compiled from: Unknown */
public class PlaceBuffer extends AbstractDataBuffer<Place> implements Result {
    private final Context mContext;
    private final Status zzUX;
    private final String zzaPy;

    public PlaceBuffer(DataHolder dataHolder, Context context) {
        super(dataHolder);
        this.mContext = context;
        this.zzUX = PlacesStatusCodes.zzhU(dataHolder.getStatusCode());
        if (dataHolder == null || dataHolder.zzpZ() == null) {
            this.zzaPy = null;
        } else {
            this.zzaPy = dataHolder.zzpZ().getString("com.google.android.gms.location.places.PlaceBuffer.ATTRIBUTIONS_EXTRA_KEY");
        }
    }

    public Place get(int position) {
        return new zzr(this.zzahi, position, this.mContext);
    }

    @Nullable
    public CharSequence getAttributions() {
        return this.zzaPy;
    }

    public Status getStatus() {
        return this.zzUX;
    }
}
