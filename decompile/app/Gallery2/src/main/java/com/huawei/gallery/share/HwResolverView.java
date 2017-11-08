package com.huawei.gallery.share;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.IntentChooser.IShareItem;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.video.ShareClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HwResolverView extends LinearLayout implements OnItemClickListener {
    private static final Uri INSERT = Uri.parse("content://com.huawei.share.lru.file/share/historyinfo/update");
    private static final Uri QUERY = Uri.parse("content://com.huawei.share.lru.file/share/historyinfo");
    private ResolveListAdapter mAdapter;
    int mCurrentScreenNum = 0;
    private Delegate mDelegate;
    private boolean mDidFirstLayout = true;
    private int mIconDpi;
    private int mInitialScreenNum;
    private int mLaunchedFromUid;
    private GridViewClickListener mListener;
    protected final ArrayList<String> mLruApplications = new ArrayList();
    private PackageManager mPm;
    private ArrayList<DisplayResolveInfo> mPreviligedApp = new ArrayList();
    private QuickNavigation mQuickNavigation;
    private int mSavedSelectedItem = -1;
    int mScreenCount;
    private ShareClickListener mShareListener;
    private boolean mShowExtended;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    public interface GridViewClickListener {
        void itemClicked();

        void onClickCancel();
    }

    public static final class DisplayResolveInfo {
        Drawable displayIcon;
        CharSequence displayLabel;
        CharSequence extendedInfo;
        IShareItem item;
        Intent origIntent;
        ResolveInfo ri;

        public DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.ri = pri;
            this.displayLabel = pLabel;
            this.extendedInfo = pInfo;
            this.origIntent = pOrigIntent;
        }

        public DisplayResolveInfo(IShareItem item) {
            this.item = item;
            this.displayLabel = item.getLabel();
            this.displayIcon = item.getIcon();
        }
    }

    private final class GridViewAdapter extends BaseAdapter {
        List<DisplayResolveInfo> mGridList = new ArrayList();
        GridView mGridView;
        SparseArray<View> mItemViews = new SparseArray(6);

        public GridViewAdapter(Context context, List<DisplayResolveInfo> rList, GridView gv, int pageNo) {
            int end = (pageNo + 1) * 6;
            int i = pageNo * 6;
            while (i < end && i < rList.size()) {
                this.mGridList.add((DisplayResolveInfo) rList.get(i));
                i++;
            }
            this.mGridView = gv;
        }

        public int getCount() {
            return this.mGridList.size();
        }

        public Object getItem(int position) {
            return this.mGridList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public ResolveInfo resolveInfoForPosition(int position) {
            return ((DisplayResolveInfo) this.mGridList.get(position)).ri;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = (View) this.mItemViews.get(position);
            if (view == null) {
                view = View.inflate(HwResolverView.this.getContext(), R.layout.resolve_list_item_emui, null);
                this.mItemViews.put(position, view);
                ViewHolder holder = new ViewHolder(view);
                view.setTag(holder);
                LayoutParams lp = holder.icon.getLayoutParams();
                int dpToPixel = GalleryUtils.dpToPixel(62);
                lp.height = dpToPixel;
                lp.width = dpToPixel;
            }
            bindView(view, (DisplayResolveInfo) this.mGridList.get(position));
            HwImageView hiv = (HwImageView) ((ViewGroup) view).findViewById(R.id.icon);
            if (position == this.mGridView.getCheckedItemPosition()) {
                if (hiv.getDrawable() != null) {
                    hiv.setForceSelected(true);
                }
            } else if (hiv.getDrawable() != null) {
                hiv.setForceSelected(false);
            }
            return view;
        }

        private final void bindView(View view, DisplayResolveInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text.setText(info.displayLabel);
            if (!HwResolverView.this.mShowExtended || TextUtils.isEmpty(info.extendedInfo) || info.ri == null || (info.ri.activityInfo.applicationInfo.flags & 1) != 0) {
                holder.text2.setVisibility(8);
            } else {
                holder.text2.setVisibility(8);
                holder.text.setText(info.extendedInfo);
            }
            if (info.displayIcon == null) {
                new LoadIconTask(this).execute(new DisplayResolveInfo[]{info});
            }
            holder.icon.setImageDrawable(info.displayIcon);
        }
    }

    class ItemLongClickListener implements OnItemLongClickListener {
        private GridViewAdapter mGridViewAdapter;

        public ItemLongClickListener(GridViewAdapter adapter) {
            this.mGridViewAdapter = adapter;
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            ResolveInfo ri = this.mGridViewAdapter.resolveInfoForPosition(position);
            HwResolverView.this.mListener.itemClicked();
            if (ri == null) {
                return true;
            }
            HwResolverView.this.showAppDetails(ri);
            return true;
        }
    }

    class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        BaseAdapter mTaskInternalAdapter;

        public LoadIconTask(BaseAdapter adapter) {
            this.mTaskInternalAdapter = adapter;
        }

        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = HwResolverView.this.loadIconForResolveInfo(info.ri);
            }
            return info;
        }

        protected void onPostExecute(DisplayResolveInfo info) {
            this.mTaskInternalAdapter.notifyDataSetChanged();
        }
    }

    private class ResolveListAdapter extends BaseAdapter {
        private final List<ResolveInfo> mBaseResolveList;
        private int mInitialHighlight = -1;
        private final Intent[] mInitialIntents;
        private final Intent mIntent;
        private ResolveInfo mLastChosen;
        private final int mLaunchedFromUid;
        List<DisplayResolveInfo> mList;
        List<ResolveInfo> mOrigResolveList;

        public ResolveListAdapter(Context context, Intent intent, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid) {
            this.mIntent = new Intent(intent);
            this.mInitialIntents = initialIntents;
            this.mBaseResolveList = rList;
            this.mLaunchedFromUid = launchedFromUid;
            this.mList = new ArrayList();
            rebuildList();
            appendCustomItem();
        }

        public int getInitialHighlight() {
            return this.mInitialHighlight;
        }

        public int getTotalCount() {
            return this.mList.size();
        }

        private void appendCustomItem() {
            HwResolverView.this.mPreviligedApp.addAll(HwResolverView.this.mDelegate.getGalleryShareItem());
            this.mList.addAll(0, HwResolverView.this.mPreviligedApp);
        }

        private void rebuildList() {
            List<ResolveInfo> currentResolveList;
            int i;
            if (!HwResolverView.this.mDelegate.alwaysUseOption()) {
                try {
                    this.mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(this.mIntent, this.mIntent.resolveTypeIfNeeded(HwResolverView.this.getContext().getContentResolver()), HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT);
                } catch (RemoteException re) {
                    GalleryLog.d("HwResolverView", "Error calling setLastChosenActivity\n" + re);
                }
            }
            this.mList.clear();
            if (this.mBaseResolveList != null) {
                currentResolveList = this.mBaseResolveList;
                this.mOrigResolveList = null;
            } else {
                currentResolveList = HwResolverView.this.mPm.queryIntentActivities(this.mIntent, HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT);
                this.mOrigResolveList = currentResolveList;
                if (currentResolveList != null) {
                    for (i = currentResolveList.size() - 1; i >= 0; i--) {
                        ActivityInfo ai = ((ResolveInfo) currentResolveList.get(i)).activityInfo;
                        int granted = ActivityManager.checkComponentPermission(ai.permission, this.mLaunchedFromUid, ai.applicationInfo.uid, ai.exported);
                        GalleryLog.d("HwResolverView", "all displayed label, pos: " + i + ",label: " + ((ResolveInfo) currentResolveList.get(i)).loadLabel(HwResolverView.this.mPm));
                        if (granted != 0) {
                            if (this.mOrigResolveList == currentResolveList) {
                                this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                            }
                            currentResolveList.remove(i);
                        }
                    }
                }
            }
            if (currentResolveList != null) {
                int N = currentResolveList.size();
                if (N > 0) {
                    ResolveInfo ri;
                    ResolveInfo r0 = (ResolveInfo) currentResolveList.get(0);
                    for (i = 1; i < N; i++) {
                        ri = (ResolveInfo) currentResolveList.get(i);
                        if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                            while (i < N) {
                                if (this.mOrigResolveList == currentResolveList) {
                                    this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                                }
                                currentResolveList.remove(i);
                                N--;
                            }
                        }
                    }
                    if (N > 1) {
                        Collections.sort(currentResolveList, new DisplayNameComparator(HwResolverView.this.mPm));
                    }
                    if (this.mInitialIntents != null) {
                        for (Intent ii : this.mInitialIntents) {
                            if (ii != null) {
                                ai = ii.resolveActivityInfo(HwResolverView.this.getContext().getPackageManager(), 0);
                                if (ai == null) {
                                    GalleryLog.w("ResolverActivity", "No activity found for " + ii);
                                } else {
                                    ri = new ResolveInfo();
                                    ri.activityInfo = ai;
                                    if (ii instanceof LabeledIntent) {
                                        LabeledIntent li = (LabeledIntent) ii;
                                        ri.resolvePackageName = li.getSourcePackage();
                                        ri.labelRes = li.getLabelResource();
                                        ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                                        ri.icon = li.getIconResource();
                                    }
                                    this.mList.add(new DisplayResolveInfo(ri, ri.loadLabel(HwResolverView.this.getContext().getPackageManager()), null, ii));
                                }
                            }
                        }
                    }
                    r0 = (ResolveInfo) currentResolveList.get(0);
                    int start = 0;
                    CharSequence r0Label = r0.loadLabel(HwResolverView.this.mPm);
                    HwResolverView.this.mShowExtended = false;
                    for (i = 1; i < N; i++) {
                        if (r0Label == null) {
                            r0Label = r0.activityInfo.packageName;
                        }
                        ri = (ResolveInfo) currentResolveList.get(i);
                        CharSequence riLabel = ri.loadLabel(HwResolverView.this.mPm);
                        if (riLabel == null) {
                            riLabel = ri.activityInfo.packageName;
                        }
                        if (!riLabel.equals(r0Label)) {
                            processGroup(currentResolveList, start, i - 1, r0, r0Label);
                            r0 = ri;
                            r0Label = riLabel;
                            start = i;
                        }
                    }
                    processGroup(currentResolveList, start, N - 1, r0, r0Label);
                }
            }
            if (this.mList.size() > 60) {
                this.mList = this.mList.subList(0, 60);
            }
            GalleryLog.d("HwResolverView", "using LRU " + HwResolverView.this.mLruApplications.size());
            List<DisplayResolveInfo> currentList = new ArrayList(this.mList);
            for (int index = HwResolverView.this.mLruApplications.size() - 1; index >= 0; index--) {
                String lruCompName = (String) HwResolverView.this.mLruApplications.get(index);
                for (int iindex = 0; iindex < currentList.size(); iindex++) {
                    DisplayResolveInfo dri = (DisplayResolveInfo) currentList.get(iindex);
                    if (dri.ri.activityInfo.name.equals(lruCompName)) {
                        this.mList.remove(dri);
                        this.mList.add(0, dri);
                    }
                }
            }
            HwResolverView.this.mPreviligedApp.clear();
            for (DisplayResolveInfo item : currentList) {
                if (GalleryUtils.isPrivilegedApp(item.ri.activityInfo.name)) {
                    this.mList.remove(item);
                    HwResolverView.this.mPreviligedApp.add(item);
                }
            }
        }

        private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro, CharSequence roLabel) {
            if ((end - start) + 1 == 1) {
                if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(ro.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(ro.activityInfo.name)) {
                    this.mInitialHighlight = this.mList.size();
                }
                this.mList.add(new DisplayResolveInfo(ro, roLabel, null, null));
                return;
            }
            HwResolverView.this.mShowExtended = true;
            boolean usePkg = false;
            CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(HwResolverView.this.mPm);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                HashSet<CharSequence> duplicates = new HashSet();
                duplicates.add(startApp);
                int j = start + 1;
                while (j <= end) {
                    CharSequence jApp = ((ResolveInfo) rList.get(j)).activityInfo.applicationInfo.loadLabel(HwResolverView.this.mPm);
                    if (jApp == null || duplicates.contains(jApp)) {
                        usePkg = true;
                        break;
                    } else {
                        duplicates.add(jApp);
                        j++;
                    }
                }
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                ResolveInfo add = (ResolveInfo) rList.get(k);
                if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(add.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(add.activityInfo.name)) {
                    this.mInitialHighlight = this.mList.size();
                }
                if (usePkg) {
                    this.mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.packageName, null));
                } else {
                    this.mList.add(new DisplayResolveInfo(add, roLabel, add.activityInfo.applicationInfo.loadLabel(HwResolverView.this.mPm), null));
                }
            }
        }

        public DisplayResolveInfo displayResolveInfoForPosition(int position) {
            if (position >= this.mList.size()) {
                GalleryLog.e("HwResolverView", "intentInfoForPosition() has IndexOutOfBoundsException -> position : " + position);
                position = 0;
            }
            return (DisplayResolveInfo) this.mList.get(position);
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int position) {
            return this.mList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        List<DisplayResolveInfo> getList() {
            return this.mList;
        }
    }

    static class ViewHolder {
        public ImageView icon;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.text = (TextView) view.findViewById(16908308);
            this.text2 = (TextView) view.findViewById(16908309);
            this.icon = (ImageView) view.findViewById(R.id.icon);
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {
        HashMap<Integer, GridView> mGridViewHash;

        private ViewPagerAdapter() {
            this.mGridViewHash = new HashMap();
        }

        public void destroyItem(View container, int position, Object obj) {
            if (container instanceof ViewPager) {
                ((ViewPager) container).removeView((View) obj);
            }
        }

        public void finishUpdate(View container) {
        }

        public int getCount() {
            return HwResolverView.this.mScreenCount;
        }

        public Object instantiateItem(View container, int position) {
            if (!(container instanceof ViewPager)) {
                return null;
            }
            if (this.mGridViewHash.get(Integer.valueOf(position)) == null) {
                GridView gv = (GridView) HwResolverView.this.makeView();
                GridViewAdapter gridViewAdapter = new GridViewAdapter(HwResolverView.this.getContext(), HwResolverView.this.mAdapter.getList(), gv, position);
                gv.setAdapter(gridViewAdapter);
                gv.setOnItemClickListener(HwResolverView.this);
                gv.setOnItemLongClickListener(new ItemLongClickListener(gridViewAdapter));
                gv.setChoiceMode(1);
                gv.setNumColumns(Math.min(HwResolverView.this.mAdapter.getCount(), 3));
                ((ViewPager) container).addView(gv);
                this.mGridViewHash.put(Integer.valueOf(position), gv);
                return gv;
            }
            ((ViewPager) container).addView((View) this.mGridViewHash.get(Integer.valueOf(position)));
            return this.mGridViewHash.get(Integer.valueOf(position));
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        public Parcelable saveState() {
            return null;
        }

        public void startUpdate(View container) {
        }
    }

    public void setGridViewClickListener(GridViewClickListener listener) {
        this.mListener = listener;
    }

    public void setShareClickListener(ShareClickListener shareListener) {
        this.mShareListener = shareListener;
    }

    public HwResolverView(Context context) {
        super(context);
    }

    public HwResolverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwResolverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Intent makeMyIntent(Intent i) {
        Intent intent = new Intent(i);
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & -8388609);
        return intent;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void fillView(Delegate delegate) {
        this.mDelegate = delegate;
        Intent intent = makeMyIntent(delegate.getIntent());
        this.mLaunchedFromUid = Process.myUid();
        this.mPm = getContext().getPackageManager();
        this.mIconDpi = ((ActivityManager) getContext().getSystemService("activity")).getLauncherLargeIconDensity();
        readLruApplications();
        this.mAdapter = new ResolveListAdapter(getContext(), intent, null, null, this.mLaunchedFromUid);
        setUpViewPager(this.mAdapter.getTotalCount());
    }

    private Drawable getIcon(Resources res, int resId) {
        try {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private Drawable loadIconForResolveInfo(ResolveInfo ri) {
        try {
            Drawable dr;
            if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                dr = getIcon(this.mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(this.mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            GalleryLog.e("HwResolverView", "Couldn't find resources for package." + e.getMessage());
        }
        return ri.loadIcon(this.mPm);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        DisplayResolveInfo dri = this.mAdapter.displayResolveInfoForPosition(position + (this.mCurrentScreenNum * 6));
        this.mListener.itemClicked();
        try {
            if (dri.item != null) {
                dri.item.onClicked(null);
            } else if (dri.ri == null) {
                GalleryLog.d("HwResolverView", "info is null:" + dri.displayLabel);
                GalleryUtils.dismissDialogSafely(this.mDelegate.getDialog(), null);
            } else {
                Intent intent = new Intent(dri.origIntent != null ? dri.origIntent : makeMyIntent(this.mDelegate.getIntent()));
                intent.addFlags(50331648);
                ActivityInfo ai = dri.ri.activityInfo;
                intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
                if (this.mShareListener == null || !this.mShareListener.onShareItemClicked(intent)) {
                    getContext().startActivity(intent);
                }
                updateLruApplications(dri.ri.activityInfo.name);
                GalleryUtils.dismissDialogSafely(this.mDelegate.getDialog(), null);
            }
        } finally {
            GalleryUtils.dismissDialogSafely(this.mDelegate.getDialog(), null);
        }
    }

    private void showAppDetails(ResolveInfo ri) {
        getContext().startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288));
    }

    public View makeView() {
        return View.inflate(getContext(), R.layout.grid_item_emui, null);
    }

    public int getScreenCount() {
        return this.mScreenCount;
    }

    private void setUpViewPager(int itemCount) {
        this.mScreenCount = (int) Math.ceil(((double) itemCount) / 6.0d);
        this.mViewPagerAdapter = new ViewPagerAdapter();
        this.mViewPager = (ViewPager) findViewById(R.id.view_pager);
        this.mViewPager.setAdapter(this.mViewPagerAdapter);
        this.mViewPager.setCurrentItem(0);
        this.mViewPager.setPageMargin(0);
        this.mViewPager.setOffscreenPageLimit(10);
        GalleryLog.d("HwResolverView", "setUpViewPager, set current item: 0");
        GalleryLog.d("HwResolverView", "setUpViewPager, mViewPagerAdapter count: " + this.mViewPagerAdapter.getCount());
        this.mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                HwResolverView.this.mQuickNavigation.setCurrentScreen(position);
                HwResolverView.this.mQuickNavigation.invalidate();
                HwResolverView.this.mViewPager.setCurrentItem(position);
                HwResolverView.this.mCurrentScreenNum = position;
                GalleryLog.d("HwResolverView", "onPageSelected, mViewPagerAdapter count: " + HwResolverView.this.mViewPagerAdapter.getCount());
                GalleryLog.d("HwResolverView", "onPageSelected, set current item: " + position);
            }

            public void onPageScrollStateChanged(int state) {
                switch (state) {
                }
            }
        });
        ViewTreeObserver vto = this.mViewPager.getViewTreeObserver();
        if (vto != null) {
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (HwResolverView.this.mDidFirstLayout) {
                        HwResolverView.this.mInitialScreenNum = (HwResolverView.this.mSavedSelectedItem != -1 ? HwResolverView.this.mSavedSelectedItem : HwResolverView.this.mAdapter.getInitialHighlight()) / 6;
                        HwResolverView.this.mViewPager.setCurrentItem(HwResolverView.this.mInitialScreenNum);
                        GalleryLog.d("HwResolverView", "onGlobalLayout, mViewPagerAdapter count: " + HwResolverView.this.mViewPagerAdapter.getCount());
                        GalleryLog.d("HwResolverView", "onGlobalLayout, set current item: " + HwResolverView.this.mInitialScreenNum);
                        HwResolverView.this.mDidFirstLayout = false;
                    }
                }
            });
        }
        this.mQuickNavigation = (QuickNavigation) findViewById(R.id.navigation_view);
        this.mQuickNavigation.setResolverActivity(this);
        this.mQuickNavigation.setDirection(this.mViewPager.getLayoutDirection());
        this.mQuickNavigation.setVisibility(this.mScreenCount > 1 ? 0 : 8);
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (HwResolverView.this.mListener != null) {
                    HwResolverView.this.mListener.onClickCancel();
                }
                GalleryUtils.dismissDialogSafely(HwResolverView.this.mDelegate.getDialog(), null);
            }
        });
    }

    private final void updateLruApplications(String app) {
        try {
            ContentValues values = new ContentValues();
            values.put("history-info", app);
            getContext().getContentResolver().insert(INSERT, values);
        } catch (Exception e) {
            GalleryLog.w("HwResolverView", "insert history info[" + app + "] error !!! " + INSERT + "." + e.getMessage());
        }
    }

    private final void readLruApplications() {
        if (this.mLruApplications.size() <= 0) {
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(QUERY, null, null, null, null);
                if (cursor == null) {
                    GalleryLog.w("HwResolverView", "Can't find resolver. " + QUERY);
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            GalleryLog.w("HwResolverView", "Close cursor error. " + e.getMessage());
                        }
                    }
                    return;
                }
                while (cursor.moveToNext()) {
                    this.mLruApplications.add(cursor.getString(0));
                }
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e2) {
                        GalleryLog.w("HwResolverView", "Close cursor error. " + e2.getMessage());
                    }
                }
            } catch (Exception e22) {
                GalleryLog.w("HwResolverView", "get data from cursor error. " + QUERY + "." + e22.getMessage());
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e222) {
                        GalleryLog.w("HwResolverView", "Close cursor error. " + e222.getMessage());
                    }
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e2222) {
                        GalleryLog.w("HwResolverView", "Close cursor error. " + e2222.getMessage());
                    }
                }
            }
        }
    }
}
