package com.huawei.rcs.incallui.service;

import android.content.Context;
import android.net.Uri;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.UnsupportContentTypeException;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager.AttachmentDataManagerListener;
import com.android.mms.attachment.datamodel.media.AttachmentDataManager.AttachmentState;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager;
import com.android.mms.attachment.datamodel.media.AttachmentThreadManager.AttachmentThreadCallBack;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.ui.SlideshowEditor;
import com.huawei.cspcommon.MLog;

public class ServiceRichMessageEditor {
    private HandlerErrorCallBack cb;
    private AttachmentDataManager mAttachmentDataManager = new AttachmentDataManager(new AttachmentDataManagerListener() {
        public void onResultAttachment(boolean result, int position, int type) {
            ServiceRichMessageEditor.this.dispatchAttachmentResult(result, position, type);
        }
    });
    private Context mContext;
    private int mPosition = -1;
    private WorkingMessage mWorkingMessage;

    public interface HandlerErrorCallBack {
        void getAttachermentErrorCode(int i, WorkingMessage workingMessage);
    }

    private class RichAttachmentThreadCallBack implements AttachmentThreadCallBack {
        private RichAttachmentThreadCallBack() {
        }

        public void onModelFinish(int resultCode, int position, MediaModel mediaModel, int type) {
            if (!ServiceRichMessageEditor.this.mAttachmentDataManager.setAttachmentResult(position, type, mediaModel, resultCode)) {
                MLog.e("ServiceRichMessageEditor", "onModelFinish setAttachmentResult failed");
            }
        }
    }

    public ServiceRichMessageEditor(Context context, HandlerErrorCallBack callBack) {
        this.mContext = context;
        this.cb = callBack;
    }

    public void createWorkingMessage(Context context, MessageStatusListener listener) {
        this.mWorkingMessage = WorkingMessage.createEmpty(context, listener);
    }

    private int createSlide(int position, int type, boolean mainThread) {
        SlideshowEditor editor = this.mWorkingMessage.getSlideshowEditor();
        if (editor == null || !editor.addNewSlide(position)) {
            return -1;
        }
        return position;
    }

    public SlideshowModel getSlideshow() {
        return this.mWorkingMessage.getSlideshow();
    }

    public int getSlideSize() {
        SlideshowModel slideShow = this.mWorkingMessage.getSlideshow();
        return slideShow != null ? slideShow.size() : 0;
    }

    public WorkingMessage getWorkingMessage() {
        return this.mWorkingMessage;
    }

    public void setNewAttachment(Context activity, Uri uri, int type, boolean append) {
        if (this.mWorkingMessage.ensureSlideshow() && this.mPosition < 0) {
            this.mPosition++;
        }
        addNewAttachment(activity, type, uri);
    }

    private void addNewAttachment(Context activity, int type, Uri uri) {
        if (this.mWorkingMessage.ensureSlideshow() && this.mPosition < 0) {
            this.mPosition++;
        }
        if (this.mPosition < 0) {
            this.mPosition++;
        }
        int addPosition = this.mAttachmentDataManager.getCanAddPosition(this.mPosition);
        MLog.d("ServiceRichMessageEditor", "addNewAttachment  addPosition==>" + addPosition + "  getSlideSize()==>" + getSlideSize());
        if (addPosition >= getSlideSize()) {
            MLog.e("ServiceRichMessageEditor", "333333333333addNewAttachmentUri()=" + uri);
            this.mAttachmentDataManager.addAttachment(addPosition, type, uri);
            AttachmentThreadManager.addAttachment(activity, addPosition, type, uri, this.mWorkingMessage, new RichAttachmentThreadCallBack());
            return;
        }
        MLog.e("ServiceRichMessageEditor", "4444444444444addNewAttachmentUri()=" + uri);
        SlideModel slideModel = getSlideshow().get(addPosition);
        while (slideModel != null && !slideModel.hasRoomForAttachment()) {
            addPosition++;
            slideModel = getSlideshow().get(addPosition);
        }
        MLog.e("ServiceRichMessageEditor", "addNewAttachment  addPosition==>" + addPosition);
        this.mAttachmentDataManager.addAttachment(addPosition, type, uri);
        AttachmentThreadManager.addAttachment(activity, addPosition, type, uri, this.mWorkingMessage, new RichAttachmentThreadCallBack());
    }

