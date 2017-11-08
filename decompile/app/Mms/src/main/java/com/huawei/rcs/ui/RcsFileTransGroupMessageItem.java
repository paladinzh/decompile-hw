package com.huawei.rcs.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.util.VcardMessageHelper;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter.GroupMessageColumn;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.telephony.RcseTelephonyExt.RcsAttachments;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;

public class RcsFileTransGroupMessageItem implements RcsGetFileTransInfo {
    private static RcsImageCache mCache;
    public int mChatType;
    final Context mContext;
    public Bitmap mFileIcon;
    public long mFileTransId;
    public int mFileTransType;
    private RcsGetFileTransInfo mGetFileTransInfo;
    public String mImAttachmentContent;
    public String mImAttachmentContentType;
    public String mImAttachmentGlobalTransId;
    public String mImAttachmentPath;
    public int mImAttachmentStatus;
    public long mImAttachmentTotalSize;
    public long mImAttachmentTransSize;
    public String mImAttatchmentIcon;
    private boolean mIsDelayMsg;
    public boolean mIsOutgoing = false;
    public int mLoadType = 0;
    public final long mMsgId;
    public RcsVCardInfo mVcardInfo = null;
    public int mtype;

    public static RcsImageCache getmCache() {
        return mCache;
    }

    public static void setmCache(RcsImageCache cache) {
        mCache = cache;
    }

    public RcsFileTransGroupMessageItem(Context context, Cursor cursor, ColumnsMap columnsMap, boolean isScroll, int chatType, int loadType) throws MmsException {
        this.mContext = context;
        this.mLoadType = loadType;
        if (1 == loadType) {
            this.mtype = cursor.getInt(cursor.getColumnIndex(NumberInfo.TYPE_KEY));
            this.mMsgId = cursor.getLong(cursor.getColumnIndex("origin_id"));
            setOutGoingStatus(chatType, cursor);
        } else {
            this.mtype = cursor.getInt(columnsMap.mColumnMsgType);
            this.mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
        }
        this.mChatType = chatType;
        this.mGetFileTransInfo = this;
        this.mGetFileTransInfo.createAttachmentForRcse(context, this.mMsgId, isScroll);
    }

    private void setOutGoingStatus(int chatType, Cursor cursor) {
        if (!(101 == chatType && 100 == this.mtype) && (100 != chatType || this.mtype == 1 || this.mtype == 0)) {
            if (102 != chatType) {
                return;
            }
        }
        this.mIsOutgoing = true;
    }

