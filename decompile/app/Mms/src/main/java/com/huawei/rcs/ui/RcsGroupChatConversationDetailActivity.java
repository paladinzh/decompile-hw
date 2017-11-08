package com.huawei.rcs.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatConversationDetailFragment;
import com.huawei.mms.ui.HwBaseActivity;

public class RcsGroupChatConversationDetailActivity extends HwBaseActivity {
    private RcsGroupChatConversationDetailFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(1073741824);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mFragment == null) {
            this.mFragment = new RcsGroupChatConversationDetailFragment();
        }
        if (this.mFragment != null) {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "Mms_UI_GCCDF");
            transaction.commit();
        }
    }
}
