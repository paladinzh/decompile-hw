package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public class gt implements Parcelable {
    public static Creator<gt> CREATOR = new Creator<gt>() {
        public gt a(Parcel parcel) {
            gt gtVar = new gt();
            gtVar.status = parcel.readInt();
            gtVar.nt = parcel.readInt();
            gtVar.pq = parcel.readString();
            gtVar.size = parcel.readLong();
            return gtVar;
        }

        public gt[] ay(int i) {
            return new gt[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return a(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return ay(i);
        }
    };
    private int nt = -1;
    private String pq;
    private fy pr;
    public boolean ps = false;
    private long size;
    private int status = 0;

    public void aS(String str) {
        this.pq = str;
    }

    public String aZ() {
        return this.pq;
    }

    public int al() {
        return this.nt;
    }

    public void an(int i) {
        this.nt = i;
    }

    public void b(fy fyVar) {
        this.pr = fyVar;
    }

    public fy ba() {
        return this.pr;
    }

    public int describeContents() {
        return 4;
    }

    public int getStatus() {
        return this.status;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public void setStatus(int i) {
        this.status = i;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.status);
        parcel.writeInt(this.nt);
        parcel.writeString(this.pq);
        parcel.writeLong(this.size);
    }
}
