package com.huawei.powergenie.integration.eventhub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    private static Intent mBootIntent = null;
    private static IHandleMsg mHandleMsg;
    private static boolean mIsNeedReSendBootCompleted = false;

    public interface IHandleMsg {
        void handleMsg(int i, Intent intent);
    }

    protected abstract Integer getMsgId(String str);

    public void onReceive(Context context, Intent intent) {
        Integer msgId = getMsgId(intent.getAction());
        if (msgId != null) {
            dispatchMessage(msgId.intValue(), intent);
        } else {
            Log.e("MsgHub", "unknown intent action:" + intent.getAction());
        }
    }

    private void dispatchMessage(int msgId, Intent intent) {
        if (mHandleMsg == null) {
            mHandleMsg = MsgHub.getIHandleMsg();
        }
        if (mHandleMsg != null) {
            mHandleMsg.handleMsg(msgId, intent);
            if (mIsNeedReSendBootCompleted) {
                mHandleMsg.handleMsg(302, mBootIntent);
                Log.w("MsgHub", "ReSend BootCompleted.");
                mIsNeedReSendBootCompleted = false;
                return;
            }
            return;
        }
        if (302 == msgId) {
            mBootIntent = (Intent) intent.clone();
            mIsNeedReSendBootCompleted = true;
        }
        Log.i("MsgHub", "Not handle msgId: " + msgId);
    }
}
