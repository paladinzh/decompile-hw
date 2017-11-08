package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.internal.ed;
import com.google.android.gms.internal.eg;
import com.google.android.gms.internal.ep;
import com.google.android.gms.internal.fy;

/* compiled from: Unknown */
public final class PlayerEntity extends fy implements Player {
    public static final Creator<PlayerEntity> CREATOR = new a();
    private final String FE;
    private final Uri FJ;
    private final Uri FK;
    private final String FU;
    private final String FV;
    private final String Gh;
    private final long Gi;
    private final int Gj;
    private final long Gk;
    private final int wj;

    /* compiled from: Unknown */
    static final class a extends c {
        a() {
        }

        public PlayerEntity ak(Parcel parcel) {
            if (fy.c(eg.dY()) || eg.ae(PlayerEntity.class.getCanonicalName())) {
                return super.ak(parcel);
            }
            String readString = parcel.readString();
            String readString2 = parcel.readString();
            String readString3 = parcel.readString();
            String readString4 = parcel.readString();
            return new PlayerEntity(4, readString, readString2, readString3 != null ? Uri.parse(readString3) : null, readString4 != null ? Uri.parse(readString4) : null, parcel.readLong(), -1, -1, null, null);
        }

        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return ak(x0);
        }
    }

    PlayerEntity(int versionCode, String playerId, String displayName, Uri iconImageUri, Uri hiResImageUri, long retrievedTimestamp, int isInCircles, long lastPlayedWithTimestamp, String iconImageUrl, String hiResImageUrl) {
        this.wj = versionCode;
        this.Gh = playerId;
        this.FE = displayName;
        this.FJ = iconImageUri;
        this.FU = iconImageUrl;
        this.FK = hiResImageUri;
        this.FV = hiResImageUrl;
        this.Gi = retrievedTimestamp;
        this.Gj = isInCircles;
        this.Gk = lastPlayedWithTimestamp;
    }

    public PlayerEntity(Player player) {
        boolean z = true;
        this.wj = 4;
        this.Gh = player.getPlayerId();
        this.FE = player.getDisplayName();
        this.FJ = player.getIconImageUri();
        this.FU = player.getIconImageUrl();
        this.FK = player.getHiResImageUri();
        this.FV = player.getHiResImageUrl();
        this.Gi = player.getRetrievedTimestamp();
        this.Gj = player.fl();
        this.Gk = player.getLastPlayedWithTimestamp();
        ed.d(this.Gh);
        ed.d(this.FE);
        if (this.Gi <= 0) {
            z = false;
        }
        ed.v(z);
    }

    static int a(Player player) {
        return ep.hashCode(player.getPlayerId(), player.getDisplayName(), player.getIconImageUri(), player.getHiResImageUri(), Long.valueOf(player.getRetrievedTimestamp()));
    }

    static boolean a(Player player, Object obj) {
        boolean z = true;
        if (!(obj instanceof Player)) {
            return false;
        }
        if (player == obj) {
            return true;
        }
        Player player2 = (Player) obj;
        if (ep.equal(player2.getPlayerId(), player.getPlayerId()) && ep.equal(player2.getDisplayName(), player.getDisplayName()) && ep.equal(player2.getIconImageUri(), player.getIconImageUri()) && ep.equal(player2.getHiResImageUri(), player.getHiResImageUri())) {
            if (!ep.equal(Long.valueOf(player2.getRetrievedTimestamp()), Long.valueOf(player.getRetrievedTimestamp()))) {
            }
            return z;
        }
        z = false;
        return z;
    }

    static String b(Player player) {
        return ep.e(player).a("PlayerId", player.getPlayerId()).a("DisplayName", player.getDisplayName()).a("IconImageUri", player.getIconImageUri()).a("IconImageUrl", player.getIconImageUrl()).a("HiResImageUri", player.getHiResImageUri()).a("HiResImageUrl", player.getHiResImageUrl()).a("RetrievedTimestamp", Long.valueOf(player.getRetrievedTimestamp())).toString();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        return a(this, obj);
    }

    public int fl() {
        return this.Gj;
    }

    public Player freeze() {
        return this;
    }

    public String getDisplayName() {
        return this.FE;
    }

    public Uri getHiResImageUri() {
        return this.FK;
    }

    public String getHiResImageUrl() {
        return this.FV;
    }

    public Uri getIconImageUri() {
        return this.FJ;
    }

    public String getIconImageUrl() {
        return this.FU;
    }

    public long getLastPlayedWithTimestamp() {
        return this.Gk;
    }

    public String getPlayerId() {
        return this.Gh;
    }

    public long getRetrievedTimestamp() {
        return this.Gi;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return a(this);
    }

    public String toString() {
        return b((Player) this);
    }

    public void writeToParcel(Parcel dest, int flags) {
        String str = null;
        if (dZ()) {
            dest.writeString(this.Gh);
            dest.writeString(this.FE);
            dest.writeString(this.FJ != null ? this.FJ.toString() : null);
            if (this.FK != null) {
                str = this.FK.toString();
            }
            dest.writeString(str);
            dest.writeLong(this.Gi);
            return;
        }
        c.a(this, dest, flags);
    }
}
