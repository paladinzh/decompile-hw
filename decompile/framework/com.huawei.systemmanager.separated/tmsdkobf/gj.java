package tmsdkobf;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.utils.d;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;

/* compiled from: Unknown */
public class gj {
    static final Object oE = new Object();
    private static gj oq;
    private pf lP = jq.cu();
    private Handler mHandler;
    private Object mLock = new Object();
    private ArrayList<String> oA;
    lg oB = new lg(this) {
        final /* synthetic */ gj oF;

        {
            this.oF = r1;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            d.d("ListNetService", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
            if (i3 != 0) {
                synchronized (gj.oE) {
                    this.oF.oA = null;
                    this.oF.oz = null;
                    this.oF.oC = false;
                }
                return;
            }
            d.d("ListNetService", "onFinish() null");
            gh ghVar = new gh();
            Message obtainMessage = this.oF.mHandler.obtainMessage();
            ghVar.H = i2;
            ghVar.dG = i;
            ghVar.ol = fsVar;
            obtainMessage.obj = ghVar;
            obtainMessage.what = 1;
            this.oF.mHandler.sendMessage(obtainMessage);
            if (this.oF.oz == null || this.oF.oz.size() == 0) {
                new ge().q(true);
                this.oF.oC = false;
                return;
            }
            this.oF.mHandler.sendEmptyMessage(2);
        }
    };
    private boolean oC = false;
    private final int oD = 50;
    private HandlerThread or = jq.ct().bF("networkSharkThread");
    private final int os = 1;
    private final int ot = 2;
    private final int ou = 3;
    private final int ov = 4;
    private ge ow = new ge();
    UpdateRubbishDataCallback ox;
    li oy = new li(this) {
        final /* synthetic */ gj oF;

        {
            this.oF = r1;
        }

        public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
            switch (i2) {
                case 13652:
                    synchronized (this.oF.mLock) {
                        if (this.oF.ox != null) {
                            this.oF.ox.updateFinished();
                            this.oF.ox = null;
                        }
                        if (fsVar != null) {
                            gh ghVar = new gh();
                            Message obtainMessage = this.oF.mHandler.obtainMessage();
                            ghVar.H = i2;
                            ghVar.dG = i;
                            ghVar.dF = j;
                            ghVar.ol = fsVar;
                            obtainMessage.obj = ghVar;
                            obtainMessage.what = 1;
                            this.oF.mHandler.sendMessage(obtainMessage);
                            break;
                        }
                        d.d("ListNetService", "push == null");
                        return null;
                    }
            }
            return null;
        }
    };
    private ArrayList<String> oz;

    private gj() {
        this.or.start();
        this.mHandler = new Handler(this, this.or.getLooper()) {
            final /* synthetic */ gj oF;

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (message.obj != null) {
                            gh ghVar = (gh) message.obj;
                            ad adVar = (ad) ghVar.ol;
                            d.c("ListNetService", "收到云端push");
                            d.c("ListNetService", "onRecvPush + info.seqNo :" + ghVar.dG + "  info.pushId :" + ghVar.dF);
                            d.c("ListNetService", "size:" + adVar.aC.size());
                            List<String> a = this.oF.b(adVar.aC);
                            if (!this.oF.ow.aG()) {
                                this.oF.ow.p(true);
                            }
                            fs abVar = new ab();
                            abVar.ay = new ArrayList();
                            for (String str : a) {
                                ac acVar = new ac();
                                acVar.fileName = str;
                                acVar.aB = 0;
                                abVar.ay.add(acVar);
                            }
                            this.oF.lP.b(ghVar.dG, ghVar.dF, ghVar.H, abVar);
                            return;
                        }
                        return;
                    case 2:
                        this.oF.aM();
                        return;
                    case 3:
                        this.oF.oz = this.oF.aO();
                        this.oF.aL();
                        return;
                    case 4:
                        kr.do().ba(kz.dx().dl());
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public static gj aJ() {
        if (oq == null) {
            oq = new gj();
        }
        return oq;
    }

    private void aL() {
        if (!this.oC) {
            this.oC = true;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void aM() {
        this.oA = null;
        if (this.oz != null) {
            int size = this.oz.size();
            d.d("ListNetService", "report sd card !!: ");
            if (size > 50) {
                this.oA = new ArrayList();
                for (size = 0; size < 50; size++) {
                    this.oA.add(this.oz.get(size));
                }
                this.oz.removeAll(this.oA);
            } else if (size <= 0) {
                this.oC = false;
                return;
            } else {
                this.oA = (ArrayList) this.oz.clone();
                this.oz.removeAll(this.oA);
            }
        }
        synchronized (oE) {
            if (this.oA == null) {
            } else if (this.oA.size() >= 1) {
                fs yVar = new y();
                yVar.ak = this.oA;
                this.lP.a(3652, yVar, new ad(), 0, this.oB);
            }
        }
    }

    private Map<String, String> aN() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("tencent", null);
        hashMap.put("baidu", null);
        hashMap.put("sina", null);
        hashMap.put("alibaba", null);
        hashMap.put("gameloft", null);
        hashMap.put("netease", null);
        hashMap.put("dcim", null);
        hashMap.put("snda", null);
        hashMap.put("hjapp", null);
        hashMap.put("ifeng", null);
        hashMap.put("pictures", null);
        hashMap.put("android", "/data");
        return hashMap;
    }

    private ArrayList<String> aO() {
        d.d("ListNetService", "report sd card !!: : ");
        if (Environment.getExternalStorageState() == "unmounted") {
            return null;
        }
        File[] listFiles = Environment.getExternalStorageDirectory().listFiles();
        ArrayList<String> arrayList = new ArrayList();
        if (listFiles == null) {
            return null;
        }
        Map aN = aN();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                String toLowerCase = file.getName().toLowerCase();
                if (aN.containsKey(toLowerCase)) {
                    String str = (String) aN.get(toLowerCase);
                    String absolutePath = file.getAbsolutePath();
                    if (str != null) {
                        absolutePath = absolutePath + str;
                    }
                    Collection b = b(absolutePath, str != null ? toLowerCase + str : toLowerCase);
                    if (b != null) {
                        arrayList.addAll(b);
                    }
                } else {
                    arrayList.add("/" + toLowerCase);
                }
            }
        }
        return arrayList;
    }

    private List<String> b(String str, String str2) {
        if (str == null) {
            return null;
        }
        File[] listFiles = new File(str).listFiles();
        if (listFiles == null) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                arrayList.add("/" + str2 + "/" + file.getName().toLowerCase());
            }
        }
        return arrayList;
    }

    private List<String> b(List<ah> list) {
        return gg.aI().a(list);
    }

    public synchronized boolean a(UpdateRubbishDataCallback updateRubbishDataCallback) {
        this.ox = updateRubbishDataCallback;
        this.mHandler.sendEmptyMessage(4);
        if (!new ge().aH()) {
            this.mHandler.sendEmptyMessage(3);
        }
        ly.ep();
        return true;
    }

    public synchronized void aK() {
        this.lP.v(13652, 2);
        this.lP.a(13652, new ad(), 2, this.oy);
    }

    public boolean isUseCloudList() {
        return this.ow.aG();
    }
}
