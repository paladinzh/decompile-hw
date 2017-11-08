package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import java.io.File;

public class HwCustRegulatoryInfoDisplayImpl extends HwCustRegulatoryInfoDisplay {
    private static final String REGULATORY_RESOURCE_DEFAULTPATH = "system/media/image.png";
    private static final String REGULATORY_RESOURCE_PATH = "regulatory_resource_path";
    File mRegulatoryFile = null;
    final String regulatoryInfoPicturePath = System.getString(this.mContext.getContentResolver(), REGULATORY_RESOURCE_PATH);

    private static class ImageLoadingTask extends AsyncTask<File, Void, Bitmap> {
        Builder builder;
        View view;

        public ImageLoadingTask(Builder builder, View view) {
            this.builder = builder;
            this.view = view;
        }

        protected Bitmap doInBackground(File... file) {
            return BitmapFactory.decodeFile(file[0].getAbsolutePath());
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView image = (ImageView) this.view.findViewById(2131887073);
            if (bitmap != null && image != null) {
                image.setImageBitmap(bitmap);
                this.builder.setView(this.view);
                this.builder.show();
            }
        }
    }

    public HwCustRegulatoryInfoDisplayImpl(Context context) {
        super(context);
    }

    public boolean isImageFlagExits() {
        this.mRegulatoryFile = new File(REGULATORY_RESOURCE_DEFAULTPATH);
        if (this.mRegulatoryFile.exists()) {
            return true;
        }
        if (TextUtils.isEmpty(this.regulatoryInfoPicturePath)) {
            return false;
        }
        this.mRegulatoryFile = new File(this.regulatoryInfoPicturePath);
        if (this.mRegulatoryFile.exists()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setRegulatoryInfoImages(Builder builder, View view) {
        if (!(builder == null || view == null || this.mRegulatoryFile == null)) {
            new ImageLoadingTask(builder, view).execute(new File[]{this.mRegulatoryFile});
        }
    }
}
