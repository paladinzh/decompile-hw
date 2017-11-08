package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class rt {
    public List<String> OH = new ArrayList();
    public List<a> OI = new ArrayList();
    public List<a> OJ = new ArrayList();
    public List<a> OK = new ArrayList();

    /* compiled from: Unknown */
    public static class a extends rm {
        public String mAdapter;
        public String[] mPlayers;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean O(Context context) {
        cx cxVar = (cx) nj.b(TMSDKContext.getApplicaionContext(), UpdateConfig.PROCESSMANAGER_WHITE_LIST_NAME, UpdateConfig.intToString(40006), new cx(), "UTF-8");
        if (cxVar == null || cxVar.gw == null) {
            return false;
        }
        Iterator it = cxVar.gw.iterator();
        while (it.hasNext()) {
            cw cwVar = (cw) it.next();
            if (cwVar.go != null) {
                try {
                    a b;
                    switch (Integer.valueOf(cwVar.go).intValue()) {
                        case 3:
                            if (cwVar.gp == null) {
                                break;
                            }
                            this.OH.add(cwVar.gp);
                            break;
                        case 4:
                            break;
                        case 5:
                            b = b(cwVar);
                            if (b != null) {
                                if (!l.dm(b.mAdapter)) {
                                    a(this.OK, b);
                                    break;
                                }
                                a(this.OJ, b);
                                break;
                            }
                            break;
                        case 6:
                            b = b(cwVar);
                            if (b == null) {
                                break;
                            }
                            a(this.OI, b);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private static void a(List<a> list, a aVar) {
        if (l.dm(aVar.pa)) {
            list.add(aVar);
            return;
        }
        int i = 0;
        while (i < list.size() && !l.dm(((a) list.get(i)).pa)) {
            i++;
        }
        list.add(i, aVar);
    }

    private static a b(cw cwVar) {
        a aVar = new a();
        aVar.pa = cwVar.gp;
        if (l.dl(cwVar.gq)) {
            String[] split = cwVar.gq.split("&");
            if (split != null) {
                for (String str : split) {
                    String str2;
                    if (str2.length() > 2) {
                        char charAt = str2.charAt(0);
                        str2 = str2.substring(2);
                        switch (charAt) {
                            case '1':
                                aVar.mFileName = str2;
                                break;
                            case '2':
                                aVar.pb = str2;
                                break;
                            case '3':
                                aVar.pc = str2;
                                break;
                            case '4':
                                aVar.pd = str2;
                                break;
                            case '5':
                                aVar.pe = str2;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        if (l.dl(cwVar.gr)) {
            aVar.mPlayers = cwVar.gr.split("&");
        }
        if (l.dl(cwVar.gs)) {
            aVar.mAdapter = cwVar.gs;
        }
        return aVar;
    }

    public boolean N(Context context) {
        return O(context);
    }
}
