package com.loc;

import android.telephony.CellLocation;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

/* compiled from: Unknown */
public final class eb {
    int a = Integer.MAX_VALUE;
    int b = Integer.MAX_VALUE;
    int c = Integer.MAX_VALUE;
    int d = Integer.MAX_VALUE;
    int e = Integer.MAX_VALUE;

    eb(CellLocation cellLocation) {
        if (cellLocation != null) {
            if (cellLocation instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                this.e = gsmCellLocation.getCid();
                this.d = gsmCellLocation.getLac();
            } else if (cellLocation instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                this.c = cdmaCellLocation.getBaseStationId();
                this.b = cdmaCellLocation.getNetworkId();
                this.a = cdmaCellLocation.getSystemId();
            }
        }
    }
}
