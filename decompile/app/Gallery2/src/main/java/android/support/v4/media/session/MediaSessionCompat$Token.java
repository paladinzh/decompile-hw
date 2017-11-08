package android.support.v4.media.session;

import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class MediaSessionCompat$Token implements Parcelable {
    public static final Creator<MediaSessionCompat$Token> CREATOR = new Creator<MediaSessionCompat$Token>() {
        public MediaSessionCompat$Token createFromParcel(Parcel in) {
            Object inner;
            if (VERSION.SDK_INT >= 21) {
                inner = in.readParcelable(null);
            } else {
                inner = in.readStrongBinder();
            }
            return new MediaSessionCompat$Token(inner);
        }

        public MediaSessionCompat$Token[] newArray(int size) {
            return new MediaSessionCompat$Token[size];
        }
    };
    private final Object mInner;

    MediaSessionCompat$Token(Object inner) {
        this.mInner = inner;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (VERSION.SDK_INT >= 21) {
            dest.writeParcelable((Parcelable) this.mInner, flags);
        } else {
            dest.writeStrongBinder((IBinder) this.mInner);
        }
    }
}
