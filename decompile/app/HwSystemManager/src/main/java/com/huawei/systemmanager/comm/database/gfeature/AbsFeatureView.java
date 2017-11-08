package com.huawei.systemmanager.comm.database.gfeature;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import java.util.List;
import java.util.Set;

public abstract class AbsFeatureView {
    public static final String COL_PACKAGE_NAME = "packageName";

    static class QuotStringFunction implements Function<FeatureToColumn, String> {
        QuotStringFunction() {
        }

        public String apply(FeatureToColumn arg0) {
            return SqlMarker.QUOTATION + arg0.getFeatureName() + SqlMarker.QUOTATION;
        }
    }

    public abstract String getLinkedRealTablePrefix();

    public abstract String getQueryViewName();

    public abstract String getTempViewPrefix();

    public abstract List<FeatureToColumn> getViewColumnFeatureList();

    public List<String> generateCreateViewSqls(String realTable) {
        List<String> createSqls = Lists.newArrayList();
        String viewPrefix = getTempViewPrefix();
        List<FeatureToColumn> feature2ColList = getViewColumnFeatureList();
        createSqls.add(genPkgListView(realTable, viewPrefix, feature2ColList));
        for (FeatureToColumn oneCvt : feature2ColList) {
            createSqls.add(generateColumnsFeatureView(realTable, viewPrefix, oneCvt.getFeatureName()));
        }
        createSqls.add(genFinalFeatureView(viewPrefix, feature2ColList));
        return createSqls;
    }

    private String genPkgListView(String realTable, String viewPrefix, List<FeatureToColumn> feature2ColList) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS ").append(getPkgListViewName(viewPrefix));
        strBuf.append(" AS SELECT DISTINCT ");
        strBuf.append("packageName").append(" FROM ");
        strBuf.append(realTable).append(" WHERE ").append(GFeatureTable.COL_FEATURE_NAME);
        strBuf.append(" IN ").append(SqlMarker.LEFT_PARENTHESES);
        strBuf.append(Joiner.on(SqlMarker.COMMA_SEPARATE).join(Collections2.transform(feature2ColList, new QuotStringFunction())));
        strBuf.append(SqlMarker.RIGHT_PARENTHESES).append(SqlMarker.SQL_END);
        return strBuf.toString();
    }

    private String generateColumnsFeatureView(String realTable, String viewPrefixName, String featureName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS ").append(getColumnsFeatureViewName(viewPrefixName, featureName));
        strBuf.append(" AS SELECT ").append("packageName");
        strBuf.append(SqlMarker.COMMA_SEPARATE).append(GFeatureTable.COL_FEATURE_VALUE);
        strBuf.append(" FROM ").append(realTable).append(" WHERE ");
        strBuf.append(GFeatureTable.COL_FEATURE_NAME).append("=");
        strBuf.append(SqlMarker.QUOTATION).append(featureName).append(SqlMarker.QUOTATION);
        return strBuf.toString();
    }

    private String genFinalFeatureView(String viewPrefix, List<FeatureToColumn> feature2ColList) {
        String pkgListViewName = getPkgListViewName(viewPrefix);
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS ").append(getQueryViewName());
        strBuf.append(" AS SELECT ").append(pkgListViewName);
        strBuf.append(".").append("packageName");
        for (FeatureToColumn oneCvt : feature2ColList) {
            oneCvt.appendAbsViewSelection(strBuf, getColumnsFeatureViewName(viewPrefix, oneCvt.getFeatureName()));
        }
        strBuf.append(" FROM ").append(pkgListViewName);
        for (FeatureToColumn oneCvt2 : feature2ColList) {
            oneCvt2.appendAbsViewLeftJoin(strBuf, pkgListViewName, getColumnsFeatureViewName(viewPrefix, oneCvt2.getFeatureName()));
        }
        return strBuf.toString();
    }

    public List<String> generateDropViewSqls() {
        List<String> dropSqls = Lists.newArrayList();
        dropSqls.add("DROP VIEW " + getPkgListViewName(getTempViewPrefix()));
        for (String feature : getFeatureSet(getViewColumnFeatureList())) {
            dropSqls.add("DROP VIEW " + getColumnsFeatureViewName(getTempViewPrefix(), feature));
        }
        dropSqls.add("DROP VIEW " + getQueryViewName());
        return dropSqls;
    }

    private String getPkgListViewName(String viewPrefix) {
        return viewPrefix + "_PKGLIST_TEMP_VIEW";
    }

    private String getColumnsFeatureViewName(String viewPrefix, String featureName) {
        return viewPrefix + "_COL_" + featureName + "_TEMP_VIEW";
    }

    private Set<String> getFeatureSet(List<FeatureToColumn> featureColList) {
        Set<String> result = Sets.newHashSet();
        for (FeatureToColumn obj : featureColList) {
            result.add(obj.getFeatureName());
        }
        return result;
    }
}
