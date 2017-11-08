package com.android.dialer.greeting;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GreetingStatusDelta implements Parcelable {
    public static final Creator<GreetingStatusDelta> CREATOR = new Creator<GreetingStatusDelta>() {
        public GreetingStatusDelta createFromParcel(Parcel in) {
            return new GreetingStatusDelta(in);
        }

        public GreetingStatusDelta[] newArray(int size) {
            return new GreetingStatusDelta[size];
        }
    };
    private long after;
    private long before;

    public GreetingStatusDelta() {
        this.before = -1;
        this.after = -1;
    }

    public boolean isChanged() {
        return (this.after == -1 || this.before == this.after) ? false : true;
    }

    public void setQueryItem(long greetingId) {
        this.before = greetingId;
    }

    public void setSelectItem(long greetingId) {
        this.after = greetingId;
    }

    public long getSelectItem() {
        if (this.after == -1) {
            return this.before;
        }
        return this.after;
    }

    public void invalidateGreetingId(long id) {
        if (this.before == id) {
            this.before = -1;
        }
        if (this.after == id) {
            this.after = -1;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.before);
        out.writeLong(this.after);
    }

    private GreetingStatusDelta(Parcel in) {
        this.before = -1;
        this.after = -1;
        this.before = in.readLong();
        this.after = in.readLong();
    }
}
