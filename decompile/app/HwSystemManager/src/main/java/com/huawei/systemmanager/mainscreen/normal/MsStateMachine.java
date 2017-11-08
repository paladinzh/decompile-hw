package com.huawei.systemmanager.mainscreen.normal;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.statmachine.IState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleState;
import com.huawei.systemmanager.comm.widget.statmachine.SimpleStateMachine;
import com.huawei.systemmanager.mainscreen.SettingActivity;
import com.huawei.systemmanager.mainscreen.detector.DetectTaskManager;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTask;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskListener;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskListener.SimpleDetectTaskListener;
import com.huawei.systemmanager.mainscreen.view.MainCircleProgressView;
import com.huawei.systemmanager.mainscreen.view.MainScreenRollingView;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicInteger;

public class MsStateMachine extends SimpleStateMachine implements OnClickListener {
    private static final int DETECT_RESULT_REQUEST_CODE = 2;
    private static final int INIT_SCORE = 30;
    private static final int INIT_SCROE_ROLLING_TIME = 2000;
    public static final int MAX_SCORE = 100;
    private static final int MSG_BACK_FROM_DETECT_RESULT = 27;
    private static final int MSG_BLUETOOTH_CHANGED = 42;
    private static final int MSG_DETECT_FINISH = 23;
    private static final int MSG_DETECT_PROGRESS_CHANGE = 22;
    private static final int MSG_DETECT_START = 21;
    static final int MSG_ON_PAUSE = 6;
    static final int MSG_ON_RESUME = 5;
    private static final int MSG_SCORE_CHANGED = 25;
    private static final int MSG_START_DETECT = 30;
    private static final int MSG_WIFI_CHANGED = 44;
    static final int MSG_WINDOW_FOCUS_CHANGED = 10;
    private static final int PROBRESS_ROLL_TIME = 200;
    private static final int SCORE_ROLL_TIME = 600;
    private static final int SCROE_ROLLING_TIME = 800;
    private static final int SHADE_REFRENCE = 99;
    public static final long START_DETECT_TASK_DELAY = 600;
    public static final String TAG = "MsStateMachine";
    private Animation animationCome;
    private Animation animationGo;
    private Animation animationGoUnit;
    private DetectTaskManager detectManager;
    private boolean isChinese;
    private Activity mActivity;
    private MainCircleProgressView mCircleImage;
    private View mContainer;
    private DetectTaskListener mDetectListener = new SimpleDetectTaskListener() {
        public void onStart(DetectTask task) {
            HwLog.i(MsStateMachine.TAG, "detectlistener onStart");
            MsStateMachine.this.sendMessage(21);
        }

        public void onItemFount(DetectTask task, DetectItem item) {
        }

        public void onProgressChange(DetectTask task, String itemName, float progress) {
            MsStateMachine.this.sendMessage(22, itemName, (int) progress, 0);
        }

        public void onItemScoreChange(int score) {
            HwLog.i(MsStateMachine.TAG, "onItemScoreChange, score:" + score);
            MsStateMachine.this.notifyScoreChanged(score);
        }

        public void onTaskFinish(DetectTask task) {
            HwLog.i(MsStateMachine.TAG, "onTaskFinish ");
            MsStateMachine.this.sendMessage(22, "", 100, 0);
            MsStateMachine.this.sendMessageDelay(23, 400);
        }
    };
    private Fragment mFragment;
    private IState mInitialState = new InitState();
    private boolean mIsPercentTextSeparate;
    private Button mOptimizeButton;
    private PkgRemoveReceiver mPkgRemoveReceiver = new PkgRemoveReceiver() {
        private static final long BLUETOOTH_NOTIFYCHANGE_DELAY = 200;
        private static final long WIFI_NOTIFY_DELAY = 800;

        protected void doPkgRemove(String pkgName) {
            if (MsStateMachine.this.detectManager != null && MsStateMachine.this.detectManager.handlerPkgRemove(pkgName)) {
                HwLog.i(MsStateMachine.TAG, "item state changed when pkg removed:" + pkgName);
            }
        }

        protected void doBluetoothStateChange() {
            MsStateMachine.this.removeMessage(42);
            MsStateMachine.this.sendMessageDelay(42, 200);
        }

        protected void doWifiStateChange() {
            MsStateMachine.this.removeMessage(44);
            MsStateMachine.this.sendMessageDelay(44, WIFI_NOTIFY_DELAY);
        }

        protected void doFoundVirusscanApp(Intent intent) {
            if (MsStateMachine.this.detectManager != null && MsStateMachine.this.detectManager.handlerReceiveVirusScanApp(intent)) {
                HwLog.i(MsStateMachine.TAG, "handler found virusscan app");
            }
        }
    };
    private TextView mProgressUnit;
    private MainScreenRollingView mProgressView;
    private IState mScanEndState = new ScanEndState();
    private IState mScanningState = new ScanningState();
    private AtomicInteger mScore = new AtomicInteger(100);
    private TextView mScoreUnit;
    private MainScreenRollingView mScoreView;
    private ImageView mSettingBtn;
    private SwitchViewController mSwitcher;

