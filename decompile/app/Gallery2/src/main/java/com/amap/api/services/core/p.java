package com.amap.api.services.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import java.util.List;

/* compiled from: MessageHandler */
public class p extends Handler {
    private static p a;

    /* compiled from: MessageHandler */
    public static class a {
        public BusLineResult a;
        public OnBusLineSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class b {
        public BusStationResult a;
        public OnBusStationSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class c {
        public GeocodeResult a;
        public OnGeocodeSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class d {
        public PoiItemDetail a;
        public OnPoiSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class e {
        public PoiResult a;
        public OnPoiSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class f {
        public RegeocodeResult a;
        public OnGeocodeSearchListener b;
    }

    public static synchronized p a() {
        p pVar;
        synchronized (p.class) {
            if (a == null) {
                if (Looper.myLooper() != null) {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        a = new p();
                    }
                }
                a = new p(Looper.getMainLooper());
            }
            pVar = a;
        }
        return pVar;
    }

    p() {
    }

    p(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        try {
            switch (message.arg1) {
                case 1:
                    g(message);
                    return;
                case 2:
                    d(message);
                    return;
                case 3:
                    f(message);
                    return;
                case 4:
                    e(message);
                    return;
                case 5:
                    c(message);
                    return;
                case 6:
                    b(message);
                    return;
                case 7:
                    a(message);
                    return;
                default:
                    return;
            }
        } catch (Throwable th) {
            d.a(th, "MessageHandler", "handleMessage");
        }
        d.a(th, "MessageHandler", "handleMessage");
    }

    private void a(Message message) {
        b bVar = (b) message.obj;
        if (bVar != null) {
            OnBusStationSearchListener onBusStationSearchListener = bVar.b;
            if (onBusStationSearchListener != null) {
                BusStationResult busStationResult;
                if (message.what != 0) {
                    busStationResult = null;
                } else {
                    busStationResult = bVar.a;
                }
                onBusStationSearchListener.onBusStationSearched(busStationResult, message.what);
            }
        }
    }

    private void b(Message message) {
        OnPoiSearchListener onPoiSearchListener;
        Bundle data;
        if (message.what == 60) {
            e eVar = (e) message.obj;
            if (eVar != null) {
                onPoiSearchListener = eVar.b;
                if (onPoiSearchListener != null) {
                    data = message.getData();
                    if (data != null) {
                        onPoiSearchListener.onPoiSearched(eVar.a, data.getInt("errorCode"));
                    }
                }
            }
        } else if (message.what == 61) {
            d dVar = (d) message.obj;
            if (dVar != null) {
                onPoiSearchListener = dVar.b;
                data = message.getData();
                if (data != null) {
                    onPoiSearchListener.onPoiItemDetailSearched(dVar.a, data.getInt("errorCode"));
                }
            }
        }
    }

    private void c(Message message) {
        List list = null;
        InputtipsListener inputtipsListener = (InputtipsListener) message.obj;
        if (inputtipsListener != null) {
            if (message.what == 0) {
                list = message.getData().getParcelableArrayList("result");
            }
            inputtipsListener.onGetInputtips(list, message.what);
        }
    }

    private void d(Message message) {
        OnGeocodeSearchListener onGeocodeSearchListener;
        if (message.what == 21) {
            f fVar = (f) message.obj;
            if (fVar != null) {
                onGeocodeSearchListener = fVar.b;
                if (onGeocodeSearchListener != null) {
                    onGeocodeSearchListener.onRegeocodeSearched(fVar.a, message.arg2);
                }
            }
        } else if (message.what == 20) {
            c cVar = (c) message.obj;
            if (cVar != null) {
                onGeocodeSearchListener = cVar.b;
                if (onGeocodeSearchListener != null) {
                    onGeocodeSearchListener.onGeocodeSearched(cVar.a, message.arg2);
                }
            }
        }
    }

    private void e(Message message) {
        OnDistrictSearchListener onDistrictSearchListener = (OnDistrictSearchListener) message.obj;
        if (onDistrictSearchListener != null) {
            onDistrictSearchListener.onDistrictSearched((DistrictResult) message.getData().getParcelable("result"));
        }
    }

    private void f(Message message) {
        a aVar = (a) message.obj;
        if (aVar != null) {
            OnBusLineSearchListener onBusLineSearchListener = aVar.b;
            if (onBusLineSearchListener != null) {
                BusLineResult busLineResult;
                if (message.what != 0) {
                    busLineResult = null;
                } else {
                    busLineResult = aVar.a;
                }
                onBusLineSearchListener.onBusLineSearched(busLineResult, message.what);
            }
        }
    }

    private void g(Message message) {
        OnRouteSearchListener onRouteSearchListener = (OnRouteSearchListener) message.obj;
        if (onRouteSearchListener != null) {
            Bundle data;
            if (message.what == 10) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onBusRouteSearched((BusRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            } else if (message.what == 11) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onDriveRouteSearched((DriveRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            } else if (message.what == 12) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onWalkRouteSearched((WalkRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            }
        }
    }
}
