package com.huawei.gallery.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.MultiWindowStatusHolder.IMultiWindowModeChangeListener;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.Locale;

public class UserGuardPage extends Activity {
    private IMultiWindowModeChangeListener mMultiWindowModeChangeListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_guard);
        TextView positiveButton = (TextView) findViewById(R.id.positive_button);
        TextView negativeButton = (TextView) findViewById(R.id.negative_button);
        positiveButton.setText(getResources().getString(R.string.user_guard_login).toUpperCase(Locale.getDefault()));
        negativeButton.setText(getResources().getString(R.string.paste_samename_jumpover).toUpperCase(Locale.getDefault()));
        positiveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    UserGuardPage.this.startActivityForResult(new Intent("com.huawei.hicloud.action.GALLERY_LOGIN"), 0);
                } catch (ActivityNotFoundException e) {
                    GalleryLog.e("photoshareLogTag", "com.huawei.hicloud.action.GALLERY_LOGIN can not find Activity");
                } finally {
                    UserGuardPage.disableShow(UserGuardPage.this.getApplicationContext());
                    UserGuardPage.this.setResult(-1);
                }
            }
        });
        negativeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UserGuardPage.disableShow(UserGuardPage.this.getApplicationContext());
                UserGuardPage.this.setResult(-1);
                UserGuardPage.this.finish();
            }
        });
        this.mMultiWindowModeChangeListener = new IMultiWindowModeChangeListener() {
            public void multiWindowModeChangeCallback(boolean isInMultiWindowMode) {
                UserGuardPage.this.setLayoutParam(isInMultiWindowMode);
            }
        };
        MultiWindowStatusHolder.registerMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener, true);
    }

    public void onBackPressed() {
        setResult(1104);
        super.onBackPressed();
    }

    protected void onDestroy() {
        MultiWindowStatusHolder.unregisterMultiWindowModeChangeListener(this.mMultiWindowModeChangeListener);
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static boolean isNeedShow(Context context) {
        return context.getSharedPreferences("user-guard-record", 0).getBoolean("key-show-user-guard", true);
    }

    public static void disableShow(Context context) {
        Editor editor = context.getSharedPreferences("user-guard-record", 0).edit();
        editor.putBoolean("key-show-user-guard", false);
        editor.commit();
        PhotoShareUtils.saveClickTime(context, true);
    }

    private void setLayoutParam(boolean isInMultiWindowMode) {
        ImageView guardImage = (ImageView) findViewById(R.id.guard_view);
        TextView title = (TextView) findViewById(R.id.guard_description);
        TextView description = (TextView) findViewById(R.id.guard_title);
        LayoutParams imageLayoutParams = (LayoutParams) guardImage.getLayoutParams();
        ViewGroup.LayoutParams titleLayoutParams = title.getLayoutParams();
        ViewGroup.LayoutParams descriptionLayoutParams = description.getLayoutParams();
        int imageTopMargin = getResources().getDimensionPixelSize(R.dimen.user_guard_title_top_margin);
        int imageWidth = getResources().getDimensionPixelSize(R.dimen.user_guard_picture_width);
        int imageHeight = getResources().getDimensionPixelSize(R.dimen.user_guard_picture_height);
        int imageBottomMargin = getResources().getDimensionPixelSize(R.dimen.user_guard_picture_bottom_margin);
        int labelMargin = getResources().getDimensionPixelSize(R.dimen.user_guard_margin_18);
        if (isInMultiWindowMode) {
            imageLayoutParams.width = imageWidth / 2;
            imageLayoutParams.height = imageHeight / 2;
            if (titleLayoutParams instanceof LayoutParams) {
                imageLayoutParams.bottomMargin = imageBottomMargin / 2;
                ((LayoutParams) titleLayoutParams).topMargin = imageTopMargin / 2;
                ((LayoutParams) descriptionLayoutParams).topMargin = labelMargin / 2;
            } else {
                ((LinearLayout.LayoutParams) descriptionLayoutParams).topMargin = labelMargin / 2;
            }
        } else {
            imageLayoutParams.width = imageWidth;
            imageLayoutParams.height = imageHeight;
            if (titleLayoutParams instanceof LayoutParams) {
                imageLayoutParams.bottomMargin = imageBottomMargin;
                ((LayoutParams) titleLayoutParams).topMargin = imageTopMargin;
                ((LayoutParams) descriptionLayoutParams).topMargin = labelMargin;
            } else {
                ((LinearLayout.LayoutParams) descriptionLayoutParams).topMargin = labelMargin;
            }
        }
        guardImage.setLayoutParams(imageLayoutParams);
        title.setLayoutParams(titleLayoutParams);
        description.setLayoutParams(descriptionLayoutParams);
    }
}
