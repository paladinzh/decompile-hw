package com.android.contacts.widget;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.gms.R;

public class MaterialColorMapUtils {
    private final MaterialPalette mDefaultMaterialPalette;
    private final TypedArray sPrimaryColors;
    private final TypedArray sSecondaryColors = this.sPrimaryColors;

    public static class MaterialPalette implements Parcelable {
        public static final Creator<MaterialPalette> CREATOR = new Creator<MaterialPalette>() {
            public MaterialPalette createFromParcel(Parcel in) {
                return new MaterialPalette(in);
            }

            public MaterialPalette[] newArray(int size) {
                return new MaterialPalette[size];
            }
        };
        public final int mPrimaryColor;
        public final int mSecondaryColor;

        public MaterialPalette(int primaryColor, int secondaryColor) {
            this.mPrimaryColor = primaryColor;
            this.mSecondaryColor = secondaryColor;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MaterialPalette other = (MaterialPalette) obj;
            return this.mPrimaryColor == other.mPrimaryColor && this.mSecondaryColor == other.mSecondaryColor;
        }

        public int hashCode() {
            return ((this.mPrimaryColor + 31) * 31) + this.mSecondaryColor;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mPrimaryColor);
            dest.writeInt(this.mSecondaryColor);
        }

        private MaterialPalette(Parcel in) {
            this.mPrimaryColor = in.readInt();
            this.mSecondaryColor = in.readInt();
        }
    }

    public MaterialColorMapUtils(Resources resources) {
        this.sPrimaryColors = resources.obtainTypedArray(R.array.letter_tile_colors);
        this.mDefaultMaterialPalette = getDefaultPrimaryAndSecondaryColors(resources);
    }

    public static MaterialPalette getDefaultPrimaryAndSecondaryColors(Resources resources) {
        return new MaterialPalette(resources.getColor(R.color.quickcontact_default_photo_tint_color), resources.getColor(R.color.quickcontact_default_photo_tint_color_dark));
    }
}
