package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public final class Dining implements Parcelable {
    public static final Creator<Dining> CREATOR = new b();
    private boolean a;
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
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private List<Photo> t = new ArrayList();

    public boolean isMealOrdering() {
        return this.a;
    }

    public void setMealOrdering(boolean z) {
        this.a = z;
    }

    public String getCuisines() {
        return this.b;
    }

    public void setCuisines(String str) {
        this.b = str;
    }

    public String getTag() {
        return this.c;
    }

    public void setTag(String str) {
        this.c = str;
    }

    public String getIntro() {
        return this.d;
    }

    public void setIntro(String str) {
        this.d = str;
    }

    public String getRating() {
        return this.e;
    }

    public void setRating(String str) {
        this.e = str;
    }

    public String getCpRating() {
        return this.f;
    }

    public void setCpRating(String str) {
        this.f = str;
    }

    public String getDeepsrc() {
        return this.g;
    }

    public void setDeepsrc(String str) {
        this.g = str;
    }

    public String getTasteRating() {
        return this.h;
    }

    public void setTasteRating(String str) {
        this.h = str;
    }

    public String getEnvironmentRating() {
        return this.i;
    }

    public void setEnvironmentRating(String str) {
        this.i = str;
    }

    public String getServiceRating() {
        return this.j;
    }

    public void setServiceRating(String str) {
        this.j = str;
    }

    public String getCost() {
        return this.k;
    }

    public void setCost(String str) {
        this.k = str;
    }

    public String getRecommend() {
        return this.l;
    }

    public void setRecommend(String str) {
        this.l = str;
    }

    public String getAtmosphere() {
        return this.m;
    }

    public void setAtmosphere(String str) {
        this.m = str;
    }

    public String getOrderingWapUrl() {
        return this.n;
    }

    public void setOrderingWapUrl(String str) {
        this.n = str;
    }

    public String getOrderingWebUrl() {
        return this.o;
    }

    public void setOrderingWebUrl(String str) {
        this.o = str;
    }

    public String getOrderinAppUrl() {
        return this.p;
    }

    public void setOrderinAppUrl(String str) {
        this.p = str;
    }

    public String getOpentimeGDF() {
        return this.q;
    }

    public void setOpentimeGDF(String str) {
        this.q = str;
    }

    public String getOpentime() {
        return this.r;
    }

    public void setOpentime(String str) {
        this.r = str;
    }

    public String getAddition() {
        return this.s;
    }

    public void setAddition(String str) {
        this.s = str;
    }

    public List<Photo> getPhotos() {
        return this.t;
    }

    public void setPhotos(List<Photo> list) {
        this.t = list;
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
        parcel.writeString(this.h);
        parcel.writeString(this.i);
        parcel.writeString(this.j);
        parcel.writeString(this.k);
        parcel.writeString(this.l);
        parcel.writeString(this.m);
        parcel.writeString(this.n);
        parcel.writeString(this.o);
        parcel.writeString(this.p);
        parcel.writeString(this.q);
        parcel.writeString(this.r);
        parcel.writeString(this.s);
        parcel.writeTypedList(this.t);
    }

    public Dining(Parcel parcel) {
        boolean[] zArr = new boolean[1];
        parcel.readBooleanArray(zArr);
        this.a = zArr[0];
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
        this.m = parcel.readString();
        this.n = parcel.readString();
        this.o = parcel.readString();
        this.p = parcel.readString();
        this.q = parcel.readString();
        this.r = parcel.readString();
        this.s = parcel.readString();
        this.t = parcel.createTypedArrayList(Photo.CREATOR);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.s != null ? this.s.hashCode() : 0) + 31) * 31;
        if (this.m != null) {
            hashCode = this.m.hashCode();
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
        if (this.f != null) {
            hashCode = this.f.hashCode();
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
        if (this.i != null) {
            hashCode = this.i.hashCode();
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
        if (this.a) {
            hashCode = 1231;
        } else {
            hashCode = 1237;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.r != null) {
            hashCode = this.r.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.q != null) {
            hashCode = this.q.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.p != null) {
            hashCode = this.p.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.n != null) {
            hashCode = this.n.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.o != null) {
            hashCode = this.o.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.t != null) {
            hashCode = this.t.hashCode();
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
        if (this.l != null) {
            hashCode = this.l.hashCode();
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
        if (this.c != null) {
            hashCode = this.c.hashCode();
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
        Dining dining = (Dining) obj;
        if (this.s != null) {
            if (!this.s.equals(dining.s)) {
                return false;
            }
        } else if (dining.s != null) {
            return false;
        }
        if (this.m != null) {
            if (!this.m.equals(dining.m)) {
                return false;
            }
        } else if (dining.m != null) {
            return false;
        }
        if (this.k != null) {
            if (!this.k.equals(dining.k)) {
                return false;
            }
        } else if (dining.k != null) {
            return false;
        }
        if (this.f != null) {
            if (!this.f.equals(dining.f)) {
                return false;
            }
        } else if (dining.f != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(dining.b)) {
                return false;
            }
        } else if (dining.b != null) {
            return false;
        }
        if (this.g != null) {
            if (!this.g.equals(dining.g)) {
                return false;
            }
        } else if (dining.g != null) {
            return false;
        }
        if (this.i != null) {
            if (!this.i.equals(dining.i)) {
                return false;
            }
        } else if (dining.i != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(dining.d)) {
                return false;
            }
        } else if (dining.d != null) {
            return false;
        }
        if (this.a != dining.a) {
            return false;
        }
        if (this.r != null) {
            if (!this.r.equals(dining.r)) {
                return false;
            }
        } else if (dining.r != null) {
            return false;
        }
        if (this.q != null) {
            if (!this.q.equals(dining.q)) {
                return false;
            }
        } else if (dining.q != null) {
            return false;
        }
        if (this.p != null) {
            if (!this.p.equals(dining.p)) {
                return false;
            }
        } else if (dining.p != null) {
            return false;
        }
        if (this.n != null) {
            if (!this.n.equals(dining.n)) {
                return false;
            }
        } else if (dining.n != null) {
            return false;
        }
        if (this.o != null) {
            if (!this.o.equals(dining.o)) {
                return false;
            }
        } else if (dining.o != null) {
            return false;
        }
        if (this.t != null) {
            if (!this.t.equals(dining.t)) {
                return false;
            }
        } else if (dining.t != null) {
            return false;
        }
        if (this.e != null) {
            if (!this.e.equals(dining.e)) {
                return false;
            }
        } else if (dining.e != null) {
            return false;
        }
        if (this.l != null) {
            if (!this.l.equals(dining.l)) {
                return false;
            }
        } else if (dining.l != null) {
            return false;
        }
        if (this.j != null) {
            if (!this.j.equals(dining.j)) {
                return false;
            }
        } else if (dining.j != null) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(dining.c)) {
                return false;
            }
        } else if (dining.c != null) {
            return false;
        }
        if (this.h != null) {
            return this.h.equals(dining.h);
        } else {
            if (dining.h != null) {
                return false;
            }
        }
    }
}
