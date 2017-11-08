package com.amap.api.services.busline;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.d;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BusLineItem implements Parcelable {
    public static final Creator<BusLineItem> CREATOR = new a();
    private float a;
    private String b;
    private String c;
    private String d;
    private List<LatLonPoint> e = new ArrayList();
    private List<LatLonPoint> f = new ArrayList();
    private String g;
    private String h;
    private String i;
    private Date j;
    private Date k;
    private String l;
    private float m;
    private float n;
    private List<BusStationItem> o = new ArrayList();

    public float getDistance() {
        return this.a;
    }

    public void setDistance(float f) {
        this.a = f;
    }

    public String getBusLineName() {
        return this.b;
    }

    public void setBusLineName(String str) {
        this.b = str;
    }

    public String getBusLineType() {
        return this.c;
    }

    public void setBusLineType(String str) {
        this.c = str;
    }

    public String getCityCode() {
        return this.d;
    }

    public void setCityCode(String str) {
        this.d = str;
    }

    public List<LatLonPoint> getDirectionsCoordinates() {
        return this.e;
    }

    public void setDirectionsCoordinates(List<LatLonPoint> list) {
        this.e = list;
    }

    public List<LatLonPoint> getBounds() {
        return this.f;
    }

    public void setBounds(List<LatLonPoint> list) {
        this.f = list;
    }

    public String getBusLineId() {
        return this.g;
    }

    public void setBusLineId(String str) {
        this.g = str;
    }

    public String getOriginatingStation() {
        return this.h;
    }

    public void setOriginatingStation(String str) {
        this.h = str;
    }

    public String getTerminalStation() {
        return this.i;
    }

    public void setTerminalStation(String str) {
        this.i = str;
    }

    public Date getFirstBusTime() {
        if (this.j != null) {
            return (Date) this.j.clone();
        }
        return null;
    }

    public void setFirstBusTime(Date date) {
        if (date != null) {
            this.j = (Date) date.clone();
        } else {
            this.j = null;
        }
    }

    public Date getLastBusTime() {
        if (this.k != null) {
            return (Date) this.k.clone();
        }
        return null;
    }

    public void setLastBusTime(Date date) {
        if (date != null) {
            this.k = (Date) date.clone();
        } else {
            this.k = null;
        }
    }

    public String getBusCompany() {
        return this.l;
    }

    public void setBusCompany(String str) {
        this.l = str;
    }

    public float getBasicPrice() {
        return this.m;
    }

    public void setBasicPrice(float f) {
        this.m = f;
    }

    public float getTotalPrice() {
        return this.n;
    }

    public void setTotalPrice(float f) {
        this.n = f;
    }

    public List<BusStationItem> getBusStations() {
        return this.o;
    }

    public void setBusStations(List<BusStationItem> list) {
        this.o = list;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BusLineItem busLineItem = (BusLineItem) obj;
        if (this.g != null) {
            return this.g.equals(busLineItem.g);
        } else {
            if (busLineItem.g != null) {
                return false;
            }
        }
    }

    public int hashCode() {
        int hashCode;
        if (this.g != null) {
            hashCode = this.g.hashCode();
        } else {
            hashCode = 0;
        }
        return hashCode + 31;
    }

    public String toString() {
        return this.b + " " + d.a(this.j) + "-" + d.a(this.k);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeList(this.e);
        parcel.writeList(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
        parcel.writeString(this.i);
        parcel.writeString(d.a(this.j));
        parcel.writeString(d.a(this.k));
        parcel.writeString(this.l);
        parcel.writeFloat(this.m);
        parcel.writeFloat(this.n);
        parcel.writeList(this.o);
    }

    public BusLineItem(Parcel parcel) {
        this.a = parcel.readFloat();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readString();
        this.e = parcel.readArrayList(LatLonPoint.class.getClassLoader());
        this.f = parcel.readArrayList(LatLonPoint.class.getClassLoader());
        this.g = parcel.readString();
        this.h = parcel.readString();
        this.i = parcel.readString();
        this.j = d.e(parcel.readString());
        this.k = d.e(parcel.readString());
        this.l = parcel.readString();
        this.m = parcel.readFloat();
        this.n = parcel.readFloat();
        this.o = parcel.readArrayList(BusStationItem.class.getClassLoader());
    }
}
