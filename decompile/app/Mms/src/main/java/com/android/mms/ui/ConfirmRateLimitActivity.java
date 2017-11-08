package com.android.mms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;

public class ConfirmRateLimitActivity extends HwBaseActivity {
    private long mCreateTime;
    private Handler mHandler;
    private Runnable mRunnable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.confirm_rate_limit_activity);
        ((Button) findViewById(R.id.btn_yes)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ConfirmRateLimitActivity.this.doAnswer(true);
            }
        });
        ((Button) findViewById(R.id.btn_no)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ConfirmRateLimitActivity.this.doAnswer(false);
            }
        });
        this.mHandler = new Handler();
        this.mRunnable = new Runnable() {
            public void run() {
                ConfirmRateLimitActivity.this.doAnswer(false);
            }
        };
        this.mCreateTime = System.currentTimeMillis();
    }

    protected void onResume() {
        super.onResume();
        long delay = (this.mCreateTime - System.currentTimeMillis()) + 19500;
        if (delay <= 0) {
            doAnswer(false);
        } else if (this.mHandler != null) {
            this.mHandler.postDelayed(this.mRunnable, delay);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && event.getRepeatCount() == 0) {
            doAnswer(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doAnswer(boolean answer) {
        Intent intent = new Intent("com.android.mms.RATE_LIMIT_CONFIRMED");
        intent.putExtra("answer", answer);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }
}
