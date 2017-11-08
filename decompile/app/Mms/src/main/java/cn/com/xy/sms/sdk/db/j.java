package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.k;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;

/* compiled from: Unknown */
final class j implements XyCallBack {
    private final /* synthetic */ boolean a;
    private final /* synthetic */ Map b;
    private final /* synthetic */ k c;
    private final /* synthetic */ XyCallBack d;

    j(boolean z, Map map, k kVar, XyCallBack xyCallBack) {
        this.a = z;
        this.b = map;
        this.c = kVar;
        this.d = xyCallBack;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr[0].toString().equals("0")) {
                    if (objArr.length == 2) {
                        k b = i.b(objArr[1].toString());
                        String str;
                        ContentValues contentValues;
                        if (b == null) {
                            str = this.c.b;
                            contentValues = new ContentValues();
                            contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
                            str = this.c.b;
                            XyUtil.doXycallBack(this.d, "0");
                        } else {
                            long currentTimeMillis = System.currentTimeMillis();
                            long j;
                            if (StringUtils.isNull(b.c) || StringUtils.isNull(b.d) || b.c.equals(this.c.c)) {
                                this.c.h = b.h + currentTimeMillis;
                                this.c.i = currentTimeMillis + b.i;
                                str = this.c.b;
                                currentTimeMillis = this.c.h;
                                j = this.c.i;
                                contentValues = new ContentValues();
                                contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                                contentValues.put("delaystart", new StringBuilder(String.valueOf(currentTimeMillis)).toString());
                                contentValues.put("delayend", new StringBuilder(String.valueOf(j)).toString());
                                DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
                                str = this.c.b;
                                XyUtil.doXycallBack(this.d, "0");
                            } else {
                                this.c.d = b.d;
                                this.c.e = System.currentTimeMillis();
                                this.c.c = b.c;
                                this.c.f = 0;
                                this.c.h = b.h + currentTimeMillis;
                                this.c.i = currentTimeMillis + b.i;
                                str = this.c.b;
                                String str2 = this.c.c;
                                String str3 = this.c.d;
                                j = this.c.e;
                                int i = this.c.f;
                                long j2 = this.c.h;
                                long j3 = this.c.i;
                                ContentValues contentValues2 = new ContentValues();
                                contentValues2.put(NumberInfo.VERSION_KEY, str2);
                                contentValues2.put(Constant.URLS, str3);
                                contentValues2.put("status", Integer.valueOf(i));
                                contentValues2.put("update_time", new StringBuilder(String.valueOf(j)).toString());
                                contentValues2.put("delaystart", new StringBuilder(String.valueOf(j2)).toString());
                                contentValues2.put("delayend", new StringBuilder(String.valueOf(j3)).toString());
                                DBManager.update("tb_menu_list", contentValues2, "name = ? ", new String[]{str});
                                XyUtil.doXycallBack(this.d, "1");
                            }
                        }
                    }
                }
                XyUtil.doXycallBack(this.d, Constant.ACTION_PARSE);
            } catch (Throwable th) {
                i.b(this.c);
            }
        }
        if (this.a && SysParamEntityManager.getIntParam(Constant.getContext(), Constant.AUTO_UPDATE_DATA) == 0 && NetUtil.checkAccessNetWork(this.b)) {
            i.b(this.c);
        }
    }
}
