package com.huawei.android.quickaction;

import android.content.ComponentName;
import android.content.IntentSender;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class QuickAction implements Parcelable {
    public static final Creator<QuickAction> CREATOR = new Creator<QuickAction>() {
        public QuickAction createFromParcel(Parcel parcel) {
            return new QuickAction(parcel);
        }

        public QuickAction[] newArray(int i) {
            return new QuickAction[i];
        }
    };
    private ComponentName mComponentName;
    private ActionIcon mIcon;
    private IntentSender mIntentSender;
    private String mSummary;
    private String mTitle;

    public QuickAction(String str, ActionIcon actionIcon, ComponentName componentName, IntentSender intentSender) {
        this.mTitle = str;
        this.mIcon = actionIcon;
        this.mComponentName = componentName;
        this.mIntentSender = intentSender;
    }

    public QuickAction(String str, String str2, ActionIcon actionIcon, ComponentName componentName, IntentSender intentSender) {
        this(str, actionIcon, componentName, intentSender);
        this.mSummary = str2;
    }

    QuickAction(Parcel parcel) {
        this.mTitle = parcel.readString();
        this.mSummary = parcel.readString();
        if (parcel.readInt() == 0) {
            this.mIcon = null;
        } else {
            this.mIcon = (ActionIcon) ActionIcon.CREATOR.createFromParcel(parcel);
        }
        this.mComponentName = ComponentName.readFromParcel(parcel);
        this.mIntentSender = IntentSender.readIntentSenderOrNullFromParcel(parcel);
    }

    String getTitle() {
        return this.mTitle;
    }

    String getSummary() {
        return this.mSummary;
    }

    ActionIcon getIcon() {
        return this.mIcon;
    }

    ComponentName getComponentName() {
        return this.mComponentName;
    }

    IntentSender getIntentSender() {
        return this.mIntentSender;
    }

    public String toString() {
        return "QuickAction{componentName = " + this.mComponentName + ", intentSender = " + this.mIntentSender + ", title = '" + this.mTitle + ", summary = '" + this.mSummary + "'}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mSummary);
        if (this.mIcon == null) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(1);
            this.mIcon.writeToParcel(parcel, 0);
        }
        ComponentName.writeToParcel(this.mComponentName, parcel);
        IntentSender.writeIntentSenderOrNullToParcel(this.mIntentSender, parcel);
    }
}
