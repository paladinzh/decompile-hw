package com.huawei.systemmanager.spacecleanner.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.util.HwLog;

public class DialogManager {
    private static final String TAG = "DialogManager";
    private Dialog mCancelDialog;
    private final Context mContext;
    private final Fragment mFragment;
    private ProgressDialog mWaitingDialog = null;
    private Dialog mWarnDialog = null;

    public DialogManager(Context ctx, Fragment frag) {
        this.mContext = ctx;
        this.mFragment = frag;
        hideCheckSureDialog();
        hideLongclickDialog();
    }

    public void showCancelScanningDlg() {
        if (checkFragmentState()) {
            hiddenCancelDlg();
            createCancelDlg(this.mContext.getResources().getString(R.string.common_dialog_title_tip), this.mContext.getResources().getString(R.string.space_clean_cancel_scanning_message));
            return;
        }
        HwLog.i(TAG, "showCancelScanningDlg checkFragmentState failed!");
    }

    public void showCancelCleanningDlg() {
        if (checkFragmentState()) {
            hiddenCancelDlg();
            createCancelDlg(this.mContext.getResources().getString(R.string.common_dialog_title_tip), this.mContext.getResources().getString(R.string.space_clean_cancel_cleanning_message));
            return;
        }
        HwLog.i(TAG, "showCancelCleanningDlg checkFragmentState failed!");
    }

    public void destory() {
        hiddenCancelDlg();
        hideCheckSureDialog();
        hideLongclickDialog();
    }

    private boolean checkFragmentState() {
        Activity ac = this.mFragment.getActivity();
        if (ac == null) {
            HwLog.i(TAG, "checkFragmentState activity is null");
            return false;
        } else if (!ac.isDestroyed()) {
            return true;
        } else {
            HwLog.i(TAG, "checkFragmentState activity is destory");
            return false;
        }
    }

    private void createCancelDlg(String title, String message) {
        Activity ac = this.mFragment.getActivity();
        if (ac != null) {
            Builder alertDialog = new Builder(ac);
            alertDialog.setTitle(title);
            alertDialog.setMessage(message);
            alertDialog.setNegativeButton(this.mContext.getResources().getString(R.string.cancel), null);
            alertDialog.setPositiveButton(this.mContext.getResources().getString(R.string.space_clean_exit), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Activity ac = DialogManager.this.mFragment.getActivity();
                    if (ac != null) {
                        SpaceStatsUtils.reportStopSpaceScanOp("0");
                        ac.finish();
                    }
                }
            });
            this.mCancelDialog = alertDialog.show();
        }
    }

    public void hiddenCancelDlg() {
        if (this.mCancelDialog != null) {
            this.mCancelDialog.dismiss();
            this.mCancelDialog = null;
        }
    }

    public void createSdcardWarningDlg(String message, final boolean isDoFinish) {
        Activity ac = this.mFragment.getActivity();
        if (ac != null) {
            hiddenWarnDialog();
            Builder alertDialog = new Builder(ac);
            alertDialog.setTitle(this.mContext.getResources().getString(R.string.common_dialog_title_tip));
            alertDialog.setMessage(message);
            alertDialog.setPositiveButton(this.mContext.getResources().getString(R.string.space_clean_sdcard_exception_tips), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DialogManager.this.finish(isDoFinish);
                }
            });
            alertDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    DialogManager.this.finish(isDoFinish);
                }
            });
            this.mWarnDialog = alertDialog.show();
        }
    }

    private void hiddenWarnDialog() {
        if (this.mWarnDialog != null) {
            this.mWarnDialog.dismiss();
            this.mWarnDialog = null;
        }
    }

    private void finish(boolean isDoFinish) {
        if (isDoFinish) {
            Activity ac = this.mFragment.getActivity();
            if (ac != null) {
                ac.finish();
            }
        }
    }

    private boolean ensureActivityResume() {
        Activity activity = this.mFragment.getActivity();
        if (activity == null || !activity.isResumed()) {
            return false;
        }
        return true;
    }

    public void hideCheckSureDialog() {
        hildeDialog(DeepEnsureDialog.TAG);
    }

    public void showChecksureDialog(OnClickListener dialogClicker, View.OnClickListener detailClicker) {
        if (ensureActivityResume()) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            DeepEnsureDialog newFragment = new DeepEnsureDialog();
            newFragment.setDialogClicker(dialogClicker, detailClicker);
            newFragment.show(ft, DeepEnsureDialog.TAG);
            return;
        }
        HwLog.i(TAG, "showChecksureDialog, ensureAcitivity resume failed!");
    }

    public boolean showLongclickDialog(OnClickListener clicker, ITrashItem item) {
        if (ensureActivityResume()) {
            DialogFragment dialogFragment = LongClickDialog.getFragment(clicker, item);
            if (dialogFragment == null) {
                HwLog.i(TAG, "showLongclickDialog, getFragment is null");
                return false;
            }
            dialogFragment.show(getFragmentManager().beginTransaction(), LongClickDialog.TAG);
            return true;
        }
        HwLog.i(TAG, "showLongclickDialog, ensureAcitivity resume failed!");
        return false;
    }

    public void hideLongclickDialog() {
        hildeDialog(LongClickDialog.TAG);
    }

    private void hildeDialog(String tag) {
        Activity ac = this.mFragment.getActivity();
        if (ac != null) {
            Fragment frag = getFragmentManager().findFragmentByTag(tag);
            if (!(frag == null || ac.isDestroyed())) {
                ((DialogFragment) frag).dismissAllowingStateLoss();
            }
        }
    }

    private FragmentManager getFragmentManager() {
        return this.mFragment.getFragmentManager();
    }

    public void showWaitingDialog(Fragment frg) {
        Activity ac = frg.getActivity();
        if (ac != null && this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(ac, "", ac.getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    public void hideWaitingDialog() {
        if (this.mWaitingDialog != null) {
            if (this.mWaitingDialog.isShowing()) {
                this.mWaitingDialog.dismiss();
            }
            this.mWaitingDialog = null;
        }
    }
}
