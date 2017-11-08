package com.android.settings.widget;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.view.View.BaseSavedState;

class SwitchBar$SavedState extends BaseSavedState {
    public static final Creator<SwitchBar$SavedState> CREATOR = new Creator<SwitchBar$SavedState>() {
        public SwitchBar$SavedState createFromParcel(Parcel in) {
            return new SwitchBar$SavedState(in);
        }

        public SwitchBar$SavedState[] newArray(int size) {
            return new SwitchBar$SavedState[size];
        }
    };
    boolean checked;
    boolean visible;

    private SwitchBar$SavedState(Parcel in) {
        super(in);
        this.checked = ((Boolean) in.readValue(null)).booleanValue();
        this.visible = ((Boolean) in.readValue(null)).booleanValue();
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeValue(Boolean.valueOf(this.checked));
        out.writeValue(Boolean.valueOf(this.visible));
    }

    public String toString() {
        return "SwitchBar.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checked=" + this.checked + " visible=" + this.visible + "}";
    }
}
