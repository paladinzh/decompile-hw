package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import java.util.ArrayDeque;

public class HumanInteractionClassifier extends Classifier {
    private static HumanInteractionClassifier sInstance = null;
    private final ArrayDeque<MotionEvent> mBufferedEvents = new ArrayDeque();
    private final Context mContext;
    private int mCurrentType = 7;
    private final float mDpi;
    private boolean mEnableClassifier = false;
    private final GestureClassifier[] mGestureClassifiers;
    private final Handler mHandler = new Handler();
    private final HistoryEvaluator mHistoryEvaluator;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            HumanInteractionClassifier.this.updateConfiguration();
        }
    };
    private final StrokeClassifier[] mStrokeClassifiers;

    private HumanInteractionClassifier(Context context) {
        this.mContext = context;
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        this.mDpi = (displayMetrics.xdpi + displayMetrics.ydpi) / 2.0f;
        this.mClassifierData = new ClassifierData(this.mDpi);
        this.mHistoryEvaluator = new HistoryEvaluator();
        this.mStrokeClassifiers = new StrokeClassifier[]{new DurationCountClassifier(this.mClassifierData), new EndPointRatioClassifier(this.mClassifierData), new EndPointLengthClassifier(this.mClassifierData), new AccelerationClassifier(this.mClassifierData), new DirectionClassifier(this.mClassifierData)};
        this.mGestureClassifiers = new GestureClassifier[]{new PointerCountClassifier(this.mClassifierData), new ProximityClassifier(this.mClassifierData)};
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("HIC_enable"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static HumanInteractionClassifier getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HumanInteractionClassifier(context);
        }
        return sInstance;
    }

    private void updateConfiguration() {
        boolean z = true;
        if (Global.getInt(this.mContext.getContentResolver(), "HIC_enable", 1) == 0) {
            z = false;
        }
        this.mEnableClassifier = z;
    }

    public void setType(int type) {
        this.mCurrentType = type;
    }

    public void onTouchEvent(MotionEvent event) {
        if (this.mEnableClassifier) {
            if (this.mCurrentType == 2) {
                this.mBufferedEvents.add(MotionEvent.obtain(event));
                Point pointEnd = new Point(event.getX() / this.mDpi, event.getY() / this.mDpi);
                while (pointEnd.dist(new Point(((MotionEvent) this.mBufferedEvents.getFirst()).getX() / this.mDpi, ((MotionEvent) this.mBufferedEvents.getFirst()).getY() / this.mDpi)) > 0.1f) {
                    addTouchEvent((MotionEvent) this.mBufferedEvents.getFirst());
                    this.mBufferedEvents.remove();
                }
                if (event.getActionMasked() == 1) {
                    ((MotionEvent) this.mBufferedEvents.getFirst()).setAction(1);
                    addTouchEvent((MotionEvent) this.mBufferedEvents.getFirst());
                    this.mBufferedEvents.clear();
                }
            } else {
                addTouchEvent(event);
            }
        }
    }

    private void addTouchEvent(MotionEvent event) {
        this.mClassifierData.update(event);
        for (StrokeClassifier c : this.mStrokeClassifiers) {
            c.onTouchEvent(event);
        }
        for (GestureClassifier c2 : this.mGestureClassifiers) {
            c2.onTouchEvent(event);
        }
        int size = this.mClassifierData.getEndingStrokes().size();
        for (int i = 0; i < size; i++) {
            Stroke stroke = (Stroke) this.mClassifierData.getEndingStrokes().get(i);
            float evaluation = 0.0f;
            StringBuilder stringBuilder = FalsingLog.ENABLED ? new StringBuilder("stroke") : null;
            for (StrokeClassifier c3 : this.mStrokeClassifiers) {
                String tag;
                float e = c3.getFalseTouchEvaluation(this.mCurrentType, stroke);
                if (FalsingLog.ENABLED) {
                    tag = c3.getTag();
                    StringBuilder append = stringBuilder.append(" ");
                    if (e < 1.0f) {
                        tag = tag.toLowerCase();
                    }
                    append.append(tag).append("=").append(e);
                }
                evaluation += e;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", stringBuilder.toString());
            }
            this.mHistoryEvaluator.addStroke(evaluation);
        }
        int action = event.getActionMasked();
        if (action == 1 || action == 3) {
            evaluation = 0.0f;
            stringBuilder = FalsingLog.ENABLED ? new StringBuilder("gesture") : null;
            for (GestureClassifier c22 : this.mGestureClassifiers) {
                e = c22.getFalseTouchEvaluation(this.mCurrentType);
                if (FalsingLog.ENABLED) {
                    tag = c22.getTag();
                    append = stringBuilder.append(" ");
                    if (e < 1.0f) {
                        tag = tag.toLowerCase();
                    }
                    append.append(tag).append("=").append(e);
                }
                evaluation += e;
            }
            if (FalsingLog.ENABLED) {
                FalsingLog.i(" addTouchEvent", stringBuilder.toString());
            }
            this.mHistoryEvaluator.addGesture(evaluation);
            setType(7);
        }
        this.mClassifierData.cleanUp(event);
    }

    public void onSensorChanged(SensorEvent event) {
        int i = 0;
        for (Classifier c : this.mStrokeClassifiers) {
            c.onSensorChanged(event);
        }
        GestureClassifier[] gestureClassifierArr = this.mGestureClassifiers;
        int length = gestureClassifierArr.length;
        while (i < length) {
            gestureClassifierArr[i].onSensorChanged(event);
            i++;
        }
    }

    public boolean isFalseTouch() {
        int i = 0;
        if (!this.mEnableClassifier) {
            return false;
        }
        float evaluation = this.mHistoryEvaluator.getEvaluation();
        boolean result = evaluation >= 5.0f;
        if (FalsingLog.ENABLED) {
            String str = "isFalseTouch";
            StringBuilder append = new StringBuilder().append("eval=").append(evaluation).append(" result=");
            if (result) {
                i = 1;
            }
            FalsingLog.i(str, append.append(i).toString());
        }
        return result;
    }

    public boolean isEnabled() {
        return this.mEnableClassifier;
    }

    public String getTag() {
        return "HIC";
    }
}
