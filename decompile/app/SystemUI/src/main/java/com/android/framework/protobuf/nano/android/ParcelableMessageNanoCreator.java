package com.android.framework.protobuf.nano.android;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.framework.protobuf.nano.InvalidProtocolBufferNanoException;
import com.android.framework.protobuf.nano.MessageNano;
import java.lang.reflect.Array;

public final class ParcelableMessageNanoCreator<T extends MessageNano> implements Creator<T> {
    private final Class<T> mClazz;

    public T createFromParcel(Parcel in) {
        String className = in.readString();
        T t = null;
        try {
            t = (MessageNano) Class.forName(className).newInstance();
            MessageNano.mergeFrom(t, in.createByteArray());
            return t;
        } catch (ClassNotFoundException e) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e);
            return t;
        } catch (IllegalAccessException e2) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e2);
            return t;
        } catch (InstantiationException e3) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e3);
            return t;
        } catch (InvalidProtocolBufferNanoException e4) {
            Log.e("PMNCreator", "Exception trying to create proto from parcel", e4);
            return t;
        }
    }

    public T[] newArray(int i) {
        return (MessageNano[]) Array.newInstance(this.mClazz, i);
    }

    static <T extends MessageNano> void writeToParcel(Class<T> clazz, MessageNano message, Parcel out) {
        out.writeString(clazz.getName());
        out.writeByteArray(MessageNano.toByteArray(message));
    }
}
