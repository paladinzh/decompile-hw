package com.android.mms.attachment.datamodel.media;

import android.net.Uri;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.mms.model.MediaModel;
import com.huawei.cspcommon.MLog;
import java.util.Collection;
import java.util.HashMap;

public class AttachmentDataManager {
    private HashMap<Integer, AttachmentPage> mAttachmentData = new HashMap();
    private AttachmentDataManagerListener mAttachmentListener;
    private Object mLock = new Object();

    public interface AttachmentDataManagerListener {
        void onResultAttachment(boolean z, int i, int i2);
    }

    public static class AttachmentPage {
        private HashMap<Integer, AttachmentState> mAttachmentPage = new HashMap();

        public AttachmentState getAttachmentState(int key) {
            if (this.mAttachmentPage.containsKey(Integer.valueOf(key))) {
                return (AttachmentState) this.mAttachmentPage.get(Integer.valueOf(key));
            }
            return null;
        }

        public AttachmentState removeAttachmentState(int key) {
            if (this.mAttachmentPage.containsKey(Integer.valueOf(key))) {
                return (AttachmentState) this.mAttachmentPage.remove(Integer.valueOf(key));
            }
            return null;
        }

        public void putAttachmentState(int key, AttachmentState attachmentState) {
            this.mAttachmentPage.put(Integer.valueOf(key), attachmentState);
        }

        public boolean containskey(int key) {
            return this.mAttachmentPage.containsKey(Integer.valueOf(key));
        }

        public boolean isContainsKey() {
            if (this.mAttachmentPage.size() == 0) {
                return false;
            }
            return true;
        }
    }

    public static class AttachmentState {
        private Uri attachmentUri;
        private int currentState;
        private MediaModel mediaModel;
        private int resultCode;
        private int type;

        public void setCurrentState(int state) {
            this.currentState = state;
        }

        public int getCurrentState() {
            return this.currentState;
        }

        public void setMediaModel(MediaModel mediamodel) {
            this.mediaModel = mediamodel;
        }

        public MediaModel getMediaModel() {
            return this.mediaModel;
        }

        public void setAttachmentUri(Uri uri) {
            this.attachmentUri = uri;
        }

        public Uri getAttachmentUri() {
            return this.attachmentUri;
        }

        public void setResultCode(int resultcode) {
            this.resultCode = resultcode;
        }

        public int getResultCode() {
            return this.resultCode;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }

