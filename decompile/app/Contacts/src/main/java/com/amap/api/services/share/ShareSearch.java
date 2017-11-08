package com.amap.api.services.share;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.LatLonSharePoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ac;
import com.amap.api.services.core.t;

public class ShareSearch {
    public static final int BusComfortable = 4;
    public static final int BusDefault = 0;
    public static final int BusLeaseChange = 2;
    public static final int BusLeaseWalk = 3;
    public static final int BusNoSubway = 5;
    public static final int BusSaveMoney = 1;
    public static final int DrivingAvoidCongestion = 4;
    public static final int DrivingDefault = 0;
    public static final int DrivingNoHighWay = 3;
    public static final int DrivingNoHighWayAvoidCongestion = 6;
    public static final int DrivingNoHighWaySaveMoney = 5;
    public static final int DrivingNoHighWaySaveMoneyAvoidCongestion = 8;
    public static final int DrivingSaveMoney = 1;
    public static final int DrivingSaveMoneyAvoidCongestion = 7;
    public static final int DrivingShortDistance = 2;
    public static final int NaviAvoidCongestion = 4;
    public static final int NaviDefault = 0;
    public static final int NaviNoHighWay = 3;
    public static final int NaviNoHighWayAvoidCongestion = 6;
    public static final int NaviNoHighWaySaveMoney = 5;
    public static final int NaviNoHighWaySaveMoneyAvoidCongestion = 8;
    public static final int NaviSaveMoney = 1;
    public static final int NaviSaveMoneyAvoidCongestion = 7;
    public static final int NaviShortDistance = 2;
    private static String b = "http://wb.amap.com/?r=%f,%f,%s,%f,%f,%s,%d,%d,%d,%s,%s,%s&sourceapplication=openapi/0";
    private static String c = "http://wb.amap.com/?q=%f,%f,%s&sourceapplication=openapi/0";
    private static String d = "http://wb.amap.com/?n=%f,%f,%f,%f,%d&sourceapplication=openapi/0";
    private static String e = "http://wb.amap.com/?p=%s,%f,%f,%s,%s&sourceapplication=openapi/0";
    private static final String f = String.valueOf("");
    private static final String g = String.valueOf("|");
    private Context a;
    private OnShareSearchListener h;

    public interface OnShareSearchListener {
        void onBusRouteShareUrlSearched(String str, int i);

        void onDrivingRouteShareUrlSearched(String str, int i);

        void onLocationShareUrlSearched(String str, int i);

        void onNaviShareUrlSearched(String str, int i);

        void onPoiShareUrlSearched(String str, int i);

        void onWalkRouteShareUrlSearched(String str, int i);
    }

    public static class ShareBusRouteQuery {
        private ShareFromAndTo a;
        private int b;

        public ShareBusRouteQuery(ShareFromAndTo shareFromAndTo, int i) {
            this.a = shareFromAndTo;
            this.b = i;
        }

        public int getBusMode() {
            return this.b;
        }

        public ShareFromAndTo getShareFromAndTo() {
            return this.a;
        }
    }

    public static class ShareDrivingRouteQuery {
        private ShareFromAndTo a;
        private int b;

        public ShareDrivingRouteQuery(ShareFromAndTo shareFromAndTo, int i) {
            this.a = shareFromAndTo;
            this.b = i;
        }

        public int getDrivingMode() {
            return this.b;
        }

        public ShareFromAndTo getShareFromAndTo() {
            return this.a;
        }
    }

    public static class ShareFromAndTo {
        private LatLonPoint a;
        private LatLonPoint b;
        private String c = "起点";
        private String d = "终点";

        public ShareFromAndTo(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.a = latLonPoint;
            this.b = latLonPoint2;
        }

        public void setFromName(String str) {
            this.c = str;
        }

        public void setToName(String str) {
            this.d = str;
        }

        public LatLonPoint getFrom() {
            return this.a;
        }

        public LatLonPoint getTo() {
            return this.b;
        }

        public String getFromName() {
            return this.c;
        }

