package com.google.android.gms.location.places.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePrediction.Substring;
import java.util.Collections;
import java.util.List;

/* compiled from: Unknown */
public class AutocompletePredictionEntity implements SafeParcelable, AutocompletePrediction {
    public static final Creator<AutocompletePredictionEntity> CREATOR = new zza();
    private static final List<SubstringEntity> zzaQc = Collections.emptyList();
    final int mVersionCode;
    final String zzaPH;
    final List<Integer> zzaPd;
    final String zzaQd;
    final List<SubstringEntity> zzaQe;
    final int zzaQf;
    final String zzaQg;
    final List<SubstringEntity> zzaQh;
    final String zzaQi;
    final List<SubstringEntity> zzaQj;

    /* compiled from: Unknown */
    public static class SubstringEntity implements SafeParcelable, Substring {
        public static final Creator<SubstringEntity> CREATOR = new zzu();
        final int mLength;
        final int mOffset;
        final int mVersionCode;

        public SubstringEntity(int versionCode, int offset, int length) {
            this.mVersionCode = versionCode;
            this.mOffset = offset;
            this.mLength = length;
        }

        public int describeContents() {
            return 0;
        }

        public boolean equals(Object object) {
            boolean z = true;
            if (this == object) {
                return true;
            }
            if (!(object instanceof SubstringEntity)) {
                return false;
            }
            SubstringEntity substringEntity = (SubstringEntity) object;
            if (zzw.equal(Integer.valueOf(this.mOffset), Integer.valueOf(substringEntity.mOffset))) {
                if (!zzw.equal(Integer.valueOf(this.mLength), Integer.valueOf(substringEntity.mLength))) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int getLength() {
            return this.mLength;
        }

        public int getOffset() {
            return this.mOffset;
        }

        public int hashCode() {
            return zzw.hashCode(Integer.valueOf(this.mOffset), Integer.valueOf(this.mLength));
        }

        public String toString() {
            return zzw.zzy(this).zzg("offset", Integer.valueOf(this.mOffset)).zzg("length", Integer.valueOf(this.mLength)).toString();
        }

        public void writeToParcel(Parcel parcel, int flags) {
            zzu.zza(this, parcel, flags);
        }
    }

    AutocompletePredictionEntity(int versionCode, String placeId, List<Integer> placeTypes, int personalizationType, String fullText, List<SubstringEntity> fullTextMatchedSubstrings, String primaryText, List<SubstringEntity> primaryTextMatchedSubstrings, String secondaryText, List<SubstringEntity> secondaryTextMatchedSubstrings) {
        this.mVersionCode = versionCode;
        this.zzaPH = placeId;
        this.zzaPd = placeTypes;
        this.zzaQf = personalizationType;
        this.zzaQd = fullText;
        this.zzaQe = fullTextMatchedSubstrings;
        this.zzaQg = primaryText;
        this.zzaQh = primaryTextMatchedSubstrings;
        this.zzaQi = secondaryText;
        this.zzaQj = secondaryTextMatchedSubstrings;
    }

    public static AutocompletePredictionEntity zza(String str, List<Integer> list, int i, String str2, List<SubstringEntity> list2, String str3, List<SubstringEntity> list3, String str4, List<SubstringEntity> list4) {
        return new AutocompletePredictionEntity(0, str, list, i, (String) zzx.zzz(str2), list2, str3, list3, str4, list4);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof AutocompletePredictionEntity)) {
            return false;
        }
        AutocompletePredictionEntity autocompletePredictionEntity = (AutocompletePredictionEntity) object;
        if (zzw.equal(this.zzaPH, autocompletePredictionEntity.zzaPH) && zzw.equal(this.zzaPd, autocompletePredictionEntity.zzaPd) && zzw.equal(Integer.valueOf(this.zzaQf), Integer.valueOf(autocompletePredictionEntity.zzaQf)) && zzw.equal(this.zzaQd, autocompletePredictionEntity.zzaQd) && zzw.equal(this.zzaQe, autocompletePredictionEntity.zzaQe) && zzw.equal(this.zzaQg, autocompletePredictionEntity.zzaQg) && zzw.equal(this.zzaQh, autocompletePredictionEntity.zzaQh) && zzw.equal(this.zzaQi, autocompletePredictionEntity.zzaQi)) {
            if (!zzw.equal(this.zzaQj, autocompletePredictionEntity.zzaQj)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public /* synthetic */ Object freeze() {
        return zzzf();
    }

    public String getDescription() {
        return this.zzaQd;
    }

    public CharSequence getFullText(@Nullable CharacterStyle matchStyle) {
        return zzc.zza(this.zzaQd, this.zzaQe, matchStyle);
    }

    public List<? extends Substring> getMatchedSubstrings() {
        return this.zzaQe;
    }

    @Nullable
    public String getPlaceId() {
        return this.zzaPH;
    }

    public List<Integer> getPlaceTypes() {
        return this.zzaPd;
    }

    public CharSequence getPrimaryText(@Nullable CharacterStyle matchStyle) {
        return zzc.zza(this.zzaQg, this.zzaQh, matchStyle);
    }

    public CharSequence getSecondaryText(@Nullable CharacterStyle matchStyle) {
        return zzc.zza(this.zzaQi, this.zzaQj, matchStyle);
    }

    public int hashCode() {
        return zzw.hashCode(this.zzaPH, this.zzaPd, Integer.valueOf(this.zzaQf), this.zzaQd, this.zzaQe, this.zzaQg, this.zzaQh, this.zzaQi, this.zzaQj);
    }

    public boolean isDataValid() {
        return true;
    }

    public String toString() {
        return zzw.zzy(this).zzg("placeId", this.zzaPH).zzg("placeTypes", this.zzaPd).zzg("fullText", this.zzaQd).zzg("fullTextMatchedSubstrings", this.zzaQe).zzg("primaryText", this.zzaQg).zzg("primaryTextMatchedSubstrings", this.zzaQh).zzg("secondaryText", this.zzaQi).zzg("secondaryTextMatchedSubstrings", this.zzaQj).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zza.zza(this, parcel, flags);
    }

    public AutocompletePrediction zzzf() {
        return this;
    }
}
