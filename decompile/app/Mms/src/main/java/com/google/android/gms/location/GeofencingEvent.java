package com.google.android.gms.location;

import android.content.Intent;
import android.location.Location;
import com.google.android.gms.location.internal.ParcelableGeofence;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class GeofencingEvent {
    private final int zzGu;
    private final int zzaNK;
    private final List<Geofence> zzaNL;
    private final Location zzaNM;

    private GeofencingEvent(int errorCode, int transitionType, List<Geofence> triggeringGeofences, Location triggeringLocaton) {
        this.zzGu = errorCode;
        this.zzaNK = transitionType;
        this.zzaNL = triggeringGeofences;
        this.zzaNM = triggeringLocaton;
    }

    public static GeofencingEvent fromIntent(Intent intent) {
        return intent != null ? new GeofencingEvent(intent.getIntExtra("gms_error_code", -1), zzx(intent), zzy(intent), (Location) intent.getParcelableExtra("com.google.android.location.intent.extra.triggering_location")) : null;
    }

    private static int zzx(Intent intent) {
        int intExtra = intent.getIntExtra("com.google.android.location.intent.extra.transition", -1);
        return intExtra != -1 ? (intExtra == 1 || intExtra == 2 || intExtra == 4) ? intExtra : -1 : -1;
    }

    private static List<Geofence> zzy(Intent intent) {
        ArrayList arrayList = (ArrayList) intent.getSerializableExtra("com.google.android.location.intent.extra.geofence_list");
        if (arrayList == null) {
            return null;
        }
        List arrayList2 = new ArrayList(arrayList.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add(ParcelableGeofence.zzo((byte[]) it.next()));
        }
        return arrayList2;
    }

    public int getErrorCode() {
        return this.zzGu;
    }

    public int getGeofenceTransition() {
        return this.zzaNK;
    }

    public List<Geofence> getTriggeringGeofences() {
        return this.zzaNL;
    }

    public Location getTriggeringLocation() {
        return this.zzaNM;
    }

    public boolean hasError() {
        return this.zzGu != -1;
    }
}
