package com.amap.api.services.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.cloud.CloudItemDetail;
import com.amap.api.services.cloud.CloudResult;
import com.amap.api.services.cloud.CloudSearch.OnCloudSearchListener;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch.OnDistrictSearchListener;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.nearby.NearbySearch.NearbyListener;
import com.amap.api.services.nearby.NearbySearchResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.share.ShareSearch.OnShareSearchListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch.OnWeatherSearchListener;
import java.util.List;

/* compiled from: MessageHandler */
public class t extends Handler {
    private static t a;

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
        public CloudItemDetail a;
        public OnCloudSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class d {
        public CloudResult a;
        public OnCloudSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class e {
        public GeocodeResult a;
        public OnGeocodeSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class f {
        public List<NearbyListener> a;
        public NearbySearchResult b;
    }

    /* compiled from: MessageHandler */
    public static class g {
        public PoiItem a;
        public OnPoiSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class h {
        public PoiResult a;
        public OnPoiSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class i {
        public RegeocodeResult a;
        public OnGeocodeSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class j {
        public LocalWeatherForecastResult a;
        public OnWeatherSearchListener b;
    }

    /* compiled from: MessageHandler */
    public static class k {
        public LocalWeatherLiveResult a;
        public OnWeatherSearchListener b;
    }

    public static synchronized t a() {
        t tVar;
        synchronized (t.class) {
            if (a == null) {
                if (Looper.myLooper() != null) {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        a = new t();
                    }
                }
                a = new t(Looper.getMainLooper());
            }
            tVar = a;
        }
        return tVar;
    }

    t() {
    }

    t(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message message) {
        try {
            switch (message.arg1) {
                case 1:
                    k(message);
                    return;
                case 2:
                    h(message);
                    return;
                case 3:
                    j(message);
                    return;
                case 4:
                    i(message);
                    return;
                case 5:
                    g(message);
                    return;
                case 6:
                    f(message);
                    return;
                case 7:
                    e(message);
                    return;
                case 8:
                    d(message);
                    return;
                case 9:
                    c(message);
                    return;
                case 10:
                    b(message);
                    return;
                case 11:
                    a(message);
                    return;
                case 12:
                    l(message);
                    return;
                case 13:
                    m(message);
                    return;
                default:
                    return;
            }
        } catch (Throwable th) {
            i.a(th, "MessageHandler", "handleMessage");
        }
        i.a(th, "MessageHandler", "handleMessage");
    }

    private void a(Message message) {
        int i = message.arg2;
        OnShareSearchListener onShareSearchListener = (OnShareSearchListener) message.obj;
        String string = message.getData().getString("shareurlkey");
        if (onShareSearchListener != null) {
            switch (message.what) {
                case AMapException.CODE_AMAP_ENGINE_RESPONSE_ERROR /*1100*/:
                    onShareSearchListener.onPoiShareUrlSearched(string, i);
                    break;
                case AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR /*1101*/:
                    onShareSearchListener.onLocationShareUrlSearched(string, i);
                    break;
                case AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT /*1102*/:
                    onShareSearchListener.onNaviShareUrlSearched(string, i);
                    break;
                case AMapException.CODE_AMAP_ENGINE_RETURN_TIMEOUT /*1103*/:
                    onShareSearchListener.onBusRouteShareUrlSearched(string, i);
                    break;
                case 1104:
                    onShareSearchListener.onDrivingRouteShareUrlSearched(string, i);
                    break;
                case 1105:
                    onShareSearchListener.onWalkRouteShareUrlSearched(string, i);
                    break;
            }
        }
    }

    private void b(Message message) {
        List<NearbyListener> list = (List) message.obj;
        if (list != null && list.size() != 0) {
            for (NearbyListener onNearbyInfoUploaded : list) {
                onNearbyInfoUploaded.onNearbyInfoUploaded(message.what);
            }
        }
    }

    private void c(Message message) {
        NearbySearchResult nearbySearchResult = null;
        f fVar = (f) message.obj;
        if (fVar != null) {
            List<NearbyListener> list = fVar.a;
            if (list != null && list.size() != 0) {
                if (message.what == 1000) {
                    nearbySearchResult = fVar.b;
                }
                for (NearbyListener onNearbyInfoSearched : list) {
                    onNearbyInfoSearched.onNearbyInfoSearched(nearbySearchResult, message.what);
                }
            }
        }
    }

    private void d(Message message) {
        List<NearbyListener> list = (List) message.obj;
        if (list != null && list.size() != 0) {
            for (NearbyListener onUserInfoCleared : list) {
                onUserInfoCleared.onUserInfoCleared(message.what);
            }
        }
    }

    private void e(Message message) {
        b bVar = (b) message.obj;
        if (bVar != null) {
            OnBusStationSearchListener onBusStationSearchListener = bVar.b;
            if (onBusStationSearchListener != null) {
                BusStationResult busStationResult;
                if (message.what != 1000) {
                    busStationResult = null;
                } else {
                    busStationResult = bVar.a;
                }
                onBusStationSearchListener.onBusStationSearched(busStationResult, message.what);
            }
        }
    }

    private void f(Message message) {
        OnPoiSearchListener onPoiSearchListener;
        Bundle data;
        if (message.what == 600) {
            h hVar = (h) message.obj;
            if (hVar != null) {
                onPoiSearchListener = hVar.b;
                if (onPoiSearchListener != null) {
                    data = message.getData();
                    if (data != null) {
                        onPoiSearchListener.onPoiSearched(hVar.a, data.getInt("errorCode"));
                    }
                }
            }
        } else if (message.what == 602) {
            g gVar = (g) message.obj;
            if (gVar != null) {
                onPoiSearchListener = gVar.b;
                data = message.getData();
                if (data != null) {
                    onPoiSearchListener.onPoiItemSearched(gVar.a, data.getInt("errorCode"));
                }
            }
        }
    }

    private void g(Message message) {
        List list = null;
        InputtipsListener inputtipsListener = (InputtipsListener) message.obj;
        if (inputtipsListener != null) {
            if (message.what == 1000) {
                list = message.getData().getParcelableArrayList("result");
            }
            inputtipsListener.onGetInputtips(list, message.what);
        }
    }

    private void h(Message message) {
        OnGeocodeSearchListener onGeocodeSearchListener;
        if (message.what == 201) {
            i iVar = (i) message.obj;
            if (iVar != null) {
                onGeocodeSearchListener = iVar.b;
                if (onGeocodeSearchListener != null) {
                    onGeocodeSearchListener.onRegeocodeSearched(iVar.a, message.arg2);
                }
            }
        } else if (message.what == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
            e eVar = (e) message.obj;
            if (eVar != null) {
                onGeocodeSearchListener = eVar.b;
                if (onGeocodeSearchListener != null) {
                    onGeocodeSearchListener.onGeocodeSearched(eVar.a, message.arg2);
                }
            }
        }
    }

    private void i(Message message) {
        OnDistrictSearchListener onDistrictSearchListener = (OnDistrictSearchListener) message.obj;
        if (onDistrictSearchListener != null) {
            onDistrictSearchListener.onDistrictSearched((DistrictResult) message.getData().getParcelable("result"));
        }
    }

    private void j(Message message) {
        a aVar = (a) message.obj;
        if (aVar != null) {
            OnBusLineSearchListener onBusLineSearchListener = aVar.b;
            if (onBusLineSearchListener != null) {
                BusLineResult busLineResult;
                if (message.what != 1000) {
                    busLineResult = null;
                } else {
                    busLineResult = aVar.a;
                }
                onBusLineSearchListener.onBusLineSearched(busLineResult, message.what);
            }
        }
    }

    private void k(Message message) {
        OnRouteSearchListener onRouteSearchListener = (OnRouteSearchListener) message.obj;
        if (onRouteSearchListener != null) {
            Bundle data;
            if (message.what == 100) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onBusRouteSearched((BusRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            } else if (message.what == 101) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onDriveRouteSearched((DriveRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            } else if (message.what == 102) {
                data = message.getData();
                if (data != null) {
                    onRouteSearchListener.onWalkRouteSearched((WalkRouteResult) message.getData().getParcelable("result"), data.getInt("errorCode"));
                }
            }
        }
    }

    private void l(Message message) {
        if (message.what == 700) {
            d dVar = (d) message.obj;
            if (dVar != null) {
                dVar.b.onCloudSearched(dVar.a, message.arg2);
            }
        } else if (message.what == 701) {
            c cVar = (c) message.obj;
            if (cVar != null) {
                cVar.b.onCloudItemDetailSearched(cVar.a, message.arg2);
            }
        }
    }

    private void m(Message message) {
        OnWeatherSearchListener onWeatherSearchListener;
        Bundle data;
        if (message.what == 1301) {
            k kVar = (k) message.obj;
            if (kVar != null) {
                onWeatherSearchListener = kVar.b;
                if (onWeatherSearchListener != null) {
                    data = message.getData();
                    if (data != null) {
                        onWeatherSearchListener.onWeatherLiveSearched(kVar.a, data.getInt("errorCode"));
                    }
                }
            }
        } else if (message.what == 1302) {
            j jVar = (j) message.obj;
            if (jVar != null) {
                onWeatherSearchListener = jVar.b;
                if (onWeatherSearchListener != null) {
                    data = message.getData();
                    if (data != null) {
                        onWeatherSearchListener.onWeatherForecastSearched(jVar.a, data.getInt("errorCode"));
                    }
                }
            }
        }
    }
}
