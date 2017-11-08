package com.huawei.powergenie.core.thermal;

import android.content.Context;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.api.IThermalState;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import java.util.HashMap;

public final class ThermalStateService extends BaseService implements IThermalState {
    private Context mContext;
    private int mCurAppAction = 0;
    private final HashMap<Integer, Integer> mCurTemp = new HashMap();
    private final ISdkService mISdkService;

    public ThermalStateService(ICoreContext context) {
        this.mContext = context.getContext();
        this.mISdkService = (ISdkService) context.getService("sdk");
    }

    public void start() {
    }

    public void onInputHookEvent(HookEvent evt) {
        switch (evt.getEventId()) {
            case 145:
                try {
                    if ("app_action".equals(evt.getPkgName()) && this.mISdkService != null) {
                        int stateType = Integer.parseInt(evt.getValue4());
                        int pid = Integer.parseInt(evt.getValue1());
                        String pkg = evt.getValue2();
                        this.mCurAppAction = Integer.parseInt(pkg);
                        Log.i("ThermalStateService", "thermal app_action step = " + pkg + " temperature = " + pid);
                        this.mISdkService.handleStateChanged(9, stateType, pid, pkg, 0);
                        return;
                    }
                    return;
                } catch (Exception e) {
                    Log.w("ThermalStateService", "handle app_action error: " + e);
                    return;
                }
            case 146:
                int temperature = -100000;
                int sensorType = -1;
                try {
                    if (evt.getValue1() != null) {
                        sensorType = Integer.parseInt(evt.getValue1());
                    }
                    if (evt.getValue2() != null) {
                        temperature = Integer.parseInt(evt.getValue2());
                    }
                } catch (NumberFormatException e2) {
                    Log.e("ThermalStateService", "thermal event : " + evt + ", exception:" + e2);
                }
                if (sensorType == 9 && (temperature <= -1000 || temperature >= 1000)) {
                    temperature /= 1000;
                }
                this.mCurTemp.put(Integer.valueOf(sensorType), Integer.valueOf(temperature));
                return;
            default:
                return;
        }
    }

    public int getThermalTemp(int type) {
        Integer temp = (Integer) this.mCurTemp.get(Integer.valueOf(type));
        return temp == null ? -100000 : temp.intValue();
    }

    public int getCurThermalStep() {
        return this.mCurAppAction;
    }
}
