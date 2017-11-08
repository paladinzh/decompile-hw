package com.google.android.gms.cast;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.images.WebImage;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.dr;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class CastDevice implements SafeParcelable {
    public static final Creator<CastDevice> CREATOR = new b();
    private String wC;
    String wD;
    private Inet4Address wE;
    private String wF;
    private String wG;
    private String wH;
    private int wI;
    private List<WebImage> wJ;
    private final int wj;

    private CastDevice() {
        this(1, null, null, null, null, null, -1, new ArrayList());
    }

    CastDevice(int versionCode, String deviceId, String hostAddress, String friendlyName, String modelName, String deviceVersion, int servicePort, List<WebImage> icons) {
        this.wj = versionCode;
        this.wC = deviceId;
        this.wD = hostAddress;
        if (this.wD != null) {
            try {
                InetAddress byName = InetAddress.getByName(this.wD);
                if (byName instanceof Inet4Address) {
                    this.wE = (Inet4Address) byName;
                }
            } catch (UnknownHostException e) {
                this.wE = null;
            }
        }
        this.wF = friendlyName;
        this.wG = modelName;
        this.wH = deviceVersion;
        this.wI = servicePort;
        this.wJ = icons;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CastDevice)) {
            return false;
        }
        CastDevice castDevice = (CastDevice) obj;
        if (getDeviceId() == null) {
            return castDevice.getDeviceId() == null;
        } else {
            if (dr.a(this.wC, castDevice.wC) && dr.a(this.wE, castDevice.wE) && dr.a(this.wG, castDevice.wG) && dr.a(this.wF, castDevice.wF) && dr.a(this.wH, castDevice.wH) && this.wI == castDevice.wI) {
                if (!dr.a(this.wJ, castDevice.wJ)) {
                }
                return z;
            }
            z = false;
            return z;
        }
    }

    public String getDeviceId() {
        return this.wC;
    }

    public String getDeviceVersion() {
        return this.wH;
    }

    public String getFriendlyName() {
        return this.wF;
    }

    public List<WebImage> getIcons() {
        return Collections.unmodifiableList(this.wJ);
    }

    public String getModelName() {
        return this.wG;
    }

    public int getServicePort() {
        return this.wI;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return this.wC != null ? this.wC.hashCode() : 0;
    }

    public String toString() {
        return String.format("\"%s\" (%s)", new Object[]{this.wF, this.wC});
    }

    public void writeToParcel(Parcel out, int flags) {
        b.a(this, out, flags);
    }
}
