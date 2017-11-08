package com.android.mms.data;

import android.content.Context;
import android.database.Cursor;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsConfig;
import com.android.mms.util.HwCustUiUtils;
import com.google.android.gms.R;

public class HwCustConversationImpl extends HwCustConversation {
    private static final String TAG = "HwCustConversationImpl";
    public static final String THREADS_COLLOMN_THUMB_NAIL = "thumbnail";
    private String mThumbnailPath;

    public void setThumanailPath(Cursor aCursor) {
        if (HwCustUiUtils.THUMBNAIL_SUPPORT) {
            int lColumIndex = aCursor.getColumnIndex(THREADS_COLLOMN_THUMB_NAIL);
            if (-1 != lColumIndex) {
                this.mThumbnailPath = aCursor.getString(lColumIndex);
            }
        }
    }

    public String getThumbnailPath() {
        return this.mThumbnailPath;
    }

    public String[] getAllThreadsProjection(String[] aAllThreadsProjection) {
        if (!HwCustUiUtils.THUMBNAIL_SUPPORT) {
            return aAllThreadsProjection;
        }
        String[] lAllThreadsProjection = new String[(aAllThreadsProjection.length + 1)];
        for (int i = 0; i < aAllThreadsProjection.length; i++) {
            lAllThreadsProjection[i] = aAllThreadsProjection[i];
        }
        lAllThreadsProjection[aAllThreadsProjection.length] = THREADS_COLLOMN_THUMB_NAIL;
        return lAllThreadsProjection;
    }

    public String getDefaultEmptySubject(Context aContext, String aSubject) {
        if (HwCustMmsConfigImpl.allowSubject() || HwCustMmsConfigImpl.supportEmptyFWDSubject() || MmsConfig.getSendingBlankSMSEnabled() || aContext == null) {
            return aSubject;
        }
        return aContext.getString(R.string.multimedia_message);
    }
}
