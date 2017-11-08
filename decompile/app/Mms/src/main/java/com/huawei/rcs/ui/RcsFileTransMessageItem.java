package com.huawei.rcs.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.util.VcardMessageHelper;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.telephony.RcseTelephonyExt.RcsAttachments;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.util.regex.Pattern;

public class RcsFileTransMessageItem extends MessageItem {
    private boolean isFailedFileTransMessage = false;
    private RcsImageCache mCache;
    public int mChatType;
    final Context mContext;
    public String mFileAddress;
    public Bitmap mFileIcon;
    public long mFileTransId;
    public int mFileTransType;
    public String mImAttachmentContent;
    public String mImAttachmentContentType;
    public String mImAttachmentGlobalTransId;
    public String mImAttachmentPath;
    public int mImAttachmentStatus;
    public long mImAttachmentTotalSize;
    public long mImAttachmentTransSize;
    public String mImAttatchmentIcon;
    public boolean mIsOutgoing = false;
    public int mLoadType = 0;
    final long mRcsMsgType;
    public RcsVCardInfo mVcardInfo = null;

    public RcsFileTransMessageItem(Context context, String type, Cursor cursor, ColumnsMap columnsMap, Pattern highlight, boolean isScroll, boolean isGroupCov) throws MmsException {
        super(context, type, cursor, columnsMap, highlight);
        this.mContext = context;
        this.mCache = RcsImageCache.getInstance(((Activity) this.mContext).getFragmentManager(), context);
        this.mFileAddress = this.mAddress;
        this.mMsgId = cursor.getLong(columnsMap.mColumnMsgId);
        this.mRcsMsgType = (long) RcsProfileUtils.getRcsMsgType(cursor);
        if (this.mRcsMsgType == 4) {
            setLocationData(this.mMsgId);
        }
        int smsType = cursor.getInt(columnsMap.mColumnSmsType);
        if (!(smsType == 1 || smsType == 0)) {
            this.mIsOutgoing = true;
        }
        if (isGroupCov) {
            this.mChatType = 3;
        } else {
            this.mChatType = 1;
        }
        createAttachmentForRcse(context, isScroll);
        if (!(this.mImAttachmentStatus == 1009 || this.mImAttachmentStatus == 1010)) {
            if (this.mImAttachmentStatus != 1001) {
                return;
            }
        }
        this.isFailedFileTransMessage = true;
    }

