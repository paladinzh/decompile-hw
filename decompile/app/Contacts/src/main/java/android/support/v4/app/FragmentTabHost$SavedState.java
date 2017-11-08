package android.support.v4.app;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.view.View.BaseSavedState;

class FragmentTabHost$SavedState extends BaseSavedState {
    public static final Creator<FragmentTabHost$SavedState> CREATOR = new Creator<FragmentTabHost$SavedState>() {
        public FragmentTabHost$SavedState createFromParcel(Parcel in) {
            return new FragmentTabHost$SavedState(in);
        }

        public FragmentTabHost$SavedState[] newArray(int size) {
            return new FragmentTabHost$SavedState[size];
        }
    };
    String curTab;

    private FragmentTabHost$SavedState(Parcel in) {
        super(in);
        this.curTab = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(this.curTab);
    }

    public String toString() {
        return "FragmentTabHost.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " curTab=" + this.curTab + "}";
    }
}
