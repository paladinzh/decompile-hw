package cn.com.xy.sms.sdk.ui.popu.util;

import android.text.format.DateUtils;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrainDataUtil extends TravelDataUtil {
    private static final String INTERFACE_STATION_LIST_KEY = "station_list";
    public static final String INTERFACE_TRAIN_ARRIVE_CITY_KEY_START = "db_train_arrive_city_";
    public static final String INTERFACE_TRAIN_ARRIVE_DATE_KEY_START = "db_train_arrive_date_";
    public static final String INTERFACE_TRAIN_ARRIVE_LONG_TIME_KEY_START = "db_train_arrive_dtime_";
    public static final String INTERFACE_TRAIN_ARRIVE_TIME_KEY_START = "db_train_arrive_time_";
    public static final String INTERFACE_TRAIN_DEPART_TIME_KEY_START = "db_train_depart_time_";
    private static final String NETWORK_STATE_KEY_START = "net_work_state_";
    private static final String OFF_NETWORK = "offNetwork";
    private static final String STATION_NAME_KEY = "name";

    private static class TrainDataUtilHolder {
        private static TrainDataUtil instance = new TrainDataUtil();

        private TrainDataUtilHolder() {
        }
    }

    private TrainDataUtil() {
        super("db_train_data_index", "card_arr", "station_list_", "query_time_");
    }

    public static TrainDataUtil getInstance() {
        return TrainDataUtilHolder.instance;
    }

    public void queryTrainStation(BusinessSmsMessage smsMessage, SdkCallBack callBack, String type) {
        try {
            if (!ContentUtil.bubbleDataIsNull(smsMessage) && !hasInterfaceData(smsMessage) && !isRepeatQuery(smsMessage) && !isOffNetwork(smsMessage)) {
                smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), System.currentTimeMillis());
                smsMessage.bubbleJsonObj.put(getNetworkStateKey(smsMessage), null);
                JSONObject viewContentData = getViewContentData(smsMessage);
                if (viewContentData != null) {
                    String trainNum = getTrainNum(viewContentData);
                    if (!StringUtils.isNull(trainNum)) {
                        final Long departDateMills = Long.valueOf(viewContentData.optLong("view_depart_date_time"));
                        final BusinessSmsMessage businessSmsMessage = smsMessage;
                        final String str = type;
                        final SdkCallBack sdkCallBack = callBack;
                        SdkCallBack xyCallBack = new SdkCallBack() {
                            public void execute(Object... results) {
                                if (queryFail(results)) {
                                    try {
                                        if (hasOffNetworkState(results)) {
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getNetworkStateKey(businessSmsMessage), TrainDataUtil.OFF_NETWORK);
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getQueryTimeKey(businessSmsMessage), null);
                                        } else if (isTimeout(results)) {
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getQueryTimeKey(businessSmsMessage), null);
                                        }
                                    } catch (Throwable ex) {
                                        ex.printStackTrace();
                                    }
                                    return;
                                }
                                try {
                                    String stationInfoStr = ((JSONObject) results[1]).optString("station_list");
                                    if (!StringUtils.isNull(stationInfoStr)) {
                                        JSONObject trainDepart = TrainDataUtil.this.getFilterStationData(businessSmsMessage, new JSONArray(stationInfoStr), "view_depart_city");
                                        String str = null;
                                        if (trainDepart != null) {
                                            str = (String) trainDepart.opt("travel_time");
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getInterfaceTrainDepartTimeKey(businessSmsMessage), trainDepart.opt("stt"));
                                        }
                                        JSONObject trainArrive = TrainDataUtil.this.getFilterStationData(businessSmsMessage, new JSONArray(stationInfoStr), "view_arrive_city");
                                        String str2 = null;
                                        if (trainArrive != null) {
                                            str2 = (String) trainArrive.opt("travel_time");
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getInterfaceTrainArriveTimeKey(businessSmsMessage), trainArrive.opt("spt"));
                                        }
                                        if (!StringUtils.isNull(str2)) {
                                            Date date = new Date(departDateMills.longValue() + (TrainDataUtil.this.timeStrTolong(str2) - TrainDataUtil.this.timeStrTolong(str)));
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getInterfaceTrainArriveDateKey(businessSmsMessage), ContentUtil.TRAIN_SUPPLEMENT_DATE + new SimpleDateFormat(ContentUtil.TRAIN_DATE_FORMAT).format(date));
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getInterfaceTrainArriveLongTimeKey(businessSmsMessage), date.getTime());
                                        }
                                        businessSmsMessage.bubbleJsonObj.put("hasQuery" + TrainDataUtil.this.getDataIndex(businessSmsMessage), true);
                                        businessSmsMessage.bubbleJsonObj.put("supplementType" + TrainDataUtil.this.getDataIndex(businessSmsMessage), str);
                                        JSONArray stationInfoJson = TrainDataUtil.this.getFilterStationData(businessSmsMessage, new JSONArray(stationInfoStr));
                                        if (stationInfoJson != null) {
                                            businessSmsMessage.bubbleJsonObj.put(TrainDataUtil.this.getInterfaceDataKey(businessSmsMessage), stationInfoJson);
                                        }
                                        ContentUtil.updateMatchCache(businessSmsMessage);
                                        String smsId = results[0];
                                        ContentUtil.callBackExecute(sdkCallBack, smsId, stationInfoJson);
                                    }
                                } catch (Throwable ex2) {
                                    ex2.printStackTrace();
                                }
                            }

                            private boolean hasOffNetworkState(Object... results) {
                                if (results == null || results.length <= 0 || results[0] == null) {
                                    return false;
                                }
                                return TrainDataUtil.OFF_NETWORK.equalsIgnoreCase(results[0].toString());
                            }

                            private boolean queryFail(Object... results) {
                                if (results == null || results.length != 6 || results[0] == null || results[1] == null) {
                                    return true;
                                }
                                if (results[1] instanceof JSONObject) {
                                    return false;
                                }
                                return true;
                            }

                            private boolean isTimeout(Object... results) {
                                return results != null && results.length > 0 && results[0] == null;
                            }
                        };
                        String departCity = viewContentData.optString("view_depart_city");
                        String arriveCity = viewContentData.optString("view_arrive_city");
                        str = String.valueOf(smsMessage.getSmsId());
                        ParseManager.queryTrainInfo(str, trainNum, departCity, arriveCity, getExtend(smsMessage, str), xyCallBack);
                    }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private long timeStrTolong(String timeStr) {
        if (StringUtils.isNull(timeStr) || !timeStr.contains(":")) {
            return 0;
        }
        String[] time = timeStr.split(":");
        return (((Long.parseLong(time[0]) * 60) + Long.parseLong(time[1])) * 60) * 1000;
    }

    public static JSONObject stationFilter(JSONArray stationInfoJsonArray, String departName) {
        if (stationInfoJsonArray == null || stationInfoJsonArray.length() == 0) {
            return null;
        }
        JSONObject result = null;
        int len = stationInfoJsonArray.length();
        int i = 0;
        while (i < len) {
            try {
                if (stationInfoJsonArray.getJSONObject(i).optString("name").equalsIgnoreCase(departName)) {
                    result = stationInfoJsonArray.getJSONObject(i);
                }
                i++;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static JSONArray stationFilter(JSONArray stationInfoJsonArray, String departName, String arriveName) {
        if (stationInfoJsonArray == null || stationInfoJsonArray.length() == 0) {
            return null;
        }
        JSONArray result = new JSONArray();
        boolean addStationInfo = false;
        int len = stationInfoJsonArray.length();
        int i = 0;
        while (i < len) {
            try {
                String stationName = stationInfoJsonArray.getJSONObject(i).optString("name");
                if (addStationInfo) {
                    result.put(stationInfoJsonArray.getJSONObject(i));
                }
                if (stationName.equalsIgnoreCase(departName)) {
                    addStationInfo = true;
                }
                if (stationName.equalsIgnoreCase(arriveName)) {
                    break;
                }
                i++;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public JSONObject getInterfaceData(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONObject(getInterfaceDataKey(smsMessage));
    }

    public int getDefaultStationSelectedIndex(BusinessSmsMessage smsMessage) {
        int defaultSelectedIndex = 0;
        try {
            defaultSelectedIndex = Integer.parseInt(getDataIndex(smsMessage));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultSelectedIndex;
    }

    public void saveSelectedStationData(BusinessSmsMessage smsMessage, String stationName, String arriveTime) {
        if (!ContentUtil.bubbleDataIsNull(smsMessage)) {
            try {
                smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveCityKey(smsMessage), stationName);
                smsMessage.bubbleJsonObj.put(getInterfaceTrainArriveTimeKey(smsMessage), arriveTime);
                ContentUtil.updateMatchCache(smsMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONArray putSelectedStationDataToSimpleBubble(JSONObject bubbleData) {
        if (bubbleData == null || bubbleData.length() == 0) {
            return null;
        }
        try {
            JSONArray simpleBubbleData = ContentUtil.getActionJsonArray(bubbleData);
            if (simpleBubbleData == null || simpleBubbleData.length() == 0) {
                return null;
            }
            for (int i = 0; i < simpleBubbleData.length(); i++) {
                JSONObject actionData = simpleBubbleData.optJSONObject(i);
                if (actionData != null && ContentUtil.WEB_TRAIN_STATION_NEW.equals(actionData.optString("action"))) {
                    JSONObject selectedStationData = new JSONObject();
                    int dataIndex = bubbleData.optInt("db_train_data_index", 0);
                    String arriveCityKey = INTERFACE_TRAIN_ARRIVE_CITY_KEY_START + dataIndex;
                    String arriveLongTimeKey = INTERFACE_TRAIN_ARRIVE_LONG_TIME_KEY_START + dataIndex;
                    selectedStationData.put(arriveCityKey, bubbleData.optString(arriveCityKey));
                    selectedStationData.put(arriveLongTimeKey, bubbleData.optLong(arriveLongTimeKey, 0));
                    selectedStationData.put("train_list", bubbleData.optJSONArray("train_list"));
                    actionData.put("bubbleJson", selectedStationData);
                    break;
                }
            }
            return simpleBubbleData;
        } catch (Exception ex) {
            LogManager.e("XIAOYUAN", "TrainDataUtil putSelectedStationDataToSimpleBubble error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String getTrainNum(JSONObject viewContentData) {
        if (viewContentData == null) {
            return null;
        }
        return viewContentData.optString(viewContentData.has("view_m_trainnumber") ? "view_m_trainnumber" : "view_train_number");
    }

    public String getInterfaceTrainArriveCity(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveCityKey(smsMessage));
    }

    public String getInterfaceTrainArriveCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_CITY_KEY_START);
    }

    private JSONObject getFilterStationData(BusinessSmsMessage smsMessage, JSONArray interfaceData, String key) {
        if (interfaceData == null || ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        JSONObject viewContentData = getViewContentData(smsMessage);
        if (viewContentData == null) {
            return null;
        }
        return stationFilter(interfaceData, viewContentData.optString(key));
    }

    private JSONArray getFilterStationData(BusinessSmsMessage smsMessage, JSONArray interfaceData) {
        if (interfaceData == null || ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        JSONObject viewContentData = getViewContentData(smsMessage);
        if (viewContentData == null) {
            return null;
        }
        return stationFilter(interfaceData, viewContentData.optString("view_depart_city"), viewContentData.optString("view_arrive_city"));
    }

    private String getNetworkState(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getNetworkStateKey(smsMessage));
    }

    private String getNetworkStateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, NETWORK_STATE_KEY_START);
    }

    private Map<String, Object> getExtend(BusinessSmsMessage smsMessage, String smsId) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        Map<String, Object> extend = new HashMap();
        extend.put("phoneNumber", smsMessage.bubbleJsonObj.optString("phoneNum"));
        extend.put("titleNo", smsMessage.getTitleNo());
        extend.put("msgId", smsId);
        extend.put("bubbleJsonObj", smsMessage.bubbleJsonObj.toString());
        extend.put("messageBody", smsMessage.getMessageBody());
        extend.put("notSaveToDb", Boolean.TRUE.toString());
        return extend;
    }

    private boolean isOffNetwork(BusinessSmsMessage smsMessage) {
        if (NetUtil.checkAccessNetWork(2)) {
            return false;
        }
        return OFF_NETWORK.equalsIgnoreCase(getNetworkState(smsMessage));
    }

    private boolean isRepeatQuery(BusinessSmsMessage smsMessage) {
        if (!DateUtils.isToday(getQueryTime(smsMessage)) || OFF_NETWORK.equalsIgnoreCase(getNetworkState(smsMessage))) {
            return false;
        }
        return true;
    }

    public String getInterfaceTrainDepartTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainDepartTimeKey(smsMessage));
    }

    public String getInterfaceTrainDepartTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_DEPART_TIME_KEY_START);
    }

    public String getInterfaceTrainArriveDate(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveDateKey(smsMessage));
    }

    public String getInterfaceTrainArriveDateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_DATE_KEY_START);
    }

    public String getInterfaceTrainArriveTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceTrainArriveTimeKey(smsMessage));
    }

    public String getInterfaceTrainArriveTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_TIME_KEY_START);
    }

    public String getInterfaceTrainArriveLongTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_TRAIN_ARRIVE_LONG_TIME_KEY_START);
    }
}
