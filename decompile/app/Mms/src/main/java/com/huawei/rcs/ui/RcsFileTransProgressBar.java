package com.huawei.rcs.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.gms.R;

public class RcsFileTransProgressBar extends RelativeLayout {
    private RcsRoundProgressBar bar;
    private ImageView imageView;
    private boolean isFadingOnProgress = false;
    private ImageView mFileIcon;
    private RelativeLayout mMediaInfo;
    private TextView mSizeText;
    private boolean opacity = false;

    public RcsFileTransProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public RcsFileTransProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RcsFileTransProgressBar(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.rcs_progressbarview, this, true);
        this.bar = (RcsRoundProgressBar) findViewById(R.id.squareProgressBar1);
        this.bar.bringToFront();
        hideProgressBar();
        this.mMediaInfo = (RelativeLayout) findViewById(R.id.rcs_media_info);
        this.mSizeText = (TextView) this.mMediaInfo.findViewById(R.id.rcs_media_info_size);
        this.mFileIcon = (ImageView) findViewById(R.id.rcs_file_icon);
        hideMediaFileInfo();
        hideFileIcon();
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.imageView = (ImageView) findViewById(R.id.imageView1);
        this.imageView.setImageBitmap(bitmap);
        this.bar.bringToFront();
    }

    public void setProgress(int progress) {
        this.bar.setVisibility(0);
        this.bar.setAlpha(255.0f);
        this.bar.setProgress(progress);
        if (!this.opacity) {
            setOpacity(100);
        } else if (this.isFadingOnProgress) {
            setOpacity(100 - progress);
        } else {
            setOpacity(progress);
        }
        this.bar.bringToFront();
    }

    public void hideProgressBar() {
        this.bar.setVisibility(8);
    }

    private void setOpacity(int progress) {
        if (this.imageView != null) {
            this.imageView.setAlpha((int) (((double) progress) * 2.55d));
        }
    }

    public void showMediaFileInfo(String str) {
        this.mSizeText.setText(str);
        this.mMediaInfo.setVisibility(0);
        hideProgressBar();
    }

    public void hideMediaFileInfo() {
        this.mMediaInfo.setVisibility(8);
    }

    public void showFileIcon(int fileType, Context context) {
        this.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.rcs_file_backgroud));
        this.imageView.setVisibility(0);
        switch (fileType) {
            case 7:
                this.mFileIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.rcs_image_icon));
                this.mFileIcon.setVisibility(0);
                return;
            case 8:
                this.mFileIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.mms_ic_item_video));
                this.mFileIcon.setVisibility(0);
                return;
            case 9:
                this.mFileIcon.setImageResource(R.drawable.ic_sms_audio);
                this.mFileIcon.setVisibility(0);
                return;
            case 20:
                this.mFileIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.rcs_file_icon));
                this.mFileIcon.setVisibility(0);
                return;
            default:
                return;
        }
    }

    public void hideFileIcon() {
        this.mFileIcon.setVisibility(8);
    }

    public void showVideoIcon(Context context) {
        this.mFileIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.cs_btn_default_disabled_emui));
        this.mFileIcon.setVisibility(0);
    }

    public void showRejectIcon(Context context) {
        this.imageView = (ImageView) findViewById(R.id.imageView1);
        this.mFileIcon = (ImageView) findViewById(R.id.rcs_file_icon);
        if (this.imageView != null && this.mFileIcon != null && context != null) {
            this.imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.rcs_file_backgroud));
            this.imageView.setVisibility(0);
            this.mFileIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.cs_beijing));
            this.mFileIcon.setVisibility(0);
        }
    }
}
