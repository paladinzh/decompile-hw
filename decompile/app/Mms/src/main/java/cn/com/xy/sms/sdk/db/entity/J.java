package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ int a;
    private final /* synthetic */ int b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;

    j(int i, int i2, String str, String str2, String str3) {
        this.a = i;
        this.b = i2;
        this.c = str;
        this.d = str2;
        this.e = str3;
    }

    public final void run() {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("is_mark", Integer.valueOf(this.a));
            contentValues.put(Constant.IS_FAVORITE, Integer.valueOf(this.b));
            String md5 = MatchCacheManager.getMD5(this.c, this.d);
            String[] strArr = new String[]{this.e, md5};
            DBManager.update(MatchCacheManager.TABLE_NAME, contentValues, "msg_id = ? and msg_num_md5 = ?", strArr);
        } catch (Throwable th) {
        }
    }
}
