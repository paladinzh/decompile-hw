package tmsdk.common.module.qscanner;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class QScanResultEntity implements Parcelable, Serializable {
    public static final Creator<QScanResultEntity> CREATOR = new Creator<QScanResultEntity>() {
        public QScanResultEntity[] bU(int i) {
            return new QScanResultEntity[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return o(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return bU(i);
        }

        public QScanResultEntity o(Parcel parcel) {
            boolean z = false;
            QScanResultEntity qScanResultEntity = new QScanResultEntity();
            qScanResultEntity.packageName = parcel.readString();
            qScanResultEntity.softName = parcel.readString();
            qScanResultEntity.version = parcel.readString();
            qScanResultEntity.versionCode = parcel.readInt();
            qScanResultEntity.path = parcel.readString();
            qScanResultEntity.apkType = parcel.readInt();
            qScanResultEntity.certMd5 = parcel.readString();
            qScanResultEntity.size = parcel.readInt();
            qScanResultEntity.dexSha1 = parcel.readString();
            qScanResultEntity.plugins = parcel.createTypedArrayList(QScanAdPluginEntity.CREATOR);
            qScanResultEntity.name = parcel.readString();
            qScanResultEntity.type = parcel.readInt();
            qScanResultEntity.advice = parcel.readInt();
            qScanResultEntity.malwareid = parcel.readInt();
            qScanResultEntity.name = parcel.readString();
            qScanResultEntity.label = parcel.readString();
            qScanResultEntity.discription = parcel.readString();
            qScanResultEntity.url = parcel.readString();
            qScanResultEntity.safeLevel = parcel.readInt();
            qScanResultEntity.shortDesc = parcel.readString();
            int readInt = parcel.readInt();
            if (readInt > 0) {
                qScanResultEntity.dirtyDataPathes = new ArrayList(readInt);
                parcel.readStringList(qScanResultEntity.dirtyDataPathes);
            }
            qScanResultEntity.special = parcel.readInt();
            qScanResultEntity.systemFlaw = parcel.readInt();
            qScanResultEntity.isInPayList = parcel.readByte() == (byte) 1;
            qScanResultEntity.isInStealAccountList = parcel.readByte() == (byte) 1;
            qScanResultEntity.needRootToHandle = parcel.readByte() == (byte) 1;
            if (parcel.readByte() == (byte) 1) {
                z = true;
            }
            qScanResultEntity.needOpenAppMonitorToHandle = z;
            qScanResultEntity.product = parcel.readInt();
            qScanResultEntity.category = parcel.readInt();
            qScanResultEntity.officialPackName = parcel.readString();
            qScanResultEntity.officialCertMd5 = parcel.readString();
            return qScanResultEntity;
        }
    };
    public int advice;
    public int apkType;
    public int category = 0;
    public String certMd5;
    public String dexSha1;
    public List<String> dirtyDataPathes;
    public String discription;
    public boolean isInPayList = false;
    public boolean isInStealAccountList = false;
    public String label;
    public int malwareid;
    public String name;
    public boolean needOpenAppMonitorToHandle = false;
    public boolean needRootToHandle = false;
    public int official = 0;
    public String officialCertMd5;
    public String officialPackName;
    public String packageName;
    public String path;
    public ArrayList<QScanAdPluginEntity> plugins;
    public int product = 0;
    public int safeLevel;
    public String shortDesc;
    public int size;
    public String softName;
    public int special = -1;
    public int systemFlaw = -1;
    public int type;
    public String url;
    public String version;
    public int versionCode;

    public int describeContents() {
        return 0;
    }

    public String uniqueKey() {
        return this.systemFlaw == -1 ? (this.apkType == 0 || this.apkType == 1) ? this.packageName : this.apkType == 2 ? this.path : null : "flaw_" + this.systemFlaw;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = 0;
        parcel.writeString(this.packageName);
        parcel.writeString(this.softName);
        parcel.writeString(this.version);
        parcel.writeInt(this.versionCode);
        parcel.writeString(this.path);
        parcel.writeInt(this.apkType);
        parcel.writeString(this.certMd5);
        parcel.writeInt(this.size);
        parcel.writeString(this.dexSha1);
        parcel.writeTypedList(this.plugins);
        parcel.writeString(this.name);
        parcel.writeInt(this.type);
        parcel.writeInt(this.advice);
        parcel.writeInt(this.malwareid);
        parcel.writeString(this.name);
        parcel.writeString(this.label);
        parcel.writeString(this.discription);
        parcel.writeString(this.url);
        parcel.writeInt(this.safeLevel);
        parcel.writeString(this.shortDesc);
        if (this.dirtyDataPathes == null || this.dirtyDataPathes.size() == 0) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(this.dirtyDataPathes.size());
            parcel.writeStringList(this.dirtyDataPathes);
        }
        parcel.writeInt(this.special);
        parcel.writeInt(this.systemFlaw);
        parcel.writeByte((byte) (!this.isInPayList ? 0 : 1));
        parcel.writeByte((byte) (!this.isInStealAccountList ? 0 : 1));
        parcel.writeByte((byte) (!this.needRootToHandle ? 0 : 1));
        if (this.needOpenAppMonitorToHandle) {
            i2 = 1;
        }
        parcel.writeByte((byte) i2);
        parcel.writeInt(this.product);
        parcel.writeInt(this.category);
        parcel.writeString(this.officialPackName);
        parcel.writeString(this.officialCertMd5);
    }
}
