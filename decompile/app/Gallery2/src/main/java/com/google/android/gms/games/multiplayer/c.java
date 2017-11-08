package com.google.android.gms.games.multiplayer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.games.PlayerEntity;

/* compiled from: Unknown */
public class c implements Creator<ParticipantEntity> {
    static void a(ParticipantEntity participantEntity, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, participantEntity.getParticipantId(), false);
        b.a(parcel, 2, participantEntity.getDisplayName(), false);
        b.a(parcel, 3, participantEntity.getIconImageUri(), i, false);
        b.a(parcel, 4, participantEntity.getHiResImageUri(), i, false);
        b.c(parcel, 5, participantEntity.getStatus());
        b.a(parcel, 6, participantEntity.ge(), false);
        b.a(parcel, 7, participantEntity.isConnectedToRoom());
        b.a(parcel, 8, participantEntity.getPlayer(), i, false);
        b.c(parcel, 9, participantEntity.getCapabilities());
        b.a(parcel, 10, participantEntity.getResult(), i, false);
        b.a(parcel, 11, participantEntity.getIconImageUrl(), false);
        b.a(parcel, 12, participantEntity.getHiResImageUrl(), false);
        b.c(parcel, 1000, participantEntity.getVersionCode());
        b.D(parcel, p);
    }

    public ParticipantEntity ao(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        Uri uri = null;
        Uri uri2 = null;
        int i2 = 0;
        String str3 = null;
        boolean z = false;
        PlayerEntity playerEntity = null;
        int i3 = 0;
        ParticipantResult participantResult = null;
        String str4 = null;
        String str5 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str = a.m(parcel, n);
                    break;
                case 2:
                    str2 = a.m(parcel, n);
                    break;
                case 3:
                    uri = (Uri) a.a(parcel, n, Uri.CREATOR);
                    break;
                case 4:
                    uri2 = (Uri) a.a(parcel, n, Uri.CREATOR);
                    break;
                case 5:
                    i2 = a.g(parcel, n);
                    break;
                case 6:
                    str3 = a.m(parcel, n);
                    break;
                case 7:
                    z = a.c(parcel, n);
                    break;
                case 8:
                    playerEntity = (PlayerEntity) a.a(parcel, n, PlayerEntity.CREATOR);
                    break;
                case 9:
                    i3 = a.g(parcel, n);
                    break;
                case 10:
                    participantResult = (ParticipantResult) a.a(parcel, n, ParticipantResult.CREATOR);
                    break;
                case 11:
                    str4 = a.m(parcel, n);
                    break;
                case 12:
                    str5 = a.m(parcel, n);
                    break;
                case 1000:
                    i = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new ParticipantEntity(i, str, str2, uri, uri2, i2, str3, z, playerEntity, i3, participantResult, str4, str5);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public ParticipantEntity[] bb(int i) {
        return new ParticipantEntity[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return ao(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return bb(x0);
    }
}
