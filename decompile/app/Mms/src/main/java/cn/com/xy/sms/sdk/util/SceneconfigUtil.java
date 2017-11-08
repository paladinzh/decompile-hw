package cn.com.xy.sms.sdk.util;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.entity.A;
import cn.com.xy.sms.sdk.db.entity.B;
import cn.com.xy.sms.sdk.db.entity.J;
import cn.com.xy.sms.sdk.db.entity.SceneRule;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.d;
import cn.com.xy.sms.sdk.db.entity.w;
import cn.com.xy.sms.sdk.db.entity.x;
import cn.com.xy.sms.sdk.db.entity.z;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
public class SceneconfigUtil {
    private static final Map<String, Long> a = new HashMap();

    static /* synthetic */ Set a(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Set hashSet = new HashSet();
        for (A a : list) {
            hashSet.add(a.a);
        }
        return hashSet;
    }

    private static void a(String str, String str2) {
        List<String> urls = getUrls(str2);
        if (urls != null && !urls.isEmpty()) {
            for (String str3 : urls) {
                if (!x.b(str3)) {
                    w wVar = new w();
                    wVar.e = 0;
                    wVar.b = str;
                    wVar.d = 0;
                    wVar.c = str3;
                    x.a(wVar);
                    J.a(str3);
                }
            }
            J.a(false);
        }
    }

    private static void a(List<A> list, int i, boolean z) {
        try {
            if (NetUtil.isEnhance() && NetUtil.checkAccessNetWork(2)) {
                String b = j.b((List) list);
                if (!StringUtils.isNull(b)) {
                    NetUtil.executeHttpRequest(0, i, b, new A(list, i, z), NetUtil.getPopupServiceUrl() + NetUtil.QuerySceneRequest, true);
                }
            }
        } catch (Throwable th) {
        }
    }

    private static boolean a(String str) {
        synchronized (a) {
            Long l = (Long) a.get(str);
            if (l != null) {
                if (System.currentTimeMillis() <= l.longValue() + Constant.MINUTE) {
                    return false;
                }
            }
            a.put(str, Long.valueOf(System.currentTimeMillis()));
            return true;
        }
    }

