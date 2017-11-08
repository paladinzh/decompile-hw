package com.amap.api.services.busline;

import com.amap.api.services.core.d;

public class BusLineQuery {
    private String a;
    private String b;
    private int c = 10;
    private int d = 0;
    private SearchType e;

    public enum SearchType {
        BY_LINE_ID,
        BY_LINE_NAME
    }

    public BusLineQuery(String str, SearchType searchType, String str2) {
        this.a = str;
        this.e = searchType;
        this.b = str2;
        if (!a()) {
            throw new IllegalArgumentException("Empty query");
        }
    }

    private boolean a() {
        return !d.a(this.a);
    }

    public SearchType getCategory() {
        return this.e;
    }

    public String getQueryString() {
        return this.a;
    }

    public void setQueryString(String str) {
        this.a = str;
    }

    public String getCity() {
        return this.b;
    }

    public void setCity(String str) {
        this.b = str;
    }

    public int getPageSize() {
        return this.c;
    }

    public void setPageSize(int i) {
        this.c = i;
    }

    public int getPageNumber() {
        return this.d;
    }

    public void setPageNumber(int i) {
        this.d = i;
    }

    public void setCategory(SearchType searchType) {
        this.e = searchType;
    }

    protected BusLineQuery clone() {
        BusLineQuery busLineQuery = new BusLineQuery(this.a, this.e, this.b);
        busLineQuery.setPageNumber(this.d);
        busLineQuery.setPageSize(this.c);
        return busLineQuery;
    }

    protected boolean weakEquals(BusLineQuery busLineQuery) {
        return busLineQuery.getQueryString().equals(this.a) && busLineQuery.getCity().equals(this.b) && busLineQuery.getPageSize() == this.c && busLineQuery.getCategory().compareTo(this.e) == 0;
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        int hashCode2 = ((this.e != null ? this.e.hashCode() : 0) + 31) * 31;
        if (this.b != null) {
            hashCode = this.b.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (((((hashCode + hashCode2) * 31) + this.d) * 31) + this.c) * 31;
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
        BusLineQuery busLineQuery = (BusLineQuery) obj;
        if (this.e != busLineQuery.e) {
            return false;
        }
        if (this.b != null) {
            if (!this.b.equals(busLineQuery.b)) {
                return false;
            }
        } else if (busLineQuery.b != null) {
            return false;
        }
        if (this.d != busLineQuery.d || this.c != busLineQuery.c) {
            return false;
        }
        if (this.a != null) {
            return this.a.equals(busLineQuery.a);
        } else {
            if (busLineQuery.a != null) {
                return false;
            }
        }
    }
}
