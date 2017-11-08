package com.huawei.rcs.incallui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.android.mms.attachment.utils.ContentType;
import com.android.mms.attachment.utils.UriUtil;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.model.SlideModel;
import com.android.mms.model.TextModel;
import com.android.mms.transaction.SmsMessageSender;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.rcs.incallui.service.IMessagePlusService.Stub;
import com.huawei.rcs.incallui.service.ServiceRichMessageEditor.HandlerErrorCallBack;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import rcstelephony.RcsMessagingConstants$Threads;

public class MessagePlusService extends Service {
    private static final RemoteCallbackList<ISendListener> SSENDLISTENERS = new RemoteCallbackList();
    private static final ArrayList<String> mmsUriList = new ArrayList();
    private static final ArrayList<String> smsUriList = new ArrayList();
    private String mContent = null;
    private Context mContext;
    private String mRecipient = null;
    Stub stub = new Stub() {
        public void sendMessageWithSubId(String receiver, String content, String path, int subId) throws RemoteException {
            MessagePlusService.this.mContent = content;
            MessagePlusService.this.mRecipient = receiver;
            MessagePlusService.this.sendCurrentMessage(receiver, content, path, subId);
        }

        public void registerSendListener(ISendListener listener) throws RemoteException {
            if (listener != null) {
                MessagePlusService.SSENDLISTENERS.register(listener);
            }
        }

        public void unregisterSendListener(ISendListener listener) throws RemoteException {
            if (listener != null) {
                MessagePlusService.SSENDLISTENERS.unregister(listener);
            }
        }
    };

    private class ErrorCallBack implements HandlerErrorCallBack {
        private ErrorCallBack() {
        }

        public void getAttachermentErrorCode(int code, final WorkingMessage msg) {
            int errorReason;
            switch (code) {
                case -4:
                    MLog.d("MessagePlusService", "getErrorCode toolarge");
                    errorReason = 4;
                    break;
                case -3:
                    MLog.d("MessagePlusService", "getErrorCode unsupport");
                    errorReason = 2;
                    break;
                case -2:
                    MLog.d("MessagePlusService", "getErrorCode SIZE_EXCEEDED");
                    errorReason = 3;
                    break;
                case -1:
                    MLog.d("MessagePlusService", "getErrorCode error");
                    errorReason = 1;
                    break;
                case 0:
                    errorReason = 0;
                    MLog.d("MessagePlusService", "getErrorCode ok");
                    ThreadEx.getSerialExecutor().execute(new Runnable() {
                        public void run() {
                            if (TextUtils.isEmpty(MessagePlusService.this.mContent) || MessagePlusService.this.mContent.equals("")) {
                                MLog.d("MessagePlusService", "mContent is null");
                            } else {
                                msg.syncTextToSlideshow(MessagePlusService.this.mContent);
                            }
                            MLog.d("MessagePlusService", "mWorkingMessage.send");
                            msg.send(MessagePlusService.this.mRecipient, 0);
                        }
                    });
                    break;
                default:
                    throw new IllegalArgumentException("unknown error " + code);
            }
            MessagePlusService.notifyStateChanged(errorReason);
        }
    }

    public static void notifyMmsStateUri(int state) {
        int size = SSENDLISTENERS.beginBroadcast();
        for (int i = 0; i < size; i++) {
            try {
                ((ISendListener) SSENDLISTENERS.getBroadcastItem(i)).onMmsSendState(state);
            } catch (RemoteException e) {
                MLog.e("MessagePlusService", "RemoteException occurs in notifyMmsStateUri()");
            }
        }
        SSENDLISTENERS.finishBroadcast();
    }

    public static void notifySmsStateUri(int state) {
        int size = SSENDLISTENERS.beginBroadcast();
        for (int i = 0; i < size; i++) {
            try {
                ((ISendListener) SSENDLISTENERS.getBroadcastItem(i)).onSmsSendState(state);
            } catch (RemoteException e) {
                MLog.e("MessagePlusService", "RemoteException occurs in notifySmsStateUri()");
            }
        }
        SSENDLISTENERS.finishBroadcast();
    }

