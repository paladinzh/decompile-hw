package com.amap.api.services.nearby;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.i;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.f;
import com.amap.api.services.core.u;
import com.amap.api.services.core.v;
import com.amap.api.services.core.w;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class NearbySearch {
    public static final int AMAP = 1;
    public static final int GPS = 0;
    private static NearbySearch a;
    private static long f = 0;
    private List<NearbyListener> b = new ArrayList();
    private String c;
    private Context d;
    private t e;
    private ExecutorService g;
    private LatLonPoint h = null;
    private String i = null;
    private boolean j = false;
    private Timer k = new Timer();
    private UploadInfoCallback l;
    private TimerTask m;

    public interface NearbyListener {
        void onNearbyInfoSearched(NearbySearchResult nearbySearchResult, int i);

        void onNearbyInfoUploaded(int i);

        void onUserInfoCleared(int i);
    }

    public static class NearbyQuery {
        private LatLonPoint a;
        private NearbySearchFunctionType b = NearbySearchFunctionType.DISTANCE_SEARCH;
        private int c = 1000;
        private int d = AMapException.CODE_AMAP_CLIENT_ERRORCODE_MISSSING;
        private int e = 1;

        public void setCenterPoint(LatLonPoint latLonPoint) {
            this.a = latLonPoint;
        }

        public LatLonPoint getCenterPoint() {
            return this.a;
        }

        public int getRadius() {
            return this.c;
        }

        public void setRadius(int i) {
            if (i > 10000) {
                i = 10000;
            }
            this.c = i;
        }

        public void setType(NearbySearchFunctionType nearbySearchFunctionType) {
            this.b = nearbySearchFunctionType;
        }

        public int getType() {
            switch (a.a[this.b.ordinal()]) {
                case 2:
                    return 1;
                default:
                    return 0;
            }
        }

        public void setCoordType(int i) {
            if (i == 0 || i == 1) {
                this.e = i;
            } else {
                this.e = 1;
            }
        }

        public int getCoordType() {
            return this.e;
        }

        public void setTimeRange(int i) {
            if (i < 5) {
                i = 5;
            } else if (i > 86400) {
                i = 86400;
            }
            this.d = i;
        }

        public int getTimeRange() {
            return this.d;
        }
    }

    private static class a extends TimerTask {
        private a() {
        }

        public void run() {
            try {
                if (!(NearbySearch.a == null || NearbySearch.a.l == null)) {
                    int b = NearbySearch.a.b(NearbySearch.a.l.OnUploadInfoCallback());
                    Message obtainMessage = NearbySearch.a.e.obtainMessage();
                    obtainMessage.arg1 = 10;
                    obtainMessage.obj = NearbySearch.a.b;
                    obtainMessage.what = b;
                    NearbySearch.a.e.sendMessage(obtainMessage);
                }
            } catch (Throwable th) {
                i.a(th, "NearbySearch", "UpdateDataTask");
            }
        }
    }

    public static synchronized NearbySearch getInstance(Context context) {
        NearbySearch nearbySearch;
        synchronized (NearbySearch.class) {
            if (a == null) {
                a = new NearbySearch(context);
            }
            nearbySearch = a;
        }
        return nearbySearch;
    }

    private NearbySearch(Context context) {
        this.d = context.getApplicationContext();
        this.e = t.a();
    }

    public synchronized void addNearbyListener(NearbyListener nearbyListener) {
        try {
            this.b.add(nearbyListener);
        } catch (Throwable th) {
            i.a(th, "NearbySearch", "addNearbyListener");
        }
    }

    public synchronized void removeNearbyListener(NearbyListener nearbyListener) {
        if (nearbyListener != null) {
            try {
                this.b.remove(nearbyListener);
            } catch (Throwable th) {
                i.a(th, "NearbySearch", "removeNearbyListener");
            }
        }
    }

    public void clearUserInfoAsyn() {
        new Thread(this) {
            final /* synthetic */ NearbySearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = this.a.e.obtainMessage();
                obtainMessage.arg1 = 8;
                obtainMessage.obj = this.a.b;
                try {
                    this.a.b();
                    obtainMessage.what = 1000;
                    if (this.a.e != null) {
                        this.a.e.sendMessage(obtainMessage);
                    }
                } catch (Throwable e) {
                    obtainMessage.what = e.getErrorCode();
                    i.a(e, "NearbySearch", "clearUserInfoAsyn");
                    if (this.a.e != null) {
                        this.a.e.sendMessage(obtainMessage);
                    }
                } catch (Throwable th) {
                    if (this.a.e != null) {
                        this.a.e.sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    private int b() throws AMapException {
        AMapException e;
        try {
            if (this.j) {
                throw new AMapException(AMapException.AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR);
            } else if (a(this.c)) {
                q.a(this.d);
                return ((Integer) new u(this.d, this.c).a()).intValue();
            } else {
                throw new AMapException(AMapException.AMAP_CLIENT_USERID_ILLEGAL);
            }
        } catch (AMapException e2) {
            throw e2;
        } catch (Throwable th) {
            e2 = new AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR);
        }
    }

    public void setUserID(String str) {
        this.c = str;
    }

    public synchronized void startUploadNearbyInfoAuto(UploadInfoCallback uploadInfoCallback, int i) {
        if (i < 7000) {
            i = 7000;
        }
        try {
            this.l = uploadInfoCallback;
            if (this.j) {
                if (this.m != null) {
                    this.m.cancel();
                }
            }
            this.j = true;
            this.m = new a();
            this.k.schedule(this.m, 0, (long) i);
        } catch (Throwable th) {
            i.a(th, "NearbySearch", "startUploadNearbyInfoAuto");
        }
    }

    public synchronized void stopUploadNearbyInfoAuto() {
        try {
            if (this.m != null) {
                this.m.cancel();
            }
        } catch (Throwable th) {
            i.a(th, "NearbySearch", "stopUploadNearbyInfoAuto");
        }
        this.j = false;
        this.m = null;
    }

    private int a(UploadInfo uploadInfo) {
        return !this.j ? b(uploadInfo) : AMapException.CODE_AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR;
    }

    private boolean a(String str) {
        return Pattern.compile("^[a-z0-9A-Z_-]{1,32}$").matcher(str).find();
    }

    private int b(UploadInfo uploadInfo) {
        Object obj = null;
        try {
            q.a(this.d);
            if (uploadInfo == null) {
                return AMapException.CODE_AMAP_CLIENT_NEARBY_NULL_RESULT;
            }
            long time = new Date().getTime();
            if (time - f >= 6500) {
                obj = 1;
            }
            if (obj == null) {
                return AMapException.CODE_AMAP_CLIENT_UPLOAD_TOO_FREQUENT;
            }
            f = time;
            String userID = uploadInfo.getUserID();
            if (TextUtils.isEmpty(userID) || !a(userID)) {
                return AMapException.CODE_AMAP_CLIENT_USERID_ILLEGAL;
            }
            if (TextUtils.isEmpty(this.i)) {
                this.i = userID;
            }
            if (!userID.equals(this.i)) {
                return AMapException.CODE_AMAP_CLIENT_USERID_ILLEGAL;
            }
            LatLonPoint point = uploadInfo.getPoint();
            if (point != null) {
                if (!point.equals(this.h)) {
                    Integer num = (Integer) new w(this.d, uploadInfo).a();
                    this.h = point;
                    return 1000;
                }
            }
            return AMapException.CODE_AMAP_CLIENT_UPLOAD_LOCATION_ERROR;
        } catch (AMapException e) {
            return e.getErrorCode();
        } catch (Throwable th) {
            return AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR;
        }
    }

    public void uploadNearbyInfoAsyn(final UploadInfo uploadInfo) {
        if (this.g == null) {
            this.g = Executors.newSingleThreadExecutor();
        }
        this.g.submit(new Runnable(this) {
            final /* synthetic */ NearbySearch b;

            public void run() {
                try {
                    Message obtainMessage = this.b.e.obtainMessage();
                    obtainMessage.arg1 = 10;
                    obtainMessage.obj = this.b.b;
                    obtainMessage.what = this.b.a(uploadInfo);
                    this.b.e.sendMessage(obtainMessage);
                } catch (Throwable th) {
                    i.a(th, "NearbySearch", "uploadNearbyInfoAsyn");
                }
            }
        });
    }

    public void searchNearbyInfoAsyn(final NearbyQuery nearbyQuery) {
        new Thread(this) {
            final /* synthetic */ NearbySearch b;

            public void run() {
                Message obtainMessage = this.b.e.obtainMessage();
                obtainMessage.arg1 = 9;
                f fVar = new f();
                fVar.a = this.b.b;
                obtainMessage.obj = fVar;
                try {
                    fVar.b = this.b.searchNearbyInfo(nearbyQuery);
                    obtainMessage.what = 1000;
                    if (this.b.e != null) {
                        this.b.e.sendMessage(obtainMessage);
                    }
                } catch (Throwable e) {
                    obtainMessage.what = e.getErrorCode();
                    i.a(e, "NearbySearch", "searchNearbyInfoAsyn");
                    if (this.b.e != null) {
                        this.b.e.sendMessage(obtainMessage);
                    }
                } catch (Throwable th) {
                    if (this.b.e != null) {
                        this.b.e.sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public NearbySearchResult searchNearbyInfo(NearbyQuery nearbyQuery) throws AMapException {
        AMapException e;
        try {
            q.a(this.d);
            return (NearbySearchResult) new v(this.d, nearbyQuery).a();
        } catch (AMapException e2) {
            throw e2;
        } catch (Throwable th) {
            i.a(th, "NearbySearch", "searchNearbyInfo");
            e2 = new AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR);
        }
    }

    public static synchronized void destroy() {
        synchronized (NearbySearch.class) {
            if (a != null) {
                try {
                    a.c();
                } catch (Throwable th) {
                    i.a(th, "NearbySearch", "destryoy");
                }
            }
            a = null;
        }
    }

    private void c() {
        this.k.cancel();
    }
}
