package com.huawei.netassistant.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.common.PhoneSimCardInfo;
import com.huawei.netassistant.common.SimCardInfo;
import com.huawei.netassistant.service.INetAssistantService;
import com.huawei.netassistant.service.INetAssistantService.Stub;
import com.huawei.netassistant.service.NetAssistantService;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.comparator.SizeComparator;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.netassistant.traffic.appdetail.AppDetailActivity;
import com.huawei.systemmanager.netassistant.traffic.appdetail.Constant;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.utils.NatConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetAssistant4GInfoActivity extends HsmActivity implements OnItemClickListener {
    private static final String TAG = NetAssistant4GInfoActivity.class.getSimpleName();
    private static final int TRAFFIC_NET_NUM = 4;
    private static HwCustNetAssistant4GInfoActivity mCust = ((HwCustNetAssistant4GInfoActivity) HwCustUtils.createObj(HwCustNetAssistant4GInfoActivity.class, new Object[0]));
    private Net4GListAdapter mAdapter;
    private List<Net4GAppInfo> mAppList;
    private Context mContext;
    private View mEmptyView;
    private ListView mListView;
    private PhoneSimCardInfo mPhoneSimCardInfo;
    private INetAssistantService mService;

    public static class Net4GAppInfo {
        public static final AlpComparator<Net4GAppInfo> NETASSISTANT_4G_NAME_COMPARATOR = new AlpComparator<Net4GAppInfo>() {
            public String getStringKey(Net4GAppInfo t) {
                return t.getLabel() != null ? t.getLabel() : "";
            }
        };
        public static final SizeComparator<Net4GAppInfo> NETASSISTANT_SIZE_COMPARATOR = new SizeComparator<Net4GAppInfo>() {
            public long getKey(Net4GAppInfo t) {
                return t.getTraffic();
            }
        };
        private NetAppInfo appInfo;
        private long traffic;

        public Net4GAppInfo(NetAppInfo info, long tra) {
            this.traffic = tra;
            this.appInfo = info;
        }

        public String getLabel() {
            return this.appInfo.mAppLabel;
        }

        public long getTraffic() {
            return this.traffic;
        }

        public Drawable getIcon() {
            return this.appInfo == null ? null : this.appInfo.getIcon();
        }

        public boolean isMultiPgk() {
            return this.appInfo.isMultiPkg;
        }
    }

    private class Net4GListAdapter extends BaseAdapter {
        private List<Net4GAppInfo> appList = new ArrayList();

        public void setData(List<Net4GAppInfo> list) {
            if (list != null) {
                this.appList.clear();
                this.appList.addAll(list);
                notifyDataSetChanged();
            }
        }

        public int getCount() {
            return this.appList.size();
        }

        public Net4GAppInfo getItem(int position) {
            return (Net4GAppInfo) this.appList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(NetAssistant4GInfoActivity.this.mContext).inflate(R.layout.common_list_item_twolines_image_detail, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.title = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                holder.traffic = (TextView) convertView.findViewById(R.id.detail);
                holder.multiPkg = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Net4GAppInfo net4gAppInfo = getItem(position);
            holder.image.setBackground(net4gAppInfo.getIcon());
            holder.title.setText(net4gAppInfo.getLabel());
            holder.traffic.setText(CommonMethodUtil.formatBytes(NetAssistant4GInfoActivity.this.mContext, net4gAppInfo.traffic));
            if (net4gAppInfo.isMultiPgk()) {
                holder.multiPkg.setVisibility(0);
                holder.multiPkg.setText(NetAssistant4GInfoActivity.this.getResources().getText(R.string.net_assistant_more_application));
            } else {
                holder.multiPkg.setVisibility(8);
            }
            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView image;
        TextView multiPkg;
        TextView title;
        TextView traffic;

        private ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAsLocalNum();
        setContentView(R.layout.net_assistant_4g_info);
        this.mContext = this;
        initViews();
        initDatas();
    }

    private void initDatas() {
        this.mAppList = new ArrayList();
        this.mPhoneSimCardInfo = SimCardManager.getInstance().getPhoneSimCardInfo();
        this.mService = Stub.asInterface(ServiceManager.getService(NetAssistantService.NET_ASSISTANT));
        if (mCust != null && !HsmSubsciptionManager.isMultiSubs()) {
            mCust.showLTEString(this, -1);
        }
    }

    protected void onResume() {
        refreshDatas();
        super.onResume();
    }

    private void initViews() {
        this.mListView = (ListView) findViewById(R.id.net_assistant_4g_info);
        this.mEmptyView = findViewById(R.id.empty_view);
        ViewUtil.initEmptyViewMargin(getApplicationContext(), this.mEmptyView);
        this.mAdapter = new Net4GListAdapter();
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this);
    }

    private void refreshDatas() {
        Intent intent = getIntent();
        if (intent == null) {
            this.mEmptyView.setVisibility(0);
            return;
        }
        int subId = intent.getIntExtra(NatConst.KEY_SUBID, -1);
        if (subId < 0) {
            HwLog.e(TAG, "subinfo is -1");
            this.mEmptyView.setVisibility(0);
            return;
        }
        if (HsmSubsciptionManager.isMultiSubs()) {
            getActionBar().setTitle(getString(R.string.net_assistant_traffic_4G_list_with_card, new Object[]{Integer.valueOf(subId + 1)}));
            if (mCust != null) {
                mCust.showLTEString(this, subId);
            }
        }
        String imsi = HsmSubsciptionManager.getImsi(subId);
        if (imsi == null) {
            HwLog.e(TAG, "imsi from intent is null,get the default data imsi");
            SimCardInfo simCardInfo = this.mPhoneSimCardInfo.getDataUsedSimCard();
            if (simCardInfo == null) {
                HwLog.e(TAG, "imsi of default data is null");
                this.mEmptyView.setVisibility(0);
                return;
            }
            imsi = simCardInfo.getImsiNumber();
        }
        this.mAppList.clear();
        if (imsi == null) {
            this.mEmptyView.setVisibility(0);
            return;
        }
        try {
            for (ParcelableAppItem item : this.mService.getMonth4GMobileAppList(imsi)) {
                this.mAppList.add(new Net4GAppInfo(NetAppInfo.buildInfo(item.key), item.mobiletotal));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (this.mAppList.size() == 0) {
            this.mEmptyView.setVisibility(0);
        } else {
            this.mEmptyView.setVisibility(8);
        }
        Collections.sort(this.mAppList, Net4GAppInfo.NETASSISTANT_4G_NAME_COMPARATOR);
        Collections.sort(this.mAppList, Net4GAppInfo.NETASSISTANT_SIZE_COMPARATOR);
        this.mAdapter.setData(this.mAppList);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Net4GAppInfo appInfo = (Net4GAppInfo) parent.getItemAtPosition(position);
        if (appInfo != null) {
            Intent intent = new Intent();
            intent.putExtra("uid", appInfo.appInfo.mUid);
            intent.putExtra(Constant.EXTRA_ACTIVITY_FROM, 1);
            intent.putExtra(Constant.EXTRA_APP_LABEL, appInfo.appInfo.mAppLabel);
            Intent oriIntent = getIntent();
            if (oriIntent != null) {
                int subId = oriIntent.getIntExtra(NatConst.KEY_SUBID, -1);
                if (subId < 0) {
                    HwLog.e(TAG, "subinfo is -1");
                    return;
                }
                String imsi = HsmSubsciptionManager.getImsi(subId);
                String str = Constant.EXTRA_SUBID;
                if (!HsmSubsciptionManager.isMultiSubs()) {
                    subId = -1;
                }
                intent.putExtra(str, subId);
                intent.putExtra(Constant.EXTRA_IMSI, imsi);
                intent.setClass(this, AppDetailActivity.class);
                startActivity(intent);
            }
        }
    }

    private void setTitleAsLocalNum() {
        setTitle(getString(R.string.net_assistant_traffic_4G_list).replace(String.valueOf(4), Utility.getLocaleNumber(4)));
    }
}
