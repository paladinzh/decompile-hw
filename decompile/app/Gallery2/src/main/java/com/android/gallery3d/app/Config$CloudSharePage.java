package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;

public class Config$CloudSharePage extends Config$LocalCameraAlbumPage {
    private static Config$CloudSharePage sCloudSharePageInstance;

    public static synchronized Config$CloudSharePage get(Context context) {
        Config$CloudSharePage config$CloudSharePage;
        synchronized (Config$CloudSharePage.class) {
            if (sCloudSharePageInstance == null) {
                sCloudSharePageInstance = new Config$CloudSharePage(context);
            }
            config$CloudSharePage = sCloudSharePageInstance;
        }
        return config$CloudSharePage;
    }

    private Config$CloudSharePage(Context context) {
        super(context);
        Resources r = context.getResources();
        this.titleLabelSpec.label_member_text_size = r.getDimensionPixelSize(R.dimen.cloud_share_member_text_size);
        this.titleLabelSpec.label_member_text_color = r.getColor(R.color.cloud_share_member_text_color);
        this.titleLabelSpec.label_share_gap = r.getDimensionPixelSize(R.dimen.cloud_share_gap);
        this.titleLabelSpec.label_share_text_size = r.getDimensionPixelSize(R.dimen.cloud_share_msg_text_size);
        this.titleLabelSpec.label_share_text_color = r.getColor(R.color.cloud_share_msg_text_color);
        if (this.titleLabelSpec.label_member_text_size > this.titleLabelSpec.label_share_text_size) {
            this.titleLabelSpec.label_min_height = Config$LocalCameraAlbumPage.getTitleHeight(context, this.titleLabelSpec, this.titleLabelSpec.mTimeTextHeight, GalleryUtils.getFontHeightOfPaint(this.titleLabelSpec.mPhotoShareMemberPaint));
        } else {
            this.titleLabelSpec.label_min_height = Config$LocalCameraAlbumPage.getTitleHeight(context, this.titleLabelSpec, this.titleLabelSpec.mTimeTextHeight, GalleryUtils.getFontHeightOfPaint(this.titleLabelSpec.mPhotoShareShareMsgPaint));
        }
        this.titleLabelSpec.label_tag = 3;
        this.slotViewSpec.titleHeight = this.titleLabelSpec.label_min_height;
    }
}
