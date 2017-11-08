package com.google.android.gms.location.places;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.location.places.internal.zzd;
import com.google.android.gms.location.places.internal.zze;
import com.google.android.gms.location.places.internal.zze.zza;
import com.google.android.gms.location.places.internal.zzj;
import com.google.android.gms.location.places.internal.zzk;

/* compiled from: Unknown */
public class Places {
    public static final Api<PlacesOptions> GEO_DATA_API = new Api("Places.GEO_DATA_API", new zza(null), zzaPN);
    public static final GeoDataApi GeoDataApi = new zzd();
    public static final Api<PlacesOptions> PLACE_DETECTION_API = new Api("Places.PLACE_DETECTION_API", new zzk.zza(null), zzaPO);
    public static final PlaceDetectionApi PlaceDetectionApi = new zzj();
    public static final zzc<zze> zzaPN = new zzc();
    public static final zzc<zzk> zzaPO = new zzc();

    private Places() {
    }
}
