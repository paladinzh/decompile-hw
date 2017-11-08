package com.huawei.systemmanager.netassistant.traffic.trafficranking;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.appdetail.AppDetailActivity;
import com.huawei.systemmanager.netassistant.traffic.appdetail.Constant;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.control.TrafficRankingManager;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.AbsTrafficAppInfo;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.DayTrafficInfoFactory;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.MonthTrafficInfoFactory;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.entry.WeeklyTrafficInfoFactory;
import java.util.Collections;
import java.util.List;

public class TrafficRankingFragment extends ListFragment implements LoaderCallbacks<List<AbsTrafficAppInfo>> {
    public static final String EXTRA_TRAFFIC_PERIOD = "extra_app_period";
    private static final String TAG = TrafficRankingFragment.class.getSimpleName();
    private CommonAdapter<AbsTrafficAppInfo> mAdaptar;
    private View mEmptyView;
    private String mImsi;
    private int mPeriodType;

    public static class RankingListAdapter extends CommonAdapter<AbsTrafficAppInfo> {
        private static final int TYPE_MORE_APP = 0;
        private static final int TYPE_ONE_APP = 1;
        Context ctx;
        boolean isMobile;

        public RankingListAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
            this.ctx = context;
        }

        public RankingListAdapter(Context context, LayoutInflater inflater, boolean mobile) {
            super(context, inflater);
            this.ctx = context;
            this.isMobile = mobile;
        }

        public int getItemViewType(int position) {
            if (((AbsTrafficAppInfo) getItem(position)).isMultiApp()) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 2;
        }

        protected View newView(int position, ViewGroup parent, AbsTrafficAppInfo item) {
            ViewHolder vh = new ViewHolder();
            View view = this.mInflater.inflate(R.layout.traffic_ranking_list_item, null);
            vh.icon = (ImageView) view.findViewById(R.id.icon);
            vh.title = (TextView) view.findViewById(R.id.title);
            vh.mobileTxt = (TextView) view.findViewById(R.id.mobile_data);
            vh.wifiTxt = (TextView) view.findViewById(R.id.wifi_data);
            vh.moreAppTxt = (TextView) view.findViewById(R.id.more_app);
            vh.noTrafficAppTxt = (TextView) view.findViewById(R.id.no_traffic_app);
            view.setTag(vh);
            return view;
        }

