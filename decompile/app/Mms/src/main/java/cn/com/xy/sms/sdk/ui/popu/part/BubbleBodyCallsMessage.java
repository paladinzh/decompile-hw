package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils.TruncateAt;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UIConstant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import org.json.JSONObject;

public class BubbleBodyCallsMessage extends UIPart {
    private static final String CALL_NUMBER_TEXT = "smart_call_number_text";
    private static final String CALL_NUMBER_TEXT_COLOR = "smart_call_number_text_color";
    private TextView mCallNumber;
    private TextView mCallNumberdate;
    private TextView mCallNumberdatelable;
    private TextView mCallNumberfrom;
    private TextView mCallNumberfromlable;
    private TextView mCallNumberlable;
    private TextView mCallNumberstate;
    private TextView mCallNumberstatelable;
    private TextView mCallUserNameTextView;
    private TextView mCallUserNameTextViewlable;
    private TextView mCallingstate;
    private TextView mCallingstatelable;
    private TextView mCallingtime;
    private TextView mCallingtimelable;
    private TextView mLastcallNumberdate;
    private TextView mLastcallNumberdatelable;
    private TextView mNumberFrequencyTextView;
    private TextView mNumberFrequencyTextViewlable;
    private int mNumberMaxLable = 0;
    private LayoutParams mTextParams;

    public BubbleBodyCallsMessage(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        this.mCallUserNameTextView = (TextView) this.mView.findViewById(R.id.duoqu_call_username);
        this.mCallUserNameTextViewlable = (TextView) this.mView.findViewById(R.id.duoqu_call_username_title);
        this.mNumberFrequencyTextView = (TextView) this.mView.findViewById(R.id.duoqu_number_frequency);
        this.mNumberFrequencyTextViewlable = (TextView) this.mView.findViewById(R.id.duoqu_comingcall_number_title);
        this.mCallNumberdate = (TextView) this.mView.findViewById(R.id.duoqu_call_time);
        this.mCallNumberdatelable = (TextView) this.mView.findViewById(R.id.duoqu_call_time_title);
        this.mLastcallNumberdate = (TextView) this.mView.findViewById(R.id.duoqu_lastcall_time);
        this.mLastcallNumberdatelable = (TextView) this.mView.findViewById(R.id.duoqu_lastcall_time_title);
        this.mCallNumberfrom = (TextView) this.mView.findViewById(R.id.duoqu_call_from);
        this.mCallNumberfromlable = (TextView) this.mView.findViewById(R.id.duoqu_callfrom_time_title);
        this.mCallNumberstate = (TextView) this.mView.findViewById(R.id.duoqu_call_state);
        this.mCallNumberstatelable = (TextView) this.mView.findViewById(R.id.duoqu_call_state_title);
        this.mCallingstate = (TextView) this.mView.findViewById(R.id.duoqu_calling_state);
        this.mCallingstatelable = (TextView) this.mView.findViewById(R.id.duoqu_calling_state_title);
        this.mCallingtime = (TextView) this.mView.findViewById(R.id.duoqu_callingfrom_time);
        this.mCallingtimelable = (TextView) this.mView.findViewById(R.id.duoqu_callingfrom_time_title);
        this.mCallNumber = (TextView) this.mView.findViewById(R.id.duoqu_call_number);
        this.mCallNumberlable = (TextView) this.mView.findViewById(R.id.duoqu_call_number_title);
        this.mTextParams = new LayoutParams(-2, -2);
        this.mTextParams.setMargins(0, ContentUtil.getTableLineSpacing(), 0, 0);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (!isRebind) {
            setViewStyle();
        }
        this.mNumberMaxLable = 0;
        ContentUtil.setViewVisibility(this.mView, 0);
        String callNumber = (String) message.getValue("view_side_phone_num");
        String frequency = (String) message.getValue("view_frequency");
        String call_time = (String) message.getValue("view_call_time");
        String lastcall_time = (String) message.getValue("view_lastcall_time");
        String sideattribution = (String) message.getValue("view_sideattribution");
        String ownstate = (String) message.getValue("view_ownstate");
        String sidestate = (String) message.getValue("view_sidestate");
        String calling_time = (String) message.getValue("view_calling_time");
        int callNumberColor = getCallNumberColor();
        setLableVisibility(this.mNumberFrequencyTextView, this.mNumberFrequencyTextViewlable, frequency, null);
        setLableVisibility(this.mCallNumberdate, this.mCallNumberdatelable, call_time, null);
        setLableVisibility(this.mLastcallNumberdate, this.mLastcallNumberdatelable, lastcall_time, null);
        setLableVisibility(this.mCallNumberfrom, this.mCallNumberfromlable, sideattribution, null);
        setLableVisibility(this.mCallNumberstate, this.mCallNumberstatelable, ownstate, null);
        setLableVisibility(this.mCallingstate, this.mCallingstatelable, sidestate, null);
        setLableVisibility(this.mCallingtime, this.mCallingtimelable, calling_time, null);
        setLableVisibility(this.mCallNumber, this.mCallNumberlable, callNumber, null);
        ContentUtil.setViewVisibility(this.mCallUserNameTextView, 8);
        ContentUtil.setViewVisibility(this.mCallUserNameTextViewlable, 8);
        if (callNumberColor != Integer.MIN_VALUE) {
            bindCallUserViewValue((String) this.mMessage.getValue(CALL_NUMBER_TEXT), callNumberColor);
        } else {
            bindCallUserInfo();
        }
    }

