package com.android.mms.attachment.ui.mediapicker;

import android.app.Fragment;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.datamodel.data.GalleryGridItemData;
import com.android.mms.attachment.datamodel.data.MediaPickerData;
import com.android.mms.attachment.datamodel.data.MediaPickerData.MediaPickerDataListener;
import com.android.mms.attachment.datamodel.media.GalleryCursorManager;
import com.android.mms.attachment.ui.mediapicker.GalleryGridView.GalleryGridViewListener;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.WarningDialog;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcseMmsExt;

public class GalleryMediaChooser extends MediaChooser implements GalleryGridViewListener, MediaPickerDataListener {
    private GalleryGridAdapter mAdapter = null;
    private GalleryGridView mGalleryGridView;
    private View mMissingPermissionView;

    public GalleryMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    public int getSupportedMediaTypes() {
        return 3;
    }

    public View destroyView() {
        this.mGalleryGridView.setAdapter(null);
        GalleryCursorManager.get().clearGalleryCursor();
        if (this.mAdapter != null) {
            this.mAdapter.setHostInterface(null);
        }
        Fragment fragment;
        if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
            fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
            if (fragment != null) {
                ((RcsGroupChatComposeMessageFragment) fragment).removeMediaUpdateListener(this.mGalleryGridView.getRcsGroupMediaUpdateListener());
            }
        } else {
            fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
            if (fragment != null) {
                ((ComposeMessageFragment) fragment).removeMediaUpdateListener(this.mGalleryGridView.getMediaUpdateListener());
            }
        }
        if (OsUtil.hasStoragePermission()) {
            ((MediaPickerData) this.mBindingRef.getData()).destroyLoader(1);
        }
        return super.destroyView();
    }

    public int getIconResource() {
        return this.mSelected ? R.drawable.ic_attachment_tab_picture_checked : R.drawable.ic_attachment_tab_picture;
    }

    protected int getIconTextResource() {
        return R.string.attach_image;
    }

    public boolean canSwipeDown() {
        return this.mGalleryGridView.canSwipeDown();
    }

    public void onItemSelected(Uri item, String contentType) {
        if (!(!RcseMmsExt.isRcsMode() || this.mMediaPicker == null || item == null)) {
            WarningDialog.show(this.mMediaPicker.getChildFragmentManager(), this.mMediaPicker.getActivity(), item.getPath());
        }
        this.mMediaPicker.dispatchItemsSelected(createAttachmentData(item, contentType), !this.mGalleryGridView.isMultiSelectEnabled());
    }

    public void onItemUnselected(Uri item, String contentType) {
        this.mMediaPicker.dispatchItemUnselected(createAttachmentData(item, contentType));
    }

    public void onUpdate() {
        this.mMediaPicker.invalidateOptionsMenu();
    }

    public void onCompressImage(int position) {
        this.mMediaPicker.dispatchPickerOperate(1002, createImageCompressData(position));
    }

    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
        if (this.mView != null) {
            this.mGalleryGridView.onCreateOptionsMenu(inflater, menu);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mView != null ? this.mGalleryGridView.onOptionsItemSelected(item) : false;
    }

    protected void onRestoreChooserState() {
    }

    protected View createView(ViewGroup container) {
        View view = getLayoutInflater().inflate(R.layout.mediapicker_image_chooser, container, false);
        this.mGalleryGridView = (GalleryGridView) view.findViewById(R.id.gallery_grid_view);
        this.mAdapter = new GalleryGridAdapter(getContext(), null);
        this.mAdapter.setHostInterface(this.mGalleryGridView);
        this.mGalleryGridView.setAdapter(this.mAdapter);
        this.mGalleryGridView.setHostInterface(this);
        Fragment fragment;
        if (HwMessageUtils.isSplitOn() && (this.mMediaPicker.getActivity() instanceof ConversationList)) {
            fragment = ((ConversationList) this.mMediaPicker.getActivity()).getRightFragment();
            if (fragment != null) {
                if (((ConversationList) this.mMediaPicker.getActivity()).getRightFragment() instanceof RcsGroupChatComposeMessageFragment) {
                    ((RcsGroupChatComposeMessageFragment) fragment).addMediaUpdateListener(this.mGalleryGridView.getRcsGroupMediaUpdateListener());
                    this.mGalleryGridView.initRcsGroupImageSelection(((RcsGroupChatComposeMessageFragment) fragment).getRichEditor().getMediaModelData());
                } else {
                    ((ComposeMessageFragment) fragment).addMediaUpdateListener(this.mGalleryGridView.getMediaUpdateListener());
                    this.mGalleryGridView.initImageSelection(((ComposeMessageFragment) fragment).getRichEditor().getSlideshow());
                }
            }
        } else if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
            fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
            if (fragment != null) {
                ((RcsGroupChatComposeMessageFragment) fragment).addMediaUpdateListener(this.mGalleryGridView.getRcsGroupMediaUpdateListener());
                this.mGalleryGridView.initRcsGroupImageSelection(((RcsGroupChatComposeMessageFragment) fragment).getRichEditor().getMediaModelData());
            }
        } else {
            fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
            if (fragment != null) {
                ((ComposeMessageFragment) fragment).addMediaUpdateListener(this.mGalleryGridView.getMediaUpdateListener());
                this.mGalleryGridView.initImageSelection(((ComposeMessageFragment) fragment).getRichEditor().getSlideshow());
            }
        }
        if (OsUtil.hasStoragePermission()) {
            startMediaPickerDataLoader();
        }
        this.mMissingPermissionView = view.findViewById(R.id.missing_permission_view);
        updateForPermissionState(OsUtil.hasStoragePermission());
        return view;
    }

    protected int getActionBarTitleResId() {
        return 1;
    }

    public void onDocumentPickerItemClicked() {
        this.mMediaPicker.dispatchPickerOperate(1001, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void updateActionBar(AbstractEmuiActionBar actionBar) {
        super.updateActionBar(actionBar);
        if (!(this.mGalleryGridView == null || actionBar == null || !this.mMediaPicker.isFullScreen())) {
            actionBar.setUseSelecteSize(this.mGalleryGridView.getSelectionCount());
        }
    }

    public void onMediaPickerDataUpdated(MediaPickerData mediaPickerData, Object data, int loaderId) {
        this.mBindingRef.ensureBound(mediaPickerData);
        Cursor rawCursor = null;
        if (data instanceof Cursor) {
            rawCursor = (Cursor) data;
        }
        new MatrixCursor(GalleryGridItemData.getSpecialItemCoulumns()).addRow(new Object[]{ThemeUtil.SET_NULL_STR});
        MergeCursor cursor = new MergeCursor(new Cursor[]{specialItemsCursor, rawCursor});
        GalleryCursorManager.get().swapGalleryCursor(cursor);
        if (this.mAdapter != null) {
            this.mAdapter.swapCursor(cursor);
        }
    }

    public void onResume() {
        if (OsUtil.hasStoragePermission()) {
            startMediaPickerDataLoader();
        }
    }

    protected void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected && !OsUtil.hasStoragePermission()) {
            this.mMediaPicker.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 4);
        }
    }

    private void startMediaPickerDataLoader() {
        ((MediaPickerData) this.mBindingRef.getData()).startLoader(1, this.mBindingRef, null, this);
    }

    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = false;
        if (requestCode == 4) {
            if (grantResults[0] == 0) {
                permissionGranted = true;
            }
            if (permissionGranted) {
                startMediaPickerDataLoader();
            }
            updateForPermissionState(permissionGranted);
        }
    }

    private void updateForPermissionState(boolean granted) {
        int i = 8;
        if (this.mGalleryGridView != null) {
            int i2;
            GalleryGridView galleryGridView = this.mGalleryGridView;
            if (granted) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            galleryGridView.setVisibility(i2);
            if (this.mMissingPermissionView != null) {
                View view = this.mMissingPermissionView;
                if (!granted) {
                    i = 0;
                }
                view.setVisibility(i);
            }
        }
    }

    private AttachmentSelectData createAttachmentData(Uri uriItem, String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return null;
        }
        AttachmentSelectData attachmentItem;
        if (contentType.startsWith("video")) {
            attachmentItem = new AttachmentSelectData(5);
        } else {
            attachmentItem = new AttachmentSelectData(2);
        }
        attachmentItem.setAttachmentUri(uriItem);
        return attachmentItem;
    }

    private AttachmentSelectData createImageCompressData(int position) {
        AttachmentSelectData attachmentItem = new AttachmentSelectData(2);
        attachmentItem.setPosition(position);
        return attachmentItem;
    }
}
