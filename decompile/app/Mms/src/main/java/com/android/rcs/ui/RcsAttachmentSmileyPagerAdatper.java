package com.android.rcs.ui;

import android.content.Context;
import com.android.mms.MmsConfig;
import com.android.mms.ui.AttachmentTypeSelectorAdapter;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class RcsAttachmentSmileyPagerAdatper {
    private static boolean mIsGroup = false;
    private Context mContext;

    public RcsAttachmentSmileyPagerAdatper(Context context) {
        this.mContext = context;
    }

    public static void setIsGroup(boolean isGroup) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            mIsGroup = isGroup;
        }
    }

    public boolean getIsGroup() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return mIsGroup;
        }
        return false;
    }

    public AttachmentTypeSelectorAdapter getGroupAttachmentTypeSelectorAdapter(int resourceId) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            return new AttachmentTypeSelectorAdapter(this.mContext, resourceId, getGroupData());
        }
        return null;
    }

    private List<IconListItem> getGroupData() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return null;
        }
        List<IconListItem> data = new ArrayList(7);
        AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_Smiley), R.drawable.icon_media_expression, 0);
        AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_take_photo), R.drawable.ic_attach_capture_picture, 1);
        AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_image), R.drawable.ic_attach_picture_selector, 2);
        if (!MmsConfig.isInSimpleUI()) {
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_contacts), R.drawable.icon_media_card, 8);
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_phrases), R.drawable.icon_media_phrase, 7);
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_video), R.drawable.ic_attach_add_video, 4);
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_capture_video), R.drawable.ic_attach_capture_video, 3);
            if (MmsConfig.getSupportedVCalendarEnabled()) {
                AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.vcalendar_calendar), R.drawable.ic_launcher_calendar, 10);
            }
            if (MmsConfig.getAllowAttachAudio()) {
                AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_sound), R.drawable.ic_attach_audio_holo_light, 6);
            }
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_record_sound), R.drawable.ic_attach_capture_audio, 5);
            AttachmentTypeSelectorAdapter.addItem(data, this.mContext.getString(R.string.attach_anyfile), R.drawable.rcs_ic_attach_anyfile_holo_light, 23);
            AttachmentTypeSelectorAdapter.addItem(data, "", -1, 25);
        }
        return data;
    }
}
