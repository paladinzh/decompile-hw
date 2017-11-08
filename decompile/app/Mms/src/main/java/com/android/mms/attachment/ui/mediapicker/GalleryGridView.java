package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.BaseSavedState;
import com.android.mms.attachment.datamodel.data.GalleryGridItemData;
import com.android.mms.attachment.ui.PersistentInstanceState;
import com.android.mms.attachment.ui.mediapicker.GalleryGridItemView.HostInterface;
import com.android.mms.attachment.utils.ContentType;
import com.android.mms.model.MediaModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.ui.ComposeMessageFragment.MediaUpdateListener;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.RichMessageEditor;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment.RcsGroupMediaUpdateListener;
import com.android.rcs.ui.RcsGroupChatRichMessageEditor;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.StatisticalHelper;
import java.util.ArrayList;
import java.util.List;

public class GalleryGridView extends MediaPickerGridView implements HostInterface, PersistentInstanceState {
    private boolean mIsMultiSelectMode = true;
    private GalleryGridViewListener mListener;
    private MediaUpdateListener mMediaUpdateListener = new MediaUpdateListener() {
        public void updateMedia(RichMessageEditor richMessageEditor) {
            GalleryGridView.this.refreshImageSelectionStateOnAttachmentChange(richMessageEditor);
        }

        public boolean hasUpdateMedida(int changedType) {
            if (changedType == 13 || changedType == 2 || changedType == 5) {
                return true;
            }
            return false;
        }
    };
    private RcsGroupMediaUpdateListener mRcsGroupMediaUpdateListener = new RcsGroupMediaUpdateListener() {
        public boolean hasUpdateMedida(int changedType) {
            if (changedType == 13 || changedType == 2 || changedType == 5) {
                return true;
            }
            return false;
        }

        public void updateMedia(RcsGroupChatRichMessageEditor rcsGroupRichMessageEditor) {
            GalleryGridView.this.refreshRcsGroupImageSelectionStateOnAttachmentChange(rcsGroupRichMessageEditor);
        }
    };
    private final ArrayList<String> mSelectedImages = new ArrayList();

    public interface GalleryGridViewListener {
        void onCompressImage(int i);

        void onDocumentPickerItemClicked();

        void onItemSelected(Uri uri, String str);

        void onItemUnselected(Uri uri, String str);

        void onUpdate();
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean isMultiSelectMode;
        String[] selectedImages;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = true;
            super(in);
            if (in.readInt() != 1) {
                z = false;
            }
            this.isMultiSelectMode = z;
            int partCount = in.readInt();
            this.selectedImages = new String[partCount];
            for (int i = 0; i < partCount; i++) {
                this.selectedImages[i] = in.readString();
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            int i;
            int i2 = 0;
            super.writeToParcel(out, flags);
            if (this.isMultiSelectMode) {
                i = 1;
            } else {
                i = 0;
            }
            out.writeInt(i);
            out.writeInt(this.selectedImages.length);
            String[] strArr = this.selectedImages;
            int length = strArr.length;
            while (i2 < length) {
                out.writeString(strArr[i2]);
                i2++;
            }
        }
    }

    public GalleryGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHostInterface(GalleryGridViewListener hostInterface) {
        this.mListener = hostInterface;
    }

    public void onItemClicked(View view, GalleryGridItemData data, boolean longClick) {
        if (data.isDocumentPickerItem()) {
            this.mListener.onDocumentPickerItemClicked();
            StatisticalHelper.incrementReportCount(getContext(), 2255);
            return;
        }
        this.mListener.onCompressImage(data.getCurrentPosition());
        StatisticalHelper.incrementReportCount(getContext(), 2257);
    }

    public void onCheckBoxClicked(View view, GalleryGridItemData data, boolean longClick) {
        if (ContentType.isMediaType(data.getContentType())) {
            MessageUtils.setIsMediaPanelInScrollingStatus(false);
            Rect startRect = new Rect();
            view.getGlobalVisibleRect(startRect);
            if (isMultiSelectEnabled()) {
                toggleItemSelection(startRect, data);
                return;
            } else {
                this.mListener.onItemSelected(data.getImageUri(), data.getContentType());
                return;
            }
        }
        MLog.w("GalleryGridView", "Selected item has invalid contentType " + data.getContentType());
    }

    public boolean isItemSelected(GalleryGridItemData data) {
        if (data == null || data.getImageUri() == null) {
            return false;
        }
        return this.mSelectedImages.contains(data.getImageUri().getPath());
    }

    int getSelectionCount() {
        return this.mSelectedImages.size();
    }

    public boolean isMultiSelectEnabled() {
        return this.mIsMultiSelectMode;
    }

    private void toggleItemSelection(Rect startRect, GalleryGridItemData data) {
        if (isItemSelected(data)) {
            this.mSelectedImages.remove(data.getImageUri().getPath());
            this.mListener.onItemUnselected(data.getImageUri(), data.getContentType());
        } else {
            this.mSelectedImages.add(data.getImageUri().getPath());
            this.mListener.onItemSelected(data.getImageUri(), data.getContentType());
        }
        invalidateViews();
    }

