package com.amap.api.services.route;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ad;
import com.amap.api.services.core.c;
import com.amap.api.services.core.i;
import com.amap.api.services.core.k;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import java.util.ArrayList;
import java.util.List;

public class RouteSearch {
    public static final int BusComfortable = 4;
    public static final int BusDefault = 0;
    public static final int BusLeaseChange = 2;
    public static final int BusLeaseWalk = 3;
    public static final int BusNoSubway = 5;
    public static final int BusSaveMoney = 1;
    public static final int DrivingAvoidCongestion = 4;
    public static final int DrivingDefault = 0;
    public static final int DrivingMultiStrategy = 5;
    public static final int DrivingNoExpressways = 3;
    public static final int DrivingNoHighAvoidCongestionSaveMoney = 9;
    public static final int DrivingNoHighWay = 6;
    public static final int DrivingNoHighWaySaveMoney = 7;
    public static final int DrivingSaveMoney = 1;
    public static final int DrivingSaveMoneyAvoidCongestion = 8;
    public static final int DrivingShortDistance = 2;
    public static final int WalkDefault = 0;
    public static final int WalkMultipath = 1;
    private OnRouteSearchListener a;
    private Context b;
    private Handler c = t.a();

    public static class BusRouteQuery implements Parcelable, Cloneable {
        public static final Creator<BusRouteQuery> CREATOR = new m();
        private FromAndTo a;
        private int b;
        private String c;
        private int d;

        public BusRouteQuery(FromAndTo fromAndTo, int i, String str, int i2) {
            this.a = fromAndTo;
            this.b = i;
            this.c = str;
            this.d = i2;
        }

        public FromAndTo getFromAndTo() {
            return this.a;
        }

        public int getMode() {
            return this.b;
        }

        public String getCity() {
            return this.c;
        }

        public int getNightFlag() {
            return this.d;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.a, i);
            parcel.writeInt(this.b);
            parcel.writeString(this.c);
            parcel.writeInt(this.d);
        }

