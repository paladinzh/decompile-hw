package defpackage;

import android.content.Context;
import android.text.TextUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/* renamed from: ai */
public class ai {
    private static ai bb = null;
    private List aV = new LinkedList();
    private List aW = new LinkedList();
    private List aX = new LinkedList();
    private List aY = new LinkedList();
    private List aZ = new LinkedList();
    private List ba = new LinkedList();
    private Context context = null;

    private ai(Context context) {
        this.context = context;
        load();
        if (this.aV.size() == 0 && this.aW.size() == 0 && this.aX.size() == 0 && this.aY.size() == 0 && this.aZ.size() == 0 && this.ba.size() == 0) {
            aw.d("PushLog2841", "Connect Control is not set, begin to config it");
            bz();
        }
    }

    public static synchronized boolean a(Context context, int i) {
        boolean z;
        synchronized (ai.class) {
            bb = ai.q(context);
            if (bb == null) {
                aw.e("PushLog2841", "cannot get ConnectControlMgr instance, may be system err!!");
                z = false;
            } else {
                z = bb.b(i);
            }
        }
        return z;
    }

    private boolean a(bt btVar, List list, String str) {
        int i = 0;
        String str2 = "\\|";
        list.clear();
        String string = btVar.getString(str);
        if (TextUtils.isEmpty(string)) {
            aw.d("PushLog2841", str + " is not set");
        } else {
            aw.d("PushLog2841", str + "=" + string);
            String[] split = string.split(str2);
            if (split == null || split.length == 0) {
                aw.e("PushLog2841", str + " len 0, maybe system err");
                return false;
            }
            int length = split.length;
            while (i < length) {
                String str3 = split[i];
                al alVar = new al();
                if (alVar.i(str3)) {
                    list.add(alVar);
                }
                i++;
            }
        }
        return true;
    }

    private boolean a(List list) {
        if (a(list, 1)) {
            b(list, 1);
            save();
            return true;
        }
        aw.i("PushLog2841", "volumeControl not allow to pass!!");
        return false;
    }

    private boolean a(List list, long j) {
        if (list == null || list.size() == 0) {
            aw.d("PushLog2841", "there is no volome control");
            return true;
        }
        for (am amVar : list) {
            if (amVar.k(j)) {
                aw.d("PushLog2841", " pass:" + amVar);
            } else {
                aw.i("PushLog2841", " not pass:" + amVar);
                return false;
            }
        }
        return true;
    }

    private boolean a(List list, List list2) {
        if (list == null && list2 == null) {
            return true;
        }
        if (list == null || list2 == null || list.size() != list2.size()) {
            return false;
        }
        for (am amVar : list) {
            Object obj;
            for (am a : list2) {
                if (amVar.a(a)) {
                    obj = 1;
                    continue;
                    break;
                }
            }
            obj = null;
            continue;
            if (obj == null) {
                return false;
            }
        }
        return true;
    }

    private boolean b(int i) {
        return 1 == au.G(this.context) ? a(this.ba) : a(this.aX);
    }

    private boolean b(bt btVar, List list, String str) {
        String str2 = "|";
        StringBuffer stringBuffer = new StringBuffer();
        for (am bH : list) {
            stringBuffer.append(bH.bH()).append(str2);
        }
        if (btVar.f(str, stringBuffer.toString())) {
            return true;
        }
        aw.e("PushLog2841", "save " + str + " failed!!");
        return false;
    }

    private synchronized boolean b(List list, long j) {
        boolean z;
        if (list != null) {
            if (list.size() != 0) {
                for (am amVar : list) {
                    if (!amVar.l(j)) {
                        aw.i("PushLog2841", " control info:" + amVar);
                        z = false;
                        break;
                    }
                }
                z = true;
            }
        }
        z = true;
        return z;
    }

    private boolean b(List list, List list2) {
        if (0 == ag.a(this.context, "lastQueryTRSsucc_time", 0)) {
            if (a(list, 1)) {
                b(list, 1);
            } else {
                aw.e("PushLog2841", "trsFirstFlowControl not allowed to pass!!");
                return false;
            }
        } else if (a(list2, 1)) {
            b(list2, 1);
        } else {
            aw.e("PushLog2841", "trsFlowControl not allowed to pass!!");
            return false;
        }
        save();
        return true;
    }

    private boolean bA() {
        return 1 == au.G(this.context) ? b(this.aY, this.aZ) : b(this.aV, this.aW);
    }

