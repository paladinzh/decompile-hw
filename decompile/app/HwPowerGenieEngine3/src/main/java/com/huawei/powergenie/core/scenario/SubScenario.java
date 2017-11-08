package com.huawei.powergenie.core.scenario;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.ScenarioAction;
import com.huawei.powergenie.integration.eventhub.HookEvent;

public class SubScenario {
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final ScenarioService mIScenario;
    private boolean mIsFlinging = false;
    private boolean mIsIdling = false;
    private SubPhoneStateHandler mSubPhoneStateHandler;

    private class SubPhoneStateHandler extends Handler {
        private SubPhoneStateHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 300:
                    SubScenario.this.cancelFlingStop();
                    return;
                case 301:
                    SubScenario.this.InIdleState();
                    return;
                case 302:
                    SubScenario.this.stopSpeedUp();
                    return;
                case 303:
                    SubScenario.this.checkLittleCamera();
                    return;
                default:
                    return;
            }
        }
    }

    protected SubScenario(ICoreContext coreContext, ScenarioService iScenario) {
        this.mICoreContext = coreContext;
        this.mIScenario = iScenario;
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mSubPhoneStateHandler = new SubPhoneStateHandler();
    }

    private boolean isSubState(int actionId) {
        if (PowerAction.mSubActionMap.containsKey(Integer.valueOf(actionId))) {
            return true;
        }
        return PowerAction.mSubActionMap.containsValue(Integer.valueOf(actionId));
    }

    protected void handleEvent(HookEvent event) {
        int eventId = event.getEventId();
        if (174 == eventId) {
            boolean down = "down".equals(event.getPkgName());
            if (down) {
                userActivity();
            } else {
                checkIdleState();
            }
            int keyCode = Integer.parseInt(event.getValue1());
            if (down && this.mICoreContext.isScreenOff() && keyCode == 26) {
                startSpeedUp(newStateAction(508, "power_key_speed"));
            }
        } else if (113 == eventId) {
            appChange();
        } else if (126 == eventId) {
            checkIdleState();
            handleSubState(newStateAction(232, "touch_up"));
        } else if (125 == eventId) {
            userActivity();
            handleSubState(newStateAction(231, "touch_down"));
        }
    }

    protected boolean handleSubState(ScenarioAction action) {
        int actionId = action.getActionId();
        if (!isSubState(actionId)) {
            return false;
        }
        cancleIdleState();
        switch (actionId) {
            case 267:
                if (!this.mIsFlinging) {
                    dispatchSubStateAction(action);
                    checkFlingStop();
                    break;
                }
                break;
            case 506:
                if (!this.mSubPhoneStateHandler.hasMessages(303)) {
                    Message msg = Message.obtain();
                    msg.what = 303;
                    this.mSubPhoneStateHandler.sendMessageDelayed(msg, 500);
                    break;
                }
                break;
            case 507:
                if (this.mSubPhoneStateHandler.hasMessages(303)) {
                    this.mSubPhoneStateHandler.removeMessages(303);
                }
                dispatchSubStateAction(action);
                break;
            case 508:
                startSpeedUp(action);
                break;
            case 509:
                if (this.mSubPhoneStateHandler.hasMessages(302)) {
                    this.mSubPhoneStateHandler.removeMessages(302);
                    dispatchSubStateAction(action);
                    break;
                }
                break;
            default:
                dispatchSubStateAction(action);
                break;
        }
        return true;
    }

    private void dispatchSubStateAction(ScenarioAction action) {
        int subFlag = getActionSubFlag(action.getActionId());
        try {
            action.updateFlag(subFlag);
            if (subFlag == 1) {
                this.mIScenario.notifySubStateAction(action);
            } else if (subFlag == 2) {
                this.mIScenario.notifySubStateAction(action);
            }
        } catch (Exception e) {
            Log.e("SubScenario", "Exception:", e);
        }
    }

    private void userActivity() {
        removeCheckIdleState();
        cancleIdleState();
    }

    private void appChange() {
        if (this.mICoreContext.isScreenOff()) {
            startSpeedUp(newStateAction(508, "app_speed"));
        }
    }

    private boolean canIdle() {
        if (this.mIScenario.isFullScreen() || this.mICoreContext.isScreenOff() || this.mIScenario.isPlayingVideo() || this.mIScenario.isCameraStart()) {
            return false;
        }
        return true;
    }

    private void checkIdleState() {
        if (canIdle() && !this.mSubPhoneStateHandler.hasMessages(301)) {
            Message msg = Message.obtain();
            msg.what = 301;
            this.mSubPhoneStateHandler.sendMessageDelayed(msg, 10000);
        }
    }

    private void removeCheckIdleState() {
        if (this.mSubPhoneStateHandler.hasMessages(301)) {
            this.mSubPhoneStateHandler.removeMessages(301);
        }
    }

    private ScenarioAction newStateAction(int id, String name) {
        ScenarioAction stAction = ScenarioAction.obtain();
        if (stAction != null) {
            stAction.resetAs(id, name, 0, System.currentTimeMillis(), "");
        }
        return stAction;
    }

    private void InIdleState() {
        if (!this.mIsIdling && canIdle() && !this.mICoreContext.isScreenOff()) {
            dispatchSubStateAction(newStateAction(500, "idle_start"));
            this.mIsIdling = true;
        }
    }

    private void cancleIdleState() {
        if (this.mIsIdling) {
            dispatchSubStateAction(newStateAction(501, "idle_stop"));
            this.mIsIdling = false;
        }
    }

    private void startSpeedUp(ScenarioAction action) {
        if (!this.mSubPhoneStateHandler.hasMessages(302)) {
            dispatchSubStateAction(action);
            Message msg = Message.obtain();
            msg.what = 302;
            this.mSubPhoneStateHandler.sendMessageDelayed(msg, 1000);
        }
    }

    private void stopSpeedUp() {
        dispatchSubStateAction(newStateAction(509, "speed_end"));
    }

    private void checkFlingStop() {
        this.mIsFlinging = true;
        Message msg = Message.obtain();
        msg.what = 300;
        this.mSubPhoneStateHandler.sendMessageDelayed(msg, 500);
    }

    private void cancelFlingStop() {
        dispatchSubStateAction(newStateAction(268, "fling_end"));
        this.mIsFlinging = false;
    }

    private void checkLittleCamera() {
        if (!this.mIScenario.isFullScreen()) {
            dispatchSubStateAction(newStateAction(506, "little_camera_start"));
        }
    }

    private int getActionSubFlag(int actionID) {
        if (PowerAction.mSubActionMap.containsKey(Integer.valueOf(actionID))) {
            return 1;
        }
        if (PowerAction.mSubActionMap.containsValue(Integer.valueOf(actionID))) {
            return 2;
        }
        return 3;
    }
}
