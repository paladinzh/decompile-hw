package com.google.android.gms.maps;

import android.os.RemoteException;
import com.google.android.gms.maps.internal.IUiSettingsDelegate;
import com.google.android.gms.maps.model.RuntimeRemoteException;

/* compiled from: Unknown */
public final class UiSettings {
    private final IUiSettingsDelegate PA;

    UiSettings(IUiSettingsDelegate delegate) {
        this.PA = delegate;
    }

    public void setCompassEnabled(boolean enabled) {
        try {
            this.PA.setCompassEnabled(enabled);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void setZoomControlsEnabled(boolean enabled) {
        try {
            this.PA.setZoomControlsEnabled(enabled);
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }
}
