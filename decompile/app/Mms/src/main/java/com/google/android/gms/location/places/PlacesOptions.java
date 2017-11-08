package com.google.android.gms.location.places;

import com.google.android.gms.common.api.Api.ApiOptions.Optional;

/* compiled from: Unknown */
public final class PlacesOptions implements Optional {
    public final String zzaPU;
    public final int zzaPV;

    /* compiled from: Unknown */
    public static class Builder {
        private int zzaPV = 0;
        private String zzaPW;

        public PlacesOptions build() {
            return new PlacesOptions();
        }
    }

    private PlacesOptions(Builder builder) {
        this.zzaPU = builder.zzaPW;
        this.zzaPV = builder.zzaPV;
    }
}
