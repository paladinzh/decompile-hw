package com.huawei.powergenie.core.contextaware;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.IContextAware;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.UserAction;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import java.util.HashMap;

public final class UserStateManager extends BaseService implements IContextAware {
    private static HashMap<String, Integer> mUserStatesIds = new HashMap<String, Integer>() {
        {
            put("STATIONARY", Integer.valueOf(5));
            put("WALKING", Integer.valueOf(2));
            put("RUNNING", Integer.valueOf(4));
            put("DRIVING", Integer.valueOf(3));
            put("SLEEPING", Integer.valueOf(1));
            put("POCKET", Integer.valueOf(10));
        }
    };
    private UserAction mCurDeviceState;
    private UserAction mCurUserActivity;
    private final ICoreContext mICoreContext;
    private MotionDetection mMotionDetection;
    private long mUserStationaryStart = 0;

    private Integer getMsgId(String[] state) {
        if (state != null && state.length >= 2) {
            return (Integer) mUserStatesIds.get(state[0]);
        }
        Log.e("UserStateManager", "state is invalid!");
        return null;
    }

    public UserStateManager(ICoreContext context) {
        this.mICoreContext = context;
        this.mMotionDetection = new MotionDetection(this.mICoreContext.getContext(), this);
    }

    public void onInputMsgEvent(MsgEvent evt) {
        if (evt.getEventId() == 362) {
            MsgEvent userEvent = evt;
            Intent intent = evt.getIntent();
            if (intent != null) {
                String[] state = intent.getStringArrayExtra("user_device_app_state");
                Integer msgId = getMsgId(state);
                if (msgId != null) {
                    long ts = evt.getTimeStamp();
                    UserAction action = createUserStateAction(msgId.intValue(), ts, 1);
                    if (action == null) {
                        action = createDeviceStateAction(msgId.intValue(), ts, 1);
                    }
                    if (action == null) {
                        Log.w("UserStateManager", "handleEvent unknown user event: " + msgId);
                        return;
                    }
                    notifyPowerActionChanged(this.mICoreContext, action);
                    sendNotification(action.getStateName());
                    return;
                }
                Log.e("UserStateManager", "unknown user state:" + state);
            }
        }
    }

    private UserAction createUserStateAction(int userEventId, long ts, int subState) {
        UserAction action;
        switch (userEventId) {
            case NativeAdapter.PLATFORM_MTK /*1*/:
                action = new UserAction(1, ts, "SLEEPING", subState, 1);
                break;
            case NativeAdapter.PLATFORM_HI /*2*/:
                action = new UserAction(2, ts, "WALKING", subState, 1);
                break;
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
                action = new UserAction(3, ts, "DRIVING", subState, 1);
                break;
            case 4:
                action = new UserAction(4, ts, "RUNNING", subState, 1);
                break;
            case 5:
                action = new UserAction(5, ts, "STATIONARY", subState, 1);
                break;
            default:
                return null;
        }
        if (action != null) {
            this.mCurUserActivity = action;
        }
        return action;
    }

    private UserAction createDeviceStateAction(int userEventId, long ts, int subState) {
        UserAction action;
        switch (userEventId) {
            case 10:
                action = new UserAction(10, ts, "INPOCKET", subState, 4);
                break;
            case 11:
                action = new UserAction(11, ts, "HOLDING", subState, 4);
                break;
            case 12:
                action = new UserAction(12, ts, "ONTABLE", subState, 4);
                break;
            default:
                return null;
        }
        if (action != null) {
            this.mCurDeviceState = action;
        }
        return action;
    }

    protected void startUserStationary() {
        if (this.mUserStationaryStart > 0) {
            Log.d("UserStateManager", "repeat enter still, do nothing.");
            return;
        }
        this.mUserStationaryStart = SystemClock.elapsedRealtime();
        UserAction action = createUserStateAction(5, 0, 0);
        notifyPowerActionChanged(this.mICoreContext, action);
        sendNotification(action.getStateName());
    }

    protected void stopUserStationary() {
        this.mUserStationaryStart = 0;
        UserAction action = createUserStateAction(2, 0, 1);
        notifyPowerActionChanged(this.mICoreContext, action);
        sendNotification(action.getStateName());
    }

    public boolean startMotionDetection(long reportLatencySec) {
        if (!((IDeviceState) this.mICoreContext.getService("device")).isCtsRunning()) {
            return this.mMotionDetection.startMotionDetection(reportLatencySec);
        }
        Log.i("UserStateManager", "not start motion detection for stc");
        return false;
    }

    public void stopMotionDetection() {
        this.mMotionDetection.stopMotionDetection();
    }

    public long getUserStationaryDuration() {
        if (this.mUserStationaryStart > 0) {
            return SystemClock.elapsedRealtime() - this.mUserStationaryStart;
        }
        return 0;
    }

    public int getUserState() {
        if (this.mCurUserActivity != null) {
            return this.mCurUserActivity.getUserState();
        }
        return 0;
    }

    private void sendNotification(String newState) {
        DbgUtils.sendNotification("CA Changed", newState);
    }
}
