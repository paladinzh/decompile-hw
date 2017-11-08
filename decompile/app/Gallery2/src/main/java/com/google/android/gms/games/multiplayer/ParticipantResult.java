package com.google.android.gms.games.multiplayer;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.er;
import com.google.android.gms.internal.gr;

/* compiled from: Unknown */
public final class ParticipantResult implements SafeParcelable {
    public static final ParticipantResultCreator CREATOR = new ParticipantResultCreator();
    private final String GZ;
    private final int JF;
    private final int JG;
    private final int wj;

    public ParticipantResult(int versionCode, String participantId, int result, int placing) {
        this.wj = versionCode;
        this.GZ = (String) er.f(participantId);
        er.v(gr.isValid(result));
        this.JF = result;
        this.JG = placing;
    }

    public ParticipantResult(String participantId, int result, int placing) {
        this(1, participantId, result, placing);
    }

    public int describeContents() {
        return 0;
    }

    public String getParticipantId() {
        return this.GZ;
    }

    public int getPlacing() {
        return this.JG;
    }

    public int getResult() {
        return this.JF;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel out, int flags) {
        ParticipantResultCreator.a(this, out, flags);
    }
}
