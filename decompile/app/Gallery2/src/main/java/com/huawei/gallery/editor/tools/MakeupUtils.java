package com.huawei.gallery.editor.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ImageFileNamer;
import java.util.ArrayList;

public class MakeupUtils {
    public static Intent buildPituIntent(Context mContext, Uri mUri) {
        String uri = GalleryUtils.convertUriToPath(mContext, mUri);
        if (uri == null) {
            return null;
        }
        int index = uri.lastIndexOf("/");
        ArrayList<Uri> files = new ArrayList();
        files.add(Uri.parse(uri));
        Intent intent = new Intent();
        intent.setPackage("com.tencent.ttpic4huawei");
        intent.setAction("android.intent.action.SEND_MULTIPLE");
        intent.addCategory("com.tencent.ttpic4huawei.intent.category.OPENAPI");
        intent.putExtra("invoked_to_module", Uri.parse("pitu://TTPTCOSMETICS"));
        intent.putParcelableArrayListExtra("android.intent.extra.STREAM", files);
        intent.putExtra("output", Uri.parse(uri.substring(0, index + 1) + ImageFileNamer.generateName() + ".jpg"));
        return intent;
    }

    public static boolean isPituSupport(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        try {
            ApplicationInfo info = pm.getApplicationInfo("com.tencent.ttpic4huawei", 0);
            if (!(info == null || (info.flags & 1) == 0)) {
                return true;
            }
        } catch (NameNotFoundException e) {
        }
        return false;
    }
}
