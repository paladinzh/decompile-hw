package com.android.mms.attachment.datamodel.media;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import cn.com.xy.sms.sdk.ui.popu.util.ViewPartId;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ResolutionException;
import com.android.mms.TempFileProvider;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.attachment.datamodel.control.AttachmentSelectLocationControl;
import com.android.mms.data.WorkingMessage;
import com.android.mms.model.AudioModel;
import com.android.mms.model.CarrierContentRestriction;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.VCalendarModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VideoModel;
import com.android.mms.model.control.ImageModelControl;
import com.android.mms.model.control.MediaModelControl;
import com.android.mms.ui.UriImage;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.DocumentsUIUtil;
import com.huawei.rcs.utils.RcseMmsExt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class AttachmentThreadManager {
    private static Object mLock = new Object();
    private static Object mPartLock = new Object();
    private static final HashMap<Uri, AttachmentThread> mRunningThreads = new HashMap();
    private static final ArrayList<AttachmentThread> mWaitThreads = new ArrayList();

    public static class AttachmentThread extends Thread {
        private AttachmentThreadCallBack attachmentCallback;
        private Context context;
        private boolean isSingleChat;
        private MediaModel mediaModel = null;
        private int position;
        private RcsGroupChatAttachmentThreadCallBack rcsGroupChatAttachmentThreadCallBack;
        private int type;
        private Uri uri;
        private WorkingMessage workingMessage;

        public AttachmentThread(Context context, int position, int type, Uri uri, WorkingMessage workingMessage, AttachmentThreadCallBack attachmentCallback) {
            this.context = context;
            this.position = position;
            this.type = type;
            this.workingMessage = workingMessage;
            this.attachmentCallback = attachmentCallback;
            this.uri = uri;
            this.isSingleChat = true;
        }

        public AttachmentThread(Context context, int type, Uri uri, RcsGroupChatAttachmentThreadCallBack rcsGroupChatAttachmentCallback) {
            this.context = context;
            this.type = type;
            this.rcsGroupChatAttachmentThreadCallBack = rcsGroupChatAttachmentCallback;
            this.uri = uri;
            this.isSingleChat = false;
        }

        public Uri getUri() {
            return this.uri;
        }

        public void run() {
            if (this.uri == null) {
                notifyResult(-1);
                return;
            }
            int result = 0;
            this.uri = DocumentsUIUtil.convertUri(this.context, this.uri);
            if (this.uri == null) {
                notifyResult(-1);
                return;
            }
            MLog.e("AttachmentThreadManager", "AttachmentThread start change");
            MLog.e("AttachmentThreadManager", "AttachmentThread type:" + this.type);
            try {
                if (this.type == 2) {
                    MmsApp.getApplication().removeThumbnail(this.uri);
                    if (this.isSingleChat) {
                        this.mediaModel = changeImage(this.context, this.uri, this.workingMessage, false);
                    } else {
                        this.mediaModel = changeImage(this.context, this.uri, false);
                    }
                    notifyResult(result);
                } else if (this.type == 8) {
                    MmsApp.getApplication().removeThumbnail(this.uri);
                    if (this.isSingleChat) {
                        this.mediaModel = changeImage(this.context, this.uri, this.workingMessage, true);
                    } else {
                        this.mediaModel = changeImage(this.context, this.uri, true);
                    }
                    this.mediaModel.setLocation(true);
                    notifyResult(result);
                } else if (this.type == 5) {
                    if (this.isSingleChat) {
                        this.mediaModel = changeVideo(this.context, this.uri, this.workingMessage.getSlideTextRegion());
                    } else {
                        this.mediaModel = changeVideo(this.context, this.uri, null);
                    }
                    notifyResult(result);
                } else {
                    if (this.type == 3) {
                        this.mediaModel = changeAudio(this.context, this.uri);
                    } else if (this.type == 6) {
                        Uri lUri = changeToLocalUri(this.context, this.uri);
                        this.mediaModel = changeVCard(this.context, lUri);
                        if (lUri.equals(TempFileProvider.SCRAP_VCARD_URI)) {
                            try {
                                this.context.getContentResolver().openOutputStream(TempFileProvider.SCRAP_VCARD_URI);
                            } catch (FileNotFoundException e) {
                                MLog.e("AttachmentThreadManager", "addVcard FileNotFoundException" + e.getMessage());
                            }
                        }
                    } else if (this.type == 7) {
                        this.mediaModel = changeVCalendar(this.context, this.uri);
                    } else {
                        result = -3;
                    }
                    notifyResult(result);
                }
            } catch (MmsException e2) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e2);
                result = -1;
            } catch (ExceedMessageSizeException e3) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e3);
                result = -2;
            } catch (ResolutionException e4) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e4);
                result = -4;
            } catch (UnsupportContentTypeException e5) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e5);
                result = -3;
            } catch (IllegalArgumentException e6) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e6);
                result = -1;
            } catch (Exception e7) {
                MLog.e("AttachmentThreadManager", "AttachmentThread:", (Throwable) e7);
                result = -1;
            }
        }

        private void notifyResult(int resultCode) {
            AttachmentThreadManager.endAttachmentThread(this);
            if (this.attachmentCallback != null) {
                this.attachmentCallback.onModelFinish(resultCode, this.position, this.mediaModel, this.type);
            }
            if (this.rcsGroupChatAttachmentThreadCallBack != null) {
                this.rcsGroupChatAttachmentThreadCallBack.onModelFinish(resultCode, this.uri, this.mediaModel, this.type);
            }
        }

        public MediaModel changeImage(Context context, Uri newImage, boolean isLocation) throws MmsException {
            MediaModel mediaModel = null;
            if (newImage != null) {
                mediaModel = new ImageModel(context, newImage, null);
                MediaModelControl.setBuildResource(mediaModel, newImage.getPath());
            }
            if (isLocation && mediaModel != null) {
                MediaModelControl.setMediaModelLocationMap(mediaModel, AttachmentSelectLocationControl.parseLocationMap(context, "Mms_UI_GCCMF"));
            }
            return mediaModel;
        }

        public MediaModel changeImage(Context context, Uri newImage, WorkingMessage workingMessage, boolean isLocation) throws MmsException {
            MLog.e("AttachmentThreadManager", "changeImage method start");
            if (context == null || newImage == null || workingMessage == null) {
                return null;
            }
            ImageModel mImageModel;
            boolean isHeightRestrictedConfig;
            boolean isHeightLTRestrictedConfig;
            ImageModelControl imageModelControl = new ImageModelControl();
            RegionModel regionModel = workingMessage.getSlideTextRegion();
            int totalSize = workingMessage.getSlideCurrentSize();
            UriImage image = new UriImage(context, newImage);
            if (image.isGifImage()) {
                mImageModel = new ImageModel(context, newImage, regionModel);
            } else {
                mImageModel = new ImageModel(context, newImage, regionModel);
                totalSize = imageModelControl.addSize(mImageModel, totalSize);
                boolean overLimit = totalSize < 0 || totalSize > MmsConfig.getMaxMessageSizeCharge(4096);
                if ((!RcseMmsExt.isRcsMode() && overLimit) || isLocation) {
                    int widthLimit;
                    int heightLimit;
                    int sizeLimit;
                    if (MmsConfig.isCurrentRestrictedMode()) {
                        widthLimit = MmsConfig.getMaxRestrictedImageWidth();
                        heightLimit = MmsConfig.getMaxRestrictedImageHeight();
                        sizeLimit = CarrierContentRestriction.getRestricedModeType(image.getContentType()).intValue();
                    } else {
                        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
                        widthLimit = dm.widthPixels;
                        heightLimit = dm.heightPixels;
                        sizeLimit = MmsConfig.getMaxMessageSize() - 5000;
                    }
                    if (image.isLtUriImage()) {
                        int temp = widthLimit;
                        widthLimit = heightLimit;
                        heightLimit = temp;
                    }
                    PduPart part = image.getResizedImageAsPart(widthLimit, heightLimit, sizeLimit);
                    if (part == null) {
                        return null;
                    }
                    mImageModel = new ImageModel(context, AttachmentThreadManager.persisterPart(context, workingMessage, part, newImage, isLocation), regionModel);
                    if (isLocation) {
                        MediaModelControl.setMediaModelLocationMap(mImageModel, AttachmentSelectLocationControl.parseLocationMap(context, "Mms_UI_CMF"));
                    }
                }
            }
            MediaModelControl.setBuildResource(mImageModel, newImage.getPath());
            if (imageModelControl.isWidthRestrictedConfig(mImageModel)) {
                isHeightRestrictedConfig = imageModelControl.isHeightRestrictedConfig(mImageModel);
            } else {
                isHeightRestrictedConfig = false;
            }
            if (imageModelControl.isWidthLTRestrictedConfig(mImageModel)) {
                isHeightLTRestrictedConfig = imageModelControl.isHeightLTRestrictedConfig(mImageModel);
            } else {
                isHeightLTRestrictedConfig = false;
            }
            boolean exceedsbound = (isHeightRestrictedConfig || isHeightLTRestrictedConfig) ? false : true;
            if (MmsConfig.isGcfMms305Enabled() && exceedsbound && MmsConfig.isCurrentRestrictedMode()) {
                throw new UnsupportContentTypeException();
            }
            MLog.e("AttachmentThreadManager", "changeImage method end");
            return mImageModel;
        }

        public MediaModel changeAudio(Context context, Uri newAudio) throws MmsException {
            return new AudioModel(context, newAudio);
        }

        public MediaModel changeVideo(Context context, Uri newVideo, RegionModel regionModel) throws MmsException {
            VideoModel videoModel = new VideoModel(context, newVideo, regionModel);
            MediaModelControl.setBuildResource(videoModel, newVideo.getPath());
            return videoModel;
        }

        public MediaModel changeVCard(Context context, Uri uri) throws MmsException {
            return new VcardModel(context, "text/x-vCard", uri);
        }

        public MediaModel changeVCalendar(Context context, Uri uri) throws MmsException {
            if ("vcs".equalsIgnoreCase(getVCalendarExtendName(uri))) {
                return new VCalendarModel(context, "text/x-vCalendar", uri);
            }
            throw new UnsupportContentTypeException();
        }

        private String getVCalendarExtendName(Uri uri) {
            String extendName = "";
            String name = uri.getLastPathSegment();
            int index = name.lastIndexOf(".");
            if (index > 0) {
                return name.substring(index + 1);
            }
            return extendName;
        }

        private Uri changeToLocalUri(Context context, Uri vcardUri) {
            if (context == null || !vcardUri.toString().startsWith("content://com.android.contacts/contacts/as_multi_vcard_large")) {
                return vcardUri;
            }
            MLog.d("AttachmentThreadManager", "add vCard use mms temp file as interval storage");
            byte[] buf = new byte[ViewPartId.PART_BODY_SIMPLE_CALL_NUMBER];
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(vcardUri);
                outputStream = context.getContentResolver().openOutputStream(TempFileProvider.SCRAP_VCARD_URI);
                if (inputStream == null || outputStream == null) {
                    MLog.e("AttachmentThreadManager", "is == null || os == null");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e);
                            return vcardUri;
                        }
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    return vcardUri;
                }
                while (true) {
                    int nRead = inputStream.read(buf);
                    if (nRead < 0) {
                        break;
                    }
                    outputStream.write(buf, 0, nRead);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e2);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return TempFileProvider.SCRAP_VCARD_URI;
            } catch (FileNotFoundException e3) {
                MLog.e("AttachmentThreadManager", "addVcard FileNotFoundException" + e3.getMessage());
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e22);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return vcardUri;
            } catch (IOException e222) {
                MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2222) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e2222);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return vcardUri;
            } catch (NullPointerException e4) {
                MLog.e("AttachmentThreadManager", "addVcard NullPointerException", (Throwable) e4);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22222) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e22222);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return vcardUri;
            } catch (Exception e5) {
                MLog.e("AttachmentThreadManager", "addVcard Exception", (Throwable) e5);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222222) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e222222);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                return vcardUri;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2222222) {
                        MLog.e("AttachmentThreadManager", "addVcard IOException", (Throwable) e2222222);
                        return vcardUri;
                    }
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }

    public interface AttachmentThreadCallBack {
        void onModelFinish(int i, int i2, MediaModel mediaModel, int i3);
    }

    public interface RcsGroupChatAttachmentThreadCallBack {
        void onModelFinish(int i, Uri uri, MediaModel mediaModel, int i2);
    }

    public static boolean startAttachmentThread(AttachmentThread attachmentThread) {
        if (attachmentThread == null) {
            return false;
        }
        MLog.e("AttachmentThreadManager", "startAttachmentThread method start");
        synchronized (mLock) {
            MLog.e("AttachmentThreadManager", "startAttachmentThread get lock success");
            if (mRunningThreads.size() >= 5) {
                MLog.e("AttachmentThreadManager", "mRunningThreads size >= 5");
                mWaitThreads.add(attachmentThread);
                return true;
            } else if (attachmentThread.getUri() != null) {
                mRunningThreads.put(attachmentThread.getUri(), attachmentThread);
                MLog.e("AttachmentThreadManager", "attachmentThread start");
                attachmentThread.start();
                return true;
            } else {
                MLog.e("AttachmentThreadManager", "attachmentThread uri is null");
                return false;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean endAttachmentThread(AttachmentThread attachmentThread) {
        if (attachmentThread == null) {
            return false;
        }
        synchronized (mLock) {
            Uri uri = attachmentThread.getUri();
            if (uri != null && mRunningThreads.containsKey(uri)) {
                mRunningThreads.remove(uri);
                if (mWaitThreads.size() > 0) {
                    AttachmentThread waitThread = (AttachmentThread) mWaitThreads.remove(0);
                    if (!(waitThread == null || waitThread.getUri() == null)) {
                        mRunningThreads.put(waitThread.getUri(), waitThread);
                        waitThread.start();
                        return true;
                    }
                }
            }
        }
    }

    public static void addAttachment(Context context, int position, int type, Uri uri, WorkingMessage workingMessage, AttachmentThreadCallBack attachmentCallback) {
        if (context == null || uri == null || workingMessage == null || attachmentCallback == null) {
            MLog.e("AttachmentThreadManager", "addAttachment params is error");
        } else {
            startAttachmentThread(new AttachmentThread(context, position, type, uri, workingMessage, attachmentCallback));
        }
    }

    public static void addAttachment(Context context, int type, Uri uri, RcsGroupChatAttachmentThreadCallBack rcsGroupChatattachmentCallback) {
        if (context != null && uri != null && rcsGroupChatattachmentCallback != null) {
            startAttachmentThread(new AttachmentThread(context, type, uri, rcsGroupChatattachmentCallback));
        }
    }

    public static Uri persisterPart(Context context, WorkingMessage workingMessage, PduPart part, Uri oldUri, boolean isLocation) throws MmsException {
        Uri uri;
        synchronized (mPartLock) {
            if (workingMessage.getMessageUri() == null) {
                MLog.e("AttachmentThreadManager", "persisterPart: saveAsMms");
                workingMessage.saveAsMms(true, false);
            }
            long messageId = ContentUris.parseId(workingMessage.getMessageUri());
            MLog.e("AttachmentThreadManager", "workingMesage messageId:" + messageId);
            uri = PduPersister.getPduPersister(context).persistPart(part, messageId, null);
            MLog.e("AttachmentThreadManager", "workingMessage newUri:" + uri);
            insertPartSource(context, uri, oldUri, isLocation);
        }
        return uri;
    }

    private static void insertPartSource(Context context, Uri newUri, Uri oldUri, boolean isLocation) {
        if (context != null && newUri != null) {
            SqliteWrapper.delete(context, context.getContentResolver(), Uri.parse("content://mms/part_source/" + newUri.getLastPathSegment()), null, null);
            Uri uri = Uri.parse("content://mms/part_source");
            ContentValues values = new ContentValues();
            values.put("part_id", Integer.valueOf(Integer.parseInt(newUri.getLastPathSegment())));
            values.put("old_data", oldUri.getPath());
            if (isLocation) {
                AttachmentSelectLocationControl.sealLoactionValue(context, "Mms_UI_CMF", values);
            }
            SqliteWrapper.insert(context, uri, values);
        }
    }
}
