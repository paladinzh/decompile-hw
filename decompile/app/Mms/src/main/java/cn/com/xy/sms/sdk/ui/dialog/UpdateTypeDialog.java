package cn.com.xy.sms.sdk.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.settings.SmartUpdateTypeAdapter;
import cn.com.xy.sms.util.ParseManager;
import com.google.android.gms.R;
import com.huawei.mms.util.StatisticalHelper;

public class UpdateTypeDialog implements OnClickListener {
    public static final String TAG = "UpdateTypeDialog";
    public static final int UPDATE_TYPE_ALL = 2;
    public static final int UPDATE_TYPE_CLOSE = 0;
    public static final int UPDATE_TYPE_WALAN = 1;
    private static Dialog mDialog;
    private XyCallBack mCallBack;
    private Context mContext;

    private UpdateTypeDialog(Context context, XyCallBack callBack) {
        this.mContext = context;
        this.mCallBack = callBack;
        showDialog();
    }

    public static UpdateTypeDialog showDialog(Context context, XyCallBack callBack) {
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            return new UpdateTypeDialog(context, callBack);
        }
        return null;
    }

    private void initDialog() {
        AlertDialog updateTypeDlg = new Builder(this.mContext).setIconAttribute(16843605).setTitle(R.string.duoqu_pre_version_update_2).setAdapter(new SmartUpdateTypeAdapter(this.mContext, R.array.duoqu_update_type_arr, SmartSmsSdkUtil.getUpdateType(this.mContext)), null).setNegativeButton(R.string.duoqu_setting_cancel, this).create();
        setDialog(updateTypeDlg);
        updateTypeDlg.getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                int value = 1;
                if (pos == 0) {
                    StatisticalHelper.incrementReportCount(UpdateTypeDialog.this.mContext, 2127);
                    new Thread(new Runnable() {
                        public void run() {
                            ParseManager.updateNow();
                        }
                    }).start();
                    value = 2;
                } else if (pos == 2) {
                    StatisticalHelper.incrementReportCount(UpdateTypeDialog.this.mContext, 2129);
                    value = 0;
                } else {
                    StatisticalHelper.incrementReportCount(UpdateTypeDialog.this.mContext, 2128);
                    new Thread(new Runnable() {
                        public void run() {
                            ParseManager.updateNow();
                        }
                    }).start();
                }
                SmartSmsSdkUtil.setUpdateType(UpdateTypeDialog.this.mContext, value);
                UpdateTypeDialog.dismissDialog();
                UpdateTypeDialog.this.callBack(pos);
            }
        });
    }

    private void callBack(int selectPos) {
        if (this.mCallBack != null) {
            this.mCallBack.execute(Integer.valueOf(selectPos));
        }
    }

    private void showDialog() {
        initDialog();
        if ((this.mContext instanceof Activity) && !((Activity) this.mContext).isFinishing()) {
            try {
                getDialog().show();
            } catch (Exception e) {
                SmartSmsSdkUtil.smartSdkExceptionLog("UpdateTypeDialog showDialog error:" + e.getMessage(), e);
            }
        }
    }

    private static Dialog getDialog() {
        Dialog dialog;
        synchronized (UpdateTypeDialog.class) {
            dialog = mDialog;
        }
        return dialog;
    }

    private static void setDialog(Dialog dialog) {
        synchronized (UpdateTypeDialog.class) {
            mDialog = dialog;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void dismissDialog() {
        synchronized (UpdateTypeDialog.class) {
            if (mDialog == null) {
            } else if (mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            } else {
                mDialog = null;
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (getDialog() != null) {
            dismissDialog();
        }
    }
}
