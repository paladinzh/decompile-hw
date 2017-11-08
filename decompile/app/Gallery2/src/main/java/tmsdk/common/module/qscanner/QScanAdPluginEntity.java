package tmsdk.common.module.qscanner;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

/* compiled from: Unknown */
public class QScanAdPluginEntity implements Parcelable {
    public static final Creator<QScanAdPluginEntity> CREATOR = new Creator<QScanAdPluginEntity>() {
        public QScanAdPluginEntity[] bS(int i) {
            return new QScanAdPluginEntity[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return n(parcel);
        }

        public QScanAdPluginEntity n(Parcel parcel) {
            QScanAdPluginEntity qScanAdPluginEntity = new QScanAdPluginEntity();
            qScanAdPluginEntity.id = parcel.readInt();
            qScanAdPluginEntity.type = parcel.readInt();
            qScanAdPluginEntity.behaviors = parcel.readLong();
            qScanAdPluginEntity.banUrls = parcel.createStringArrayList();
            qScanAdPluginEntity.banIps = parcel.createStringArrayList();
            qScanAdPluginEntity.name = parcel.readString();
            return qScanAdPluginEntity;
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bS(i);
        }
    };
    public ArrayList<String> banIps = null;
    public ArrayList<String> banUrls = null;
    public long behaviors = 0;
    public int id = 0;
    public String name = null;
    public int type = 0;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.type);
        parcel.writeLong(this.behaviors);
        parcel.writeStringList(this.banUrls);
        parcel.writeStringList(this.banIps);
        parcel.writeString(this.name);
    }
}
