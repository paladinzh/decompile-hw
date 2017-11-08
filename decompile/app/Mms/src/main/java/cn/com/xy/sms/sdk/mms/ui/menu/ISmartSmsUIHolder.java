package cn.com.xy.sms.sdk.mms.ui.menu;

import android.app.Activity;
import android.view.View;
import com.android.mms.ui.SmartSmsBubbleManager;

public interface ISmartSmsUIHolder {
    void addNeedRefreshSmartBubbleItem(SmartSmsBubbleManager smartSmsBubbleManager);

    boolean editorHasContent();

    boolean equalMsgNumber(String str);

    View findViewById(int i);

    Activity getActivityContext();

    boolean isIntentHasSmsBody();

    boolean isNotifyComposeMessage();

    boolean onSmartSmsEvent(short s);

    void setFlingState(boolean z);

    void setReplySmsBody(String str);
}
