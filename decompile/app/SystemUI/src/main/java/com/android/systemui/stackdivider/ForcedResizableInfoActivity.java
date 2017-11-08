package com.android.systemui.stackdivider;

import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import com.android.systemui.R;

public class ForcedResizableInfoActivity extends Activity implements OnTouchListener {
    private final Runnable mFinishRunnable = new Runnable() {
        public void run() {
            ForcedResizableInfoActivity.this.finish();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forced_resizable_activity);
        ((TextView) findViewById(16908299)).setText(R.string.dock_forced_resizable);
        getWindow().setTitle(getString(R.string.dock_forced_resizable));
        getWindow().getDecorView().setOnTouchListener(this);
    }

    protected void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(this.mFinishRunnable, 2500);
    }

    protected void onStop() {
        super.onStop();
        finish();
    }

    public boolean onTouch(View v, MotionEvent event) {
        finish();
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();
        return true;
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.forced_resizable_exit);
    }

    public void setTaskDescription(TaskDescription taskDescription) {
    }
}
