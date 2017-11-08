package com.huawei.gallery.share;

import android.app.Dialog;
import android.content.Intent;
import com.huawei.gallery.share.HwResolverView.DisplayResolveInfo;
import java.util.ArrayList;

public interface Delegate {
    boolean alwaysUseOption();

    Dialog getDialog();

    ArrayList<DisplayResolveInfo> getGalleryShareItem();

    Intent getIntent();
}
