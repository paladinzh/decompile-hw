package com.google.android.gms.location.places.internal;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.internal.PlaceImpl.zza;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/* compiled from: Unknown */
public class zzr extends zzt implements Place {
    private final String zzaPH = zzG("place_id", "");

    public zzr(DataHolder dataHolder, int i, Context context) {
        super(dataHolder, i);
    }

    private PlaceImpl zzzA() {
        PlaceImpl zzzx = new zza().zzeo(getAddress().toString()).zzy(zzzq()).zzem(getId()).zzan(zzzr()).zza(getLatLng()).zzf(zzzo()).zzen(getName().toString()).zzep(getPhoneNumber().toString()).zzhX(getPriceLevel()).zzg(getRating()).zzx(getPlaceTypes()).zza(getViewport()).zzo(getWebsiteUri()).zzzx();
        zzzx.setLocale(getLocale());
        return zzzx;
    }

    private List<String> zzzq() {
        return zzb("place_attributions", Collections.emptyList());
    }

    public /* synthetic */ Object freeze() {
        return zzzw();
    }

    public CharSequence getAddress() {
        return zzG("place_address", "");
    }

    public CharSequence getAttributions() {
        return zzc.zzj(zzzq());
    }

    public String getId() {
        return this.zzaPH;
    }

    public LatLng getLatLng() {
        return (LatLng) zza("place_lat_lng", LatLng.CREATOR);
    }

    public Locale getLocale() {
        Object zzG = zzG("place_locale", "");
        return TextUtils.isEmpty(zzG) ? Locale.getDefault() : new Locale(zzG);
    }

    public CharSequence getName() {
        return zzG("place_name", "");
    }

    public CharSequence getPhoneNumber() {
        return zzG("place_phone_number", "");
    }

    public List<Integer> getPlaceTypes() {
        return zza("place_types", Collections.emptyList());
    }

    public int getPriceLevel() {
        return zzz("place_price_level", -1);
    }

    public float getRating() {
        return zzb("place_rating", -1.0f);
    }

    public LatLngBounds getViewport() {
        return (LatLngBounds) zza("place_viewport", LatLngBounds.CREATOR);
    }

    public Uri getWebsiteUri() {
        String zzG = zzG("place_website_uri", null);
        return zzG != null ? Uri.parse(zzG) : null;
    }

    public float zzzo() {
        return zzb("place_level_number", 0.0f);
    }

    public boolean zzzr() {
        return zzl("place_is_permanently_closed", false);
    }

    public Place zzzw() {
        return zzzA();
    }
}
