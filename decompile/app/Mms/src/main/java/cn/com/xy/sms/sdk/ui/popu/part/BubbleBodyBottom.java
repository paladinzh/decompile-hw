package cn.com.xy.sms.sdk.ui.popu.part;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.google.android.gms.R;

public class BubbleBodyBottom extends UIPart {
    private TextView mBottomContentTextView = null;

    public BubbleBodyBottom(Activity mContext, BusinessSmsMessage message, XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(mContext, message, callback, layoutId, root, partId);
    }

    public void initUi() {
        this.mBottomContentTextView = (TextView) this.mView.findViewById(R.id.duoqu_tv_bottom_content);
    }

    public void setContent(BusinessSmsMessage message, boolean isRebind) throws Exception {
        this.mMessage = message;
        if (message == null) {
            ContentUtil.setViewVisibility(this.mView, 8);
            return;
        }
        String bottomContent = (String) message.getValue("view_bottom_content");
        if (StringUtils.isNull(bottomContent)) {
            ContentUtil.setViewVisibility(this.mView, 8);
            return;
        }
        ContentUtil.setViewVisibility(this.mView, 0);
        ContentUtil.setText(this.mBottomContentTextView, bottomContent, null);
        if (!isRebind) {
            setViewStyle();
        }
    }

    private void setViewStyle() {
        ContentUtil.setTextColor(this.mBottomContentTextView, (String) this.mMessage.getValue("view_bottom_content_color"));
    }
}
