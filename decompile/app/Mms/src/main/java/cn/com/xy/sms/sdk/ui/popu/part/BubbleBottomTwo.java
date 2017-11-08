package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.BottomButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SimpleButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class BubbleBottomTwo extends UIPart implements OnClickListener {
    private LinearLayout mBtn1Layout = null;
    private LinearLayout mBtn2Layout = null;
    private boolean mDisLogo = false;
    private View mDuoqu_bottom_split_line;
    private View mDuoqu_btn_split_line;
    private int[] mHarr = new int[2];
    private TextView mTextView1 = null;
    private TextView mTextView2 = null;
    HashMap<String, String> mValueMap = null;

    public BubbleBottomTwo(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        this.mHarr[0] = Math.round(this.mContext.getResources().getDimension(R.dimen.bubble_bottom_two_height));
        this.mHarr[1] = Math.round(this.mContext.getResources().getDimension(R.dimen.bubble_bottom_two_minheight));
        this.mDuoqu_bottom_split_line = this.mView.findViewById(R.id.duoqu_bottom_split_line);
        this.mDuoqu_btn_split_line = this.mView.findViewById(R.id.duoqu_btn_split_line);
        this.mTextView1 = (TextView) this.mView.findViewById(R.id.duoqu_btn_text_1);
        this.mTextView2 = (TextView) this.mView.findViewById(R.id.duoqu_btn_text_2);
        this.mBtn1Layout = (LinearLayout) this.mView.findViewById(R.id.duoqu_ll_button_1);
        this.mBtn2Layout = (LinearLayout) this.mView.findViewById(R.id.duoqu_ll_button_2);
        this.mBtn1Layout.setOnClickListener(this);
        this.mBtn2Layout.setOnClickListener(this);
        this.mTextView1.setTextSize(0, (float) ContentUtil.getBottomButtonTextSize());
        this.mTextView2.setTextSize(0, (float) ContentUtil.getBottomButtonTextSize());
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        setButton(this.mMessage, this.mBasePopupView.groupValue);
    }

    private void setButton(BusinessSmsMessage message, String groupValue) {
        Object obj = message.getExtendParamValue("isClickAble");
        if (obj == null || Boolean.valueOf(obj.toString()).booleanValue()) {
            setButtonClickAble(true, groupValue);
        } else {
            setButtonClickAble(false, groupValue);
        }
    }

    private void setButtonViewVisibility(int button1Visible, int button2Visible, int splitLineVisible) {
        ContentUtil.setViewVisibility(this.mDuoqu_btn_split_line, splitLineVisible);
        ContentUtil.setViewVisibility(this.mBtn1Layout, button1Visible);
        ContentUtil.setViewVisibility(this.mTextView1, button1Visible);
        ContentUtil.setViewVisibility(this.mBtn2Layout, button2Visible);
        ContentUtil.setViewVisibility(this.mTextView2, button2Visible);
        setLayoutParams();
    }

    public void setLayoutParams() {
        if (this.mView != null) {
            LayoutParams lp = this.mView.getLayoutParams();
            if (this.mTextView1.getVisibility() == 0) {
                if (lp != null) {
                    lp.height = this.mHarr[0];
                    this.mView.setLayoutParams(lp);
                }
            } else if (lp != null) {
                lp.height = this.mHarr[1];
                this.mView.setLayoutParams(lp);
            }
        }
    }

    public void destroy() {
        ViewUtil.recycleViewBg(this.mDuoqu_bottom_split_line);
        ViewUtil.recycleViewBg(this.mView);
        super.destroy();
    }

    public void setButtonClickAble(boolean isClickAble, String groupValue) {
        try {
            JSONArray actionArr = BottomButtonUtil.getAdAction(this.mMessage, groupValue, -1, this.mMessage.extendParamMap);
            if (actionArr == null || actionArr.length() < 1) {
                this.mMessage.putValue("hasButtonData", Boolean.valueOf(false));
                setButtonViewVisibility(8, 8, 8);
                return;
            }
            if (actionArr.length() == 1) {
                this.mMessage.putValue("hasButtonData", Boolean.valueOf(true));
                this.mBtn1Layout.setClickable(isClickAble);
                setButtonViewVisibility(0, 8, 8);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView1, actionArr.getJSONObject(0), this.mDisLogo, isClickAble);
            } else {
                this.mMessage.putValue("hasButtonData", Boolean.valueOf(true));
                this.mBtn1Layout.setClickable(isClickAble);
                this.mBtn2Layout.setClickable(isClickAble);
                setButtonViewVisibility(0, 0, 4);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView1, actionArr.getJSONObject(0), this.mDisLogo, isClickAble);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView2, actionArr.getJSONObject(1), this.mDisLogo, isClickAble);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    public void onClick(View view) {
        try {
            JSONArray actionArr = BottomButtonUtil.getAdAction(this.mMessage, this.mBasePopupView.groupValue, -1, this.mMessage.extendParamMap);
            if (view == this.mBtn1Layout && actionArr != null && actionArr.length() > 0) {
                doAction(actionArr.getJSONObject(0));
            } else if (view == this.mBtn2Layout && actionArr != null && actionArr.length() > 1) {
                doAction(actionArr.getJSONObject(1));
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    private void doAction(JSONObject jsonObject) {
        if (this.mValueMap == null) {
            this.mValueMap = new HashMap();
        } else {
            this.mValueMap.clear();
        }
        this.mValueMap.put("msgKey", String.valueOf(this.mMessage.smsId) + String.valueOf(this.mMessage.msgTime));
        this.mValueMap.put("simIndex", String.valueOf(this.mMessage.simIndex));
        this.mValueMap.put("phoneNum", this.mMessage.originatingAddress);
        this.mValueMap.put("content", this.mMessage.getMessageBody());
        this.mValueMap.put("viewType", String.valueOf(this.mMessage.viewType));
        JsonUtil.putJsonToMap(jsonObject, this.mValueMap);
        String action_data = (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data");
        String type = jsonObject.optString("action", "");
        if (!StringUtils.isNull(type)) {
            HashMap<String, Object> param = new HashMap();
            if ("WEB_QUERY_EXPRESS_FLOW".equals(type)) {
                param.put(NumberInfo.TYPE_KEY, Integer.valueOf(0));
                param.put("catagory", "express");
            } else if ("WEB_QUERY_FLIGHT_TREND".equals(type)) {
                param.put(NumberInfo.TYPE_KEY, Integer.valueOf(3));
                param.put("isFlightState", Boolean.valueOf(true));
            } else {
                param.put(NumberInfo.TYPE_KEY, Integer.valueOf(0));
            }
            JSONObject jSONObject = null;
            try {
                JSONObject simpleObj = new JSONObject(this.mMessage.bubbleJsonObj.toString());
                try {
                    simpleObj.remove("NEW_ADACTION");
                    simpleObj.remove("ADACTION");
                    simpleObj.remove("viewPartParam");
                    jSONObject = simpleObj;
                } catch (Throwable th) {
                    jSONObject = simpleObj;
                }
            } catch (Throwable th2) {
            }
            param.put("adjust_data", this.mValueMap);
            this.mBasePopupView.changeData(param);
            if (jSONObject != null) {
                this.mValueMap.put("bubbleJson", jSONObject.toString());
            }
            SimpleButtonUtil.putHWParseTimeToExtend(this.mValueMap, jsonObject);
            DuoquUtils.doAction(this.mContext, action_data, this.mValueMap);
        }
    }

    public void changeData(Map<String, Object> map) {
        setButton(this.mMessage, this.mBasePopupView.groupValue);
    }
}
