package com.google.android.gms.location.places;

import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import com.google.android.gms.common.data.Freezable;
import java.util.List;

/* compiled from: Unknown */
public interface AutocompletePrediction extends Freezable<AutocompletePrediction> {

    @Deprecated
    /* compiled from: Unknown */
    public interface Substring {
        int getLength();

        int getOffset();
    }

    @Deprecated
    String getDescription();

    CharSequence getFullText(@Nullable CharacterStyle characterStyle);

    @Deprecated
    List<? extends Substring> getMatchedSubstrings();

    @Nullable
    String getPlaceId();

    @Nullable
    List<Integer> getPlaceTypes();

    CharSequence getPrimaryText(@Nullable CharacterStyle characterStyle);

    CharSequence getSecondaryText(@Nullable CharacterStyle characterStyle);
}
