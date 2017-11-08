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
    private j c;

    static PoiResult a(j jVar, ArrayList<PoiItem> arrayList) {
        return new PoiResult(jVar, arrayList);
    }

    private PoiResult(j jVar, ArrayList<PoiItem> arrayList) {
        this.c = jVar;
        this.a = a(jVar.i());
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
        int f = this.c.f();
        f = ((i + f) - 1) / f;
        if (f <= 30) {
            return f;
        }
        return 30;
    }
}
