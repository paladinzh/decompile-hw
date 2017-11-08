package cn.com.xy.sms.sdk.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import java.util.Map;

/* compiled from: Unknown */
final class e extends AbsSdkDoAction {
    e() {
    }

    public final void deleteMsgForDatabase(Context context, String str) {
    }

    public final String getContactName(Context context, String str) {
        return null;
    }

    public final void markAsReadForDatabase(Context context, String str) {
    }

    public final void openSms(Context context, String str, Map<String, String> map) {
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("sms:" + str)));
    }

    public final void openSmsDetail(Context context, String str, Map map) {
    }

    public final void sendSms(Context context, String str, String str2, int i, Map<String, String> map) {
        try {
            Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:" + str));
            intent.putExtra("sms_body", str2);
            context.startActivity(intent);
        } catch (Throwable th) {
        }
    }
}
