package com.huawei.netassistant.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import com.huawei.hwsystemmanager.HwCustSystemManagerUtils;
import com.huawei.systemmanager.R;

public class HwCustNetAssistant4GInfoActivityImpl extends HwCustNetAssistant4GInfoActivity {
    public void showLTEString(Activity activity, int subId) {
        boolean isLTE = false;
        boolean is4_5G = false;
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_lte", activity.getApplicationContext(), subId)) {
            isLTE = true;
        }
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_4_5G_for_mcc", activity.getApplicationContext(), subId)) {
            is4_5G = true;
        }
        updateLTEString(activity, isLTE, is4_5G);
    }

    public void showLTEString(Activity activity) {
        boolean isLTE = false;
        boolean is4_5G = false;
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_lte", activity.getApplicationContext())) {
            isLTE = true;
        }
        if (HwCustSystemManagerUtils.isMccChange4G("hw_show_4_5G_for_mcc", activity.getApplicationContext())) {
            is4_5G = true;
        }
        updateLTEString(activity, isLTE, is4_5G);
    }

    private void updateLTEString(Activity activity, boolean isLTE, boolean is4_5G) {
        if (isLTE || is4_5G) {
            ViewStub viewStub = (ViewStub) activity.findViewById(R.id.empty_view);
            if (viewStub != null) {
                View inflateView = viewStub.inflate();
                if (inflateView != null) {
                    TextView netAssistantView = (TextView) inflateView.findViewById(R.id.net_assistant_4g_no_app);
                    if (netAssistantView != null) {
                        String netAssistant4gString = netAssistantView.getText().toString();
                        if (isLTE) {
                            activity.setTitle(activity.getTitle().toString().replace("4G", "LTE"));
                            netAssistant4gString = netAssistant4gString.replace("4G", "LTE");
                            netAssistantView.setText(netAssistant4gString);
                        }
                        if (is4_5G) {
                            String str = activity.getTitle().toString();
                            activity.setTitle(str.replace("4G", "4.5G"));
                            netAssistant4gString = netAssistant4gString.replace("4G", "4.5G");
                            activity.getActionBar().setTitle(str.replace("4G", "4.5G"));
                            netAssistantView.setText(netAssistant4gString);
                        }
                    }
                }
            }
        }
    }
}
