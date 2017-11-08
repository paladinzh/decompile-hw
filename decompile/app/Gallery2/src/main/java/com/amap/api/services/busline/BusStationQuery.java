package com.amap.api.services.busline;

import com.amap.api.services.core.d;

public class BusStationQuery {
    private String a;
    private String b;
    private int c = 10;
    private int d = 0;

    public BusStationQuery(String str, String str2) {
        this.a = str;
        this.b = str2;
        if (!a()) {
            throw new IllegalArgumentException("Empty query");
        }
    }

    private boolean a() {
        return !d.a(this.a);
    }

    public String getQueryString() {
        return this.a;
    }

    public String getCity() {
        return this.b;
    }

    public int getPageSize() {
        return this.c;
    }

    public int getPageNumber() {
        return this.d;
    }

    public void setQueryString(String str) {
        this.a = str;
    }

    public void setCity(String str) {
        this.b = str;
    }

    public void setPageSize(int i) {
        int i2 = 20;
        if (i <= 20) {
            i2 = i;
        }
        if (i2 <= 0) {
            i2 = 10;
        }
        this.c = i2;
    }

    public void setPageNumber(int i) {
        this.d = i;
    }

    protected BusStationQuery clone() {
        BusStationQuery busStationQuery = new BusStationQuery(this.a, this.b);
        busStationQuery.setPageNumber(this.d);
        busStationQuery.setPageSize(this.c);
        return busStationQuery;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((((((this.b != null ? this.b.hashCode() : 0) + 31) * 31) + this.d) * 31) + this.c) * 31;
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
        BusStationQuery busStationQuery = (BusStationQuery) obj;
        if (this.b != null) {
            if (!this.b.equals(busStationQuery.b)) {
                return false;
            }
        } else if (busStationQuery.b != null) {
            return false;
        }
        if (this.d != busStationQuery.d || this.c != busStationQuery.c) {
            return false;
        }
        if (this.a != null) {
            return this.a.equals(busStationQuery.a);
        } else {
            if (busStationQuery.a != null) {
                return false;
            }
        }
    }

    protected boolean weakEquals(BusStationQuery busStationQuery) {
        if (this == busStationQuery) {
            return true;
        }
        if (busStationQuery == null) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(busStationQuery.b)) {
                return false;
            }
        } else if (busStationQuery.b != null) {
            return false;
        }
        if (this.c != busStationQuery.c) {
            return false;
        }
        if (this.a != null) {
            return this.a.equals(busStationQuery.a);
        } else {
            if (busStationQuery.a != null) {
                return false;
            }
        }
    }
}