    public void onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    private void refreshImageSelectionStateOnAttachmentChange(RichMessageEditor richMessageEditor) {
        boolean changed = false;
        if (richMessageEditor != null) {
            SlideshowModel slideshowmode = richMessageEditor.getSlideshow();
            if (slideshowmode != null && slideshowmode.size() != 0) {
                ArrayList<String> richImageSourceBuilds = slideshowmode.getImageSourceBuilds();
                if (richImageSourceBuilds.size() != this.mSelectedImages.size()) {
                    this.mSelectedImages.clear();
                    this.mSelectedImages.addAll(richImageSourceBuilds);
                    changed = true;
                } else {
                    ArrayList<String> tempBuilds = new ArrayList();
                    tempBuilds.addAll(this.mSelectedImages);
                    for (String imagePath : tempBuilds) {
                        if (!richImageSourceBuilds.contains(imagePath)) {
                            this.mSelectedImages.remove(imagePath);
                            changed = true;
                        }
                    }
                    tempBuilds.clear();
                }
            } else if (this.mSelectedImages.size() > 0) {
                this.mSelectedImages.clear();
                changed = true;
            }
        }
        if (changed) {
            this.mListener.onUpdate();
            invalidateViews();
        }
    }

    private void refreshRcsGroupImageSelectionStateOnAttachmentChange(RcsGroupChatRichMessageEditor rcsGroupRichMessageEditor) {
        if (rcsGroupRichMessageEditor != null) {
            boolean changed = false;
            ArrayList<String> richImageSourceBuilds = new ArrayList();
            List<MediaModel> sourceBuilds = rcsGroupRichMessageEditor.getMediaModelData();
            if (sourceBuilds.size() > 0) {
                for (MediaModel mediaModel : sourceBuilds) {
                    richImageSourceBuilds.add(mediaModel.getSourceBuild());
                }
            }
            if (richImageSourceBuilds.size() != this.mSelectedImages.size()) {
                this.mSelectedImages.clear();
                this.mSelectedImages.addAll(richImageSourceBuilds);
                changed = true;
            } else {
                ArrayList<String> tempBuilds = new ArrayList();
                tempBuilds.addAll(this.mSelectedImages);
                for (String imagePath : tempBuilds) {
                    if (!richImageSourceBuilds.contains(imagePath)) {
                        this.mSelectedImages.remove(imagePath);
                        changed = true;
                    }
                }
                tempBuilds.clear();
            }
            if (changed) {
                this.mListener.onUpdate();
                invalidateViews();
            }
        }
    }

    public void initImageSelection(SlideshowModel slideshowModel) {
        if (slideshowModel != null && slideshowModel.size() > 0) {
            for (int i = 0; i < slideshowModel.size(); i++) {
                String sourceBuild = null;
                SlideModel slideModel = slideshowModel.get(i);
                if (slideModel != null && slideModel.hasImage()) {
                    sourceBuild = slideModel.getImage().getSourceBuild();
                } else if (slideModel != null && slideModel.hasVideo()) {
                    sourceBuild = slideModel.getVideo().getSourceBuild();
                }
                addSelectImage(sourceBuild);
            }
        }
    }

    public void initRcsGroupImageSelection(List<MediaModel> mListData) {
        if (mListData != null && mListData.size() > 0) {
            for (int i = 0; i < mListData.size(); i++) {
                String sourceBuild = null;
                MediaModel mediaModel = (MediaModel) mListData.get(i);
                if (mediaModel != null && mediaModel.isImage()) {
                    sourceBuild = mediaModel.getSourceBuild();
                } else if (mediaModel != null && mediaModel.isVideo()) {
                    sourceBuild = mediaModel.getSourceBuild();
                }
                addSelectImage(sourceBuild);
            }
        }
    }

    private void addSelectImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            this.mSelectedImages.add(imagePath);
        }
    }

    public Parcelable saveState() {
        return onSaveInstanceState();
    }

    public void restoreState(Parcelable restoredState) {
        onRestoreInstanceState(restoredState);
        invalidateViews();
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.isMultiSelectMode = this.mIsMultiSelectMode;
        if (this.mSelectedImages != null) {
            savedState.selectedImages = (String[]) this.mSelectedImages.toArray(new String[this.mSelectedImages.size()]);
        }
        return savedState;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            this.mIsMultiSelectMode = savedState.isMultiSelectMode;
            this.mSelectedImages.clear();
            for (Object add : savedState.selectedImages) {
                this.mSelectedImages.add(add);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public MediaUpdateListener getMediaUpdateListener() {
        return this.mMediaUpdateListener;
    }

    public RcsGroupMediaUpdateListener getRcsGroupMediaUpdateListener() {
        return this.mRcsGroupMediaUpdateListener;
    }
}
