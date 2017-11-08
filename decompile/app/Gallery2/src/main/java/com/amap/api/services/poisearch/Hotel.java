package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class Hotel implements Parcelable {
    public static final Creator<Hotel> CREATOR = new e();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
    private String j;
    private String k;
    private List<Photo> l = new ArrayList();

    public String getRating() {
        return this.a;
    }

    public void setRating(String str) {
        this.a = str;
    }

    public String getStar() {
        return this.b;
    }

    public void setStar(String str) {
        this.b = str;
    }

    public String getIntro() {
        return this.c;
    }

    public void setIntro(String str) {
        this.c = str;
    }

    public String getLowestPrice() {
        return this.d;
    }

    public void setLowestPrice(String str) {
        this.d = str;
    }

    public String getFaciRating() {
        return this.e;
    }

    public void setFaciRating(String str) {
        this.e = str;
    }

    public String getHealthRating() {
        return this.f;
    }

    public void setHealthRating(String str) {
        this.f = str;
    }

    public String getEnvironmentRating() {
        return this.g;
    }

    public void setEnvironmentRating(String str) {
        this.g = str;
    }

    public String getServiceRating() {
        return this.h;
    }

    public void setServiceRating(String str) {
        this.h = str;
    }

    public String getTraffic() {
        return this.i;
    }

    public void setTraffic(String str) {
        this.i = str;
    }

    public String getAddition() {
        return this.j;
    }

    public void setAddition(String str) {
        this.j = str;
    }

    public String getDeepsrc() {
        return this.k;
    }

    public void setDeepsrc(String str) {
        this.k = str;
    }

    public List<Photo> getPhotos() {
        return this.l;
    }

    public void setPhotos(List<Photo> list) {
        this.l = list;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
        parcel.writeString(this.i);
        parcel.writeString(this.j);
        parcel.writeString(this.k);
        parcel.writeTypedList(this.l);
    }

    public Hotel(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readString();
        this.g = parcel.readString();
        this.h = parcel.readString();
        this.i = parcel.readString();
        this.j = parcel.readString();
        this.k = parcel.readString();
        this.l = parcel.createTypedArrayList(Photo.CREATOR);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.j != null ? this.j.hashCode() : 0) + 31) * 31;
        if (this.k != null) {
            hashCode = this.k.hashCode();
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
        if (this.e != null) {
            hashCode = this.e.hashCode();
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
        if (this.c != null) {
            hashCode = this.c.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.d != null) {
            hashCode = this.d.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.l != null) {
            hashCode = this.l.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.a != null) {
            hashCode = this.a.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.h != null) {
            hashCode = this.h.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.b != null) {
            hashCode = this.b.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + hashCode2) * 31;
        if (this.i != null) {
            i = this.i.hashCode();
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
        Hotel hotel = (Hotel) obj;
        if (this.j != null) {
            if (!this.j.equals(hotel.j)) {
                return false;
            }
        } else if (hotel.j != null) {
            return false;
        }
        if (this.k != null) {
            if (!this.k.equals(hotel.k)) {
                return false;
            }
        } else if (hotel.k != null) {
            return false;
        }
        if (this.g != null) {
            if (!this.g.equals(hotel.g)) {
                return false;
            }
        } else if (hotel.g != null) {
            return false;
        }
        if (this.e != null) {
            if (!this.e.equals(hotel.e)) {
                return false;
            }
        } else if (hotel.e != null) {
            return false;
        }
        if (this.f != null) {
            if (!this.f.equals(hotel.f)) {
                return false;
            }
        } else if (hotel.f != null) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(hotel.c)) {
                return false;
            }
        } else if (hotel.c != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(hotel.d)) {
                return false;
            }
        } else if (hotel.d != null) {
            return false;
        }
        if (this.l != null) {
            if (!this.l.equals(hotel.l)) {
                return false;
            }
        } else if (hotel.l != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(hotel.a)) {
                return false;
            }
        } else if (hotel.a != null) {
            return false;
        }
        if (this.h != null) {
            if (!this.h.equals(hotel.h)) {
                return false;
            }
        } else if (hotel.h != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(hotel.b)) {
                return false;
            }
        } else if (hotel.b != null) {
            return false;
        }
        if (this.i != null) {
            return this.i.equals(hotel.i);
        } else {
            if (hotel.i != null) {
                return false;
            }
        }
    }
}
