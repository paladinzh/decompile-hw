package cn.com.xy.sms.sdk.Iservice;

import android.content.Context;
import org.json.JSONObject;

/* compiled from: Unknown */
public interface IActionService {
    boolean doAction(Context context, String str, JSONObject jSONObject);

    boolean startAppActionViewURL(Context context, String str, String str2, JSONObject jSONObject);
}
