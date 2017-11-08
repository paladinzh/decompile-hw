package com.google.android.gms.games.multiplayer.realtime;

import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.games.multiplayer.Participatable;

/* compiled from: Unknown */
public interface Room extends Parcelable, Freezable<Room>, Participatable {
    Bundle getAutoMatchCriteria();

    int getAutoMatchWaitEstimateSeconds();

    long getCreationTimestamp();

    String getCreatorId();

    String getDescription();

    String getRoomId();

    int getStatus();

    int getVariant();
}
