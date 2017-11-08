package com.huawei.hwid.api.common.apkimpl;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;

/* compiled from: DummyActivity */
class f implements AccountManagerCallback {
    final /* synthetic */ DummyActivity a;

    private f(DummyActivity dummyActivity) {
        this.a = dummyActivity;
    }

    public void run(AccountManagerFuture accountManagerFuture) {
        if (accountManagerFuture != null) {
            try {
                this.a.a((Bundle) accountManagerFuture.getResult());
            } catch (Throwable e) {
                a.d("DummyActivity", "OperationCanceledException / " + e.toString(), e);
                return;
            } catch (Throwable e2) {
                a.d("DummyActivity", "AuthenticatorException / " + e2.toString(), e2);
                return;
            } catch (Throwable e22) {
                a.d("DummyActivity", "IOException / " + e22.toString(), e22);
                return;
            } finally {
                this.a.finish();
            }
        }
        this.a.finish();
    }
}
