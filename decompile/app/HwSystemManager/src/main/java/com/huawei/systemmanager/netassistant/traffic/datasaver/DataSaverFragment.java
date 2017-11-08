package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.View;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingTrafficListActivity;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.lang.ref.WeakReference;
import java.util.List;

public class DataSaverFragment extends ListFragment implements LoaderCallbacks<List<DataSaverEntry>>, View {
    private static final int MSG_NOTIFY_DATASET_ACTION = 201;
    private static final int MSG_NOTIFY_DATASET_DELAY_TIME = 20;
    private DataSaverAdapter mAdapter;
    private int mAppType;
    private DataSaverManager mDataSaverManager;
    private android.view.View mEmptyView;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 201:
                    DataSaverFragment.this.mAdapter.notifyDataSetChanged();
                    return;
                default:
                    return;
            }
        }
    };
    private PackageManager mPackageManager;
    private ProgressDialog progressDialog;

    class BitmapWorkerTask extends AsyncTask<Object, Void, Drawable> {
        private final WeakReference<ImageView> imageViewReference;
        private int uid = 0;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Drawable doInBackground(Object... params) {
            this.uid = ((Integer) params[0]).intValue();
            String packageName = params[1];
            String[] pkgs = DataSaverFragment.this.mPackageManager.getPackagesForUid(this.uid);
            if (pkgs != null && pkgs.length > 0) {
                for (String pkg : pkgs) {
                    if (pkg.equals(packageName)) {
                        return HsmPackageManager.getInstance().getIcon(pkg);
                    }
                }
            }
            return HsmPackageManager.getInstance().getDefaultIcon();
        }

        protected void onPostExecute(Drawable bitmap) {
            if (bitmap != null) {
                ImageView imageView = (ImageView) this.imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageDrawable(bitmap);
                }
            }
        }
    }

    public class DataSaverAdapter extends CommonAdapter<DataSaverEntry> {
        public DataSaverAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
        }

        protected android.view.View newView(int position, ViewGroup parent, DataSaverEntry item) {
            ViewHolder vh = new ViewHolder();
            android.view.View view = this.mInflater.inflate(R.layout.common_list_item_twolines_image_switch, null);
            vh.icon = (ImageView) view.findViewById(R.id.image);
            vh.title = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            vh.title.setSingleLine(false);
            vh.summary = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
            vh.aSwitch = (Switch) view.findViewById(R.id.switcher);
            vh.aSwitch.setClickable(false);
            view.setTag(vh);
            return view;
        }

        protected void bindView(int position, android.view.View view, DataSaverEntry item) {
            ViewHolder vh = (ViewHolder) view.getTag();
            if (vh != null && vh.title != null && vh.summary != null && vh.aSwitch != null) {
                vh.title.setText(item.title);
                vh.summary.setText(item.summary);
                vh.aSwitch.setChecked(item.isWhiteListed);
                if (item.isProtectListed) {
                    if (item.isBlackListed) {
                        vh.summary.setVisibility(0);
                        vh.summary.setText(getString(R.string.data_saver_restrict_background_blacklisted_summary));
                        vh.aSwitch.setEnabled(false);
                    } else {
                        vh.summary.setVisibility(0);
                        vh.summary.setText(getString(R.string.data_saver_restrict_background_protected_summary));
                        vh.aSwitch.setEnabled(true);
                    }
                } else if (item.isBlackListed) {
                    vh.summary.setVisibility(0);
                    vh.summary.setText(getString(R.string.data_saver_restrict_background_blacklisted_summary));
                    vh.aSwitch.setEnabled(false);
                } else {
                    vh.summary.setVisibility(8);
                    vh.summary.setText("");
                    vh.aSwitch.setEnabled(true);
                }
                new BitmapWorkerTask(vh.icon).execute(new Object[]{Integer.valueOf(item.uid), item.pkgName});
            }
        }
    }

    public static class DataSaverLoader extends AsyncTaskLoader<List<DataSaverEntry>> {
        private final int mAppType;
        List<DataSaverEntry> mApps;
        private DataSaverDataCenter mDataCenter;

        public DataSaverLoader(Context context, int appType, DataSaverDataCenter dataCenter) {
            super(context);
            this.mAppType = appType;
            this.mDataCenter = dataCenter;
        }

        public List<DataSaverEntry> loadInBackground() {
            return this.mDataCenter.getList(this.mAppType);
        }

        public void deliverResult(List<DataSaverEntry> apps) {
            if (isReset() && apps != null) {
                onReleaseResources(apps);
            }
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

        public void onCanceled(List<DataSaverEntry> apps) {
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

        private void onReleaseResources(List<DataSaverEntry> list) {
        }
    }

    static class ViewHolder {
        Switch aSwitch;
        ImageView icon;
        TextView summary;
        TextView title;

        ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPackageManager = GlobalContext.getContext().getPackageManager();
        this.mAdapter = new DataSaverAdapter(getActivity(), getActivity().getLayoutInflater());
        setListAdapter(this.mAdapter);
        this.mDataSaverManager = new DataSaverManager(getContext(), this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mAppType = bundle.getInt("app_type");
        }
    }

    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.data_saver_fragment, container, false);
        this.mEmptyView = view.findViewById(R.id.empty_view);
        return view;
    }

    public void onResume() {
        super.onResume();
        this.mDataSaverManager.registerListener();
        getLoaderManager().restartLoader(this.mAppType, getArguments(), this);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mDataSaverManager.release();
        this.mAdapter.clear();
        this.mHandler.removeCallbacksAndMessages(null);
    }

    public void onPause() {
        super.onPause();
        this.mDataSaverManager.unRegisterListener();
        getLoaderManager().destroyLoader(this.mAppType);
    }

    public Loader<List<DataSaverEntry>> onCreateLoader(int id, Bundle args) {
        if (this.mAppType == 0) {
            showLoadingDialog();
        }
        return new DataSaverLoader(getActivity(), this.mAppType, this.mDataSaverManager.getDataCenter());
    }

    public void onLoadFinished(Loader<List<DataSaverEntry>> loader, List<DataSaverEntry> data) {
        if (this.mAppType == 0) {
            dismissLoadingDialog();
        }
        if (data.size() == 0) {
            this.mEmptyView.setVisibility(0);
        } else {
            this.mEmptyView.setVisibility(8);
        }
        this.mAdapter.swapData(data);
    }

    public void onLoaderReset(Loader<List<DataSaverEntry>> loader) {
    }

    public void onWhiteListedChanged(int uid, boolean whitelisted) {
        List<DataSaverEntry> data = this.mAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            DataSaverEntry entry = (DataSaverEntry) data.get(i);
            if (entry.uid == uid) {
                entry.isWhiteListed = whitelisted;
            }
        }
        notifyChangeSendToRunUithread();
    }

    public void showLoadingDialog() {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(getContext());
            this.progressDialog.setMessage(getContext().getString(R.string.data_usage_restrict_background_wait));
            this.progressDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        try {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public void onBlacklistedChanged(int uid, boolean blackListed) {
        List<DataSaverEntry> data = this.mAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            DataSaverEntry entry = (DataSaverEntry) data.get(i);
            if (entry.uid == uid) {
                entry.isBlackListed = blackListed;
            }
        }
        notifyChangeSendToRunUithread();
    }

    public void onDataSaverStateChanged(boolean enable) {
    }

    public void onListItemClick(ListView l, android.view.View v, int position, long id) {
        DataSaverEntry entry = (DataSaverEntry) this.mAdapter.getItem(position);
        if (entry.isBlackListed) {
            enterRoamingTrafficListPage(position);
        } else {
            updateSwitchViewAndWhiteList(v, entry);
        }
    }

    private void enterRoamingTrafficListPage(int position) {
        Intent intent = new Intent(getActivity(), RoamingTrafficListActivity.class);
        intent.setAction(DataSaverConstants.ACTION_FROM_DATA_SAVER);
        intent.putExtra(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID, ((DataSaverEntry) this.mAdapter.getItem(position)).uid);
        intent.putExtra("app_type", this.mAppType);
        startActivity(intent);
    }

    private void updateSwitchViewAndWhiteList(android.view.View v, DataSaverEntry entry) {
        boolean z;
        DataSaverManager dataSaverManager = this.mDataSaverManager;
        if (entry.isWhiteListed) {
            z = false;
        } else {
            z = true;
        }
        dataSaverManager.setWhiteListed(z, entry.uid, entry.pkgName);
        String[] strArr = new String[4];
        strArr[0] = HsmStatConst.PARAM_PKG;
        strArr[1] = entry.pkgName;
        strArr[2] = HsmStatConst.PARAM_VAL;
        strArr[3] = !entry.isWhiteListed ? "1" : "0";
        HsmStat.statE((int) Events.E_DATA_SAVER_UNRESTRICT_APP_SELECTING_CLICKED, HsmStatConst.constructJsonParams(strArr));
    }

    private void notifyChangeSendToRunUithread() {
        this.mHandler.removeMessages(201);
        this.mHandler.sendEmptyMessageDelayed(201, 20);
    }
}
