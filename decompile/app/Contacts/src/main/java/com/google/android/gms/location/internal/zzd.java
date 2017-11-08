package com.google.android.gms.location.internal;

import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;
import android.os.RemoteException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/* compiled from: Unknown */
public class zzd implements FusedLocationProviderApi {

    /* compiled from: Unknown */
    private static abstract class zza extends com.google.android.gms.location.LocationServices.zza<Status> {
        public zza(GoogleApiClient googleApiClient) {
            super(googleApiClient);
        }

        public Status zzb(Status status) {
            return status;
        }

        public /* synthetic */ Result zzc(Status status) {
            return zzb(status);
        }
    }

    /* compiled from: Unknown */
    private static class zzb extends com.google.android.gms.location.internal.zzg.zza {
        private final com.google.android.gms.common.api.internal.zza.zzb<Status> zzamC;

        public zzb(com.google.android.gms.common.api.internal.zza.zzb<Status> zzb) {
            this.zzamC = zzb;
        }

        public void zza(FusedLocationProviderResult fusedLocationProviderResult) {
            this.zzamC.zzs(fusedLocationProviderResult.getStatus());
        }
    }

    public PendingResult<Status> flushLocations(GoogleApiClient client) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(new zzb(this));
            }
        });
    }

    public Location getLastLocation(GoogleApiClient client) {
        try {
            return LocationServices.zzi(client).getLastLocation();
        } catch (Exception e) {
            return null;
        }
    }

    public LocationAvailability getLocationAvailability(GoogleApiClient client) {
        try {
            return LocationServices.zzi(client).zzyO();
        } catch (Exception e) {
            return null;
        }
    }

    public PendingResult<Status> removeLocationUpdates(GoogleApiClient client, final PendingIntent callbackIntent) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(callbackIntent, new zzb(this));
            }
        });
    }

    public PendingResult<Status> removeLocationUpdates(GoogleApiClient client, final LocationCallback callback) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(callback, new zzb(this));
            }
        });
    }

    public PendingResult<Status> removeLocationUpdates(GoogleApiClient client, final LocationListener listener) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(listener, new zzb(this));
            }
        });
    }

    public PendingResult<Status> requestLocationUpdates(GoogleApiClient client, final LocationRequest request, final PendingIntent callbackIntent) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(request, callbackIntent, new zzb(this));
            }
        });
    }

    public PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationCallback callback, Looper looper) {
        final LocationRequest locationRequest = request;
        final LocationCallback locationCallback = callback;
        final Looper looper2 = looper;
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(LocationRequestInternal.zzb(locationRequest), locationCallback, looper2, new zzb(this));
            }
        });
    }

    public PendingResult<Status> requestLocationUpdates(GoogleApiClient client, final LocationRequest request, final LocationListener listener) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(request, listener, null, new zzb(this));
            }
        });
    }

    public PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener, Looper looper) {
        final LocationRequest locationRequest = request;
        final LocationListener locationListener = listener;
        final Looper looper2 = looper;
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zza(locationRequest, locationListener, looper2, new zzb(this));
            }
        });
    }

    public PendingResult<Status> setMockLocation(GoogleApiClient client, final Location mockLocation) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zzc(mockLocation);
                zza(Status.zzagC);
            }
        });
    }

    public PendingResult<Status> setMockMode(GoogleApiClient client, final boolean isMockMode) {
        return client.zzb(new zza(this, client) {
            final /* synthetic */ zzd zzaOx;

            protected void zza(zzl zzl) throws RemoteException {
                zzl.zzam(isMockMode);
                zza(Status.zzagC);
            }
        });
    }
}
