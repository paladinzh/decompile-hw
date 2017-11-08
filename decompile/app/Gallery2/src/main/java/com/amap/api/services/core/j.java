package com.amap.api.services.core;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.BusinessArea;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.Cinema;
import com.amap.api.services.poisearch.Dining;
import com.amap.api.services.poisearch.Discount;
import com.amap.api.services.poisearch.Groupbuy;
import com.amap.api.services.poisearch.Hotel;
import com.amap.api.services.poisearch.Photo;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiItemDetail.DeepType;
import com.amap.api.services.poisearch.Scenic;
import com.amap.api.services.road.Crossroad;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.District;
import com.amap.api.services.route.Doorway;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteSearchCity;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.huawei.watermark.manager.parse.util.ParseJson;
import com.huawei.watermark.ui.WMEditor;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: JSONHelper */
public class j {
    public static ArrayList<SuggestionCity> a(JSONObject jSONObject) throws JSONException, NumberFormatException {
        ArrayList<SuggestionCity> arrayList = new ArrayList();
        if (!jSONObject.has("cities")) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("cities");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(new SuggestionCity(b(optJSONObject, "name"), b(optJSONObject, "citycode"), b(optJSONObject, "adcode"), i(b(optJSONObject, "num"))));
            }
        }
        return arrayList;
    }

    public static ArrayList<String> b(JSONObject jSONObject) throws JSONException {
        ArrayList<String> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("keywords");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            arrayList.add(optJSONArray.optString(i));
        }
        return arrayList;
    }

    public static ArrayList<PoiItem> c(JSONObject jSONObject) throws JSONException {
        ArrayList<PoiItem> arrayList = new ArrayList();
        if (jSONObject == null) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("pois");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(d(optJSONObject));
            }
        }
        return arrayList;
    }

    public static PoiItemDetail d(JSONObject jSONObject) throws JSONException {
        PoiItemDetail poiItemDetail = new PoiItemDetail(b(jSONObject, "id"), c(jSONObject, "location"), b(jSONObject, "name"), b(jSONObject, "address"));
        poiItemDetail.setAdCode(b(jSONObject, "adcode"));
        poiItemDetail.setProvinceName(b(jSONObject, "pname"));
        poiItemDetail.setCityName(b(jSONObject, "cityname"));
        poiItemDetail.setAdName(b(jSONObject, "adname"));
        poiItemDetail.setCityCode(b(jSONObject, "citycode"));
        poiItemDetail.setProvinceCode(b(jSONObject, "pcode"));
        poiItemDetail.setDirection(b(jSONObject, "direction"));
        if (jSONObject.has("distance")) {
            String b = b(jSONObject, "distance");
            if (!h(b)) {
                try {
                    poiItemDetail.setDistance((int) Float.parseFloat(b));
                } catch (Throwable e) {
                    d.a(e, "JSONHelper", "parseBasePoi");
                } catch (Throwable e2) {
                    d.a(e2, "JSONHelper", "parseBasePoi");
                }
                if (poiItemDetail.getDistance() == 0) {
                    poiItemDetail.setDistance(-1);
                }
            }
        }
        poiItemDetail.setTel(b(jSONObject, "tel"));
        poiItemDetail.setTypeDes(b(jSONObject, "type"));
        poiItemDetail.setEnter(c(jSONObject, "entr_location"));
        poiItemDetail.setExit(c(jSONObject, "exit_location"));
        poiItemDetail.setWebsite(b(jSONObject, "website"));
        poiItemDetail.setPostcode(b(jSONObject, "citycode"));
        poiItemDetail.setEmail(b(jSONObject, "email"));
        if (g(b(jSONObject, "groupbuy_num"))) {
            poiItemDetail.setGroupbuyInfo(false);
        } else {
            poiItemDetail.setGroupbuyInfo(true);
        }
        if (g(b(jSONObject, "discount_num"))) {
            poiItemDetail.setDiscountInfo(false);
        } else {
            poiItemDetail.setDiscountInfo(true);
        }
        if (g(b(jSONObject, "indoor_map"))) {
            poiItemDetail.setIndoorMap(false);
        } else {
            poiItemDetail.setIndoorMap(true);
        }
        return poiItemDetail;
    }

    public static ArrayList<BusStationItem> e(JSONObject jSONObject) throws JSONException {
        ArrayList<BusStationItem> arrayList = new ArrayList();
        if (jSONObject == null) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("busstops");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(f(optJSONObject));
            }
        }
        return arrayList;
    }

    public static BusStationItem f(JSONObject jSONObject) throws JSONException {
        BusStationItem g = g(jSONObject);
        if (g == null) {
            return g;
        }
        g.setAdCode(b(jSONObject, "adcode"));
        g.setCityCode(b(jSONObject, "citycode"));
        JSONArray optJSONArray = jSONObject.optJSONArray("buslines");
        List arrayList = new ArrayList();
        if (optJSONArray != null) {
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(h(optJSONObject));
                }
            }
            g.setBusLineItems(arrayList);
            return g;
        }
        g.setBusLineItems(arrayList);
        return g;
    }

    public static BusStationItem g(JSONObject jSONObject) throws JSONException {
        BusStationItem busStationItem = new BusStationItem();
        busStationItem.setBusStationId(b(jSONObject, "id"));
        busStationItem.setLatLonPoint(c(jSONObject, "location"));
        busStationItem.setBusStationName(b(jSONObject, "name"));
        return busStationItem;
    }

    public static BusLineItem h(JSONObject jSONObject) throws JSONException {
        BusLineItem busLineItem = new BusLineItem();
        busLineItem.setBusLineId(b(jSONObject, "id"));
        busLineItem.setBusLineType(b(jSONObject, "type"));
        busLineItem.setBusLineName(b(jSONObject, "name"));
        busLineItem.setDirectionsCoordinates(d(jSONObject, "polyline"));
        busLineItem.setCityCode(b(jSONObject, "citycode"));
        busLineItem.setOriginatingStation(b(jSONObject, "start_stop"));
        busLineItem.setTerminalStation(b(jSONObject, "end_stop"));
        return busLineItem;
    }

    public static ArrayList<BusLineItem> i(JSONObject jSONObject) throws JSONException {
        ArrayList<BusLineItem> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("buslines");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(j(optJSONObject));
            }
        }
        return arrayList;
    }

    public static BusLineItem j(JSONObject jSONObject) throws JSONException {
        BusLineItem h = h(jSONObject);
        if (h == null) {
            return h;
        }
        h.setFirstBusTime(d.d(b(jSONObject, "start_time")));
        h.setLastBusTime(d.d(b(jSONObject, "end_time")));
        h.setBusCompany(b(jSONObject, "company"));
        h.setDistance(j(b(jSONObject, "distance")));
        h.setBasicPrice(j(b(jSONObject, "basic_price")));
        h.setTotalPrice(j(b(jSONObject, "total_price")));
        h.setBounds(d(jSONObject, "bounds"));
        List arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("busstops");
        if (optJSONArray != null) {
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(g(optJSONObject));
                }
            }
            h.setBusStations(arrayList);
            return h;
        }
        h.setBusStations(arrayList);
        return h;
    }

    public static Scenic a(PoiItemDetail poiItemDetail, JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        Scenic scenic = new Scenic();
        scenic.setIntro(b(jSONObject, "intro"));
        scenic.setRating(b(jSONObject, "rating"));
        scenic.setDeepsrc(b(jSONObject, "deepsrc"));
        scenic.setLevel(b(jSONObject, ParseJson.LEVEL));
        scenic.setPrice(b(jSONObject, "price"));
        scenic.setSeason(b(jSONObject, "season"));
        scenic.setRecommend(b(jSONObject, "recommend"));
        scenic.setTheme(b(jSONObject, "theme"));
        scenic.setOrderWapUrl(b(jSONObject, "ordering_wap_url"));
        scenic.setOrderWebUrl(b(jSONObject, "ordering_web_url"));
        scenic.setOpentimeGDF(b(jSONObject, "opentime_GDF"));
        scenic.setOpentime(b(jSONObject, "opentime"));
        scenic.setPhotos(l(jSONObject));
        poiItemDetail.setDeepType(DeepType.SCENIC);
        poiItemDetail.setScenic(scenic);
        return scenic;
    }

    public static void b(PoiItemDetail poiItemDetail, JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        Cinema cinema = new Cinema();
        cinema.setIntro(b(jSONObject, "intro"));
        cinema.setRating(b(jSONObject, "rating"));
        cinema.setDeepsrc(b(jSONObject, "deepsrc"));
        cinema.setParking(b(jSONObject, "parking"));
        cinema.setOpentimeGDF(b(jSONObject, "opentime_GDF"));
        cinema.setOpentime(b(jSONObject, "opentime"));
        cinema.setPhotos(l(jSONObject));
        if (k(jSONObject2)) {
            cinema.setSeatOrdering(a(jSONObject2, "seat_ordering"));
        }
        poiItemDetail.setDeepType(DeepType.CINEMA);
        poiItemDetail.setCinema(cinema);
    }

    public static void c(PoiItemDetail poiItemDetail, JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        Hotel hotel = new Hotel();
        hotel.setStar(b(jSONObject, "star"));
        hotel.setIntro(b(jSONObject, "intro"));
        hotel.setRating(b(jSONObject, "rating"));
        hotel.setLowestPrice(b(jSONObject, "lowest_price"));
        hotel.setDeepsrc(b(jSONObject, "deepsrc"));
        hotel.setFaciRating(b(jSONObject, "faci_rating"));
        hotel.setHealthRating(b(jSONObject, "health_rating"));
        hotel.setEnvironmentRating(b(jSONObject, "environment_rating"));
        hotel.setServiceRating(b(jSONObject, "service_rating"));
        hotel.setTraffic(b(jSONObject, "traffic"));
        hotel.setAddition(b(jSONObject, "addition"));
        hotel.setPhotos(l(jSONObject));
        poiItemDetail.setDeepType(DeepType.HOTEL);
        poiItemDetail.setHotel(hotel);
    }

    public static void d(PoiItemDetail poiItemDetail, JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        Dining dining = new Dining();
        dining.setCuisines(b(jSONObject, "cuisines"));
        dining.setTag(b(jSONObject, "tag"));
        dining.setIntro(b(jSONObject, "intro"));
        dining.setRating(b(jSONObject, "rating"));
        dining.setCpRating(b(jSONObject, "cp_rating"));
        dining.setDeepsrc(b(jSONObject, "deepsrc"));
        dining.setTasteRating(b(jSONObject, "taste_rating"));
        dining.setEnvironmentRating(b(jSONObject, "environment_rating"));
        dining.setServiceRating(b(jSONObject, "service_rating"));
        dining.setCost(b(jSONObject, "cost"));
        dining.setRecommend(b(jSONObject, "recommend"));
        dining.setAtmosphere(b(jSONObject, "atmosphere"));
        dining.setOrderingWapUrl(b(jSONObject, "ordering_wap_url"));
        dining.setOrderingWebUrl(b(jSONObject, "ordering_web_url"));
        dining.setOrderinAppUrl(b(jSONObject, "ordering_app_url"));
        dining.setOpentimeGDF(b(jSONObject, "opentime_GDF"));
        dining.setOpentime(b(jSONObject, "opentime"));
        dining.setAddition(b(jSONObject, "addition"));
        dining.setPhotos(l(jSONObject));
        if (k(jSONObject2)) {
            dining.setMealOrdering(a(jSONObject2, "meal_ordering"));
        }
        poiItemDetail.setDeepType(DeepType.DINING);
        poiItemDetail.setDining(dining);
    }

    public static void e(PoiItemDetail poiItemDetail, JSONObject jSONObject, JSONObject jSONObject2) throws JSONException {
        if (jSONObject != null) {
            String b = b(jSONObject, "type");
            if (b.equalsIgnoreCase("hotel")) {
                c(poiItemDetail, jSONObject, jSONObject2);
            }
            if (b.equalsIgnoreCase("dining")) {
                d(poiItemDetail, jSONObject, jSONObject2);
            }
            if (b.equalsIgnoreCase("cinema")) {
                b(poiItemDetail, jSONObject, jSONObject2);
            }
            if (b.equalsIgnoreCase("scenic")) {
                a(poiItemDetail, jSONObject, jSONObject2);
            }
        }
    }

    public static boolean a(JSONObject jSONObject, String str) throws JSONException {
        return a(b(jSONObject.optJSONObject("biz_ext"), str));
    }

    public static boolean a(String str) {
        try {
            if (str.equals("")) {
                return false;
            }
            int parseInt = Integer.parseInt(str);
            if (parseInt == 0 || parseInt != 1) {
                return false;
            }
            return true;
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "ordingStr2Boolean");
            return false;
        } catch (Throwable e2) {
            d.a(e2, "JSONHelper", "ordingStr2BooleanException");
            return false;
        }
    }

    public static boolean k(JSONObject jSONObject) {
        if (jSONObject == null || !jSONObject.has("biz_ext")) {
            return false;
        }
        return true;
    }

    public static void a(PoiItemDetail poiItemDetail, JSONObject jSONObject) throws JSONException {
        if (jSONObject != null) {
            if (poiItemDetail.isGroupbuyInfo()) {
                b(poiItemDetail, jSONObject);
            }
            if (poiItemDetail.isDiscountInfo()) {
                c(poiItemDetail, jSONObject);
            }
        }
    }

    public static void b(PoiItemDetail poiItemDetail, JSONObject jSONObject) throws JSONException {
        if (jSONObject != null) {
            JSONArray optJSONArray = jSONObject.optJSONArray("groupbuys");
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        Groupbuy groupbuy = new Groupbuy();
                        groupbuy.setTypeCode(b(optJSONObject, "typecode"));
                        groupbuy.setTypeDes(b(optJSONObject, "type"));
                        groupbuy.setDetail(b(optJSONObject, "detail"));
                        groupbuy.setStartTime(d.c(b(optJSONObject, "start_time")));
                        groupbuy.setEndTime(d.c(b(optJSONObject, "end_time")));
                        groupbuy.setCount(i(b(optJSONObject, "num")));
                        groupbuy.setSoldCount(i(b(optJSONObject, "sold_num")));
                        groupbuy.setOriginalPrice(j(b(optJSONObject, "original_price")));
                        groupbuy.setGroupbuyPrice(j(b(optJSONObject, "groupbuy_price")));
                        groupbuy.setDiscount(j(b(optJSONObject, "discount")));
                        groupbuy.setTicketAddress(b(optJSONObject, "ticket_address"));
                        groupbuy.setTicketTel(b(optJSONObject, "ticket_tel"));
                        groupbuy.setUrl(b(optJSONObject, "url"));
                        groupbuy.setProvider(b(optJSONObject, "provider"));
                        a(groupbuy, optJSONObject);
                        poiItemDetail.addGroupbuy(groupbuy);
                    }
                }
            }
        }
    }

    public static void a(Groupbuy groupbuy, JSONObject jSONObject) throws JSONException {
        groupbuy.initPhotos(l(jSONObject));
    }

    public static void c(PoiItemDetail poiItemDetail, JSONObject jSONObject) throws JSONException {
        JSONArray optJSONArray = jSONObject.optJSONArray("discounts");
        if (optJSONArray != null) {
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    Discount discount = new Discount();
                    discount.setTitle(b(optJSONObject, "title"));
                    discount.setDetail(b(optJSONObject, "detail"));
                    discount.setStartTime(d.c(b(optJSONObject, "start_time")));
                    discount.setEndTime(d.c(b(optJSONObject, "end_time")));
                    discount.setSoldCount(i(b(optJSONObject, "sold_num")));
                    discount.setUrl(b(optJSONObject, "url"));
                    discount.setProvider(b(optJSONObject, "provider"));
                    a(discount, optJSONObject);
                    poiItemDetail.addDiscount(discount);
                }
            }
        }
    }

    public static void a(Discount discount, JSONObject jSONObject) {
        discount.initPhotos(l(jSONObject));
    }

    public static List<Photo> l(JSONObject jSONObject) {
        List<Photo> arrayList = new ArrayList();
        if (jSONObject == null || !jSONObject.has("photos")) {
            return arrayList;
        }
        try {
            JSONArray optJSONArray = jSONObject.optJSONArray("photos");
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                Photo photo = new Photo();
                photo.setTitle(b(optJSONObject, "title"));
                photo.setUrl(b(optJSONObject, "url"));
                arrayList.add(photo);
            }
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "getPhotoList");
        }
        return arrayList;
    }

    public static DistrictItem m(JSONObject jSONObject) throws JSONException {
        DistrictItem districtItem = new DistrictItem();
        districtItem.setCitycode(b(jSONObject, "citycode"));
        districtItem.setAdcode(b(jSONObject, "adcode"));
        districtItem.setName(b(jSONObject, "name"));
        districtItem.setLevel(b(jSONObject, ParseJson.LEVEL));
        districtItem.setCenter(c(jSONObject, "center"));
        if (jSONObject.has("polyline")) {
            String string = jSONObject.getString("polyline");
            if (string != null && string.length() > 0) {
                districtItem.setDistrictBoundary(string.split("\\|"));
            }
        }
        a(jSONObject.optJSONArray("districts"), new ArrayList(), districtItem);
        return districtItem;
    }

    public static void a(JSONArray jSONArray, ArrayList<DistrictItem> arrayList, DistrictItem districtItem) throws JSONException {
        if (jSONArray != null) {
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    arrayList.add(m(optJSONObject));
                }
            }
            if (districtItem != null) {
                districtItem.setSubDistrict(arrayList);
            }
        }
    }

    public static ArrayList<GeocodeAddress> n(JSONObject jSONObject) throws JSONException {
        ArrayList<GeocodeAddress> arrayList = new ArrayList();
        if (jSONObject == null) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("geocodes");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                GeocodeAddress geocodeAddress = new GeocodeAddress();
                geocodeAddress.setFormatAddress(b(optJSONObject, "formatted_address"));
                geocodeAddress.setProvince(b(optJSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
                geocodeAddress.setCity(b(optJSONObject, DistrictSearchQuery.KEYWORDS_CITY));
                geocodeAddress.setDistrict(b(optJSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
                geocodeAddress.setTownship(b(optJSONObject, "township"));
                geocodeAddress.setNeighborhood(b(optJSONObject.optJSONObject("neighborhood"), "name"));
                geocodeAddress.setBuilding(b(optJSONObject.optJSONObject("building"), "name"));
                geocodeAddress.setAdcode(b(optJSONObject, "adcode"));
                geocodeAddress.setLatLonPoint(c(optJSONObject, "location"));
                geocodeAddress.setLevel(b(optJSONObject, ParseJson.LEVEL));
                arrayList.add(geocodeAddress);
            }
        }
        return arrayList;
    }

    public static ArrayList<Tip> o(JSONObject jSONObject) throws JSONException {
        ArrayList<Tip> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("tips");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            Tip tip = new Tip();
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                tip.setName(b(optJSONObject, "name"));
                tip.setDistrict(b(optJSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
                tip.setAdcode(b(optJSONObject, "adcode"));
                arrayList.add(tip);
            }
        }
        return arrayList;
    }

    public static void a(JSONArray jSONArray, RegeocodeAddress regeocodeAddress) throws JSONException {
        List arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            Crossroad crossroad = new Crossroad();
            JSONObject optJSONObject = jSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                crossroad.setId(b(optJSONObject, "id"));
                crossroad.setDirection(b(optJSONObject, "direction"));
                crossroad.setDistance(j(b(optJSONObject, "distance")));
                crossroad.setCenterPoint(c(optJSONObject, "location"));
                crossroad.setFirstRoadId(b(optJSONObject, "first_id"));
                crossroad.setFirstRoadName(b(optJSONObject, "first_name"));
                crossroad.setSecondRoadId(b(optJSONObject, "second_id"));
                crossroad.setSecondRoadName(b(optJSONObject, "second_name"));
                arrayList.add(crossroad);
            }
        }
        regeocodeAddress.setCrossroads(arrayList);
    }

    public static void b(JSONArray jSONArray, RegeocodeAddress regeocodeAddress) throws JSONException {
        List arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            RegeocodeRoad regeocodeRoad = new RegeocodeRoad();
            JSONObject optJSONObject = jSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                regeocodeRoad.setId(b(optJSONObject, "id"));
                regeocodeRoad.setName(b(optJSONObject, "name"));
                regeocodeRoad.setLatLngPoint(c(optJSONObject, "location"));
                regeocodeRoad.setDirection(b(optJSONObject, "direction"));
                regeocodeRoad.setDistance(j(b(optJSONObject, "distance")));
                arrayList.add(regeocodeRoad);
            }
        }
        regeocodeAddress.setRoads(arrayList);
    }

    public static void a(JSONObject jSONObject, RegeocodeAddress regeocodeAddress) throws JSONException {
        regeocodeAddress.setProvince(b(jSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
        regeocodeAddress.setCity(b(jSONObject, DistrictSearchQuery.KEYWORDS_CITY));
        regeocodeAddress.setCityCode(b(jSONObject, "citycode"));
        regeocodeAddress.setAdCode(b(jSONObject, "adcode"));
        regeocodeAddress.setDistrict(b(jSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
        regeocodeAddress.setTownship(b(jSONObject, "township"));
        regeocodeAddress.setNeighborhood(b(jSONObject.optJSONObject("neighborhood"), "name"));
        regeocodeAddress.setBuilding(b(jSONObject.optJSONObject("building"), "name"));
        StreetNumber streetNumber = new StreetNumber();
        JSONObject optJSONObject = jSONObject.optJSONObject("streetNumber");
        streetNumber.setStreet(b(optJSONObject, "street"));
        streetNumber.setNumber(b(optJSONObject, WMEditor.TYPENUM));
        streetNumber.setLatLonPoint(c(optJSONObject, "location"));
        streetNumber.setDirection(b(optJSONObject, "direction"));
        streetNumber.setDistance(j(b(optJSONObject, "distance")));
        regeocodeAddress.setStreetNumber(streetNumber);
        regeocodeAddress.setBusinessAreas(p(jSONObject));
    }

    public static List<BusinessArea> p(JSONObject jSONObject) throws JSONException {
        List<BusinessArea> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("businessAreas");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            BusinessArea businessArea = new BusinessArea();
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                businessArea.setCenterPoint(c(optJSONObject, "location"));
                businessArea.setName(b(optJSONObject, "name"));
                arrayList.add(businessArea);
            }
        }
        return arrayList;
    }

    public static BusRouteResult b(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("route")) {
                return null;
            }
            BusRouteResult busRouteResult = new BusRouteResult();
            jSONObject = jSONObject.optJSONObject("route");
            if (jSONObject == null) {
                return busRouteResult;
            }
            busRouteResult.setStartPos(c(jSONObject, "origin"));
            busRouteResult.setTargetPos(c(jSONObject, "destination"));
            busRouteResult.setTaxiCost(j(b(jSONObject, "taxi_cost")));
            if (!jSONObject.has("transits")) {
                return busRouteResult;
            }
            JSONArray optJSONArray = jSONObject.optJSONArray("transits");
            if (optJSONArray == null) {
                return busRouteResult;
            }
            busRouteResult.setPaths(a(optJSONArray));
            return busRouteResult;
        } catch (JSONException e) {
            throw new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static List<BusPath> a(JSONArray jSONArray) throws JSONException {
        List<BusPath> arrayList = new ArrayList();
        if (jSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < jSONArray.length(); i++) {
            BusPath busPath = new BusPath();
            JSONObject optJSONObject = jSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                busPath.setCost(j(b(optJSONObject, "cost")));
                busPath.setDuration(k(b(optJSONObject, "duration")));
                busPath.setNightBus(l(b(optJSONObject, "nightflag")));
                busPath.setWalkDistance(j(b(optJSONObject, "walking_distance")));
                JSONArray optJSONArray = optJSONObject.optJSONArray("segments");
                if (optJSONArray != null) {
                    List arrayList2 = new ArrayList();
                    float f = 0.0f;
                    float f2 = 0.0f;
                    for (int i2 = 0; i2 < optJSONArray.length(); i2++) {
                        JSONObject optJSONObject2 = optJSONArray.optJSONObject(i2);
                        if (optJSONObject2 != null) {
                            BusStep q = q(optJSONObject2);
                            if (q != null) {
                                arrayList2.add(q);
                                if (q.getWalk() != null) {
                                    f += q.getWalk().getDistance();
                                }
                                if (q.getBusLine() != null) {
                                    f2 += q.getBusLine().getDistance();
                                }
                            }
                        }
                    }
                    busPath.setSteps(arrayList2);
                    busPath.setBusDistance(f2);
                    busPath.setWalkDistance(f);
                    arrayList.add(busPath);
                }
            }
        }
        return arrayList;
    }

    public static BusStep q(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        BusStep busStep = new BusStep();
        JSONObject optJSONObject = jSONObject.optJSONObject("walking");
        if (optJSONObject != null) {
            busStep.setWalk(r(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("bus");
        if (optJSONObject != null) {
            busStep.setBusLines(s(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("entrance");
        if (optJSONObject != null) {
            busStep.setEntrance(t(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("exit");
        if (optJSONObject != null) {
            busStep.setExit(t(optJSONObject));
        }
        return busStep;
    }

    public static RouteBusWalkItem r(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        RouteBusWalkItem routeBusWalkItem = new RouteBusWalkItem();
        routeBusWalkItem.setOrigin(c(jSONObject, "origin"));
        routeBusWalkItem.setDestination(c(jSONObject, "destination"));
        routeBusWalkItem.setDistance(j(b(jSONObject, "distance")));
        routeBusWalkItem.setDuration(k(b(jSONObject, "duration")));
        if (!jSONObject.has("steps")) {
            return routeBusWalkItem;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("steps");
        if (optJSONArray == null) {
            return routeBusWalkItem;
        }
        List arrayList = new ArrayList();
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(u(optJSONObject));
            }
        }
        routeBusWalkItem.setSteps(arrayList);
        return routeBusWalkItem;
    }

    public static List<RouteBusLineItem> s(JSONObject jSONObject) throws JSONException {
        List<RouteBusLineItem> arrayList = new ArrayList();
        if (jSONObject == null) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("buslines");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(v(optJSONObject));
            }
        }
        return arrayList;
    }

    public static Doorway t(JSONObject jSONObject) throws JSONException {
        Doorway doorway = new Doorway();
        doorway.setName(b(jSONObject, "name"));
        doorway.setLatLonPoint(c(jSONObject, "location"));
        return doorway;
    }

    public static WalkStep u(JSONObject jSONObject) throws JSONException {
        WalkStep walkStep = new WalkStep();
        walkStep.setInstruction(b(jSONObject, "instruction"));
        walkStep.setOrientation(b(jSONObject, "orientation"));
        walkStep.setRoad(b(jSONObject, "road"));
        walkStep.setDistance(j(b(jSONObject, "distance")));
        walkStep.setDuration(j(b(jSONObject, "duration")));
        walkStep.setPolyline(d(jSONObject, "polyline"));
        walkStep.setAction(b(jSONObject, "action"));
        walkStep.setAssistantAction(b(jSONObject, "assistant_action"));
        return walkStep;
    }

    public static RouteBusLineItem v(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        RouteBusLineItem routeBusLineItem = new RouteBusLineItem();
        routeBusLineItem.setDepartureBusStation(x(jSONObject.optJSONObject("departure_stop")));
        routeBusLineItem.setArrivalBusStation(x(jSONObject.optJSONObject("arrival_stop")));
        routeBusLineItem.setBusLineName(b(jSONObject, "name"));
        routeBusLineItem.setBusLineId(b(jSONObject, "id"));
        routeBusLineItem.setBusLineType(b(jSONObject, "type"));
        routeBusLineItem.setDistance(j(b(jSONObject, "distance")));
        routeBusLineItem.setDuration(j(b(jSONObject, "duration")));
        routeBusLineItem.setPolyline(d(jSONObject, "polyline"));
        routeBusLineItem.setFirstBusTime(d.d(b(jSONObject, "start_time")));
        routeBusLineItem.setLastBusTime(d.d(b(jSONObject, "end_time")));
        routeBusLineItem.setPassStationNum(i(b(jSONObject, "via_num")));
        routeBusLineItem.setPassStations(w(jSONObject));
        return routeBusLineItem;
    }

    public static List<BusStationItem> w(JSONObject jSONObject) throws JSONException {
        List<BusStationItem> arrayList = new ArrayList();
        if (jSONObject == null) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("via_stops");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                arrayList.add(x(optJSONObject));
            }
        }
        return arrayList;
    }

    public static BusStationItem x(JSONObject jSONObject) throws JSONException {
        BusStationItem busStationItem = new BusStationItem();
        busStationItem.setBusStationName(b(jSONObject, "name"));
        busStationItem.setBusStationId(b(jSONObject, "id"));
        busStationItem.setLatLonPoint(c(jSONObject, "location"));
        return busStationItem;
    }

    public static DriveRouteResult c(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("route")) {
                return null;
            }
            DriveRouteResult driveRouteResult = new DriveRouteResult();
            jSONObject = jSONObject.optJSONObject("route");
            if (jSONObject == null) {
                return driveRouteResult;
            }
            driveRouteResult.setStartPos(c(jSONObject, "origin"));
            driveRouteResult.setTargetPos(c(jSONObject, "destination"));
            driveRouteResult.setTaxiCost(j(b(jSONObject, "taxi_cost")));
            if (!jSONObject.has("paths")) {
                return driveRouteResult;
            }
            JSONArray optJSONArray = jSONObject.optJSONArray("paths");
            if (optJSONArray == null) {
                return driveRouteResult;
            }
            List arrayList = new ArrayList();
            for (int i = 0; i < optJSONArray.length(); i++) {
                DrivePath drivePath = new DrivePath();
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    drivePath.setDistance(j(b(optJSONObject, "distance")));
                    drivePath.setDuration(k(b(optJSONObject, "duration")));
                    drivePath.setStrategy(b(optJSONObject, "strategy"));
                    drivePath.setTolls(j(b(optJSONObject, "tolls")));
                    drivePath.setTollDistance(j(b(optJSONObject, "toll_distance")));
                    JSONArray optJSONArray2 = optJSONObject.optJSONArray("steps");
                    if (optJSONArray2 != null) {
                        List arrayList2 = new ArrayList();
                        for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                            DriveStep driveStep = new DriveStep();
                            JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i2);
                            if (optJSONObject2 != null) {
                                driveStep.setInstruction(b(optJSONObject2, "instruction"));
                                driveStep.setOrientation(b(optJSONObject2, "orientation"));
                                driveStep.setRoad(b(optJSONObject2, "road"));
                                driveStep.setDistance(j(b(optJSONObject2, "distance")));
                                driveStep.setTolls(j(b(optJSONObject2, "tolls")));
                                driveStep.setTollDistance(j(b(optJSONObject2, "toll_distance")));
                                driveStep.setTollRoad(b(optJSONObject2, "toll_road"));
                                driveStep.setDuration(j(b(optJSONObject2, "duration")));
                                driveStep.setPolyline(d(optJSONObject2, "polyline"));
                                driveStep.setAction(b(optJSONObject2, "action"));
                                driveStep.setAssistantAction(b(optJSONObject2, "assistant_action"));
                                a(driveStep, optJSONObject2);
                                arrayList2.add(driveStep);
                            }
                        }
                        drivePath.setSteps(arrayList2);
                        arrayList.add(drivePath);
                    }
                }
            }
            driveRouteResult.setPaths(arrayList);
            return driveRouteResult;
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "parseDriveRoute");
            throw new AMapException("协议解析错误 - ProtocolException");
        } catch (Throwable e2) {
            d.a(e2, "JSONHelper", "parseDriveRouteThrowable");
            AMapException aMapException = new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static void a(DriveStep driveStep, JSONObject jSONObject) {
        try {
            List arrayList = new ArrayList();
            JSONArray optJSONArray = jSONObject.optJSONArray("cities");
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    RouteSearchCity routeSearchCity = new RouteSearchCity();
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        routeSearchCity.setSearchCityName(b(optJSONObject, "name"));
                        routeSearchCity.setSearchCitycode(b(optJSONObject, "citycode"));
                        routeSearchCity.setSearchCityhAdCode(b(optJSONObject, "adcode"));
                        a(routeSearchCity, optJSONObject);
                        arrayList.add(routeSearchCity);
                    }
                }
                driveStep.setRouteSearchCityList(arrayList);
            }
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "parseCrossCity");
        }
    }

    public static void a(RouteSearchCity routeSearchCity, JSONObject jSONObject) {
        if (jSONObject.has("districts")) {
            try {
                List arrayList = new ArrayList();
                JSONArray optJSONArray = jSONObject.optJSONArray("districts");
                if (optJSONArray != null) {
                    for (int i = 0; i < optJSONArray.length(); i++) {
                        District district = new District();
                        JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                        if (optJSONObject != null) {
                            district.setDistrictName(b(optJSONObject, "name"));
                            district.setDistrictAdcode(b(optJSONObject, "adcode"));
                            arrayList.add(district);
                        }
                    }
                    routeSearchCity.setDistricts(arrayList);
                    return;
                }
                routeSearchCity.setDistricts(arrayList);
            } catch (Throwable e) {
                d.a(e, "JSONHelper", "parseCrossDistricts");
            }
        }
    }

    public static WalkRouteResult d(String str) {
        Throwable e;
        WalkRouteResult walkRouteResult;
        WalkRouteResult walkRouteResult2;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("route")) {
                return null;
            }
            walkRouteResult2 = new WalkRouteResult();
            try {
                jSONObject = jSONObject.optJSONObject("route");
                walkRouteResult2.setStartPos(c(jSONObject, "origin"));
                walkRouteResult2.setTargetPos(c(jSONObject, "destination"));
                if (!jSONObject.has("paths")) {
                    return walkRouteResult2;
                }
                List arrayList = new ArrayList();
                JSONArray optJSONArray = jSONObject.optJSONArray("paths");
                if (optJSONArray != null) {
                    for (int i = 0; i < optJSONArray.length(); i++) {
                        WalkPath walkPath = new WalkPath();
                        JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                        if (optJSONObject != null) {
                            walkPath.setDistance(j(b(optJSONObject, "distance")));
                            walkPath.setDuration(k(b(optJSONObject, "duration")));
                            if (optJSONObject.has("steps")) {
                                JSONArray optJSONArray2 = optJSONObject.optJSONArray("steps");
                                List arrayList2 = new ArrayList();
                                if (optJSONArray2 != null) {
                                    for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                                        WalkStep walkStep = new WalkStep();
                                        JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i2);
                                        if (optJSONObject2 != null) {
                                            walkStep.setInstruction(b(optJSONObject2, "instruction"));
                                            walkStep.setOrientation(b(optJSONObject2, "orientation"));
                                            walkStep.setRoad(b(optJSONObject2, "road"));
                                            walkStep.setDistance(j(b(optJSONObject2, "distance")));
                                            walkStep.setDuration(j(b(optJSONObject2, "duration")));
                                            walkStep.setPolyline(d(optJSONObject2, "polyline"));
                                            walkStep.setAction(b(optJSONObject2, "action"));
                                            walkStep.setAssistantAction(b(optJSONObject2, "assistant_action"));
                                            arrayList2.add(walkStep);
                                        }
                                    }
                                    walkPath.setSteps(arrayList2);
                                } else {
                                    continue;
                                }
                            }
                            arrayList.add(walkPath);
                        }
                    }
                    walkRouteResult2.setPaths(arrayList);
                    return walkRouteResult2;
                }
                walkRouteResult2.setPaths(arrayList);
                return walkRouteResult2;
            } catch (JSONException e2) {
                e = e2;
                walkRouteResult = walkRouteResult2;
            }
        } catch (JSONException e3) {
            e = e3;
            walkRouteResult = null;
            d.a(e, "JSONHelper", "parseWalkRoute");
            walkRouteResult2 = walkRouteResult;
            return walkRouteResult2;
        }
    }

    public static String b(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject == null) {
            return "";
        }
        String str2 = "";
        if (jSONObject.has(str) && !jSONObject.getString(str).equals("[]")) {
            str2 = jSONObject.optString(str);
        }
        return str2;
    }

    public static LatLonPoint c(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject == null || !jSONObject.has(str)) {
            return null;
        }
        return f(jSONObject.optString(str));
    }

    public static ArrayList<LatLonPoint> d(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject.has(str)) {
            return e(jSONObject.getString(str));
        }
        return null;
    }

    public static ArrayList<LatLonPoint> e(String str) {
        ArrayList<LatLonPoint> arrayList = new ArrayList();
        String[] split = str.split(";");
        for (String f : split) {
            arrayList.add(f(f));
        }
        return arrayList;
    }

    public static LatLonPoint f(String str) {
        if (str == null || str.equals("") || str.equals("[]")) {
            return null;
        }
        String[] split = str.split(",");
        if (split.length != 2) {
            return null;
        }
        return new LatLonPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
    }

    public static boolean g(String str) {
        if (str == null || str.equals("") || str.equals("0")) {
            return true;
        }
        return false;
    }

    public static boolean h(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        return false;
    }

    public static int i(String str) {
        int i = 0;
        if (str == null || str.equals("") || str.equals("[]")) {
            return i;
        }
        try {
            i = Integer.parseInt(str);
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "str2int");
        }
        return i;
    }

    public static float j(String str) {
        float f = 0.0f;
        if (str == null || str.equals("") || str.equals("[]")) {
            return f;
        }
        try {
            f = Float.parseFloat(str);
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "str2float");
        }
        return f;
    }

    public static long k(String str) {
        long j = 0;
        if (str == null || str.equals("") || str.equals("[]")) {
            return j;
        }
        try {
            j = Long.parseLong(str);
        } catch (Throwable e) {
            d.a(e, "JSONHelper", "str2long");
        }
        return j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean l(String str) {
        if (str == null || str.equals("") || str.equals("[]") || str.equals("0") || !str.equals("1")) {
            return false;
        }
        return true;
    }
}
