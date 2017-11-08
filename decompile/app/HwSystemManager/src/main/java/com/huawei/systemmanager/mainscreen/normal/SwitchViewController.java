package com.huawei.systemmanager.mainscreen.normal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonPageAdapter;
import com.huawei.systemmanager.customize.OverseaCfgConst;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.mainscreen.detector.DetectTaskManager;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance;
import com.huawei.systemmanager.mainscreen.entrance.entry.Entries;
import com.huawei.systemmanager.mainscreen.entrance.entry.EntryAppLocker;
import com.huawei.systemmanager.mainscreen.entrance.entry.EntryProtect;
import com.huawei.systemmanager.mainscreen.entrance.entry.EntryStorageClean;
import com.huawei.systemmanager.mainscreen.entrance.entry.EntryVirusScan;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class SwitchViewController implements MessageHandler {
    private static final int MSG_INFLATE_PAGE2 = 2;
    private static final int MSG_INFLATE_PAGE2_ABROAD_INSTALL = 3;
    public static final String TAG = "SwitchViewController";
    private ImageView icon1;
    private ImageView icon1_off;
    private ImageView icon2;
    private ImageView icon2_on;
    private final List<AbsEntrance> mAllEntrances;
    private final Activity mContext;
    protected LinkedHashMap<String, AbsEntrance> mEntries = new LinkedHashMap();
    private OnClickListener mEntryClicker = new OnClickListener() {
        public void onClick(View v) {
            AbsEntrance entry = (AbsEntrance) v.getTag(R.id.convertview_tag_item);
            if (entry != null) {
                String entryName = entry.getEntryName();
                final Intent intent = entry.getEntryIntent(SwitchViewController.this.mContext);
                if (intent == null) {
                    HwLog.e(SwitchViewController.TAG, "onclick intent is null! entryName:" + entryName);
                    return;
                }
                try {
                    if (EntryStorageClean.NAME.equalsIgnoreCase(entryName)) {
                        HsmStat.statE(Events.E_OPTMIZE_ENTER_FROM_SYSTEMMANAGER);
                    } else if (EntryAppLocker.NAME.equalsIgnoreCase(entryName)) {
                        HsmStat.statE(Events.E_APPLOCK_ENTER_FROM_SYSTEMMANAGER);
                    } else if (EntryProtect.NAME.equalsIgnoreCase(entryName)) {
                        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                        HsmStat.statE((int) Events.E_ENTER_PROTECTEDAPP, statParam);
                    }
                    new Thread() {
                        public void run() {
                            try {
                                AnonymousClass1.sleep(43);
                                SwitchViewController.this.mContext.startActivity(intent);
                            } catch (Exception e) {
                                HwLog.e("TAG", "startActivity error", e);
                            }
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private EntryVirusScan mEntryVirusScan;
    private Handler mHandler = new GenericHandler(this);
    protected View mMainView;
    private BroadcastReceiver mOverseaChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                HwLog.i(SwitchViewController.TAG, "mOverseaChangeReceiver, received");
                SwitchViewController.this.mHandler.sendEmptyMessageDelayed(3, 200);
                return;
            }
            HwLog.i(SwitchViewController.TAG, "mOverseaChangeReceiver,check failed!");
        }
    };
    private boolean mOverseaReceiverRegisted = false;
    private List<RelativeLayout> mRelativeLayouts = Lists.newArrayList();
    private ViewPager mViewPager;
    private OnTouchListener mViewPagerTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            SwitchViewController.this.mViewPager.setOnTouchListener(null);
            SwitchViewController.this.mHandler.sendEmptyMessage(2);
            return false;
        }
    };
    private boolean mViewpagerPage2Inflated;
    private ArrayList<View> mViews;

    SwitchViewController(Activity ac, View mainView) {
        this.mContext = ac;
        this.mMainView = mainView;
        this.mAllEntrances = Entries.getEntries();
        initEntrance(ac.getApplicationContext());
        ensureEntryView();
        sendHandlerMsg();
    }

    protected void sendHandlerMsg() {
        this.mHandler.sendEmptyMessageDelayed(2, 550);
    }

    private void initEntrance(Context ctx) {
        for (AbsEntrance entrance : this.mAllEntrances) {
            if (entrance.isEnable(this.mContext)) {
                String pkg = entrance.getEntryName();
                entrance.onCreate(ctx);
                this.mEntries.put(pkg, entrance);
            }
        }
        this.mEntryVirusScan = (EntryVirusScan) this.mEntries.get(EntryVirusScan.NAME);
    }

    public void setDetectManager(DetectTaskManager detectManager) {
        if (this.mEntryVirusScan != null) {
            this.mEntryVirusScan.setDetectMgr(detectManager);
        }
    }

    public void onResume() {
        for (AbsEntrance entrance : this.mAllEntrances) {
            entrance.onResume();
        }
    }

    public void onPause() {
        for (AbsEntrance entrance : this.mAllEntrances) {
            entrance.onPause();
        }
    }

    private void ensureEntryView() {
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        this.mViewPager = (ViewPager) this.mMainView.findViewById(R.id.bottom_viewpager);
        LayoutAnimationController anima = new LayoutAnimationController(AnimationUtils.loadAnimation(this.mContext, R.anim.mainscreen_entry_icon), 0.4f);
        this.mViews = Lists.newArrayList();
        int j = getPageCount();
        for (int i = 0; i < j; i++) {
            RelativeLayout page = (RelativeLayout) inflater.inflate(R.layout.main_screen_entry_page, null);
            this.mRelativeLayouts.add(page);
            this.mViews.add(page);
            page.setLayoutAnimation(anima);
        }
        initPageContent(inflater, 0, (RelativeLayout) this.mViews.get(0));
        this.mViewPager.setAdapter(new CommonPageAdapter(this.mContext, this.mViews));
        ensureEntryViewForDevice();
    }

    protected void ensureEntryViewForDevice() {
        this.icon1 = (ImageView) this.mMainView.findViewById(R.id.icon_01);
        this.icon2 = (ImageView) this.mMainView.findViewById(R.id.icon_02);
        this.icon1_off = (ImageView) this.mMainView.findViewById(R.id.icon_01_off);
        this.icon2_on = (ImageView) this.mMainView.findViewById(R.id.icon_02_on);
        this.mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                if (position == 0) {
                    SwitchViewController.this.icon1_off.setVisibility(8);
                    SwitchViewController.this.icon2_on.setVisibility(8);
                    SwitchViewController.this.icon1.setVisibility(0);
                    SwitchViewController.this.icon2.setVisibility(0);
                    HsmStat.statE(Events.E_MAINSCREEN_FLIP_PAGE_TO_1);
                    return;
                }
                SwitchViewController.this.inflaterPage2();
                SwitchViewController.this.icon1_off.setVisibility(0);
                SwitchViewController.this.icon2_on.setVisibility(0);
                SwitchViewController.this.icon1.setVisibility(8);
                SwitchViewController.this.icon2.setVisibility(8);
                HsmStat.statE(Events.E_MAINSCREEN_FLIP_PAGE_TO_2);
            }
        });
        this.mViewPager.setOnTouchListener(this.mViewPagerTouchListener);
    }

    private int getEntryViewWidth(int column) {
        int width;
        if (HSMConst.isLand()) {
            int i = HSMConst.getLongOrShortLength(true);
            width = (i - HSMConst.getNuoyiLeftWidth()) - HSMConst.getDimensionPixelSize(R.dimen.virtual_navigator_height);
        } else {
            width = HSMConst.getDisplayMetrics().widthPixels;
        }
        return (width - (HSMConst.getDimensionPixelSize(R.dimen.main_screen_entry_space_to_borad_horizontal) * 2)) / column;
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                inflaterPage2();
                return;
            default:
                return;
        }
    }

    private void inflaterPage2() {
        if (!this.mViewpagerPage2Inflated) {
            if (this.mViews == null) {
                HwLog.w(TAG, "inflaterPage2 but mView is null!");
                return;
            }
            this.mViewpagerPage2Inflated = true;
            LayoutInflater inflater = LayoutInflater.from(this.mContext);
            if (this.mViews.size() > 1) {
                initPageContent(inflater, 1, (RelativeLayout) this.mViews.get(1));
            }
        }
    }

    public void registerOverseaReceiver() {
        if (!this.mOverseaReceiverRegisted) {
            this.mOverseaReceiverRegisted = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(OverseaCfgConst.OVERSEA_SWITCH_CHANGE_ACTION);
            this.mContext.registerReceiver(this.mOverseaChangeReceiver, intentFilter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        }
    }

    public void unregisterOverseaReceiver() {
        if (this.mOverseaReceiverRegisted) {
            this.mOverseaReceiverRegisted = false;
            this.mContext.unregisterReceiver(this.mOverseaChangeReceiver);
        }
    }

    public void release(Activity ac) {
        unregisterOverseaReceiver();
        for (AbsEntrance entrace : this.mEntries.values()) {
            entrace.onDestory();
        }
    }

    private void initPageContent(LayoutInflater inflater, int pageNo, RelativeLayout container) {
        ArrayList<RelativeLayout> entryLayoutLs = new ArrayList();
        List<AbsEntrance> entries = Lists.newArrayList(this.mEntries.values());
        int pageStartIndex = pageNo * getEntryViewCountPerPage();
        if (pageStartIndex < 0 || pageStartIndex >= entries.size()) {
            HwLog.e(TAG, "initPageContent failed, pageStartIndex:" + pageStartIndex + ", entrance size:" + entries.size());
            return;
        }
        int entryWidth = getEntryViewWidth(getColumn());
        boolean isLand = HSMConst.isLand();
        for (int indexPage = 0; indexPage < getEntryViewCountPerPage(); indexPage++) {
            RelativeLayout entryLayout;
            int index = pageStartIndex + indexPage;
            if (index < entries.size()) {
                entryLayout = (RelativeLayout) ((AbsEntrance) entries.get(index)).createView(inflater, indexPage, container);
                entryLayout.setOnClickListener(this.mEntryClicker);
            } else {
                entryLayout = (RelativeLayout) inflater.inflate(R.layout.main_screen_entry_empty_item, container, false);
            }
            setId(indexPage, entryLayout);
            entryLayoutLs.add(entryLayout);
            entryLayout.setMinimumHeight(this.mContext.getResources().getDimensionPixelSize(R.dimen.main_screen_normal_entry_up_item_height));
            container.addView(entryLayout);
            initParams(entryLayoutLs, entryWidth, pageNo, indexPage, isLand);
        }
    }

    private void initParams(ArrayList<RelativeLayout> entryLayoutLs, int width, int pageNo, int indexInPage, boolean isLand) {
        if (indexInPage >= entryLayoutLs.size()) {
            HwLog.e(TAG, "initParams OutOfIndexException");
            return;
        }
        View view = (View) entryLayoutLs.get(indexInPage);
        if (!setEmptyEntryViewVisibleGone(view, (getEntryViewCountPerPage() * pageNo) + indexInPage, isLand)) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            params.removeRule(3);
            params.removeRule(6);
            params.removeRule(17);
            params.width = width;
            int row = indexInPage / getColumn();
            if (indexInPage % getColumn() != 0) {
                params.addRule(6, ((RelativeLayout) entryLayoutLs.get(indexInPage - 1)).getId());
                params.addRule(17, ((RelativeLayout) entryLayoutLs.get(indexInPage - 1)).getId());
            } else if (row != 0) {
                params.addRule(3, ((RelativeLayout) entryLayoutLs.get(indexInPage - getColumn())).getId());
            }
            view.setLayoutParams(params);
        }
    }

    protected boolean setEmptyEntryViewVisibleGone(View view, int realPos, boolean isLand) {
        return false;
    }

    private void setId(int index, View view) {
        switch (index) {
            case 0:
                view.setId(R.id.entry_view_layout0);
                return;
            case 1:
                view.setId(R.id.entry_view_layout1);
                return;
            case 2:
                view.setId(R.id.entry_view_layout2);
                return;
            case 3:
                view.setId(R.id.entry_view_layout3);
                return;
            case 4:
                view.setId(R.id.entry_view_layout4);
                return;
            case 5:
                view.setId(R.id.entry_view_layout5);
                return;
            case 6:
                view.setId(R.id.entry_view_layout6);
                return;
            case 7:
                view.setId(R.id.entry_view_layout7);
                return;
            case 8:
                view.setId(R.id.entry_view_layout8);
                return;
            default:
                return;
        }
    }

    protected int getPageCount() {
        if (this.mEntries.size() > getEntryViewCountPerPage()) {
            return 2;
        }
        this.mMainView.findViewById(R.id.dot_bar_layout).setVisibility(8);
        return 1;
    }

    protected int getEntryViewCountPerPage() {
        return 6;
    }

    protected int getColumn() {
        if (HSMConst.isLand()) {
            return 2;
        }
        return 3;
    }

    protected void refreshScreenOrientation(Configuration configuration) {
        if (this.mViewPager != null) {
            int spaceToBoard = HSMConst.getDimensionPixelSize(R.dimen.main_screen_entry_space_to_borad_horizontal);
            LayoutParams vpParams = (LayoutParams) this.mViewPager.getLayoutParams();
            vpParams.leftMargin = spaceToBoard;
            vpParams.rightMargin = spaceToBoard;
            this.mViewPager.setLayoutParams(vpParams);
        }
        int entryViewWidth = getEntryViewWidth(getColumn());
        boolean isLand = configuration.orientation == 2;
        int page = 0;
        int totalPage = this.mRelativeLayouts.size();
        while (page < totalPage) {
            RelativeLayout layout = (RelativeLayout) this.mRelativeLayouts.get(page);
            int count = layout.getChildCount();
            if (count == getEntryViewCountPerPage()) {
                ArrayList<RelativeLayout> entryLayoutArray = new ArrayList();
                for (int index = 0; index < count; index++) {
                    entryLayoutArray.add((RelativeLayout) layout.getChildAt(index));
                    initParams(entryLayoutArray, entryViewWidth, page, index, isLand);
                }
                page++;
            } else {
                return;
            }
        }
    }
}
