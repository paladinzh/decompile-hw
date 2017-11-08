package tmsdkobf;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.SparseArray;

/* compiled from: Unknown */
public class jp extends SparseArray<String> implements Parcelable {
    public static final Creator<jp> CREATOR = new Creator<jp>() {
        public jp[] aW(int i) {
            return new jp[i];
        }

        public jp b(Parcel parcel) {
            int readInt = parcel.readInt();
            if (readInt >= 0) {
                return new jp(parcel, readInt);
            }
            throw new IllegalArgumentException("negative size " + readInt);
        }

        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return b(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return aW(i);
        }
    };

    public jp(int i) {
        super(i);
    }

    protected jp(Parcel parcel, int i) {
        this((i + 32) & -32);
        for (int i2 = 0; i2 < i; i2++) {
            put(parcel.readInt(), parcel.readString());
        }
    }

    public int describeContents() {
        return 0;
    }

    public synchronized void writeToParcel(Parcel parcel, int i) {
        int size = size();
        parcel.writeInt(size);
        for (int i2 = 0; i2 < size; i2++) {
            parcel.writeInt(keyAt(i2));
            parcel.writeString((String) valueAt(i2));
        }
    }
}
