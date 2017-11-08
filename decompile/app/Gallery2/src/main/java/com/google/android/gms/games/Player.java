package com.google.android.gms.games;

import android.net.Uri;
import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;

/* compiled from: Unknown */
public interface Player extends Parcelable, Freezable<Player> {
    int fl();

    String getDisplayName();

    Uri getHiResImageUri();

    @Deprecated
    String getHiResImageUrl();

    Uri getIconImageUri();

    @Deprecated
    String getIconImageUrl();

    long getLastPlayedWithTimestamp();

    String getPlayerId();

    long getRetrievedTimestamp();
}
