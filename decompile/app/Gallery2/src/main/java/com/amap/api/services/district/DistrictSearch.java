package com.amap.api.services.district;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.d;
import com.amap.api.services.core.e;
import com.amap.api.services.core.l;
import com.amap.api.services.core.p;
import java.util.HashMap;

public class DistrictSearch {
    private static HashMap<Integer, DistrictResult> f;
    private Context a;
    private DistrictSearchQuery b;
    private OnDistrictSearchListener c;
    private DistrictSearchQuery d;
    private int e;
    private Handler g = p.a();

    public interface OnDistrictSearchListener {
        void onDistrictSearched(DistrictResult districtResult);
    }

    public DistrictSearch(Context context) {
        this.a = context.getApplicationContext();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(DistrictResult districtResult) {
        f = new HashMap();
        if (this.b != null && districtResult != null && this.e > 0 && this.e > this.b.getPageNum()) {
            f.put(Integer.valueOf(this.b.getPageNum()), districtResult);
        }
    }

    public DistrictSearchQuery getQuery() {
        return this.b;
    }

    public void setQuery(DistrictSearchQuery districtSearchQuery) {
        this.b = districtSearchQuery;
    }

    private boolean a() {
        if (this.b != null) {
            return true;
        }
        return false;
    }

    protected DistrictResult getPageLocal(int i) throws AMapException {
        if (a(i)) {
            return (DistrictResult) f.get(Integer.valueOf(i));
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    private boolean a(int i) {
        return i < this.e && i >= 0;
    }

    private DistrictResult b() throws AMapException {
        DistrictResult districtResult = new DistrictResult();
        l.a(this.a);
        if (!a()) {
            this.b = new DistrictSearchQuery();
        }
        districtResult.setQuery(this.b.clone());
        if (!this.b.weakEquals(this.d)) {
            this.e = 0;
            this.d = this.b.clone();
            if (f != null) {
                f.clear();
            }
        }
        if (this.e != 0) {
            districtResult = getPageLocal(this.b.getPageNum());
            if (districtResult == null) {
                districtResult = (DistrictResult) new e(this.a, this.b.clone()).g();
                if (this.b == null || districtResult == null) {
                    return districtResult;
                }
                if (this.e > 0 && this.e > this.b.getPageNum()) {
                    f.put(Integer.valueOf(this.b.getPageNum()), districtResult);
                }
            }
        } else {
            districtResult = (DistrictResult) new e(this.a, this.b.clone()).g();
            if (districtResult == null) {
                return districtResult;
            }
            this.e = districtResult.getPageCount();
            a(districtResult);
        }
        return districtResult;
    }

    public void searchDistrictAnsy() {
        new Thread(this) {
            final /* synthetic */ DistrictSearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = p.a().obtainMessage();
                Parcelable districtResult = new DistrictResult();
                districtResult.setQuery(this.a.b);
                Bundle bundle;
                try {
                    Object b = this.a.b();
                    if (b != null) {
                        b.setAMapException(new AMapException());
                    }
                    obtainMessage.arg1 = 4;
                    obtainMessage.obj = this.a.c;
                    bundle = new Bundle();
                    bundle.putParcelable("result", b);
                    obtainMessage.setData(bundle);
                    if (this.a.g != null) {
                        this.a.g.sendMessage(obtainMessage);
                    }
                } catch (Throwable e) {
                    d.a(e, "DistrictSearch", "searchDistrictAnsy");
                    districtResult.setAMapException(e);
                    obtainMessage.arg1 = 4;
                    obtainMessage.obj = this.a.c;
                    bundle = new Bundle();
                    bundle.putParcelable("result", districtResult);
                    obtainMessage.setData(bundle);
                    if (this.a.g != null) {
                        this.a.g.sendMessage(obtainMessage);
                    }
                } catch (Throwable th) {
                    obtainMessage.arg1 = 4;
                    obtainMessage.obj = this.a.c;
                    Bundle bundle2 = new Bundle();
                    bundle2.putParcelable("result", districtResult);
                    obtainMessage.setData(bundle2);
                    if (this.a.g != null) {
                        this.a.g.sendMessage(obtainMessage);
                    }
                }
            }
        }.start();
    }

    public void setOnDistrictSearchListener(OnDistrictSearchListener onDistrictSearchListener) {
        this.c = onDistrictSearchListener;
    }
}
