package cn.com.xy.sms.sdk.ui.notification;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.NotificationReceiver;
import com.android.mms.ui.MessageUtils;
import java.util.HashMap;

public class DoActionActivity extends Activity {
    private static final String CALL_PHONE = "call_phone";
    private String mActionType = null;

    protected void onStart() {
        super.onStart();
        if (isMultiSimToCalling()) {
            setVisible(true);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawable(new ColorDrawable(17170445));
        String actionData = getIntent().getStringExtra("action_data");
        this.mActionType = getIntent().getStringExtra("action_type");
        boolean doFinishActivity = true;
        if (!StringUtils.isNull(actionData)) {
            HashMap<String, String> valueMap = new HashMap();
            String hwParseTime = getIntent().getStringExtra(Constant.KEY_HW_PARSE_TIME);
            if (!StringUtils.isNull(hwParseTime)) {
                valueMap.put(Constant.KEY_HW_PARSE_TIME, hwParseTime);
                doFinishActivity = false;
            }
            Intent intent = new Intent();
            intent.setFlags(268435456);
            String msgId = getIntent().getStringExtra("msgId");
            if (!StringUtils.isNull(msgId)) {
                intent.putExtra("msg_uri", Uri.parse("content://sms/" + msgId));
            }
            long threadId = -1;
            String threadIdStr = getIntent().getStringExtra("thread_id");
            if (!TextUtils.isEmpty(threadIdStr)) {
                threadId = Long.parseLong(threadIdStr);
            }
            intent.putExtra("thread_id", threadId);
            intent.putExtra("mms_notification_id", NotificationReceiver.getInst().getNotificationId(this));
            intent.putExtra("HandleType", 0);
            intent.setAction("com.huawei.mms.action.headsup.clicked");
            intent.setPackage("com.android.mms");
            sendBroadcastAsUser(intent, UserHandle.OWNER);
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, -1, false);
            DuoquUtils.doActionContext(this, actionData, valueMap);
            if (!StringUtils.isNull(msgId)) {
                SmartSmsSdkUtil.clearSmartNotifyResult(msgId);
                DuoquNotificationViewManager.cancelNotification(this, Integer.parseInt(msgId));
            }
        }
        if (!isMultiSimToCalling() && doFinishActivity) {
            finish();
        }
    }

    private boolean isMultiSimToCalling() {
        boolean enableCard1 = false;
        boolean enableCard2 = false;
        if (MessageUtils.isMultiSimEnabled()) {
            enableCard1 = 1 == MessageUtils.getIccCardStatus(0);
            enableCard2 = 1 == MessageUtils.getIccCardStatus(1);
        }
        if (this.mActionType != null && CALL_PHONE.equalsIgnoreCase(this.mActionType) && enableCard1) {
            return enableCard2;
        }
        return false;
    }
}
