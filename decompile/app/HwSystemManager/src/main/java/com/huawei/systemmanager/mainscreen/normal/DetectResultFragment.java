package com.huawei.systemmanager.mainscreen.normal;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IWindowFocusChangedListener;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.mainscreen.detector.DetectTaskManager;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.view.MainCircleProgressView;
import com.huawei.systemmanager.mainscreen.view.MainScreenRollingView;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DetectResultFragment extends Fragment implements IWindowFocusChangedListener {
    private static final long OPTIMIZE_DELAY = 1800;
    private static final String TAG = "DetectResultFragment";
    public static final int TYPE_BATTERY = 3;
    public static final int TYPE_MANAGEMENT = 4;
    public static final int TYPE_PERFORMANCE = 1;
    public static final int TYPE_SECURITY = 2;
    public static final int TYPE_UNKNOW = 0;
    private View btnContainerEnd;
    private RelativeLayout btn_container;
    private RelativeLayout contanerStart;
    private RelativeLayout contentLand;
    private LayoutInflater layoutInflater;
    private Animation mAnimationOptimize;
    private boolean mAutoOptimizeComplete;
    private Button mButton;
    private Button mButtonEnd;
    private MainCircleProgressView mCircleImageOptimize;
    private OnClickListener mCompleteBtnClick = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i(DetectResultFragment.TAG, "complete button click, optimize complete:" + DetectResultFragment.this.mAutoOptimizeComplete);
            Activity ac = DetectResultFragment.this.getActivity();
            if (ac != null) {
                ac.finish();
            }
            if (DetectResultFragment.this.mAutoOptimizeComplete) {
                HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_CLICK_FINISH);
            }
        }
    };
    private ViewGroup mContanerEnd;
    private Activity mContext_end;
    private DetectTaskManager mDetectManager;
    private Map<Integer, List<DetectItem>> mGroupItems = new LinkedHashMap();
    private TextView mIndicateOptimize;
    private boolean mIsSupportOrientation;
    private Map<Integer, ViewController> mItemController = new LinkedHashMap();
    private List<DetectItem> mItems = Lists.newArrayList();
    private OnClickListener mOptimizeBtnClick = new OnClickListener() {
        public void onClick(View v) {
            DetectItem item = (DetectItem) v.getTag();
            if (item == null) {
                HwLog.e(DetectResultFragment.TAG, "mOptimizeBtnClick onClick item is null!");
                return;
            }
            Activity ac = DetectResultFragment.this.getActivity();
            if (ac == null) {
                HwLog.w(DetectResultFragment.TAG, "mOptimizeBtnClick onClick but activity is null!");
                return;
            }
            DetectResultFragment.this.statOptimizeAction(item);
            int itemType = item.getItemType();
            HwLog.i(DetectResultFragment.TAG, "user click opimitze btn, item type:" + itemType);
            if (item.getOptimizeActionType() == 1) {
                item.doOptimize(ac.getApplicationContext());
                if (DetectResultFragment.this.mUnoptimizedItems.containsKey(Integer.valueOf(item.getItemType()))) {
                    ((UnoptimizedController) DetectResultFragment.this.mUnoptimizedItems.get(Integer.valueOf(item.getItemType()))).update(item);
                }
                return;
            }
            Intent intent = item.getOptimizeIntent(ac);
            if (intent == null) {
                HwLog.e(DetectResultFragment.TAG, "user click opimitze btn but intent is null! item type:" + itemType);
                return;
            }
            try {
                DetectResultFragment.this.startActivity(intent);
            } catch (Exception e) {
                HwLog.w(DetectResultFragment.TAG, "mOptimizeBtnClick startactivity failed! itemType:" + itemType);
                e.printStackTrace();
            }
        }
    };
    private OptimizeTask mOptimizeTask;
    private PkgRemoveReceiver mPkgRemoveReceiver = new PkgRemoveReceiver() {
        protected void doPkgRemove(String pkgName) {
            if (DetectResultFragment.this.mDetectManager != null && DetectResultFragment.this.mDetectManager.handlerPkgRemove(pkgName)) {
                HwLog.i(DetectResultFragment.TAG, "item state changed when pkg removed:" + pkgName);
            }
        }
    };
    private ResultPresentTask mResultPresentTask;
    private MainScreenRollingView mScoreViewOptimize;
    private final Map<Integer, UnoptimizedController> mUnoptimizedItems = new LinkedHashMap();
    private RelativeLayout mUpperLayoutOptimize;
    private ViewGroup optimizeviewGroup = null;
    private ViewGroup scrollViewEndContent;

    private static class DetectItemScrollListener implements OnScrollListener {
        private DetectItemScrollListener() {
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_SCROLL);
            }
        }

        public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        }
    }

    private class OptimizeTask extends AsyncTask<Map<Integer, List<DetectItem>>, DetectItemEx, Void> {
        private OptimizeTask() {
        }

        protected void onPreExecute() {
            DetectResultFragment.this.mAutoOptimizeComplete = false;
            DetectResultFragment.this.mItemController.clear();
        }

        protected Void doInBackground(Map<Integer, List<DetectItem>>... params) {
            long start = SystemClock.elapsedRealtime();
            HwLog.i(DetectResultFragment.TAG, "OptimizeTask start optimized, ");
            Map<Integer, List<DetectItem>> list = params[0];
            if (isCancelled()) {
                return null;
            }
            Context ctx = GlobalContext.getContext();
            for (Entry entry : list.entrySet()) {
                if (isCancelled()) {
                    HwLog.i(DetectResultFragment.TAG, "Optimize thread canceled!");
                    return null;
                }
                List<DetectItem> currentList = (List) entry.getValue();
                DetectItemEx tempItem;
                if (currentList == null) {
                    tempItem = new DetectItemEx();
                    tempItem.setmCurrent(null);
                    tempItem.setmType(((Integer) entry.getKey()).intValue());
                    publishProgress(new DetectItemEx[]{tempItem});
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        HwLog.i(DetectResultFragment.TAG, "OptimizeTask, doInBackground is interrupted");
                    }
                } else {
                    tempItem = new DetectItemEx();
                    tempItem.setmCurrent((DetectItem) currentList.get(0));
                    tempItem.setmType(((Integer) entry.getKey()).intValue());
                    publishProgress(new DetectItemEx[]{tempItem});
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e2) {
                        HwLog.i(DetectResultFragment.TAG, "OptimizeTask, doInBackground is interrupted");
                    }
                    for (DetectItem currentItem : currentList) {
                        if (currentItem.isOptimized()) {
                            HwLog.i(DetectResultFragment.TAG, "item is already optimized:" + currentItem.getItemType());
                        }
                        if (currentItem.isManulOptimize()) {
                            HwLog.i(DetectResultFragment.TAG, "item is to be manually optmize:" + currentItem.getItemType());
                        } else {
                            HwLog.i(DetectResultFragment.TAG, "start to optimize item:" + currentItem.getItemType());
                            DetectItemEx item = new DetectItemEx();
                            item.setmCurrent(currentItem);
                            item.setmType(((Integer) entry.getKey()).intValue());
                            currentItem.doOptimize(ctx);
                            publishProgress(new DetectItemEx[]{item});
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e3) {
                                HwLog.i(DetectResultFragment.TAG, "OptimizeTask, doInBackground is interrupted");
                            }
                        }
                    }
                    DetectItemEx val = new DetectItemEx();
                    val.setmCurrent(null);
                    val.setmType(((Integer) entry.getKey()).intValue());
                    publishProgress(new DetectItemEx[]{val});
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e4) {
                        HwLog.i(DetectResultFragment.TAG, "OptimizeTask, doInBackground is interrupted");
                    }
                }
            }
            HwLog.i(DetectResultFragment.TAG, "OptimizeTask end, costTime" + (SystemClock.elapsedRealtime() - start));
            return null;
        }

        protected void onProgressUpdate(DetectItemEx... values) {
            super.onProgressUpdate(values);
            ViewController vc;
            if (DetectResultFragment.this.mItemController.get(Integer.valueOf(values[0].getmType())) == null) {
                vc = ViewController.create(DetectResultFragment.this.layoutInflater, DetectResultFragment.this.optimizeviewGroup, values[0]);
                vc.getView().startAnimation(DetectResultFragment.this.mAnimationOptimize);
                DetectResultFragment.this.mItemController.put(Integer.valueOf(values[0].getmType()), vc);
                if (values[0].getmCurrent() == null) {
                    vc.completeView();
                }
            } else {
                vc = (ViewController) DetectResultFragment.this.mItemController.get(Integer.valueOf(values[0].getmType()));
                if (values[0].getmCurrent() == null) {
                    vc.completeView();
                    return;
                }
                vc.updateItemView(values[0].getmCurrent());
            }
            DetectResultFragment.this.updateInfo(true);
        }

        protected void onPostExecute(Void aVoid) {
            DetectResultFragment.this.mAutoOptimizeComplete = true;
            DetectResultFragment.this.updateInfo(true);
            DetectResultFragment.this.mResultPresentTask = new ResultPresentTask();
            DetectResultFragment.this.mResultPresentTask.executeOnExecutor(DetectResultFragment.this.mDetectManager.getCleanExecutor(), new List[]{DetectResultFragment.this.mItems});
            DetectResultFragment.this.mButtonEnd.setText(R.string.main_screen_detect_btn_complete);
        }
    }

    private class RefreshItemTask extends AsyncTask<List<DetectItem>, Void, Void> {
        private RefreshItemTask() {
        }

        protected Void doInBackground(List<DetectItem>... params) {
            long start = SystemClock.elapsedRealtime();
            HwLog.i(DetectResultFragment.TAG, "RefreshItemTask doInBackground start");
            for (DetectItem item : params[0]) {
                if (!item.isOptimized()) {
                    int itemType = item.getItemType();
                    if (itemType != 4) {
                        HwLog.i(DetectResultFragment.TAG, "refresh item:" + itemType);
                        item.refresh();
                    }
                }
            }
            HwLog.i(DetectResultFragment.TAG, "RefreshItemTask end, cost time:" + (SystemClock.elapsedRealtime() - start));
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            DetectResultFragment.this.updateInfo(true);
        }
    }

    private class ResultPresentTask extends AsyncTask<List<DetectItem>, DetectItem, Void> {
        private ResultPresentTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            DetectResultFragment.this.mUnoptimizedItems.clear();
        }

        protected Void doInBackground(List<DetectItem>... params) {
            long start = SystemClock.elapsedRealtime();
            HwLog.i(DetectResultFragment.TAG, "ResultPresentTask doInBackground start");
            for (DetectItem item : params[0]) {
                if (!item.isOptimized() && item.isManulOptimize()) {
                    publishProgress(new DetectItem[]{item});
                }
            }
            HwLog.i(DetectResultFragment.TAG, "ResultPresentTask end, cost time:" + (SystemClock.elapsedRealtime() - start));
            return null;
        }

        protected void onProgressUpdate(DetectItem... item) {
            super.onProgressUpdate(new DetectItem[]{item[0]});
            DetectResultFragment.this.mUnoptimizedItems.put(Integer.valueOf(item[0].getItemType()), UnoptimizedController.create(DetectResultFragment.this.layoutInflater, DetectResultFragment.this.scrollViewEndContent, DetectResultFragment.this.mOptimizeBtnClick, item[0]));
        }

        protected void onPostExecute(Void aVoid) {
            DetectResultFragment.this.displayOptimized();
            DetectResultFragment.this.outputAnim(DetectResultFragment.this.mContext_end);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mIsSupportOrientation = Utility.isSupportOrientation();
        View view = inflater.inflate(R.layout.main_screen_detect_result_layout, container, false);
        initDetectFragment(view);
        initShowFragment(view);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity ac = getActivity();
        this.mContext_end = ac;
        inputAnim(ac);
        this.mDetectManager = ((DetectResultActivity) ac).getDetectorManager();
        if (this.mDetectManager == null) {
            HwLog.e(TAG, "onViewCreated getDetectorManager is null!!");
            ac.finish();
            return;
        }
        this.mPkgRemoveReceiver.registeReceiver();
        this.mDetectManager.setCouldRefresh(false);
        HwLog.i(TAG, "onViewCreated setcouldrefresh false");
        initDetectItems();
        checkAndStartOtpimize();
        if (this.mIsSupportOrientation) {
            initScreenOrientation(ac.getResources().getConfiguration());
        }
    }

    private void initDetectFragment(View view) {
        this.layoutInflater = LayoutInflater.from(GlobalContext.getContext());
        this.mButton = (Button) view.findViewById(R.id.complete_btn);
        this.mButton.setOnClickListener(this.mCompleteBtnClick);
        this.mUpperLayoutOptimize = (RelativeLayout) view.findViewById(R.id.upper_layout_optimize);
        this.mCircleImageOptimize = (MainCircleProgressView) view.findViewById(R.id.scan_image_optimize);
        this.mIndicateOptimize = (TextView) view.findViewById(R.id.indicate_optimize);
        this.mScoreViewOptimize = (MainScreenRollingView) view.findViewById(R.id.score_optimize);
        this.btn_container = (RelativeLayout) view.findViewById(R.id.btn_container);
        this.contanerStart = (RelativeLayout) view.findViewById(R.id.contaner_start);
        this.contentLand = (RelativeLayout) view.findViewById(R.id.container);
        this.optimizeviewGroup = (ViewGroup) view.findViewById(R.id.item_container);
    }

    private void initShowFragment(View view) {
        this.mContanerEnd = (ViewGroup) view.findViewById(R.id.contaner_end);
        this.btnContainerEnd = (RelativeLayout) view.findViewById(R.id.btn_container_end);
        this.mButtonEnd = (Button) view.findViewById(R.id.complete_btn_end);
        this.mButtonEnd.setOnClickListener(this.mCompleteBtnClick);
        this.scrollViewEndContent = (ViewGroup) view.findViewById(R.id.scroll_view_end_content);
        this.scrollViewEndContent.getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void divideGroup(List<DetectItem> items) {
        this.mGroupItems.clear();
        this.mGroupItems.put(Integer.valueOf(1), null);
        this.mGroupItems.put(Integer.valueOf(2), null);
        this.mGroupItems.put(Integer.valueOf(3), null);
        this.mGroupItems.put(Integer.valueOf(4), null);
        for (DetectItem temp : items) {
            int values = getGroupType(temp.getItemType());
            List<DetectItem> tList;
            if (this.mGroupItems.get(Integer.valueOf(values)) == null) {
                tList = new ArrayList();
                tList.add(temp);
                this.mGroupItems.put(Integer.valueOf(values), tList);
            } else {
                tList = (List) this.mGroupItems.get(Integer.valueOf(values));
                tList.add(temp);
                this.mGroupItems.put(Integer.valueOf(values), tList);
            }
        }
    }

    public static int getGroupType(int key) {
        switch (key) {
            case 1:
            case 4:
            case 5:
            case 12:
            case 17:
                return 2;
            case 2:
            case 6:
            case 7:
            case 10:
            case 11:
                return 1;
            case 3:
            case 14:
            case 15:
            case 16:
                return 4;
            case 8:
            case 9:
            case 13:
                return 3;
            default:
                return 0;
        }
    }

    private void inputAnim(Activity ac) {
        this.mAnimationOptimize = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_optimize_stepone);
    }

    private void outputAnim(final Activity ac) {
        final Animation animation = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_optimize_steptwo);
        Animation animation_hide = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_optimize_hide);
        final LayoutAnimationController anima = new LayoutAnimationController(animation, 0.2f);
        anima.setOrder(1);
        animation_hide.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation a) {
                DetectResultFragment.this.mCircleImageOptimize.updateSocre(0, 350);
                DetectResultFragment.this.btn_container.setAnimation(animation);
                DetectResultFragment.this.optimizeviewGroup.setLayoutAnimation(anima);
                DetectResultFragment.this.optimizeviewGroup.startLayoutAnimation();
            }

            public void onAnimationRepeat(Animation a) {
            }

            public void onAnimationEnd(Animation a) {
                DetectResultFragment.this.btn_container.setVisibility(8);
                DetectResultFragment.this.contanerStart.setVisibility(8);
                DetectResultFragment.this.inputEndAnim(ac);
            }
        });
        this.mUpperLayoutOptimize.startAnimation(animation_hide);
    }

    private void inputEndAnim(Activity ac) {
        Animation animation = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_optimize_stepthree);
        LayoutAnimationController anima = new LayoutAnimationController(animation, 0.1f);
        LayoutAnimationController anima_all = new LayoutAnimationController(animation, 0.5f);
        this.mContanerEnd.setVisibility(0);
        this.scrollViewEndContent.setLayoutAnimation(anima);
        this.scrollViewEndContent.startLayoutAnimation();
        this.btnContainerEnd.setVisibility(0);
        this.mContanerEnd.setLayoutAnimation(anima_all);
        this.mContanerEnd.startLayoutAnimation();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsSupportOrientation) {
            initScreenOrientation(newConfig);
        }
    }

    private void initScreenOrientation(Configuration newConfig) {
        int nuoyiLeftWidth;
        int i = R.id.upper_layout_optimize;
        boolean isLand = 2 == newConfig.orientation;
        LayoutParams cp = (LayoutParams) this.mUpperLayoutOptimize.getLayoutParams();
        if (isLand) {
            nuoyiLeftWidth = HSMConst.getNuoyiLeftWidth();
        } else {
            nuoyiLeftWidth = -1;
        }
        cp.width = nuoyiLeftWidth;
        cp.height = -1;
        this.mUpperLayoutOptimize.setLayoutParams(cp);
        LayoutParams ctp = (LayoutParams) this.contentLand.getLayoutParams();
        ctp.addRule(17, isLand ? R.id.upper_layout_optimize : -1);
        ctp.addRule(3, isLand ? -1 : R.id.upper_layout_optimize);
        this.contentLand.setLayoutParams(ctp);
        LayoutParams bp = (LayoutParams) this.btn_container.getLayoutParams();
        if (!isLand) {
            i = -1;
        }
        bp.addRule(17, i);
        this.btn_container.setLayoutParams(bp);
        LayoutParams mIndiParams = (LayoutParams) this.mIndicateOptimize.getLayoutParams();
        mIndiParams.bottomMargin = GlobalContext.getContext().getResources().getDimensionPixelSize(R.dimen.hsm_nuoyi_circleinfo_marginbottom);
        this.mIndicateOptimize.setLayoutParams(mIndiParams);
    }

    private void checkAndStartOtpimize() {
        for (DetectItem item : this.mItems) {
            if (!item.isOptimized() && !item.isManulOptimize()) {
                break;
            }
        }
        this.mCircleImageOptimize.setCompleteStatus();
        updateInfo(false);
        this.mOptimizeTask = new OptimizeTask();
        divideGroup(this.mItems);
        this.mOptimizeTask.executeOnExecutor(this.mDetectManager.getCleanExecutor(), new Map[]{this.mGroupItems});
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mDetectManager != null) {
            HwLog.i(TAG, "onDestroyView setcouldrefresh true");
            this.mDetectManager.setCouldRefresh(true);
        }
        this.mPkgRemoveReceiver.unRegisteReceiver();
        if (this.mOptimizeTask != null) {
            this.mOptimizeTask.cancel(true);
        }
    }

    private void setResult(int score) {
        Activity ac = getActivity();
        if (ac != null) {
            ac.setResult(score);
        }
    }

    private void initDetectItems() {
        this.mItems.addAll(this.mDetectManager.getResult());
    }

    private void updateInfo(boolean anima) {
        HwLog.i(TAG, "updateInfo called");
        int score = this.mDetectManager.getScoreByItems(this.mItems);
        setResult(score);
        if (anima) {
            this.mCircleImageOptimize.updateSocre(score, IUpdateListener.ERROR_CODE_NO_NETWORK);
            this.mScoreViewOptimize.setNumberByDuration(score, IUpdateListener.ERROR_CODE_NO_NETWORK);
            return;
        }
        this.mCircleImageOptimize.updateScoreImmidiately(score);
        this.mScoreViewOptimize.setNumberByDuration(score, 0);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (this.mAutoOptimizeComplete && hasFocus) {
            HwLog.i(TAG, "onWindowFocusChanged true, start refresh task");
            new RefreshItemTask().executeOnExecutor(this.mDetectManager.getCleanExecutor(), new List[]{this.mItems});
            for (Entry entry : this.mUnoptimizedItems.entrySet()) {
                DetectItem currentItem = null;
                for (DetectItem item : this.mItems) {
                    if (((Integer) entry.getKey()).intValue() == item.getItemType()) {
                        currentItem = item;
                        break;
                    }
                }
                if (currentItem != null) {
                    currentItem.refresh();
                    ((UnoptimizedController) entry.getValue()).update(currentItem);
                }
            }
        }
    }

    private void displayOptimized() {
        this.scrollViewEndContent.addView(this.layoutInflater.inflate(R.layout.main_screen_optimize_result_divideline, this.scrollViewEndContent, false));
        for (Entry entry : this.mGroupItems.entrySet()) {
            List<DetectItem> currentList = (List) entry.getValue();
            View optView = this.layoutInflater.inflate(R.layout.main_screen_optimize_result_set, this.scrollViewEndContent, false);
            ((TextView) optView.findViewById(R.id.title)).setText(getTitle(((Integer) entry.getKey()).intValue()));
            if (currentList == null) {
                this.scrollViewEndContent.addView(optView);
            } else {
                ViewGroup textGroup = (ViewGroup) optView.findViewById(R.id.textitem_container);
                for (DetectItem currentItem : currentList) {
                    if (currentItem.isOptimized() && currentItem.isVisiable()) {
                        View singleitem = this.layoutInflater.inflate(R.layout.main_screen_optimize_result_set_item, textGroup, false);
                        ((TextView) singleitem.findViewById(R.id.tv_item)).setText(currentItem.getTitle(GlobalContext.getContext()));
                        textGroup.addView(singleitem);
                    }
                }
                this.scrollViewEndContent.addView(optView);
            }
        }
    }

    private int getTitle(int entryName) {
        switch (entryName) {
            case 1:
                return R.string.space_optimize_performance;
            case 2:
                return R.string.space_optimize_security;
            case 3:
                return R.string.space_optimize_battery;
            case 4:
                return R.string.space_optimize_manage;
            default:
                return R.string.space_optimize_completed;
        }
    }

    private void statOptimizeAction(DetectItem item) {
        if (item != null) {
            item.statOptimizeEvent();
        }
    }
}
