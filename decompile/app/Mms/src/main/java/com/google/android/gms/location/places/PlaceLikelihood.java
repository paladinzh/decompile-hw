package com.google.android.gms.location.places;

import com.google.android.gms.common.data.Freezable;

/* compiled from: Unknown */
public interface PlaceLikelihood extends Freezable<PlaceLikelihood> {
    float getLikelihood();

    Place getPlace();
}
