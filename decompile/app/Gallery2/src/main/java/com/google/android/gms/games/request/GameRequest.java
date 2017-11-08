package com.google.android.gms.games.request;

import android.os.Parcelable;
import com.google.android.gms.common.data.Freezable;
import com.google.android.gms.games.Game;
import com.google.android.gms.games.Player;
import java.util.ArrayList;

/* compiled from: Unknown */
public interface GameRequest extends Parcelable, Freezable<GameRequest> {
    ArrayList<Player> fU();

    long getCreationTimestamp();

    byte[] getData();

    long getExpirationTimestamp();

    Game getGame();

    int getRecipientStatus(String str);

    String getRequestId();

    Player getSender();

    int getType();
}