    private void setViewStyle() {
        setViewStyle(this.mCallUserNameTextViewlable, this.mCallUserNameTextView);
        setViewStyle(this.mNumberFrequencyTextViewlable, this.mNumberFrequencyTextView);
        setViewStyle(this.mCallNumberdatelable, this.mCallNumberdate);
        setViewStyle(this.mLastcallNumberdatelable, this.mLastcallNumberdate);
        setViewStyle(this.mCallNumberfromlable, this.mCallNumberfrom);
        setViewStyle(this.mCallNumberstatelable, this.mCallNumberstate);
        setViewStyle(this.mCallingstatelable, this.mCallingstate);
        setViewStyle(this.mCallingtimelable, this.mCallingtime);
        setViewStyle(this.mCallNumberlable, this.mCallNumber);
    }

    private void setViewStyle(TextView titleTextView, TextView contentTextView) {
        titleTextView.setLayoutParams(this.mTextParams);
        contentTextView.setLayoutParams(this.mTextParams);
        titleTextView.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
        contentTextView.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
        if (!ContentUtil.isHugeEnabled()) {
            contentTextView.setSingleLine();
            contentTextView.setEllipsize(TruncateAt.valueOf("END"));
        }
    }

    private void bindCallUserInfo() {
        new AsyncTask() {
            protected BusinessSmsMessage doInBackground(Object... arg) {
                String name;
                BusinessSmsMessage msg = arg[0];
                String callNumber = (String) msg.getValue("view_side_phone_num");
                JSONObject contactObj = DuoquUtils.getSdkDoAction().getContactObj(BubbleBodyCallsMessage.this.mContext, callNumber);
                int contentColor = ContentUtil.getContentTextColor();
                if (contactObj == null) {
                    name = callNumber;
                } else {
                    Integer contactType = (Integer) JsonUtil.getValFromJsonObject(contactObj, UIConstant.CONTACT_TYPE);
                    if (!(contactType == null || contactType.intValue() == 0 || 1 == contactType.intValue())) {
                        contentColor = ContentUtil.COR_RED;
                    }
                    name = (String) JsonUtil.getValFromJsonObject(contactObj, UIConstant.CONTACT_NAME);
                }
                msg.putValue(BubbleBodyCallsMessage.CALL_NUMBER_TEXT_COLOR, Integer.valueOf(contentColor));
                msg.putValue(BubbleBodyCallsMessage.CALL_NUMBER_TEXT, name);
                return msg;
            }

            protected void onPostExecute(Object result) {
                if (BubbleBodyCallsMessage.this.mMessage == result) {
                    BubbleBodyCallsMessage.this.bindCallUserViewValue((String) BubbleBodyCallsMessage.this.mMessage.getValue(BubbleBodyCallsMessage.CALL_NUMBER_TEXT), ((Integer) BubbleBodyCallsMessage.this.mMessage.getValue(BubbleBodyCallsMessage.CALL_NUMBER_TEXT_COLOR)).intValue());
                }
            }
        }.execute(new Object[]{this.mMessage});
    }

    private void bindCallUserViewValue(String callNumberName, int contentTextColor) {
        if (this.mCallUserNameTextView != null && this.mCallUserNameTextViewlable != null) {
            if (StringUtils.isNull(callNumberName)) {
                this.mCallUserNameTextView.setVisibility(8);
                this.mCallUserNameTextViewlable.setVisibility(8);
            } else {
                this.mCallUserNameTextView.setText(callNumberName);
                if (contentTextColor != Integer.MIN_VALUE) {
                    this.mCallUserNameTextView.setTextColor(contentTextColor);
                }
                this.mCallUserNameTextView.setVisibility(0);
                this.mCallUserNameTextViewlable.setVisibility(0);
            }
        }
    }

    public void setLableVisibility(TextView t1, TextView t2, String value, String color) {
        if (this.mNumberMaxLable >= 4) {
            t1.setVisibility(8);
            t2.setVisibility(8);
            return;
        }
        if (StringUtils.isNull(value)) {
            t1.setVisibility(8);
            t2.setVisibility(8);
        } else {
            t1.setVisibility(0);
            t2.setVisibility(0);
            ContentUtil.setText(t1, value, null);
            ContentUtil.setTextColor(t1, color);
            this.mNumberMaxLable++;
        }
    }

    private int getCallNumberColor() {
        Object callNumberColorObj = this.mMessage.getValue(CALL_NUMBER_TEXT_COLOR);
        if (callNumberColorObj == null || !(callNumberColorObj instanceof Integer)) {
            return Integer.MIN_VALUE;
        }
        return ((Integer) callNumberColorObj).intValue();
    }
}
