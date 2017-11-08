package com.amap.api.services.district;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.AMapException;
import java.util.ArrayList;

public final class DistrictResult implements Parcelable {
    public Creator<DistrictResult> CREATOR = new b(this);
    private DistrictSearchQuery a;
    private ArrayList<DistrictItem> b = new ArrayList();
    private int c;
    private AMapException d;

    public DistrictResult(DistrictSearchQuery districtSearchQuery, ArrayList<DistrictItem> arrayList) {
        this.a = districtSearchQuery;
        this.b = arrayList;
    }

    public ArrayList<DistrictItem> getDistrict() {
        return this.b;
    }

    public void setDistrict(ArrayList<DistrictItem> arrayList) {
        this.b = arrayList;
    }

    public DistrictSearchQuery getQuery() {
        return this.a;
    }

    public void setQuery(DistrictSearchQuery districtSearchQuery) {
        this.a = districtSearchQuery;
    }

    public int getPageCount() {
        return this.c;
    }

    public void setPageCount(int i) {
        this.c = i;
    }

    public AMapException getAMapException() {
        return this.d;
    }

    public void setAMapException(AMapException aMapException) {
        this.d = aMapException;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.a, i);
        parcel.writeTypedList(this.b);
    }

    protected DistrictResult(Parcel parcel) {
        this.a = (DistrictSearchQuery) parcel.readParcelable(DistrictSearchQuery.class.getClassLoader());
        this.b = parcel.createTypedArrayList(DistrictItem.CREATOR);
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        if (this.a != null) {
            hashCode = this.a.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + 31) * 31;
        if (this.b != null) {
            i = this.b.hashCode();
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
        DistrictResult districtResult = (DistrictResult) obj;
        if (this.a != null) {
            if (!this.a.equals(districtResult.a)) {
                return false;
            }
        } else if (districtResult.a != null) {
            return false;
        }
        if (this.b != null) {
            return this.b.equals(districtResult.b);
        } else {
            if (districtResult.b != null) {
                return false;
            }
        }
    }

    public String toString() {
        return "DistrictResult [mDisQuery=" + this.a + ", mDistricts=" + this.b + "]";
    }
}
