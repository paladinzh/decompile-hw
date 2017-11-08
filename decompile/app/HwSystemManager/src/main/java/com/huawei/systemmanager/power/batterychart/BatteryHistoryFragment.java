package com.huawei.systemmanager.power.batterychart;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.os.BatteryStatsHelper;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.power.batterychart.BatterHistoryUtils.UpdateCallBack;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.power.ui.ConsumeDetailAdapter;
import com.huawei.systemmanager.power.ui.ConsumeDetailInfo;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class BatteryHistoryFragment extends Fragment implements UpdateCallBack {
    private static final String ACTION_BATTERY_DETAIL = "com.android.settings.BATTERY_HISTORY_DETAIL";
    private static final double BATTERY_CHART_HEIGHT_RATIO = 0.75d;
    private static final String DRAG_UP_TIME = "drag_up_time";
    private static final String ENTER_DETAIL = "1";
    private static final int FETCH_TOP_CONSUMEAPP = 1;
    private static final int HOUR_PER_DAY = 24;
    private static final int MILLI_SECOND = 1000;
    private static final int MINUTE_PER_HOUR = 60;
    private static final int MSG_CLICK_DIVIDER = 3;
    private static final int MSG_START_CHOOSET_ANIM = 2;
    private static final int PERCENTAGE_RATIO_100 = 100;
    private static final String QUIT_DETAIL = "0";
    private static final int SECOND_PER_MINUTE = 60;
    private static final String TAG = "BatteryHistoryFragment";
    private static final int TWO_DAY = 2;
    private static final long TWO_DAY_IN_MICRO_SECONDS = 172800000;
    private BatteryHistoryOnlyChart chart;
    private int chartAreaRightPadding = 0;
    private int chartLeft = 0;
    private int chartRight = 0;
    private boolean isCanClick = true;
    private ListView lv;
    private Activity mActivity;
    private Context mAppContext;
    private RelativeLayout mBatteryHistoryChooseTimeLayout = null;
    private LinearLayout mBatteryHistoryUpperLayout = null;
    private TextView mBatteryInfo = null;
    private TextView mBattery_des = null;
    private TextView mChoose_time = null;
    private ConsumeDetailAdapter mConsumeDetailAdapter;
    private double mCurrModeLeftTime = 0.0d;
    private ImageView mDetailImg;
    private BatteryDraggingBar mDragBar;
    private TextView mEstimatedTime = null;
    private Handler mHandler = null;
    private boolean mIsDetail = false;
    private boolean mIsFirstShowTopApp = true;
    private boolean mIsSupportOrientation;
    private LinearLayout mLinesLayout = null;
    private LinearLayout mListWrap;
    private LinearLayout mNoAppsLayout = null;
    public Animator mProblemAnima;
    private List<ConsumeDetailInfo> mSoftwareList = Lists.newArrayList();
    private BatteryStats mStats;
    private BatteryStatsHelper mStatsHelper;
    private LinearLayout mTopAppLayout = null;
    private TextView mTopConsume_des = null;

    static class BatteryHistoryAnimatorListener implements AnimatorListener {
        BatteryHistoryAnimatorListener() {
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public static BatteryHistoryFragment newInstance() {
        return new BatteryHistoryFragment();
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mIsSupportOrientation = Utility.isSupportOrientation();
        this.mActivity = getActivity();
        this.mAppContext = this.mActivity.getApplicationContext();
        if (!this.mIsSupportOrientation) {
            this.mActivity.setRequestedOrientation(1);
        }
        this.chartLeft = (int) this.mAppContext.getResources().getDimension(R.dimen.battery_history_chart_left_padding);
        this.chartRight = (int) this.mAppContext.getResources().getDimension(R.dimen.battery_history_chart_right_margin);
        this.chartAreaRightPadding = (int) getResources().getDimension(R.dimen.battery_history_chart_area_right_padding);
        this.mStatsHelper = new BatteryStatsHelper(this.mActivity, true);
        this.mStatsHelper.create((Bundle) null);
        this.mStats = this.mStatsHelper.getStats();
        setHasOptionsMenu(true);
        this.mCurrModeLeftTime = getActivity().getIntent().getDoubleExtra("estimatedTime", 0.0d);
        HwLog.i(TAG, " mCurrModeLeftTime =" + this.mCurrModeLeftTime);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_history_only_chart, null);
        this.chart = (BatteryHistoryOnlyChart) view.findViewById(R.id.battery_history_chart);
        this.chart.setData(this.mStats);
        initComponent();
        initView(view);
        this.mDragBar = (BatteryDraggingBar) view.findViewById(R.id.dragging_bar);
        this.mDragBar.setChartStartTime(this.chart.getChartStartTime());
        this.mDragBar.setChartEndTime(this.chart.getChartEndTime());
        this.mDragBar.setCallBack(this);
        ViewUtils.setVisibility(this.mDragBar, 4);
        this.mChoose_time.setVisibility(4);
        ((BatteryHistoryText) view.findViewById(R.id.batter_line)).setData(this.mStats);
        return view;
    }

    private void initComponent() {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        BatteryHistoryFragment.this.showTopApps(msg.getData().getLong(BatteryHistoryFragment.DRAG_UP_TIME));
                        return;
                    case 2:
                        ViewUtils.setVisibility(BatteryHistoryFragment.this.mChoose_time, 0);
                        BatteryHistoryFragment.this.mChoose_time.startAnimation(AnimationUtils.loadAnimation(BatteryHistoryFragment.this.mActivity, R.anim.power_battery_history_choosetime));
                        return;
                    case 3:
                        BatteryHistoryFragment.this.isCanClick = true;
                        ViewUtils.setEnabled(BatteryHistoryFragment.this.mDetailImg, true);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void showTopApps(long time) {
        this.mSoftwareList.clear();
        List<UidAndPower> list = BatteryStatisticsHelper.queryBatteryStatistics(this.mAppContext, time);
        HwLog.i(TAG, "showTopApps time= " + time);
        if (list.size() > 0) {
            if (!this.mIsFirstShowTopApp) {
                this.mTopAppLayout.setVisibility(0);
                this.mNoAppsLayout.setVisibility(8);
            }
            int topAppLen = 0;
            for (UidAndPower temp : list) {
                if (getTopAppInfoByUid(temp.getUid(), temp.getPower())) {
                    topAppLen++;
                }
            }
            if (this.mSoftwareList.size() > 0) {
                double maxConsumeValue = ((ConsumeDetailInfo) this.mSoftwareList.get(0)).getmPowerValue();
                for (int i = 0; i < this.mSoftwareList.size(); i++) {
                    ((ConsumeDetailInfo) this.mSoftwareList.get(i)).setmPowerLevel((int) ((((ConsumeDetailInfo) this.mSoftwareList.get(i)).getmPowerValue() / maxConsumeValue) * 100.0d));
                }
            }
            this.mTopConsume_des.setText(this.mAppContext.getResources().getQuantityString(R.plurals.power_battery_history_topapp_tip, topAppLen, new Object[]{Integer.valueOf(topAppLen)}));
            this.mConsumeDetailAdapter.swapData(this.mSoftwareList);
            return;
        }
        HwLog.i(TAG, "fetch top consume apps is zero, time = " + time);
        if (!this.mIsFirstShowTopApp) {
            this.mTopAppLayout.setVisibility(8);
            this.mNoAppsLayout.setVisibility(0);
        }
    }

    private void initView(View view) {
        this.mBatteryHistoryUpperLayout = (LinearLayout) view.findViewById(R.id.battery_history_upper_layout);
        int screenWidth = SysCoreUtils.getScreenWidth(this.mAppContext);
        LayoutParams params = (LayoutParams) this.mBatteryHistoryUpperLayout.getLayoutParams();
        params.height = (int) (((((double) screenWidth) * BATTERY_CHART_HEIGHT_RATIO) + ((double) this.mAppContext.getResources().getDimensionPixelSize(R.dimen.battery_history_chart_bottom_padding))) - ((double) this.mAppContext.getResources().getDimensionPixelSize(R.dimen.battery_history_actionBar_height)));
        this.mBatteryHistoryUpperLayout.setLayoutParams(params);
        this.mBatteryInfo = (TextView) view.findViewById(R.id.battery_history_powerinfo_des);
        this.mTopAppLayout = (LinearLayout) view.findViewById(R.id.battery_history_topApp_layout);
        this.mLinesLayout = (LinearLayout) view.findViewById(R.id.battery_history_lines_layout);
        this.mNoAppsLayout = (LinearLayout) view.findViewById(R.id.battery_history_no_apps);
        this.mDetailImg = (ImageView) view.findViewById(R.id.battery_history_detail_imgview);
        this.mChoose_time = (TextView) view.findViewById(R.id.battery_history_choose_time);
        this.mBatteryHistoryChooseTimeLayout = (RelativeLayout) view.findViewById(R.id.battery_history_choose_time_layout);
        this.mBatteryHistoryChooseTimeLayout.getLayoutParams().height = this.mAppContext.getResources().getDrawable(R.drawable.power_time_picker_battery_new, null).getMinimumHeight();
        setChooseTimeContext(0);
        this.mListWrap = (LinearLayout) view.findViewById(R.id.list_wrap);
        this.mBattery_des = (TextView) view.findViewById(R.id.battery_des);
        this.mTopConsume_des = (TextView) view.findViewById(R.id.top_consume_des);
        this.mEstimatedTime = (TextView) view.findViewById(R.id.battery_history_estimated_time);
        showBatteryInfo();
        this.lv = (ListView) view.findViewById(R.id.listview);
        this.mConsumeDetailAdapter = new ConsumeDetailAdapter(this.mAppContext, screenWidth);
        this.lv.setAdapter(this.mConsumeDetailAdapter);
        this.mDetailImg.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwLog.i(BatteryHistoryFragment.TAG, "mDetailImg is clicked ");
                if (BatteryHistoryFragment.this.isCanClick) {
                    BatteryHistoryFragment.this.isCanClick = false;
                    ViewUtils.setEnabled(BatteryHistoryFragment.this.mDetailImg, false);
                    BatteryHistoryFragment.this.mHandler.sendEmptyMessageDelayed(3, 1000);
                    String[] strArr;
                    if (BatteryHistoryFragment.this.mIsDetail) {
                        BatteryHistoryFragment.this.mDetailImg.setImageResource(R.drawable.btn_more_details);
                        BatteryHistoryFragment.this.mIsDetail = false;
                        strArr = new String[2];
                        strArr[0] = HsmStatConst.PARAM_OP;
                        strArr[1] = BatteryHistoryFragment.this.mIsDetail ? "1" : "0";
                        HsmStat.statE((int) Events.E_POWER_BATTERYHISTORY_CLICK_DETAIL, HsmStatConst.constructJsonParams(strArr));
                        BatteryHistoryFragment.this.mChoose_time.setVisibility(4);
                        ViewUtils.setVisibility(BatteryHistoryFragment.this.mDragBar, 4);
                        BatteryHistoryFragment.this.mProblemAnima = BatteryHistoryFragment.this.buildAnimator(BatteryHistoryFragment.this.mListWrap, BatteryHistoryFragment.this.mLinesLayout);
                        BatteryHistoryFragment.this.mProblemAnima.start();
                    } else {
                        BatteryHistoryFragment.this.mDetailImg.setImageResource(R.drawable.btn_more_details_selected);
                        BatteryHistoryFragment.this.mIsDetail = true;
                        strArr = new String[2];
                        strArr[0] = HsmStatConst.PARAM_OP;
                        strArr[1] = BatteryHistoryFragment.this.mIsDetail ? "1" : "0";
                        HsmStat.statE((int) Events.E_POWER_BATTERYHISTORY_CLICK_DETAIL, HsmStatConst.constructJsonParams(strArr));
                        BatteryHistoryFragment.this.mProblemAnima = BatteryHistoryFragment.this.buildAnimator(BatteryHistoryFragment.this.mLinesLayout, BatteryHistoryFragment.this.mListWrap);
                        ViewUtils.setVisibility(BatteryHistoryFragment.this.mDragBar, 0);
                        BatteryHistoryFragment.this.mDragBar.startAnimation(AnimationUtils.loadAnimation(BatteryHistoryFragment.this.mActivity, R.anim.power_battery_history_dragbar));
                        if (BatteryHistoryFragment.this.mIsFirstShowTopApp) {
                            HwLog.i(BatteryHistoryFragment.TAG, "mIsFirstShowTopApp");
                            long initTime = BatteryHistoryFragment.this.mDragBar.getLastLeftTime();
                            int initLeft = BatteryHistoryFragment.this.mDragBar.getLastLeft();
                            BatteryHistoryFragment.this.setChooseTimeContext(initTime);
                            BatteryHistoryFragment.this.setChooseTimeMargin(initLeft - BatteryHistoryFragment.this.chartLeft);
                            BatteryHistoryFragment.this.showTopApps(initTime);
                            BatteryHistoryFragment.this.mIsFirstShowTopApp = false;
                        }
                        BatteryHistoryFragment.this.mHandler.sendEmptyMessageDelayed(2, 200);
                        BatteryHistoryFragment.this.mProblemAnima.start();
                    }
                    return;
                }
                HwLog.i(BatteryHistoryFragment.TAG, "onClick ,now it can not click , return");
            }
        });
    }

    private void showBatteryInfo() {
        long startTime = this.chart.getChartStartTime();
        long endTime = this.chart.getChartEndTime();
        long factStartTime = this.chart.getBinFileStartWallTime();
        long duringTime = endTime - startTime;
        long duringFactTime = endTime - factStartTime;
        HwLog.i(TAG, "showBatteryInfo, factStartTime= " + factStartTime + ", startTime =" + startTime + ", endTime= " + endTime + ", duringTime =" + duringTime + ", duringFactTime=" + duringFactTime);
        if (duringTime < TWO_DAY_IN_MICRO_SECONDS) {
            this.mBattery_des.setText(getString(R.string.power_battery_history_from_fullpower_des));
        } else {
            this.mBattery_des.setText(String.format(getString(R.string.power_battery_history_two_days_des), new Object[]{Integer.valueOf(2)}));
        }
        int mTimeInMin = (int) (duringFactTime / 60000);
        int hour = mTimeInMin / 60;
        int min = mTimeInMin % 60;
        boolean isBelowZero = duringFactTime < 0;
        if (hour <= 0) {
            if (isBelowZero) {
                HwLog.i(TAG, "time is below zero, set min to 0, min =" + min);
                this.mBatteryInfo.setText(this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, 0, new Object[]{Integer.valueOf(0)}));
            } else {
                this.mBatteryInfo.setText(this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, min, new Object[]{Integer.valueOf(min)}));
            }
        } else if (hour < 24) {
            r20 = this.mBatteryInfo;
            r21 = this.mAppContext.getResources().getString(R.string.power_battery_history_battery_used_hour_time);
            r22 = new Object[2];
            r22[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, hour, new Object[]{Integer.valueOf(hour)});
            r22[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, min, new Object[]{Integer.valueOf(min)});
            r20.setText(String.format(r21, r22));
        } else {
            int temp_day = hour / 24;
            int temp_hour = hour % 24;
            r20 = this.mBatteryInfo;
            r21 = this.mAppContext.getResources().getString(R.string.power_battery_history_battery_used_hour_time);
            r22 = new Object[2];
            r22[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_day_array, temp_day, new Object[]{Integer.valueOf(temp_day)});
            r22[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, temp_hour, new Object[]{Integer.valueOf(temp_hour)});
            r20.setText(String.format(r21, r22));
        }
        showCurrModeEmstimateTime();
    }

    private void showCurrModeEmstimateTime() {
        int hour = (int) (this.mCurrModeLeftTime / 60.0d);
        int min = (int) (this.mCurrModeLeftTime % 60.0d);
        if (hour < 24) {
            TextView textView = this.mEstimatedTime;
            String string = this.mAppContext.getResources().getString(R.string.power_battery_history_estimate_time);
            Object[] objArr = new Object[2];
            objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, hour, new Object[]{Integer.valueOf(hour)});
            objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_min_array, min, new Object[]{Integer.valueOf(min)});
            textView.setText(String.format(string, objArr));
            return;
        }
        int temp_day = hour / 24;
        int temp_hour = hour % 24;
        textView = this.mEstimatedTime;
        string = this.mAppContext.getResources().getString(R.string.power_battery_history_estimate_time);
        objArr = new Object[2];
        objArr[0] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_day_array, temp_day, new Object[]{Integer.valueOf(temp_day)});
        objArr[1] = this.mAppContext.getResources().getQuantityString(R.plurals.power_time_hour_array, temp_hour, new Object[]{Integer.valueOf(temp_hour)});
        textView.setText(String.format(string, objArr));
    }

    private boolean getTopAppInfoByUid(int uid, double powerValue) {
        ConsumeDetailInfo tempConsumeDetailInfo = new ConsumeDetailInfo();
        try {
            PackageManager pm = this.mAppContext.getPackageManager();
            String[] packages = pm.getPackagesForUid(uid);
            if (packages == null || packages.length == 0) {
                HwLog.e(TAG, "getTopAppInfoByUid invalid uid: " + uid);
                return false;
            } else if (1 == packages.length) {
                ApplicationInfo appInfo = pm.getApplicationInfo(packages[0], 0);
                tempConsumeDetailInfo.setmIcon(pm.getApplicationIcon(appInfo));
                tempConsumeDetailInfo.setmPkgTitle(pm.getApplicationLabel(appInfo).toString());
                tempConsumeDetailInfo.setmPkgName(packages[0]);
                tempConsumeDetailInfo.setmPowerValue(powerValue);
                this.mSoftwareList.add(tempConsumeDetailInfo);
                return true;
            } else {
                for (String pkg : packages) {
                    PackageInfo pi = PackageManagerWrapper.getPackageInfo(pm, pkg, 0);
                    if (pi.sharedUserLabel != 0) {
                        CharSequence nm = pm.getText(pkg, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            tempConsumeDetailInfo.setmPkgTitle(nm.toString());
                            tempConsumeDetailInfo.setmIcon(pi.applicationInfo.loadIcon(pm));
                            tempConsumeDetailInfo.setmPkgName(pkg);
                            tempConsumeDetailInfo.setmPowerValue(powerValue);
                            this.mSoftwareList.add(tempConsumeDetailInfo);
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (NameNotFoundException ex) {
            HwLog.e(TAG, "getTopAppInfoByUid catch NameNotFoundException failed uid: " + uid);
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(TAG, "getTopAppInfoByUid catch Exception failed uid: " + uid);
            ex2.printStackTrace();
        }
    }

    private void setChooseTimeContext(long start) {
        long end = start + 3600000;
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        String s = formatter.format(calendar.getTime());
        calendar.setTimeInMillis(end);
        String d = formatter.format(calendar.getTime());
        this.mChoose_time.setText(String.format(getString(R.string.power_battery_choose_time), new Object[]{s, d}));
    }

    private void setChooseTimeMargin(int leftMargin) {
        MarginLayoutParams params = (MarginLayoutParams) this.mChoose_time.getLayoutParams();
        int chooseTextMarginLeft = (this.chartLeft + leftMargin) - (this.mChoose_time.getWidth() / 2);
        int maxBarMoveLength = ((SysCoreUtils.getScreenWidth(this.mAppContext) - this.chartRight) - this.chartAreaRightPadding) - this.mChoose_time.getWidth();
        int minBarMoveLength = this.chartLeft;
        if (chooseTextMarginLeft >= maxBarMoveLength) {
            chooseTextMarginLeft = maxBarMoveLength;
            this.mChoose_time.setBackgroundResource(R.drawable.power_time_picker_right_battery_new);
        } else if (chooseTextMarginLeft <= this.chartLeft) {
            chooseTextMarginLeft = minBarMoveLength;
            this.mChoose_time.setBackgroundResource(R.drawable.power_time_picker_left_battery_new);
        } else {
            chooseTextMarginLeft += dp2px(this.mAppContext, Const.FREE_MEMORY_DANGEROUS_FLOAT);
            this.mChoose_time.setBackgroundResource(R.drawable.power_time_picker_battery_new);
        }
        params.setMarginStart(chooseTextMarginLeft);
        this.mChoose_time.setLayoutParams(params);
    }

    private int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void updateTime(long start, int leftMargin) {
        if (this.mChoose_time != null) {
            HwLog.i(TAG, "start= " + start + " ,leftMargin= " + leftMargin);
            setChooseTimeContext(start);
            setChooseTimeMargin(leftMargin);
        }
    }

    public void onDragBarIdle(long start) {
        HwLog.i(TAG, "onDragBarIdle, start= " + start);
        HsmStat.statE(Events.E_POWER_BATTERYHISTORY_DRAGGING_BAR);
        Message msg = Message.obtain();
        msg.what = 1;
        Bundle bundle = new Bundle();
        bundle.putLong(DRAG_UP_TIME, start);
        msg.setData(bundle);
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessage(msg);
    }

    private Animator buildAnimator(final View progress, final View success) {
        Context ctx = GlobalContext.getContext();
        Animator laodEndAnima = AnimatorInflater.loadAnimator(ctx, R.animator.power_battery_history_disapper);
        laodEndAnima.setTarget(progress);
        laodEndAnima.addListener(new BatteryHistoryAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                progress.setVisibility(8);
                if (BatteryHistoryFragment.this.mSoftwareList.size() > 0) {
                    BatteryHistoryFragment.this.mTopAppLayout.setVisibility(0);
                    BatteryHistoryFragment.this.mNoAppsLayout.setVisibility(8);
                    return;
                }
                BatteryHistoryFragment.this.mTopAppLayout.setVisibility(8);
                BatteryHistoryFragment.this.mNoAppsLayout.setVisibility(0);
            }
        });
        Animator successStartAnima = AnimatorInflater.loadAnimator(ctx, R.animator.power_battery_history_appear);
        successStartAnima.setTarget(success);
        successStartAnima.addListener(new BatteryHistoryAnimatorListener() {
            public void onAnimationStart(Animator animation) {
                success.setVisibility(0);
            }
        });
        AnimatorSet result = new AnimatorSet();
        result.playSequentially(new Animator[]{laodEndAnima, successStartAnima});
        return result;
    }
}
