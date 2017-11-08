package tmsdk.common.module.network;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

/* compiled from: Unknown */
public final class TrafficEntity implements Parcelable {
    public static Creator<TrafficEntity> CREATOR = new Creator<TrafficEntity>() {
        public TrafficEntity[] bP(int i) {
            return new TrafficEntity[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return l(parcel);
        }

        public TrafficEntity l(Parcel parcel) {
            TrafficEntity trafficEntity = new TrafficEntity();
            trafficEntity.mPkg = parcel.readString();
            trafficEntity.mLastUpValue = parcel.readLong();
            trafficEntity.mLastDownValue = parcel.readLong();
            trafficEntity.mMobileUpValue = parcel.readLong();
            trafficEntity.mMobileDownValue = parcel.readLong();
            trafficEntity.mWIFIUpValue = parcel.readLong();
            trafficEntity.mWIFIDownValue = parcel.readLong();
            return trafficEntity;
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bP(i);
        }
    };
    public long mLastDownValue = 0;
    public long mLastUpValue = 0;
    public long mMobileDownValue = 0;
    public long mMobileUpValue = 0;
    public String mPkg;
    public long mWIFIDownValue = 0;
    public long mWIFIUpValue = 0;

    public static TrafficEntity fromString(String str) {
        TrafficEntity trafficEntity = null;
        if (!TextUtils.isEmpty(str)) {
            TrafficEntity trafficEntity2 = new TrafficEntity();
            String[] split = str.trim().split("[,:]");
            try {
                trafficEntity2.mPkg = split[0];
                trafficEntity2.mLastUpValue = Long.parseLong(split[1]);
                trafficEntity2.mLastDownValue = Long.parseLong(split[2]);
                trafficEntity2.mMobileUpValue = Long.parseLong(split[3]);
                trafficEntity2.mMobileDownValue = Long.parseLong(split[4]);
                trafficEntity2.mWIFIUpValue = Long.parseLong(split[5]);
                trafficEntity2.mWIFIDownValue = Long.parseLong(split[6]);
                trafficEntity = trafficEntity2;
            } catch (NumberFormatException e) {
                return null;
            } catch (ArrayIndexOutOfBoundsException e2) {
                return null;
            } catch (Exception e3) {
                return null;
            }
        }
        return trafficEntity;
    }

    public static String toString(TrafficEntity trafficEntity) {
        return String.format("%s,%s,%s,%s,%s,%s,%s", new Object[]{trafficEntity.mPkg, Long.valueOf(trafficEntity.mLastUpValue), Long.valueOf(trafficEntity.mLastDownValue), Long.valueOf(trafficEntity.mMobileUpValue), Long.valueOf(trafficEntity.mMobileDownValue), Long.valueOf(trafficEntity.mWIFIUpValue), Long.valueOf(trafficEntity.mWIFIDownValue)});
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return toString(this);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mPkg);
        parcel.writeLong(this.mLastUpValue);
        parcel.writeLong(this.mLastDownValue);
        parcel.writeLong(this.mMobileUpValue);
        parcel.writeLong(this.mMobileDownValue);
        parcel.writeLong(this.mWIFIUpValue);
        parcel.writeLong(this.mWIFIDownValue);
    }
}
