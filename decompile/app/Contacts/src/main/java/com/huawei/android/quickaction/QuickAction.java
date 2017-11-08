package com.huawei.android.quickaction;

import android.content.ComponentName;
import android.content.IntentSender;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class QuickAction implements Parcelable {
    public static final Creator<QuickAction> CREATOR = new Creator<QuickAction>() {
        public QuickAction createFromParcel(Parcel source) {
            return new QuickAction(source);
        }

        public QuickAction[] newArray(int size) {
            return new QuickAction[size];
        }
    };
    private ComponentName mComponentName;
    private ActionIcon mIcon;
    private IntentSender mIntentSender;
    private String mSummary;
    private String mTitle;

    public QuickAction(String title, ActionIcon icon, ComponentName componentName, IntentSender intentSender) {
        this.mTitle = title;
        this.mIcon = icon;
        this.mComponentName = componentName;
        this.mIntentSender = intentSender;
    }

    QuickAction(Parcel in) {
        this.mTitle = in.readString();
        this.mSummary = in.readString();
        if (in.readInt() == 0) {
            this.mIcon = null;
        } else {
            this.mIcon = (ActionIcon) ActionIcon.CREATOR.createFromParcel(in);
        }
        this.mComponentName = ComponentName.readFromParcel(in);
        this.mIntentSender = IntentSender.readIntentSenderOrNullFromParcel(in);
    }

    public String toString() {
        return "QuickAction{componentName = " + this.mComponentName + ", intentSender = " + this.mIntentSender + ", title = '" + this.mTitle + ", summary = '" + this.mSummary + "'}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTitle);
        dest.writeString(this.mSummary);
        if (this.mIcon == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            this.mIcon.writeToParcel(dest, 0);
        }
        ComponentName.writeToParcel(this.mComponentName, dest);
        IntentSender.writeIntentSenderOrNullToParcel(this.mIntentSender, dest);
    }
}
