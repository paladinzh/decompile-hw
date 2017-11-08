package com.android.rcs.ui;

import android.content.Context;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AttachmentTypeSelectorAdapter;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.rcs.utils.RcseMmsExt;
import java.util.List;

public class RcsAttachmentTypeSelectorAdapter {
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();

    public void addExtItem(List<IconListItem> data, Context context) {
        if (this.isRcsEnable) {
            boolean mShowAnyFile = false;
            if (context instanceof ComposeMessageActivity) {
                ComposeMessageFragment fragment = (ComposeMessageFragment) FragmentTag.getFragmentByTag((ComposeMessageActivity) context, "Mms_UI_CMF");
                if (fragment != null) {
                    mShowAnyFile = fragment.getRcsComposeMessage().getFtCapabilityReqForInsertFile();
                }
            }
            if (RcseMmsExt.isRcsMode() && (r2 || MmsConfig.getHwCustMmsConfig().getFileTransferCapability())) {
                AttachmentTypeSelectorAdapter.addItem(data, context.getString(R.string.attach_anyfile), R.drawable.rcs_ic_attach_anyfile_holo_light, 23);
            }
        }
    }

    public boolean isImModeNow() {
        boolean z = false;
        if (!this.isRcsEnable) {
            return false;
        }
        if (this.isRcsEnable) {
            z = RcseMmsExt.isRcsMode();
        }
        return z;
    }
}
