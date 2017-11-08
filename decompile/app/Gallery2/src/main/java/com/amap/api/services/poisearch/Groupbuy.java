package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.d;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Groupbuy implements Parcelable {
    public static final Creator<Groupbuy> CREATOR = new d();
    private String a;
    private String b;
    private String c;
    private Date d;
    private Date e;
    private int f;
    private int g;
    private float h;
    private float i;
    private float j;
    private String k;
    private String l;
    private List<Photo> m = new ArrayList();
    private String n;
    private String o;

    public String getTypeCode() {
        return this.a;
    }

    public void setTypeCode(String str) {
        this.a = str;
    }

    public String getTypeDes() {
        return this.b;
    }

    public void setTypeDes(String str) {
        this.b = str;
    }

    public String getDetail() {
        return this.c;
    }

    public void setDetail(String str) {
        this.c = str;
    }

    public Date getStartTime() {
        if (this.d != null) {
            return (Date) this.d.clone();
        }
        return null;
    }

    public void setStartTime(Date date) {
        if (date != null) {
            this.d = (Date) date.clone();
        } else {
            this.d = null;
        }
    }

    public Date getEndTime() {
        if (this.e != null) {
            return (Date) this.e.clone();
        }
        return null;
    }

    public void setEndTime(Date date) {
        if (date != null) {
            this.e = (Date) date.clone();
        } else {
            this.e = null;
        }
    }

    public int getCount() {
        return this.f;
    }

    public void setCount(int i) {
        this.f = i;
    }

    public int getSoldCount() {
        return this.g;
    }

    public void setSoldCount(int i) {
        this.g = i;
    }

    public float getOriginalPrice() {
        return this.h;
    }

    public void setOriginalPrice(float f) {
        this.h = f;
    }

    public float getGroupbuyPrice() {
        return this.i;
    }

    public void setGroupbuyPrice(float f) {
        this.i = f;
    }

    public float getDiscount() {
        return this.j;
    }

    public void setDiscount(float f) {
        this.j = f;
    }

    public String getTicketAddress() {
        return this.k;
    }

    public void setTicketAddress(String str) {
        this.k = str;
    }

    public String getTicketTel() {
        return this.l;
    }

    public void setTicketTel(String str) {
        this.l = str;
    }

    public List<Photo> getPhotos() {
        return this.m;
    }

    public void addPhotos(Photo photo) {
        this.m.add(photo);
    }

    public void initPhotos(List<Photo> list) {
        if (list != null && list.size() != 0) {
            this.m.clear();
            for (Photo add : list) {
                this.m.add(add);
            }
        }
    }

    public String getUrl() {
        return this.n;
    }

    public void setUrl(String str) {
        this.n = str;
    }

    public String getProvider() {
        return this.o;
    }

    public void setProvider(String str) {
        this.o = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(d.a(this.d));
        parcel.writeString(d.a(this.e));
        parcel.writeInt(this.f);
        parcel.writeInt(this.g);
        parcel.writeFloat(this.h);
        parcel.writeFloat(this.i);
        parcel.writeFloat(this.j);
        parcel.writeString(this.k);
        parcel.writeString(this.l);
        parcel.writeTypedList(this.m);
        parcel.writeString(this.n);
        parcel.writeString(this.o);
    }

    public Groupbuy(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = d.e(parcel.readString());
        this.e = d.e(parcel.readString());
        this.f = parcel.readInt();
        this.g = parcel.readInt();
        this.h = parcel.readFloat();
        this.i = parcel.readFloat();
        this.j = parcel.readFloat();
        this.k = parcel.readString();
        this.l = parcel.readString();
        this.m = parcel.createTypedArrayList(Photo.CREATOR);
        this.n = parcel.readString();
        this.o = parcel.readString();
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((((this.c != null ? this.c.hashCode() : 0) + ((this.f + 31) * 31)) * 31) + Float.floatToIntBits(this.j)) * 31;
        if (this.e != null) {
            hashCode = this.e.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (((((hashCode + hashCode2) * 31) + Float.floatToIntBits(this.i)) * 31) + Float.floatToIntBits(this.h)) * 31;
        if (this.m != null) {
            hashCode = this.m.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.o != null) {
            hashCode = this.o.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (((hashCode + hashCode2) * 31) + this.g) * 31;
        if (this.d != null) {
            hashCode = this.d.hashCode();
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
        if (this.b != null) {
            hashCode = this.b.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + hashCode2) * 31;
        if (this.n != null) {
            i = this.n.hashCode();
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
        Groupbuy groupbuy = (Groupbuy) obj;
        if (this.f != groupbuy.f) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(groupbuy.c)) {
                return false;
            }
        } else if (groupbuy.c != null) {
            return false;
        }
        if (Float.floatToIntBits(this.j) != Float.floatToIntBits(groupbuy.j)) {
            return false;
        }
        if (this.e != null) {
            if (!this.e.equals(groupbuy.e)) {
                return false;
            }
        } else if (groupbuy.e != null) {
            return false;
        }
        if (Float.floatToIntBits(this.i) != Float.floatToIntBits(groupbuy.i) || Float.floatToIntBits(this.h) != Float.floatToIntBits(groupbuy.h)) {
            return false;
        }
        if (this.m != null) {
            if (!this.m.equals(groupbuy.m)) {
                return false;
            }
        } else if (groupbuy.m != null) {
            return false;
        }
        if (this.o != null) {
            if (!this.o.equals(groupbuy.o)) {
                return false;
            }
        } else if (groupbuy.o != null) {
            return false;
        }
        if (this.g != groupbuy.g) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(groupbuy.d)) {
                return false;
            }
        } else if (groupbuy.d != null) {
            return false;
        }
        if (this.k != null) {
            if (!this.k.equals(groupbuy.k)) {
                return false;
            }
        } else if (groupbuy.k != null) {
            return false;
        }
        if (this.l != null) {
            if (!this.l.equals(groupbuy.l)) {
                return false;
            }
        } else if (groupbuy.l != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(groupbuy.a)) {
                return false;
            }
        } else if (groupbuy.a != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(groupbuy.b)) {
                return false;
            }
        } else if (groupbuy.b != null) {
            return false;
        }
        if (this.n != null) {
            return this.n.equals(groupbuy.n);
        } else {
            if (groupbuy.n != null) {
                return false;
            }
        }
    }
}
