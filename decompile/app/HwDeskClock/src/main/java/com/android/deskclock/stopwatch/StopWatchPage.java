package com.android.deskclock.stopwatch;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.alarmclock.WorldAnalogClock;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.ClockFragment;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.ViewHolder$StopwatchViewHolder;
import com.android.deskclock.stopwatch.AnaimationListView.SpeedDownAnimation;
import com.android.deskclock.stopwatch.StopwatchTimesShow.OnCircleTimeCallBack;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import com.huawei.immersion.Vibetonz;
import java.util.ArrayList;
import java.util.Locale;

public class StopWatchPage extends ClockFragment implements OnClickListener {
    private static float list_y = 0.0f;
    private static int mAnimationOrList = 0;
    private static boolean mHasShutdown = false;
    private static int mState = 0;
    private static int move_height = 177;
    private static boolean shouldcount = true;
    private static int view_height = 0;
    private View btnview;
    private boolean isTimeOverHour;
    private int leftwight;
    long mAccumulatedIntervalTime = 0;
    long mAccumulatedTime = 0;
    private FrameLayout mFramelayoutRing;
    private TextView mHeadViewAccTimeAnim;
    private View mHeadViewDivider;
    private TextView mHeadViewIntervalTimeAnim;
    private TextView mHeadViewLapNumAnim;
    private Vibetonz mImmDevice;
    private Interpolator mInterpolatorAD = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
    private Interpolator mInterpolatorTM = new PathInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
    long mIntervalStartTime = 0;
    private ArrayList<Lap> mLaps = new ArrayList();
    LapsListAdapter mLapsAdapter;
    private AnaimationListView mLapsList;
    private TextView mLastViewAccTimeAnim;
    private TextView mLastViewIntervalTimeAnim;
    private TextView mLastViewLapNumAnim;
    private RelativeLayout mLastitemAnim;
    private int mLayoutDirection;
    private RelativeLayout mListFirstItemAnim;
    private ImageView mMeterTimesTVbtn;
    private Long mMinGap;
    private ImageView mResetTVbtn;
    private RelativeLayout mSWLayout;
    private ImageView mStartTVbtn;
    long mStartTime = 0;
    private ImageView mStopTVbtn;
    private CircleStopWatch mStopWatchRing;
    Runnable mTimeUpdateThread = new Runnable() {
        public void run() {
            long curTime = Utils.getTimeNow();
            long totalTime = StopWatchPage.this.mAccumulatedTime + (curTime - StopWatchPage.this.mStartTime);
            long intervalTime = StopWatchPage.this.mAccumulatedIntervalTime + (curTime - StopWatchPage.this.mIntervalStartTime);
            if (StopWatchPage.this.stopwatch_ring_timetext != null) {
                StopWatchPage.this.stopwatch_ring_timetext.updateTotalTime(totalTime);
                StopWatchPage.this.stopwatch_ring_timetext.updateIntervalTime(intervalTime);
                StopWatchPage.this.stopwatch_ring_timetext.postDelayed(StopWatchPage.this.mTimeUpdateThread, 120);
            }
        }
    };
    private ObjectAnimator ob1;
    private ObjectAnimator ob2;
    private SharedPreferences prefs;
    private int rightwight;
    private StopwatchTimesShow stopwatch_ring_timetext;

    private static class FirstSpeedDownAnimation extends Animation {
        private float mFrom;
        private View mTarget;
        private float mTo;

        public void setTarget(View view) {
            this.mTarget = view;
        }

        public FirstSpeedDownAnimation(float from, float to) {
            this.mFrom = from;
            this.mTo = to;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (this.mTarget != null) {
                float mFromYDelta = this.mFrom;
                float mToYDelta = this.mTo;
                float dy = mFromYDelta;
                if (mFromYDelta != mToYDelta) {
                    dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
                }
                t.getMatrix().setTranslate(0.0f, dy);
            }
        }

        protected FirstSpeedDownAnimation clone() throws CloneNotSupportedException {
            FirstSpeedDownAnimation result = (FirstSpeedDownAnimation) super.clone();
            result.setTarget(null);
            return result;
        }
    }

    static class Lap {
        public long mLapTime;
        public long mTotalTime;

        Lap() {
            this.mLapTime = 0;
            this.mTotalTime = 0;
        }

        Lap(long time, long total) {
            if ((time % 10) + ((total - time) % 10) >= 10) {
                time += 10;
            }
            this.mLapTime = time;
            this.mTotalTime = total;
        }
    }

    class LapsListAdapter extends BaseAdapter {
        private boolean allVisiable = true;
        private Context mContext;
        private final LayoutInflater mInflater;

