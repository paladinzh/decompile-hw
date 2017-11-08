package com.huawei.gallery.photoshare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.R;
import com.android.gallery3d.app.IntentChooser.IShareItem;
import com.huawei.gallery.photoshare.ui.ShareToCloudAlbumActivity;

public class PhotoShareItem implements IShareItem {
    private Drawable icon;
    private String label;
    private ComponentName mComponent;
    private Context mContext;

    public PhotoShareItem(Context context) {
        this.mContext = context;
        this.label = context.getString(R.string.share_album);
        this.icon = context.getResources().getDrawable(R.drawable.ic_photoshare);
        this.mComponent = new ComponentName(context, ShareToCloudAlbumActivity.class);
    }

    public String getLabel() {
        return this.label;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void onClicked(Intent intent) {
        this.mContext.startActivity(intent);
    }

    public int getKey() {
        return 0;
    }

    public String[] getSupportActions() {
        return new String[0];
    }
}
