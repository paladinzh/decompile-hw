package tmsdk.fg.module.urlcheck;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.module.urlcheck.UrlCheckResultV3;
import tmsdk.common.utils.d;
import tmsdk.fg.creator.BaseManagerF;
import tmsdkobf.cg;
import tmsdkobf.ch;
import tmsdkobf.ci;
import tmsdkobf.fs;
import tmsdkobf.jq;
import tmsdkobf.lg;
import tmsdkobf.ly;
import tmsdkobf.pe;

/* compiled from: Unknown */
final class b extends BaseManagerF {
    private ConcurrentHashMap<Long, UrlCheckResultV3> OY;
    private LinkedHashMap<Long, UrlCheckResultV3> OZ;
    private long Pa;

    b() {
    }

    private void jQ() {
        if (this.OY == null) {
            this.OY = new ConcurrentHashMap();
        }
        if (this.OZ == null) {
            this.OZ = new LinkedHashMap();
        }
    }

    private void jR() {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.Pa != 0) {
            if ((currentTimeMillis - this.Pa >= 21600000 ? 1 : null) != null) {
                List<Long> arrayList = new ArrayList();
                synchronized (this.OZ) {
                    Iterator it = this.OZ.keySet().iterator();
                    while (it.hasNext()) {
                        long longValue = ((Long) it.next()).longValue();
                        this.Pa = longValue;
                        if ((currentTimeMillis - longValue < 21600000 ? 1 : null) != null) {
                            break;
                        }
                        it.remove();
                        arrayList.add(Long.valueOf(longValue));
                    }
                }
                for (Long remove : arrayList) {
                    this.OY.remove(remove);
                }
                if (this.OY.size() == 0) {
                    this.Pa = 0;
                }
            }
        }
    }

    public boolean a(final String str, int i, final ICheckUrlCallbackV3 iCheckUrlCallbackV3) {
        if (str == null || str.length() == 0 || iCheckUrlCallbackV3 == null) {
            return false;
        }
        jQ();
        jR();
        for (UrlCheckResultV3 urlCheckResultV3 : this.OY.values()) {
            if (str.equalsIgnoreCase(urlCheckResultV3.url)) {
                iCheckUrlCallbackV3.onCheckUrlCallback(urlCheckResultV3);
                return true;
            }
        }
        fs cgVar = new cg();
        cgVar.fd = 1;
        cgVar.fe = i;
        cgVar.ff = new ci();
        cgVar.ff.url = str;
        fs chVar = new ch();
        pe cu = jq.cu();
        d.d("UrlCheckManagerV2Impl", "sendShark url = " + cgVar.ff.url);
        cu.a(2002, cgVar, chVar, 0, new lg(this) {
            final /* synthetic */ b Pd;

            public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                d.d("UrlCheckManagerV2Impl", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                ch chVar = (ch) fsVar;
                if (chVar == null || chVar.fh == null) {
                    iCheckUrlCallbackV3.onCheckUrlCallback(null);
                    return;
                }
                UrlCheckResultV3 urlCheckResultV3 = new UrlCheckResultV3(str, chVar.fh);
                iCheckUrlCallbackV3.onCheckUrlCallback(urlCheckResultV3);
                long currentTimeMillis = System.currentTimeMillis();
                this.Pd.OY.put(Long.valueOf(currentTimeMillis), urlCheckResultV3);
                synchronized (this.Pd.OZ) {
                    this.Pd.OZ.put(Long.valueOf(currentTimeMillis), urlCheckResultV3);
                }
                if (this.Pd.Pa == 0) {
                    this.Pd.Pa = currentTimeMillis;
                }
            }
        });
        ly.ep();
        return true;
    }

    public int getSingletonType() {
        return 0;
    }

    public void onCreate(Context context) {
    }
}