    public RcsFileTransGroupMessageItem(Context context, Cursor cursor, GroupMessageColumn columnsMap, boolean isScroll, int chatType) throws MmsException {
        this.mContext = context;
        this.mGetFileTransInfo = this;
        this.mMsgId = cursor.getLong(columnsMap.columnMessageID);
        this.mtype = cursor.getInt(columnsMap.columnType);
        if (this.mtype == 101) {
            this.mIsOutgoing = false;
        }
        if (this.mtype == 100) {
            this.mIsOutgoing = true;
        }
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "RcsFileTransGroupMessageItem fileTrans mtype=" + this.mtype + " mMsgId " + this.mMsgId + " mIsOutgoing = " + this.mIsOutgoing);
        this.mChatType = chatType;
        this.mGetFileTransInfo.createAttachmentForRcse(context, this.mMsgId, isScroll);
    }

    public void createAttachmentForRcse(Context aContext, long msgId, boolean isScroll) {
        Cursor aCursor = RcsAttachments.query(aContext.getContentResolver(), null, "msg_id = " + msgId + " and " + "chat_type" + " = " + this.mChatType);
        if (aCursor == null) {
            MLog.w("RcsFileTransGroupMessageItem FileTrans: ", "createAttachmentForRcse,but aCursor is null");
        }
        if (aCursor != null) {
            try {
                if (aCursor.moveToFirst()) {
                    this.mImAttachmentStatus = aCursor.getInt(aCursor.getColumnIndex("transfer_status"));
                    this.mImAttachmentPath = aCursor.getString(aCursor.getColumnIndex("file_content"));
                    this.mFileTransId = (long) aCursor.getInt(aCursor.getColumnIndex("_id"));
                    this.mImAttachmentContent = aCursor.getString(aCursor.getColumnIndex("file_content"));
                    this.mImAttachmentContentType = aCursor.getString(aCursor.getColumnIndex("file_type"));
                    this.mImAttachmentTransSize = aCursor.getLong(aCursor.getColumnIndex("trans_size"));
                    this.mImAttachmentTotalSize = aCursor.getLong(aCursor.getColumnIndex("file_size"));
                    this.mImAttatchmentIcon = aCursor.getString(aCursor.getColumnIndex("file_icon"));
                    this.mImAttachmentGlobalTransId = aCursor.getString(aCursor.getColumnIndex("global_trans_id"));
                    if (mCache != null) {
                        this.mFileIcon = mCache.getBitmapFromMemCache(RcsUtility.getBitmapFromMemCacheKey(this.mMsgId, this.mChatType));
                        if (this.mFileIcon != null) {
                            MLog.i("RcsFileTransGroupMessageItem FileTrans: ", " createAttachmentForRcse -> We found a cached image in cache.");
                        }
                    }
                    if (!isScroll) {
                        createFileIcon();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (aCursor != null) {
                    aCursor.close();
                }
            } catch (Throwable th) {
                if (aCursor != null) {
                    aCursor.close();
                }
            }
        }
        if (aCursor != null) {
            aCursor.close();
        }
        if (this.mFileTransType == 0) {
            setFileTransType();
        }
    }

    public void createFileIcon() {
        if (this.mFileIcon == null) {
            if (mCache != null) {
                this.mFileIcon = mCache.getBitmapFromMemCache(RcsUtility.getBitmapFromMemCacheKey(this.mMsgId, this.mChatType));
                if (this.mFileIcon != null) {
                    MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "createFileIcon -> We found a cached image in cache.");
                    return;
                }
            }
            if (!this.mIsOutgoing && !TextUtils.isEmpty(this.mImAttatchmentIcon) && new File(this.mImAttatchmentIcon).exists()) {
                MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "createFileIcon -> msgId = " + this.mMsgId + " Pre_ThumbNail_mode.");
            } else if (((!this.mIsOutgoing && (this.mImAttachmentStatus == 1002 || this.mImAttachmentStatus == Place.TYPE_ROUTE)) || this.mIsOutgoing) && !TextUtils.isEmpty(this.mImAttachmentPath) && new File(this.mImAttachmentPath).exists()) {
                MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "createFileIcon ->  we receive ok or we send.");
            }
        }
    }

    private void setFileTransType() {
        if (this.mImAttachmentPath != null) {
            MediaFileType fileType = RcsMediaFileUtils.getFileType(this.mImAttachmentPath);
            if (fileType != null) {
                if (RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                    int i;
                    if (RcsMediaFileUtils.isSpecialFile(this.mImAttachmentPath, "amr")) {
                        i = 9;
                    } else {
                        i = this.mFileTransType;
                    }
                    this.mFileTransType = i;
                } else if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                    this.mFileTransType = 7;
                } else if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                    this.mFileTransType = 8;
                } else if (RcsMediaFileUtils.isVCardFileType(fileType.fileType)) {
                    this.mFileTransType = 10;
                } else if (RcsMediaFileUtils.isVCalendarFileType(fileType.fileType)) {
                    this.mFileTransType = 11;
                }
            }
        }
    }

    public boolean isVoiceMessage() {
        return false;
    }

    public boolean isImage() {
        return this.mFileTransType == 7;
    }

    public boolean isVideo() {
        return this.mFileTransType == 8;
    }

    public Uri getFileUri() {
        File file = new File(this.mImAttachmentPath);
        if (file.exists()) {
            return Uri.fromFile(file);
        }
        return null;
    }

    public File getAttachmentFile() {
        if (this.mImAttachmentPath == null) {
            return null;
        }
        File file = new File(this.mImAttachmentPath);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public boolean isLocation() {
        return "location/position".equals(this.mImAttachmentContentType);
    }

    public void saveVcard() {
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "saveVcard");
        if (this.mFileTransType == 10) {
            VcardMessageHelper vCardMessageHelper = createIMVcardMessageHelper();
            if (vCardMessageHelper != null) {
                vCardMessageHelper.saveVcard();
            }
        }
    }

    private VcardMessageHelper createIMVcardMessageHelper() {
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "getVcardDetail createIMVcardMessageHelper");
        if (this.mVcardInfo != null) {
            return this.mVcardInfo.getVcardMessageHelper();
        }
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "getVcardDetail mVcardInfo is null, create a new one");
        if (createVcardParsingModule()) {
            return this.mVcardInfo.getVcardMessageHelper();
        }
        MLog.w("RcsFileTransGroupMessageItem FileTrans: ", "createIMVcardMessageHelper is null");
        return null;
    }

    public boolean createVcardParsingModule() {
        if (TextUtils.isEmpty(this.mImAttachmentPath) || this.mContext == null) {
            MLog.w("RcsFileTransGroupMessageItem FileTrans: ", "createVcardParsingModule mImAttachmentPath is null or mContext is null");
            return false;
        }
        File file = new File(this.mImAttachmentPath);
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            try {
                if (RcsCommonConfig.isRCSSwitchOn() && this.mVcardInfo == null) {
                    this.mVcardInfo = new RcsVCardInfo(this.mContext, uri);
                }
                if (this.mVcardInfo != null) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                MLog.e("RcsFileTransGroupMessageItem FileTrans: ", "createVcardParsingModule failed happen");
            }
            return false;
        }
        MLog.w("RcsFileTransGroupMessageItem FileTrans: ", "createVcardParsingModule file is not exist happen");
        return false;
    }

    public void showVCardDetailDialog() {
        if (this.mVcardInfo != null) {
            this.mVcardInfo.showVcardDetail();
            return;
        }
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "showVCardDetailDialog: msgItem.mVcardInfo is null");
        if (createVcardParsingModule()) {
            this.mVcardInfo.showVcardDetail();
            return;
        }
        MLog.i("RcsFileTransGroupMessageItem FileTrans: ", "showVCardDetailDialog: create mVcardInfo failed");
        Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
    }

    public boolean isVCardFileTypeMsg() {
        return this.mFileTransType == 10;
    }

    public boolean isImageFileType() {
        return this.mFileTransType == 7;
    }

    public boolean isVideoFileType() {
        return this.mFileTransType == 8;
    }

    public boolean isAudioFileType() {
        return this.mFileTransType == 9;
    }

    public void setMsgDelay(boolean msgDelay) {
        this.mIsDelayMsg = msgDelay;
    }

    public boolean isDelayMsg() {
        return this.mIsDelayMsg;
    }
}
