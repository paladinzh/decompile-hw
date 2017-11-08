package com.android.contacts.hap.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import huawei.android.app.HwProgressDialog;

public class CopyProgressBarActivity extends Activity {
    private boolean exportSim;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int value;
            boolean mAlive = !CopyProgressBarActivity.this.isFinishing();
            if (msg.what == 5) {
                if (CopyProgressBarActivity.this.mTotal < 50 && mAlive) {
                    CopyProgressBarActivity.this.finish();
                } else if (!CopyProgressBarActivity.this.importSim && CopyProgressBarActivity.this.mTotal >= 50 && mAlive) {
                    CopyProgressBarActivity.this.showProgressDialog(CopyProgressBarActivity.this.mTotal);
                } else if (CopyProgressBarActivity.this.importSim && CopyProgressBarActivity.this.mTotal >= 50 && mAlive) {
                    CopyProgressBarActivity.this.showProgressDialogWithButton(CopyProgressBarActivity.this.mTotal, R.string.import_contacts_message);
                }
            }
            if (msg.what == 4) {
                if (msg.arg1 == 1) {
                    CopyProgressBarActivity.this.exportSim = true;
                } else {
                    CopyProgressBarActivity.this.exportSim = false;
                }
            }
            if (msg.what == 3) {
                CopyProgressBarActivity.this.handlerToProcess = (Handler) msg.obj;
            }
            if (msg.what == 2) {
                value = msg.arg1;
                CopyProgressBarActivity.this.mTotalNumerToCopy = value;
                if (value < 50 && mAlive) {
                    CopyProgressBarActivity.this.finish();
                } else if (CopyProgressBarActivity.this.exportSim && mAlive) {
                    CopyProgressBarActivity.this.showProgressDialogWithButton(value, R.string.export_contacts_message);
                } else if (!CopyProgressBarActivity.this.exportSim && mAlive) {
                    CopyProgressBarActivity.this.showProgressDialog(value);
                }
            }
            if (msg.what == 1 && (CopyProgressBarActivity.this.mTotal >= 50 || CopyProgressBarActivity.this.mTotalNumerToCopy >= 50)) {
                value = msg.arg1;
                if (CopyProgressBarActivity.this.mProgressDialog != null) {
                    if ((value < 0 || value >= CopyProgressBarActivity.this.mTotal) && mAlive && (value < 0 || value >= CopyProgressBarActivity.this.mTotalNumerToCopy)) {
                        CopyProgressBarActivity.this.mProgressDialog.dismiss();
                        CopyProgressBarActivity.this.mProgressDialog = null;
                        if (HwLog.HWFLOW) {
                            HwLog.i("CopyProgressBarActivity", "close the Progressdialog completely");
                        }
                        CopyProgressBarActivity.this.finish();
                    } else {
                        CopyProgressBarActivity.this.mProgressDialog.setProgress(value);
                        CopyProgressBarActivity.this.mCurrentCount = value;
                    }
                }
            }
            if (msg.what == 7) {
                CopyProgressBarActivity.this.finish();
            }
        }
    };
    private Handler handlerToProcess;
    private boolean importSim;
    private String mAccountType;
    private int mCurrentCount;
    private HwProgressDialog mProgressDialog;
    private int mTotal;
    private int mTotalNumerToCopy;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        Bundle lBundle = getIntent().getBundleExtra("bundle");
        boolean isReCreate = false;
        if (savedInstanceState != null) {
            isReCreate = savedInstanceState.getBoolean("recreate");
        }
        if (lBundle == null) {
            finish();
            return;
        }
        Intent lIntent = (Intent) lBundle.getParcelable("intent");
        if (lIntent == null) {
            HwLog.w("CopyProgressBarActivity", "lIntent in bundle is null");
            return;
        }
        this.importSim = lIntent.getBooleanExtra("import_to_sim", false);
        this.exportSim = lIntent.getBooleanExtra("export_to_sim", false);
        this.mAccountType = lIntent.getStringExtra("AccountType");
        this.mTotal = lIntent.getLongArrayExtra("ContactIds").length;
        if (this.mTotal != 0) {
            if (isReCreate) {
                finish();
            } else {
                lIntent.putExtra("messenger", new Messenger(this.handler));
                startService(lIntent);
            }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("recreate", true);
        super.onSaveInstanceState(outState);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
            if (HwLog.HWFLOW) {
                HwLog.i("CopyProgressBarActivity", "close the Progressdialog completely");
            }
        }
    }

    private void showProgressDialogWithButton(int count, final int messageID) {
        this.mProgressDialog = new HwProgressDialog(this);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setIndeterminate(false);
        this.mProgressDialog.setMessage(getString(messageID));
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setMax(count);
        this.mProgressDialog.setButton(-1, getString(R.string.hide), new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                CopyProgressBarActivity.this.finish();
            }
        });
        this.mProgressDialog.show();
        View cancelView = this.mProgressDialog.getCancelButton();
        if (cancelView != null) {
            cancelView.setContentDescription(getString(R.string.stop));
            cancelView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CopyProgressBarActivity.this.showAlertDialog(messageID);
                }
            });
        }
    }

    private void showAlertDialog(int messageID) {
        AlertDialog mAlertDialog = new Builder(this).create();
        if (R.string.import_contacts_message == messageID) {
            mAlertDialog.setMessage(getString(R.string.stop_import_message));
        } else if (R.string.export_contacts_message == messageID) {
            String exportString = getString(R.string.stop_export_to_sim_card_message);
            String accName = SimFactoryManager.getSimCardDisplayLabel(this.mAccountType);
            mAlertDialog.setMessage(String.format(exportString, new Object[]{accName}));
        }
        mAlertDialog.setMessageNotScrolling();
        OnClickListener aListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    msg.obj = Boolean.valueOf(true);
                    CopyProgressBarActivity.this.handlerToProcess.sendMessage(msg);
                    if (CopyProgressBarActivity.this.mProgressDialog != null) {
                        CopyProgressBarActivity.this.mProgressDialog.dismiss();
                        return;
                    }
                    return;
                }
                CopyProgressBarActivity.this.mProgressDialog.show();
                CopyProgressBarActivity.this.mProgressDialog.setProgress(CopyProgressBarActivity.this.mCurrentCount);
            }
        };
        mAlertDialog.setButton(-1, getString(R.string.stop), aListener);
        mAlertDialog.setButton(-2, getString(R.string.cancel), aListener);
        mAlertDialog.show();
    }

    private void showProgressDialog(int count) {
        this.mProgressDialog = new HwProgressDialog(this);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setMessage(getString(R.string.copy_contacts_message));
        this.mProgressDialog.setProgressStyle(1);
        this.mProgressDialog.setCancelable(false);
        this.mProgressDialog.setIndeterminate(false);
        this.mProgressDialog.setMax(count);
        this.mProgressDialog.show();
        this.mProgressDialog.disableCancelButton();
    }

    public void onPause() {
        super.onPause();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.show();
        }
    }
}
