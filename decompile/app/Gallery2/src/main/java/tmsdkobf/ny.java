package tmsdkobf;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsNeighborCell;

/* compiled from: Unknown */
public class ny {
    private PhoneStateListener DK;
    private a DL;
    private CellLocation DM;
    private int DN = -133;
    private int DO = -1;
    private int DP = -1;
    private TelephonyManager mTelephonyManager;

    /* compiled from: Unknown */
    public interface a {
        void a(BsInput bsInput);
    }

    public ny(nx nxVar) {
    }

    public void a(a aVar) {
        this.DL = aVar;
    }

    void fB() {
        if (this.DL != null) {
            this.DL.a(fC());
        }
    }

    public BsInput fC() {
        BsInput bsInput = new BsInput();
        bsInput.timeInSeconds = (int) (System.currentTimeMillis() / 1000);
        bsInput.networkType = (short) ((short) this.DO);
        bsInput.dataState = (short) ((short) this.DP);
        if (this.DM == null) {
            try {
                CellLocation cellLocation = this.mTelephonyManager.getCellLocation();
                if (cellLocation != null && (cellLocation instanceof GsmCellLocation)) {
                    this.DM = (GsmCellLocation) cellLocation;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        if (this.DM != null) {
            if (this.DM instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) this.DM;
                bsInput.cid = gsmCellLocation.getCid();
                bsInput.lac = gsmCellLocation.getLac();
            } else if (this.DM instanceof CdmaCellLocation) {
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) this.DM;
                bsInput.cid = cdmaCellLocation.getBaseStationId();
                bsInput.lac = cdmaCellLocation.getNetworkId();
                bsInput.loc = (((long) cdmaCellLocation.getBaseStationLatitude()) << 32) | ((long) cdmaCellLocation.getBaseStationLongitude());
            }
        }
        bsInput.bsss = (short) ((short) this.DN);
        try {
            String networkOperator = this.mTelephonyManager.getNetworkOperator();
            if (networkOperator != null) {
                if (networkOperator.length() >= 4) {
                    bsInput.mcc = (short) ((short) Integer.parseInt(networkOperator.substring(0, 3)));
                    bsInput.mnc = (short) ((short) Integer.parseInt(networkOperator.substring(3)));
                }
            }
        } catch (Throwable th2) {
            th2.printStackTrace();
        }
        List arrayList = new ArrayList();
        try {
            List<NeighboringCellInfo> neighboringCellInfo = this.mTelephonyManager.getNeighboringCellInfo();
            if (neighboringCellInfo != null) {
                for (NeighboringCellInfo neighboringCellInfo2 : neighboringCellInfo) {
                    BsNeighborCell bsNeighborCell = new BsNeighborCell();
                    bsNeighborCell.cid = neighboringCellInfo2.getCid();
                    bsNeighborCell.lac = neighboringCellInfo2.getLac();
                    bsNeighborCell.bsss = (short) ((short) ((neighboringCellInfo2.getRssi() * 2) - 113));
                    arrayList.add(bsNeighborCell);
                }
            }
        } catch (Throwable th22) {
            th22.printStackTrace();
        }
        bsInput.neighbors = arrayList;
        return bsInput;
    }

    public void r(Context context) {
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.DK = new PhoneStateListener(this) {
            final /* synthetic */ ny DQ;

            {
                this.DQ = r1;
            }

            private String bP(int i) {
                String str = "null";
                switch (i) {
                    case 0:
                        str = "DATA_DISCONNECTED";
                        break;
                    case 1:
                        str = "DATA_CONNECTING";
                        break;
                    case 2:
                        str = "DATA_CONNECTED";
                        break;
                    case 3:
                        str = "DATA_SUSPENDED";
                        break;
                    default:
                        str = "DATA_OTHER";
                        break;
                }
                return str + "(" + i + ")";
            }

            private String bQ(int i) {
                String str = "null";
                switch (i) {
                    case 0:
                        str = "NETWORK_TYPE_UNKNOWN";
                        break;
                    case 1:
                        str = "NETWORK_TYPE_GPRS";
                        break;
                    case 2:
                        str = "NETWORK_TYPE_EDGE";
                        break;
                    case 3:
                        str = "NETWORK_TYPE_UMTS";
                        break;
                    case 4:
                        str = "NETWORK_TYPE_CDMA";
                        break;
                    case 5:
                        str = "NETWORK_TYPE_EVDO_0";
                        break;
                    case 6:
                        str = "NETWORK_TYPE_EVDO_A";
                        break;
                    case 7:
                        str = "NETWORK_TYPE_1xRTT";
                        break;
                    case 8:
                        str = "NETWORK_TYPE_HSDPA";
                        break;
                    case 9:
                        str = "NETWORK_TYPE_HSUPA";
                        break;
                    case 10:
                        str = "NETWORK_TYPE_HSPA";
                        break;
                    default:
                        str = "NETWORK_TYPE_OTHER--" + i;
                        break;
                }
                return str + "(" + i + ")";
            }

            public void onCellLocationChanged(CellLocation cellLocation) {
                super.onCellLocationChanged(cellLocation);
                if (cellLocation != null) {
                    try {
                        List<NeighboringCellInfo> neighboringCellInfo = this.DQ.mTelephonyManager.getNeighboringCellInfo();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("----------\n");
                        if (neighboringCellInfo != null) {
                            for (NeighboringCellInfo neighboringCellInfo2 : neighboringCellInfo) {
                                stringBuilder.append("lac=").append(neighboringCellInfo2.getLac()).append(",");
                                stringBuilder.append("cid=").append(neighboringCellInfo2.getCid()).append(",");
                                stringBuilder.append("networkType=").append(bQ(neighboringCellInfo2.getNetworkType())).append(",");
                                stringBuilder.append("bsss=").append((neighboringCellInfo2.getRssi() * 2) - 113).append("\n");
                            }
                        }
                        stringBuilder.append("----------\n");
                    } catch (Exception e) {
                    }
                    this.DQ.DM = cellLocation;
                    this.DQ.fB();
                }
            }

            public void onDataConnectionStateChanged(int i, int i2) {
                super.onDataConnectionStateChanged(i, i2);
                bP(i);
                bQ(i2);
                this.DQ.DO = i2;
                this.DQ.DP = i;
                this.DQ.fB();
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                if (signalStrength != null) {
                    this.DQ.DN = !signalStrength.isGsm() ? signalStrength.getCdmaDbm() : (signalStrength.getGsmSignalStrength() * 2) - 113;
                    if (this.DQ.DN == 1) {
                        return;
                    }
                }
                this.DQ.fB();
            }
        };
        this.mTelephonyManager.listen(this.DK, 336);
    }

    public void s(Context context) {
        this.mTelephonyManager.listen(this.DK, 0);
    }
}
