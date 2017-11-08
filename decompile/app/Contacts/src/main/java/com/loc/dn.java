package com.loc;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;

/* compiled from: Unknown */
final class dn extends PhoneStateListener {
    private /* synthetic */ dl a;

    private dn(dl dlVar) {
        this.a = dlVar;
    }

    public final void onCellLocationChanged(CellLocation cellLocation) {
        try {
            this.a.y = System.currentTimeMillis();
            this.a.C = cellLocation;
            super.onCellLocationChanged(cellLocation);
        } catch (Exception e) {
        }
    }

    public final void onServiceStateChanged(ServiceState serviceState) {
        try {
            if (serviceState.getState() != 0) {
                this.a.p = false;
            } else {
                this.a.p = true;
                String[] a = dl.b(this.a.g);
                this.a.t = Integer.parseInt(a[0]);
                this.a.u = Integer.parseInt(a[1]);
            }
            super.onServiceStateChanged(serviceState);
        } catch (Exception e) {
        }
    }

    public final void onSignalStrengthsChanged(SignalStrength signalStrength) {
        try {
            if (this.a.n) {
                this.a.o = signalStrength.getCdmaDbm();
            } else {
                this.a.o = signalStrength.getGsmSignalStrength();
                if (this.a.o != 99) {
                    this.a.o = (this.a.o * 2) - 113;
                } else {
                    this.a.o = -1;
                }
            }
            super.onSignalStrengthsChanged(signalStrength);
        } catch (Exception e) {
        }
    }
}
