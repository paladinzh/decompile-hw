package cn.com.xy.sms.sdk.ui.popu.simplepart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.part.BubbleTrainBody;
import cn.com.xy.sms.sdk.ui.popu.util.BroadcastReceiverUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ReceiverInterface;
import cn.com.xy.sms.sdk.ui.popu.util.SimpleButtonUtil;
import cn.com.xy.sms.sdk.ui.popu.util.TrainDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.XyBroadcastReceiver;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class SimpleBubbleBottom extends RelativeLayout implements OnClickListener, ReceiverInterface {
    public static final String TAG = "SimpleBubbleBottom";
    private static final BroadcastReceiverUtils mBroadcastReceiverUtils = new BroadcastReceiverUtils();
    public LinearLayout mBtn1Layout = null;
    public LinearLayout mBtn2Layout = null;
    private JSONObject mBubbleData = null;
    public Activity mContext;
    public boolean mDisLogo = false;
    public HashMap<String, Object> mExtend;
    public JSONArray mJsonArray;
    private String mMsgKey = null;
    private XyBroadcastReceiver mRecerver = null;
    public TextView mTextView1 = null;
    public TextView mTextView2 = null;

    public SimpleBubbleBottom(Activity mContext, JSONArray jsonArray, HashMap<String, Object> extend) throws Exception {
        super(mContext);
        this.mExtend = extend;
        this.mContext = mContext;
        inflate(mContext, R.layout.duoqu_simple_bubble_bottom_two, this);
        initViews();
        setContent(jsonArray, extend);
    }

    private void initViews() {
        this.mTextView1 = (TextView) findViewById(R.id.duoqu_btn_text_1);
        this.mTextView2 = (TextView) findViewById(R.id.duoqu_btn_text_2);
        this.mBtn1Layout = (LinearLayout) findViewById(R.id.duoqu_ll_button_1);
        this.mBtn2Layout = (LinearLayout) findViewById(R.id.duoqu_ll_button_2);
        this.mBtn1Layout.setOnClickListener(this);
        this.mBtn2Layout.setOnClickListener(this);
        this.mTextView1.setTextSize(0, (float) ContentUtil.getBottomButtonTextSize());
        this.mTextView2.setTextSize(0, (float) ContentUtil.getBottomButtonTextSize());
    }

    public void setContent(JSONArray jsonArray, HashMap<String, Object> extend) {
        try {
            this.mExtend = extend;
            this.mJsonArray = jsonArray;
            if (this.mJsonArray == null || this.mJsonArray.length() == 0) {
                setVisibility(8);
                this.mMsgKey = null;
                this.mBubbleData = null;
                return;
            }
            if (extend != null) {
                this.mMsgKey = String.valueOf(extend.get("smsId")) + String.valueOf(extend.get("msgTime"));
                this.mBubbleData = (JSONObject) extend.get("bubbleData");
            }
            Object obj = null;
            if (this.mExtend != null) {
                obj = this.mExtend.get("isClickAble");
            }
            if (obj == null || Boolean.valueOf(obj.toString()).booleanValue()) {
                setButtonClickAble(true);
            } else {
                setButtonClickAble(false);
            }
            mBroadcastReceiverUtils.reRegisterReceiver(this.mContext, this.mMsgKey, this, this.mRecerver, BubbleTrainBody.BROADCAST_ACTION, "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setButtonClickAble(boolean isClickAble) {
        try {
            if (this.mJsonArray == null || this.mJsonArray.length() < 1) {
                setButtonViewVisibility(8, 8, 8);
                return;
            }
            if (this.mJsonArray.length() == 1) {
                this.mBtn1Layout.setClickable(isClickAble);
                setButtonViewVisibility(0, 8, 8);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView1, this.mJsonArray.getJSONObject(0), this.mDisLogo, isClickAble);
            } else {
                this.mBtn1Layout.setClickable(isClickAble);
                this.mBtn2Layout.setClickable(isClickAble);
                setButtonViewVisibility(0, 0, 4);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView1, this.mJsonArray.getJSONObject(0), this.mDisLogo, isClickAble);
                SimpleButtonUtil.setBottonValue(this.mContext, this.mTextView2, this.mJsonArray.getJSONObject(1), this.mDisLogo, isClickAble);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SimpleBubbleBottom setButtonClickAble error:" + e.getMessage(), e);
        }
    }

    public void onClick(View view) {
        try {
            if (this.mJsonArray != null && this.mJsonArray.length() != 0) {
                if (view == this.mBtn1Layout) {
                    doAction(0);
                } else if (view == this.mBtn2Layout) {
                    doAction(1);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SimpleBubbleBottom onClick error:" + e.getMessage(), e);
        }
    }

    private void doAction(int dataIndex) {
        if (this.mJsonArray.length() > dataIndex) {
            JSONObject actionData = this.mJsonArray.optJSONObject(dataIndex);
            if (isTrainTimetableAction(actionData)) {
                registerReceiver();
            }
            SimpleButtonUtil.doAction(this.mContext, actionData, this.mExtend);
        }
    }

    private boolean isTrainTimetableAction(JSONObject actionData) {
        if (actionData == null || actionData.length() == 0) {
            return false;
        }
        return actionData.optString("action").equals(ContentUtil.WEB_TRAIN_STATION_NEW);
    }

    private void registerReceiver() {
        try {
            String checkString = getCheckString();
            if (StringUtils.isNull(checkString)) {
                LogManager.e("XIAOYUAN", "SimpleBubbleBottom registerReceiver checkString is empty", null);
                return;
            }
            this.mExtend.put("checkString", checkString);
            SmartSmsSdkUtil.putCheckString(this.mMsgKey, checkString);
            mBroadcastReceiverUtils.register(this.mContext, this.mMsgKey, this, BubbleTrainBody.BROADCAST_ACTION, "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED");
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "SimpleBubbleBottom registerReceiver error:" + e.getMessage(), e);
        }
    }

    private String getCheckString() {
        try {
            if (this.mBubbleData == null) {
                return null;
            }
            JSONArray cardArr = this.mBubbleData.optJSONArray("card_arr");
            if (cardArr == null) {
                return null;
            }
            int dataIndex = this.mBubbleData.optInt("db_train_data_index", 0);
            JSONObject bubbleDate = cardArr.optJSONObject(dataIndex);
            if (bubbleDate == null) {
                return null;
            }
            StringBuilder sbrCheckString = new StringBuilder();
            sbrCheckString.append(bubbleDate.optString("view_m_trainnumber"));
            sbrCheckString.append(System.currentTimeMillis());
            sbrCheckString.append(String.valueOf(this.mExtend.get("smsId")));
            sbrCheckString.append(dataIndex);
            return sbrCheckString.toString();
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "SimpleBubbleBottom getCheckString error:" + e.getMessage(), e);
            return null;
        }
    }

    public boolean onReceive(Intent intent) {
        try {
            if (!BubbleTrainBody.BROADCAST_ACTION.equals(intent.getAction())) {
                return false;
            }
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
            String userSelectedResult = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(userSelectedResult) || this.mBubbleData == null) {
                return false;
            }
            JSONObject jSONObject = new JSONObject(userSelectedResult);
            String currentCheckString = jSONObject.optString("checkString");
            String localCheckString = SmartSmsSdkUtil.getCheckString(this.mMsgKey);
            if (currentCheckString == null || !currentCheckString.equals(localCheckString)) {
                return false;
            }
            String arriveCity = jSONObject.optString("view_arrive_city");
            String arriveTime = jSONObject.optString("view_arrive_time");
            String strArriveDate = jSONObject.optString("view_arrive_date");
            String formatArriveDate = null;
            if (!StringUtils.isNull(strArriveDate)) {
                formatArriveDate = new SimpleDateFormat(ContentUtil.TRAIN_DATE_FORMAT).format(new SimpleDateFormat(Constant.PATTERN).parse(strArriveDate));
            }
            if (StringUtils.isNull(arriveCity) || StringUtils.isNull(formatArriveDate) || StringUtils.isNull(arriveTime)) {
                return false;
            }
            int dataIndex = this.mBubbleData.optInt("db_train_data_index", 0);
            String displayArriveDate = ContentUtil.TRAIN_SUPPLEMENT_DATE + formatArriveDate;
            this.mBubbleData.put(TrainDataUtil.INTERFACE_TRAIN_ARRIVE_CITY_KEY_START + dataIndex, arriveCity);
            this.mBubbleData.put(TrainDataUtil.INTERFACE_TRAIN_ARRIVE_DATE_KEY_START + dataIndex, displayArriveDate);
            this.mBubbleData.put(TrainDataUtil.INTERFACE_TRAIN_ARRIVE_TIME_KEY_START + dataIndex, arriveTime);
            this.mBubbleData.put("hasSelect" + dataIndex, true);
            Date arriveDate = DexUtil.convertDate(formatArriveDate + arriveTime);
            if (arriveDate != null) {
                this.mBubbleData.put(TrainDataUtil.INTERFACE_TRAIN_ARRIVE_LONG_TIME_KEY_START + dataIndex, arriveDate.getTime());
            }
            ContentUtil.updateMatchCache(this.mBubbleData.optString("phoneNum"), this.mBubbleData.optString("title_num"), String.valueOf(this.mExtend.get("smsId")), this.mBubbleData, TrainDataUtil.putSelectedStationDataToSimpleBubble(this.mBubbleData), String.valueOf(this.mExtend.get("messageBody")));
            return true;
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "SimpleBubbleBottom onReciver error" + e.getMessage(), e);
            return false;
        }
    }

    public BroadcastReceiver getReceiver() {
        if (this.mRecerver == null) {
            this.mRecerver = new XyBroadcastReceiver(this);
        } else {
            this.mRecerver.setReceiver(this);
        }
        return this.mRecerver;
    }

    public void destroy() {
        try {
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SimpleBubbleBottom destroy error:" + e.getMessage(), e);
        }
    }

    private void setButtonViewVisibility(int button1Visible, int button2Visible, int splitLineVisible) {
        ContentUtil.setViewVisibility(this.mBtn1Layout, button1Visible);
        ContentUtil.setViewVisibility(this.mTextView1, button1Visible);
        ContentUtil.setViewVisibility(this.mBtn2Layout, button2Visible);
        ContentUtil.setViewVisibility(this.mTextView2, button2Visible);
    }
}
