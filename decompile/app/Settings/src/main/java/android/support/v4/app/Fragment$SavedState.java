package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Fragment$SavedState implements Parcelable {
    public static final Creator<Fragment$SavedState> CREATOR = new Creator<Fragment$SavedState>() {
        public Fragment$SavedState createFromParcel(Parcel in) {
            return new Fragment$SavedState(in, null);
        }

        public Fragment$SavedState[] newArray(int size) {
            return new Fragment$SavedState[size];
        }
    };
    final Bundle mState;

    Fragment$SavedState(Parcel in, ClassLoader loader) {
        this.mState = in.readBundle();
        if (loader != null && this.mState != null) {
            this.mState.setClassLoader(loader);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(this.mState);
    }
}
