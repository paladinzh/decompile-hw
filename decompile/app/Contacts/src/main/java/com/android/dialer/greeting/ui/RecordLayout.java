package com.android.dialer.greeting.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.dialer.greeting.presenter.RecordPresenter;
import com.android.dialer.greeting.presenter.RecordPresenter.RecordView;
import com.google.android.gms.R;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RecordLayout extends LinearLayout implements RecordView {
    private PositionUpdater mPositionUpdater;
    private RecordPresenter mPresenter;
    private ProgressBar mProgressBar;
    private RecordStatusListener mStatusListener;
    private TextView mTextView;
    private TextView mTotalDuractionTv;

    public interface RecordStatusListener {
        void onRecordAbort();

        void onRecordStarted();

        void onRecordStop(String str, int i);
    }

    private final class PositionUpdater implements Runnable {
        private int mDurationMs;
        private final ScheduledExecutorService mExecutorService;
        private final Object mLock = new Object();
        private ScheduledFuture<?> mScheduledFuture;
        private Runnable mUpdateClipPositionRunnable = new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                synchronized (PositionUpdater.this.mLock) {
                    if (PositionUpdater.this.mScheduledFuture == null || RecordLayout.this.mPresenter == null) {
                    } else {
                        int currentPositionMs = RecordLayout.this.mPresenter.getRecordPosition();
                        RecordLayout.this.setClipPosition(currentPositionMs, PositionUpdater.this.mDurationMs);
                    }
                }
            }
        };

        public PositionUpdater(int durationMs, ScheduledExecutorService executorService) {
            this.mDurationMs = durationMs;
            this.mExecutorService = executorService;
        }

        public void run() {
            RecordLayout.this.post(this.mUpdateClipPositionRunnable);
        }

        public void startUpdating() {
            synchronized (this.mLock) {
                cancelPendingRunnables();
                this.mScheduledFuture = this.mExecutorService.scheduleAtFixedRate(this, 0, 33, TimeUnit.MILLISECONDS);
            }
        }

        public void stopUpdating() {
            synchronized (this.mLock) {
                cancelPendingRunnables();
            }
        }

        private void cancelPendingRunnables() {
            if (this.mScheduledFuture != null) {
                this.mScheduledFuture.cancel(true);
                this.mScheduledFuture = null;
            }
            RecordLayout.this.removeCallbacks(this.mUpdateClipPositionRunnable);
        }
    }

    public void setStatusListener(RecordStatusListener listener) {
        this.mStatusListener = listener;
    }

    public RecordLayout(Context context) {
        this(context, null);
    }

    public RecordLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.audio_recorder_dialog, this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.mTextView = (TextView) findViewById(R.id.duration);
        this.mTotalDuractionTv = (TextView) findViewById(R.id.total_duration_text);
        this.mTotalDuractionTv.setText(formatTime(15));
        if (getContext() != null) {
            this.mTotalDuractionTv.setContentDescription(formatTimeDescription(getContext(), 15));
        }
    }

    public void setClipPosition(int positionMs, int durationMs) {
        this.mProgressBar.setMax(durationMs);
        this.mProgressBar.setProgress(positionMs);
        int time = positionMs / 30;
        this.mTextView.setText(formatTime(time));
        if (getContext() != null) {
            this.mTextView.setContentDescription(formatTimeDescription(getContext(), time));
        }
        if (durationMs == positionMs && this.mPresenter != null) {
            this.mPresenter.stopRecording();
        }
    }

    private String formatTimeDescription(Context context, int time) {
        String seconds = String.format(context.getResources().getQuantityText(R.plurals.callDetailsDurationFormatHours_Seconds, time).toString(), new Object[]{Integer.valueOf(time)});
        return context.getString(R.string.callDetailsDurationFormat_Merge, new Object[]{"", "", seconds});
    }

    private static String formatTime(int time) {
        return String.format("00:%02d", new Object[]{Integer.valueOf(time)});
    }

    public void onRecordStarted(int duration, ScheduledExecutorService executorService) {
        if (this.mStatusListener != null) {
            this.mStatusListener.onRecordStarted();
        }
        if (this.mPositionUpdater != null) {
            this.mPositionUpdater.stopUpdating();
            this.mPositionUpdater = null;
        }
        this.mPositionUpdater = new PositionUpdater(duration, executorService);
        this.mPositionUpdater.startUpdating();
    }

    public void onRecordStop(String fileName, int duration) {
        if (this.mStatusListener != null) {
            this.mStatusListener.onRecordStop(fileName, duration);
        }
        if (this.mPositionUpdater != null) {
            this.mPositionUpdater.stopUpdating();
            this.mPositionUpdater = null;
        }
    }

    public void setPresenter(RecordPresenter presenter) {
        this.mPresenter = presenter;
    }

    public void onRecordAbort() {
        if (this.mStatusListener != null) {
            this.mStatusListener.onRecordAbort();
        }
    }

    public void onShowError() {
        Toast.makeText(getContext(), R.string.audio_occupy_error_Toast, 1).show();
    }
}
