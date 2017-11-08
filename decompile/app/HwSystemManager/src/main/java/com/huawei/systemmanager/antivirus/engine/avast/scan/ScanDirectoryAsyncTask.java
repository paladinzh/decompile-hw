package com.huawei.systemmanager.antivirus.engine.avast.scan;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class ScanDirectoryAsyncTask extends AsyncTask<Boolean, ScanDirectoryProgress, Boolean> {
    private static final String APK_POSIX = ".apk";
    private static final String TAG = "ScanDirectoryAsyncTask";
    protected List<ScanResultEntity> mCloudResults = null;
    private final Context mContext;
    private final Stack<File> mFileStacks = new Stack();
    private final List<String> mScanDirs;

    private class ScanApkFiles implements Callable<Boolean> {
        private ArrayList<File> files;

        public ScanApkFiles(List<File> files) {
            this.files = Lists.newArrayList((Iterable) files);
        }

        public Boolean call() throws Exception {
            for (File file : this.files) {
                if (ScanDirectoryAsyncTask.this.isCancelled()) {
                    return Boolean.valueOf(false);
                }
                List<ScanResultStructure> results = EngineInterface.scan(ScanDirectoryAsyncTask.this.mContext, null, file, null, 33);
                ScanDirectoryAsyncTask.this.publishProgress(new ScanDirectoryProgress[]{new ScanDirectoryProgress(0, file.getAbsolutePath(), results)});
            }
            return Boolean.valueOf(true);
        }
    }

    protected abstract void onPostExecute(Boolean bool);

    protected abstract void onPostScanProgressUpdate(ScanDirectoryProgress scanDirectoryProgress);

    protected ScanDirectoryAsyncTask(Context context) {
        this.mContext = context.getApplicationContext();
        this.mScanDirs = null;
    }

    protected ScanDirectoryAsyncTask(Context context, List<String> rootDirs) {
        this.mContext = context.getApplicationContext();
        this.mScanDirs = rootDirs;
    }

    protected final void onProgressUpdate(ScanDirectoryProgress... values) {
        onPostScanProgressUpdate(values[0]);
    }

    protected void onCancelled() {
        super.onCancelled();
        onPostExecute(Boolean.valueOf(false));
    }

    protected Boolean doInBackground(Boolean... isCloudEnable) {
        Iterable filesToScan = new ArrayList();
        Map<String, PackageInfo> pkgMap = new HashMap();
        for (String root : this.mScanDirs) {
            this.mFileStacks.add(new File(root));
        }
        try {
            List<File> firstTask;
            List<File> secTask;
            List<FutureTask<Boolean>> tasks = Lists.newArrayList();
            while (!this.mFileStacks.isEmpty()) {
                if (isCancelled()) {
                    return Boolean.valueOf(false);
                }
                File file = (File) this.mFileStacks.pop();
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File info : files) {
                            this.mFileStacks.push(info);
                        }
                    }
                } else if (file.exists()) {
                    String fileName = file.getName();
                    int startIndex = fileName.length() - APK_POSIX.length();
                    if (startIndex > 0 && fileName.substring(startIndex).equalsIgnoreCase(APK_POSIX)) {
                        filesToScan.add(file);
                        PackageInfo packInfo = this.mContext.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 1);
                        if (packInfo != null) {
                            pkgMap.put(file.getAbsolutePath(), packInfo);
                        }
                    }
                }
            }
            int lenth = filesToScan.size();
            if (lenth < 2) {
                firstTask = Lists.newArrayList(filesToScan);
                secTask = Lists.newArrayList();
            } else {
                firstTask = filesToScan.subList(0, lenth / 2);
                secTask = filesToScan.subList(lenth / 2, lenth);
            }
            tasks.add(new FutureTask(new ScanApkFiles(firstTask)));
            tasks.add(new FutureTask(new ScanApkFiles(secTask)));
            int threadIndex = 0;
            for (FutureTask<Boolean> t : tasks) {
                int threadIndex2 = threadIndex + 1;
                new Thread(t, "ScanApkFiles_thread_" + threadIndex).start();
                threadIndex = threadIndex2;
            }
            for (FutureTask<Boolean> t2 : tasks) {
                t2.get();
            }
        } catch (InterruptedException e) {
            HwLog.e("TAG", "global scan InterruptedException" + e.toString());
        } catch (ExecutionException ex) {
            HwLog.e("TAG", "global scan ExecutionException" + ex.toString());
        }
        if (isCancelled()) {
            return Boolean.valueOf(false);
        }
        if (isCloudEnable[0].booleanValue()) {
            this.mCloudResults = CloudScanMgr.cloudScan(this.mContext, filesToScan, pkgMap, true);
        }
        return Boolean.valueOf(true);
    }
}
