package com.huawei.rcs.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class HwRcsCommonObject {
    public static final String ACTION_RESTORE_RCS_MESSAGE = "com.huawei.rcs.service.restore";
    public static final String ACTION_RESTORE_RCS_MESSAGE_BATCH = "com.huawei.rcs.service.restore_batch";
    public static final int BLACKLIST_MSG_AUDIO_TYPE = 32;
    public static final int BLACKLIST_MSG_CHAT_TYPE_MASK = 15;
    public static final int BLACKLIST_MSG_FILE_TYPE = 80;
    public static final int BLACKLIST_MSG_FILE_TYPE_MASK = 4080;
    public static final int BLACKLIST_MSG_IMAGE_TYPE = 16;
    public static final int BLACKLIST_MSG_LOCATION_TYPE = 96;
    public static final int BLACKLIST_MSG_TYPE_GROUP_CHAT = 2;
    public static final int BLACKLIST_MSG_TYPE_SINGLE_CHAT = 1;
    public static final int BLACKLIST_MSG_VCARD_TYPE = 64;
    public static final int BLACKLIST_MSG_VIDEO_TYPE = 48;
    public static final String INTENT_KEY_GROUP_MSG_NAME = "group_message_name";
    public static final String INTENT_KEY_GROUP_MSG_NAME_BATCH = "group_message_name_batch";
    public static final String INTENT_KEY_MSG_ID = "message_id";
    public static final String INTENT_KEY_MSG_ID_BATCH = "message_id_batch";
    public static final String INTENT_KEY_MSG_TYPE = "message_type";
    public static final String INTENT_KEY_MSG_TYPE_BATCH = "message_type_batch";
    public static final int OPERATE_TYPE_DELETE = 3;
    public static final int OPERATE_TYPE_INSERT = 1;
    public static final int OPERATE_TYPE_UPDATE = 2;
    public static final int ORIGINAL_MSG_TYPE = 0;
    public static final int RCS_MSG_TYPE_GROUP = 2;
    public static final int RCS_MSG_TYPE_IM = 1;
    public static final int RCS_MSG_TYPE_INVALID = 0;
    public static final int TABLE_CHAT = 1;
    public static final int TABLE_RCS_GROUPS = 200;
    public static final int TABLE_RCS_GROUP_MEMBERS = 210;
    public static final int TABLE_RCS_GROUP_MESSAGE = 220;
    public static final Uri rcs_message_file_uri = Uri.parse("content://com.huawei.rcs.joyn/file");
    public static final Uri rcs_message_sms_uri = Uri.parse("content://com.huawei.rcs.joyn/chat");

    public static class FileRcsExtColumns {
        private String fileImage;
        private String fileName;
        private long fileSize;
        private int fileType;

        public FileRcsExtColumns(String fileName, long fileSize, int fileType, String fileImage) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.fileImage = fileImage;
        }

        public String getFileImage() {
            return this.fileImage;
        }

        public void setFileImage(String fileImage) {
            this.fileImage = fileImage;
        }

        public int getFileType() {
            return this.fileType;
        }

        public void setFileType(int fileType) {
            this.fileType = fileType;
        }

        public String getFileName() {
            return this.fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getFileSize() {
            return this.fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String formatInfo(Context context) {
            String info = "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_file) + "]";
            switch (this.fileType) {
                case 16:
                    return "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_image) + "]";
                case 32:
                    return "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_audio) + "]";
                case HwRcsCommonObject.BLACKLIST_MSG_VIDEO_TYPE /*48*/:
                    return "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_video) + "]";
                case 64:
                    return "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_vcard) + "]";
                case 96:
                    return "[" + context.getResources().getString(R.string.rcs_file_trans_msg_type_loc) + "]";
                default:
                    return info;
            }
        }

        public String formatLocation(String locationBody) {
            try {
                if (!TextUtils.isEmpty(locationBody)) {
                    return new JSONObject(locationBody).get(TbMessages.BODY).toString();
                }
            } catch (JSONException e) {
                HwLog.e("HwRcsCommonObject", "JSON error");
            } catch (Exception e2) {
                HwLog.e("HwRcsCommonObject", "formatLocation error");
            }
            return locationBody;
        }
    }

    public static class ImIntentWrapper {
        private Intent mImIntent;
        private int mmsgType;
        private long mmsgid;

        public ImIntentWrapper(long msgid, int msgType, Intent ImIntent) {
            this.mmsgid = msgid;
            this.mImIntent = ImIntent;
            this.mmsgType = msgType;
        }

        public void setImMsgId(long msgid) {
            this.mmsgid = msgid;
        }

        public void setImIntent(Intent ImIntent) {
            this.mImIntent = ImIntent;
        }

        public void setImMsgType(int msgType) {
            this.mmsgType = msgType;
        }

        public long getImMsgId() {
            return this.mmsgid;
        }

        public Intent getImIntent() {
            return this.mImIntent;
        }

        public int getImMsgType() {
            return this.mmsgType;
        }
    }

    public static class RcsExtendColumn {
        private String mGroupMessageName = null;
        private long mMessageId = -1;
        private int mMessageType = 0;

        public void setMessageType(int messageType) {
            this.mMessageType = messageType;
        }

        public int getMessageType() {
            return this.mMessageType;
        }

        public void setMessageId(long messageId) {
            this.mMessageId = messageId;
        }

        public long getMessageId() {
            return this.mMessageId;
        }

        public void setGroupMessageName(String groupMessageName) {
            this.mGroupMessageName = groupMessageName;
        }

        public String getGroupMessageName() {
            return this.mGroupMessageName;
        }
    }
}
