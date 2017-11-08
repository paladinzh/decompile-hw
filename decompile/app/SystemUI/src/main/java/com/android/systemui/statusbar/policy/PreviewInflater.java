package com.android.systemui.statusbar.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.phone.KeyguardPreviewContainer;
import java.util.List;

public class PreviewInflater {
    private Context mContext;
    private LockPatternUtils mLockPatternUtils;

    private static class WidgetInfo {
        String contextPackage;
        int layoutId;

        private WidgetInfo() {
        }
    }

    public PreviewInflater(Context context, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mLockPatternUtils = lockPatternUtils;
    }

    public View inflatePreview(Intent intent) {
        return inflatePreview(getWidgetInfo(intent));
    }

    public View inflatePreviewFromService(ComponentName componentName) {
        return inflatePreview(getWidgetInfoFromService(componentName));
    }

    private KeyguardPreviewContainer inflatePreview(WidgetInfo info) {
        if (info == null) {
            return null;
        }
        View v = inflateWidgetView(info);
        if (v == null) {
            return null;
        }
        KeyguardPreviewContainer container = new KeyguardPreviewContainer(this.mContext, null);
        container.addView(v);
        return container;
    }

    private View inflateWidgetView(WidgetInfo widgetInfo) {
        View widgetView = null;
        try {
            Context appContext = this.mContext.createPackageContext(widgetInfo.contextPackage, 4);
            widgetView = ((LayoutInflater) appContext.getSystemService("layout_inflater")).cloneInContext(appContext).inflate(widgetInfo.layoutId, null, false);
        } catch (Exception e) {
            Log.w("PreviewInflater", "Error creating widget view", e);
        }
        return widgetView;
    }

    private WidgetInfo getWidgetInfoFromService(ComponentName componentName) {
        try {
            return getWidgetInfoFromMetaData(componentName.getPackageName(), this.mContext.getPackageManager().getServiceInfo(componentName, 128).metaData);
        } catch (NameNotFoundException e) {
            Log.w("PreviewInflater", "Failed to load preview; " + componentName.flattenToShortString() + " not found", e);
            return null;
        }
    }

    private WidgetInfo getWidgetInfoFromMetaData(String contextPackage, Bundle metaData) {
        if (metaData == null) {
            return null;
        }
        int layoutId = metaData.getInt("com.android.keyguard.layout");
        if (layoutId == 0) {
            return null;
        }
        WidgetInfo info = new WidgetInfo();
        info.contextPackage = contextPackage;
        info.layoutId = layoutId;
        return info;
    }

    private WidgetInfo getWidgetInfo(Intent intent) {
        PackageManager packageManager = this.mContext.getPackageManager();
        List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(intent, 65536, KeyguardUpdateMonitor.getCurrentUser());
        if (appList.size() == 0) {
            return null;
        }
        ResolveInfo resolved = packageManager.resolveActivityAsUser(intent, 65664, KeyguardUpdateMonitor.getCurrentUser());
        if (wouldLaunchResolverActivity(resolved, appList) || resolved == null || resolved.activityInfo == null) {
            return null;
        }
        return getWidgetInfoFromMetaData(resolved.activityInfo.packageName, resolved.activityInfo.metaData);
    }

    public static boolean wouldLaunchResolverActivity(Context ctx, Intent intent, int currentUserId) {
        return getTargetActivityInfo(ctx, intent, currentUserId) == null;
    }

    public static ActivityInfo getTargetActivityInfo(Context ctx, Intent intent, int currentUserId) {
        PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> appList = packageManager.queryIntentActivitiesAsUser(intent, 65536, currentUserId);
        if (appList.size() == 0) {
            return null;
        }
        ResolveInfo resolved = packageManager.resolveActivityAsUser(intent, 65664, currentUserId);
        if (resolved == null || wouldLaunchResolverActivity(resolved, appList)) {
            return null;
        }
        return resolved.activityInfo;
    }

    private static boolean wouldLaunchResolverActivity(ResolveInfo resolved, List<ResolveInfo> appList) {
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo tmp = (ResolveInfo) appList.get(i);
            if (tmp.activityInfo.name.equals(resolved.activityInfo.name) && tmp.activityInfo.packageName.equals(resolved.activityInfo.packageName)) {
                return false;
            }
        }
        return true;
    }
}
