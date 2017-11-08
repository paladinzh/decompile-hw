package com.android.mms.ui.views;

import android.view.View;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.ui.views.ComposeRecipientsView.IRecipientsHoler;
import com.huawei.mms.ui.HwRecipientsEditor;

public class HwCustComposeRecipientsViewImpl extends HwCustComposeRecipientsView {
    private int mRecipientCountOld = 0;

    public boolean getIsTitleChangeWhenRecepientsChange() {
        return HwCustMmsConfigImpl.getIsTitleChangeWhenRecepientsChange();
    }

    public void checkUpdateTitle(HwRecipientsEditor mRecipientsEditor, int count, IRecipientsHoler mHolder) {
        int length = mRecipientsEditor.getRecipientCount();
        if (count == -1 || this.mRecipientCountOld != length) {
            mHolder.updateTitle(mRecipientsEditor.constructContactsFromInput(false));
            this.mRecipientCountOld = length;
        }
    }

    public void updateTitle(IRecipientsHoler mHolder, View v) {
        mHolder.updateTitle(((HwRecipientsEditor) v).constructContactsFromInput(false));
    }
}