    public void createAttachmentForRcse(Context aContext, boolean isScroll) {
        Cursor aCursor = RcsAttachments.query(aContext.getContentResolver(), null, "msg_id = " + this.mMsgId + " AND " + "chat_type" + "=" + 1);
        if (aCursor == null) {
            MLog.w("RcsFileTransMessageItem FileTrans: ", "createAttachmentForRcse,but aCursor is null");
        }
        if (aCursor != null) {
            try {
                if (aCursor.moveToFirst()) {
                    this.mImAttachmentStatus = aCursor.getInt(aCursor.getColumnIndex("transfer_status"));
                    this.mImAttachmentPath = aCursor.getString(aCursor.getColumnIndex("file_content"));
                    this.mFileTransId = (long) aCursor.getInt(aCursor.getColumnIndex("_id"));
                    this.mImAttachmentContentType = aCursor.getString(aCursor.getColumnIndex("file_type"));
                    this.mImAttachmentTransSize = aCursor.getLong(aCursor.getColumnIndex("trans_size"));
                    this.mImAttachmentTotalSize = aCursor.getLong(aCursor.getColumnIndex("file_size"));
                    this.mImAttachmentContent = aCursor.getString(aCursor.getColumnIndex("file_content"));
                    this.mImAttatchmentIcon = aCursor.getString(aCursor.getColumnIndex("file_icon"));
                    this.mImAttachmentGlobalTransId = aCursor.getString(aCursor.getColumnIndex("global_trans_id"));
                    MLog.d("RcsFileTransMessageItem FileTrans: ", "createAttachmentForRcse -> mImAttachmentGlobalTransId = " + this.mImAttachmentGlobalTransId);
                    if (this.mCache != null) {
                        this.mFileIcon = this.mCache.getBitmapFromMemCache(RcsUtility.getBitmapFromMemCacheKey(this.mMsgId, this.mChatType));
                        if (this.mFileIcon != null) {
                            MLog.i("RcsFileTransMessageItem FileTrans: ", " createAttachmentForRcse -> We found a cached image in cache.");
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
        setFileTransType();
    }

    public void createFileIcon() {
        if (this.mFileIcon == null) {
            if (this.mCache != null) {
                this.mFileIcon = this.mCache.getBitmapFromMemCache(RcsUtility.getBitmapFromMemCacheKey(this.mMsgId, this.mChatType));
                if (this.mFileIcon != null) {
                    MLog.i("RcsFileTransMessageItem FileTrans: ", " createFileIcon -> We found a cached image in cache.");
                    return;
                }
            }
            if (!this.mIsOutgoing && !TextUtils.isEmpty(this.mImAttatchmentIcon) && new File(this.mImAttatchmentIcon).exists()) {
                MLog.i("RcsFileTransMessageItem FileTrans: ", "createFileIcon -> msgId = " + this.mMsgId + " Pre_ThumbNail_mode" + "Icon_Path=" + this.mImAttatchmentIcon);
            } else if (((!this.mIsOutgoing && this.mImAttachmentStatus == 1002) || this.mImAttachmentStatus == Place.TYPE_ROUTE || this.mIsOutgoing) && !TextUtils.isEmpty(this.mImAttachmentPath) && new File(this.mImAttachmentPath).exists()) {
                MLog.i("RcsFileTransMessageItem FileTrans: ", "createFileIcon -> msgId = " + this.mMsgId + " we receive ok or we send.");
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

    public boolean isLocation() {
        return "location/position".equals(this.mImAttachmentContentType);
    }

    public boolean isVCardFile() {
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

    public boolean isFTOutGoingMessage() {
        return this.mIsOutgoing;
    }

    public boolean createVcardParsingModule() {
        if (this.mImAttachmentPath == null || this.mContext == null) {
            MLog.w("RcsFileTransMessageItem FileTrans: ", "createVcardParsingModule mImAttachmentPath is null");
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
                MLog.e("RcsFileTransMessageItem FileTrans: ", "createVcardParsingModule failed");
            }
            return false;
        }
        MLog.w("RcsFileTransMessageItem FileTrans: ", "createVcardParsingModule file is not exist");
        return false;
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

    public Uri getFileUri() {
        File file = new File(this.mImAttachmentPath);
        if (file.exists()) {
            return Uri.fromFile(file);
        }
        return null;
    }

    public void setLocationData(long msgId) {
    }

    public boolean isFailedFileTransMessage() {
        return this.isFailedFileTransMessage;
    }

    public void setFailedFileTransMessage(boolean isFailedFileTransMessage) {
        this.isFailedFileTransMessage = isFailedFileTransMessage;
    }

    public String[] getVcardDetail() {
        if (this.mVcardInfo == null || this.mVcardInfo.getVcardMessageHelper() == null) {
            return new String[0];
        }
        return this.mVcardInfo.getVcardMessageHelper().getVcardDetail();
    }

    public void saveVcard() {
        MLog.i("RcsFileTransMessageItem FileTrans: ", "saveVcard");
        VcardMessageHelper vCardMessageHelper = null;
        if (isVCardFile()) {
            vCardMessageHelper = createIMVcardMessageHelper();
        }
        if (vCardMessageHelper != null) {
            vCardMessageHelper.saveVcard();
        }
    }

    private VcardMessageHelper createIMVcardMessageHelper() {
        MLog.i("RcsFileTransMessageItem FileTrans: ", "getVcardDetail createIMVcardMessageHelper");
        if (this.mVcardInfo != null) {
            return this.mVcardInfo.getVcardMessageHelper();
        }
        MLog.i("RcsFileTransMessageItem FileTrans: ", "getVcardDetail VcardMessageHelper is null, create a new one");
        if (createVcardParsingModule()) {
            return this.mVcardInfo.getVcardMessageHelper();
        }
        MLog.w("RcsFileTransMessageItem FileTrans: ", "createIMVcardMessageHelper is null");
        return null;
    }

    public void showVCardDetailDialog() {
        if (this.mVcardInfo != null) {
            this.mVcardInfo.showVcardDetail();
            return;
        }
        MLog.i("RcsFileTransMessageItem FileTrans: ", "showVCardDetailDialog: msgItem.mVcardInfo is null");
        if (createVcardParsingModule()) {
            this.mVcardInfo.showVcardDetail();
            return;
        }
        Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
        MLog.i("RcsFileTransMessageItem FileTrans: ", "mVcardInfo failed");
    }

    public RcsImageCache getImageCache() {
        return this.mCache;
    }
}
