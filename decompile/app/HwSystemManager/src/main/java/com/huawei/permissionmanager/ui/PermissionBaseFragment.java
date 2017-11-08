package com.huawei.permissionmanager.ui;

import android.app.ListFragment;
import android.content.Context;
import java.util.List;

public abstract class PermissionBaseFragment extends ListFragment {
    private static final String LOG_TAG = "PermissionBaseFragment";
    private static final long UPDATE_DIALOG_SHOW_TIME_MIN = 1500;
    protected Context mContext = null;
    protected List<AppInfoWrapper> mPermissonAppsList = null;
    protected RecommendManager mRecommendManager;

    protected com.huawei.permissionmanager.utils.RecommendMutilAppItem getRecommendItem(com.huawei.permissionmanager.ui.AppInfoWrapper r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.ui.PermissionBaseFragment.getRecommendItem(com.huawei.permissionmanager.ui.AppInfoWrapper):com.huawei.permissionmanager.utils.RecommendMutilAppItem
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.ui.PermissionBaseFragment.getRecommendItem(com.huawei.permissionmanager.ui.AppInfoWrapper):com.huawei.permissionmanager.utils.RecommendMutilAppItem");
    }

    protected abstract void removeRecommendHeader();

    protected abstract void updateUIAfterRecommendOperation(List<AppInfoWrapper> list);

    protected void updateRecommendAppsInfo() {
    }
}
