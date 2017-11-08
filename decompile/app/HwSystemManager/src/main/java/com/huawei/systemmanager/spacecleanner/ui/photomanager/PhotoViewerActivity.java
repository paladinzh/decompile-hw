package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.common.imageloader.core.DisplayImageOptions;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.ImageLoader;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.spacecleanner.ui.secondaryui.SecondaryConstant;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class PhotoViewerActivity extends HsmActivity {
    public static final String TAG = "PhotoViewerActivity";
    private String photoPath;
    private ImageView photoView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState != null || intent == null) {
            HwLog.d(TAG, "intent is invalidate or save");
            finish();
            return;
        }
        this.photoPath = intent.getStringExtra(SecondaryConstant.PHOTO_TRASH_PATH_EXTRA);
        if (TextUtils.isEmpty(this.photoPath)) {
            HwLog.d(TAG, "path is invalidate");
            finish();
            return;
        }
        setContentView(R.layout.photo_viewer);
        initViewer();
    }

    private void initViewer() {
        DisplayImageOptions options = new Builder().cacheInMemory(false).cacheOnDisk(false).considerExifParams(true).build();
        this.photoView = (ImageView) findViewById(R.id.photo_view);
        ImageLoader.getInstance().displayImage(Utility.getLocalPath(this.photoPath), this.photoView, options, null);
    }

    public static void startPhotoViewer(Context ct, String photoPath) {
        if (new File(photoPath).exists()) {
            Intent requestIntent = new Intent();
            requestIntent.setClass(GlobalContext.getContext(), PhotoViewerActivity.class);
            requestIntent.putExtra(SecondaryConstant.PHOTO_TRASH_PATH_EXTRA, photoPath);
            ct.startActivity(requestIntent);
            return;
        }
        Toast.makeText(GlobalContext.getContext(), GlobalContext.getContext().getString(R.string.space_clean_photo_not_exits_tip, new Object[]{photoPath}), 0).show();
    }
}
