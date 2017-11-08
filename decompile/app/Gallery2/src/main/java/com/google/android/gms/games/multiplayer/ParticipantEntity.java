package com.google.android.gms.games.multiplayer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayerEntity;
import com.google.android.gms.internal.eg;
import com.google.android.gms.internal.ep;
import com.google.android.gms.internal.fy;

/* compiled from: Unknown */
public final class ParticipantEntity extends fy implements Participant {
    public static final Creator<ParticipantEntity> CREATOR = new a();
    private final String FE;
    private final Uri FJ;
    private final Uri FK;
    private final String FU;
    private final String FV;
    private final String GZ;
    private final boolean JA;
    private final PlayerEntity JB;
    private final int JC;
    private final ParticipantResult JD;
    private final int Jy;
    private final String Jz;
    private final int wj;

    /* compiled from: Unknown */
    static final class a extends c {
        a() {
        }

        public ParticipantEntity ao(Parcel parcel) {
            Object obj = null;
            if (fy.c(eg.dY()) || eg.ae(ParticipantEntity.class.getCanonicalName())) {
                return super.ao(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            String readString3 = parcel.readString();
            Uri parse = readString3 != null ? Uri.parse(readString3) : null;
            String readString4 = parcel.readString();
            Uri parse2 = readString4 != null ? Uri.parse(readString4) : null;
            int readInt = parcel.readInt();
            String readString5 = parcel.readString();
            boolean z = parcel.readInt() > 0;
            if (parcel.readInt() > 0) {
                int i = 1;
            }
            return new ParticipantEntity(3, readString, readString2, parse, parse2, readInt, readString5, z, obj == null ? null : (PlayerEntity) PlayerEntity.CREATOR.createFromParcel(parcel), 7, null, null, null);
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return ao(x0);
        }
    }

    ParticipantEntity(int versionCode, String participantId, String displayName, Uri iconImageUri, Uri hiResImageUri, int status, String clientAddress, boolean connectedToRoom, PlayerEntity player, int capabilities, ParticipantResult result, String iconImageUrl, String hiResImageUrl) {
        this.wj = versionCode;
        this.GZ = participantId;
        this.FE = displayName;
        this.FJ = iconImageUri;
        this.FK = hiResImageUri;
        this.Jy = status;
        this.Jz = clientAddress;
        this.JA = connectedToRoom;
        this.JB = player;
        this.JC = capabilities;
        this.JD = result;
        this.FU = iconImageUrl;
        this.FV = hiResImageUrl;
    }

    public ParticipantEntity(Participant participant) {
        PlayerEntity playerEntity = null;
        this.wj = 3;
        this.GZ = participant.getParticipantId();
        this.FE = participant.getDisplayName();
        this.FJ = participant.getIconImageUri();
        this.FK = participant.getHiResImageUri();
        this.Jy = participant.getStatus();
        this.Jz = participant.ge();
        this.JA = participant.isConnectedToRoom();
        Player player = participant.getPlayer();
        if (player != null) {
            playerEntity = new PlayerEntity(player);
        }
        this.JB = playerEntity;
        this.JC = participant.getCapabilities();
        this.JD = participant.getResult();
        this.FU = participant.getIconImageUrl();
        this.FV = participant.getHiResImageUrl();
    }

    static int a(Participant participant) {
        return ep.hashCode(participant.getPlayer(), Integer.valueOf(participant.getStatus()), participant.ge(), Boolean.valueOf(participant.isConnectedToRoom()), participant.getDisplayName(), participant.getIconImageUri(), participant.getHiResImageUri(), Integer.valueOf(participant.getCapabilities()), participant.getResult());
    }

    static boolean a(Participant participant, Object obj) {
        boolean z = true;
        if (!(obj instanceof Participant)) {
            return false;
        }
        if (participant == obj) {
            return true;
        }
        Participant participant2 = (Participant) obj;
        if (ep.equal(participant2.getPlayer(), participant.getPlayer()) && ep.equal(Integer.valueOf(participant2.getStatus()), Integer.valueOf(participant.getStatus())) && ep.equal(participant2.ge(), participant.ge()) && ep.equal(Boolean.valueOf(participant2.isConnectedToRoom()), Boolean.valueOf(participant.isConnectedToRoom())) && ep.equal(participant2.getDisplayName(), participant.getDisplayName()) && ep.equal(participant2.getIconImageUri(), participant.getIconImageUri()) && ep.equal(participant2.getHiResImageUri(), participant.getHiResImageUri()) && ep.equal(Integer.valueOf(participant2.getCapabilities()), Integer.valueOf(participant.getCapabilities()))) {
            if (!ep.equal(participant2.getResult(), participant.getResult())) {
            }
            return z;
        }
        z = false;
        return z;
    }

    static String b(Participant participant) {
        return ep.e(participant).a("Player", participant.getPlayer()).a("Status", Integer.valueOf(participant.getStatus())).a("ClientAddress", participant.ge()).a("ConnectedToRoom", Boolean.valueOf(participant.isConnectedToRoom())).a("DisplayName", participant.getDisplayName()).a("IconImage", participant.getIconImageUri()).a("IconImageUrl", participant.getIconImageUrl()).a("HiResImage", participant.getHiResImageUri()).a("HiResImageUrl", participant.getHiResImageUrl()).a("Capabilities", Integer.valueOf(participant.getCapabilities())).a("Result", participant.getResult()).toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return a(this, obj);
    }

    public Participant freeze() {
        return this;
    }

    public String ge() {
        return this.Jz;
    }

    public int getCapabilities() {
        return this.JC;
    }

    public String getDisplayName() {
        return this.JB != null ? this.JB.getDisplayName() : this.FE;
    }

    public Uri getHiResImageUri() {
        return this.JB != null ? this.JB.getHiResImageUri() : this.FK;
    }

    public String getHiResImageUrl() {
        return this.JB != null ? this.JB.getHiResImageUrl() : this.FV;
    }

    public Uri getIconImageUri() {
        return this.JB != null ? this.JB.getIconImageUri() : this.FJ;
    }

    public String getIconImageUrl() {
        return this.JB != null ? this.JB.getIconImageUrl() : this.FU;
    }

    public String getParticipantId() {
        return this.GZ;
    }

    public Player getPlayer() {
        return this.JB;
    }

    public ParticipantResult getResult() {
        return this.JD;
    }

    public int getStatus() {
        return this.Jy;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return a(this);
    }

    public boolean isConnectedToRoom() {
        return this.JA;
    }

    public String toString() {
        return b((Participant) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        String str = null;
        if (dZ()) {
            dest.writeString(this.GZ);
            dest.writeString(this.FE);
            dest.writeString(this.FJ != null ? this.FJ.toString() : null);
            if (this.FK != null) {
                str = this.FK.toString();
            }
            dest.writeString(str);
            dest.writeInt(this.Jy);
            dest.writeString(this.Jz);
            dest.writeInt(!this.JA ? 0 : 1);
            if (this.JB == null) {
                i = 0;
            }
            dest.writeInt(i);
            if (this.JB != null) {
                this.JB.writeToParcel(dest, flags);
                return;
            }
            return;
        }
        c.a(this, dest, flags);
    }
}
