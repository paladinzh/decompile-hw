package cn.com.xy.sms.sdk.ui.popu.util;

import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class FlightDataUtil extends TravelDataUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String FIGHT = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_fight);
    private static final String INTERFACE_FLIGHT_ARRIVE_CITY_KEY_START = "db_flight_arrive_city_";
    private static final String INTERFACE_FLIGHT_ARRIVE_DATE_KEY_START = "db_flight_arrive_date_";
    private static final String INTERFACE_FLIGHT_ARRIVE_DATE_TIME_KEY_START = "db_flight_arrive_date_time_";
    private static final String INTERFACE_FLIGHT_ARRIVE_TIME_KEY_START = "db_flight_arrive_time_";
    private static final String INTERFACE_FLIGHT_DEPART_CITY_KEY_START = "db_flight_depart_city_";
    private static final String INTERFACE_FLIGHT_DEPART_TIME_KEY_START = "db_flight_depart_time_";
    private static final String INTERFACE_FLIGHT_STATE_KEY_START = "db_flight_state_";
    private final SimpleDateFormat MMDD;
    private final SimpleDateFormat YYYYMMDDHHMM;

    private static class FlightDataUtilHolder {
        private static FlightDataUtil instance = new FlightDataUtil();

        private FlightDataUtilHolder() {
        }
    }

    private FlightDataUtil() {
        super("db_air_data_index", "flight_data_arr", "flight_data_", "query_time_");
        this.YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.MMDD = new SimpleDateFormat(ContentUtil.TRAIN_DATE_FORMAT);
    }

    public static FlightDataUtil getInstance() {
        return FlightDataUtilHolder.instance;
    }

    public void queryFlyData(final BusinessSmsMessage smsMessage, final SdkCallBack callBack) {
        try {
            if (!ContentUtil.bubbleDataIsNull(smsMessage) && !hasInterfaceData(smsMessage) && !isOffNetwork() && !isRepeatQuery(smsMessage)) {
                smsMessage.bubbleJsonObj.put(getQueryTimeKey(smsMessage), System.currentTimeMillis());
                JSONObject viewContentData = getViewContentData(smsMessage);
                if (viewContentData != null) {
                    String flightNum = getFlightNum(viewContentData);
                    if (!StringUtils.isNull(flightNum)) {
                        ParseManager.queryFlightData(String.valueOf(smsMessage.getSmsId()), flightNum, viewContentData.optString("view_depart_date"), getExtend(smsMessage, viewContentData), new SdkCallBack() {
                            public void execute(Object... results) {
                                try {
                                    if (!queryFail(results)) {
                                        saveArriveInfo(smsMessage, results);
                                        String smsId = results[0].toString();
                                        ContentUtil.callBackExecute(callBack, smsId);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                            private void saveArriveInfo(BusinessSmsMessage smsMessage, Object... obj) {
                                try {
                                    FlightDataUtil.this.savaArriveInfo(smsMessage, obj[1]);
                                } catch (Exception e) {
                                    SmartSmsSdkUtil.smartSdkExceptionLog("FlightDataUtil saveArriveInfo error: " + e.getMessage(), e);
                                }
                            }

                            private boolean queryFail(Object... results) {
                                if (results == null || results.length != 2 || results[0] == null || results[1] == null) {
                                    return true;
                                }
                                if (results[1] instanceof JSONObject) {
                                    return false;
                                }
                                return true;
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savaArriveInfo(BusinessSmsMessage smsMessage, JSONObject flightData) throws JSONException {
        smsMessage.bubbleJsonObj.put(getInterfaceDataKey(smsMessage), flightData);
        smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveCityKey(smsMessage), getCityAndAirportAndHTerminal(flightData, "FlightArr", "FlightArrAirport", "FlightTerminal"));
        smsMessage.bubbleJsonObj.put(getInterfaceFlightDepartCityKey(smsMessage), getCityAndAirportAndHTerminal(flightData, "FlightDep", "FlightDepAirport", "FlightHTerminal"));
        smsMessage.bubbleJsonObj.put(getInterfaceFlightStateKey(smsMessage), flightData.opt("FlightState"));
        Date arriveDateTime = ContentUtil.stringToDate(flightData.optString("FlightArrtimePlanDate"), DATE_FORMAT);
        if (arriveDateTime != null) {
            String dateTime = ContentUtil.getFormatDate(arriveDateTime, this.YYYYMMDDHHMM);
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveDateTimeKey(smsMessage), dateTime);
            String[] dateTimeArr = dateTime.split(" ");
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveDateKey(smsMessage), ContentUtil.getFormatDate(arriveDateTime, this.MMDD));
            smsMessage.bubbleJsonObj.put(getInterfaceFlightArriveTimeKey(smsMessage), dateTimeArr[1]);
        }
        Date departDateTime = ContentUtil.stringToDate(flightData.optString("FlightDeptimePlanDate"), DATE_FORMAT);
        if (departDateTime != null) {
            smsMessage.bubbleJsonObj.put(getInterfaceFlightDepartTimeKey(smsMessage), ContentUtil.getFormatDate(departDateTime, this.YYYYMMDDHHMM).split(" ")[1]);
        }
        smsMessage.bubbleJsonObj.put("hasQuery" + getDataIndex(smsMessage), true);
        ContentUtil.updateMatchCache(smsMessage);
    }

    private Map<String, Object> getExtend(BusinessSmsMessage smsMessage, JSONObject viewContentData) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        String departCity = viewContentData.optString("view_depart_city");
        String arriveCity = viewContentData.optString("view_arrive_city");
        Map<String, Object> extend = new HashMap();
        extend.put("flight_form", departCity);
        extend.put("flight_to", arriveCity);
        extend.put("flight_from_airport", departCity);
        extend.put("flight_to_airport", arriveCity);
        extend.put("phoneNumber", smsMessage.bubbleJsonObj.optString("phoneNum"));
        extend.put("titleNo", smsMessage.getTitleNo());
        extend.put("msgId", String.valueOf(smsMessage.getSmsId()));
        extend.put("bubbleJsonObj", smsMessage.bubbleJsonObj.toString());
        extend.put("messageBody", smsMessage.getMessageBody());
        extend.put("notSaveToDb", Boolean.TRUE.toString());
        return extend;
    }

    public JSONObject getInterfaceData(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optJSONObject(getInterfaceDataKey(smsMessage));
    }

    public String getInterfaceFlightArriveCity(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveCityKey(smsMessage));
    }

    public String getInterfaceFlightArriveCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_CITY_KEY_START);
    }

    public String getInterfaceFlightArriveDate(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveDateKey(smsMessage));
    }

    public String getInterfaceFlightArriveDateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_DATE_KEY_START);
    }

    public String getInterfaceFlightArriveTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveTimeKey(smsMessage));
    }

    public String getInterfaceFlightArriveTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_TIME_KEY_START);
    }

    public String getInterfaceFlightArriveDateTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightArriveDateTimeKey(smsMessage));
    }

    public String getInterfaceFlightArriveDateTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_ARRIVE_DATE_TIME_KEY_START);
    }

    public void setViewValue(String value, TextView textView, ImageView lostValueShowImage) {
        TravelDataUtil.setViewValue(value, textView, ContentUtil.NO_DATA_EN, lostValueShowImage);
    }

    public String getCityAndAirport(JSONObject flightData, String cityKey, String airportKey) {
        if (flightData == null) {
            return null;
        }
        String arriveCity = flightData.optString(cityKey);
        String arriveAirport = flightData.optString(airportKey);
        StringBuilder append = new StringBuilder().append(arriveCity).append(" ");
        if (arriveAirport.startsWith(arriveCity)) {
            arriveAirport = arriveAirport.replace(arriveCity, "");
        }
        return append.append(arriveAirport).toString();
    }

    public String getCityAndAirportAndHTerminal(JSONObject flightData, String cityKey, String airportKey, String hTerminal) {
        if (flightData == null) {
            return null;
        }
        return getCityAndAirport(flightData, cityKey, airportKey) + flightData.optString(hTerminal);
    }

    private static String getFlightNum(JSONObject viewContentData) {
        if (viewContentData == null) {
            return null;
        }
        return TravelDataUtil.getDataByKey(viewContentData, "view_flight_number", FIGHT, " ");
    }

    private static boolean isOffNetwork() {
        return !NetUtil.checkAccessNetWork(2);
    }

    private boolean isRepeatQuery(BusinessSmsMessage smsMessage) {
        return DateUtils.isToday(getQueryTime(smsMessage));
    }

    public String getInterfaceFlightDepartTime(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightDepartTimeKey(smsMessage));
    }

    public String getInterfaceFlightDepartTimeKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_DEPART_TIME_KEY_START);
    }

    public String getInterfaceFlightDepartCity(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightDepartCityKey(smsMessage));
    }

    public String getInterfaceFlightDepartCityKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_DEPART_CITY_KEY_START);
    }

    public String getInterfaceFlightState(BusinessSmsMessage smsMessage) {
        if (ContentUtil.bubbleDataIsNull(smsMessage)) {
            return null;
        }
        return smsMessage.bubbleJsonObj.optString(getInterfaceFlightStateKey(smsMessage));
    }

    public String getInterfaceFlightStateKey(BusinessSmsMessage smsMessage) {
        return getKey(smsMessage, INTERFACE_FLIGHT_STATE_KEY_START);
    }
}
