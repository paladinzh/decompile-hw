package com.android.mms.ui;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.data.HwCustConversation;
import com.android.mms.util.HwCustUiUtils;
import com.android.mms.util.ItemLoadedCallback;
import com.android.mms.util.ItemLoadedFuture;
import com.android.mms.util.ThumbnailManager;
import com.android.mms.util.ThumbnailManager.ImageLoaded;
import com.google.android.gms.R;

public class HwCustConversationListItemImpl extends HwCustConversationListItem {
    private static final String MMS_URI = "content://mms/part/";
    private static final String TAG = "HwCustConversationListItemImpl";
    private GifView mAttachImage;
    private ImageLoadedCallback mImageLoadedCallback;
    private ItemLoadedFuture mItemLoadedFuture;
    private Uri mUri;

    private class ImageLoadedCallback implements ItemLoadedCallback<ImageLoaded> {
        private long mConversationId;
        private final ConversationListItem mListItem;

        public ImageLoadedCallback(ConversationListItem aListItem) {
            this.mListItem = aListItem;
            if (aListItem != null) {
                this.mConversationId = this.mListItem.getItemId();
            }
        }

        public void reset(ConversationListItem aListItem) {
            if (aListItem != null) {
                this.mConversationId = aListItem.getItemId();
            }
        }

        public void onItemLoaded(ImageLoaded imageLoaded, Throwable exception) {
            if (HwCustConversationListItemImpl.this.mItemLoadedFuture != null) {
                synchronized (HwCustConversationListItemImpl.this.mItemLoadedFuture) {
                    HwCustConversationListItemImpl.this.mItemLoadedFuture.setIsDone(true);
                }
            }
            if (this.mListItem != null && this.mListItem.getItemId() == this.mConversationId) {
                HwCustConversationListItemImpl.this.mAttachImage.setImageBitmap(imageLoaded.mBitmap, 16.0f);
                HwCustConversationListItemImpl.this.mAttachImage.setVisibility(0);
            }
        }
    }

    public HwCustConversationListItemImpl(Context context) {
        super(context);
    }

    public void showThumbnailAttachment(ConversationListItem aConversationListItem, HwCustConversation aHwCustConversation, boolean aHasAttachment) {
        if (aHwCustConversation != null && HwCustUiUtils.THUMBNAIL_SUPPORT) {
            updateThumbnailAttachment(aConversationListItem, aHwCustConversation, aHasAttachment);
        }
    }

    public void initlizeThumbnailAttachment(ConversationListItem aConversationListItem) {
        if (HwCustUiUtils.THUMBNAIL_SUPPORT) {
            this.mAttachImage = (GifView) aConversationListItem.findViewById(R.id.attach_img);
        }
    }

    private void updateThumbnailAttachment(ConversationListItem aConversationListItem, HwCustConversation aHwCustConversation, boolean aHasAttachment) {
        if (!TextUtils.isEmpty(aHwCustConversation.getThumbnailPath()) && aHasAttachment) {
            ThumbnailManager thumbnailManager = MmsApp.getApplication().getThumbnailManager();
            if (this.mImageLoadedCallback == null) {
                this.mImageLoadedCallback = new ImageLoadedCallback(aConversationListItem);
            } else {
                this.mImageLoadedCallback.reset(aConversationListItem);
            }
            if (thumbnailManager != null) {
                String[] lThumbNailPathSplit = aHwCustConversation.getThumbnailPath().split(";");
                if (lThumbNailPathSplit.length > 1) {
                    this.mUri = getUri(lThumbNailPathSplit[0]);
                    if (isVideoFile(lThumbNailPathSplit[1])) {
                        this.mItemLoadedFuture = thumbnailManager.getVideoThumbnail(this.mUri, this.mImageLoadedCallback);
                    } else {
                        this.mItemLoadedFuture = thumbnailManager.getThumbnail(this.mUri, this.mImageLoadedCallback);
                    }
                }
            }
        } else if (aHasAttachment) {
            this.mAttachImage.setImageDrawable(aConversationListItem.getResources().getDrawable(R.drawable.cs_pass_display));
            this.mAttachImage.setVisibility(0);
        } else {
            this.mAttachImage.setVisibility(8);
        }
    }

    private Uri getUri(String aFilePath) {
        return Uri.parse(MMS_URI + aFilePath);
    }

    public int getAttachmentVisiblity(int aVisiblity) {
        if (HwCustUiUtils.THUMBNAIL_SUPPORT) {
            return 8;
        }
        return aVisiblity;
    }

    private boolean isVideoFile(String path) {
        return path != null ? path.contains("video") : false;
    }

    public void release() {
        if (HwCustUiUtils.THUMBNAIL_SUPPORT && this.mItemLoadedFuture != null && !this.mItemLoadedFuture.isDone() && this.mUri != null) {
            synchronized (this.mItemLoadedFuture) {
                this.mItemLoadedFuture.cancel(this.mUri);
                this.mItemLoadedFuture = null;
            }
        }
    }
}
