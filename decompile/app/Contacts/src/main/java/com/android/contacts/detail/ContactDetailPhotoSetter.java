package com.android.contacts.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import com.android.contacts.activities.PhotoSelectionActivity;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.util.ImageViewDrawableSetter;
import com.google.android.gms.location.places.PlacesStatusCodes;

public class ContactDetailPhotoSetter extends ImageViewDrawableSetter {

    public static final class PhotoClickListener implements OnClickListener, OnLongClickListener {
        private final Contact mContactData;
        private final Context mContext;
        private final boolean mHasPhoto;
        private final Bitmap mPhotoBitmap;

        public PhotoClickListener(Context context, Contact contactData, Bitmap photoBitmap, boolean hasPhoto) {
            this.mContext = context;
            this.mContactData = contactData;
            this.mPhotoBitmap = photoBitmap;
            this.mHasPhoto = hasPhoto;
        }

        public void onClick(View v) {
            RawContactDeltaList delta = this.mContactData.createRawContactDeltaList();
            if (delta != null) {
                float appScale = this.mContext.getResources().getCompatibilityInfo().applicationScale;
                int[] pos = new int[2];
                v.getLocationOnScreen(pos);
                Rect rect = new Rect();
                rect.left = Float.valueOf(((float) pos[0]) * appScale).intValue();
                rect.top = Float.valueOf(((float) pos[1]) * appScale).intValue();
                rect.right = Float.valueOf(((float) (pos[0] + v.getWidth())) * appScale).intValue();
                rect.bottom = Float.valueOf(((float) (pos[1] + v.getHeight())) * appScale).intValue();
                Uri photoUri = null;
                if (this.mContactData.getPhotoUri() != null) {
                    photoUri = Uri.parse(this.mContactData.getPhotoUri());
                }
                Intent photoSelectionIntent = PhotoSelectionActivity.buildIntent(this.mContext, photoUri, this.mPhotoBitmap, this.mHasPhoto, rect, delta, this.mContactData.isUserProfile(), this.mContactData.isDirectoryEntry(), true, this.mContactData.getPhotoId());
                if (this.mContext instanceof Activity) {
                    ((Activity) this.mContext).startActivityForResult(photoSelectionIntent, PlacesStatusCodes.USAGE_LIMIT_EXCEEDED);
                } else {
                    this.mContext.startActivity(photoSelectionIntent);
                }
            }
        }

        public boolean onLongClick(View v) {
            onClick(v);
            return false;
        }
    }

    public PhotoClickListener setupContactPhotoForClick(Context context, Contact contactData, ImageView photoView) {
        Bitmap bitmap = setupContactPhoto(contactData, photoView);
        String lAccountType = ((RawContact) contactData.getRawContacts().get(0)).getAccountTypeString();
        if (!contactData.isWritableContact(context) || "com.android.huawei.sim".equals(lAccountType) || "com.android.huawei.secondsim".equals(lAccountType)) {
            return null;
        }
        return setupClickListener(context, contactData, bitmap, false);
    }

    private PhotoClickListener setupClickListener(Context context, Contact contactData, Bitmap bitmap, boolean expandPhotoOnClick) {
        if (getTarget() == null) {
            return null;
        }
        return new PhotoClickListener(context, contactData, bitmap, getCompressedImage() != null);
    }
}
