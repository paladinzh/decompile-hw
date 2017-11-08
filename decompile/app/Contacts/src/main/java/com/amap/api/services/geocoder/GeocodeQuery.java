package com.amap.api.services.geocoder;

public class GeocodeQuery {
    private String a;
    private String b;

    public GeocodeQuery(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public String getLocationName() {
        return this.a;
    }

    public void setLocationName(String str) {
        this.a = str;
    }

    public String getCity() {
        return this.b;
    }

    public void setCity(String str) {
        this.b = str;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        if (this.b != null) {
            hashCode = this.b.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + 31) * 31;
        if (this.a != null) {
            i = this.a.hashCode();
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
        GeocodeQuery geocodeQuery = (GeocodeQuery) obj;
        if (this.b != null) {
            if (!this.b.equals(geocodeQuery.b)) {
                return false;
            }
        } else if (geocodeQuery.b != null) {
            return false;
        }
        if (this.a != null) {
            return this.a.equals(geocodeQuery.a);
        } else {
            if (geocodeQuery.a != null) {
                return false;
            }
        }
    }
}
