package com.amap.api.services.weather;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.ae;
import com.amap.api.services.core.af;
import com.amap.api.services.core.i;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.j;
import com.amap.api.services.core.t.k;

public class WeatherSearch {
    private Context a;
    private WeatherSearchQuery b;
    private OnWeatherSearchListener c;
    private LocalWeatherLiveResult d;
    private LocalWeatherForecastResult e;
    private Handler f = null;

    public interface OnWeatherSearchListener {
        void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i);

        void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i);
    }

    public WeatherSearch(Context context) {
        this.a = context;
        this.f = t.a();
    }

    public WeatherSearchQuery getQuery() {
        return this.b;
    }

    public void setQuery(WeatherSearchQuery weatherSearchQuery) {
        this.b = weatherSearchQuery;
    }

    public void searchWeatherAsyn() {
        new Thread(new Runnable(this) {
            final /* synthetic */ WeatherSearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.arg1 = 13;
                Bundle bundle = new Bundle();
                if (this.a.b.getType() == 1) {
                    try {
                        this.a.d = this.a.a();
                        bundle.putInt("errorCode", 1000);
                    } catch (Throwable e) {
                        bundle.putInt("errorCode", e.getErrorCode());
                        i.a(e, "WeatherSearch", "searchWeatherAsyn");
                    } catch (Throwable e2) {
                        i.a(e2, "WeatherSearch", "searchWeatherAnsyThrowable");
                    } finally {
                        k kVar = new k();
                        obtainMessage.what = 1301;
                        kVar.b = this.a.c;
                        kVar.a = this.a.d;
                        obtainMessage.obj = kVar;
                        obtainMessage.setData(bundle);
                        this.a.f.sendMessage(obtainMessage);
                    }
                } else if (this.a.b.getType() == 2) {
                    try {
                        this.a.e = this.a.b();
                        bundle.putInt("errorCode", 1000);
                    } catch (Throwable e22) {
                        bundle.putInt("errorCode", e22.getErrorCode());
                        i.a(e22, "WeatherSearch", "searchWeatherAsyn");
                    } catch (Throwable e222) {
                        i.a(e222, "WeatherSearch", "searchWeatherAnsyThrowable");
                    } finally {
                        j jVar = new j();
                        obtainMessage.what = 1302;
                        jVar.b = this.a.c;
                        jVar.a = this.a.e;
                        obtainMessage.obj = jVar;
                        obtainMessage.setData(bundle);
                        this.a.f.sendMessage(obtainMessage);
                    }
                }
            }
        }).start();
    }

    private LocalWeatherLiveResult a() throws AMapException {
        q.a(this.a);
        if (this.b != null) {
            af afVar = new af(this.a, this.b);
            return LocalWeatherLiveResult.a(afVar, (LocalWeatherLive) afVar.a());
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    private LocalWeatherForecastResult b() throws AMapException {
        q.a(this.a);
        if (this.b != null) {
            ae aeVar = new ae(this.a, this.b);
            return LocalWeatherForecastResult.a(aeVar, (LocalWeatherForecast) aeVar.a());
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    public void setOnWeatherSearchListener(OnWeatherSearchListener onWeatherSearchListener) {
        this.c = onWeatherSearchListener;
    }
}
