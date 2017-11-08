package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;

class SlidingPaneLayout$SavedState extends AbsSavedState {
    public static final Creator<SlidingPaneLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SlidingPaneLayout$SavedState>() {
        public SlidingPaneLayout$SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SlidingPaneLayout$SavedState(in, loader);
        }

        public SlidingPaneLayout$SavedState[] newArray(int size) {
            return new SlidingPaneLayout$SavedState[size];
        }
    });
    boolean isOpen;

    private SlidingPaneLayout$SavedState(Parcel in, ClassLoader loader) {
        boolean z = false;
        super(in, loader);
        if (in.readInt() != 0) {
            z = true;
        }
        this.isOpen = z;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.isOpen ? 1 : 0);
    }
}
