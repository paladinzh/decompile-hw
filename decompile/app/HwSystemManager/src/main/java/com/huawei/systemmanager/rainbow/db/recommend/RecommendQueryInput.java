package com.huawei.systemmanager.rainbow.db.recommend;

import android.os.Bundle;
import com.google.android.collect.Sets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaConst;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RecommendQueryInput {

    public interface RecommendQueryKey {
        public static final String RECOMMEND_BUSINESS_ID = "Recommend_business_id";
        public static final String RECOMMEND_QUERY_ITEMIDS_KEY = "Recommend_query_itemids";
        public static final String RECOMMEND_QUERY_PKGS_KEY = "Recommend_query_pkgs";
        public static final String RECOMMEND_QUERY_TYPE = "Recommend_query_type";
    }

    public interface RecommendQueryType {
        public static final int RECOMMEND_QUERY_TYPE_ALL = 3;
        public static final int RECOMMEND_QUERY_TYPE_MULTI_PKG_ONE_ITEM = 1;
        public static final int RECOMMEND_QUERY_TYPE_ONE_PKG_MULTI_ITEM = 2;
    }

    public static Bundle generateOnePkgMultiItemInput(int businessId, String pkgName, List<Integer> ids) throws RecommendParamException {
        ArrayList arrayList = null;
        if (Strings.isNullOrEmpty(pkgName)) {
            throw new RecommendParamException("Empty package name for generateOnePkgMultiItemInput");
        }
        ArrayList newArrayList = Lists.newArrayList(pkgName);
        if (ids != null) {
            arrayList = Lists.newArrayList((Iterable) ids);
        }
        return generateInner(2, businessId, newArrayList, arrayList);
    }

    public static Bundle generateMultiPkgOneItemInput(int businessId, List<String> pkgList, int id) throws RecommendParamException {
        ArrayList arrayList = null;
        if (CloudMetaConst.STRING_HOLDER.equals(CloudMetaMgr.getItemName(id))) {
            throw new RecommendParamException("Invalid id for generateMultiPkgOneItemInput");
        }
        if (pkgList != null) {
            arrayList = Lists.newArrayList((Iterable) pkgList);
        }
        return generateInner(1, businessId, arrayList, Lists.newArrayList(Integer.valueOf(id)));
    }

    public static Bundle generateInputForBusiness(int businessId) throws RecommendParamException {
        return generateInner(3, businessId, null, null);
    }

    static int extractQueryType(Bundle bundle) {
        return bundle.getInt(RecommendQueryKey.RECOMMEND_QUERY_TYPE);
    }

    static int extractQueryBusinessId(Bundle bundle) {
        return bundle.getInt(RecommendQueryKey.RECOMMEND_BUSINESS_ID);
    }

    static String extractPkgNameFromBundle(int queryType, Bundle bundle) throws RecommendParamException {
        if (2 == queryType) {
            ArrayList<String> list = bundle.getStringArrayList(RecommendQueryKey.RECOMMEND_QUERY_PKGS_KEY);
            return (list == null || list.size() <= 0) ? "" : (String) list.get(0);
        } else {
            throw new RecommendParamException("Only RECOMMEND_QUERY_TYPE_ONE_PKG_MULTI_ITEM could use this function");
        }
    }

    static List<String> extractPkgListFromBundle(int queryType, Bundle bundle) throws RecommendParamException {
        if (1 == queryType) {
            return Lists.newArrayList(bundle.getStringArrayList(RecommendQueryKey.RECOMMEND_QUERY_PKGS_KEY));
        }
        throw new RecommendParamException("Only RECOMMEND_QUERY_TYPE_MULTI_PKG_ONE_ITEM could use this function");
    }

    static int extractItemIdFromBundle(int queryType, Bundle bundle) throws RecommendParamException {
        if (1 == queryType) {
            ArrayList<Integer> list = bundle.getIntegerArrayList(RecommendQueryKey.RECOMMEND_QUERY_ITEMIDS_KEY);
            if (list == null || list.size() <= 0) {
                return 0;
            }
            return ((Integer) list.get(0)).intValue();
        }
        throw new RecommendParamException("Only RECOMMEND_QUERY_TYPE_MULTI_PKG_ONE_ITEM could use this function");
    }

    static List<Integer> extractItemIdListFromBundle(int queryType, Bundle bundle) throws RecommendParamException {
        if (2 == queryType) {
            return Lists.newArrayList(bundle.getIntegerArrayList(RecommendQueryKey.RECOMMEND_QUERY_ITEMIDS_KEY));
        }
        throw new RecommendParamException("Only RECOMMEND_QUERY_TYPE_ONE_PKG_MULTI_ITEM could use this function");
    }

    private static Bundle generateInner(int queryType, int businessId, ArrayList<String> pkgs, ArrayList<Integer> ids) throws RecommendParamException {
        checkValidQueryParameter(queryType, businessId, ids);
        Bundle bundle = new Bundle();
        bundle.putInt(RecommendQueryKey.RECOMMEND_QUERY_TYPE, queryType);
        bundle.putInt(RecommendQueryKey.RECOMMEND_BUSINESS_ID, businessId);
        String str = RecommendQueryKey.RECOMMEND_QUERY_PKGS_KEY;
        if (pkgs == null) {
            pkgs = new ArrayList();
        }
        bundle.putStringArrayList(str, pkgs);
        str = RecommendQueryKey.RECOMMEND_QUERY_ITEMIDS_KEY;
        if (ids == null) {
            ids = new ArrayList();
        }
        bundle.putIntegerArrayList(str, ids);
        return bundle;
    }

    private static void checkValidQueryParameter(int queryType, int businessId, List<Integer> ids) throws RecommendParamException {
        if (1 != queryType && 2 != queryType && 3 != queryType) {
            throw new RecommendParamException("checkValidQueryParameter invalid queryType");
        } else if (CloudMetaConst.STRING_HOLDER.endsWith(CloudMetaMgr.getBusinessName(businessId))) {
            throw new RecommendParamException("checkValidQueryParameter invalid businessId:" + businessId);
        } else if (ids != null && !ids.isEmpty()) {
            Set<Integer> validIds = Sets.newHashSet();
            for (AbsConfigItem item : CloudMetaMgr.getBusinessInstance(businessId).getConfigItemList()) {
                validIds.add(Integer.valueOf(item.getCfgItemId()));
            }
            for (Integer id : ids) {
                if (!validIds.contains(id)) {
                    throw new RecommendParamException("checkValidQueryParameter invalid ids:" + ids + " for business:" + businessId);
                }
            }
        }
    }
}
