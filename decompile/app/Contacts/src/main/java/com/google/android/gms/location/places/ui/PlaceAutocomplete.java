package com.google.android.gms.location.places.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLngBounds;

/* compiled from: Unknown */
public class PlaceAutocomplete extends zza {
    public static final int MODE_FULLSCREEN = 1;
    public static final int MODE_OVERLAY = 2;
    public static final int RESULT_ERROR = 2;

    /* compiled from: Unknown */
    public static class IntentBuilder extends zza {
        public IntentBuilder(int mode) {
            super("com.google.android.gms.location.places.ui.AUTOCOMPLETE");
            this.mIntent.putExtra("gmscore_client_jar_version", GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE);
            this.mIntent.putExtra("mode", mode);
            this.mIntent.putExtra("origin", 2);
        }

        public Intent build(Activity activity) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
            return super.build(activity);
        }

        public IntentBuilder setBoundsBias(@Nullable LatLngBounds bounds) {
            if (bounds == null) {
                this.mIntent.removeExtra("bounds");
            } else {
                this.mIntent.putExtra("bounds", bounds);
            }
            return this;
        }

        public IntentBuilder setFilter(@Nullable AutocompleteFilter filter) {
            if (filter == null) {
                this.mIntent.removeExtra("filter");
            } else {
                this.mIntent.putExtra("filter", filter);
            }
            return this;
        }

        public IntentBuilder zzeq(@Nullable String str) {
            if (str == null) {
                this.mIntent.removeExtra("initial_query");
            } else {
                this.mIntent.putExtra("initial_query", str);
            }
            return this;
        }

        public IntentBuilder zzig(int i) {
            this.mIntent.putExtra("origin", i);
            return this;
        }
    }

    private PlaceAutocomplete() {
    }

    public static Place getPlace(Context context, Intent intent) {
        return zza.getPlace(context, intent);
    }

    public static Status getStatus(Context context, Intent intent) {
        return zza.getStatus(context, intent);
    }
}
