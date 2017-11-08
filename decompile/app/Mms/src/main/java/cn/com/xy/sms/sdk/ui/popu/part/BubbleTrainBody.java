package cn.com.xy.sms.sdk.ui.popu.part;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.BroadcastReceiverUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ReceiverInterface;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.TrainDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.TravelDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.XyBroadcastReceiver;
import cn.com.xy.sms.sdk.ui.popu.widget.DuoquFieldShapeItem;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({"SimpleDateFormat"})
public class BubbleTrainBody extends UIPart implements ReceiverInterface {
    public static final String BROADCAST_ACTION = "cn.com.xy.sms.TrianStationSelectedReceiver";
    public static final String BROADCAST_PERMISSION_GET_TRIAN_STATION_SELECTED = "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED";
    public static final int MIN_CLICK_DELAY_TIME = 1000;
    private static final String TABLE_KEY = "duoqu_table_data_vert";
    private static final BroadcastReceiverUtils mBroadcastReceiverUtils = new BroadcastReceiverUtils();
    private String chectString;
    int currentListIndex = 0;
    private String departDateString = null;
    private int horizontal_data_size = 1;
    private long lastClickTime = 0;
    private TextView mArriveCity;
    private TextView mArriveDate;
    private TextView mArriveTime;
    private DuoquFieldShapeItem mContentListView;
    private TextView mDepartCity;
    private TextView mDepartDate;
    private TextView mDepartTime;
    private ImageView mLogo;
    private String mMsgKey = null;
    private XyBroadcastReceiver mRecerver = null;
    private TextView mSeatInfo;
    private TextView mSeatTitle;
    private ImageView mSelectTrainPlace;
    private int mTimeColor_hasTime = 0;
    private int mTimeColor_noTime = 0;
    private JSONObject mToWebActivityJsonObject = null;
    private TrainDataUtil mTrainDataUtil = TrainDataUtil.getInstance();
    private String supplementType_1 = "1";
    private String supplementType_2 = "2";
    private JSONObject train_data;

    public BubbleTrainBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        this.mLogo = (ImageView) this.mView.findViewById(R.id.duoqu_train_direction);
        this.mDepartCity = (TextView) this.mView.findViewById(R.id.duoqu_train_depart_city);
        this.mDepartTime = (TextView) this.mView.findViewById(R.id.duoqu_train_depart_time);
        this.mDepartDate = (TextView) this.mView.findViewById(R.id.duoqu_train_depart_date);
        this.mArriveCity = (TextView) this.mView.findViewById(R.id.duoqu_train_arrive_city);
        this.mArriveTime = (TextView) this.mView.findViewById(R.id.duoqu_train_arrive_time);
        this.mArriveDate = (TextView) this.mView.findViewById(R.id.duoqu_train_arrive_date);
        this.mSelectTrainPlace = (ImageView) this.mView.findViewById(R.id.duoqu_train_list_select_iction);
        this.mSeatTitle = (TextView) this.mView.findViewById(R.id.duoqu_train_sta_info_title);
        this.mSeatInfo = (TextView) this.mView.findViewById(R.id.duoqu_train_sta_info);
        this.mContentListView = (DuoquFieldShapeItem) this.mView.findViewById(R.id.duoqu_field_shape_list);
        setImageAndTextColor(this.mMessage);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (ContentUtil.bubbleDataIsNull(this.mMessage)) {
            this.mMsgKey = null;
            return;
        }
        this.mMsgKey = String.valueOf(this.mMessage.smsId) + String.valueOf(this.mMessage.msgTime);
        mBroadcastReceiverUtils.reRegisterReceiver(this.mContext, this.mMsgKey, this, this.mRecerver, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED");
        initData();
        setFixedTrainInfo(Boolean.valueOf(isRebind));
        bindTrainTickeInfo(this.currentListIndex);
    }

    public BroadcastReceiver getReceiver() {
        if (this.mRecerver == null) {
            this.mRecerver = new XyBroadcastReceiver(this);
        } else {
            this.mRecerver.setReceiver(this);
        }
        return this.mRecerver;
    }

    private void initData() {
        Object index = this.mMessage.getValue("currentListIndex");
        if (this.mTimeColor_hasTime == 0) {
            this.mTimeColor_hasTime = this.mContext.getResources().getColor(R.color.duoqu_theme_color_3010);
        }
        if (this.mTimeColor_noTime == 0) {
            this.mTimeColor_noTime = this.mContext.getResources().getColor(R.color.duoqu_theme_color_5010);
        }
        if (index != null) {
            this.currentListIndex = ((Integer) index).intValue();
        } else {
            this.currentListIndex = 0;
        }
        this.train_data = null;
        this.departDateString = null;
        this.mToWebActivityJsonObject = new JSONObject();
        this.chectString = null;
    }

