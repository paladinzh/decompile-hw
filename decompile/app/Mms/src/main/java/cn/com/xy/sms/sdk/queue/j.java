package cn.com.xy.sms.sdk.queue;

import android.content.ContentValues;
import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.entity.A;
import cn.com.xy.sms.sdk.db.entity.C;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.L;
import cn.com.xy.sms.sdk.db.entity.a.c;
import cn.com.xy.sms.sdk.db.entity.a.l;
import cn.com.xy.sms.sdk.db.entity.d;
import cn.com.xy.sms.sdk.db.entity.u;
import cn.com.xy.sms.sdk.db.entity.v;
import cn.com.xy.sms.sdk.db.entity.z;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.service.d.a;
import cn.com.xy.sms.sdk.service.e.b;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.g;
import cn.com.xy.sms.util.ParseSmsMessage;
import java.util.Map;

/* compiled from: Unknown */
final class j extends Thread {
    j() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        XyCursor xyCursor;
        Throwable th;
        try {
            setName("xiaoyuan_taskqueue");
            Process.setThreadPriority(i.b);
            while (true) {
                k kVar = (k) i.a.take();
                if (kVar != null) {
                    String str;
                    Map map;
                    switch (kVar.a) {
                        case 1:
                            IccidLocationUtil.startQueryIccidLocation(kVar.b, true);
                            break;
                        case 2:
                            g.a();
                            break;
                        case 3:
                            u.a(kVar.b);
                            break;
                        case 4:
                            u.b(kVar.b);
                            break;
                        case 5:
                            v.a(kVar.b);
                            break;
                        case 6:
                            d.a(kVar.b);
                            break;
                        case 7:
                            e.a();
                            break;
                        case 8:
                            str = (String) kVar.b.get("titleNo");
                            A a = new A();
                            a.a = str;
                            a.c = 1;
                            try {
                                if (StringUtils.isNull(a.a)) {
                                    xyCursor = null;
                                } else {
                                    xyCursor = DBManager.query("tb_scene_config", new String[]{ParseItemManager.SCENE_ID, "isUse", "useCount"}, "scene_id = ? ", new String[]{a.a});
                                }
                                if (xyCursor != null) {
                                    try {
                                        if (xyCursor.getCount() > 0) {
                                            while (xyCursor.moveToNext()) {
                                                int i = xyCursor.getInt(xyCursor.getColumnIndex("useCount")) + 1;
                                                a.e = 1;
                                                a.c = i;
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put("useCount", Integer.valueOf(a.c));
                                                contentValues.put("isUse", Integer.valueOf(a.e));
                                                DBManager.update("tb_scene_config", contentValues, "scene_id = ? ", new String[]{new StringBuilder(String.valueOf(a.a)).toString()});
                                            }
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        break;
                                    }
                                }
                                XyCursor.closeCursor(xyCursor, true);
                            } catch (Throwable th3) {
                            }
                            z.a(str);
                            c.a();
                        case 9:
                            c.a(kVar.b);
                            b.a();
                            b.b();
                            c.c(kVar.b);
                            break;
                        case 10:
                            c.b(kVar.b);
                            b.a();
                            b.c();
                            break;
                        case 11:
                            L.a(kVar.b);
                            break;
                        case 12:
                            cn.com.xy.sms.sdk.util.u.a(Integer.parseInt((String) kVar.b.get(ParseItemManager.STATE)));
                            break;
                        case 13:
                            a.a((String) kVar.b.get("phoneNum"), (String) kVar.b.get("dbresoult"));
                            break;
                        case 14:
                            map = kVar.b;
                            C.a((String) map.get("phoneNumber"), Boolean.valueOf((String) map.get("isSuccess")).booleanValue());
                            break;
                        case 15:
                            ParseSmsMessage.queryRecognisedValueFromApi((String) kVar.b.get("msgId"), (String) kVar.b.get(IccidInfoManager.NUM), (String) kVar.b.get(IccidInfoManager.CNUM), (String) kVar.b.get("msg"), Long.valueOf((String) kVar.b.get("smsTime")).longValue(), kVar.a(), null);
                            break;
                        case 16:
                            try {
                                DBManager.delete("tb_shard_data", "msg_time<=?", new String[]{String.valueOf(System.currentTimeMillis() - DexUtil.getUpdateCycleByType(35, Constant.month))});
                            } catch (Throwable th4) {
                            }
                            map = kVar.b;
                            if (!(map == null || map.isEmpty())) {
                                try {
                                    String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) map.get(IccidInfoManager.NUM));
                                    str = (String) map.get("msg");
                                    String str2 = (String) map.get("smsTime");
                                    if (!(StringUtils.isNull(phoneNumberNo86) || StringUtils.isNull(str) || StringUtils.isNull(str2))) {
                                        str = DexUtil.multiReplace(str.trim());
                                        if (!StringUtils.isNull(str)) {
                                            String encode = StringUtils.encode(str);
                                            if (!StringUtils.isNull(encode)) {
                                                l.a(phoneNumberNo86, encode, m.a(str), str2);
                                            }
                                        }
                                    }
                                } catch (Throwable th5) {
                                }
                            }
                            b.a();
                            break;
                        default:
                            break;
                    }
                }
                continue;
            }
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        } catch (Throwable th6) {
            th6.getMessage();
        }
    }
}
