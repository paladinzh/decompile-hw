package cn.com.xy.sms.sdk.Iservice;

import android.content.Context;
import java.util.Map;

/* compiled from: Unknown */
public interface OnlineParseInterface {
    int getActionCode(String str);

    String getCorp(String str);

    String getReqVersion(String str);

    String getSceneVersion(String str);

    int getSmsTypeByMap(Map<String, Object> map, int i);

    boolean isAppChannel(String str);

    boolean isEnterpriseSms(Context context, String str, String str2, Map<String, String> map);

    int isServiceChoose(String str, String str2);

    Map<String, Object> parseMessage(String str, String str2, Map<String, String> map);

    String[] parseMsgToNewContacts(String str, String str2, String str3, String[] strArr);

    Map<String, Object> parseVerCode(String str, String str2, Map<String, String> map);
}
