package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.net.HwNetworkPolicyManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.common.collect.Lists;
import com.huawei.cust.HwCustUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.Collections;
import java.util.List;

public class RoamingTrafficListPresenter {
    private static final String TAG = "RoamingTrafficListPresenter";
    private static final int TASK_ACCESS_TRAFFIC = 202;
    private static final int TASK_ACCESS_TRAFFIC_LIST = 204;
    private static final int TASK_DENY_TRAFFIC = 203;
    private static final int TASK_DENY_TRAFFIC_LIST = 205;
    private static final int TASK_LOADING_TRAFFIC_DATA = 201;
    boolean isRemovable;
    private HwCustRoamingTrafficListPresenter mHwRoamingTrafficListPresenter = ((HwCustRoamingTrafficListPresenter) HwCustUtils.createObj(HwCustRoamingTrafficListPresenter.class, new Object[]{GlobalContext.getContext()}));
    HwNetworkPolicyManager mPolicyManager = HwNetworkPolicyManager.from(GlobalContext.getContext());
    RoamingTrafficView mView;
    RoamingTrafficTask task;

    private class BackgroundTrafficTask extends AsyncTask<Void, Void, Void> {
        public static final String MASSTASK = "2";
        public static final String SINGLETASK = "1";
        private String mBackgroundTrafficTask;
        private RoamingAppInfo mBackgroundTrafficTaskInfo;
        private List<RoamingAppInfo> mBackgroundTrafficTaskList;
        private boolean mBackgroundTrafficTaskValue;

        public BackgroundTrafficTask(String task, RoamingAppInfo info, boolean value) {
            this.mBackgroundTrafficTask = task;
            this.mBackgroundTrafficTaskInfo = info;
            this.mBackgroundTrafficTaskValue = value;
        }

        public BackgroundTrafficTask(String task, List<RoamingAppInfo> list, boolean value) {
            this.mBackgroundTrafficTask = task;
            this.mBackgroundTrafficTaskList = list;
            this.mBackgroundTrafficTaskValue = value;
        }

        protected void onPreExecute() {
            if (TextUtils.equals(this.mBackgroundTrafficTask, "2")) {
                RoamingTrafficListPresenter.this.mView.showLoadingDialog();
            }
        }

        protected Void doInBackground(Void... voids) {
            boolean z = false;
            if (TextUtils.equals(this.mBackgroundTrafficTask, "1")) {
                if (this.mBackgroundTrafficTaskInfo != null) {
                    RoamingAppInfo roamingAppInfo = this.mBackgroundTrafficTaskInfo;
                    if (!this.mBackgroundTrafficTaskValue) {
                        z = true;
                    }
                    roamingAppInfo.setBackgroundChecked(z);
                }
            } else if (TextUtils.equals(this.mBackgroundTrafficTask, "2") && this.mBackgroundTrafficTaskList != null) {
                for (RoamingAppInfo info : this.mBackgroundTrafficTaskList) {
                    info.setBackgroundChecked(!this.mBackgroundTrafficTaskValue);
                }
            }
            return null;
        }

        protected void onPostExecute(Void voids) {
            RoamingTrafficListPresenter.this.mView.syncBackgroundHeadCheckBox();
            if (TextUtils.equals(this.mBackgroundTrafficTask, "2")) {
                RoamingTrafficListPresenter.this.mView.dismissLoadingDialog();
            }
        }
    }

    private class RoamingTrafficTask extends AsyncTask<RoamingAppInfo, Void, List<RoamingAppInfo>> {
        boolean isRemovable;
        int taskId;

        public RoamingTrafficTask(boolean isRemovableApp, int id) {
            this.isRemovable = isRemovableApp;
            this.taskId = id;
        }

        protected void onPreExecute() {
            if (this.taskId != 202 && this.taskId != 203) {
                if (this.taskId != 201 || !this.isRemovable) {
                    RoamingTrafficListPresenter.this.mView.showLoadingDialog();
                }
            }
        }

        protected List<RoamingAppInfo> doInBackground(RoamingAppInfo... params) {
            int i = 0;
            int length;
            RoamingAppInfo info;
            switch (this.taskId) {
                case 201:
                    return loadingTrafficDataFromDB();
                case 202:
                case 204:
                    if (params != null) {
                        length = params.length;
                        while (i < length) {
                            info = params[i];
                            RoamingTrafficListPresenter.this.mPolicyManager.removeHwUidPolicy(info.appInfo.mUid, 4);
                            info.isNetAccess = true;
                            i++;
                        }
                        break;
                    }
                    return null;
                case 203:
                case 205:
                    if (params != null) {
                        for (RoamingAppInfo info2 : params) {
                            RoamingTrafficListPresenter.this.mPolicyManager.addHwUidPolicy(info2.appInfo.mUid, 4);
                            info2.isNetAccess = false;
                        }
                        break;
                    }
                    return null;
            }
            return null;
        }

