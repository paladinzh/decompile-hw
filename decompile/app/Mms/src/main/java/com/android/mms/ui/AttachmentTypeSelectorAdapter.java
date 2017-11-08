package com.android.mms.ui;

import android.content.Context;
import com.android.mms.MmsConfig;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.android.rcs.ui.RcsAttachmentTypeSelectorAdapter;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class AttachmentTypeSelectorAdapter extends IconListAdapter {
    private static HwCustAttachmentTypeSelectorAdapter mHwCustAttachmentTypeSelectorAdapter = ((HwCustAttachmentTypeSelectorAdapter) HwCustUtils.createObj(HwCustAttachmentTypeSelectorAdapter.class, new Object[0]));
    private static RcsAttachmentTypeSelectorAdapter mRcsAttachmentTypeSelectorAdapter = new RcsAttachmentTypeSelectorAdapter();

    public static class AttachmentListItem extends IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, String contentDesCription, int resource, int command) {
            super(title, contentDesCription, resource);
            this.mCommand = command;
        }

        public int getCommand() {
            return this.mCommand;
        }
    }

    public AttachmentTypeSelectorAdapter(Context context, int mode, int resourceId) {
        super(context, resourceId, getData(mode, context));
    }

    public AttachmentTypeSelectorAdapter(Context context, int resourceId, List<IconListItem> date) {
        super(context, resourceId, date);
    }

    protected static List<IconListItem> getData(int mode, Context context) {
        List<IconListItem> data = new ArrayList(7);
        addItem(data, context.getString(R.string.attach_Smiley), R.drawable.icon_media_expression, 0);
        addItem(data, context.getString(R.string.attach_take_photo), R.drawable.ic_attach_capture_picture, 1);
        addItem(data, context.getString(R.string.attach_image), R.drawable.ic_attach_picture_selector, 2);
        if (mHwCustAttachmentTypeSelectorAdapter != null) {
            mHwCustAttachmentTypeSelectorAdapter.addSubjectForSimpleUi(context, data);
        }
        if (!MmsConfig.isInSimpleUI()) {
            addItem(data, context.getString(R.string.attach_contacts), R.drawable.icon_media_card, 8);
            if (mRcsAttachmentTypeSelectorAdapter == null || !mRcsAttachmentTypeSelectorAdapter.isImModeNow()) {
                addItem(data, context.getString(R.string.subject_hint), R.drawable.ic_attach_add_subject, 13);
            }
            addItem(data, context.getString(R.string.attach_phrases), R.drawable.icon_media_phrase, 7);
            addItem(data, context.getString(R.string.attach_video), R.drawable.ic_attach_add_video, 4);
            addItem(data, context.getString(R.string.attach_capture_video), R.drawable.ic_attach_capture_video, 3);
            if (MmsConfig.getSupportedVCalendarEnabled()) {
                addItem(data, context.getString(R.string.vcalendar_calendar), R.drawable.ic_launcher_calendar, 10);
            }
            if (MmsConfig.getAllowAttachAudio()) {
                addItem(data, context.getString(R.string.attach_sound), R.drawable.ic_attach_audio_holo_light, 6);
            }
            addItem(data, context.getString(R.string.attach_record_sound), R.drawable.ic_attach_capture_audio, 5);
            if (mRcsAttachmentTypeSelectorAdapter != null) {
                mRcsAttachmentTypeSelectorAdapter.addExtItem(data, context);
            }
            if (mode != 0) {
                addItem(data, "", 0, -1);
            } else if (mRcsAttachmentTypeSelectorAdapter == null || !mRcsAttachmentTypeSelectorAdapter.isImModeNow()) {
                addItem(data, context.getString(R.string.slide_options), R.drawable.ic_attach_slideshow_holo_light, 14);
            }
            if (!MmsConfig.getSupportedVCalendarEnabled()) {
                addItem(data, "", 0, -1);
            }
            if (!MmsConfig.getAllowAttachAudio()) {
                addItem(data, "", 0, -1);
            }
        }
        int sizeOfDataBeforeRemove = data.size();
        if (mHwCustAttachmentTypeSelectorAdapter != null) {
            mHwCustAttachmentTypeSelectorAdapter.removeAdapterOptions(context, data);
        }
        for (int sizeOfDataAfterRemove = data.size(); sizeOfDataAfterRemove < sizeOfDataBeforeRemove; sizeOfDataAfterRemove++) {
            addItem(data, "", 0, -1);
        }
        return data;
    }

    public static void addItem(List<IconListItem> data, String title, int resource, int command) {
        data.add(new AttachmentListItem(title, title, resource, command));
    }

    public static void addItem(List<IconListItem> data, String title, String contentDesCription, int resource, int command) {
        data.add(new AttachmentListItem(title, contentDesCription, resource, command));
    }
}
