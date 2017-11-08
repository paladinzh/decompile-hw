package com.amap.api.services.busline;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.d;
import com.amap.api.services.core.i;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.b;
import java.util.ArrayList;

public class BusStationSearch {
    private Context a;
    private OnBusStationSearchListener b;
    private BusStationQuery c;
    private BusStationQuery d;
    private ArrayList<BusStationResult> e = new ArrayList();
    private int f;
    private Handler g;

    public interface OnBusStationSearchListener {
        void onBusStationSearched(BusStationResult busStationResult, int i);
    }

    public BusStationSearch(Context context, BusStationQuery busStationQuery) {
        this.a = context.getApplicationContext();
        this.c = busStationQuery;
        this.g = t.a();
    }

    public BusStationResult searchBusStation() throws AMapException {
        q.a(this.a);
        if (!this.c.weakEquals(this.d)) {
            this.d = this.c.clone();
            this.f = 0;
            if (this.e != null) {
                this.e.clear();
            }
        }
        BusStationResult b;
        if (this.f != 0) {
            b = b(this.c.getPageNumber());
            if (b != null) {
                return b;
            }
            d dVar = new d(this.a, this.c);
            b = BusStationResult.a(dVar, (ArrayList) dVar.a());
            this.e.set(this.c.getPageNumber(), b);
            return b;
        }
        dVar = new d(this.a, this.c);
        b = BusStationResult.a(dVar, (ArrayList) dVar.a());
        this.f = b.getPageCount();
        a(b);
        return b;
    }

    private void a(BusStationResult busStationResult) {
        this.e = new ArrayList();
        for (int i = 0; i <= this.f; i++) {
            this.e.add(null);
        }
        if (this.f > 0) {
            this.e.set(this.c.getPageNumber(), busStationResult);
        }
    }

    private boolean a(int i) {
        return i <= this.f && i >= 0;
    }

    private BusStationResult b(int i) {
        if (a(i)) {
            return (BusStationResult) this.e.get(i);
        }
        throw new IllegalArgumentException("page out of range");
    }

    public void setOnBusStationSearchListener(OnBusStationSearchListener onBusStationSearchListener) {
        this.b = onBusStationSearchListener;
    }

    public void searchBusStationAsyn() {
        new Thread(new Runnable(this) {
            final /* synthetic */ BusStationSearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 7;
                    obtainMessage.what = 1000;
                    b bVar = new b();
                    bVar.b = this.a.b;
                    obtainMessage.obj = bVar;
                    bVar.a = this.a.searchBusStation();
                } catch (Throwable e) {
                    i.a(e, "BusStationSearch", "searchBusStationAsyn");
                    obtainMessage.what = e.getErrorCode();
                } finally {
                    this.a.g.sendMessage(obtainMessage);
                }
            }
        }).start();
    }

    public void setQuery(BusStationQuery busStationQuery) {
        if (!busStationQuery.weakEquals(this.c)) {
            this.c = busStationQuery;
        }
    }

    public BusStationQuery getQuery() {
        return this.c;
    }
}
