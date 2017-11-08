package com.huawei.gallery.servicemanager;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GeoCode;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiscoverLocationNameManager {
    private static final MyPrinter LOG = new MyPrinter("DiscoverLocationNameManager");
    private Application mApp;
    private Map<String, Map<String, String>> mGeoNameCache = new HashMap(100);
    private Map<Locale, Geocoder> mGeocoderCache = new HashMap(3);
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    public interface DiscoverLocationNameListener {
        void getLatLong(double[] dArr);

        void onDiscoverLocationNameFound(String str, String str2, String str3);
    }

    private class GeoNameCacheIniter implements Runnable {
        private GeoNameCacheIniter() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                Closeable c = DiscoverLocationNameManager.this.mApp.getContentResolver().query(GeoCode.URI, GeoCode.PROJECTION(), null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        DiscoverLocationNameManager.this.addToCache(new GeoCode(c));
                    }
                }
                Utils.closeSilently(c);
            } catch (RuntimeException e) {
                DiscoverLocationNameManager.LOG.d("cache geo name failed");
            } catch (Throwable th) {
                Utils.closeSilently(null);
            }
        }
    }

    private class GeoNameResolver implements Runnable {
        private String mGeoCode;
        private Locale mLanguage;
        private DiscoverLocationNameListener mListener;

        GeoNameResolver(DiscoverLocationNameListener listener, String geoCode, Locale language) {
            this.mListener = listener;
            this.mGeoCode = geoCode;
            this.mLanguage = language;
        }

        public void run() {
            try {
                Map<String, String> nameCache = (Map) DiscoverLocationNameManager.this.mGeoNameCache.get(this.mLanguage.getLanguage());
                Object obj = null;
                if (nameCache != null) {
                    obj = (String) nameCache.get(this.mGeoCode);
                }
                if (TextUtils.isEmpty(obj)) {
                    Geocoder geocoder = (Geocoder) DiscoverLocationNameManager.this.mGeocoderCache.get(this.mLanguage);
                    if (geocoder == null) {
                        geocoder = new Geocoder(DiscoverLocationNameManager.this.mApp, this.mLanguage);
                        DiscoverLocationNameManager.this.mGeocoderCache.put(this.mLanguage, geocoder);
                    }
                    double[] latlng = new double[2];
                    this.mListener.getLatLong(latlng);
                    if (GalleryUtils.forbidWithNetwork()) {
                        DiscoverLocationNameManager.LOG.d("chinese version forbid net work. " + this.mGeoCode);
                        return;
                    }
                    List<Address> addresses = geocoder.getFromLocation(latlng[0], latlng[1], 1);
                    if (addresses == null || addresses.isEmpty()) {
                        DiscoverLocationNameManager.LOG.d("cann't find address for " + this.mGeoCode + " in " + this.mLanguage);
                        return;
                    }
                    String geoName = ((Address) addresses.get(0)).getLocality();
                    if (TextUtils.isEmpty(geoName)) {
                        DiscoverLocationNameManager.LOG.d("reverse geo. invalid name.  " + this.mGeoCode);
                        return;
                    }
                    GeoCode geoCode = new GeoCode();
                    geoCode.setGeoCode(this.mGeoCode);
                    geoCode.setGeoName(this.mLanguage, geoName);
                    DiscoverLocationNameManager.this.addToCache(geoCode);
                    geoCode.insert(DiscoverLocationNameManager.this.mApp.getContentResolver());
                    this.mListener.onDiscoverLocationNameFound(this.mGeoCode, this.mLanguage.getLanguage(), geoName);
                    return;
                }
                DiscoverLocationNameManager.LOG.d("find from cache.");
                this.mListener.onDiscoverLocationNameFound(this.mGeoCode, this.mLanguage.getLanguage(), obj);
            } catch (IOException e) {
                DiscoverLocationNameManager.LOG.w("reverse geo failed  . " + e.getMessage());
            } catch (Exception e2) {
                DiscoverLocationNameManager.LOG.w("reverse geo failed  . " + e2.getMessage());
            }
        }
    }

    public DiscoverLocationNameManager(Application app) {
        this.mApp = app;
        this.mHandlerThread = new HandlerThread("Discover location name resolver thread");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mHandler.post(new GeoNameCacheIniter());
    }

    public String getGeoName(DiscoverLocationNameListener listener, String geoCode, Locale language) {
        if (language == null) {
            LOG.d("locale is null, can't rever name for " + geoCode);
            return geoCode;
        }
        Map<String, String> nameCache = (Map) this.mGeoNameCache.get(language.getLanguage());
        String str = null;
        if (nameCache != null) {
            str = (String) nameCache.get(geoCode);
        }
        if (str == null) {
            this.mHandler.post(new GeoNameResolver(listener, geoCode, language));
            str = geoCode;
        }
        return str;
    }

    private void addToCache(GeoCode geoCode) {
        String language = geoCode.getLanguage();
        Map<String, String> cacheByLang = (Map) this.mGeoNameCache.get(language);
        if (cacheByLang == null) {
            cacheByLang = new HashMap(20);
            this.mGeoNameCache.put(language, cacheByLang);
        }
        cacheByLang.put(geoCode.getGeoCode(), geoCode.getGeoName());
        LOG.d("cached: " + geoCode);
    }
}