    public class MsState extends SimpleState {
        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    HwLog.i(MsStateMachine.TAG, "receive msg on resume");
                    MsStateMachine.this.mSwitcher.onResume();
                    break;
                case 6:
                    HwLog.i(MsStateMachine.TAG, "receive msg on pause");
                    MsStateMachine.this.mSwitcher.onPause();
                    break;
                case 10:
                    HwLog.i(MsStateMachine.TAG, "receive msg onwidowfouces changed");
                    if (MsStateMachine.this.detectManager == null) {
                        HwLog.i(MsStateMachine.TAG, "detectManager is null");
                        break;
                    }
                    MsStateMachine.this.detectManager.refreshItem();
                    break;
                case 42:
                    if (MsStateMachine.this.detectManager != null) {
                        MsStateMachine.this.detectManager.refreshBluetooth();
                        break;
                    }
                    break;
                case 44:
                    if (MsStateMachine.this.detectManager != null) {
                        MsStateMachine.this.detectManager.refreshWifi();
                        break;
                    }
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }
    }

    public class InitState extends MsState {
        public InitState() {
            super();
        }

        public void enter() {
            MsStateMachine.this.mProgressView.setNumberByDuration(30, 2000);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 30:
                    MsStateMachine.this.transitionTo(MsStateMachine.this.mScanningState);
                    return true;
                default:
                    return super.processMessage(msg);
            }
        }
    }

    public class ScanEndState extends MsState {
        public ScanEndState() {
            super();
        }

        public void enter() {
            MsStateMachine.this.mOptimizeButton.setEnabled(true);
            updateStateByScore(true);
            MsStateMachine.this.mSwitcher.setDetectManager(MsStateMachine.this.detectManager);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 25:
                    updateStateByScore(true);
                    break;
                case 27:
                    MsStateMachine.this.mScore.set(msg.arg1);
                    updateStateByScore(false);
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }

        private void updateStateByScore(boolean anima) {
            boolean health = true;
            int score = MsStateMachine.this.mScore.get();
            HwLog.i(MsStateMachine.TAG, "updateStateByScore, score is:" + score);
            if (anima) {
                MsStateMachine.this.mScoreView.setNumberQuick(score);
            } else {
                MsStateMachine.this.mScoreView.setNumberImmediately(score);
            }
            MsStateMachine.this.mCircleImage.updateScore(score);
            if (MsStateMachine.getStates(score) != 1) {
                health = false;
            }
            if (health) {
                MsStateMachine.this.mOptimizeButton.setEnabled(false);
            } else {
                Utility.setViewEnabled(MsStateMachine.this.mOptimizeButton, Utility.isOwnerUser(false));
            }
        }
    }

    public class ScanningState extends MsState {
        int mProgress;

        public ScanningState() {
            super();
        }

        public void enter() {
            this.mProgress = 0;
            MsStateMachine.this.detectManager = DetectTaskManager.create();
            MsStateMachine.this.detectManager.startDetectTask(GlobalContext.getContext(), MsStateMachine.this.mDetectListener);
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 22:
                    if (msg.arg1 > this.mProgress) {
                        this.mProgress = msg.arg1;
                        if (this.mProgress > 99) {
                            MsStateMachine.this.mCircleImage.isShading = true;
                        }
                        if (this.mProgress >= 30) {
                            MsStateMachine.this.mProgressView.setNumberByDuration(this.mProgress, 200);
                            break;
                        }
                    }
                    break;
                case 23:
                    final int score = MsStateMachine.this.mScore.get();
                    HwLog.i(MsStateMachine.TAG, "Scan finished! score:" + score + ", health:" + MsStateMachine.getStates(score));
                    MsStateMachine.this.mCircleImage.setCompleteStatus();
                    MsStateMachine.this.animationGo.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation a) {
                            MsStateMachine.this.mCircleImage.updateSocre(score, 600);
                            MsStateMachine.this.mScoreView.setNumberImmediately(score);
                            MsStateMachine.this.mScoreView.setVisibility(0);
                            MsStateMachine.this.mScoreView.startAnimation(MsStateMachine.this.animationCome);
                            if (MsStateMachine.this.isChinese) {
                                MsStateMachine.this.mScoreUnit.setVisibility(0);
                                MsStateMachine.this.mScoreUnit.startAnimation(MsStateMachine.this.animationCome);
                            }
                            MsStateMachine.this.mProgressUnit.startAnimation(MsStateMachine.this.animationGoUnit);
                        }

                        public void onAnimationRepeat(Animation a) {
                        }

                        public void onAnimationEnd(Animation a) {
                            MsStateMachine.this.mProgressView.setVisibility(8);
                            MsStateMachine.this.mProgressUnit.setVisibility(8);
                        }
                    });
                    MsStateMachine.this.mProgressView.startAnimation(MsStateMachine.this.animationGo);
                    MsStateMachine.this.transitionTo(MsStateMachine.this.mScanEndState);
                    break;
                case 25:
                    MsStateMachine.this.mScoreView.setNumberByDuration(MsStateMachine.this.mScore.get(), MsStateMachine.SCROE_ROLLING_TIME);
                    break;
                default:
                    return super.processMessage(msg);
            }
            return true;
        }
    }

    public MsStateMachine(Fragment frag, View container, Looper looper) {
        boolean z = false;
        super(TAG, looper);
        this.mFragment = frag;
        this.mActivity = this.mFragment.getActivity();
        this.mContainer = container;
        Activity ac = frag.getActivity();
        if (HSMConst.getDeviceSize() < HSMConst.DEVICE_SIZE_80) {
            this.mSwitcher = new SwitchViewController(ac, this.mContainer);
        } else {
            this.mSwitcher = new SwitchViewControllerForPad(ac, this.mContainer);
        }
        this.mCircleImage = (MainCircleProgressView) container.findViewById(R.id.scan_image);
        this.mScoreView = (MainScreenRollingView) container.findViewById(R.id.score);
        this.mProgressView = (MainScreenRollingView) container.findViewById(R.id.progress);
        this.mScoreUnit = (TextView) container.findViewById(R.id.score_unit);
        this.mProgressUnit = (TextView) container.findViewById(R.id.progress_unit);
        this.animationGo = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_roll_stepone);
        this.animationGoUnit = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_roll_stepone);
        this.animationCome = AnimationUtils.loadAnimation(ac, R.anim.mainscreen_roll_steptwo);
        this.mIsPercentTextSeparate = this.mActivity.getResources().getBoolean(R.bool.is_percent_text_separate);
        this.isChinese = this.mActivity.getResources().getBoolean(R.bool.mainscreen_show_score);
        this.mProgressUnit.setVisibility(this.mIsPercentTextSeparate ? 0 : 8);
        MainScreenRollingView mainScreenRollingView = this.mProgressView;
        if (!this.mIsPercentTextSeparate) {
            z = true;
        }
        mainScreenRollingView.mIncludePercent = z;
        this.mSettingBtn = (ImageView) this.mContainer.findViewById(R.id.setting_menu_button);
        this.mSettingBtn.setOnClickListener(this);
        this.mOptimizeButton = (Button) this.mContainer.findViewById(R.id.optimize_button);
        this.mOptimizeButton.setOnClickListener(this);
        setInitialState(this.mInitialState);
    }

    protected void defaultHandlerMessage(Message msg) {
        super.defaultHandlerMessage(msg);
    }

    private void jumpToResultActivity() {
        HwLog.i(TAG, "user click button, current state is:" + getName());
        Activity ac = this.mFragment.getActivity();
        if (ac != null) {
            Intent intent = new Intent(ac, DetectResultActivity.class);
            intent.putExtra(DetectResultActivity.KEY_DETECTOR_ID, this.detectManager.getId());
            try {
                this.mFragment.startActivityForResult(intent, 2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void jumpToSettingActivity() {
        try {
            this.mFragment.startActivity(new Intent(this.mActivity, SettingActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Context getContext() {
        return GlobalContext.getContext();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_menu_button:
                jumpToSettingActivity();
                return;
            case R.id.optimize_button:
                if (Utility.isOwnerUser()) {
                    jumpToResultActivity();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void start() {
        super.start();
        this.mSwitcher.registerOverseaReceiver();
        this.mPkgRemoveReceiver.registeReceiver();
    }

    public void quit() {
        super.quit();
        this.mPkgRemoveReceiver.unRegisteReceiver();
        if (this.detectManager != null) {
            this.detectManager.destory();
        }
        this.mSwitcher.release(this.mActivity);
    }

    private void notifyScoreChanged(int score) {
        if (this.mScore.getAndSet(score) != score) {
            sendEmptyMessage(25);
        }
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            HwLog.i(TAG, "backfrom detect result actvity, resultCode:" + requestCode);
            if (this.detectManager != null) {
                this.detectManager.setCouldRefresh(true);
            }
            sendMessage(27, resultCode);
        }
    }

    private static int getStates(int score) {
        if (score >= 100) {
            return 1;
        }
        if (score > 75) {
            return 2;
        }
        return 3;
    }

    public void startDetect() {
        sendMessageDelay(30, 600);
    }

    public void refreshScreenOrientation(Configuration configuration) {
        this.mSwitcher.refreshScreenOrientation(configuration);
    }
}
