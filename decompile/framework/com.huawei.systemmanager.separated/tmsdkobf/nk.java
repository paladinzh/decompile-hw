package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.Buffalo;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class nk {
    private static nk Cu = null;
    private Buffalo Ct = null;
    private int mRefCount = 0;

    private nk() {
        d.g("QQPimSecure", "BuffaloImpl 00");
    }

    public static nk fk() {
        if (Cu == null) {
            synchronized (nk.class) {
                if (Cu == null) {
                    Cu = new nk();
                }
            }
        }
        return Cu;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String a(String str, String str2, int i) {
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                Buffalo buffalo = this.Ct;
            } else {
                d.e("QQPimSecure", "BumbleBeeImpl checkSms mRefCount==0");
                return null;
            }
        }
    }

    public void fl() {
        d.g("QQPimSecure", "BuffaloImpl 01");
        String a = ms.a(TMSDKContext.getApplicaionContext(), "rule.dat", null);
        if (a != null) {
            synchronized (nl.class) {
                if (this.mRefCount <= 0) {
                    this.Ct = new Buffalo();
                    this.Ct.nativeInitHashChecker_c(a);
                    this.mRefCount = 1;
                    d.g("QQPimSecure", "BuffaloImpl 02");
                    return;
                }
                this.mRefCount++;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fm() {
        d.g("QQPimSecure", "BuffaloImpl 03");
        synchronized (nl.class) {
            if (this.mRefCount > 0) {
                this.mRefCount--;
                if (this.mRefCount <= 0) {
                    if (this.Ct != null) {
                        this.Ct.nativeFinishHashChecker_c();
                    }
                    Cu = null;
                }
            }
        }
    }
}
