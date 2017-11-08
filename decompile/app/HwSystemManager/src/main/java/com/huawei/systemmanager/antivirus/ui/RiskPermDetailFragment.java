package com.huawei.systemmanager.antivirus.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AppSecurityPermissions;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.optimizer.utils.PackageUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class RiskPermDetailFragment extends Fragment {
    public static final String KEY_PACKAGE = "key_package";
    public static final String TAG = "RiskPermDetailFragment";
    private String mPackgeName;
    private LinearLayout mPermissionContainer;
    private Button mUninstalledBtn;

    protected class PermissionTask extends AsyncTask<Void, Void, AppSecurityPermissions> {
        private Context mContext;
        private String mPackageName;

        public PermissionTask(Context context, String packageName) {
            this.mContext = context;
            this.mPackageName = packageName;
        }

        protected AppSecurityPermissions doInBackground(Void... params) {
            AppSecurityPermissions asp = new AppSecurityPermissions(this.mContext, this.mPackageName);
            this.mContext = null;
            return asp;
        }

        protected void onPostExecute(AppSecurityPermissions asp) {
            if (!isCancelled()) {
                RiskPermDetailFragment.this.refreshPermissions(asp);
            }
        }
    }

    public static class RiskPermDetailActivity extends SingleFragmentActivity {
        public String getDetailPkg() {
            Intent intent = getIntent();
            if (intent != null) {
                return intent.getStringExtra(RiskPermDetailFragment.KEY_PACKAGE);
            }
            return "";
        }

        protected Fragment buildFragment() {
            return new RiskPermDetailFragment();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.antivirus_risk_perm_details, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        initViews(view);
    }

    public void onResume() {
        super.onResume();
        refreshUninstallBtn();
    }

    private void initViews(View view) {
        Activity ac = getActivity();
        if (ac != null) {
            this.mPackgeName = ((RiskPermDetailActivity) ac).getDetailPkg();
            if (TextUtils.isEmpty(this.mPackgeName)) {
                HwLog.i(TAG, "resolveItem pkg is null!!");
                ac.finish();
                return;
            }
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(this.mPackgeName);
            if (info == null) {
                HwLog.e(TAG, "initViews, cannot found pkg, pkg:" + this.mPackgeName);
                ac.finish();
                return;
            }
            ((ImageView) view.findViewById(R.id.app_icon)).setImageDrawable(info.icon());
            ((TextView) view.findViewById(R.id.app_name)).setText(info.label());
            this.mUninstalledBtn = (Button) view.findViewById(R.id.uninstall_button);
            this.mUninstalledBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Activity ac = RiskPermDetailFragment.this.getActivity();
                    if (!TextUtils.isEmpty(RiskPermDetailFragment.this.mPackgeName) && ac != null) {
                        String pkg = RiskPermDetailFragment.this.mPackgeName;
                        HwLog.i(RiskPermDetailFragment.TAG, "user click to uninstall pkg:" + pkg);
                        HsmStat.statE((int) Events.E_COMPETITOR_VIEW_UNINSTALL, HsmStatConst.PARAM_PKG, pkg);
                        PackageUtils.uninstallApp(ac, pkg, true);
                    }
                }
            });
            this.mPermissionContainer = (LinearLayout) view.findViewById(R.id.risk_perm_detail_container);
            new PermissionTask(ac, this.mPackgeName).executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    private void refreshPermissions(AppSecurityPermissions asp) {
        if (this.mPermissionContainer == null) {
            HwLog.e(TAG, "refreshPermissions mPermissionContainer == null");
        } else if (getActivity() == null) {
            HwLog.w(TAG, "refreshPermissions getActivity is null");
        } else if (asp.getPermissionCount() <= 0) {
            HwLog.w(TAG, "refreshPermissions getPermissionCount <= 0");
        } else {
            this.mPermissionContainer.addView(asp.getPermissionsViewWithRevokeButtons());
        }
    }

    private void refreshUninstallBtn() {
        boolean installed = false;
        if (TextUtils.isEmpty(this.mPackgeName)) {
            HwLog.e(TAG, "refreshUninstallBtn, mItem is null!");
        } else {
            String pkg = this.mPackgeName;
            if (HsmPackageManager.getInstance().packageExists(pkg, 8192)) {
                installed = true;
            } else {
                HwLog.i(TAG, "refreshUninstallBtn, pkg is uninstalled, pkg:" + pkg);
            }
        }
        Activity ac = getActivity();
        if (ac != null) {
            Intent intent = new Intent();
            intent.putExtra(AntiVirusTools.DELETE_ITEM, !installed);
            ac.setResult(AntiVirusTools.RESULT_CODE, intent);
        }
        if (this.mUninstalledBtn != null) {
            int color;
            this.mUninstalledBtn.setEnabled(installed);
            Button button = this.mUninstalledBtn;
            if (installed) {
                color = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_enable);
            } else {
                color = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_disable);
            }
            button.setTextColor(color);
        }
    }
}