    private boolean by() {
        List linkedList = new LinkedList();
        linkedList.add(new al(86400000, ae.l(this.context).U()));
        linkedList.add(new al(3600000, ae.l(this.context).V()));
        if (a(linkedList, this.aV)) {
            linkedList = new LinkedList();
            linkedList.add(new al(86400000, ae.l(this.context).W()));
            if (a(linkedList, this.aW)) {
                List linkedList2 = new LinkedList();
                for (Entry entry : ae.l(this.context).X().entrySet()) {
                    linkedList2.add(new al(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
                }
                if (a(linkedList2, this.aX)) {
                    linkedList = new LinkedList();
                    linkedList.add(new al(86400000, ae.l(this.context).Y()));
                    linkedList.add(new al(3600000, ae.l(this.context).Z()));
                    if (a(linkedList, this.aY)) {
                        linkedList = new LinkedList();
                        linkedList.add(new al(86400000, ae.l(this.context).aa()));
                        if (a(linkedList, this.aZ)) {
                            linkedList2 = new LinkedList();
                            for (Entry entry2 : ae.l(this.context).af().entrySet()) {
                                linkedList2.add(new al(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
                            }
                            if (a(linkedList2, this.ba)) {
                                aw.d("PushLog2841", "cur control is equal trs cfg");
                                return true;
                            }
                            aw.d("PushLog2841", "wifiVolumeControl cfg is change!!");
                            return false;
                        }
                        aw.d("PushLog2841", "wifiTrsFlowControl cfg is change!!");
                        return false;
                    }
                    aw.d("PushLog2841", "wifiTrsFirstFlowControl cfg is change!");
                    return false;
                }
                aw.d("PushLog2841", "flowcControl cfg is change!!");
                return false;
            }
            aw.d("PushLog2841", "trsFlowControl cfg is change!!");
            return false;
        }
        aw.d("PushLog2841", "trsFirstFlowControl cfg is change!");
        return false;
    }

    private boolean bz() {
        this.aV.clear();
        this.aV.add(new al(86400000, ae.l(this.context).U()));
        this.aV.add(new al(3600000, ae.l(this.context).V()));
        this.aW.clear();
        this.aW.add(new al(86400000, ae.l(this.context).W()));
        this.aX.clear();
        for (Entry entry : ae.l(this.context).X().entrySet()) {
            this.aX.add(new al(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
        }
        this.aY.clear();
        this.aY.add(new al(86400000, ae.l(this.context).Y()));
        this.aY.add(new al(3600000, ae.l(this.context).Z()));
        this.aZ.clear();
        this.aZ.add(new al(86400000, ae.l(this.context).aa()));
        this.ba.clear();
        for (Entry entry2 : ae.l(this.context).af().entrySet()) {
            this.ba.add(new al(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
        }
        save();
        return true;
    }

    private boolean load() {
        try {
            bt btVar = new bt(this.context, "PushConnectControl");
            a(btVar, this.aV, "trsFirstFlowControlData");
            a(btVar, this.aW, "trsFlowControlData");
            a(btVar, this.aX, "volumeControlData");
            a(btVar, this.aY, "wifiTrsFirstFlowControlData");
            a(btVar, this.aZ, "wifiTrsFlowControlData");
            a(btVar, this.ba, "wifiVolumeControlData");
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
            return false;
        }
    }

    public static synchronized boolean p(Context context) {
        boolean z;
        synchronized (ai.class) {
            bb = ai.q(context);
            if (bb == null) {
                aw.e("PushLog2841", "cannot get ConnectControlMgr instance, may be system err!!");
                z = false;
            } else {
                z = bb.bA();
            }
        }
        return z;
    }

    public static synchronized ai q(Context context) {
        ai aiVar;
        synchronized (ai.class) {
            if (bb == null) {
                bb = new ai(context);
            }
            aiVar = bb;
        }
        return aiVar;
    }

    public static void r(Context context) {
        bb = ai.q(context);
        if (bb != null && !bb.by()) {
            aw.d("PushLog2841", "TRS cfg change, need reload");
            bb.bz();
        }
    }

    private boolean save() {
        try {
            bt btVar = new bt(this.context, "PushConnectControl");
            return b(btVar, this.aY, "wifiTrsFirstFlowControlData") && b(btVar, this.aZ, "wifiTrsFlowControlData") && b(btVar, this.ba, "wifiVolumeControlData") && b(btVar, this.aV, "trsFirstFlowControlData") && b(btVar, this.aW, "trsFlowControlData") && b(btVar, this.aX, "volumeControlData");
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
            return false;
        }
    }

    public void bB() {
        this.aV.clear();
        this.aW.clear();
        this.aX.clear();
        this.aY.clear();
        this.aZ.clear();
        this.ba.clear();
    }
}
