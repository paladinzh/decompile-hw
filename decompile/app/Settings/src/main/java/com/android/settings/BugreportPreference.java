package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;

public class BugreportPreference extends CustomDialogPreference {
    private TextView mFullSummary;
    private CheckedTextView mFullTitle;
    private TextView mInteractiveSummary;
    private CheckedTextView mInteractiveTitle;

    public BugreportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        View dialogView = View.inflate(getContext(), 2130968661, null);
        this.mInteractiveTitle = (CheckedTextView) dialogView.findViewById(2131886323);
        this.mInteractiveSummary = (TextView) dialogView.findViewById(2131886324);
        this.mFullTitle = (CheckedTextView) dialogView.findViewById(2131886325);
        this.mFullSummary = (TextView) dialogView.findViewById(2131886326);
        View.OnClickListener l = new View.OnClickListener() {
            public void onClick(View v) {
                if (v == BugreportPreference.this.mFullTitle || v == BugreportPreference.this.mFullSummary) {
                    BugreportPreference.this.mInteractiveTitle.setChecked(false);
                    BugreportPreference.this.mFullTitle.setChecked(true);
                }
                if (v == BugreportPreference.this.mInteractiveTitle || v == BugreportPreference.this.mInteractiveSummary) {
                    BugreportPreference.this.mInteractiveTitle.setChecked(true);
                    BugreportPreference.this.mFullTitle.setChecked(false);
                }
            }
        };
        this.mInteractiveTitle.setOnClickListener(l);
        this.mFullTitle.setOnClickListener(l);
        this.mInteractiveSummary.setOnClickListener(l);
        this.mFullSummary.setOnClickListener(l);
        builder.setPositiveButton(17040275, listener);
        builder.setView(dialogView);
    }

    protected void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            Context context = getContext();
            if (this.mFullTitle.isChecked()) {
                Log.v("BugreportPreference", "Taking full bugreport right away");
                MetricsLogger.action(context, 295);
                takeBugreport(0);
            } else {
                Log.v("BugreportPreference", "Taking interactive bugreport in 3s");
                MetricsLogger.action(context, 294);
                String msg = context.getResources().getQuantityString(18087937, 3, new Object[]{Integer.valueOf(3)});
                Log.v("BugreportPreference", msg);
                Toast.makeText(context, msg, 0).show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        BugreportPreference.this.takeBugreport(1);
                    }
                }, 3000);
            }
            ItemUseStat.getInstance().handleClick(getContext(), 2, "bug_report");
        }
    }

    private void takeBugreport(int bugreportType) {
        try {
            ActivityManagerNative.getDefault().requestBugReport(bugreportType);
        } catch (RemoteException e) {
            Log.e("BugreportPreference", "error taking bugreport (bugreportType=" + bugreportType + ")", e);
        }
    }
}
