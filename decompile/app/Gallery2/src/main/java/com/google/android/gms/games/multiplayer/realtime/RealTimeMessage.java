package com.google.android.gms.games.multiplayer.realtime;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.gms.internal.er;

/* compiled from: Unknown */
public final class RealTimeMessage implements Parcelable {
    public static final Creator<RealTimeMessage> CREATOR = new Creator<RealTimeMessage>() {
        public RealTimeMessage ap(Parcel parcel) {
            return new RealTimeMessage(parcel);
        }

        public RealTimeMessage[] bc(int i) {
            return new RealTimeMessage[i];
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return ap(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return bc(x0);
        }
    };
    private final String JH;
    private final byte[] JI;
    private final int JJ;

    private RealTimeMessage(Parcel parcel) {
        this(parcel.readString(), parcel.createByteArray(), parcel.readInt());
    }

    public RealTimeMessage(String senderParticipantId, byte[] messageData, int isReliable) {
        this.JH = (String) er.f(senderParticipantId);
        this.JI = (byte[]) ((byte[]) er.f(messageData)).clone();
        this.JJ = isReliable;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(this.JH);
        parcel.writeByteArray(this.JI);
        parcel.writeInt(this.JJ);
    }
}
