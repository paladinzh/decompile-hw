package com.huawei.systemmanager.rainbow.comm.base;

import android.net.Uri;

public final class CloudProviderConst {
    public static final String CLOUD_AUTHORITY = "com.huawei.systemmanager.rainbow.rainbowprovider";
    public static final Uri CLOUD_AUTHORITY_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider");

    public interface ProviderSubSegment {
        public static final String APPLIST_PROVIDER_SEGMENT = "applistmaintain";
        public static final String EXTEND_VIEW_PROVIDER_SEGMENT = "extendviewquery";
        public static final String GFEATURE_PROVIDER_SEGMENT = "gfeaturemaintain";
        public static final String VIEWCOPY_PROVIDER_SEGMENT = "viewcopymaintain";
    }

    public interface RealFeatureCallMethod {
        public static final String CALL_METHOD_DELETE_GFEATURE = "call_method_delete_gfeature";
        public static final String EXTRA_PKG_NAME_LIST_KEY = "extra_pkg_name_list_key";
    }

    public interface RecommendCallMethod {
        public static final String CALL_METHOD_QUERY_RECOMMEND = "call_method_query_recommend";
    }
}
