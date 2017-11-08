package android.content.pm;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public final class EphemeralResolveInfo implements Parcelable {
    public static final Creator<EphemeralResolveInfo> CREATOR = new Creator<EphemeralResolveInfo>() {
        public EphemeralResolveInfo createFromParcel(Parcel in) {
            return new EphemeralResolveInfo(in);
        }

        public EphemeralResolveInfo[] newArray(int size) {
            return new EphemeralResolveInfo[size];
        }
    };
    public static final String SHA_ALGORITHM = "SHA-256";
    private final byte[] mDigestBytes;
    private final int mDigestPrefix;
    private final List<IntentFilter> mFilters = new ArrayList();
    private final String mPackageName;

    public static final class EphemeralResolveIntentInfo extends IntentFilter {
        private final EphemeralResolveInfo mResolveInfo;

        public EphemeralResolveIntentInfo(IntentFilter orig, EphemeralResolveInfo resolveInfo) {
            super(orig);
            this.mResolveInfo = resolveInfo;
        }

        public EphemeralResolveInfo getEphemeralResolveInfo() {
            return this.mResolveInfo;
        }
    }

    public EphemeralResolveInfo(Uri uri, String packageName, List<IntentFilter> filters) {
        if (uri == null || packageName == null || filters == null || filters.size() == 0) {
            throw new IllegalArgumentException();
        }
        this.mDigestBytes = generateDigest(uri);
        this.mDigestPrefix = (((this.mDigestBytes[0] << 24) | (this.mDigestBytes[1] << 16)) | (this.mDigestBytes[2] << 8)) | (this.mDigestBytes[3] << 0);
        this.mFilters.addAll(filters);
        this.mPackageName = packageName;
    }

    EphemeralResolveInfo(Parcel in) {
        this.mDigestBytes = in.createByteArray();
        this.mDigestPrefix = in.readInt();
        this.mPackageName = in.readString();
        in.readList(this.mFilters, null);
    }

    public byte[] getDigestBytes() {
        return this.mDigestBytes;
    }

    public int getDigestPrefix() {
        return this.mDigestPrefix;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public List<IntentFilter> getFilters() {
        return this.mFilters;
    }

    private static byte[] generateDigest(Uri uri) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(uri.getHost().getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("could not find digest algorithm");
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.mDigestBytes);
        out.writeInt(this.mDigestPrefix);
        out.writeString(this.mPackageName);
        out.writeList(this.mFilters);
    }
}
