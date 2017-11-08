package com.huawei.keyguard.support.magazine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Window;
import com.android.keyguard.R$string;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.util.Locale;
import java.util.Stack;

public abstract class ApkScanner extends Handler {
    private Context mContext;
    public boolean mIsScanningApk = false;
    private ProgressDialog mScanningDialog;

    protected abstract OnClickListener getDialogListener(boolean z, String str, String str2);

    public ApkScanner(Context context) {
        this.mContext = context;
    }

    public void dispatchMessage(Message msg) {
        switch (msg.what) {
            case 0:
                handleScanningToast(msg.obj);
                return;
            case 1:
                popDialog(false, BuildConfig.FLAVOR, (String) msg.obj);
                return;
            case 2:
                String[] scanResult = msg.obj;
                popDialog(!TextUtils.isEmpty(scanResult[0]), scanResult[0], scanResult[1]);
                return;
            default:
                return;
        }
    }

    public void resetWorkState() {
        this.mIsScanningApk = false;
        removeMessages(1);
        removeMessages(0);
        if (this.mScanningDialog != null && this.mScanningDialog.isShowing()) {
            this.mScanningDialog.dismiss();
        }
    }

    public void execute(final String[] paths, final String pkgname, final String download) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                ApkScanner.this.obtainMessage(0, download).sendToTarget();
                String[] scanResult = ApkScanner.this.getApk(paths, pkgname, download);
                ApkScanner.this.mIsScanningApk = false;
                if (scanResult.length != 2) {
                    HwLog.e("ApkScanner", "scanApksThread onPostExecute : error, array is null or array.length is not equal 2");
                } else {
                    ApkScanner.this.obtainMessage(2, scanResult).sendToTarget();
                }
            }
        });
    }

    private boolean isCancelled() {
        return !this.mIsScanningApk;
    }

    private String[] getApk(String[] paths, String pkgname, String download) {
        if (TextUtils.isEmpty(pkgname) || paths == null) {
            return new String[]{BuildConfig.FLAVOR, download};
        }
        PackageManager pm = this.mContext.getPackageManager();
        Stack<String> stack = new Stack();
        int countPath = paths.length;
        for (int k = 0; k < countPath; k++) {
            if (isCancelled()) {
                HwLog.d("ApkScanner", "work thread has been cancled while try to scan: " + paths[k]);
                return new String[]{BuildConfig.FLAVOR, download};
            }
            HwLog.d("ApkScanner", "start scaning: " + paths[k]);
            stack.push(paths[k]);
            while (!stack.isEmpty()) {
                String filePath = (String) stack.pop();
                if (filePath != null) {
                    File file = new File(filePath);
                    if (file.canRead()) {
                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File childFile : files) {
                                if (childFile.canRead()) {
                                    if (childFile.isDirectory()) {
                                        stack.push(childFile.getPath());
                                    } else {
                                        String fileName = childFile.getName().toLowerCase(Locale.getDefault());
                                        int surfixIdx = fileName.lastIndexOf(".");
                                        if (surfixIdx > 0 && fileName.substring(surfixIdx + 1).equals("apk")) {
                                            PackageInfo info = pm.getPackageArchiveInfo(childFile.toString(), 0);
                                            if (info != null) {
                                                if (pkgname.equalsIgnoreCase(info.packageName)) {
                                                    return new String[]{childFile.toString(), download};
                                                }
                                            } else {
                                                continue;
                                            }
                                        }
                                    }
                                }
                            }
                            continue;
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        return new String[]{BuildConfig.FLAVOR, download};
    }

    private void popDialog(boolean isDownloaded, String apkPath, String urlPath) {
        resetWorkState();
        ContextThemeWrapper context = MagazineUtils.getHwThemeContext(this.mContext, "androidhwext:style/Theme.Emui.Dialog");
        if (context != null) {
            Builder builder = new Builder(context);
            OnClickListener listener = getDialogListener(isDownloaded, apkPath, urlPath);
            if (isDownloaded) {
                builder.setTitle(R$string.magazine_info_link_install_title).setMessage(R$string.magazine_info_link_install_content).setPositiveButton(R$string.magazine_info_link_install_ok, listener);
            } else {
                builder.setTitle(R$string.magazine_info_link_downlod_title).setMessage(R$string.magazine_info_link_downlod_content).setPositiveButton(R$string.magazine_info_link_downlod_ok, listener);
            }
            AlertDialog dialog = builder.setNegativeButton(17039360, null).create();
            dialog.getWindow().setType(2009);
            dialog.show();
            if (dialog.getWindow().getDecorView() != null) {
                dialog.getWindow().getDecorView().setSystemUiVisibility(2097152);
            }
        }
    }

    private void handleScanningToast(Object obj) {
        if (this.mScanningDialog == null) {
            ContextThemeWrapper context = MagazineUtils.getHwThemeContext(this.mContext, "androidhwext:style/Theme.Emui.Dialog");
            if (context != null) {
                this.mScanningDialog = new ProgressDialog(context);
                this.mScanningDialog.setMessage(context.getString(R$string.magazine_scanning_message));
                this.mScanningDialog.setProgressStyle(0);
                Window window = this.mScanningDialog.getWindow();
                if (window != null) {
                    window.setType(2009);
                    window.addFlags(128);
                }
                this.mScanningDialog.setCanceledOnTouchOutside(false);
                this.mScanningDialog.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        ApkScanner.this.resetWorkState();
                    }
                });
                this.mScanningDialog.setButton(-2, context.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ApkScanner.this.mScanningDialog.dismiss();
                    }
                });
                this.mScanningDialog.show();
            } else {
                return;
            }
        } else if (!this.mScanningDialog.isShowing()) {
            this.mScanningDialog.show();
        }
        sendMessageDelayed(obtainMessage(1, obj), 20000);
    }
}