    private void setFixedTrainInfo(Boolean isRebind) {
        if (this.mMessage.getTableDataSize(TABLE_KEY) > this.horizontal_data_size) {
            this.mContentListView.setContentList(this.mMessage, this.horizontal_data_size, TABLE_KEY, isRebind.booleanValue());
            return;
        }
        this.mContentListView.setVisibility(8);
    }

    @SuppressLint({"ResourceAsColor"})
    private void bindTrainTickeInfo(int currentTrainIndex) {
        this.mMessage.putValue("db_train_data_index", Integer.valueOf(currentTrainIndex));
        this.train_data = this.mTrainDataUtil.getViewContentData(this.mMessage);
        if (this.train_data != null) {
            String trainNumber = this.train_data.optString("view_m_trainnumber");
            String departCityString = this.train_data.optString("view_depart_city");
            this.departDateString = this.train_data.optString("view_depart_date");
            String departTimeString = this.train_data.optString("view_depart_time");
            String arriveCityString = this.train_data.optString("view_arrive_city");
            String arriveDateString = this.train_data.optString("view_arrive_date");
            String arriveTimeString = this.train_data.optString("view_arrive_time");
            setWebJson(currentTrainIndex, this.train_data, trainNumber, departCityString, this.departDateString, departTimeString, arriveCityString, arriveDateString, arriveTimeString);
            ContentUtil.setText(this.mDepartCity, departCityString, "");
            ContentUtil.setText(this.mDepartDate, this.departDateString, "");
            ContentUtil.setText(this.mDepartTime, departTimeString, ContentUtil.NO_DATA_DEP_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
            setArriveInfo(arriveCityString, arriveDateString, arriveTimeString);
            ContentUtil.setText(this.mSeatInfo, this.train_data.optString("view_seat_info_list"), ContentUtil.NO_DATA_EN);
            if (StringUtils.isNull(arriveCityString)) {
                this.mSelectTrainPlace.setVisibility(0);
                this.mArriveCity.setOnClickListener(getPopStationClickListener(this.train_data));
                this.mSelectTrainPlace.setOnClickListener(getPopStationClickListener(this.train_data));
                ThemeUtil.setTextColor(this.mContext, this.mArriveCity, (String) this.mMessage.getValue("v_by_text_4"), R.color.duoqu_theme_color_3010);
            } else {
                this.mSelectTrainPlace.setVisibility(8);
                this.mArriveCity.setOnClickListener(null);
                this.mSelectTrainPlace.setOnClickListener(null);
                ThemeUtil.setTextColor(this.mContext, this.mArriveCity, null, R.color.duoqu_theme_color_5010);
            }
            Object hasSelect = this.mMessage.getValue("hasSelect" + this.mTrainDataUtil.getDataIndex(this.mMessage));
            if (hasSelect != null && ((Boolean) hasSelect).booleanValue()) {
                trainSelectCompletion();
            }
            if (!StringUtils.isNull(trainNumber) && !StringUtils.isNull(this.departDateString) && !StringUtils.isNull(departCityString)) {
                Object hasQuery = this.mMessage.getValue("hasQuery" + this.mTrainDataUtil.getDataIndex(this.mMessage));
                Object supType = this.mMessage.getValue("supplementType" + this.mTrainDataUtil.getDataIndex(this.mMessage));
                if (hasQuery != null && ((Boolean) hasQuery).booleanValue() && supType != null) {
                    trainDataCompletion(this.mMessage, (String) supType);
                } else if (StringUtils.isNull(arriveCityString)) {
                    if (StringUtils.isNull(departTimeString)) {
                        queryTrainStation(this.supplementType_1);
                    }
                } else if (StringUtils.isNull(departTimeString) || StringUtils.isNull(arriveDateString) || StringUtils.isNull(arriveTimeString)) {
                    queryTrainStation(this.supplementType_2);
                }
            }
        }
    }

    private void trainSelectCompletion() {
        if (!TravelDataUtil.hasValue(this.mArriveCity, ContentUtil.TRAIN_SELECT_SITES)) {
            String arriveCity = this.mTrainDataUtil.getInterfaceTrainArriveCity(this.mMessage);
            if (this.mToWebActivityJsonObject != null) {
                try {
                    this.mToWebActivityJsonObject.put("view_arrive_city", arriveCity);
                } catch (Throwable th) {
                }
            }
            ContentUtil.setText(this.mArriveCity, arriveCity, ContentUtil.TRAIN_SELECT_SITES);
        }
        setTrainData();
    }

    private void setWebJson(int currentTrainIndex, JSONObject train_data, String trainNumber, String departCityString, String departDateString, String departTimeString, String arriveCityString, String arriveDateString, String arriveTimeString) {
        try {
            this.mToWebActivityJsonObject.put("checkString", this.chectString);
            this.mToWebActivityJsonObject.put(NumberInfo.TYPE_KEY, ContentUtil.WEB_TRAIN_STATION_NEW);
            this.mToWebActivityJsonObject.put("view_train_number", trainNumber);
            this.mToWebActivityJsonObject.put("view_depart_date_time", train_data.optLong("view_depart_date_time"));
            this.mToWebActivityJsonObject.put("view_arrive_city", arriveCityString);
            this.mToWebActivityJsonObject.put("view_arrive_date", arriveDateString);
            this.mToWebActivityJsonObject.put("view_arrive_time", arriveTimeString);
            this.mToWebActivityJsonObject.put("view_depart_city", departCityString);
            this.mToWebActivityJsonObject.put("view_depart_date", departDateString);
            this.mToWebActivityJsonObject.put("view_depart_time", departTimeString);
        } catch (JSONException e) {
            LogManager.e("XIAOYUAN", "BubbleTrainBody: setWebJson : " + e.getMessage(), e);
        }
    }

    private void trainDataCompletion(BusinessSmsMessage message, String supType) {
        if (!TravelDataUtil.hasValue(this.mDepartTime, ContentUtil.NO_DATA_DEP_TIME)) {
            ContentUtil.setText(this.mDepartTime, this.mTrainDataUtil.getInterfaceTrainDepartTime(this.mMessage), ContentUtil.NO_DATA_DEP_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
        }
        if (this.supplementType_2.equals(supType)) {
            setTrainData();
        }
    }

    private void setTrainData() {
        if (!TravelDataUtil.hasValue(this.mArriveDate, "")) {
            String arriveDate = this.mTrainDataUtil.getInterfaceTrainArriveDate(this.mMessage);
            if (this.mToWebActivityJsonObject != null) {
                try {
                    this.mToWebActivityJsonObject.put("view_arrive_date", arriveDate);
                } catch (Throwable th) {
                }
            }
            ContentUtil.setText(this.mArriveDate, arriveDate, "");
        }
        if (!TravelDataUtil.hasValue(this.mArriveTime, ContentUtil.NO_DATA_ARR_TIME)) {
            String arriveTime = this.mTrainDataUtil.getInterfaceTrainArriveTime(this.mMessage);
            if (this.mToWebActivityJsonObject != null) {
                try {
                    this.mToWebActivityJsonObject.put("view_arrive_time", arriveTime);
                } catch (Throwable th2) {
                }
            }
            ContentUtil.setText(this.mArriveTime, arriveTime, ContentUtil.NO_DATA_ARR_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
        }
    }

    private OnClickListener getPopStationClickListener(JSONObject jsonObject) {
        return new OnClickListener() {
            public void onClick(View v) {
                if (!BubbleTrainBody.this.isOverClick()) {
                    try {
                        BubbleTrainBody.this.chectString = BubbleTrainBody.this.getCheckString(BubbleTrainBody.this.mMessage, BubbleTrainBody.this.currentListIndex);
                        if (StringUtils.isNull(BubbleTrainBody.this.chectString)) {
                            LogManager.e("XIAOYUAN", "getPopStationClickListener chectString is empty", null);
                            return;
                        }
                        BubbleTrainBody.mBroadcastReceiverUtils.register(BubbleTrainBody.this.mContext, BubbleTrainBody.this.mMsgKey, BubbleTrainBody.this, BubbleTrainBody.BROADCAST_ACTION, "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED");
                        BubbleTrainBody.this.mToWebActivityJsonObject.put("checkString", BubbleTrainBody.this.chectString);
                        SmartSmsSdkUtil.putCheckString(BubbleTrainBody.this.mMsgKey, BubbleTrainBody.this.chectString);
                        PopupUtil.startWebActivity(BubbleTrainBody.this.mContext, BubbleTrainBody.this.mToWebActivityJsonObject, "", "");
                    } catch (Throwable e) {
                        LogManager.e("XIAOYUAN", "getPopStationClickListener : " + e.getMessage(), e);
                    }
                }
            }
        };
    }

    private boolean isOverClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastClickTime <= 1000) {
            return true;
        }
        this.lastClickTime = currentTime;
        return false;
    }

    public boolean onReceive(Intent intent) {
        try {
            if (!BROADCAST_ACTION.equals(intent.getAction())) {
                return false;
            }
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
            String userSelectedResult = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(userSelectedResult)) {
                return false;
            }
            if (this.mMessage == null || this.mMessage.bubbleJsonObj == null) {
                return false;
            }
            this.mToWebActivityJsonObject = new JSONObject(userSelectedResult);
            this.mToWebActivityJsonObject.remove("bubbleJson");
            String chect = this.mToWebActivityJsonObject.optString("checkString");
            String strTemp = SmartSmsSdkUtil.getCheckString(this.mMsgKey);
            if (!TextUtils.isEmpty(strTemp)) {
                this.chectString = strTemp;
            }
            if (chect == null || !chect.equals(this.chectString)) {
                return false;
            }
            String arriveCity = this.mToWebActivityJsonObject.optString("view_arrive_city");
            String arriveTime = this.mToWebActivityJsonObject.optString("view_arrive_time");
            String arriveDate = null;
            String strArriveDate = this.mToWebActivityJsonObject.optString("view_arrive_date");
            if (!StringUtils.isNull(strArriveDate)) {
                arriveDate = new SimpleDateFormat(ContentUtil.TRAIN_DATE_FORMAT).format(new SimpleDateFormat(Constant.PATTERN).parse(strArriveDate));
            }
            if (StringUtils.isNull(arriveCity) || StringUtils.isNull(arriveDate) || StringUtils.isNull(arriveTime)) {
                return false;
            }
            String displayArriveDate = ContentUtil.TRAIN_SUPPLEMENT_DATE + arriveDate;
            this.mMessage.bubbleJsonObj.put(this.mTrainDataUtil.getInterfaceTrainArriveCityKey(this.mMessage), arriveCity);
            this.mMessage.bubbleJsonObj.put(this.mTrainDataUtil.getInterfaceTrainArriveDateKey(this.mMessage), displayArriveDate);
            this.mMessage.bubbleJsonObj.put(this.mTrainDataUtil.getInterfaceTrainArriveTimeKey(this.mMessage), arriveTime);
            this.mMessage.putValue("hasSelect" + this.mTrainDataUtil.getDataIndex(this.mMessage), Boolean.valueOf(true));
            setArriveInfo(arriveCity, displayArriveDate, arriveTime);
            Date date = DexUtil.convertDate(arriveDate + arriveTime);
            if (date != null) {
                long arrayLongTime = date.getTime();
                this.mMessage.bubbleJsonObj.put(this.mTrainDataUtil.getInterfaceTrainArriveLongTimeKey(this.mMessage), arrayLongTime);
            }
            ContentUtil.updateMatchCache(this.mMessage, TrainDataUtil.putSelectedStationDataToSimpleBubble(this.mMessage.bubbleJsonObj));
            return true;
        } catch (Throwable e) {
            LogManager.e("XIAOYUAN", "BubbleTrainBody: onReciver : " + e.getMessage(), e);
            return false;
        }
    }

