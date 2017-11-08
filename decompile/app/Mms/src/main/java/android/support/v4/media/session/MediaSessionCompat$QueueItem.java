package android.support.v4.media.session;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.media.MediaDescriptionCompat;

public final class MediaSessionCompat$QueueItem implements Parcelable {
    public static final Creator<MediaSessionCompat$QueueItem> CREATOR = new Creator<MediaSessionCompat$QueueItem>() {
        public MediaSessionCompat$QueueItem createFromParcel(Parcel p) {
            return new MediaSessionCompat$QueueItem(p);
        }

        public MediaSessionCompat$QueueItem[] newArray(int size) {
            return new MediaSessionCompat$QueueItem[size];
        }
    };
    private final MediaDescriptionCompat mDescription;
    private final long mId;

    private MediaSessionCompat$QueueItem(Parcel in) {
        this.mDescription = (MediaDescriptionCompat) MediaDescriptionCompat.CREATOR.createFromParcel(in);
        this.mId = in.readLong();
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mDescription.writeToParcel(dest, flags);
        dest.writeLong(this.mId);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "MediaSession.QueueItem {Description=" + this.mDescription + ", Id=" + this.mId + " }";
    }
}
