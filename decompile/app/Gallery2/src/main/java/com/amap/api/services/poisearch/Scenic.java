package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class Scenic implements Parcelable {
    public static final Creator<Scenic> CREATOR = new k();
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
    private String l;
    private List<Photo> m = new ArrayList();

    public String getIntro() {
        return this.a;
    }

    public void setIntro(String str) {
        this.a = str;
    }

    public String getRating() {
        return this.b;
    }

    public void setRating(String str) {
        this.b = str;
    }

    public String getDeepsrc() {
        return this.c;
    }

    public void setDeepsrc(String str) {
        this.c = str;
    }

    public String getLevel() {
        return this.d;
    }

    public void setLevel(String str) {
        this.d = str;
    }

    public String getPrice() {
        return this.e;
    }

    public void setPrice(String str) {
        this.e = str;
    }

    public String getSeason() {
        return this.f;
    }

    public void setSeason(String str) {
        this.f = str;
    }

    public String getRecommend() {
        return this.g;
    }

    public void setRecommend(String str) {
        this.g = str;
    }

    public String getTheme() {
        return this.h;
    }

    public void setTheme(String str) {
        this.h = str;
    }

    public String getOrderWapUrl() {
        return this.i;
    }

    public void setOrderWapUrl(String str) {
        this.i = str;
    }

    public String getOrderWebUrl() {
        return this.j;
    }

    public void setOrderWebUrl(String str) {
        this.j = str;
    }

    public String getOpentimeGDF() {
        return this.k;
    }

    public void setOpentimeGDF(String str) {
        this.k = str;
    }

    public String getOpentime() {
        return this.l;
    }

    public void setOpentime(String str) {
        this.l = str;
    }

    public List<Photo> getPhotos() {
        return this.m;
    }

    public void setPhotos(List<Photo> list) {
        this.m = list;
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
        parcel.writeString(this.l);
        parcel.writeTypedList(this.m);
    }

    public Scenic(Parcel parcel) {
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
        this.l = parcel.readString();
        this.m = parcel.createTypedArrayList(Photo.CREATOR);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.c != null ? this.c.hashCode() : 0) + 31) * 31;
        if (this.a != null) {
            hashCode = this.a.hashCode();
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
        if (this.k != null) {
            hashCode = this.k.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.i != null) {
            hashCode = this.i.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.j != null) {
            hashCode = this.j.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.m != null) {
            hashCode = this.m.hashCode();
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
        hashCode = (hashCode + hashCode2) * 31;
        if (this.h != null) {
            i = this.h.hashCode();
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
        Scenic scenic = (Scenic) obj;
        if (this.c != null) {
            if (!this.c.equals(scenic.c)) {
                return false;
            }
        } else if (scenic.c != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(scenic.a)) {
                return false;
            }
        } else if (scenic.a != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(scenic.d)) {
                return false;
            }
        } else if (scenic.d != null) {
            return false;
        }
        if (this.l != null) {
            if (!this.l.equals(scenic.l)) {
                return false;
            }
        } else if (scenic.l != null) {
            return false;
        }
        if (this.k != null) {
            if (!this.k.equals(scenic.k)) {
                return false;
            }
        } else if (scenic.k != null) {
            return false;
        }
        if (this.i != null) {
            if (!this.i.equals(scenic.i)) {
                return false;
            }
        } else if (scenic.i != null) {
            return false;
        }
        if (this.j != null) {
            if (!this.j.equals(scenic.j)) {
                return false;
            }
        } else if (scenic.j != null) {
            return false;
        }
        if (this.m != null) {
            if (!this.m.equals(scenic.m)) {
                return false;
            }
        } else if (scenic.m != null) {
            return false;
        }
        if (this.e != null) {
            if (!this.e.equals(scenic.e)) {
                return false;
            }
        } else if (scenic.e != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(scenic.b)) {
                return false;
            }
        } else if (scenic.b != null) {
            return false;
        }
        if (this.g != null) {
            if (!this.g.equals(scenic.g)) {
                return false;
            }
        } else if (scenic.g != null) {
            return false;
        }
        if (this.f != null) {
            if (!this.f.equals(scenic.f)) {
                return false;
            }
        } else if (scenic.f != null) {
            return false;
        }
        if (this.h != null) {
            return this.h.equals(scenic.h);
        } else {
            if (scenic.h != null) {
                return false;
            }
        }
    }
}
