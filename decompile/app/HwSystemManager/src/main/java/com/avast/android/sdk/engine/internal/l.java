package com.avast.android.sdk.engine.internal;

import android.content.Context;
import com.avast.android.sdk.engine.ProgressObserver;
import com.avast.android.sdk.engine.UpdateCheckResultStructure;
import com.avast.android.sdk.engine.UpdateCheckResultStructure.UpdateCheck;
import com.avast.android.sdk.engine.UpdateResultStructure;
import com.avast.android.sdk.engine.UpdateResultStructure.UpdateResult;
import com.avast.android.sdk.engine.VpsInformation;
import com.avast.android.sdk.engine.internal.q.c;
import com.avast.android.sdk.engine.internal.vps.a.b;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.apache.http.HttpException;

/* compiled from: Unknown */
public class l {
    private static final int a = SpaceConst.SCANNER_TYPE_ALL;
    private static Semaphore b = new Semaphore(a, true);
    private static Semaphore c = new Semaphore(1, true);

    public static UpdateResultStructure a(Context context, ProgressObserver progressObserver) {
        c.acquireUninterruptibly();
        UpdateResultStructure updateResultStructure = new UpdateResultStructure();
        updateResultStructure.newVps = null;
        UpdateCheckResultStructure a = v.a(context);
        if (progressObserver != null) {
            progressObserver.onProgressChanged(10, 100);
        }
        if (UpdateCheck.RESULT_UPDATE_AVAILABLE.equals(a.checkResult)) {
            try {
                File a2 = v.a(context, a.vpsUrl, progressObserver);
                if (progressObserver != null) {
                    progressObserver.onProgressChanged(60, 100);
                }
                if (a2 == null || !a2.exists()) {
                    updateResultStructure.nextUpdateCheckMins = v.b();
                    updateResultStructure.result = UpdateResult.RESULT_NOT_ENOUGH_INTERNAL_SPACE_TO_UPDATE;
                    v.c(context);
                    c.release();
                    return updateResultStructure;
                } else if (v.b(context, a2)) {
                    if (progressObserver != null) {
                        progressObserver.onProgressChanged(70, 100);
                    }
                    if (v.a()) {
                        if (progressObserver != null) {
                            progressObserver.onProgressChanged(80, 100);
                        }
                        b.acquireUninterruptibly(a);
                        UpdateResultStructure a3 = v.a(context, a2);
                        if (progressObserver != null) {
                            progressObserver.onProgressChanged(90, 100);
                        }
                        if (UpdateResult.RESULT_UPDATED.equals(a3.result)) {
                            v.b(context);
                            Map hashMap = new HashMap();
                            hashMap.put(Short.valueOf(b.STRUCTURE_VERSION_INT_ID.a()), VpsInformation.getVersionCode());
                            hashMap.put(Short.valueOf(b.CONTEXT_CONTEXT_ID.a()), context);
                            List parseResultList = VpsInformation.parseResultList((byte[]) q.a(context, c.GET_VPS_INFORMATION, hashMap));
                            a3.newVps = null;
                            if (parseResultList != null && parseResultList.size() > 0) {
                                a3.newVps = (VpsInformation) parseResultList.get(0);
                            }
                        }
                        v.c(context);
                        b.release(a);
                        c.release();
                        a3.nextUpdateCheckMins = v.b();
                        if (progressObserver != null) {
                            progressObserver.onProgressChanged(100, 100);
                        }
                        return a3;
                    }
                    a2.delete();
                    updateResultStructure.nextUpdateCheckMins = v.b();
                    updateResultStructure.result = UpdateResult.RESULT_INVALID_VPS;
                    v.c(context);
                    c.release();
                    return updateResultStructure;
                } else {
                    a2.delete();
                    updateResultStructure.nextUpdateCheckMins = v.b();
                    updateResultStructure.result = UpdateResult.RESULT_INVALID_VPS;
                    v.c(context);
                    c.release();
                    return updateResultStructure;
                }
            } catch (HttpException e) {
                updateResultStructure.nextUpdateCheckMins = v.b();
                updateResultStructure.result = UpdateResult.RESULT_CONNECTION_PROBLEMS;
                v.c(context);
                c.release();
                return updateResultStructure;
            }
        }
        UpdateResult updateResult;
        c.release();
        updateResultStructure.nextUpdateCheckMins = v.b();
        switch (m.a[a.checkResult.ordinal()]) {
            case 1:
                updateResult = UpdateResult.RESULT_UP_TO_DATE;
                break;
            case 2:
                updateResult = UpdateResult.RESULT_OLD_APPLICATION_VERSION;
                break;
            case 3:
                updateResult = UpdateResult.RESULT_INVALID_VPS;
                break;
            default:
                updateResult = UpdateResult.RESULT_CONNECTION_PROBLEMS;
                break;
        }
        updateResultStructure.result = updateResult;
        return updateResultStructure;
    }

    public static void a() {
        b.acquireUninterruptibly();
    }

    public static void b() {
        b.release();
    }
}
