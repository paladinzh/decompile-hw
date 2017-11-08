package com.android.contacts.speeddial;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class GridItem extends RelativeLayout {
    private long mContactId;
    private boolean mIsPrivate;
    private long mPhotoId;
    private ContactPhotoManager mPhotoManager;
    private DefaultImageRequest mRequest = new DefaultImageRequest();

    public GridItem(Context context) {
        super(context);
    }

    public GridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
            ImageView lImage = (ImageView) findViewById(R.id.speeddial_contactimage);
            if (this.mPhotoManager != null) {
                int i;
                DefaultImageRequest request = null;
                if (this.mPhotoId <= 0) {
                    this.mRequest.identifier = String.valueOf(this.mContactId);
                    this.mRequest.isCircular = true;
                    request = this.mRequest;
                }
                ContactPhotoManager contactPhotoManager = this.mPhotoManager;
                long j = this.mPhotoId;
                if (this.mIsPrivate) {
                    i = 13;
                } else {
                    i = 9;
                }
                contactPhotoManager.loadThumbnail(lImage, j, false, request, i);
            }
        } catch (Exception e) {
            HwLog.e("SpeedDial", "There is exception when doing onLayout");
        }
    }

    public void setPhotoManager(ContactPhotoManager aPhotoManager) {
        this.mPhotoManager = aPhotoManager;
    }

    public void setIsPrivate(boolean aIsPrivate) {
        this.mIsPrivate = aIsPrivate;
    }

    public void setPhotoId(long aPhotoId) {
        this.mPhotoId = aPhotoId;
    }

    public void setContactId(long aContactId) {
        this.mContactId = aContactId;
    }
}
