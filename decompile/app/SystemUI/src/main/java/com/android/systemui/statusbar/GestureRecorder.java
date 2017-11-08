package com.android.systemui.statusbar;

import android.os.Handler;
import android.os.SystemClock;
import java.util.HashSet;
import java.util.LinkedList;

public class GestureRecorder {
    public static final String TAG = GestureRecorder.class.getSimpleName();
    private Gesture mCurrentGesture;
    private LinkedList<Gesture> mGestures;
    private Handler mHandler;

    public class Gesture {
        boolean mComplete = false;
        long mDownTime = -1;
        private LinkedList<Record> mRecords = new LinkedList();
        private HashSet<String> mTags = new HashSet();

        public abstract class Record {
            long time;
        }

        public class TagRecord extends Record {
            public String info;
            public String tag;

            public TagRecord(long when, String tag, String info) {
                super();
                this.time = when;
                this.tag = tag;
                this.info = info;
            }
        }

        public void tag(long when, String tag, String info) {
            this.mRecords.add(new TagRecord(when, tag, info));
            this.mTags.add(tag);
        }
    }

    public void tag(long when, String tag, String info) {
        synchronized (this.mGestures) {
            if (this.mCurrentGesture == null) {
                this.mCurrentGesture = new Gesture();
                this.mGestures.add(this.mCurrentGesture);
            }
            this.mCurrentGesture.tag(when, tag, info);
        }
        saveLater();
    }

    public void tag(String tag, String info) {
        tag(SystemClock.uptimeMillis(), tag, info);
    }

    public void saveLater() {
        this.mHandler.removeMessages(6351);
        this.mHandler.sendEmptyMessageDelayed(6351, 5000);
    }
}
