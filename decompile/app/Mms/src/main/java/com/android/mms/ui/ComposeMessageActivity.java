package com.android.mms.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import com.amap.api.services.core.AMapException;
import com.android.mms.data.Conversation;
import com.android.rcs.ui.RcsComposeMessageActivityUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.ActivityExWrapper;

public class ComposeMessageActivity extends HwBaseActivity {
    private Drawable mCallBackGround;
    private OnClickListener mCallClickListener;
    private String mCallString;
    private Drawable mDeleteBackGround;
    private OnClickListener mDeleteClickListener;
    private ComposeMessageFragment mFragment;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST /*2000*/:
                    if (ActivityExWrapper.IS_PRESS_SUPPORT) {
                        ComposeMessageActivity.this.mWrapper.run("onRelease");
                        return;
                    }
                    return;
                case AMapException.CODE_AMAP_ID_NOT_EXIST /*2001*/:
                    ComposeMessageActivity.this.getComposeMessageFragment().onFragmentPop();
                    return;
                default:
                    return;
            }
        }
    };
    public int mPeekActionStatus = 1;
    private Drawable mReplyBackGround;
    private OnClickListener mReplyClickListener;
    private String mReplyString;
    private String mdeleteString;

    public static Intent createIntent(Context context, long threadId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        if (RcsComposeMessageActivityUtils.getHwCust() != null) {
            intent = RcsComposeMessageActivityUtils.getHwCust().setRcsConversationMode(context, intent);
        }
        if (threadId > 0) {
            intent.setData(Conversation.getUri(threadId));
        }
        return intent;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (this.mFragment != null) {
            this.mFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d("Mms_UI_CMA", "ComposeMessageActivity onCreate start");
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            MLog.d("Mms_UI_CMA", "ComposeMessageActivity redirect to PermissionCheck");
        } else if (!redirectToConversation()) {
            FragmentManager fm = getFragmentManager();
            if (savedInstanceState == null) {
                createConversationListFragment(fm);
            } else {
                this.mFragment = (ComposeMessageFragment) fm.findFragmentByTag("Mms_UI_CMF");
                if (this.mFragment == null) {
                    createConversationListFragment(fm);
                } else {
                    this.mFragment.setController(new ControllerImpl(this, this.mFragment));
                }
            }
            initPeekActionInfo();
        }
    }

    private void createConversationListFragment(FragmentManager fm) {
        this.mFragment = new ComposeMessageFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(16908290, this.mFragment, "Mms_UI_CMF");
        transaction.commit();
    }

    private boolean redirectToConversation() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        if (isFromWidget() && intent.getLongExtra("thread_id", -1) == -1 && !"android.intent.action.SENDTO".equals(getIntent().getAction())) {
            goToConversationList();
            return true;
        }
        if (!"multi-thread".equalsIgnoreCase(intent.getStringExtra("new_message_type"))) {
            return false;
        }
        goToConversationList(intent.getIntExtra("arg_number_type", 0));
        return true;
    }

    public void onBackPressed() {
        if (this.mFragment.onBackPressed()) {
            MLog.d("Mms_UI_CMA", "in CMA, CMF onBackPressed return true");
        } else {
            this.mFragment.exitComposeMessageActivity(new Runnable() {
                public void run() {
                    MLog.d("Mms_UI_CMA", "in CMA, isFromWidget() is " + ComposeMessageActivity.this.isFromWidget());
                    if (ComposeMessageActivity.this.isFromWidget()) {
                        ComposeMessageActivity.this.goToConversationList();
                        return;
                    }
                    Object result = ComposeMessageActivity.this.mWrapper.run("getIsPeeking");
                    if (result != null && ActivityExWrapper.IS_PRESS_SUPPORT && ((Boolean) result).booleanValue()) {
                        ComposeMessageActivity.this.mWrapper.run("onExit");
                    } else {
                        ComposeMessageActivity.this.finish();
                    }
                }
            }, true);
        }
    }

    private boolean isFromWidget() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra("fromWidget", false);
        }
        return false;
    }

    private boolean isFromNotify() {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getBooleanExtra("fromNotification", false);
        }
        return false;
    }

    public void goToConversationList() {
        MessageUtils.goToConversationList(this, true, -1, isFromNotify());
    }

    public void goToConversationList(int numberType) {
        if (numberType == 1) {
            BaseConversationListFragment.gotoNotificationList(this, 5, isFromNotify());
            finish();
        } else if (numberType == 2) {
            BaseConversationListFragment.gotoNotificationList(this, 4, isFromNotify());
            finish();
        } else {
            goToConversationList();
        }
    }

    public void onUserInteraction() {
        if (!(this.mFragment == null || this.mFragment.isDetached())) {
            this.mFragment.onUserInteraction();
        }
        super.onUserInteraction();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mFragment.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mFragment.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.mFragment.onWindowFocusChanged(hasFocus);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mFragment == null) {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                createConversationListFragment(fm);
            } else {
                return;
            }
        }
        this.mFragment.onNewIntent(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mFragment.isFromRcsContact() && resultCode == 0) {
            finish();
        } else {
            this.mFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void reSend(MessageItem msgItem) {
        this.mFragment.reSend(msgItem);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public static boolean isFromQuickAction(Intent intent) {
        if (intent != null && "QUICKACTION_QUICK_NEW_MESSAGE_VALUE".equals(intent.getStringExtra("QUICKACTION_QUICK_NEW_MESSAGE_KEY"))) {
            return true;
        }
        return false;
    }

    private Fragment getFragmentByTAG(String tag) {
        return getFragmentManager().findFragmentByTag(tag);
    }

    private void delayRelease() {
        this.mHandler.sendEmptyMessageDelayed(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, 200);
    }

    private void initPeekActionInfo() {
        this.mReplyString = getString(R.string.menu_reply);
        this.mdeleteString = getString(R.string.delete);
        this.mCallString = getString(R.string.menu_call);
        this.mReplyBackGround = getResources().getDrawable(R.drawable.quick_peek_reply, null);
        this.mDeleteBackGround = getResources().getDrawable(R.drawable.quick_peek_delete, null);
        this.mCallBackGround = getResources().getDrawable(R.drawable.quick_peek_call, null);
        this.mReplyClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                ComposeMessageActivity.this.getComposeMessageFragment().replyCurrentConversation(true);
                ComposeMessageActivity.this.delayRelease();
            }
        };
        this.mDeleteClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                ComposeMessageActivity.this.mWrapper.run("showConfirmPanel");
            }
        };
        this.mCallClickListener = new OnClickListener() {
            public void onClick(View arg0) {
                ComposeMessageActivity.this.getComposeMessageFragment().callCurrentConversation();
                ComposeMessageActivity.this.delayRelease();
            }
        };
    }

    private ComposeMessageFragment getComposeMessageFragment() {
        return (ComposeMessageFragment) getFragmentByTAG("Mms_UI_CMF");
    }

    public void setPeekActionStatus(int mPeekActionStatus) {
        this.mPeekActionStatus = mPeekActionStatus;
    }

    public void onFragmentRelease() {
        this.mWrapper.run("onRelease");
    }

    public void onFragmentExit() {
        this.mWrapper.run("onExit");
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
