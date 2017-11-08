package com.huawei.hwid.core.c;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;

/* compiled from: VerifyCodeUtil */
public class s extends ContentObserver {
    private Context a;
    private Handler b;

    public s(Context context, Handler handler) {
        super(handler);
        this.a = context;
        this.b = handler;
    }

    public void onChange(boolean z) {
        String str;
        Uri parse = Uri.parse("content://sms/inbox");
        String[] strArr = new String[]{"body"};
        String[] strArr2 = new String[]{"0", "1"};
        Cursor query = this.a.getContentResolver().query(parse, strArr, "read=? and type=?", strArr2, "_id desc");
        if (query == null) {
            str = null;
        } else if (query.moveToFirst()) {
            str = query.getString(query.getColumnIndex("body"));
        } else {
            str = null;
        }
        if (query != null) {
            query.close();
        }
        Object a = r.b(str, this.a);
        if (TextUtils.isEmpty(a)) {
            a.d("VerifyCodeUtil", "verifyCode is null or empty");
        } else {
            Message obtainMessage = this.b.obtainMessage(1);
            Bundle bundle = new Bundle();
            bundle.putString("verifyCode", a);
            obtainMessage.setData(bundle);
            this.b.handleMessage(obtainMessage);
        }
        super.onChange(z);
    }
}
