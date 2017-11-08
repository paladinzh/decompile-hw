package com.huawei.systemmanager.widget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmAsyncTask;

public class OneKeyCleanActivity extends Activity implements AnimationListener {
    public static final String EMUI_THEME = "androidhwext:style/Theme.Emui";
    public static final String TAG = "OneKeyCleanActivity";
    private boolean mAnimationEnd;
    private int mAppNum;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    OneKeyCleanActivity.this.mBatteryLevel = intent.getIntExtra("level", 0);
                }
            }
        }
    };
    private int mBatteryAfterClean;
    private int mBatteryBeforeClean;
    private int mBatteryLevel;
    private long mMemAfterClean;
    private long mMemBeforeClean;
    private boolean mRefreshMemEnd;
    private long mTotalMem;

    private class CleanProgressTask extends HsmAsyncTask<Void, Void, Void> {
        private CleanProgressTask() {
        }

        protected Void doInBackground(Void... params) {
            Context context = OneKeyCleanActivity.this.getApplicationContext();
            OneKeyCleanActivity.this.mMemBeforeClean = WidgetCleanManager.getMemoryAvailSize(context);
            OneKeyCleanActivity.this.mBatteryBeforeClean = WidgetCleanManager.getBattery(OneKeyCleanActivity.this.mBatteryLevel, context);
            OneKeyCleanActivity.this.mTotalMem = WidgetCleanManager.getMemoryTotalSize(context);
            OneKeyCleanActivity.this.mAppNum = WidgetCleanManager.doOneKeyCleanTask(context, true);
            OneKeyCleanActivity.this.mMemAfterClean = WidgetCleanManager.getMemoryAvailSize(context);
            OneKeyCleanActivity.this.mBatteryAfterClean = WidgetCleanManager.getBattery(OneKeyCleanActivity.this.mBatteryLevel, context);
            return null;
        }

        protected void onSuccess(Void p) {
            OneKeyCleanActivity.this.mRefreshMemEnd = true;
            OneKeyCleanActivity.this.showToast();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_scan_view);
        int themeId = getResources().getIdentifier(EMUI_THEME, null, null);
        if (themeId != 0) {
            setTheme(themeId);
        }
        registerIntent();
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.widget_glowline);
        operatingAnim.setInterpolator(new DecelerateInterpolator());
        operatingAnim.setAnimationListener(this);
        ((ImageView) findViewById(R.id.clean_glowline)).startAnimation(operatingAnim);
    }

    protected void onDestroy() {
        unregisterReceiver(this.mBatInfoReceiver);
        super.onDestroy();
    }

    public void onAnimationStart(Animation animation) {
        View v = findViewById(R.id.onekey_clean_widget_layout);
        v.setBackgroundColor(getResources().getColor(R.color.widget_onkey_clean_bg));
        v.setAnimation(AnimationUtils.loadAnimation(this, R.anim.widget_alpha_bg));
        new CleanProgressTask().executeParallel(new Void[0]);
    }

    public void onAnimationEnd(Animation animation) {
        this.mAnimationEnd = true;
        showToast();
        findViewById(R.id.onekey_clean_widget_layout).setVisibility(8);
        finish();
        overridePendingTransition(0, 0);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    private void registerIntent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        registerReceiver(this.mBatInfoReceiver, intentFilter);
    }

    private void showToast() {
        boolean z;
        if (this.mAnimationEnd) {
            z = this.mRefreshMemEnd;
        } else {
            z = false;
        }
        if (z) {
            int percent = 0;
            if (this.mTotalMem > 0) {
                percent = (int) ((((float) (this.mTotalMem - this.mMemAfterClean)) * 100.0f) / ((float) this.mTotalMem));
            }
            Toast.makeText(getApplicationContext(), WidgetCleanManager.getToastMessage(this.mAppNum, this.mMemAfterClean - this.mMemBeforeClean, this.mBatteryAfterClean - this.mBatteryBeforeClean, percent, this), 0).show();
        }
    }
}
