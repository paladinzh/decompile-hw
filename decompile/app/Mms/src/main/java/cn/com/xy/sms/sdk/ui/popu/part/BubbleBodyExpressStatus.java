package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.BroadcastReceiverUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ReceiverInterface;
import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.XyBroadcastReceiver;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.Map;
import org.json.JSONObject;

public class BubbleBodyExpressStatus extends UIPart implements ReceiverInterface {
    public static final String BROADCAST_ACTION = "cn.com.xy.sms.ExpressStatusReceiver";
    public static final String BROADCAST_PERMISSION_GET_EXPRESS_STATUS = "xy.permmisons.smartsms.GET_EXPRESS_STATUS";
    private static final BroadcastReceiverUtils mBroadcastReceiverUtils = new BroadcastReceiverUtils();
    private TextView mExpressStatus = null;
    private String mMsgKey = null;
    private XyBroadcastReceiver mRecerver = null;

    public BubbleBodyExpressStatus(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        super.initUi();
        this.mExpressStatus = (TextView) this.mView.findViewById(R.id.duoqu_express_status);
        this.mExpressStatus.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
    }

    @SuppressLint({"ResourceAsColor"})
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        super.setContent(message, isRebind);
        this.mMessage = message;
        if (message == null) {
            this.mMsgKey = null;
            return;
        }
        this.mMsgKey = String.valueOf(this.mMessage.smsId) + String.valueOf(this.mMessage.msgTime);
        mBroadcastReceiverUtils.reRegisterReceiver(this.mContext, this.mMsgKey, this, this.mRecerver, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_EXPRESS_STATUS");
        ThemeUtil.setTextColor(this.mContext, this.mExpressStatus, (String) message.getValue("v_hd_text_2"), R.color.duoqu_theme_color_3010);
        String statusString = (String) message.bubbleJsonObj.opt("view_express_latest_status");
        if (StringUtils.isNull(statusString)) {
            statusString = (String) message.getValue("m_hd_express_state");
        }
        ContentUtil.setText(this.mExpressStatus, statusString, ContentUtil.EXPRESS_STATUS_DELIVERING);
    }

    public BroadcastReceiver getReceiver() {
        if (this.mRecerver == null) {
            this.mRecerver = new XyBroadcastReceiver(this);
        } else {
            this.mRecerver.setReceiver(this);
        }
        return this.mRecerver;
    }

    public void changeData(Map<String, Object> param) {
        super.changeData(param);
        if (param.containsKey("catagory") && ((String) param.get("catagory")).equals("express")) {
            mBroadcastReceiverUtils.register(this.mContext, this.mMsgKey, this, BROADCAST_ACTION, "xy.permmisons.smartsms.GET_EXPRESS_STATUS");
        }
    }

    public boolean onReceive(Intent intent) {
        try {
            if (!BROADCAST_ACTION.equals(intent.getAction())) {
                return false;
            }
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
            if (this.mMessage == null || this.mMessage.bubbleJsonObj == null) {
                return false;
            }
            String result = intent.getStringExtra("JSONDATA");
            if (StringUtils.isNull(result)) {
                return false;
            }
            JSONObject jsonObject = new JSONObject(result);
            String msgKey = jsonObject.optString("msgKey");
            if (StringUtils.isNull(msgKey) || !msgKey.equals(this.mMsgKey)) {
                return false;
            }
            String statusString = jsonObject.optString("view_express_latest_status");
            if (StringUtils.isNull(statusString)) {
                return false;
            }
            ContentUtil.setText(this.mExpressStatus, statusString, this.mContext.getResources().getString(R.string.duoqu_delivering));
            this.mMessage.bubbleJsonObj.put("view_express_latest_status", statusString);
            ContentUtil.updateMatchCache(this.mMessage);
            return true;
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleBodyExpressStatus onReceive error:" + e.getMessage(), e);
            return false;
        }
    }

    public void destroy() {
        try {
            BroadcastReceiverUtils.unregisterReceiver(this.mContext, this.mRecerver);
            mBroadcastReceiverUtils.removeWorkingReceiver(this.mContext.hashCode());
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("BubbleBodyExpressStatus destroy error:" + e.getMessage(), e);
        }
        super.destroy();
    }
}
