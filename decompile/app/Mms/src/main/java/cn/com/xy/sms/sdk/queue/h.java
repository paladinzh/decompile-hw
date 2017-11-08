package cn.com.xy.sms.sdk.queue;

import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
final class h extends Thread {
    h() {
    }

    public final void run() {
        try {
            setName("xiaoyuan_bubbletaskqueue");
            Process.setThreadPriority(i.b);
            while (true) {
                JSONObject jSONObject = (JSONObject) g.a.take();
                if (jSONObject != null) {
                    Integer num = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "dataStatu");
                    if (num != null) {
                        Integer num2 = (Integer) JsonUtil.getValueFromJsonObject(jSONObject, "dataType");
                        if (num2 != null) {
                            String str;
                            Object obj;
                            Map parseMsgToBubbleCardResult;
                            if (num2.intValue() == 2) {
                                if (num.intValue() == 2) {
                                    str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "save_time");
                                    if (!StringUtils.isNull(str)) {
                                        if ((System.currentTimeMillis() - Long.valueOf(str).longValue() <= 2592000 ? 1 : null) == null) {
                                        }
                                    }
                                    obj = null;
                                    if (obj != null) {
                                        parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                        if (!(parseMsgToBubbleCardResult == null || ((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID")) == null)) {
                                            D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                        }
                                    }
                                }
                                obj = 1;
                                if (obj != null) {
                                    parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                    D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                }
                            } else if (num2.intValue() == 1) {
                                Map hashMap;
                                if (num.intValue() == 2) {
                                    str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "session_lasttime");
                                    if (!StringUtils.isNull(str)) {
                                        if ((System.currentTimeMillis() - Long.valueOf(str).longValue() <= 2592000 ? 1 : null) == null) {
                                        }
                                    }
                                    obj = null;
                                    if (obj != null) {
                                        hashMap = new HashMap();
                                        hashMap.put("msgTime", (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime"));
                                        parseMsgToBubbleCardResult = ParseRichBubbleManager.parseMsgToSimpleBubbleResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (byte) 1, hashMap);
                                        if (parseMsgToBubbleCardResult != null) {
                                            r2 = (Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID");
                                            if (r2 == null) {
                                                if ((r2.longValue() > -1 ? 1 : null) == null) {
                                                    D.a((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf(r2)}));
                                                }
                                            }
                                        }
                                    }
                                }
                                obj = 1;
                                if (obj != null) {
                                    hashMap = new HashMap();
                                    hashMap.put("msgTime", (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime"));
                                    parseMsgToBubbleCardResult = ParseRichBubbleManager.parseMsgToSimpleBubbleResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (byte) 1, hashMap);
                                    if (parseMsgToBubbleCardResult != null) {
                                        r2 = (Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID");
                                        if (r2 == null) {
                                            if (r2.longValue() > -1) {
                                            }
                                            if ((r2.longValue() > -1 ? 1 : null) == null) {
                                                D.a((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf(r2)}));
                                            }
                                        }
                                    }
                                }
                            } else if (num2.intValue() == 3) {
                                Object obj2 = null;
                                if (num.intValue() == 2) {
                                    str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "session_lasttime");
                                    if (!StringUtils.isNull(str)) {
                                        if ((System.currentTimeMillis() - Long.valueOf(str).longValue() <= 2592000 ? 1 : null) == null) {
                                        }
                                    }
                                    if (obj2 != null) {
                                        parseMsgToBubbleCardResult = ParseRichBubbleManager.parseMsgToSimpleBubbleResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (byte) 1, null);
                                        if (parseMsgToBubbleCardResult != null) {
                                            r2 = (Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID");
                                            if (r2 != null) {
                                                if ((r2.longValue() > -1 ? 1 : null) == null) {
                                                    D.a((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf(r2)}));
                                                }
                                            }
                                        }
                                    }
                                    if (num.intValue() == 2) {
                                        str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "save_time");
                                        if (!StringUtils.isNull(str)) {
                                            if ((System.currentTimeMillis() - Long.valueOf(str).longValue() > 2592000 ? 1 : null) == null) {
                                            }
                                        }
                                        obj = obj2;
                                        if (obj == null) {
                                            parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                            if (!(parseMsgToBubbleCardResult == null || ((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID")) == null)) {
                                                D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                            }
                                        }
                                    }
                                    obj = 1;
                                    if (obj == null) {
                                        parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                        D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                    }
                                }
                                obj2 = 1;
                                if (obj2 != null) {
                                    parseMsgToBubbleCardResult = ParseRichBubbleManager.parseMsgToSimpleBubbleResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), (byte) 1, null);
                                    if (parseMsgToBubbleCardResult != null) {
                                        r2 = (Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID");
                                        if (r2 != null) {
                                            if (r2.longValue() > -1) {
                                            }
                                            if ((r2.longValue() > -1 ? 1 : null) == null) {
                                                D.a((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf(r2)}));
                                            }
                                        }
                                    }
                                }
                                if (num.intValue() == 2) {
                                    str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "save_time");
                                    if (StringUtils.isNull(str)) {
                                        if (System.currentTimeMillis() - Long.valueOf(str).longValue() > 2592000) {
                                        }
                                        if ((System.currentTimeMillis() - Long.valueOf(str).longValue() > 2592000 ? 1 : null) == null) {
                                        }
                                    }
                                    obj = obj2;
                                    if (obj == null) {
                                        parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                        D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                    }
                                }
                                obj = 1;
                                if (obj == null) {
                                    parseMsgToBubbleCardResult = ParseManager.parseMsgToBubbleCardResult(Constant.getContext(), (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_id"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "centerNum"), (String) JsonUtil.getValueFromJsonObject(jSONObject, "smsContent"), ((Long) JsonUtil.getValueFromJsonObject(jSONObject, "smsReceiveTime")).longValue(), (byte) 1, null);
                                    D.b((String) JsonUtil.getValueFromJsonObject(jSONObject, "phoneNum"), MatchCacheManager.loadDataByParam("id=?", new String[]{String.valueOf((Long) parseMsgToBubbleCardResult.get("CACHE_SDK_MSG_ID"))}));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
