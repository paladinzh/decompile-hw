package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.UIConstant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.HashMap;
import org.json.JSONObject;

@SuppressLint({"ResourceAsColor"})
public class BubbleSimpleCallNumberBody extends UIPart {
    private static String mReNumber = "";
    private static HashMap<String, ViewHolder> sViewHolderCach = new HashMap(5);
    private boolean isQuery = false;
    private String mMsgKey = null;
    private TextView mNumberNameView = null;
    private TextView mNumbertagView = null;
    private String mPhoneNumberQuery = "";
    private ViewHolder mVh = null;

    private static class ViewHolder {
        private String contactsName;
        private String contentColor;
        private String msgKey;
        private String normalContent;
        private String phoneNumber;
        private String reconizeTitle;
        private String titleColor;

        private ViewHolder() {
            this.normalContent = "";
            this.reconizeTitle = "";
            this.msgKey = "";
            this.titleColor = "";
            this.contentColor = "";
            this.phoneNumber = "";
            this.contactsName = "";
        }
    }

    public BubbleSimpleCallNumberBody(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mNumbertagView = (TextView) this.mView.findViewById(R.id.duoqu_bubble_generalone_number_tag);
        this.mNumberNameView = (TextView) this.mView.findViewById(R.id.duoqu_bubble_generalone_name);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null) {
            this.mMsgKey = null;
            return;
        }
        String reNumber = (String) message.getValue("phoneNum");
        if (reNumber == null || !reNumber.equals(mReNumber)) {
            if (sViewHolderCach != null) {
                sViewHolderCach.clear();
            }
            this.mVh = null;
        }
        setReNumber(reNumber);
        ViewHolder viewHolder = null;
        this.mMsgKey = String.valueOf(message.smsId) + String.valueOf(message.msgTime);
        if (this.mVh == null || TextUtils.isEmpty(this.mVh.msgKey) || !this.mVh.msgKey.equals(this.mMsgKey)) {
            if (!(this.mMsgKey == null || sViewHolderCach == null || !sViewHolderCach.containsKey(this.mMsgKey))) {
                viewHolder = (ViewHolder) sViewHolderCach.get(this.mMsgKey);
            }
            if (viewHolder == null) {
                viewHolder = buildViewHold(message);
                if (!(viewHolder == null || sViewHolderCach == null)) {
                    sViewHolderCach.put(this.mMsgKey, viewHolder);
                }
            }
            this.mVh = viewHolder;
        } else {
            viewHolder = this.mVh;
        }
        buildReconize();
        setStyle(isRebind);
        updateReconizeView();
        if (!(this.mVh == null || TextUtils.isEmpty(this.mVh.phoneNumber))) {
            startQueryContats(this.mVh.phoneNumber);
        }
    }

    private ViewHolder buildViewHold(BusinessSmsMessage message) {
        ViewHolder vh = new ViewHolder();
        if (message == null) {
            return null;
        }
        vh.reconizeTitle = (String) message.getValue("m_by_text_1");
        if (TextUtils.isEmpty(vh.reconizeTitle)) {
            vh.reconizeTitle = this.mContext.getString(R.string.duoqu_phonenumber_reconize_tag);
        }
        vh.normalContent = (String) message.getValue("m_by_text_2");
        vh.phoneNumber = (String) message.getValue("view_side_phone_num");
        if (TextUtils.isEmpty(vh.phoneNumber)) {
            vh.phoneNumber = vh.normalContent;
        }
        vh.titleColor = (String) message.getValue("v_by_text_1");
        vh.contentColor = (String) message.getValue("v_by_text_2");
        vh.msgKey = String.valueOf(message.smsId);
        return vh;
    }

    private void startQueryContats(final String number) {
        if (!this.isQuery || this.mPhoneNumberQuery == null || !this.mPhoneNumberQuery.equals(number)) {
            this.mPhoneNumberQuery = number;
            if (!StringUtils.isNull(number)) {
                new AsyncTask() {
                    protected String doInBackground(Object... arg) {
                        BubbleSimpleCallNumberBody.this.isQuery = true;
                        String name = "";
                        JSONObject contactObj = DuoquUtils.getSdkDoAction().getContactObj(BubbleSimpleCallNumberBody.this.mContext, number);
                        if (contactObj == null) {
                            return "";
                        }
                        return (String) JsonUtil.getValFromJsonObject(contactObj, UIConstant.CONTACT_NAME);
                    }

                    protected void onPostExecute(Object result) {
                        String name = "";
                        if (BubbleSimpleCallNumberBody.this.mContext != null) {
                            if (result != null) {
                                name = (String) result;
                            }
                            if (TextUtils.isEmpty(name)) {
                                name = BubbleSimpleCallNumberBody.this.mContext.getString(R.string.duoqu_calls_reminder_stranger);
                            }
                            if (!(BubbleSimpleCallNumberBody.this.mVh == null || !number.equals(BubbleSimpleCallNumberBody.this.mVh.phoneNumber) || name.equals(BubbleSimpleCallNumberBody.this.mVh.contactsName))) {
                                BubbleSimpleCallNumberBody.this.mVh.contactsName = name;
                                BubbleSimpleCallNumberBody.this.buildReconize();
                                BubbleSimpleCallNumberBody.this.updateReconizeView();
                            }
                            BubbleSimpleCallNumberBody.this.isQuery = false;
                        }
                    }
                }.execute(new Object[]{number});
            }
        }
    }

    private void buildReconize() {
        if (this.mVh != null) {
            if (StringUtils.isNull(this.mVh.phoneNumber)) {
                if (this.mNumberNameView != null) {
                    this.mNumberNameView.setText("");
                }
                if (this.mNumbertagView != null) {
                    this.mNumbertagView.setText("");
                }
            } else if (StringUtils.isNull(this.mVh.reconizeTitle)) {
                if (this.mNumbertagView != null) {
                    this.mNumbertagView.setText("");
                }
            } else {
                if (this.mNumbertagView != null) {
                    this.mNumbertagView.setText(this.mVh.reconizeTitle);
                }
                if (TextUtils.isEmpty(this.mVh.contactsName)) {
                    this.mNumberNameView.setText(R.string.duoqu_unknow_phone_number);
                } else {
                    this.mNumberNameView.setText(this.mVh.contactsName);
                }
            }
        }
    }

    private void updateReconizeView() {
        if (this.mNumberNameView == null || this.mNumberNameView.getText() == null || this.mNumberNameView.getText().length() <= 0 || this.mNumbertagView == null || this.mNumbertagView.getText() == null || this.mNumbertagView.getText().length() <= 0) {
            if (this.mNumberNameView != null) {
                this.mNumberNameView.setVisibility(8);
            }
            if (this.mNumbertagView != null) {
                this.mNumbertagView.setVisibility(8);
                return;
            }
            return;
        }
        this.mNumberNameView.setVisibility(0);
        this.mNumbertagView.setVisibility(0);
    }

    private void setStyle(boolean isRebind) {
        if (!isRebind) {
            setTextStyle();
        }
    }

    private void setTextStyle() {
        if (this.mVh != null) {
            if (!StringUtils.isNull(this.mVh.titleColor)) {
                ThemeUtil.setTextColor(this.mContext, this.mNumbertagView, this.mVh.titleColor, R.color.duoqu_theme_color_5010);
            }
            if (!StringUtils.isNull(this.mVh.contentColor)) {
                ThemeUtil.setTextColor(this.mContext, this.mNumberNameView, this.mVh.contentColor, R.color.duoqu_theme_color_3010);
            }
        }
    }

    public static void setReNumber(String reNumber) {
        mReNumber = reNumber;
    }

    public void destroy() {
        super.destroy();
        if (sViewHolderCach != null) {
            sViewHolderCach.clear();
        }
    }
}
