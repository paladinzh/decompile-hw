package com.amap.api.services.busline;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.d;
import com.amap.api.services.core.i;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.a;
import java.util.ArrayList;

public class BusLineSearch {
    private Context a;
    private OnBusLineSearchListener b;
    private BusLineQuery c;
    private BusLineQuery d;
    private int e;
    private ArrayList<BusLineResult> f = new ArrayList();
    private Handler g = null;

    public interface OnBusLineSearchListener {
        void onBusLineSearched(BusLineResult busLineResult, int i);
    }

    public BusLineSearch(Context context, BusLineQuery busLineQuery) {
        this.a = context.getApplicationContext();
        this.c = busLineQuery;
        this.d = busLineQuery.clone();
        this.g = t.a();
    }

    public BusLineResult searchBusLine() throws AMapException {
        q.a(this.a);
        if (!this.c.weakEquals(this.d)) {
            this.d = this.c.clone();
            this.e = 0;
            if (this.f != null) {
                this.f.clear();
            }
        }
        BusLineResult b;
        if (this.e != 0) {
            b = b(this.c.getPageNumber());
            if (b != null) {
                return b;
            }
            d dVar = new d(this.a, this.c);
            b = BusLineResult.a(dVar, (ArrayList) dVar.a());
            this.f.set(this.c.getPageNumber(), b);
            return b;
        }
        dVar = new d(this.a, this.c.clone());
        b = BusLineResult.a(dVar, (ArrayList) dVar.a());
        this.e = b.getPageCount();
        a(b);
        return b;
    }

    private void a(BusLineResult busLineResult) {
        this.f = new ArrayList();
        for (int i = 0; i < this.e; i++) {
            this.f.add(null);
        }
        if (this.e >= 0 && a(this.c.getPageNumber())) {
            this.f.set(this.c.getPageNumber(), busLineResult);
        }
    }

    private boolean a(int i) {
        return i < this.e && i >= 0;
    }

    private BusLineResult b(int i) {
        if (a(i)) {
            return (BusLineResult) this.f.get(i);
        }
        throw new IllegalArgumentException("page out of range");
    }

    public void setOnBusLineSearchListener(OnBusLineSearchListener onBusLineSearchListener) {
        this.b = onBusLineSearchListener;
    }

    public void searchBusLineAsyn() {
        new Thread(new Runnable(this) {
            final /* synthetic */ BusLineSearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 3;
                    obtainMessage.what = 1000;
                    a aVar = new a();
                    obtainMessage.obj = aVar;
                    aVar.b = this.a.b;
                    aVar.a = this.a.searchBusLine();
                } catch (Throwable e) {
                    obtainMessage.what = e.getErrorCode();
                    i.a(e, "BusLineSearch", "searchBusLineAsyn");
                } finally {
                    this.a.g.sendMessage(obtainMessage);
                }
            }
        }).start();
    }

    public void setQuery(BusLineQuery busLineQuery) {
        if (!this.c.weakEquals(busLineQuery)) {
            this.c = busLineQuery;
            this.d = busLineQuery.clone();
        }
    }

    public BusLineQuery getQuery() {
        return this.c;
    }
}
