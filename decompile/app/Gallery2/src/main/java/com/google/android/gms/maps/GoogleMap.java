package com.google.android.gms.maps;

import android.os.RemoteException;
import com.google.android.gms.internal.er;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.internal.e.a;
import com.google.android.gms.maps.internal.i;
import com.google.android.gms.maps.internal.k;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.maps.model.internal.d;

/* compiled from: Unknown */
public final class GoogleMap {
    private final IGoogleMapDelegate OK;
    private UiSettings OL;

    /* compiled from: Unknown */
    public interface OnCameraChangeListener {
        void onCameraChange(CameraPosition cameraPosition);
    }

    /* compiled from: Unknown */
    public interface OnMapLoadedCallback {
        void onMapLoaded();
    }

    /* compiled from: Unknown */
    public interface OnMarkerClickListener {
        boolean onMarkerClick(Marker marker);
    }

    protected GoogleMap(IGoogleMapDelegate map) {
        this.OK = (IGoogleMapDelegate) er.f(map);
    }

    public final Marker addMarker(MarkerOptions options) {
        try {
            d addMarker = this.OK.addMarker(options);
            return addMarker == null ? null : new Marker(addMarker);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final void animateCamera(CameraUpdate update) {
        try {
            this.OK.animateCamera(update.gK());
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final void clear() {
        try {
            this.OK.clear();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final CameraPosition getCameraPosition() {
        try {
            return this.OK.getCameraPosition();
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final Projection getProjection() {
        try {
            return new Projection(this.OK.getProjection());
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final UiSettings getUiSettings() {
        try {
            if (this.OL == null) {
                this.OL = new UiSettings(this.OK.getUiSettings());
            }
            return this.OL;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final void moveCamera(CameraUpdate update) {
        try {
            this.OK.moveCamera(update.gK());
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public final void setOnCameraChangeListener(final OnCameraChangeListener listener) {
        if (listener != null) {
            try {
                this.OK.setOnCameraChangeListener(new a(this) {
                    final /* synthetic */ GoogleMap ON;

                    public void onCameraChange(CameraPosition position) {
                        listener.onCameraChange(position);
                    }
                });
                return;
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
        this.OK.setOnCameraChangeListener(null);
    }

    public void setOnMapLoadedCallback(final OnMapLoadedCallback callback) {
        if (callback != null) {
            try {
                this.OK.setOnMapLoadedCallback(new i.a(this) {
                    final /* synthetic */ GoogleMap ON;

                    public void onMapLoaded() throws RemoteException {
                        callback.onMapLoaded();
                    }
                });
                return;
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
        this.OK.setOnMapLoadedCallback(null);
    }

    public final void setOnMarkerClickListener(final OnMarkerClickListener listener) {
        if (listener != null) {
            try {
                this.OK.setOnMarkerClickListener(new k.a(this) {
                    final /* synthetic */ GoogleMap ON;

                    public boolean a(d dVar) {
                        return listener.onMarkerClick(new Marker(dVar));
                    }
                });
                return;
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }
        this.OK.setOnMarkerClickListener(null);
    }
}
