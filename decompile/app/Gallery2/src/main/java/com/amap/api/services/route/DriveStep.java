package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.LatLonPoint;
import java.util.ArrayList;
import java.util.List;

public class DriveStep implements Parcelable {
    public static final Creator<DriveStep> CREATOR = new h();
    private String a;
    private String b;
    private String c;
    private float d;
    private float e;
    private float f;
    private String g;
    private float h;
    private List<LatLonPoint> i = new ArrayList();
    private String j;
    private String k;
    private List<RouteSearchCity> l = new ArrayList();

    public String getInstruction() {
        return this.a;
    }

    public void setInstruction(String str) {
        this.a = str;
    }

    public String getOrientation() {
        return this.b;
    }

    public void setOrientation(String str) {
        this.b = str;
    }

    public String getRoad() {
        return this.c;
    }

    public void setRoad(String str) {
        this.c = str;
    }

    public float getDistance() {
        return this.d;
    }

    public void setDistance(float f) {
        this.d = f;
    }

    public float getTolls() {
        return this.e;
    }

    public void setTolls(float f) {
        this.e = f;
    }

    public float getTollDistance() {
        return this.f;
    }

    public void setTollDistance(float f) {
        this.f = f;
    }

    public String getTollRoad() {
        return this.g;
    }

    public void setTollRoad(String str) {
        this.g = str;
    }

    public float getDuration() {
        return this.h;
    }

    public void setDuration(float f) {
        this.h = f;
    }

    public List<LatLonPoint> getPolyline() {
        return this.i;
    }

    public void setPolyline(List<LatLonPoint> list) {
        this.i = list;
    }

    public String getAction() {
        return this.j;
    }

    public void setAction(String str) {
        this.j = str;
    }

    public String getAssistantAction() {
        return this.k;
    }

    public void setAssistantAction(String str) {
        this.k = str;
    }

    public List<RouteSearchCity> getRouteSearchCityList() {
        return this.l;
    }

    public void setRouteSearchCityList(List<RouteSearchCity> list) {
        this.l = list;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeFloat(this.d);
        parcel.writeFloat(this.e);
        parcel.writeFloat(this.f);
        parcel.writeString(this.g);
        parcel.writeFloat(this.h);
        parcel.writeTypedList(this.i);
        parcel.writeString(this.j);
        parcel.writeString(this.k);
        parcel.writeTypedList(this.l);
    }

    public DriveStep(Parcel parcel) {
        this.a = parcel.readString();
        this.b = parcel.readString();
        this.c = parcel.readString();
        this.d = parcel.readFloat();
        this.e = parcel.readFloat();
        this.f = parcel.readFloat();
        this.g = parcel.readString();
        this.h = parcel.readFloat();
        this.i = parcel.createTypedArrayList(LatLonPoint.CREATOR);
        this.j = parcel.readString();
        this.k = parcel.readString();
        this.l = parcel.createTypedArrayList(RouteSearchCity.CREATOR);
    }
}