    private void dispatchAttachmentResult(boolean result, int position, int type) {
        if (result) {
            MLog.e("ServiceRichMessageEditor", "dispatchAttachmentResult ===>mAttachmentDataManager.isCanAddAttachment(position)=" + this.mAttachmentDataManager.isCanAddAttachment(position));
            if (this.mAttachmentDataManager.isCanAddAttachment(position)) {
                AttachmentState resultAttachmentState = this.mAttachmentDataManager.removeAttachmentState(position, type);
                if (resultAttachmentState == null) {
                    MLog.d("ServiceRichMessageEditor", "result can't add , resultAttachmentState is null");
                    return;
                }
                int resultCode = resultAttachmentState.getResultCode();
                int resultState = resultAttachmentState.getCurrentState();
                MediaModel resultMediaModel = resultAttachmentState.getMediaModel();
                if (resultCode == 0 && resultState == 3) {
                    addResultSlide(position, type, resultMediaModel);
                } else if (resultCode != 0) {
                    handleErrorResult(this.mContext, resultCode, type);
                }
                this.mAttachmentDataManager.notifyResultContinue();
            } else {
                MLog.e("ServiceRichMessageEditor", "result can't add ,postion is not small");
            }
        } else if (this.mAttachmentDataManager.removeAttachmentState(position, type) == null) {
        }
    }

    private void addResultSlide(int position, int type, MediaModel mediaModel) {
        int result = 0;
        if (mediaModel == null) {
            MLog.e("ServiceRichMessageEditor", "addResultSlide mediaModel == null");
            handleErrorResult(this.mContext, -1, type);
            return;
        }
        try {
            SlideshowModel slideshowModel = this.mWorkingMessage.getSlideshow();
            if (slideshowModel != null) {
                int resultPosition;
                SlideModel slideModel;
                if (slideshowModel.size() <= position) {
                    int createPosition;
                    if (slideshowModel.get(this.mPosition) == null || !slideshowModel.get(this.mPosition).hasRoomForAttachment()) {
                        createPosition = createSlide(slideshowModel.size(), type, true);
                    } else {
                        createPosition = this.mPosition;
                    }
                    resultPosition = createPosition;
                    slideModel = slideshowModel.get(createPosition);
                    slideModel.add(mediaModel);
                    if (type == 3 || type == 5) {
                        slideModel.updateDuration(mediaModel.getDuration());
                    }
                    updateAttachmentResult(resultPosition, type);
                } else {
                    slideModel = slideshowModel.get(position);
                    if (slideModel == null) {
                        MLog.e("ServiceRichMessageEditor", "addResultSlide slideModel is null.");
                        return;
                    }
                    resultPosition = position;
                    if (slideModel.hasRoomForAttachment(type)) {
                        slideModel.add(mediaModel);
                        updateAttachmentResult(position, type);
                    }
                }
                this.mPosition = resultPosition;
                handleErrorResult(this.mContext, result, type);
            }
        } catch (ExceedMessageSizeException e) {
            result = -2;
        } catch (UnsupportContentTypeException e2) {
            result = -3;
        }
    }

    private void updateAttachmentResult(int position, int type) {
        if (type == 2) {
            SlideshowModel slideShowModel = getSlideshow();
            if (slideShowModel != null) {
                SlideModel slideModel = slideShowModel.get(position);
                if (slideModel != null && slideModel.hasImage()) {
                    slideShowModel.addImageSourceBuild(slideModel);
                    return;
                }
                return;
            }
            return;
        }
        this.mWorkingMessage.saveAsMms(true, false);
    }

    private void handleErrorResult(Context context, int errorCode, int type) {
        if (context == null) {
            MLog.e("ServiceRichMessageEditor", "handleErrorResult context is null. ");
            return;
        }
        switch (type) {
            case 2:
            case 3:
            case 5:
            case 6:
            case 7:
                break;
            default:
                errorCode = -3;
                break;
        }
        switch (errorCode) {
            case -4:
            case -3:
            case -2:
            case -1:
            case 0:
                if (this.cb != null) {
                    this.cb.getAttachermentErrorCode(errorCode, getWorkingMessage());
                }
                return;
            default:
                throw new IllegalArgumentException("unknown error " + errorCode);
        }
    }
}
