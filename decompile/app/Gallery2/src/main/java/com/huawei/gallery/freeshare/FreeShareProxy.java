package com.huawei.gallery.freeshare;

import android.content.DialogInterface.OnDismissListener;
import android.view.ViewGroup;
import com.android.gallery3d.app.GalleryContext;
import com.huawei.gallery.app.AbsPhotoPage.Model;

public abstract class FreeShareProxy {

    public static class FreeShareStub implements FreeShare {
        public void doShow(int launchmode, boolean supportShare) {
        }

        public void doHide() {
        }

        public boolean isShowing() {
            return false;
        }

        public void setModel(Model model) {
        }

        public boolean doCancel(OnDismissListener listener) {
            return false;
        }

        public void doClean() {
        }

        public void onNavigationBarChanged(boolean show, int height) {
        }
    }

    public static FreeShare get(GalleryContext context, FreeShareAdapter adapter, ViewGroup root) {
        if (adapter == null) {
            return new FreeShareStub();
        }
        return new FreeShareHost(context.getActivityContext(), adapter, root);
    }
}
