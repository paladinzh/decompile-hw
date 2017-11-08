package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.road.Crossroad;
import com.amap.api.services.road.Road;
import java.util.ArrayList;
import java.util.List;

public final class RegeocodeAddress implements Parcelable {
    public static final Creator<RegeocodeAddress> CREATOR = new c();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private StreetNumber h;
    private String i;
    private String j;
    private List<RegeocodeRoad> k;
    private List<Crossroad> l;
    private List<PoiItem> m;
    private List<BusinessArea> n;

    public RegeocodeAddress() {
        this.k = new ArrayList();
        this.l = new ArrayList();
        this.m = new ArrayList();
        this.n = new ArrayList();
    }

    public String getFormatAddress() {
        return this.a;
    }

    public void setFormatAddress(String str) {
        this.a = str;
    }

    public String getProvince() {
        return this.b;
    }

    public void setProvince(String str) {
        this.b = str;
    }

    public String getCity() {
        return this.c;
    }

    public void setCity(String str) {
        this.c = str;
    }

    public String getCityCode() {
        return this.i;
    }

    public void setCityCode(String str) {
        this.i = str;
    }

    public String getAdCode() {
        return this.j;
    }

    public void setAdCode(String str) {
        this.j = str;
    }

    public String getDistrict() {
        return this.d;
    }

    public void setDistrict(String str) {
        this.d = str;
    }

    public String getTownship() {
        return this.e;
    }

    public void setTownship(String str) {
        this.e = str;
    }

    public String getNeighborhood() {
        return this.f;
    }

    public void setNeighborhood(String str) {
        this.f = str;
    }

    public String getBuilding() {
        return this.g;
    }

    public void setBuilding(String str) {
        this.g = str;
    }

    public StreetNumber getStreetNumber() {
        return this.h;
    }

    public void setStreetNumber(StreetNumber streetNumber) {
        this.h = streetNumber;
    }

    public List<RegeocodeRoad> getRoads() {
        return this.k;
    }

    public void setRoads(List<RegeocodeRoad> list) {
        this.k = list;
    }

    public List<PoiItem> getPois() {
        return this.m;
    }

    public void setPois(List<PoiItem> list) {
        this.m = list;
    }

    public List<Crossroad> getCrossroads() {
        return this.l;
    }

    public void setCrossroads(List<Crossroad> list) {
        this.l = list;
    }

    public List<BusinessArea> getBusinessAreas() {
        return this.n;
    }

    public void setBusinessAreas(List<BusinessArea> list) {
        this.n = list;
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
        parcel.writeValue(this.h);
        parcel.writeList(this.k);
        parcel.writeList(this.l);
        parcel.writeList(this.m);
        parcel.writeString(this.i);
        parcel.writeString(this.j);
        parcel.writeList(this.n);
    }

    private RegeocodeAddress(Parcel parcel) {
        this.k = new ArrayList();
        this.l = new ArrayList();
        this.m = new ArrayList();
        this.n = new ArrayList();
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readString();
        this.g = parcel.readString();
        this.h = (StreetNumber) parcel.readValue(StreetNumber.class.getClassLoader());
        this.k = parcel.readArrayList(Road.class.getClassLoader());
        this.l = parcel.readArrayList(Crossroad.class.getClassLoader());
        this.m = parcel.readArrayList(PoiItem.class.getClassLoader());
        this.i = parcel.readString();
        this.j = parcel.readString();
        this.n = parcel.readArrayList(BusinessArea.class.getClassLoader());
    }
}
