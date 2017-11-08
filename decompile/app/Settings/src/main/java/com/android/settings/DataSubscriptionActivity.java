package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.internal.telephony.HwSubscriptionManager;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class DataSubscriptionActivity extends Activity implements OnItemClickListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (!Utils.isChinaTelecomArea() || !Utils.isOwnerUser()) {
                return null;
            }
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String action = "com.android.settings.DataSubscriptionActivity";
            String intentTargetPackage = "com.android.settings";
            String intentTargetClass = "com.android.settings.DataSubscriptionActivity";
            String screenTitle = res.getString(2131628021);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.iconResId = 2130838362;
            data.intentAction = action;
            data.intentTargetPackage = intentTargetPackage;
            data.intentTargetClass = intentTargetClass;
            result.add(data);
            data = new SearchIndexableRaw(context);
            screenTitle = res.getString(2131628023);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = action;
            data.intentTargetPackage = intentTargetPackage;
            data.intentTargetClass = intentTargetClass;
            result.add(data);
            data = new SearchIndexableRaw(context);
            screenTitle = res.getString(2131628024);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = action;
            data.intentTargetPackage = intentTargetPackage;
            data.intentTargetClass = intentTargetClass;
            result.add(data);
            return result;
        }
    };
    private int desiredSubId = 0;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 18:
                    Log.d("DataSub", "received EVENT_SET_DATA_SUBSCRIPTION_DONE, refresh view");
                    DataSubscriptionActivity.this.sendBroadcast();
                    DataSubscriptionActivity.this.simStates.clear();
                    DataSubscriptionActivity.this.getSimState();
                    if (DataSubscriptionActivity.this.mDataSubscriptionAdapter != null) {
                        DataSubscriptionActivity.this.mDataSubscriptionAdapter.notifyDataSetChanged();
                        return;
                    }
                    return;
                case 100:
                case 101:
                    if (DataSubscriptionActivity.this.mDataSubscriptionAdapter != null) {
                        DataSubscriptionActivity.this.mDataSubscriptionAdapter.notifyDataSetChanged();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private IntentFilter intentFilter;
    private ContentObserver mAirPlaneObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DataSubscriptionActivity.this.simStates.clear();
            DataSubscriptionActivity.this.getSimState();
            if (DataSubscriptionActivity.this.mDataSubscriptionAdapter != null) {
                DataSubscriptionActivity.this.mDataSubscriptionAdapter.notifyDataSetChanged();
            }
        }
    };
    private Context mContext = this;
    private DataSubscriptionAdapter mDataSubscriptionAdapter;
    private HwCustSplitUtils mHwCustSplitUtils;
    private ListView mListView;
    private ContentObserver mMultiSimDataCallSub = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DataSubscriptionActivity.this.simStates.clear();
            DataSubscriptionActivity.this.getSimState();
            if (DataSubscriptionActivity.this.mDataSubscriptionAdapter != null) {
                DataSubscriptionActivity.this.mDataSubscriptionAdapter.notifyDataSetChanged();
            }
        }
    };
    private List<SimState> simStates = new ArrayList();
    private boolean state0;
    private boolean state1;

    public static class SimState {
        private boolean isActive;
        private boolean isPrefDataSubscription;
        private int subscription;
        private int textRes;

        public void setSubscription(int subscription) {
            this.subscription = subscription;
        }

        public int getTextRes() {
            return this.textRes;
        }

        public void setTextRes(int textRes) {
            this.textRes = textRes;
        }

        public boolean isPrefDataSubscription() {
            return this.isPrefDataSubscription;
        }

        public void setPrefDataSubscription(boolean isPrefDataSubscription) {
            this.isPrefDataSubscription = isPrefDataSubscription;
        }

        public boolean isActive() {
            return this.isActive;
        }

        public void setActive(boolean isActive) {
            this.isActive = isActive;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mHwCustSplitUtils != null) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                boolean z;
                if (this.mHwCustSplitUtils.isSplitMode()) {
                    z = false;
                } else {
                    z = true;
                }
                actionBar.setDisplayHomeAsUpEnabled(z);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initView());
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            boolean z;
            this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{this});
            if (this.mHwCustSplitUtils.isSplitMode()) {
                z = false;
            } else {
                z = true;
            }
            actionBar.setDisplayHomeAsUpEnabled(z);
            actionBar.setTitle(2131628022);
        }
        getContentResolver().registerContentObserver(Global.getUriFor("multi_sim_data_call"), true, this.mMultiSimDataCallSub);
        getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirPlaneObserver);
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction("com.android.huawei.DUAL_CARD_DATA_SUBSCRIPTION_CHANGED");
        new Thread(new Runnable() {
            public void run() {
                DataSubscriptionActivity.this.getSimState();
                DataSubscriptionActivity.this.handler.sendMessage(DataSubscriptionActivity.this.handler.obtainMessage(100));
            }
        }).start();
    }

    public View initView() {
        this.mListView = new ListView(this.mContext);
        this.mDataSubscriptionAdapter = new DataSubscriptionAdapter(this.simStates, this.mContext);
        this.mListView.setAdapter(this.mDataSubscriptionAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setDivider(getResources().getDrawable(2130838529));
        return this.mListView;
    }

    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(this.mMultiSimDataCallSub);
        getContentResolver().unregisterContentObserver(this.mAirPlaneObserver);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void getSimState() {
        boolean z;
        int preSub = -1;
        SimState simState0 = new SimState();
        simState0.setSubscription(0);
        simState0.setTextRes(2131628023);
        if (isCardReady(0) && isSubActive(0) && !isAirplaneModeOn(this.mContext)) {
            z = true;
        } else {
            z = false;
        }
        this.state0 = z;
        SimState simState1 = new SimState();
        simState1.setSubscription(1);
        simState1.setTextRes(2131628024);
        if (isCardReady(1) && isSubActive(1) && !isAirplaneModeOn(this.mContext)) {
            z = true;
        } else {
            z = false;
        }
        this.state1 = z;
        try {
            preSub = getDataSubscription(this.mContext);
        } catch (Exception e) {
            Log.i("DataSub", "getPrefDataSubscription:error,return default sub");
        }
        if (this.state0 && this.state1) {
            if (preSub == 0 || preSub == -1) {
                simState0.setActive(true);
                simState0.setPrefDataSubscription(true);
                simState1.setActive(true);
                simState1.setPrefDataSubscription(false);
            } else if (preSub == 1) {
                simState0.setActive(true);
                simState0.setPrefDataSubscription(false);
                simState1.setActive(true);
                simState1.setPrefDataSubscription(true);
            }
        } else if (!this.state0 && this.state1) {
            simState0.setActive(false);
            simState0.setPrefDataSubscription(false);
            simState1.setActive(true);
            simState1.setPrefDataSubscription(true);
        } else if (!this.state0 || this.state1) {
            simState0.setActive(false);
            simState0.setPrefDataSubscription(false);
            simState1.setActive(false);
            simState1.setPrefDataSubscription(false);
        } else {
            simState0.setActive(true);
            simState0.setPrefDataSubscription(true);
            simState1.setActive(false);
            simState1.setPrefDataSubscription(false);
        }
        this.simStates.add(simState0);
        this.simStates.add(simState1);
        Log.d("DataSub", "load simstate succes");
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        SimState simState = (SimState) arg0.getItemAtPosition(arg2);
        if (simState.isActive() && !simState.isPrefDataSubscription()) {
            Log.d("DataSub", "SELECT Data  Subscription" + arg2);
            for (int i = 0; i < this.simStates.size(); i++) {
                Log.d("DataSub", "set all items enable");
                ((SimState) this.simStates.get(i)).setActive(false);
            }
            sendMessage(101);
            setDataSubscription();
        }
    }

    public void sendMessage(int what) {
        this.handler.sendMessage(this.handler.obtainMessage(what));
    }

    private boolean isAirplaneModeOn(Context context) {
        return Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction("com.android.huawei.DUAL_CARD_DATA_SUBSCRIPTION_CHANGED");
        sendBroadcast(intent);
    }

    public int getDataSubscription(Context sContext) {
        return getRealCardForMobileData();
    }

    public void setDataSubscription() {
        switch (getDataSubscription(this.mContext)) {
            case 0:
                this.desiredSubId = 1;
                break;
            case 1:
                this.desiredSubId = 0;
                break;
            default:
                Log.d("DataSub", "set pref data subscription:error input");
                return;
        }
        new Thread() {
            public void run() {
                super.run();
                try {
                    HwSubscriptionManager.getInstance().setUserPrefDataSlotId(DataSubscriptionActivity.this.desiredSubId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private int readSlotId(String name) {
        long subId;
        try {
            subId = (long) Global.getInt(this.mContext.getContentResolver(), name);
        } catch (SettingNotFoundException e) {
            MLog.e("DataSub", "Settings Exception reading values of " + name);
            subId = 0;
        }
        return getSlotId(subId);
    }

    public int getSlotId(long subId) {
        int slotId = SubscriptionManager.getSlotId((int) subId);
        if (isValidSlotId(slotId)) {
            return slotId;
        }
        MLog.e("DataSub", "getSlotId is invalid, we use the defualt slotid.slotId=" + slotId);
        return 0;
    }

    private boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < 2;
    }

    public int getRealCardForMobileData() {
        int slotId = readSlotId("multi_sim_data_call");
        MLog.i("DataSub", "real def data:" + slotId);
        if (isValidSlotId(slotId)) {
            return slotId;
        }
        return 0;
    }

    private boolean isSubActive(int slotId) {
        boolean z = true;
        try {
            int state = HwTelephonyManager.getDefault().getSubState((long) slotId);
            Log.d("DataSub", "slotId" + slotId + "state:" + state);
            if (state != 1) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isCardReady(int slotId) {
        return TelephonyManager.getDefault().getSimState(slotId) == 5;
    }
}
