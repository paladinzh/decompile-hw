package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.metadata.internal.MetadataBundle;

/* compiled from: Unknown */
public class d implements Creator<CloseContentsAndUpdateMetadataRequest> {
    static void a(CloseContentsAndUpdateMetadataRequest closeContentsAndUpdateMetadataRequest, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, closeContentsAndUpdateMetadataRequest.wj);
        b.a(parcel, 2, closeContentsAndUpdateMetadataRequest.Do, i, false);
        b.a(parcel, 3, closeContentsAndUpdateMetadataRequest.Dp, i, false);
        b.a(parcel, 4, closeContentsAndUpdateMetadataRequest.Dq, i, false);
        b.D(parcel, p);
    }

    public CloseContentsAndUpdateMetadataRequest E(Parcel parcel) {
        int o = a.o(parcel);
        MetadataBundle metadataBundle = null;
        DriveId driveId = null;
        int i = 0;
        Contents contents = null;
        while (parcel.dataPosition() < o) {
            int i2;
            DriveId driveId2;
            Contents contents2;
            MetadataBundle metadataBundle2;
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    i2 = i;
                    MetadataBundle metadataBundle3 = metadataBundle;
                    driveId2 = (DriveId) a.a(parcel, n, DriveId.CREATOR);
                    contents2 = contents;
                    metadataBundle2 = metadataBundle3;
                    continue;
                case 3:
                    driveId2 = driveId;
                    i2 = i;
                    Contents contents3 = contents;
                    metadataBundle2 = (MetadataBundle) a.a(parcel, n, MetadataBundle.CREATOR);
                    contents2 = contents3;
                    continue;
                case 4:
                    contents2 = (Contents) a.a(parcel, n, Contents.CREATOR);
                    metadataBundle2 = metadataBundle;
                    driveId2 = driveId;
                    i2 = i;
                    continue;
                default:
                    a.b(parcel, n);
                    break;
            }
            contents2 = contents;
            metadataBundle2 = metadataBundle;
            driveId2 = driveId;
            i2 = i;
            i = i2;
            driveId = driveId2;
            metadataBundle = metadataBundle2;
            contents = contents2;
        }
        if (parcel.dataPosition() == o) {
            return new CloseContentsAndUpdateMetadataRequest(i, driveId, metadataBundle, contents);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public CloseContentsAndUpdateMetadataRequest[] aj(int i) {
        return new CloseContentsAndUpdateMetadataRequest[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return E(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aj(x0);
    }
}
