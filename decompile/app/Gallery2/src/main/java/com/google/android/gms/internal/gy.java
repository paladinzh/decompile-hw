package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationEntity;
import com.google.android.gms.games.multiplayer.Participant;
import java.util.ArrayList;

/* compiled from: Unknown */
public final class gy implements SafeParcelable, Invitation {
    public static final gx CREATOR = new gx();
    private final ArrayList<InvitationEntity> IF;
    private final int wj;

    gy(int i, ArrayList<InvitationEntity> arrayList) {
        this.wj = i;
        this.IF = arrayList;
        fR();
    }

    private void fR() {
        int i = 1;
        ed.v(!this.IF.isEmpty());
        Invitation invitation = (Invitation) this.IF.get(0);
        int size = this.IF.size();
        while (i < size) {
            ed.a(invitation.getInviter().equals(((Invitation) this.IF.get(i)).getInviter()), "All the invitations must be from the same inviter");
            i++;
        }
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof gy)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        gy gyVar = (gy) obj;
        if (gyVar.IF.size() != this.IF.size()) {
            return false;
        }
        int size = this.IF.size();
        for (int i = 0; i < size; i++) {
            if (!((Invitation) this.IF.get(i)).equals((Invitation) gyVar.IF.get(i))) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Invitation> fS() {
        return new ArrayList(this.IF);
    }

    public Invitation freeze() {
        return this;
    }

    public int getAvailableAutoMatchSlots() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public long getCreationTimestamp() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public Game getGame() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public String getInvitationId() {
        return ((InvitationEntity) this.IF.get(0)).getInvitationId();
    }

    public int getInvitationType() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public Participant getInviter() {
        return ((InvitationEntity) this.IF.get(0)).getInviter();
    }

    public ArrayList<Participant> getParticipants() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public int getVariant() {
        throw new UnsupportedOperationException("Method not supported on a cluster");
    }

    public int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return ep.hashCode(this.IF.toArray());
    }

    public void writeToParcel(Parcel dest, int flags) {
        gx.a(this, dest, flags);
    }
}
