package com.huawei.netassistant.ui;

import android.content.Context;
import android.widget.TextView;
import com.huawei.hwsystemmanager.HwCustSystemManagerUtils;
import com.huawei.systemmanager.R;

public class HwCustNetAssistantMainActivityImpl extends HwCustNetAssistantMainActivity {
    public boolean showLteString(Context context, TextView netAssistantView, int titleId) {
        if (titleId == R.string.net_assistant_4g_info && HwCustSystemManagerUtils.isMccChange4G("hw_show_lte", context)) {
            netAssistantView.setText(context.getResources().getString(R.string.net_assistant_4g_info).replace("4G", "LTE"));
            return true;
        } else if (titleId != R.string.net_assistant_4g_info || !HwCustSystemManagerUtils.isMccChange4G("hw_show_4_5G_for_mcc", context)) {
            return false;
        } else {
            netAssistantView.setText(context.getResources().getString(R.string.net_assistant_4g_info).replace("4G", "4.5G"));
            return true;
        }
    }
}
