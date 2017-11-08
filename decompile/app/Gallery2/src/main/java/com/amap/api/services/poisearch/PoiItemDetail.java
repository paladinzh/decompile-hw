package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

public class PoiItemDetail extends PoiItem implements Parcelable {
    public static final Creator<PoiItemDetail> CREATOR = new h();
    private List<Groupbuy> a;
    private List<Discount> b;
    private Dining c;
    private Hotel d;
    private Cinema e;
    private Scenic f;
    private DeepType g;

    public enum DeepType {
        UNKNOWN,
        DINING,
        HOTEL,
        CINEMA,
        SCENIC
    }

    public PoiItemDetail(String str, LatLonPoint latLonPoint, String str2, String str3) {
        super(str, latLonPoint, str2, str3);
        this.a = new ArrayList();
        this.b = new ArrayList();
    }

    public List<Groupbuy> getGroupbuys() {
        return this.a;
    }

    public void initGroupbuys(List<Groupbuy> list) {
        if (list != null && list.size() != 0) {
            for (Groupbuy add : list) {
                this.a.add(add);
            }
        }
    }

    public void addGroupbuy(Groupbuy groupbuy) {
        this.a.add(groupbuy);
    }

    public List<Discount> getDiscounts() {
        return this.b;
    }

    public void initDiscounts(List<Discount> list) {
        if (list != null && list.size() != 0) {
            this.b.clear();
            for (Discount add : list) {
                this.b.add(add);
            }
        }
    }

    public void addDiscount(Discount discount) {
        this.b.add(discount);
    }

    public DeepType getDeepType() {
        return this.g;
    }

    public void setDeepType(DeepType deepType) {
        this.g = deepType;
    }

    public Dining getDining() {
        return this.c;
    }

    public void setDining(Dining dining) {
        this.c = dining;
    }

    public Hotel getHotel() {
        return this.d;
    }

    public void setHotel(Hotel hotel) {
        this.d = hotel;
    }

    public Cinema getCinema() {
        return this.e;
    }

    public void setCinema(Cinema cinema) {
        this.e = cinema;
    }

    public Scenic getScenic() {
        return this.f;
    }

    public void setScenic(Scenic scenic) {
        this.f = scenic;
    }

    private PoiItemDetail(Parcel parcel) {
        super(parcel);
        this.a = new ArrayList();
        this.b = new ArrayList();
        this.a = parcel.readArrayList(Groupbuy.class.getClassLoader());
        this.b = parcel.readArrayList(Discount.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeList(this.a);
        parcel.writeList(this.b);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.e != null ? this.e.hashCode() : 0) + (super.hashCode() * 31)) * 31;
        if (this.g != null) {
            hashCode = this.g.hashCode();
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
        if (this.b != null) {
            hashCode = this.b.hashCode();
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
        if (this.d != null) {
            hashCode = this.d.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + hashCode2) * 31;
        if (this.f != null) {
            i = this.f.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        PoiItemDetail poiItemDetail = (PoiItemDetail) obj;
        if (this.e != null) {
            if (!this.e.equals(poiItemDetail.e)) {
                return false;
            }
        } else if (poiItemDetail.e != null) {
            return false;
        }
        if (this.g != poiItemDetail.g) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(poiItemDetail.c)) {
                return false;
            }
        } else if (poiItemDetail.c != null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(poiItemDetail.b)) {
                return false;
            }
        } else if (poiItemDetail.b != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(poiItemDetail.a)) {
                return false;
            }
        } else if (poiItemDetail.a != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(poiItemDetail.d)) {
                return false;
            }
        } else if (poiItemDetail.d != null) {
            return false;
        }
        if (this.f != null) {
            return this.f.equals(poiItemDetail.f);
        } else {
            if (poiItemDetail.f != null) {
                return false;
            }
        }
    }
}
