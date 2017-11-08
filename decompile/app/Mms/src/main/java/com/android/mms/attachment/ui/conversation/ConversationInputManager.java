package com.android.mms.attachment.ui.conversation;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.ui.conversation.ConversationInput.ConversationInputBase;
import com.android.mms.attachment.ui.mediapicker.CameraMediaChooser;
import com.android.mms.attachment.ui.mediapicker.GalleryMediaChooser;
import com.android.mms.attachment.ui.mediapicker.MapMediaChooser;
import com.android.mms.attachment.ui.mediapicker.MediaChooser;
import com.android.mms.attachment.ui.mediapicker.MediaPicker;
import com.android.mms.attachment.ui.mediapicker.MediaPicker.MediaPickerListener;
import com.android.mms.attachment.ui.mediapicker.MediaPickerPanel;
import com.android.mms.ui.ConversationList;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.util.HwMessageUtils;
import java.util.Collection;

public class ConversationInputManager implements ConversationInputBase {
    private boolean isShowBeforeChangeMode = false;
    private Context mContext;
    private final FragmentManager mFragmentManager;
    private ConversationInputHost mHost;
    private ConversationMediaPicker mMediaInput;
    private int mUpdateCount;

    public interface ConversationInputHost {
        MediaPicker createMediaPicker();

        void invalidateActionBar();

        void onChooserSlected(MediaChooser mediaChooser);

        void onMediaFullScreenChanged(boolean z, int i);

        void onMediaItemsSelected(Collection<AttachmentSelectData> collection);

        void onMediaItemsUnselected(AttachmentSelectData attachmentSelectData);

        void onPendingOperate(int i, AttachmentSelectData attachmentSelectData);

        void resumeComposeMessage();
    }

    private class ConversationMediaPicker extends ConversationInput {
        protected boolean mFullScreenState = false;
        private MediaPicker mMediaPicker;

        public ConversationMediaPicker(ConversationInputBase baseHost) {
            super(baseHost, false);
        }

        public void removeMediaPicker() {
            if (this.mMediaPicker != null) {
                ConversationInputManager.this.mFragmentManager.beginTransaction().remove(this.mMediaPicker);
                this.mMediaPicker = null;
            }
        }

        public boolean show(boolean animate) {
            if (this.mMediaPicker != null && this.mMediaPicker.checkSimpleStype()) {
                ConversationInputManager.this.mFragmentManager.beginTransaction().remove(this.mMediaPicker);
                this.mMediaPicker = null;
            }
            if (this.mMediaPicker == null) {
                this.mMediaPicker = getExistingOrCreateMediaPicker();
                this.mMediaPicker.setListener(new MediaPickerListener() {
                    public void onOpened() {
                        handleStateChange();
                    }

                    public void onFullScreenChanged(boolean fullScreen) {
                        handleStateChange();
                        MediaChooser mediachooser = ConversationMediaPicker.this.mMediaPicker.getSelectedChooser();
                        ConversationMediaPicker.this.mFullScreenState = fullScreen;
                        if (mediachooser == null) {
                            return;
                        }
                        if (mediachooser instanceof GalleryMediaChooser) {
                            ConversationInputManager.this.mHost.onMediaFullScreenChanged(fullScreen, 102);
                        } else if (mediachooser instanceof CameraMediaChooser) {
                            ConversationInputManager.this.mHost.onMediaFullScreenChanged(fullScreen, 101);
                        } else if (mediachooser instanceof MapMediaChooser) {
                            ConversationInputManager.this.mHost.onMediaFullScreenChanged(fullScreen, LocationRequest.PRIORITY_LOW_POWER);
                        } else {
                            ConversationInputManager.this.mHost.onMediaFullScreenChanged(fullScreen, OfflineMapStatus.EXCEPTION_SDCARD);
                        }
                    }

                    public void onDismissed() {
                        handleStateChange();
                        if (ConversationMediaPicker.this.mMediaPicker != null) {
                            MediaChooser mediachooser = ConversationMediaPicker.this.mMediaPicker.getSelectedChooser();
                            if (mediachooser != null && ConversationMediaPicker.this.mFullScreenState) {
                                if (mediachooser instanceof GalleryMediaChooser) {
                                    ConversationInputManager.this.mHost.onMediaFullScreenChanged(false, 102);
                                } else if (mediachooser instanceof CameraMediaChooser) {
                                    ConversationInputManager.this.mHost.onMediaFullScreenChanged(false, 101);
                                } else if (mediachooser instanceof MapMediaChooser) {
                                    ConversationInputManager.this.mHost.onMediaFullScreenChanged(false, LocationRequest.PRIORITY_LOW_POWER);
                                } else {
                                    ConversationInputManager.this.mHost.onMediaFullScreenChanged(false, OfflineMapStatus.EXCEPTION_SDCARD);
                                }
                            }
                            ConversationMediaPicker.this.mFullScreenState = false;
                        }
                    }

                    private void handleStateChange() {
                        ConversationMediaPicker.this.onVisibilityChanged(ConversationMediaPicker.this.isOpen());
                        ConversationInputManager.this.mHost.invalidateActionBar();
                    }

                    public void onItemsSelected(Collection<AttachmentSelectData> attachmentItems, boolean resumeCompose) {
                        ConversationInputManager.this.mHost.onMediaItemsSelected(attachmentItems);
                        if (resumeCompose) {
                            ConversationInputManager.this.mHost.resumeComposeMessage();
                        }
                    }

                    public void onItemUnselected(AttachmentSelectData attachmentItem) {
                        ConversationInputManager.this.mHost.onMediaItemsUnselected(attachmentItem);
                    }

                    public void onChooserSelected(MediaChooser mediaChooser) {
                        if (mediaChooser != null) {
                            ConversationInputManager.this.mHost.onChooserSlected(mediaChooser);
                        }
                    }

                    public void onPendingAddOperate(int type, AttachmentSelectData attachmentItem) {
                        ConversationInputManager.this.mHost.onPendingOperate(type, attachmentItem);
                    }
                });
            }
            this.mMediaPicker.open(1, animate);
            return isOpen();
        }

