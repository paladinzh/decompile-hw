package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.CoordinateConverter.CoordType;
import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.LBSTraceBase;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.huawei.watermark.manager.parse.WMElement;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: TraceManager */
public class ev implements LBSTraceBase {
    private Context a;
    private b b = new b(this);
    private TraceListener c;
    private CoordinateConverter d = new CoordinateConverter(this.a);
    private ExecutorService e = Executors.newFixedThreadPool((Runtime.getRuntime().availableProcessors() * 2) + 3);

    /* compiled from: TraceManager */
    class a implements Runnable {
        final /* synthetic */ ev a;
        private List<TraceLocation> b = new ArrayList();
        private int c;
        private int d;
        private List<TraceLocation> e;

        public a(ev evVar, int i, List<TraceLocation> list, int i2) {
            this.a = evVar;
            this.c = i2;
            this.d = i;
            this.e = list;
        }

        public void run() {
            int a = a();
            List arrayList = new ArrayList();
            if (this.e == null || this.e.size() == 0) {
                a("轨迹点太少或距离太近,轨迹纠偏失败");
                return;
            }
            for (TraceLocation copy : this.e) {
                TraceLocation copy2 = copy2.copy();
                if (copy2 != null && copy2.getLatitude() > 0.0d && copy2.getLongitude() > 0.0d) {
                    this.b.add(copy2);
                }
            }
            this.b = ew.a(this.b, WMElement.CAMERASIZEVALUE1B1);
            int i = 0;
            int i2 = 0;
            while (this.b.size() > 0) {
                int i3;
                Message obtainMessage = this.a.b.obtainMessage();
                List arrayList2 = new ArrayList();
                int size = this.b.size();
                if (size > 503) {
                    i3 = 500;
                } else if (size <= 503 && size > 500) {
                    i3 = 3;
                } else {
                    i3 = this.b.size();
                }
                for (int i4 = 0; i4 < i3; i4++) {
                    copy2 = (TraceLocation) this.b.remove(0);
                    if (copy2 != null) {
                        if (this.c != 1) {
                            if (this.c == 3) {
                                this.a.d.from(CoordType.BAIDU);
                            } else if (this.c == 2) {
                                this.a.d.from(CoordType.GPS);
                            }
                            this.a.d.coord(new LatLng(copy2.getLatitude(), copy2.getLongitude()));
                            LatLng convert = this.a.d.convert();
                            if (convert != null) {
                                copy2.setLatitude(convert.latitude);
                                copy2.setLongitude(convert.longitude);
                            }
                        }
                        arrayList2.add(copy2);
                    }
                }
                if (arrayList2.size() >= 2 && arrayList2.size() <= 500) {
                    eu euVar = new eu(this.a.a, arrayList2, this.c);
                    ArrayList arrayList3 = new ArrayList();
                    try {
                        List list = (List) euVar.d();
                        arrayList.addAll(list);
                        obtainMessage.obj = list;
                        obtainMessage.what = 100;
                        obtainMessage.arg1 = i2;
                        Bundle bundle = new Bundle();
                        bundle.putInt("lineID", this.d);
                        obtainMessage.setData(bundle);
                        i3 = i2 + 1;
                        size = i + 1;
                        this.a.b.sendMessage(obtainMessage);
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        i = size;
                        i2 = i3;
                    } catch (eq e2) {
                        a(e2.a());
                        return;
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }
            if (i <= 0) {
                a("轨迹点太少或距离太近,轨迹纠偏失败");
            } else {
                size = et.a(arrayList);
                Message obtainMessage2 = this.a.b.obtainMessage();
                obtainMessage2.obj = arrayList;
                obtainMessage2.what = 101;
                obtainMessage2.arg1 = size;
                obtainMessage2.arg2 = a;
                bundle = new Bundle();
                bundle.putInt("lineID", this.d);
                obtainMessage2.setData(bundle);
                this.a.b.sendMessage(obtainMessage2);
            }
        }

        private void a(String str) {
            Message obtainMessage = this.a.b.obtainMessage();
            obtainMessage.obj = str;
            obtainMessage.what = 102;
            Bundle bundle = new Bundle();
            bundle.putInt("lineID", this.d);
            obtainMessage.setData(bundle);
            this.a.b.sendMessage(obtainMessage);
        }

        private int a() {
            if (this.e == null || this.e.size() == 0) {
                return 0;
            }
            List arrayList = new ArrayList();
            int i = 0;
            for (TraceLocation traceLocation : this.e) {
                if (traceLocation != null) {
                    if (((double) traceLocation.getSpeed()) < 0.01d) {
                        arrayList.add(traceLocation);
                    } else {
                        int a = a(arrayList) + i;
                        arrayList.clear();
                        i = a;
                    }
                }
            }
            return i;
        }

        private int a(List<TraceLocation> list) {
            int size = list.size();
            if (size <= 1) {
                return 0;
            }
            TraceLocation traceLocation = (TraceLocation) list.get(0);
            TraceLocation traceLocation2 = (TraceLocation) list.get(size - 1);
            if (traceLocation == null || traceLocation2 == null) {
                return 0;
            }
            int i;
            if (traceLocation == null || traceLocation2 == null) {
                i = 0;
            } else {
                i = (int) ((traceLocation2.getTime() - traceLocation.getTime()) / 1000);
            }
            return i;
        }
    }

    /* compiled from: TraceManager */
    static class b extends Handler {
        WeakReference<ev> a;
        ev b;

        public b(ev evVar) {
            a(evVar);
        }

        private void a(ev evVar) {
            this.a = new WeakReference(evVar);
            this.b = (ev) this.a.get();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message message) {
            try {
                if (this.b != null && this.b.c != null) {
                    Bundle data = message.getData();
                    if (data != null) {
                        int i = data.getInt("lineID");
                        switch (message.what) {
                            case 100:
                                this.b.c.onTraceProcessing(i, message.arg1, (List) message.obj);
                                break;
                            case 101:
                                this.b.c.onFinished(i, (List) message.obj, message.arg1, message.arg2);
                                break;
                            case 102:
                                this.b.c.onRequestFailed(i, (String) message.obj);
                                break;
                        }
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public ev(Context context) {
        this.a = context.getApplicationContext();
    }

    public void queryProcessedTrace(int i, List<TraceLocation> list, int i2, TraceListener traceListener) {
        this.c = traceListener;
        this.e.execute(new a(this, i, list, i2));
    }
}
