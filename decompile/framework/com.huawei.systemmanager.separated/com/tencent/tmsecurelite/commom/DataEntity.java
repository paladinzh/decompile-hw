package com.tencent.tmsecurelite.commom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public final class DataEntity extends JSONObject implements Parcelable {
    public static final Creator<DataEntity> CREATOR = new Creator<DataEntity>() {
        public DataEntity createFromParcel(Parcel parcel) {
            try {
                return new DataEntity(parcel);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        public DataEntity[] newArray(int i) {
            return new DataEntity[i];
        }
    };

    public DataEntity(Parcel parcel) throws JSONException {
        super(parcel.readString());
    }

    public static ArrayList<DataEntity> readFromParcel(Parcel parcel) {
        ArrayList<DataEntity> arrayList = new ArrayList();
        try {
            int readInt = parcel.readInt();
            arrayList.ensureCapacity(readInt);
            for (int i = 0; i < readInt; i++) {
                arrayList.add(i, new DataEntity(parcel));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static void writeToParcel(List<DataEntity> list, Parcel parcel) {
        parcel.writeInt(list.size());
        for (DataEntity writeToParcel : list) {
            writeToParcel.writeToParcel(parcel, 0);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(toString());
    }
}
