package com.android.settings.nfc;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfcRouteTable extends SettingsPreferenceFragment implements OnItemClickListener, OnClickListener {
    private static Object mNxpNfcAdapter;
    private static Class mNxpNfcAdapterClass;
    private static Method[] mNxpNfcAdapterMethodList;
    private Activity mActivity;
    private NfcAdapter mAdapter;
    private Map<String, Boolean> mAidMap;
    private AppListAdapter mAppAdapter;
    private ArrayList<PaymentAppInfo> mAppInfos;
    private Button mButton;
    private CardEmulation mCardEmuManager;
    private int mHCERouteSize = 0;
    private ListView mListView;
    private int mMaxRouteSize = 0;
    private PackageManager mPm;
    private View mRootView;
    private int mUICCRouteSize = 0;

    public class AppListAdapter extends BaseAdapter {

        private class ViewHolder {
            TextView appName;
            ImageView icon;
            CheckBox mChecked;

            private ViewHolder() {
            }
        }

        public int getCount() {
            if (NfcRouteTable.this.mAppInfos != null) {
                return NfcRouteTable.this.mAppInfos.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            if (NfcRouteTable.this.mAppInfos != null) {
                return NfcRouteTable.this.mAppInfos.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(NfcRouteTable.this.mActivity, 2130968877, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.icon = (ImageView) convertView.findViewById(2131886812);
                holder.appName = (TextView) convertView.findViewById(2131886813);
                holder.mChecked = (CheckBox) convertView.findViewById(2131886814);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PaymentAppInfo bean = (PaymentAppInfo) NfcRouteTable.this.mAppInfos.get(position);
            if (((Boolean) NfcRouteTable.this.mAidMap.get(bean.componentName.flattenToString())).booleanValue()) {
                holder.mChecked.setChecked(true);
            }
            holder.appName.setText((bean.isOnHost ? "HCE : " : "SIM : ") + bean.description.toString());
            holder.icon.setImageDrawable(bean.banner);
            return convertView;
        }
    }

    static {
        mNxpNfcAdapterMethodList = null;
        try {
            mNxpNfcAdapterClass = Class.forName("com.nxp.nfc.NxpNfcAdapter");
            mNxpNfcAdapterMethodList = mNxpNfcAdapterClass.getMethods();
        } catch (Exception e) {
            Log.e("Settings.NfcRouteTable", String.format("NxpNfcAdapterMethodList initialize failed:", new Object[0]) + e.getMessage());
        }
    }

    private static Method findNxpNfcAdapterMethod(String method) {
        if (mNxpNfcAdapterMethodList == null) {
            Log.d("Settings.NfcRouteTable", "findNxpNfcAdapterMethod mNxpNfcAdapterMethodList is null");
            return null;
        }
        for (int i = 0; i < mNxpNfcAdapterMethodList.length; i++) {
            if (mNxpNfcAdapterMethodList[i].getName().equals(method)) {
                return mNxpNfcAdapterMethodList[i];
            }
        }
        Log.e("Settings.NfcRouteTable", "Can't findMethod method: " + method);
        return null;
    }

    private static Object reflectInvokeNxpNfcAdapterS(String methodName, Object... params) {
        Method method = findNxpNfcAdapterMethod(methodName);
        if (method == null) {
            Log.d("Settings.NfcRouteTable", "reflectInvokeNxpNfcAdapterS method is null");
            return null;
        }
        try {
            return method.invoke(mNxpNfcAdapterClass, params);
        } catch (IllegalAccessException e) {
            Log.e("Settings.NfcRouteTable", String.format("reflectInvoke(%s) IllegalAccessException", new Object[]{methodName}));
            return null;
        } catch (InvocationTargetException e2) {
            Log.e("Settings.NfcRouteTable", String.format("reflectInvoke(%s) InvocationTargetException", new Object[]{methodName}));
            return null;
        }
    }

    public static Object reflectInvokeNxpNfcAdapter(String methodName, Object... params) {
        Method method = findNxpNfcAdapterMethod(methodName);
        if (mNxpNfcAdapter == null || method == null) {
            Log.d("Settings.NfcRouteTable", "reflectInvokeNxpNfcAdapter mNxpNfcAdapter or method is null");
            return null;
        }
        try {
            return method.invoke(mNxpNfcAdapter, params);
        } catch (IllegalAccessException e) {
            Log.e("Settings.NfcRouteTable", String.format("reflectInvoke(%s) IllegalAccessException", new Object[]{methodName}));
            return null;
        } catch (InvocationTargetException e2) {
            Log.e("Settings.NfcRouteTable", String.format("reflectInvoke(%s) InvocationTargetException", new Object[]{methodName}));
            return null;
        }
    }

    private static void getNxpNfcAdapters(NfcAdapter mAdapter) {
        mNxpNfcAdapter = reflectInvokeNxpNfcAdapterS("getNxpNfcAdapter", mAdapter);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
        this.mAdapter = NfcAdapter.getDefaultAdapter(this.mActivity);
        this.mPm = this.mActivity.getPackageManager();
        try {
            if (this.mAdapter == null) {
                this.mCardEmuManager = null;
                return;
            }
            this.mCardEmuManager = CardEmulation.getInstance(this.mAdapter);
            getNxpNfcAdapters(this.mAdapter);
        } catch (UnsupportedOperationException e) {
            MLog.e("Settings.NfcRouteTable", "This device does not support card emulation");
            this.mCardEmuManager = null;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(2130968882, null);
        return this.mRootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mListView = (ListView) view.findViewById(2131886827);
        this.mButton = (Button) view.findViewById(2131886828);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mAppAdapter = new AppListAdapter();
        List<ApduServiceInfo> serviceInfos = this.mCardEmuManager.getServices("other");
        if (serviceInfos != null) {
            this.mAppInfos = new ArrayList();
            if (reflectInvokeNxpNfcAdapter("getMaxAidRoutingTableSize", new Object[0]) != null) {
                this.mMaxRouteSize = ((Integer) reflectInvokeNxpNfcAdapter("getMaxAidRoutingTableSize", new Object[0])).intValue();
            }
            this.mAidMap = new HashMap();
            for (ApduServiceInfo service : serviceInfos) {
                PaymentAppInfo appInfo = new PaymentAppInfo();
                appInfo.description = service.getDescription();
                appInfo.banner = service.loadBanner(this.mPm);
                appInfo.componentName = service.getComponent();
                appInfo.isOnHost = service.isOnHost();
                this.mAppInfos.add(appInfo);
                this.mAidMap.put(service.getComponent().flattenToString(), Boolean.valueOf(service.isServiceEnabled("other")));
                int aidsize = service.getAidCacheSize("other");
                if (service.isServiceEnabled("other")) {
                    if (service.isOnHost()) {
                        this.mHCERouteSize += aidsize;
                    } else {
                        this.mUICCRouteSize += aidsize;
                    }
                }
            }
            this.mListView.setAdapter(this.mAppAdapter);
            this.mListView.setOnItemClickListener(this);
            this.mButton.setOnClickListener(this);
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected int getMetricsCategory() {
        return 69;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        PaymentAppInfo bean = (PaymentAppInfo) this.mAppInfos.get(position);
        CheckBox cbChecked = (CheckBox) view.findViewById(2131886814);
        boolean cbIsChecked = !cbChecked.isChecked();
        cbChecked.setChecked(cbIsChecked);
        this.mAidMap.put(bean.componentName.flattenToString(), Boolean.valueOf(cbIsChecked));
        this.mAppAdapter.notifyDataSetChanged();
    }

    public void onClick(View v) {
        reflectInvokeNxpNfcAdapter("updateServiceState", this.mAidMap);
        finish();
    }
}
