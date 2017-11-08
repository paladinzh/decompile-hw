package com.android.contacts.util;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.lettertiles.LetterTileDrawable;
import com.android.contacts.model.Contact;
import java.util.Arrays;

public class ImageViewDrawableSetter {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final String TAG = ImageViewDrawableSetter.class.getSimpleName();
    private byte[] mCompressed;
    private Contact mContact;
    private int mDurationInMillis = 0;
    private boolean mIsPrivate;
    private Drawable mPreviousDrawable;
    private ImageView mTarget;

    public Bitmap setupContactPhoto(Contact contactData, ImageView photoView) {
        this.mContact = contactData;
        setTarget(photoView);
        if (DEBUG) {
            HwLog.d(TAG, "setupContactPhoto photoView width:" + photoView.getWidth());
        }
        return setCompressedImage(contactData.mPhotoBinaryData);
    }

    public void setupContactPhoto(Contact contactData, ImageView photoView, View overLayView) {
        if (contactData != null && photoView != null) {
            setupContactPhoto(contactData, photoView);
        }
    }

    public ImageView getTarget() {
        return this.mTarget;
    }

    protected void setTarget(ImageView target) {
        if (this.mTarget != target) {
            this.mTarget = target;
            this.mCompressed = null;
            this.mPreviousDrawable = null;
        }
    }

    protected byte[] getCompressedImage() {
        return this.mCompressed;
    }

    protected Bitmap setCompressedImage(byte[] compressed) {
        boolean z = true;
        if (this.mPreviousDrawable != null && this.mCompressed != null && Arrays.equals(this.mCompressed, compressed)) {
            return previousBitmap();
        }
        Drawable newDrawable;
        if (compressed == null) {
            newDrawable = defaultDrawable();
        } else {
            newDrawable = decodedBitmapDrawable(compressed);
        }
        this.mCompressed = compressed;
        if (newDrawable == null) {
            return previousBitmap();
        }
        if (this.mPreviousDrawable == null || this.mDurationInMillis == 0) {
            if (DEBUG) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("(mPreviousDrawable == null || mDurationInMillis == 0):");
                if (!(this.mPreviousDrawable == null || this.mDurationInMillis == 0)) {
                    z = false;
                }
                HwLog.d(str, append.append(z).toString());
            }
            this.mTarget.setImageDrawable(newDrawable);
        } else {
            TransitionDrawable transition = new TransitionDrawable(new Drawable[]{this.mPreviousDrawable, newDrawable});
            if (compressed == null) {
                this.mTarget.setImageDrawable(transition);
            } else {
                this.mTarget.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(transition));
            }
            transition.startTransition(this.mDurationInMillis);
        }
        this.mPreviousDrawable = newDrawable;
        return previousBitmap();
    }

    private Bitmap previousBitmap() {
        if (this.mPreviousDrawable == null) {
            return null;
        }
        if (this.mPreviousDrawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) this.mPreviousDrawable).getBitmap();
        }
        if (this.mPreviousDrawable instanceof RoundedBitmapDrawable) {
            return ((RoundedBitmapDrawable) this.mPreviousDrawable).getBitmap();
        }
        if (this.mPreviousDrawable instanceof LetterTileDrawable) {
            return ContactPhotoManager.getLetterTileDrawableBitmap((LetterTileDrawable) this.mPreviousDrawable);
        }
        return null;
    }

    private Drawable defaultDrawable() {
        Resources resources = this.mTarget.getResources();
        if (this.mContact != null && !this.mContact.isUserProfile() && this.mContact.getId() > 0 && EmuiFeatureManager.isSupportMultiColorPhoto()) {
            return ContactPhotoManager.getDefaultAvatarDrawableForContact(resources, true, new DefaultImageRequest(this.mContact.getDisplayName(), String.valueOf(this.mContact.getId()), 2, true));
        }
        int resId = ContactPhotoManager.getDefaultAvatarResId(true, false);
        if (this.mIsPrivate) {
            Bitmap lBitmap = ContactPhotoManager.DEFAULT_AVATAR.getDefaultImageBitmapForPrivateContact(this.mTarget.getWidth(), this.mTarget.getHeight(), resId);
            if (lBitmap != null) {
                return new BitmapDrawable(resources, lBitmap);
            }
        }
        try {
            return resources.getDrawable(resId);
        } catch (NotFoundException e) {
            HwLog.wtf(TAG, "Cannot load default avatar resource.");
            return null;
        }
    }

    private Drawable decodedBitmapDrawable(byte[] compressed) {
        Resources rsrc = this.mTarget.getResources();
        Bitmap aSource = BitmapFactory.decodeByteArray(compressed, 0, compressed.length);
        if (aSource != null) {
            return new BitmapDrawable(rsrc, aSource);
        }
        return null;
    }
}
