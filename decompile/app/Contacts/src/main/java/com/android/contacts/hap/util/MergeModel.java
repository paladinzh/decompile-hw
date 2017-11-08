package com.android.contacts.hap.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class MergeModel implements Parcelable {
    public static final Creator<MergeModel> CREATOR = new Creator<MergeModel>() {
        public MergeModel[] newArray(int size) {
            return new MergeModel[size];
        }

        public MergeModel createFromParcel(Parcel in) {
            return new MergeModel(in);
        }
    };
    private ArrayList<Long> contactIds;
    private int id;
    private boolean selected;

    public MergeModel(int id) {
        this.contactIds = new ArrayList();
        this.id = id;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setContactIds(ArrayList<Long> contactIds) {
        this.contactIds = contactIds;
    }

    public ArrayList<Long> getContactIds() {
        return this.contactIds;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeValue(Boolean.valueOf(this.selected));
        dest.writeList(this.contactIds);
    }

    private MergeModel(Parcel in) {
        this.contactIds = new ArrayList();
        this.id = in.readInt();
        this.selected = ((Boolean) in.readValue(getClass().getClassLoader())).booleanValue();
        this.contactIds = in.readArrayList(getClass().getClassLoader());
    }
}
