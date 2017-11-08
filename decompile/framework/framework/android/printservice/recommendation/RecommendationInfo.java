package android.printservice.recommendation;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.Preconditions;

public final class RecommendationInfo implements Parcelable {
    public static final Creator<RecommendationInfo> CREATOR = new Creator<RecommendationInfo>() {
        public RecommendationInfo createFromParcel(Parcel in) {
            return new RecommendationInfo(in);
        }

        public RecommendationInfo[] newArray(int size) {
            return new RecommendationInfo[size];
        }
    };
    private final CharSequence mName;
    private final int mNumDiscoveredPrinters;
    private final CharSequence mPackageName;
    private final boolean mRecommendsMultiVendorService;

    public RecommendationInfo(CharSequence packageName, CharSequence name, int numDiscoveredPrinters, boolean recommendsMultiVendorService) {
        this.mPackageName = Preconditions.checkStringNotEmpty(packageName);
        this.mName = Preconditions.checkStringNotEmpty(name);
        this.mNumDiscoveredPrinters = Preconditions.checkArgumentNonnegative(numDiscoveredPrinters);
        this.mRecommendsMultiVendorService = recommendsMultiVendorService;
    }

    private RecommendationInfo(Parcel parcel) {
        boolean z = false;
        CharSequence readCharSequence = parcel.readCharSequence();
        CharSequence readCharSequence2 = parcel.readCharSequence();
        int readInt = parcel.readInt();
        if (parcel.readByte() != (byte) 0) {
            z = true;
        }
        this(readCharSequence, readCharSequence2, readInt, z);
    }

    public CharSequence getPackageName() {
        return this.mPackageName;
    }

    public boolean recommendsMultiVendorService() {
        return this.mRecommendsMultiVendorService;
    }

    public int getNumDiscoveredPrinters() {
        return this.mNumDiscoveredPrinters;
    }

    public CharSequence getName() {
        return this.mName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(this.mPackageName);
        dest.writeCharSequence(this.mName);
        dest.writeInt(this.mNumDiscoveredPrinters);
        dest.writeByte((byte) (this.mRecommendsMultiVendorService ? 1 : 0));
    }
}
