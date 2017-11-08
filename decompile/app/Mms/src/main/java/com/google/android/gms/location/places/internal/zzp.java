package com.google.android.gms.location.places.internal;

import android.os.RemoteException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.zzf;
import com.google.android.gms.location.places.zzf.zza;

/* compiled from: Unknown */
public class zzp implements PlacePhotoMetadata {
    private int mIndex;
    private final int zzDF;
    private final int zzDG;
    private final String zzaQR;
    private final CharSequence zzaQS;

    public zzp(String str, int i, int i2, CharSequence charSequence, int i3) {
        this.zzaQR = str;
        this.zzDF = i;
        this.zzDG = i2;
        this.zzaQS = charSequence;
        this.mIndex = i3;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof zzp)) {
            return false;
        }
        zzp zzp = (zzp) other;
        if (zzp.zzDF == this.zzDF && zzp.zzDG == this.zzDG && zzw.equal(zzp.zzaQR, this.zzaQR)) {
            if (!zzw.equal(zzp.zzaQS, this.zzaQS)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public /* synthetic */ Object freeze() {
        return zzzz();
    }

    public CharSequence getAttributions() {
        return this.zzaQS;
    }

    public int getMaxHeight() {
        return this.zzDG;
    }

    public int getMaxWidth() {
        return this.zzDF;
    }

    public PendingResult<PlacePhotoResult> getPhoto(GoogleApiClient client) {
        return getScaledPhoto(client, getMaxWidth(), getMaxHeight());
    }

    public PendingResult<PlacePhotoResult> getScaledPhoto(GoogleApiClient client, int width, int height) {
        final int i = width;
        final int i2 = height;
        return client.zza(new zza<zze>(this, Places.zzaPN, client) {
            final /* synthetic */ zzp zzaQV;

            protected void zza(zze zze) throws RemoteException {
                zze.zza(new zzf((zza) this), this.zzaQV.zzaQR, i, i2, this.zzaQV.mIndex);
            }
        });
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.zzDF), Integer.valueOf(this.zzDG), this.zzaQR, this.zzaQS);
    }

    public boolean isDataValid() {
        return true;
    }

    public PlacePhotoMetadata zzzz() {
        return this;
    }
}
