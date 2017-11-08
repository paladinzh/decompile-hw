package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class Cinema implements Parcelable {
    public static final Creator<Cinema> CREATOR = new a();
    private boolean a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private List<Photo> h = new ArrayList();

    public boolean isSeatOrdering() {
        return this.a;
    }

    public void setSeatOrdering(boolean z) {
        this.a = z;
    }

    public String getIntro() {
        return this.b;
    }

    public void setIntro(String str) {
        this.b = str;
    }

    public String getRating() {
        return this.c;
    }

    public void setRating(String str) {
        this.c = str;
    }

    public String getDeepsrc() {
        return this.d;
    }

    public void setDeepsrc(String str) {
        this.d = str;
    }

    public String getParking() {
        return this.e;
    }

    public void setParking(String str) {
        this.e = str;
    }

    public String getOpentimeGDF() {
        return this.f;
    }

    public void setOpentimeGDF(String str) {
        this.f = str;
    }

    public String getOpentime() {
        return this.g;
    }

    public void setOpentime(String str) {
        this.g = str;
    }

    public List<Photo> getPhotos() {
        return this.h;
    }

    public void setPhotos(List<Photo> list) {
        this.h = list;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBooleanArray(new boolean[]{this.a});
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
        parcel.writeTypedList(this.h);
    }

    public Cinema(Parcel parcel) {
        boolean[] zArr = new boolean[1];
        parcel.readBooleanArray(zArr);
        this.a = zArr[0];
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readString();
        this.g = parcel.readString();
        this.h = parcel.createTypedArrayList(Photo.CREATOR);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.d != null ? this.d.hashCode() : 0) + 31) * 31;
        if (this.b != null) {
            hashCode = this.b.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.g != null) {
            hashCode = this.g.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.f != null) {
            hashCode = this.f.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.e != null) {
            hashCode = this.e.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.h != null) {
            hashCode = this.h.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + hashCode2) * 31;
        if (this.c != null) {
            i = this.c.hashCode();
        }
        i = (hashCode + i) * 31;
        if (this.a) {
            hashCode = 1231;
        } else {
            hashCode = 1237;
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Cinema cinema = (Cinema) obj;
        if (this.d != null) {
            if (!this.d.equals(cinema.d)) {
                return false;
            }
        } else if (cinema.d != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(cinema.b)) {
                return false;
            }
        } else if (cinema.b != null) {
            return false;
        }
        if (this.g != null) {
            if (!this.g.equals(cinema.g)) {
                return false;
            }
        } else if (cinema.g != null) {
            return false;
        }
        if (this.f != null) {
            if (!this.f.equals(cinema.f)) {
                return false;
            }
        } else if (cinema.f != null) {
            return false;
        }
        if (this.e != null) {
            if (!this.e.equals(cinema.e)) {
                return false;
            }
        } else if (cinema.e != null) {
            return false;
        }
        if (this.h != null) {
            if (!this.h.equals(cinema.h)) {
                return false;
            }
        } else if (cinema.h != null) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(cinema.c)) {
                return false;
            }
        } else if (cinema.c != null) {
            return false;
        }
        return this.a == cinema.a;
    }
}
