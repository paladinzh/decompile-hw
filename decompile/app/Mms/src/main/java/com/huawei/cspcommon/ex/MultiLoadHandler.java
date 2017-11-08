package com.huawei.cspcommon.ex;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import com.amap.api.services.core.AMapException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx.SerialExecutor;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MultiLoadHandler extends HandlerEx {
    private ArrayList<JobRunner> mAdleJobs = new ArrayList(2);
    private ILoadCallBack mCallBack;
    protected Context mContext;
    private ThreadPoolExecutorEx mExecutor = ((ThreadPoolExecutorEx) ThreadEx.createExecutor(2, 5, AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "MultiLoadHandler-ParallExecutor"));
    private ContentResolver mResolver = null;
    private ArrayList<JobRunner> mRunningJobs = new ArrayList(2);

    public interface ILoadCallBack {
        void onLoadComplete(int i, Cursor cursor);
    }

    public static abstract class CursorMerger {
        private ArrayList<ReusedCursor> mCursors = new ArrayList();

        public abstract Cursor getCursor();

        public synchronized void replace(String tag, ReusedCursor csr) {
            ReusedCursor rc = getCursor(tag);
            if (rc != null) {
                rc.close();
                this.mCursors.remove(rc);
            }
            csr.addReference();
            this.mCursors.add(csr);
        }

        private synchronized ReusedCursor getCursor(String tag) {
            for (ReusedCursor r : this.mCursors) {
                if (r.mTag.equals(tag)) {
                    return r;
                }
            }
            return null;
        }

        public synchronized ReusedCursor[] getCursors(int factor) {
            ReusedCursor[] reusedCursorArr;
            synchronized (this.mCursors) {
                reusedCursorArr = this.mCursors.size() == 0 ? null : (ReusedCursor[]) this.mCursors.toArray(new ReusedCursor[(this.mCursors.size() * factor)]);
            }
            return reusedCursorArr;
        }

        protected static void closeCursors(ReusedCursor[] cursors) {
            if (cursors != null) {
                for (ReusedCursor r : cursors) {
                    if (r != null) {
                        r.close();
                    }
                }
            }
        }
    }

    public interface DataJob {
        String getJobName();

        int getToken();

        Object loadData();
    }

    public static abstract class ReusedCursor extends CursorWrapper {
        protected int mPriority;
        protected int mRefCount;
        protected String mTag;

        public ReusedCursor(Cursor cursor, int priority, String tag) {
            super(cursor);
            this.mRefCount = 0;
            this.mPriority = 0;
            this.mTag = null;
            this.mRefCount = 0;
            this.mPriority = priority;
            this.mTag = tag;
            Log.d("CSP_MultiLoadHandler", "ReusedCursor Create for" + this.mTag + " " + this.mPriority + " " + this);
        }

        public int getPriority() {
            return this.mPriority;
        }

        public synchronized void addReference() {
            this.mRefCount++;
        }

        public synchronized int delReference() {
            this.mRefCount--;
            return this.mRefCount;
        }

        public void close() {
            if (delReference() <= 0 && !super.isClosed()) {
                super.close();
                Log.d("CSP_MultiLoadHandler", "ReusedCursor Close for" + this.mTag + " " + this.mPriority + " " + this);
            }
        }

        protected boolean isPriorityCollomn(int columnIndex) {
            return false;
        }

        public String getString(int columnIndex) {
            if (isPriorityCollomn(columnIndex)) {
                return this.mTag;
            }
            return super.getString(columnIndex);
        }

        public int getInt(int columnIndex) {
            if (isPriorityCollomn(columnIndex)) {
                return this.mPriority;
            }
            return super.getInt(columnIndex);
        }
    }

    protected class JobRunner implements Runnable {
        private DataJob mJob;

        protected JobRunner() {
        }

        public JobRunner setJob(DataJob job) {
            this.mJob = job;
            return this;
        }

        public void run() {
            if (this.mJob != null) {
                MLog.d("CSP_MultiLoadHandler", "Load job start " + this.mJob.getJobName());
                Object o = this.mJob.loadData();
                MLog.d("CSP_MultiLoadHandler", "Load job finish " + this.mJob.getJobName());
                onJobDone(this.mJob, o);
            }
        }

        private void onJobDone(DataJob job, Object result) {
            MultiLoadHandler.this.recycleJobRunner(this);
            Cursor c = MultiLoadHandler.this.mergeData(job.getToken(), job.getJobName(), result);
            Message msg = MultiLoadHandler.this.obtainMessage(1);
            msg.obj = c;
            msg.arg1 = job.getToken();
            msg.sendToTarget();
        }
    }

    public MultiLoadHandler(Context context, ILoadCallBack callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
    }

    protected JobRunner obtainJobRunner() {
        Throwable th;
        synchronized (this.mAdleJobs) {
            try {
                JobRunner job;
                JobRunner job2;
                if (this.mAdleJobs.size() > 0) {
                    job = (JobRunner) this.mAdleJobs.remove(0);
                } else {
                    job = null;
                }
                if (job == null) {
                    try {
                        job2 = new JobRunner();
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                job2 = job;
                this.mRunningJobs.add(job2);
                return job2;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private void recycleJobRunner(JobRunner jr) {
        synchronized (this.mAdleJobs) {
            jr.setJob(null);
            this.mRunningJobs.remove(jr);
            this.mAdleJobs.add(jr);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                onLoadComplete(msg.arg1, (Cursor) msg.obj);
                return;
            default:
                return;
        }
    }

    protected void onLoadComplete(int token, Cursor cursor) {
        if (this.mCallBack != null) {
            this.mCallBack.onLoadComplete(token, cursor);
        } else {
            Log.e("CSP_MultiLoadHandler", "No callback for this job " + token);
        }
    }

    protected Cursor mergeData(int token, String tag, Object result) {
        return null;
    }

    public void startQuery(int token, String mergeTag, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        startDataJob(getQueryJob(token, mergeTag, uri, projection, selection, selectionArgs, orderBy));
    }

    public DataJob getQueryJob(int token, String mergeTag, Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        final Uri uri2 = uri;
        final String[] strArr = projection;
        final String str = selection;
        final String[] strArr2 = selectionArgs;
        final String str2 = orderBy;
        final int i = token;
        final String str3 = mergeTag;
        return new DataJob() {
            public Cursor loadData() {
                try {
                    if (MultiLoadHandler.this.mResolver == null) {
                        MultiLoadHandler.this.mResolver = MultiLoadHandler.this.mContext.getContentResolver();
                    }
                    Cursor cursor = MultiLoadHandler.this.mResolver.query(uri2, strArr, str, strArr2, str2);
                    if (cursor == null) {
                        return cursor;
                    }
                    cursor.getCount();
                    return cursor;
                } catch (Exception e) {
                    Log.w("CSP_MultiLoadHandler", "Exception thrown during handling EVENT_ARG_QUERY", e);
                    return null;
                }
            }

            public int getToken() {
                return i;
            }

            public String getJobName() {
                return str3;
            }
        };
    }

    public void startDataJob(DataJob job) {
        startDataJob(job, this.mExecutor);
    }

    public void startDataJob(DataJob job, Executor executor) {
        executor.execute(obtainJobRunner().setJob(job));
    }

    public boolean hasUnfinishedJob() {
        boolean z = false;
        synchronized (this.mAdleJobs) {
            if (this.mRunningJobs.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    public SerialExecutor createSerialExecutor() {
        return new SerialExecutor(this.mExecutor);
    }
}
