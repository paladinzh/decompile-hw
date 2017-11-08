package com.android.mms.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.web.IXYSmartMessageActivity;
import cn.com.xy.sms.sdk.ui.popu.web.NearbyPointListFragment;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.attachment.datamodel.media.RichMessageManager;
import com.android.mms.ui.twopane.LeftPaneConversationListFragment;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsConversationList;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.HwListFragment;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import huawei.com.android.internal.widget.HwFragmentContainer;
import huawei.com.android.internal.widget.HwFragmentLayout;

public class ConversationList extends HwBaseActivity implements IXYSmartMessageActivity, OnLayoutChangeListener {
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && "ACTION_BROAD_CAST_FINISH".equals(intent.getAction())) {
                ConversationList.this.finish();
            }
        }
    };
    private TextView mCoverView;
    private boolean mFlagBackToLeft = false;
    private HwBaseFragment mFragment;
    private HwFragmentContainer mFragmentContainer;
    private HwFragmentLayout mFragmentLayout;
    private boolean mInSelectionMode = false;
    private boolean mIsLeftCovered;
    private boolean mIsPausing = false;
    private boolean mIsRightCovered;
    private RcsConversationList mRcsConversationList = new RcsConversationList();
    private HwBaseFragment mRightFragment;
    private HwListFragment mRightListFragment;
    private boolean mShouldRecord = true;
    private View mSmileFaceView;
    private ViewStub mSmileyFaceStub;
    private Intent mSplitIntent;
    private int mSplitRequestCode = -1;
    private int mSplitResultCode = -1;

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d("ConversationList", "ConversationList  onCreate start");
        if (HwMessageUtils.isSplitOn() && !OsUtil.hasRequiredPermissions()) {
            savedInstanceState = null;
        }
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            MLog.d("ConversationList", "ConversationList  redirect to PermissionCheck");
            return;
        }
        Intent intent = getIntent();
        MLog.d("ConversationList", "isSplitOn : " + HwMessageUtils.isSplitOn());
        if (HwMessageUtils.isSplitOn()) {
            requestWindowFeature(1);
            setContentView(R.layout.conversation_list_layout_split);
            findViewById(R.id.fragment_container).addOnLayoutChangeListener(this);
        } else {
            int runningMode = -1;
            if (intent != null) {
                runningMode = intent.getIntExtra("running_mode", -1);
            }
            if (OsUtil.isAppStart() && runningMode == 0) {
                View rootView = HwBackgroundLoader.getCachedConversationListViews(0);
                if (rootView != null) {
                    setContentView(rootView);
                } else {
                    setContentView(R.layout.conversation_list_layout);
                }
            } else {
                setContentView(R.layout.conversation_list_layout);
            }
        }
        MLog.d("ConversationList", "ConversationList intent is " + intent);
        if (intent != null) {
            String action = intent.getAction();
            MLog.d("ConversationList", "action : " + action);
            if ("android.intent.action.MAIN".equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    bundle = new Bundle();
                }
                bundle.putInt("running_mode", 0);
                intent.putExtras(bundle);
                setIntent(intent);
            }
        }
        if (HwMessageUtils.isSplitOn()) {
            this.mFragmentLayout = (HwFragmentLayout) findViewById(R.id.fragment_container);
            this.mFragmentContainer = new HwFragmentContainer(this, this.mFragmentLayout, getFragmentManager());
            this.mFragmentContainer.setSplitMode(3);
            this.mFragmentContainer.setCanMove(true);
        }
        if (this.mFragment == null) {
            this.mFragment = createConversationListFragment();
            if (HwMessageUtils.isSplitOn()) {
                this.mFragment.setIntent(intent);
                openLeftClearStack(this.mFragment);
            } else {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.conversation_list, this.mFragment, "cl_fragment_tag");
                transaction.commitAllowingStateLoss();
            }
        }
        intent = getIntent();
        if (intent != null && intent.getBooleanExtra("SELECTION_MODE", false)) {
            this.mInSelectionMode = true;
        }
        if (this.mRcsConversationList != null) {
            this.mRcsConversationList.onCreate();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mBroadcastReceiver, new IntentFilter("ACTION_BROAD_CAST_FINISH"));
        MLog.d("ConversationList", "ConversationList onCreate end ");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MLog.d("ConversationList", "create option menu");
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("fromNotification", false)) {
            finish();
            intent.setComponent(getComponentName());
            startActivity(intent);
            MLog.d("ConversationList", "Recrate activity for NotificationView");
        }
        if (this.mRcsConversationList != null) {
            this.mRcsConversationList.onNewIntent();
        }
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        this.mIsPausing = false;
        invalidateOptionsMenu();
        super.onResume();
        if (this.mRightFragment instanceof NearbyPointListFragment) {
            ((NearbyPointListFragment) this.mRightFragment).onResume();
        }
        if (this.mFlagBackToLeft && HwMessageUtils.isSplitOn() && !isSplitState()) {
            this.mFragmentContainer.setSelectedContainer(0);
            this.mFlagBackToLeft = false;
        }
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        if (this.mShouldRecord) {
            this.mShouldRecord = false;
            StatisticalHelper.incrementReportCount(MmsApp.getApplication(), 2116);
        } else {
            this.mShouldRecord = true;
        }
        super.onMenuOpened(featureId, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFinishing()) {
            return true;
        }
        MLog.d("ConversationList", "prepare option menu in landscape");
        menu.setGroupVisible(R.id.mms_options, true);
        this.mFragment.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mFragment.onOptionsItemSelected(item);
    }

    public boolean isLeftCovered() {
        return this.mIsLeftCovered;
    }

    public void onBackPressed() {
        if (HwMessageUtils.isSplitOn()) {
            if (!this.mIsRightCovered || this.mFragment == null || !this.mFragment.onBackPressed()) {
                if (this.mRightListFragment != null && this.mRightListFragment.onBackPressed()) {
                    MLog.d("ConversationList", "finish list right onbackpressed");
                } else if (this.mRightFragment != null && ((this.mIsLeftCovered || this.mRightFragment.needHidePanel()) && this.mRightFragment.onBackPressed())) {
                    MLog.d("ConversationList", "finish right onbackpressed");
                } else if (this.mFragment == null || !this.mFragment.onBackPressed()) {
                    if ((this.mRightFragment instanceof ComposeMessageFragment) && ((ComposeMessageFragment) this.mRightFragment).mComposeRecipientsView.isVisible()) {
                        ((ComposeMessageFragment) this.mRightFragment).mComposeRecipientsView.commitNumberChip();
                        ((ComposeMessageFragment) this.mRightFragment).mRichEditor.setWorkingRecipients(((ComposeMessageFragment) this.mRightFragment).mComposeRecipientsView.getNumbers());
                    }
                    MLog.d("ConversationList", "start container onbackpressed");
                    if (this.mFragmentContainer.isBackPressed()) {
                        MLog.d("ConversationList", "this instance of NotificationList:" + (this instanceof NotificationList));
                        if ((this instanceof NotificationList) || (this instanceof ConversationEditor)) {
                            Intent intent = new Intent(this, ConversationList.class);
                            intent.setAction("android.intent.action.MAIN");
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        } else if (isInMultiWindowMode()) {
                            moveTaskToBack(false);
                        } else {
                            finish();
                        }
                    }
                    if (!isSplitState() && isRightPaneOnTop() && isLeftContainerSelected()) {
                        clearRightInBackStack();
                    }
                } else {
                    MLog.d("ConversationList", "finish left onbackpressed");
                }
            }
        } else if (!(this.mIsPausing || this.mFragment.onBackPressed())) {
            try {
                super.onBackPressed();
            } catch (IllegalStateException e) {
                MLog.e("ConversationList", "catch a IllegalStateException when onBackPressed is called");
            }
        }
    }

    public HwBaseFragment createConversationListFragment() {
        if (HwMessageUtils.isSplitOn()) {
            return new LeftPaneConversationListFragment();
        }
        return new ConversationListFragment();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != 4 || !isListInEditMode()) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == 1) {
            onBackPressed();
        }
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        if (HwMessageUtils.isSplitOn() && keyCode == 82 && this.mFragment != null && (this.mFragment instanceof BaseConversationListFragment) && duration < ((long) ViewConfiguration.getLongPressTimeout()) && ((BaseConversationListFragment) this.mFragment).handleKeyEvent(82)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public HwFragmentContainer getFragmentContainer() {
        return this.mFragmentContainer;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        MLog.d("ConversationList", "dispatchTouchEvent  ev.getPointerCount()=" + ev.getPointerCount() + "  ev.getActionIndex()=" + ev.getActionIndex());
        if (ev.getPointerCount() > 1 && ev.getActionIndex() != 0) {
            return false;
        }
        switch (ev.getAction()) {
            case 0:
                if ((this.mRightFragment instanceof RightPaneComposeMessageFragment) && HwMessageUtils.isSplitOn()) {
                    if (((RightPaneComposeMessageFragment) this.mRightFragment).mActionMode == null) {
                        if (!(!(this.mFragment instanceof BaseConversationListFragment) || ((RightPaneComposeMessageFragment) this.mRightFragment).getConversationInputManager() == null || ((BaseConversationListFragment) this.mFragment).getListView() == null)) {
                            if (!((RightPaneComposeMessageFragment) this.mRightFragment).getConversationInputManager().isMediaPickerVisible()) {
                                ((BaseConversationListFragment) this.mFragment).getListView().setVerticalScrollBarEnabled(true);
                                break;
                            }
                            ((BaseConversationListFragment) this.mFragment).getListView().setVerticalScrollBarEnabled(false);
                            break;
                        }
                    }
                    ((RightPaneComposeMessageFragment) this.mRightFragment).mActionMode.finish();
                    ((RightPaneComposeMessageFragment) this.mRightFragment).mActionMode = null;
                    return true;
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void onDestroy() {
        SmartSmsSdkUtil.setNoShowEnhanceDialog(false);
        SmartSmsSdkUtil.setNoShowUpdateDialog(false);
        if (this.mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBroadcastReceiver);
        }
        if (ConversationList.class.getCanonicalName().equals(getPackageName() + "." + getLocalClassName())) {
            SmartSmsSdkUtil.resetSmartSmsState();
        }
        HwBackgroundLoader.clearConvListViewCache();
        if (HwMessageUtils.isSplitOn()) {
            RichMessageManager.get().removeRichMessageManager(hashCode());
        }
        super.onDestroy();
    }

    protected void onPause() {
        this.mIsPausing = true;
        super.onPause();
    }

    public HwBaseFragment getFragment() {
        return this.mFragment;
    }

    public void setRightFragment(Fragment f) {
        if (f instanceof HwBaseFragment) {
            this.mRightFragment = (HwBaseFragment) f;
            this.mRightListFragment = null;
        } else if (f instanceof HwListFragment) {
            this.mRightListFragment = (HwListFragment) f;
            this.mRightFragment = null;
        }
    }

    public HwBaseFragment getRightFragment() {
        return this.mRightFragment;
    }

    public void openLeftClearStack(HwBaseFragment fragment) {
        MLog.d("ConversationList", "openLeftClearStack");
        this.mFragmentContainer.openLeftClearStack(fragment);
    }

    public void openRightClearStack(HwBaseFragment fragment) {
        MLog.d("ConversationList", "openRightClearStack");
        if (this.mIsRightCovered) {
            showOrHideRightCover();
        }
        this.mFragmentContainer.openRightClearStack(fragment);
        this.mRightFragment = fragment;
    }

    public void openRightClearStack(Fragment fragment) {
        MLog.d("ConversationList", "openRightClearStack");
        if (this.mIsRightCovered) {
            showOrHideRightCover();
        }
        this.mFragmentContainer.openRightClearStack(fragment);
        setRightFragment(fragment);
    }

    public void changeRightAddToStack(HwBaseFragment nextFragment, HwBaseFragment currentFragment) {
        MLog.d("ConversationList", "changeRightAddToStack");
        this.mFragmentContainer.changeRightAddToStack(nextFragment, currentFragment);
        this.mRightFragment = nextFragment;
    }

    public void changeRightAddToStack(Fragment nextFragment, Fragment currentFragment) {
        MLog.d("ConversationList", "changeRightAddToStack");
        this.mFragmentContainer.changeRightAddToStack(nextFragment, currentFragment);
        setRightFragment(nextFragment);
    }

    public void changeRightAddToStack(HwBaseFragment nextFragment) {
        MLog.d("ConversationList", "changeRightAddToStack");
        this.mFragmentContainer.changeRightAddToStack(nextFragment, this.mRightFragment);
        this.mRightFragment = nextFragment;
    }

    public void changeRightAddToStack(Fragment nextFragment) {
        MLog.d("ConversationList", "changeRightAddToStack");
        this.mFragmentContainer.changeRightAddToStack(nextFragment, this.mRightFragment);
        setRightFragment(nextFragment);
    }

    public void showOrHideLeftCover() {
        boolean z;
        initCoverView();
        if (this.mIsLeftCovered) {
            this.mFragmentContainer.getLeftLayout().removeView(this.mCoverView);
        } else {
            this.mFragmentContainer.getLeftLayout().addView(this.mCoverView);
        }
        if (this.mIsLeftCovered) {
            z = false;
        } else {
            z = true;
        }
        this.mIsLeftCovered = z;
    }

    public void showOrHideRightCover() {
        boolean z;
        initCoverView();
        ViewGroup parent = (ViewGroup) this.mCoverView.getParent();
        if (parent != null) {
            parent.removeView(this.mCoverView);
        }
        if (this.mIsRightCovered) {
            this.mFragmentContainer.getRightLayout().removeView(this.mCoverView);
        } else {
            this.mFragmentContainer.getRightLayout().addView(this.mCoverView);
        }
        if (this.mIsRightCovered) {
            z = false;
        } else {
            z = true;
        }
        this.mIsRightCovered = z;
    }

    public void showRightCover() {
        initCoverView();
        ViewGroup parent = (ViewGroup) this.mCoverView.getParent();
        if (parent != null) {
            parent.removeView(this.mCoverView);
        }
        if (!this.mIsRightCovered) {
            boolean z;
            this.mFragmentContainer.getRightLayout().addView(this.mCoverView);
            if (this.mIsRightCovered) {
                z = false;
            } else {
                z = true;
            }
            this.mIsRightCovered = z;
        }
    }

    public void showLeftCover() {
        initCoverView();
        if (!this.mIsLeftCovered) {
            this.mFragmentContainer.getLeftLayout().addView(this.mCoverView);
            this.mIsLeftCovered = !this.mIsLeftCovered;
        }
    }

    public void hideLeftCover() {
        if (this.mCoverView != null && this.mIsLeftCovered) {
            boolean z;
            this.mFragmentContainer.getLeftLayout().removeView(this.mCoverView);
            if (this.mIsLeftCovered) {
                z = false;
            } else {
                z = true;
            }
            this.mIsLeftCovered = z;
        }
    }

    private void initCoverView() {
        if (this.mCoverView == null) {
            this.mCoverView = new TextView(getApplicationContext());
            this.mCoverView.setId(R.id.cover_view);
            this.mCoverView.setText("");
            this.mCoverView.setLayoutParams(new LayoutParams(-1, -1));
            this.mCoverView.setBackground(getResources().getDrawable(R.drawable.tmplate_gray));
            this.mCoverView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (HwMessageUtils.isSplitOn()) {
                        ConversationList.this.hideInputMode();
                    }
                    if (ConversationList.this instanceof ConversationEditor) {
                        Intent intent = new Intent(ConversationList.this, ConversationList.class);
                        intent.setAction("android.intent.action.MAIN");
                        ConversationList.this.startActivity(intent);
                        ConversationList.this.overridePendingTransition(0, 0);
                        ConversationList.this.finish();
                    }
                    ConversationList.this.onBackPressed();
                }
            });
        }
    }

    private void hideInputMode() {
        try {
            ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 2);
        } catch (Exception e) {
            MLog.w("ConversationList", e.toString());
        }
    }

    public boolean isNeedToCreateRightMessge() {
        if (this.mFragmentContainer.getLeftRightBackStackCount()[1] == 0) {
            return true;
        }
        return false;
    }

    public boolean isRightPaneOnTop() {
        if (this.mFragmentContainer.getLeftRightBackStackCount()[1] == 1) {
            return true;
        }
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (HwMessageUtils.isSplitOn() && this.mIsRightCovered && !isSplitState()) {
            this.mFragmentContainer.setSelectedContainer(0);
        }
    }

    public void setSplitResultData(int reqCode, int resCode, Intent i) {
        this.mSplitRequestCode = reqCode;
        this.mSplitResultCode = resCode;
        this.mSplitIntent = i;
    }

    public void resetSplitResultData() {
        this.mSplitRequestCode = -1;
        this.mSplitResultCode = -1;
        this.mSplitIntent = null;
    }

    public int getSplitRequestCode() {
        return this.mSplitRequestCode;
    }

    public int getSplitResultCode() {
        return this.mSplitResultCode;
    }

    public Intent getSplitIntent() {
        return this.mSplitIntent;
    }

    public boolean isSplitState() {
        return 2 == this.mFragmentContainer.getColumnsNumber(getResources().getConfiguration().orientation, getWindowManager().getDefaultDisplay().getWidth());
    }

    public void finshFragemnt(Fragment fragement) {
        onBackPressed();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mRightFragment instanceof NearbyPointListFragment) {
            ((NearbyPointListFragment) this.mRightFragment).onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showNextConversation() {
        if (this.mFragment instanceof LeftPaneConversationListFragment) {
            ((LeftPaneConversationListFragment) this.mFragment).showNextConversation();
        }
    }

    public void clearRightInBackStack() {
        getFragmentManager().popBackStackImmediate("left_container", 0);
        this.mFragmentContainer.setSelectedContainer(0);
    }

    public void resetSelectedItem() {
        if (this.mFragment instanceof LeftPaneConversationListFragment) {
            ((LeftPaneConversationListFragment) this.mFragment).resetSelectedItem();
        }
    }

    public boolean isLeftContainerSelected() {
        return this.mFragmentContainer.getSelectedContainer() == 0;
    }

    public void backToListWhenSplit() {
        if (this.mFragment instanceof LeftPaneConversationListFragment) {
            ((LeftPaneConversationListFragment) this.mFragment).backToListWhenSplit();
        }
    }

    private boolean isListInEditMode() {
        if (this.mFragment instanceof BaseConversationListFragment) {
            return ((BaseConversationListFragment) this.mFragment).isInEditMode();
        }
        return false;
    }

    public void setFlagBackToLeft(boolean isBack) {
        this.mFlagBackToLeft = isBack;
    }

    public void updateGroupBottomStatus() {
        if (this.mRightFragment instanceof RcsGroupChatComposeMessageFragment) {
            ((RcsGroupChatComposeMessageFragment) this.mRightFragment).updateGroupStatus();
        }
    }

    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (bottom != oldBottom && !(this instanceof ConversationEditor)) {
            updateLeftSplitActionbarVisibility();
        }
    }

    public void updateLeftSplitActionbarVisibility() {
        int inputMethodHeight = 0;
        boolean z = false;
        boolean isMediaPickerShow = false;
        try {
            inputMethodHeight = InputMethodManager.getInstance().getInputMethodWindowVisibleHeight();
        } catch (Exception e) {
            MLog.w("ConversationList", e.toString());
        }
        if (this.mRightFragment instanceof RightPaneComposeMessageFragment) {
            z = ((RightPaneComposeMessageFragment) this.mRightFragment).isSmileFaceVisiable();
            isMediaPickerShow = ((RightPaneComposeMessageFragment) this.mRightFragment).isMediaPickerVisible();
        }
        if (HwMessageUtils.isSplitOn() && isSplitState() && this.mFragment != null) {
            boolean z2;
            if (inputMethodHeight != 0 || r3 || r2) {
                z2 = false;
            } else {
                z2 = true;
            }
            updateLeftSplitActionbar(z2);
        }
    }

    public void updateLeftSplitActionbar(boolean show) {
        if (this.mFragment instanceof LeftPaneConversationListFragment) {
            ((LeftPaneConversationListFragment) this.mFragment).updateSplitActionbar(show);
        }
    }

    public View findSmileyFaceStub() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mSmileyFaceStub = (ViewStub) findViewById(R.id.smileyfaceview_split);
        } else {
            this.mSmileyFaceStub = (ViewStub) findViewById(R.id.smiley_face_view_emoji_split);
        }
        this.mSmileFaceView = this.mSmileyFaceStub.inflate();
        return this.mSmileFaceView;
    }

    public ViewStub getSmileyFaceStub() {
        return this.mSmileyFaceStub;
    }

    public View getSmileyFaceView() {
        return this.mSmileFaceView;
    }

    public void reSend(MessageItem msgItem) {
        if (this.mRightFragment instanceof ComposeMessageFragment) {
            ((ComposeMessageFragment) this.mRightFragment).reSend(msgItem);
        }
    }
}
