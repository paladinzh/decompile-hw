package huawei.android.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.MSimTelephonyConstants;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownLoadWidget extends FrameLayout {
    private ProgressBar mDownLoadProgress;
    private boolean mIsStop;
    private TextView mPercentage;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int isStop;
        int progress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.progress = in.readInt();
            this.isStop = in.readByte();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.progress);
            out.writeInt(this.isStop);
        }
    }

    public DownLoadWidget(Context context) {
        super(context);
        this.mDownLoadProgress = null;
        this.mPercentage = null;
        this.mIsStop = true;
        init();
    }

    public DownLoadWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDownLoadProgress = null;
        this.mPercentage = null;
        this.mIsStop = true;
        init();
    }

    private void init() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(34013214, this, true);
        this.mDownLoadProgress = (ProgressBar) findViewById(34603101);
        this.mPercentage = (TextView) findViewById(34603102);
    }

    public void incrementProgressBy(int progress) {
        this.mIsStop = false;
        this.mDownLoadProgress.setBackground(null);
        this.mDownLoadProgress.incrementProgressBy(progress);
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    private void setPercentage(int percent) {
        if (this.mIsStop) {
            this.mPercentage.setText(MSimTelephonyConstants.MY_RADIO_PLATFORM);
            return;
        }
        this.mPercentage.setText(String.format("%2d", new Object[]{Integer.valueOf(percent)}) + "%");
    }

    public void stop() {
        this.mIsStop = true;
        this.mDownLoadProgress.setBackground(getResources().getDrawable(33751289));
        setPercentage(this.mDownLoadProgress.getProgress());
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.progress = this.mDownLoadProgress.getProgress();
        ss.isStop = this.mIsStop ? 1 : 0;
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        boolean z = false;
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (ss.isStop != 0) {
            z = true;
        }
        this.mIsStop = z;
        if (this.mIsStop) {
            this.mDownLoadProgress.setBackground(getResources().getDrawable(33751289));
        }
        setPercentage(ss.progress);
    }
}
