package com.huawei.gallery.freeshare;

import android.content.DialogInterface.OnDismissListener;
import com.huawei.gallery.app.AbsPhotoPage.Model;
import com.huawei.gallery.util.NavigationBarHandler.Listener;

public interface FreeShare extends Listener {
    boolean doCancel(OnDismissListener onDismissListener);

    void doClean();

    void doHide();

    void doShow(int i, boolean z);

    boolean isShowing();

    void setModel(Model model);
}