        public String getToName() {
            return this.d;
        }
    }

    public static class ShareNaviQuery {
        private ShareFromAndTo a;
        private int b;

        public ShareNaviQuery(ShareFromAndTo shareFromAndTo, int i) {
            this.a = shareFromAndTo;
            this.b = i;
        }

        public ShareFromAndTo getFromAndTo() {
            return this.a;
        }

        public int getNaviMode() {
            return this.b;
        }
    }

    public static class ShareWalkRouteQuery {
        private ShareFromAndTo a;
        private int b;

        public ShareWalkRouteQuery(ShareFromAndTo shareFromAndTo, int i) {
            this.a = shareFromAndTo;
            this.b = i;
        }

        public int getWalkMode() {
            return this.b;
        }

        public ShareFromAndTo getShareFromAndTo() {
            return this.a;
        }
    }

    public ShareSearch(Context context) {
        this.a = context;
    }

    public void setOnShareSearchListener(OnShareSearchListener onShareSearchListener) {
        this.h = onShareSearchListener;
    }

    public void searchPoiShareUrlAsyn(final PoiItem poiItem) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = AMapException.CODE_AMAP_ENGINE_RESPONSE_ERROR;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchPoiShareUrl = this.b.searchPoiShareUrl(poiItem);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchPoiShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void searchBusRouteShareUrlAsyn(final ShareBusRouteQuery shareBusRouteQuery) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = AMapException.CODE_AMAP_ENGINE_RETURN_TIMEOUT;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchBusRouteShareUrl = this.b.searchBusRouteShareUrl(shareBusRouteQuery);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchBusRouteShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void searchWalkRouteShareUrlAsyn(final ShareWalkRouteQuery shareWalkRouteQuery) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = 1105;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchWalkRouteShareUrl = this.b.searchWalkRouteShareUrl(shareWalkRouteQuery);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchWalkRouteShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void searchDrivingRouteShareUrlAsyn(final ShareDrivingRouteQuery shareDrivingRouteQuery) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = 1104;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchDrivingRouteShareUrl = this.b.searchDrivingRouteShareUrl(shareDrivingRouteQuery);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchDrivingRouteShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void searchNaviShareUrlAsyn(final ShareNaviQuery shareNaviQuery) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = AMapException.CODE_AMAP_ENGINE_CONNECT_TIMEOUT;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchNaviShareUrl = this.b.searchNaviShareUrl(shareNaviQuery);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchNaviShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void searchLocationShareUrlAsyn(final LatLonSharePoint latLonSharePoint) {
        new Thread(this) {
            final /* synthetic */ ShareSearch b;

            public void run() {
                if (this.b.h != null) {
                    Message obtainMessage = t.a().obtainMessage();
                    obtainMessage.arg1 = 11;
                    obtainMessage.what = AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR;
                    obtainMessage.obj = this.b.h;
                    try {
                        String searchLocationShareUrl = this.b.searchLocationShareUrl(latLonSharePoint);
                        Bundle bundle = new Bundle();
                        bundle.putString("shareurlkey", searchLocationShareUrl);
                        obtainMessage.setData(bundle);
                        obtainMessage.arg2 = 1000;
                    } catch (AMapException e) {
                        obtainMessage.arg2 = e.getErrorCode();
                    } finally {
                        t.a().sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public String searchPoiShareUrl(PoiItem poiItem) throws AMapException {
        if (poiItem == null || poiItem.getLatLonPoint() == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        LatLonPoint latLonPoint = poiItem.getLatLonPoint();
        return (String) new ac(this.a, String.format(e, new Object[]{poiItem.getPoiId(), Double.valueOf(latLonPoint.getLatitude()), Double.valueOf(latLonPoint.getLongitude()), poiItem.getTitle(), poiItem.getSnippet()})).a();
    }

    public String searchNaviShareUrl(ShareNaviQuery shareNaviQuery) throws AMapException {
        ShareFromAndTo fromAndTo = shareNaviQuery.getFromAndTo();
        if (fromAndTo.getTo() != null) {
            String format;
            LatLonPoint from = fromAndTo.getFrom();
            LatLonPoint to = fromAndTo.getTo();
            int naviMode = shareNaviQuery.getNaviMode();
            if (fromAndTo.getFrom() != null) {
                format = String.format(d, new Object[]{Double.valueOf(from.getLatitude()), Double.valueOf(from.getLongitude()), Double.valueOf(to.getLatitude()), Double.valueOf(to.getLongitude()), Integer.valueOf(naviMode)});
            } else {
                format = String.format(d, new Object[]{null, null, Double.valueOf(to.getLatitude()), Double.valueOf(to.getLongitude()), Integer.valueOf(naviMode)});
            }
            return (String) new ac(this.a, format).a();
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    public String searchLocationShareUrl(LatLonSharePoint latLonSharePoint) throws AMapException {
        return (String) new ac(this.a, String.format(c, new Object[]{Double.valueOf(latLonSharePoint.getLatitude()), Double.valueOf(latLonSharePoint.getLongitude()), latLonSharePoint.getSharePointName()})).a();
    }

    public String searchBusRouteShareUrl(ShareBusRouteQuery shareBusRouteQuery) throws AMapException {
        int busMode = shareBusRouteQuery.getBusMode();
        ShareFromAndTo shareFromAndTo = shareBusRouteQuery.getShareFromAndTo();
        if (shareFromAndTo.getFrom() == null || shareFromAndTo.getTo() == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        LatLonPoint from = shareFromAndTo.getFrom();
        LatLonPoint to = shareFromAndTo.getTo();
        String fromName = shareFromAndTo.getFromName();
        String toName = shareFromAndTo.getToName();
        return (String) new ac(this.a, String.format(b, new Object[]{Double.valueOf(from.getLatitude()), Double.valueOf(from.getLongitude()), fromName, Double.valueOf(to.getLatitude()), Double.valueOf(to.getLongitude()), toName, Integer.valueOf(busMode), Integer.valueOf(1), Integer.valueOf(0), f, f, f})).a();
    }

    public String searchDrivingRouteShareUrl(ShareDrivingRouteQuery shareDrivingRouteQuery) throws AMapException {
        int drivingMode = shareDrivingRouteQuery.getDrivingMode();
        ShareFromAndTo shareFromAndTo = shareDrivingRouteQuery.getShareFromAndTo();
        if (shareFromAndTo.getFrom() == null || shareFromAndTo.getTo() == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        LatLonPoint from = shareFromAndTo.getFrom();
        LatLonPoint to = shareFromAndTo.getTo();
        String fromName = shareFromAndTo.getFromName();
        String toName = shareFromAndTo.getToName();
        return (String) new ac(this.a, String.format(b, new Object[]{Double.valueOf(from.getLatitude()), Double.valueOf(from.getLongitude()), fromName, Double.valueOf(to.getLatitude()), Double.valueOf(to.getLongitude()), toName, Integer.valueOf(drivingMode), Integer.valueOf(0), Integer.valueOf(0), f, f, f})).a();
    }

    public String searchWalkRouteShareUrl(ShareWalkRouteQuery shareWalkRouteQuery) throws AMapException {
        int walkMode = shareWalkRouteQuery.getWalkMode();
        ShareFromAndTo shareFromAndTo = shareWalkRouteQuery.getShareFromAndTo();
        if (shareFromAndTo.getFrom() == null || shareFromAndTo.getTo() == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        LatLonPoint from = shareFromAndTo.getFrom();
        LatLonPoint to = shareFromAndTo.getTo();
        String fromName = shareFromAndTo.getFromName();
        String toName = shareFromAndTo.getToName();
        return (String) new ac(this.a, String.format(b, new Object[]{Double.valueOf(from.getLatitude()), Double.valueOf(from.getLongitude()), fromName, Double.valueOf(to.getLatitude()), Double.valueOf(to.getLongitude()), toName, Integer.valueOf(walkMode), Integer.valueOf(2), Integer.valueOf(0), f, f, f})).a();
    }
}
