package com.amap.api.services.district;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.i;

public class DistrictSearchQuery implements Parcelable, Cloneable {
    public static final Creator<DistrictSearchQuery> CREATOR = new c();
    public static final String KEYWORDS_BUSINESS = "biz_area";
    public static final String KEYWORDS_CITY = "city";
    public static final String KEYWORDS_COUNTRY = "country";
    public static final String KEYWORDS_DISTRICT = "district";
    public static final String KEYWORDS_PROVINCE = "province";
    private int a;
    private int b;
    private String c;
    private String d;
    private boolean e;
    private boolean f;

    public void setShowBoundary(boolean z) {
        this.f = z;
    }

    public boolean isShowBoundary() {
        return this.f;
    }

    public DistrictSearchQuery() {
        this.a = 0;
        this.b = 20;
        this.e = true;
        this.f = false;
    }

    public DistrictSearchQuery(String str, String str2, int i) {
        this.a = 0;
        this.b = 20;
        this.e = true;
        this.f = false;
        this.c = str;
        this.d = str2;
        this.a = i;
    }

    public DistrictSearchQuery(String str, String str2, int i, boolean z, int i2) {
        this(str, str2, i);
        this.e = z;
        this.b = i2;
    }

    public int getPageNum() {
        return this.a;
    }

    public void setPageNum(int i) {
        this.a = i;
    }

    public int getPageSize() {
        return this.b;
    }

    public void setPageSize(int i) {
        this.b = i;
    }

    public String getKeywords() {
        return this.c;
    }

    public void setKeywords(String str) {
        this.c = str;
    }

    public String getKeywordsLevel() {
        return this.d;
    }

    public void setKeywordsLevel(String str) {
        this.d = str;
    }

    public boolean isShowChild() {
        return this.e;
    }

    public void setShowChild(boolean z) {
        this.e = z;
    }

    public boolean checkLevels() {
        if (this.d == null) {
            return false;
        }
        if (this.d.trim().equals(KEYWORDS_COUNTRY) || this.d.trim().equals(KEYWORDS_PROVINCE) || this.d.trim().equals(KEYWORDS_CITY) || this.d.trim().equals(KEYWORDS_DISTRICT) || this.d.trim().equals(KEYWORDS_BUSINESS)) {
            return true;
        }
        return false;
    }

    public boolean checkKeyWords() {
        if (this.c == null || this.c.trim().equalsIgnoreCase("")) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hashCode;
        int i = 1237;
        int i2 = 0;
        int i3 = ((!this.f ? 1237 : 1231) + 31) * 31;
        if (this.c != null) {
            hashCode = this.c.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + i3) * 31;
        if (this.d != null) {
            i2 = this.d.hashCode();
        }
        hashCode = (((((hashCode + i2) * 31) + this.a) * 31) + this.b) * 31;
        if (this.e) {
            i = 1231;
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
        DistrictSearchQuery districtSearchQuery = (DistrictSearchQuery) obj;
        if (this.f != districtSearchQuery.f) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(districtSearchQuery.c)) {
                return false;
            }
        } else if (districtSearchQuery.c != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(districtSearchQuery.d)) {
                return false;
            }
        } else if (districtSearchQuery.d != null) {
            return false;
        }
        return this.a == districtSearchQuery.a && this.b == districtSearchQuery.b && this.e == districtSearchQuery.e;
    }

    protected boolean weakEquals(DistrictSearchQuery districtSearchQuery) {
        if (this == districtSearchQuery) {
            return true;
        }
        if (districtSearchQuery == null) {
            return false;
        }
        if (this.c != null) {
            if (!this.c.equals(districtSearchQuery.c)) {
                return false;
            }
        } else if (districtSearchQuery.c != null) {
            return false;
        }
        if (this.d != null) {
            if (!this.d.equals(districtSearchQuery.d)) {
                return false;
            }
        } else if (districtSearchQuery.d != null) {
            return false;
        }
        return this.b == districtSearchQuery.b && this.e == districtSearchQuery.e && this.f == districtSearchQuery.f;
    }

    public DistrictSearchQuery clone() {
        try {
            super.clone();
        } catch (Throwable e) {
            i.a(e, "DistrictSearchQuery", "clone");
        }
        DistrictSearchQuery districtSearchQuery = new DistrictSearchQuery(this.c, this.d, this.a, this.e, this.b);
        districtSearchQuery.setShowBoundary(this.f);
        return districtSearchQuery;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2;
        int i3 = 0;
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeInt(this.a);
        parcel.writeInt(this.b);
        if (this.e) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        parcel.writeByte((byte) i2);
        if (this.f) {
            i3 = 1;
        }
        parcel.writeByte((byte) i3);
    }
}
