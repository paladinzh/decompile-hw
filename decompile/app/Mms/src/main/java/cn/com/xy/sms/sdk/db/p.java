package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.net.util.i;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class p implements XyCallBack {
    private final /* synthetic */ XyCallBack a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;

    p(XyCallBack xyCallBack, String str, String str2, String str3, String str4, String str5) {
        this.a = xyCallBack;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = str5;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void execute(Object... objArr) {
        SQLiteDatabase sQLiteDatabase = null;
        if (objArr != null && objArr.length == 2 && objArr[0].toString().equals("0")) {
            try {
                JSONObject g = i.g(objArr[1].toString());
                if (g != null) {
                    if (g.length() > 0) {
                        sQLiteDatabase = DBManager.getSQLiteDatabase();
                        g.put("cc", this.c.replace("次", ""));
                        Object optString = g.optString("date");
                        if (StringUtils.isNull(optString)) {
                            optString = this.d;
                        }
                        g.put(TrainManager.DAY, optString);
                        g = TrainManager.b(g);
                        if (g != null) {
                            ContentValues contentValues = TrainManager.getContentValues(g);
                            if (contentValues != null) {
                                BaseManager.saveOrUpdate(sQLiteDatabase, TrainManager.TABLE_NAME, contentValues, "train_num = ? AND day = ? ", new String[]{r3, optString});
                                XyUtil.doXycallBackResult(this.a, this.b, g, this.c, this.e, this.f, Boolean.valueOf(true));
                                DBManager.close(sQLiteDatabase);
                                return;
                            }
                        }
                    }
                }
                XyUtil.doXycallBackResult(this.a, this.b);
                DBManager.close(sQLiteDatabase);
                return;
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                DBManager.close(sQLiteDatabase2);
                throw th3;
            }
        }
        XyUtil.doXycallBackResult(this.a, this.b);
    }
}
