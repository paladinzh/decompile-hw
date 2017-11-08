package cn.com.xy.sms.sdk.ui.popu.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.HashMap;
import java.util.Map.Entry;
import org.json.JSONObject;

public class SimpleButtonUtil {
    private static final int DRAWABLE_BOUNDS_RIGHT = ((int) ViewUtil.getDimension(R.dimen.duoqu_drawable_bounds_right));
    static int mBottom = 0;
    static int mRight = 0;
    private static int mTop = 0;

    public static void setButtonTextAndImg(Context mContext, TextView buttonText, String action, boolean disLogo, boolean isClickAble) {
        if (buttonText != null) {
            try {
                int resLogoId = bindButtonData(buttonText, action, StringUtils.isNull(buttonText.getText().toString()), isClickAble);
                if (!disLogo || resLogoId == -1) {
                    buttonText.setCompoundDrawablesRelative(null, null, null, null);
                }
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                dw.setBounds(0, getTop(), getRight(), getBottom());
                buttonText.setCompoundDrawablesRelative(dw, null, null, null);
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
            }
        }
    }

    public static int bindButtonData(TextView buttonText, String action, boolean setText, boolean isClickAble) {
        if (StringUtils.isNull(action)) {
            return -1;
        }
        int resLogoId;
        if (action.equalsIgnoreCase(Constant.URLS) || action.equalsIgnoreCase("access_url")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_network;
            } else {
                resLogoId = R.drawable.duoqu_network_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_open_net);
            return resLogoId;
        } else if (action.equalsIgnoreCase("reply_sms") || action.equalsIgnoreCase("send_sms")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_reply;
            } else {
                resLogoId = R.drawable.duoqu_reply_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_reply_sms);
            return resLogoId;
        } else if (action.equalsIgnoreCase("reply_sms_fwd")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_reply;
            } else {
                resLogoId = R.drawable.duoqu_reply_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_forword_sms);
            return resLogoId;
        } else if (action.equalsIgnoreCase("call_phone") || action.equalsIgnoreCase("call")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_call;
            } else {
                resLogoId = R.drawable.duoqu_call_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_call_phone);
            return resLogoId;
        } else if (action.equalsIgnoreCase("reply_sms_open")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_reply;
            } else {
                resLogoId = R.drawable.duoqu_reply_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_open_text);
            return resLogoId;
        } else if (action.equalsIgnoreCase("down_url")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_download;
            } else {
                resLogoId = R.drawable.duoqu_download_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_open_net);
            return resLogoId;
        } else if (action.equalsIgnoreCase("send_email")) {
            if (!setText) {
                return R.drawable.duoqu_email;
            }
            buttonText.setText(R.string.duoqu_send_email);
            return R.drawable.duoqu_email;
        } else if (action.equalsIgnoreCase("map_site") || action.equalsIgnoreCase("open_map_list")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_map;
            } else {
                resLogoId = R.drawable.duoqu_map_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_open_map);
            return resLogoId;
        } else if (action.equalsIgnoreCase("chong_zhi") || action.equalsIgnoreCase("recharge")) {
            if (isClickAble) {
                resLogoId = R.drawable.duoqu_chongzhi;
            } else {
                resLogoId = R.drawable.duoqu_chongzhi_disable;
            }
            if (!setText) {
                return resLogoId;
            }
            buttonText.setText(R.string.duoqu_chonzhi);
            return resLogoId;
        } else if (action.equalsIgnoreCase("WEB_TRAFFIC_ORDER") || action.equalsIgnoreCase("WEB_INSTALMENT_PLAN") || action.equalsIgnoreCase("zfb_repayment") || action.equalsIgnoreCase("repayment") || action.equalsIgnoreCase("pay_water_gas")) {
            if (isClickAble) {
                return R.drawable.duoqu_chongzhi;
            }
            return R.drawable.duoqu_chongzhi_disable;
        } else if (action.equalsIgnoreCase("copy_code")) {
            if (isClickAble) {
                return R.drawable.duoqu_copy_code;
            }
            return R.drawable.duoqu_copy_code_disable;
        } else if (action.equalsIgnoreCase("sdk_time_remind") || action.equalsIgnoreCase("time_remind")) {
            if (isClickAble) {
                return R.drawable.duoqu_time_remind;
            }
            return R.drawable.duoqu_time_remind_disable;
        } else if (action.equalsIgnoreCase("open_map")) {
            if (isClickAble) {
                return R.drawable.duoqu_map;
            }
            return R.drawable.duoqu_map_disable;
        } else if (isClickAble) {
            return R.drawable.duoqu_chakan;
        } else {
            return R.drawable.duoqu_chakan_disable;
        }
    }

    public static int getTop() {
        return mTop;
    }

    public static int getRight() {
        if (mRight == 0) {
            mRight = DRAWABLE_BOUNDS_RIGHT;
        }
        return mRight;
    }

    public static int getBottom() {
        if (mBottom == 0) {
            mBottom = getRight();
        }
        return mBottom;
    }

    public static void setBotton(final Activity mContext, View button, TextView buttonText, final JSONObject actionMap, boolean disLogo, final HashMap<String, Object> extend) {
        if (actionMap != null) {
            String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
            setButtonValue(mContext, buttonText, actionMap, disLogo, action, true);
            if (!StringUtils.isNull(action)) {
                button.setTag(actionMap);
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        SimpleButtonUtil.doAction(mContext, actionMap, extend);
                    }
                });
            }
        }
    }

    public static void setBottonValue(Activity mContext, TextView buttonText, JSONObject actionMap, boolean disLogo, boolean isClickAble) {
        if (actionMap != null) {
            setButtonValue(mContext, buttonText, actionMap, disLogo, (String) JsonUtil.getValueFromJsonObject(actionMap, "action"), isClickAble);
        }
    }

    private static void setButtonValue(Activity mContext, TextView buttonText, JSONObject actionMap, boolean disLogo, String action, boolean isClickAble) {
        String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
        if (!StringUtils.isNull(btnName)) {
            if (isClickAble) {
                buttonText.setTextColor(buttonText.getResources().getColorStateList(R.color.duoqu_button_text_color));
            } else {
                buttonText.setTextColor(buttonText.getResources().getColor(R.color.duoqu_huawei_text_disable));
            }
            buttonText.setText(btnName);
            setButtonTextAndImg(mContext, buttonText, action, disLogo, isClickAble);
        }
    }

    public static void doAction(Activity mContext, JSONObject actionMap, HashMap<String, Object> extend) {
        try {
            HashMap<String, String> valueMap = new HashMap();
            if (!(extend == null || extend.isEmpty())) {
                for (Entry<String, Object> entry : extend.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        valueMap.put((String) entry.getKey(), (String) entry.getValue());
                    }
                }
            }
            JsonUtil.putJsonToMap(actionMap, valueMap);
            putHWParseTimeToExtend(valueMap, actionMap);
            DuoquUtils.doAction(mContext, (String) JsonUtil.getValueFromJsonObject(actionMap, "action_data"), valueMap);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    public static void putHWParseTimeToExtend(HashMap<String, String> extend, JSONObject simpleBubbleData) {
        if (extend != null && simpleBubbleData != null) {
            String hwParseTime = simpleBubbleData.optString(Constant.KEY_HW_PARSE_TIME);
            if (!StringUtils.isNull(hwParseTime)) {
                extend.put(Constant.KEY_HW_PARSE_TIME, hwParseTime);
            }
        }
    }
}
