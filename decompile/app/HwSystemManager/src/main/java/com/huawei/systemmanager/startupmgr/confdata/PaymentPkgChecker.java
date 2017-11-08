package com.huawei.systemmanager.startupmgr.confdata;

import android.content.Context;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Set;

public class PaymentPkgChecker {
    private static final String PAYMENT_PKG_PATH = "startupmgr/startup_payment_apps.xml";
    private static final String TAG = "PaymentPkgChecker";
    private static final PaymentPkgChecker sInstance = new PaymentPkgChecker();
    private final Set<String> mPaymentPkgs = Sets.newHashSet();

    private PaymentPkgChecker() {
    }

    public void init(Context ctx) {
        if (this.mPaymentPkgs.isEmpty()) {
            try {
                List<SimpleXmlRow> result = XmlParsers.assetSimpleXmlRows(ctx, PAYMENT_PKG_PATH);
                if (!HsmCollections.isEmpty(result)) {
                    for (SimpleXmlRow row : result) {
                        this.mPaymentPkgs.add(row.getAttrValue("name"));
                    }
                }
            } catch (XmlParserException e) {
                HwLog.e(TAG, "parser failed:" + e);
            } catch (Exception e2) {
                HwLog.e(TAG, "init failed:" + e2);
            }
        }
    }

    public boolean contain(String pkg) {
        return this.mPaymentPkgs.contains(pkg);
    }

    public static final PaymentPkgChecker getInstance() {
        return sInstance;
    }

    public static final boolean isPaymentPkg(String pkg) {
        return sInstance.contain(pkg);
    }
}
