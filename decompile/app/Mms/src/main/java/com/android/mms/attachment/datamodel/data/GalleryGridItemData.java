package com.android.mms.attachment.datamodel.data;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.android.mms.attachment.datamodel.media.FileImageRequestDescriptor;
import com.android.mms.attachment.datamodel.media.UriImageRequestDescriptor;
import com.android.mms.attachment.datamodel.media.VideoThumbnailRequestDescriptor;

public class GalleryGridItemData {
    private static final String[] IMAGE_PROJECTION = new String[]{"_id", "_data", "width", "height", "mime_type", "date_modified"};
    private static final String[] SPECIAL_ITEM_COLUMNS = new String[]{"_id"};
    private String mContentType;
    private int mCurrentPosition;
    private long mDateSeconds;
    private UriImageRequestDescriptor mImageData;
    private boolean mIsDocumentPickerItem;
    private boolean mIsVideo = false;

    public static String[] getImageProjection() {
        return (String[]) IMAGE_PROJECTION.clone();
    }

    public static String[] getSpecialItemCoulumns() {
        return (String[]) SPECIAL_ITEM_COLUMNS.clone();
    }

    public void bind(Cursor cursor, int desiredWidth, int desiredHeight) {
        this.mCurrentPosition = cursor.getPosition();
        this.mIsDocumentPickerItem = TextUtils.equals(cursor.getString(0), ThemeUtil.SET_NULL_STR);
        long mediaId = !TextUtils.isEmpty(cursor.getString(0)) ? Long.parseLong(cursor.getString(0)) : -1;
        if (this.mIsDocumentPickerItem) {
            this.mImageData = null;
            this.mContentType = null;
            return;
        }
        int sourceWidth = cursor.getInt(2);
        int sourceHeight = cursor.getInt(3);
        if (sourceWidth <= 0) {
            sourceWidth = -1;
        }
        if (sourceHeight <= 0) {
            sourceHeight = -1;
        }
        this.mContentType = cursor.getString(4);
        String dateModified = cursor.getString(5);
        this.mDateSeconds = !TextUtils.isEmpty(dateModified) ? Long.parseLong(dateModified) : -1;
        if (TextUtils.isEmpty(this.mContentType) || !this.mContentType.startsWith("video")) {
            this.mImageData = new FileImageRequestDescriptor(cursor.getString(1), desiredWidth, desiredHeight, sourceWidth, sourceHeight, true, true, true);
            return;
        }
        this.mImageData = new VideoThumbnailRequestDescriptor(mediaId, cursor.getString(1), desiredWidth, desiredHeight, sourceWidth, sourceHeight);
        this.mIsVideo = true;
    }

    public boolean isDocumentPickerItem() {
        return this.mIsDocumentPickerItem;
    }

    public Uri getImageUri() {
        if (this.mImageData == null) {
            return null;
        }
        return this.mImageData.uri;
    }

    public UriImageRequestDescriptor getImageRequestDescriptor() {
        return this.mImageData;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public int getCurrentPosition() {
        return this.mCurrentPosition;
    }

    public boolean isVideo() {
        return this.mIsVideo;
    }
}