    private String getCheckString(BusinessSmsMessage message, int dataIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONObject contentData = this.mTrainDataUtil.getViewContentData(message);
        if (contentData != null) {
            stringBuilder.append(contentData.optString("view_m_trainnumber"));
        }
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append(message.getSmsId());
        stringBuilder.append(dataIndex);
        return stringBuilder.toString();
    }

    private void setArriveInfo(String arriveCity, String arriveDate, String arriveTime) {
        ContentUtil.setText(this.mArriveCity, arriveCity, ContentUtil.TRAIN_SELECT_SITES);
        ContentUtil.setText(this.mArriveDate, arriveDate, "");
        ContentUtil.setText(this.mArriveTime, arriveTime, ContentUtil.NO_DATA_ARR_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
    }

    @SuppressLint({"ResourceAsColor"})
    private void setImageAndTextColor(BusinessSmsMessage message) {
        ThemeUtil.setViewBg(this.mContext, this.mLogo, "", R.drawable.duoqu_train_direction);
        ThemeUtil.setViewBg(this.mContext, this.mSelectTrainPlace, (String) message.getValue("v_by_icon_3"), R.drawable.duoqu_train_more);
        ThemeUtil.setTextColor(this.mContext, this.mDepartCity, (String) message.getValue("v_by_text_3"), R.color.duoqu_theme_color_5010);
        ThemeUtil.setTextColor(this.mContext, this.mDepartDate, (String) message.getValue("v_by_text_7"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mArriveDate, (String) message.getValue("v_by_text_8"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mSeatTitle, (String) message.getValue("v_by_text_9"), R.color.duoqu_theme_color_5010);
        ThemeUtil.setTextColor(this.mContext, this.mSeatInfo, (String) message.getValue("v_by_text_10"), R.color.duoqu_theme_color_3010);
        this.mDepartCity.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mArriveCity.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mDepartDate.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mArriveDate.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mSeatTitle.setTextSize(0, (float) ContentUtil.getVerticalTableTitleTextSize());
        this.mSeatInfo.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
    }

    protected void queryTrainStation(final String type) {
        if (!ContentUtil.bubbleDataIsNull(this.mMessage)) {
            this.mTrainDataUtil.queryTrainStation(this.mMessage, new SdkCallBack() {
                public void execute(Object... obj) {
                    if (!queryFail(obj) && BubbleTrainBody.this.mTrainDataUtil.dataBelongCurrentMsg(BubbleTrainBody.this.mMessage, obj) && BubbleTrainBody.this.mContext != null && !BubbleTrainBody.this.mContext.isFinishing() && BubbleTrainBody.this.currentListIndex == BubbleTrainBody.this.mTrainDataUtil.getDefaultSelectedIndex(BubbleTrainBody.this.mMessage)) {
                        Activity activity = BubbleTrainBody.this.mContext;
                        final String str = type;
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                BubbleTrainBody.this.trainDataCompletion(BubbleTrainBody.this.mMessage, str);
                            }
                        });
                    }
                }

                private boolean queryFail(Object... results) {
                    if (results == null || results.length != 2 || results[0] == null) {
                        return true;
                    }
                    if (results[1] instanceof JSONArray) {
                        return false;
                    }
                    return true;
                }
            }, type);
        }
    }

