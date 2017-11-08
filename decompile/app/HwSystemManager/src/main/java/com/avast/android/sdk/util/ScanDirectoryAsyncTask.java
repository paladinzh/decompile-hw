package com.avast.android.sdk.util;

import android.content.Context;
import android.os.AsyncTask;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.bh;
import com.avast.android.sdk.internal.g;
import java.io.File;
import java.util.List;
import java.util.Stack;

/* compiled from: Unknown */
public abstract class ScanDirectoryAsyncTask extends AsyncTask<Void, ScanDirectoryProgress, Boolean> {
    private final Context a;
    private final List<String> b;
    private final Stack<File> c = new Stack();

    protected ScanDirectoryAsyncTask(Context context) {
        this.a = context.getApplicationContext();
        this.b = null;
    }

    protected ScanDirectoryAsyncTask(Context context, List<String> list) {
        this.a = context.getApplicationContext();
        this.b = list;
    }

    protected Boolean doInBackground(Void... voidArr) {
        if (this.b != null) {
            for (String file : this.b) {
                this.c.add(new File(file));
            }
        } else {
            List customScanStorages = EngineInterface.getEngineConfig().getCustomScanStorages();
            if (customScanStorages == null) {
                customScanStorages = new g().b();
            }
            for (String file2 : r0) {
                this.c.add(new File(file2));
            }
        }
        Integer acquireVpsContextId = EngineInterface.acquireVpsContextId(this.a);
        int i = 0;
        while (!this.c.isEmpty()) {
            if (isCancelled()) {
                Boolean valueOf = Boolean.valueOf(false);
                EngineInterface.releaseVpsContextId(this.a, acquireVpsContextId.intValue());
                return valueOf;
            }
            File file3 = (File) this.c.pop();
            try {
                if (bh.a(file3)) {
                    continue;
                } else {
                    int i2;
                    if (file3.isDirectory()) {
                        File[] listFiles = file3.listFiles();
                        if (listFiles != null) {
                            for (Object push : listFiles) {
                                this.c.push(push);
                            }
                            i2 = i;
                        }
                    } else if (file3.exists()) {
                        ScanDirectoryProgress[] scanDirectoryProgressArr = new ScanDirectoryProgress[1];
                        i2 = i + 1;
                        scanDirectoryProgressArr[0] = new ScanDirectoryProgress(i2, file3.getAbsolutePath(), EngineInterface.scan(this.a, acquireVpsContextId, file3, null, 33));
                        publishProgress(scanDirectoryProgressArr);
                        if (i2 % 1000 == 0) {
                            System.gc();
                        }
                    }
                    i = i2;
                }
            } catch (Throwable e) {
                ao.c("Scanning error. Skipping: " + file3.getAbsolutePath(), e);
            } catch (Throwable th) {
                EngineInterface.releaseVpsContextId(this.a, acquireVpsContextId.intValue());
            }
        }
        EngineInterface.releaseVpsContextId(this.a, acquireVpsContextId.intValue());
        return Boolean.valueOf(true);
    }

    protected void onCancelled() {
        super.onCancelled();
        onPostExecute(Boolean.valueOf(false));
    }

    protected abstract void onPostExecute(Boolean bool);

    protected abstract void onPostScanProgressUpdate(ScanDirectoryProgress scanDirectoryProgress);

    protected final void onProgressUpdate(ScanDirectoryProgress... scanDirectoryProgressArr) {
        onPostScanProgressUpdate(scanDirectoryProgressArr[0]);
    }
}