        protected void bindView(int position, View view, AbsTrafficAppInfo item) {
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.icon.setImageDrawable(item.getIcon());
            vh.title.setText(item.getLabel());
            if (this.isMobile) {
                vh.wifiTxt.setVisibility(8);
            } else {
                vh.mobileTxt.setVisibility(8);
            }
            vh.mobileTxt.setText(CommonMethodUtil.formatBytes(this.ctx, item.getMobileTraffic()));
            vh.wifiTxt.setText(CommonMethodUtil.formatBytes(this.ctx, item.getWifiTraffic()));
            if (item.isChecked()) {
                vh.noTrafficAppTxt.setText(R.string.no_traffic_app);
            } else {
                vh.noTrafficAppTxt.setText("");
            }
            if (getItemViewType(position) == 0) {
                vh.moreAppTxt.setText(R.string.net_assistant_more_application);
            }
        }
    }

    public static class RankingListTask extends AsyncTaskLoader<List<AbsTrafficAppInfo>> {
        List<AbsTrafficAppInfo> mApps;
        String mImsi;
        int mPeriod;

        public RankingListTask(Context context, String imsi, int periodType) {
            super(context);
            this.mImsi = imsi;
            this.mPeriod = periodType;
        }

        public List<AbsTrafficAppInfo> loadInBackground() {
            List<AbsTrafficAppInfo> list = Lists.newArrayList();
            if (this.mImsi != null) {
                if (this.mPeriod == 2) {
                    list = TrafficRankingManager.getDefault().getDailyTrafficInfoList(new DayTrafficInfoFactory(), this.mImsi, this.mPeriod);
                } else if (this.mPeriod == 3) {
                    list = TrafficRankingManager.getDefault().getWeeklyTrafficInfoList(new WeeklyTrafficInfoFactory(), this.mImsi, this.mPeriod);
                } else if (this.mPeriod == 1) {
                    list = TrafficRankingManager.getDefault().getMonthTrafficInfoList(new MonthTrafficInfoFactory(), this.mImsi, this.mPeriod);
                }
                TrafficRankingManager.getDefault().initNoTrafficApp(list, this.mImsi);
                Collections.sort(list, AbsTrafficAppInfo.NETASSISTANT_TRAFFIC_RANKING_COMPARATOR);
                Collections.sort(list, AbsTrafficAppInfo.TRAFFIC_RANKING_MOBILE_COMPARATOR);
            } else {
                if (this.mPeriod == 2) {
                    list = TrafficRankingManager.getDefault().getDailyTrafficInfoList(new DayTrafficInfoFactory(), this.mPeriod);
                } else if (this.mPeriod == 3) {
                    list = TrafficRankingManager.getDefault().getWeeklyTrafficInfoList(new WeeklyTrafficInfoFactory(), this.mPeriod);
                } else if (this.mPeriod == 1) {
                    list = TrafficRankingManager.getDefault().getMonthTrafficInfoList(new MonthTrafficInfoFactory(), this.mPeriod);
                }
                Collections.sort(list, AbsTrafficAppInfo.NETASSISTANT_TRAFFIC_RANKING_COMPARATOR);
                Collections.sort(list, AbsTrafficAppInfo.TRAFFIC_RANKING_WIFI_COMPARATOR);
            }
            return list;
        }

        public void deliverResult(List<AbsTrafficAppInfo> apps) {
            if (isReset() && apps != null) {
                onReleaseResources(apps);
            }
            List<AbsTrafficAppInfo> oldApps = apps;
            this.mApps = apps;
            if (isStarted()) {
                super.deliverResult(apps);
            }
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        protected void onStartLoading() {
            if (this.mApps != null) {
                deliverResult(this.mApps);
            }
            if (takeContentChanged() || this.mApps == null) {
                forceLoad();
            }
        }

        protected void onStopLoading() {
            cancelLoad();
        }

        public void onCanceled(List<AbsTrafficAppInfo> apps) {
            super.onCanceled(apps);
            onReleaseResources(apps);
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            if (this.mApps != null) {
                onReleaseResources(this.mApps);
                this.mApps = null;
            }
        }

        protected void onReleaseResources(List<AbsTrafficAppInfo> list) {
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView mobileTxt;
        TextView moreAppTxt;
        TextView noTrafficAppTxt;
        TextView title;
        TextView wifiTxt;

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        this.mImsi = bundle.getString(CommonConstantUtil.KEY_NETASSISTANT_IMSI);
        this.mAdaptar = new RankingListAdapter(getActivity(), getActivity().getLayoutInflater(), this.mImsi != null);
        this.mPeriodType = bundle.getInt(EXTRA_TRAFFIC_PERIOD);
        setListAdapter(this.mAdaptar);
    }

    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(this.mPeriodType, getArguments(), this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_ranking_fragment, container, false);
        this.mEmptyView = view.findViewById(R.id.empty_view);
        return view;
    }

    public void onStop() {
        super.onStop();
        getLoaderManager().destroyLoader(this.mPeriodType);
    }

    public Loader<List<AbsTrafficAppInfo>> onCreateLoader(int id, Bundle args) {
        return new RankingListTask(getActivity(), this.mImsi, this.mPeriodType);
    }

    public void onLoadFinished(Loader<List<AbsTrafficAppInfo>> loader, List<AbsTrafficAppInfo> arg1) {
        if (arg1.size() == 0) {
            this.mEmptyView.setVisibility(0);
            ViewUtil.initEmptyViewMargin(GlobalContext.getContext(), this.mEmptyView);
        } else {
            this.mEmptyView.setVisibility(8);
        }
        this.mAdaptar.swapData(arg1);
    }

    public void onLoaderReset(Loader<List<AbsTrafficAppInfo>> loader) {
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        AbsTrafficAppInfo absNetAppInfo = (AbsTrafficAppInfo) l.getItemAtPosition(position);
        if (absNetAppInfo != null) {
            Intent intent = new Intent();
            intent.putExtra(Constant.EXTRA_ACTIVITY_FROM, 0);
            intent.putExtra(Constant.EXTRA_APP_LABEL, absNetAppInfo.getLabel());
            intent.putExtra("uid", absNetAppInfo.getUid());
            intent.putExtra(Constant.EXTRA_IMSI, this.mImsi);
            intent.setClass(GlobalContext.getContext(), AppDetailActivity.class);
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_ID, String.valueOf(absNetAppInfo.getLabel()));
            HsmStat.statE((int) Events.E_NETASSISTANT_APP_DETAIL, statParam);
            startActivity(intent);
        }
    }
}