        public LapsListAdapter(Context context) {
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mContext = context;
            if (Vibetonz.isVibrateOn(StopWatchPage.this.getContext())) {
                StopWatchPage.this.mImmDevice = Vibetonz.getInstance();
            }
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder$StopwatchViewHolder viewHolder;
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.lap_view, parent, false);
                viewHolder = new ViewHolder$StopwatchViewHolder();
                viewHolder.mCount = (TextView) convertView.findViewById(R.id.lap_number);
                viewHolder.mLapTime = (TextView) convertView.findViewById(R.id.lap_time);
                viewHolder.mTotalTime = (TextView) convertView.findViewById(R.id.lap_total);
                viewHolder.mLine = convertView.findViewById(R.id.item_divider);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder$StopwatchViewHolder) convertView.getTag();
            }
            long lap = ((Lap) StopWatchPage.this.mLaps.get(position)).mLapTime;
            long total = ((Lap) StopWatchPage.this.mLaps.get(position)).mTotalTime;
            viewHolder.mLapTime.setText(this.mContext.getResources().getString(R.string.list_stopwatch_Interval, new Object[]{Stopwatches.getTimeText(lap, false)}));
            viewHolder.mTotalTime.setText(Stopwatches.getTimeText(total, false));
            if (total >= 3600000 || StopWatchPage.this.isTimeOverHour) {
                viewHolder.mTotalTime.setTextSize(1, 22.0f);
            } else {
                viewHolder.mTotalTime.setTextSize(1, WorldAnalogClock.DEGREE_ONE_HOUR);
            }
            viewHolder.mCount.setText(this.mContext.getResources().getString(R.string.listname_stopwatch_name, new Object[]{Integer.valueOf(StopWatchPage.this.mLaps.size() - position)}));
            if (this.allVisiable) {
                viewHolder.mCount.setVisibility(0);
                viewHolder.mLapTime.setVisibility(0);
                viewHolder.mTotalTime.setVisibility(0);
                viewHolder.mLine.setVisibility(0);
            } else {
                viewHolder.mCount.setVisibility(4);
                viewHolder.mLapTime.setVisibility(4);
                viewHolder.mTotalTime.setVisibility(4);
                viewHolder.mLine.setVisibility(4);
            }
            return convertView;
        }

        public int getCount() {
            return StopWatchPage.this.mLaps.size();
        }

        public Object getItem(int position) {
            return StopWatchPage.this.mLaps.get(position);
        }

        public void addLap(Lap l) {
            StopWatchPage.this.mLaps.add(0, l);
            notifyDataSetChanged();
        }

        public void clearLaps() {
            StopWatchPage.this.mLaps.clear();
            notifyDataSetChanged();
        }

        public long[] getLapTimes() {
            int size = StopWatchPage.this.mLaps.size();
            long[] laps = new long[size];
            for (int i = 0; i < size; i++) {
                laps[i] = ((Lap) StopWatchPage.this.mLaps.get(i)).mTotalTime;
            }
            return laps;
        }

        public void setLapTimes(long[] laps) {
            if (laps != null && laps.length != 0) {
                int i;
                StopWatchPage.this.mLaps.clear();
                for (long lap : laps) {
                    StopWatchPage.this.mLaps.add(new Lap(lap, 0));
                }
                long totalTime = 0;
                for (i = size - 1; i >= 0; i--) {
                    totalTime += laps[i];
                    ((Lap) StopWatchPage.this.mLaps.get(i)).mTotalTime = totalTime;
                }
                notifyDataSetChanged();
            }
        }
    }

    private void setObject2Null() {
        this.mStopWatchRing = null;
        this.stopwatch_ring_timetext = null;
        this.mListFirstItemAnim = null;
        this.mHeadViewAccTimeAnim = null;
        this.mHeadViewIntervalTimeAnim = null;
        this.mHeadViewLapNumAnim = null;
        this.mHeadViewDivider = null;
        this.mLastitemAnim = null;
        this.mLastViewAccTimeAnim = null;
        this.mLastViewIntervalTimeAnim = null;
        this.mLastViewLapNumAnim = null;
        this.mStartTVbtn = null;
        this.mStopTVbtn = null;
        this.mResetTVbtn = null;
        this.mMeterTimesTVbtn = null;
        this.mLapsList = null;
        if (this.mLaps != null) {
            this.mLaps.clear();
        }
        this.mLaps = null;
        this.mSWLayout = null;
        this.mLapsAdapter = null;
    }

    public static boolean isRunning() {
        return mState == 1;
    }

    private void meterTimesBtnAction() {
        long time = Utils.getTimeNow();
        switch (mState) {
            case 1:
                if (Utils.isLandScreen(getActivity())) {
                    this.mLapsList.setVisibility(0);
                    if (this.mLapsAdapter.getCount() == 0) {
                        int screenwith = getActivity().getWindowManager().getDefaultDisplay().getWidth();
                        int trance = (screenwith - ((this.leftwight * screenwith) / (this.leftwight + this.rightwight))) / 2;
                        if (this.mLayoutDirection == 1) {
                            trance = -trance;
                        }
                        this.ob1 = ObjectAnimator.ofFloat(this.btnview, "translationX", new float[]{(float) (-trance)});
                        this.ob1.setDuration(300);
                        this.ob1.start();
                        this.ob2 = ObjectAnimator.ofFloat(this.mFramelayoutRing, "translationX", new float[]{(float) (-trance)});
                        this.ob2.setDuration(300);
                        this.ob2.start();
                    }
                }
                addLapTime(time);
                int count = this.mLapsAdapter.getCount();
                if (shouldcount) {
                    this.mStopWatchRing.increaseCount(count);
                    this.mStopWatchRing.startCountAnimation(true, count - 1);
                }
                if (count == 99) {
                    setShoudCount(false);
                }
                Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, StopwatchService.class);
                    intent.setAction("com.deskclock.stopwatch.soundpool.count");
                    activity.startService(intent);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void resetBtnAction() {
        switch (mState) {
            case 2:
                doReset();
                return;
            default:
                return;
        }
    }

    private void stopBtnAction() {
        switch (mState) {
            case 1:
                long curTime = Utils.getTimeNow();
                this.mAccumulatedTime += curTime - this.mStartTime;
                this.mAccumulatedIntervalTime += curTime - this.mIntervalStartTime;
                doStop();
                if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
                    this.mImmDevice.pausePlayEffect(200);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void startBtnAction() {
        long time = Utils.getTimeNow();
        switch (mState) {
            case 0:
                doStart(time);
                return;
            case 2:
                if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
                    this.mImmDevice.resumePausedEffect(200);
                }
                doStart(time);
                return;
            default:
                return;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("StopWatchPage", "onCreateView");
        Stopwatches.updateLocal(Locale.getDefault());
        this.prefs = Utils.getDefaultSharedPreferences(getActivity());
        this.mSWLayout = (RelativeLayout) inflater.inflate(R.layout.stopwatch, container, false);
        this.leftwight = getResources().getInteger(R.integer.display_layout_layout_weight);
        this.rightwight = getResources().getInteger(R.integer.menu_layout_layout_weight);
        initView(this.mSWLayout);
        restoreTimerState();
        this.mLayoutDirection = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
        if (Utils.isLandScreen(getActivity()) && this.mLapsAdapter != null && this.mLapsAdapter.getCount() > 0) {
            int screenwith = getActivity().getWindowManager().getDefaultDisplay().getWidth();
            int trance = (screenwith - ((this.leftwight * screenwith) / (this.leftwight + this.rightwight))) / 2;
            if (this.mLayoutDirection == 1) {
                trance = -trance;
            }
            this.ob1 = ObjectAnimator.ofFloat(this.btnview, "translationX", new float[]{(float) (-trance)});
            this.ob1.setDuration(0);
            this.ob1.start();
            this.ob2 = ObjectAnimator.ofFloat(this.mFramelayoutRing, "translationX", new float[]{(float) (-trance)});
            this.ob2.setDuration(0);
            this.ob2.start();
        }
        return this.mSWLayout;
    }

    public void restoreTimerState() {
        if (this.prefs != null) {
            readFromSharedPref(this.prefs);
            this.mStopWatchRing.readFromSharedPref(this.prefs, "sw", mState);
            initStatus();
        }
    }

    private void initView(View v) {
        this.btnview = v.findViewById(R.id.btn_container);
        this.mStopWatchRing = (CircleStopWatch) v.findViewById(R.id.stopwatch_ring_view);
        this.stopwatch_ring_timetext = (StopwatchTimesShow) v.findViewById(R.id.stopwatch_ring_timetext);
        this.stopwatch_ring_timetext.setOnCircleTimeCallBack(new OnCircleTimeCallBack() {
            public void onTimeOverHour() {
                if (!StopWatchPage.this.isTimeOverHour) {
                    StopWatchPage.this.isTimeOverHour = true;
                    StopWatchPage.this.mLapsList.invalidate();
                }
            }

            public void onTimeBelowHour() {
                if (StopWatchPage.this.isTimeOverHour) {
                    StopWatchPage.this.isTimeOverHour = false;
                    StopWatchPage.this.mLapsList.invalidate();
                }
            }
        });
        this.mFramelayoutRing = (FrameLayout) v.findViewById(R.id.stopwatch_ring_farmelayout);
        setRingPading();
        if (!Utils.isLandScreen(getActivity())) {
            this.mListFirstItemAnim = (RelativeLayout) v.findViewById(R.id.list_item_anim);
            if (this.mListFirstItemAnim != null) {
                this.mListFirstItemAnim.setVisibility(8);
            }
            this.mHeadViewAccTimeAnim = (TextView) v.findViewById(R.id.lap_number);
            this.mHeadViewIntervalTimeAnim = (TextView) v.findViewById(R.id.lap_time);
            this.mHeadViewLapNumAnim = (TextView) v.findViewById(R.id.lap_total);
            this.mHeadViewDivider = v.findViewById(R.id.item_divider);
            this.mLastitemAnim = (RelativeLayout) v.findViewById(R.id.last_item_anim);
            if (this.mLastitemAnim != null) {
                this.mLastitemAnim.setVisibility(8);
            }
            this.mLastViewAccTimeAnim = (TextView) v.findViewById(R.id.last_lap_number);
            this.mLastViewIntervalTimeAnim = (TextView) v.findViewById(R.id.last_lap_time);
            this.mLastViewLapNumAnim = (TextView) v.findViewById(R.id.last_lap_total);
        }
        this.mStartTVbtn = (ImageView) v.findViewById(R.id.stopwatch_start_tv_btn);
        this.mStopTVbtn = (ImageView) v.findViewById(R.id.stopwatch_stop_tv_btn);
        this.mResetTVbtn = (ImageView) v.findViewById(R.id.stopwatch_reset_tv_btn);
        this.mMeterTimesTVbtn = (ImageView) v.findViewById(R.id.stopwatch_meter_times_tv_btn);
        this.mLapsList = (AnaimationListView) v.findViewById(16908298);
        this.mLapsAdapter = new LapsListAdapter(getActivity());
        if (this.mLapsList != null) {
            this.mLapsList.setAdapter(this.mLapsAdapter);
            if (!Utils.isLandScreen(getActivity())) {
                this.mLapsList.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.stopwatch_footview, this.mLapsList, false), null, false);
            }
        }
        this.mStartTVbtn.setOnClickListener(this);
        this.mStopTVbtn.setOnClickListener(this);
        this.mResetTVbtn.setOnClickListener(this);
        addMeterTimeLayoutLintener(v);
    }

    private void addMeterTimeLayoutLintener(View v) {
        HwLog.d("StopWatchPage", "addMeterTimeLayoutLintener");
        v.findViewById(R.id.stopwatch_meter_times_layout).setOnClickListener(this);
    }

    private void initStatus() {
        if (this.mSWLayout != null && this.mStopWatchRing != null) {
            setButtons(mState);
            checkUI();
            this.stopwatch_ring_timetext.updateTotalTime(this.mAccumulatedTime);
            this.stopwatch_ring_timetext.updateIntervalTime(this.mAccumulatedIntervalTime);
            AlarmsMainActivity.wakeLock(Config.clockTabIndex());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("StopWatchPage", "onConfigurationChanged");
    }

    private void startThread() {
        if (this.mSWLayout != null && this.mStopWatchRing != null) {
            if (mState == 1) {
                startUpdateThread();
            } else if (mState == 0) {
                this.stopwatch_ring_timetext.updateTotalTime(this.mAccumulatedTime);
                this.stopwatch_ring_timetext.updateIntervalTime(this.mAccumulatedIntervalTime);
            }
        }
    }

    public void onResume() {
        Log.d("StopWatchPage", "onResume");
        Activity activity = getActivity();
        if (activity != null) {
            startThread();
            if (mState == 1 && Config.clockTabIndex() == 2) {
                Intent intent = new Intent(activity, StopwatchService.class);
                intent.setAction("com.deskclock.stopwatch.soundpool.resume");
                intent.putExtra("stopwatch_state", mState);
                intent.putExtra("aquire_wakelock", true);
                activity.startService(intent);
            }
            if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
                Config.updateVibratePause(false);
            }
            super.onResume();
        }
    }

    private boolean reachedMaxLaps() {
        return this.mLapsAdapter.getCount() >= 99;
    }

    public void onPause() {
        Log.d("StopWatchPage", "onPause");
        Activity activity = getActivity();
        if (activity != null) {
            if (mState == 1) {
                stopUpdateThread();
            } else {
                this.mStopWatchRing.writeToSharedPrefForStatus(this.prefs, "sw");
            }
            if (!mHasShutdown) {
                writeToSharedPref(this.prefs);
            }
            if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
                Config.updateVibratePause(true);
            }
            if (mState == 1) {
                Intent intent = new Intent(activity, StopwatchService.class);
                intent.setAction("com.deskclock.stopwatch.soundpool.pause");
                intent.putExtra("release_wakelock", true);
                activity.startService(intent);
            }
            super.onPause();
        }
    }

    public static void setShutdown(boolean isShutdown) {
        mHasShutdown = isShutdown;
    }

    public void onFragmentPause() {
        super.onFragmentPause();
        Log.d("StopWatchPage", "onFragmentPause mState = " + mState);
        Activity activity = getActivity();
        if (activity != null) {
            if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
                this.mImmDevice.pausePlayEffect(200);
            }
            if (mState == 1) {
                Intent intent = new Intent(activity, StopwatchService.class);
                intent.setAction("com.deskclock.stopwatch.soundpool.pause");
                activity.startService(intent);
            }
        }
    }

    public void onFragmentResume() {
        super.onFragmentResume();
        Log.d("StopWatchPage", "onFragmentResume mState = " + mState);
        Activity activity = getActivity();
        if (activity != null && mState == 1) {
            Intent intent = new Intent(activity, StopwatchService.class);
            intent.setAction("com.deskclock.stopwatch.soundpool.resume");
            activity.startService(intent);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("StopWatchPage", "onCreate");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setHeight() {
        this.mFramelayoutRing.getLayoutParams().height = (getActivity().getWindowManager().getDefaultDisplay().getHeight() * 277) / 690;
    }

    public void setRingPading() {
        if (!Utils.isLandScreen(getActivity())) {
            setHeight();
            float density = getResources().getDisplayMetrics().density;
            if (density - 2.0f >= 0.01f) {
                if (density - 2.5f < 0.01f) {
                    this.mFramelayoutRing.setPadding(0, (int) getResources().getDimension(R.dimen.stopwatch_ring_paddingtop_3_small), 0, 0);
                } else if (density - 2.75f < 0.01f) {
                    this.mFramelayoutRing.setPadding(0, (int) getResources().getDimension(R.dimen.stopwatch_ring_paddingtop_3_medium), 0, 0);
                } else if (Math.abs(density - 3.125f) < 0.01f) {
                    this.mFramelayoutRing.setPadding(0, (int) getResources().getDimension(R.dimen.stopwatch_ring_paddingtop_4_small), 0, 0);
                } else if (Math.abs(density - 3.5f) < 0.01f) {
                    this.mFramelayoutRing.setPadding(0, (int) getResources().getDimension(R.dimen.stopwatch_ring_paddingtop_4_medium), 0, 0);
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
        restoreTimerState();
        Log.d("StopWatchPage", "onStart");
        dealQuickAction();
    }

    private void dealQuickAction() {
        Activity activity = getActivity();
        if (activity == null) {
            Log.iRelease("StopWatchPage", "dealQuickAction actvity is null.");
            return;
        }
        Intent intent = activity.getIntent();
        if (intent == null) {
            Log.iRelease("StopWatchPage", "dealQuickAction intent is null.");
        } else if (intent.getIntExtra("deskclock.select.tab", -1) != 2) {
            Log.iRelease("StopWatchPage", "dealQuickAction is not stopwatch.");
        } else if (intent.getBooleanExtra("is_quickaction_type", false)) {
            int state = intent.getIntExtra("quickaction_type_state", 1);
            if (state == mState) {
                Log.iRelease("StopWatchPage", "dealQuickAction state is same.");
                return;
            }
            int action_type = intent.getIntExtra("quickaction_type", 1);
            Log.dRelease("StopWatchPage", "dealQuickAction state = " + state + ", action_type = " + action_type + ", mState = " + mState);
            if (action_type == 1) {
                this.mStartTVbtn.performClick();
            } else if (action_type == 2) {
                this.mStopTVbtn.performClick();
            } else if (action_type == 3) {
                this.mStartTVbtn.performClick();
            }
            ClockReporter.reportEventContainMessage(getActivity(), 75, "STARTWAY", 2);
        } else {
            Log.iRelease("StopWatchPage", "dealQuickAction is not quickaction.");
        }
    }

    public void onStop() {
        super.onStop();
        if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
            this.mImmDevice.pausePlayEffect(200);
        }
        Log.d("StopWatchPage", "onStop");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("StopWatchPage", "onDestroy");
        setObject2Null();
        if (this.ob1 != null) {
            this.ob1.end();
            this.ob1 = null;
        }
        if (this.ob2 != null) {
            this.ob2.end();
            this.ob2 = null;
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        Log.d("StopWatchPage", "onDestroyView");
        if (Vibetonz.isVibrateOn(getContext()) && this.mImmDevice != null) {
            this.mImmDevice.stopPlayEffect();
            this.mImmDevice = null;
        }
    }

    private void doStop() {
        stopUpdateThread();
        this.stopwatch_ring_timetext.updateTotalTime(this.mAccumulatedTime);
        this.stopwatch_ring_timetext.updateIntervalTime(this.mAccumulatedIntervalTime);
        setButtons(2);
        Activity activity = getActivity();
        if (activity != null) {
            activity.stopService(new Intent(activity, StopwatchService.class));
        }
    }

    private void doStart(long time) {
        this.mStartTime = time;
        this.mIntervalStartTime = time;
        startUpdateThread();
        setButtons(1);
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, StopwatchService.class);
            intent.setAction("com.deskclock.stopwatch.startService");
            intent.putExtra("stopwatch_state", mState);
            activity.startService(intent);
        }
    }

    private void doReset() {
        setShoudCount(true);
        Utils.clearSwSharedPref(this.prefs);
        this.mAccumulatedTime = 0;
        this.mAccumulatedIntervalTime = 0;
        this.mStartTime = 0;
        this.mIntervalStartTime = 0;
        this.mMinGap = Long.valueOf(0);
        this.mLapsAdapter.clearLaps();
        this.stopwatch_ring_timetext.updateTotalTime(this.mAccumulatedTime);
        this.stopwatch_ring_timetext.updateIntervalTime(this.mAccumulatedIntervalTime);
        this.stopwatch_ring_timetext.isRun = false;
        this.stopwatch_ring_timetext.postInvalidate();
        this.mStopWatchRing.stopAnimation();
        setButtons(0);
        Activity activity = getActivity();
        if (activity != null) {
            activity.stopService(new Intent(activity, StopwatchService.class));
        }
        if (Utils.isLandScreen(getActivity())) {
            this.ob1 = ObjectAnimator.ofFloat(this.btnview, "translationX", new float[]{0.0f});
            this.ob1.setDuration(300);
            this.ob1.start();
            this.ob2 = ObjectAnimator.ofFloat(this.mFramelayoutRing, "translationX", new float[]{0.0f});
            this.ob2.setDuration(300);
            this.ob2.start();
        }
    }

    private static void setShoudCount(boolean b) {
        shouldcount = b;
    }

    private void setButtons(int state) {
        setmState(state);
        AlarmsMainActivity.wakeLock(2);
        switch (state) {
            case 0:
                this.mStartTVbtn.setVisibility(0);
                this.mStopTVbtn.setVisibility(8);
                this.mResetTVbtn.setEnabled(false);
                this.mResetTVbtn.setAlpha(0.2f);
                this.mMeterTimesTVbtn.setEnabled(false);
                this.mMeterTimesTVbtn.setAlpha(0.2f);
                return;
            case 1:
                this.mStartTVbtn.setVisibility(8);
                this.mStopTVbtn.setVisibility(0);
                this.mResetTVbtn.setEnabled(false);
                this.mResetTVbtn.setAlpha(0.2f);
                this.mMeterTimesTVbtn.setEnabled(true);
                this.mMeterTimesTVbtn.setAlpha(1.0f);
                return;
            case 2:
                this.mStopTVbtn.setVisibility(8);
                this.mStartTVbtn.setVisibility(0);
                this.mResetTVbtn.setEnabled(true);
                this.mResetTVbtn.setAlpha(1.0f);
                this.mMeterTimesTVbtn.setEnabled(false);
                this.mMeterTimesTVbtn.setAlpha(0.2f);
                return;
            default:
                return;
        }
    }

    private void addLapTime(long time) {
        if (reachedMaxLaps()) {
            Context activity = getActivity();
            if (activity != null) {
                ToastMaster.showToast(activity, (int) R.string.record_full_Toast, 1);
            }
            return;
        }
        int size = this.mLapsAdapter.getCount();
        long curTime = (time - this.mStartTime) + this.mAccumulatedTime;
        Log.d("StopWatchPage", "curTime + " + Stopwatches.getTimeText(curTime, true));
        this.mIntervalStartTime = time;
        this.mAccumulatedIntervalTime = 0;
        if (size == 0) {
            this.mLapsAdapter.addLap(new Lap(curTime, curTime));
        } else {
            this.mLapsAdapter.addLap(new Lap(curTime - ((Lap) this.mLapsAdapter.getItem(0)).mTotalTime, curTime));
        }
        if (!Utils.isLandScreen(getActivity())) {
            if (!(this.mLapsList.getAdapter() == null || this.mLapsAdapter.getCount() == 0)) {
                if (this.mLapsList.getFirstVisiblePosition() == 0) {
                }
            }
            this.mLapsList.setVisibility(0);
            this.mLapsList.smoothScrollToPosition(0);
            list_y = this.mLapsList.getY();
            addListAnimation(curTime, Stopwatches.getTimeText(((Lap) this.mLaps.get(0)).mLapTime, false), this.mLaps.size());
        }
        this.mLapsAdapter.notifyDataSetChanged();
    }

    private void checkUI() {
        if (this.mLapsAdapter == null) {
            Log.i("StopWatchPage", "changeStatus : the adapter is null...");
            return;
        }
        if (!(mAnimationOrList == 1 || !Utils.isLandScreen(getActivity()) || this.mLaps.size() == 0)) {
            this.mLapsList.setVisibility(0);
        }
        setLineVisibility();
    }

    private void setLineVisibility() {
    }

    private void startUpdateThread() {
        this.stopwatch_ring_timetext.removeCallbacks(this.mTimeUpdateThread);
        this.mStopWatchRing.startAnimation();
        this.stopwatch_ring_timetext.isRun = true;
        this.stopwatch_ring_timetext.postInvalidate();
        this.stopwatch_ring_timetext.post(this.mTimeUpdateThread);
    }

    private void stopUpdateThread() {
        this.mStopWatchRing.pauseAnimation();
        this.stopwatch_ring_timetext.isRun = false;
        if (!mHasShutdown) {
            this.mStopWatchRing.writeToSharedPref(this.prefs, "sw");
        }
        this.stopwatch_ring_timetext.removeCallbacks(this.mTimeUpdateThread);
    }

    private void writeToSharedPref(SharedPreferences prefs) {
        Editor editor = prefs.edit();
        editor.putBoolean("shouldcount_stopwatch", shouldcount);
        editor.putLong("sw_start_time", this.mStartTime);
        editor.putLong("sw_interval_start_time", this.mIntervalStartTime);
        editor.putLong("sw_accum_time", this.mAccumulatedTime);
        editor.putLong("sw_accum_interval_time", this.mAccumulatedIntervalTime);
        editor.putLong("sw_min_time", this.mMinGap.longValue());
        editor.putInt("sw_state", mState);
        if (this.mLapsAdapter != null) {
            long[] laps = this.mLapsAdapter.getLapTimes();
            editor.putInt("sw_lap_num", laps.length);
            for (int i = 0; i < laps.length; i++) {
                editor.putLong("sw_lap_time_" + Integer.toString(laps.length - i), laps[i]);
            }
        }
        editor.apply();
    }

    private void readFromSharedPref(SharedPreferences prefs) {
        setShoudCount(prefs.getBoolean("shouldcount_stopwatch", true));
        this.mStartTime = prefs.getLong("sw_start_time", 0);
        this.mIntervalStartTime = prefs.getLong("sw_interval_start_time", 0);
        this.mAccumulatedTime = prefs.getLong("sw_accum_time", 0);
        this.mAccumulatedIntervalTime = prefs.getLong("sw_accum_interval_time", 0);
        this.mMinGap = Long.valueOf(prefs.getLong("sw_min_time", 0));
        mState = prefs.getInt("sw_state", 0);
        int numLaps = prefs.getInt("sw_lap_num", 0);
        if (this.mLapsAdapter != null && this.mLapsAdapter.getLapTimes().length < numLaps) {
            long[] laps = new long[numLaps];
            long prevLapElapsedTime = 0;
            for (int lap_i = 0; lap_i < numLaps; lap_i++) {
                long lap = prefs.getLong("sw_lap_time_" + Integer.toString(lap_i + 1), 0);
                laps[(numLaps - lap_i) - 1] = lap - prevLapElapsedTime;
                prevLapElapsedTime = lap;
            }
            this.mLapsAdapter.setLapTimes(laps);
        }
    }

    public static void setmState(int state) {
        mState = state;
    }

    private void handleUpEvent(View v) {
        switch (v.getId()) {
            case R.id.stopwatch_reset_tv_btn:
                ClockReporter.reportEventMessage(getActivity(), 57, "");
                resetBtnAction();
                return;
            case R.id.stopwatch_start_tv_btn:
                ClockReporter.reportEventMessage(DeskClockApplication.getDeskClockApplication(), 16, "");
                startBtnAction();
                return;
            case R.id.stopwatch_stop_tv_btn:
                ClockReporter.reportEventMessage(getActivity(), 56, "");
                stopBtnAction();
                return;
            case R.id.stopwatch_meter_times_layout:
                ClockReporter.reportEventMessage(getActivity(), 55, "");
                meterTimesBtnAction();
                return;
            default:
                return;
        }
    }

    private void addListAnimation(long nowStr, String lapStr, int lap_count) {
        setListFirstItemAnim(nowStr, lapStr, lap_count);
        setListLastItemAnim(this.mLastitemAnim);
        Animation b = new SpeedDownAnimation();
        b.setInterpolator(this.mInterpolatorAD);
        b.setDuration(400);
        LayoutAnimationController controller = new LayoutAnimationController(b);
        controller.setDelay(0.0f);
        this.mLapsList.setLayoutAnimation(controller);
    }

    public void setListLastItemAnim(final RelativeLayout lastItemValue) {
        final int position = this.mLapsList.getLastVisiblePosition();
        if (position > -1) {
            if (!(this.mLapsList.getFirstVisiblePosition() == 0 && this.mLapsList.getCount() - 2 == position) && this.mLapsList.getChildAt(position).getBottom() >= this.mLapsList.getHeight()) {
                View item_view = this.mLapsAdapter.getView(0, null, this.mLapsList);
                item_view.measure(0, 0);
                final int mHeight = item_view.getMeasuredHeight();
                float y = this.mLapsList.getY() + ((float) (position * mHeight));
                AnimationSet smoothAnim = new AnimationSet(true);
                TranslateAnimation trans = new TranslateAnimation(0.0f, 0.0f, y, ((float) mHeight) + y);
                trans.setInterpolator(this.mInterpolatorAD);
                trans.setStartOffset(0);
                trans.setDuration(300);
                smoothAnim.addAnimation(trans);
                smoothAnim.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                        Lap lap = (Lap) StopWatchPage.this.mLapsAdapter.getItem(position + 1);
                        StopWatchPage.this.setlastItemAnimationText(StopWatchPage.this.getResources().getString(R.string.listname_stopwatch_name, new Object[]{Integer.valueOf((StopWatchPage.this.mLaps.size() - position) - 1)}), StopWatchPage.this.getResources().getString(R.string.list_stopwatch_Interval, new Object[]{Stopwatches.getTimeText(lap.mLapTime, false)}), lap.mTotalTime);
                        lastItemValue.getLayoutParams().height = mHeight;
                        lastItemValue.setVisibility(0);
                    }

                    public void onAnimationEnd(Animation animation) {
                        lastItemValue.setVisibility(4);
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                lastItemValue.setAnimation(smoothAnim);
            }
        }
    }

    public void setlastItemAnimationText(CharSequence num, CharSequence time, long total) {
        if (this.mLastViewAccTimeAnim == null) {
            this.mLastViewAccTimeAnim = (TextView) this.mSWLayout.findViewById(R.id.last_lap_number);
        }
        if (this.mLastViewIntervalTimeAnim == null) {
            this.mLastViewIntervalTimeAnim = (TextView) this.mSWLayout.findViewById(R.id.last_lap_time);
        }
        if (this.mLastViewLapNumAnim == null) {
            this.mLastViewLapNumAnim = (TextView) this.mSWLayout.findViewById(R.id.last_lap_total);
        }
        this.mLastViewAccTimeAnim.setText(num);
        this.mLastViewIntervalTimeAnim.setText(time);
        this.mLastViewLapNumAnim.setText(Stopwatches.getTimeText(total, false));
        if (total >= 3600000 || this.isTimeOverHour) {
            this.mLastViewLapNumAnim.setTextSize(1, 22.0f);
        } else {
            this.mLastViewLapNumAnim.setTextSize(1, WorldAnalogClock.DEGREE_ONE_HOUR);
        }
    }

    private void setListFirstItemAnim(long number, String interval, int moment) {
        float from;
        float to;
        HwLog.i("StopWatchPage", "number" + number + "|interval" + interval + "|moment" + moment);
        AnimationSet s = new AnimationSet(true);
        s.setFillBefore(true);
        View item_view = this.mLapsAdapter.getView(0, null, this.mLapsList);
        item_view.measure(0, 0);
        view_height = item_view.getMeasuredHeight();
        if (view_height != 0) {
            move_height = view_height;
        }
        if (list_y != 0.0f) {
            from = list_y - (((float) move_height) * 0.5f);
            to = list_y;
        } else {
            from = this.mLapsList.getY() - (((float) move_height) * 0.5f);
            to = this.mLapsList.getY();
        }
        FirstSpeedDownAnimation tta = new FirstSpeedDownAnimation(from, to);
        tta.setTarget(this.mListFirstItemAnim);
        tta.setInterpolator(new LinearInterpolator());
        tta.setStartOffset(0);
        tta.setDuration(200);
        s.addAnimation(tta);
        final AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
        aa.setInterpolator(this.mInterpolatorTM);
        aa.setStartOffset(100);
        aa.setDuration(300);
        s.setStartOffset(0);
        s.setDuration(400);
        final int i = moment;
        final String str = interval;
        final long j = number;
        s.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                StopWatchPage.this.mListFirstItemAnim.setVisibility(0);
                View item = StopWatchPage.this.mLapsAdapter.getView(0, null, StopWatchPage.this.mLapsList);
                item.measure(0, 0);
                StopWatchPage.this.mListFirstItemAnim.getLayoutParams().height = item.getMeasuredHeight();
                StopWatchPage.this.mHeadViewAccTimeAnim.setText(StopWatchPage.this.getResources().getString(R.string.listname_stopwatch_name, new Object[]{Integer.valueOf(i)}));
                StopWatchPage.this.mHeadViewIntervalTimeAnim.setText(StopWatchPage.this.getResources().getString(R.string.list_stopwatch_Interval, new Object[]{str}));
                StopWatchPage.this.mHeadViewLapNumAnim.setText(Stopwatches.getTimeText(j, false));
                if (j >= 3600000) {
                    StopWatchPage.this.mHeadViewLapNumAnim.setTextSize(1, 22.0f);
                } else {
                    StopWatchPage.this.mHeadViewLapNumAnim.setTextSize(1, WorldAnalogClock.DEGREE_ONE_HOUR);
                }
                StopWatchPage.this.mHeadViewLapNumAnim.setAnimation(aa);
                StopWatchPage.this.mHeadViewIntervalTimeAnim.setAnimation(aa);
                StopWatchPage.this.mHeadViewAccTimeAnim.setAnimation(aa);
                StopWatchPage.this.mHeadViewDivider.setAnimation(aa);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                StopWatchPage.this.mListFirstItemAnim.setVisibility(4);
            }
        });
        if (this.mListFirstItemAnim != null) {
            this.mListFirstItemAnim.setAnimation(s);
        } else {
            HwLog.i("StopWatchPage", "StopWatchPage ListFirstItemAnim is null");
        }
    }

    public void onClick(View v) {
        handleUpEvent(v);
    }
}
