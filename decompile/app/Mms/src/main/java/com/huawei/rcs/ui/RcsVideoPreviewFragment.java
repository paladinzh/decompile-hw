package com.huawei.rcs.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import java.io.File;

public class RcsVideoPreviewFragment extends HwBaseFragment {
    private Bitmap bp;
    private ImageView cancelBtn;
    private Intent compressIntent;
    private ImageView imv;
    private Uri mCurrentVideoUri;
    private String mFilePath;
    private ImageButton mPlayBtn;
    private View mRootView;
    private ImageView sendBtn;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.rcs_preview_video_layout, container, false);
        return this.mRootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mRootView != null) {
            this.imv = (ImageView) this.mRootView.findViewById(R.id.iv_img);
            this.cancelBtn = (ImageView) this.mRootView.findViewById(R.id.btn_cancel);
            this.sendBtn = (ImageView) this.mRootView.findViewById(R.id.btn_send);
            this.mPlayBtn = (ImageButton) this.mRootView.findViewById(R.id.btn_play);
        }
        this.compressIntent = getIntent();
        initImageView(this.compressIntent);
        setClickListener();
    }

    private void initImageView(Intent compressIntent) {
        this.mFilePath = compressIntent.getStringExtra("file_path");
        if (!TextUtils.isEmpty(this.mFilePath)) {
            File file = new File(this.mFilePath);
            if (file.exists()) {
                this.mCurrentVideoUri = RcsProfileUtils.getVideoContentUri(getContext(), file);
                this.bp = ThumbnailUtils.createVideoThumbnail(this.mFilePath, 0);
            }
            try {
                if (!(this.imv == null || this.bp == null)) {
                    this.imv.setImageBitmap(this.bp);
                }
            } catch (OutOfMemoryError e) {
            }
        }
    }

    private void setClickListener() {
        if (this.cancelBtn != null) {
            this.cancelBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    RcsVideoPreviewFragment.this.finishSelf(false);
                }
            });
        }
        if (this.sendBtn != null) {
            this.sendBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (RcsTransaction.isFileExist(RcsVideoPreviewFragment.this.mFilePath)) {
                        Intent intent = new Intent();
                        intent.putExtra("file_path", RcsVideoPreviewFragment.this.mFilePath);
                        RcsVideoPreviewFragment.this.getController().setResult(RcsVideoPreviewFragment.this, -1, intent);
                        RcsVideoPreviewFragment.this.finishSelf(false);
                        return;
                    }
                    Toast.makeText(RcsVideoPreviewFragment.this.getContext(), R.string.text_file_not_exist, 0).show();
                }
            });
        }
        if (this.mPlayBtn != null) {
            this.mPlayBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (RcsVideoPreviewFragment.this.mCurrentVideoUri == null) {
                        MLog.w("RcsVideoPreviewActivity FileTrans: ", "null == mCurrentVideoUri");
                        return;
                    }
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.setDataAndType(RcsVideoPreviewFragment.this.mCurrentVideoUri, "video/*");
                    try {
                        RcsVideoPreviewFragment.this.startActivityForResult(intent, 142);
                    } catch (Throwable ex) {
                        MLog.e("RcsVideoPreviewActivity FileTrans: ", "Couldn't view video " + RcsVideoPreviewFragment.this.mCurrentVideoUri, ex);
                    }
                }
            });
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.bp != null && !this.bp.isRecycled()) {
            this.bp.recycle();
        }
    }
}
