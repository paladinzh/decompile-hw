package cn.com.xy.sms.sdk.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.Iservice.IActionService;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class m implements IActionService {
    private static IActionService a = new m();

    public static boolean a(Context context, String str, String str2, JSONObject jSONObject) {
        try {
            if (!I.a(context, str)) {
                return false;
            }
            IActionService actionInterfaceImpl = DexUtil.getActionInterfaceImpl(false);
            return actionInterfaceImpl == null ? a.startAppActionViewURL(context, str, str2, jSONObject) : actionInterfaceImpl.startAppActionViewURL(context, str, str2, jSONObject);
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean a(Context context, String str, JSONObject jSONObject) {
        try {
            IActionService actionInterfaceImpl = DexUtil.getActionInterfaceImpl(false);
            if (actionInterfaceImpl != null) {
                return actionInterfaceImpl.doAction(context, str, jSONObject);
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public final boolean doAction(Context context, String str, JSONObject jSONObject) {
        return false;
    }

    public final boolean startAppActionViewURL(Context context, String str, String str2, JSONObject jSONObject) {
        if (context != null) {
            try {
                if (!(StringUtils.isNull(str) || jSONObject == null)) {
                    Intent intent;
                    Intent intent2;
                    int i;
                    Iterator keys;
                    Object obj;
                    String optString = jSONObject.optString("viewUrl");
                    String optString2 = jSONObject.optString("className");
                    JSONArray optJSONArray = jSONObject.optJSONArray("flags");
                    String optString3 = jSONObject.optString("actionName");
                    JSONObject optJSONObject = jSONObject.optJSONObject("extras");
                    String optString4 = jSONObject.optString(NumberInfo.TYPE_KEY);
                    if (!StringUtils.isNull(optString) || !StringUtils.isNull(optString3) || !StringUtils.isNull(optString4)) {
                        intent = new Intent();
                        if (StringUtils.isNull(optString3)) {
                            optString3 = "android.intent.action.VIEW";
                        }
                        if (!StringUtils.isNull(optString)) {
                            intent.setData(Uri.parse(optString));
                        }
                        if (!StringUtils.isNull(optString4)) {
                            intent.setType(optString4);
                        }
                        intent.setAction(optString3);
                    } else if (StringUtils.isNull(str)) {
                        intent2 = null;
                        if (intent2 != null) {
                            return false;
                        }
                        if (!(StringUtils.isNull(str) || StringUtils.isNull(optString2))) {
                            intent2.setComponent(new ComponentName(str, optString2));
                        }
                        if (optJSONArray != null && optJSONArray.length() > 0) {
                            for (i = 0; i < optJSONArray.length(); i++) {
                                intent2.addFlags(optJSONArray.getInt(i));
                            }
                        }
                        if (optJSONObject != null && optJSONObject.length() > 0) {
                            keys = optJSONObject.keys();
                            while (keys.hasNext()) {
                                optString2 = keys.next().toString();
                                if (!TextUtils.isEmpty(optString2)) {
                                    obj = optJSONObject.get(optString2);
                                    if (!(obj instanceof String)) {
                                        intent2.putExtra(optString2, (String) obj);
                                    } else if (!(obj instanceof Boolean)) {
                                        intent2.putExtra(optString2, (Boolean) obj);
                                    } else if (obj instanceof Integer) {
                                        intent2.putExtra(optString2, (Integer) obj);
                                    }
                                }
                            }
                        }
                        context.startActivity(intent2);
                        return true;
                    } else {
                        intent = context.getPackageManager().getLaunchIntentForPackage(str);
                    }
                    intent2 = intent;
                    if (intent2 != null) {
                        return false;
                    }
                    intent2.setComponent(new ComponentName(str, optString2));
                    for (i = 0; i < optJSONArray.length(); i++) {
                        intent2.addFlags(optJSONArray.getInt(i));
                    }
                    keys = optJSONObject.keys();
                    while (keys.hasNext()) {
                        optString2 = keys.next().toString();
                        if (!TextUtils.isEmpty(optString2)) {
                            obj = optJSONObject.get(optString2);
                            if (!(obj instanceof String)) {
                                intent2.putExtra(optString2, (String) obj);
                            } else if (!(obj instanceof Boolean)) {
                                intent2.putExtra(optString2, (Boolean) obj);
                            } else if (obj instanceof Integer) {
                                intent2.putExtra(optString2, (Integer) obj);
                            }
                        }
                    }
                    context.startActivity(intent2);
                    return true;
                }
            } catch (Throwable th) {
                return false;
            }
        }
        return false;
    }
}
