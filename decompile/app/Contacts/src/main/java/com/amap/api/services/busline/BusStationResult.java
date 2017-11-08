package com.amap.api.services.busline;

import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.core.d;
import java.util.ArrayList;
import java.util.List;

public final class BusStationResult {
    private int a;
    private ArrayList<BusStationItem> b = new ArrayList();
    private BusStationQuery c;
    private List<String> d = new ArrayList();
    private List<SuggestionCity> e = new ArrayList();

    static BusStationResult a(d dVar, ArrayList<?> arrayList) {
        return new BusStationResult(dVar, arrayList);
    }

    private BusStationResult(d dVar, ArrayList<?> arrayList) {
        this.c = (BusStationQuery) dVar.h();
        this.a = a(dVar.i());
        this.e = dVar.k();
        this.d = dVar.j();
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
