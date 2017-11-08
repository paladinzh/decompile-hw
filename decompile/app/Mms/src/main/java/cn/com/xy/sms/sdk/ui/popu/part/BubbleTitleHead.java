package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.BroadcastReceiverUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.FlightDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ReceiverInterface;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.util.XyBroadcastReceiver;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BubbleTitleHead extends UIPart implements ReceiverInterface {
    public static final String BROADCAST_ACTION = "cn.com.xy.sms.FlightStateQueryReceiver";
    public static final String BROADCAST_PERMISSION_GET_FLIGHT_STATUS = "xy.permmisons.smartsms.GET_FLIGHT_STATUS";
    private static final String mAirDateKey = "flight_data_arr";
    private static final String mAirListKey = "view_flight_number";
    private static final BroadcastReceiverUtils mBroadcastReceiverUtils = new BroadcastReceiverUtils();
    private static final String mTrainDateKey = "card_arr";
    private static final String mTrainListKey = "view_m_trainnumber";
    private int currentListIndex = 0;
    private String groupValue = null;
    private String listKey = null;
    private JSONArray mDataArray = null;
    public FlightDataUtil mFlightDataUtil = FlightDataUtil.getInstance();
    private String mMsgKey = null;
    private XyBroadcastReceiver mRecerver = null;
    private TextView mStationSelect = null;
    private ImageView mStationSelectImage = null;
    private LinearLayout mStationSelectLinear = null;
    public OnClickListener mStationSelectListener = null;
    private TextView mStatus = null;
    private View mSubTitleArea = null;
    private TextView mSubTitle_1 = null;
    private TextView mSubTitle_2 = null;
    private TextView mTitleTextView = null;
    private PopupWindow pop = null;
    private View spaceView = null;
    private List<String> stationList = new ArrayList();
    private int stationListSize = 0;

    public BubbleTitleHead(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mTitleTextView = (TextView) this.mView.findViewById(R.id.duoqu_head_main_title);
        this.mStationSelect = (TextView) this.mView.findViewById(R.id.duoqu_head_list_select);
        this.mStatus = (TextView) this.mView.findViewById(R.id.duoqu_head_state);
        this.mStationSelectImage = (ImageView) this.mView.findViewById(R.id.duoqu_head_list_select_iction);
        this.mStationSelectLinear = (LinearLayout) this.mView.findViewById(R.id.duoqu_head_list_select_linear);
        this.mSubTitle_1 = (TextView) this.mView.findViewById(R.id.duoqu_head_sub_title_1);
        this.mSubTitle_2 = (TextView) this.mView.findViewById(R.id.duoqu_head_sub_title_2);
        this.spaceView = this.mView.findViewById(R.id.duoqu_head_sub_title_2_space);
        this.mSubTitleArea = this.mView.findViewById(R.id.duoqu_head_list_sec_title_linear);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (ContentUtil.bubbleDataIsNull(this.mMessage)) {
            this.mMsgKey = null;
            return;
        }
        this.mMsgKey = String.valueOf(this.mMessage.smsId) + String.valueOf(this.mMessage.msgTime);
        clearAll();
        String ob = this.mMessage.getValue("m_special_layout");
        if (ob != null) {
            String sp = ob;
            if ("1".equals(sp)) {
                this.listKey = mAirListKey;
                Object obAir = this.mMessage.getValue(mAirDateKey);
                if (obAir != null) {
                    this.mDataArray = (JSONArray) obAir;
                }
                mBroadcastReceiverUtils.reRegisterReceiver(this.mContext, this.mMsgKey, this, this.mRecerver, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_FLIGHT_STATUS");
            } else if ("2".equals(sp)) {
                this.listKey = mTrainListKey;
                Object obTrain = this.mMessage.getValue(mTrainDateKey);
                if (obTrain != null) {
                    this.mDataArray = (JSONArray) obTrain;
                }
            }
        }
        if (!isRebind) {
            setViewStyle();
        }
        setAllContent();
    }

    public BroadcastReceiver getReceiver() {
        if (this.mRecerver == null) {
            this.mRecerver = new XyBroadcastReceiver(this);
        } else {
            this.mRecerver.setReceiver(this);
        }
        return this.mRecerver;
    }

    private int getDataIndex() {
        int dataIndex = 0;
        try {
            Object index = this.mMessage.getValue("currentListIndex");
            if (index != null) {
                dataIndex = ((Integer) index).intValue();
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTitleHead getDataIndex error:" + e.getMessage(), e);
        }
        return dataIndex;
    }

    @SuppressLint({"ResourceAsColor"})
    private void setViewStyle() {
        ThemeUtil.setTextColor(this.mContext, this.mTitleTextView, (String) this.mMessage.getValue("v_hd_text_1"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mStationSelect, (String) this.mMessage.getValue("v_hd_text_1"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mSubTitle_1, (String) this.mMessage.getValue("v_hd_text_1"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mSubTitle_2, (String) this.mMessage.getValue("v_hd_text_2"), R.color.duoqu_theme_color_3010);
        this.mTitleTextView.setTextSize(0, (float) ContentUtil.getHeadTitleTextSize());
        this.mSubTitle_1.setTextSize(0, (float) ContentUtil.getHeadSubtitleTextSize());
        this.mSubTitle_2.setTextSize(0, (float) ContentUtil.getHeadSubtitleTextSize());
        this.mStatus.setTextSize(0, (float) ContentUtil.getFlightStatusTextSize());
        this.mStationSelect.setTextSize(0, (float) ContentUtil.getHeadStationSelectTextSize());
    }

    @SuppressLint({"ResourceAsColor"})
    private void setAllContent() {
        try {
            this.mStatus.setVisibility(8);
            ContentUtil.setText(this.mTitleTextView, (String) this.mMessage.getValue("view_title_name"), null);
            if (StringUtils.isNull(this.listKey) || this.mDataArray == null) {
                this.mStationSelectLinear.setVisibility(8);
                this.mStationSelectImage.setBackground(null);
                this.mStationSelectImage.setVisibility(8);
                this.mStationSelectLinear.setOnClickListener(null);
                this.mStationSelectLinear.setClickable(false);
            } else {
                this.currentListIndex = getDataIndex();
                setStationSelect();
                setStateTextView();
            }
            setTextAndShow("m_hd_text_2", this.mSubTitle_1);
            setTextAndShow("m_hd_text_3", this.mSubTitle_2);
            int subTitleViewState = this.mSubTitle_1.getVisibility();
            int secTitleViewState = this.mSubTitle_2.getVisibility();
            if (subTitleViewState == 8) {
                this.spaceView.setVisibility(8);
            }
            if (secTitleViewState == 8) {
                this.spaceView.setVisibility(8);
            } else {
                this.spaceView.setVisibility(0);
            }
            if (subTitleViewState == 8 && secTitleViewState == 8) {
                this.mSubTitleArea.setVisibility(8);
            } else {
                this.mSubTitleArea.setVisibility(0);
            }
        } catch (Exception e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTitleHead setAllContent error:" + e.getMessage(), e);
        }
    }

    @SuppressLint({"ResourceAsColor"})
    private void setStateTextView() {
        if (mAirListKey.equals(this.listKey)) {
            Object hasQuery = this.mMessage.getValue("hasQuery" + this.currentListIndex);
            if (hasQuery == null || !((Boolean) hasQuery).booleanValue()) {
                this.mStatus.setVisibility(8);
                return;
            }
            this.mMessage.putValue("db_air_data_index", Integer.valueOf(this.currentListIndex));
            String mState = FlightDataUtil.getInstance().getInterfaceFlightState(this.mMessage);
            if (StringUtils.isNull(mState)) {
                this.mStatus.setVisibility(8);
                return;
            }
            ContentUtil.setText(this.mStatus, mState, "");
            ThemeUtil.setTextColor(this.mContext, this.mStatus, ContentUtil.getFightStateColor(mState), R.color.duoqu_theme_color_3010);
            this.mStatus.setVisibility(0);
            return;
        }
        this.mStatus.setVisibility(8);
    }

    private void clearAll() {
        this.currentListIndex = 0;
        this.mDataArray = null;
        this.pop = null;
        this.stationList.clear();
        this.stationListSize = 0;
        this.mStationSelectListener = null;
        this.listKey = null;
        this.groupValue = null;
    }

    private void setStationSelect() {
        JSONObject currentListData = this.mDataArray.optJSONObject(this.currentListIndex);
        this.groupValue = currentListData.optString(this.listKey);
        if (this.groupValue.contains(ContentUtil.TRAIN_SUPPLEMENT_NUMBER)) {
            this.groupValue = this.groupValue.replace(ContentUtil.TRAIN_SUPPLEMENT_NUMBER, "");
        }
        this.mBasePopupView.groupValue = this.groupValue;
        this.mMessage.putValue("deadline", Long.valueOf(currentListData.optLong("deadline")));
        ContentUtil.setText(this.mStationSelect, this.groupValue, "");
        if (this.mDataArray.length() > 1) {
            this.mStationSelectLinear.setVisibility(0);
            ThemeUtil.setViewBg(this.mContext, this.mStationSelectImage, "", R.drawable.duoqu_train_more);
            this.mStationSelectImage.setVisibility(0);
            setStationList(this.listKey);
            getStationSelectListener();
            this.mStationSelectLinear.setOnClickListener(this.mStationSelectListener);
            this.mStationSelectLinear.setClickable(true);
        } else if (this.mDataArray.length() == 1) {
            this.mStationSelectLinear.setVisibility(0);
            this.mStationSelectImage.setBackground(null);
            this.mStationSelectImage.setVisibility(8);
            this.mStationSelectLinear.setOnClickListener(null);
            this.mStationSelectLinear.setClickable(false);
        }
    }

    private void setTextAndShow(String key, TextView view) {
        String airStatus = (String) this.mMessage.getValue(key);
        if (StringUtils.isNull(airStatus)) {
            view.setVisibility(8);
            return;
        }
        ContentUtil.setText(view, airStatus, null);
        view.setVisibility(0);
    }

    private void setStationList(String key) {
        try {
            int length = this.mDataArray.length();
            for (int i = 0; i < length; i++) {
                String va = ((JSONObject) this.mDataArray.get(i)).optString(key);
                if (!StringUtils.isNull(va)) {
                    if (va.contains(ContentUtil.TRAIN_SUPPLEMENT_NUMBER)) {
                        this.stationList.add(va.replace(ContentUtil.TRAIN_SUPPLEMENT_NUMBER, ""));
                    } else {
                        this.stationList.add(va);
                    }
                }
            }
            this.stationListSize = this.stationList.size();
        } catch (JSONException e) {
            SmartSmsSdkUtil.smartSdkExceptionLog(e.getMessage(), e);
        }
    }

    private void getStationSelectListener() {
        this.mStationSelectListener = new OnClickListener() {
            LinearLayout ly = null;

            public void onClick(View arg0) {
                int[] location = new int[2];
                BubbleTitleHead.this.mStationSelectImage.getLocationOnScreen(location);
                if (BubbleTitleHead.this.pop == null) {
                    this.ly = new LinearLayout(BubbleTitleHead.this.mContext);
                    this.ly.setOrientation(1);
                    this.ly.setPadding(ViewUtil.dp2px(BubbleTitleHead.this.mContext, 17), ViewUtil.dp2px(BubbleTitleHead.this.mContext, 5), ViewUtil.dp2px(BubbleTitleHead.this.mContext, 17), ViewUtil.dp2px(BubbleTitleHead.this.mContext, 9));
                    LayoutParams lyparam = new LayoutParams(ViewUtil.dp2px(BubbleTitleHead.this.mContext, 132), ViewUtil.dp2px(BubbleTitleHead.this.mContext, 30));
                    LayoutParams lyparamview = new LayoutParams(ViewUtil.dp2px(BubbleTitleHead.this.mContext, 132), ViewUtil.dp2px(BubbleTitleHead.this.mContext, 1));
                    for (int i = 0; i < BubbleTitleHead.this.stationListSize; i++) {
                        TextView tv = new TextView(BubbleTitleHead.this.mContext);
                        tv.setText((CharSequence) BubbleTitleHead.this.stationList.get(i));
                        tv.setTextColor(ThemeUtil.getColorInteger(BubbleTitleHead.this.mContext, "1000"));
                        tv.setTextSize(1, 14.0f);
                        tv.setGravity(8388627);
                        tv.setLayoutParams(lyparam);
                        tv.setId(i);
                        this.ly.addView(tv);
                        if (i < BubbleTitleHead.this.stationListSize - 1) {
                            View v = new View(BubbleTitleHead.this.mContext);
                            v.setLayoutParams(lyparamview);
                            v.setBackgroundColor(ThemeUtil.getColorInteger(BubbleTitleHead.this.mContext, "5021"));
                            this.ly.addView(v);
                        }
                        tv.setOnClickListener(BubbleTitleHead.this.getOnClick());
                    }
                    BubbleTitleHead.this.pop = new PopupWindow(this.ly, ViewUtil.dp2px(BubbleTitleHead.this.mContext, 146), -2, false);
                    BubbleTitleHead.this.pop.setBackgroundDrawable(BubbleTitleHead.this.mContext.getResources().getDrawable(R.drawable.duoqu_pop_back));
                    BubbleTitleHead.this.pop.setOutsideTouchable(true);
                }
                BubbleTitleHead.this.pop.showAtLocation(BubbleTitleHead.this.mStationSelectImage, 0, (location[0] - ViewUtil.dp2px(BubbleTitleHead.this.mContext, 73)) + (BubbleTitleHead.this.mStationSelectImage.getWidth() / 2), location[1] + BubbleTitleHead.this.mStationSelectImage.getHeight());
            }
        };
    }

    private OnClickListener getOnClick() {
        return new OnClickListener() {
            public void onClick(View view) {
                int position = view.getId();
                try {
                    Long deadline = Long.valueOf(((JSONObject) BubbleTitleHead.this.mDataArray.get(position)).optLong("deadline"));
                    HashMap<String, Object> param = new HashMap();
                    BubbleTitleHead.this.mMessage.putValue("currentListIndex", Integer.valueOf(position));
                    BubbleTitleHead.this.mMessage.putValue("deadline", deadline);
                    BubbleTitleHead.this.groupValue = (String) BubbleTitleHead.this.stationList.get(position);
                    BubbleTitleHead.this.mStationSelect.setText(BubbleTitleHead.this.groupValue);
                    param.put("currentListIndex", Integer.valueOf(position));
                    param.put(NumberInfo.TYPE_KEY, Integer.valueOf(1));
                    param.put("deadline", deadline);
                    if (BubbleTitleHead.this.pop != null) {
                        BubbleTitleHead.this.pop.dismiss();
                    }
                    if (BubbleTitleHead.this.mBasePopupView != null) {
                        BubbleTitleHead.this.mBasePopupView.groupValue = BubbleTitleHead.this.groupValue;
                        BubbleTitleHead.this.mBasePopupView.changeData(param);
                    }
                } catch (Exception e) {
                    LogManager.e("XIAOYUAN", "BubbleTitleHead: setWebJson : " + e.getMessage(), e);
                }
            }
        };
    }

    public void changeData(Map<String, Object> param) {
        try {
            Object isFlightState = param.get("isFlightState");
            if (isFlightState == null || !((Boolean) isFlightState).booleanValue()) {
                Object ob = param.get("currentListIndex");
                if (ob != null) {
                    this.currentListIndex = ((Integer) ob).intValue();
                }
                setStateTextView();
                return;
            }
            mBroadcastReceiverUtils.register(this.mContext, this.mMsgKey, this, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_FLIGHT_STATUS");
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "BubbleTitleHead: changeData : " + e.getMessage(), e);
        }
    }

    public boolean onReceive(Intent intent) {
        try {
            if (!BROADCAST_ACTION.equals(intent.getAction())) {
                return false;
            }
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
            String result = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(result) || this.mMessage == null || this.mMessage.bubbleJsonObj == null) {
                return false;
            }
            JSONObject jsonObject = new JSONObject(result);
            String msgKey = jsonObject.optString("msgKey");
            if (StringUtils.isNull(msgKey) || !msgKey.equals(this.mMsgKey)) {
                return false;
            }
            String statusString = jsonObject.optString("view_flight_latest_status");
            if (StringUtils.isNull(statusString)) {
                return false;
            }
            this.mFlightDataUtil.savaArriveInfo(this.mMessage, new JSONObject(statusString));
            HashMap<String, Object> param = new HashMap();
            param.put(NumberInfo.TYPE_KEY, Integer.valueOf(4));
            if (this.mBasePopupView != null) {
                this.mBasePopupView.changeData(param);
            }
            return true;
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "BubbleTitleHead: onReciver : " + e.getMessage(), e);
            return false;
        }
    }

    public void destroy() {
        try {
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleTitleHead destroy error:" + e.getMessage(), e);
        }
        super.destroy();
    }
}
