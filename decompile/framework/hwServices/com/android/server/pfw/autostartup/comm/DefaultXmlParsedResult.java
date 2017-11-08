package com.android.server.pfw.autostartup.comm;

import com.android.server.pfw.log.HwPFWLogger;
import java.util.ArrayList;
import java.util.List;

public class DefaultXmlParsedResult {
    private static final String TAG = "CombinedParsedResult";
    private List<String> mBlackSysPkgs = new ArrayList();
    private Object mLock = new Object();
    private List<PreciseComponent> mProviderIgnoreList = new ArrayList();
    private List<PreciseComponent> mReceiverIgnoreList = new ArrayList();
    private List<PreciseComponent> mServiceIgnoreList = new ArrayList();
    private List<String> mThirdWhitePkgs = new ArrayList();

    public DefaultXmlParsedResult appendData(DefaultXmlParsedResult data) {
        synchronized (this.mLock) {
            if (data != null) {
                this.mBlackSysPkgs.addAll(data.mBlackSysPkgs);
                this.mThirdWhitePkgs.addAll(data.mThirdWhitePkgs);
                this.mReceiverIgnoreList.addAll(data.mReceiverIgnoreList);
                this.mServiceIgnoreList.addAll(data.mServiceIgnoreList);
                this.mProviderIgnoreList.addAll(data.mProviderIgnoreList);
            }
        }
        return this;
    }

    public void addSystemBlackPkgs(List<String> pkgs) {
        synchronized (this.mLock) {
            this.mBlackSysPkgs.addAll(pkgs);
        }
    }

    public void addThirdWhitePkgs(List<String> pkgs) {
        synchronized (this.mLock) {
            this.mThirdWhitePkgs.addAll(pkgs);
        }
    }

    public void addPreciseComponent(PreciseComponent preciseComp) {
        synchronized (this.mLock) {
            switch (preciseComp.getCompType()) {
                case 0:
                    this.mProviderIgnoreList.add(preciseComp);
                    break;
                case 1:
                    this.mReceiverIgnoreList.add(preciseComp);
                    break;
                case 2:
                    this.mServiceIgnoreList.add(preciseComp);
                    break;
                default:
                    HwPFWLogger.e(TAG, "addPreciseComponent invalid comp: " + preciseComp);
                    break;
            }
        }
    }

    public boolean isSystemBlackPkgs(String pkg) {
        boolean contains;
        synchronized (this.mLock) {
            contains = this.mBlackSysPkgs.contains(pkg);
        }
        return contains;
    }

    public boolean isThirdWhitePkgs(String pkg) {
        boolean contains;
        synchronized (this.mLock) {
            contains = this.mThirdWhitePkgs.contains(pkg);
        }
        return contains;
    }

    public List<String> copyOfSystemBlackPkgs() {
        List arrayList;
        synchronized (this.mLock) {
            arrayList = new ArrayList(this.mBlackSysPkgs);
        }
        return arrayList;
    }

    public List<String> copyOfThirdWhitePkgs() {
        List arrayList;
        synchronized (this.mLock) {
            arrayList = new ArrayList(this.mThirdWhitePkgs);
        }
        return arrayList;
    }

    public boolean whiteReceiverAction(String action) {
        synchronized (this.mLock) {
            for (PreciseComponent comp : this.mReceiverIgnoreList) {
                if (comp.matchKey(action)) {
                    if (comp.isScopeIndividual()) {
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public String toString() {
        String stringBuffer;
        synchronized (this.mLock) {
            StringBuffer buf = new StringBuffer();
            buf.append(TAG).append("{ SystemBlackList ").append(this.mBlackSysPkgs).append(", ThirdPartyWhiteList ").append(this.mThirdWhitePkgs).append(", ReceiverIgnoreList ").append(this.mReceiverIgnoreList).append("}");
            stringBuffer = buf.toString();
        }
        return stringBuffer;
    }
}
