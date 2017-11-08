package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;

public final class ValidationProbeEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<ValidationProbeEvent> CREATOR = new Creator<ValidationProbeEvent>() {
        public ValidationProbeEvent createFromParcel(Parcel in) {
            return new ValidationProbeEvent(in);
        }

        public ValidationProbeEvent[] newArray(int size) {
            return new ValidationProbeEvent[size];
        }
    };
    public static final int DNS_FAILURE = 0;
    public static final int DNS_SUCCESS = 1;
    public static final int PROBE_DNS = 0;
    public static final int PROBE_HTTP = 1;
    public static final int PROBE_HTTPS = 2;
    public static final int PROBE_PAC = 3;
    public final long durationMs;
    public final int netId;
    public final int probeType;
    public final int returnCode;

    static final class Decoder {
        static final SparseArray<String> constants = MessageUtils.findMessageNames(new Class[]{ValidationProbeEvent.class}, new String[]{"PROBE_"});

        Decoder() {
        }
    }

    private ValidationProbeEvent(int netId, long durationMs, int probeType, int returnCode) {
        this.netId = netId;
        this.durationMs = durationMs;
        this.probeType = probeType;
        this.returnCode = returnCode;
    }

    private ValidationProbeEvent(Parcel in) {
        this.netId = in.readInt();
        this.durationMs = in.readLong();
        this.probeType = in.readInt();
        this.returnCode = in.readInt();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.netId);
        out.writeLong(this.durationMs);
        out.writeInt(this.probeType);
        out.writeInt(this.returnCode);
    }

    public int describeContents() {
        return 0;
    }

    public static String getProbeName(int probeType) {
        return (String) Decoder.constants.get(probeType, "PROBE_???");
    }

    public static void logEvent(int netId, long durationMs, int probeType, int returnCode) {
        IpConnectivityEvent.logEvent(new ValidationProbeEvent(netId, durationMs, probeType, returnCode));
    }

    public String toString() {
        return String.format("ValidationProbeEvent(%d, %s:%d, %dms)", new Object[]{Integer.valueOf(this.netId), getProbeName(this.probeType), Integer.valueOf(this.returnCode), Long.valueOf(this.durationMs)});
    }
}
