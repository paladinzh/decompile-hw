package tmsdk.common.module.qscanner;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public class QScanAdBehaviorInfo implements Parcelable {
    public static final Creator<QScanAdBehaviorInfo> CREATOR = new Creator<QScanAdBehaviorInfo>() {
        public QScanAdBehaviorInfo[] bS(int i) {
            return new QScanAdBehaviorInfo[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return m(parcel);
        }

        public QScanAdBehaviorInfo m(Parcel parcel) {
            QScanAdBehaviorInfo qScanAdBehaviorInfo = new QScanAdBehaviorInfo();
            qScanAdBehaviorInfo.behavior = parcel.readLong();
            qScanAdBehaviorInfo.description = parcel.readString();
            qScanAdBehaviorInfo.damage = parcel.readString();
            qScanAdBehaviorInfo.level = parcel.readString();
            return qScanAdBehaviorInfo;
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bS(i);
        }
    };
    public long behavior;
    public String damage;
    public String description;
    public String level;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.behavior);
        parcel.writeString(this.description);
        parcel.writeString(this.damage);
        parcel.writeString(this.level);
    }
}
