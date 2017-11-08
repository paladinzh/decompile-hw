package android.net.metrics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class DhcpClientEvent extends IpConnectivityEvent implements Parcelable {
    public static final Creator<DhcpClientEvent> CREATOR = new Creator<DhcpClientEvent>() {
        public DhcpClientEvent createFromParcel(Parcel in) {
            return new DhcpClientEvent(in);
        }

        public DhcpClientEvent[] newArray(int size) {
            return new DhcpClientEvent[size];
        }
    };
    public final String ifName;
    public final String msg;

    private DhcpClientEvent(String ifName, String msg) {
        this.ifName = ifName;
        this.msg = msg;
    }

    private DhcpClientEvent(Parcel in) {
        this.ifName = in.readString();
        this.msg = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.ifName);
        out.writeString(this.msg);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("DhcpClientEvent(%s, %s)", new Object[]{this.ifName, this.msg});
    }

    public static void logStateEvent(String ifName, String state) {
        IpConnectivityEvent.logEvent(new DhcpClientEvent(ifName, state));
    }
}
