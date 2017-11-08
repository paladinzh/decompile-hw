package android.rog;

import android.content.res.Configuration;
import android.net.ProxyInfo;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;

public class AppRogInfo implements Parcelable {
    public static final Creator<AppRogInfo> CREATOR = new Creator<AppRogInfo>() {
        public AppRogInfo createFromParcel(Parcel in) {
            return new AppRogInfo(in);
        }

        public AppRogInfo[] newArray(int size) {
            return new AppRogInfo[size];
        }
    };
    public static final int ROG_FEATURE_OFF = 0;
    public static final int ROG_POLICY_NORMAL = 2;
    public static final int ROG_POLICY_PERFORMANCE = 1;
    public static final int ROG_RESOLUTION_FHD = 3;
    public static final int ROG_RESOLUTION_HD = 4;
    public static final int ROG_RESOLUTION_QHD = 2;
    public static final int ROG_RESOLUTION_UHD = 1;
    private static final String TAG = "AppRogInfo";
    public String mPackageName = ProxyInfo.LOCAL_EXCL_LIST;
    public int mRogMode;
    public float mRogScale;
    public boolean mSupportHotSwitch;

    public static final class UpdateRog {
        public String packageName;
        public boolean rogEnable;
        public AppRogInfo rogInfo;
    }

    public AppRogInfo(Parcel parcel) {
        readFromParcel(parcel);
    }

    public AppRogInfo(AppRogInfo another) {
        this.mPackageName = another.mPackageName;
        this.mRogMode = another.mRogMode;
        this.mRogScale = another.mRogScale;
        this.mSupportHotSwitch = another.mSupportHotSwitch;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(this.mPackageName);
        dest.writeInt(this.mRogMode);
        dest.writeFloat(this.mRogScale);
        dest.writeInt(this.mSupportHotSwitch ? 1 : 0);
    }

    public void readFromParcel(Parcel in) {
        boolean z = false;
        this.mPackageName = in.readString();
        this.mRogMode = in.readInt();
        this.mRogScale = in.readFloat();
        if (in.readInt() > 0) {
            z = true;
        }
        this.mSupportHotSwitch = z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name:").append(this.mPackageName).append(",mode:").append(this.mRogMode).append(", scale:").append(this.mRogScale).append(", SupportHotSwitch:").append(this.mSupportHotSwitch);
        return sb.toString();
    }

    public float getRogAppSclae() {
        return this.mRogScale;
    }

    public boolean isRogEnable() {
        return this.mRogMode != 0;
    }

    public boolean isSupportHotSwitch() {
        return this.mSupportHotSwitch;
    }

    public boolean equals(Object another) {
        boolean z = false;
        if (!(another instanceof AppRogInfo)) {
            return false;
        }
        AppRogInfo anotherCopy = (AppRogInfo) another;
        if (anotherCopy.mPackageName.equalsIgnoreCase(this.mPackageName) && anotherCopy.mRogMode == this.mRogMode && Float.compare(anotherCopy.mRogScale, this.mRogScale) == 0) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return ((((this.mRogMode + 527) * 31) + Float.floatToIntBits(this.mRogScale)) * 31) + this.mPackageName.hashCode();
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm, boolean rogEnable) {
        if (Float.compare(this.mRogScale, 1.0f) != 0) {
            if (rogEnable) {
                inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                inoutDm.density = ((float) inoutDm.densityDpi) * 0.00625f;
                inoutDm.scaledDensity = inoutDm.noncompatScaledDensity / this.mRogScale;
            } else {
                inoutDm.densityDpi = inoutDm.noncompatDensityDpi;
                inoutDm.density = ((float) inoutDm.densityDpi) * 0.00625f;
                inoutDm.scaledDensity = inoutDm.noncompatScaledDensity;
            }
        }
    }

    public void applyToConfiguration(DisplayMetrics inoutDm, Configuration inoutConfig) {
        inoutConfig.screenLayout = (inoutConfig.screenLayout & -16) | 2;
        inoutConfig.densityDpi = inoutDm.densityDpi;
    }

    public void getRealSizeDisplayMetrics(DisplayMetrics inoutDm, boolean rogEnable) {
        if (Float.compare(this.mRogScale, 1.0f) != 0) {
            if (rogEnable) {
                inoutDm.xdpi = inoutDm.noncompatXdpi / this.mRogScale;
                inoutDm.ydpi = inoutDm.noncompatYdpi / this.mRogScale;
                inoutDm.widthPixels = (int) ((((float) inoutDm.noncompatWidthPixels) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                inoutDm.heightPixels = (int) ((((float) inoutDm.noncompatHeightPixels) / this.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            } else {
                inoutDm.xdpi = inoutDm.noncompatXdpi;
                inoutDm.ydpi = inoutDm.noncompatYdpi;
                inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
                inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
            }
        }
    }
}
