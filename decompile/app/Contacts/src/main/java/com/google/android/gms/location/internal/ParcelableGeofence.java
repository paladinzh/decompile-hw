package com.google.android.gms.location.internal;

import android.annotation.SuppressLint;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.location.Geofence;
import java.util.Locale;

/* compiled from: Unknown */
public class ParcelableGeofence implements SafeParcelable, Geofence {
    public static final zzo CREATOR = new zzo();
    private final int mVersionCode;
    private final String zzEY;
    private final int zzaNC;
    private final short zzaNE;
    private final double zzaNF;
    private final double zzaNG;
    private final float zzaNH;
    private final int zzaNI;
    private final int zzaNJ;
    private final long zzaOZ;

    public ParcelableGeofence(int version, String requestId, int transitionTypes, short type, double latitude, double longitude, float radius, long expireAt, int notificationResponsiveness, int loiteringDelayMillis) {
        zzek(requestId);
        zze(radius);
        zza(latitude, longitude);
        transitionTypes = zzhF(transitionTypes);
        this.mVersionCode = version;
        this.zzaNE = (short) type;
        this.zzEY = requestId;
        this.zzaNF = latitude;
        this.zzaNG = longitude;
        this.zzaNH = radius;
        this.zzaOZ = expireAt;
        this.zzaNC = transitionTypes;
        this.zzaNI = notificationResponsiveness;
        this.zzaNJ = loiteringDelayMillis;
    }

    public ParcelableGeofence(String requestId, int transitionTypes, short type, double latitude, double longitude, float radius, long expireAt, int notificationResponsiveness, int loiteringDelayMillis) {
        this(1, requestId, transitionTypes, type, latitude, longitude, radius, expireAt, notificationResponsiveness, loiteringDelayMillis);
    }

    private static void zza(double d, double d2) {
        Object obj = 1;
        if ((d > 90.0d ? 1 : null) != null || d < -90.0d) {
            throw new IllegalArgumentException("invalid latitude: " + d);
        }
        if (d2 <= 180.0d) {
            obj = null;
        }
        if (obj != null || d2 < -180.0d) {
            throw new IllegalArgumentException("invalid longitude: " + d2);
        }
    }

    private static void zze(float f) {
        if (f <= 0.0f) {
            throw new IllegalArgumentException("invalid radius: " + f);
        }
    }

    private static void zzek(String str) {
        if (str == null || str.length() > 100) {
            throw new IllegalArgumentException("requestId is null or too long: " + str);
        }
    }

    private static int zzhF(int i) {
        int i2 = i & 7;
        if (i2 != 0) {
            return i2;
        }
        throw new IllegalArgumentException("No supported transition specified: " + i);
    }

    @SuppressLint({"DefaultLocale"})
    private static String zzhG(int i) {
        switch (i) {
            case 1:
                return "CIRCLE";
            default:
                return null;
        }
    }

    public static ParcelableGeofence zzo(byte[] bArr) {
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(bArr, 0, bArr.length);
        obtain.setDataPosition(0);
        ParcelableGeofence zzeZ = CREATOR.zzeZ(obtain);
        obtain.recycle();
        return zzeZ;
    }

    public int describeContents() {
        zzo zzo = CREATOR;
        return 0;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ParcelableGeofence)) {
            return false;
        }
        ParcelableGeofence parcelableGeofence = (ParcelableGeofence) obj;
        return this.zzaNH == parcelableGeofence.zzaNH && this.zzaNF == parcelableGeofence.zzaNF && this.zzaNG == parcelableGeofence.zzaNG && this.zzaNE == parcelableGeofence.zzaNE;
    }

    public long getExpirationTime() {
        return this.zzaOZ;
    }

    public double getLatitude() {
        return this.zzaNF;
    }

    public double getLongitude() {
        return this.zzaNG;
    }

    public int getNotificationResponsiveness() {
        return this.zzaNI;
    }

    public String getRequestId() {
        return this.zzEY;
    }

    public int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        long doubleToLongBits = Double.doubleToLongBits(this.zzaNF);
        int i = ((int) (doubleToLongBits ^ (doubleToLongBits >>> 32))) + 31;
        long doubleToLongBits2 = Double.doubleToLongBits(this.zzaNG);
        return (((((((i * 31) + ((int) (doubleToLongBits2 ^ (doubleToLongBits2 >>> 32)))) * 31) + Float.floatToIntBits(this.zzaNH)) * 31) + this.zzaNE) * 31) + this.zzaNC;
    }

    public String toString() {
        return String.format(Locale.US, "Geofence[%s id:%s transitions:%d %.6f, %.6f %.0fm, resp=%ds, dwell=%dms, @%d]", new Object[]{zzhG(this.zzaNE), this.zzEY, Integer.valueOf(this.zzaNC), Double.valueOf(this.zzaNF), Double.valueOf(this.zzaNG), Float.valueOf(this.zzaNH), Integer.valueOf(this.zzaNI / 1000), Integer.valueOf(this.zzaNJ), Long.valueOf(this.zzaOZ)});
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzo zzo = CREATOR;
        zzo.zza(this, parcel, flags);
    }

    public short zzyT() {
        return this.zzaNE;
    }

    public float zzyU() {
        return this.zzaNH;
    }

    public int zzyV() {
        return this.zzaNC;
    }

    public int zzyW() {
        return this.zzaNJ;
    }
}
