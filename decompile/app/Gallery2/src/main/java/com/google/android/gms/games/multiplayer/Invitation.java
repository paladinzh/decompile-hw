package com.google.android.gms.games.multiplayer;

import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.games.Game;

/* compiled from: Unknown */
public interface Invitation extends Parcelable, Freezable<Invitation>, Participatable {
    int getAvailableAutoMatchSlots();

    long getCreationTimestamp();

    Game getGame();

    String getInvitationId();

    int getInvitationType();

    Participant getInviter();

    int getVariant();
}
