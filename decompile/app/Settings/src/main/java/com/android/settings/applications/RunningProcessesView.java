package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.BidiFormatter;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.util.MemInfoReader;
import com.android.settings.SettingsActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class RunningProcessesView extends FrameLayout implements OnItemClickListener, RecyclerListener, OnRefreshUiListener {
    long SECONDARY_SERVER_MEM;
    final HashMap<View, ActiveItem> mActiveItems = new HashMap();
    ServiceListAdapter mAdapter;
    ActivityManager mAm;
    TextView mAppsProcessPrefix;
    TextView mAppsProcessText;
    TextView mBackgroundProcessPrefix;
    TextView mBackgroundProcessText;
    StringBuilder mBuilder = new StringBuilder(128);
    LinearColorBar mColorBar;
    long mCurHighRam = -1;
    long mCurLowRam = -1;
    long mCurMedRam = -1;
    BaseItem mCurSelected;
    boolean mCurShowCached = false;
    long mCurTotalRam = -1;
    Runnable mDataAvail;
    TextView mForegroundProcessPrefix;
    TextView mForegroundProcessText;
    View mHeader;
    ListView mListView;
    MemInfoReader mMemInfoReader = new MemInfoReader();
    final int mMyUserId = UserHandle.myUserId();
    Fragment mOwner;
    RunningState mState;

    public static class ActiveItem {
        long mFirstRunTime;
        ViewHolder mHolder;
        BaseItem mItem;
        View mRootView;
        boolean mSetBackground;

        void updateTime(Context context, StringBuilder builder) {
            TextView uptimeView = null;
            if (this.mItem instanceof ServiceItem) {
                uptimeView = this.mHolder.size;
            } else {
                String size = this.mItem.mSizeStr != null ? this.mItem.mSizeStr : "";
                if (!size.equals(this.mItem.mCurSizeStr)) {
                    this.mItem.mCurSizeStr = size;
                    this.mHolder.size.setText(size);
                }
                if (this.mItem.mBackground) {
                    if (!this.mSetBackground) {
                        this.mSetBackground = true;
                        this.mHolder.uptime.setText("");
                    }
                } else if (this.mItem instanceof MergedItem) {
                    uptimeView = this.mHolder.uptime;
                }
            }
            if (uptimeView != null) {
                this.mSetBackground = false;
                if (this.mFirstRunTime >= 0) {
                    uptimeView.setText(DateUtils.formatElapsedTime(builder, (SystemClock.elapsedRealtime() - this.mFirstRunTime) / 1000));
                    return;
                }
                boolean isService = false;
                if (this.mItem instanceof MergedItem) {
                    isService = ((MergedItem) this.mItem).mServices.size() > 0;
                }
                if (isService) {
                    uptimeView.setText(context.getResources().getText(2131625709));
                } else {
                    uptimeView.setText("");
                }
            }
        }
    }

    class ServiceListAdapter extends BaseAdapter {
        final LayoutInflater mInflater;
        final ArrayList<MergedItem> mItems = new ArrayList();
        ArrayList<MergedItem> mOrigItems;
        boolean mShowBackground;
        final RunningState mState;

        ServiceListAdapter(RunningState state) {
            this.mState = state;
            this.mInflater = (LayoutInflater) RunningProcessesView.this.getContext().getSystemService("layout_inflater");
            refreshItems();
        }

        void setShowBackground(boolean showBackground) {
            if (this.mShowBackground != showBackground) {
                this.mShowBackground = showBackground;
                this.mState.setWatchingBackgroundItems(showBackground);
                refreshItems();
                RunningProcessesView.this.refreshUi(true);
            }
        }

        boolean getShowBackground() {
            return this.mShowBackground;
        }

        void refreshItems() {
            ArrayList<MergedItem> newItems;
            if (this.mShowBackground) {
                newItems = this.mState.getCurrentBackgroundItems();
            } else {
                newItems = this.mState.getCurrentMergedItems();
            }
            if (this.mOrigItems != newItems) {
                this.mOrigItems = newItems;
                if (newItems == null) {
                    this.mItems.clear();
                    return;
                }
                this.mItems.clear();
                this.mItems.addAll(newItems);
                if (this.mShowBackground) {
                    Collections.sort(this.mItems, this.mState.mBackgroundComparator);
                }
            }
        }

        public boolean hasStableIds() {
            return true;
        }

        public int getCount() {
            return this.mItems.size();
        }

        public boolean isEmpty() {
            return this.mState.hasData() && this.mItems.size() == 0;
        }

        public Object getItem(int position) {
            if (position >= this.mItems.size()) {
                return null;
            }
            return this.mItems.get(position);
        }

        public long getItemId(int position) {
            if (position >= this.mItems.size()) {
                return 0;
            }
            return (long) ((MergedItem) this.mItems.get(position)).hashCode();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            if (position >= this.mItems.size()) {
                return false;
            }
            return !((MergedItem) this.mItems.get(position)).mIsProcess;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = newView(parent);
            } else {
                v = convertView;
            }
            bindView(v, position);
            return v;
        }

        public View newView(ViewGroup parent) {
            View v = this.mInflater.inflate(2130969068, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return v;
        }

        public void bindView(View view, int position) {
            synchronized (this.mState.mLock) {
                if (position >= this.mItems.size()) {
                    return;
                }
                RunningProcessesView.this.mActiveItems.put(view, ((ViewHolder) view.getTag()).bind(this.mState, (MergedItem) this.mItems.get(position), RunningProcessesView.this.mBuilder));
            }
        }
    }

    static class TimeTicker extends TextView {
    }

    public static class ViewHolder {
        public TextView description;
        public ImageView icon;
        public TextView name;
        public View rootView;
        public TextView size;
        public TextView uptime;

        public ViewHolder(View v) {
            this.rootView = v;
            this.icon = (ImageView) v.findViewById(2131886147);
            this.name = (TextView) v.findViewById(2131886300);
            this.description = (TextView) v.findViewById(2131886500);
            this.size = (TextView) v.findViewById(2131887097);
            this.uptime = (TextView) v.findViewById(2131886295);
            v.setTag(this);
        }

        public ActiveItem bind(RunningState state, BaseItem item, StringBuilder builder) {
            ActiveItem ai;
            synchronized (state.mLock) {
                PackageManager pm = this.rootView.getContext().getPackageManager();
                if (item.mPackageInfo == null && (item instanceof MergedItem) && ((MergedItem) item).mProcess != null) {
                    ((MergedItem) item).mProcess.ensureLabel(pm);
                    item.mPackageInfo = ((MergedItem) item).mProcess.mPackageInfo;
                    item.mDisplayLabel = ((MergedItem) item).mProcess.mDisplayLabel;
                }
                this.name.setText(item.mDisplayLabel);
                ai = new ActiveItem();
                ai.mRootView = this.rootView;
                ai.mItem = item;
                ai.mHolder = this;
                ai.mFirstRunTime = item.mActiveSince;
                if (item.mBackground) {
                    this.description.setText(this.rootView.getContext().getText(2131625710));
                } else {
                    this.description.setText(item.mDescription);
                }
                item.mCurSizeStr = null;
                this.icon.setImageDrawable(item.loadIcon(this.rootView.getContext(), state));
                this.icon.setVisibility(0);
                ai.updateTime(this.rootView.getContext(), builder);
            }
            return ai;
        }
    }

    void refreshUi(boolean dataChanged) {
        if (dataChanged) {
            ServiceListAdapter adapter = this.mAdapter;
            adapter.refreshItems();
            adapter.notifyDataSetChanged();
        }
        if (this.mDataAvail != null) {
            this.mDataAvail.run();
            this.mDataAvail = null;
        }
        this.mMemInfoReader.readMemInfo();
        synchronized (this.mState.mLock) {
            long lowRam;
            long medRam;
            if (this.mCurShowCached != this.mAdapter.mShowBackground) {
                this.mCurShowCached = this.mAdapter.mShowBackground;
                if (this.mCurShowCached) {
                    this.mForegroundProcessPrefix.setText(getResources().getText(2131625728));
                    this.mAppsProcessPrefix.setText(getResources().getText(2131625729));
                } else {
                    this.mForegroundProcessPrefix.setText(getResources().getText(2131625725));
                    this.mAppsProcessPrefix.setText(getResources().getText(2131625726));
                }
            }
            long totalRam = this.mMemInfoReader.getTotalSize();
            if (this.mCurShowCached) {
                lowRam = this.mMemInfoReader.getFreeSize() + this.mMemInfoReader.getCachedSize();
                medRam = this.mState.mBackgroundProcessMemory;
            } else {
                lowRam = (this.mMemInfoReader.getFreeSize() + this.mMemInfoReader.getCachedSize()) + this.mState.mBackgroundProcessMemory;
                medRam = this.mState.mServiceProcessMemory;
            }
            long highRam = (totalRam - medRam) - lowRam;
            if (this.mCurTotalRam == totalRam && this.mCurHighRam == highRam && this.mCurMedRam == medRam) {
                if (this.mCurLowRam != lowRam) {
                }
            }
            this.mCurTotalRam = totalRam;
            this.mCurHighRam = highRam;
            this.mCurMedRam = medRam;
            this.mCurLowRam = lowRam;
            BidiFormatter bidiFormatter = BidiFormatter.getInstance();
            String sizeStr = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), lowRam));
            this.mBackgroundProcessText.setText(getResources().getString(2131625730, new Object[]{sizeStr}));
            sizeStr = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), medRam));
            this.mAppsProcessText.setText(getResources().getString(2131625730, new Object[]{sizeStr}));
            sizeStr = bidiFormatter.unicodeWrap(Formatter.formatShortFileSize(getContext(), highRam));
            this.mForegroundProcessText.setText(getResources().getString(2131625730, new Object[]{sizeStr}));
            this.mColorBar.setRatios(((float) highRam) / ((float) totalRam), ((float) medRam) / ((float) totalRam), ((float) lowRam) / ((float) totalRam));
        }
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        MergedItem mi = (MergedItem) ((ListView) parent).getAdapter().getItem(position);
        this.mCurSelected = mi;
        startServiceDetailsActivity(mi);
    }

    private void startServiceDetailsActivity(MergedItem mi) {
        if (this.mOwner != null && mi != null) {
            Bundle args = new Bundle();
            if (mi.mProcess != null) {
                args.putInt("uid", mi.mProcess.mUid);
                args.putString("process", mi.mProcess.mProcessName);
            }
            args.putInt("user_id", mi.mUserId);
            args.putBoolean("background", this.mAdapter.mShowBackground);
            ((SettingsActivity) this.mOwner.getActivity()).startPreferencePanel(RunningServiceDetails.class.getName(), args, 2131625731, null, null, 0);
        }
    }

    public void onMovedToScrapHeap(View view) {
        this.mActiveItems.remove(view);
    }

    public RunningProcessesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void doCreate() {
        this.mAm = (ActivityManager) getContext().getSystemService("activity");
        this.mState = RunningState.getInstance(getContext());
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        inflater.inflate(2130969069, this);
        this.mListView = (ListView) findViewById(16908298);
        View emptyView = findViewById(16908292);
        if (emptyView != null) {
            this.mListView.setEmptyView(emptyView);
        }
        this.mListView.setDivider(getResources().getDrawable(2130838530));
        this.mListView.setHeaderDividersEnabled(false);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setRecyclerListener(this);
        View footerView = inflater.inflate(2130968937, this.mListView, false);
        if (footerView != null) {
            this.mListView.addFooterView(footerView, null, false);
            this.mListView.setFooterDividersEnabled(true);
        }
        this.mAdapter = new ServiceListAdapter(this.mState);
        this.mListView.setAdapter(this.mAdapter);
        this.mHeader = inflater.inflate(2130969066, null);
        this.mListView.addHeaderView(this.mHeader, null, false);
        this.mColorBar = (LinearColorBar) this.mHeader.findViewById(2131887014);
        Context context = getContext();
        this.mColorBar.setColors(context.getColor(2131427453), context.getColor(2131427454), context.getColor(2131427455));
        this.mBackgroundProcessPrefix = (TextView) this.mHeader.findViewById(2131887094);
        this.mAppsProcessPrefix = (TextView) this.mHeader.findViewById(2131887092);
        this.mForegroundProcessPrefix = (TextView) this.mHeader.findViewById(2131887090);
        this.mBackgroundProcessText = (TextView) this.mHeader.findViewById(2131887095);
        this.mAppsProcessText = (TextView) this.mHeader.findViewById(2131887093);
        this.mForegroundProcessText = (TextView) this.mHeader.findViewById(2131887091);
        MemoryInfo memInfo = new MemoryInfo();
        this.mAm.getMemoryInfo(memInfo);
        this.SECONDARY_SERVER_MEM = memInfo.secondaryServerThreshold;
    }

    public void doPause() {
        this.mState.pause();
        this.mDataAvail = null;
        this.mOwner = null;
    }

    public boolean doResume(Fragment owner, Runnable dataAvail) {
        this.mOwner = owner;
        this.mState.resume(this);
        if (this.mState.hasData()) {
            refreshUi(true);
            return true;
        }
        this.mDataAvail = dataAvail;
        return false;
    }

    void updateTimes() {
        Iterator<ActiveItem> it = this.mActiveItems.values().iterator();
        while (it.hasNext()) {
            ActiveItem ai = (ActiveItem) it.next();
            if (ai.mRootView.getWindowToken() == null) {
                it.remove();
            } else {
                ai.updateTime(getContext(), this.mBuilder);
            }
        }
    }

    public void onRefreshUi(int what) {
        switch (what) {
            case 0:
                updateTimes();
                return;
            case 1:
                refreshUi(false);
                updateTimes();
                return;
            case 2:
                refreshUi(true);
                updateTimes();
                return;
            default:
                return;
        }
    }
}
