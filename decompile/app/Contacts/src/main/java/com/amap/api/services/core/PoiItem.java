package com.amap.api.services.core;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.poisearch.IndoorData;
import com.amap.api.services.poisearch.SubPoiItem;
import java.util.ArrayList;
import java.util.List;

public class PoiItem implements Parcelable {
    public static final Creator<PoiItem> CREATOR = new x();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e = "";
    private int f = -1;
    private LatLonPoint g;
    private LatLonPoint h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    protected final LatLonPoint mPoint;
    protected final String mSnippet;
    protected final String mTitle;
    private String n;
    private String o;
    private boolean p;
    private IndoorData q;
    private String r;
    private String s;
    private String t;
    private List<SubPoiItem> u = new ArrayList();

    public PoiItem(String str, LatLonPoint latLonPoint, String str2, String str3) {
        this.a = str;
        this.mPoint = latLonPoint;
        this.mTitle = str2;
        this.mSnippet = str3;
    }

    public String getBusinessArea() {
        return this.s;
    }

    public void setBusinessArea(String str) {
        this.s = str;
    }

    public String getAdName() {
        return this.o;
    }

    public void setAdName(String str) {
        this.o = str;
    }

    public String getCityName() {
        return this.n;
    }

    public void setCityName(String str) {
        this.n = str;
    }

    public String getProvinceName() {
        return this.m;
    }

    public void setProvinceName(String str) {
        this.m = str;
    }

    public String getTypeDes() {
        return this.e;
    }

    public void setTypeDes(String str) {
        this.e = str;
    }

    public String getTel() {
        return this.b;
    }

    public void setTel(String str) {
        this.b = str;
    }

    public String getAdCode() {
        return this.c;
    }

    public void setAdCode(String str) {
        this.c = str;
    }

    public String getPoiId() {
        return this.a;
    }

    public int getDistance() {
        return this.f;
    }

    public void setDistance(int i) {
        this.f = i;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getSnippet() {
        return this.mSnippet;
    }

    public LatLonPoint getLatLonPoint() {
        return this.mPoint;
    }

    public String getCityCode() {
        return this.d;
    }

    public void setCityCode(String str) {
        this.d = str;
    }

    public LatLonPoint getEnter() {
        return this.g;
    }

    public void setEnter(LatLonPoint latLonPoint) {
        this.g = latLonPoint;
    }

    public LatLonPoint getExit() {
        return this.h;
    }

    public void setExit(LatLonPoint latLonPoint) {
        this.h = latLonPoint;
    }

    public String getWebsite() {
        return this.i;
    }

    public void setWebsite(String str) {
        this.i = str;
    }

    public String getPostcode() {
        return this.j;
    }

    public void setPostcode(String str) {
        this.j = str;
    }

    public String getEmail() {
        return this.k;
    }

    public void setEmail(String str) {
        this.k = str;
    }

    public String getDirection() {
        return this.l;
    }

    public void setDirection(String str) {
        this.l = str;
    }

    public void setIndoorMap(boolean z) {
        this.p = z;
    }

    public boolean isIndoorMap() {
        return this.p;
    }

    public void setProvinceCode(String str) {
        this.r = str;
    }

    public String getProvinceCode() {
        return this.r;
    }

    public void setParkingType(String str) {
        this.t = str;
    }

    public String getParkingType() {
        return this.t;
    }

    public void setSubPois(List<SubPoiItem> list) {
        this.u = list;
    }

    public List<SubPoiItem> getSubPois() {
        return this.u;
    }

    public IndoorData getIndoorData() {
        return this.q;
    }

    public void setIndoorDate(IndoorData indoorData) {
        this.q = indoorData;
    }

    protected PoiItem(Parcel parcel) {
        this.a = parcel.readString();
        this.c = parcel.readString();
        this.b = parcel.readString();
        this.e = parcel.readString();
        this.f = parcel.readInt();
        this.mPoint = (LatLonPoint) parcel.readValue(LatLonPoint.class.getClassLoader());
        this.mTitle = parcel.readString();
        this.mSnippet = parcel.readString();
        this.d = parcel.readString();
        this.g = (LatLonPoint) parcel.readValue(LatLonPoint.class.getClassLoader());
        this.h = (LatLonPoint) parcel.readValue(LatLonPoint.class.getClassLoader());
        this.i = parcel.readString();
        this.j = parcel.readString();
        this.k = parcel.readString();
        boolean[] zArr = new boolean[1];
        parcel.readBooleanArray(zArr);
        this.p = zArr[0];
        this.l = parcel.readString();
        this.m = parcel.readString();
        this.n = parcel.readString();
        this.o = parcel.readString();
        this.r = parcel.readString();
        this.s = parcel.readString();
        this.t = parcel.readString();
        this.u = parcel.readArrayList(SubPoiItem.class.getClassLoader());
        this.q = (IndoorData) parcel.readValue(IndoorData.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.c);
        parcel.writeString(this.b);
        parcel.writeString(this.e);
        parcel.writeInt(this.f);
        parcel.writeValue(this.mPoint);
        parcel.writeString(this.mTitle);
        parcel.writeString(this.mSnippet);
        parcel.writeString(this.d);
        parcel.writeValue(this.g);
        parcel.writeValue(this.h);
        parcel.writeString(this.i);
        parcel.writeString(this.j);
        parcel.writeString(this.k);
        parcel.writeBooleanArray(new boolean[]{this.p});
        parcel.writeString(this.l);
        parcel.writeString(this.m);
        parcel.writeString(this.n);
        parcel.writeString(this.o);
        parcel.writeString(this.r);
        parcel.writeString(this.s);
        parcel.writeString(this.t);
        parcel.writeList(this.u);
        parcel.writeValue(this.q);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PoiItem poiItem = (PoiItem) obj;
        if (this.a != null) {
            return this.a.equals(poiItem.a);
        } else {
            if (poiItem.a != null) {
                return false;
            }
        }
    }

    public int hashCode() {
        int hashCode;
        if (this.a != null) {
            hashCode = this.a.hashCode();
        } else {
            hashCode = 0;
        }
        return hashCode + 31;
    }

    public String toString() {
        return this.mTitle;
    }
}
