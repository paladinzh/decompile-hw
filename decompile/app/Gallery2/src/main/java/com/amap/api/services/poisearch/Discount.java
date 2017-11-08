package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.d;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Discount implements Parcelable {
    public static final Creator<Discount> CREATOR = new c();
    private String a;
    private String b;
    private Date c;
    private Date d;
    private int e;
    private List<Photo> f = new ArrayList();
    private String g;
    private String h;

    public String getTitle() {
        return this.a;
    }

    public void setTitle(String str) {
        this.a = str;
    }

    public String getDetail() {
        return this.b;
    }

    public void setDetail(String str) {
        this.b = str;
    }

    public Date getStartTime() {
        if (this.c != null) {
            return (Date) this.c.clone();
        }
        return null;
    }

    public void setStartTime(Date date) {
        if (date != null) {
            this.c = (Date) date.clone();
        } else {
            this.c = null;
        }
    }

    public Date getEndTime() {
        if (this.d != null) {
            return (Date) this.d.clone();
        }
        return null;
    }

    public void setEndTime(Date date) {
        if (date != null) {
            this.d = (Date) date.clone();
        } else {
            this.d = null;
        }
    }

    public int getSoldCount() {
        return this.e;
    }

    public void setSoldCount(int i) {
        this.e = i;
    }

    public List<Photo> getPhotos() {
        return this.f;
    }

    public void addPhotos(Photo photo) {
        this.f.add(photo);
    }

    public void initPhotos(List<Photo> list) {
        if (list != null && list.size() != 0) {
            this.f.clear();
            for (Photo add : list) {
                this.f.add(add);
            }
        }
    }

    public String getUrl() {
        return this.g;
    }

    public void setUrl(String str) {
        this.g = str;
    }

    public String getProvider() {
        return this.h;
    }

    public void setProvider(String str) {
        this.h = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(d.a(this.c));
        parcel.writeString(d.a(this.d));
        parcel.writeInt(this.e);
        parcel.writeTypedList(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
    }

    public Discount(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = d.e(parcel.readString());
        this.d = d.e(parcel.readString());
        this.e = parcel.readInt();
        this.f = parcel.createTypedArrayList(Photo.CREATOR);
        this.g = parcel.readString();
        this.h = parcel.readString();
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.b != null ? this.b.hashCode() : 0) + 31) * 31;
        if (this.d != null) {
            hashCode = this.d.hashCode();
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
        if (this.h != null) {
            hashCode = this.h.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (((hashCode + hashCode2) * 31) + this.e) * 31;
        if (this.c != null) {
            hashCode = this.c.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode2 = (hashCode + hashCode2) * 31;
        if (this.a != null) {
            hashCode = this.a.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + hashCode2) * 31;
        if (this.g != null) {
            i = this.g.hashCode();
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
        Discount discount = (Discount) obj;
        if (this.b != null) {
            if (!this.b.equals(discount.b)) {
                return false;
            }
        } else if (discount.b != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(discount.d)) {
                return false;
            }
        } else if (discount.d != null) {
            return false;
        }
        if (this.f != null) {
            if (!this.f.equals(discount.f)) {
                return false;
            }
        } else if (discount.f != null) {
            return false;
        }
        if (this.h != null) {
            if (!this.h.equals(discount.h)) {
                return false;
            }
        } else if (discount.h != null) {
            return false;
        }
        if (this.e != discount.e) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(discount.c)) {
                return false;
            }
        } else if (discount.c != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(discount.a)) {
                return false;
            }
        } else if (discount.a != null) {
            return false;
        }
        if (this.g != null) {
            return this.g.equals(discount.g);
        } else {
            if (discount.g != null) {
                return false;
            }
        }
    }
}
