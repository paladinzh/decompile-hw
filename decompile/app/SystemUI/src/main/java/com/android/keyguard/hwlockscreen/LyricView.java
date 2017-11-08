package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.R$id;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class LyricView extends LinearLayout implements Runnable, Callback {
    private List<String> mLyricList = new ArrayList();
    private int mNextTime = 0;
    private TextView mTextViewFirst;
    private TextView mTextViewSecond;
    private List<Integer> mTimeList = new ArrayList();

    public LyricView(Context context) {
        super(context);
    }

    public LyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateLyricContent();
    }

    private void updateLyricContent() {
        this.mTimeList.clear();
        this.mLyricList.clear();
        this.mTimeList.addAll(MusicInfo.getInst().getLyricTimeList());
        this.mLyricList.addAll(MusicInfo.getInst().getLyricContentList());
        HwLog.d("HwMusicLyricView", "The time is " + this.mTimeList);
        HwLog.d("HwMusicLyricView", "The lyric is " + this.mLyricList);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setLyricTextView(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
        if (MusicInfo.getInst().isPositionValid()) {
            updateLyricView(MusicInfo.getInst().getNowPosition());
        }
        AppHandler.addListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTextViewFirst = (TextView) findViewById(R$id.first_line);
        this.mTextViewSecond = (TextView) findViewById(R$id.second_line);
    }

    private void updateLyricView(long currentPosition) {
        HwLog.d("HwMusicLyricView", "startShowLyric start");
        this.mNextTime = 0;
        int timeSize = this.mTimeList.size();
        int contentSize = this.mLyricList.size();
        if (timeSize == 0 || contentSize == 0 || timeSize != contentSize) {
            HwLog.w("HwMusicLyricView", "startShowLyric with size invalid timeSize : " + timeSize + " contentSize : " + contentSize);
            return;
        }
        int index = 0;
        while (index < timeSize - 1) {
            int currentTime = ((Integer) this.mTimeList.get(index)).intValue();
            int nextTime = ((Integer) this.mTimeList.get(index + 1)).intValue();
            HwLog.d("HwMusicLyricView", "startShowLyric with currentPosition : " + currentPosition + " currentTime : " + currentTime + " nextTime : " + nextTime);
            if (currentPosition >= ((long) currentTime) && currentPosition < ((long) nextTime)) {
                setLyricTextView((String) this.mLyricList.get(index), (String) this.mLyricList.get(index + 1));
                removeCallbacks(this);
                this.mNextTime = nextTime;
                postDelayed(this, ((long) nextTime) - currentPosition);
                break;
            } else if (currentPosition < ((long) currentTime)) {
                setLyricTextView(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
                removeCallbacks(this);
                this.mNextTime = currentTime;
                postDelayed(this, ((long) currentTime) - currentPosition);
                break;
            } else {
                HwLog.i("HwMusicLyricView", "startShowLyric no match!");
                index++;
            }
        }
    }

    public void run() {
        HwLog.d("HwMusicLyricView", "runable start");
        updateLyricView((long) this.mNextTime);
    }

    private void setLyricTextView(String firstLyric, String secondLyric) {
        if (this.mTextViewFirst != null) {
            this.mTextViewFirst.setText(firstLyric);
        }
        if (this.mTextViewSecond != null) {
            this.mTextViewSecond.setText(secondLyric);
        }
    }

    public void dealWithLyricChange() {
        HwLog.d("HwMusicLyricView", "updateContent start");
        updateLyricContent();
        removeCallbacks(this);
        if (MusicInfo.getInst().isPositionValid()) {
            updateLyricView(MusicInfo.getInst().getNowPosition());
        } else {
            setLyricTextView(BuildConfig.FLAVOR, BuildConfig.FLAVOR);
        }
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 111) {
            dealWithLyricChange();
        }
        return false;
    }
}
