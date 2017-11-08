package com.android.deskclock.worldclock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.NameType;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.alarmclock.WorldAnalogClock;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.ClockFragment;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.R;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.ViewHolder$WorldClockViewHolder;
import com.android.deskclock.alarmclock.SettingsActivity;
import com.android.deskclock.worldclock.City.LocationColumns;
import com.android.deskclock.worldclock.WorldAnaimationListView.CustomTranslateAnimation;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.FormatTime;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WorldClockPage extends ClockFragment implements OnItemClickListener, OnItemLongClickListener {
    private static boolean isUpdateUI = false;
    private static boolean mInvalidateViewsBecauseOfCheck = false;
    private static int mShowMode;
    private Interpolator m20_90Interpolator;
    private Interpolator m40_80Interpolator;
    private WorldAnalogClock mAnalogClock;
    private AnimationSet mAnalogClockAnim;
    private int mAnalogHeight;
    private int mAnalogLytHeight;
    private ValueAnimator mAnimator;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private RelativeLayout mCityListLyt;
    private Cursor mCursor;
    private TextView mDateLabel;
    private RelativeLayout mDateTimeZoneLyt;
    private AnimationSet mDigitClockAnim;
    private int mDigitHeight;
    private int mDigitLytHeight;
    private DigitalClock mDigitalClock;
    private TextView mFullTime;
    private Handler mHandler = new Handler();
    private ViewGroup mHeader;
    private LinearLayout mHeaderLyt;
    private boolean mHeaderShowing = true;
    private int mHeaderViewHeight;
    private boolean mIsDialAnimRuning = false;
    private boolean mIsListAnimRuning = false;
    private ListView mListView;
    private LocationAdapter mLocationAdapter;
    private LinearLayout mLocationView;
    private boolean mNeedUpperAnim = true;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            WorldClockPage.this.mHandler.postDelayed(this, 200);
            if (WorldClockPage.this.mFullTime != null) {
                WorldClockPage.this.updateFormatTimeView(WorldClockPage.this.mFullTime, false);
            }
        }
    };
    private TextView mTimeZoneLabel;
    private int mTouchSlop;
    private WorldContentObserver mWorldContentObserver = new WorldContentObserver(null);

    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.iRelease("worldclock", "onQueryComplete:" + ((Integer) cookie).intValue());
            Activity activity = WorldClockPage.this.getActivity();
            if (activity != null) {
                switch (token) {
                    case 20001:
                        if (cursor != null && !cursor.isClosed()) {
                            int countNum = cursor.getCount();
                            WorldClockPage.this.mCursor = cursor;
                            ClockReporter.reportEventContainMessage(WorldClockPage.this.getActivity(), 63, "CITY_COUNT", countNum);
                            WorldClockPage.this.updateUI(cursor, activity, countNum);
                            break;
                        }
                        Log.w("WorldClockPage", "the world cursor has been closed.");
                        WorldClockPage.this.mCursor = null;
                        return;
                }
            }
        }
    }

    public static class HwAnimationLinstener implements AnimationListener {
        public View mHidenView;
        public View mVisiableView;

        public HwAnimationLinstener(View analogClock, View digitalClock) {
            this.mVisiableView = digitalClock;
            this.mHidenView = analogClock;
        }

        public void onAnimationStart(Animation animation) {
            this.mVisiableView.setVisibility(0);
        }

        public void onAnimationEnd(Animation animation) {
            this.mHidenView.setVisibility(4);
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private static class ListRecyclerListener implements RecyclerListener {
        private ListRecyclerListener() {
        }

        public void onMovedToScrapHeap(View view) {
            view.setTranslationY(0.0f);
        }
    }

    private static class LocationAdapter extends CursorAdapter {
        private LayoutInflater mFactory;

        public LocationAdapter(Context context, Cursor c) {
            super(context, c);
            this.mContext = context;
            this.mFactory = LayoutInflater.from(context);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor == null || cursor.isClosed()) {
                Log.w("WorldClockPage", "the cursor is null or have close.");
                return;
            }
            City city = new City(context, cursor);
            ViewHolder$WorldClockViewHolder viewHolder = (ViewHolder$WorldClockViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = createViewHolder(view, city);
                view.setTag(viewHolder);
            }
            createCityItem(view, viewHolder, city);
            if (WorldClockPage.getmInvalidateViewsReason()) {
                checkItemMode(viewHolder);
            } else {
                initItemMode(viewHolder);
            }
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return this.mFactory.inflate(R.layout.world_clock_list_item, parent, false);
        }

        private ViewHolder$WorldClockViewHolder createViewHolder(View view, City city) {
            ViewHolder$WorldClockViewHolder viewHolder = new ViewHolder$WorldClockViewHolder();
            viewHolder.mCityName = (TextView) view.findViewById(R.id.city_label);
            viewHolder.mDigitalClock = (DigitalClock) view.findViewById(R.id.digital_clock);
            viewHolder.mAnalogClock = (WorldAnalogClock) view.findViewById(R.id.analog_clock_anim);
            viewHolder.mGmtDValue = (ClockDValueTextView) view.findViewById(R.id.gmt_d_value);
            return viewHolder;
        }

        private void createCityItem(View view, ViewHolder$WorldClockViewHolder viewHolder, City city) {
            if (city.sortId == 0) {
                Drawable homeIcon = this.mContext.getResources().getDrawable(R.drawable.ic_clock_home);
                homeIcon.setBounds(2, 0, homeIcon.getIntrinsicWidth() + 2, homeIcon.getIntrinsicHeight());
                viewHolder.mCityName.setCompoundDrawablesRelative(null, null, homeIcon, null);
            } else {
                viewHolder.mCityName.setCompoundDrawablesRelative(null, null, null, null);
            }
            viewHolder.mCityName.setText(city.displayName);
            viewHolder.mDigitalClock.setTimeZone(city.timeZone);
            viewHolder.mAnalogClock.setmTimeZone(city.timeZone);
            viewHolder.mGmtDValue.setmTimeZone(city.timeZone);
        }

        private void initItemMode(ViewHolder$WorldClockViewHolder viewHolder) {
            if (WorldClockPage.getmShowMode() == 0) {
                viewHolder.mAnalogClock.setVisibility(8);
                viewHolder.mDigitalClock.setVisibility(0);
                return;
            }
            viewHolder.mAnalogClock.setVisibility(0);
            viewHolder.mDigitalClock.setVisibility(4);
        }

        private void checkItemMode(ViewHolder$WorldClockViewHolder viewHolder) {
            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(250);
            AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation1.setDuration(350);
            animation1.setStartOffset(50);
            animation.setAnimationListener(new ListViewAnimationListener(viewHolder));
            if (WorldClockPage.getmShowMode() == 0) {
                viewHolder.mAnalogClock.startAnimation(animation);
                viewHolder.mDigitalClock.startAnimation(animation1);
                return;
            }
            viewHolder.mDigitalClock.startAnimation(animation);
            viewHolder.mAnalogClock.startAnimation(animation1);
        }
    }

    public class WorldContentObserver extends ContentObserver {
        public WorldContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            WorldClockPage.this.startquery();
        }
    }

    public static int getmShowMode() {
        return mShowMode;
    }

    public static void setmShowMode(int mShowMode) {
        mShowMode = mShowMode;
    }

    public static boolean getmInvalidateViewsReason() {
        return mInvalidateViewsBecauseOfCheck;
    }

    public static void setmInvalidateViewsReason(boolean mInvalidateViewsBecauseOfCheck) {
        mInvalidateViewsBecauseOfCheck = mInvalidateViewsBecauseOfCheck;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WorldClockPage", "onCreate");
        Activity activity = getActivity();
        if (activity != null) {
            this.mBackgroundQueryHandler = new BackgroundQueryHandler(activity.getContentResolver());
            startquery();
            activity.getContentResolver().registerContentObserver(LocationColumns.CONTENT_URI, true, this.mWorldContentObserver);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLocationView = (LinearLayout) inflater.inflate(R.layout.location_list, container, false);
        this.mListView = (ListView) this.mLocationView.findViewById(R.id.worldclock_list);
        this.mListView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
                if (!Utils.isLandScreen(WorldClockPage.this.getActivity())) {
                    if (arg0.getFirstVisiblePosition() == 0) {
                        arg0.setVerticalScrollBarEnabled(false);
                    } else {
                        arg0.setVerticalScrollBarEnabled(true);
                    }
                }
            }

            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
                if (!Utils.isLandScreen(WorldClockPage.this.getActivity())) {
                    if (arg1 == 0) {
                        arg0.setVerticalScrollBarEnabled(false);
                    } else {
                        arg0.setVerticalScrollBarEnabled(true);
                    }
                }
            }
        });
        if (Utils.isLandScreen(getActivity())) {
            this.mHeaderLyt = (LinearLayout) this.mLocationView.findViewById(R.id.time_show);
            this.mDateTimeZoneLyt = (RelativeLayout) this.mLocationView.findViewById(R.id.cur_timezone_date);
            this.mDateLabel = (TextView) this.mLocationView.findViewById(R.id.cur_date_time);
            this.mTimeZoneLabel = (TextView) this.mLocationView.findViewById(R.id.cur_timezone);
            this.mFullTime = (TextView) this.mLocationView.findViewById(R.id.digital_full_time);
            this.mAnalogClock = (WorldAnalogClock) this.mLocationView.findViewById(R.id.analog_clock);
            this.mDigitalClock = (DigitalClock) this.mLocationView.findViewById(R.id.digital_clock);
            this.mCityListLyt = (RelativeLayout) this.mLocationView.findViewById(R.id.city_menu_layout);
        } else {
            this.mHeader = (ViewGroup) inflater.inflate(R.layout.world_clock_head_item, this.mListView, false);
            this.mHeaderLyt = (LinearLayout) this.mHeader.findViewById(R.id.time_show);
            this.mDateTimeZoneLyt = (RelativeLayout) this.mHeader.findViewById(R.id.cur_timezone_date);
            this.mDateLabel = (TextView) this.mHeader.findViewById(R.id.cur_date_time);
            this.mTimeZoneLabel = (TextView) this.mHeader.findViewById(R.id.cur_timezone);
            this.mFullTime = (TextView) this.mHeader.findViewById(R.id.digital_full_time);
            this.mAnalogClock = (WorldAnalogClock) this.mHeader.findViewById(R.id.analog_clock);
            this.mDigitalClock = (DigitalClock) this.mHeader.findViewById(R.id.digital_clock);
            this.mListView.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.worldclock_footview, this.mListView, false), null, false);
        }
        this.m40_80Interpolator = AnimationUtils.loadInterpolator(getActivity(), 17563661);
        this.m20_90Interpolator = new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f);
        if (Utils.isLandScreen(getActivity())) {
            this.mHeaderLyt.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    WorldClockPage.this.onClickHeader();
                }
            });
            this.mHeaderLyt.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Context context = WorldClockPage.this.getActivity();
                    if (WorldClockPage.this.mListView.getCount() == 0 || (WorldClockPage.this.hasHomeClock(context) && 1 == WorldClockPage.this.mListView.getCount())) {
                        return false;
                    }
                    ClockReporter.reportEventMessage(context, 46, "");
                    WorldClockPage.this.startActivityForResult(new Intent(context, SortCityActivity.class), 1);
                    return true;
                }
            });
        }
        showLocalTime();
        return this.mLocationView;
    }

    private boolean hasHomeClock(Context context) {
        SharedPreferences mPreferences = Utils.getSharedPreferences(context, "setting_activity", 0);
        String tz = mPreferences.getString("home_city_timezone", "");
        String homeId = mPreferences.getString("home_time_index", "");
        if (!mPreferences.getBoolean("ISCHECKED", false) || TextUtils.isEmpty(homeId) || TextUtils.isEmpty(tz)) {
            return false;
        }
        return true;
    }

    private void showLocalTime() {
        setmShowMode(Utils.getSharedPreferences(getActivity(), "setting_activity", 0).getInt("clock_style", 1));
        initMode();
    }

    private void initMode() {
        invalidateHeaderHeight();
        setHeaderHeight();
        if (getmShowMode() == 0) {
            this.mAnalogClock.setVisibility(8);
            this.mDigitalClock.setVisibility(0);
            return;
        }
        this.mAnalogClock.setVisibility(0);
        this.mDigitalClock.setVisibility(4);
    }

    private void invalidateHeaderHeight() {
        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        if (!Utils.isLandScreen(getActivity())) {
            this.mDigitLytHeight = (screenHeight * 277) / 690;
            this.mAnalogLytHeight = (screenHeight * 360) / 690;
        }
        this.mDigitHeight = this.mDigitalClock.getHeight();
        this.mAnalogHeight = this.mAnalogClock.getMeasuredHeight();
    }

    private void setHeaderHeight() {
        if (Utils.isLandScreen(getActivity())) {
            this.mDateTimeZoneLyt.setTranslationY(0.0f);
            this.mDigitalClock.setTranslationY(0.0f);
            this.mHeaderLyt.invalidate();
            return;
        }
        if (getmShowMode() == 0) {
            this.mHeaderLyt.getLayoutParams().height = this.mDigitLytHeight;
            this.mHeaderViewHeight = this.mDigitLytHeight;
        } else {
            this.mHeaderLyt.getLayoutParams().height = this.mAnalogLytHeight;
            this.mHeaderViewHeight = this.mAnalogLytHeight;
        }
    }

    public void onClickHeader() {
        if (!this.mIsDialAnimRuning && !this.mIsListAnimRuning) {
            if (this.mAnalogHeight == 0) {
                invalidateHeaderHeight();
            }
            setmShowMode(getmShowMode() ^ 1);
            Editor ed = Utils.getSharedPreferences(getActivity(), "setting_activity", 0).edit();
            ed.putInt("clock_style", getmShowMode());
            ed.commit();
            if (this.mHeader == null || this.mHeader.getHeight() != 0) {
                showDialAnim();
            } else if (getmShowMode() == 0) {
                this.mHeaderLyt.getLayoutParams().height = this.mDigitLytHeight;
                this.mHeaderViewHeight = this.mDigitLytHeight;
                this.mHeader.setPadding(this.mHeader.getPaddingLeft(), -this.mDigitLytHeight, this.mHeader.getPaddingRight(), this.mHeader.getPaddingBottom());
                this.mAnalogClock.setVisibility(8);
                this.mDigitalClock.setVisibility(0);
                this.mRunnable.run();
            } else {
                this.mHeaderLyt.getLayoutParams().height = this.mAnalogLytHeight;
                this.mHeaderViewHeight = this.mAnalogLytHeight;
                this.mHeader.setPadding(this.mHeader.getPaddingLeft(), -this.mAnalogLytHeight, this.mHeader.getPaddingRight(), this.mHeader.getPaddingBottom());
                this.mAnalogClock.setVisibility(0);
                this.mDigitalClock.setVisibility(4);
                this.mHandler.removeCallbacks(this.mRunnable);
            }
            setmInvalidateViewsReason(true);
            ClockReporter.reportEventContainMessage(getActivity(), 91, "show_mode:" + getmShowMode(), 0);
            if (Utils.isLandScreen(getActivity())) {
                setmInvalidateViewsReason(false);
                if (getmShowMode() == 0) {
                    updateListView(true);
                } else {
                    updateListView(false);
                }
                return;
            }
            if (!(this.mHeader == null || this.mHeader.getHeight() == 0)) {
                showListAnim();
            }
            this.mListView.invalidateViews();
        }
    }

    private void addAnimalEffect(View analogClock, View digitalClock) {
        if (analogClock != null && digitalClock != null) {
            AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.setDuration(250);
            AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation1.setDuration(350);
            animation1.setStartOffset(50);
            animation.setAnimationListener(new HwAnimationLinstener(analogClock, digitalClock));
            analogClock.startAnimation(animation);
            digitalClock.startAnimation(animation1);
        }
    }

    public void updateListView(boolean isDigit) {
        int i = 0;
        while (i < this.mListView.getCount()) {
            View view = this.mListView.getChildAt(i);
            if (view != null) {
                View analogClock = view.findViewById(R.id.analog_clock_anim);
                View digitalClock = view.findViewById(R.id.digital_clock);
                if (isDigit) {
                    addAnimalEffect(analogClock, digitalClock);
                } else {
                    addAnimalEffect(digitalClock, analogClock);
                }
                i++;
            } else {
                return;
            }
        }
    }

    private void showDialAnim() {
        if (getmShowMode() == 1) {
            if (!Utils.isLandScreen(getActivity())) {
                this.mHeaderViewHeight = this.mAnalogLytHeight;
                this.mHeaderLyt.getLayoutParams().height = this.mAnalogLytHeight;
            }
            this.mAnalogClock.setVisibility(4);
            this.mAnalogHeight = this.mAnalogClock.getMeasuredHeight();
            showBig();
            this.mHandler.removeCallbacks(this.mRunnable);
            return;
        }
        showLittle();
        this.mHandler.removeCallbacks(this.mRunnable);
        this.mRunnable.run();
    }

    private void showListAnim() {
        Animation layoutAnimation;
        if (getmShowMode() == 1) {
            layoutAnimation = new CustomTranslateAnimation(0.0f, 0.0f, (float) (-(this.mAnalogLytHeight - this.mDigitLytHeight)), 0.0f, 0);
            this.mListView.setLayoutAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    WorldClockPage.this.mIsListAnimRuning = true;
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    WorldClockPage.this.mIsListAnimRuning = false;
                }
            });
        } else {
            this.mListView.setPadding(this.mListView.getPaddingLeft(), this.mListView.getPaddingTop(), this.mListView.getPaddingRight(), -this.mHeaderViewHeight);
            layoutAnimation = new CustomTranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-(this.mAnalogLytHeight - this.mDigitLytHeight)), this.mAnalogLytHeight - this.mDigitLytHeight);
            this.mListView.setRecyclerListener(new ListRecyclerListener());
            this.mListView.setLayoutAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    WorldClockPage.this.mIsListAnimRuning = true;
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    if (WorldClockPage.this.mAnalogClock != null && WorldClockPage.this.mDateTimeZoneLyt != null && WorldClockPage.this.mDigitalClock != null && WorldClockPage.this.mHeaderLyt != null && WorldClockPage.this.mListView != null) {
                        WorldClockPage.this.mIsListAnimRuning = false;
                        WorldClockPage.this.setupListViewTranslate(0);
                        WorldClockPage.this.mAnalogClock.setVisibility(8);
                        WorldClockPage.this.mDateTimeZoneLyt.setTranslationY(0.0f);
                        WorldClockPage.this.mDigitalClock.setTranslationY(0.0f);
                        WorldClockPage.this.mHeaderViewHeight = WorldClockPage.this.mDigitLytHeight;
                        WorldClockPage.this.mHeaderLyt.getLayoutParams().height = WorldClockPage.this.mDigitLytHeight;
                        WorldClockPage.this.mListView.setPadding(WorldClockPage.this.mListView.getPaddingLeft(), WorldClockPage.this.mListView.getPaddingTop(), WorldClockPage.this.mListView.getPaddingRight(), 0);
                    }
                }
            });
        }
        if (this.mHeader.getHeight() != 0) {
            layoutAnimation.setDuration(400);
            layoutAnimation.setInterpolator(this.m20_90Interpolator);
            LayoutAnimationController controller = new LayoutAnimationController(layoutAnimation);
            controller.setDelay(0.0f);
            this.mListView.setLayoutAnimation(controller);
        }
    }

    private void setupListViewTranslate(int tY) {
        int i = 1;
        while (this.mListView != null && i < this.mListView.getChildCount()) {
            View child = this.mListView.getChildAt(i);
            if (child != null) {
                child.setTranslationY((float) tY);
            }
            i++;
        }
    }

    private void showBig() {
        int littleTranslateHeight;
        this.mAnalogClockAnim = new AnimationSet(false);
        Animation bigShow = new AlphaAnimation(0.0f, 1.0f);
        ScaleAnimation sa = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, 1, 0.5f, 1, 0.5f);
        sa.setInterpolator(this.m20_90Interpolator);
        this.mAnalogClockAnim.addAnimation(bigShow);
        this.mAnalogClockAnim.addAnimation(sa);
        this.mAnalogClockAnim.setDuration(400);
        this.mAnalogClock.startAnimation(this.mAnalogClockAnim);
        ScaleAnimation sa1 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, 1, 0.5f, 1, 0.5f);
        sa1.setInterpolator(this.m20_90Interpolator);
        sa1.setDuration(400);
        Animation littleHide = new AlphaAnimation(1.0f, 0.0f);
        littleHide.setDuration(150);
        littleHide.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                if (Utils.isLandScreen(WorldClockPage.this.getActivity())) {
                    WorldClockPage.this.mDigitalClock.setTranslationY(((float) (WorldClockPage.this.mAnalogHeight - WorldClockPage.this.mDigitHeight)) / 2.0f);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                WorldClockPage.this.mDigitalClock.setVisibility(4);
                WorldClockPage.this.mAnalogClock.setVisibility(0);
            }
        });
        if (Utils.isLandScreen(getActivity())) {
            littleTranslateHeight = (this.mAnalogHeight - this.mDigitHeight) / 2;
        } else {
            littleTranslateHeight = (this.mAnalogLytHeight - this.mDigitLytHeight) / 2;
        }
        Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-littleTranslateHeight), (float) (-littleTranslateHeight));
        translateAnimation.setDuration(400);
        this.mDigitClockAnim = new AnimationSet(true);
        this.mDigitClockAnim.addAnimation(sa1);
        this.mDigitClockAnim.addAnimation(littleHide);
        this.mDigitClockAnim.addAnimation(translateAnimation);
        this.mDigitalClock.startAnimation(this.mDigitClockAnim);
        int height = ((this.mAnalogHeight - this.mDigitHeight) / 2) + ((this.mAnalogLytHeight - this.mDigitLytHeight) / 2);
        if (this.mAnalogHeight == 0) {
            height = ((getActivity().getResources().getDrawable(R.drawable.img_clock_worldclock_dial_light).getIntrinsicWidth() - this.mDigitHeight) / 2) + ((this.mAnalogLytHeight - this.mDigitLytHeight) / 2);
        }
        Animation bottomMove = new TranslateAnimation(0.0f, 0.0f, (float) (-height), 0.0f);
        bottomMove.setInterpolator(this.m20_90Interpolator);
        bottomMove.setDuration(400);
        bottomMove.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation arg0) {
                WorldClockPage.this.mIsDialAnimRuning = true;
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationEnd(Animation arg0) {
                WorldClockPage.this.mIsDialAnimRuning = false;
            }
        });
        this.mDateTimeZoneLyt.startAnimation(bottomMove);
    }

    private void showLittle() {
        this.mAnalogClockAnim = new AnimationSet(true);
        ScaleAnimation sa1 = new ScaleAnimation(1.0f, 0.8f, 1.0f, 0.8f, 1, 0.5f, 1, 0.5f);
        sa1.setInterpolator(this.m20_90Interpolator);
        sa1.setDuration(400);
        Animation bigHide = new AlphaAnimation(1.0f, 0.0f);
        bigHide.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                WorldClockPage.this.mAnalogClock.setVisibility(4);
            }
        });
        bigHide.setDuration(150);
        this.mAnalogClockAnim.addAnimation(bigHide);
        this.mAnalogClockAnim.addAnimation(sa1);
        this.mAnalogClock.startAnimation(this.mAnalogClockAnim);
        final int littleTranslateHeight = (((this.mAnalogHeight - this.mDigitHeight) / 2) + ((this.mAnalogLytHeight - this.mAnalogHeight) / 2)) - ((this.mDigitLytHeight - this.mDigitHeight) / 2);
        Animation littleShow = new AlphaAnimation(0.0f, 1.0f);
        ScaleAnimation sa = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f, 1, 0.5f, 1, 0.5f);
        sa.setInterpolator(this.m20_90Interpolator);
        this.mDigitClockAnim = new AnimationSet(false);
        this.mDigitClockAnim.addAnimation(sa);
        this.mDigitClockAnim.addAnimation(littleShow);
        this.mDigitClockAnim.setDuration(400);
        littleShow.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                WorldClockPage.this.mDigitalClock.setVisibility(0);
                WorldClockPage.this.mDigitalClock.setTranslationY((float) (-littleTranslateHeight));
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
            }
        });
        littleShow.setDuration(400);
        this.mDigitalClock.startAnimation(this.mDigitClockAnim);
        final int height = ((this.mAnalogHeight - this.mDigitHeight) / 2) + ((this.mAnalogLytHeight - this.mDigitLytHeight) / 2);
        Animation bottomMove = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-height));
        bottomMove.setInterpolator(this.m20_90Interpolator);
        bottomMove.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                WorldClockPage.this.mIsDialAnimRuning = true;
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                WorldClockPage.this.mIsDialAnimRuning = false;
                RelativeLayout -get3 = WorldClockPage.this.mDateTimeZoneLyt;
                final int i = height;
                -get3.post(new Runnable() {
                    public void run() {
                        if (Utils.isLandScreen(WorldClockPage.this.getActivity())) {
                            WorldClockPage.this.mAnalogClock.setVisibility(8);
                            WorldClockPage.this.mDateTimeZoneLyt.setTranslationY(0.0f);
                            WorldClockPage.this.mDigitalClock.setTranslationY(0.0f);
                        } else if (WorldClockPage.this.mAnalogClock.getVisibility() != 8) {
                            WorldClockPage.this.mDateTimeZoneLyt.setTranslationY((float) (-i));
                        }
                    }
                });
            }
        });
        bottomMove.setDuration(400);
        this.mDateTimeZoneLyt.startAnimation(bottomMove);
    }

    private void initLocaleTime() {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        boolean hasDST = tz.inDaylightTime(calendar.getTime());
        if (this.mTimeZoneLabel != null) {
            NameType nameType;
            TimeZoneNames names = TimeZoneNames.getInstance(Locale.getDefault());
            if (hasDST) {
                nameType = NameType.LONG_DAYLIGHT;
            } else {
                nameType = NameType.LONG_STANDARD;
            }
            String timeLabel = names.getDisplayName(tz.getID(), nameType, calendar.getTimeInMillis());
            if (timeLabel == null) {
                timeLabel = tz.getDisplayName(hasDST, 1);
            }
            if (timeLabel == null) {
                HwLog.e("WorldClockPage", "timeLabel is null");
                return;
            } else {
                this.mTimeZoneLabel.setText(timeLabel.substring(0, 1).toUpperCase(Locale.getDefault()) + timeLabel.substring(1));
            }
        }
        if (this.mDateLabel != null) {
            String text = new FormatTime(getActivity(), calendar).getTimeString(9);
            if (hasDST) {
                String dst = getActivity().getResources().getString(R.string.world_digital_dst_tv);
                text = getActivity().getResources().getString(R.string.date_DST, new Object[]{text, dst});
            }
            this.mDateLabel.setText(text);
        }
    }

    public void onDestroyView() {
        Log.d("WorldClockPage", "onDestroyView");
        if (this.mLocationAdapter != null) {
            this.mLocationAdapter.changeCursor(null);
        }
        this.mLocationAdapter = null;
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
        super.onDestroyView();
    }

    public void onDestroy() {
        if (this.mBackgroundQueryHandler != null) {
            this.mBackgroundQueryHandler.cancelOperation(20001);
            this.mBackgroundQueryHandler.removeCallbacksAndMessages(null);
        }
        Log.d("WorldClockPage", "onDestroy");
        try {
            Activity activity = getActivity();
            if (activity != null) {
                activity.getContentResolver().unregisterContentObserver(this.mWorldContentObserver);
            }
        } catch (Exception e) {
            Log.e("WorldClockPage", "onDestroy : Exception1 = " + e.getMessage());
        }
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
        try {
            super.onDestroy();
        } catch (Exception e2) {
            Log.e("WorldClockPage", "onDestroy : Exception2 = " + e2.getMessage());
        }
        setObject2Null();
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        this.mHandler.removeCallbacks(this.mRunnable);
    }

    public void onStop() {
        super.onStop();
    }

    public boolean clockModeChange() {
        return Utils.getSharedPreferences(getActivity(), "setting_activity", 0).getInt("clock_style", 1) != getmShowMode();
    }

    public void onResume() {
        super.onResume();
        initLocaleTime();
        Activity activity = getActivity();
        if (!((!isUpdateUI && !clockModeChange()) || activity == null || this.mCursor == null || this.mCursor.isClosed())) {
            updateUI(this.mCursor, activity, this.mCursor.getCount());
        }
        setIsUpdateUI(false);
        if (Config.clockTabIndex() == 1) {
            if (!Utils.isLandScreen(getActivity())) {
                setHasOptionsMenu(true);
            }
            if (getmShowMode() == 0) {
                this.mHandler.removeCallbacks(this.mRunnable);
                this.mRunnable.run();
                return;
            }
            this.mHandler.removeCallbacks(this.mRunnable);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getActivity() != null) {
            if (requestCode == 0) {
                onAddCityResult(requestCode, resultCode, data);
            } else if (requestCode == 1) {
                if (resultCode == -1) {
                    setIsUpdateUI(true);
                }
                int mode = Utils.getSharedPreferences(getActivity(), "setting_activity", 0).getInt("clock_style", 1);
                if (mode != getmShowMode()) {
                    setIsUpdateUI(true);
                    if (1 == mode && this.mHeader != null && this.mHeader.getHeight() == 0) {
                        this.mHeader.setPadding(this.mHeader.getPaddingLeft(), -this.mAnalogLytHeight, this.mHeader.getPaddingRight(), this.mHeader.getPaddingBottom());
                    }
                }
            }
        }
    }

    private boolean isMaxCity(String city) {
        Cursor cursor = getActivity().getContentResolver().query(LocationColumns.CONTENT_URI, new String[]{"_id"}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() >= 24) {
                ToastMaster.showToast(DeskClockApplication.getDeskClockApplication(), getString(R.string.city_full_Toast, new Object[]{city}), 0);
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private void updateDateSource(Bundle bundle) {
        if (bundle != null) {
            String tz = bundle.getString("id", "");
            String displayName = bundle.getString("name", "");
            String index = bundle.getString("unique_id", "");
            if (TextUtils.isEmpty(tz) || TextUtils.isEmpty(displayName) || TextUtils.isEmpty(index)) {
                Log.printf("WorldClockSettingActivity bundle data exception", new Object[0]);
                return;
            }
            Log.printf("WorldClockSettingActivity timezone=%s, displayName=%s, index=%s", tz, displayName, index);
            String cityName = TimeZoneUtils.getCityName(displayName);
            if (!isMaxCity(cityName)) {
                Map<String, String> map = new HashMap();
                map.put(index, cityName);
                TimeZoneUtils.saveTimeZoneMap(getActivity(), map);
                ContentValues values = new ContentValues();
                values.put("city_index", index);
                values.put("timezone", tz);
                values.put("sort_order", Integer.valueOf(9999));
                values.put("homecity", Integer.valueOf(0));
                getActivity().getContentResolver().insert(LocationColumns.CONTENT_URI, values);
            }
        }
    }

    public void onAddCityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 0) {
            updateDateSource(data.getExtras());
        }
    }

    private void setObject2Null() {
        this.mLocationAdapter = null;
        this.mListView = null;
        this.mLocationView = null;
    }

    private void startquery() {
        Log.dRelease("worldclock", "startQuery");
        if (this.mBackgroundQueryHandler != null) {
            this.mBackgroundQueryHandler.startQuery(20001, Integer.valueOf(0), LocationColumns.CONTENT_URI, null, null, null, null);
        }
    }

    private void updateUI(Cursor cursor, Activity activity, int countNum) {
        int mode = Utils.getSharedPreferences(activity, "setting_activity", 0).getInt("clock_style", 1);
        if (mode != getmShowMode()) {
            setmShowMode(mode);
            initMode();
            this.mListView.invalidateViews();
        }
        if (this.mCityListLyt != null) {
            if (countNum == 0) {
                this.mCityListLyt.setVisibility(8);
            } else {
                this.mCityListLyt.setVisibility(0);
            }
        }
        if (countNum == 0 && Utils.isLandScreen(activity)) {
            this.mListView.setVisibility(8);
        } else {
            this.mListView.setVisibility(0);
        }
        if (this.mLocationAdapter == null) {
            this.mLocationAdapter = new LocationAdapter(activity, cursor);
            if (!(Utils.isLandScreen(activity) || this.mHeader == null)) {
                this.mListView.addHeaderView(this.mHeader, null, true);
                this.mListView.setHeaderDividersEnabled(false);
                setOnTouchListener();
            }
            this.mListView.setOnItemClickListener(this);
            this.mListView.setOnItemLongClickListener(this);
            this.mListView.setAdapter(this.mLocationAdapter);
            return;
        }
        this.mLocationAdapter.changeCursor(cursor);
    }

    private void setOnTouchListener() {
        this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mAnimator.setInterpolator(this.m40_80Interpolator);
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (WorldClockPage.this.mHeaderShowing) {
                    WorldClockPage.this.mHeader.setPadding(WorldClockPage.this.mHeader.getPaddingLeft(), (int) (((Float) animation.getAnimatedValue()).floatValue() * ((float) (-WorldClockPage.this.mHeaderViewHeight))), WorldClockPage.this.mHeader.getPaddingRight(), WorldClockPage.this.mHeader.getPaddingBottom());
                    return;
                }
                WorldClockPage.this.mHeader.setPadding(WorldClockPage.this.mHeader.getPaddingLeft(), (int) ((1.0f - ((Float) animation.getAnimatedValue()).floatValue()) * ((float) (-WorldClockPage.this.mHeaderViewHeight))), WorldClockPage.this.mHeader.getPaddingRight(), WorldClockPage.this.mHeader.getPaddingBottom());
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                boolean z = false;
                WorldClockPage worldClockPage = WorldClockPage.this;
                if (WorldClockPage.this.mHeader.getHeight() > 0) {
                    z = true;
                }
                worldClockPage.mHeaderShowing = z;
            }
        });
        this.mAnimator.setDuration(400);
        this.mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        this.mListView.setOnTouchListener(new OnTouchListener() {
            private float mDownPositionY;
            private long mDownTime = -1;
            private float mLastMotionY;

            public boolean onTouch(View v, MotionEvent event) {
                boolean z = true;
                if (event.getAction() == 3) {
                    return false;
                }
                if (this.mDownTime == event.getDownTime()) {
                    return true;
                }
                if (WorldClockPage.this.mAnimator.isRunning()) {
                    this.mDownTime = event.getDownTime();
                    return true;
                }
                switch (event.getAction()) {
                    case 0:
                        WorldClockPage worldClockPage = WorldClockPage.this;
                        if (WorldClockPage.this.mListView.getChildCount() == WorldClockPage.this.mListView.getCount() && WorldClockPage.this.mListView.getChildAt(WorldClockPage.this.mListView.getChildCount() - 1).getBottom() <= WorldClockPage.this.mListView.getHeight()) {
                            z = false;
                        }
                        worldClockPage.mNeedUpperAnim = z;
                        this.mDownPositionY = event.getY();
                        this.mLastMotionY = this.mDownPositionY;
                        break;
                    case 2:
                        boolean topUpperDirection;
                        if ((event.getY() - this.mDownPositionY) * (event.getY() - this.mLastMotionY) <= 0.0f) {
                            this.mDownPositionY = this.mLastMotionY;
                        }
                        this.mLastMotionY = event.getY();
                        float offset = this.mLastMotionY - this.mDownPositionY;
                        if (offset >= 0.0f || WorldClockPage.this.mHeader.getHeight() != WorldClockPage.this.mHeaderViewHeight) {
                            topUpperDirection = false;
                        } else {
                            topUpperDirection = WorldClockPage.this.mNeedUpperAnim;
                        }
                        boolean topDownDirection = (offset <= 0.0f || WorldClockPage.this.mHeader.getHeight() != 0) ? false : WorldClockPage.this.mListView.getFirstVisiblePosition() == 0;
                        if (Math.abs(offset) <= 50.0f) {
                            return true;
                        }
                        if ((offset < ((float) (-WorldClockPage.this.mTouchSlop)) && topUpperDirection) || (offset > ((float) WorldClockPage.this.mTouchSlop) && topDownDirection)) {
                            WorldClockPage.this.mAnimator.start();
                            event.setAction(3);
                            return false;
                        } else if (topUpperDirection || topDownDirection) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
        onClickHeader();
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        return isHandleLongClick();
    }

    private boolean isHandleLongClick() {
        Context context = getActivity();
        if (Utils.isLandScreen(context)) {
            if (hasHomeClock(context) && 1 == this.mListView.getCount()) {
                return false;
            }
        } else if (1 == this.mListView.getCount() - this.mListView.getFooterViewsCount() || (hasHomeClock(context) && 2 == this.mListView.getCount() - this.mListView.getFooterViewsCount())) {
            return false;
        }
        ClockReporter.reportEventMessage(context, 46, "");
        startActivityForResult(new Intent(context, SortCityActivity.class), 1);
        return true;
    }

    public static void setIsUpdateUI(boolean isUpdate) {
        isUpdateUI = isUpdate;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.world_clock_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_city_btn:
                handleAddEvent();
                break;
            case R.id.settings_btn:
                handleSettingsEvent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFragmentResume() {
        super.onFragmentResume();
        setHasOptionsMenu(true);
        if (getmShowMode() == 0) {
            this.mHandler.removeCallbacks(this.mRunnable);
            this.mRunnable.run();
        }
    }

    public void onFragmentPause() {
        super.onFragmentPause();
        setHasOptionsMenu(false);
        this.mHandler.removeCallbacks(this.mRunnable);
    }

    public void handleAddEvent() {
        Context context = getActivity();
        if (AlarmsMainActivity.ismLockedEnter()) {
            context.sendBroadcastAsUser(new Intent("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD"), UserHandle.OWNER);
        }
        ClockReporter.reportEventMessage(context, 15, "");
        Bundle bundle = new Bundle();
        bundle.putInt("request_type", 2);
        bundle.putString("request_description", "HWDESKCLOCK_ADD_CITY");
        bundle.putStringArrayList("excluded_unique_ids", TimeZoneUtils.getCityIndexList(context));
        TimeZoneUtils.startPickZoneActivity(getActivity(), 0, bundle);
    }

    public void handleSettingsEvent() {
        Context context = getActivity();
        ClockReporter.reportEventContainMessage(context, 10, "tab", 2);
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.setAction("action_from_worldclock");
        startActivityForResult(intent, 1);
    }

    private void updateFormatTimeView(TextView view, boolean upperCase) {
        if (getActivity() != null) {
            String text = FormatTime.getFormatTime(getActivity(), Calendar.getInstance());
            if (upperCase) {
                text = text.toUpperCase(Locale.ENGLISH);
            }
            view.setText(text);
        }
    }
}