        private List<RoamingAppInfo> loadingTrafficDataFromDB() {
            int i;
            HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(GlobalContext.getContext());
            List<RoamingAppInfo> list = Lists.newArrayList();
            List<RoamingAppInfo> detailFilterList = Lists.newArrayList();
            SparseArray<HsmPkgInfo> pkgInfos = RoamingTrafficListPresenter.this.getAllUids(this.isRemovable);
            for (i = 0; i < pkgInfos.size(); i++) {
                boolean z;
                int policy = manager.getHwUidPolicy(((HsmPkgInfo) pkgInfos.valueAt(i)).getUid());
                NetAppInfo buildInfo = NetAppInfo.buildInfo(pkgInfos.keyAt(i));
                if ((policy & 4) == 0) {
                    z = true;
                } else {
                    z = false;
                }
                RoamingAppInfo appInfo = new RoamingAppInfo(buildInfo, z);
                HwLog.i(RoamingTrafficListPresenter.TAG, "add pkgInfo to list, uid = " + appInfo.appInfo.mUid);
                list.add(appInfo);
            }
            if (RoamingTrafficListPresenter.this.mHwRoamingTrafficListPresenter != null) {
                List<Integer> uidFilterList = RoamingTrafficListPresenter.this.mHwRoamingTrafficListPresenter.getFilterUidList();
                if (uidFilterList != null) {
                    for (i = 0; i < list.size(); i++) {
                        RoamingAppInfo roamingAppInfo = (RoamingAppInfo) list.get(i);
                        if (uidFilterList.contains(Integer.valueOf(roamingAppInfo.appInfo.mUid))) {
                            detailFilterList.add(roamingAppInfo);
                        }
                    }
                    if (detailFilterList.size() > 0) {
                        list.removeAll(detailFilterList);
                    }
                }
            }
            Collections.sort(list, RoamingAppInfo.ABS_NET_APP_ALP_COMPARATOR);
            return list;
        }

        protected void onPostExecute(List<RoamingAppInfo> roamingApps) {
            super.onPostExecute(roamingApps);
            if (roamingApps != null) {
                RoamingTrafficListPresenter.this.mView.showTrafficList(roamingApps);
            }
            RoamingTrafficListPresenter.this.mView.syncRoamingHeadCheckBox();
            if (201 == this.taskId) {
                RoamingTrafficListPresenter.this.mView.syncBackgroundHeadCheckBox();
            }
            if (this.taskId != 202 && this.taskId != 203) {
                RoamingTrafficListPresenter.this.mView.dismissLoadingDialog();
            }
        }
    }

    public RoamingTrafficListPresenter(RoamingTrafficView view, boolean isRemovableApp) {
        this.mView = view;
        this.isRemovable = isRemovableApp;
    }

    void loadingData() {
        this.task = new RoamingTrafficTask(this.isRemovable, 201);
        this.task.execute(new RoamingAppInfo[0]);
    }

    void access(RoamingAppInfo... appInfo) {
        this.task = new RoamingTrafficTask(this.isRemovable, 202);
        this.task.execute(appInfo);
    }

    void deny(RoamingAppInfo... appInfo) {
        this.task = new RoamingTrafficTask(this.isRemovable, 203);
        this.task.execute(appInfo);
    }

    void accessList(RoamingAppInfo... appInfo) {
        this.task = new RoamingTrafficTask(this.isRemovable, 204);
        this.task.execute(appInfo);
    }

    void denyList(RoamingAppInfo... appInfo) {
        this.task = new RoamingTrafficTask(this.isRemovable, 205);
        this.task.execute(appInfo);
    }

    void setBackgroundChecked(RoamingAppInfo info, boolean value) {
        new BackgroundTrafficTask("1", info, value).execute(new Void[0]);
    }

    void setBackgroundListChecked(List<RoamingAppInfo> list, boolean value) {
        new BackgroundTrafficTask("2", (List) list, value).execute(new Void[0]);
    }

    private SparseArray<HsmPkgInfo> getAllUids(boolean isRemovable) {
        SparseArray<HsmPkgInfo> sparseArray = new SparseArray();
        List<HsmPkgInfo> list;
        if (isRemovable) {
            list = NetAppUtils.getAllNetRemovableUidHwPkgInfo();
        } else {
            list = NetAppUtils.getAllNetUnRemovableUidHwPkgInfo();
        }
        for (HsmPkgInfo info : list) {
            sparseArray.put(info.mUid, info);
        }
        return sparseArray;
    }
}
