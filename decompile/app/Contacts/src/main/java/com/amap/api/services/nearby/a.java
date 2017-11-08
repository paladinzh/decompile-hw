package com.amap.api.services.nearby;

/* compiled from: NearbySearch */
/* synthetic */ class a {
    static final /* synthetic */ int[] a = new int[NearbySearchFunctionType.values().length];

    static {
        try {
            a[NearbySearchFunctionType.DISTANCE_SEARCH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[NearbySearchFunctionType.DRIVING_DISTANCE_SEARCH.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
    }
}