    public void changeData(Map<String, Object> param) {
        try {
            Object ob = param.get("adjust_data");
            if (ob != null) {
                setUpScheduleData(ob);
                return;
            }
            ob = param.get("currentListIndex");
            if (ob != null) {
                this.currentListIndex = ((Integer) ob).intValue();
            }
            bindTrainTickeInfo(this.currentListIndex);
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "BubbleTrainBody: onReciver : " + e.getMessage(), e);
        }
    }

    private void setUpScheduleData(Object ob) {
        if (ob != null && (ob instanceof HashMap) && this.mToWebActivityJsonObject != null && this.mSelectTrainPlace != null && this.mSelectTrainPlace.getVisibility() != 8) {
            try {
                HashMap<String, Object> msgData = (HashMap) ob;
                this.chectString = getCheckString(this.mMessage, this.currentListIndex);
                this.mToWebActivityJsonObject.put("checkString", this.chectString);
                msgData.put("checkString", this.chectString);
                SmartSmsSdkUtil.putCheckString(this.mMsgKey, this.chectString);
                mBroadcastReceiverUtils.register(this.mContext, this.mMsgKey, this, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_TRIAN_STATION_SELECTED");
            } catch (Throwable th) {
            }
        }
    }

    public void destroy() {
        try {
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
        } catch (Throwable th) {
        }
        super.destroy();
    }

    public void runAnimation() {
        ObjectAnimator alphaAnima = ObjectAnimator.ofFloat(this.mLogo, "alpha", new float[]{0.0f, ContentUtil.FONT_SIZE_NORMAL}).setDuration(100);
        ObjectAnimator moveAnima = ObjectAnimator.ofFloat(this.mLogo, "translationX", new float[]{-100.0f, 0.0f}).setDuration(400);
        moveAnima.setInterpolator(new PathInterpolator(0.1f, ContentUtil.FONT_SIZE_NORMAL, 0.9f, ContentUtil.FONT_SIZE_NORMAL));
        moveAnima.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                BubbleTrainBody.this.mLogo.setAlpha(0.0f);
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(moveAnima).with(alphaAnima).after(100);
        animSet.start();
    }
}