        public boolean hide(boolean animate) {
            if (this.mMediaPicker != null) {
                if (this.mMediaPicker.getIsCameraChooser()) {
                    ((CameraMediaChooser) this.mMediaPicker.getSelectedChooser()).setScrollFullScreenState(false);
                }
                this.mMediaPicker.dismiss(animate);
            }
            if (isOpen()) {
                return false;
            }
            return true;
        }

        private boolean isOpen() {
            return this.mMediaPicker != null ? this.mMediaPicker.isOpen() : false;
        }

        private MediaPicker getExistingOrCreateMediaPicker() {
            if (this.mMediaPicker != null) {
                return this.mMediaPicker;
            }
            MediaPicker mediaPicker = null;
            if (!RcsCommonConfig.isRCSSwitchOn()) {
                mediaPicker = (MediaPicker) ConversationInputManager.this.mFragmentManager.findFragmentByTag("mediapicker");
            }
            if (mediaPicker == null || mediaPicker.checkSimpleStype()) {
                mediaPicker = ConversationInputManager.this.mHost.createMediaPicker();
                if (mediaPicker == null) {
                    return null;
                }
                FragmentTransaction beginTransaction = ConversationInputManager.this.mFragmentManager.beginTransaction();
                int i = (HwMessageUtils.isSplitOn() && (ConversationInputManager.this.mContext instanceof ConversationList)) ? R.id.mediapicker_container_split : R.id.mediapicker_container;
                beginTransaction.replace(i, mediaPicker, "mediapicker").commit();
            }
            return mediaPicker;
        }

        public MediaPickerPanel getMediaPickerPanel() {
            return this.mMediaPicker == null ? null : this.mMediaPicker.getMediaPickerPanel();
        }

        public boolean updateActionBar(AbstractEmuiActionBar actionBar) {
            if (this.mMediaPicker != null) {
                this.mMediaPicker.updateActionBar(actionBar);
            }
            return true;
        }
    }

    public void removeMediaPick() {
        if (isMediaPickerVisible()) {
            this.isShowBeforeChangeMode = true;
        }
        showHideInternal(this.mMediaInput, false, false);
        this.mMediaInput.removeMediaPicker();
    }

    public boolean isShowBeforeChangeMode() {
        return this.isShowBeforeChangeMode;
    }

    public void setShowBeforeChangeMode(boolean show) {
        this.isShowBeforeChangeMode = show;
    }

    public ConversationInputManager(Context context, FragmentManager mfragmentmanager, ConversationInputHost mconversationinputhost) {
        this.mContext = context;
        this.mHost = mconversationinputhost;
        this.mFragmentManager = mfragmentmanager;
        this.mMediaInput = new ConversationMediaPicker(this);
    }

    public boolean showHideInternal(ConversationInput target, boolean show, boolean animate) {
        if (target.mShowing == show) {
            return false;
        }
        boolean success;
        beginUpdate();
        if (show) {
            success = target.show(animate);
        } else {
            success = target.hide(animate);
        }
        if (success) {
            target.onVisibilityChanged(show);
        }
        endUpdate();
        return true;
    }

    public void beginUpdate() {
        this.mUpdateCount++;
    }

    public void handleOnShow(ConversationInput target) {
        beginUpdate();
        endUpdate();
    }

    public void endUpdate() {
        int i = this.mUpdateCount - 1;
        this.mUpdateCount = i;
        if (i == 0) {
            this.mHost.invalidateActionBar();
        }
    }

    public void showHideMediaPicker(boolean show, boolean animate) {
        showHideInternal(this.mMediaInput, show, animate);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showMediaPicker(boolean isFullScreen, boolean animate) {
        if (this.mMediaInput != null && this.mMediaInput.getMediaPickerPanel() != null && this.mMediaInput.getMediaPickerPanel().isFullScreen() != isFullScreen) {
            this.mMediaInput.getMediaPickerPanel().setFullScreenView(false, animate);
        }
    }

    public void updateActionBar(AbstractEmuiActionBar actionBar) {
        this.mMediaInput.updateActionBar(actionBar);
    }

    public boolean isMediaPickerVisible() {
        return this.mMediaInput.mShowing;
    }

    public boolean getMediaPickerFullScreenState() {
        return this.mMediaInput.mFullScreenState;
    }
}
