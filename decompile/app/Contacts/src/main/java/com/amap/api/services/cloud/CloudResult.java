package com.amap.api.services.cloud;

import com.amap.api.services.cloud.CloudSearch.Query;
import com.amap.api.services.cloud.CloudSearch.SearchBound;
import com.amap.api.services.core.g;
import java.util.ArrayList;

public final class CloudResult {
    private int a = a(this.d);
    private ArrayList<CloudItem> b;
    private g c;
    private int d;

    static CloudResult a(g gVar, ArrayList<CloudItem> arrayList) {
        return new CloudResult(gVar, arrayList);
    }

    private CloudResult(g gVar, ArrayList<CloudItem> arrayList) {
        this.c = gVar;
        this.d = gVar.i();
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

    public ArrayList<CloudItem> getClouds() {
        return this.b;
    }

    public int getTotalCount() {
        return this.d;
    }

    private int a(int i) {
        int h = this.c.h();
        return ((i + h) - 1) / h;
    }
}