        public BusRouteQuery(Parcel parcel) {
            this.a = (FromAndTo) parcel.readParcelable(FromAndTo.class.getClassLoader());
            this.b = parcel.readInt();
            this.c = parcel.readString();
            this.d = parcel.readInt();
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.c != null ? this.c.hashCode() : 0) + 31) * 31;
            if (this.a != null) {
                i = this.a.hashCode();
            }
            return ((((hashCode + i) * 31) + this.b) * 31) + this.d;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            BusRouteQuery busRouteQuery = (BusRouteQuery) obj;
            if (this.c != null) {
                if (!this.c.equals(busRouteQuery.c)) {
                    return false;
                }
            } else if (busRouteQuery.c != null) {
                return false;
            }
            if (this.a != null) {
                if (!this.a.equals(busRouteQuery.a)) {
                    return false;
                }
            } else if (busRouteQuery.a != null) {
                return false;
            }
            return this.b == busRouteQuery.b && this.d == busRouteQuery.d;
        }

        public BusRouteQuery clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                i.a(e, "RouteSearch", "BusRouteQueryclone");
            }
            return new BusRouteQuery(this.a, this.b, this.c, this.d);
        }
    }

    public static class DriveRouteQuery implements Parcelable, Cloneable {
        public static final Creator<DriveRouteQuery> CREATOR = new n();
        private FromAndTo a;
        private int b;
        private List<LatLonPoint> c;
        private List<List<LatLonPoint>> d;
        private String e;

        public DriveRouteQuery(FromAndTo fromAndTo, int i, List<LatLonPoint> list, List<List<LatLonPoint>> list2, String str) {
            this.a = fromAndTo;
            this.b = i;
            this.c = list;
            this.d = list2;
            this.e = str;
        }

        public FromAndTo getFromAndTo() {
            return this.a;
        }

        public int getMode() {
            return this.b;
        }

        public List<LatLonPoint> getPassedByPoints() {
            return this.c;
        }

        public List<List<LatLonPoint>> getAvoidpolygons() {
            return this.d;
        }

        public String getAvoidRoad() {
            return this.e;
        }

        public String getPassedPointStr() {
            StringBuffer stringBuffer = new StringBuffer();
            if (this.c == null || this.c.size() == 0) {
                return null;
            }
            for (int i = 0; i < this.c.size(); i++) {
                LatLonPoint latLonPoint = (LatLonPoint) this.c.get(i);
                stringBuffer.append(latLonPoint.getLongitude());
                stringBuffer.append(",");
                stringBuffer.append(latLonPoint.getLatitude());
                if (i < this.c.size() - 1) {
                    stringBuffer.append(";");
                }
            }
            return stringBuffer.toString();
        }

        public boolean hasPassPoint() {
            if (i.a(getPassedPointStr())) {
                return false;
            }
            return true;
        }

        public String getAvoidpolygonsStr() {
            StringBuffer stringBuffer = new StringBuffer();
            if (this.d == null || this.d.size() == 0) {
                return null;
            }
            for (int i = 0; i < this.d.size(); i++) {
                List list = (List) this.d.get(i);
                for (int i2 = 0; i2 < list.size(); i2++) {
                    LatLonPoint latLonPoint = (LatLonPoint) list.get(i2);
                    stringBuffer.append(latLonPoint.getLongitude());
                    stringBuffer.append(",");
                    stringBuffer.append(latLonPoint.getLatitude());
                    if (i2 < list.size() - 1) {
                        stringBuffer.append(";");
                    }
                }
                if (i < this.d.size() - 1) {
                    stringBuffer.append("|");
                }
            }
            return stringBuffer.toString();
        }

        public boolean hasAvoidpolygons() {
            if (i.a(getAvoidpolygonsStr())) {
                return false;
            }
            return true;
        }

        public boolean hasAvoidRoad() {
            if (i.a(getAvoidRoad())) {
                return false;
            }
            return true;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.a, i);
            parcel.writeInt(this.b);
            parcel.writeTypedList(this.c);
            if (this.d != null) {
                parcel.writeInt(this.d.size());
                for (List writeTypedList : this.d) {
                    parcel.writeTypedList(writeTypedList);
                }
            } else {
                parcel.writeInt(0);
            }
            parcel.writeString(this.e);
        }

        public DriveRouteQuery(Parcel parcel) {
            this.a = (FromAndTo) parcel.readParcelable(FromAndTo.class.getClassLoader());
            this.b = parcel.readInt();
            this.c = parcel.createTypedArrayList(LatLonPoint.CREATOR);
            int readInt = parcel.readInt();
            if (readInt != 0) {
                this.d = new ArrayList();
            } else {
                this.d = null;
            }
            for (int i = 0; i < readInt; i++) {
                this.d.add(parcel.createTypedArrayList(LatLonPoint.CREATOR));
            }
            this.e = parcel.readString();
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            if (this.e != null) {
                hashCode = this.e.hashCode();
            } else {
                hashCode = 0;
            }
            int i2 = (hashCode + 31) * 31;
            if (this.d != null) {
                hashCode = this.d.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.a != null) {
                hashCode = this.a.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (((hashCode + i2) * 31) + this.b) * 31;
            if (this.c != null) {
                i = this.c.hashCode();
            }
            return hashCode + i;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DriveRouteQuery driveRouteQuery = (DriveRouteQuery) obj;
            if (this.e != null) {
                if (!this.e.equals(driveRouteQuery.e)) {
                    return false;
                }
            } else if (driveRouteQuery.e != null) {
                return false;
            }
            if (this.d != null) {
                if (!this.d.equals(driveRouteQuery.d)) {
                    return false;
                }
            } else if (driveRouteQuery.d != null) {
                return false;
            }
            if (this.a != null) {
                if (!this.a.equals(driveRouteQuery.a)) {
                    return false;
                }
            } else if (driveRouteQuery.a != null) {
                return false;
            }
            if (this.b != driveRouteQuery.b) {
                return false;
            }
            if (this.c != null) {
                return this.c.equals(driveRouteQuery.c);
            } else {
                if (driveRouteQuery.c != null) {
                    return false;
                }
            }
        }

        public DriveRouteQuery clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                i.a(e, "RouteSearch", "DriveRouteQueryclone");
            }
            return new DriveRouteQuery(this.a, this.b, this.c, this.d, this.e);
        }
    }

    public static class FromAndTo implements Parcelable, Cloneable {
        public static final Creator<FromAndTo> CREATOR = new o();
        private LatLonPoint a;
        private LatLonPoint b;
        private String c;
        private String d;

        public FromAndTo(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.a = latLonPoint;
            this.b = latLonPoint2;
        }

        public LatLonPoint getFrom() {
            return this.a;
        }

        public LatLonPoint getTo() {
            return this.b;
        }

        public String getStartPoiID() {
            return this.c;
        }

        public void setStartPoiID(String str) {
            this.c = str;
        }

        public String getDestinationPoiID() {
            return this.d;
        }

        public void setDestinationPoiID(String str) {
            this.d = str;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.a, i);
            parcel.writeParcelable(this.b, i);
            parcel.writeString(this.c);
            parcel.writeString(this.d);
        }

        public FromAndTo(Parcel parcel) {
            this.a = (LatLonPoint) parcel.readParcelable(LatLonPoint.class.getClassLoader());
            this.b = (LatLonPoint) parcel.readParcelable(LatLonPoint.class.getClassLoader());
            this.c = parcel.readString();
            this.d = parcel.readString();
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            if (this.d != null) {
                hashCode = this.d.hashCode();
            } else {
                hashCode = 0;
            }
            int i2 = (hashCode + 31) * 31;
            if (this.a != null) {
                hashCode = this.a.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.c != null) {
                hashCode = this.c.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (hashCode + i2) * 31;
            if (this.b != null) {
                i = this.b.hashCode();
            }
            return hashCode + i;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FromAndTo fromAndTo = (FromAndTo) obj;
            if (this.d != null) {
                if (!this.d.equals(fromAndTo.d)) {
                    return false;
                }
            } else if (fromAndTo.d != null) {
                return false;
            }
            if (this.a != null) {
                if (!this.a.equals(fromAndTo.a)) {
                    return false;
                }
            } else if (fromAndTo.a != null) {
                return false;
            }
            if (this.c != null) {
                if (!this.c.equals(fromAndTo.c)) {
                    return false;
                }
            } else if (fromAndTo.c != null) {
                return false;
            }
            if (this.b != null) {
                return this.b.equals(fromAndTo.b);
            } else {
                if (fromAndTo.b != null) {
                    return false;
                }
            }
        }

        public FromAndTo clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                i.a(e, "RouteSearch", "FromAndToclone");
            }
            FromAndTo fromAndTo = new FromAndTo(this.a, this.b);
            fromAndTo.setStartPoiID(this.c);
            fromAndTo.setDestinationPoiID(this.d);
            return fromAndTo;
        }
    }

    public interface OnRouteSearchListener {
        void onBusRouteSearched(BusRouteResult busRouteResult, int i);

        void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i);

        void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i);
    }

    public static class WalkRouteQuery implements Parcelable, Cloneable {
        public static final Creator<WalkRouteQuery> CREATOR = new p();
        private FromAndTo a;
        private int b;

        public WalkRouteQuery(FromAndTo fromAndTo, int i) {
            this.a = fromAndTo;
            this.b = i;
        }

        public FromAndTo getFromAndTo() {
            return this.a;
        }

        public int getMode() {
            return this.b;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(this.a, i);
            parcel.writeInt(this.b);
        }

        public WalkRouteQuery(Parcel parcel) {
            this.a = (FromAndTo) parcel.readParcelable(FromAndTo.class.getClassLoader());
            this.b = parcel.readInt();
        }

        public int hashCode() {
            int hashCode;
            if (this.a != null) {
                hashCode = this.a.hashCode();
            } else {
                hashCode = 0;
            }
            return ((hashCode + 31) * 31) + this.b;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            WalkRouteQuery walkRouteQuery = (WalkRouteQuery) obj;
            if (this.a != null) {
                if (!this.a.equals(walkRouteQuery.a)) {
                    return false;
                }
            } else if (walkRouteQuery.a != null) {
                return false;
            }
            return this.b == walkRouteQuery.b;
        }

        public WalkRouteQuery clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                i.a(e, "RouteSearch", "WalkRouteQueryclone");
            }
            return new WalkRouteQuery(this.a, this.b);
        }
    }

    public RouteSearch(Context context) {
        this.b = context.getApplicationContext();
    }

    public void setRouteSearchListener(OnRouteSearchListener onRouteSearchListener) {
        this.a = onRouteSearchListener;
    }

    public WalkRouteResult calculateWalkRoute(WalkRouteQuery walkRouteQuery) throws AMapException {
        q.a(this.b);
        WalkRouteQuery clone = walkRouteQuery.clone();
        WalkRouteResult walkRouteResult = (WalkRouteResult) new ad(this.b, clone).a();
        if (walkRouteResult != null) {
            walkRouteResult.setWalkQuery(clone);
        }
        return walkRouteResult;
    }

    public void calculateWalkRouteAsyn(final WalkRouteQuery walkRouteQuery) {
        new Thread(this) {
            final /* synthetic */ RouteSearch b;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.what = 102;
                obtainMessage.arg1 = 1;
                Bundle bundle = new Bundle();
                Parcelable parcelable = null;
                try {
                    parcelable = this.b.calculateWalkRoute(walkRouteQuery);
                    bundle.putInt("errorCode", 1000);
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable e) {
                    i.a(e, "RouteSearch", "calculateWalkRouteAsyn");
                    bundle.putInt("errorCode", e.getErrorCode());
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable th) {
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    public BusRouteResult calculateBusRoute(BusRouteQuery busRouteQuery) throws AMapException {
        q.a(this.b);
        BusRouteQuery clone = busRouteQuery.clone();
        BusRouteResult busRouteResult = (BusRouteResult) new c(this.b, clone).a();
        if (busRouteResult != null) {
            busRouteResult.setBusQuery(clone);
        }
        return busRouteResult;
    }

    public void calculateBusRouteAsyn(final BusRouteQuery busRouteQuery) {
        new Thread(this) {
            final /* synthetic */ RouteSearch b;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.what = 100;
                obtainMessage.arg1 = 1;
                Bundle bundle = new Bundle();
                Parcelable parcelable = null;
                try {
                    parcelable = this.b.calculateBusRoute(busRouteQuery);
                    bundle.putInt("errorCode", 1000);
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable e) {
                    i.a(e, "RouteSearch", "calculateBusRouteAsyn");
                    bundle.putInt("errorCode", e.getErrorCode());
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable th) {
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    public DriveRouteResult calculateDriveRoute(DriveRouteQuery driveRouteQuery) throws AMapException {
        q.a(this.b);
        DriveRouteQuery clone = driveRouteQuery.clone();
        DriveRouteResult driveRouteResult = (DriveRouteResult) new k(this.b, clone).a();
        if (driveRouteResult != null) {
            driveRouteResult.setDriveQuery(clone);
        }
        return driveRouteResult;
    }

    public void calculateDriveRouteAsyn(final DriveRouteQuery driveRouteQuery) {
        new Thread(this) {
            final /* synthetic */ RouteSearch b;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                obtainMessage.what = 101;
                obtainMessage.arg1 = 1;
                Bundle bundle = new Bundle();
                Parcelable parcelable = null;
                try {
                    parcelable = this.b.calculateDriveRoute(driveRouteQuery);
                    bundle.putInt("errorCode", 1000);
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable e) {
                    i.a(e, "RouteSearch", "calculateDriveRouteAsyn");
                    bundle.putInt("errorCode", e.getErrorCode());
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                } catch (Throwable th) {
                    obtainMessage.obj = this.b.a;
                    bundle.putParcelable("result", parcelable);
                    obtainMessage.setData(bundle);
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }.start();
    }
}
