package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.internal.app.procstats.ProcessStats;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ProcStatsData.MemInfo;

public abstract class ProcessStatsBase extends SettingsPreferenceFragment {
    private static final long DURATION_QUANTUM = ProcessStats.COMMIT_PERIOD;
    protected static final long[] sDurations = new long[]{10800000 - (DURATION_QUANTUM / 2), 21600000 - (DURATION_QUANTUM / 2), 43200000 - (DURATION_QUANTUM / 2), 86400000 - (DURATION_QUANTUM / 2)};
    protected final int[] DURATION_COUNT = new int[]{3, 6, 12, 1};
    private boolean isFromProcessStatusUi = false;
    protected int mDurationIndex;
    private AsyncHandler mHandler = new AsyncHandler();
    private boolean mIsFirstLoad = false;
    protected boolean mShowMax;
    protected ProcStatsData mStatsManager;
    protected final int[] sDurationLabels_ex = new int[]{2131627249, 2131627250, 2131627251, 2131627252};

    private class AsyncHandler extends Handler {
        private AsyncHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 4097) {
                ProcessStatsBase.this.refreshUi();
                ProcessStatsBase.this.mIsFirstLoad = false;
            } else if (msg.what == 4098) {
                ProcessStatsBase.this.refreshUi();
            }
        }
    }

    private class AsyncMemoryTask extends AsyncTask<Context, Object, Object> {
        long mDuration;

        public AsyncMemoryTask(long duration) {
            this.mDuration = duration;
        }

        protected Object doInBackground(Context... arg0) {
            ProcessStatsBase.this.loadMemoryData(this.mDuration);
            return null;
        }

        protected void onPostExecute(Object result) {
            Activity activity = ProcessStatsBase.this.getActivity();
            if (activity != null && !activity.isFinishing()) {
                ProcessStatsBase.this.mHandler.sendMessage(ProcessStatsBase.this.mHandler.obtainMessage(4097));
            }
        }
    }

    private class AsyncRefreshTask extends AsyncTask<Context, Object, Object> {
        private AsyncRefreshTask() {
        }

        protected Object doInBackground(Context... arg0) {
            ProcessStatsBase.this.mStatsManager.refreshStats(false);
            return null;
        }

        protected void onPostExecute(Object result) {
            Activity activity = ProcessStatsBase.this.getActivity();
            if (activity != null && !activity.isFinishing()) {
                ProcessStatsBase.this.mHandler.sendMessage(ProcessStatsBase.this.mHandler.obtainMessage(4098));
            }
        }
    }

    public abstract void refreshUi();

    public void onCreate(Bundle icicle) {
        boolean z;
        super.onCreate(icicle);
        Context activity = getActivity();
        if (icicle == null) {
            z = this.isFromProcessStatusUi;
        } else {
            z = true;
        }
        this.mStatsManager = new ProcStatsData(activity, z);
        if (icicle != null) {
            this.mDurationIndex = icicle.getInt("duration_index");
        }
        this.mIsFirstLoad = true;
        new AsyncMemoryTask(icicle != null ? icicle.getLong("duration", sDurations[0]) : sDurations[this.mDurationIndex]).execute(new Context[]{getActivity()});
    }

    private void loadMemoryData(long duration) {
        this.mStatsManager.setDuration(duration);
    }

    public void setDurationIndex(int index) {
        this.mDurationIndex = index;
    }

    public void setCurrentFragmentTag(boolean isFromProcessStatusUi) {
        this.isFromProcessStatusUi = isFromProcessStatusUi;
    }

    public void setIsShowMax(boolean mShowMax) {
        this.mShowMax = mShowMax;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("duration", this.mStatsManager.getDuration());
        outState.putInt("duration_index", this.mDurationIndex);
    }

    public void onResume() {
        super.onResume();
        if (!this.mIsFirstLoad) {
            new AsyncRefreshTask().execute(new Context[]{getActivity()});
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            this.mStatsManager.xferStats();
        }
    }

    public static void launchMemoryDetail(SettingsActivity activity, MemInfo memInfo, ProcStatsPackageEntry entry, boolean includeAppInfo) {
        Bundle args = new Bundle();
        args.putParcelable("package_entry", entry);
        args.putDouble("weight_to_ram", memInfo.weightToRam);
        args.putLong("total_time", memInfo.memTotalTime);
        args.putDouble("max_memory_usage", memInfo.usedWeight * memInfo.weightToRam);
        args.putDouble("total_scale", memInfo.totalScale);
        args.putBoolean("hideInfoButton", !includeAppInfo);
        activity.startPreferencePanel(ProcessStatsDetail.class.getName(), args, 2131627014, null, null, 0);
    }
}
