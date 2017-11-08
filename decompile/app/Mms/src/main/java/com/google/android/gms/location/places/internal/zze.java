package com.google.android.gms.location.places.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.PlacesOptions;
import com.google.android.gms.location.places.PlacesOptions.Builder;
import com.google.android.gms.location.places.zzl;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;
import java.util.Locale;

/* compiled from: Unknown */
public class zze extends zzj<zzg> {
    private final PlacesParams zzaQq;
    private final Locale zzaQr = Locale.getDefault();

    /* compiled from: Unknown */
    public static class zza extends com.google.android.gms.common.api.Api.zza<zze, PlacesOptions> {
        private final String zzaQs;

        public zza(String str) {
            this.zzaQs = str;
        }

        public zze zza(Context context, Looper looper, zzf zzf, PlacesOptions placesOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zze(context, looper, zzf, connectionCallbacks, onConnectionFailedListener, this.zzaQs == null ? context.getPackageName() : this.zzaQs, placesOptions != null ? placesOptions : new Builder().build());
        }
    }

    public zze(Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, String str, PlacesOptions placesOptions) {
        super(context, looper, 65, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzaQq = new PlacesParams(str, this.zzaQr, zzf.getAccount() == null ? null : zzf.getAccount().name, placesOptions.zzaPU, placesOptions.zzaPV);
    }

    protected /* synthetic */ IInterface zzW(IBinder iBinder) {
        return zzcl(iBinder);
    }

    public void zza(com.google.android.gms.location.places.zzf zzf, String str) throws RemoteException {
        zzx.zzb((Object) str, (Object) "placeId cannot be null");
        ((zzg) zzqJ()).zza(str, this.zzaQq, (zzh) zzf);
    }

    public void zza(com.google.android.gms.location.places.zzf zzf, String str, int i, int i2, int i3) throws RemoteException {
        boolean z = false;
        zzx.zzb((Object) str, (Object) "fifeUrl cannot be null");
        zzx.zzb(i > 0, (Object) "width should be > 0");
        if (i > 0) {
            z = true;
        }
        zzx.zzb(z, (Object) "height should be > 0");
        ((zzg) zzqJ()).zza(str, i, i2, i3, this.zzaQq, (zzh) zzf);
    }

    public void zza(zzl zzl, AddPlaceRequest addPlaceRequest) throws RemoteException {
        zzx.zzb((Object) addPlaceRequest, (Object) "userAddedPlace == null");
        ((zzg) zzqJ()).zza(addPlaceRequest, this.zzaQq, (zzi) zzl);
    }

    public void zza(zzl zzl, String str, @Nullable LatLngBounds latLngBounds, @Nullable AutocompleteFilter autocompleteFilter) throws RemoteException {
        zzx.zzb((Object) zzl, (Object) "callback == null");
        ((zzg) zzqJ()).zza(str != null ? str : "", latLngBounds, autocompleteFilter != null ? autocompleteFilter : AutocompleteFilter.create(null), this.zzaQq, (zzi) zzl);
    }

    public void zza(zzl zzl, List<String> list) throws RemoteException {
        ((zzg) zzqJ()).zzb((List) list, this.zzaQq, (zzi) zzl);
    }

    protected zzg zzcl(IBinder iBinder) {
        return com.google.android.gms.location.places.internal.zzg.zza.zzcn(iBinder);
    }

    protected String zzgu() {
        return "com.google.android.gms.location.places.GeoDataApi";
    }

    protected String zzgv() {
        return "com.google.android.gms.location.places.internal.IGooglePlacesService";
    }
}
