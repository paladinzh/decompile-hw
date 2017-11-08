package com.amap.api.services.core;

import android.text.TextUtils;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.AoiItem;
import com.amap.api.services.geocoder.BusinessArea;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.help.Tip;
import com.amap.api.services.nearby.NearbyInfo;
import com.amap.api.services.poisearch.IndoorData;
import com.amap.api.services.poisearch.SubPoiItem;
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
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherLive;
import com.google.android.gms.common.Scopes;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: JSONHelper */
public class n {
    public static ArrayList<NearbyInfo> a(JSONObject jSONObject, boolean z) throws JSONException {
        JSONArray jSONArray = jSONObject.getJSONArray("datas");
        if (jSONArray == null || jSONArray.length() == 0) {
            return new ArrayList();
        }
        ArrayList<NearbyInfo> arrayList = new ArrayList();
        int length = jSONArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jSONObject2 = jSONArray.getJSONObject(i);
            String a = a(jSONObject2, "userid");
            String a2 = a(jSONObject2, "location");
            double d = 0.0d;
            double d2 = 0.0d;
            if (a2 != null) {
                String[] split = a2.split(",");
                if (split.length == 2) {
                    d = l(split[0]);
                    d2 = l(split[1]);
                }
            }
            a2 = a(jSONObject2, "distance");
            long m = m(a(jSONObject2, "updatetime"));
            int j = j(a2);
            LatLonPoint latLonPoint = new LatLonPoint(d2, d);
            NearbyInfo nearbyInfo = new NearbyInfo();
            nearbyInfo.setUserID(a);
            nearbyInfo.setTimeStamp(m);
            nearbyInfo.setPoint(latLonPoint);
            if (z) {
                nearbyInfo.setDrivingDistance(j);
            } else {
                nearbyInfo.setDistance(j);
            }
            arrayList.add(nearbyInfo);
        }
        return arrayList;
    }

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
                arrayList.add(new SuggestionCity(a(optJSONObject, "name"), a(optJSONObject, "citycode"), a(optJSONObject, "adcode"), j(a(optJSONObject, "num"))));
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

    public static PoiItem d(JSONObject jSONObject) throws JSONException {
        PoiItem poiItem = new PoiItem(a(jSONObject, "id"), b(jSONObject, "location"), a(jSONObject, "name"), a(jSONObject, "address"));
        poiItem.setAdCode(a(jSONObject, "adcode"));
        poiItem.setProvinceName(a(jSONObject, "pname"));
        poiItem.setCityName(a(jSONObject, "cityname"));
        poiItem.setAdName(a(jSONObject, "adname"));
        poiItem.setCityCode(a(jSONObject, "citycode"));
        poiItem.setProvinceCode(a(jSONObject, "pcode"));
        poiItem.setDirection(a(jSONObject, "direction"));
        if (jSONObject.has("distance")) {
            String a = a(jSONObject, "distance");
            if (!i(a)) {
                try {
                    poiItem.setDistance((int) Float.parseFloat(a));
                } catch (Throwable e) {
                    i.a(e, "JSONHelper", "parseBasePoi");
                } catch (Throwable e2) {
                    i.a(e2, "JSONHelper", "parseBasePoi");
                }
            }
        }
        poiItem.setTel(a(jSONObject, "tel"));
        poiItem.setTypeDes(a(jSONObject, "type"));
        poiItem.setEnter(b(jSONObject, "entr_location"));
        poiItem.setExit(b(jSONObject, "exit_location"));
        poiItem.setWebsite(a(jSONObject, "website"));
        poiItem.setPostcode(a(jSONObject, "postcode"));
        poiItem.setBusinessArea(a(jSONObject, "business_area"));
        poiItem.setEmail(a(jSONObject, Scopes.EMAIL));
        if (h(a(jSONObject, "indoor_map"))) {
            poiItem.setIndoorMap(false);
        } else {
            poiItem.setIndoorMap(true);
        }
        poiItem.setParkingType(a(jSONObject, "parking_type"));
        List arrayList = new ArrayList();
        if (jSONObject.has("children")) {
            JSONArray optJSONArray = jSONObject.optJSONArray("children");
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        arrayList.add(w(optJSONObject));
                    }
                }
                poiItem.setSubPois(arrayList);
            } else {
                poiItem.setSubPois(arrayList);
            }
        }
        poiItem.setIndoorDate(d(jSONObject, "indoor_data"));
        return poiItem;
    }

    private static SubPoiItem w(JSONObject jSONObject) throws JSONException {
        SubPoiItem subPoiItem = new SubPoiItem(a(jSONObject, "id"), b(jSONObject, "location"), a(jSONObject, "name"), a(jSONObject, "address"));
        subPoiItem.setSubName(a(jSONObject, "sname"));
        subPoiItem.setSubTypeDes(a(jSONObject, "subtype"));
        if (jSONObject.has("distance")) {
            String a = a(jSONObject, "distance");
            if (!i(a)) {
                try {
                    subPoiItem.setDistance((int) Float.parseFloat(a));
                } catch (Throwable e) {
                    i.a(e, "JSONHelper", "parseSubPoiItem");
                } catch (Throwable e2) {
                    i.a(e2, "JSONHelper", "parseSubPoiItem");
                }
            }
        }
        return subPoiItem;
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
        g.setAdCode(a(jSONObject, "adcode"));
        g.setCityCode(a(jSONObject, "citycode"));
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
        busStationItem.setBusStationId(a(jSONObject, "id"));
        busStationItem.setLatLonPoint(b(jSONObject, "location"));
        busStationItem.setBusStationName(a(jSONObject, "name"));
        return busStationItem;
    }

    public static BusLineItem h(JSONObject jSONObject) throws JSONException {
        BusLineItem busLineItem = new BusLineItem();
        busLineItem.setBusLineId(a(jSONObject, "id"));
        busLineItem.setBusLineType(a(jSONObject, "type"));
        busLineItem.setBusLineName(a(jSONObject, "name"));
        busLineItem.setDirectionsCoordinates(c(jSONObject, "polyline"));
        busLineItem.setCityCode(a(jSONObject, "citycode"));
        busLineItem.setOriginatingStation(a(jSONObject, "start_stop"));
        busLineItem.setTerminalStation(a(jSONObject, "end_stop"));
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
        h.setFirstBusTime(i.c(a(jSONObject, "start_time")));
        h.setLastBusTime(i.c(a(jSONObject, "end_time")));
        h.setBusCompany(a(jSONObject, "company"));
        h.setDistance(k(a(jSONObject, "distance")));
        h.setBasicPrice(k(a(jSONObject, "basic_price")));
        h.setTotalPrice(k(a(jSONObject, "total_price")));
        h.setBounds(c(jSONObject, "bounds"));
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

    public static DistrictItem k(JSONObject jSONObject) throws JSONException {
        DistrictItem districtItem = new DistrictItem();
        districtItem.setCitycode(a(jSONObject, "citycode"));
        districtItem.setAdcode(a(jSONObject, "adcode"));
        districtItem.setName(a(jSONObject, "name"));
        districtItem.setLevel(a(jSONObject, "level"));
        districtItem.setCenter(b(jSONObject, "center"));
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
                    arrayList.add(k(optJSONObject));
                }
            }
            if (districtItem != null) {
                districtItem.setSubDistrict(arrayList);
            }
        }
    }

    public static ArrayList<GeocodeAddress> l(JSONObject jSONObject) throws JSONException {
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
                geocodeAddress.setFormatAddress(a(optJSONObject, "formatted_address"));
                geocodeAddress.setProvince(a(optJSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
                geocodeAddress.setCity(a(optJSONObject, DistrictSearchQuery.KEYWORDS_CITY));
                geocodeAddress.setDistrict(a(optJSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
                geocodeAddress.setTownship(a(optJSONObject, "township"));
                geocodeAddress.setNeighborhood(a(optJSONObject.optJSONObject("neighborhood"), "name"));
                geocodeAddress.setBuilding(a(optJSONObject.optJSONObject("building"), "name"));
                geocodeAddress.setAdcode(a(optJSONObject, "adcode"));
                geocodeAddress.setLatLonPoint(b(optJSONObject, "location"));
                geocodeAddress.setLevel(a(optJSONObject, "level"));
                arrayList.add(geocodeAddress);
            }
        }
        return arrayList;
    }

    public static ArrayList<Tip> m(JSONObject jSONObject) throws JSONException {
        ArrayList<Tip> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("tips");
        if (optJSONArray == null) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            Tip tip = new Tip();
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                tip.setName(a(optJSONObject, "name"));
                tip.setDistrict(a(optJSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
                tip.setAdcode(a(optJSONObject, "adcode"));
                tip.setID(a(optJSONObject, "id"));
                Object a = a(optJSONObject, "location");
                if (!TextUtils.isEmpty(a)) {
                    String[] split = a.split(",");
                    if (split.length == 2) {
                        tip.setPostion(new LatLonPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0])));
                    }
                }
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
                crossroad.setId(a(optJSONObject, "id"));
                crossroad.setDirection(a(optJSONObject, "direction"));
                crossroad.setDistance(k(a(optJSONObject, "distance")));
                crossroad.setCenterPoint(b(optJSONObject, "location"));
                crossroad.setFirstRoadId(a(optJSONObject, "first_id"));
                crossroad.setFirstRoadName(a(optJSONObject, "first_name"));
                crossroad.setSecondRoadId(a(optJSONObject, "second_id"));
                crossroad.setSecondRoadName(a(optJSONObject, "second_name"));
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
                regeocodeRoad.setId(a(optJSONObject, "id"));
                regeocodeRoad.setName(a(optJSONObject, "name"));
                regeocodeRoad.setLatLngPoint(b(optJSONObject, "location"));
                regeocodeRoad.setDirection(a(optJSONObject, "direction"));
                regeocodeRoad.setDistance(k(a(optJSONObject, "distance")));
                arrayList.add(regeocodeRoad);
            }
        }
        regeocodeAddress.setRoads(arrayList);
    }

    public static void a(JSONObject jSONObject, RegeocodeAddress regeocodeAddress) throws JSONException {
        regeocodeAddress.setProvince(a(jSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
        regeocodeAddress.setCity(a(jSONObject, DistrictSearchQuery.KEYWORDS_CITY));
        regeocodeAddress.setCityCode(a(jSONObject, "citycode"));
        regeocodeAddress.setAdCode(a(jSONObject, "adcode"));
        regeocodeAddress.setDistrict(a(jSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT));
        regeocodeAddress.setTownship(a(jSONObject, "township"));
        regeocodeAddress.setNeighborhood(a(jSONObject.optJSONObject("neighborhood"), "name"));
        regeocodeAddress.setBuilding(a(jSONObject.optJSONObject("building"), "name"));
        StreetNumber streetNumber = new StreetNumber();
        JSONObject optJSONObject = jSONObject.optJSONObject("streetNumber");
        streetNumber.setStreet(a(optJSONObject, "street"));
        streetNumber.setNumber(a(optJSONObject, "number"));
        streetNumber.setLatLonPoint(b(optJSONObject, "location"));
        streetNumber.setDirection(a(optJSONObject, "direction"));
        streetNumber.setDistance(k(a(optJSONObject, "distance")));
        regeocodeAddress.setStreetNumber(streetNumber);
        regeocodeAddress.setBusinessAreas(n(jSONObject));
    }

    public static List<BusinessArea> n(JSONObject jSONObject) throws JSONException {
        List<BusinessArea> arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("businessAreas");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            return arrayList;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            BusinessArea businessArea = new BusinessArea();
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                businessArea.setCenterPoint(b(optJSONObject, "location"));
                businessArea.setName(a(optJSONObject, "name"));
                arrayList.add(businessArea);
            }
        }
        return arrayList;
    }

    public static BusRouteResult a(String str) throws AMapException {
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
            busRouteResult.setStartPos(b(jSONObject, "origin"));
            busRouteResult.setTargetPos(b(jSONObject, "destination"));
            busRouteResult.setTaxiCost(k(a(jSONObject, "taxi_cost")));
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
                busPath.setCost(k(a(optJSONObject, "cost")));
                busPath.setDuration(m(a(optJSONObject, "duration")));
                busPath.setNightBus(n(a(optJSONObject, "nightflag")));
                busPath.setWalkDistance(k(a(optJSONObject, "walking_distance")));
                JSONArray optJSONArray = optJSONObject.optJSONArray("segments");
                if (optJSONArray != null) {
                    List arrayList2 = new ArrayList();
                    float f = 0.0f;
                    float f2 = 0.0f;
                    for (int i2 = 0; i2 < optJSONArray.length(); i2++) {
                        JSONObject optJSONObject2 = optJSONArray.optJSONObject(i2);
                        if (optJSONObject2 != null) {
                            BusStep o = o(optJSONObject2);
                            if (o != null) {
                                arrayList2.add(o);
                                if (o.getWalk() != null) {
                                    f += o.getWalk().getDistance();
                                }
                                if (o.getBusLine() != null) {
                                    f2 += o.getBusLine().getDistance();
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

    public static BusStep o(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        BusStep busStep = new BusStep();
        JSONObject optJSONObject = jSONObject.optJSONObject("walking");
        if (optJSONObject != null) {
            busStep.setWalk(p(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("bus");
        if (optJSONObject != null) {
            busStep.setBusLines(q(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("entrance");
        if (optJSONObject != null) {
            busStep.setEntrance(r(optJSONObject));
        }
        optJSONObject = jSONObject.optJSONObject("exit");
        if (optJSONObject != null) {
            busStep.setExit(r(optJSONObject));
        }
        return busStep;
    }

    public static RouteBusWalkItem p(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        RouteBusWalkItem routeBusWalkItem = new RouteBusWalkItem();
        routeBusWalkItem.setOrigin(b(jSONObject, "origin"));
        routeBusWalkItem.setDestination(b(jSONObject, "destination"));
        routeBusWalkItem.setDistance(k(a(jSONObject, "distance")));
        routeBusWalkItem.setDuration(m(a(jSONObject, "duration")));
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
                arrayList.add(s(optJSONObject));
            }
        }
        routeBusWalkItem.setSteps(arrayList);
        return routeBusWalkItem;
    }

    public static List<RouteBusLineItem> q(JSONObject jSONObject) throws JSONException {
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
                arrayList.add(t(optJSONObject));
            }
        }
        return arrayList;
    }

    public static Doorway r(JSONObject jSONObject) throws JSONException {
        Doorway doorway = new Doorway();
        doorway.setName(a(jSONObject, "name"));
        doorway.setLatLonPoint(b(jSONObject, "location"));
        return doorway;
    }

    public static WalkStep s(JSONObject jSONObject) throws JSONException {
        WalkStep walkStep = new WalkStep();
        walkStep.setInstruction(a(jSONObject, "instruction"));
        walkStep.setOrientation(a(jSONObject, "orientation"));
        walkStep.setRoad(a(jSONObject, "road"));
        walkStep.setDistance(k(a(jSONObject, "distance")));
        walkStep.setDuration(k(a(jSONObject, "duration")));
        walkStep.setPolyline(c(jSONObject, "polyline"));
        walkStep.setAction(a(jSONObject, "action"));
        walkStep.setAssistantAction(a(jSONObject, "assistant_action"));
        return walkStep;
    }

    public static RouteBusLineItem t(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        RouteBusLineItem routeBusLineItem = new RouteBusLineItem();
        routeBusLineItem.setDepartureBusStation(v(jSONObject.optJSONObject("departure_stop")));
        routeBusLineItem.setArrivalBusStation(v(jSONObject.optJSONObject("arrival_stop")));
        routeBusLineItem.setBusLineName(a(jSONObject, "name"));
        routeBusLineItem.setBusLineId(a(jSONObject, "id"));
        routeBusLineItem.setBusLineType(a(jSONObject, "type"));
        routeBusLineItem.setDistance(k(a(jSONObject, "distance")));
        routeBusLineItem.setDuration(k(a(jSONObject, "duration")));
        routeBusLineItem.setPolyline(c(jSONObject, "polyline"));
        routeBusLineItem.setFirstBusTime(i.c(a(jSONObject, "start_time")));
        routeBusLineItem.setLastBusTime(i.c(a(jSONObject, "end_time")));
        routeBusLineItem.setPassStationNum(j(a(jSONObject, "via_num")));
        routeBusLineItem.setPassStations(u(jSONObject));
        return routeBusLineItem;
    }

    public static List<BusStationItem> u(JSONObject jSONObject) throws JSONException {
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
                arrayList.add(v(optJSONObject));
            }
        }
        return arrayList;
    }

    public static BusStationItem v(JSONObject jSONObject) throws JSONException {
        BusStationItem busStationItem = new BusStationItem();
        busStationItem.setBusStationName(a(jSONObject, "name"));
        busStationItem.setBusStationId(a(jSONObject, "id"));
        busStationItem.setLatLonPoint(b(jSONObject, "location"));
        return busStationItem;
    }

    public static DriveRouteResult b(String str) throws AMapException {
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
            driveRouteResult.setStartPos(b(jSONObject, "origin"));
            driveRouteResult.setTargetPos(b(jSONObject, "destination"));
            driveRouteResult.setTaxiCost(k(a(jSONObject, "taxi_cost")));
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
                    drivePath.setDistance(k(a(optJSONObject, "distance")));
                    drivePath.setDuration(m(a(optJSONObject, "duration")));
                    drivePath.setStrategy(a(optJSONObject, "strategy"));
                    drivePath.setTolls(k(a(optJSONObject, "tolls")));
                    drivePath.setTollDistance(k(a(optJSONObject, "toll_distance")));
                    drivePath.setTotalTrafficlights(j(a(optJSONObject, "traffic_lights")));
                    JSONArray optJSONArray2 = optJSONObject.optJSONArray("roads");
                    if (optJSONArray2 != null) {
                        List arrayList2 = new ArrayList();
                        for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                            JSONArray optJSONArray3 = optJSONArray2.optJSONObject(i2).optJSONArray("steps");
                            if (optJSONArray3 != null) {
                                for (int i3 = 0; i3 < optJSONArray3.length(); i3++) {
                                    DriveStep driveStep = new DriveStep();
                                    JSONObject optJSONObject2 = optJSONArray3.optJSONObject(i3);
                                    if (optJSONObject2 != null) {
                                        driveStep.setInstruction(a(optJSONObject2, "instruction"));
                                        driveStep.setOrientation(a(optJSONObject2, "orientation"));
                                        driveStep.setRoad(a(optJSONObject2, "road"));
                                        driveStep.setDistance(k(a(optJSONObject2, "distance")));
                                        driveStep.setTolls(k(a(optJSONObject2, "tolls")));
                                        driveStep.setTollDistance(k(a(optJSONObject2, "toll_distance")));
                                        driveStep.setTollRoad(a(optJSONObject2, "toll_road"));
                                        driveStep.setDuration(k(a(optJSONObject2, "duration")));
                                        driveStep.setPolyline(c(optJSONObject2, "polyline"));
                                        driveStep.setAction(a(optJSONObject2, "action"));
                                        driveStep.setAssistantAction(a(optJSONObject2, "assistant_action"));
                                        a(driveStep, optJSONObject2);
                                        arrayList2.add(driveStep);
                                    }
                                }
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
            i.a(e, "JSONHelper", "parseDriveRoute");
            throw new AMapException("协议解析错误 - ProtocolException");
        } catch (Throwable e2) {
            i.a(e2, "JSONHelper", "parseDriveRouteThrowable");
            AMapException aMapException = new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static void a(DriveStep driveStep, JSONObject jSONObject) throws AMapException {
        try {
            List arrayList = new ArrayList();
            JSONArray optJSONArray = jSONObject.optJSONArray("cities");
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    RouteSearchCity routeSearchCity = new RouteSearchCity();
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        routeSearchCity.setSearchCityName(a(optJSONObject, "name"));
                        routeSearchCity.setSearchCitycode(a(optJSONObject, "citycode"));
                        routeSearchCity.setSearchCityhAdCode(a(optJSONObject, "adcode"));
                        a(routeSearchCity, optJSONObject);
                        arrayList.add(routeSearchCity);
                    }
                }
                driveStep.setRouteSearchCityList(arrayList);
            }
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "parseCrossCity");
            throw new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static void a(RouteSearchCity routeSearchCity, JSONObject jSONObject) throws AMapException {
        if (jSONObject.has("districts")) {
            try {
                List arrayList = new ArrayList();
                JSONArray optJSONArray = jSONObject.optJSONArray("districts");
                if (optJSONArray != null) {
                    for (int i = 0; i < optJSONArray.length(); i++) {
                        District district = new District();
                        JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                        if (optJSONObject != null) {
                            district.setDistrictName(a(optJSONObject, "name"));
                            district.setDistrictAdcode(a(optJSONObject, "adcode"));
                            arrayList.add(district);
                        }
                    }
                    routeSearchCity.setDistricts(arrayList);
                    return;
                }
                routeSearchCity.setDistricts(arrayList);
            } catch (Throwable e) {
                i.a(e, "JSONHelper", "parseCrossDistricts");
                throw new AMapException("协议解析错误 - ProtocolException");
            }
        }
    }

    public static WalkRouteResult c(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("route")) {
                return null;
            }
            WalkRouteResult walkRouteResult = new WalkRouteResult();
            jSONObject = jSONObject.optJSONObject("route");
            walkRouteResult.setStartPos(b(jSONObject, "origin"));
            walkRouteResult.setTargetPos(b(jSONObject, "destination"));
            if (!jSONObject.has("paths")) {
                return walkRouteResult;
            }
            List arrayList = new ArrayList();
            JSONArray optJSONArray = jSONObject.optJSONArray("paths");
            if (optJSONArray != null) {
                for (int i = 0; i < optJSONArray.length(); i++) {
                    WalkPath walkPath = new WalkPath();
                    JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        walkPath.setDistance(k(a(optJSONObject, "distance")));
                        walkPath.setDuration(m(a(optJSONObject, "duration")));
                        if (optJSONObject.has("steps")) {
                            JSONArray optJSONArray2 = optJSONObject.optJSONArray("steps");
                            List arrayList2 = new ArrayList();
                            if (optJSONArray2 != null) {
                                for (int i2 = 0; i2 < optJSONArray2.length(); i2++) {
                                    WalkStep walkStep = new WalkStep();
                                    JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i2);
                                    if (optJSONObject2 != null) {
                                        walkStep.setInstruction(a(optJSONObject2, "instruction"));
                                        walkStep.setOrientation(a(optJSONObject2, "orientation"));
                                        walkStep.setRoad(a(optJSONObject2, "road"));
                                        walkStep.setDistance(k(a(optJSONObject2, "distance")));
                                        walkStep.setDuration(k(a(optJSONObject2, "duration")));
                                        walkStep.setPolyline(c(optJSONObject2, "polyline"));
                                        walkStep.setAction(a(optJSONObject2, "action"));
                                        walkStep.setAssistantAction(a(optJSONObject2, "assistant_action"));
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
                walkRouteResult.setPaths(arrayList);
                return walkRouteResult;
            }
            walkRouteResult.setPaths(arrayList);
            return walkRouteResult;
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "parseWalkRoute");
            throw new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static LocalWeatherLive d(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("lives")) {
                return null;
            }
            LocalWeatherLive localWeatherLive = new LocalWeatherLive();
            JSONArray optJSONArray = jSONObject.optJSONArray("lives");
            if (optJSONArray == null || optJSONArray.length() <= 0) {
                return localWeatherLive;
            }
            jSONObject = optJSONArray.optJSONObject(0);
            if (jSONObject == null) {
                return localWeatherLive;
            }
            localWeatherLive.setAdCode(a(jSONObject, "adcode"));
            localWeatherLive.setProvince(a(jSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
            localWeatherLive.setCity(a(jSONObject, DistrictSearchQuery.KEYWORDS_CITY));
            localWeatherLive.setWeather(a(jSONObject, "weather"));
            localWeatherLive.setTemperature(a(jSONObject, "temperature"));
            localWeatherLive.setWindDirection(a(jSONObject, "winddirection"));
            localWeatherLive.setWindPower(a(jSONObject, "windpower"));
            localWeatherLive.setHumidity(a(jSONObject, "humidity"));
            localWeatherLive.setReportTime(a(jSONObject, "reporttime"));
            return localWeatherLive;
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "WeatherForecastResult");
            throw new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static LocalWeatherForecast e(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("forecasts")) {
                return null;
            }
            LocalWeatherForecast localWeatherForecast = new LocalWeatherForecast();
            JSONArray jSONArray = jSONObject.getJSONArray("forecasts");
            if (jSONArray == null || jSONArray.length() <= 0) {
                return localWeatherForecast;
            }
            jSONObject = jSONArray.optJSONObject(0);
            if (jSONObject == null) {
                return localWeatherForecast;
            }
            localWeatherForecast.setCity(a(jSONObject, DistrictSearchQuery.KEYWORDS_CITY));
            localWeatherForecast.setAdCode(a(jSONObject, "adcode"));
            localWeatherForecast.setProvince(a(jSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE));
            localWeatherForecast.setReportTime(a(jSONObject, "reporttime"));
            if (!jSONObject.has("casts")) {
                return localWeatherForecast;
            }
            List arrayList = new ArrayList();
            jSONArray = jSONObject.optJSONArray("casts");
            if (jSONArray != null && jSONArray.length() > 0) {
                for (int i = 0; i < jSONArray.length(); i++) {
                    LocalDayWeatherForecast localDayWeatherForecast = new LocalDayWeatherForecast();
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        localDayWeatherForecast.setDate(a(optJSONObject, "date"));
                        localDayWeatherForecast.setWeek(a(optJSONObject, "week"));
                        localDayWeatherForecast.setDayWeather(a(optJSONObject, "dayweather"));
                        localDayWeatherForecast.setNightWeather(a(optJSONObject, "nightweather"));
                        localDayWeatherForecast.setDayTemp(a(optJSONObject, "daytemp"));
                        localDayWeatherForecast.setNightTemp(a(optJSONObject, "nighttemp"));
                        localDayWeatherForecast.setDayWindDirection(a(optJSONObject, "daywind"));
                        localDayWeatherForecast.setNightWindDirection(a(optJSONObject, "nightwind"));
                        localDayWeatherForecast.setDayWindPower(a(optJSONObject, "daypower"));
                        localDayWeatherForecast.setNightWindPower(a(optJSONObject, "nightpower"));
                        arrayList.add(localDayWeatherForecast);
                    }
                }
                localWeatherForecast.setWeatherForecast(arrayList);
                return localWeatherForecast;
            }
            localWeatherForecast.setWeatherForecast(arrayList);
            return localWeatherForecast;
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "WeatherForecastResult");
            throw new AMapException("协议解析错误 - ProtocolException");
        }
    }

    public static String a(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject == null) {
            return "";
        }
        String str2 = "";
        if (jSONObject.has(str) && !jSONObject.getString(str).equals("[]")) {
            str2 = jSONObject.optString(str).trim();
        }
        return str2;
    }

    public static LatLonPoint b(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject == null || !jSONObject.has(str)) {
            return null;
        }
        return g(jSONObject.optString(str));
    }

    public static ArrayList<LatLonPoint> c(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject.has(str)) {
            return f(jSONObject.getString(str));
        }
        return null;
    }

    public static ArrayList<LatLonPoint> f(String str) {
        ArrayList<LatLonPoint> arrayList = new ArrayList();
        String[] split = str.split(";");
        for (String g : split) {
            arrayList.add(g(g));
        }
        return arrayList;
    }

    public static LatLonPoint g(String str) {
        if (str == null || str.equals("") || str.equals("[]")) {
            return null;
        }
        String[] split = str.split(",");
        if (split.length != 2) {
            return null;
        }
        return new LatLonPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
    }

    public static boolean h(String str) {
        if (str == null || str.equals("") || str.equals("0")) {
            return true;
        }
        return false;
    }

    public static boolean i(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        return false;
    }

    public static int j(String str) {
        int i = 0;
        if (str == null || str.equals("") || str.equals("[]")) {
            return i;
        }
        try {
            i = Integer.parseInt(str);
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "str2int");
        }
        return i;
    }

    public static float k(String str) {
        float f = 0.0f;
        if (str == null || str.equals("") || str.equals("[]")) {
            return f;
        }
        try {
            f = Float.parseFloat(str);
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "str2float");
        }
        return f;
    }

    public static double l(String str) {
        double d = 0.0d;
        if (str == null || str.equals("") || str.equals("[]")) {
            return d;
        }
        try {
            d = Double.parseDouble(str);
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "str2float");
        }
        return d;
    }

    public static long m(String str) {
        long j = 0;
        if (str == null || str.equals("") || str.equals("[]")) {
            return j;
        }
        try {
            j = Long.parseLong(str);
        } catch (Throwable e) {
            i.a(e, "JSONHelper", "str2long");
        }
        return j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean n(String str) {
        if (str == null || str.equals("") || str.equals("[]") || str.equals("0") || !str.equals(CallInterceptDetails.BRANDED_STATE)) {
            return false;
        }
        return true;
    }

    private static IndoorData d(JSONObject jSONObject, String str) throws JSONException {
        int i = 0;
        String str2 = "";
        String str3 = "";
        if (jSONObject.has(str)) {
            JSONObject optJSONObject = jSONObject.optJSONObject(str);
            if (optJSONObject != null && optJSONObject.has("cpid") && optJSONObject.has("floor")) {
                str2 = a(optJSONObject, "cpid");
                i = j(a(optJSONObject, "floor"));
                str3 = a(optJSONObject, "truefloor");
            }
        }
        return new IndoorData(str2, i, str3);
    }

    public static void c(JSONArray jSONArray, RegeocodeAddress regeocodeAddress) throws JSONException {
        List arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            AoiItem aoiItem = new AoiItem();
            JSONObject optJSONObject = jSONArray.optJSONObject(i);
            if (optJSONObject != null) {
                aoiItem.setId(a(optJSONObject, "id"));
                aoiItem.setName(a(optJSONObject, "name"));
                aoiItem.setAdcode(a(optJSONObject, "adcode"));
                aoiItem.setLocation(b(optJSONObject, "location"));
                aoiItem.setArea(Float.valueOf(k(a(optJSONObject, "area"))));
                arrayList.add(aoiItem);
            }
        }
        regeocodeAddress.setAois(arrayList);
    }
}
