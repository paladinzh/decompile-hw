package com.amap.api.services.busline;

import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.core.b;
import java.util.ArrayList;
import java.util.List;

public final class BusStationResult {
    private int a;
    private ArrayList<BusStationItem> b = new ArrayList();
    private BusStationQuery c;
    private List<String> d = new ArrayList();
    private List<SuggestionCity> e = new ArrayList();

    static BusStationResult a(b bVar, ArrayList<?> arrayList) {
        return new BusStationResult(bVar, arrayList);
    }

    private BusStationResult(b bVar, ArrayList<?> arrayList) {
        this.c = (BusStationQuery) bVar.c();
        this.a = a(bVar.d());
        this.e = bVar.f();
        this.d = bVar.b_();
        this.b = arrayList;
    }

    private int a(int i) {
        int pageSize = this.c.getPageSize();
        pageSize = ((i + pageSize) - 1) / pageSize;
        if (pageSize <= 30) {
            return pageSize;
        }
        return 30;
    }

    public int getPageCount() {
        return this.a;
    }

    public BusStationQuery getQuery() {
        return this.c;
    }

    public List<String> getSearchSuggestionKeywords() {
        return this.d;
    }

    public List<SuggestionCity> getSearchSuggestionCities() {
        return this.e;
    }

    public List<BusStationItem> getBusStations() {
        return this.b;
    }
}
