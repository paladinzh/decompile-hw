package com.android.mms.ui;

import android.content.Context;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AttachmentTypeSelectorAdapter.AttachmentListItem;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwCustAttachmentTypeSelectorAdapterImpl extends HwCustAttachmentTypeSelectorAdapter {
    private static final String TAG = "HwCustAttachmentTypeSelectorAdapterImpl";

    public void removeAdapterOptions(Context context, List<IconListItem> data) {
        ArrayList<String> itemTitlesToBeRemoved = new ArrayList();
        if (!HwCustMmsConfigImpl.allowSubject()) {
            itemTitlesToBeRemoved.add(context.getString(R.string.subject_hint));
        }
        synchronized (data) {
            Iterator<IconListItem> mIterator = data.iterator();
            while (mIterator.hasNext()) {
                if (itemTitlesToBeRemoved.contains(((IconListItem) mIterator.next()).getTitle())) {
                    mIterator.remove();
                }
            }
        }
    }

    public void addSubjectForSimpleUi(Context context, List<IconListItem> data) {
        if (MmsConfig.isInSimpleUI() && HwCustMmsConfigImpl.getAddSubject()) {
            addItem(data, context.getString(R.string.subject_hint), R.drawable.ic_attach_add_subject, 13);
            addItem(data, "", 0, -1);
            addItem(data, "", 0, -1);
        }
    }

    public static void addItem(List<IconListItem> data, String title, int resource, int command) {
        data.add(new AttachmentListItem(title, title, resource, command));
    }

    public boolean hideAllowAttachRecordSound() {
        if (HwCustMmsConfigImpl.isAllowAttachRecordSound()) {
            return true;
        }
        return false;
    }
}