    public static void notifyStateChanged(int state) {
        MLog.d("MessagePlusService", "notifyStateChanged-state" + state);
        int size = SSENDLISTENERS.beginBroadcast();
        for (int i = 0; i < size; i++) {
            try {
                ((ISendListener) SSENDLISTENERS.getBroadcastItem(i)).onAttachmentStateChanged(state);
            } catch (RemoteException e) {
                MLog.e("MessagePlusService", "RemoteException occurs in notifyStateChanged()");
            }
        }
        SSENDLISTENERS.finishBroadcast();
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = this;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        return this.stub;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void sendCurrentMessage(String semiSepRecipients, String msgText, String path, int subscription) {
        String[] dests = TextUtils.split(semiSepRecipients, ";");
        if (path == null) {
            MLog.e("MessagePlusService", "into sms");
            long threadId = RcsMessagingConstants$Threads.getOrCreateThreadId((Context) this, semiSepRecipients);
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.d("Mms_TXN", "sendSmsWorker sending  for threadId=" + threadId);
            }
            try {
                new SmsMessageSender(this.mContext, dests, msgText, threadId, subscription).sendMessage(threadId);
                Recycler.getSmsRecycler().deleteOldMessagesByThreadId(this.mContext, threadId);
                notifyStateChanged(5);
            } catch (Exception e) {
                wirteLogMsg(threadId, e);
                MmsWidgetProvider.notifyDatasetChanged(this.mContext);
                notifyStateChanged(6);
            }
        } else {
            MLog.e("MessagePlusService", "into mms" + path);
            String str = null;
            Uri uri = UriUtil.getUriForResourceFile(path);
            if (uri != null && uri.getScheme().equals("file")) {
                String mSrc = path.substring(path.lastIndexOf(47) + 1);
                if (mSrc.startsWith(".") && mSrc.length() > 1) {
                    mSrc = mSrc.substring(1);
                }
                String extension = MimeTypeMap.getFileExtensionFromUrl(mSrc);
                if (TextUtils.isEmpty(extension)) {
                    int dotPos = mSrc.lastIndexOf(46);
                    if (dotPos >= 0) {
                        extension = mSrc.substring(dotPos + 1);
                    }
                }
                str = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                MLog.d("MessagePlusService", "mContentType======>" + str);
                if (str == null) {
                    notifyStateChanged(7);
                    return;
                }
            }
            Conversation conv = Conversation.get(this.mContext, ContactList.getByNumbers(semiSepRecipients, false, true), false);
            MessagePlusService messagePlusService = this;
            ServiceRichMessageEditor serviceRichMessageEditor = new ServiceRichMessageEditor(this.mContext, new ErrorCallBack());
            serviceRichMessageEditor.createWorkingMessage(this.mContext, new MessageStatusListener() {
                public void onProtocolChanged(boolean mms) {
                }

                public void onPreMessageSent() {
                }

                public void onMessageStateChanged() {
                }

                public void onMessageSent() {
                }

                public void onEmailAddressInput() {
                }
            });
            WorkingMessage mWorkingMessage = serviceRichMessageEditor.getWorkingMessage();
            mWorkingMessage.setConversation(conv);
            mWorkingMessage.ensureSlideshow();
            mWorkingMessage.setAttachmentState(true, true);
            mWorkingMessage.getSlideshow().clear();
            if (ContentType.isAudioType(str)) {
                MLog.d("MessagePlusService", "isAudioType");
                serviceRichMessageEditor.setNewAttachment(this.mContext, uri, 3, false);
            } else if (ContentType.isVideoType(str)) {
                MLog.d("MessagePlusService", "isVideoType");
                serviceRichMessageEditor.setNewAttachment(this.mContext, uri, 5, false);
            } else if (ContentType.isImageType(str)) {
                MLog.e("MessagePlusService", "isImageType");
                serviceRichMessageEditor.setNewAttachment(this.mContext, uri, 2, false);
            } else if (ContentType.isVCardType(str)) {
                MLog.d("MessagePlusService", "isVCardType");
                serviceRichMessageEditor.setNewAttachment(this.mContext, uri, 6, false);
            } else if (ContentType.isTextType(str)) {
                MLog.d("MessagePlusService", "isTextType");
                final String contentType = str;
                final WorkingMessage workingMessage = mWorkingMessage;
                final String str2 = path;
                ThreadEx.getSerialExecutor().execute(new Runnable() {
                    public void run() {
                        Throwable th;
                        SlideModel slide = workingMessage.getSlideshow().get(0);
                        TextModel model = slide.getText();
                        byte[] buffer = null;
                        FileInputStream fileInputStream = null;
                        try {
                            FileInputStream is = new FileInputStream(new File(str2));
                            try {
                                buffer = new byte[is.available()];
                                if (is.read(buffer) == -1) {
                                    MLog.e("MessagePlusService", "FileInputStream read complete");
                                }
                                if (is != null) {
                                    try {
                                        is.close();
                                    } catch (IOException e) {
                                        MLog.e("MessagePlusService", "IOException occures");
                                    }
                                }
                                fileInputStream = is;
                            } catch (FileNotFoundException e2) {
                                fileInputStream = is;
                                MLog.e("MessagePlusService", "FileNotFoundException occures");
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e3) {
                                        MLog.e("MessagePlusService", "IOException occures");
                                    }
                                }
                                slide.add(new TextModel(MessagePlusService.this.mContext, contentType, "text_" + str2.substring(str2.lastIndexOf(47) + 1, str2.lastIndexOf(".")) + ".txt", 106, buffer, workingMessage.getSlideTextRegion()));
                                MLog.d("MessagePlusService", "mWorkingMessage.send");
                                workingMessage.send(MessagePlusService.this.mRecipient, 0);
                            } catch (IOException e4) {
                                fileInputStream = is;
                                try {
                                    MLog.e("MessagePlusService", "IOException occures");
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e5) {
                                            MLog.e("MessagePlusService", "IOException occures");
                                        }
                                    }
                                    slide.add(new TextModel(MessagePlusService.this.mContext, contentType, "text_" + str2.substring(str2.lastIndexOf(47) + 1, str2.lastIndexOf(".")) + ".txt", 106, buffer, workingMessage.getSlideTextRegion()));
                                    MLog.d("MessagePlusService", "mWorkingMessage.send");
                                    workingMessage.send(MessagePlusService.this.mRecipient, 0);
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e6) {
                                            MLog.e("MessagePlusService", "IOException occures");
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                fileInputStream = is;
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                throw th;
                            }
                        } catch (FileNotFoundException e7) {
                            MLog.e("MessagePlusService", "FileNotFoundException occures");
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            slide.add(new TextModel(MessagePlusService.this.mContext, contentType, "text_" + str2.substring(str2.lastIndexOf(47) + 1, str2.lastIndexOf(".")) + ".txt", 106, buffer, workingMessage.getSlideTextRegion()));
                            MLog.d("MessagePlusService", "mWorkingMessage.send");
                            workingMessage.send(MessagePlusService.this.mRecipient, 0);
                        } catch (IOException e8) {
                            MLog.e("MessagePlusService", "IOException occures");
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            slide.add(new TextModel(MessagePlusService.this.mContext, contentType, "text_" + str2.substring(str2.lastIndexOf(47) + 1, str2.lastIndexOf(".")) + ".txt", 106, buffer, workingMessage.getSlideTextRegion()));
                            MLog.d("MessagePlusService", "mWorkingMessage.send");
                            workingMessage.send(MessagePlusService.this.mRecipient, 0);
                        }
                        slide.add(new TextModel(MessagePlusService.this.mContext, contentType, "text_" + str2.substring(str2.lastIndexOf(47) + 1, str2.lastIndexOf(".")) + ".txt", 106, buffer, workingMessage.getSlideTextRegion()));
                        MLog.d("MessagePlusService", "mWorkingMessage.send");
                        workingMessage.send(MessagePlusService.this.mRecipient, 0);
                    }
                });
            } else {
                notifyStateChanged(2);
                return;
            }
            notifyStateChanged(0);
        }
    }

    private void wirteLogMsg(long threadId, Exception e) {
        MLog.d("MessagePlusService", "Failed to send SMS message, threadId=" + threadId, (Throwable) e);
        MmsRadarInfoManager.getInstance().writeLogMsg(1311, e.getMessage());
    }

    public static int getMmsUriListSize() {
        if (mmsUriList.size() > 0) {
            return mmsUriList.size();
        }
        return 0;
    }

    public static String getUriFromMmsUriList(int index) {
        if (index < 0) {
            return null;
        }
        return (String) mmsUriList.get(index);
    }

    public static void addToMmsUriList(String value) {
        if (TextUtils.isEmpty(value)) {
            MLog.e("MessagePlusService", "in addToMmsUriList(), get a null value");
        } else {
            mmsUriList.add(value);
        }
    }

    public static void removeFromMmsUriList(int index) {
        if (index >= 0 && index < mmsUriList.size()) {
            mmsUriList.remove(index);
        }
    }

    public static int getSmsUriListSize() {
        if (smsUriList.size() > 0) {
            return smsUriList.size();
        }
        return 0;
    }

    public static String getUriFromSmsUriList(int index) {
        if (index < 0) {
            return null;
        }
        return (String) smsUriList.get(index);
    }

    public static void removeFromSmsUriList(int index) {
        if (index >= 0 && index < smsUriList.size()) {
            smsUriList.remove(index);
        }
    }

    public static void addToSmsUriList(String value) {
        if (TextUtils.isEmpty(value)) {
            MLog.e("MessagePlusService", "in addToSmsUriList(), get a null value");
        } else {
            smsUriList.add(value);
        }
    }
}
