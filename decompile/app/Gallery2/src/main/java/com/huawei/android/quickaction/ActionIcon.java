package com.huawei.android.quickaction;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class ActionIcon implements Parcelable {
    public static final Creator<ActionIcon> CREATOR = new Creator<ActionIcon>() {
        public ActionIcon createFromParcel(Parcel in) {
            return new ActionIcon(in);
        }

        public ActionIcon[] newArray(int size) {
            return new ActionIcon[size];
        }
    };
    private int mInt1;
    private Object mObj1;
    private String mString1;
    private final int mType;

    private Bitmap getBitmap() {
        if (this.mType == 1) {
            return (Bitmap) this.mObj1;
        }
        throw new IllegalStateException("called getBitmap() on " + this);
    }

    private String getResPackage() {
        if (this.mType == 2) {
            return this.mString1;
        }
        throw new IllegalStateException("called getResPackage() on " + this);
    }

    private int getResId() {
        if (this.mType == 2) {
            return this.mInt1;
        }
        throw new IllegalStateException("called getResId() on " + this);
    }

    private String getUriString() {
        if (this.mType == 3) {
            return this.mString1;
        }
        throw new IllegalStateException("called getUriString() on " + this);
    }

    private static final String typeToString(int x) {
        switch (x) {
            case 1:
                return "BITMAP";
            case 2:
                return "RESOURCE";
            case 3:
                return "URI";
            default:
                return "UNKNOWN";
        }
    }

    private ActionIcon(int mType) {
        this.mType = mType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Icon(typ=").append(typeToString(this.mType));
        switch (this.mType) {
            case 1:
                sb.append(" size=").append(getBitmap().getWidth()).append("x").append(getBitmap().getHeight());
                break;
            case 2:
                sb.append(" pkg=").append(getResPackage()).append(" id=").append(String.format("0x%08x", new Object[]{Integer.valueOf(getResId())}));
                break;
            case 3:
                sb.append(" uri=").append(getUriString());
                break;
        }
        sb.append(")");
        return sb.toString();
    }

    public int describeContents() {
        return this.mType != 1 ? 0 : 1;
    }

    ActionIcon(Parcel in) {
        this(in.readInt());
        switch (this.mType) {
            case 1:
                this.mObj1 = (Bitmap) Bitmap.CREATOR.createFromParcel(in);
                return;
            case 2:
                String pkg = in.readString();
                int resId = in.readInt();
                this.mString1 = pkg;
                this.mInt1 = resId;
                return;
            case 3:
                this.mString1 = in.readString();
                return;
            default:
                throw new RuntimeException("invalid " + getClass().getSimpleName() + " type in parcel: " + this.mType);
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        switch (this.mType) {
            case 1:
                getBitmap().writeToParcel(dest, flags);
                return;
            case 2:
                dest.writeString(getResPackage());
                dest.writeInt(getResId());
                return;
            case 3:
                dest.writeString(getUriString());
                return;
            default:
                return;
        }
    }
}
