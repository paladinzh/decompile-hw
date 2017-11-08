package tmsdk.bg.module.network;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.utils.ScriptHelper;
import tmsdkobf.dl;
import tmsdkobf.dm;
import tmsdkobf.nc;
import tmsdkobf.nj;

/* compiled from: Unknown */
final class f {
    private static String ym = "upload_config_des";
    private Context mContext = TMSDKContext.getApplicaionContext();
    private String xY;
    private final String yj = "MOBILE";
    private final String yk = "WIFI";
    private final String yl = "EXCLUDE";
    private final List<String> yn = new ArrayList();
    private final List<String> yo = new ArrayList();
    private final ArrayList<String> yp = new ArrayList();
    private nc yq;
    private int yr = 0;
    private String ys;

    public f(String str) {
        this.xY = str;
        this.yq = new nc("NetInterfaceManager");
    }

    private void a(dm dmVar) {
        if (dmVar != null && dmVar.iA != null) {
            Iterator it = dmVar.iA.iterator();
            while (it.hasNext()) {
                dl dlVar = (dl) it.next();
                if ("MOBILE".equalsIgnoreCase(dlVar.ix)) {
                    this.yn.clear();
                    this.yn.addAll(dlVar.iy);
                } else if ("WIFI".equalsIgnoreCase(dlVar.ix)) {
                    this.yo.clear();
                    this.yo.addAll(dlVar.iy);
                } else if ("EXCLUDE".equalsIgnoreCase(dlVar.ix)) {
                    this.yp.clear();
                    this.yp.addAll(dlVar.iy);
                }
            }
        }
    }

    private boolean a(List<String> list, String str) {
        for (String startsWith : list) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    private boolean cb(String str) {
        if (!str.startsWith("ppp")) {
            return false;
        }
        if (this.ys != null && this.ys.equals(str)) {
            return true;
        }
        this.ys = dS();
        return this.ys != null && this.ys.equals(str);
    }

    private dm dQ() {
        return (dm) nj.b(this.mContext, UpdateConfig.TRAFFIC_MONITOR_CONFIG_NAME, UpdateConfig.intToString(20001), new dm());
    }

    private String dS() {
        List<String> dT = dT();
        if (dT.size() <= 1) {
            return null;
        }
        List arrayList = new ArrayList(1);
        for (String str : dT) {
            String str2;
            if (str2.startsWith("ppp")) {
                arrayList.add(str2);
            }
        }
        if (arrayList == null || arrayList.size() <= 0) {
            return (String) dT.get(0);
        }
        str2 = (String) arrayList.get(0);
        if (arrayList.size() <= 1) {
            return str2;
        }
        m(arrayList);
        return str2;
    }

    private List<String> dT() {
        List arrayList = new ArrayList(1);
        CharSequence runScript = ScriptHelper.runScript(1000, "ip route");
        if (runScript != null) {
            Matcher matcher = Pattern.compile("dev\\s+([\\w]+)").matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(1);
                if (!arrayList.contains(group)) {
                    arrayList.add(group);
                }
            }
        }
        return arrayList;
    }

    private void m(List<String> list) {
        String replaceAll = n(list).replaceAll("\n", ",");
        StringBuilder stringBuilder = new StringBuilder("IpAddr: ");
        stringBuilder.append(replaceAll).append(";");
        if (this.yq != null) {
            this.yq.a(ym, stringBuilder.toString(), true);
        }
    }

    private String n(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        CharSequence runScript = ScriptHelper.runScript(1000, "ip addr");
        if (runScript != null) {
            StringBuilder stringBuilder2 = new StringBuilder("(");
            for (String str : list) {
                stringBuilder2.append("(?:" + str + ")|");
            }
            stringBuilder2.deleteCharAt(stringBuilder2.length() - 1);
            stringBuilder2.append(")");
            Matcher matcher = Pattern.compile("^\\d+:\\s+" + stringBuilder2.toString() + ".*$\n*" + "(^[^\\d].*$\n*)*", 8).matcher(runScript);
            while (matcher.find()) {
                String group = matcher.group(0);
                if (group != null) {
                    stringBuilder.append(group);
                }
            }
        }
        return stringBuilder.toString();
    }

    public boolean bZ(String str) {
        return !cb(str) && a(this.yn, str);
    }

    public boolean ca(String str) {
        return !cb(str) && a(this.yo, str);
    }

    public void dR() {
        a(dQ());
    }
}
