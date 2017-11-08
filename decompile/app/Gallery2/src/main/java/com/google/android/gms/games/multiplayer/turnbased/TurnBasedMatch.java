package com.google.android.gms.games.multiplayer.turnbased;

import android.os.Bundle;
import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.multiplayer.Participatable;

/* compiled from: Unknown */
public interface TurnBasedMatch extends Parcelable, Freezable<TurnBasedMatch>, Participatable {
    public static final int[] MATCH_TURN_STATUS_ALL = new int[]{0, 1, 2, 3};

    Bundle getAutoMatchCriteria();

    int getAvailableAutoMatchSlots();

    long getCreationTimestamp();

    String getCreatorId();

    byte[] getData();

    String getDescription();

    String getDescriptionParticipantId();

    Game getGame();

    long getLastUpdatedTimestamp();

    String getLastUpdaterId();

    String getMatchId();

    int getMatchNumber();

    String getPendingParticipantId();

    byte[] getPreviousMatchData();

    String getRematchId();

    int getStatus();

    int getTurnStatus();

    int getVariant();

    int getVersion();

    boolean isLocallyModified();
}