    public AttachmentDataManager(AttachmentDataManagerListener attachmentListener) {
        this.mAttachmentListener = attachmentListener;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addAttachment(int position, int type, Uri uri) {
        synchronized (this.mLock) {
            int result;
            if (this.mAttachmentData.containsKey(Integer.valueOf(position))) {
                AttachmentPage attachmentPage = (AttachmentPage) this.mAttachmentData.get(Integer.valueOf(position));
                if (attachmentPage == null) {
                    return 102;
                }
                AttachmentState attachmentState = attachmentPage.getAttachmentState(type);
                if (attachmentState == null) {
                    MLog.d("AttachmentDataManager", "postion has set, need to adding new");
                    AttachmentState newAttachmentState = new AttachmentState();
                    newAttachmentState.setAttachmentUri(uri);
                    newAttachmentState.setCurrentState(1);
                    newAttachmentState.setType(type);
                    attachmentPage.putAttachmentState(type, newAttachmentState);
                    result = 101;
                } else if (uri.equals(attachmentState.getAttachmentUri())) {
                    if (attachmentState.getCurrentState() == 2) {
                        MLog.d("AttachmentDataManager", "attachment has stop, need to loading");
                        attachmentState.setCurrentState(1);
                    } else {
                        MLog.d("AttachmentDataManager", "attachment has loaded, don't need to loading");
                    }
                    result = 101;
                } else {
                    MLog.d("AttachmentDataManager", "postion has set, need to updating state");
                    attachmentPage.removeAttachmentState(type);
                    AttachmentState updateAttachmentState = new AttachmentState();
                    updateAttachmentState.setAttachmentUri(uri);
                    updateAttachmentState.setCurrentState(1);
                    updateAttachmentState.setType(type);
                    attachmentPage.putAttachmentState(type, updateAttachmentState);
                    result = OfflineMapStatus.EXCEPTION_SDCARD;
                }
            } else {
                MLog.d("AttachmentDataManager", "postion hasn't set, need to adding new");
                AttachmentState addAttachmentState = new AttachmentState();
                addAttachmentState.setAttachmentUri(uri);
                addAttachmentState.setCurrentState(1);
                addAttachmentState.setType(type);
                AttachmentPage addAttachmentPage = new AttachmentPage();
                addAttachmentPage.putAttachmentState(type, addAttachmentState);
                this.mAttachmentData.put(Integer.valueOf(position), addAttachmentPage);
                result = 101;
            }
        }
    }

    public boolean setAttachmentResult(int position, int type, MediaModel mediaModel, int resultCode) {
        boolean result = false;
        synchronized (this.mLock) {
            if (this.mAttachmentData.containsKey(Integer.valueOf(position))) {
                AttachmentPage attachmentPage = (AttachmentPage) this.mAttachmentData.get(Integer.valueOf(position));
                if (attachmentPage == null || !attachmentPage.containskey(type)) {
                    MLog.e("AttachmentDataManager", "setAttachmentResult failed, AttachmentPage is null");
                    this.mAttachmentData.remove(Integer.valueOf(position));
                } else {
                    AttachmentState attachmentState = attachmentPage.getAttachmentState(type);
                    attachmentState.setMediaModel(mediaModel);
                    attachmentState.setResultCode(resultCode);
                    attachmentState.setCurrentState(3);
                    result = true;
                }
            } else {
                MLog.e("AttachmentDataManager", "setAttachmentResult failed, don't have the position");
            }
        }
        this.mAttachmentListener.onResultAttachment(result, position, type);
        return result;
    }

    public AttachmentState removeAttachmentState(int position, int type) {
        AttachmentState attachmentState = null;
        synchronized (this.mLock) {
            if (this.mAttachmentData.containsKey(Integer.valueOf(position))) {
                AttachmentPage attachmentData = (AttachmentPage) this.mAttachmentData.get(Integer.valueOf(position));
                if (attachmentData != null) {
                    attachmentState = attachmentData.removeAttachmentState(type);
                    if (!attachmentData.isContainsKey()) {
                        this.mAttachmentData.remove(Integer.valueOf(position));
                    }
                }
            }
        }
        return attachmentState;
    }

    public boolean isCanAddAttachment(int currPosition) {
        synchronized (this.mLock) {
            if (this.mAttachmentData.size() == 0) {
                return true;
            }
            for (Integer postion : this.mAttachmentData.keySet()) {
                if (postion.intValue() < currPosition) {
                    return false;
                }
            }
            return true;
        }
    }

    public int getCanAddPosition(int currentPosition) {
        synchronized (this.mLock) {
            if (this.mAttachmentData.size() == 0) {
                MLog.e("AttachmentDataManager", "getCanAddPosition size is 0");
                return currentPosition;
            }
            int tempPosition = -1;
            for (Integer postion : this.mAttachmentData.keySet()) {
                if (postion.intValue() > tempPosition) {
                    tempPosition = postion.intValue();
                }
            }
            MLog.e("AttachmentDataManager", "getCanAddPosition tempPosition:" + tempPosition);
            if (tempPosition >= currentPosition) {
                int i = tempPosition + 1;
                return i;
            }
            return currentPosition;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void notifyResultContinue() {
        MLog.e("AttachmentDataManager", "notifyResultContinue method start");
        int tempPosition = Integer.MAX_VALUE;
        int type = -1;
        synchronized (this.mLock) {
            if (this.mAttachmentData.size() != 0) {
                for (Integer postion : this.mAttachmentData.keySet()) {
                    if (postion.intValue() < tempPosition) {
                        tempPosition = postion.intValue();
                    }
                }
                MLog.e("AttachmentDataManager", "notifyResultContinue current small position:" + tempPosition);
                Collection<AttachmentState> attachments = ((AttachmentPage) this.mAttachmentData.get(Integer.valueOf(tempPosition))).mAttachmentPage.values();
                if (attachments != null) {
                    for (AttachmentState attachmentState : attachments) {
                        if (attachmentState.currentState != 1) {
                            type = attachmentState.getType();
                            MLog.e("AttachmentDataManager", "notifyResultContinue current small position state is OK.");
                            break;
                        }
                    }
                }
                return;
            }
            MLog.e("AttachmentDataManager", "notifyResultContinue size is 0, return");
        }
    }

    public int getSize() {
        int size;
        synchronized (this.mLock) {
            size = this.mAttachmentData.size();
        }
        return size;
    }
}
