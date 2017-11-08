package com.android.contacts.editor;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;

public final class ViewIdGenerator implements Parcelable {
    public static final Creator<ViewIdGenerator> CREATOR = new Creator<ViewIdGenerator>() {
        public ViewIdGenerator createFromParcel(Parcel in) {
            ViewIdGenerator vig = new ViewIdGenerator();
            vig.readFromParcel(in);
            return vig;
        }

        public ViewIdGenerator[] newArray(int size) {
            return new ViewIdGenerator[size];
        }
    };
    private static final StringBuilder sWorkStringBuilder = new StringBuilder();
    private Bundle mIdMap = new Bundle();
    private int mNextId = 1;

    public int describeContents() {
        return 0;
    }

    public int getId(RawContactDelta entity, DataKind kind, ValuesDelta values, int viewIndex) {
        String k = getMapKey(entity, kind, values, viewIndex);
        int id = this.mIdMap.getInt(k, 0);
        if (id != 0) {
            return id;
        }
        int i = this.mNextId;
        this.mNextId = i + 1;
        id = i & 65535;
        this.mIdMap.putInt(k, id);
        return id;
    }

    private static String getMapKey(RawContactDelta entity, DataKind kind, ValuesDelta values, int viewIndex) {
        sWorkStringBuilder.setLength(0);
        if (entity != null) {
            sWorkStringBuilder.append(entity.getValues().getId());
            if (kind != null) {
                sWorkStringBuilder.append('*');
                sWorkStringBuilder.append(kind.mimeType);
                if (values != null) {
                    sWorkStringBuilder.append('*');
                    sWorkStringBuilder.append(values.getId());
                    if (viewIndex != -1) {
                        sWorkStringBuilder.append('*');
                        sWorkStringBuilder.append(viewIndex);
                    }
                }
            }
        }
        return sWorkStringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mNextId);
        dest.writeBundle(this.mIdMap);
    }

    private void readFromParcel(Parcel src) {
        this.mNextId = src.readInt();
        this.mIdMap = src.readBundle();
    }
}
