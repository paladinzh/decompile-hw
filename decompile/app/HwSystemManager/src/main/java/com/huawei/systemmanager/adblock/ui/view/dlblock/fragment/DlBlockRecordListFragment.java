package com.huawei.systemmanager.adblock.ui.view.dlblock.fragment;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult.UpdateRecordOptRunnable;
import com.huawei.systemmanager.comm.component.BaseListFragment;
import com.huawei.systemmanager.comm.component.ContentLoader;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class DlBlockRecordListFragment extends BaseListFragment<AdCheckUrlResult> {
    private static final int DATA_LOADER_ID = 1;
    private static final String TAG = "DlBlockRecordListFragment";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DlBlockRecordListFragment.this.loadData();
        }
    };
    private ViewGroup mContainerView;
    private TextView mHeaderTitle;
    private boolean mIsLoading;
    private LayoutInflater mLayoutInflater;
    private View mLoadingView;

    private static class AdCheckUrlResultAdapter extends CommonAdapter<AdCheckUrlResult> {
        AdCheckUrlResultAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
        }

        protected View newView(int position, ViewGroup parent, AdCheckUrlResult item) {
            View innerView = getInflater().inflate(R.layout.common_list_item_twolines_image_switch, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.mIcon = (ImageView) innerView.findViewById(R.id.image);
            holder.mTitle = (TextView) innerView.findViewById(ViewUtil.HWID_TEXT_1);
            holder.mDescription = (TextView) innerView.findViewById(ViewUtil.HWID_TEXT_2);
            holder.mSwitch = (Switch) innerView.findViewById(R.id.switcher);
            innerView.setTag(holder);
            return innerView;
        }

        protected void bindView(int position, View view, AdCheckUrlResult item) {
            Context ctx = getContext();
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.mIcon.setImageDrawable(item.getDownloaderIcon());
            holder.mTitle.setText(getString(R.string.ad_dl_block_record_download, item.getDownloaderAppName(), item.getApkAppName(), item.getDownloaderAppName()));
            holder.mSwitch.setOnCheckedChangeListener(null);
            if (item.getOptPolicy() == 4) {
                holder.mDescription.setText(R.string.ad_dl_block_intercept);
                holder.mSwitch.setChecked(true);
            } else {
                holder.mDescription.setText(R.string.ad_dl_block_not_intercept);
                holder.mSwitch.setChecked(false);
            }
            holder.mSwitch.setOnCheckedChangeListener(new DlBlockItemOnCheckedChangeListener(ctx, item, holder));
        }
    }

    private static class AdCheckUrlResultLoader extends ContentLoader<List<AdCheckUrlResult>> {
        public AdCheckUrlResultLoader(Context context) {
            super(context);
        }

        public List<AdCheckUrlResult> loadInBackground() {
            List<AdCheckUrlResult> lists = AdCheckUrlResult.getAllDlBlockList(getContext());
            for (AdCheckUrlResult adCheckUrlResult : lists) {
                adCheckUrlResult.loadLabelAndIcon(getContext());
            }
            return lists;
        }
    }

    private static class AnimationListenerImpl implements AnimationListener {
        private View view;

        public AnimationListenerImpl(View view) {
            this.view = view;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            this.view.setVisibility(4);
        }
    }

    private static class DlBlockItemOnCheckedChangeListener implements OnCheckedChangeListener {
        Context mContext;
        AdCheckUrlResult mItem;
        ViewHolder mViewHolder;

        DlBlockItemOnCheckedChangeListener(Context context, AdCheckUrlResult item, ViewHolder viewHolder) {
            this.mContext = context;
            this.mItem = item;
            this.mViewHolder = viewHolder;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int optPolicy;
            if (isChecked) {
                optPolicy = 4;
                this.mViewHolder.mDescription.setText(R.string.ad_dl_block_intercept);
            } else {
                optPolicy = 3;
                this.mViewHolder.mDescription.setText(R.string.ad_dl_block_not_intercept);
            }
            HsmExecutor.THREAD_POOL_EXECUTOR.execute(new UpdateRecordOptRunnable(this.mContext.getApplicationContext(), this.mItem.getUidPkgName(), this.mItem.getApkPkgName(), optPolicy, true));
        }
    }

    private static class ViewHolder {
        TextView mDescription;
        ImageView mIcon;
        Switch mSwitch;
        TextView mTitle;

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        HwLog.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ActionBar ab = getActivity().getActionBar();
        if (ab != null) {
            ab.setTitle(R.string.ad_dl_block_records_title);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HwLog.e(TAG, "onCreateView");
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.dlblock_list_frame, container, false);
        this.mContainerView = (ViewGroup) rootView.findViewById(R.id.list_container);
        this.mHeaderTitle = (TextView) rootView.findViewById(R.id.dl_block_info);
        this.mLoadingView = rootView.findViewById(R.id.loading_container);
        this.mLayoutInflater = inflater;
        setLoading(true, false, true);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(this.mBroadcastReceiver, new IntentFilter(AdCheckUrlResult.DL_BLOCK_CHANGE_ACTION));
        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HwLog.i(TAG, "onViewCreated");
        ListView listView = (ListView) getListView();
        if (listView.getEmptyView() == null) {
            listView.setEmptyView((TextView) getView().findViewById(R.id.no_records));
        }
        setListAdapter(new AdCheckUrlResultAdapter(getContext(), this.mLayoutInflater));
        initLoader(1);
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        View switcher = v.findViewById(R.id.switcher);
        if (switcher != null) {
            switcher.performClick();
        }
    }

    public Loader<List<AdCheckUrlResult>> onCreateLoader(int id, Bundle args) {
        HwLog.i(TAG, "onCreateLoader");
        return new AdCheckUrlResultLoader(getContext());
    }

    public void onLoadFinished(Loader<List<AdCheckUrlResult>> loader, List<AdCheckUrlResult> data) {
        super.onLoadFinished((Loader) loader, (List) data);
        int blockRecordCount = 0;
        for (AdCheckUrlResult item : data) {
            if (item.getOptPolicy() == 4) {
                blockRecordCount++;
            }
        }
        updateHeader(blockRecordCount);
        setLoading(false, true);
    }

    public void onLoaderReset(Loader<List<AdCheckUrlResult>> loader) {
        super.onLoaderReset(loader);
        HwLog.i(TAG, "onLoaderReset");
    }

    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        HwLog.i(TAG, "loadData");
        notifyLoader(1);
    }

    public void updateHeader(int blockRecordCount) {
        if (this.mHeaderTitle != null) {
            this.mHeaderTitle.setText(getResources().getQuantityString(R.plurals.ad_dl_block_entity_message, blockRecordCount, new Object[]{Integer.valueOf(blockRecordCount)}));
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(this.mBroadcastReceiver);
    }

    private void setLoading(boolean loading, boolean animate) {
        setLoading(loading, animate, false);
    }

    private void setLoading(boolean loading, boolean animate, boolean force) {
        if (this.mIsLoading != loading || force) {
            this.mIsLoading = loading;
            if (getView() == null) {
                animate = false;
            }
            if (this.mContainerView != null) {
                setViewShown(this.mContainerView, !loading, animate);
            }
            if (this.mLoadingView != null) {
                setViewShown(this.mLoadingView, loading, animate);
            }
        }
    }

    private void setViewShown(View view, boolean shown, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), shown ? 17432576 : 17432577);
            if (shown) {
                view.setVisibility(0);
            } else if (animation != null) {
                animation.setAnimationListener(new AnimationListenerImpl(view));
            }
            view.startAnimation(animation);
            return;
        }
        view.clearAnimation();
        view.setVisibility(shown ? 0 : 4);
    }
}
