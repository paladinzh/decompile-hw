package tmsdkobf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdkobf.jq.a;

/* compiled from: Unknown */
public final class ni implements a {
    private static volatile ni Ce;
    private static final String[] Cf = new String[]{"phone", "phone1", "phone2", "phoneEX"};
    private List<mx> Cg = new ArrayList();
    private List Ch = new ArrayList(2);

    private ni() {
        fh();
    }

    public static synchronized ni fg() {
        ni niVar;
        synchronized (ni.class) {
            if (Ce == null) {
                Ce = new ni();
            }
            niVar = Ce;
        }
        return niVar;
    }

    private boolean fh() {
        if (this.Cg.size() == 0) {
            synchronized (this.Cg) {
                if (this.Cg.size() == 0) {
                    for (String myVar : fi()) {
                        this.Cg.add(new my(myVar));
                    }
                }
            }
        }
        return this.Cg.size() > 0;
    }

    public static final List<String> fi() {
        List list = null;
        qz qzVar = jq.uh;
        if (qzVar != null) {
            list = qzVar.ig();
        }
        if (list == null) {
            list = Arrays.asList(Cf);
        }
        List arrayList = new ArrayList();
        for (String str : r0) {
            if (nh.checkService(str) != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public boolean endCall() {
        boolean z = false;
        qz qzVar = jq.uh;
        if (!fh()) {
            return false;
        }
        boolean z2;
        if (qzVar != null && qzVar.il()) {
            z2 = false;
            for (mx mxVar : this.Cg) {
                if (mxVar.bF(0)) {
                    z2 = true;
                }
                z2 = !mxVar.bF(1) ? z2 : true;
            }
        } else {
            for (mx mxVar2 : this.Cg) {
                if (mxVar2.endCall()) {
                    z = true;
                }
            }
            z2 = z;
        }
        return z2;
    }
}
