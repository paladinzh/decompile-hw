package com.huawei.systemmanager.rainbow.comm.base;

import android.net.Uri;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.ProviderSubSegment;

public class CloudProviderUtils {
    public static String wildcardSegment(String segmentName) {
        return segmentName + "/*";
    }

    public static Uri generateGFeatureUri(String realPrefix) {
        return Uri.withAppendedPath(Uri.withAppendedPath(CloudProviderConst.CLOUD_AUTHORITY_URI, ProviderSubSegment.GFEATURE_PROVIDER_SEGMENT), realPrefix);
    }

    public static Uri generateViewCopyTableUri(String tableName) {
        return Uri.withAppendedPath(Uri.withAppendedPath(CloudProviderConst.CLOUD_AUTHORITY_URI, ProviderSubSegment.VIEWCOPY_PROVIDER_SEGMENT), tableName);
    }

    public static Uri generateApplistTableUri(String tableName) {
        return Uri.withAppendedPath(Uri.withAppendedPath(CloudProviderConst.CLOUD_AUTHORITY_URI, ProviderSubSegment.APPLIST_PROVIDER_SEGMENT), tableName);
    }

    public static Uri generateExtendViewUri(String viewName) {
        return Uri.withAppendedPath(Uri.withAppendedPath(CloudProviderConst.CLOUD_AUTHORITY_URI, ProviderSubSegment.EXTEND_VIEW_PROVIDER_SEGMENT), viewName);
    }
}
