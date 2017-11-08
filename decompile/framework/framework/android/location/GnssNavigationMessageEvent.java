package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.InvalidParameterException;

public final class GnssNavigationMessageEvent implements Parcelable {
    public static final Creator<GnssNavigationMessageEvent> CREATOR = new Creator<GnssNavigationMessageEvent>() {
        public GnssNavigationMessageEvent createFromParcel(Parcel in) {
            return new GnssNavigationMessageEvent((GnssNavigationMessage) in.readParcelable(getClass().getClassLoader()));
        }

        public GnssNavigationMessageEvent[] newArray(int size) {
            return new GnssNavigationMessageEvent[size];
        }
    };
    public static final int STATUS_GNSS_LOCATION_DISABLED = 2;
    public static final int STATUS_NOT_SUPPORTED = 0;
    public static final int STATUS_READY = 1;
    private final GnssNavigationMessage mNavigationMessage;

    public static abstract class Callback {
        public void onGnssNavigationMessageReceived(GnssNavigationMessageEvent event) {
        }

        public void onStatusChanged(int status) {
        }
    }

    public GnssNavigationMessageEvent(GnssNavigationMessage message) {
        if (message == null) {
            throw new InvalidParameterException("Parameter 'message' must not be null.");
        }
        this.mNavigationMessage = message;
    }

    public GnssNavigationMessage getNavigationMessage() {
        return this.mNavigationMessage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mNavigationMessage, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GnssNavigationMessageEvent:\n\n");
        builder.append(this.mNavigationMessage.toString());
        builder.append("\n]");
        return builder.toString();
    }
}
