package com.huawei.gallery.story.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.huawei.gallery.media.LocationFailedRecordUtils;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class LocationUtils {
    private static final MyPrinter LOG = new MyPrinter("Clustering_LocationUtils");

    public static class AddressInfo {
        public String admin_area;
        public String locality;
        public String sub_locality;

        AddressInfo(String admin_area, String locality, String sub_locality) {
            this.admin_area = admin_area;
            this.locality = locality;
            this.sub_locality = sub_locality;
        }

        AddressInfo(String admin_area, String locality) {
            this.admin_area = admin_area;
            this.locality = locality;
        }

        AddressInfo(String admin_area) {
            this.admin_area = admin_area;
        }

        public AddressInfo(Cursor c) {
            this.admin_area = c.getString(0);
            this.locality = c.getString(1);
            this.sub_locality = c.getString(2);
        }
    }

    public static class LatlngData {
        public double latitude;
        public double longitude;

        public LatlngData(Cursor c) {
            this.latitude = c.getDouble(0);
            this.longitude = c.getDouble(1);
        }

        public LatlngData(double lat, double lgt) {
            this.latitude = lat;
            this.longitude = lgt;
        }
    }

    public static class SamplingFileInfo {
        public long dateTaken;
        public LatlngData latlng;

        public SamplingFileInfo(long millisec, LatlngData latlng) {
            this.dateTaken = millisec;
            this.latlng = latlng;
        }
    }

    public static AddressInfo getPopularAddress(Map<AddressInfo, Integer> addressCountMap) {
        AddressInfo popularAddress = null;
        int addressLevel = 1;
        while (popularAddress == null) {
            popularAddress = getMostPopularAddressByLevel(addressCountMap, addressLevel);
            addressLevel++;
            if (addressLevel > 3) {
                break;
            }
        }
        return popularAddress;
    }

    public static List<SamplingFileInfo> samplingSimilarLatLngPoints(List<SamplingFileInfo> points) {
        List<SamplingFileInfo> ret = new ArrayList();
        List<SamplingFileInfo> similarPoint = new ArrayList();
        List<SamplingFileInfo> pointPool = new ArrayList();
        int i = 0;
        while (i < points.size()) {
            if (!pointPool.contains(points.get(i))) {
                int j = 0;
                while (j < points.size()) {
                    if (!pointPool.contains(points.get(j)) && calculateDistance(((SamplingFileInfo) points.get(i)).latlng, ((SamplingFileInfo) points.get(j)).latlng) <= 10.0d) {
                        similarPoint.add((SamplingFileInfo) points.get(j));
                    }
                    j++;
                }
                int samplingNum = similarPoint.size() % 5 == 0 ? similarPoint.size() / 5 : (similarPoint.size() / 5) + 1;
                ret.add((SamplingFileInfo) similarPoint.get(0));
                if (samplingNum > 1) {
                    ret.add((SamplingFileInfo) similarPoint.get(similarPoint.size() - 1));
                    for (int index = 1; index < samplingNum - 1; index++) {
                        ret.add((SamplingFileInfo) similarPoint.get((similarPoint.size() * index) / samplingNum));
                    }
                }
                pointPool.addAll(similarPoint);
                similarPoint.clear();
            }
            i++;
        }
        return ret;
    }

    public static Map<AddressInfo, List<Uri>> getAddressInfoByLatLngData(Map<LatlngData, Uri> latlngMap, ContentResolver contentResolver) {
        Map<AddressInfo, List<Uri>> addressInfo = new HashMap();
        if (latlngMap == null) {
            return addressInfo;
        }
        String language = Locale.getDefault().getLanguage();
        for (Entry<LatlngData, Uri> latlngMapEntry : latlngMap.entrySet()) {
            LatlngData latlng = (LatlngData) latlngMapEntry.getKey();
            AddressInfo newAddress = StoryAlbumUtils.queryStoryAlbumAddressInfo(latlng, contentResolver, language);
            if (newAddress != null) {
                boolean findMatch = false;
                for (Entry<AddressInfo, List<Uri>> entry : addressInfo.entrySet()) {
                    AddressInfo address = (AddressInfo) entry.getKey();
                    if (isSameAddress(newAddress, address)) {
                        ((List) addressInfo.get(address)).add((Uri) latlngMap.get(latlng));
                        findMatch = true;
                        break;
                    }
                }
                if (!findMatch) {
                    List<Uri> newUri = new ArrayList();
                    newUri.add((Uri) latlngMap.get(latlng));
                    addressInfo.put(newAddress, newUri);
                }
            } else {
                long key = ReverseGeocoder.genLocationKey(latlng.latitude, latlng.longitude);
                String localKey = LocationFailedRecordUtils.getLocalLocationKey(key, language);
                String defaultKey = LocationFailedRecordUtils.getLocalLocationKey(key, Locale.ENGLISH.getLanguage());
                if (LocationFailedRecordUtils.getPreferenceValue(localKey) == 0 && LocationFailedRecordUtils.getPreferenceValue(defaultKey) == 0) {
                    LOG.d("[" + key + "] " + Locale.getDefault().getLanguage() + " not queryed in geoservice");
                    return null;
                }
            }
        }
        return addressInfo;
    }

    public static ArrayList<Uri> getStoryAlbumSummaryUri(String clusterCode, int maxValue, ContentResolver contentResolver) {
        ArrayList<Uri> summaryUris = new ArrayList();
        LinkedHashMap<LatlngData, Uri> latlngMap = StoryAlbumUtils.queryStoryAlbumLocationData(clusterCode, contentResolver);
        LOG.d("getStoryAlbumSummaryUri for album " + clusterCode);
        Uri uri;
        if (latlngMap.size() <= maxValue) {
            for (Uri uri2 : latlngMap.values()) {
                summaryUris.add(uri2);
            }
            return summaryUris;
        }
        int videoCount = 0;
        for (Uri uri22 : latlngMap.values()) {
            if (isVideoUri(uri22.toString())) {
                videoCount++;
            }
        }
        LOG.d("video count is " + videoCount);
        List<Uri> selectedUris = getSelectedUris(getSelectCountOfUriCollection(maxValue - videoCount, getSimilarLocationUriList(latlngMap), latlngMap.size()));
        LOG.d("selected uris size: " + selectedUris.size());
        for (Entry<LatlngData, Uri> entry : latlngMap.entrySet()) {
            uri22 = (Uri) entry.getValue();
            if (selectedUris.contains(uri22) || isVideoUri(uri22.toString())) {
                summaryUris.add(uri22);
            }
        }
        return summaryUris;
    }

    private static Map<List<Uri>, Integer> getSelectCountOfUriCollection(int imageCount, List<List<Uri>> similarUriList, int totalCount) {
        Map<List<Uri>, Integer> selectCount = new HashMap();
        int leftCount = imageCount;
        int unitCount = similarUriList.size();
        int distributedCount = 0;
        int selectedCount = 0;
        if (unitCount <= imageCount) {
            selectedCount = 1;
        }
        for (List<Uri> uris : similarUriList) {
            List<Uri> uris2;
            selectCount.put(uris2, Integer.valueOf(selectedCount + (((imageCount - (unitCount * selectedCount)) * (uris2.size() - selectedCount)) / (totalCount - (unitCount * selectedCount)))));
        }
        for (Integer i : selectCount.values()) {
            distributedCount += i.intValue();
        }
        leftCount = imageCount - distributedCount;
        Map<List<Uri>, Double> selectRate = new HashMap();
        for (List<Uri> uris22 : similarUriList) {
            selectRate.put(uris22, Double.valueOf((((double) leftCount) * ((double) (uris22.size() - ((Integer) selectCount.get(uris22)).intValue()))) / ((double) (totalCount - distributedCount))));
        }
        List<List<Uri>> selected = new ArrayList();
        while (leftCount > 0) {
            double max = 0.0d;
            Object maxKey = null;
            for (Entry<List<Uri>, Double> entry : selectRate.entrySet()) {
                uris22 = (List) entry.getKey();
                if (!selected.contains(uris22) && ((Double) selectRate.get(uris22)).doubleValue() > max) {
                    max = ((Double) selectRate.get(uris22)).doubleValue();
                    maxKey = uris22;
                }
            }
            if (maxKey != null) {
                selectCount.put(maxKey, Integer.valueOf(((Integer) selectCount.get(maxKey)).intValue() + 1));
                selected.add(maxKey);
            }
            leftCount--;
        }
        return selectCount;
    }

    private static List<Uri> getSelectedUris(Map<List<Uri>, Integer> selectCount) {
        List<Uri> selectedUris = new ArrayList();
        for (Entry<List<Uri>, Integer> entry : selectCount.entrySet()) {
            int count = ((Integer) entry.getValue()).intValue();
            List<Uri> uris = (List) entry.getKey();
            switch (count) {
                case 0:
                    break;
                case 1:
                    selectedUris.add((Uri) uris.get(0));
                    break;
                case 2:
                    selectedUris.add((Uri) uris.get(0));
                    selectedUris.add((Uri) uris.get(uris.size() - 1));
                    break;
                default:
                    selectedUris.add((Uri) uris.get(0));
                    selectedUris.add((Uri) uris.get(uris.size() - 1));
                    for (int i = 1; i < count - 1; i++) {
                        selectedUris.add((Uri) uris.get(((uris.size() - 2) * i) / (count - 2)));
                    }
                    break;
            }
        }
        return selectedUris;
    }

    private static List<List<Uri>> getSimilarLocationUriList(LinkedHashMap<LatlngData, Uri> latlngMap) {
        int i;
        List<List<Uri>> ret = new ArrayList();
        List<LatlngData> locationList = new ArrayList();
        List<Uri> uriList = new ArrayList();
        locationList.addAll(latlngMap.keySet());
        uriList.addAll(latlngMap.values());
        Map<Integer, Double> distanceMap = new HashMap();
        for (i = 0; i < locationList.size() - 1; i++) {
            distanceMap.put(Integer.valueOf(i), Double.valueOf(calculateDistance((LatlngData) locationList.get(i), (LatlngData) locationList.get(i + 1))));
        }
        List<Entry<Integer, Double>> list = new ArrayList(distanceMap.entrySet());
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
                return Double.compare(((Double) o2.getValue()).doubleValue(), ((Double) o1.getValue()).doubleValue());
            }
        });
        List<Integer> divideList = new ArrayList();
        for (Entry<Integer, Double> map : list) {
            divideList.add((Integer) map.getKey());
            if (divideList.size() >= 20) {
                break;
            }
        }
        Collections.sort(divideList);
        for (i = 0; i <= divideList.size(); i++) {
            if (i == 0) {
                ret.add(uriList.subList(0, ((Integer) divideList.get(i)).intValue() + 1));
            } else if (i == divideList.size()) {
                ret.add(uriList.subList(((Integer) divideList.get(i - 1)).intValue() + 1, uriList.size()));
            } else {
                ret.add(uriList.subList(((Integer) divideList.get(i - 1)).intValue() + 1, ((Integer) divideList.get(i)).intValue() + 1));
            }
        }
        return ret;
    }

    private static AddressInfo getMostPopularAddressByLevel(Map<AddressInfo, Integer> addressCountMap, int level) {
        Iterator<Entry<AddressInfo, Integer>> iter = addressCountMap.entrySet().iterator();
        Map<AddressInfo, Integer> newMap = new HashMap();
        while (iter.hasNext()) {
            AddressInfo newAddress;
            Entry<AddressInfo, Integer> entry = (Entry) iter.next();
            AddressInfo address = (AddressInfo) entry.getKey();
            Integer count = (Integer) entry.getValue();
            switch (level) {
                case 1:
                    newAddress = new AddressInfo(address.admin_area, address.locality, address.sub_locality);
                    break;
                case 2:
                    newAddress = new AddressInfo(address.admin_area, address.locality);
                    break;
                case 3:
                    newAddress = new AddressInfo(address.admin_area);
                    break;
                default:
                    newAddress = new AddressInfo("");
                    break;
            }
            boolean findMatch = false;
            for (Entry<AddressInfo, Integer> newMapEntry : newMap.entrySet()) {
                AddressInfo entryKey = (AddressInfo) newMapEntry.getKey();
                Integer entryCount = (Integer) newMapEntry.getValue();
                if (isSameAddress(entryKey, newAddress)) {
                    newMap.put(entryKey, Integer.valueOf(entryCount.intValue() + count.intValue()));
                    findMatch = true;
                    if (!findMatch) {
                        newMap.put(newAddress, count);
                    }
                    iter.remove();
                }
            }
            if (!findMatch) {
                newMap.put(newAddress, count);
            }
            iter.remove();
        }
        addressCountMap.putAll(newMap);
        int totalAddressNum = 0;
        for (Integer count2 : addressCountMap.values()) {
            totalAddressNum += count2.intValue();
        }
        for (Entry<AddressInfo, Integer> entry2 : addressCountMap.entrySet()) {
            AddressInfo addr = (AddressInfo) entry2.getKey();
            if (((double) (((float) ((Integer) addressCountMap.get(addr)).intValue()) / ((float) totalAddressNum))) >= 0.9d) {
                return addr;
            }
        }
        return null;
    }

    private static boolean isSameAddress(AddressInfo addr1, AddressInfo addr2) {
        if (!TextUtils.isEmpty(addr1.admin_area) && !addr1.admin_area.equalsIgnoreCase(addr2.admin_area)) {
            return false;
        }
        if (TextUtils.isEmpty(addr1.admin_area) && !TextUtils.isEmpty(addr2.admin_area)) {
            return false;
        }
        if (!TextUtils.isEmpty(addr1.locality) && !addr1.locality.equalsIgnoreCase(addr2.locality)) {
            return false;
        }
        if (TextUtils.isEmpty(addr1.locality) && !TextUtils.isEmpty(addr2.locality)) {
            return false;
        }
        if (!TextUtils.isEmpty(addr1.sub_locality) && !addr1.sub_locality.equalsIgnoreCase(addr2.sub_locality)) {
            return false;
        }
        if (!TextUtils.isEmpty(addr1.sub_locality) || TextUtils.isEmpty(addr2.sub_locality)) {
            return true;
        }
        return false;
    }

    private static double calculateDistance(LatlngData latlng1, LatlngData latlng2) {
        double radLat1 = (latlng1.latitude * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude;
        double radLat2 = (latlng2.latitude * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude;
        return ((double) Math.round(10000.0d * ((2.0d * Math.asin(Math.sqrt(Math.pow(Math.sin((radLat1 - radLat2) / 2.0d), 2.0d) + ((Math.cos(radLat1) * Math.cos(radLat2)) * Math.pow(Math.sin((((latlng1.longitude - latlng2.longitude) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) / 2.0d), 2.0d))))) * 6378137.0d))) / 10000.0d;
    }

    private static boolean isVideoUri(String uri) {
        return uri.substring(uri.length() - "mp4".length(), uri.length()).equalsIgnoreCase("mp4");
    }
}
