package com.amap.api.services.poisearch;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import java.util.ArrayList;
import java.util.List;

public final class PoiResult {
    private int a;
    private ArrayList<PoiItem> b = new ArrayList();
    private d c;

    static PoiResult a(d dVar, ArrayList<PoiItem> arrayList) {
        return new PoiResult(dVar, arrayList);
    }

    private PoiResult(d dVar, ArrayList<PoiItem> arrayList) {
        this.c = dVar;
        this.a = a(dVar.i());
        this.b = arrayList;
    }

    public int getPageCount() {
        return this.a;
    }

    public Query getQuery() {
        return this.c.j();
    }

    public SearchBound getBound() {
        return this.c.k();
    }

    public ArrayList<PoiItem> getPois() {
        return this.b;
    }

    public List<String> getSearchSuggestionKeywords() {
        return this.c.l();
    }

    public List<SuggestionCity> getSearchSuggestionCitys() {
        return this.c.m();
    }

    private int a(int i) {
        int h = this.c.h();
        h = ((i + h) - 1) / h;
        if (h <= 30) {
            return h;
        }
        return 30;
    }
}
