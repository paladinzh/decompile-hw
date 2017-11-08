package cn.com.xy.sms.sdk.ui.popu.part;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.FlightDataUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.R;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

@SuppressLint({"ResourceAsColor"})
public class BubbleAirBody extends UIPart {
    private String arriveAdress;
    private int currentListIndex = 0;
    private String departAdress;
    protected TextView mArriveCityTextView = null;
    protected TextView mArriveDateTextView = null;
    private TextView mArriveTimeTextView = null;
    protected TextView mDepartCityTextView = null;
    protected TextView mDepartDateTextView = null;
    private TextView mDepartTimeTextView = null;
    public FlightDataUtil mFlightDataUtil = FlightDataUtil.getInstance();
    private ImageView mLogo = null;
    private int mTimeColor_hasTime = 0;
    private int mTimeColor_noTime = 0;
    private JSONObject viewContentData;

    public BubbleAirBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mDepartCityTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_depart_city);
        this.mDepartDateTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_depart_date);
        this.mDepartTimeTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_depart_time);
        this.mArriveCityTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_arrive_city);
        this.mArriveDateTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_arrive_date);
        this.mArriveTimeTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_arrive_time);
        this.mLogo = (ImageView) this.mView.findViewById(R.id.duoqu_iv_direction);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (!ContentUtil.bubbleDataIsNull(this.mMessage)) {
            if (!isRebind) {
                setViewStyle(this.mMessage);
            }
            initData();
            bindData(this.currentListIndex);
        }
    }

    private void initData() {
        Object index = this.mMessage.getValue("currentListIndex");
        if (index != null) {
            this.currentListIndex = ((Integer) index).intValue();
        } else {
            this.currentListIndex = 0;
        }
        if (this.mTimeColor_hasTime == 0) {
            this.mTimeColor_hasTime = this.mContext.getResources().getColor(R.color.duoqu_theme_color_3010);
        }
        if (this.mTimeColor_noTime == 0) {
            this.mTimeColor_noTime = this.mContext.getResources().getColor(R.color.duoqu_theme_color_5010);
        }
        this.viewContentData = null;
        this.departAdress = null;
        this.arriveAdress = null;
    }

    @SuppressLint({"ResourceAsColor"})
    private void setViewStyle(BusinessSmsMessage message) {
        ThemeUtil.setTextColor(this.mContext, this.mDepartCityTextView, (String) message.getValue("v_by_text_3"), R.color.duoqu_theme_color_5010);
        ThemeUtil.setTextColor(this.mContext, this.mDepartDateTextView, (String) message.getValue("v_hd_text_7"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mArriveCityTextView, (String) message.getValue("v_hd_text_4"), R.color.duoqu_theme_color_5010);
        ThemeUtil.setTextColor(this.mContext, this.mArriveDateTextView, (String) message.getValue("v_hd_text_8"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setViewBg(this.mContext, this.mLogo, "", R.drawable.duoqu_air_direction);
        this.mDepartCityTextView.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mArriveCityTextView.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mDepartDateTextView.setTextSize(0, (float) ContentUtil.getDateTextSize());
        this.mArriveDateTextView.setTextSize(0, (float) ContentUtil.getDateTextSize());
    }

    protected void bindData(int currentListIndex) {
        try {
            this.mMessage.putValue("db_air_data_index", Integer.valueOf(currentListIndex));
            this.viewContentData = this.mFlightDataUtil.getViewContentData(this.mMessage);
            if (this.viewContentData != null) {
                String fightNumber = this.viewContentData.optString("view_flight_number");
                String departData = this.viewContentData.optString("view_depart_date");
                String departTime = this.viewContentData.optString("view_depart_time");
                String arriveData = this.viewContentData.optString("view_arrive_date");
                String arriveTime = this.viewContentData.optString("view_arrive_time");
                this.departAdress = getAdress(this.viewContentData.optString("view_depart_city"), this.viewContentData.optString("view_depart_airport"), this.viewContentData.optString("view_depart_terminal"));
                this.arriveAdress = getAdress(this.viewContentData.optString("view_arrive_city"), this.viewContentData.optString("view_arrive_airport"), this.viewContentData.optString("view_arrive_terminal"));
                ContentUtil.setText(this.mDepartDateTextView, departData, "");
                ContentUtil.setText(this.mDepartTimeTextView, departTime, ContentUtil.NO_DATA_DEP_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
                ContentUtil.setText(this.mArriveDateTextView, arriveData, "");
                ContentUtil.setText(this.mArriveTimeTextView, arriveTime, ContentUtil.NO_DATA_ARR_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
                ContentUtil.setText(this.mDepartCityTextView, this.departAdress, "");
                ContentUtil.setText(this.mArriveCityTextView, this.arriveAdress, "");
                Object hasQuery = this.mMessage.getValue("hasQuery" + currentListIndex);
                if (hasQuery == null || !((Boolean) hasQuery).booleanValue()) {
                    if (!((!StringUtils.isNull(departTime) && !StringUtils.isNull(this.arriveAdress) && !StringUtils.isNull(arriveData) && !StringUtils.isNull(arriveTime) && !StringUtils.isNull(this.mFlightDataUtil.getInterfaceFlightState(this.mMessage))) || StringUtils.isNull(fightNumber) || StringUtils.isNull(departData) || StringUtils.isNull(this.departAdress))) {
                        queryFlyDataAsy(this.mMessage);
                    }
                }
                flyDataCompletion(this.mMessage, arriveData, arriveTime, departTime);
            }
        } catch (RuntimeException e) {
            LogManager.e("XIAOYUAN", "BubbleAirBody: bindData RuntimeException:" + e.getMessage(), e);
        } catch (Throwable e2) {
            LogManager.e("XIAOYUAN", "BubbleAirBody: bindData error:" + e2.getMessage(), e2);
        }
    }

    private String getAdress(String city, String airport, String hterminal) {
        StringBuilder append = new StringBuilder().append(city).append(" ");
        if (airport.startsWith(city)) {
            airport = airport.replace(city, "");
        }
        return append.append(airport).append(hterminal).toString();
    }

    protected void queryFlyDataAsy(final BusinessSmsMessage smsMessage) {
        if (!ContentUtil.bubbleDataIsNull(smsMessage)) {
            this.mFlightDataUtil.queryFlyData(smsMessage, new SdkCallBack() {
                public void execute(Object... obj) {
                    if (!queryFail(obj) && BubbleAirBody.this.mFlightDataUtil.dataBelongCurrentMsg(BubbleAirBody.this.mMessage, obj) && BubbleAirBody.this.mContext != null && !BubbleAirBody.this.mContext.isFinishing() && BubbleAirBody.this.currentListIndex == BubbleAirBody.this.mFlightDataUtil.getDefaultSelectedIndex(BubbleAirBody.this.mMessage)) {
                        try {
                            final HashMap<String, Object> param = new HashMap();
                            param.put("flightState", BubbleAirBody.this.mFlightDataUtil.getInterfaceFlightState(smsMessage));
                            param.put("currentListIndex", Integer.valueOf(BubbleAirBody.this.currentListIndex));
                            param.put(NumberInfo.TYPE_KEY, Integer.valueOf(4));
                            BubbleAirBody.this.mContext.runOnUiThread(new Runnable() {
                                public void run() {
                                    BubbleAirBody.this.mBasePopupView.changeData(param);
                                }
                            });
                        } catch (Exception e) {
                            LogManager.e("XIAOYUAN", "BubbleAirBody: setWebJson : " + e.getMessage(), e);
                        }
                    }
                }

                private boolean queryFail(Object... obj) {
                    return obj == null || obj.length < 1 || obj[0] == null;
                }
            });
        }
    }

    protected void flyDataCompletion(BusinessSmsMessage smsMessage, String arriveData, String arriveTime, String departTime) {
        String mArriveCity = this.mFlightDataUtil.getInterfaceFlightArriveCity(smsMessage);
        if (!StringUtils.isNull(mArriveCity) && StringUtils.isNull(this.arriveAdress)) {
            ContentUtil.setText(this.mArriveCityTextView, mArriveCity, "");
        }
        if (StringUtils.isNull(arriveData)) {
            ContentUtil.setText(this.mArriveDateTextView, this.mFlightDataUtil.getInterfaceFlightArriveDate(smsMessage), "");
        }
        if (StringUtils.isNull(arriveTime)) {
            ContentUtil.setText(this.mArriveTimeTextView, this.mFlightDataUtil.getInterfaceFlightArriveTime(smsMessage), ContentUtil.NO_DATA_ARR_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
        }
        if (StringUtils.isNull(departTime)) {
            ContentUtil.setText(this.mDepartTimeTextView, this.mFlightDataUtil.getInterfaceFlightDepartTime(smsMessage), ContentUtil.NO_DATA_DEP_TIME, ContentUtil.getTimeTextSize(), ContentUtil.getNoneTimeTextSize(), this.mTimeColor_hasTime, this.mTimeColor_noTime);
        }
        String mDepartCity = this.mFlightDataUtil.getInterfaceFlightDepartCity(smsMessage);
        if (!StringUtils.isNull(mDepartCity) && StringUtils.isNull(this.departAdress)) {
            ContentUtil.setText(this.mDepartCityTextView, mDepartCity, "");
        }
    }

    public void changeData(Map<String, Object> param) {
        try {
            Object ob = param.get("currentListIndex");
            if (ob != null) {
                this.currentListIndex = ((Integer) ob).intValue();
            }
            bindData(this.currentListIndex);
        } catch (Exception e) {
            LogManager.e("XIAOYUAN", "BubbleAirBody: setWebJson : " + e.getMessage(), e);
        }
    }

    public void runAnimation() {
        ObjectAnimator alphaAnima = ObjectAnimator.ofFloat(this.mLogo, "alpha", new float[]{0.0f, ContentUtil.FONT_SIZE_NORMAL}).setDuration(100);
        ObjectAnimator moveAnima = ObjectAnimator.ofFloat(this.mLogo, "translationX", new float[]{-100.0f, 0.0f}).setDuration(400);
        moveAnima.setInterpolator(new PathInterpolator(0.1f, ContentUtil.FONT_SIZE_NORMAL, 0.9f, ContentUtil.FONT_SIZE_NORMAL));
        moveAnima.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                BubbleAirBody.this.mLogo.setAlpha(0.0f);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(moveAnima).with(alphaAnima).after(100);
        animSet.start();
    }
}
