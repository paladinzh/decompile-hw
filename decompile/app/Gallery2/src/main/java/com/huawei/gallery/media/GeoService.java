package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import com.huawei.gallery.service.AsyncService;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GeoService extends AsyncService {
    private static final MyPrinter LOG = new MyPrinter("GeoService");
    private static String[] PROJECTION = new String[]{"latitude", "longitude", "location_key", "language"};
    private Geocoder mBaseGeocoder;
    private volatile boolean mForceStop = false;
    private Locale mGeoCodeLocale = Locale.ENGLISH;
    private Map<String, String> mGeoDictionary = new HashMap(50);
    private Map<Locale, Geocoder> mGeocoderCache = new HashMap(3);
    private boolean mHashJob = false;
    private Map<Long, String> mKnowledgeCache = new HashMap(100);

    static class GeoFile {
        private static String[] PROJECTION = new String[]{"_id", "latitude", "longitude"};
        private static final Uri URI = GalleryMedia.URI;
        String geoCode;
        int id;
        double latitude;
        double longitude;

        GeoFile(Cursor c) {
            this.id = c.getInt(0);
            this.latitude = c.getDouble(1);
            this.longitude = c.getDouble(2);
        }

        void updateGeoCode(ContentResolver resolver) {
            if (!TextUtils.isEmpty(this.geoCode)) {
                ContentValues values = new ContentValues();
                values.put("geo_code", this.geoCode);
                resolver.update(URI, values, " _id = ? ", new String[]{String.valueOf(this.id)});
            }
        }
    }

    private static class KnowledgeMeta {
        String language;
        double lat;
        double lng;
        long locationKey;

        KnowledgeMeta(Cursor c) {
            this.lat = c.getDouble(0);
            this.lng = c.getDouble(1);
            this.locationKey = c.getLong(2);
            this.language = c.getString(3);
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mBaseGeocoder = new Geocoder(this, this.mGeoCodeLocale);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.mHashJob) {
            return 2;
        }
        this.mHashJob = true;
        LOG.d("--schedule-- [onStartCommand] start reverse geo code.");
        this.mForceStop = false;
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        LOG.d("--schedule-- GeoService onDestroy");
        this.mForceStop = true;
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean handleMessage(Message msg) {
        this.mHashJob = false;
        int totalCount = queryGeoFileJobCount();
        LOG.d("total " + totalCount + " should process.");
        int start = 0;
        for (int queryCount = 0; queryCount < totalCount; queryCount += 500) {
            for (GeoFile file : queryGeoFileBatch(start, 500)) {
                double lat = file.latitude;
                double lng = file.longitude;
                if (this.mForceStop) {
                    LOG.d("--schedule--  GeoService stopself");
                    stopSelf();
                    return true;
                }
                String geoCode = getGeoCodeFromKnowledge(lat, lng);
                if (geoCode == null) {
                    if (GalleryUtils.forbidWithNetwork()) {
                        LOG.d("chinese version forbid net work.");
                        start += 500;
                        break;
                    }
                    String localLocationKey = LocationFailedRecordUtils.getLocalLocationKey(ReverseGeocoder.genLocationKey(lat, lng), this.mGeoCodeLocale.getLanguage());
                    if (LocationFailedRecordUtils.skipAnalysisFailedLocation(localLocationKey)) {
                        start++;
                    } else {
                        Address address = lookupAddress(lat, lng);
                        if (address == null) {
                            LOG.d("can't reverse geo, " + file);
                            start++;
                            LocationFailedRecordUtils.rememberFailedLocationInfo(localLocationKey);
                        } else {
                            GeoKnowledge knowledge = new GeoKnowledge(address, this.mGeoCodeLocale, lat, lng);
                            if (TextUtils.isEmpty(knowledge.locality) || knowledge.locality.trim().length() == 0) {
                                LOG.w("invalide city area");
                                start++;
                                LocationFailedRecordUtils.rememberFailedLocationInfo(localLocationKey);
                            } else {
                                storeDictionaryItem(knowledge.getGeoCode(), knowledge.language, knowledge.locality);
                                geoCode = knowledge.getGeoCode();
                                learnAndRemember(lat, lng, geoCode, knowledge);
                            }
                        }
                    }
                }
                file.geoCode = geoCode;
                file.updateGeoCode(getContentResolver());
            }
        }
        if (resolveLocalAdderss()) {
            return true;
        }
        finishService();
        return true;
    }

    private boolean resolveLocalAdderss() {
        LOG.d("resolve local address");
        Locale locale = Locale.getDefault();
        if (this.mGeoCodeLocale.equals(locale)) {
            LOG.d("current is enlish, address is OK.");
            finishService();
            return true;
        } else if (GalleryUtils.forbidWithNetwork()) {
            LOG.d("chinese version forbid net work.");
            finishService();
            return true;
        } else {
            String en = this.mGeoCodeLocale.getLanguage();
            String[] args = new String[]{this.mGeoCodeLocale.getLanguage(), locale.getLanguage()};
            int totalCount = queryCount(getContentResolver(), GeoKnowledge.URI, "location_key  IN (SELECT location_key FROM (SELECT location_key,COUNT(1) cnt FROM t_geo_knowledge WHERE language = ? OR language = ? GROUP BY location_key) WHERE cnt = 1 )", args);
            LOG.d("total " + totalCount + " should process.");
            int start = 0;
            for (int queryCount = 0; queryCount < totalCount; queryCount += 500) {
                for (KnowledgeMeta record : queryRecordBatch(start, 500, "location_key  IN (SELECT location_key FROM (SELECT location_key,COUNT(1) cnt FROM t_geo_knowledge WHERE language = ? OR language = ? GROUP BY location_key) WHERE cnt = 1 )", args)) {
                    double lat = record.lat;
                    double lng = record.lng;
                    if (this.mForceStop) {
                        LOG.d("--schedule--  GeoService stopself");
                        stopSelf();
                        return true;
                    } else if (en.equals(record.language)) {
                        Geocoder geocoder = (Geocoder) this.mGeocoderCache.get(locale);
                        if (geocoder == null) {
                            geocoder = new Geocoder(this, locale);
                            this.mGeocoderCache.put(locale, geocoder);
                        }
                        if (GalleryUtils.forbidWithNetwork()) {
                            LOG.d("chinese version forbid net work.");
                            start += 500;
                            break;
                        }
                        String localLocationKey = LocationFailedRecordUtils.getLocalLocationKey(ReverseGeocoder.genLocationKey(lat, lng), locale.getLanguage());
                        if (LocationFailedRecordUtils.skipAnalysisFailedLocation(localLocationKey)) {
                            start++;
                        } else {
                            Address address = lookupAddress(geocoder, lat, lng);
                            if (address == null) {
                                LOG.d("can't resolve local geo, " + record.locationKey);
                                start++;
                                LocationFailedRecordUtils.rememberFailedLocationInfo(localLocationKey);
                            } else {
                                getContentResolver().insert(GeoKnowledge.URI, new GeoKnowledge(address, locale, lat, lng).toContentValues());
                            }
                        }
                    } else {
                        LOG.w("wrong state record for knowledge " + record.locationKey);
                        start++;
                    }
                }
            }
            return false;
        }
    }

    private void finishService() {
        if (!this.mHashJob) {
            LOG.d("--schedule-- work done, stop GeoService");
            StoryAlbumService.startStoryService(this, 1);
            stopSelf();
        }
    }

    private Address lookupAddress(double lat, double lng) {
        return lookupAddress(this.mBaseGeocoder, lat, lng);
    }

    public static Address lookupAddress(Geocoder geocoder, double lat, double lng) {
        Address addr = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses == null || addresses.isEmpty()) {
                LOG.d("cann't find address in english");
                return addr;
            }
            addr = (Address) addresses.get(0);
            return addr;
        } catch (IOException e) {
            LOG.d("reverse geo failed. " + e.getMessage());
        } catch (Exception e2) {
            LOG.d("Exception ", e2);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int queryGeoFileJobCount() {
        int ret = 0;
        try {
            Closeable c = getContentResolver().query(GalleryMedia.URI, new String[]{"COUNT(1)"}, " (geo_code = '' OR geo_code IS NULL ) AND latitude != '0.0' AND longitude != '0.0'", null, null);
            if (c != null && c.moveToNext()) {
                ret = c.getInt(0);
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query geo file job count failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<GeoFile> queryGeoFileBatch(int start, int count) {
        Uri uri = GeoFile.URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        List<GeoFile> ret = new ArrayList(count);
        try {
            Closeable c = getContentResolver().query(uri, GeoFile.PROJECTION, " (geo_code = '' OR geo_code IS NULL ) AND latitude != '0.0' AND longitude != '0.0'", null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(new GeoFile(c));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query geo file batch failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Map<String, String> queryDictionary(String language) {
        Map<String, String> ret = new HashMap(20);
        try {
            Closeable c = getContentResolver().query(GeoCode.URI, GeoCode.PROJECTION(), "language = ?", new String[]{language}, null);
            if (c != null) {
                while (c.moveToNext()) {
                    GeoCode code = new GeoCode(c);
                    ret.put(code.geoCode, code.getGeoName());
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query dictioinary error.");
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getGeoCodeFromKnowledge(double lat, double lng) {
        long key = ReverseGeocoder.genLocationKey(lat, lng);
        if (GalleryUtils.isValidLocation(lat, lng)) {
            String geoCode = (String) this.mKnowledgeCache.get(Long.valueOf(key));
            if (geoCode == null) {
                String whereClause = "latitude = ? AND longitude = ? AND language = ?";
                try {
                    Closeable c = getContentResolver().query(GeoKnowledge.URI, new String[]{"locality"}, whereClause, new String[]{String.valueOf(lat), String.valueOf(lng), this.mGeoCodeLocale.getLanguage()}, null);
                    if (c != null && c.moveToNext()) {
                        geoCode = c.getString(0);
                        this.mKnowledgeCache.put(Long.valueOf(key), geoCode);
                    }
                    Utils.closeSilently(c);
                } catch (RuntimeException e) {
                    LOG.w("query geo code  from knowledge failed . " + key);
                } catch (Throwable th) {
                    Utils.closeSilently(null);
                }
            }
            return geoCode;
        }
        LOG.d("location is invalide." + key);
        return null;
    }

    private void storeDictionaryItem(String newGeoCode, String language, String geoName) {
        Map<String, String> cache = this.mGeoDictionary;
        if (cache == null) {
            LOG.d("make up cache for geocode " + language);
            cache = queryDictionary(language);
            this.mGeoDictionary = cache;
        }
        if (cache.get(newGeoCode) != null) {
            LOG.d("dictionary item has been stored before : " + newGeoCode);
            return;
        }
        GeoCode item = new GeoCode();
        item.geoCode = newGeoCode;
        item.language = language;
        item.geoName = geoName;
        cache.put(newGeoCode, geoName);
        getContentResolver().insert(GeoCode.URI, item.toContentValues());
        LOG.d("store new dictionary item: " + item);
    }

    private void learnAndRemember(double lat, double lng, String newGeoCode, GeoKnowledge knowledge) {
        if (newGeoCode == null) {
            LOG.d("R u sure remember a invalide geo code ?");
            return;
        }
        this.mKnowledgeCache.put(Long.valueOf(ReverseGeocoder.genLocationKey(lat, lng)), newGeoCode);
        getContentResolver().insert(GeoKnowledge.URI, knowledge.toContentValues());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<KnowledgeMeta> queryRecordBatch(int start, int count, String selection, String[] selectionArgs) {
        Uri uri = GeoKnowledge.URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        List<KnowledgeMeta> ret = new ArrayList(count);
        try {
            Closeable c = getContentResolver().query(uri, PROJECTION, selection, selectionArgs, null);
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(new KnowledgeMeta(c));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query geo file batch failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int queryCount(ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) {
        int ret = 0;
        try {
            Closeable c = resolver.query(uri, new String[]{"COUNT(1)"}, selection, selectionArgs, null);
            if (c != null && c.moveToNext()) {
                ret = c.getInt(0);
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query count failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    protected String getServiceTag() {
        return "Geo service thread";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
    }
}