    static /* synthetic */ void b(List list) {
        if (list != null && !list.isEmpty()) {
            try {
                for (A a : list) {
                    synchronized (a) {
                        Long l = (Long) a.get(a.toString());
                        if (l != null) {
                            if ((System.currentTimeMillis() - l.longValue() <= Constant.MINUTE ? 1 : null) == null) {
                                a.remove(a.toString());
                            }
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void b(List<SceneRule> list, int i) {
        try {
            if (NetUtil.isEnhance() && NetUtil.checkAccessNetWork(2)) {
                String a = j.a((List) list);
                if (!StringUtils.isNull(a)) {
                    NetUtil.executeHttpRequest(0, i, a, new B(list, i), NetUtil.getPopupServiceUrl(), true);
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void c(List<A> list) {
        if (list != null && !list.isEmpty()) {
            try {
                for (A a : list) {
                    synchronized (a) {
                        Long l = (Long) a.get(a.toString());
                        if (l != null) {
                            if ((System.currentTimeMillis() - l.longValue() <= Constant.MINUTE ? 1 : null) == null) {
                                a.remove(a.toString());
                            }
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void c(List<SceneRule> list, int i) {
        if (list != null && !list.isEmpty()) {
            for (SceneRule sceneRule : list) {
                if (sceneRule != null) {
                    z.b(sceneRule, i);
                    String str = sceneRule.scene_id;
                    List<String> urls = getUrls(sceneRule.res_urls);
                    if (!(urls == null || urls.isEmpty())) {
                        for (String str2 : urls) {
                            if (!x.b(str2)) {
                                w wVar = new w();
                                wVar.e = 0;
                                wVar.b = str;
                                wVar.d = 0;
                                wVar.c = str2;
                                x.a(wVar);
                                J.a(str2);
                            }
                        }
                        J.a(false);
                    }
                }
            }
        }
    }

    private static void d(List<A> list) {
        int i = 0;
        if (list != null && !list.isEmpty()) {
            while (i < list.size()) {
                try {
                    int i2;
                    if (a(((A) list.get(i)).toString())) {
                        i2 = i;
                    } else {
                        list.remove(i);
                        i2 = i - 1;
                    }
                    i = i2 + 1;
                } catch (Throwable th) {
                    return;
                }
            }
        }
    }

    private static Set<String> e(List<A> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Set<String> hashSet = new HashSet();
        for (A a : list) {
            hashSet.add(a.a);
        }
        return hashSet;
    }

    public static SceneRule getSceneRule(String str, int i) {
        boolean booleanParam = SysParamEntityManager.getBooleanParam(Constant.getContext(), Constant.HAS_IMPORT_DRAWABLE_DATA);
        A a;
        if (B.a(str, i) == null && !booleanParam) {
            a = new A();
            a.b = ThemeUtil.SET_NULL_STR;
            a.a = str;
            a.d = 0;
            if (XyUtil.checkNetWork(Constant.getContext()) != 0) {
                B.a(a, i);
            } else {
                List arrayList = new ArrayList();
                arrayList.add(a);
                B.a(a, i);
                insertOrUpdateSceneConfigAndRequestScenceConfig(arrayList, i, false);
            }
            return null;
        }
        List a2 = ViewUtil.getChannelType() != 7 ? z.a(str, i, false) : z.a(str, i, true);
        if (a2 == null || a2.isEmpty()) {
            a = new A();
            a.b = ThemeUtil.SET_NULL_STR;
            a.a = str;
            arrayList = new ArrayList();
            arrayList.add(a);
            a.d = 0;
            B.a(a, i);
            insertOrUpdateSceneConfigAndRequestScenceConfig(arrayList, i, false);
            return null;
        }
        SceneRule querySceneRuleByCondition = querySceneRuleByCondition(a2);
        if (querySceneRuleByCondition == null) {
            booleanParam = LogManager.debug;
            return null;
        }
        String str2 = "";
        if (querySceneRuleByCondition != null) {
            str2 = querySceneRuleByCondition.res_urls;
        }
        if (querySceneRuleByCondition.isDownload == 1 || isResDownloaded(str2)) {
            booleanParam = LogManager.debug;
            if (!(querySceneRuleByCondition == null || querySceneRuleByCondition.isDownload != 0 || querySceneRuleByCondition == null)) {
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("isdownload", Integer.valueOf(1));
                    DBManager.update("tb_scenerule_config", contentValues, "id = ? ", new String[]{querySceneRuleByCondition.id});
                } catch (Throwable th) {
                }
            }
            return querySceneRuleByCondition;
        }
        List a3 = x.a(str2);
        if (!(a3 == null || a3.isEmpty())) {
            int size = a3.size();
            for (int i2 = 0; i2 < size; i2++) {
                J.a((String) a3.get(i2));
                booleanParam = LogManager.debug;
            }
            J.a(false);
        }
        booleanParam = LogManager.debug;
        return null;
    }

    public static List<String> getUrls(String str) {
        return StringUtils.isNull(str) ? null : Arrays.asList(str.replaceAll("；", ";").split(";"));
    }

    public static void handleSceneUrllist(List<A> list, ArrayList<String> arrayList, int i) {
        XyCursor query;
        int i2 = 1;
        int i3 = arrayList == null ? 0 : 1;
        String str;
        try {
            if (arrayList.isEmpty()) {
                i2 = 0;
            }
            if ((i2 & i3) != 0) {
                int size = arrayList.size();
                for (i3 = 0; i3 < size; i3++) {
                    str = (String) arrayList.get(i3);
                    if (!J.a(str)) {
                        String str2 = "";
                        query = DBManager.query("tb_xml_res_download", new String[]{"id", ParseItemManager.SCENE_ID, Constant.URLS, "status", "pos"}, "url = ? ", new String[]{str});
                        if (query != null) {
                            if (query.getCount() > 0) {
                                XyCursor.closeCursor(query, true);
                                F.a(str);
                            }
                        }
                        try {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ParseItemManager.SCENE_ID, str2);
                            contentValues.put(Constant.URLS, str);
                            contentValues.put("status", Integer.valueOf(0));
                            contentValues.put("pos", Integer.valueOf(0));
                            contentValues.put("sceneType", Integer.valueOf(i));
                            contentValues.put("insert_time", Long.valueOf(System.currentTimeMillis()));
                            DBManager.insert("tb_xml_res_download", contentValues);
                            XyCursor.closeCursor(query, true);
                        } catch (Throwable th) {
                            Throwable th2 = th;
                        }
                        F.a(str);
                    }
                }
                F.a(false);
                return;
            }
        } catch (Throwable th3) {
        }
        return;
        XyCursor.closeCursor(query, true);
        throw th2;
    }

    public static void handleSceneconfig(List<A> list, int i) {
        try {
            if (((!list.isEmpty() ? 1 : 0) & (list == null ? 0 : 1)) != 0) {
                int size = list.size();
                for (int i2 = 0; i2 < size; i2++) {
                    A a = (A) list.get(i2);
                    if (a != null) {
                        String str;
                        a.d = 1;
                        String str2 = a.a;
                        if (i != 1) {
                            try {
                                str = "scene_id=? and sceneType != 1";
                            } catch (Throwable th) {
                            }
                        } else {
                            str = "scene_id=? and sceneType = " + i;
                        }
                        DBManager.delete("tb_scenerule_config", str, new String[]{str2});
                        B.a(a, i);
                        c(a.f, i);
                    }
                }
            }
        } catch (Throwable th2) {
        }
    }

    public static void insertOrUpdateSceneConfigAndRequestScenceConfig(List<A> list, int i, boolean z) {
        int i2;
        if (!(list == null || list.isEmpty())) {
            int i3 = 0;
            while (i3 < list.size()) {
                try {
                    if (a(((A) list.get(i3)).toString())) {
                        i2 = i3;
                    } else {
                        list.remove(i3);
                        i2 = i3 - 1;
                    }
                    i3 = i2 + 1;
                } catch (Throwable th) {
                }
            }
        }
        if (list != null && !list.isEmpty()) {
            int i4 = 0;
            while (true) {
                i2 = list.size();
                if (i2 <= 0) {
                    break;
                }
                List arrayList = new ArrayList();
                if (i2 > 25) {
                    i2 = 25;
                }
                arrayList.addAll(list.subList(0, i2));
                try {
                    if (NetUtil.isEnhance()) {
                        if (NetUtil.checkAccessNetWork(2)) {
                            String b = j.b(arrayList);
                            if (!StringUtils.isNull(b)) {
                                NetUtil.executeHttpRequest(0, i, b, new A(arrayList, i, z), NetUtil.getPopupServiceUrl() + NetUtil.QuerySceneRequest, true);
                            }
                        }
                    }
                } catch (Throwable th2) {
                }
                list.removeAll(arrayList);
                i2 = i4 + 1;
                if (i2 > 10) {
                    break;
                }
                i4 = i2;
            }
        }
    }

    public static boolean isResDownloaded(String str) {
        if (StringUtils.isNull(str)) {
            return true;
        }
        List asList = Arrays.asList(str.replaceAll("；", ";").split(";"));
        if (asList == null || asList.isEmpty()) {
            return false;
        }
        int size = asList.size();
        int i = 0;
        while (i < size) {
            if (x.b((String) asList.get(i))) {
                i++;
            } else {
                boolean z = LogManager.debug;
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void postqueryIccidScene() {
        int i = 1;
        int i2 = 0;
        try {
            Long valueOf = Long.valueOf(SysParamEntityManager.getLongParam("PostCount", 0, Constant.getContext()));
            long longParam = SysParamEntityManager.getLongParam("LastPostIccidSceneTime", 0, Constant.getContext());
            if (longParam == 0) {
                SysParamEntityManager.setParam("LastPostIccidSceneTime", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                longParam = System.currentTimeMillis();
            }
            if (valueOf.longValue() != 0) {
                if ((System.currentTimeMillis() <= longParam + DexUtil.getUpdateCycleByType(13, Constant.postqueryIccidScene) ? 1 : 0) == 0) {
                }
                i = 0;
            }
            if (i != 0) {
                List a = d.a();
                StringBuffer stringBuffer = new StringBuffer();
                if (!a.isEmpty()) {
                    while (i2 < a.size()) {
                        stringBuffer.append(new StringBuilder(String.valueOf(((A) a.get(i2)).a)).append(",").append(((A) a.get(i2)).c).append(";").toString());
                        i2++;
                    }
                    XyCallBack c = new C();
                    if (NetUtil.isEnhance()) {
                        String a2 = j.a(StringUtils.getMD5(IccidLocationUtil.getICCID(Constant.getContext())), "1", XyUtil.getImeiAndXinghao(Constant.getContext()), stringBuffer.toString());
                        if (!StringUtils.isNull(a2)) {
                            NetUtil.executeLoginBeforeHttpRequest(a2, "990005", c, NetUtil.STATSERVICE_URL, true);
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static SceneRule querySceneRuleByCondition(List<SceneRule> list) {
        return (list == null || list.isEmpty() || list.size() <= 0) ? null : (SceneRule) list.get(0);
    }

    public static void requestQuerySceneRuleRequest(List<SceneRule> list, int i) {
        if (list != null && !list.isEmpty()) {
            int i2 = 0;
            while (true) {
                int size = list.size();
                if (size <= 0) {
                    break;
                }
                List arrayList = new ArrayList();
                if (size > 25) {
                    size = 25;
                }
                arrayList.addAll(list.subList(0, size));
                try {
                    if (NetUtil.isEnhance()) {
                        if (NetUtil.checkAccessNetWork(2)) {
                            String a = j.a(arrayList);
                            if (!StringUtils.isNull(a)) {
                                NetUtil.executeHttpRequest(0, i, a, new B(arrayList, i), NetUtil.getPopupServiceUrl(), true);
                            }
                        }
                    }
                } catch (Throwable th) {
                }
                list.removeAll(arrayList);
                size = i2 + 1;
                if (size > 10) {
                    break;
                }
                i2 = size;
            }
        }
    }

    public static void updateData() {
        if (NetUtil.isEnhance()) {
            long updateCycleByType = DexUtil.getUpdateCycleByType(11, 1209600000);
            insertOrUpdateSceneConfigAndRequestScenceConfig(B.a(0, updateCycleByType), 0, true);
            insertOrUpdateSceneConfigAndRequestScenceConfig(B.a(1, updateCycleByType), 1, true);
            updateCycleByType = DexUtil.getUpdateCycleByType(11, 1209600000);
            requestQuerySceneRuleRequest(z.a(0, updateCycleByType), 0);
            requestQuerySceneRuleRequest(z.a(1, updateCycleByType), 1);
        }
    }
}
