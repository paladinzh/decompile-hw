package com.android.contacts.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.util.BackgroundViewCacher;
import com.android.contacts.hap.util.HwAnimationReflection;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.File;
import java.util.List;

public class ContactDetailActivity extends Activity {
    private ContactInfoFragment mContactInfoFragment;
    public AlertDialog mGlobalDialogReference;
    public AlertDialog mGlobalRoamingDialogReference;
    private boolean mIsNeedOverrideBackAnimation = true;

    public static class TranslucentActivity extends ContactDetailActivity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().setBackgroundDrawableResource(17170445);
            requestWindowFeature(1);
            if (CommonUtilMethods.isLayoutRTL()) {
                overridePendingTransition(R.anim.slide_in_left, 0);
            }
        }

        public void finish() {
            super.finish();
            if (CommonUtilMethods.isLayoutRTL()) {
                overridePendingTransition(0, R.anim.slide_out_left);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            BackgroundViewCacher.getInstance(this).startInflatring();
        }
        if (!(this instanceof TranslucentActivity) && getResources().getConfiguration().orientation == 1) {
            getWindow().setSplitActionBarAlways(true);
        }
        super.onCreate(savedInstanceState);
        setTheme(R.style.PeopleThemeWithListSelector);
        FragmentManager fm = getFragmentManager();
        if (getResources().getConfiguration().orientation == 1) {
            getWindow().addFlags(67108864);
        }
        if (savedInstanceState == null) {
            createContactInfoFragment(fm);
        } else {
            this.mContactInfoFragment = (ContactInfoFragment) fm.findFragmentByTag("ContactInfoFragment");
            if (this.mContactInfoFragment == null) {
                createContactInfoFragment(fm);
            }
        }
        if (getIntent() != null) {
            if ("fromLauncher".equals(getIntent().getStringExtra("fromWhere"))) {
                this.mIsNeedOverrideBackAnimation = false;
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mContactInfoFragment != null) {
            this.mContactInfoFragment.onNewIntent(intent);
        }
    }

    private void createContactInfoFragment(FragmentManager fm) {
        this.mContactInfoFragment = new ContactInfoFragment();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(16908290, this.mContactInfoFragment, "ContactInfoFragment");
        transaction.commit();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mContactInfoFragment.isNeedUpdateWindows() || 4 != event.getAction()) {
            return super.onTouchEvent(event);
        }
        finish();
        return true;
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mGlobalDialogReference != null) {
            this.mGlobalDialogReference.dismiss();
        }
        if (this.mGlobalRoamingDialogReference != null) {
            this.mGlobalRoamingDialogReference.dismiss();
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mContactInfoFragment == null || !this.mContactInfoFragment.onKeyUp(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.mContactInfoFragment != null) {
            this.mContactInfoFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (this.mIsNeedOverrideBackAnimation) {
            new HwAnimationReflection(this).overrideTransition(2);
        }
    }

    public boolean onNavigateUp() {
        List<RunningTaskInfo> taskInfoList = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        if (taskInfoList == null || taskInfoList.size() != 1 || !((RunningTaskInfo) taskInfoList.get(0)).baseActivity.getClassName().equalsIgnoreCase(PeopleActivity.class.getName())) {
            return super.onNavigateUp();
        }
        onBackPressed();
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        File file = new File(getExternalCacheDir(), "profile.jpg");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (HwLog.HWDBG) {
                HwLog.i("ContactDetailActivity", "file delete :" + deleted);
            }
        }
    }
}
