package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.model.SlideshowModel;
import com.android.mms.util.HwCustUiUtils;
import com.google.android.gms.R;
import com.huawei.mms.ui.EmuiMenu;

public class HwCustSlideSmootShowFragmentImpl extends HwCustSlideSmootShowFragment {
    Context mContext;

    public HwCustSlideSmootShowFragmentImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public void showToastInSlideshowWithVcardOrVcal(SlideshowModel model, int location) {
        if (!HwCustMmsConfigImpl.getEnableToastInSlideshowWithVcardOrVcal()) {
            return;
        }
        if (model.get(location).getVcard() != null || model.get(location).getVCalendar() != null) {
            Toast.makeText(this.mContext, R.string.unsupported_mediatype_slideshow, 0).show();
        }
    }

    public String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        return HwCustUiUtils.updateForwardSubject(aFwdSubject, aMsgSubject);
    }

    public void prepareReplyMenu(EmuiMenu aOptionMenu, int drawableId, MessageItem aMsgItem) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aOptionMenu != null && aMsgItem != null) {
            aOptionMenu.addMenu(278925337, R.string.menu_reply, drawableId);
            if (1 == aMsgItem.mBoxId) {
                aOptionMenu.setItemVisible(278925337, true);
            } else {
                aOptionMenu.setItemVisible(278925337, false);
            }
        }
    }

    public void handleReplyMenu(MessageItem aMsgItem) {
        if (HwCustMmsConfigImpl.supportReplyInGroupMessage() && aMsgItem != null) {
            Intent intent = new Intent();
            intent.setData(Uri.parse(aMsgItem.mAddress));
            intent.setClassName(this.mContext, "com.android.mms.ui.ComposeMessageActivity");
            this.mContext.startActivity(intent);
        }
    }
}
