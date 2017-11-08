package cn.com.xy.sms.sdk.ui.popu.part;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;
import java.util.HashMap;
import java.util.Map;

public class BubbleOriginSmsText extends UIPart {
    private Map<String, View> mSmartSmsEventParam = new HashMap();
    private TextView mSmsContent;
    private TextView mWarningContent;

    public BubbleOriginSmsText(Activity context, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    public void initUi() throws Exception {
        super.initUi();
        this.mSmsContent = (TextView) this.mView.findViewById(R.id.duoqu_sms_origin_text);
        this.mWarningContent = (TextView) this.mView.findViewById(R.id.duoqu_warning_text);
        this.mSmsContent.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
        this.mWarningContent.setTextSize(0, (float) ContentUtil.getVerticalTableContentTextSize());
    }

    @SuppressLint({"ResourceAsColor"})
    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        super.setContent(message, isRebind);
        this.mMessage = message;
        ThemeUtil.setTextColor(this.mContext, this.mSmsContent, (String) message.getValue("v_by_text_d_1"), R.color.duoqu_theme_color_3010);
        ThemeUtil.setTextColor(this.mContext, this.mWarningContent, (String) message.getValue("v_by_text_d_1"), R.color.duoqu_theme_color_3010);
        String smsContent = (String) message.getValue(ContentUtil.DUOQU_SMS_CONTENT_KEY);
        String smsWarning = (String) message.getValue("m_by_text_4");
        if (StringUtils.isNull(smsContent)) {
            smsContent = message.getMessageBody();
        }
        SpannableStringBuilder linkingMsg = (SpannableStringBuilder) message.extendParamMap.get("linkingMsg");
        if (linkingMsg == null || linkingMsg.getSpans(0, linkingMsg.length(), Object.class).length == 0) {
            ContentUtil.setText(this.mSmsContent, smsContent, null);
        } else if (this.mSmsContent != null) {
            this.mSmsContent.setText(linkingMsg, BufferType.SPANNABLE);
            setSpandTouchMonitor(this.mSmsContent);
        }
        if (this.mWarningContent != null) {
            ContentUtil.setText(this.mWarningContent, smsWarning, null);
            if (StringUtils.isNull(smsWarning)) {
                this.mWarningContent.setVisibility(8);
            } else {
                this.mWarningContent.setVisibility(0);
            }
        }
    }

    public void setSpandTouchMonitor(View view) {
        if (view != null) {
            try {
                if (this.mBasePopupView != null) {
                    this.mSmartSmsEventParam.put("spandTextView", view);
                    this.mBasePopupView.callSmartSmsEvent(2, this.mSmartSmsEventParam);
                }
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("BubbleOriginSmsText setSpandTouchMonitor error=" + e.getMessage(), e);
            }
        }
    }
}
