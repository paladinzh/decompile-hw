package android.support.v7.app;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;

class AppCompatDelegateImplV7$PanelFeatureState$SavedState implements Parcelable {
    public static final Creator<AppCompatDelegateImplV7$PanelFeatureState$SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<AppCompatDelegateImplV7$PanelFeatureState$SavedState>() {
        public AppCompatDelegateImplV7$PanelFeatureState$SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return AppCompatDelegateImplV7$PanelFeatureState$SavedState.readFromParcel(in, loader);
        }

        public AppCompatDelegateImplV7$PanelFeatureState$SavedState[] newArray(int size) {
            return new AppCompatDelegateImplV7$PanelFeatureState$SavedState[size];
        }
    });
    int featureId;
    boolean isOpen;
    Bundle menuState;

    private AppCompatDelegateImplV7$PanelFeatureState$SavedState() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.featureId);
        dest.writeInt(this.isOpen ? 1 : 0);
        if (this.isOpen) {
            dest.writeBundle(this.menuState);
        }
    }

    private static AppCompatDelegateImplV7$PanelFeatureState$SavedState readFromParcel(Parcel source, ClassLoader loader) {
        boolean z = true;
        AppCompatDelegateImplV7$PanelFeatureState$SavedState savedState = new AppCompatDelegateImplV7$PanelFeatureState$SavedState();
        savedState.featureId = source.readInt();
        if (source.readInt() != 1) {
            z = false;
        }
        savedState.isOpen = z;
        if (savedState.isOpen) {
            savedState.menuState = source.readBundle(loader);
        }
        return savedState;
    }
}
