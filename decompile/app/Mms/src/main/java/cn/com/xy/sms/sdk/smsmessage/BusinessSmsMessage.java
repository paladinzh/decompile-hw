package cn.com.xy.sms.sdk.smsmessage;

import cn.com.xy.sms.sdk.util.JsonUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class BusinessSmsMessage implements Serializable, Cloneable {
    private static final long a = 1;
    public static BusinessSmsMessage emptyObj = null;
    private HashMap<String, List<Map<String, String>>> b = null;
    public JSONObject bubbleJsonObj = null;
    private HashMap<String, JSONArray> c = null;
    public HashMap<String, Object> extendParamMap = null;
    public HashMap<String, String> imagePathMap = null;
    public boolean isBgVis;
    public boolean isPopByWeishi = false;
    public String messageBody;
    public long msgTime = 0;
    public String originatingAddress;
    public boolean reBindData = false;
    public int simIndex = -1;
    public String simName = "";
    public long smsId = -1;
    public String titleNo;
    public Map<String, Object> valueMap = null;
    public byte viewType = (byte) 0;

    public static BusinessSmsMessage createMsgObj() {
        try {
            if (emptyObj == null) {
                emptyObj = new BusinessSmsMessage();
            }
            return (BusinessSmsMessage) emptyObj.clone();
        } catch (CloneNotSupportedException e) {
            return new BusinessSmsMessage();
        }
    }

    public Object clone() {
        return super.clone();
    }

    public JSONArray getActionJsonArray() {
        Object value = getValue("ADACTION");
        if (value == null) {
            return null;
        }
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        JSONArray parseStrToJsonArray = JsonUtil.parseStrToJsonArray((String) value);
        putValue("ADACTION", parseStrToJsonArray);
        return parseStrToJsonArray;
    }

    public String getCenterAddress() {
        return "";
    }

    public Object getExtendParamValue(String str) {
        return this.extendParamMap == null ? null : this.extendParamMap.get(str);
    }

    public String getImgNameByKey(String str) {
        String str2;
        if (this.viewType != (byte) 0) {
            if (this.bubbleJsonObj != null && this.bubbleJsonObj.has(str)) {
                try {
                    str2 = (String) this.bubbleJsonObj.get(str);
                } catch (Throwable th) {
                }
                return str2 == null ? str2 : str2.trim().replace(" ", "");
            }
        } else if (this.imagePathMap != null) {
            str2 = (String) this.imagePathMap.get(str);
            if (str2 == null) {
            }
        }
        str2 = null;
        if (str2 == null) {
        }
    }

    public String getMessageBody() {
        return this.messageBody;
    }

    public String getOriginatingAddress() {
        return this.originatingAddress;
    }

    public long getSmsId() {
        return this.smsId;
    }

    public Object getTableData(int i, String str) {
        if (this.viewType != (byte) 0) {
            if (this.c != null) {
                try {
                    JSONArray jSONArray = (JSONArray) this.c.get(str);
                    if (jSONArray != null && i >= 0 && jSONArray.length() > i) {
                        return jSONArray.get(i);
                    }
                } catch (Throwable th) {
                }
            }
        } else if (this.b != null) {
            List list = (List) this.b.get(str);
            if (list != null && i >= 0) {
                try {
                    if (list.size() > i) {
                        return list.get(i);
                    }
                } catch (Throwable th2) {
                }
            }
        }
        return null;
    }

    public int getTableDataSize(String str) {
        try {
            Object value;
            if (this.viewType != (byte) 0) {
                if (this.c == null) {
                    this.c = new HashMap();
                }
                JSONArray jSONArray = (JSONArray) this.c.get(str);
                if (jSONArray == null) {
                    value = getValue(str);
                    if (value != null && (value instanceof JSONArray)) {
                        jSONArray = (JSONArray) value;
                        this.c.put(str, jSONArray);
                    }
                }
                if (jSONArray != null) {
                    return jSONArray.length();
                }
            }
            if (this.b == null) {
                this.b = new HashMap();
            }
            List list = (List) this.b.get(str);
            if (list == null) {
                value = getValue(str);
                if (value != null && (value instanceof List)) {
                    list = (List) value;
                    this.b.put(str, list);
                }
            }
            if (list != null) {
                return list.size();
            }
        } catch (Throwable th) {
        }
        return 0;
    }

    public String getTitleNo() {
        return this.titleNo;
    }

    public Object getValue(String str) {
        if (this.viewType != (byte) 0) {
            if (this.bubbleJsonObj != null && this.bubbleJsonObj.has(str)) {
                try {
                    return this.bubbleJsonObj.get(str);
                } catch (Throwable th) {
                }
            }
        } else if (this.valueMap != null) {
            return this.valueMap.get(str);
        }
        return null;
    }

    public boolean isDataNull(String str) {
        if (this.viewType != (byte) 0) {
            if (this.c != null) {
                JSONArray jSONArray = (JSONArray) this.c.get(str);
                if (jSONArray != null && jSONArray.length() > 0) {
                    return false;
                }
            }
        } else if (this.b != null) {
            List list = (List) this.b.get(str);
            if (list != null && list.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void putValue(String str, Object obj) {
        if (str != null && obj != null) {
            if (this.viewType != (byte) 0) {
                if (this.bubbleJsonObj == null) {
                    this.bubbleJsonObj = new JSONObject();
                }
                try {
                    this.bubbleJsonObj.put(str, obj);
                    return;
                } catch (Throwable th) {
                    return;
                }
            }
            if (this.valueMap == null) {
                this.valueMap = new HashMap();
            }
            this.valueMap.put(str, obj);
        }
    }

    public void setMessageBody(String str) {
        this.messageBody = str;
    }

    public void setOriginatingAddress(String str) {
        this.originatingAddress = str;
    }

    public void setSmsId(long j) {
        this.smsId = j;
    }

    public void setTitleNo(String str) {
        this.titleNo = str;
    }
}
