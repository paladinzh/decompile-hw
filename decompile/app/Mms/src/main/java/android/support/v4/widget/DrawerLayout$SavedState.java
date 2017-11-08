package android.support.v4.widget;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;

class DrawerLayout$SavedState extends AbsSavedState {
    public static final Creator<DrawerLayout$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<DrawerLayout$SavedState>() {
        public DrawerLayout$SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new DrawerLayout$SavedState(in, loader);
        }

        public DrawerLayout$SavedState[] newArray(int size) {
            return new DrawerLayout$SavedState[size];
        }
    });
    int lockModeEnd;
    int lockModeLeft;
    int lockModeRight;
    int lockModeStart;
    int openDrawerGravity = 0;

    public DrawerLayout$SavedState(Parcel in, ClassLoader loader) {
        super(in, loader);
        this.openDrawerGravity = in.readInt();
        this.lockModeLeft = in.readInt();
        this.lockModeRight = in.readInt();
        this.lockModeStart = in.readInt();
        this.lockModeEnd = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.openDrawerGravity);
        dest.writeInt(this.lockModeLeft);
        dest.writeInt(this.lockModeRight);
        dest.writeInt(this.lockModeStart);
        dest.writeInt(this.lockModeEnd);
    }
}
