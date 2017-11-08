package com.huawei.rcs.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;

@SuppressLint({"NewApi"})
public class RcsGroupChatComposeMessageActivity extends HwBaseActivity {
    private Drawable mDeleteBackGround;
    private OnClickListener mDeleteClickListener;
    private RcsGroupChatComposeMessageFragment mFragment;
    public int mPeekActionStatus = 1;
    private Drawable mReplyBackGround;
    private OnClickListener mReplyClickListener;
    private String mReplyString;
    private String mdeleteString;

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d("RcsGroupChatComposeMessageActivity", "onCreate");
        super.onCreate(savedInstanceState);
        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            createConversationListFragment(fm);
            return;
        }
        this.mFragment = (RcsGroupChatComposeMessageFragment) fm.findFragmentByTag("Mms_UI_GCCMF");
        if (this.mFragment == null) {
            createConversationListFragment(fm);
        } else {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        }
    }

    private void createConversationListFragment(FragmentManager fm) {
        getWindow().setSoftInputMode(18);
        this.mFragment = new RcsGroupChatComposeMessageFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment, "Mms_UI_GCCMF");
        transaction.commit();
        initPeekActionInfo();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MLog.d("RcsGroupChatComposeMessageActivity", "onNewIntent intent: " + intent);
        this.mFragment.onNewIntent(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.mFragment != null) {
            this.mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        this.mFragment.onWindowFocusChanged(hasFocus);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!this.mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void onUserInteraction() {
        this.mFragment.onUserInteraction();
    }

    private void initPeekActionInfo() {
        this.mReplyString = getString(R.string.menu_reply);
        this.mdeleteString = getString(R.string.delete);
        this.mReplyBackGround = getResources().getDrawable(R.drawable.quick_peek_reply, null);
        this.mDeleteBackGround = getResources().getDrawable(R.drawable.quick_peek_delete, null);
        this.mReplyClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                RcsGroupChatComposeMessageActivity.this.getRcsGroupChatComposeFragment().replyCurrentConversation(true);
                RcsGroupChatComposeMessageActivity.this.mWrapper.run("onRelease");
            }
        };
        this.mDeleteClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                RcsGroupChatComposeMessageActivity.this.mWrapper.run("showConfirmPanel");
            }
        };
    }

    private RcsGroupChatComposeMessageFragment getRcsGroupChatComposeFragment() {
        return (RcsGroupChatComposeMessageFragment) getFragmentByTAG("Mms_UI_GCCMF");
    }

    private Fragment getFragmentByTAG(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    public void setPeekActionStatus(int peekActionStatus) {
        this.mPeekActionStatus = peekActionStatus;
    }

    public void onFragmentRelease() {
        this.mWrapper.run("onRelease");
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (!(this.mFragment == null || this.mFragment.mActionMode == null)) {
                    this.mFragment.mActionMode.finish();
                    this.mFragment.mActionMode = null;
                    return true;
                }
        }
        return super.dispatchTouchEvent(event);
    }
}
