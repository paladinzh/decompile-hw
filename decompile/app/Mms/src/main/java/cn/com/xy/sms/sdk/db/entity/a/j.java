package cn.com.xy.sms.sdk.db.entity.a;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;

    j(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public final void run() {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("isuse", "1");
            if (StringUtils.isNull(this.a)) {
                DBManager.update("tb_public_num_info", contentValues, " num = ? ", new String[]{this.b});
                return;
            }
            DBManager.update("tb_public_num_info", contentValues, " num = ? and areaCode LIKE '%" + this.a + "%'", new String[]{this.b});
        } catch (Throwable th) {
        }
    }
}
