package com.google.android.gms.common;

import android.content.Context;
import android.content.Intent;

/* compiled from: Unknown */
public class GoogleApiAvailability {
    public static final int GOOGLE_PLAY_SERVICES_VERSION_CODE = GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE;
    private static final GoogleApiAvailability zzYk = new GoogleApiAvailability();

    GoogleApiAvailability() {
    }

    public static GoogleApiAvailability getInstance() {
        return zzYk;
    }

    public int isGooglePlayServicesAvailable(Context context) {
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        return !GooglePlayServicesUtil.zzd(context, isGooglePlayServicesAvailable) ? isGooglePlayServicesAvailable : 18;
    }

    public void zzac(Context context) {
        GooglePlayServicesUtil.zzac(context);
    }

    public Intent zzbb(int i) {
        return GooglePlayServicesUtil.zzbc(i);
    }

    public boolean zzd(Context context, int i) {
        return GooglePlayServicesUtil.zzd(context, i);
    }
}
