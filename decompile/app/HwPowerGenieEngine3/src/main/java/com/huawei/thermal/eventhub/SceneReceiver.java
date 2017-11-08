package com.huawei.thermal.eventhub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.pgmng.plug.PGSdk;
import com.huawei.pgmng.plug.PGSdk.Sink;
import com.huawei.thermal.event.SceneEvent;
import java.util.ArrayList;

public class SceneReceiver {
    private static final boolean DEBUG;
    private EventListener mCallback = null;
    private Context mContext;
    private final ArrayList<SceneEvent> mFreePool = new ArrayList();
    private PGSdk mPGSdk = null;
    private ArrayList<Integer> mRegActions = new ArrayList();
    private Sink mStateRecognitionListener = new Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            SceneReceiver.this.onSceneChg(stateType, eventType, pid, pkg, uid);
        }
    };
    private BroadcastReceiver mSystemReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null && action.equals("android.intent.action.SCREEN_OFF")) {
                    SceneReceiver.this.onSceneChg(20004, 1, 0, null, 0);
                } else if (action != null && action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    SceneReceiver.this.onSceneChg(20005, 1, 0, null, 0);
                }
            }
        }
    };

    static {
        boolean z = false;
        if (Log.isLoggable("SceneReceiver", 2)) {
            z = true;
        }
        DEBUG = z;
    }

    public SceneReceiver(Context context, EventListener callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public void start() {
        registerAllActions();
        registerSystemBroadcast();
    }

    public boolean registerPGActions(ArrayList<Integer> scenes) {
        synchronized (this.mRegActions) {
            this.mRegActions.clear();
            this.mRegActions.addAll(scenes);
        }
        return true;
    }

    private void registerSystemBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.getApplicationContext().registerReceiver(this.mSystemReceiver, filter);
    }

    public boolean registerAllActions() {
        this.mPGSdk = PGSdk.getInstance();
        if (this.mPGSdk == null) {
            return false;
        }
        try {
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10000);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10001);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10002);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10003);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10004);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10007);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10008);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10009);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10010);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10011);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10013);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10017);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10020);
            this.mPGSdk.enableStateEvent(this.mStateRecognitionListener, 10021);
            return true;
        } catch (RemoteException e) {
            Log.e("SceneReceiver", "Exception e: initialize pgdskd error:", e);
            return false;
        }
    }

    public synchronized void onSceneChg(int stateType, int eventType, int pid, String pkg, int uid) {
        Log.i("SceneReceiver", "state type: " + stateType + " eventType:" + eventType + " pid:" + pid + " uid:" + uid + " pkg:" + pkg);
        if (this.mPGSdk == null) {
            Log.w("SceneReceiver", "scene receiver is not running!");
        } else if (eventType == 1) {
            SceneEvent evt;
            if (!this.mRegActions.contains(Integer.valueOf(stateType))) {
                stateType = (stateType == 10021 || stateType == 10020) ? 10007 : 10000;
            }
            if (this.mFreePool.size() <= 0) {
                if (DEBUG) {
                    Log.i("SceneReceiver", "new SceneEvent ");
                }
                evt = new SceneEvent(stateType, pkg);
            } else {
                evt = (SceneEvent) this.mFreePool.remove(0);
                evt.setEventId(stateType);
                evt.setPkg(pkg);
                evt.resetAs(evt);
            }
            if (DEBUG) {
                Log.v("SceneReceiver", "new SceneEvent:" + evt);
            }
            if (this.mCallback != null) {
                this.mCallback.handleEvent(evt);
            }
            if (3 > this.mFreePool.size()) {
                this.mFreePool.add(evt);
            }
        } else if (eventType == 2) {
            if (DEBUG) {
                Log.i("SceneReceiver", "eventType = " + eventType + ", current scene end do noting");
            }
        }
    }
}
